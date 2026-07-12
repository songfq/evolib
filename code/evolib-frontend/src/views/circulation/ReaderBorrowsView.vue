<template>
  <AppLayout>
    <div class="page-container">
      <h2 class="page-title">在借清单</h2>
      <div class="search-form">
        <EvoInput v-model="readerId" placeholder="请输入读者ID" label="读者ID" />
        <EvoButton type="primary" @click="loadBorrows">查询</EvoButton>
      </div>
      <div v-if="errorMsg" class="error-text">{{ errorMsg }}</div>
      <EvoTable v-if="borrowList.length > 0" :columns="columns" :data="borrowList" rowKey="id" />
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
const errorMsg = ref('');
const borrowList = ref([]);

const columns = [
  { key: 'title', title: '书名', width: 'auto' },
  { key: 'isbn', title: 'ISBN', width: '180px' },
  { key: 'borrowDate', title: '借阅日期', width: '120px' },
  { key: 'dueDate', title: '应还日期', width: '120px' },
];

async function loadBorrows() {
  errorMsg.value = '';
  if (!readerId.value.trim()) {
    errorMsg.value = '请输入读者ID';
    return;
  }
  try {
    const resp = await api.get(`/readers/${readerId.value.trim()}/borrows`);
    if (resp.code === 0) {
      borrowList.value = resp.data || [];
    } else {
      errorMsg.value = resp.message;
    }
  } catch {
    errorMsg.value = '网络异常，请稍后重试';
  }
}
</script>

<style scoped>
.search-form { display: flex; gap: var(--spacing-md); align-items: flex-end; margin-bottom: var(--spacing-lg); }
</style>