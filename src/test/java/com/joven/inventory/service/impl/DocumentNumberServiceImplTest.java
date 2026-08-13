package com.joven.inventory.service.impl;

import com.joven.inventory.audit.AuditContext;
import com.joven.inventory.enums.DocumentType;
import com.joven.inventory.service.AppSettingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DocumentNumberServiceImpl}.
 * Tests document number generation with sequential numbering and prefix formatting.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentNumberServiceImpl Tests")
class DocumentNumberServiceImplTest {

    @Mock
    private AppSettingService appSettingService;

    @InjectMocks
    private DocumentNumberServiceImpl documentNumberService;

    private String currentYearMonth;

    @BeforeEach
    void setUp() {
        AuditContext.set("testuser", "127.0.0.1");
        currentYearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    @AfterEach
    void tearDown() {
        AuditContext.clear();
    }

    // --- OR Type ---

    @Test
    @DisplayName("generateNextNumber - given OR type - returns formatted number with OR prefix")
    void generateNextNumber_givenOrType_returnsFormattedNumber() {
        // Arrange
        when(appSettingService.getValueOrDefault("or_prefix", "OR-")).thenReturn("OR-");
        when(appSettingService.getIntValue("or_next_number", 1)).thenReturn(1);

        // Act
        String result = documentNumberService.generateNextNumber(DocumentType.OR);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).matches("OR-\\d{6}-\\d{5}");
        assertThat(result).isEqualTo("OR-" + currentYearMonth + "-00001");

        verify(appSettingService).updateValue("or_next_number", "2", "testuser");
    }

    // --- SI Type ---

    @Test
    @DisplayName("generateNextNumber - given SI type - returns formatted number with SI prefix")
    void generateNextNumber_givenSiType_returnsFormattedNumber() {
        // Arrange
        when(appSettingService.getValueOrDefault("si_prefix", "SI-")).thenReturn("SI-");
        when(appSettingService.getIntValue("si_next_number", 1)).thenReturn(5);

        // Act
        String result = documentNumberService.generateNextNumber(DocumentType.SI);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("SI-");
        assertThat(result).contains("00005");
        assertThat(result).isEqualTo("SI-" + currentYearMonth + "-00005");

        verify(appSettingService).updateValue("si_next_number", "6", "testuser");
    }

    // --- NONE Type ---

    @Test
    @DisplayName("generateNextNumber - given NONE type - returns null")
    void generateNextNumber_givenNoneType_returnsNull() {
        // Arrange - no mocks needed

        // Act
        String result = documentNumberService.generateNextNumber(DocumentType.NONE);

        // Assert
        assertThat(result).isNull();

        verifyNoInteractions(appSettingService);
    }

    // --- Null Type ---

    @Test
    @DisplayName("generateNextNumber - given null type - returns null")
    void generateNextNumber_givenNullType_returnsNull() {
        // Arrange - no mocks needed

        // Act
        String result = documentNumberService.generateNextNumber(null);

        // Assert
        assertThat(result).isNull();

        verifyNoInteractions(appSettingService);
    }

    // --- PO Type ---

    @Test
    @DisplayName("generateNextNumber - given PO type - increments counter correctly")
    void generateNextNumber_givenPoType_incrementsCounter() {
        // Arrange
        when(appSettingService.getValueOrDefault("po_prefix", "PO-")).thenReturn("PO-");
        when(appSettingService.getIntValue("po_next_number", 1)).thenReturn(100);

        // Act
        String result = documentNumberService.generateNextNumber(DocumentType.PO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("PO-");
        assertThat(result).contains("00100");
        assertThat(result).isEqualTo("PO-" + currentYearMonth + "-00100");

        verify(appSettingService).updateValue("po_next_number", "101", "testuser");
    }
}
