<template>
  <AppLayout>
    <div class="page-container">
      <h2 class="page-title">借书操作</h2>
      <div class="borrow-form">
        <EvoInput v-model="readerId" placeholder="请输入读者借阅证号" label="读者ID" />
        <EvoInput v-model="isbn" placeholder="请输入图书ISBN" label="图书ISBN" />
        <EvoButton type="primary" :loading="loading" @click="doBorrow">确认借书</EvoButton>
      </div>
      <div v-if="errorMsg" class="error-text">{{ errorMsg }}</div>
      <div v-if="successMsg" class="success-text">{{ successMsg }}</div>
      <div v-if="borrowList.length > 0" class="borrow-list">
        <h3>当前在借清单（{{ readerId }}）</h3>
        <EvoTable :columns="columns" :data="borrowList" rowKey="id" />
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
import EvoTable from '@/components/common/EvoTable.vue';

const readerId = ref('');
const isbn = ref('');
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');
const borrowList = ref([]);

const columns = [
  { key: 'title', title: '书名', width: 'auto' },
  { key: 'isbn', title: 'ISBN', width: '180px' },
  { key: 'borrowDate', title: '借阅日期', width: '120px' },
  { key: 'dueDate', title: '应还日期', width: '120px' },
];

async function doBorrow() {
  errorMsg.value = '';
  successMsg.value = '';
  if (!readerId.value.trim() || !isbn.value.trim()) {
    errorMsg.value = '请输入读者ID和图书ISBN';
    return;
  }
  loading.value = true;
  try {
    const resp = await api.post('/borrow-records', {
      readerId: readerId.value.trim(),
      isbn: isbn.value.trim(),
    });
    if (resp.code === 0) {
      successMsg.value = '借书成功';
      isbn.value = '';
      const listResp = await api.get(`/readers/${readerId.value.trim()}/borrows`);
      borrowList.value = listResp.data || [];
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
.borrow-form { display: flex; gap: var(--spacing-md); align-items: flex-end; margin-bottom: var(--spacing-md); }
.borrow-list { margin-top: var(--spacing-lg); }
</style>