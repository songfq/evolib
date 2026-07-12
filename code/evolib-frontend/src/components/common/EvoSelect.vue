<template>
  <div class="evo-select-wrapper">
    <label v-if="label" class="evo-select__label">{{ label }}</label>
    <select
      :value="modelValue"
      :disabled="disabled"
      @change="$emit('update:modelValue', $event.target.value)"
      class="evo-select"
    >
      <option v-if="placeholder" value="" disabled>{{ placeholder }}</option>
      <option v-for="opt in options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
    </select>
    <span v-if="error" class="evo-select__error">{{ error }}</span>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  options: { type: Array, default: () => [] },
  error: { type: String, default: '' },
});
defineEmits(['update:modelValue']);
</script>

<style scoped>
.evo-select-wrapper { display: flex; flex-direction: column; gap: var(--spacing-sm); }
.evo-select__label { font-size: var(--font-size-base); color: var(--color-text); font-weight: 500; }
.evo-select {
  height: var(--input-height);
  padding: 0 var(--spacing-md);
  border: 1px solid var(--color-border-input);
  border-radius: var(--border-radius);
  font-size: var(--font-size-base);
  color: var(--color-text);
  background: var(--color-bg-input);
  outline: none;
  cursor: pointer;
  transition: border-color 0.2s;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%23666' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right var(--spacing-md) center;
}
.evo-select:focus { border-color: var(--color-primary); }
.evo-select:disabled { background: var(--color-bg-container); color: var(--color-text-disabled); cursor: not-allowed; }
.evo-select__error { font-size: var(--font-size-sm); color: var(--color-error); }
</style>