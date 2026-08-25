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

/**
 * JPA entity representing a store/branch location. Maps to the stores table.
 * Each store maintains its own stock, sales, shifts, and document numbering
 * sequences. Master data (items, categories, customers, suppliers) is shared
 * globally across all stores.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "tin", length = 20)
    private String tin;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
