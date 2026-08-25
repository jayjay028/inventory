package com.joven.inventory.mapper;

import com.joven.inventory.dto.response.StoreResponse;
import com.joven.inventory.entity.Store;

import java.util.List;

/**
 * Utility mapper class for converting between {@link Store} entities
 * and their corresponding DTOs.
 *
 * <p>This class uses static methods and cannot be instantiated.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class StoreMapper {

    /**
     * Private constructor to prevent instantiation.
     */
    private StoreMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a {@link Store} entity to a {@link StoreResponse} DTO.
     *
     * @param store the store entity to convert
     * @return the corresponding store response DTO
     */
    public static StoreResponse toResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .code(store.getCode())
                .name(store.getName())
                .address(store.getAddress())
                .tin(store.getTin())
                .phone(store.getPhone())
                .active(store.getActive())
                .build();
    }

    /**
     * Converts a list of {@link Store} entities to a list of {@link StoreResponse} DTOs.
     *
     * @param stores the list of store entities
     * @return the list of store response DTOs
     */
    public static List<StoreResponse> toResponseList(List<Store> stores) {
        return stores.stream()
                .map(StoreMapper::toResponse)
                .toList();
    }
}
