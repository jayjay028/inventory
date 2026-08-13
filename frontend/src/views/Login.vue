<template>
  <div class="login-page d-flex align-items-center justify-content-center min-vh-100 bg-light">
    <div class="card shadow-sm" style="width: 100%; max-width: 400px;">
      <div class="card-body p-4">
        <div class="text-center mb-4">
          <h3 class="fw-bold text-primary">Inventory System</h3>
          <p class="text-muted">Sign in to your account</p>
        </div>

        <div v-if="errorMessage" class="alert alert-danger d-flex align-items-center" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>
          <span>{{ errorMessage }}</span>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <label for="username" class="form-label">Username</label>
            <div class="input-group">
              <span class="input-group-text"><i class="bi bi-person"></i></span>
              <input
                id="username"
                v-model="form.username"
                type="text"
                class="form-control"
                placeholder="Enter username"
                required
                :disabled="loading"
                autocomplete="username"
              />
            </div>
          </div>

          <div class="mb-4">
            <label for="password" class="form-label">Password</label>
            <div class="input-group">
              <span class="input-group-text"><i class="bi bi-lock"></i></span>
              <input
                id="password"
                v-model="form.password"
                type="password"
                class="form-control"
                placeholder="Enter password"
                required
                :disabled="loading"
                autocomplete="current-password"
              />
            </div>
          </div>

          <button type="submit" class="btn btn-primary w-100" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            {{ loading ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: ''
})
const loading = ref(false)
const errorMessage = ref('')

async function handleLogin() {
  errorMessage.value = ''
  loading.value = true

  try {
    await authStore.login(form)
    router.push('/dashboard')
  } catch (error) {
    const msg = error.response?.data?.message
    errorMessage.value = msg || 'Invalid username or password. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}
</style>
