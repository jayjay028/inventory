package com.joven.inventory.controller;

import com.joven.inventory.common.ApiResponse;
import com.joven.inventory.dto.response.DashboardResponse;
import com.joven.inventory.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for dashboard data retrieval.
 * Provides a single endpoint to retrieve aggregated system metrics
 * including sales, inventory, and stock transaction summaries.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retrieves aggregated dashboard data including item counts, sales metrics,
     * stock values, recent transactions, low stock alerts, and top-selling items.
     *
     * @return the API response containing dashboard data
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        DashboardResponse response = dashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", response));
    }
}
