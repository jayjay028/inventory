/**
 * Permission bit positions (0–24). The user's effective permissions are a
 * bitmask derived from their assigned role; each bit here maps to one feature.
 *
 * Kept in a dependency-free module so both the router and the auth store can
 * import it without creating a circular dependency.
 */
export const PERMISSIONS = {
  VIEW_DASHBOARD: 0,
  VIEW_ITEMS: 1,
  MANAGE_ITEMS: 2,
  VIEW_CATEGORIES: 3,
  MANAGE_CATEGORIES: 4,
  VIEW_CUSTOMERS: 5,
  MANAGE_CUSTOMERS: 6,
  VIEW_SUPPLIERS: 7,
  MANAGE_SUPPLIERS: 8,
  VIEW_STOCK: 9,
  MANAGE_STOCK_IN: 10,
  MANAGE_STOCK_OUT: 11,
  MANAGE_STOCK_ADJ: 12,
  VIEW_TRANSACTIONS: 13,
  USE_POS: 14,
  VOID_SALES: 15,
  MANAGE_SHIFTS: 16,
  VIEW_REPORTS: 17,
  VIEW_AUDIT_TRAIL: 18,
  MANAGE_USERS: 19,
  MANAGE_SETTINGS: 20,
  MANAGE_ADDONS: 21,
  APPROVE_TRANSACTIONS: 22,
  CANCEL_TRANSACTIONS: 23,
  REPRINT: 24
}
