# Inventory + POS System - Backend Coding Standards

## Overview

This document defines the coding standards, naming conventions, packaging structure, and development patterns for the Java/Spring Boot backend.

---

## Package Structure

```
com.joven.inventory
├── InventoryApplication.java              # Main application class (extends SpringBootServletInitializer)
│
├── config/                                 # Configuration classes
│   ├── SecurityConfig.java                # Spring Security configuration
│   ├── CorsConfig.java                    # CORS settings
│   ├── JpaConfig.java                     # JPA/Hibernate settings
│   ├── JasperConfig.java                  # JasperReports configuration
│   └── AppProperties.java                 # Custom @ConfigurationProperties
│
├── security/                               # Authentication & Authorization
│   ├── JwtTokenProvider.java              # JWT token creation and validation
│   ├── JwtAuthenticationFilter.java       # OncePerRequestFilter for JWT
│   ├── CustomUserDetailsService.java      # Loads user from DB
│   ├── CustomUserDetails.java             # UserDetails implementation
│   ├── Permission.java                    # Bitwise permission constants
│   └── RequiresPermission.java            # Custom annotation for permission check
│
├── common/                                 # Shared/base classes
│   ├── BaseEntity.java                    # @MappedSuperclass with audit fields
│   ├── ApiResponse.java                   # Standard response wrapper
│   ├── PageResponse.java                  # Paginated response wrapper
│   ├── ApiError.java                      # Error detail object
│   └── Constants.java                     # Application-wide constants
│
├── entity/                                 # JPA entities (database models)
│   ├── User.java
│   ├── Category.java
│   ├── Item.java
│   ├── Customer.java
│   ├── Supplier.java
│   ├── Stock.java
│   ├── StockTransaction.java
│   ├── TransactionAddon.java
│   ├── AddonMaster.java
│   ├── AuditTrail.java
│   ├── AppSetting.java
│   ├── Sale.java
│   ├── SaleItem.java
│   ├── SaleAddon.java
│   ├── SalePayment.java
│   └── Shift.java
│
├── enums/                                  # Enumerations
│   ├── TransactionType.java               # IN, OUT, ADJUSTMENT
│   ├── TransactionStatus.java             # CREATED, APPROVED, CANCELLED
│   ├── DiscountType.java                  # NONE, FIXED, PERCENTAGE, SENIOR_PWD
│   ├── TaxType.java                       # VAT, NON_VAT, EXEMPT, ZERO_RATED
│   ├── DocumentType.java                  # OR, SI, DR, PO, RR, NONE
│   ├── PaymentMethod.java                 # CASH, GCASH, BANK_TRANSFER, CREDIT, MULTIPLE
│   ├── SaleStatus.java                    # OPEN, PAID, CLOSED, VOIDED
│   ├── ShiftStatus.java                   # OPEN, CLOSED
│   ├── AuditAction.java                   # CREATE, UPDATE, DELETE
│   └── UserRole.java                      # ADMIN, STAFF
│
├── repository/                             # JPA repositories
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── ItemRepository.java
│   ├── CustomerRepository.java
│   ├── SupplierRepository.java
│   ├── StockRepository.java
│   ├── StockTransactionRepository.java
│   ├── TransactionAddonRepository.java
│   ├── AddonMasterRepository.java
│   ├── AuditTrailRepository.java
│   ├── AppSettingRepository.java
│   ├── SaleRepository.java
│   ├── SaleItemRepository.java
│   ├── SaleAddonRepository.java
│   ├── SalePaymentRepository.java
│   └── ShiftRepository.java
│
├── dto/                                    # Data Transfer Objects
│   ├── request/                           # Incoming request bodies
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── CreateCategoryRequest.java
│   │   ├── UpdateCategoryRequest.java
│   │   ├── CreateItemRequest.java
│   │   ├── UpdateItemRequest.java
│   │   ├── CreateCustomerRequest.java
│   │   ├── UpdateCustomerRequest.java
│   │   ├── CreateSupplierRequest.java
│   │   ├── UpdateSupplierRequest.java
│   │   ├── StockInRequest.java
│   │   ├── StockOutRequest.java
│   │   ├── StockAdjustRequest.java
│   │   ├── CreateSaleRequest.java
│   │   ├── SaleItemRequest.java
│   │   ├── SalePaymentRequest.java
│   │   ├── VoidSaleRequest.java
│   │   ├── OpenShiftRequest.java
│   │   ├── CloseShiftRequest.java
│   │   ├── CreateUserRequest.java
│   │   ├── UpdateUserRequest.java
│   │   ├── ResetPasswordRequest.java
│   │   ├── UpdateSettingsRequest.java
│   │   └── StatusUpdateRequest.java
│   └── response/                          # Outgoing response bodies
│       ├── LoginResponse.java
│       ├── UserResponse.java
│       ├── CategoryResponse.java
│       ├── ItemResponse.java
│       ├── CustomerResponse.java
│       ├── SupplierResponse.java
│       ├── StockResponse.java
│       ├── StockTransactionResponse.java
│       ├── SaleResponse.java
│       ├── SaleDetailResponse.java
│       ├── ShiftResponse.java
│       ├── ShiftSummaryResponse.java
│       ├── AuditTrailResponse.java
│       ├── AppSettingResponse.java
│       ├── DashboardResponse.java
│       └── ReceiptResponse.java
│
├── mapper/                                 # Entity ↔ DTO converters
│   ├── UserMapper.java
│   ├── CategoryMapper.java
│   ├── ItemMapper.java
│   ├── CustomerMapper.java
│   ├── SupplierMapper.java
│   ├── StockMapper.java
│   ├── StockTransactionMapper.java
│   ├── SaleMapper.java
│   ├── ShiftMapper.java
│   └── AuditTrailMapper.java
│
├── service/                                # Business logic (interfaces)
│   ├── AuthService.java
│   ├── UserService.java
│   ├── CategoryService.java
│   ├── ItemService.java
│   ├── CustomerService.java
│   ├── SupplierService.java
│   ├── StockService.java
│   ├── StockTransactionService.java
│   ├── AddonMasterService.java
│   ├── SaleService.java
│   ├── ShiftService.java
│   ├── TaxService.java
│   ├── DocumentNumberService.java
│   ├── AuditTrailService.java
│   ├── AppSettingService.java
│   ├── DashboardService.java
│   ├── ReportService.java
│   └── impl/                              # Service implementations
│       ├── AuthServiceImpl.java
│       ├── UserServiceImpl.java
│       ├── CategoryServiceImpl.java
│       ├── ItemServiceImpl.java
│       ├── CustomerServiceImpl.java
│       ├── SupplierServiceImpl.java
│       ├── StockServiceImpl.java
│       ├── StockTransactionServiceImpl.java
│       ├── AddonMasterServiceImpl.java
│       ├── SaleServiceImpl.java
│       ├── ShiftServiceImpl.java
│       ├── TaxServiceImpl.java
│       ├── DocumentNumberServiceImpl.java
│       ├── AuditTrailServiceImpl.java
│       ├── AppSettingServiceImpl.java
│       ├── DashboardServiceImpl.java
│       └── ReportServiceImpl.java
│
├── controller/                             # REST controllers
│   ├── AuthController.java
│   ├── UserController.java
│   ├── CategoryController.java
│   ├── ItemController.java
│   ├── CustomerController.java
│   ├── SupplierController.java
│   ├── StockController.java
│   ├── TransactionController.java
│   ├── AddonMasterController.java
│   ├── PosController.java
│   ├── ShiftController.java
│   ├── AuditTrailController.java
│   ├── AppSettingController.java
│   ├── DashboardController.java
│   └── ReportController.java
│
├── exception/                              # Exception handling
│   ├── GlobalExceptionHandler.java        # @RestControllerAdvice
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   ├── UnauthorizedException.java
│   ├── ForbiddenException.java
│   ├── DuplicateResourceException.java
│   ├── InsufficientStockException.java
│   └── BusinessRuleException.java
│
├── audit/                                  # Audit trail mechanism
│   ├── AuditEntityListener.java           # JPA @EntityListeners
│   ├── AuditContext.java                  # Holds current user + IP per request
│   └── AuditInterceptor.java             # HandlerInterceptor to set AuditContext
│
├── report/                                 # JasperReports
│   ├── ReportGenerator.java               # Compiles & fills reports
│   └── ReportDataSource.java              # Custom JR data source adapter
│
└── util/                                   # Utility classes
    ├── DateUtil.java                       # Date/time helpers (Asia/Manila)
    ├── CurrencyUtil.java                   # Peso formatting (₱1,234.56)
    ├── ValidationUtil.java                 # Common validation helpers
    └── PermissionUtil.java                 # Bitwise permission checks
```

