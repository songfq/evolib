<template>
  <div class="evo-pagination">
    <button class="evo-pagination__btn" :disabled="current === 1" @click="$emit('page-change', current - 1)">上一页</button>
    <span class="evo-pagination__info">第 {{ current }} / {{ total }} 页</span>
    <button class="evo-pagination__btn" :disabled="current === total" @click="$emit('page-change', current + 1)">下一页</button>
  </div>
</template>

<script setup>
defineProps({
  current: { type: Number, default: 1 },
  total: { type: Number, default: 1 },
});
defineEmits(['page-change']);
</script>

<style scoped>
.evo-pagination { display: flex; align-items: center; justify-content: center; gap: var(--spacing-md); margin-top: var(--spacing-lg); }
.evo-pagination__btn {
  height: var(--btn-height);
  padding: 0 var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  font-size: var(--font-size-base);
  cursor: pointer;
  background: var(--color-bg-page);
  color: var(--color-text);
  transition: all 0.2s;
}
.evo-pagination__btn:hover:not(:disabled) { border-color: var(--color-primary); color: var(--color-primary); }
.evo-pagination__btn:disabled { opacity: 0.5; cursor: not-allowed; }
.evo-pagination__info { font-size: var(--font-size-base); color: var(--color-text-secondary); }
</style>