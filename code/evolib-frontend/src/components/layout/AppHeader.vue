<template>
  <header class="app-header">
    <div class="app-header__logo">EvoLib</div>
    <div class="app-header__user">
      <span class="app-header__username">{{ auth.readerName }}</span>
      <EvoButton type="default" @click="auth.logout">退出登录</EvoButton>
    </div>
  </header>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';
import EvoButton from '@/components/common/EvoButton.vue';
const auth = useAuthStore();
</script>

<style scoped>
.app-header {
  height: var(--header-height);
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  position: fixed;
  top: 0;
  left: var(--sidebar-width);
  right: 0;
  z-index: 100;
}
.app-header__logo { font-size: var(--font-size-xl); font-weight: 600; }
.app-header__user { display: flex; align-items: center; gap: var(--spacing-md); }
.app-header__username { font-size: var(--font-size-base); }
.app-header__user .evo-btn--default { background: rgba(255,255,255,0.2); border-color: rgba(255,255,255,0.3); color: #fff; }
.app-header__user .evo-btn--default:hover { background: rgba(255,255,255,0.3); }
</style>