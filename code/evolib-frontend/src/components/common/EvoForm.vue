<template>
  <form class="evo-form" @submit.prevent>
    <div class="evo-form__body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="evo-form__footer">
      <slot name="footer" />
    </div>
  </form>
</template>

<script setup>
defineProps({
  layout: { type: String, default: 'grid' },
  cols: { type: Number, default: 3 },
});
</script>

<style scoped>
.evo-form {
  background: var(--color-bg-container);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}
.evo-form__body {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-md);
}
.evo-form__footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border-light);
}

@media (max-width: 1200px) {
  .evo-form__body { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .evo-form__body { grid-template-columns: 1fr; }
}
</style>