---

## Naming Conventions

### Classes

| Type | Convention | Example |
|------|-----------|---------|
| Entity | Singular noun, PascalCase | `Item`, `StockTransaction`, `Sale` |
| Repository | Entity + Repository | `ItemRepository`, `SaleRepository` |
| Service (interface) | Entity + Service | `ItemService`, `SaleService` |
| Service (impl) | Entity + ServiceImpl | `ItemServiceImpl`, `SaleServiceImpl` |
| Controller | Entity + Controller | `ItemController`, `PosController` |
| Request DTO | Action + Entity + Request | `CreateItemRequest`, `StockInRequest` |
| Response DTO | Entity + Response | `ItemResponse`, `SaleDetailResponse` |
| Mapper | Entity + Mapper | `ItemMapper`, `SaleMapper` |
| Enum | Descriptive noun, PascalCase | `TransactionType`, `PaymentMethod` |
| Exception | Descriptive + Exception | `InsufficientStockException` |
| Config | Feature + Config | `SecurityConfig`, `CorsConfig` |
| Util | Feature + Util | `DateUtil`, `CurrencyUtil` |

### Methods

| Type | Convention | Example |
|------|-----------|---------|
| Service (create) | `create` + Entity | `createItem(request)` |
| Service (update) | `update` + Entity | `updateItem(id, request)` |
| Service (get one) | `get` + Entity + `ById` | `getItemById(id)` |
| Service (get list) | `getAll` + Entity(plural) | `getAllItems(pageable, filters)` |
| Service (delete/deactivate) | `deactivate` + Entity | `deactivateItem(id)` |
| Service (search) | `search` + Entity(plural) | `searchItems(query)` |
| Controller | Same as service | Maps 1:1 to service methods |
| Mapper (to DTO) | `toResponse` | `toResponse(entity)` |
| Mapper (to entity) | `toEntity` | `toEntity(request)` |
| Repository (custom) | `findBy` + field | `findByItemCode(code)` |
| Boolean check | `is` or `has` prefix | `isActive()`, `hasPermission()` |

