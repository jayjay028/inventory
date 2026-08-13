package com.joven.inventory.service;

import com.joven.inventory.dto.request.AddonMasterRequest;
import com.joven.inventory.dto.response.AddonMasterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for add-on master management operations.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface AddonMasterService {

    /**
     * Retrieves all add-on master records with pagination.
     *
     * @param pageable pagination information
     * @return a page of add-on master responses
     */
    Page<AddonMasterResponse> getAll(Pageable pageable);

    /**
     * Retrieves all active add-on master records.
     *
     * @return a list of active add-on master responses
     */
    List<AddonMasterResponse> getAllActive();

    /**
     * Retrieves a single add-on master record by its ID.
     *
     * @param id the add-on master ID
     * @return the add-on master response
     */
    AddonMasterResponse getById(Long id);

    /**
     * Creates a new add-on master record.
     *
     * @param request the add-on master creation request
     * @return the created add-on master response
     */
    AddonMasterResponse create(AddonMasterRequest request);

    /**
     * Updates an existing add-on master record.
     *
     * @param id      the add-on master ID
     * @param request the add-on master update request
     * @return the updated add-on master response
     */
    AddonMasterResponse update(Long id, AddonMasterRequest request);

    /**
     * Activates or deactivates an add-on master record.
     *
     * @param id     the add-on master ID
     * @param active the desired active status
     * @return the updated add-on master response
     */
    AddonMasterResponse updateStatus(Long id, boolean active);
}
