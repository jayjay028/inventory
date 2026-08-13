package com.joven.inventory.service.impl;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.SupplierRequest;
import com.joven.inventory.dto.response.SupplierResponse;
import com.joven.inventory.entity.Supplier;
import com.joven.inventory.exception.DuplicateResourceException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.SupplierMapper;
import com.joven.inventory.repository.SupplierRepository;
import com.joven.inventory.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link SupplierService}.
 * Provides CRUD operations and search functionality for suppliers.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<SupplierResponse> getAll(Pageable pageable) {
        Page<Supplier> page = supplierRepository.findByActiveTrue(pageable);
        return SupplierMapper.toPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SupplierResponse getById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        return SupplierMapper.toResponse(supplier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<SupplierResponse> search(String query, Pageable pageable) {
        Page<Supplier> page = supplierRepository.searchSuppliers(query, pageable);
        return SupplierMapper.toPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (supplierRepository.existsByNameAndTin(request.getName(), request.getTin())) {
            throw new DuplicateResourceException("Supplier", "name and TIN",
                    request.getName() + " / " + request.getTin());
        }

        Supplier supplier = new Supplier();
        SupplierMapper.updateEntity(supplier, request);
        supplier.setActive(true);

        Supplier saved = supplierRepository.save(supplier);
        return SupplierMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        SupplierMapper.updateEntity(supplier, request);

        Supplier saved = supplierRepository.save(supplier);
        return SupplierMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SupplierResponse updateStatus(Long id, boolean active) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        supplier.setActive(active);

        Supplier saved = supplierRepository.save(supplier);
        return SupplierMapper.toResponse(saved);
    }
}
