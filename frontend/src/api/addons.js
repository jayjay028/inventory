import api from './axios'

export default {
  getAll(params) { return api.get('/addons', { params }) },
  getById(id) { return api.get(`/addons/${id}`) },
  create(data) { return api.post('/addons', data) },
  update(id, data) { return api.put(`/addons/${id}`, data) },
  updateStatus(id, active) { return api.patch(`/addons/${id}/status`, null, { params: { active } }) }
}
