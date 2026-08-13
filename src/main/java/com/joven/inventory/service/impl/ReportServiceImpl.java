package com.joven.inventory.service.impl;

import com.joven.inventory.entity.Item;
import com.joven.inventory.entity.Sale;
import com.joven.inventory.entity.SaleItem;
import com.joven.inventory.entity.Shift;
import com.joven.inventory.entity.Stock;
import com.joven.inventory.entity.StockTransaction;
import com.joven.inventory.enums.PaymentMethod;
import com.joven.inventory.enums.SaleStatus;
import com.joven.inventory.enums.TransactionStatus;
import com.joven.inventory.enums.TransactionType;
import com.joven.inventory.exception.BusinessRuleException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.repository.CategoryRepository;
import com.joven.inventory.repository.ItemRepository;
import com.joven.inventory.repository.SaleItemRepository;
import com.joven.inventory.repository.SalePaymentRepository;
import com.joven.inventory.repository.SaleRepository;
import com.joven.inventory.repository.ShiftRepository;
import com.joven.inventory.repository.StockRepository;
import com.joven.inventory.repository.StockTransactionRepository;
import com.joven.inventory.service.AppSettingService;
import com.joven.inventory.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ReportService} providing PDF report generation
 * using JasperReports. Queries data from repositories, builds report data sources,
 * and compiles/fills JasperReports templates to produce PDF output.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final SalePaymentRepository salePaymentRepository;
    private final ShiftRepository shiftRepository;
    private final AppSettingService appSettingService;
    private final ResourceLoader resourceLoader;

    @Value("${app.reports.template-path:classpath:reports/}")
    private String templatePath;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

    // ==================== Inventory Reports ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateStockLevelReport(Long categoryId) {
        log.info("Generating stock level report. categoryId={}", categoryId);

        List<Stock> stocks = stockRepository.findAll();
        List<Map<String, Object>> data = stocks.stream()
                .filter(stock -> categoryId == null || stock.getItem().getCategory().getId().equals(categoryId))
                .map(stock -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    Item item = stock.getItem();
                    row.put("itemCode", item.getItemCode());
                    row.put("itemName", item.getName());
                    row.put("category", item.getCategory().getName());
                    row.put("unit", item.getUnit());
                    row.put("quantityOnHand", stock.getQuantityOnHand());
                    row.put("reorderLevel", item.getReorderLevel());
                    row.put("costPrice", item.getCostPrice());
                    row.put("sellingPrice", item.getPrice());
                    row.put("stockValue", item.getCostPrice().multiply(BigDecimal.valueOf(stock.getQuantityOnHand())));
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Stock Level Report");
        parameters.put("categoryFilter", categoryId != null ? getCategoryName(categoryId) : "All Categories");

        return generatePdf("stock_level", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateStockMovementReport(LocalDateTime from, LocalDateTime to, Long categoryId) {
        log.info("Generating stock movement report. from={}, to={}, categoryId={}", from, to, categoryId);

        List<StockTransaction> transactions = stockTransactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.APPROVED)
                .filter(t -> !t.getTransactionDate().isBefore(from) && !t.getTransactionDate().isAfter(to))
                .filter(t -> categoryId == null || t.getItem().getCategory().getId().equals(categoryId))
                .collect(Collectors.toList());

        List<Map<String, Object>> data = transactions.stream()
                .map(t -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("transactionDate", t.getTransactionDate().format(DATETIME_FORMATTER));
                    row.put("itemCode", t.getItem().getItemCode());
                    row.put("itemName", t.getItem().getName());
                    row.put("transactionType", t.getTransactionType().name());
                    row.put("quantity", t.getQuantity());
                    row.put("unitCost", t.getUnitCost());
                    row.put("totalAmount", t.getTotalAmount());
                    row.put("referenceNo", t.getReferenceNo());
                    row.put("remarks", t.getRemarks());
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Stock Movement Report");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));
        parameters.put("categoryFilter", categoryId != null ? getCategoryName(categoryId) : "All Categories");

        return generatePdf("stock_movement", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateLowStockReport() {
        log.info("Generating low stock report");

        Pageable all = Pageable.unpaged();
        List<Item> items = itemRepository.findLowStockItems(all).getContent();

        List<Map<String, Object>> data = items.stream()
                .map(item -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("itemCode", item.getItemCode());
                    row.put("itemName", item.getName());
                    row.put("category", item.getCategory().getName());
                    row.put("unit", item.getUnit());
                    row.put("reorderLevel", item.getReorderLevel());
                    int quantityOnHand = stockRepository.findByItemId(item.getId())
                            .map(Stock::getQuantityOnHand)
                            .orElse(0);
                    row.put("quantityOnHand", quantityOnHand);
                    row.put("shortage", item.getReorderLevel() - quantityOnHand);
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Low Stock Report");

        return generatePdf("low_stock", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateTransactionSummaryReport(LocalDateTime from, LocalDateTime to, String type) {
        log.info("Generating transaction summary report. from={}, to={}, type={}", from, to, type);

        List<StockTransaction> transactions = stockTransactionRepository.findAll().stream()
                .filter(t -> !t.getTransactionDate().isBefore(from) && !t.getTransactionDate().isAfter(to))
                .filter(t -> "ALL".equalsIgnoreCase(type) || t.getTransactionType().name().equalsIgnoreCase(type))
                .collect(Collectors.toList());

        List<Map<String, Object>> data = transactions.stream()
                .map(t -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("transactionDate", t.getTransactionDate().format(DATETIME_FORMATTER));
                    row.put("transactionType", t.getTransactionType().name());
                    row.put("status", t.getStatus().name());
                    row.put("itemCode", t.getItem().getItemCode());
                    row.put("itemName", t.getItem().getName());
                    row.put("quantity", t.getQuantity());
                    row.put("unitCost", t.getUnitCost());
                    row.put("totalAmount", t.getTotalAmount());
                    row.put("documentNo", t.getDocumentNo());
                    row.put("referenceNo", t.getReferenceNo());
                    row.put("createdBy", t.getCreatedBy());
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Transaction Summary Report");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));
        parameters.put("typeFilter", type);

        return generatePdf("transaction_summary", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateItemListReport(Long categoryId) {
        log.info("Generating item list report. categoryId={}", categoryId);

        List<Item> items;
        if (categoryId != null) {
            items = itemRepository.findByCategoryIdAndActiveTrue(categoryId, Pageable.unpaged()).getContent();
        } else {
            items = itemRepository.findByActiveTrue(Pageable.unpaged()).getContent();
        }

        List<Map<String, Object>> data = items.stream()
                .map(item -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("itemCode", item.getItemCode());
                    row.put("itemName", item.getName());
                    row.put("description", item.getDescription());
                    row.put("category", item.getCategory().getName());
                    row.put("unit", item.getUnit());
                    row.put("costPrice", item.getCostPrice());
                    row.put("sellingPrice", item.getPrice());
                    row.put("reorderLevel", item.getReorderLevel());
                    row.put("taxable", item.getTaxable() ? "Yes" : "No");
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Item List Report");
        parameters.put("categoryFilter", categoryId != null ? getCategoryName(categoryId) : "All Categories");

        return generatePdf("item_list", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateInventoryCountReport(Long categoryId) {
        log.info("Generating inventory count report. categoryId={}", categoryId);

        List<Stock> stocks = stockRepository.findAll();
        List<Map<String, Object>> data = stocks.stream()
                .filter(stock -> categoryId == null || stock.getItem().getCategory().getId().equals(categoryId))
                .map(stock -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    Item item = stock.getItem();
                    row.put("itemCode", item.getItemCode());
                    row.put("itemName", item.getName());
                    row.put("category", item.getCategory().getName());
                    row.put("unit", item.getUnit());
                    row.put("systemQuantity", stock.getQuantityOnHand());
                    row.put("physicalQuantity", "");
                    row.put("variance", "");
                    row.put("remarks", "");
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Inventory Count Sheet");
        parameters.put("categoryFilter", categoryId != null ? getCategoryName(categoryId) : "All Categories");
        parameters.put("countDate", LocalDate.now().format(DATE_FORMATTER));

        return generatePdf("inventory_count", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateStockValuationReport(Long categoryId) {
        log.info("Generating stock valuation report. categoryId={}", categoryId);

        List<Stock> stocks = stockRepository.findAll();
        List<Map<String, Object>> data = stocks.stream()
                .filter(stock -> categoryId == null || stock.getItem().getCategory().getId().equals(categoryId))
                .map(stock -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    Item item = stock.getItem();
                    BigDecimal costValue = item.getCostPrice().multiply(BigDecimal.valueOf(stock.getQuantityOnHand()));
                    BigDecimal retailValue = item.getPrice().multiply(BigDecimal.valueOf(stock.getQuantityOnHand()));
                    row.put("itemCode", item.getItemCode());
                    row.put("itemName", item.getName());
                    row.put("category", item.getCategory().getName());
                    row.put("unit", item.getUnit());
                    row.put("quantityOnHand", stock.getQuantityOnHand());
                    row.put("costPrice", item.getCostPrice());
                    row.put("sellingPrice", item.getPrice());
                    row.put("costValue", costValue);
                    row.put("retailValue", retailValue);
                    row.put("potentialProfit", retailValue.subtract(costValue));
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Stock Valuation Report");
        parameters.put("categoryFilter", categoryId != null ? getCategoryName(categoryId) : "All Categories");

        BigDecimal totalCostValue = data.stream()
                .map(row -> (BigDecimal) row.get("costValue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRetailValue = data.stream()
                .map(row -> (BigDecimal) row.get("retailValue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        parameters.put("totalCostValue", totalCostValue);
        parameters.put("totalRetailValue", totalRetailValue);
        parameters.put("totalPotentialProfit", totalRetailValue.subtract(totalCostValue));

        return generatePdf("stock_valuation", parameters, data);
    }

    // ==================== Financial Reports ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateGrossProfitReport(LocalDateTime from, LocalDateTime to, Long categoryId) {
        log.info("Generating gross profit report. from={}, to={}, categoryId={}", from, to, categoryId);

        List<Sale> sales = saleRepository.findTodaySales(from, to).stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .collect(Collectors.toList());

        List<Map<String, Object>> data = new ArrayList<>();
        for (Sale sale : sales) {
            List<SaleItem> items = saleItemRepository.findBySaleId(sale.getId());
            for (SaleItem item : items) {
                if (categoryId != null && !item.getItem().getCategory().getId().equals(categoryId)) {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("saleDate", sale.getSaleDate().format(DATETIME_FORMATTER));
                row.put("saleNo", sale.getSaleNo());
                row.put("itemCode", item.getItemCode());
                row.put("itemName", item.getItemName());
                row.put("quantity", item.getQuantity());
                row.put("unitPrice", item.getUnitPrice());
                row.put("unitCost", item.getUnitCost());
                BigDecimal revenue = item.getLineTotal();
                BigDecimal cost = item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal profit = revenue.subtract(cost);
                row.put("revenue", revenue);
                row.put("cost", cost);
                row.put("grossProfit", profit);
                row.put("margin", revenue.compareTo(BigDecimal.ZERO) > 0
                        ? profit.multiply(BigDecimal.valueOf(100)).divide(revenue, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);
                data.add(row);
            }
        }

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Gross Profit Report");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));
        parameters.put("categoryFilter", categoryId != null ? getCategoryName(categoryId) : "All Categories");

        BigDecimal totalRevenue = data.stream()
                .map(row -> (BigDecimal) row.get("revenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = data.stream()
                .map(row -> (BigDecimal) row.get("cost"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        parameters.put("totalRevenue", totalRevenue);
        parameters.put("totalCost", totalCost);
        parameters.put("totalGrossProfit", totalRevenue.subtract(totalCost));

        return generatePdf("gross_profit", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateProfitShareReport(LocalDateTime from, LocalDateTime to, String groupBy) {
        log.info("Generating profit share report. from={}, to={}, groupBy={}", from, to, groupBy);

        List<Sale> sales = saleRepository.findTodaySales(from, to).stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .collect(Collectors.toList());

        Map<String, BigDecimal[]> aggregation = new LinkedHashMap<>();

        for (Sale sale : sales) {
            List<SaleItem> items = saleItemRepository.findBySaleId(sale.getId());
            for (SaleItem item : items) {
                String key = "CATEGORY".equalsIgnoreCase(groupBy)
                        ? item.getItem().getCategory().getName()
                        : item.getItemName();
                BigDecimal revenue = item.getLineTotal();
                BigDecimal cost = item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal profit = revenue.subtract(cost);

                aggregation.computeIfAbsent(key, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
                BigDecimal[] values = aggregation.get(key);
                values[0] = values[0].add(revenue);
                values[1] = values[1].add(cost);
                values[2] = values[2].add(profit);
            }
        }

        BigDecimal grandTotalProfit = aggregation.values().stream()
                .map(v -> v[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> data = aggregation.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("groupName", entry.getKey());
                    row.put("revenue", entry.getValue()[0]);
                    row.put("cost", entry.getValue()[1]);
                    row.put("profit", entry.getValue()[2]);
                    row.put("share", grandTotalProfit.compareTo(BigDecimal.ZERO) > 0
                            ? entry.getValue()[2].multiply(BigDecimal.valueOf(100))
                            .divide(grandTotalProfit, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Profit Share Report");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));
        parameters.put("groupBy", groupBy);
        parameters.put("grandTotalProfit", grandTotalProfit);

        return generatePdf("profit_share", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateSalesSummaryReport(LocalDateTime from, LocalDateTime to) {
        log.info("Generating sales summary report. from={}, to={}", from, to);

        List<Sale> sales = saleRepository.findTodaySales(from, to).stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .collect(Collectors.toList());

        List<Map<String, Object>> data = sales.stream()
                .map(sale -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("saleDate", sale.getSaleDate().format(DATETIME_FORMATTER));
                    row.put("saleNo", sale.getSaleNo());
                    row.put("cashier", sale.getCreatedBy());
                    row.put("subtotal", sale.getSubtotal());
                    row.put("discountAmount", sale.getDiscountAmount());
                    row.put("addonsTotal", sale.getAddonsTotal());
                    row.put("taxAmount", sale.getTaxAmount());
                    row.put("totalAmount", sale.getTotalAmount());
                    row.put("paymentMethod", sale.getPaymentMethod().name());
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Sales Summary Report");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));
        parameters.put("totalSalesCount", sales.size());

        BigDecimal totalAmount = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = sales.stream()
                .map(Sale::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax = sales.stream()
                .map(Sale::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        parameters.put("grandTotal", totalAmount);
        parameters.put("totalDiscount", totalDiscount);
        parameters.put("totalTax", totalTax);

        return generatePdf("sales_summary", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generatePurchaseSummaryReport(LocalDateTime from, LocalDateTime to, Long supplierId) {
        log.info("Generating purchase summary report. from={}, to={}, supplierId={}", from, to, supplierId);

        List<StockTransaction> transactions = stockTransactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.IN)
                .filter(t -> t.getStatus() == TransactionStatus.APPROVED)
                .filter(t -> !t.getTransactionDate().isBefore(from) && !t.getTransactionDate().isAfter(to))
                .filter(t -> supplierId == null || (t.getSupplier() != null && t.getSupplier().getId().equals(supplierId)))
                .collect(Collectors.toList());

        List<Map<String, Object>> data = transactions.stream()
                .map(t -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("transactionDate", t.getTransactionDate().format(DATETIME_FORMATTER));
                    row.put("documentNo", t.getDocumentNo());
                    row.put("referenceNo", t.getReferenceNo());
                    row.put("supplier", t.getSupplier() != null ? t.getSupplier().getName() : "N/A");
                    row.put("itemCode", t.getItem().getItemCode());
                    row.put("itemName", t.getItem().getName());
                    row.put("quantity", t.getQuantity());
                    row.put("unitCost", t.getUnitCost());
                    row.put("totalAmount", t.getTotalAmount());
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Purchase Summary Report");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));
        parameters.put("supplierFilter", supplierId != null ? "Filtered" : "All Suppliers");

        BigDecimal grandTotal = transactions.stream()
                .map(StockTransaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        parameters.put("grandTotal", grandTotal);

        return generatePdf("purchase_summary", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateVatSummaryReport(LocalDateTime from, LocalDateTime to) {
        log.info("Generating VAT summary report. from={}, to={}", from, to);

        List<Sale> sales = saleRepository.findTodaySales(from, to).stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .filter(s -> s.getTaxEnabled())
                .collect(Collectors.toList());

        List<StockTransaction> purchases = stockTransactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.IN)
                .filter(t -> t.getStatus() == TransactionStatus.APPROVED)
                .filter(t -> t.getTaxEnabled())
                .filter(t -> !t.getTransactionDate().isBefore(from) && !t.getTransactionDate().isAfter(to))
                .collect(Collectors.toList());

        BigDecimal outputVat = sales.stream()
                .map(Sale::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outputVatable = sales.stream()
                .map(Sale::getVatableAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal inputVat = purchases.stream()
                .map(StockTransaction::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal inputVatable = purchases.stream()
                .map(StockTransaction::getVatableAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> outputRow = new LinkedHashMap<>();
        outputRow.put("type", "Output VAT (Sales)");
        outputRow.put("vatableAmount", outputVatable);
        outputRow.put("vatAmount", outputVat);
        outputRow.put("transactionCount", sales.size());
        data.add(outputRow);

        Map<String, Object> inputRow = new LinkedHashMap<>();
        inputRow.put("type", "Input VAT (Purchases)");
        inputRow.put("vatableAmount", inputVatable);
        inputRow.put("vatAmount", inputVat);
        inputRow.put("transactionCount", purchases.size());
        data.add(inputRow);

        Map<String, Object> netRow = new LinkedHashMap<>();
        netRow.put("type", "Net VAT Payable");
        netRow.put("vatableAmount", outputVatable.subtract(inputVatable));
        netRow.put("vatAmount", outputVat.subtract(inputVat));
        netRow.put("transactionCount", 0);
        data.add(netRow);

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "VAT Summary Report");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));
        parameters.put("outputVat", outputVat);
        parameters.put("inputVat", inputVat);
        parameters.put("netVat", outputVat.subtract(inputVat));

        return generatePdf("vat_summary", parameters, data);
    }

    // ==================== POS Reports ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateDailySalesReport(LocalDate date) {
        log.info("Generating daily sales report. date={}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findTodaySales(startOfDay, endOfDay).stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .collect(Collectors.toList());

        List<Map<String, Object>> data = sales.stream()
                .map(sale -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("saleTime", sale.getSaleDate().format(DateTimeFormatter.ofPattern("hh:mm a")));
                    row.put("saleNo", sale.getSaleNo());
                    row.put("cashier", sale.getCreatedBy());
                    row.put("totalAmount", sale.getTotalAmount());
                    row.put("paymentMethod", sale.getPaymentMethod().name());
                    row.put("discountAmount", sale.getDiscountAmount());
                    row.put("status", sale.getStatus().name());
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Daily Sales Report");
        parameters.put("reportDate", date.format(DATE_FORMATTER));
        parameters.put("totalTransactions", sales.size());

        BigDecimal grandTotal = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        parameters.put("grandTotal", grandTotal);

        long voidedCount = saleRepository.findTodaySales(startOfDay, endOfDay).stream()
                .filter(s -> s.getStatus() == SaleStatus.VOIDED)
                .count();
        parameters.put("voidedCount", voidedCount);

        return generatePdf("daily_sales", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateShiftReport(Long shiftId) {
        log.info("Generating shift report. shiftId={}", shiftId);

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + shiftId));

        List<Sale> sales = saleRepository.findByShiftId(shiftId);
        List<Sale> paidSales = sales.stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .collect(Collectors.toList());

        List<Map<String, Object>> data = paidSales.stream()
                .map(sale -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("saleTime", sale.getSaleDate().format(DateTimeFormatter.ofPattern("hh:mm a")));
                    row.put("saleNo", sale.getSaleNo());
                    row.put("totalAmount", sale.getTotalAmount());
                    row.put("paymentMethod", sale.getPaymentMethod().name());
                    row.put("discountAmount", sale.getDiscountAmount());
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Shift Report");
        parameters.put("cashier", shift.getCashier());
        parameters.put("shiftId", shift.getId());
        parameters.put("openedAt", shift.getOpenedAt().format(DATETIME_FORMATTER));
        parameters.put("closedAt", shift.getClosedAt() != null ? shift.getClosedAt().format(DATETIME_FORMATTER) : "Open");
        parameters.put("status", shift.getStatus().name());
        parameters.put("openingAmount", shift.getOpeningAmount());
        parameters.put("closingAmount", shift.getClosingAmount());
        parameters.put("expectedAmount", shift.getExpectedAmount());
        parameters.put("difference", shift.getDifference());
        parameters.put("totalSales", shift.getTotalSales());
        parameters.put("totalTransactions", shift.getTotalTransactions());
        parameters.put("totalVoided", shift.getTotalVoided());

        return generatePdf("shift_report", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateSalesByPaymentReport(LocalDateTime from, LocalDateTime to) {
        log.info("Generating sales by payment report. from={}, to={}", from, to);

        List<Map<String, Object>> data = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (PaymentMethod method : PaymentMethod.values()) {
            BigDecimal amount = salePaymentRepository.sumByPaymentMethodAndDateRange(method, from, to);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("paymentMethod", method.name());
            row.put("totalAmount", amount);
            data.add(row);
            grandTotal = grandTotal.add(amount);
        }

        // Calculate percentages
        for (Map<String, Object> row : data) {
            BigDecimal amount = (BigDecimal) row.get("totalAmount");
            row.put("percentage", grandTotal.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(grandTotal, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
        }

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Sales by Payment Method");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));
        parameters.put("grandTotal", grandTotal);

        return generatePdf("sales_by_payment", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateSalesByCashierReport(LocalDateTime from, LocalDateTime to) {
        log.info("Generating sales by cashier report. from={}, to={}", from, to);

        List<Sale> sales = saleRepository.findTodaySales(from, to).stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .collect(Collectors.toList());

        Map<String, List<Sale>> grouped = sales.stream()
                .collect(Collectors.groupingBy(Sale::getCreatedBy));

        List<Map<String, Object>> data = grouped.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("cashier", entry.getKey());
                    row.put("transactionCount", entry.getValue().size());
                    BigDecimal totalAmount = entry.getValue().stream()
                            .map(Sale::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    row.put("totalAmount", totalAmount);
                    BigDecimal totalDiscount = entry.getValue().stream()
                            .map(Sale::getDiscountAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    row.put("totalDiscount", totalDiscount);
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Sales by Cashier Report");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));

        return generatePdf("sales_by_cashier", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateTopSellingReport(LocalDateTime from, LocalDateTime to, int limit) {
        log.info("Generating top selling report. from={}, to={}, limit={}", from, to, limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> topItems = saleItemRepository.findTopSellingItems(from, to, pageable).getContent();

        List<Map<String, Object>> data = topItems.stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("itemId", row[0]);
                    map.put("itemName", row[1]);
                    map.put("totalQuantity", row[2]);
                    map.put("totalAmount", row[3]);
                    return map;
                })
                .collect(Collectors.toList());

        // Add ranking
        for (int i = 0; i < data.size(); i++) {
            data.get(i).put("rank", i + 1);
        }

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Top Selling Items Report");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));
        parameters.put("limit", limit);

        return generatePdf("top_selling", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateVoidedTransactionsReport(LocalDateTime from, LocalDateTime to) {
        log.info("Generating voided transactions report. from={}, to={}", from, to);

        List<Sale> voidedSales = saleRepository.findTodaySales(from, to).stream()
                .filter(s -> s.getStatus() == SaleStatus.VOIDED)
                .collect(Collectors.toList());

        List<Map<String, Object>> data = voidedSales.stream()
                .map(sale -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("saleDate", sale.getSaleDate().format(DATETIME_FORMATTER));
                    row.put("saleNo", sale.getSaleNo());
                    row.put("totalAmount", sale.getTotalAmount());
                    row.put("cashier", sale.getCreatedBy());
                    row.put("voidedBy", sale.getVoidedBy());
                    row.put("voidedAt", sale.getVoidedAt() != null ? sale.getVoidedAt().format(DATETIME_FORMATTER) : "");
                    row.put("voidReason", sale.getVoidReason());
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Voided Transactions Report");
        parameters.put("dateFrom", from.format(DATETIME_FORMATTER));
        parameters.put("dateTo", to.format(DATETIME_FORMATTER));
        parameters.put("totalVoidedCount", voidedSales.size());

        BigDecimal totalVoidedAmount = voidedSales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        parameters.put("totalVoidedAmount", totalVoidedAmount);

        return generatePdf("voided_transactions", parameters, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateHourlySalesReport(LocalDate date) {
        log.info("Generating hourly sales report. date={}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findTodaySales(startOfDay, endOfDay).stream()
                .filter(s -> s.getStatus() == SaleStatus.PAID || s.getStatus() == SaleStatus.CLOSED)
                .collect(Collectors.toList());

        Map<Integer, List<Sale>> hourlyGrouped = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getSaleDate().getHour()));

        List<Map<String, Object>> data = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            List<Sale> hourSales = hourlyGrouped.getOrDefault(hour, List.of());
            if (!hourSales.isEmpty()) {
                Map<String, Object> row = new LinkedHashMap<>();
                String hourLabel = String.format("%02d:00 - %02d:59", hour, hour);
                row.put("hour", hourLabel);
                row.put("transactionCount", hourSales.size());
                BigDecimal totalAmount = hourSales.stream()
                        .map(Sale::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                row.put("totalAmount", totalAmount);
                BigDecimal avgAmount = totalAmount.divide(BigDecimal.valueOf(hourSales.size()), 2, RoundingMode.HALF_UP);
                row.put("averageAmount", avgAmount);
                data.add(row);
            }
        }

        Map<String, Object> parameters = buildCommonParameters();
        parameters.put("reportTitle", "Hourly Sales Report");
        parameters.put("reportDate", date.format(DATE_FORMATTER));
        parameters.put("totalTransactions", sales.size());

        BigDecimal grandTotal = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        parameters.put("grandTotal", grandTotal);

        return generatePdf("hourly_sales", parameters, data);
    }

    // ==================== Private Helpers ====================

    /**
     * Builds common report parameters from application settings.
     *
     * @return a map of common parameter keys and values
     */
    private Map<String, Object> buildCommonParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("businessName", appSettingService.getValueOrDefault("business_name", ""));
        parameters.put("businessAddress", appSettingService.getValueOrDefault("business_address", ""));
        parameters.put("businessTin", appSettingService.getValueOrDefault("business_tin", ""));
        parameters.put("reportDate", LocalDateTime.now().format(DATETIME_FORMATTER));
        parameters.put("currencySymbol", "₱");
        return parameters;
    }

    /**
     * Generates a PDF report from a JasperReports template.
     * Loads the .jrxml template from the configured template path, compiles it,
     * fills it with the provided data source and parameters, then exports to PDF.
     *
     * @param templateName the template file name (without extension)
     * @param parameters   the report parameters
     * @param dataSource   the report data source collection
     * @return the generated PDF as a byte array
     * @throws BusinessRuleException if template is missing or report generation fails
     */
    private byte[] generatePdf(String templateName, Map<String, Object> parameters, Collection<?> dataSource) {
        try {
            String templateLocation = templatePath + templateName + ".jrxml";
            Resource resource = resourceLoader.getResource(templateLocation);

            if (!resource.exists()) {
                log.error("Report template not found: {}", templateLocation);
                throw new BusinessRuleException("Report template not found: " + templateName);
            }

            try (InputStream inputStream = resource.getInputStream()) {
                JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

                JRBeanCollectionDataSource jrDataSource = new JRBeanCollectionDataSource(
                        dataSource != null ? dataSource : List.of());

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jrDataSource);

                byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
                log.info("Report '{}' generated successfully. Size: {} bytes", templateName, pdfBytes.length);

                return pdfBytes;
            }
        } catch (BusinessRuleException e) {
            throw e;
        } catch (JRException e) {
            log.error("Failed to generate report '{}': {}", templateName, e.getMessage(), e);
            throw new BusinessRuleException("Failed to generate report: " + templateName + ". " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error generating report '{}': {}", templateName, e.getMessage(), e);
            throw new BusinessRuleException("Unexpected error generating report: " + templateName);
        }
    }

    /**
     * Retrieves the category name by ID.
     *
     * @param categoryId the category ID
     * @return the category name
     */
    private String getCategoryName(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(category -> category.getName())
                .orElse("Unknown Category");
    }
}
