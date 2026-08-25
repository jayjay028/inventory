package com.joven.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating and updating stores.
 * Contains validated fields for store code, name, and contact details.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRequest {

    /**
     * The store code. Must not be blank and cannot exceed 20 characters.
     */
    @NotBlank(message = "Store code is required")
    @Size(max = 20, message = "Store code must not exceed 20 characters")
    private String code;

    /**
     * The store name. Must not be blank and cannot exceed 150 characters.
     */
    @NotBlank(message = "Store name is required")
    @Size(max = 150, message = "Store name must not exceed 150 characters")
    private String name;

    /**
     * The store address. Optional, cannot exceed 255 characters.
     */
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    /**
     * The store tax identification number. Optional, cannot exceed 20 characters.
     */
    @Size(max = 20, message = "TIN must not exceed 20 characters")
    private String tin;

    /**
     * The store contact phone number. Optional, cannot exceed 30 characters.
     */
    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;
}
