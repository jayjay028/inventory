package com.joven.inventory.service.impl;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.StoreRequest;
import com.joven.inventory.dto.response.StoreResponse;
import com.joven.inventory.entity.Store;
import com.joven.inventory.exception.DuplicateResourceException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.StoreMapper;
import com.joven.inventory.repository.StoreRepository;
import com.joven.inventory.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link StoreService} providing store management operations.
 * Handles business logic, validation, and persistence for stores.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> getAll(Pageable pageable) {
        log.debug("Fetching all stores with pageable: {}", pageable);
        Page<Store> page = storeRepository.findAll(pageable);
        Page<StoreResponse> responsePage = page.map(StoreMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<StoreResponse> getAllActive() {
        log.debug("Fetching all active stores");
        return StoreMapper.toResponseList(storeRepository.findByActiveTrue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public StoreResponse getById(Long id) {
        log.debug("Fetching store by id: {}", id);
        Store store = findStoreById(id);
        return StoreMapper.toResponse(store);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoreResponse create(StoreRequest request) {
        log.info("Creating new store with code: {}", request.getCode());

        if (storeRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Store", "code", request.getCode());
        }

        Store store = new Store();
        applyRequest(store, request);
        store.setActive(true);

        Store savedStore = storeRepository.save(store);
        log.info("Store created successfully with id: {}", savedStore.getId());
        return StoreMapper.toResponse(savedStore);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoreResponse update(Long id, StoreRequest request) {
        log.info("Updating store with id: {}", id);

        Store store = findStoreById(id);

        storeRepository.findByCode(request.getCode())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Store", "code", request.getCode());
                });

        applyRequest(store, request);

        Store updatedStore = storeRepository.save(store);
        log.info("Store updated successfully with id: {}", updatedStore.getId());
        return StoreMapper.toResponse(updatedStore);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoreResponse updateStatus(Long id, boolean active) {
        log.info("Updating store status for id: {} to active: {}", id, active);

        Store store = findStoreById(id);
        store.setActive(active);

        Store updatedStore = storeRepository.save(store);
        log.info("Store status updated successfully for id: {}", updatedStore.getId());
        return StoreMapper.toResponse(updatedStore);
    }

    /**
     * Applies the values from a {@link StoreRequest} onto a {@link Store} entity.
     *
     * @param store   the store entity to update
     * @param request the request DTO containing new values
     */
    private void applyRequest(Store store, StoreRequest request) {
        store.setCode(request.getCode());
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setTin(request.getTin());
        store.setPhone(request.getPhone());
    }

    /**
     * Finds a store by ID or throws {@link ResourceNotFoundException}.
     *
     * @param id the store ID
     * @return the found store entity
     * @throws ResourceNotFoundException if no store exists with the given ID
     */
    private Store findStoreById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
    }
}
