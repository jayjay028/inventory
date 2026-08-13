package com.joven.inventory.mapper;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.response.StockResponse;
import com.joven.inventory.dto.response.StockTransactionResponse;
import com.joven.inventory.dto.response.StockTransactionResponse.AddonResponse;
import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Stock;
import com.joven.inventory.entity.StockTransaction;
import com.joven.inventory.entity.TransactionAddon;
import com.joven.inventory.repository.TransactionAddonRepository;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

/**
 * Utility class for mapping stock-related entities to their corresponding response DTOs.
 * Provides static conversion methods for {@link StockTransaction}, {@link Stock},
 * and paginated results.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class StockTransactionMapper {

    private StockTransactionMapper() {
        // Utility class — prevent instantiation
    }

    /**
     * Converts a {@link StockTransaction} entity and its associated add-ons
     * to a {@link StockTransactionResponse} DTO.
     *
     * @param entity the stock transaction entity to convert
     * @param addons the list of add-ons associated with the transaction
     * @return the corresponding stock transaction response DTO
     */
    public static StockTransactionResponse toResponse(StockTransaction entity, List<TransactionAddon> addons) {
        Item item = entity.getItem();

        List<AddonResponse> addonResponses = addons == null ? Collections.emptyList() : addons.stream()
                .map(addon -> AddonResponse.builder()
                        .id(addon.getId())
                        .addonName(addon.getAddonName())
                        .amount(addon.getAmount())
                        .build())
                .toList();

        return StockTransactionResponse.builder()
                .id(entity.getId())
                .itemId(item.getId())
                .itemCode(item.getItemCode())
                .itemName(item.getName())
                .transactionType(entity.getTransactionType().name())
                .status(entity.getStatus().name())
                .quantity(entity.getQuantity())
                .unitCost(entity.getUnitCost())
                .unitPrice(entity.getUnitPrice())
                .discountType(entity.getDiscountType().name())
                .discountValue(entity.getDiscountValue())
                .discountAmount(entity.getDiscountAmount())
                .subtotal(entity.getSubtotal())
                .netAmount(entity.getNetAmount())
                .taxEnabled(entity.getTaxEnabled())
                .taxType(entity.getTaxType() != null ? entity.getTaxType().name() : null)
                .taxRate(entity.getTaxRate())
                .taxAmount(entity.getTaxAmount())
                .vatableAmount(entity.getVatableAmount())
                .totalAmount(entity.getTotalAmount())
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null)
                .customerName(entity.getCustomer() != null ? entity.getCustomer().getName() : null)
                .supplierId(entity.getSupplier() != null ? entity.getSupplier().getId() : null)
                .supplierName(entity.getSupplier() != null ? entity.getSupplier().getName() : null)
                .documentType(entity.getDocumentType().name())
                .documentNo(entity.getDocumentNo())
                .referenceNo(entity.getReferenceNo())
                .remarks(entity.getRemarks())
                .transactionDate(entity.getTransactionDate())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .approvedBy(entity.getApprovedBy())
                .approvedAt(entity.getApprovedAt())
                .addons(addonResponses)
                .build();
    }

    /**
     * Converts a {@link Stock} entity to a {@link StockResponse} DTO.
     * Includes item details, category name, and a computed stock status indicator.
     * <ul>
     *     <li>{@code "OUT_OF_STOCK"} — quantity on hand is zero</li>
     *     <li>{@code "LOW"} — quantity on hand is at or below the reorder level</li>
     *     <li>{@code "NORMAL"} — quantity on hand is above the reorder level</li>
     * </ul>
     *
     * @param stock the stock entity to convert
     * @return the corresponding stock response DTO
     */
    public static StockResponse toStockResponse(Stock stock) {
        Item item = stock.getItem();
        Integer quantityOnHand = stock.getQuantityOnHand();

        String stockStatus;
        if (quantityOnHand == 0) {
            stockStatus = "OUT_OF_STOCK";
        } else if (quantityOnHand <= item.getReorderLevel()) {
            stockStatus = "LOW";
        } else {
            stockStatus = "NORMAL";
        }

        return StockResponse.builder()
                .id(stock.getId())
                .itemId(item.getId())
                .itemCode(item.getItemCode())
                .itemName(item.getName())
                .categoryName(item.getCategory().getName())
                .unit(item.getUnit())
                .quantityOnHand(quantityOnHand)
                .reorderLevel(item.getReorderLevel())
                .price(item.getPrice())
                .costPrice(item.getCostPrice())
                .lastUpdated(stock.getLastUpdated())
                .stockStatus(stockStatus)
                .build();
    }

    /**
     * Converts a {@link Page} of {@link Stock} entities to a {@link PageResponse} of {@link StockResponse} DTOs.
     *
     * @param page the page of stock entities
     * @return the page response containing stock response DTOs with pagination metadata
     */
    public static PageResponse<StockResponse> toStockPageResponse(Page<Stock> page) {
        Page<StockResponse> responsePage = page.map(StockTransactionMapper::toStockResponse);
        return PageResponse.of(responsePage);
    }

    /**
     * Converts a {@link Page} of {@link StockTransaction} entities to a {@link PageResponse}
     * of {@link StockTransactionResponse} DTOs. Fetches associated add-ons for each transaction
     * from the repository.
     *
     * @param page            the page of stock transaction entities
     * @param addonRepository the repository used to fetch add-ons for each transaction
     * @return the page response containing stock transaction response DTOs with pagination metadata
     */
    public static PageResponse<StockTransactionResponse> toTransactionPageResponse(
            Page<StockTransaction> page, TransactionAddonRepository addonRepository) {

        Page<StockTransactionResponse> responsePage = page.map(transaction -> {
            List<TransactionAddon> addons = addonRepository.findByTransactionId(transaction.getId());
            return toResponse(transaction, addons);
        });

        return PageResponse.of(responsePage);
    }
}