### Variables & Fields

| Convention | Example |
|-----------|---------|
| camelCase for all variables | `itemCode`, `transactionDate`, `unitPrice` |
| Prefix boolean with `is`/`has` (in DTOs) | `isActive`, `hasTax` |
| Constants: UPPER_SNAKE_CASE | `MAX_DISCOUNT_PERCENT`, `DEFAULT_PAGE_SIZE` |
| Collections: plural | `items`, `saleItems`, `payments` |

### Database Mapping

| Java | Database |
|------|----------|
| `camelCase` field | `snake_case` column via `@Column(name = "...")` |
| `LocalDateTime` | `DATETIME` |
| `BigDecimal` | `DECIMAL(12,2)` |
| `Boolean` | `TINYINT(1)` |
| `Long` | `BIGINT` |
| `Enum` | `@Enumerated(EnumType.STRING)` stored as `VARCHAR` or `ENUM` |

---

## Coding Standards

### General Rules

1. **No business logic in controllers** — controllers only handle HTTP request/response
2. **No direct repository calls from controllers** — always go through service
3. **Transaction boundaries in service layer** — `@Transactional` on service methods
4. **Always use interfaces for services** — `ItemService` interface, `ItemServiceImpl` class
5. **Never use `SELECT *`** — use projections or specific field selections when performance matters
6. **Never hardcode values** — use constants, enums, or app_settings
7. **Always validate input** — use Jakarta Bean Validation annotations (`@NotNull`, `@Size`, etc.)
8. **Never expose entities directly in API** — always use DTOs for request/response
9. **Never use fully-qualified class names in code** — always import at top
10. **One class per file** — no inner classes except for builder patterns or simple DTOs

