<template>
  <AppLayout>
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">新增借书</h2>
        <EvoButton @click="goBack">返回列表</EvoButton>
      </div>
      <EvoForm>
        <EvoFormItem label="读者ID" required>
          <EvoInput v-model="readerId" placeholder="请输入读者借阅证号" />
        </EvoFormItem>
        <EvoFormItem label="图书ISBN" required>
          <EvoInput v-model="isbn" placeholder="请输入图书ISBN" />
        </EvoFormItem>
        <template #footer>
          <span v-if="errorMsg" class="error-text">{{ errorMsg }}</span>
          <span v-if="successMsg" class="success-text">{{ successMsg }}</span>
          <EvoButton type="primary" :loading="loading" @click="doBorrow">确认借书</EvoButton>
        </template>
      </EvoForm>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '@/utils/api';
import AppLayout from '@/components/layout/AppLayout.vue';
import EvoInput from '@/components/common/EvoInput.vue';
import EvoButton from '@/components/common/EvoButton.vue';
import EvoForm from '@/components/common/EvoForm.vue';
import EvoFormItem from '@/components/common/EvoFormItem.vue';

const router = useRouter();
const readerId = ref('');
const isbn = ref('');
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

function goBack() {
  router.push('/circulation/borrow');
}

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