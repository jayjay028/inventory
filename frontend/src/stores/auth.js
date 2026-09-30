import { defineStore } from 'pinia'
import authApi from '@/api/auth'
import { PERMISSIONS } from '@/constants/permissions'

// Re-export so existing imports `{ PERMISSIONS } from '@/stores/auth'` keep working.
export { PERMISSIONS }

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
    },

    /** Route path for the user's inferred landing page (POS or Dashboard). */
    landingPath: (state) => (state.user?.landingPage === 'POS' ? '/pos' : '/dashboard')
  },

  actions: {
    async login(credentials) {
      this.loading = true
      try {
        const response = await authApi.login(credentials)
        const { accessToken, refreshToken, user } = response.data.data

        this.setTokens(accessToken, refreshToken)
        this.user = user

        // Populate accessible stores from login response
        const { useStoreStore } = await import('@/stores/store')
        const storeStore = useStoreStore()
        storeStore.setAccessibleStores(user.stores || [])

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

      // Clear store selection on logout
      import('@/stores/store').then(({ useStoreStore }) => {
        useStoreStore().clearStore()
      })

      // Full navigation to login (avoids importing the router here, which
      // would create a circular dependency router <-> auth store).
      if (window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
    },

    async refreshTokenAction() {
      try {
        const response = await authApi.refresh(this.refreshToken)
        const { accessToken, refreshToken } = response.data.data

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
        this.user = response.data.data

        // Repopulate accessible stores on page reload
        const { useStoreStore } = await import('@/stores/store')
        useStoreStore().setAccessibleStores(this.user.stores || [])

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
