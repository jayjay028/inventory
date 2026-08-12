package com.joven.inventory.service;

import com.joven.inventory.entity.AppSetting;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for application settings management.
 * Provides access to configurable system settings (company info, tax, documents, POS, etc.).
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface AppSettingService {

    /**
     * Retrieves the value of a setting by its key.
     *
     * @param key the setting key
     * @return an optional containing the setting value, or empty if not found
     */
    Optional<String> getValue(String key);

    /**
     * Retrieves the value of a setting by its key, returning a default value if not found.
     *
     * @param key          the setting key
     * @param defaultValue the default value to return if the setting is not found
     * @return the setting value or the default value
     */
    String getValueOrDefault(String key, String defaultValue);

    /**
     * Retrieves all active application settings.
     *
     * @return a list of active application settings
     */
    List<AppSetting> getAllActive();

    /**
     * Updates the value of an existing setting.
     *
     * @param key       the setting key
     * @param value     the new value
     * @param updatedBy the username of the person making the update
     */
    void updateValue(String key, String value, String updatedBy);

    /**
     * Updates multiple settings at once.
     *
     * @param settings  a map of setting keys to new values
     * @param updatedBy the username of the person making the update
     */
    void updateValues(Map<String, String> settings, String updatedBy);

    /**
     * Retrieves a boolean setting value.
     *
     * @param key          the setting key
     * @param defaultValue the default value if the setting is not found or cannot be parsed
     * @return the boolean value of the setting
     */
    boolean getBooleanValue(String key, boolean defaultValue);

    /**
     * Retrieves an integer setting value.
     *
     * @param key          the setting key
     * @param defaultValue the default value if the setting is not found or cannot be parsed
     * @return the integer value of the setting
     */
    int getIntValue(String key, int defaultValue);

    /**
     * Retrieves a BigDecimal setting value.
     *
     * @param key          the setting key
     * @param defaultValue the default value if the setting is not found or cannot be parsed
     * @return the BigDecimal value of the setting
     */
    BigDecimal getDecimalValue(String key, BigDecimal defaultValue);
}
