package com.joven.inventory.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Custom application properties bound from application.yml under the 'app' prefix.
 * Provides typed access to JWT, CORS, reporting, POS, pagination, and audit configuration.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Reports reports = new Reports();
    private Pos pos = new Pos();
    private Pagination pagination = new Pagination();
    private Audit audit = new Audit();

    /**
     * JWT configuration properties.
     */
    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenExpiry = 1800000;
        private long refreshTokenExpiry = 604800000;
        private String issuer = "inventory-system";
    }

    /**
     * CORS configuration properties.
     */
    @Data
    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private boolean allowCredentials = true;
        private long maxAge = 3600;
    }

    /**
     * Report generation configuration properties.
     */
    @Data
    public static class Reports {
        private String templatePath;
        private String outputPath;
    }

    /**
     * Point of Sale configuration properties.
     */
    @Data
    public static class Pos {
        private String receiptTemplate;
        private String receiptWidth = "80mm";
        private boolean printingEnabled = false;
        private boolean autoPrint = false;
        private boolean kioskMode = false;
        private boolean barcodeScanner = true;
    }

    /**
     * Pagination configuration properties.
     */
    @Data
    public static class Pagination {
        private int defaultPageSize = 20;
        private int maxPageSize = 100;
    }

    /**
     * Audit trail configuration properties.
     */
    @Data
    public static class Audit {
        private boolean enabled = true;
        private boolean logIpAddress = true;
    }
}