### Annotations Usage

```java
// Entity
@Entity
@Table(name = "items")
@EntityListeners(AuditEntityListener.class)
public class Item extends BaseEntity { }

// Repository
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> { }

// Service Interface
public interface ItemService {
    ItemResponse createItem(CreateItemRequest request);
}

// Service Implementation
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    
    @Override
    @Transactional
    public ItemResponse createItem(CreateItemRequest request) { }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ItemResponse> getAllItems(Pageable pageable) { }
}

// Controller
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(
            @Valid @RequestBody CreateItemRequest request) { }
}
```

### BaseEntity (Shared Audit Fields)

```java
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public abstract class BaseEntity {

    @Column(name = "created_by", nullable = false, updatable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // createdBy set via AuditContext
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        // updatedBy set via AuditContext
    }
}
```

### Standard API Response

```java
@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<ApiError> errors;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, List<ApiError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

### DTO Validation

```java
@Data
public class CreateItemRequest {

    @NotBlank(message = "Item code is required")
    @Size(max = 50, message = "Item code must not exceed 50 characters")
    private String itemCode;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Unit is required")
    @Size(max = 30, message = "Unit must not exceed 30 characters")
    private String unit;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be zero or positive")
    @Digits(integer = 10, fraction = 2, message = "Price format invalid")
    private BigDecimal price;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.00", message = "Cost price must be zero or positive")
    @Digits(integer = 10, fraction = 2, message = "Cost price format invalid")
    private BigDecimal costPrice;

    @Min(value = 0, message = "Reorder level must be zero or positive")
    private Integer reorderLevel = 0;

    private Boolean taxable = true;
}
```

### Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiError(e.getField(), e.getDefaultMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage(), null));
    }
}
```

---

## Transaction Patterns

### Read Operations
```java
@Transactional(readOnly = true)
public ItemResponse getItemById(Long id) {
    Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
    return itemMapper.toResponse(item);
}
```

### Write Operations
```java
@Transactional
public ItemResponse createItem(CreateItemRequest request) {
    // Validate uniqueness
    if (itemRepository.existsByItemCode(request.getItemCode())) {
        throw new DuplicateResourceException("Item code already exists: " + request.getItemCode());
    }
    
    // Map and save
    Item item = itemMapper.toEntity(request);
    item = itemRepository.save(item);
    
    // Create initial stock record
    Stock stock = new Stock();
    stock.setItem(item);
    stock.setQuantityOnHand(0);
    stockRepository.save(stock);
    
    return itemMapper.toResponse(item);
}
```

### Complex Transaction (POS Sale)
```java
@Transactional
public SaleResponse createSale(CreateSaleRequest request) {
    // 1. Validate shift is open
    Shift shift = shiftService.getCurrentShift();
    
    // 2. Validate all items have sufficient stock
    for (SaleItemRequest item : request.getItems()) {
        Stock stock = stockRepository.findByItemId(item.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));
        if (stock.getQuantityOnHand() < item.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for: " + item.getItemId());
        }
    }
    
    // 3. Create sale header
    Sale sale = buildSaleFromRequest(request, shift);
    sale = saleRepository.save(sale);
    
    // 4. Create sale items + deduct stock
    for (SaleItemRequest itemReq : request.getItems()) {
        SaleItem saleItem = buildSaleItem(sale, itemReq);
        saleItemRepository.save(saleItem);
        stockService.deductStock(itemReq.getItemId(), itemReq.getQuantity(), sale.getSaleNo());
    }
    
    // 5. Process payments
    processPayments(sale, request.getPayments());
    
    // 6. Generate document number
    sale.setDocumentNo(documentNumberService.generateNext(sale.getDocumentType()));
    
    return saleMapper.toDetailResponse(sale);
}
```

