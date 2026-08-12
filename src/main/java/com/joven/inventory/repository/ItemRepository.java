package com.joven.inventory.repository;

import com.joven.inventory.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link Item} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Finds an item by its item code.
     *
     * @param itemCode the item code to search for
     * @return an Optional containing the item if found
     */
    Optional<Item> findByItemCode(String itemCode);

    /**
     * Checks if an item exists with the given item code.
     *
     * @param itemCode the item code to check
     * @return true if an item with the item code exists
     */
    boolean existsByItemCode(String itemCode);

    /**
     * Finds all active items with pagination.
     *
     * @param pageable pagination information
     * @return a page of active items
     */
    Page<Item> findByActiveTrue(Pageable pageable);

    /**
     * Finds active items by category ID with pagination.
     *
     * @param categoryId the category ID to filter by
     * @param pageable pagination information
     * @return a page of active items in the specified category
     */
    Page<Item> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    /**
     * Searches items by name or item code containing the query string (case insensitive).
     *
     * @param query the search query
     * @param pageable pagination information
     * @return a page of matching items
     */
    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Item> searchItems(@Param("query") String query, Pageable pageable);

    /**
     * Finds items where current stock quantity is less than or equal to the reorder level.
     *
     * @param pageable pagination information
     * @return a page of items with low stock
     */
    @Query("SELECT i FROM Item i JOIN Stock s ON s.item = i WHERE s.quantityOnHand <= i.reorderLevel AND i.active = true")
    Page<Item> findLowStockItems(Pageable pageable);
}
