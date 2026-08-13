package com.joven.inventory.mapper;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.SupplierRequest;
import com.joven.inventory.dto.response.SupplierResponse;
import com.joven.inventory.entity.Supplier;
import org.springframework.data.domain.Page;

/**
 * Utility class for mapping between {@link Supplier} entity and DTOs.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class SupplierMapper {

    private SupplierMapper() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a {@link Supplier} entity to a {@link SupplierResponse} DTO.
     *
     * @param supplier the supplier entity
     * @return the supplier response DTO
     */
    public static SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .tin(supplier.getTin())
                .address(supplier.getAddress())
                .contactPerson(supplier.getContactPerson())
                .contactNumber(supplier.getContactNumber())
                .email(supplier.getEmail())
                .active(supplier.getActive())
                .createdBy(supplier.getCreatedBy())
                .createdAt(supplier.getCreatedAt())
                .updatedBy(supplier.getUpdatedBy())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }

    /**
     * Converts a {@link Page} of {@link Supplier} entities to a {@link PageResponse} of {@link SupplierResponse}.
     *
     * @param page the page of supplier entities
     * @return the page response containing supplier response DTOs
     */
    public static PageResponse<SupplierResponse> toPageResponse(Page<Supplier> page) {
        Page<SupplierResponse> responsePage = page.map(SupplierMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    /**
     * Updates the fields of an existing {@link Supplier} entity from a {@link SupplierRequest} DTO.
     *
     * @param supplier the supplier entity to update
     * @param request  the supplier request DTO containing updated values
     */
    public static void updateEntity(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.getName());
        supplier.setTin(request.getTin());
        supplier.setAddress(request.getAddress());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setContactNumber(request.getContactNumber());
        supplier.setEmail(request.getEmail());
    }
}
