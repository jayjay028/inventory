package com.joven.inventory.service.impl;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.ItemRequest;
import com.joven.inventory.dto.response.ItemResponse;
import com.joven.inventory.entity.Category;
import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Stock;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.ItemMapper;
import com.joven.inventory.repository.CategoryRepository;
import com.joven.inventory.repository.ItemRepository;
import com.joven.inventory.repository.StockRepository;
import com.joven.inventory.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of {@link ItemService} providing inventory item management operations.
 * Handles item creation with auto-generated item codes, stock initialization,
 * and all CRUD operations.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> getAll(Pageable pageable) {
        Page<Item> page = itemRepository.findByActiveTrue(pageable);
        return ItemMapper.toPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> getAll(Long categoryId, Pageable pageable) {
        Page<Item> page = itemRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        return ItemMapper.toPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ItemResponse getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return ItemMapper.toResponse(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> search(String query, Pageable pageable) {
        Page<Item> page = itemRepository.searchItems(query, pageable);
        return ItemMapper.toPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> getLowStock(Pageable pageable) {
        Page<Item> page = itemRepository.findLowStockItems(pageable);
        return ItemMapper.toPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemResponse create(ItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Item item = new Item();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(category);
        item.setUnit(request.getUnit());
        item.setPrice(request.getPrice());
        item.setCostPrice(request.getCostPrice());
        item.setReorderLevel(request.getReorderLevel());
        item.setTaxable(request.getTaxable());
        item.setActive(true);

        // Save first to obtain generated ID
        item.setItemCode("TEMP");
        item = itemRepository.save(item);

        // Generate item code using the ID and save again
        String itemCode = "ITM-" + String.format("%05d", item.getId());
        item.setItemCode(itemCode);
        item = itemRepository.save(item);

        // Create stock record with initial quantity of 0
        Stock stock = new Stock();
        stock.setItem(item);
        stock.setQuantityOnHand(0);
        stock.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock);

        log.info("Created item '{}' with code '{}'", item.getName(), itemCode);

        return ItemMapper.toResponse(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(category);
        item.setUnit(request.getUnit());
        item.setPrice(request.getPrice());
        item.setCostPrice(request.getCostPrice());
        item.setReorderLevel(request.getReorderLevel());
        item.setTaxable(request.getTaxable());

        item = itemRepository.save(item);

        log.info("Updated item '{}' (id={})", item.getName(), id);

        return ItemMapper.toResponse(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemResponse updateStatus(Long id, boolean active) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        item.setActive(active);
        item = itemRepository.save(item);

        log.info("Item '{}' (id={}) status changed to active={}", item.getName(), id, active);

        return ItemMapper.toResponse(item);
    }
}
