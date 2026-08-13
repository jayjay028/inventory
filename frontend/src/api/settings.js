import api from './axios'

export default {
  getAll() { return api.get('/settings') },
  update(data) { return api.put('/settings', data) }
}
