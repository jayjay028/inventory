import api from './axios'

export default {
  getAll(params) { return api.get('/items', { params }) },
  getById(id) { return api.get(`/items/${id}`) },
  create(data) { return api.post('/items', data) },
  update(id, data) { return api.put(`/items/${id}`, data) },
  updateStatus(id, active) { return api.patch(`/items/${id}/status`, null, { params: { active } }) },
  search(q, params) { return api.get('/items/search', { params: { q, ...params } }) },
  getLowStock(params) { return api.get('/items/low-stock', { params }) }
}
