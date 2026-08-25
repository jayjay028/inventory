package com.joven.inventory.service.impl;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.context.StoreContext;
import com.joven.inventory.dto.response.StockResponse;
import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Stock;
import com.joven.inventory.entity.Store;
import com.joven.inventory.exception.InsufficientStockException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.StockTransactionMapper;
import com.joven.inventory.repository.ItemRepository;
import com.joven.inventory.repository.StockRepository;
import com.joven.inventory.repository.StoreRepository;
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
    private final StoreRepository storeRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<StockResponse> getAll(Pageable pageable) {
        Page<Stock> page = stockRepository.findAllWithItemByStoreId(StoreContext.getStoreId(), pageable);
        return StockTransactionMapper.toStockPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StockResponse getByItemIdAndStore(Long itemId, Long storeId) {
        Stock stock = stockRepository.findByItemIdAndStoreId(itemId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found for item ID: " + itemId + " in store ID: " + storeId));
        return StockTransactionMapper.toStockResponse(stock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addStock(Long itemId, Long storeId, int quantity) {
        Stock stock = stockRepository.findByItemIdAndStoreId(itemId, storeId)
                .orElseGet(() -> createStock(itemId, storeId));

        stock.setQuantityOnHand(stock.getQuantityOnHand() + quantity);
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        log.info("Added {} units to item ID {} in store ID {}. New quantity: {}",
                quantity, itemId, storeId, stock.getQuantityOnHand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deductStock(Long itemId, Long storeId, int quantity) {
        Stock stock = stockRepository.findByItemIdAndStoreId(itemId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found for item ID: " + itemId + " in store ID: " + storeId));

        if (stock.getQuantityOnHand() < quantity) {
            throw new InsufficientStockException(itemId, stock.getQuantityOnHand(), quantity);
        }

        stock.setQuantityOnHand(stock.getQuantityOnHand() - quantity);
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        log.info("Deducted {} units from item ID {} in store ID {}. New quantity: {}",
                quantity, itemId, storeId, stock.getQuantityOnHand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void setStock(Long itemId, Long storeId, int quantity) {
        Stock stock = stockRepository.findByItemIdAndStoreId(itemId, storeId)
                .orElseGet(() -> createStock(itemId, storeId));

        int previousQuantity = stock.getQuantityOnHand();
        stock.setQuantityOnHand(quantity);
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        log.info("Adjusted stock for item ID {} in store ID {} from {} to {}",
                itemId, storeId, previousQuantity, quantity);
    }

    /**
     * Creates a new stock record for the given item and store with a zero
     * starting quantity. The caller is responsible for setting the quantity
     * and persisting the record.
     *
     * @param itemId  the ID of the item
     * @param storeId the ID of the store
     * @return a new, unsaved {@link Stock} instance with item and store set
     * @throws ResourceNotFoundException if the item or store cannot be found
     */
    private Stock createStock(Long itemId, Long storeId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        Stock stock = new Stock();
        stock.setItem(item);
        stock.setStore(store);
        stock.setQuantityOnHand(0);
        return stock;
    }
}
