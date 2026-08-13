package com.joven.inventory.mapper;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.response.ItemResponse;
import com.joven.inventory.entity.Item;
import org.springframework.data.domain.Page;

/**
 * Utility class for mapping {@link Item} entities to {@link ItemResponse} DTOs.
 * Provides static conversion methods for single entities and paginated results.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class ItemMapper {

    private ItemMapper() {
        // Utility class — prevent instantiation
    }

    /**
     * Converts an {@link Item} entity to an {@link ItemResponse} DTO.
     * Includes the associated category name from the item's category relationship.
     *
     * @param item the item entity to convert
     * @return the corresponding item response DTO
     */
    public static ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .name(item.getName())
                .description(item.getDescription())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .unit(item.getUnit())
                .price(item.getPrice())
                .costPrice(item.getCostPrice())
                .reorderLevel(item.getReorderLevel())
                .taxable(item.getTaxable())
                .active(item.getActive())
                .createdBy(item.getCreatedBy())
                .createdAt(item.getCreatedAt())
                .updatedBy(item.getUpdatedBy())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    /**
     * Converts a {@link Page} of {@link Item} entities to a {@link PageResponse} of {@link ItemResponse} DTOs.
     *
     * @param page the page of item entities
     * @return the page response containing item response DTOs with pagination metadata
     */
    public static PageResponse<ItemResponse> toPageResponse(Page<Item> page) {
        Page<ItemResponse> responsePage = page.map(ItemMapper::toResponse);
        return PageResponse.of(responsePage);
    }
}
