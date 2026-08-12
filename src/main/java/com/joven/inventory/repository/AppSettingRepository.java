package com.joven.inventory.repository;

import com.joven.inventory.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link AppSetting} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {

    /**
     * Finds an application setting by its key.
     *
     * @param settingKey the setting key to search for
     * @return an Optional containing the setting if found
     */
    Optional<AppSetting> findBySettingKey(String settingKey);

    /**
     * Finds all active application settings.
     *
     * @return a list of active application settings
     */
    List<AppSetting> findByActiveTrue();

    /**
     * Checks if an application setting exists with the given key.
     *
     * @param settingKey the setting key to check
     * @return true if a setting with the key exists
     */
    boolean existsBySettingKey(String settingKey);
}
