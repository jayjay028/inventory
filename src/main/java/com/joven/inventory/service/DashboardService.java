package com.joven.inventory.service;

import com.joven.inventory.dto.response.DashboardResponse;

/**
 * Service interface for dashboard data aggregation.
 * Provides consolidated metrics for the inventory and POS system overview.
 *
 * @author Joven Q. Divinagracia Jr.
 */
public interface DashboardService {

    /**
     * Retrieves aggregated dashboard data including item counts, sales metrics,
     * stock values, recent transactions, low stock alerts, and top-selling items.
     *
     * @return the dashboard response containing all aggregated metrics
     */
    DashboardResponse getDashboard();
}
