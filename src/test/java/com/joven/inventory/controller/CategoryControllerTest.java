package com.joven.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CategoryRequest;
import com.joven.inventory.dto.response.CategoryResponse;
import com.joven.inventory.exception.GlobalExceptionHandler;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.service.CategoryService;
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
 * Standalone MockMvc tests for {@link CategoryController}.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("GET /api/categories - returns 200 with paged results")
    void getAll_returns200() throws Exception {
        CategoryResponse cat = CategoryResponse.builder().id(1L).name("Electronics").active(true).build();
        PageResponse<CategoryResponse> page = PageResponse.<CategoryResponse>builder()
                .content(List.of(cat)).page(0).size(20).totalElements(1).totalPages(1).build();

        when(categoryService.getAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/categories/1 - returns 200")
    void getById_returns200() throws Exception {
        CategoryResponse cat = CategoryResponse.builder().id(1L).name("Electronics").active(true).build();
        when(categoryService.getById(1L)).thenReturn(cat);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("GET /api/categories/999 - not found returns 404")
    void getById_notFound_returns404() throws Exception {
        when(categoryService.getById(999L)).thenThrow(new ResourceNotFoundException("Category", "id", 999L));

        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/categories - valid returns 201")
    void create_validRequest_returns201() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        CategoryResponse response = CategoryResponse.builder().id(1L).name("Electronics").active(true).build();
        when(categoryService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Electronics"));
    }

    @Test
    @DisplayName("PUT /api/categories/1 - returns 200")
    void update_returns200() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated");

        CategoryResponse response = CategoryResponse.builder().id(1L).name("Updated").active(true).build();
        when(categoryService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated"));
    }

    @Test
    @DisplayName("PATCH /api/categories/1/status - returns 200")
    void updateStatus_returns200() throws Exception {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Electronics").active(false).build();
        when(categoryService.updateStatus(eq(1L), eq(false))).thenReturn(response);

        mockMvc.perform(patch("/api/categories/1/status").param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }
}
