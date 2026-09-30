package com.joven.inventory.mapper;

import com.joven.inventory.dto.response.UserResponse;
import com.joven.inventory.entity.User;
import org.springframework.data.domain.Page;

/**
 * Utility class for mapping User entities to response DTOs.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class UserMapper {

    private UserMapper() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a User entity to a UserResponse DTO.
     *
     * @param user the user entity
     * @return the user response DTO
     */
    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .accessRights(user.getAccessRights())
                .active(user.getActive())
                .lastLogin(user.getLastLogin())
                .storeIds(user.getAccessibleStores().stream().map(store -> store.getId()).toList())
                .createdBy(user.getCreatedBy())
                .createdAt(user.getCreatedAt())
                .updatedBy(user.getUpdatedBy())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Converts a Page of User entities to a Page of UserResponse DTOs.
     *
     * @param page the page of user entities
     * @return the page of user response DTOs
     */
    public static Page<UserResponse> toPageResponse(Page<User> page) {
        return page.map(UserMapper::toResponse);
    }
}
