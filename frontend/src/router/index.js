import { createRouter, createWebHistory } from 'vue-router'
import { PERMISSIONS } from '@/constants/permissions'
import { useAuthStore } from '@/stores/auth'
import { useStoreStore } from '@/stores/store'

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
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.VIEW_CATEGORIES }
  },
  {
    path: '/categories/new',
    name: 'CategoryCreate',
    component: () => import('@/views/categories/CategoryForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_CATEGORIES }
  },
  {
    path: '/categories/:id/edit',
    name: 'CategoryEdit',
    component: () => import('@/views/categories/CategoryForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_CATEGORIES }
  },

  // Items
  {
    path: '/items',
    name: 'ItemList',
    component: () => import('@/views/items/ItemList.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.VIEW_ITEMS }
  },
  {
    path: '/items/new',
    name: 'ItemCreate',
    component: () => import('@/views/items/ItemForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_ITEMS }
  },
  {
    path: '/items/:id/edit',
    name: 'ItemEdit',
    component: () => import('@/views/items/ItemForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_ITEMS }
  },

  // Customers
  {
    path: '/customers',
    name: 'CustomerList',
    component: () => import('@/views/customers/CustomerList.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.VIEW_CUSTOMERS }
  },
  {
    path: '/customers/new',
    name: 'CustomerCreate',
    component: () => import('@/views/customers/CustomerForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_CUSTOMERS }
  },
  {
    path: '/customers/:id/edit',
    name: 'CustomerEdit',
    component: () => import('@/views/customers/CustomerForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_CUSTOMERS }
  },

  // Suppliers
  {
    path: '/suppliers',
    name: 'SupplierList',
    component: () => import('@/views/suppliers/SupplierList.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.VIEW_SUPPLIERS }
  },
  {
    path: '/suppliers/new',
    name: 'SupplierCreate',
    component: () => import('@/views/suppliers/SupplierForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_SUPPLIERS }
  },
  {
    path: '/suppliers/:id/edit',
    name: 'SupplierEdit',
    component: () => import('@/views/suppliers/SupplierForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_SUPPLIERS }
  },

  // Stock
  {
    path: '/stock',
    name: 'StockOverview',
    component: () => import('@/views/stock/StockOverview.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.VIEW_STOCK }
  },
  {
    path: '/stock/in',
    name: 'StockIn',
    component: () => import('@/views/stock/StockIn.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_STOCK_IN }
  },
  {
    path: '/stock/out',
    name: 'StockOut',
    component: () => import('@/views/stock/StockOut.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_STOCK_OUT }
  },
  {
    path: '/stock/adjust',
    name: 'StockAdjust',
    component: () => import('@/views/stock/StockAdjust.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_STOCK_ADJ }
  },

  // Transactions
  {
    path: '/transactions',
    name: 'TransactionList',
    component: () => import('@/views/transactions/TransactionList.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.VIEW_TRANSACTIONS }
  },

  // POS
  {
    path: '/pos',
    name: 'PosTerminal',
    component: () => import('@/views/pos/PosTerminal.vue'),
    meta: { layout: 'blank', requiresAuth: true, permission: PERMISSIONS.USE_POS }
  },

  // Sales
  {
    path: '/sales',
    name: 'SalesList',
    component: () => import('@/views/sales/SalesList.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.USE_POS }
  },
  {
    path: '/sales/:id',
    name: 'SaleDetail',
    component: () => import('@/views/sales/SaleDetail.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.USE_POS }
  },

  // Shifts
  {
    path: '/shifts',
    name: 'ShiftList',
    component: () => import('@/views/shifts/ShiftList.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_SHIFTS }
  },

  // Reports
  {
    path: '/reports',
    name: 'Reports',
    component: () => import('@/views/reports/Reports.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.VIEW_REPORTS }
  },

  // Users
  {
    path: '/users',
    name: 'UserList',
    component: () => import('@/views/users/UserList.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_USERS }
  },
  {
    path: '/users/new',
    name: 'UserCreate',
    component: () => import('@/views/users/UserForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_USERS }
  },
  {
    path: '/users/:id/edit',
    name: 'UserEdit',
    component: () => import('@/views/users/UserForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_USERS }
  },

  // Addons
  {
    path: '/addons',
    name: 'AddonList',
    component: () => import('@/views/addons/AddonList.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_ADDONS }
  },

  // Settings
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/settings/Settings.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_SETTINGS }
  },

  // Audit Trail
  {
    path: '/audit',
    name: 'AuditTrail',
    component: () => import('@/views/audit/AuditTrail.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.VIEW_AUDIT_TRAIL }
  },

  // Store selection (no layout, requires auth but no permission)
  {
    path: '/select-store',
    name: 'SelectStore',
    component: () => import('@/views/SelectStore.vue'),
    meta: { layout: 'blank', requiresAuth: true }
  },

  // Stores management (admin)
  {
    path: '/stores',
    name: 'StoreList',
    component: () => import('@/views/stores/StoreList.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_SETTINGS }
  },
  {
    path: '/stores/new',
    name: 'StoreCreate',
    component: () => import('@/views/stores/StoreForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_SETTINGS }
  },
  {
    path: '/stores/:id/edit',
    name: 'StoreEdit',
    component: () => import('@/views/stores/StoreForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_SETTINGS }
  },

  // Roles management (admin)
  {
    path: '/roles',
    name: 'RoleList',
    component: () => import('@/views/roles/RoleList.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_USERS }
  },
  {
    path: '/roles/new',
    name: 'RoleCreate',
    component: () => import('@/views/roles/RoleForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_USERS }
  },
  {
    path: '/roles/:id/edit',
    name: 'RoleEdit',
    component: () => import('@/views/roles/RoleForm.vue'),
    meta: { layout: 'app', requiresAuth: true, permission: PERMISSIONS.MANAGE_USERS }
  },

  // Catch-all 404
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  const requiresAuth = to.meta.requiresAuth !== false

  // Redirect unauthenticated users to login
  if (requiresAuth && !authStore.isAuthenticated) {
    next('/login')
    return
  }

  // Redirect authenticated users away from login
  if (to.path === '/login' && authStore.isAuthenticated) {
    next('/dashboard')
    return
  }

  // Permission check for authenticated routes
  if (requiresAuth && authStore.isAuthenticated && to.meta.permission != null) {
    // Ensure user data is loaded (needed for permission bitmask)
    if (!authStore.user) {
      try {
        await authStore.fetchUser()
      } catch {
        // If fetching user fails, force logout
        authStore.logout()
        next('/login')
        return
      }
    }

    // Check if user has the required permission bit
    if (!authStore.hasPermission(to.meta.permission)) {
      next('/dashboard')
      return
    }
  }

  // Store-selection guard: authenticated users must have a store selected
  // before accessing app pages (except the select-store page itself).
  if (requiresAuth && authStore.isAuthenticated && to.path !== '/select-store') {
    const storeStore = useStoreStore()

    // Ensure stores are loaded (page reload case)
    if (storeStore.accessibleStores.length === 0 && authStore.user?.stores == null) {
      try {
        await authStore.fetchUser()
      } catch {
        authStore.logout()
        next('/login')
        return
      }
    }

    // If no store selected and the user has multiple, force selection
    if (!storeStore.hasStore && storeStore.hasMultipleStores) {
      next('/select-store')
      return
    }
  }

  next()
})

export default router
