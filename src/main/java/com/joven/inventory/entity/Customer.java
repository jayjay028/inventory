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
 * JPA entity representing a customer. Maps to the customers table.
 * Used for POS sales and stock-out transactions requiring Official Receipt or Sales Invoice.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** Tax Identification Number */
    @Column(name = "tin", length = 20)
    private String tin;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
