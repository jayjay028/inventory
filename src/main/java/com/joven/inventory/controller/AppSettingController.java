package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for application settings management.
 * Provides endpoints for retrieving and updating system configuration settings.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class AppSettingController {

    private final AppSettingService appSettingService;

    /**
     * Retrieves all active application settings as a key-value map.
     *
     * @return the API response containing all settings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getAll() {
        Map<String, String> settings = appSettingService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Settings retrieved successfully", settings));
    }

    /**
     * Retrieves a single application setting value by its key.
     *
     * @param key the setting key
     * @return the API response containing the setting value
     */
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<String>> getByKey(@PathVariable String key) {
        String value = appSettingService.getValue(key)
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success("Setting retrieved successfully", value));
    }

    /**
     * Updates multiple application settings at once.
     *
     * @param settings       a map of setting keys to new values
     * @param authentication the current authentication context
     * @return the API response confirming the update
     */
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateAll(
            @RequestBody Map<String, String> settings,
            Authentication authentication) {
        appSettingService.updateValues(settings, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", null));
    }

    /**
     * Updates a single application setting by its key.
     *
     * @param key            the setting key
     * @param body           the request body containing the "value" field
     * @param authentication the current authentication context
     * @return the API response confirming the update
     */
    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> updateByKey(
            @PathVariable String key,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String value = body.get("value");
        appSettingService.updateValue(key, value, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Setting updated successfully", null));
    }
}
