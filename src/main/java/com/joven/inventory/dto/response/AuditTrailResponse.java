package com.joven.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for audit trail records.
 * Used to return audit trail data to API consumers without exposing internal entity details.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrailResponse {

    private Long id;
    private String entityName;
    private Long entityId;
    private String action;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String performedBy;
    private LocalDateTime performedAt;
    private String ipAddress;
}
