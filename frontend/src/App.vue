<template>
  <!-- App layout (sidebar + navbar + right sidebar) only for authenticated app pages.
       AppLayout contains its own <router-view>, so it renders the active page. -->
  <AppLayout v-if="useAppLayout" />

  <!-- Blank layout for login, POS terminal, store selection, and any
       not-yet-resolved / unauthenticated state. -->
  <router-view v-else />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/components/layout/AppLayout.vue'

const route = useRoute()
const authStore = useAuthStore()

// Only use the full app chrome when the route explicitly opts into the 'app'
// layout AND the user is authenticated. This prevents the navbar/sidebar (which
// call authenticated APIs) from mounting on the login page or before the
// router has resolved its first navigation.
const useAppLayout = computed(
  () => route.meta.layout === 'app' && authStore.isAuthenticated
)
</script>
