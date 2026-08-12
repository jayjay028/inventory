package com.joven.inventory.repository;

import com.joven.inventory.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Supplier} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * Finds all active suppliers with pagination.
     *
     * @param pageable pagination information
     * @return a page of active suppliers
     */
    Page<Supplier> findByActiveTrue(Pageable pageable);

    /**
     * Checks if a supplier exists with the given name and TIN.
     *
     * @param name the supplier name
     * @param tin the tax identification number
     * @return true if a supplier with the name and TIN exists
     */
    boolean existsByNameAndTin(String name, String tin);

    /**
     * Searches suppliers by name or TIN containing the query string (case insensitive).
     *
     * @param query the search query
     * @param pageable pagination information
     * @return a page of matching suppliers
     */
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.tin) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Supplier> searchSuppliers(@Param("query") String query, Pageable pageable);
}