---

## Controller Patterns

### Standard CRUD Controller

```java
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItemResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ItemResponse> result = itemService.getAllItems(pageable, search, categoryId, active);
        return ResponseEntity.ok(ApiResponse.success("Items retrieved", PageResponse.of(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getById(@PathVariable Long id) {
        ItemResponse item = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Item retrieved", item));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> create(
            @Valid @RequestBody CreateItemRequest request) {
        ItemResponse item = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item created", item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        ItemResponse item = itemService.updateItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Item updated", item));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        itemService.updateStatus(id, request.getActive());
        String action = request.getActive() ? "activated" : "deactivated";
        return ResponseEntity.ok(ApiResponse.success("Item " + action, null));
    }
}
```

### Permission Check in Controller

```java
@PostMapping("/{id}/void")
public ResponseEntity<ApiResponse<SaleResponse>> voidSale(
        @PathVariable Long id,
        @Valid @RequestBody VoidSaleRequest request,
        @AuthenticationPrincipal CustomUserDetails user) {
    
    if (!PermissionUtil.hasPermission(user.getAccessRights(), Permission.VOID_SALES)) {
        throw new ForbiddenException("You do not have permission to void sales");
    }
    
    SaleResponse sale = saleService.voidSale(id, request);
    return ResponseEntity.ok(ApiResponse.success("Sale voided", sale));
}
```

---

## Mapper Pattern

```java
@Component
public class ItemMapper {

    public ItemResponse toResponse(Item entity) {
        ItemResponse response = new ItemResponse();
        response.setId(entity.getId());
        response.setItemCode(entity.getItemCode());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setCategoryId(entity.getCategory().getId());
        response.setCategoryName(entity.getCategory().getName());
        response.setUnit(entity.getUnit());
        response.setPrice(entity.getPrice());
        response.setCostPrice(entity.getCostPrice());
        response.setReorderLevel(entity.getReorderLevel());
        response.setTaxable(entity.getTaxable());
        response.setActive(entity.getActive());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public Item toEntity(CreateItemRequest request) {
        Item entity = new Item();
        entity.setItemCode(request.getItemCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setUnit(request.getUnit());
        entity.setPrice(request.getPrice());
        entity.setCostPrice(request.getCostPrice());
        entity.setReorderLevel(request.getReorderLevel());
        entity.setTaxable(request.getTaxable());
        entity.setActive(true);
        return entity;
    }

    public void updateEntity(Item entity, UpdateItemRequest request) {
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setUnit(request.getUnit());
        entity.setPrice(request.getPrice());
        entity.setCostPrice(request.getCostPrice());
        entity.setReorderLevel(request.getReorderLevel());
        entity.setTaxable(request.getTaxable());
        // itemCode is NOT updatable after creation
    }
}
```

---

## Error Handling Standards

| HTTP Status | When to Use | Exception |
|-------------|-------------|-----------|
| 200 | Successful read/update | — |
| 201 | Successful creation | — |
| 400 | Invalid request data, validation failure | `BadRequestException`, `MethodArgumentNotValidException` |
| 401 | Missing or invalid authentication | `UnauthorizedException` |
| 403 | Valid user, insufficient permissions | `ForbiddenException` |
| 404 | Resource not found | `ResourceNotFoundException` |
| 409 | Duplicate resource (unique constraint) | `DuplicateResourceException` |
| 422 | Business rule violation | `BusinessRuleException`, `InsufficientStockException` |
| 500 | Unexpected server error | Generic `Exception` (logged, not exposed) |

**Rules:**
- Never expose stack traces to client
- Always log full stack trace server-side
- Always return consistent `ApiResponse` JSON structure
- Include meaningful error messages (user-friendly)

---

## Logging Standards

