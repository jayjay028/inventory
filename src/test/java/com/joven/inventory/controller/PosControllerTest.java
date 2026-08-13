package com.joven.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joven.inventory.dto.request.CreateSaleRequest;
import com.joven.inventory.dto.request.ProcessPaymentRequest;
import com.joven.inventory.dto.request.SaleItemRequest;
import com.joven.inventory.dto.request.VoidSaleRequest;
import com.joven.inventory.dto.response.ReceiptResponse;
import com.joven.inventory.dto.response.SaleDetailResponse;
import com.joven.inventory.dto.response.SaleResponse;
import com.joven.inventory.enums.DiscountType;
import com.joven.inventory.enums.DocumentType;
import com.joven.inventory.enums.PaymentMethod;
import com.joven.inventory.exception.BusinessRuleException;
import com.joven.inventory.exception.GlobalExceptionHandler;
import com.joven.inventory.service.ItemService;
import com.joven.inventory.service.SaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc tests for {@link PosController}.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
class PosControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SaleService saleService;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private PosController posController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(posController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("POST /api/pos/sales - valid returns 201")
    void createSale_validRequest_returns201() throws Exception {
        SaleItemRequest item = SaleItemRequest.builder()
                .itemId(1L).quantity(2).unitPrice(new BigDecimal("150.00"))
                .discountType(DiscountType.NONE).discountValue(BigDecimal.ZERO).build();

        CreateSaleRequest request = CreateSaleRequest.builder()
                .items(List.of(item))
                .discountType(DiscountType.NONE).discountValue(BigDecimal.ZERO)
                .taxEnabled(false).documentType(DocumentType.OR).build();

        SaleDetailResponse response = SaleDetailResponse.builder()
                .id(1L).saleNo("RCT-202608-00001").status("OPEN")
                .totalAmount(new BigDecimal("300.00")).build();

        when(saleService.createSale(any())).thenReturn(response);

        mockMvc.perform(post("/api/pos/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.saleNo").value("RCT-202608-00001"));
    }

    @Test
    @DisplayName("POST /api/pos/sales - no open shift returns 422")
    void createSale_noShift_returns422() throws Exception {
        SaleItemRequest item = SaleItemRequest.builder()
                .itemId(1L).quantity(1).unitPrice(new BigDecimal("100.00"))
                .discountType(DiscountType.NONE).discountValue(BigDecimal.ZERO).build();

        CreateSaleRequest request = CreateSaleRequest.builder()
                .items(List.of(item)).discountType(DiscountType.NONE)
                .discountValue(BigDecimal.ZERO).taxEnabled(false).documentType(DocumentType.OR).build();

        when(saleService.createSale(any())).thenThrow(new BusinessRuleException("No open shift found"));

        mockMvc.perform(post("/api/pos/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/pos/sales/1/pay - valid returns 200")
    void processPayment_returns200() throws Exception {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .paymentMethod(PaymentMethod.CASH)
                .amountTendered(new BigDecimal("500.00")).build();

        SaleDetailResponse response = SaleDetailResponse.builder()
                .id(1L).status("PAID").amountTendered(new BigDecimal("500.00"))
                .changeAmount(new BigDecimal("200.00")).build();

        when(saleService.processPayment(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/pos/sales/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    @DisplayName("POST /api/pos/sales/1/void - valid returns 200")
    void voidSale_returns200() throws Exception {
        VoidSaleRequest request = VoidSaleRequest.builder().voidReason("Customer cancelled").build();

        SaleDetailResponse response = SaleDetailResponse.builder()
                .id(1L).status("VOIDED").voidReason("Customer cancelled").build();

        when(saleService.voidSale(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/pos/sales/1/void")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VOIDED"));
    }

    @Test
    @DisplayName("GET /api/pos/sales/1/receipt - returns 200")
    void getReceipt_returns200() throws Exception {
        ReceiptResponse response = ReceiptResponse.builder()
                .saleNo("RCT-202608-00001").totalAmount(new BigDecimal("300.00")).build();

        when(saleService.getReceipt(1L)).thenReturn(response);

        mockMvc.perform(get("/api/pos/sales/1/receipt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.saleNo").value("RCT-202608-00001"));
    }

    @Test
    @DisplayName("GET /api/pos/sales/open - returns 200")
    void getOpenSales_returns200() throws Exception {
        when(saleService.getOpenSales()).thenReturn(List.of());

        mockMvc.perform(get("/api/pos/sales/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/pos/sales/1/close - returns 200")
    void closeSale_returns200() throws Exception {
        SaleDetailResponse response = SaleDetailResponse.builder().id(1L).status("CLOSED").build();
        when(saleService.closeSale(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/pos/sales/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }
}
