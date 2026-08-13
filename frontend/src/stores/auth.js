import { defineStore } from 'pinia'
import authApi from '@/api/auth'
import router from '@/router'

export const PERMISSIONS = {
  VIEW_DASHBOARD: 0,
  VIEW_ITEMS: 1,
  MANAGE_ITEMS: 2,
  VIEW_CATEGORIES: 3,
  MANAGE_CATEGORIES: 4,
  VIEW_CUSTOMERS: 5,
  MANAGE_CUSTOMERS: 6,
  VIEW_SUPPLIERS: 7,
  MANAGE_SUPPLIERS: 8,
  VIEW_STOCK: 9,
  MANAGE_STOCK_IN: 10,
  MANAGE_STOCK_OUT: 11,
  MANAGE_STOCK_ADJ: 12,
  VIEW_TRANSACTIONS: 13,
  USE_POS: 14,
  VOID_SALES: 15,
  MANAGE_SHIFTS: 16,
  VIEW_REPORTS: 17,
  VIEW_AUDIT_TRAIL: 18,
  MANAGE_USERS: 19,
  MANAGE_SETTINGS: 20,
  MANAGE_ADDONS: 21,
  APPROVE_TRANSACTIONS: 22,
  CANCEL_TRANSACTIONS: 23,
  REPRINT: 24
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    accessToken: localStorage.getItem('accessToken'),
    refreshToken: localStorage.getItem('refreshToken'),
    loading: false
  }),

  getters: {
    isAuthenticated: (state) => !!state.accessToken,

    userRole: (state) => state.user?.role || null,

    userName: (state) => state.user?.name || '',

    accessRights: (state) => state.user?.accessRights || 0,

    hasPermission: (state) => {
      return (bit) => (state.user?.accessRights & (1 << bit)) !== 0
    }
  },

  actions: {
    async login(credentials) {
      this.loading = true
      try {
        const response = await authApi.login(credentials)
        const { accessToken, refreshToken, user } = response.data

        this.setTokens(accessToken, refreshToken)
        this.user = user

        return response
      } finally {
        this.loading = false
      }
    },

    logout() {
      this.user = null
      this.accessToken = null
      this.refreshToken = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      router.push('/login')
    },

    async refreshTokenAction() {
      try {
        const response = await authApi.refresh(this.refreshToken)
        const { accessToken, refreshToken } = response.data

        this.setTokens(accessToken, refreshToken)

        return response
      } catch (error) {
        this.logout()
        throw error
      }
    },

    async fetchUser() {
      this.loading = true
      try {
        const response = await authApi.me()
        this.user = response.data

        return response
      } finally {
        this.loading = false
      }
    },

    setTokens(accessToken, refreshToken) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
    }
  }
})
