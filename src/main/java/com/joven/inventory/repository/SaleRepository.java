package com.joven.inventory.repository;

import com.joven.inventory.entity.Sale;
import com.joven.inventory.enums.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Sale} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    /**
     * Finds a sale by its sale number.
     *
     * @param saleNo the sale number
     * @return an Optional containing the sale if found
     */
    Optional<Sale> findBySaleNo(String saleNo);

    /**
     * Finds sales by status with pagination.
     *
     * @param status the sale status
     * @param pageable pagination information
     * @return a page of sales with the given status
     */
    Page<Sale> findByStatus(SaleStatus status, Pageable pageable);

    /**
     * Finds sales by the cashier who created them, with pagination.
     *
     * @param createdBy the username of the cashier
     * @param pageable pagination information
     * @return a page of sales created by the specified cashier
     */
    Page<Sale> findByCreatedBy(String createdBy, Pageable pageable);

    /**
     * Finds sales within a date range with pagination.
     *
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @param pageable pagination information
     * @return a page of sales within the date range
     */
    Page<Sale> findBySaleDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    /**
     * Finds all sales for a specific shift.
     *
     * @param shiftId the shift ID
     * @return a list of sales for the given shift
     */
    List<Sale> findByShiftId(Long shiftId);

    /**
     * Finds all sales for today within the specified day boundaries.
     *
     * @param startOfDay the start of the day
     * @param endOfDay the end of the day
     * @return a list of today's sales
     */
    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startOfDay AND :endOfDay")
    List<Sale> findTodaySales(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Counts sales by status within a date range.
     *
     * @param status the sale status
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @return the count of matching sales
     */
    long countByStatusAndSaleDateBetween(SaleStatus status, LocalDateTime from, LocalDateTime to);
}
