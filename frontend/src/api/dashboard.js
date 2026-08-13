import api from './axios'

export default {
  get() { return api.get('/dashboard') }
}
