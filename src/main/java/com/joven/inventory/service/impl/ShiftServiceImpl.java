package com.joven.inventory.service.impl;

import com.joven.inventory.audit.AuditContext;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CloseShiftRequest;
import com.joven.inventory.dto.request.OpenShiftRequest;
import com.joven.inventory.dto.response.ShiftResponse;
import com.joven.inventory.dto.response.ShiftSummaryResponse;
import com.joven.inventory.entity.Sale;
import com.joven.inventory.entity.SalePayment;
import com.joven.inventory.entity.Shift;
import com.joven.inventory.enums.PaymentMethod;
import com.joven.inventory.enums.SaleStatus;
import com.joven.inventory.enums.ShiftStatus;
import com.joven.inventory.exception.BusinessRuleException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.ShiftMapper;
import com.joven.inventory.repository.SalePaymentRepository;
import com.joven.inventory.repository.SaleRepository;
import com.joven.inventory.repository.ShiftRepository;
import com.joven.inventory.service.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of {@link ShiftService} providing cashier shift lifecycle management.
 *
 * <p>Handles shift opening with cash-on-hand, and closing with full reconciliation
 * including sales totals, voided amounts, payment breakdowns, and cash variance.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final SaleRepository saleRepository;
    private final SalePaymentRepository salePaymentRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ShiftResponse openShift(OpenShiftRequest request) {
        String currentUser = AuditContext.getCurrentUser();

        // Check if user already has an open shift
        shiftRepository.findByCashierAndStatus(currentUser, ShiftStatus.OPEN)
                .ifPresent(existing -> {
                    throw new BusinessRuleException(
                            "User '" + currentUser + "' already has an open shift (ID: " + existing.getId() + "). "
                                    + "Please close it before opening a new one.");
                });

        Shift shift = new Shift();
        shift.setCashier(currentUser);
        shift.setOpeningAmount(request.getOpeningAmount());
        shift.setStatus(ShiftStatus.OPEN);
        shift.setOpenedAt(LocalDateTime.now());

        shift = shiftRepository.save(shift);

        log.info("Opened shift ID {} for user '{}', opening amount={}", shift.getId(), currentUser, request.getOpeningAmount());

        return ShiftMapper.toResponse(shift);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ShiftSummaryResponse closeShift(Long shiftId, CloseShiftRequest request) {
        String currentUser = AuditContext.getCurrentUser();

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + shiftId));

        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new BusinessRuleException("Cannot close shift ID " + shiftId + ". Shift is already CLOSED.");
        }

        // Validate shift belongs to current user (allow admin override by not throwing if admin)
        if (!shift.getCashier().equals(currentUser)) {
            throw new BusinessRuleException(
                    "Cannot close shift ID " + shiftId + ". Shift belongs to '" + shift.getCashier()
                            + "', not '" + currentUser + "'.");
        }

        // Get all sales for this shift
        List<Sale> shiftSales = saleRepository.findByShiftId(shiftId);

        // Calculate totalSales = sum of totalAmount for PAID/CLOSED sales
        BigDecimal totalSales = shiftSales.stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate totalTransactions = count of PAID/CLOSED sales
        int totalTransactions = (int) shiftSales.stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .count();

        // Calculate totalVoided = sum of totalAmount for VOIDED sales
        BigDecimal totalVoided = shiftSales.stream()
                .filter(s -> s.getStatus() == SaleStatus.VOIDED)
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Get payment breakdowns from sale payments for PAID/CLOSED sales in this shift
        List<Long> completedSaleIds = shiftSales.stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .map(Sale::getId)
                .toList();

        BigDecimal cashSales = BigDecimal.ZERO;
        BigDecimal gcashSales = BigDecimal.ZERO;
        BigDecimal bankTransferSales = BigDecimal.ZERO;
        BigDecimal creditSales = BigDecimal.ZERO;
        BigDecimal totalChange = BigDecimal.ZERO;

        for (Long saleId : completedSaleIds) {
            List<SalePayment> payments = salePaymentRepository.findBySaleId(saleId);
            for (SalePayment payment : payments) {
                switch (payment.getPaymentMethod()) {
                    case CASH -> cashSales = cashSales.add(payment.getAmount());
                    case GCASH -> gcashSales = gcashSales.add(payment.getAmount());
                    case BANK_TRANSFER -> bankTransferSales = bankTransferSales.add(payment.getAmount());
                    case CREDIT -> creditSales = creditSales.add(payment.getAmount());
                    case MULTIPLE -> { /* MULTIPLE is broken into individual payment records */ }
                }
            }
        }

        // Calculate total change given (cash sales that needed change)
        for (Sale sale : shiftSales) {
            if (sale.getStatus() == SaleStatus.PAID || sale.getStatus() == SaleStatus.CLOSED) {
                if (sale.getChangeAmount() != null && sale.getChangeAmount().compareTo(BigDecimal.ZERO) > 0) {
                    totalChange = totalChange.add(sale.getChangeAmount());
                }
            }
        }

        // Expected amount = opening + cash received - change given
        BigDecimal expectedAmount = shift.getOpeningAmount()
                .add(cashSales)
                .subtract(totalChange)
                .setScale(2, RoundingMode.HALF_UP);

        // Set closing info
        BigDecimal closingAmount = request.getClosingAmount();
        BigDecimal difference = closingAmount.subtract(expectedAmount).setScale(2, RoundingMode.HALF_UP);

        shift.setClosingAmount(closingAmount);
        shift.setExpectedAmount(expectedAmount);
        shift.setDifference(difference);
        shift.setTotalSales(totalSales);
        shift.setTotalTransactions(totalTransactions);
        shift.setTotalVoided(totalVoided);
        shift.setStatus(ShiftStatus.CLOSED);
        shift.setClosedAt(LocalDateTime.now());
        shift.setRemarks(request.getRemarks());

        shift = shiftRepository.save(shift);

        log.info("Closed shift ID {} for user '{}', totalSales={}, expected={}, actual={}, difference={}",
                shiftId, currentUser, totalSales, expectedAmount, closingAmount, difference);

        return ShiftMapper.toSummaryResponse(shift,
                cashSales.setScale(2, RoundingMode.HALF_UP),
                gcashSales.setScale(2, RoundingMode.HALF_UP),
                bankTransferSales.setScale(2, RoundingMode.HALF_UP),
                creditSales.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShiftResponse getCurrentShift() {
        String currentUser = AuditContext.getCurrentUser();
        Shift shift = shiftRepository.findCurrentOpenShift(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No open shift found for user '" + currentUser + "'."));
        return ShiftMapper.toResponse(shift);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShiftResponse getById(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
        return ShiftMapper.toResponse(shift);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<ShiftResponse> getAll(Pageable pageable) {
        Page<Shift> page = shiftRepository.findAll(pageable);
        return ShiftMapper.toPageResponse(page);
    }
}
