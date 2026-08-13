package com.joven.inventory.service.impl;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.response.StockResponse;
import com.joven.inventory.entity.Stock;
import com.joven.inventory.exception.InsufficientStockException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.StockTransactionMapper;
import com.joven.inventory.repository.ItemRepository;
import com.joven.inventory.repository.StockRepository;
import com.joven.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of {@link StockService} providing stock level query and modification operations.
 * Manages current inventory quantities for items and validates stock sufficiency on deductions.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final ItemRepository itemRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<StockResponse> getAll(Pageable pageable) {
        Page<Stock> page = stockRepository.findAllWithItem(pageable);
        return StockTransactionMapper.toStockPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StockResponse getByItemId(Long itemId) {
        Stock stock = stockRepository.findByItemId(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for item ID: " + itemId));
        return StockTransactionMapper.toStockResponse(stock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addStock(Long itemId, int quantity) {
        Stock stock = stockRepository.findByItemId(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for item ID: " + itemId));

        stock.setQuantityOnHand(stock.getQuantityOnHand() + quantity);
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        log.info("Added {} units to item ID {}. New quantity: {}", quantity, itemId, stock.getQuantityOnHand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deductStock(Long itemId, int quantity) {
        Stock stock = stockRepository.findByItemId(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for item ID: " + itemId));

        if (stock.getQuantityOnHand() < quantity) {
            throw new InsufficientStockException(itemId, stock.getQuantityOnHand(), quantity);
        }

        stock.setQuantityOnHand(stock.getQuantityOnHand() - quantity);
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        log.info("Deducted {} units from item ID {}. New quantity: {}", quantity, itemId, stock.getQuantityOnHand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void setStock(Long itemId, int quantity) {
        Stock stock = stockRepository.findByItemId(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for item ID: " + itemId));

        int previousQuantity = stock.getQuantityOnHand();
        stock.setQuantityOnHand(quantity);
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        log.info("Adjusted stock for item ID {} from {} to {}", itemId, previousQuantity, quantity);
    }
}
