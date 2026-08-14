<template>
  <div class="login-page d-flex align-items-center justify-content-center min-vh-100">
    <div class="login-card">
      <!-- Logo Area -->
      <div class="text-center mb-4">
        <div class="logo-icon mb-3">
          <i class="bi bi-box-seam-fill"></i>
        </div>
        <h4 class="brand-title">Inventory + POS</h4>
        <p class="brand-subtitle">Sign in to your account</p>
      </div>

      <!-- Error Message -->
      <div v-if="errorMessage" class="alert alert-danger d-flex align-items-center" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>
        <span>{{ errorMessage }}</span>
      </div>

      <!-- Login Form -->
      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label for="username" class="form-label">Username</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            class="form-control login-input"
            placeholder="Enter your username"
            required
            :disabled="loading"
            autocomplete="username"
          />
        </div>

        <div class="mb-4">
          <label for="password" class="form-label">Password</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="form-control login-input"
            placeholder="Enter your password"
            required
            :disabled="loading"
            autocomplete="current-password"
          />
        </div>

        <button type="submit" class="btn btn-login w-100" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
          {{ loading ? 'Signing in...' : 'Sign In' }}
        </button>
      </form>

      <!-- Footer -->
      <div class="login-footer mt-4">
        <small>&copy; 2026 Powered by Joven Q. Divinagracia Jr.</small>
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
  background-color: #f4f6f9;
  min-height: 100vh;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background-color: #ffffff;
  border: 1px solid #e0e4e8;
  border-radius: 8px;
  padding: 2.5rem 2rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.logo-icon {
  font-size: 3rem;
  color: #1b2a4a;
  line-height: 1;
}

.brand-title {
  font-weight: 700;
  color: #1b2a4a;
  margin-bottom: 0.25rem;
  font-size: 1.35rem;
  letter-spacing: -0.02em;
}

.brand-subtitle {
  color: #6c757d;
  font-size: 0.9rem;
  margin-bottom: 0;
}

.form-label {
  font-weight: 500;
  font-size: 0.85rem;
  color: #344054;
  margin-bottom: 0.375rem;
}

.login-input {
  border: 1px solid #d0d5dd;
  border-radius: 6px;
  padding: 0.6rem 0.85rem;
  font-size: 0.9rem;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.login-input:focus {
  border-color: #1b2a4a;
  box-shadow: 0 0 0 3px rgba(27, 42, 74, 0.08);
}

.login-input::placeholder {
  color: #98a2b3;
}

.btn-login {
  background-color: #1b2a4a;
  border-color: #1b2a4a;
  color: #ffffff;
  font-weight: 600;
  font-size: 0.9rem;
  padding: 0.65rem 1rem;
  border-radius: 6px;
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

.btn-login:hover:not(:disabled) {
  background-color: #152238;
  border-color: #152238;
  color: #ffffff;
}

.btn-login:focus {
  box-shadow: 0 0 0 3px rgba(27, 42, 74, 0.15);
}

.btn-login:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid #f0f2f5;
  color: #98a2b3;
  font-size: 0.8rem;
}

.alert-danger {
  font-size: 0.85rem;
  border-radius: 6px;
}
</style>
