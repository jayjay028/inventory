package com.joven.inventory.service.impl;

import com.joven.inventory.audit.AuditContext;
import com.joven.inventory.common.Constants;
import com.joven.inventory.context.StoreContext;
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

        // Atomically read and increment the next number using pessimistic lock
        int nextNumber = appSettingService.getAndIncrementIntValue(nextNumberKey, 1);

        // Generate the year-month portion
        String yearMonth = LocalDateTime.now().format(YEAR_MONTH_FORMATTER);

        // Format: {prefix}{YYYYMM}-{NNNNN}
        String documentNumber = String.format(Constants.DOCUMENT_NUMBER_FORMAT, prefix, yearMonth, nextNumber);

        log.info("Generated document number '{}' for type '{}' by '{}'",
                documentNumber, documentType, AuditContext.getCurrentUser());

        return documentNumber;
    }

    /**
     * Returns the app_setting key for the document prefix, scoped to the current store.
     * When a store is present in {@link StoreContext}, the key is prefixed with the store ID
     * to give each store its own sequence; otherwise the non-prefixed key is used.
     *
     * @param documentType the document type
     * @return the prefix setting key (e.g., "store_1_or_prefix" or "or_prefix")
     */
    private String getPrefixKey(DocumentType documentType) {
        return scopedKey(documentType.name().toLowerCase() + "_prefix");
    }

    /**
     * Returns the app_setting key for the next sequence number, scoped to the current store.
     * When a store is present in {@link StoreContext}, the key is prefixed with the store ID
     * to give each store its own sequence; otherwise the non-prefixed key is used.
     *
     * @param documentType the document type
     * @return the next number setting key (e.g., "store_1_or_next_number" or "or_next_number")
     */
    private String getNextNumberKey(DocumentType documentType) {
        return scopedKey(documentType.name().toLowerCase() + "_next_number");
    }

    /**
     * Prefixes the given base key with the current store when one is set in
     * {@link StoreContext}. Falls back to the base key when no store is present.
     *
     * @param baseKey the unscoped setting key
     * @return the store-scoped key, or the base key when no store is set
     */
    private String scopedKey(String baseKey) {
        Long storeId = StoreContext.getStoreId();
        if (storeId == null) {
            return baseKey;
        }
        return "store_" + storeId + "_" + baseKey;
    }
}
