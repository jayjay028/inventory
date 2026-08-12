package com.joven.inventory.entity;

import com.joven.inventory.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity representing an inventory item. Maps to the items table.
 * Contains pricing, unit of measure, category reference, and stock threshold settings.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_code", nullable = false, unique = true, length = 50)
    private String itemCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Unit of measure (e.g., pcs, kg, box) */
    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    /** Selling price */
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    /** Acquisition/purchase cost price */
    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    /** Minimum stock level before reorder alert is triggered */
    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 0;

    /** Whether this item is subject to tax computation */
    @Column(name = "taxable", nullable = false)
    private Boolean taxable = true;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
