import api from './axios'

const blobConfig = { responseType: 'blob' }

export default {
  inventory(params) { return api.get('/reports/inventory', { params, ...blobConfig }) },
  stockMovement(params) { return api.get('/reports/stock-movement', { params, ...blobConfig }) },
  lowStock(params) { return api.get('/reports/low-stock', { params, ...blobConfig }) },
  sales(params) { return api.get('/reports/sales', { params, ...blobConfig }) },
  salesSummary(params) { return api.get('/reports/sales-summary', { params, ...blobConfig }) },
  purchaseHistory(params) { return api.get('/reports/purchase-history', { params, ...blobConfig }) },
  customerStatement(customerId, params) { return api.get(`/reports/customers/${customerId}/statement`, { params, ...blobConfig }) },
  supplierStatement(supplierId, params) { return api.get(`/reports/suppliers/${supplierId}/statement`, { params, ...blobConfig }) },
  auditTrail(params) { return api.get('/reports/audit-trail', { params, ...blobConfig }) }
}
