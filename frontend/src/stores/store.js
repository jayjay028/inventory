import { defineStore } from 'pinia'

/**
 * Pinia store holding the user's accessible stores and the currently
 * selected store. The current store ID is persisted to localStorage and
 * sent with every API request via the X-Store-Id header (see api/axios.js).
 */
export const useStoreStore = defineStore('store', {
  state: () => ({
    accessibleStores: [],
    currentStoreId: localStorage.getItem('currentStoreId')
      ? Number(localStorage.getItem('currentStoreId'))
      : null
  }),

  getters: {
    currentStore: (state) =>
      state.accessibleStores.find((s) => s.id === state.currentStoreId) || null,

    hasMultipleStores: (state) => state.accessibleStores.length > 1,

    hasStore: (state) => state.currentStoreId != null
  },

  actions: {
    /**
     * Sets the list of stores the user can access (from login response),
     * and auto-selects a store if none is selected or the selected one
     * is no longer accessible.
     */
    setAccessibleStores(stores) {
      this.accessibleStores = stores || []

      const stillValid = this.accessibleStores.some((s) => s.id === this.currentStoreId)
      if (!stillValid) {
        // Auto-select the first store if only one, else leave null for selection
        if (this.accessibleStores.length === 1) {
          this.setCurrentStore(this.accessibleStores[0].id)
        } else if (this.accessibleStores.length === 0) {
          this.clearStore()
        }
      }
    },

    setCurrentStore(storeId) {
      this.currentStoreId = storeId
      localStorage.setItem('currentStoreId', String(storeId))
    },

    clearStore() {
      this.accessibleStores = []
      this.currentStoreId = null
      localStorage.removeItem('currentStoreId')
    }
  }
})
