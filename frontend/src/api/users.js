import api from './axios'

export default {
  getAll(params) { return api.get('/users', { params }) },
  getById(id) { return api.get(`/users/${id}`) },
  create(data) { return api.post('/users', data) },
  update(id, data) { return api.put(`/users/${id}`, data) },
  updateStatus(id, active) { return api.patch(`/users/${id}/status`, null, { params: { active } }) },
  resetPassword(id, data) { return api.post(`/users/${id}/reset-password`, data) }
}
