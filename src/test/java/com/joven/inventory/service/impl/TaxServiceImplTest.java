package com.joven.inventory.service.impl;

import com.joven.inventory.common.Constants;
import com.joven.inventory.enums.TaxType;
import com.joven.inventory.service.AppSettingService;
import com.joven.inventory.service.TaxService.TaxResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TaxServiceImpl}.
 * Tests tax calculation logic for VAT-inclusive, VAT-exclusive, exempt, and zero-rated scenarios.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaxServiceImpl Tests")
class TaxServiceImplTest {

    @Mock
    private AppSettingService appSettingService;

    @InjectMocks
    private TaxServiceImpl taxService;

    // --- Tax Disabled ---

    @Test
    @DisplayName("calculateTax - given tax disabled - returns zero tax with total equal to net amount")
    void calculateTax_givenTaxDisabled_returnsZeroTax() {
        // Arrange
        BigDecimal netAmount = new BigDecimal("1000.00");
        when(appSettingService.getDecimalValue("default_tax_rate", Constants.DEFAULT_VAT_RATE))
                .thenReturn(new BigDecimal("12.00"));

        // Act
        TaxResult result = taxService.calculateTax(netAmount, false, TaxType.VAT);

        // Assert
        assertThat(result.taxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result.vatableAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    // --- VAT Inclusive ---

    @Test
    @DisplayName("calculateTax - given VAT inclusive with VAT type - returns correct breakdown")
    void calculateTax_givenVatInclusiveWithVat_returnsCorrectBreakdown() {
        // Arrange
        BigDecimal netAmount = new BigDecimal("1120.00");
        when(appSettingService.getDecimalValue("default_tax_rate", Constants.DEFAULT_VAT_RATE))
                .thenReturn(new BigDecimal("12.00"));
        when(appSettingService.getValueOrDefault("pricing_method", "VAT_INCLUSIVE"))
                .thenReturn("VAT_INCLUSIVE");

        // Act
        TaxResult result = taxService.calculateTax(netAmount, true, TaxType.VAT);

        // Assert
        assertThat(result.vatableAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result.taxAmount()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("1120.00"));
        assertThat(result.taxRate()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(result.taxType()).isEqualTo(TaxType.VAT);
    }

    // --- VAT Exclusive ---

    @Test
    @DisplayName("calculateTax - given VAT exclusive with VAT type - returns correct breakdown")
    void calculateTax_givenVatExclusiveWithVat_returnsCorrectBreakdown() {
        // Arrange
        BigDecimal netAmount = new BigDecimal("1000.00");
        when(appSettingService.getDecimalValue("default_tax_rate", Constants.DEFAULT_VAT_RATE))
                .thenReturn(new BigDecimal("12.00"));
        when(appSettingService.getValueOrDefault("pricing_method", "VAT_INCLUSIVE"))
                .thenReturn("VAT_EXCLUSIVE");

        // Act
        TaxResult result = taxService.calculateTax(netAmount, true, TaxType.VAT);

        // Assert
        assertThat(result.vatableAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result.taxAmount()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("1120.00"));
        assertThat(result.taxRate()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(result.taxType()).isEqualTo(TaxType.VAT);
    }

    // --- Exempt ---

    @Test
    @DisplayName("calculateTax - given exempt type - returns zero tax and zero vatable amount")
    void calculateTax_givenExemptType_returnsZeroTax() {
        // Arrange
        BigDecimal netAmount = new BigDecimal("1000.00");
        when(appSettingService.getDecimalValue("default_tax_rate", Constants.DEFAULT_VAT_RATE))
                .thenReturn(new BigDecimal("12.00"));

        // Act
        TaxResult result = taxService.calculateTax(netAmount, true, TaxType.EXEMPT);

        // Assert
        assertThat(result.taxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.vatableAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result.taxType()).isEqualTo(TaxType.EXEMPT);
    }

    // --- Zero Rated ---

    @Test
    @DisplayName("calculateTax - given zero rated type - returns zero tax and zero vatable amount")
    void calculateTax_givenZeroRatedType_returnsZeroTax() {
        // Arrange
        BigDecimal netAmount = new BigDecimal("1000.00");
        when(appSettingService.getDecimalValue("default_tax_rate", Constants.DEFAULT_VAT_RATE))
                .thenReturn(new BigDecimal("12.00"));

        // Act
        TaxResult result = taxService.calculateTax(netAmount, true, TaxType.ZERO_RATED);

        // Assert
        assertThat(result.taxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.vatableAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result.taxType()).isEqualTo(TaxType.ZERO_RATED);
    }

    // --- Rounding ---

    @Test
    @DisplayName("calculateTax - given VAT inclusive small amount - rounds correctly")
    void calculateTax_givenVatInclusiveSmallAmount_roundsCorrectly() {
        // Arrange
        BigDecimal netAmount = new BigDecimal("112.00");
        when(appSettingService.getDecimalValue("default_tax_rate", Constants.DEFAULT_VAT_RATE))
                .thenReturn(new BigDecimal("12.00"));
        when(appSettingService.getValueOrDefault("pricing_method", "VAT_INCLUSIVE"))
                .thenReturn("VAT_INCLUSIVE");

        // Act
        TaxResult result = taxService.calculateTax(netAmount, true, TaxType.VAT);

        // Assert
        assertThat(result.vatableAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.taxAmount()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("112.00"));
    }
}
