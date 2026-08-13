package com.joven.inventory.service.impl;

import com.joven.inventory.dto.response.DashboardResponse;
import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Sale;
import com.joven.inventory.entity.SaleItem;
import com.joven.inventory.entity.Stock;
import com.joven.inventory.entity.StockTransaction;
import com.joven.inventory.enums.SaleStatus;
import com.joven.inventory.enums.TransactionStatus;
import com.joven.inventory.repository.CategoryRepository;
import com.joven.inventory.repository.ItemRepository;
import com.joven.inventory.repository.SaleItemRepository;
import com.joven.inventory.repository.SaleRepository;
import com.joven.inventory.repository.StockRepository;
import com.joven.inventory.repository.StockTransactionRepository;
import com.joven.inventory.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link DashboardService}.
 * Aggregates data from multiple repositories to produce dashboard metrics.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public DashboardResponse getDashboard() {
        // Active items count
        long totalItems = itemRepository.findByActiveTrue(Pageable.ofSize(1)).getTotalElements();

        // Active categories count
        long totalCategories = categoryRepository.findByActiveTrueOrderByNameAsc().size();

        // Load all stocks with items (JOIN FETCH to avoid N+1)
        List<Stock> allStocks = stockRepository.findAllWithItem(Pageable.unpaged()).getContent();

        // Total stock value = sum of (quantityOnHand * costPrice)
        BigDecimal totalStockValue = allStocks.stream()
                .map(stock -> stock.getItem().getCostPrice()
                        .multiply(BigDecimal.valueOf(stock.getQuantityOnHand())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Low stock count
        long lowStockCount = itemRepository.findLowStockItems(Pageable.ofSize(1)).getTotalElements();

        // Today's date boundaries
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        // Today's sales (PAID or CLOSED)
        List<Sale> todaySales = saleRepository.findTodaySales(startOfDay, endOfDay);
        List<Sale> todayCompletedSales = todaySales.stream()
                .filter(sale -> sale.getStatus() == SaleStatus.PAID || sale.getStatus() == SaleStatus.CLOSED)
                .toList();

        long todaySalesCount = todayCompletedSales.size();
        BigDecimal todaySalesAmount = todayCompletedSales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Today's profit = totalAmount - taxAmount - totalCost for each sale
        BigDecimal todayProfit = calculateProfit(todayCompletedSales);

        // This month's date boundaries
        LocalDateTime firstOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        // Month sales (PAID or CLOSED)
        List<Sale> monthSales = saleRepository.findTodaySales(firstOfMonth, now);
        List<Sale> monthCompletedSales = monthSales.stream()
                .filter(sale -> sale.getStatus() == SaleStatus.PAID || sale.getStatus() == SaleStatus.CLOSED)
                .toList();

        long monthSalesCount = monthCompletedSales.size();
        BigDecimal monthSalesAmount = monthCompletedSales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Pending approvals (stock transactions with CREATED status)
        long pendingApprovals = stockTransactionRepository.countByStatus(TransactionStatus.CREATED);

        // Recent transactions (last 10)
        List<DashboardResponse.RecentTransaction> recentTransactions = getRecentTransactions();

        // Low stock alerts
        List<DashboardResponse.LowStockAlert> lowStockAlerts = getLowStockAlerts(allStocks);

        // Top selling items this month
        List<DashboardResponse.TopSellingItem> topSellingItems = getTopSellingItems(firstOfMonth, now);

        return DashboardResponse.builder()
                .totalItems(totalItems)
                .totalCategories(totalCategories)
                .totalStockValue(totalStockValue)
                .lowStockCount(lowStockCount)
                .todaySalesCount(todaySalesCount)
                .todaySalesAmount(todaySalesAmount)
                .todayProfit(todayProfit)
                .monthSalesCount(monthSalesCount)
                .monthSalesAmount(monthSalesAmount)
                .pendingApprovals(pendingApprovals)
                .recentTransactions(recentTransactions)
                .lowStockAlerts(lowStockAlerts)
                .topSellingItems(topSellingItems)
                .build();
    }

    /**
     * Calculates gross profit for a list of completed sales.
     * Profit = totalAmount - taxAmount - total cost of goods sold.
     *
     * @param sales the list of completed sales
     * @return the total gross profit
     */
    private BigDecimal calculateProfit(List<Sale> sales) {
        if (sales.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalProfit = BigDecimal.ZERO;

        for (Sale sale : sales) {
            List<SaleItem> saleItems = saleItemRepository.findBySaleId(sale.getId());
            BigDecimal totalCost = saleItems.stream()
                    .map(item -> item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Profit = totalAmount - taxAmount - cost of goods sold
            BigDecimal saleProfit = sale.getTotalAmount()
                    .subtract(sale.getTaxAmount())
                    .subtract(totalCost);
            totalProfit = totalProfit.add(saleProfit);
        }

        return totalProfit;
    }

    /**
     * Retrieves the last 10 stock transactions mapped to dashboard DTOs.
     *
     * @return a list of recent transaction summaries
     */
    private List<DashboardResponse.RecentTransaction> getRecentTransactions() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<StockTransaction> transactions = stockTransactionRepository.findAll(pageRequest).getContent();

        return transactions.stream()
                .map(tx -> DashboardResponse.RecentTransaction.builder()
                        .id(tx.getId())
                        .itemName(tx.getItem().getName())
                        .transactionType(tx.getTransactionType().name())
                        .quantity(tx.getQuantity())
                        .status(tx.getStatus().name())
                        .totalAmount(tx.getTotalAmount())
                        .createdBy(tx.getCreatedBy())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .toList();
    }

    /**
     * Builds low stock alert list from pre-loaded stocks.
     * Filters items where quantityOnHand is at or below the item's reorder level.
     *
     * @param allStocks the list of all stocks with items loaded
     * @return a list of low stock alerts (max 10)
     */
    private List<DashboardResponse.LowStockAlert> getLowStockAlerts(List<Stock> allStocks) {
        return allStocks.stream()
                .filter(stock -> stock.getItem().getActive()
                        && stock.getQuantityOnHand() <= stock.getItem().getReorderLevel())
                .limit(10)
                .map(stock -> {
                    Item item = stock.getItem();
                    String categoryName = item.getCategory() != null ? item.getCategory().getName() : "";
                    return DashboardResponse.LowStockAlert.builder()
                            .itemId(item.getId())
                            .itemCode(item.getItemCode())
                            .itemName(item.getName())
                            .categoryName(categoryName)
                            .quantityOnHand(stock.getQuantityOnHand())
                            .reorderLevel(item.getReorderLevel())
                            .build();
                })
                .toList();
    }

    /**
     * Retrieves the top 5 selling items within the specified date range.
     *
     * @param from the start date
     * @param to the end date
     * @return a list of top-selling item summaries
     */
    private List<DashboardResponse.TopSellingItem> getTopSellingItems(LocalDateTime from, LocalDateTime to) {
        Page<Object[]> page = saleItemRepository.findTopSellingItems(from, to, PageRequest.of(0, 5));
        List<Object[]> results = page.getContent();

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        return results.stream()
                .map(row -> DashboardResponse.TopSellingItem.builder()
                        .itemId((Long) row[0])
                        .itemName((String) row[1])
                        .totalQuantitySold((Long) row[2])
                        .totalRevenue((BigDecimal) row[3])
                        .build())
                .toList();
    }
}
