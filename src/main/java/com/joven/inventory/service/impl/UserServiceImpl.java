package com.joven.inventory.service.impl;

import com.joven.inventory.dto.request.PasswordResetRequest;
import com.joven.inventory.dto.request.UserRequest;
import com.joven.inventory.dto.response.UserResponse;
import com.joven.inventory.entity.Store;
import com.joven.inventory.entity.Role;
import com.joven.inventory.entity.User;
import com.joven.inventory.exception.DuplicateResourceException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.UserMapper;
import com.joven.inventory.repository.RoleRepository;
import com.joven.inventory.repository.StoreRepository;
import com.joven.inventory.repository.UserRepository;
import com.joven.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link UserService} providing CRUD operations for user management.
 * Handles password encoding, role assignment, and store access.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return UserMapper.toPageResponse(userRepository.findAll(pageable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = findByIdOrThrow(id);
        return UserMapper.toResponse(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User with username '" + request.getUsername() + "' already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(resolveRole(request.getRoleId()));
        user.setActive(true);
        user.setAccessibleStores(resolveStores(request.getStoreIds()));

        User saved = userRepository.save(user);
        log.info("User created: id={}, username='{}'", saved.getId(), saved.getUsername());
        return UserMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = findByIdOrThrow(id);

        // Check for duplicate username only if username is being changed
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User with username '" + request.getUsername() + "' already exists");
        }

        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(resolveRole(request.getRoleId()));

        // Update store access if provided
        if (request.getStoreIds() != null) {
            user.setAccessibleStores(resolveStores(request.getStoreIds()));
        }

        // Only update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        log.info("User updated: id={}, username='{}'", saved.getId(), saved.getUsername());
        return UserMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserResponse updateStatus(Long id, boolean active) {
        User user = findByIdOrThrow(id);
        user.setActive(active);

        User saved = userRepository.save(user);
        log.info("User status updated: id={}, active={}", saved.getId(), active);
        return UserMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserResponse resetPassword(Long id, PasswordResetRequest request) {
        User user = findByIdOrThrow(id);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        User saved = userRepository.save(user);
        log.info("Password reset for user: id={}, username='{}'", saved.getId(), saved.getUsername());
        return UserMapper.toResponse(saved);
    }

    /**
     * Finds a user by ID or throws ResourceNotFoundException.
     *
     * @param id the user ID
     * @return the found user entity
     */
    private User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Resolves a list of store IDs into a set of Store entities.
     * Ignores null/empty input by returning an empty set.
     *
     * @param storeIds the list of store IDs to resolve
     * @return a set of resolved Store entities
     */
    private Set<Store> resolveStores(List<Long> storeIds) {
        Set<Store> stores = new HashSet<>();
        if (storeIds == null || storeIds.isEmpty()) {
            return stores;
        }
        for (Long storeId : storeIds) {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));
            stores.add(store);
        }
        return stores;
    }

    /**
     * Resolves a role ID into a Role entity.
     *
     * @param roleId the role ID
     * @return the resolved Role entity
     * @throws ResourceNotFoundException if the role does not exist
     */
    private Role resolveRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
    }
}
