<template>
  <AppLayout>
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">下架图书</h2>
      </div>
      <EvoForm>
        <EvoFormItem label="ISBN" required>
          <EvoInput v-model="isbn" placeholder="请输入ISBN" />
        </EvoFormItem>
        <template #footer>
          <span v-if="errorMsg" class="error-text">{{ errorMsg }}</span>
          <EvoButton type="danger" :loading="loading" @click="doRemove">下架</EvoButton>
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

const isbn = ref('');
const loading = ref(false);
const errorMsg = ref('');

async function doRemove() {
  errorMsg.value = '';
  if (!isbn.value.trim()) {
    errorMsg.value = '请输入ISBN';
    return;
  }
  loading.value = true;
  try {
    const resp = await api.delete(`/admin/books/${isbn.value.trim()}`);
    if (resp.code === 0) {
      alert('下架成功');
      isbn.value = '';
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