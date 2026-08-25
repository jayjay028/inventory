import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    loading: false,
    toast: {
      show: false,
      message: '',
      type: 'success'
    },
    _toastTimeoutId: null
  }),

  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    showToast(message, type = 'success', duration = 3000) {
      // Clear any existing timeout to prevent premature hiding
      if (this._toastTimeoutId) {
        clearTimeout(this._toastTimeoutId)
        this._toastTimeoutId = null
      }

      this.toast = { show: true, message, type }

      this._toastTimeoutId = setTimeout(() => {
        this.hideToast()
        this._toastTimeoutId = null
      }, duration)
    },

    hideToast() {
      if (this._toastTimeoutId) {
        clearTimeout(this._toastTimeoutId)
        this._toastTimeoutId = null
      }
      this.toast = { show: false, message: '', type: 'success' }
    },

    setLoading(val) {
      this.loading = val
    }
  }
})
