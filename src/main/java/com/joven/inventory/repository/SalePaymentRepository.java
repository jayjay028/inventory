package com.joven.inventory.repository;

import com.joven.inventory.entity.SalePayment;
import com.joven.inventory.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for {@link SalePayment} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface SalePaymentRepository extends JpaRepository<SalePayment, Long> {

    /**
     * Finds all payments for a given sale.
     *
     * @param saleId the sale ID
     * @return a list of sale payments
     */
    List<SalePayment> findBySaleId(Long saleId);

    /**
     * Deletes all payments for a given sale.
     *
     * @param saleId the sale ID
     */
    void deleteBySaleId(Long saleId);

    /**
     * Calculates the sum of payment amounts by payment method within a date range.
     *
     * @param method the payment method
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @return the total amount for the specified payment method and date range
     */
    @Query("SELECT COALESCE(SUM(sp.amount), 0) FROM SalePayment sp " +
            "WHERE sp.paymentMethod = :method AND sp.sale.saleDate BETWEEN :from AND :to")
    BigDecimal sumByPaymentMethodAndDateRange(@Param("method") PaymentMethod method,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);
}
