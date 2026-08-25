package com.joven.inventory.repository;

import com.joven.inventory.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Store} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * Finds a store by its unique code.
     *
     * @param code the store code
     * @return an Optional containing the store if found
     */
    Optional<Store> findByCode(String code);

    /**
     * Finds all active stores.
     *
     * @return list of active stores
     */
    List<Store> findByActiveTrue();

    /**
     * Checks whether a store with the given code already exists.
     *
     * @param code the store code
     * @return true if a store with the code exists
     */
    boolean existsByCode(String code);

    /**
     * Checks whether the given user has been granted access to the given store,
     * verified against the user_stores join table.
     *
     * @param userId  the user ID
     * @param storeId the store ID
     * @return true if the user has access to the store
     */
    @Query("SELECT COUNT(s) > 0 FROM User u JOIN u.accessibleStores s WHERE u.id = :userId AND s.id = :storeId")
    boolean userHasStoreAccess(@Param("userId") Long userId, @Param("storeId") Long storeId);
}
