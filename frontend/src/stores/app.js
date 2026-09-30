import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    loading: false,
    // Theme: 'dark' (default) or 'light', persisted to localStorage.
    theme: localStorage.getItem('theme') || 'dark',
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

    /** Apply the current theme to the <html> element and persist it. */
    applyTheme() {
      document.documentElement.setAttribute('data-theme', this.theme)
      localStorage.setItem('theme', this.theme)
    },

    /** Switch between light and dark modes. */
    toggleTheme() {
      this.theme = this.theme === 'dark' ? 'light' : 'dark'
      this.applyTheme()
    },

    setTheme(theme) {
      this.theme = theme === 'light' ? 'light' : 'dark'
      this.applyTheme()
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
