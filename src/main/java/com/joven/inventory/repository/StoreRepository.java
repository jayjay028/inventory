package com.joven.inventory.repository;

import com.joven.inventory.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
