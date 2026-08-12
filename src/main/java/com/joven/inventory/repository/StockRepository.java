package com.joven.inventory.repository;

import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link Stock} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * Finds stock record by item ID.
     *
     * @param itemId the item ID
     * @return an Optional containing the stock record if found
     */
    Optional<Stock> findByItemId(Long itemId);

    /**
     * Finds stock record by item entity.
     *
     * @param item the item entity
     * @return an Optional containing the stock record if found
     */
    Optional<Stock> findByItem(Item item);

    /**
     * Finds all stocks with item details using fetch join, with pagination.
     *
     * @param pageable pagination information
     * @return a page of stocks with item details loaded
     */
    @Query("SELECT s FROM Stock s JOIN FETCH s.item")
    Page<Stock> findAllWithItem(Pageable pageable);
}