```java
@Slf4j  // Lombok
@Service
public class SaleServiceImpl implements SaleService {

    @Override
    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {
        log.info("Creating sale: {} items, payment: {}", 
                request.getItems().size(), request.getPaymentMethod());
        
        // ... business logic ...
        
        log.info("Sale created successfully: {}", sale.getSaleNo());
        return saleMapper.toDetailResponse(sale);
    }
}
```

| Level | When to Use |
|-------|-------------|
| `ERROR` | Unexpected failures, caught exceptions that shouldn't happen |
| `WARN` | Recoverable issues, business rule near-misses |
| `INFO` | Important business events (sale created, shift opened, void) |
| `DEBUG` | Detailed flow (disabled in production) |

**Never log:**
- Passwords or tokens
- Full credit card numbers
- Personal sensitive data in plain text

---

## Dependency Injection

- **Always use constructor injection** (via `@RequiredArgsConstructor` from Lombok)
- **Never use field injection** (`@Autowired` on fields)
- **Never use setter injection** unless for optional dependencies

```java
// ✅ CORRECT — Constructor injection
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;
    private final ItemMapper itemMapper;
}

// ❌ WRONG — Field injection
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemRepository itemRepository;  // Don't do this
}
```

---

## Testing Standards

### Unit Test Naming
```
methodName_givenCondition_expectedResult
```

Example:
```java
@Test
void createSale_givenInsufficientStock_throwsInsufficientStockException() { }

@Test
void calculateTax_givenVatInclusive_returnsCorrectVatAmount() { }

@Test
void deactivateItem_givenActiveItem_setsActiveToFalse() { }
```

### Unit Test Structure (AAA)
```java
@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private ItemMapper itemMapper;
    
    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_givenValidRequest_returnsItemResponse() {
        // Arrange
        CreateItemRequest request = new CreateItemRequest();
        request.setItemCode("ITM-001");
        request.setName("Test Item");
        
        Item entity = new Item();
        entity.setId(1L);
        
        ItemResponse expected = new ItemResponse();
        expected.setId(1L);
        
        when(itemRepository.existsByItemCode("ITM-001")).thenReturn(false);
        when(itemMapper.toEntity(request)).thenReturn(entity);
        when(itemRepository.save(entity)).thenReturn(entity);
        when(itemMapper.toResponse(entity)).thenReturn(expected);
        
        // Act
        ItemResponse result = itemService.createItem(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(itemRepository).save(entity);
    }
}
```

---

## File Organization Rules

1. **One public class per file** — filename matches class name
2. **Import order**: java → jakarta → spring → third-party → project
3. **No wildcard imports** — always use specific imports
4. **Max file length**: ~300 lines (refactor if exceeding)
5. **Method length**: max ~30 lines (extract helper methods if longer)
6. **Class annotation order**: `@Entity` → `@Table` → `@EntityListeners` → `@Data`/`@Getter`/`@Setter`

---

## JavaDoc & Comments Standards

### Author
All new Java files must include:
```
@author Joven Q. Divinagracia Jr.
```

### Class-Level JavaDoc (Required on ALL classes)
```java
/**
 * JPA entity representing an inventory item.
 * Maps to the {@code items} table in the database.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Entity
@Table(name = "items")
public class Item extends BaseEntity { }
```

### Interface-Level JavaDoc
```java
/**
 * Service interface for item management operations.
 * Handles CRUD, search, item code generation, and status updates.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface ItemService { }
```

### Method-Level JavaDoc (Required on public methods)
```java
/**
 * Creates a new item and initializes its stock record.
 *
 * @param request the item creation request containing item details
 * @return the created item response
 * @throws DuplicateResourceException if item code already exists
 */
@Transactional
public ItemResponse createItem(CreateItemRequest request) { }
```

### Field-Level Comments (for non-obvious fields)
```java
/**
 * Bitwise permissions stored as a long integer.
 * Each bit represents a specific permission (see Permission.java).
 */
@Column(name = "access_rights", nullable = false)
private Long accessRights = 0L;

/** Selling price per unit (VAT-inclusive or exclusive based on app_settings). */
@Column(nullable = false, precision = 12, scale = 2)
private BigDecimal price;
```

