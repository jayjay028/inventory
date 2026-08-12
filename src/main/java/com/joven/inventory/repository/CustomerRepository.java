package com.joven.inventory.repository;

import com.joven.inventory.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Customer} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Finds all active customers with pagination.
     *
     * @param pageable pagination information
     * @return a page of active customers
     */
    Page<Customer> findByActiveTrue(Pageable pageable);

    /**
     * Checks if a customer exists with the given name and TIN.
     *
     * @param name the customer name
     * @param tin the tax identification number
     * @return true if a customer with the name and TIN exists
     */
    boolean existsByNameAndTin(String name, String tin);

    /**
     * Searches customers by name or TIN containing the query string (case insensitive).
     *
     * @param query the search query
     * @param pageable pagination information
     * @return a page of matching customers
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.tin) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Customer> searchCustomers(@Param("query") String query, Pageable pageable);
}
