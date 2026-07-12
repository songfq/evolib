<template>
  <AppLayout>
    <div class="page-container">
      <h2 class="page-title">图书检索</h2>
      <div class="search-form">
        <EvoInput v-model="keyword" placeholder="请输入书名或作者" />
        <EvoButton type="primary" @click="handleSearch">搜索</EvoButton>
      </div>
      <EvoTable :columns="columns" :data="bookList" rowKey="isbn">
        <template #cell-isbn="{ row }">
          <router-link :to="`/reader/detail/${row.isbn}`">{{ row.isbn }}</router-link>
        </template>
      </EvoTable>
      <EvoPagination :current="currentPage" :total="totalPages" @page-change="handlePageChange" />
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
import EvoPagination from '@/components/common/EvoPagination.vue';

const keyword = ref('');
const bookList = ref([]);
const currentPage = ref(1);
const totalPages = ref(1);

const columns = [
  { key: 'isbn', title: 'ISBN', width: '180px' },
  { key: 'title', title: '书名', width: 'auto' },
  { key: 'author', title: '作者', width: '150px' },
  { key: 'availableStock', title: '可借数量', width: '100px' },
];

async function handleSearch() {
  currentPage.value = 1;
  await loadData();
}

async function handlePageChange(page) {
  currentPage.value = page;
  await loadData();
}

async function loadData() {
  const resp = await api.get('/books/search', {
    params: { keyword: keyword.value, page: currentPage.value - 1, size: 10 },
  });
  if (resp.code === 0) {
    bookList.value = resp.data.records || [];
    totalPages.value = resp.data.pages || 1;
  }
}

loadData();
</script>

<style scoped>
.search-form { display: flex; gap: var(--spacing-md); margin-bottom: var(--spacing-lg); }
.search-form .evo-input { flex: 1; max-width: 400px; }
</style>