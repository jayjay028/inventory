import api from './axios'

export default {
  login(credentials) { return api.post('/auth/login', credentials) },
  refresh(refreshToken) { return api.post('/auth/refresh', { refreshToken }) },
  logout() { return api.post('/auth/logout') },
  me() { return api.get('/auth/me') }
}
