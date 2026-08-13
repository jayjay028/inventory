import api from './axios'

export default {
  getAll(params) { return api.get('/customers', { params }) },
  getById(id) { return api.get(`/customers/${id}`) },
  create(data) { return api.post('/customers', data) },
  update(id, data) { return api.put(`/customers/${id}`, data) },
  updateStatus(id, active) { return api.patch(`/customers/${id}/status`, null, { params: { active } }) },
  search(q, params) { return api.get('/customers/search', { params: { q, ...params } }) }
}
