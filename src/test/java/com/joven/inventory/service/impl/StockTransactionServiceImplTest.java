package com.joven.inventory.service.impl;

import com.joven.inventory.audit.AuditContext;
import com.joven.inventory.dto.request.StockAdjustRequest;
import com.joven.inventory.dto.request.StockInRequest;
import com.joven.inventory.dto.request.StockOutRequest;
import com.joven.inventory.dto.response.StockTransactionResponse;
import com.joven.inventory.entity.Category;
import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Stock;
import com.joven.inventory.entity.StockTransaction;
import com.joven.inventory.enums.DiscountType;
import com.joven.inventory.enums.DocumentType;
import com.joven.inventory.enums.TransactionStatus;
import com.joven.inventory.enums.TransactionType;
import com.joven.inventory.exception.BusinessRuleException;
import com.joven.inventory.exception.InsufficientStockException;
import com.joven.inventory.repository.CustomerRepository;
import com.joven.inventory.repository.ItemRepository;
import com.joven.inventory.repository.StockRepository;
import com.joven.inventory.repository.StockTransactionRepository;
import com.joven.inventory.repository.SupplierRepository;
import com.joven.inventory.repository.TransactionAddonRepository;
import com.joven.inventory.service.AppSettingService;
import com.joven.inventory.service.DocumentNumberService;
import com.joven.inventory.service.StockService;
import com.joven.inventory.service.TaxService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StockTransactionServiceImpl}.
 * Tests creation, approval, and cancellation of stock transactions with proper
 * business rule validation and stock impact verification.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
class StockTransactionServiceImplTest {

    @Mock
    private StockTransactionRepository stockTransactionRepository;

    @Mock
    private TransactionAddonRepository transactionAddonRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SupplierRepository supplierRepository;

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

    @InjectMocks
    private StockTransactionServiceImpl stockTransactionService;

    @BeforeEach
    void setUp() {
        AuditContext.set("testuser", "127.0.0.1");
    }

    @AfterEach
    void tearDown() {
        AuditContext.clear();
    }

    // ======================== createStockIn ========================

