package com.joven.inventory.service;

import com.joven.inventory.enums.DocumentType;

/**
 * Service interface for generating sequential document numbers.
 * Produces formatted document numbers using configurable prefixes and
 * auto-incrementing sequence numbers stored in app_settings.
 *
 * <p>Generated format: {prefix}{YYYYMM}-{NNNNN}</p>
 * <p>Example: OR-202608-00001</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface DocumentNumberService {

    /**
     * Generates the next sequential document number for the given document type.
     *
     * <p>This method atomically reads and increments the next number counter in app_settings
     * to prevent duplicate document numbers in concurrent environments.</p>
     *
     * @param documentType the type of document to generate a number for
     * @return the formatted document number, or {@code null} if the document type is NONE
     */
    String generateNextNumber(DocumentType documentType);
}
