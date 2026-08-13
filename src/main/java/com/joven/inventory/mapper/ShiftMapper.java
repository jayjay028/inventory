package com.joven.inventory.mapper;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.response.ShiftResponse;
import com.joven.inventory.dto.response.ShiftSummaryResponse;
import com.joven.inventory.entity.Shift;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

/**
 * Utility class for mapping between {@link Shift} entity and shift response DTOs.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class ShiftMapper {

    private ShiftMapper() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a {@link Shift} entity to a {@link ShiftResponse} DTO.
     *
     * @param shift the shift entity
     * @return the shift response DTO
     */
    public static ShiftResponse toResponse(Shift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .cashier(shift.getCashier())
                .openingAmount(shift.getOpeningAmount())
                .closingAmount(shift.getClosingAmount())
                .expectedAmount(shift.getExpectedAmount())
                .difference(shift.getDifference())
                .totalSales(shift.getTotalSales())
                .totalTransactions(shift.getTotalTransactions())
                .totalVoided(shift.getTotalVoided())
                .totalReturns(shift.getTotalReturns())
                .status(shift.getStatus().name())
                .openedAt(shift.getOpenedAt())
                .closedAt(shift.getClosedAt())
                .remarks(shift.getRemarks())
                .build();
    }

    /**
     * Converts a {@link Shift} entity with payment breakdown totals to a {@link ShiftSummaryResponse} DTO.
     *
     * @param shift             the shift entity
     * @param cashSales         total cash sales amount for the shift
     * @param gcashSales        total GCash sales amount for the shift
     * @param bankTransferSales total bank transfer sales amount for the shift
     * @param creditSales       total credit sales amount for the shift
     * @return the shift summary response DTO
     */
    public static ShiftSummaryResponse toSummaryResponse(Shift shift, BigDecimal cashSales,
                                                         BigDecimal gcashSales, BigDecimal bankTransferSales,
                                                         BigDecimal creditSales) {
        return ShiftSummaryResponse.builder()
                .shiftId(shift.getId())
                .cashier(shift.getCashier())
                .status(shift.getStatus().name())
                .openedAt(shift.getOpenedAt())
                .closedAt(shift.getClosedAt())
                .openingAmount(shift.getOpeningAmount())
                .closingAmount(shift.getClosingAmount())
                .expectedAmount(shift.getExpectedAmount())
                .difference(shift.getDifference())
                .totalSales(shift.getTotalSales())
                .totalTransactions(shift.getTotalTransactions())
                .totalVoided(shift.getTotalVoided())
                .cashSales(cashSales)
                .gcashSales(gcashSales)
                .bankTransferSales(bankTransferSales)
                .creditSales(creditSales)
                .build();
    }

    /**
     * Converts a {@link Page} of {@link Shift} entities to a {@link PageResponse} of {@link ShiftResponse}.
     *
     * @param page the page of shift entities
     * @return the page response containing shift response DTOs
     */
    public static PageResponse<ShiftResponse> toPageResponse(Page<Shift> page) {
        Page<ShiftResponse> responsePage = page.map(ShiftMapper::toResponse);
        return PageResponse.of(responsePage);
    }
}
