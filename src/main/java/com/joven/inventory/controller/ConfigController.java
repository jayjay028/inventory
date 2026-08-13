package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * REST controller for serving application configuration files.
 * Reads YAML configuration files from the classpath and returns them as JSON.
 * Used by the frontend to retrieve navigation, form definitions, and permission structures.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    /**
     * Retrieves the navigation configuration.
     * Reads navigation.yml from the classpath and returns its contents as JSON.
     *
     * @return the API response containing the navigation configuration
     * @throws IOException if the configuration file cannot be read
     */
    @GetMapping("/navigation")
    public ResponseEntity<ApiResponse<Object>> getNavigation() throws IOException {
        Object config = loadYaml("config/navigation.yml");
        return ResponseEntity.ok(ApiResponse.success("Navigation config retrieved", config));
    }

    /**
     * Retrieves a form configuration by form name.
     * Reads forms/{formName}.yml from the classpath and returns its contents as JSON.
     *
     * @param formName the name of the form configuration file (without extension)
     * @return the API response containing the form configuration
     * @throws IOException if the configuration file cannot be read
     */
    @GetMapping("/forms/{formName}")
    public ResponseEntity<ApiResponse<Object>> getFormConfig(@PathVariable String formName) throws IOException {
        Object config = loadYaml("config/forms/" + formName + ".yml");
        return ResponseEntity.ok(ApiResponse.success("Form config retrieved", config));
    }

    /**
     * Retrieves the permissions configuration.
     * Reads permissions.yml from the classpath and returns its contents as JSON.
     *
     * @return the API response containing the permissions configuration
     * @throws IOException if the configuration file cannot be read
     */
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<Object>> getPermissions() throws IOException {
        Object config = loadYaml("config/permissions.yml");
        return ResponseEntity.ok(ApiResponse.success("Permissions config retrieved", config));
    }

    /**
     * Loads and parses a YAML file from the classpath.
     *
     * @param path the classpath-relative path to the YAML file
     * @return the parsed YAML content as a Map or List
     * @throws IOException if the file cannot be found or read
     */
    private Object loadYaml(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IOException("Configuration file not found: " + path);
        }
        Yaml yaml = new Yaml();
        try (InputStream inputStream = resource.getInputStream()) {
            return yaml.load(inputStream);
        }
    }
}
