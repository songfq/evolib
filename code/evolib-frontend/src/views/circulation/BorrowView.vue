<template>
  <AppLayout>
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">借书操作</h2>
        <EvoButton type="primary" @click="goToBorrow">新增借书</EvoButton>
      </div>
      <div class="search-card">
        <div class="search-form">
          <EvoInput v-model="readerId" placeholder="请输入读者ID" label="读者ID" />
          <EvoButton type="primary" @click="loadBorrows">查询</EvoButton>
        </div>
        <div v-if="errorMsg" class="error-text">{{ errorMsg }}</div>
      </div>
      <EvoTable :columns="columns" :data="borrowList" rowKey="id" :empty-text="showEmptyText ? '暂无在借记录' : ''" />
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@/utils/api';
import AppLayout from '@/components/layout/AppLayout.vue';
import EvoInput from '@/components/common/EvoInput.vue';
import EvoButton from '@/components/common/EvoButton.vue';
import EvoTable from '@/components/common/EvoTable.vue';

const router = useRouter();
const readerId = ref('');
const errorMsg = ref('');
const borrowList = ref([]);

const showEmptyText = computed(() => readerId.value.trim() && borrowList.value.length === 0 && !errorMsg.value);

const columns = [
  { key: 'title', title: '书名', width: 'auto' },
  { key: 'isbn', title: 'ISBN', width: '180px' },
  { key: 'borrowDate', title: '借阅日期', width: '120px' },
  { key: 'dueDate', title: '应还日期', width: '120px' },
];

function goToBorrow() {
  router.push('/circulation/borrow/form');
}

async function loadBorrows() {
  errorMsg.value = '';
  if (!readerId.value.trim()) {
    errorMsg.value = '请输入读者ID';
    return;
  }
  try {
    const resp = await api.get(`/readers/${readerId.value.trim()}/borrows`);
    if (resp.code === 0) {
      borrowList.value = resp.data?.records || [];
    } else {
      errorMsg.value = resp.message;
    }
  } catch {
    errorMsg.value = '网络异常，请稍后重试';
  }
}
</script>

<style scoped>
</style>