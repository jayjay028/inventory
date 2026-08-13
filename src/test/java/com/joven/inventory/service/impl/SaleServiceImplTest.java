package com.joven.inventory.service.impl;

import com.joven.inventory.audit.AuditContext;
import com.joven.inventory.dto.request.CreateSaleRequest;
import com.joven.inventory.dto.request.ProcessPaymentRequest;
import com.joven.inventory.dto.request.SaleItemRequest;
import com.joven.inventory.dto.request.SalePaymentRequest;
import com.joven.inventory.dto.request.VoidSaleRequest;
import com.joven.inventory.dto.response.SaleDetailResponse;
import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Sale;
import com.joven.inventory.entity.SaleItem;
import com.joven.inventory.entity.SalePayment;
import com.joven.inventory.entity.Shift;
import com.joven.inventory.enums.DiscountType;
import com.joven.inventory.enums.PaymentMethod;
import com.joven.inventory.enums.SaleStatus;
import com.joven.inventory.enums.ShiftStatus;
import com.joven.inventory.enums.TaxType;
import com.joven.inventory.exception.BusinessRuleException;
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
import com.joven.inventory.service.StockService;
import com.joven.inventory.service.TaxService;
import com.joven.inventory.service.TaxService.TaxResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SaleServiceImpl}.
 * Verifies POS sale lifecycle: creation, payment, closing, voiding, and discount calculations.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SaleServiceImpl Unit Tests")
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleItemRepository saleItemRepository;

    @Mock
    private SaleAddonRepository saleAddonRepository;

    @Mock
    private SalePaymentRepository salePaymentRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockService stockService;

    @Mock
    private TaxService taxService;

    @Mock
    private DocumentNumberService documentNumberService;

    @Mock
    private AppSettingService appSettingService;

    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private SaleServiceImpl saleService;

    @BeforeEach
    void setUp() {
        AuditContext.set("testuser", "127.0.0.1");
    }

    @AfterEach
    void tearDown() {
        AuditContext.clear();
    }

    // ========================================================================
    // createSale tests
    // ========================================================================

    @Test
    @DisplayName("createSale - given valid request - creates open sale")
    void createSale_givenValidRequest_createsOpenSale() {
        // Arrange
        Shift shift = createTestShift(1L, "testuser", ShiftStatus.OPEN);
        Item item = createTestItem(1L, "ITM-001", "Test Item", new BigDecimal("500.00"), true);

        when(shiftRepository.findByCashierAndStatus("testuser", ShiftStatus.OPEN))
                .thenReturn(Optional.of(shift));
        when(appSettingService.getValueOrDefault("pos_receipt_prefix", "POS-"))
                .thenReturn("RCT-");
        when(appSettingService.getIntValue("pos_next_number", 1))
                .thenReturn(1);
        lenient().doNothing().when(appSettingService)
                .updateValue(eq("pos_next_number"), anyString(), anyString());
        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        // Mock save to return entity with ID
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            if (sale.getId() == null) {
                sale.setId(1L);
            }
            return sale;
        });
        when(saleItemRepository.save(any(SaleItem.class))).thenAnswer(invocation -> {
            SaleItem saleItem = invocation.getArgument(0);
            saleItem.setId(1L);
            return saleItem;
        });

        SaleItemRequest itemRequest = SaleItemRequest.builder()
                .itemId(1L)
                .quantity(2)
                .unitPrice(new BigDecimal("500.00"))
                .discountType(DiscountType.NONE)
                .discountValue(BigDecimal.ZERO)
                .build();

        CreateSaleRequest request = CreateSaleRequest.builder()
                .items(List.of(itemRequest))
                .discountType(DiscountType.NONE)
                .discountValue(BigDecimal.ZERO)
                .taxEnabled(false)
                .build();

        // Act
        SaleDetailResponse response = saleService.createSale(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SaleStatus.OPEN.name());
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.getSaleNo()).startsWith("RCT-");

        // Verify stock is NOT deducted on creation (only on payment)
        verify(stockService, never()).deductStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("createSale - given no open shift - throws BusinessRuleException")
    void createSale_givenNoOpenShift_throwsBusinessRuleException() {
        // Arrange
        when(shiftRepository.findByCashierAndStatus("testuser", ShiftStatus.OPEN))
                .thenReturn(Optional.empty());

        CreateSaleRequest request = CreateSaleRequest.builder()
                .items(List.of(SaleItemRequest.builder()
                        .itemId(1L)
                        .quantity(1)
                        .unitPrice(new BigDecimal("100.00"))
                        .build()))
                .discountType(DiscountType.NONE)
                .discountValue(BigDecimal.ZERO)
                .taxEnabled(false)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No open shift found");
    }

    @Test
    @DisplayName("createSale - given Senior/PWD discount - applies 20% and tax exempt")
    void createSale_givenSeniorPwdDiscount_applies20PercentAndExempt() {
        // Arrange
        Shift shift = createTestShift(1L, "testuser", ShiftStatus.OPEN);
        Item item = createTestItem(1L, "ITM-001", "Test Item", new BigDecimal("500.00"), true);

        when(shiftRepository.findByCashierAndStatus("testuser", ShiftStatus.OPEN))
                .thenReturn(Optional.of(shift));
        when(appSettingService.getValueOrDefault("pos_receipt_prefix", "POS-"))
                .thenReturn("RCT-");
        when(appSettingService.getIntValue("pos_next_number", 1))
                .thenReturn(1);
        lenient().doNothing().when(appSettingService)
                .updateValue(eq("pos_next_number"), anyString(), anyString());
        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            if (sale.getId() == null) {
                sale.setId(1L);
            }
            return sale;
        });
        when(saleItemRepository.save(any(SaleItem.class))).thenAnswer(invocation -> {
            SaleItem saleItem = invocation.getArgument(0);
            saleItem.setId(1L);
            return saleItem;
        });

        // Mock tax calculation for EXEMPT type
        TaxResult exemptTaxResult = new TaxResult(
                BigDecimal.ZERO,                    // taxAmount
                BigDecimal.ZERO,                    // vatableAmount
                new BigDecimal("800.00"),           // totalAmount (subtotal - 20% discount)
                BigDecimal.ZERO,                    // taxRate
                TaxType.EXEMPT                      // taxType
        );
        when(taxService.calculateTax(any(BigDecimal.class), eq(true), eq(TaxType.EXEMPT)))
                .thenReturn(exemptTaxResult);

        SaleItemRequest itemRequest = SaleItemRequest.builder()
                .itemId(1L)
                .quantity(2)
                .unitPrice(new BigDecimal("500.00"))
                .discountType(DiscountType.NONE)
                .discountValue(BigDecimal.ZERO)
                .build();

        CreateSaleRequest request = CreateSaleRequest.builder()
                .items(List.of(itemRequest))
                .discountType(DiscountType.SENIOR_PWD)
                .discountValue(BigDecimal.ZERO)
                .taxEnabled(true)
                .build();

        // Act
        SaleDetailResponse response = saleService.createSale(request);

        // Assert
        assertThat(response).isNotNull();
        // Subtotal = 2 * 500 = 1000
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
        // Discount = 1000 * 0.20 = 200
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("200.00"));

        // Verify tax was calculated with EXEMPT type (Senior/PWD implies tax-exempt)
        verify(taxService).calculateTax(any(BigDecimal.class), eq(true), eq(TaxType.EXEMPT));
    }

    // ========================================================================
    // processPayment tests
    // ========================================================================

    @Test
    @DisplayName("processPayment - given sufficient amount - sets status to PAID")
    void processPayment_givenSufficientAmount_setsStatusToPaid() {
        // Arrange
        Sale sale = createTestSale(1L, "RCT-202608-00001", SaleStatus.OPEN, new BigDecimal("1000.00"));
        Item item = createTestItem(1L, "ITM-001", "Test Item", new BigDecimal("500.00"), true);
        SaleItem saleItem = createTestSaleItem(1L, sale, item, 2, new BigDecimal("500.00"));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(saleItemRepository.findBySaleId(1L)).thenReturn(List.of(saleItem));
        when(saleAddonRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        when(salePaymentRepository.save(any(SalePayment.class))).thenAnswer(invocation -> {
            SalePayment payment = invocation.getArgument(0);
            payment.setId(1L);
            return payment;
        });
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentNumberService.generateNextNumber(any())).thenReturn("OR-202608-00001");

        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .paymentMethod(PaymentMethod.CASH)
                .amountTendered(new BigDecimal("1500.00"))
                .build();

        // Act
        SaleDetailResponse response = saleService.processPayment(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SaleStatus.PAID.name());
        assertThat(response.getAmountTendered()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(response.getChangeAmount()).isEqualByComparingTo(new BigDecimal("500.00"));

        // Verify stock deducted for each item
        verify(stockService, times(1)).deductStock(eq(1L), eq(2));
    }

    @Test
    @DisplayName("processPayment - given insufficient amount - throws BusinessRuleException")
    void processPayment_givenInsufficientAmount_throwsBusinessRuleException() {
        // Arrange
        Sale sale = createTestSale(1L, "RCT-202608-00001", SaleStatus.OPEN, new BigDecimal("1000.00"));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .paymentMethod(PaymentMethod.CASH)
                .amountTendered(new BigDecimal("500.00"))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> saleService.processPayment(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("less than total amount");
    }

    @Test
    @DisplayName("processPayment - given multiple payment method - saves multiple records")
    void processPayment_givenMultiplePaymentMethod_savesMultipleRecords() {
        // Arrange
        Sale sale = createTestSale(1L, "RCT-202608-00001", SaleStatus.OPEN, new BigDecimal("1000.00"));
        Item item = createTestItem(1L, "ITM-001", "Test Item", new BigDecimal("500.00"), true);
        SaleItem saleItem = createTestSaleItem(1L, sale, item, 2, new BigDecimal("500.00"));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(saleItemRepository.findBySaleId(1L)).thenReturn(List.of(saleItem));
        when(saleAddonRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        when(salePaymentRepository.save(any(SalePayment.class))).thenAnswer(invocation -> {
            SalePayment payment = invocation.getArgument(0);
            payment.setId(1L);
            return payment;
        });
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentNumberService.generateNextNumber(any())).thenReturn("OR-202608-00001");

        List<SalePaymentRequest> splitPayments = List.of(
                SalePaymentRequest.builder()
                        .paymentMethod(PaymentMethod.CASH)
                        .amount(new BigDecimal("600.00"))
                        .build(),
                SalePaymentRequest.builder()
                        .paymentMethod(PaymentMethod.GCASH)
                        .amount(new BigDecimal("400.00"))
                        .referenceNo("ref123")
                        .build()
        );

        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .paymentMethod(PaymentMethod.MULTIPLE)
                .amountTendered(new BigDecimal("1000.00"))
                .payments(splitPayments)
                .build();

        // Act
        SaleDetailResponse response = saleService.processPayment(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SaleStatus.PAID.name());

        // Verify 2 payment records saved
        verify(salePaymentRepository, times(2)).save(any(SalePayment.class));
    }

    // ========================================================================
    // closeSale tests
    // ========================================================================

    @Test
    @DisplayName("closeSale - given paid sale - sets status to CLOSED")
    void closeSale_givenPaidSale_setsStatusToClosed() {
        // Arrange
        Sale sale = createTestSale(1L, "RCT-202608-00001", SaleStatus.PAID, new BigDecimal("1000.00"));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(saleItemRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        when(saleAddonRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        when(salePaymentRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SaleDetailResponse response = saleService.closeSale(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SaleStatus.CLOSED.name());
    }

    @Test
    @DisplayName("closeSale - given open sale - throws BusinessRuleException")
    void closeSale_givenOpenSale_throwsBusinessRuleException() {
        // Arrange
        Sale sale = createTestSale(1L, "RCT-202608-00001", SaleStatus.OPEN, new BigDecimal("1000.00"));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        // Act & Assert
        assertThatThrownBy(() -> saleService.closeSale(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Expected status: PAID");
    }

    // ========================================================================
    // voidSale tests
    // ========================================================================

    @Test
    @DisplayName("voidSale - given paid sale - reverses stock and voids")
    void voidSale_givenPaidSale_reversesStockAndVoids() {
        // Arrange
        Sale sale = createTestSale(1L, "RCT-202608-00001", SaleStatus.PAID, new BigDecimal("1000.00"));
        Item item = createTestItem(1L, "ITM-001", "Test Item", new BigDecimal("500.00"), true);
        SaleItem saleItem = createTestSaleItem(1L, sale, item, 2, new BigDecimal("500.00"));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(saleItemRepository.findBySaleId(1L)).thenReturn(List.of(saleItem));
        when(saleAddonRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        when(salePaymentRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VoidSaleRequest request = VoidSaleRequest.builder()
                .voidReason("Duplicate sale")
                .build();

        // Act
        SaleDetailResponse response = saleService.voidSale(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SaleStatus.VOIDED.name());
        assertThat(response.getVoidReason()).isEqualTo("Duplicate sale");
        assertThat(response.getVoidedBy()).isEqualTo("testuser");

        // Verify stock reversal (addStock called for each item)
        verify(stockService, times(1)).addStock(eq(1L), eq(2));
    }

    @Test
    @DisplayName("voidSale - given open sale - voids without stock reversal")
    void voidSale_givenOpenSale_voidsWithoutStockReversal() {
        // Arrange
        Sale sale = createTestSale(1L, "RCT-202608-00001", SaleStatus.OPEN, new BigDecimal("1000.00"));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(saleItemRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        when(saleAddonRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        when(salePaymentRepository.findBySaleId(1L)).thenReturn(Collections.emptyList());
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VoidSaleRequest request = VoidSaleRequest.builder()
                .voidReason("Customer changed mind")
                .build();

        // Act
        SaleDetailResponse response = saleService.voidSale(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SaleStatus.VOIDED.name());

        // Verify stock NOT reversed (was OPEN, no stock was deducted)
        verify(stockService, never()).addStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("voidSale - given already voided sale - throws BusinessRuleException")
    void voidSale_givenAlreadyVoidedSale_throwsBusinessRuleException() {
        // Arrange
        Sale sale = createTestSale(1L, "RCT-202608-00001", SaleStatus.VOIDED, new BigDecimal("1000.00"));

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        VoidSaleRequest request = VoidSaleRequest.builder()
                .voidReason("Another void attempt")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> saleService.voidSale(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already VOIDED");
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private Sale createTestSale(Long id, String saleNo, SaleStatus status, BigDecimal totalAmount) {
        Sale sale = new Sale();
        sale.setId(id);
        sale.setSaleNo(saleNo);
        sale.setStatus(status);
        sale.setTotalAmount(totalAmount);
        sale.setSubtotal(totalAmount);
        sale.setDiscountType(DiscountType.NONE);
        sale.setDiscountValue(BigDecimal.ZERO);
        sale.setDiscountAmount(BigDecimal.ZERO);
        sale.setAddonsTotal(BigDecimal.ZERO);
        sale.setNetAmount(totalAmount);
        sale.setTaxEnabled(false);
        sale.setTaxRate(BigDecimal.ZERO);
        sale.setTaxAmount(BigDecimal.ZERO);
        sale.setVatableAmount(BigDecimal.ZERO);
        sale.setAmountTendered(BigDecimal.ZERO);
        sale.setChangeAmount(BigDecimal.ZERO);
        sale.setPaymentMethod(PaymentMethod.CASH);
        sale.setCreatedBy("testuser");
        sale.setSaleDate(LocalDateTime.now());
        return sale;
    }

    private SaleItem createTestSaleItem(Long id, Sale sale, Item item, int quantity, BigDecimal unitPrice) {
        SaleItem saleItem = new SaleItem();
        saleItem.setId(id);
        saleItem.setSale(sale);
        saleItem.setItem(item);
        saleItem.setItemName(item.getName());
        saleItem.setItemCode(item.getItemCode());
        saleItem.setQuantity(quantity);
        saleItem.setUnitPrice(unitPrice);
        saleItem.setUnitCost(item.getCostPrice());
        saleItem.setDiscountType(DiscountType.NONE);
        saleItem.setDiscountValue(BigDecimal.ZERO);
        saleItem.setDiscountAmount(BigDecimal.ZERO);
        saleItem.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        return saleItem;
    }

    private Item createTestItem(Long id, String itemCode, String name, BigDecimal price, boolean active) {
        Item item = new Item();
        item.setId(id);
        item.setItemCode(itemCode);
        item.setName(name);
        item.setPrice(price);
        item.setCostPrice(new BigDecimal("300.00"));
        item.setActive(active);
        item.setUnit("pcs");
        item.setReorderLevel(10);
        item.setTaxable(true);
        return item;
    }

    private Shift createTestShift(Long id, String cashier, ShiftStatus status) {
        Shift shift = new Shift();
        shift.setId(id);
        shift.setCashier(cashier);
        shift.setStatus(status);
        shift.setOpeningAmount(new BigDecimal("5000.00"));
        shift.setOpenedAt(LocalDateTime.now());
        return shift;
    }
}
