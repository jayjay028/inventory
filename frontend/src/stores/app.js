import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    loading: false,
    toast: {
      show: false,
      message: '',
      type: 'success'
    }
  }),

  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    showToast(message, type = 'success', duration = 3000) {
      this.toast = { show: true, message, type }

      setTimeout(() => {
        this.hideToast()
      }, duration)
    },

    hideToast() {
      this.toast = { show: false, message: '', type: 'success' }
    },

    setLoading(val) {
      this.loading = val
    }
  }
})
