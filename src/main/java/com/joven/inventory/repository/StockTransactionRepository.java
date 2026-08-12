package com.joven.inventory.repository;

import com.joven.inventory.entity.StockTransaction;
import com.joven.inventory.enums.TransactionStatus;
import com.joven.inventory.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository interface for {@link StockTransaction} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    /**
     * Finds stock transactions by status with pagination.
     *
     * @param status the transaction status
     * @param pageable pagination information
     * @return a page of stock transactions with the given status
     */
    Page<StockTransaction> findByStatus(TransactionStatus status, Pageable pageable);

    /**
     * Finds stock transactions by item ID with pagination.
     *
     * @param itemId the item ID
     * @param pageable pagination information
     * @return a page of stock transactions for the given item
     */
    Page<StockTransaction> findByItemId(Long itemId, Pageable pageable);

    /**
     * Finds stock transactions by transaction type with pagination.
     *
     * @param transactionType the transaction type
     * @param pageable pagination information
     * @return a page of stock transactions with the given type
     */
    Page<StockTransaction> findByTransactionType(TransactionType transactionType, Pageable pageable);

    /**
     * Finds stock transactions within a date range with pagination.
     *
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @param pageable pagination information
     * @return a page of stock transactions within the date range
     */
    Page<StockTransaction> findByTransactionDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    /**
     * Finds stock transactions pending approval (status = CREATED).
     *
     * @param pageable pagination information
     * @return a page of stock transactions pending approval
     */
    @Query("SELECT st FROM StockTransaction st WHERE st.status = com.joven.inventory.enums.TransactionStatus.CREATED")
    Page<StockTransaction> findPendingApproval(Pageable pageable);

    /**
     * Counts stock transactions by status.
     *
     * @param status the transaction status
     * @return the count of transactions with the given status
     */
    long countByStatus(TransactionStatus status);
}
