import api from './axios'

export default {
  getAll(params) { return api.get('/transactions', { params }) },
  getById(id) { return api.get(`/transactions/${id}`) },
  getPending(params) { return api.get('/transactions/pending', { params }) }
}
