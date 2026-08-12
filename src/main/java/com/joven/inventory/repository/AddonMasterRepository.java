package com.joven.inventory.repository;

import com.joven.inventory.entity.AddonMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link AddonMaster} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface AddonMasterRepository extends JpaRepository<AddonMaster, Long> {

    /**
     * Finds an add-on master record by its name.
     *
     * @param name the add-on name to search for
     * @return an Optional containing the add-on master if found
     */
    Optional<AddonMaster> findByName(String name);

    /**
     * Checks if an add-on master record exists with the given name.
     *
     * @param name the add-on name to check
     * @return true if an add-on with the name exists
     */
    boolean existsByName(String name);

    /**
     * Finds all active add-on master records.
     *
     * @return a list of active add-on master records
     */
    List<AddonMaster> findByActiveTrue();

    /**
     * Finds all active add-on master records with pagination.
     *
     * @param pageable pagination information
     * @return a page of active add-on master records
     */
    Page<AddonMaster> findByActiveTrue(Pageable pageable);
}
