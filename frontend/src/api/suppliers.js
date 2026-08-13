import api from './axios'

export default {
  getAll(params) { return api.get('/suppliers', { params }) },
  getById(id) { return api.get(`/suppliers/${id}`) },
  create(data) { return api.post('/suppliers', data) },
  update(id, data) { return api.put(`/suppliers/${id}`, data) },
  updateStatus(id, active) { return api.patch(`/suppliers/${id}/status`, null, { params: { active } }) },
  search(q, params) { return api.get('/suppliers/search', { params: { q, ...params } }) }
}
