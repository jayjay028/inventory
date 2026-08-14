<template>
  <div class="data-table">
    <!-- Search -->
    <div v-if="searchable" class="dt-toolbar">
      <div class="dt-search">
        <i class="bi bi-search dt-search-icon"></i>
        <input
          type="text"
          class="dt-search-input"
          :placeholder="searchPlaceholder"
          :value="searchQuery"
          @input="handleSearch($event.target.value)"
        />
        <button
          v-if="searchQuery"
          class="dt-search-clear"
          type="button"
          @click="handleSearch('')"
        >
          <i class="bi bi-x"></i>
        </button>
      </div>
      <slot name="toolbar"></slot>
    </div>

    <!-- Table -->
    <div class="dt-table-wrap">
      <table class="dt-table">
        <thead>
          <tr>
            <th
              v-for="col in columns"
              :key="col.key"
              :style="col.width ? { width: col.width } : {}"
              :class="{ 'dt-sortable': col.sortable }"
              @click="col.sortable && handleSort(col.key)"
            >
              <div class="dt-th-content">
                <span>{{ col.label }}</span>
                <span v-if="col.sortable" class="dt-sort-icon">
                  <i
                    v-if="sortKey === col.key && sortOrder === 'asc'"
                    class="bi bi-caret-up-fill dt-sort-active"
                  ></i>
                  <i
                    v-else-if="sortKey === col.key && sortOrder === 'desc'"
                    class="bi bi-caret-down-fill dt-sort-active"
                  ></i>
                  <i v-else class="bi bi-chevron-expand dt-sort-idle"></i>
                </span>
              </div>
            </th>
            <th v-if="$slots.actions" style="width: 120px;">Actions</th>
          </tr>
        </thead>
        <tbody>
          <!-- Loading skeleton -->
          <template v-if="loading">
            <tr v-for="n in pageSize" :key="'skeleton-' + n">
              <td v-for="col in columns" :key="col.key">
                <div class="dt-skeleton"></div>
              </td>
              <td v-if="$slots.actions">
                <div class="dt-skeleton" style="width: 60px;"></div>
              </td>
            </tr>
          </template>

          <!-- Data rows -->
          <template v-else-if="data.length > 0">
            <tr
              v-for="(row, index) in data"
              :key="index"
              :class="{ 'dt-row-clickable': hasRowClick }"
              @click="handleRowClick(row)"
            >
              <td v-for="col in columns" :key="col.key">
                <slot :name="'cell-' + col.key" :row="row" :value="row[col.key]">
                  {{ row[col.key] }}
                </slot>
              </td>
              <td v-if="$slots.actions">
                <slot name="actions" :row="row"></slot>
              </td>
            </tr>
          </template>

          <!-- Empty state -->
          <tr v-else>
            <td :colspan="columns.length + ($slots.actions ? 1 : 0)">
              <div class="dt-empty">
                <i class="bi bi-inbox"></i>
                <p>No records found</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="dt-pagination">
      <span class="dt-page-info">
        Page {{ currentPage }} of {{ totalPages }}
      </span>
      <nav aria-label="Table pagination">
        <ul class="dt-page-list">
          <li :class="{ disabled: currentPage <= 1 }">
            <button class="dt-page-btn" type="button" @click="goToPage(currentPage - 1)">
              <i class="bi bi-chevron-left"></i>
            </button>
          </li>
          <li
            v-for="page in visiblePages"
            :key="page"
            :class="{ active: page === currentPage }"
          >
            <button class="dt-page-btn" type="button" @click="goToPage(page)">
              {{ page }}
            </button>
          </li>
          <li :class="{ disabled: currentPage >= totalPages }">
            <button class="dt-page-btn" type="button" @click="goToPage(currentPage + 1)">
              <i class="bi bi-chevron-right"></i>
            </button>
          </li>
        </ul>
      </nav>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, useSlots } from 'vue'

