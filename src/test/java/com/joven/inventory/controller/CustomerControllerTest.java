package com.joven.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CustomerRequest;
import com.joven.inventory.dto.response.CustomerResponse;
import com.joven.inventory.exception.GlobalExceptionHandler;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.service.CustomerService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc tests for {@link CustomerController}.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("GET /api/customers - returns 200")
    void getAll_returns200() throws Exception {
        CustomerResponse c = CustomerResponse.builder().id(1L).name("Juan Dela Cruz").active(true).build();
        PageResponse<CustomerResponse> page = PageResponse.<CustomerResponse>builder()
                .content(List.of(c)).page(0).size(20).totalElements(1).totalPages(1).build();

        when(customerService.getAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Juan Dela Cruz"));
    }

    @Test
    @DisplayName("GET /api/customers/1 - returns 200")
    void getById_returns200() throws Exception {
        CustomerResponse c = CustomerResponse.builder().id(1L).name("Juan").build();
        when(customerService.getById(1L)).thenReturn(c);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("GET /api/customers/999 - not found returns 404")
    void getById_notFound_returns404() throws Exception {
        when(customerService.getById(999L)).thenThrow(new ResourceNotFoundException("Customer", "id", 999L));

        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/customers - valid returns 201")
    void create_validRequest_returns201() throws Exception {
        CustomerRequest request = CustomerRequest.builder().name("Juan Dela Cruz").email("juan@email.com").build();
        CustomerResponse response = CustomerResponse.builder().id(1L).name("Juan Dela Cruz").active(true).build();

        when(customerService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Juan Dela Cruz"));
    }

    @Test
    @DisplayName("PUT /api/customers/1 - returns 200")
    void update_returns200() throws Exception {
        CustomerRequest request = CustomerRequest.builder().name("Updated Name").build();
        CustomerResponse response = CustomerResponse.builder().id(1L).name("Updated Name").active(true).build();

        when(customerService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/customers/1/status - returns 200")
    void updateStatus_returns200() throws Exception {
        CustomerResponse response = CustomerResponse.builder().id(1L).name("Juan").active(false).build();
        when(customerService.updateStatus(eq(1L), eq(false))).thenReturn(response);

        mockMvc.perform(patch("/api/customers/1/status").param("active", "false"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/customers/search - returns 200")
    void search_returns200() throws Exception {
        PageResponse<CustomerResponse> page = PageResponse.<CustomerResponse>builder()
                .content(List.of()).page(0).size(20).totalElements(0).totalPages(0).build();

        when(customerService.search(eq("juan"), any())).thenReturn(page);

        mockMvc.perform(get("/api/customers/search").param("q", "juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
