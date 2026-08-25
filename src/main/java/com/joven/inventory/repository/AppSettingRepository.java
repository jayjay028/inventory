package com.joven.inventory.repository;

import com.joven.inventory.entity.AppSetting;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Finds an application setting by its key with a pessimistic write lock.
     * Used for atomic read-and-update operations like document number generation.
     *
     * @param key the setting key to search for
     * @return an Optional containing the locked setting if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AppSetting s WHERE s.settingKey = :key")
    Optional<AppSetting> findBySettingKeyForUpdate(@Param("key") String key);

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
