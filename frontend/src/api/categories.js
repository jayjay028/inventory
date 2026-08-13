import api from './axios'

export default {
  getAll(params) { return api.get('/categories', { params }) },
  getById(id) { return api.get(`/categories/${id}`) },
  create(data) { return api.post('/categories', data) },
  update(id, data) { return api.put(`/categories/${id}`, data) },
  updateStatus(id, active) { return api.patch(`/categories/${id}/status`, null, { params: { active } }) },
  search(q, params) { return api.get('/categories/search', { params: { q, ...params } }) }
}
