package com.joven.inventory.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String field;
    private String message;

    public ApiError(String message) {
        this.message = message;
    }
}
