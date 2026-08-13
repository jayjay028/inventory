package com.joven.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.StockAdjustRequest;
import com.joven.inventory.dto.request.StockInRequest;
import com.joven.inventory.dto.request.StockOutRequest;
import com.joven.inventory.dto.response.StockResponse;
import com.joven.inventory.dto.response.StockTransactionResponse;
import com.joven.inventory.exception.BusinessRuleException;
import com.joven.inventory.exception.GlobalExceptionHandler;
import com.joven.inventory.service.StockService;
import com.joven.inventory.service.StockTransactionService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc tests for {@link StockController}.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private StockService stockService;

    @Mock
    private StockTransactionService stockTransactionService;

    @InjectMocks
    private StockController stockController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stockController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("GET /api/stock - returns 200")
    void getAllStock_returns200() throws Exception {
        StockResponse s = StockResponse.builder().id(1L).itemId(1L).itemName("USB Cable").quantityOnHand(100).build();
        PageResponse<StockResponse> page = PageResponse.<StockResponse>builder()
                .content(List.of(s)).page(0).size(20).totalElements(1).totalPages(1).build();

        when(stockService.getAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].itemName").value("USB Cable"));
    }

    @Test
    @DisplayName("GET /api/stock/1 - returns 200")
    void getStockByItemId_returns200() throws Exception {
        StockResponse s = StockResponse.builder().id(1L).itemId(1L).quantityOnHand(50).build();
        when(stockService.getByItemId(1L)).thenReturn(s);

        mockMvc.perform(get("/api/stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantityOnHand").value(50));
    }

    @Test
    @DisplayName("POST /api/stock/in - valid returns 201")
    void createStockIn_returns201() throws Exception {
        StockInRequest request = StockInRequest.builder()
                .itemId(1L).quantity(10).unitCost(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.of(2026, 8, 13, 10, 0)).build();

        StockTransactionResponse response = StockTransactionResponse.builder()
                .id(1L).transactionType("IN").status("CREATED").quantity(10).build();

        when(stockTransactionService.createStockIn(any())).thenReturn(response);

        mockMvc.perform(post("/api/stock/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transactionType").value("IN"));
    }

    @Test
    @DisplayName("POST /api/stock/out - valid returns 201")
    void createStockOut_returns201() throws Exception {
        StockOutRequest request = StockOutRequest.builder()
                .itemId(1L).quantity(5).unitPrice(new BigDecimal("150.00"))
                .transactionDate(LocalDateTime.of(2026, 8, 13, 11, 0)).build();

        StockTransactionResponse response = StockTransactionResponse.builder()
                .id(1L).transactionType("OUT").status("CREATED").quantity(5).build();

        when(stockTransactionService.createStockOut(any())).thenReturn(response);

        mockMvc.perform(post("/api/stock/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transactionType").value("OUT"));
    }

    @Test
    @DisplayName("POST /api/stock/adjust - valid returns 201")
    void createStockAdjust_returns201() throws Exception {
        StockAdjustRequest request = StockAdjustRequest.builder()
                .itemId(1L).quantity(50)
                .transactionDate(LocalDateTime.of(2026, 8, 13, 12, 0)).build();

        StockTransactionResponse response = StockTransactionResponse.builder()
                .id(1L).transactionType("ADJUSTMENT").status("CREATED").quantity(50).build();

        when(stockTransactionService.createStockAdjust(any())).thenReturn(response);

        mockMvc.perform(post("/api/stock/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PATCH /api/stock/transactions/1/approve - returns 200")
    void approveTransaction_returns200() throws Exception {
        StockTransactionResponse response = StockTransactionResponse.builder().id(1L).status("APPROVED").build();
        when(stockTransactionService.approve(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/stock/transactions/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("PATCH /api/stock/transactions/1/cancel - returns 200")
    void cancelTransaction_returns200() throws Exception {
        StockTransactionResponse response = StockTransactionResponse.builder().id(1L).status("CANCELLED").build();
        when(stockTransactionService.cancel(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/stock/transactions/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PATCH /api/stock/transactions/1/approve - already approved returns 422")
    void approveTransaction_alreadyApproved_returns422() throws Exception {
        when(stockTransactionService.approve(1L))
                .thenThrow(new BusinessRuleException("Cannot approve transaction with status: APPROVED"));

        mockMvc.perform(patch("/api/stock/transactions/1/approve"))
                .andExpect(status().isUnprocessableEntity());
    }
}
