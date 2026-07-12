<template>
  <AppLayout>
    <div class="page-container">
      <h2 class="page-title">重置密码</h2>
      <div class="reset-password-form">
        <EvoInput v-model="readerId" placeholder="请输入读者ID" label="读者ID" />
        <EvoInput v-model="newPassword" placeholder="请输入新密码" label="新密码" />
        <span v-if="errorMsg" class="error-text">{{ errorMsg }}</span>
        <span v-if="successMsg" class="success-text">{{ successMsg }}</span>
        <EvoButton type="primary" :loading="loading" @click="doReset">重置密码</EvoButton>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref } from 'vue';
import { api } from '@/utils/api';
import AppLayout from '@/components/layout/AppLayout.vue';
import EvoInput from '@/components/common/EvoInput.vue';
import EvoButton from '@/components/common/EvoButton.vue';

const readerId = ref('');
const newPassword = ref('');
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

async function doReset() {
  errorMsg.value = '';
  successMsg.value = '';
  if (!readerId.value.trim() || !newPassword.value.trim()) {
    errorMsg.value = '请输入读者ID和新密码';
    return;
  }
  loading.value = true;
  try {
    const resp = await api.put(`/admin/readers/${readerId.value.trim()}/reset-password`, {
      newPassword: newPassword.value,
    });
    if (resp.code === 0) {
      successMsg.value = '密码重置成功';
      readerId.value = '';
      newPassword.value = '';
    } else {
      errorMsg.value = resp.message;
    }
  } catch {
    errorMsg.value = '网络异常，请稍后重试';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.reset-password-form { max-width: 400px; }
</style>