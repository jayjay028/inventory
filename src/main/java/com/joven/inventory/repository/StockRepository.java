package com.joven.inventory.repository;

import com.joven.inventory.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link Stock} entity.
 * All queries are scoped by store since stock is tracked per-store.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Finds a stock record by item ID and store ID.
     *
     * @param itemId  the item ID
     * @param storeId the store ID
     * @return an Optional containing the stock record if found
     */
    Optional<Stock> findByItemIdAndStoreId(Long itemId, Long storeId);

    /**
     * Finds all stock records for a store with item details eagerly fetched, paginated.
     *
     * @param storeId  the store ID
     * @param pageable pagination information
     * @return a page of stock records with items loaded
     */
    @Query("SELECT s FROM Stock s JOIN FETCH s.item WHERE s.store.id = :storeId")
    Page<Stock> findAllWithItemByStoreId(@Param("storeId") Long storeId, Pageable pageable);
}
