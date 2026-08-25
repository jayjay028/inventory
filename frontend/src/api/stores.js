import api from './axios'

export default {
  getActive() { return api.get('/stores/active') },
  getAll(params) { return api.get('/stores', { params }) },
  getById(id) { return api.get(`/stores/${id}`) },
  create(data) { return api.post('/stores', data) },
  update(id, data) { return api.put(`/stores/${id}`, data) },
  updateStatus(id, active) { return api.patch(`/stores/${id}/status`, null, { params: { active } }) }
}
