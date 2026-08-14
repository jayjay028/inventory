<template>
  <div class="fi-group">
    <label v-if="label" :for="inputId" class="fi-label">
      {{ label }}
      <span v-if="required" class="fi-required">*</span>
    </label>

    <!-- Select -->
    <select
      v-if="type === 'select'"
      :id="inputId"
      class="fi-select"
      :class="{ 'fi-error-state': error }"
      :value="modelValue"
      :disabled="disabled"
      :required="required"
      @change="$emit('update:modelValue', $event.target.value)"
    >
      <option value="" disabled>{{ placeholder || 'Select...' }}</option>
      <option
        v-for="opt in options"
        :key="opt.value"
        :value="opt.value"
      >
        {{ opt.label }}
      </option>
    </select>

    <!-- Textarea -->
    <textarea
      v-else-if="type === 'textarea'"
      :id="inputId"
      class="fi-textarea"
      :class="{ 'fi-error-state': error }"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :required="required"
      rows="3"
      @input="$emit('update:modelValue', $event.target.value)"
    ></textarea>

    <!-- Standard input -->
    <input
      v-else
      :id="inputId"
      :type="type"
      class="fi-input"
      :class="{ 'fi-error-state': error }"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :required="required"
      @input="$emit('update:modelValue', $event.target.value)"
    />

    <!-- Help text -->
    <p v-if="helpText && !error" class="fi-help">{{ helpText }}</p>

    <!-- Error -->
    <p v-if="error" class="fi-error-msg">{{ error }}</p>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  error: {
    type: String,
    default: ''
  },
  required: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  helpText: {
    type: String,
    default: ''
  },
  options: {
    type: Array,
    default: () => []
  }
})

defineEmits(['update:modelValue'])

let _idCounter = 0
const inputId = `form-input-${++_idCounter}-${Math.random().toString(36).substring(2, 8)}`
</script>

<style scoped>
.fi-group {
  margin-bottom: 1rem;
}

.fi-label {
  display: block;
  font-size: 0.8125rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.375rem;
  line-height: 1.4;
}

.fi-required {
  color: #dc2626;
  font-size: 0.75rem;
  margin-left: 0.125rem;
}

.fi-input,
.fi-select,
.fi-textarea {
  display: block;
  width: 100%;
  padding: 0 0.75rem;
  font-size: 0.8125rem;
  font-family: inherit;
  color: #1e293b;
  background-color: #fff;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.fi-input,
.fi-select {
  height: 38px;
  line-height: 38px;
}

.fi-textarea {
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
  line-height: 1.5;
  min-height: 80px;
  resize: vertical;
}

.fi-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16' fill='%236b7280'%3e%3cpath d='M4.646 6.646a.5.5 0 0 1 .708 0L8 9.293l2.646-2.647a.5.5 0 0 1 .708.708l-3 3a.5.5 0 0 1-.708 0l-3-3a.5.5 0 0 1 0-.708z'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 14px 14px;
  padding-right: 2.25rem;
}

.fi-input::placeholder,
.fi-textarea::placeholder {
  color: #9ca3af;
}

.fi-input:focus,
.fi-select:focus,
.fi-textarea:focus {
  border-color: #1e40af;
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.1);
}

.fi-input:disabled,
.fi-select:disabled,
.fi-textarea:disabled {
  background-color: #f9fafb;
  color: #9ca3af;
  cursor: not-allowed;
}

/* Error state */
.fi-error-state {
  border-color: #dc2626 !important;
}

.fi-error-state:focus {
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1) !important;
}

.fi-error-msg {
  margin: 0.25rem 0 0;
  font-size: 0.75rem;
  color: #dc2626;
  line-height: 1.4;
}

.fi-help {
  margin: 0.25rem 0 0;
  font-size: 0.75rem;
  color: #6b7280;
  line-height: 1.4;
}
</style>
