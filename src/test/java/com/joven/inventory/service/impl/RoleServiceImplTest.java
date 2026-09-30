package com.joven.inventory.service.impl;

import com.joven.inventory.dto.request.RoleRequest;
import com.joven.inventory.dto.response.RoleResponse;
import com.joven.inventory.entity.Role;
import com.joven.inventory.exception.BadRequestException;
import com.joven.inventory.exception.DuplicateResourceException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RoleServiceImpl}.
 * Covers creation, duplicate detection, reserved ADMIN role protection,
 * status changes, deletion, and lookup failures.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleServiceImpl Tests")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    // --- Helper Methods ---

    private RoleRequest createRoleRequest(String name, String description, Long accessRights) {
        RoleRequest request = new RoleRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAccessRights(accessRights);
        return request;
    }

    private Role createRole(Long id, String name, Long accessRights, boolean system) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setDescription("desc");
        role.setAccessRights(accessRights);
        role.setActive(true);
        role.setIsSystem(system);
        return role;
    }

    // --- Create Tests ---

    @Test
    @DisplayName("create - given unique name - persists role and returns response")
    void create_givenUniqueName_returnsCreatedRole() {
        // Arrange
        RoleRequest request = createRoleRequest("CASHIER", "Point of sale operator", 16384L);
        when(roleRepository.existsByName("CASHIER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role toSave = invocation.getArgument(0);
            toSave.setId(2L);
            return toSave;
        });

        // Act
        RoleResponse response = roleService.create(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("CASHIER");
        assertThat(response.getDescription()).isEqualTo("Point of sale operator");
        assertThat(response.getAccessRights()).isEqualTo(16384L);
        assertThat(response.getActive()).isTrue();
        assertThat(response.getIsSystem()).isFalse();

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        Role saved = captor.getValue();
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getIsSystem()).isFalse();
    }

    @Test
    @DisplayName("create - given duplicate name - throws DuplicateResourceException")
    void create_givenDuplicateName_throwsDuplicateResourceException() {
        // Arrange
        RoleRequest request = createRoleRequest("ADMIN", "Duplicate", 1L);
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> roleService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Role")
                .hasMessageContaining("ADMIN");

        verify(roleRepository, never()).save(any(Role.class));
    }

    // --- Update Tests ---

    @Test
    @DisplayName("update - given reserved ADMIN role id - throws BadRequestException")
    void update_givenAdminRole_throwsBadRequestException() {
        // Arrange
        RoleRequest request = createRoleRequest("ADMIN", "tampered", 1L);

        // Act & Assert
        assertThatThrownBy(() -> roleService.update(Role.ADMIN_ROLE_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ADMIN role");

        verify(roleRepository, never()).findById(any());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("update - given non-admin role - updates and returns response")
    void update_givenNonAdminRole_returnsUpdatedRole() {
        // Arrange
        Long roleId = 3L;
        Role existing = createRole(roleId, "CASHIER", 16384L, false);
        RoleRequest request = createRoleRequest("SUPERVISOR", "Shift supervisor", 32768L);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existing));
        when(roleRepository.findByName("SUPERVISOR")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RoleResponse response = roleService.update(roleId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(roleId);
        assertThat(response.getName()).isEqualTo("SUPERVISOR");
        assertThat(response.getDescription()).isEqualTo("Shift supervisor");
        assertThat(response.getAccessRights()).isEqualTo(32768L);

        verify(roleRepository).save(existing);
    }

    // --- Update Status Tests ---

    @Test
    @DisplayName("updateStatus - deactivating reserved ADMIN role - throws BadRequestException")
    void updateStatus_deactivateAdminRole_throwsBadRequestException() {
        // Act & Assert
        assertThatThrownBy(() -> roleService.updateStatus(Role.ADMIN_ROLE_ID, false))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ADMIN role");

        verify(roleRepository, never()).findById(any());
        verify(roleRepository, never()).save(any(Role.class));
    }

    // --- Delete Tests ---

    @Test
    @DisplayName("delete - given reserved ADMIN role id - throws BadRequestException")
    void delete_givenAdminRole_throwsBadRequestException() {
        // Act & Assert
        assertThatThrownBy(() -> roleService.delete(Role.ADMIN_ROLE_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ADMIN role");

        verify(roleRepository, never()).delete(any(Role.class));
    }

    // --- Get By Id Tests ---

    @Test
    @DisplayName("getById - given nonexistent id - throws ResourceNotFoundException")
    void getById_givenNonexistentId_throwsResourceNotFoundException() {
        // Arrange
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role")
                .hasMessageContaining("99");
    }
}
