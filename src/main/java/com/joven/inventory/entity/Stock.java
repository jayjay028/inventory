package com.joven.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing current stock level for an item within a specific store.
 * Maps to the stock table. Each (item, store) pair has exactly one stock record,
 * enforced by a composite unique constraint.
 *
 * <p>Stock is per-store: the same item tracked independently across stores.
 * Updated when transactions are approved or POS sales are paid.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Entity
@Table(name = "stock", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stock_item_store", columnNames = {"item_id", "store_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
