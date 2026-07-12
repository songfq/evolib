<template>
  <Teleport to="body">
    <div v-if="visible" class="evo-modal-overlay" @click.self="$emit('close')">
      <div class="evo-modal">
        <div class="evo-modal__header">
          <h3>{{ title }}</h3>
          <button class="evo-modal__close" @click="$emit('close')">&times;</button>
        </div>
        <div class="evo-modal__body"><slot /></div>
        <div class="evo-modal__footer">
          <EvoButton type="default" @click="$emit('close')">取消</EvoButton>
          <EvoButton type="primary" @click="$emit('confirm')">{{ confirmText }}</EvoButton>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import EvoButton from './EvoButton.vue';
defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '提示' },
  confirmText: { type: String, default: '确认' },
});
defineEmits(['close', 'confirm']);
</script>

<style scoped>
.evo-modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.evo-modal { background: var(--color-bg-page); border-radius: var(--border-radius-lg); box-shadow: var(--shadow-lg); min-width: 400px; max-width: 520px; }
.evo-modal__header { padding: var(--spacing-lg); display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--color-border-light); }
.evo-modal__header h3 { margin: 0; font-size: var(--font-size-lg); color: var(--color-text); }
.evo-modal__close { background: none; border: none; font-size: 24px; cursor: pointer; color: var(--color-text-secondary); }
.evo-modal__body { padding: var(--spacing-lg); color: var(--color-text-secondary); }
.evo-modal__footer { padding: var(--spacing-lg); display: flex; justify-content: flex-end; gap: var(--spacing-sm); border-top: 1px solid var(--color-border-light); }
</style>