package com.joven.inventory.common;

public final class Constants {

    private Constants() {}

    // Default pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // System user
    public static final String SYSTEM_USER = "system";

    // Date patterns
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    // Currency
    public static final String CURRENCY_SYMBOL = "₱";
    public static final String CURRENCY_CODE = "PHP";

    // Tax
    public static final java.math.BigDecimal DEFAULT_VAT_RATE = new java.math.BigDecimal("12.00");
    public static final java.math.BigDecimal VAT_DIVISOR = new java.math.BigDecimal("1.12");
    public static final java.math.BigDecimal SENIOR_PWD_DISCOUNT_RATE = new java.math.BigDecimal("0.20");

    // Document number format
    public static final String DOCUMENT_NUMBER_FORMAT = "%s%s-%05d";
}
