<template>
  <div class="data-table">
    <!-- Search -->
    <div v-if="searchable" class="d-flex justify-content-between align-items-center mb-3">
      <div class="input-group" style="max-width: 320px;">
        <span class="input-group-text bg-white">
          <i class="bi bi-search text-muted"></i>
        </span>
        <input
          type="text"
          class="form-control border-start-0"
          :placeholder="searchPlaceholder"
          :value="searchQuery"
          @input="handleSearch($event.target.value)"
        />
        <button
          v-if="searchQuery"
          class="btn btn-outline-secondary"
          type="button"
          @click="handleSearch('')"
        >
          <i class="bi bi-x"></i>
        </button>
      </div>
      <slot name="toolbar"></slot>
    </div>

    <!-- Table -->
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th
              v-for="col in columns"
              :key="col.key"
              :style="col.width ? { width: col.width } : {}"
              :class="{ sortable: col.sortable }"
              @click="col.sortable && handleSort(col.key)"
            >
              <div class="d-flex align-items-center gap-1">
                <span>{{ col.label }}</span>
                <span v-if="col.sortable" class="sort-icons">
                  <i
                    v-if="sortKey === col.key && sortOrder === 'asc'"
                    class="bi bi-caret-up-fill text-primary"
                  ></i>
                  <i
                    v-else-if="sortKey === col.key && sortOrder === 'desc'"
                    class="bi bi-caret-down-fill text-primary"
                  ></i>
                  <i v-else class="bi bi-caret-up text-muted opacity-50"></i>
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
                <div class="skeleton-line"></div>
              </td>
              <td v-if="$slots.actions">
                <div class="skeleton-line" style="width: 60px;"></div>
              </td>
            </tr>
          </template>

          <!-- Data rows -->
          <template v-else-if="data.length > 0">
            <tr
              v-for="(row, index) in data"
              :key="index"
              :class="{ 'cursor-pointer': hasRowClick }"
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
            <td :colspan="columns.length + ($slots.actions ? 1 : 0)" class="text-center py-5">
              <div class="text-muted">
                <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                <p class="mb-0">No records found</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="d-flex justify-content-between align-items-center mt-3">
      <small class="text-muted">
        Page {{ currentPage }} of {{ totalPages }}
      </small>
      <nav aria-label="Table pagination">
        <ul class="pagination pagination-sm mb-0">
          <li class="page-item" :class="{ disabled: currentPage <= 1 }">
            <button class="page-link" type="button" @click="goToPage(currentPage - 1)">
              <i class="bi bi-chevron-left"></i>
            </button>
          </li>
          <li
            v-for="page in visiblePages"
            :key="page"
            class="page-item"
            :class="{ active: page === currentPage }"
          >
            <button class="page-link" type="button" @click="goToPage(page)">
              {{ page }}
            </button>
          </li>
          <li class="page-item" :class="{ disabled: currentPage >= totalPages }">
            <button class="page-link" type="button" @click="goToPage(currentPage + 1)">
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
.sortable {
  cursor: pointer;
  user-select: none;
}

.sortable:hover {
  background-color: #e9ecef;
}

.sort-icons {
  font-size: 0.7rem;
}

.skeleton-line {
  height: 1rem;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  width: 80%;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.cursor-pointer {
  cursor: pointer;
}
</style>
