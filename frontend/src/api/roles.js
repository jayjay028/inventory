import api from './axios'

export default {
  getActive() { return api.get('/roles/active') },
  getAll(params) { return api.get('/roles', { params }) },
  getById(id) { return api.get(`/roles/${id}`) },
  create(data) { return api.post('/roles', data) },
  update(id, data) { return api.put(`/roles/${id}`, data) },
  updateStatus(id, active) { return api.patch(`/roles/${id}/status`, null, { params: { active } }) },
  delete(id) { return api.delete(`/roles/${id}`) }
}
