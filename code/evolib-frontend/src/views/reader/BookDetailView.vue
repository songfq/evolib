<template>
  <AppLayout>
    <div class="page-container">
      <h2 class="page-title">图书详情</h2>
      <div v-if="book" class="book-detail">
        <div class="book-detail__info">
          <div class="book-detail__row">
            <span class="book-detail__label">ISBN</span>
            <span class="book-detail__value">{{ book.isbn }}</span>
          </div>
          <div class="book-detail__row">
            <span class="book-detail__label">书名</span>
            <span class="book-detail__value">{{ book.title }}</span>
          </div>
          <div class="book-detail__row">
            <span class="book-detail__label">作者</span>
            <span class="book-detail__value">{{ book.author }}</span>
          </div>
          <div class="book-detail__row">
            <span class="book-detail__label">总库存</span>
            <span class="book-detail__value">{{ book.totalStock }}</span>
          </div>
          <div class="book-detail__row">
            <span class="book-detail__label">可借数量</span>
            <span class="book-detail__value">{{ book.availableStock }}</span>
          </div>
          <div class="book-detail__row">
            <span class="book-detail__label">馆藏位置</span>
            <span class="book-detail__value">{{ book.shelfLocation || '-' }}</span>
          </div>
          <div class="book-detail__row">
            <span class="book-detail__label">简介</span>
            <span class="book-detail__value">{{ book.description || '-' }}</span>
          </div>
        </div>
        <EvoButton type="default" @click="$router.back()">返回</EvoButton>
      </div>
      <div v-else>加载中...</div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { api } from '@/utils/api';
import AppLayout from '@/components/layout/AppLayout.vue';
import EvoButton from '@/components/common/EvoButton.vue';

const route = useRoute();
const book = ref(null);

async function loadBook() {
  const isbn = route.params.isbn;
  const resp = await api.get(`/books/${isbn}`);
  if (resp.code === 0) {
    book.value = resp.data;
  }
}

onMounted(loadBook);
</script>

<style scoped>
.book-detail { background: var(--color-bg-page); border: 1px solid var(--color-border); border-radius: var(--border-radius-lg); padding: var(--spacing-lg); }
.book-detail__info { margin-bottom: var(--spacing-lg); }
.book-detail__row { display: flex; margin-bottom: var(--spacing-md); }
.book-detail__label { width: 100px; font-weight: 500; color: var(--color-text-secondary); flex-shrink: 0; }
.book-detail__value { flex: 1; color: var(--color-text); }
</style>