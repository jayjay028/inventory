package com.joven.inventory.service.impl;

import com.joven.inventory.common.Constants;
import com.joven.inventory.enums.TaxType;
import com.joven.inventory.service.AppSettingService;
import com.joven.inventory.service.TaxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of {@link TaxService} that calculates tax based on the
 * system pricing method configured in app_settings.
 *
 * <p>Supports two pricing methods:</p>
 * <ul>
 *   <li><b>VAT_INCLUSIVE</b>: Price already includes VAT; tax is extracted from the amount.</li>
 *   <li><b>VAT_EXCLUSIVE</b>: Price does not include VAT; tax is added on top.</li>
 * </ul>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaxServiceImpl implements TaxService {

    private static final String PRICING_METHOD_KEY = "pricing_method";
    private static final String DEFAULT_TAX_RATE_KEY = "default_tax_rate";
    private static final String VAT_INCLUSIVE = "VAT_INCLUSIVE";
    private static final int SCALE = 2;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final AppSettingService appSettingService;

    /**
     * {@inheritDoc}
     */
    @Override
    public TaxResult calculateTax(BigDecimal netAmount, boolean taxEnabled, TaxType taxType) {
        BigDecimal taxRate = appSettingService.getDecimalValue(DEFAULT_TAX_RATE_KEY, Constants.DEFAULT_VAT_RATE);

        // If tax is disabled, return zero tax
        if (!taxEnabled) {
            return zeroTaxResult(netAmount, taxRate, taxType);
        }

        // If tax type is EXEMPT or ZERO_RATED, return zero tax with zero vatable
        if (taxType == TaxType.EXEMPT || taxType == TaxType.ZERO_RATED) {
            return new TaxResult(
                    BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP),
                    netAmount.setScale(SCALE, RoundingMode.HALF_UP),
                    taxRate,
                    taxType
            );
        }

        // Determine pricing method
        String pricingMethod = appSettingService.getValueOrDefault(PRICING_METHOD_KEY, VAT_INCLUSIVE);

        if (VAT_INCLUSIVE.equalsIgnoreCase(pricingMethod)) {
            return calculateVatInclusive(netAmount, taxRate, taxType);
        } else {
            return calculateVatExclusive(netAmount, taxRate, taxType);
        }
    }

    /**
     * Calculates tax for VAT-inclusive pricing.
     * The net amount already contains VAT, so we extract the vatable amount and tax from it.
     *
     * @param netAmount the amount that already includes VAT
     * @param taxRate   the tax rate percentage
     * @param taxType   the tax type classification
     * @return the tax calculation result
     */
    private TaxResult calculateVatInclusive(BigDecimal netAmount, BigDecimal taxRate, TaxType taxType) {
        // vatableAmount = netAmount / VAT_DIVISOR (e.g., / 1.12)
        BigDecimal vatableAmount = netAmount.divide(Constants.VAT_DIVISOR, SCALE, RoundingMode.HALF_UP);

        // taxAmount = vatableAmount * rate / 100
        BigDecimal taxAmount = vatableAmount.multiply(taxRate)
                .divide(ONE_HUNDRED, SCALE, RoundingMode.HALF_UP);

        // totalAmount = netAmount (already includes VAT)
        BigDecimal totalAmount = netAmount.setScale(SCALE, RoundingMode.HALF_UP);

        return new TaxResult(taxAmount, vatableAmount, totalAmount, taxRate, taxType);
    }

    /**
     * Calculates tax for VAT-exclusive pricing.
     * The net amount does not contain VAT, so tax is computed on top.
     *
     * @param netAmount the amount before tax
     * @param taxRate   the tax rate percentage
     * @param taxType   the tax type classification
     * @return the tax calculation result
     */
    private TaxResult calculateVatExclusive(BigDecimal netAmount, BigDecimal taxRate, TaxType taxType) {
        // vatableAmount = netAmount
        BigDecimal vatableAmount = netAmount.setScale(SCALE, RoundingMode.HALF_UP);

        // taxAmount = netAmount * rate / 100
        BigDecimal taxAmount = netAmount.multiply(taxRate)
                .divide(ONE_HUNDRED, SCALE, RoundingMode.HALF_UP);

        // totalAmount = netAmount + taxAmount
        BigDecimal totalAmount = netAmount.add(taxAmount).setScale(SCALE, RoundingMode.HALF_UP);

        return new TaxResult(taxAmount, vatableAmount, totalAmount, taxRate, taxType);
    }

    /**
     * Returns a zero-tax result when tax is disabled.
     *
     * @param netAmount the original amount
     * @param taxRate   the configured tax rate (included for reference)
     * @param taxType   the tax type classification
     * @return a TaxResult with zero tax and total equal to net amount
     */
    private TaxResult zeroTaxResult(BigDecimal netAmount, BigDecimal taxRate, TaxType taxType) {
        return new TaxResult(
                BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP),
                netAmount.setScale(SCALE, RoundingMode.HALF_UP),
                netAmount.setScale(SCALE, RoundingMode.HALF_UP),
                taxRate,
                taxType
        );
    }
}
