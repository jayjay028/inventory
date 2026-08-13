import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { layout: 'blank', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Categories
  {
    path: '/categories',
    name: 'CategoryList',
    component: () => import('@/views/categories/CategoryList.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/categories/new',
    name: 'CategoryCreate',
    component: () => import('@/views/categories/CategoryForm.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/categories/:id/edit',
    name: 'CategoryEdit',
    component: () => import('@/views/categories/CategoryForm.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Items
  {
    path: '/items',
    name: 'ItemList',
    component: () => import('@/views/items/ItemList.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/items/new',
    name: 'ItemCreate',
    component: () => import('@/views/items/ItemForm.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/items/:id/edit',
    name: 'ItemEdit',
    component: () => import('@/views/items/ItemForm.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Customers
  {
    path: '/customers',
    name: 'CustomerList',
    component: () => import('@/views/customers/CustomerList.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/customers/new',
    name: 'CustomerCreate',
    component: () => import('@/views/customers/CustomerForm.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/customers/:id/edit',
    name: 'CustomerEdit',
    component: () => import('@/views/customers/CustomerForm.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Suppliers
  {
    path: '/suppliers',
    name: 'SupplierList',
    component: () => import('@/views/suppliers/SupplierList.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/suppliers/new',
    name: 'SupplierCreate',
    component: () => import('@/views/suppliers/SupplierForm.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/suppliers/:id/edit',
    name: 'SupplierEdit',
    component: () => import('@/views/suppliers/SupplierForm.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Stock
  {
    path: '/stock',
    name: 'StockOverview',
    component: () => import('@/views/stock/StockOverview.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/stock/in',
    name: 'StockIn',
    component: () => import('@/views/stock/StockIn.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/stock/out',
    name: 'StockOut',
    component: () => import('@/views/stock/StockOut.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/stock/adjust',
    name: 'StockAdjust',
    component: () => import('@/views/stock/StockAdjust.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Transactions
  {
    path: '/transactions',
    name: 'TransactionList',
    component: () => import('@/views/transactions/TransactionList.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // POS
  {
    path: '/pos',
    name: 'PosTerminal',
    component: () => import('@/views/pos/PosTerminal.vue'),
    meta: { layout: 'blank', requiresAuth: true }
  },

  // Sales
  {
    path: '/sales',
    name: 'SalesList',
    component: () => import('@/views/sales/SalesList.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/sales/:id',
    name: 'SaleDetail',
    component: () => import('@/views/sales/SaleDetail.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Shifts
  {
    path: '/shifts',
    name: 'ShiftList',
    component: () => import('@/views/shifts/ShiftList.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Reports
  {
    path: '/reports',
    name: 'Reports',
    component: () => import('@/views/reports/Reports.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Users
  {
    path: '/users',
    name: 'UserList',
    component: () => import('@/views/users/UserList.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/users/new',
    name: 'UserCreate',
    component: () => import('@/views/users/UserForm.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },
  {
    path: '/users/:id/edit',
    name: 'UserEdit',
    component: () => import('@/views/users/UserForm.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Addons
  {
    path: '/addons',
    name: 'AddonList',
    component: () => import('@/views/addons/AddonList.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Settings
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/settings/Settings.vue'),
    meta: { layout: 'app', requiresAuth: true }
  },

  // Audit Trail
  {
    path: '/audit',
    name: 'AuditTrail',
    component: () => import('@/views/audit/AuditTrail.vue'),
    meta: { layout: 'app', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