const props = defineProps({
  columns: {
    type: Array,
    required: true
  },
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  totalPages: {
    type: Number,
    default: 1
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  searchable: {
    type: Boolean,
    default: false
  },
  searchPlaceholder: {
    type: String,
    default: 'Search...'
  }
})

const emit = defineEmits(['page-change', 'sort-change', 'search', 'row-click'])
const slots = useSlots()

const searchQuery = ref('')
const sortKey = ref('')
const sortOrder = ref('')

const hasRowClick = computed(() => !!slots['row-click'] || true)

const visiblePages = computed(() => {
  const pages = []
  const total = props.totalPages
  const current = props.currentPage
  const maxVisible = 5

  let start = Math.max(1, current - Math.floor(maxVisible / 2))
  let end = Math.min(total, start + maxVisible - 1)

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

let searchTimeout = null

function handleSearch(value) {
  searchQuery.value = value
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    emit('search', value)
  }, 300)
}

function handleSort(key) {
  if (sortKey.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortOrder.value = 'asc'
  }
  emit('sort-change', { key: sortKey.value, order: sortOrder.value })
}

function goToPage(page) {
  if (page >= 1 && page <= props.totalPages) {
    emit('page-change', page)
  }
}

function handleRowClick(row) {
  emit('row-click', row)
}
</script>

<style scoped>
.data-table {
  width: 100%;
}

/* Toolbar */
.dt-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
  gap: 0.75rem;
}

/* Search */
.dt-search {
  position: relative;
  max-width: 280px;
  width: 100%;
}

.dt-search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.8rem;
  color: #94a3b8;
  pointer-events: none;
}

.dt-search-input {
  width: 100%;
  height: 36px;
  padding: 0 2rem 0 2.25rem;
  font-size: 0.8125rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #fff;
  color: #1e293b;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.dt-search-input::placeholder {
  color: #94a3b8;
}

.dt-search-input:focus {
  border-color: #1e40af;
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.08);
}

.dt-search-clear {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 0.125rem;
  font-size: 1rem;
  line-height: 1;
  display: flex;
  align-items: center;
}

.dt-search-clear:hover {
  color: #64748b;
}

/* Table */
.dt-table-wrap {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.dt-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.8125rem;
}

.dt-table thead {
  background-color: #f8fafc;
}

.dt-table thead th {
  padding: 0.625rem 1rem;
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
  border-bottom: 1px solid #e5e7eb;
  white-space: nowrap;
  text-align: left;
}

.dt-th-content {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
}

.dt-sortable {
  cursor: pointer;
  user-select: none;
  transition: background-color 0.15s ease;
}

.dt-sortable:hover {
  background-color: #f1f5f9;
}

.dt-sort-icon {
  font-size: 0.625rem;
  display: inline-flex;
}

.dt-sort-active {
  color: #1e40af;
}

.dt-sort-idle {
  color: #cbd5e1;
  font-size: 0.5625rem;
}

.dt-table tbody td {
  padding: 0.75rem 1rem;
  color: #334155;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.dt-table tbody tr:last-child td {
  border-bottom: none;
}

.dt-table tbody tr:hover {
  background-color: #f8fafc;
}

.dt-row-clickable {
  cursor: pointer;
}

/* Empty state */
.dt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  color: #94a3b8;
}

.dt-empty i {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
  opacity: 0.5;
}

.dt-empty p {
  margin: 0;
  font-size: 0.8125rem;
  font-weight: 500;
}

/* Skeleton */
.dt-skeleton {
  height: 0.875rem;
  background: linear-gradient(90deg, #f1f5f9 25%, #e8ecf1 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: dt-shimmer 1.8s ease-in-out infinite;
  border-radius: 4px;
  width: 75%;
}

@keyframes dt-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* Pagination */
.dt-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.875rem;
  padding-top: 0.75rem;
}

.dt-page-info {
  font-size: 0.75rem;
  color: #64748b;
}

.dt-page-list {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.dt-page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 0.375rem;
  font-size: 0.75rem;
  font-weight: 500;
  color: #475569;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.dt-page-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.dt-page-list li.active .dt-page-btn {
  background: #1e293b;
  color: #fff;
  border-color: #1e293b;
}

.dt-page-list li.disabled .dt-page-btn {
  color: #cbd5e1;
  pointer-events: none;
  cursor: default;
}
</style>
