package com.joven.inventory.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing an add-on master record.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
public class AddonMasterResponse {

    private Long id;
    private String name;
    private BigDecimal defaultAmount;
    private Boolean active;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
