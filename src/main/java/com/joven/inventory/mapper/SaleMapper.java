package com.joven.inventory.mapper;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.response.ReceiptResponse;
import com.joven.inventory.dto.response.SaleDetailResponse;
import com.joven.inventory.dto.response.SaleResponse;
import com.joven.inventory.entity.Sale;
import com.joven.inventory.entity.SaleAddon;
import com.joven.inventory.entity.SaleItem;
import com.joven.inventory.entity.SalePayment;
import com.joven.inventory.enums.DiscountType;
import com.joven.inventory.enums.TaxType;
import com.joven.inventory.repository.SaleItemRepository;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between {@link Sale} entity and POS response DTOs.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class SaleMapper {

    private SaleMapper() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a {@link Sale} entity to a {@link SaleResponse} summary DTO.
     *
     * @param sale      the sale entity
     * @param itemCount the number of line items in the sale
     * @return the sale response DTO
     */
    public static SaleResponse toResponse(Sale sale, int itemCount) {
        return SaleResponse.builder()
                .id(sale.getId())
                .saleNo(sale.getSaleNo())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getId() : null)
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getName() : null)
                .shiftId(sale.getShift() != null ? sale.getShift().getId() : null)
                .subtotal(sale.getSubtotal())
                .discountType(sale.getDiscountType().name())
                .discountAmount(sale.getDiscountAmount())
                .addonsTotal(sale.getAddonsTotal())
                .netAmount(sale.getNetAmount())
                .taxEnabled(sale.getTaxEnabled())
                .taxAmount(sale.getTaxAmount())
                .totalAmount(sale.getTotalAmount())
                .amountTendered(sale.getAmountTendered())
                .changeAmount(sale.getChangeAmount())
                .paymentMethod(sale.getPaymentMethod().name())
                .status(sale.getStatus().name())
                .documentType(sale.getDocumentType().name())
                .documentNo(sale.getDocumentNo())
                .remarks(sale.getRemarks())
                .saleDate(sale.getSaleDate())
                .createdBy(sale.getCreatedBy())
                .createdAt(sale.getCreatedAt())
                .itemCount(itemCount)
                .build();
    }

    /**
     * Converts a {@link Sale} entity with its related collections to a {@link SaleDetailResponse} DTO.
     *
     * @param sale     the sale entity
     * @param items    the list of sale line items
     * @param addons   the list of sale add-ons
     * @param payments the list of sale payments
     * @return the sale detail response DTO
     */
    public static SaleDetailResponse toDetailResponse(Sale sale, List<SaleItem> items,
                                                      List<SaleAddon> addons, List<SalePayment> payments) {
        List<SaleDetailResponse.SaleItemResponse> itemResponses = items.stream()
                .map(SaleMapper::toSaleItemResponse)
                .collect(Collectors.toList());

        List<SaleDetailResponse.SaleAddonResponse> addonResponses = addons.stream()
                .map(SaleMapper::toSaleAddonResponse)
                .collect(Collectors.toList());

        List<SaleDetailResponse.SalePaymentResponse> paymentResponses = payments.stream()
                .map(SaleMapper::toSalePaymentResponse)
                .collect(Collectors.toList());

        return SaleDetailResponse.builder()
                .id(sale.getId())
                .saleNo(sale.getSaleNo())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getId() : null)
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getName() : null)
                .shiftId(sale.getShift() != null ? sale.getShift().getId() : null)
                .subtotal(sale.getSubtotal())
                .discountType(sale.getDiscountType().name())
                .discountAmount(sale.getDiscountAmount())
                .addonsTotal(sale.getAddonsTotal())
                .netAmount(sale.getNetAmount())
                .taxEnabled(sale.getTaxEnabled())
                .taxAmount(sale.getTaxAmount())
                .totalAmount(sale.getTotalAmount())
                .amountTendered(sale.getAmountTendered())
                .changeAmount(sale.getChangeAmount())
                .paymentMethod(sale.getPaymentMethod().name())
                .status(sale.getStatus().name())
                .documentType(sale.getDocumentType().name())
                .documentNo(sale.getDocumentNo())
                .remarks(sale.getRemarks())
                .saleDate(sale.getSaleDate())
                .createdBy(sale.getCreatedBy())
                .createdAt(sale.getCreatedAt())
                .itemCount(items.size())
                .discountValue(sale.getDiscountValue())
                .taxType(sale.getTaxType() != null ? sale.getTaxType().name() : null)
                .taxRate(sale.getTaxRate())
                .vatableAmount(sale.getVatableAmount())
                .voidReason(sale.getVoidReason())
                .voidedBy(sale.getVoidedBy())
                .voidedAt(sale.getVoidedAt())
                .items(itemResponses)
                .addons(addonResponses)
                .payments(paymentResponses)
                .build();
    }

    /**
     * Converts a {@link Sale} entity with its items and payments to a {@link ReceiptResponse} DTO
     * formatted for receipt printing.
     *
     * @param sale     the sale entity
     * @param items    the list of sale line items
     * @param payments the list of sale payments
     * @param settings a map of system settings containing business info and receipt configuration
     * @return the receipt response DTO
     */
    public static ReceiptResponse toReceiptResponse(Sale sale, List<SaleItem> items,
                                                    List<SalePayment> payments, Map<String, String> settings) {
        List<ReceiptResponse.ReceiptItem> receiptItems = items.stream()
                .map(SaleMapper::toReceiptItem)
                .collect(Collectors.toList());

        List<ReceiptResponse.ReceiptPayment> receiptPayments = payments.stream()
                .map(SaleMapper::toReceiptPayment)
                .collect(Collectors.toList());

        String discountLabel = buildDiscountLabel(sale.getDiscountType(), sale.getDiscountValue());

        BigDecimal vatExemptSales = sale.getTaxType() == TaxType.EXEMPT ? sale.getNetAmount() : BigDecimal.ZERO;
        BigDecimal zeroRatedSales = sale.getTaxType() == TaxType.ZERO_RATED ? sale.getNetAmount() : BigDecimal.ZERO;

        String vatRegisteredStr = settings.get("vat_registered");
        Boolean vatRegistered = vatRegisteredStr != null ? Boolean.valueOf(vatRegisteredStr) : null;

        return ReceiptResponse.builder()
                .businessName(settings.get("business_name"))
                .businessTin(settings.get("business_tin"))
                .businessAddress(settings.get("business_address"))
                .businessContact(settings.get("business_contact"))
                .vatRegistered(vatRegistered)
                .saleNo(sale.getSaleNo())
                .documentType(sale.getDocumentType().name())
                .documentNo(sale.getDocumentNo())
                .saleDate(sale.getSaleDate())
                .cashierName(sale.getCreatedBy())
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getName() : null)
                .customerTin(sale.getCustomer() != null ? sale.getCustomer().getTin() : null)
                .items(receiptItems)
                .subtotal(sale.getSubtotal())
                .discountLabel(discountLabel)
                .discountAmount(sale.getDiscountAmount())
                .addonsTotal(sale.getAddonsTotal())
                .vatableSales(sale.getVatableAmount())
                .vatAmount(sale.getTaxAmount())
                .vatExemptSales(vatExemptSales)
                .zeroRatedSales(zeroRatedSales)
                .totalAmount(sale.getTotalAmount())
                .paymentMethod(sale.getPaymentMethod().name())
                .amountTendered(sale.getAmountTendered())
                .changeAmount(sale.getChangeAmount())
                .payments(receiptPayments)
                .receiptFooter(settings.get("pos_receipt_footer"))
                .build();
    }

    /**
     * Converts a {@link Page} of {@link Sale} entities to a {@link PageResponse} of {@link SaleResponse}.
     * Counts items for each sale using the provided repository.
     *
     * @param page               the page of sale entities
     * @param saleItemRepository the sale item repository for counting items
     * @return the page response containing sale response DTOs
     */
    public static PageResponse<SaleResponse> toPageResponse(Page<Sale> page, SaleItemRepository saleItemRepository) {
        Page<SaleResponse> responsePage = page.map(sale -> {
            int itemCount = saleItemRepository.findBySaleId(sale.getId()).size();
            return toResponse(sale, itemCount);
        });
        return PageResponse.of(responsePage);
    }

    /**
     * Builds a human-readable discount label from the discount type and value.
     *
     * @param discountType  the discount type enum
     * @param discountValue the discount value (percentage or fixed amount)
     * @return the formatted discount label, or null if no discount
     */
    private static String buildDiscountLabel(DiscountType discountType, BigDecimal discountValue) {
        if (discountType == null) {
            return null;
        }
        return switch (discountType) {
            case SENIOR_PWD -> "SC/PWD 20%";
            case PERCENTAGE -> discountValue + "% Discount";
            case FIXED -> "Discount";
            case NONE -> null;
        };
    }

    /**
     * Converts a {@link SaleItem} entity to a {@link SaleDetailResponse.SaleItemResponse}.
     *
     * @param item the sale item entity
     * @return the sale item response DTO
     */
    private static SaleDetailResponse.SaleItemResponse toSaleItemResponse(SaleItem item) {
        return SaleDetailResponse.SaleItemResponse.builder()
                .id(item.getId())
                .itemId(item.getItem() != null ? item.getItem().getId() : null)
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .quantity(BigDecimal.valueOf(item.getQuantity()))
                .unitPrice(item.getUnitPrice())
                .unitCost(item.getUnitCost())
                .discountType(item.getDiscountType().name())
                .discountValue(item.getDiscountValue())
                .discountAmount(item.getDiscountAmount())
                .lineTotal(item.getLineTotal())
                .build();
    }

    /**
     * Converts a {@link SaleAddon} entity to a {@link SaleDetailResponse.SaleAddonResponse}.
     *
     * @param addon the sale addon entity
     * @return the sale addon response DTO
     */
    private static SaleDetailResponse.SaleAddonResponse toSaleAddonResponse(SaleAddon addon) {
        return SaleDetailResponse.SaleAddonResponse.builder()
                .id(addon.getId())
                .addonName(addon.getAddonName())
                .amount(addon.getAmount())
                .build();
    }

    /**
     * Converts a {@link SalePayment} entity to a {@link SaleDetailResponse.SalePaymentResponse}.
     *
     * @param payment the sale payment entity
     * @return the sale payment response DTO
     */
    private static SaleDetailResponse.SalePaymentResponse toSalePaymentResponse(SalePayment payment) {
        return SaleDetailResponse.SalePaymentResponse.builder()
                .id(payment.getId())
                .paymentMethod(payment.getPaymentMethod().name())
                .amount(payment.getAmount())
                .referenceNo(payment.getReferenceNo())
                .build();
    }

    /**
     * Converts a {@link SaleItem} entity to a {@link ReceiptResponse.ReceiptItem}.
     *
     * @param item the sale item entity
     * @return the receipt item DTO
     */
    private static ReceiptResponse.ReceiptItem toReceiptItem(SaleItem item) {
        return ReceiptResponse.ReceiptItem.builder()
                .itemName(item.getItemName())
                .quantity(BigDecimal.valueOf(item.getQuantity()))
                .unitPrice(item.getUnitPrice())
                .amount(item.getLineTotal())
                .build();
    }

    /**
     * Converts a {@link SalePayment} entity to a {@link ReceiptResponse.ReceiptPayment}.
     *
     * @param payment the sale payment entity
     * @return the receipt payment DTO
     */
    private static ReceiptResponse.ReceiptPayment toReceiptPayment(SalePayment payment) {
        return ReceiptResponse.ReceiptPayment.builder()
                .method(payment.getPaymentMethod().name())
                .amount(payment.getAmount())
                .referenceNo(payment.getReferenceNo())
                .build();
    }
}
