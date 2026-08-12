package com.joven.inventory.repository;

import com.joven.inventory.entity.SaleItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for {@link SaleItem} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    /**
     * Finds all sale items for a given sale.
     *
     * @param saleId the sale ID
     * @return a list of sale items
     */
    List<SaleItem> findBySaleId(Long saleId);

    /**
     * Deletes all sale items for a given sale.
     *
     * @param saleId the sale ID
     */
    void deleteBySaleId(Long saleId);

    /**
     * Finds the top selling items within a date range, returning item ID, item name,
     * total quantity sold, and total line amount.
     *
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @param pageable pagination information
     * @return a page of Object arrays containing [itemId, itemName, totalQuantity, totalLineTotal]
     */
    @Query("SELECT si.item.id, si.item.name, SUM(si.quantity), SUM(si.lineTotal) " +
            "FROM SaleItem si WHERE si.sale.saleDate BETWEEN :from AND :to " +
            "GROUP BY si.item.id, si.item.name ORDER BY SUM(si.quantity) DESC")
    Page<Object[]> findTopSellingItems(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);
}
