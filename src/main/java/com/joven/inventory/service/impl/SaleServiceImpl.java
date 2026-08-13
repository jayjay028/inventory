package com.joven.inventory.service.impl;

import com.joven.inventory.audit.AuditContext;
import com.joven.inventory.common.Constants;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CreateSaleRequest;
import com.joven.inventory.dto.request.ProcessPaymentRequest;
import com.joven.inventory.dto.request.SaleItemRequest;
import com.joven.inventory.dto.request.SalePaymentRequest;
import com.joven.inventory.dto.request.VoidSaleRequest;
import com.joven.inventory.dto.response.ReceiptResponse;
import com.joven.inventory.dto.response.SaleDetailResponse;
import com.joven.inventory.dto.response.SaleResponse;
import com.joven.inventory.entity.Customer;
import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Sale;
import com.joven.inventory.entity.SaleAddon;
import com.joven.inventory.entity.SaleItem;
import com.joven.inventory.entity.SalePayment;
import com.joven.inventory.entity.Shift;
import com.joven.inventory.enums.DiscountType;
import com.joven.inventory.enums.DocumentType;
import com.joven.inventory.enums.PaymentMethod;
import com.joven.inventory.enums.SaleStatus;
import com.joven.inventory.enums.ShiftStatus;
import com.joven.inventory.enums.TaxType;
import com.joven.inventory.exception.BusinessRuleException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.SaleMapper;
import com.joven.inventory.repository.CustomerRepository;
import com.joven.inventory.repository.ItemRepository;
import com.joven.inventory.repository.SaleAddonRepository;
import com.joven.inventory.repository.SaleItemRepository;
import com.joven.inventory.repository.SalePaymentRepository;
import com.joven.inventory.repository.SaleRepository;
import com.joven.inventory.repository.ShiftRepository;
import com.joven.inventory.repository.StockRepository;
import com.joven.inventory.service.AppSettingService;
import com.joven.inventory.service.DocumentNumberService;
import com.joven.inventory.service.SaleService;
import com.joven.inventory.service.StockService;
import com.joven.inventory.service.TaxService;
import com.joven.inventory.service.TaxService.TaxResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link SaleService} providing POS sale lifecycle management.
 *
 * <p>Handles sale creation, item updates, payment processing, closing, and voiding.
 * Stock deduction occurs at payment time; stock reversal occurs on void of paid/closed sales.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SaleServiceImpl implements SaleService {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final SaleAddonRepository saleAddonRepository;
    private final SalePaymentRepository salePaymentRepository;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final TaxService taxService;
    private final DocumentNumberService documentNumberService;
    private final AppSettingService appSettingService;
    private final ShiftRepository shiftRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SaleDetailResponse createSale(CreateSaleRequest request) {
        String currentUser = AuditContext.getCurrentUser();

        // Validate open shift
        Shift shift = shiftRepository.findByCashierAndStatus(currentUser, ShiftStatus.OPEN)
                .orElseThrow(() -> new BusinessRuleException(
                        "No open shift found for user '" + currentUser + "'. Please open a shift before creating a sale."));

        // Generate sale number
        String saleNo = generateSaleNumber();

        // Validate customer if provided
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found with id: " + request.getCustomerId()));
        }

        // Create sale
        Sale sale = new Sale();
        sale.setSaleNo(saleNo);
        sale.setCustomer(customer);
        sale.setShift(shift);
        sale.setStatus(SaleStatus.OPEN);
        sale.setCreatedBy(currentUser);
        sale.setSaleDate(LocalDateTime.now());
        sale.setDiscountType(request.getDiscountType());
        sale.setDiscountValue(request.getDiscountValue());
        sale.setTaxEnabled(request.getTaxEnabled());
        sale.setDocumentType(request.getDocumentType());
        sale.setRemarks(request.getRemarks());

        // Process items and calculate totals
        sale = saleRepository.save(sale);
        List<SaleItem> saleItems = processItems(sale, request.getItems());

        // Calculate subtotal
        BigDecimal subtotal = saleItems.stream()
                .map(SaleItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        sale.setSubtotal(subtotal);

        // Calculate transaction discount
        BigDecimal discountAmount = calculateTransactionDiscount(
                request.getDiscountType(), request.getDiscountValue(), subtotal);
        sale.setDiscountAmount(discountAmount);

        // Process addons
        List<SaleAddon> saleAddons = processAddons(sale, request.getAddons());
        BigDecimal addonsTotal = saleAddons.stream()
                .map(SaleAddon::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        sale.setAddonsTotal(addonsTotal);

        // Net amount
        BigDecimal netAmount = subtotal.subtract(discountAmount).add(addonsTotal)
                .setScale(2, RoundingMode.HALF_UP);
        sale.setNetAmount(netAmount);

        // Tax calculation
        if (Boolean.TRUE.equals(request.getTaxEnabled())) {
            TaxType taxType = getTaxType(request.getDiscountType());
            TaxResult taxResult = taxService.calculateTax(netAmount, true, taxType);
            sale.setTaxType(taxResult.taxType());
            sale.setTaxRate(taxResult.taxRate());
            sale.setTaxAmount(taxResult.taxAmount());
            sale.setVatableAmount(taxResult.vatableAmount());
            sale.setTotalAmount(taxResult.totalAmount());
        } else {
            sale.setTaxType(TaxType.EXEMPT);
            sale.setTaxRate(BigDecimal.ZERO);
            sale.setTaxAmount(BigDecimal.ZERO);
            sale.setVatableAmount(BigDecimal.ZERO);
            sale.setTotalAmount(netAmount);
        }

        sale = saleRepository.save(sale);

        log.info("Created sale '{}' with {} items, total={}", saleNo, saleItems.size(), sale.getTotalAmount());

        return SaleMapper.toDetailResponse(sale, saleItems, saleAddons, Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SaleDetailResponse updateItems(Long saleId, List<SaleItemRequest> items) {
        Sale sale = findSaleById(saleId);
        validateStatus(sale, SaleStatus.OPEN, "update items on");

        // Delete existing items
        saleItemRepository.deleteBySaleId(saleId);

        // Recreate items
        List<SaleItem> saleItems = processItems(sale, items);

        // Recalculate subtotal
        BigDecimal subtotal = saleItems.stream()
                .map(SaleItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        sale.setSubtotal(subtotal);

        // Recalculate transaction discount
        BigDecimal discountAmount = calculateTransactionDiscount(
                sale.getDiscountType(), sale.getDiscountValue(), subtotal);
        sale.setDiscountAmount(discountAmount);

        // Net amount (addons already on sale)
        BigDecimal netAmount = subtotal.subtract(discountAmount).add(sale.getAddonsTotal())
                .setScale(2, RoundingMode.HALF_UP);
        sale.setNetAmount(netAmount);

        // Recalculate tax
        if (Boolean.TRUE.equals(sale.getTaxEnabled())) {
            TaxType taxType = getTaxType(sale.getDiscountType());
            TaxResult taxResult = taxService.calculateTax(netAmount, true, taxType);
            sale.setTaxType(taxResult.taxType());
            sale.setTaxRate(taxResult.taxRate());
            sale.setTaxAmount(taxResult.taxAmount());
            sale.setVatableAmount(taxResult.vatableAmount());
            sale.setTotalAmount(taxResult.totalAmount());
        } else {
            sale.setTotalAmount(netAmount);
        }

        sale = saleRepository.save(sale);

        List<SaleAddon> addons = saleAddonRepository.findBySaleId(saleId);
        List<SalePayment> payments = salePaymentRepository.findBySaleId(saleId);

        log.info("Updated items on sale '{}', new total={}", sale.getSaleNo(), sale.getTotalAmount());

        return SaleMapper.toDetailResponse(sale, saleItems, addons, payments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SaleDetailResponse processPayment(Long saleId, ProcessPaymentRequest request) {
        Sale sale = findSaleById(saleId);
        validateStatus(sale, SaleStatus.OPEN, "process payment on");

        BigDecimal totalAmount = sale.getTotalAmount();

        // Validate payment amount
        if (request.getAmountTendered().compareTo(totalAmount) < 0) {
            throw new BusinessRuleException(
                    "Amount tendered (" + request.getAmountTendered() + ") is less than total amount (" + totalAmount + ").");
        }

        // Set payment info on sale
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setAmountTendered(request.getAmountTendered());
        sale.setChangeAmount(request.getAmountTendered().subtract(totalAmount).setScale(2, RoundingMode.HALF_UP));

        // Create payment records
        List<SalePayment> payments;
        if (request.getPaymentMethod() == PaymentMethod.MULTIPLE) {
            // Validate sum of split payments covers total
            List<SalePaymentRequest> paymentRequests = request.getPayments();
            if (paymentRequests == null || paymentRequests.isEmpty()) {
                throw new BusinessRuleException("Split payments list is required when payment method is MULTIPLE.");
            }
            BigDecimal paymentsSum = paymentRequests.stream()
                    .map(SalePaymentRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (paymentsSum.compareTo(totalAmount) < 0) {
                throw new BusinessRuleException(
                        "Sum of split payments (" + paymentsSum + ") is less than total amount (" + totalAmount + ").");
            }
            payments = savePayments(sale, paymentRequests);
        } else {
            // Single payment record
            SalePayment payment = new SalePayment();
            payment.setSale(sale);
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setAmount(request.getAmountTendered());
            payment.setReferenceNo(request.getReferenceNo());
            payments = List.of(salePaymentRepository.save(payment));
        }

        // Generate document number if applicable
        DocumentType documentType = sale.getDocumentType();
        if (documentType != null && documentType != DocumentType.NONE) {
            String documentNo = documentNumberService.generateNextNumber(documentType);
            sale.setDocumentNo(documentNo);
        }

        // Update status to PAID
        sale.setStatus(SaleStatus.PAID);

        // Deduct stock for each item
        List<SaleItem> saleItems = saleItemRepository.findBySaleId(saleId);
        for (SaleItem saleItem : saleItems) {
            stockService.deductStock(saleItem.getItem().getId(), saleItem.getQuantity());
        }

        sale = saleRepository.save(sale);

        List<SaleAddon> addons = saleAddonRepository.findBySaleId(saleId);

        log.info("Processed payment for sale '{}', method={}, amount={}", sale.getSaleNo(),
                request.getPaymentMethod(), request.getAmountTendered());

        return SaleMapper.toDetailResponse(sale, saleItems, addons, payments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SaleDetailResponse closeSale(Long saleId) {
        Sale sale = findSaleById(saleId);
        validateStatus(sale, SaleStatus.PAID, "close");

        sale.setStatus(SaleStatus.CLOSED);
        sale = saleRepository.save(sale);

        List<SaleItem> items = saleItemRepository.findBySaleId(saleId);
        List<SaleAddon> addons = saleAddonRepository.findBySaleId(saleId);
        List<SalePayment> payments = salePaymentRepository.findBySaleId(saleId);

        log.info("Closed sale '{}'", sale.getSaleNo());

        return SaleMapper.toDetailResponse(sale, items, addons, payments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SaleDetailResponse voidSale(Long saleId, VoidSaleRequest request) {
        Sale sale = findSaleById(saleId);

        if (sale.getStatus() == SaleStatus.VOIDED) {
            throw new BusinessRuleException("Sale '" + sale.getSaleNo() + "' is already VOIDED.");
        }

        SaleStatus previousStatus = sale.getStatus();
        String currentUser = AuditContext.getCurrentUser();

        // Set void info
        sale.setVoidReason(request.getVoidReason());
        sale.setVoidedBy(currentUser);
        sale.setVoidedAt(LocalDateTime.now());

        // Reverse stock if was PAID or CLOSED
        List<SaleItem> saleItems = saleItemRepository.findBySaleId(saleId);
        if (previousStatus == SaleStatus.PAID || previousStatus == SaleStatus.CLOSED) {
            for (SaleItem saleItem : saleItems) {
                stockService.addStock(saleItem.getItem().getId(), saleItem.getQuantity());
            }
            log.info("Reversed stock for voided sale '{}' ({} items)", sale.getSaleNo(), saleItems.size());
        }

        sale.setStatus(SaleStatus.VOIDED);
        sale = saleRepository.save(sale);

        List<SaleAddon> addons = saleAddonRepository.findBySaleId(saleId);
        List<SalePayment> payments = salePaymentRepository.findBySaleId(saleId);

        log.info("Voided sale '{}' (was {}), reason: {}", sale.getSaleNo(), previousStatus, request.getVoidReason());

        return SaleMapper.toDetailResponse(sale, saleItems, addons, payments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SaleDetailResponse getById(Long id) {
        Sale sale = findSaleById(id);
        List<SaleItem> items = saleItemRepository.findBySaleId(id);
        List<SaleAddon> addons = saleAddonRepository.findBySaleId(id);
        List<SalePayment> payments = salePaymentRepository.findBySaleId(id);
        return SaleMapper.toDetailResponse(sale, items, addons, payments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<SaleResponse> getAll(Pageable pageable) {
        Page<Sale> page = saleRepository.findAll(pageable);
        return SaleMapper.toPageResponse(page, saleItemRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<SaleResponse> getByStatus(SaleStatus status, Pageable pageable) {
        Page<Sale> page = saleRepository.findByStatus(status, pageable);
        return SaleMapper.toPageResponse(page, saleItemRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<SaleResponse> getByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Page<Sale> page = saleRepository.findBySaleDateBetween(from, to, pageable);
        return SaleMapper.toPageResponse(page, saleItemRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReceiptResponse getReceipt(Long saleId) {
        Sale sale = findSaleById(saleId);

        if (sale.getStatus() != SaleStatus.PAID && sale.getStatus() != SaleStatus.CLOSED) {
            throw new BusinessRuleException(
                    "Receipt is only available for PAID or CLOSED sales. Current status: " + sale.getStatus().name());
        }

        List<SaleItem> items = saleItemRepository.findBySaleId(saleId);
        List<SalePayment> payments = salePaymentRepository.findBySaleId(saleId);
        Map<String, String> settings = appSettingService.getAll();

        return SaleMapper.toReceiptResponse(sale, items, payments, settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SaleResponse> getOpenSales() {
        List<Sale> openSales = saleRepository.findByStatus(SaleStatus.OPEN, Pageable.unpaged()).getContent();
        return openSales.stream()
                .map(sale -> {
                    int itemCount = saleItemRepository.findBySaleId(sale.getId()).size();
                    return SaleMapper.toResponse(sale, itemCount);
                })
                .collect(Collectors.toList());
    }

    // --- Private helper methods ---

    /**
     * Finds a sale by ID or throws ResourceNotFoundException.
     */
    private Sale findSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
    }

    /**
     * Validates that a sale is in the expected status.
     */
    private void validateStatus(Sale sale, SaleStatus expectedStatus, String action) {
        if (sale.getStatus() != expectedStatus) {
            throw new BusinessRuleException(
                    "Cannot " + action + " sale '" + sale.getSaleNo() + "'. Expected status: "
                            + expectedStatus.name() + ", actual: " + sale.getStatus().name());
        }
    }

    /**
     * Generates the next POS sale number using app settings.
     * Format: {prefix}{YYYYMM}-{NNNNN}
     */
    private String generateSaleNumber() {
        String prefix = appSettingService.getValueOrDefault("pos_receipt_prefix", "POS-");
        int nextNumber = appSettingService.getIntValue("pos_next_number", 1);
        String yearMonth = LocalDateTime.now().format(YEAR_MONTH_FORMATTER);
        String saleNo = String.format(Constants.DOCUMENT_NUMBER_FORMAT, prefix, yearMonth, nextNumber);

        // Increment the next number
        appSettingService.updateValue("pos_next_number", String.valueOf(nextNumber + 1), AuditContext.getCurrentUser());

        return saleNo;
    }

    /**
     * Processes sale item requests into persisted SaleItem entities.
     */
    private List<SaleItem> processItems(Sale sale, List<SaleItemRequest> itemRequests) {
        List<SaleItem> saleItems = new ArrayList<>();

        for (SaleItemRequest itemRequest : itemRequests) {
            Item item = itemRepository.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Item not found with id: " + itemRequest.getItemId()));

            if (!Boolean.TRUE.equals(item.getActive())) {
                throw new BusinessRuleException(
                        "Item '" + item.getName() + "' is inactive and cannot be added to a sale.");
            }

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setItem(item);
            saleItem.setItemName(item.getName());
            saleItem.setItemCode(item.getItemCode());
            saleItem.setQuantity(itemRequest.getQuantity());
            saleItem.setUnitPrice(itemRequest.getUnitPrice());
            saleItem.setUnitCost(item.getCostPrice());

            // Line-level discount
            DiscountType lineDiscountType = itemRequest.getDiscountType() != null
                    ? itemRequest.getDiscountType() : DiscountType.NONE;
            BigDecimal lineDiscountValue = itemRequest.getDiscountValue() != null
                    ? itemRequest.getDiscountValue() : BigDecimal.ZERO;
            saleItem.setDiscountType(lineDiscountType);
            saleItem.setDiscountValue(lineDiscountValue);

            BigDecimal qty = BigDecimal.valueOf(itemRequest.getQuantity());
            BigDecimal unitPrice = itemRequest.getUnitPrice();
            BigDecimal lineDiscountAmount = calculateLineDiscount(lineDiscountType, lineDiscountValue, qty, unitPrice);
            saleItem.setDiscountAmount(lineDiscountAmount);

            BigDecimal lineTotal = qty.multiply(unitPrice).subtract(lineDiscountAmount)
                    .setScale(2, RoundingMode.HALF_UP);
            saleItem.setLineTotal(lineTotal);

            saleItems.add(saleItemRepository.save(saleItem));
        }

        return saleItems;
    }

    /**
     * Calculates line-level discount amount.
     */
    private BigDecimal calculateLineDiscount(DiscountType discountType, BigDecimal discountValue,
                                             BigDecimal quantity, BigDecimal unitPrice) {
        return switch (discountType) {
            case FIXED -> discountValue.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
            case PERCENTAGE -> quantity.multiply(unitPrice)
                    .multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case NONE, SENIOR_PWD -> BigDecimal.ZERO;
        };
    }

    /**
     * Calculates transaction-level discount amount.
     */
    private BigDecimal calculateTransactionDiscount(DiscountType discountType, BigDecimal discountValue,
                                                    BigDecimal subtotal) {
        return switch (discountType) {
            case NONE -> BigDecimal.ZERO;
            case FIXED -> discountValue.setScale(2, RoundingMode.HALF_UP);
            case PERCENTAGE -> subtotal.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case SENIOR_PWD -> subtotal.multiply(Constants.SENIOR_PWD_DISCOUNT_RATE)
                    .setScale(2, RoundingMode.HALF_UP);
        };
    }

    /**
     * Processes sale addon requests into persisted SaleAddon entities.
     */
    private List<SaleAddon> processAddons(Sale sale, List<CreateSaleRequest.SaleAddonRequest> addonRequests) {
        if (addonRequests == null || addonRequests.isEmpty()) {
            return Collections.emptyList();
        }

        List<SaleAddon> addons = new ArrayList<>();
        for (CreateSaleRequest.SaleAddonRequest addonRequest : addonRequests) {
            SaleAddon addon = new SaleAddon();
            addon.setSale(sale);
            addon.setAddonName(addonRequest.getAddonName());
            addon.setAmount(addonRequest.getAmount());
            addons.add(saleAddonRepository.save(addon));
        }
        return addons;
    }

    /**
     * Saves split payment records for MULTIPLE payment method.
     */
    private List<SalePayment> savePayments(Sale sale, List<SalePaymentRequest> paymentRequests) {
        List<SalePayment> payments = new ArrayList<>();
        for (SalePaymentRequest paymentRequest : paymentRequests) {
            SalePayment payment = new SalePayment();
            payment.setSale(sale);
            payment.setPaymentMethod(paymentRequest.getPaymentMethod());
            payment.setAmount(paymentRequest.getAmount());
            payment.setReferenceNo(paymentRequest.getReferenceNo());
            payments.add(salePaymentRepository.save(payment));
        }
        return payments;
    }

    /**
     * Determines the tax type based on the discount type.
     * SENIOR_PWD discount implies tax-exempt status.
     *
     * @param discountType the discount type
     * @return the appropriate tax type
     */
    private TaxType getTaxType(DiscountType discountType) {
        if (discountType == DiscountType.SENIOR_PWD) {
            return TaxType.EXEMPT;
        }
        return TaxType.VAT;
    }
}
