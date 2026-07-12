<template>
  <AppLayout>
    <div class="page-container">
      <h2 class="page-title">上架图书</h2>
      <div class="add-book-form">
        <EvoInput v-model="isbn" placeholder="请输入ISBN" label="ISBN" />
        <EvoInput v-model="title" placeholder="请输入书名" label="书名" />
        <EvoInput v-model="author" placeholder="请输入作者" label="作者" />
        <EvoInput v-model="totalStock" placeholder="请输入总库存" label="总库存" />
        <EvoInput v-model="shelfLocation" placeholder="请输入馆藏位置" label="馆藏位置" />
        <EvoInput v-model="description" placeholder="请输入简介" label="简介" />
        <span v-if="errorMsg" class="error-text">{{ errorMsg }}</span>
        <span v-if="successMsg" class="success-text">{{ successMsg }}</span>
        <EvoButton type="primary" :loading="loading" @click="doAdd">上架</EvoButton>
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

const isbn = ref('');
const title = ref('');
const author = ref('');
const totalStock = ref('');
const shelfLocation = ref('');
const description = ref('');
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

async function doAdd() {
  errorMsg.value = '';
  successMsg.value = '';
  if (!isbn.value.trim() || !title.value.trim() || !author.value.trim() || !totalStock.value.trim()) {
    errorMsg.value = '请填写必填信息（ISBN、书名、作者、库存）';
    return;
  }
  loading.value = true;
  try {
    const resp = await api.post('/admin/books', {
      isbn: isbn.value.trim(),
      title: title.value.trim(),
      author: author.value.trim(),
      totalStock: parseInt(totalStock.value),
      availableStock: parseInt(totalStock.value),
      shelfLocation: shelfLocation.value.trim(),
      description: description.value.trim(),
    });
    if (resp.code === 0) {
      successMsg.value = '上架成功';
      isbn.value = '';
      title.value = '';
      author.value = '';
      totalStock.value = '';
      shelfLocation.value = '';
      description.value = '';
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
.add-book-form { max-width: 500px; }
</style>