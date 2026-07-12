<template>
  <AppLayout>
    <div class="page-container">
      <h2 class="page-title">还书操作</h2>
      <div class="return-form">
        <EvoInput v-model="readerId" placeholder="请输入读者借阅证号" label="读者ID" />
        <EvoButton type="primary" :loading="loading" @click="loadBorrows">查询在借清单</EvoButton>
      </div>
      <div v-if="errorMsg" class="error-text">{{ errorMsg }}</div>
      <div v-if="borrowList.length > 0" class="borrow-list">
        <h3>在借清单（{{ readerId }}）</h3>
        <EvoTable :columns="columns" :data="borrowList" rowKey="id">
          <template #cell-action="{ row }">
            <EvoButton type="primary" @click="doReturn(row.id)">还书</EvoButton>
          </template>
        </EvoTable>
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
const loading = ref(false);
const errorMsg = ref('');
const borrowList = ref([]);

const columns = [
  { key: 'title', title: '书名', width: 'auto' },
  { key: 'isbn', title: 'ISBN', width: '180px' },
  { key: 'borrowDate', title: '借阅日期', width: '120px' },
  { key: 'dueDate', title: '应还日期', width: '120px' },
  { key: 'action', title: '操作', width: '80px' },
];

async function loadBorrows() {
  errorMsg.value = '';
  if (!readerId.value.trim()) {
    errorMsg.value = '请输入读者ID';
    return;
  }
  loading.value = true;
  try {
    const resp = await api.get(`/readers/${readerId.value.trim()}/borrows`);
    if (resp.code === 0) {
      borrowList.value = resp.data || [];
    } else {
      errorMsg.value = resp.message;
    }
  } catch {
    errorMsg.value = '网络异常，请稍后重试';
  } finally {
    loading.value = false;
  }
}

async function doReturn(recordId) {
  try {
    const resp = await api.put(`/borrow-records/${recordId}/return`);
    if (resp.code === 0) {
      alert('还书成功');
      await loadBorrows();
    } else {
      alert(resp.message);
    }
  } catch {
    alert('网络异常，请稍后重试');
  }
}
</script>

<style scoped>
.return-form { display: flex; gap: var(--spacing-md); align-items: flex-end; margin-bottom: var(--spacing-md); }
.borrow-list { margin-top: var(--spacing-lg); }
</style>