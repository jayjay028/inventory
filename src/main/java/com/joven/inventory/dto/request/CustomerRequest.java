package com.joven.inventory.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating and updating customers.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    private String name;

    @Size(max = 20, message = "TIN must not exceed 20 characters")
    private String tin;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 150, message = "Contact person must not exceed 150 characters")
    private String contactPerson;

    @Size(max = 20, message = "Contact number must not exceed 20 characters")
    private String contactNumber;

    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;
}
