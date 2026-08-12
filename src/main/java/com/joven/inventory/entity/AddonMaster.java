package com.joven.inventory.entity;

import com.joven.inventory.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity representing a predefined add-on charge type. Maps to the addon_master table.
 * Used as a template when adding charges to transactions or POS sales.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Entity
@Table(name = "addon_master")
@Getter
@Setter
@NoArgsConstructor
public class AddonMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /** Default amount pre-filled when this add-on is selected */
    @Column(name = "default_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal defaultAmount = BigDecimal.ZERO;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
