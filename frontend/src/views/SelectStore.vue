<template>
  <div class="select-store-page">
    <div class="select-store-card">
      <div class="select-store-header">
        <i class="bi bi-shop select-store-logo"></i>
        <h1 class="select-store-title">Select a Store</h1>
        <p class="select-store-subtitle">Choose the store you want to work in</p>
      </div>

      <div class="store-list">
        <button
          v-for="store in storeStore.accessibleStores"
          :key="store.id"
          class="store-card"
          type="button"
          @click="choose(store.id)"
        >
          <div class="store-card-icon">
            <i class="bi bi-shop"></i>
          </div>
          <div class="store-card-info">
            <span class="store-card-name">{{ store.name }}</span>
            <span class="store-card-code">{{ store.code }}</span>
          </div>
          <i class="bi bi-chevron-right store-card-arrow"></i>
        </button>
      </div>

      <div v-if="storeStore.accessibleStores.length === 0" class="no-stores">
        <i class="bi bi-exclamation-circle"></i>
        <p>You have no store access. Contact your administrator.</p>
      </div>

      <div class="select-store-footer">
        <button class="btn-logout" type="button" @click="authStore.logout()">
          <i class="bi bi-box-arrow-right"></i> Sign out
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStoreStore } from '@/stores/store'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const storeStore = useStoreStore()
const authStore = useAuthStore()

onMounted(async () => {
  // If stores aren't loaded (e.g., direct navigation/reload), fetch the user
  if (storeStore.accessibleStores.length === 0 && authStore.isAuthenticated) {
    try {
      await authStore.fetchUser()
    } catch (e) {
      // handled by interceptor
    }
  }
  // If only one store, auto-select and proceed
  if (storeStore.accessibleStores.length === 1) {
    storeStore.setCurrentStore(storeStore.accessibleStores[0].id)
    router.push('/dashboard')
  }
})

function choose(storeId) {
  storeStore.setCurrentStore(storeId)
  router.push('/dashboard')
}
</script>

<style scoped>
.select-store-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f4f6f9;
  padding: 1rem;
}

.select-store-card {
  width: 100%;
  max-width: 440px;
  background-color: #ffffff;
  border: 1px solid #e0e4e8;
  border-radius: 8px;
  padding: 2.5rem 2rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.select-store-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.select-store-logo {
  font-size: 2.5rem;
  color: #1e40af;
}

.select-store-title {
  font-size: 1.375rem;
  font-weight: 600;
  color: #111827;
  margin: 0.75rem 0 0.25rem;
}

.select-store-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.store-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.store-card {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  width: 100%;
  padding: 0.875rem 1rem;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease;
  text-align: left;
}

.store-card:hover {
  border-color: #1e40af;
  background-color: #f8faff;
}

.store-card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 8px;
  background-color: #eff6ff;
  color: #1e40af;
  font-size: 1.1rem;
  flex-shrink: 0;
}

.store-card-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.store-card-name {
  font-size: 0.9375rem;
  font-weight: 500;
  color: #111827;
}

.store-card-code {
  font-size: 0.75rem;
  color: #9ca3af;
}

.store-card-arrow {
  color: #d1d5db;
  font-size: 0.875rem;
}

.no-stores {
  text-align: center;
  color: #6b7280;
  padding: 1.5rem 0;
}

.no-stores i {
  font-size: 1.5rem;
  color: #d97706;
}

.select-store-footer {
  margin-top: 1.5rem;
  text-align: center;
}

.btn-logout {
  border: none;
  background: none;
  color: #6b7280;
  font-size: 0.8125rem;
  cursor: pointer;
}

.btn-logout:hover {
  color: #dc2626;
}
</style>
