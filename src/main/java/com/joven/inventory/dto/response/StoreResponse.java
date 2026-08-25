package com.joven.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for store data returned to API consumers.
 * Contains all store fields including active status.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    /**
     * The unique identifier of the store.
     */
    private Long id;

    /**
     * The store code.
     */
    private String code;

    /**
     * The store name.
     */
    private String name;

    /**
     * The store address.
     */
    private String address;

    /**
     * The store tax identification number.
     */
    private String tin;

    /**
     * The store contact phone number.
     */
    private String phone;

    /**
     * Whether the store is active.
     */
    private Boolean active;
}
