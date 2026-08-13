package com.joven.inventory.service.impl;

import com.joven.inventory.entity.AppSetting;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.repository.AppSettingRepository;
import com.joven.inventory.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Implementation of {@link AppSettingService} providing access to
 * configurable system settings stored in the database.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppSettingServiceImpl implements AppSettingService {

    private final AppSettingRepository appSettingRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<String> getValue(String key) {
        return appSettingRepository.findBySettingKey(key)
                .map(AppSetting::getSettingValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public String getValueOrDefault(String key, String defaultValue) {
        return getValue(key).orElse(defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppSetting> getAllActive() {
        return appSettingRepository.findByActiveTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateValue(String key, String value, String updatedBy) {
        AppSetting setting = appSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key));

        setting.setSettingValue(value);
        setting.setUpdatedBy(updatedBy);
        appSettingRepository.save(setting);

        log.info("Setting '{}' updated by '{}'", key, updatedBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateValues(Map<String, String> settings, String updatedBy) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            updateValue(entry.getKey(), entry.getValue(), updatedBy);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean getBooleanValue(String key, boolean defaultValue) {
        return getValue(key)
                .map(value -> "true".equalsIgnoreCase(value.trim()))
                .orElse(defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public int getIntValue(String key, int defaultValue) {
        return getValue(key)
                .map(value -> {
                    try {
                        return Integer.parseInt(value.trim());
                    } catch (NumberFormatException ex) {
                        log.warn("Failed to parse integer setting '{}' with value '{}', using default: {}",
                                key, value, defaultValue);
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getDecimalValue(String key, BigDecimal defaultValue) {
        return getValue(key)
                .map(value -> {
                    try {
                        return new BigDecimal(value.trim());
                    } catch (NumberFormatException ex) {
                        log.warn("Failed to parse decimal setting '{}' with value '{}', using default: {}",
                                key, value, defaultValue);
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getAll() {
        List<AppSetting> settings = appSettingRepository.findByActiveTrue();
        Map<String, String> result = new LinkedHashMap<>();
        for (AppSetting setting : settings) {
            result.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateAll(Map<String, String> settings) {
        String currentUser = getCurrentUsername();
        updateValues(settings, currentUser);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
