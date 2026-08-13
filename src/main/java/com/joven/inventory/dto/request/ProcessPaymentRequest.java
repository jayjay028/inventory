package com.joven.inventory.dto.request;

import com.joven.inventory.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for processing payment on a sale.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    @NotNull
    private PaymentMethod paymentMethod;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amountTendered;

    @Valid
    private List<SalePaymentRequest> payments;

    @Size(max = 100)
    private String referenceNo;
}
