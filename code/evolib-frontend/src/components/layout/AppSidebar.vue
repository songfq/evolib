<template>
  <aside class="app-sidebar">
    <nav class="app-sidebar__nav">
      <router-link :class="['app-sidebar__item', { active: $route.name === 'BookSearch' }]" to="/reader/search">图书检索</router-link>
      <router-link :class="['app-sidebar__item', { active: $route.name === 'Borrow' }]" to="/circulation/borrow">借书操作</router-link>
      <router-link :class="['app-sidebar__item', { active: $route.name === 'Return' }]" to="/circulation/return">还书操作</router-link>
      <router-link :class="['app-sidebar__item', { active: $route.name === 'Register' }]" to="/circulation/register">注册读者</router-link>
      <router-link :class="['app-sidebar__item', { active: $route.name === 'Borrows' }]" to="/circulation/borrows">在借清单</router-link>
      <router-link :class="['app-sidebar__item', { active: $route.name === 'AddBook' }]" to="/admin/add-book">上架图书</router-link>
      <router-link :class="['app-sidebar__item', { active: $route.name === 'RemoveBook' }]" to="/admin/remove-book">下架图书</router-link>
      <router-link :class="['app-sidebar__item', { active: $route.name === 'orgtagList' }]" to="/orgtag/list">组织标签</router-link>
      <router-link :class="['app-sidebar__item', { active: $route.name === 'ResetPassword' }]" to="/admin/reset-password">重置密码</router-link>
    </nav>
  </aside>
</template>

<script setup>
</script>

<style scoped>
.app-sidebar {
  width: var(--sidebar-width);
  background: var(--color-bg-container);
  border-right: 1px solid var(--color-border);
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  padding-top: var(--header-height);
}
.app-sidebar__nav { padding: var(--spacing-md); }
.app-sidebar__item {
  display: block;
  padding: var(--spacing-sm) var(--spacing-md);
  margin-bottom: var(--spacing-xs);
  border-radius: var(--border-radius);
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: var(--font-size-base);
  transition: all 0.2s;
}
.app-sidebar__item:hover { background: var(--color-bg-hover); color: var(--color-text); }
.app-sidebar__item.active { background: var(--color-primary); color: #fff; }
</style>