    @Test
    @DisplayName("createStockIn - given valid request - creates transaction with status CREATED")
    void createStockIn_givenValidRequest_createsTransactionWithStatusCreated() {
        // Arrange
        Item item = createItem();
        StockInRequest request = StockInRequest.builder()
                .itemId(1L)
                .quantity(10)
                .unitCost(new BigDecimal("100.00"))
                .supplierId(null)
                .transactionDate(LocalDateTime.now())
                .taxEnabled(false)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(invocation -> {
                    StockTransaction saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        // Act
        StockTransactionResponse response = stockTransactionService.createStockIn(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTransactionType()).isEqualTo("IN");
        assertThat(response.getStatus()).isEqualTo("CREATED");
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.getQuantity()).isEqualTo(10);
        assertThat(response.getItemId()).isEqualTo(1L);

        // Stock should NOT be affected until approved
        verify(stockService, never()).addStock(any(), any(Integer.class));
    }

    @Test
    @DisplayName("createStockIn - given inactive item - throws BusinessRuleException")
    void createStockIn_givenInactiveItem_throwsBusinessRuleException() {
        // Arrange
        Item item = createItem();
        item.setActive(false);

        StockInRequest request = StockInRequest.builder()
                .itemId(1L)
                .quantity(10)
                .unitCost(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act & Assert
        assertThatThrownBy(() -> stockTransactionService.createStockIn(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactive");
    }

    // ======================== createStockOut ========================

    @Test
    @DisplayName("createStockOut - given valid request with percentage discount - creates transaction with correct financials")
    void createStockOut_givenValidRequest_createsWithDiscount() {
        // Arrange
        Item item = createItem();
        Stock stock = createStock(item, 100);

        StockOutRequest request = StockOutRequest.builder()
                .itemId(1L)
                .quantity(5)
                .unitPrice(new BigDecimal("200.00"))
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .transactionDate(LocalDateTime.now())
                .taxEnabled(false)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(stockRepository.findByItemId(1L)).thenReturn(Optional.of(stock));
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(invocation -> {
                    StockTransaction saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        // Act
        StockTransactionResponse response = stockTransactionService.createStockOut(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTransactionType()).isEqualTo("OUT");
        assertThat(response.getStatus()).isEqualTo("CREATED");
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getNetAmount()).isEqualByComparingTo(new BigDecimal("900.00"));
    }

    @Test
    @DisplayName("createStockOut - given insufficient stock - throws InsufficientStockException")
    void createStockOut_givenInsufficientStock_throwsInsufficientStockException() {
        // Arrange
        Item item = createItem();
        Stock stock = createStock(item, 3);

        StockOutRequest request = StockOutRequest.builder()
                .itemId(1L)
                .quantity(10)
                .unitPrice(new BigDecimal("200.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(stockRepository.findByItemId(1L)).thenReturn(Optional.of(stock));

        // Act & Assert
        assertThatThrownBy(() -> stockTransactionService.createStockOut(request))
                .isInstanceOf(InsufficientStockException.class);
    }

    // ======================== createStockAdjust ========================

    @Test
    @DisplayName("createStockAdjust - given valid request - creates transaction with zero financials")
    void createStockAdjust_givenValidRequest_createsWithZeroFinancials() {
        // Arrange
        Item item = createItem();

        StockAdjustRequest request = StockAdjustRequest.builder()
                .itemId(1L)
                .quantity(50)
                .transactionDate(LocalDateTime.now())
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(invocation -> {
                    StockTransaction saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        // Act
        StockTransactionResponse response = stockTransactionService.createStockAdjust(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTransactionType()).isEqualTo("ADJUSTMENT");
        assertThat(response.getStatus()).isEqualTo("CREATED");
        assertThat(response.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getNetAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTaxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ======================== approve ========================

    @Test
    @DisplayName("approve - given CREATED IN transaction - approves and adds stock")
    void approve_givenCreatedInTransaction_approvesAndAddsStock() {
        // Arrange
        StockTransaction transaction = createTransaction(TransactionType.IN, TransactionStatus.CREATED, 10);

        when(stockTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(transactionAddonRepository.findByTransactionId(1L))
                .thenReturn(Collections.emptyList());

        // Act
        StockTransactionResponse response = stockTransactionService.approve(1L);

        // Assert
        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getApprovedBy()).isEqualTo("testuser");
        verify(stockService).addStock(1L, 10);
    }

    @Test
    @DisplayName("approve - given CREATED OUT transaction - approves and deducts stock")
    void approve_givenCreatedOutTransaction_approvesAndDeductsStock() {
        // Arrange
        StockTransaction transaction = createTransaction(TransactionType.OUT, TransactionStatus.CREATED, 5);

        when(stockTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(transactionAddonRepository.findByTransactionId(1L))
                .thenReturn(Collections.emptyList());

        // Act
        StockTransactionResponse response = stockTransactionService.approve(1L);

        // Assert
        assertThat(response.getStatus()).isEqualTo("APPROVED");
        verify(stockService).deductStock(1L, 5);
    }

    @Test
    @DisplayName("approve - given already APPROVED transaction - throws BusinessRuleException")
    void approve_givenAlreadyApproved_throwsBusinessRuleException() {
        // Arrange
        StockTransaction transaction = createTransaction(TransactionType.IN, TransactionStatus.APPROVED, 10);

        when(stockTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThatThrownBy(() -> stockTransactionService.approve(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot approve transaction with status: APPROVED");
    }

    // ======================== cancel ========================

    @Test
    @DisplayName("cancel - given CREATED transaction - cancels without stock impact")
    void cancel_givenCreatedTransaction_cancelsWithoutStockImpact() {
        // Arrange
        StockTransaction transaction = createTransaction(TransactionType.IN, TransactionStatus.CREATED, 10);

        when(stockTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(transactionAddonRepository.findByTransactionId(1L))
                .thenReturn(Collections.emptyList());

        // Act
        StockTransactionResponse response = stockTransactionService.cancel(1L);

        // Assert
        assertThat(response.getStatus()).isEqualTo("CANCELLED");
        verify(stockService, never()).addStock(any(), any(Integer.class));
        verify(stockService, never()).deductStock(any(), any(Integer.class));
        verify(stockService, never()).setStock(any(), any(Integer.class));
    }

    @Test
    @DisplayName("cancel - given APPROVED transaction - throws BusinessRuleException")
    void cancel_givenApprovedTransaction_throwsBusinessRuleException() {
        // Arrange
        StockTransaction transaction = createTransaction(TransactionType.IN, TransactionStatus.APPROVED, 10);

        when(stockTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThatThrownBy(() -> stockTransactionService.cancel(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot cancel transaction with status: APPROVED");
    }

    // ======================== Helper methods ========================

    /**
     * Creates a test Item entity with all required fields for mapper usage.
     */
    private Item createItem() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Cat");
        category.setActive(true);

        Item item = new Item();
        item.setId(1L);
        item.setItemCode("ITM-001");
        item.setName("Test Item");
        item.setCategory(category);
        item.setUnit("pcs");
        item.setPrice(new BigDecimal("200.00"));
        item.setCostPrice(new BigDecimal("100.00"));
        item.setReorderLevel(10);
        item.setTaxable(true);
        item.setActive(true);

        return item;
    }

    /**
     * Creates a Stock entity associated with the given item.
     */
    private Stock createStock(Item item, int quantityOnHand) {
        Stock stock = new Stock();
        stock.setId(1L);
        stock.setItem(item);
        stock.setQuantityOnHand(quantityOnHand);
        stock.setLastUpdated(LocalDateTime.now());
        return stock;
    }

    /**
     * Creates a StockTransaction entity with the given type, status, and quantity.
     * The transaction is associated with the default test item.
     */
    private StockTransaction createTransaction(TransactionType type, TransactionStatus status, int quantity) {
        Item item = createItem();

        StockTransaction transaction = new StockTransaction();
        transaction.setId(1L);
        transaction.setItem(item);
        transaction.setTransactionType(type);
        transaction.setStatus(status);
        transaction.setQuantity(quantity);
        transaction.setUnitCost(new BigDecimal("100.00"));
        transaction.setUnitPrice(new BigDecimal("200.00"));
        transaction.setDiscountType(DiscountType.NONE);
        transaction.setDiscountValue(BigDecimal.ZERO);
        transaction.setDiscountAmount(BigDecimal.ZERO);
        transaction.setSubtotal(BigDecimal.ZERO);
        transaction.setNetAmount(BigDecimal.ZERO);
        transaction.setTaxEnabled(false);
        transaction.setTaxRate(BigDecimal.ZERO);
        transaction.setTaxAmount(BigDecimal.ZERO);
        transaction.setVatableAmount(BigDecimal.ZERO);
        transaction.setTotalAmount(BigDecimal.ZERO);
        transaction.setDocumentType(DocumentType.NONE);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setCreatedBy("testuser");
        transaction.setCreatedAt(LocalDateTime.now());

        return transaction;
    }
}
