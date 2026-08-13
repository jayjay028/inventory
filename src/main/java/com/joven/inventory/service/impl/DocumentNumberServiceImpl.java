package com.joven.inventory.service.impl;

import com.joven.inventory.audit.AuditContext;
import com.joven.inventory.common.Constants;
import com.joven.inventory.enums.DocumentType;
import com.joven.inventory.service.AppSettingService;
import com.joven.inventory.service.DocumentNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link DocumentNumberService} that generates sequential document numbers
 * using prefixes and auto-incrementing counters stored in app_settings.
 *
 * <p>Generated format: {prefix}{YYYYMM}-{NNNNN}</p>
 * <p>Example: OR-202608-00001</p>
 *
 * <p>Each document type maps to a pair of app_setting keys:
 * one for the prefix and one for the next sequence number.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentNumberServiceImpl implements DocumentNumberService {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final AppSettingService appSettingService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String generateNextNumber(DocumentType documentType) {
        if (documentType == null || documentType == DocumentType.NONE) {
            return null;
        }

        String prefixKey = getPrefixKey(documentType);
        String nextNumberKey = getNextNumberKey(documentType);

        // Read prefix from app_settings
        String prefix = appSettingService.getValueOrDefault(prefixKey, documentType.name() + "-");

        // Read and increment the next number atomically within the transaction
        int nextNumber = appSettingService.getIntValue(nextNumberKey, 1);

        // Generate the year-month portion
        String yearMonth = LocalDateTime.now().format(YEAR_MONTH_FORMATTER);

        // Format: {prefix}{YYYYMM}-{NNNNN}
        String documentNumber = String.format(Constants.DOCUMENT_NUMBER_FORMAT, prefix, yearMonth, nextNumber);

        // Increment the next number in app_settings
        String updatedBy = AuditContext.getCurrentUser();
        appSettingService.updateValue(nextNumberKey, String.valueOf(nextNumber + 1), updatedBy);

        log.info("Generated document number '{}' for type '{}' by '{}'", documentNumber, documentType, updatedBy);

        return documentNumber;
    }

    /**
     * Returns the app_setting key for the document prefix.
     *
     * @param documentType the document type
     * @return the prefix setting key (e.g., "or_prefix")
     */
    private String getPrefixKey(DocumentType documentType) {
        return documentType.name().toLowerCase() + "_prefix";
    }

    /**
     * Returns the app_setting key for the next sequence number.
     *
     * @param documentType the document type
     * @return the next number setting key (e.g., "or_next_number")
     */
    private String getNextNumberKey(DocumentType documentType) {
        return documentType.name().toLowerCase() + "_next_number";
    }
}
