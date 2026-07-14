<template>
  <div class="evo-form-item">
    <label v-if="label" class="evo-form-item__label">
      {{ label }}
      <span v-if="required" class="evo-form-item__required">*</span>
    </label>
    <div class="evo-form-item__control">
      <slot />
    </div>
    <span v-if="error" class="evo-form-item__error">{{ error }}</span>
  </div>
</template>

<script setup>
defineProps({
  label: { type: String, default: '' },
  required: { type: Boolean, default: false },
  error: { type: String, default: '' },
});
</script>

<style scoped>
.evo-form-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}
.evo-form-item__label {
  font-size: var(--font-size-base);
  color: var(--color-text);
  font-weight: 500;
}
.evo-form-item__required {
  color: var(--color-error);
  margin-left: 2px;
}
.evo-form-item__control {
  width: 100%;
}
.evo-form-item__error {
  font-size: var(--font-size-sm);
  color: var(--color-error);
}
</style>