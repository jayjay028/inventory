package com.joven.inventory.service.impl;

import com.joven.inventory.dto.request.PasswordResetRequest;
import com.joven.inventory.dto.request.UserRequest;
import com.joven.inventory.dto.response.UserResponse;
import com.joven.inventory.entity.User;
import com.joven.inventory.exception.DuplicateResourceException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.UserMapper;
import com.joven.inventory.repository.UserRepository;
import com.joven.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link UserService} providing CRUD operations for user management.
 * Handles password encoding, role assignment, and access rights.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
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
        user.setRole(request.getRole());
        user.setAccessRights(request.getAccessRights());
        user.setActive(true);

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
        user.setRole(request.getRole());
        user.setAccessRights(request.getAccessRights());

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
}
