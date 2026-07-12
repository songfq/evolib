<template>
  <div class="evo-input-wrapper">
    <label v-if="label" class="evo-input__label">{{ label }}</label>
    <input
      type="text"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      @input="$emit('update:modelValue', $event.target.value)"
      class="evo-input"
    />
    <span v-if="error" class="evo-input__error">{{ error }}</span>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  error: { type: String, default: '' },
});
defineEmits(['update:modelValue']);
</script>

<style scoped>
.evo-input-wrapper { display: flex; flex-direction: column; gap: var(--spacing-sm); }
.evo-input__label { font-size: var(--font-size-base); color: var(--color-text); font-weight: 500; }
.evo-input {
  height: var(--input-height);
  padding: 0 var(--spacing-md);
  border: 1px solid var(--color-border-input);
  border-radius: var(--border-radius);
  font-size: var(--font-size-base);
  color: var(--color-text);
  background: var(--color-bg-input);
  outline: none;
  transition: border-color 0.2s;
}
.evo-input:focus { border-color: var(--color-primary); }
.evo-input:disabled { background: var(--color-bg-container); color: var(--color-text-disabled); cursor: not-allowed; }
.evo-input::placeholder { color: var(--color-text-disabled); }
.evo-input__error { font-size: var(--font-size-sm); color: var(--color-error); }
</style>