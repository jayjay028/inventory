package com.joven.inventory.repository;

import com.joven.inventory.entity.Shift;
import com.joven.inventory.enums.ShiftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link Shift} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    /**
     * Finds a shift by cashier and status.
     *
     * @param cashier the cashier username
     * @param status the shift status
     * @return an Optional containing the shift if found
     */
    Optional<Shift> findByCashierAndStatus(String cashier, ShiftStatus status);

    /**
     * Finds shifts by status with pagination.
     *
     * @param status the shift status
     * @param pageable pagination information
     * @return a page of shifts with the given status
     */
    Page<Shift> findByStatus(ShiftStatus status, Pageable pageable);

    /**
     * Finds shifts by cashier with pagination.
     *
     * @param cashier the cashier username
     * @param pageable pagination information
     * @return a page of shifts for the given cashier
     */
    Page<Shift> findByCashier(String cashier, Pageable pageable);

    /**
     * Finds the current open shift for a specific cashier.
     *
     * @param cashier the cashier username
     * @return an Optional containing the open shift if found
     */
    @Query("SELECT s FROM Shift s WHERE s.cashier = :cashier AND s.status = com.joven.inventory.enums.ShiftStatus.OPEN")
    Optional<Shift> findCurrentOpenShift(@Param("cashier") String cashier);
}
