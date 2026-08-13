package com.joven.inventory.service.impl;

import com.joven.inventory.dto.request.AddonMasterRequest;
import com.joven.inventory.dto.response.AddonMasterResponse;
import com.joven.inventory.entity.AddonMaster;
import com.joven.inventory.exception.DuplicateResourceException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.repository.AddonMasterRepository;
import com.joven.inventory.service.AddonMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link AddonMasterService} providing CRUD operations
 * for add-on master records.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddonMasterServiceImpl implements AddonMasterService {

    private final AddonMasterRepository addonMasterRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AddonMasterResponse> getAll(Pageable pageable) {
        return addonMasterRepository.findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AddonMasterResponse> getAllActive() {
        return addonMasterRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AddonMasterResponse getById(Long id) {
        AddonMaster addon = findByIdOrThrow(id);
        return toResponse(addon);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AddonMasterResponse create(AddonMasterRequest request) {
        if (addonMasterRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Add-on with name '" + request.getName() + "' already exists");
        }

        AddonMaster addon = new AddonMaster();
        addon.setName(request.getName());
        addon.setDefaultAmount(request.getDefaultAmount());
        addon.setActive(true);

        AddonMaster saved = addonMasterRepository.save(addon);
        log.info("Add-on master created: id={}, name='{}'", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AddonMasterResponse update(Long id, AddonMasterRequest request) {
        AddonMaster addon = findByIdOrThrow(id);

        // Check for duplicate name only if name is being changed
        if (!addon.getName().equals(request.getName()) && addonMasterRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Add-on with name '" + request.getName() + "' already exists");
        }

        addon.setName(request.getName());
        addon.setDefaultAmount(request.getDefaultAmount());

        AddonMaster saved = addonMasterRepository.save(addon);
        log.info("Add-on master updated: id={}, name='{}'", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AddonMasterResponse updateStatus(Long id, boolean active) {
        AddonMaster addon = findByIdOrThrow(id);
        addon.setActive(active);

        AddonMaster saved = addonMasterRepository.save(addon);
        log.info("Add-on master status updated: id={}, active={}", saved.getId(), active);
        return toResponse(saved);
    }

    /**
     * Finds an add-on master by ID or throws ResourceNotFoundException.
     *
     * @param id the add-on master ID
     * @return the found add-on master entity
     */
    private AddonMaster findByIdOrThrow(Long id) {
        return addonMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Add-on master not found with id: " + id));
    }

    /**
     * Converts an AddonMaster entity to its response DTO.
     *
     * @param addon the add-on master entity
     * @return the response DTO
     */
    private AddonMasterResponse toResponse(AddonMaster addon) {
        return AddonMasterResponse.builder()
                .id(addon.getId())
                .name(addon.getName())
                .defaultAmount(addon.getDefaultAmount())
                .active(addon.getActive())
                .createdBy(addon.getCreatedBy())
                .createdAt(addon.getCreatedAt())
                .updatedBy(addon.getUpdatedBy())
                .updatedAt(addon.getUpdatedAt())
                .build();
    }
}
