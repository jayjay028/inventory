package com.joven.inventory.security;

/**
 * Defines all system permissions as bitwise constants. Each permission occupies
 * a single bit position in a long value, enabling efficient storage and evaluation
 * of user access rights using bitwise operations.
 *
 * <p>Usage example:
 * <pre>
 *     long userRights = user.getAccessRights();
 *     if (Permission.hasPermission(userRights, Permission.MANAGE_ITEMS)) {
 *         // user can manage items
 *     }
 * </pre>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class Permission {

    /** Private constructor to prevent instantiation */
    private Permission() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== Dashboard ====================

    /** Permission to view the main dashboard */
    public static final long VIEW_DASHBOARD = 1L << 0;

    // ==================== Items ====================

    /** Permission to view item listings */
    public static final long VIEW_ITEMS = 1L << 1;

    /** Permission to create, update, and delete items */
    public static final long MANAGE_ITEMS = 1L << 2;

    // ==================== Categories ====================

    /** Permission to view category listings */
    public static final long VIEW_CATEGORIES = 1L << 3;

    /** Permission to create, update, and delete categories */
    public static final long MANAGE_CATEGORIES = 1L << 4;

    // ==================== Customers ====================

    /** Permission to view customer listings */
    public static final long VIEW_CUSTOMERS = 1L << 5;

    /** Permission to create, update, and delete customers */
    public static final long MANAGE_CUSTOMERS = 1L << 6;

    // ==================== Suppliers ====================

    /** Permission to view supplier listings */
    public static final long VIEW_SUPPLIERS = 1L << 7;

    /** Permission to create, update, and delete suppliers */
    public static final long MANAGE_SUPPLIERS = 1L << 8;

    // ==================== Stock ====================

    /** Permission to view stock levels and history */
    public static final long VIEW_STOCK = 1L << 9;

    /** Permission to process stock-in transactions */
    public static final long MANAGE_STOCK_IN = 1L << 10;

    /** Permission to process stock-out transactions */
    public static final long MANAGE_STOCK_OUT = 1L << 11;

    /** Permission to process stock adjustment transactions */
    public static final long MANAGE_STOCK_ADJ = 1L << 12;

    // ==================== Transactions ====================

    /** Permission to view transaction history */
    public static final long VIEW_TRANSACTIONS = 1L << 13;

    // ==================== Point of Sale ====================

    /** Permission to use the POS terminal */
    public static final long USE_POS = 1L << 14;

    /** Permission to void completed sales */
    public static final long VOID_SALES = 1L << 15;

    // ==================== Shifts ====================

    /** Permission to manage cashier shifts */
    public static final long MANAGE_SHIFTS = 1L << 16;

    // ==================== Reports ====================

    /** Permission to view and generate reports */
    public static final long VIEW_REPORTS = 1L << 17;

    // ==================== Audit ====================

    /** Permission to view the audit trail */
    public static final long VIEW_AUDIT_TRAIL = 1L << 18;

    // ==================== Administration ====================

    /** Permission to manage system users */
    public static final long MANAGE_USERS = 1L << 19;

    /** Permission to manage application settings */
    public static final long MANAGE_SETTINGS = 1L << 20;

    /** Permission to manage add-on configurations */
    public static final long MANAGE_ADDONS = 1L << 21;

    // ==================== Transaction Control ====================

    /** Permission to approve pending transactions */
    public static final long APPROVE_TRANSACTIONS = 1L << 22;

    /** Permission to cancel existing transactions */
    public static final long CANCEL_TRANSACTIONS = 1L << 23;

    /** Permission to reprint receipts and documents */
    public static final long REPRINT = 1L << 24;

    // ==================== Aggregate Constants ====================

    /** Bitmask representing all available permissions (bits 0-24) */
    public static final long ALL_PERMISSIONS = (1L << 25) - 1;

    // ==================== Permission Check Methods ====================

    /**
     * Checks if the user's access rights include a specific permission.
     *
     * @param userRights the user's bitwise access rights value
     * @param permission the permission constant to check
     * @return true if the user has the specified permission
     */
    public static boolean hasPermission(long userRights, long permission) {
        return (userRights & permission) == permission;
    }

    /**
     * Checks if the user's access rights include at least one of the specified permissions.
     *
     * @param userRights  the user's bitwise access rights value
     * @param permissions one or more permission constants to check
     * @return true if the user has any of the specified permissions
     */
    public static boolean hasAnyPermission(long userRights, long... permissions) {
        for (long permission : permissions) {
            if ((userRights & permission) == permission) {
                return true;
            }
        }
        return false;
    }
}
