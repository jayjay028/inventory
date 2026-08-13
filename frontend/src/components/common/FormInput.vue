<template>
  <div class="mb-3">
    <label v-if="label" :for="inputId" class="form-label">
      {{ label }}
      <span v-if="required" class="text-danger">*</span>
    </label>

    <!-- Select -->
    <select
      v-if="type === 'select'"
      :id="inputId"
      class="form-select"
      :class="{ 'is-invalid': error }"
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
      class="form-control"
      :class="{ 'is-invalid': error }"
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
      class="form-control"
      :class="{ 'is-invalid': error }"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :required="required"
      @input="$emit('update:modelValue', $event.target.value)"
    />

    <!-- Help text -->
    <div v-if="helpText && !error" class="form-text">{{ helpText }}</div>

    <!-- Error -->
    <div v-if="error" class="invalid-feedback d-block">{{ error }}</div>
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
