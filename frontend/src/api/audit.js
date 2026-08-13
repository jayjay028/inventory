import api from './axios'

export default {
  getAll(params) { return api.get('/audit', { params }) },
  getByEntity(entityType, entityId, params) { return api.get(`/audit/${entityType}/${entityId}`, { params }) }
}
