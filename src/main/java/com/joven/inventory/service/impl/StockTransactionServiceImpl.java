package com.joven.inventory.service.impl;

import com.joven.inventory.audit.AuditContext;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.StockAdjustRequest;
import com.joven.inventory.dto.request.StockInRequest;
import com.joven.inventory.dto.request.StockOutRequest;
import com.joven.inventory.dto.response.StockTransactionResponse;
import com.joven.inventory.entity.Customer;
import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Stock;
import com.joven.inventory.entity.StockTransaction;
import com.joven.inventory.entity.Supplier;
import com.joven.inventory.entity.TransactionAddon;
import com.joven.inventory.enums.DiscountType;
import com.joven.inventory.enums.DocumentType;
import com.joven.inventory.enums.TaxType;
import com.joven.inventory.enums.TransactionStatus;
import com.joven.inventory.enums.TransactionType;
import com.joven.inventory.exception.BusinessRuleException;
import com.joven.inventory.exception.InsufficientStockException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.StockTransactionMapper;
import com.joven.inventory.repository.CustomerRepository;
import com.joven.inventory.repository.ItemRepository;
import com.joven.inventory.repository.StockRepository;
import com.joven.inventory.repository.StockTransactionRepository;
import com.joven.inventory.repository.SupplierRepository;
import com.joven.inventory.repository.TransactionAddonRepository;
import com.joven.inventory.service.AppSettingService;
import com.joven.inventory.service.DocumentNumberService;
import com.joven.inventory.service.StockService;
import com.joven.inventory.service.StockTransactionService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link StockTransactionService} providing stock transaction
 * lifecycle management including creation, approval, and cancellation.
 *
 * <p>Handles financial calculations (subtotal, discount, tax, add-ons) for stock-in
 * and stock-out transactions. Stock levels are only modified upon approval.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StockTransactionServiceImpl implements StockTransactionService {

    private final StockTransactionRepository stockTransactionRepository;
    private final TransactionAddonRepository transactionAddonRepository;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final TaxService taxService;
    private final DocumentNumberService documentNumberService;
    private final AppSettingService appSettingService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StockTransactionResponse createStockIn(StockInRequest request) {
        Item item = findActiveItem(request.getItemId());

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Supplier not found with id: " + request.getSupplierId()));
        }

        StockTransaction transaction = new StockTransaction();
        transaction.setItem(item);
        transaction.setTransactionType(TransactionType.IN);
        transaction.setStatus(TransactionStatus.CREATED);
        transaction.setQuantity(request.getQuantity());
        transaction.setUnitCost(request.getUnitCost());
        transaction.setSupplier(supplier);
        transaction.setReferenceNo(request.getReferenceNo());
        transaction.setRemarks(request.getRemarks());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCreatedBy(AuditContext.getCurrentUser());

        // Calculate subtotal
        BigDecimal subtotal = request.getUnitCost()
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        transaction.setSubtotal(subtotal);

        // No discount for stock-in
        transaction.setDiscountType(DiscountType.NONE);
        transaction.setDiscountValue(BigDecimal.ZERO);
        transaction.setDiscountAmount(BigDecimal.ZERO);

        // Process add-ons
        BigDecimal addonsTotal = calculateAddonsTotal(request.getAddons());

        // Net amount = subtotal + addons total
        BigDecimal netAmount = subtotal.add(addonsTotal).setScale(2, RoundingMode.HALF_UP);
        transaction.setNetAmount(netAmount);

        // Tax calculation
        Boolean taxEnabled = request.getTaxEnabled() != null && request.getTaxEnabled();
        transaction.setTaxEnabled(taxEnabled);
        if (taxEnabled) {
            TaxType taxType = Boolean.TRUE.equals(item.getTaxable()) ? TaxType.VAT : TaxType.EXEMPT;
            TaxResult taxResult = taxService.calculateTax(netAmount, true, taxType);
            transaction.setTaxType(taxResult.taxType());
            transaction.setTaxRate(taxResult.taxRate());
            transaction.setTaxAmount(taxResult.taxAmount());
            transaction.setVatableAmount(taxResult.vatableAmount());
            transaction.setTotalAmount(taxResult.totalAmount());
        } else {
            transaction.setTotalAmount(netAmount);
        }

        // Document number generation
        DocumentType documentType = request.getDocumentType();
        if (documentType != null && documentType != DocumentType.NONE) {
            transaction.setDocumentType(documentType);
            String documentNo = documentNumberService.generateNextNumber(documentType);
            transaction.setDocumentNo(documentNo);
        }

        // Save transaction
        transaction = stockTransactionRepository.save(transaction);

        // Save add-ons
        List<TransactionAddon> savedAddons = saveAddons(transaction, request.getAddons());

        log.info("Created stock-in transaction ID {} for item '{}', quantity={}",
                transaction.getId(), item.getName(), request.getQuantity());

        return StockTransactionMapper.toResponse(transaction, savedAddons);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StockTransactionResponse createStockOut(StockOutRequest request) {
        Item item = findActiveItem(request.getItemId());

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found with id: " + request.getCustomerId()));
        }

        // Validate sufficient stock (validation only — actual deduction on approval)
        Stock stock = stockRepository.findByItemId(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found for item ID: " + request.getItemId()));
        if (stock.getQuantityOnHand() < request.getQuantity()) {
            throw new InsufficientStockException(
                    request.getItemId(), stock.getQuantityOnHand(), request.getQuantity());
        }

        StockTransaction transaction = new StockTransaction();
        transaction.setItem(item);
        transaction.setTransactionType(TransactionType.OUT);
        transaction.setStatus(TransactionStatus.CREATED);
        transaction.setQuantity(request.getQuantity());
        transaction.setUnitPrice(request.getUnitPrice());
        transaction.setCustomer(customer);
        transaction.setReferenceNo(request.getReferenceNo());
        transaction.setRemarks(request.getRemarks());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCreatedBy(AuditContext.getCurrentUser());

        // Calculate subtotal
        BigDecimal subtotal = request.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        transaction.setSubtotal(subtotal);

        // Calculate discount
        DiscountType discountType = request.getDiscountType() != null ? request.getDiscountType() : DiscountType.NONE;
        BigDecimal discountValue = request.getDiscountValue() != null ? request.getDiscountValue() : BigDecimal.ZERO;
        BigDecimal discountAmount = calculateDiscount(discountType, discountValue, subtotal, request.getQuantity());

        transaction.setDiscountType(discountType);
        transaction.setDiscountValue(discountValue);
        transaction.setDiscountAmount(discountAmount);

        // Process add-ons
        BigDecimal addonsTotal = calculateAddonsTotal(request.getAddons());

        // Net amount = subtotal - discount + addons
        BigDecimal netAmount = subtotal.subtract(discountAmount).add(addonsTotal).setScale(2, RoundingMode.HALF_UP);
        transaction.setNetAmount(netAmount);

        // Tax calculation
        Boolean taxEnabled = request.getTaxEnabled() != null && request.getTaxEnabled();
        transaction.setTaxEnabled(taxEnabled);
        if (taxEnabled) {
            TaxType taxType = Boolean.TRUE.equals(item.getTaxable()) ? TaxType.VAT : TaxType.EXEMPT;
            TaxResult taxResult = taxService.calculateTax(netAmount, true, taxType);
            transaction.setTaxType(taxResult.taxType());
            transaction.setTaxRate(taxResult.taxRate());
            transaction.setTaxAmount(taxResult.taxAmount());
            transaction.setVatableAmount(taxResult.vatableAmount());
            transaction.setTotalAmount(taxResult.totalAmount());
        } else {
            transaction.setTotalAmount(netAmount);
        }

        // Document number generation
        DocumentType documentType = request.getDocumentType();
        if (documentType != null && documentType != DocumentType.NONE) {
            transaction.setDocumentType(documentType);
            String documentNo = documentNumberService.generateNextNumber(documentType);
            transaction.setDocumentNo(documentNo);
        }

        // Save transaction
        transaction = stockTransactionRepository.save(transaction);

        // Save add-ons
        List<TransactionAddon> savedAddons = saveAddons(transaction, request.getAddons());

        log.info("Created stock-out transaction ID {} for item '{}', quantity={}",
                transaction.getId(), item.getName(), request.getQuantity());

        return StockTransactionMapper.toResponse(transaction, savedAddons);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StockTransactionResponse createStockAdjust(StockAdjustRequest request) {
        Item item = findActiveItem(request.getItemId());

        StockTransaction transaction = new StockTransaction();
        transaction.setItem(item);
        transaction.setTransactionType(TransactionType.ADJUSTMENT);
        transaction.setStatus(TransactionStatus.CREATED);
        transaction.setQuantity(request.getQuantity());
        transaction.setRemarks(request.getRemarks());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCreatedBy(AuditContext.getCurrentUser());

        // All financial fields remain at their defaults (BigDecimal.ZERO)
        transaction.setDiscountType(DiscountType.NONE);

        // Save transaction
        transaction = stockTransactionRepository.save(transaction);

        log.info("Created stock adjustment transaction ID {} for item '{}', quantity={}",
                transaction.getId(), item.getName(), request.getQuantity());

        return StockTransactionMapper.toResponse(transaction, Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StockTransactionResponse approve(Long id) {
        StockTransaction transaction = stockTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock transaction not found with id: " + id));

        if (transaction.getStatus() != TransactionStatus.CREATED) {
            throw new BusinessRuleException(
                    "Cannot approve transaction with status: " + transaction.getStatus().name()
                            + ". Only CREATED transactions can be approved.");
        }

        transaction.setStatus(TransactionStatus.APPROVED);
        transaction.setApprovedBy(AuditContext.getCurrentUser());
        transaction.setApprovedAt(LocalDateTime.now());

        // Update stock based on transaction type
        Long itemId = transaction.getItem().getId();
        int quantity = transaction.getQuantity();

        switch (transaction.getTransactionType()) {
            case IN -> stockService.addStock(itemId, quantity);
            case OUT -> stockService.deductStock(itemId, quantity);
            case ADJUSTMENT -> stockService.setStock(itemId, quantity);
        }

        transaction = stockTransactionRepository.save(transaction);

        List<TransactionAddon> addons = transactionAddonRepository.findByTransactionId(id);

        log.info("Approved stock transaction ID {} (type={}, item={}, quantity={})",
                id, transaction.getTransactionType(), itemId, quantity);

        return StockTransactionMapper.toResponse(transaction, addons);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StockTransactionResponse cancel(Long id) {
        StockTransaction transaction = stockTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock transaction not found with id: " + id));

        if (transaction.getStatus() != TransactionStatus.CREATED) {
            throw new BusinessRuleException(
                    "Cannot cancel transaction with status: " + transaction.getStatus().name()
                            + ". Only CREATED transactions can be cancelled.");
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction = stockTransactionRepository.save(transaction);

        List<TransactionAddon> addons = transactionAddonRepository.findByTransactionId(id);

        log.info("Cancelled stock transaction ID {}", id);

        return StockTransactionMapper.toResponse(transaction, addons);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<StockTransactionResponse> getAll(Pageable pageable) {
        Page<StockTransaction> page = stockTransactionRepository.findAll(pageable);
        return StockTransactionMapper.toTransactionPageResponse(page, transactionAddonRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StockTransactionResponse getById(Long id) {
        StockTransaction transaction = stockTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock transaction not found with id: " + id));

        List<TransactionAddon> addons = transactionAddonRepository.findByTransactionId(id);
        return StockTransactionMapper.toResponse(transaction, addons);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<StockTransactionResponse> getPending(Pageable pageable) {
        Page<StockTransaction> page = stockTransactionRepository.findPendingApproval(pageable);
        return StockTransactionMapper.toTransactionPageResponse(page, transactionAddonRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<StockTransactionResponse> getByDateRange(LocalDateTime from, LocalDateTime to,
                                                                  Pageable pageable) {
        Page<StockTransaction> page = stockTransactionRepository.findByTransactionDateBetween(from, to, pageable);
        return StockTransactionMapper.toTransactionPageResponse(page, transactionAddonRepository);
    }

    // --- Private helper methods ---

    /**
     * Finds an item by ID and validates it is active.
     */
    private Item findActiveItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (!Boolean.TRUE.equals(item.getActive())) {
            throw new BusinessRuleException("Item '" + item.getName() + "' is inactive and cannot be used in transactions.");
        }

        return item;
    }

    /**
     * Calculates the total of all add-on amounts from the request.
     */
    private BigDecimal calculateAddonsTotal(List<? extends Object> addons) {
        if (addons == null || addons.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Object addon : addons) {
            BigDecimal amount;
            if (addon instanceof StockInRequest.AddonRequest inAddon) {
                amount = inAddon.getAmount();
            } else if (addon instanceof StockOutRequest.AddonRequest outAddon) {
                amount = outAddon.getAmount();
            } else {
                continue;
            }
            total = total.add(amount);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates discount amount based on discount type.
     */
    private BigDecimal calculateDiscount(DiscountType discountType, BigDecimal discountValue,
                                         BigDecimal subtotal, int quantity) {
        return switch (discountType) {
            case FIXED -> discountValue.multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);
            case PERCENTAGE -> subtotal.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case NONE, SENIOR_PWD -> BigDecimal.ZERO;
        };
    }

    /**
     * Saves add-on records for a stock transaction.
     */
    private List<TransactionAddon> saveAddons(StockTransaction transaction, List<? extends Object> addons) {
        if (addons == null || addons.isEmpty()) {
            return Collections.emptyList();
        }

        List<TransactionAddon> savedAddons = new ArrayList<>();
        for (Object addon : addons) {
            String addonName;
            BigDecimal amount;
            if (addon instanceof StockInRequest.AddonRequest inAddon) {
                addonName = inAddon.getAddonName();
                amount = inAddon.getAmount();
            } else if (addon instanceof StockOutRequest.AddonRequest outAddon) {
                addonName = outAddon.getAddonName();
                amount = outAddon.getAmount();
            } else {
                continue;
            }

            TransactionAddon entity = new TransactionAddon();
            entity.setTransaction(transaction);
            entity.setAddonName(addonName);
            entity.setAmount(amount);
            savedAddons.add(transactionAddonRepository.save(entity));
        }
        return savedAddons;
    }
}
