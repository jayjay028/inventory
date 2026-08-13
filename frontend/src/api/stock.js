import api from './axios'

export default {
  getAll(params) { return api.get('/stock', { params }) },
  getByItemId(itemId) { return api.get(`/stock/${itemId}`) },
  stockIn(data) { return api.post('/stock/in', data) },
  stockOut(data) { return api.post('/stock/out', data) },
  stockAdjust(data) { return api.post('/stock/adjust', data) },
  approve(id) { return api.patch(`/stock/transactions/${id}/approve`) },
  cancel(id) { return api.patch(`/stock/transactions/${id}/cancel`) }
}
