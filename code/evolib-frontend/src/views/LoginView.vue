<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-card__title">EvoLib</h1>
      <p class="login-card__subtitle">图书馆管理系统</p>
      <div class="login-card__form">
        <EvoInput v-model="readerId" label="读者ID" placeholder="请输入读者ID" />
        <EvoInput v-model="password" label="密码" placeholder="请输入密码" />
        <span v-if="errorMsg" class="error-text">{{ errorMsg }}</span>
        <EvoButton type="primary" :loading="loading" @click="handleLogin" class="login-btn">登录</EvoButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import EvoInput from '@/components/common/EvoInput.vue';
import EvoButton from '@/components/common/EvoButton.vue';

const auth = useAuthStore();
const readerId = ref('');
const password = ref('');
const loading = ref(false);
const errorMsg = ref('');

async function handleLogin() {
  errorMsg.value = '';
  if (!readerId.value.trim() || !password.value.trim()) {
    errorMsg.value = '请输入读者ID和密码';
    return;
  }
  loading.value = true;
  try {
    const resp = await auth.login(readerId.value.trim(), password.value);
    if (resp.code !== 0) {
      errorMsg.value = resp.message || '登录失败';
    }
  } catch {
    errorMsg.value = '网络异常，请稍后重试';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary) 0%, #096DD9 100%);
}
.login-card {
  background: var(--color-bg-page);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--spacing-xl);
  width: 400px;
}
.login-card__title {
  font-size: 32px;
  color: var(--color-primary);
  text-align: center;
  margin: 0 0 var(--spacing-xs);
}
.login-card__subtitle {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  text-align: center;
  margin: 0 0 var(--spacing-xl);
}
.login-card__form { display: flex; flex-direction: column; gap: var(--spacing-md); }
.login-btn { width: 100%; margin-top: var(--spacing-sm); }
</style>