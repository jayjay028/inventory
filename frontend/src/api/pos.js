import api from './axios'

export default {
  // Sales
  createSale(data) { return api.post('/pos/sales', data) },
  getSales(params) { return api.get('/pos/sales', { params }) },
  getSaleById(id) { return api.get(`/pos/sales/${id}`) },
  updateItems(id, items) { return api.put(`/pos/sales/${id}/items`, items) },
  processPayment(id, data) { return api.post(`/pos/sales/${id}/pay`, data) },
  closeSale(id) { return api.patch(`/pos/sales/${id}/close`) },
  voidSale(id, data) { return api.post(`/pos/sales/${id}/void`, data) },
  getReceipt(id) { return api.get(`/pos/sales/${id}/receipt`) },
  getTodaySales(params) { return api.get('/pos/sales/today', { params }) },
  getOpenSales() { return api.get('/pos/sales/open') },
  searchItems(q, params) { return api.get('/pos/items/search', { params: { q, ...params } }) },
  // Shifts
  openShift(data) { return api.post('/pos/shifts/open', data) },
  closeShift(id, data) { return api.post(`/pos/shifts/${id}/close`, data) },
  getCurrentShift() { return api.get('/pos/shifts/current') },
  getShifts(params) { return api.get('/pos/shifts', { params }) },
  getShiftById(id) { return api.get(`/pos/shifts/${id}`) }
}
