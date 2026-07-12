<template>
  <button :class="['evo-btn', `evo-btn--${type}`]" :disabled="disabled || loading" @click="$emit('click')">
    <span v-if="loading" class="evo-btn__spinner"></span>
    <slot />
  </button>
</template>

<script setup>
defineProps({
  type: { type: String, default: 'primary' },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
});
defineEmits(['click']);
</script>

<style scoped>
.evo-btn {
  height: var(--btn-height);
  padding: 0 var(--spacing-md);
  border-radius: var(--border-radius);
  font-size: var(--font-size-base);
  cursor: pointer;
  transition: background 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid transparent;
}
.evo-btn--primary {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.evo-btn--primary:hover { background: var(--color-primary-hover); }
.evo-btn--primary:active { background: var(--color-primary-active); }
.evo-btn--default {
  background: var(--color-bg-page);
  color: var(--color-text);
  border-color: var(--color-border);
}
.evo-btn--default:hover { border-color: var(--color-primary); color: var(--color-primary); }
.evo-btn--danger {
  background: var(--color-error);
  color: #fff;
  border-color: var(--color-error);
}
.evo-btn--danger:hover { opacity: 0.9; }
.evo-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.evo-btn__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #fff;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>