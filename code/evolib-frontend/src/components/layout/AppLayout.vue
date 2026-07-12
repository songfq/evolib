<template>
  <div class="app-layout">
    <AppSidebar />
    <AppHeader />
    <main class="app-layout__main">
      <slot />
    </main>
  </div>
</template>

<script setup>
import AppSidebar from './AppSidebar.vue';
import AppHeader from './AppHeader.vue';
</script>

<style scoped>
.app-layout { display: flex; height: 100%; }
.app-layout__main {
  flex: 1;
  margin-left: var(--sidebar-width);
  margin-top: var(--header-height);
  min-height: 100vh;
  background: var(--color-bg-page);
}
</style>