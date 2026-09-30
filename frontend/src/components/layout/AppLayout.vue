<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': appStore.sidebarCollapsed }">
    <Sidebar />
    <div class="app-main">
      <Navbar />
      <main class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { useAppStore } from '@/stores/app'
import Navbar from './Navbar.vue'
import Sidebar from './Sidebar.vue'

const appStore = useAppStore()
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.app-main {
  flex: 1;
  margin-left: 250px;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s ease;
}

.app-layout.sidebar-collapsed .app-main {
  margin-left: 0;
}

.app-content {
  flex: 1;
  padding: 1.5rem;
}

@media (max-width: 991.98px) {
  .app-main {
    margin-left: 0;
  }
}
</style>
