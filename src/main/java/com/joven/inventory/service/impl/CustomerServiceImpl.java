package com.joven.inventory.service.impl;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CustomerRequest;
import com.joven.inventory.dto.response.CustomerResponse;
import com.joven.inventory.entity.Customer;
import com.joven.inventory.exception.DuplicateResourceException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.CustomerMapper;
import com.joven.inventory.repository.CustomerRepository;
import com.joven.inventory.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link CustomerService}.
 * Provides CRUD operations and search functionality for customers.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<CustomerResponse> getAll(Pageable pageable) {
        Page<Customer> page = customerRepository.findByActiveTrue(pageable);
        return CustomerMapper.toPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CustomerResponse getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return CustomerMapper.toResponse(customer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<CustomerResponse> search(String query, Pageable pageable) {
        Page<Customer> page = customerRepository.searchCustomers(query, pageable);
        return CustomerMapper.toPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByNameAndTin(request.getName(), request.getTin())) {
            throw new DuplicateResourceException("Customer", "name and TIN",
                    request.getName() + " / " + request.getTin());
        }

        Customer customer = new Customer();
        CustomerMapper.updateEntity(customer, request);
        customer.setActive(true);

        Customer saved = customerRepository.save(customer);
        return CustomerMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        CustomerMapper.updateEntity(customer, request);

        Customer saved = customerRepository.save(customer);
        return CustomerMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CustomerResponse updateStatus(Long id, boolean active) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customer.setActive(active);

        Customer saved = customerRepository.save(customer);
        return CustomerMapper.toResponse(saved);
    }
}