### Inline Comments (for complex logic)
```java
// Calculate VAT: net_amount / 1.12 for VAT-inclusive pricing
BigDecimal vatableAmount = netAmount.divide(Constants.VAT_DIVISOR, 2, RoundingMode.HALF_UP);

// Reverse stock for each sale item on void
for (SaleItem saleItem : sale.getSaleItems()) {
    stockService.addStock(saleItem.getItemId(), saleItem.getQuantity(), 
            sale.getSaleNo() + " [VOID]");
}
```

### When to Comment
| Situation | Comment Type |
|-----------|-------------|
| Every class/interface | Class-level JavaDoc with description + @author |
| Every public method | Method JavaDoc with @param, @return, @throws |
| Complex business logic | Inline comment explaining WHY |
| Non-obvious field purpose | Field-level comment |
| Workarounds or TODOs | `// TODO:` or `// WORKAROUND:` |
| Magic numbers/values | Comment explaining the value |

### When NOT to Comment
- Self-explanatory getter/setter (Lombok generates these)
- Trivial private helper methods with clear names
- Code that's already clear from naming

---

## Lombok Usage

| Annotation | Used On | Purpose |
|-----------|---------|---------|
| `@Data` | DTOs (request/response) | Getters, setters, equals, hashCode, toString |
| `@Getter` / `@Setter` | Entities | Avoids `@Data` on entities (hashCode issues with JPA) |
| `@NoArgsConstructor` | Entities | Required by JPA |
| `@AllArgsConstructor` | DTOs (where needed) | Full constructor |
| `@Builder` | Response DTOs, ApiResponse | Builder pattern |
| `@RequiredArgsConstructor` | Services, Controllers | Constructor injection for `final` fields |
| `@Slf4j` | Services, Controllers | Logging |

**Do NOT use `@Data` on entities** — it generates `equals()`/`hashCode()` that break with lazy-loaded proxies.

---

## Security Patterns

### Permission Check (Service Level)
```java
public void validatePermission(CustomUserDetails user, long permission) {
    if (!PermissionUtil.hasPermission(user.getAccessRights(), permission)) {
        throw new ForbiddenException("Insufficient permissions");
    }
}
```

### Sensitive Operations (Double Check)
```java
@Transactional
public SaleResponse voidSale(Long saleId, VoidSaleRequest request, CustomUserDetails user) {
    // Permission check
    validatePermission(user, Permission.VOID_SALES);
    
    // Business validation
    Sale sale = saleRepository.findById(saleId)
            .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
    
    if (sale.getStatus() != SaleStatus.COMPLETED) {
        throw new BusinessRuleException("Only completed sales can be voided");
    }
    
    if (request.getVoidReason() == null || request.getVoidReason().isBlank()) {
        throw new BadRequestException("Void reason is required");
    }
    
    // Execute void
    sale.setStatus(SaleStatus.VOIDED);
    sale.setVoidReason(request.getVoidReason());
    sale.setVoidedBy(user.getUsername());
    sale.setVoidedAt(LocalDateTime.now());
    
    // Reverse stock
    reverseStockForSale(sale);
    
    return saleMapper.toDetailResponse(saleRepository.save(sale));
}
```

---

## Summary Checklist

Before pushing any code, verify:

- [ ] No business logic in controllers
- [ ] All service methods have `@Transactional` (or `readOnly = true`)
- [ ] All inputs validated with Jakarta annotations
- [ ] DTOs used for all request/response (never expose entities)
- [ ] Proper exception types used (not generic Exception)
- [ ] Audit fields (created_by, updated_by) populated automatically
- [ ] Permission checks on sensitive operations
- [ ] No hardcoded values (use constants, enums, settings)
- [ ] Constructor injection (no field injection)
- [ ] Lombok used appropriately (no `@Data` on entities)
- [ ] Meaningful log messages at appropriate levels
- [ ] No secrets or sensitive data in logs
- [ ] Method and class names follow conventions
- [ ] Imports are specific (no wildcards)
