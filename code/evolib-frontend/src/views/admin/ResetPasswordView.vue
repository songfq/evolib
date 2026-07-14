<template>
  <AppLayout>
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">重置密码</h2>
      </div>
      <EvoForm>
        <EvoFormItem label="读者ID" required>
          <EvoInput v-model="readerId" placeholder="请输入读者ID" />
        </EvoFormItem>
        <EvoFormItem label="新密码" required>
          <EvoInput v-model="newPassword" placeholder="请输入新密码" />
        </EvoFormItem>
        <template #footer>
          <span v-if="errorMsg" class="error-text">{{ errorMsg }}</span>
          <span v-if="successMsg" class="success-text">{{ successMsg }}</span>
          <EvoButton type="primary" :loading="loading" @click="doReset">重置密码</EvoButton>
        </template>
      </EvoForm>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref } from 'vue';
import { api } from '@/utils/api';
import AppLayout from '@/components/layout/AppLayout.vue';
import EvoInput from '@/components/common/EvoInput.vue';
import EvoButton from '@/components/common/EvoButton.vue';
import EvoForm from '@/components/common/EvoForm.vue';
import EvoFormItem from '@/components/common/EvoFormItem.vue';

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
</style>