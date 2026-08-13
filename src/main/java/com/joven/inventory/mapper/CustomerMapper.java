package com.joven.inventory.mapper;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CustomerRequest;
import com.joven.inventory.dto.response.CustomerResponse;
import com.joven.inventory.entity.Customer;
import org.springframework.data.domain.Page;

/**
 * Utility class for mapping between {@link Customer} entity and DTOs.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class CustomerMapper {

    private CustomerMapper() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a {@link Customer} entity to a {@link CustomerResponse} DTO.
     *
     * @param customer the customer entity
     * @return the customer response DTO
     */
    public static CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .tin(customer.getTin())
                .address(customer.getAddress())
                .contactPerson(customer.getContactPerson())
                .contactNumber(customer.getContactNumber())
                .email(customer.getEmail())
                .active(customer.getActive())
                .createdBy(customer.getCreatedBy())
                .createdAt(customer.getCreatedAt())
                .updatedBy(customer.getUpdatedBy())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    /**
     * Converts a {@link Page} of {@link Customer} entities to a {@link PageResponse} of {@link CustomerResponse}.
     *
     * @param page the page of customer entities
     * @return the page response containing customer response DTOs
     */
    public static PageResponse<CustomerResponse> toPageResponse(Page<Customer> page) {
        Page<CustomerResponse> responsePage = page.map(CustomerMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    /**
     * Updates the fields of an existing {@link Customer} entity from a {@link CustomerRequest} DTO.
     *
     * @param customer the customer entity to update
     * @param request  the customer request DTO containing updated values
     */
    public static void updateEntity(Customer customer, CustomerRequest request) {
        customer.setName(request.getName());
        customer.setTin(request.getTin());
        customer.setAddress(request.getAddress());
        customer.setContactPerson(request.getContactPerson());
        customer.setContactNumber(request.getContactNumber());
        customer.setEmail(request.getEmail());
    }
}
