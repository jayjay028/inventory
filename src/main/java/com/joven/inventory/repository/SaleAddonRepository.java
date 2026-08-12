package com.joven.inventory.repository;

import com.joven.inventory.entity.SaleAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link SaleAddon} entity.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Repository
public interface SaleAddonRepository extends JpaRepository<SaleAddon, Long> {

    /**
     * Finds all add-ons for a given sale.
     *
     * @param saleId the sale ID
     * @return a list of sale add-ons
     */
    List<SaleAddon> findBySaleId(Long saleId);

    /**
     * Deletes all add-ons for a given sale.
     *
     * @param saleId the sale ID
     */
    void deleteBySaleId(Long saleId);
}
