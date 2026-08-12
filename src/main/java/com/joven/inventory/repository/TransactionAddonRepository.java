package com.joven.inventory.repository;

import com.joven.inventory.entity.TransactionAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link TransactionAddon} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface TransactionAddonRepository extends JpaRepository<TransactionAddon, Long> {

    /**
     * Finds all add-ons for a given stock transaction.
     *
     * @param transactionId the stock transaction ID
     * @return a list of transaction add-ons
     */
    List<TransactionAddon> findByTransactionId(Long transactionId);

    /**
     * Deletes all add-ons for a given stock transaction.
     *
     * @param transactionId the stock transaction ID
     */
    void deleteByTransactionId(Long transactionId);
}
