package com.joven.inventory.service.impl;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.RoleRequest;
import com.joven.inventory.dto.response.RoleResponse;
import com.joven.inventory.entity.Role;
import com.joven.inventory.exception.BadRequestException;
import com.joven.inventory.exception.DuplicateResourceException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.RoleMapper;
import com.joven.inventory.repository.RoleRepository;
import com.joven.inventory.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link RoleService} providing role management operations.
 * Handles business logic, validation, and persistence for roles.
 *
 * <p>The reserved system ADMIN role ({@link Role#ADMIN_ROLE_ID}) is protected:
 * it cannot be updated, deactivated, or deleted.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> getAll(Pageable pageable) {
        log.debug("Fetching all roles with pageable: {}", pageable);
        Page<Role> page = roleRepository.findAll(pageable);
        return RoleMapper.toPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllActive() {
        log.debug("Fetching all active roles");
        return roleRepository.findByActiveTrue()
                .stream()
                .map(RoleMapper::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(Long id) {
        log.debug("Fetching role by id: {}", id);
        Role role = findRoleById(id);
        return RoleMapper.toResponse(role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleResponse create(RoleRequest request) {
        log.info("Creating new role with name: {}", request.getName());

        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role", "name", request.getName());
        }

        Role role = new Role();
        RoleMapper.updateEntity(role, request);
        role.setActive(true);
        role.setIsSystem(false);

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with id: {}", savedRole.getId());
        return RoleMapper.toResponse(savedRole);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleResponse update(Long id, RoleRequest request) {
        log.info("Updating role with id: {}", id);

        assertNotAdminRole(id, "updated");

        Role role = findRoleById(id);

        roleRepository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Role", "name", request.getName());
                });

        RoleMapper.updateEntity(role, request);

        Role updatedRole = roleRepository.save(role);
        log.info("Role updated successfully with id: {}", updatedRole.getId());
        return RoleMapper.toResponse(updatedRole);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleResponse updateStatus(Long id, boolean active) {
        log.info("Updating role status for id: {} to active: {}", id, active);

        if (!active) {
            assertNotAdminRole(id, "deactivated");
        }

        Role role = findRoleById(id);
        role.setActive(active);

        Role updatedRole = roleRepository.save(role);
        log.info("Role status updated successfully for id: {}", updatedRole.getId());
        return RoleMapper.toResponse(updatedRole);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Long id) {
        log.info("Deleting role with id: {}", id);

        assertNotAdminRole(id, "deleted");

        Role role = findRoleById(id);
        roleRepository.delete(role);
        log.info("Role deleted successfully with id: {}", id);
    }

    /**
     * Guards against structural changes to the reserved system ADMIN role.
     *
     * @param id     the role ID being acted upon
     * @param action the attempted action, used in the error message
     * @throws BadRequestException if the ID is the reserved ADMIN role ID
     */
    private void assertNotAdminRole(Long id, String action) {
        if (id != null && id == Role.ADMIN_ROLE_ID) {
            throw new BadRequestException("The system ADMIN role cannot be " + action);
        }
    }

    /**
     * Finds a role by ID or throws {@link ResourceNotFoundException}.
     *
     * @param id the role ID
     * @return the found role entity
     * @throws ResourceNotFoundException if no role exists with the given ID
     */
    private Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }
}
