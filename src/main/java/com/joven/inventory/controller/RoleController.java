package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.RoleRequest;
import com.joven.inventory.dto.response.RoleResponse;
import com.joven.inventory.security.Permission;
import com.joven.inventory.security.RequiresPermission;
import com.joven.inventory.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for role management operations.
 * Provides endpoints for CRUD operations and status management of roles.
 *
 * <p>All endpoints require the {@link Permission#MANAGE_USERS} permission.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Retrieves all roles with pagination support.
     *
     * @param page    the page number (zero-based), defaults to 0
     * @param size    the page size, defaults to 10
     * @param sortBy  the field to sort by, defaults to "name"
     * @param sortDir the sort direction (asc or desc), defaults to "asc"
     * @return the API response containing a paginated list of roles
     */
    @GetMapping
    @RequiresPermission(Permission.MANAGE_USERS)
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<RoleResponse> response = roleService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", response));
    }

    /**
     * Retrieves all active roles as a simple list (no pagination).
     * Used to populate the role dropdown on the user form.
     *
     * @return the API response containing a list of active roles
     */
    @GetMapping("/active")
    @RequiresPermission(Permission.MANAGE_USERS)
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllActive() {
        List<RoleResponse> response = roleService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success("Active roles retrieved successfully", response));
    }

    /**
     * Retrieves a single role by its unique identifier.
     *
     * @param id the role ID
     * @return the API response containing the role details
     */
    @GetMapping("/{id}")
    @RequiresPermission(Permission.MANAGE_USERS)
    public ResponseEntity<ApiResponse<RoleResponse>> getById(@PathVariable Long id) {
        RoleResponse response = roleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", response));
    }

    /**
     * Creates a new role.
     *
     * @param request the role request body containing name, description, and access rights
     * @return the API response containing the created role
     */
    @PostMapping
    @RequiresPermission(Permission.MANAGE_USERS)
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", response));
    }

    /**
     * Updates an existing role.
     *
     * @param id      the ID of the role to update
     * @param request the role request body containing updated data
     * @return the API response containing the updated role
     */
    @PutMapping("/{id}")
    @RequiresPermission(Permission.MANAGE_USERS)
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
    }

    /**
     * Updates the active status of a role.
     *
     * @param id     the ID of the role to update
     * @param active the new active status
     * @return the API response containing the updated role
     */
    @PatchMapping("/{id}/status")
    @RequiresPermission(Permission.MANAGE_USERS)
    public ResponseEntity<ApiResponse<RoleResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        RoleResponse response = roleService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Role status updated successfully", response));
    }

    /**
     * Deletes a role by its unique identifier.
     *
     * @param id the ID of the role to delete
     * @return the API response confirming deletion
     */
    @DeleteMapping("/{id}")
    @RequiresPermission(Permission.MANAGE_USERS)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }
}
