package com.joven.inventory.service;

import com.joven.inventory.enums.TaxType;

import java.math.BigDecimal;

/**
 * Service interface for tax calculation operations.
 * Calculates VAT/tax amounts based on system pricing method (VAT_INCLUSIVE or VAT_EXCLUSIVE)
 * and item-level tax type configuration.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface TaxService {

    /**
     * Calculates tax based on the system pricing method from app_settings.
     *
     * <p>Behavior depends on the configured pricing method:</p>
     * <ul>
     *   <li><b>VAT_INCLUSIVE</b>: The net amount already includes VAT. Computes vatable amount
     *       by dividing by the VAT divisor, then derives tax from that.</li>
     *   <li><b>VAT_EXCLUSIVE</b>: The net amount does not include VAT. Tax is computed
     *       on top of the net amount.</li>
     * </ul>
     *
     * <p>If {@code taxEnabled} is false, or the tax type is EXEMPT or ZERO_RATED,
     * no tax is applied and the total equals the net amount.</p>
     *
     * @param netAmount  the base amount to calculate tax on
     * @param taxEnabled whether tax calculation is enabled for this transaction/item
     * @param taxType    the tax classification (VAT, NON_VAT, EXEMPT, ZERO_RATED)
     * @return a {@link TaxResult} containing the computed tax breakdown
     */
    TaxResult calculateTax(BigDecimal netAmount, boolean taxEnabled, TaxType taxType);

    /**
     * Immutable result of a tax calculation containing the full breakdown.
     *
     * @param taxAmount     the computed tax amount
     * @param vatableAmount the vatable (tax-base) portion of the amount
     * @param totalAmount   the final total amount (inclusive of tax if applicable)
     * @param taxRate       the tax rate applied (e.g., 12.00 for 12%)
     * @param taxType       the tax type classification used in the calculation
     */
    record TaxResult(
            BigDecimal taxAmount,
            BigDecimal vatableAmount,
            BigDecimal totalAmount,
            BigDecimal taxRate,
            TaxType taxType
    ) {}
}
