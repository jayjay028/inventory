package com.joven.inventory.service.impl;

import com.joven.inventory.dto.response.StockResponse;
import com.joven.inventory.entity.Category;
import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Stock;
import com.joven.inventory.exception.InsufficientStockException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.repository.ItemRepository;
import com.joven.inventory.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StockServiceImpl}.
 * Tests stock query, addition, deduction, and set operations with proper validation.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    // ======================== getByItemId ========================

    @Test
    @DisplayName("getByItemId - given existing item - returns stock response with correct fields")
    void getByItemId_givenExistingItem_returnsStockResponse() {
        // Arrange
        Stock stock = createStock(50);

        when(stockRepository.findByItemId(1L)).thenReturn(Optional.of(stock));

        // Act
        StockResponse response = stockService.getByItemId(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItemId()).isEqualTo(1L);
        assertThat(response.getItemCode()).isEqualTo("ITM-001");
        assertThat(response.getItemName()).isEqualTo("Test Item");
        assertThat(response.getCategoryName()).isEqualTo("Cat");
        assertThat(response.getUnit()).isEqualTo("pcs");
        assertThat(response.getQuantityOnHand()).isEqualTo(50);
        assertThat(response.getReorderLevel()).isEqualTo(10);
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(response.getCostPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getStockStatus()).isEqualTo("NORMAL");
    }

    @Test
    @DisplayName("getByItemId - given non-existent item - throws ResourceNotFoundException")
    void getByItemId_givenNonExistentItem_throwsResourceNotFoundException() {
        // Arrange
        when(stockRepository.findByItemId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> stockService.getByItemId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Stock not found for item ID: 999");
    }

    // ======================== addStock ========================

    @Test
    @DisplayName("addStock - given valid item ID - increases quantity on hand")
    void addStock_givenValidItemId_increasesQuantity() {
        // Arrange
        Stock stock = createStock(100);

        when(stockRepository.findByItemId(1L)).thenReturn(Optional.of(stock));

        // Act
        stockService.addStock(1L, 50);

        // Assert
        assertThat(stock.getQuantityOnHand()).isEqualTo(150);
        verify(stockRepository).save(stock);
    }

    // ======================== deductStock ========================

    @Test
    @DisplayName("deductStock - given sufficient quantity - decreases quantity on hand")
    void deductStock_givenSufficientQuantity_decreasesQuantity() {
        // Arrange
        Stock stock = createStock(100);

        when(stockRepository.findByItemId(1L)).thenReturn(Optional.of(stock));

        // Act
        stockService.deductStock(1L, 30);

        // Assert
        assertThat(stock.getQuantityOnHand()).isEqualTo(70);
        verify(stockRepository).save(stock);
    }

    @Test
    @DisplayName("deductStock - given insufficient quantity - throws InsufficientStockException")
    void deductStock_givenInsufficientQuantity_throwsInsufficientStockException() {
        // Arrange
        Stock stock = createStock(10);

        when(stockRepository.findByItemId(1L)).thenReturn(Optional.of(stock));

        // Act & Assert
        assertThatThrownBy(() -> stockService.deductStock(1L, 50))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock for item ID 1");

        verify(stockRepository, never()).save(stock);
    }

    @Test
    @DisplayName("deductStock - given non-existent item - throws ResourceNotFoundException")
    void deductStock_givenNonExistentItem_throwsResourceNotFoundException() {
        // Arrange
        when(stockRepository.findByItemId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> stockService.deductStock(999L, 10))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Stock not found for item ID: 999");
    }

    // ======================== setStock ========================

    @Test
    @DisplayName("setStock - given valid item ID - sets quantity to specified value")
    void setStock_givenValidItemId_setsQuantity() {
        // Arrange
        Stock stock = createStock(100);

        when(stockRepository.findByItemId(1L)).thenReturn(Optional.of(stock));

        // Act
        stockService.setStock(1L, 75);

        // Assert
        assertThat(stock.getQuantityOnHand()).isEqualTo(75);
        verify(stockRepository).save(stock);
    }

    // ======================== Helper methods ========================

    /**
     * Creates a Stock entity with a fully populated Item and Category for testing.
     *
     * @param quantityOnHand the quantity on hand to set
     * @return the configured Stock entity
     */
    private Stock createStock(int quantityOnHand) {
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

        Stock stock = new Stock();
        stock.setId(1L);
        stock.setItem(item);
        stock.setQuantityOnHand(quantityOnHand);
        stock.setLastUpdated(LocalDateTime.now());

        return stock;
    }
}
