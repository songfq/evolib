<template>
  <AppLayout>
    <div class="page-container">
      <h2 class="page-title">注册读者</h2>
      <div class="register-form">
        <EvoInput v-model="readerId" placeholder="请输入读者ID" label="读者ID" />
        <EvoInput v-model="name" placeholder="请输入姓名" label="姓名" />
        <EvoInput v-model="phone" placeholder="请输入手机号" label="手机号" />
        <EvoInput v-model="password" placeholder="请输入密码" label="密码" />
        <span v-if="errorMsg" class="error-text">{{ errorMsg }}</span>
        <span v-if="successMsg" class="success-text">{{ successMsg }}</span>
        <EvoButton type="primary" :loading="loading" @click="doRegister">注册</EvoButton>
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

const readerId = ref('');
const name = ref('');
const phone = ref('');
const password = ref('');
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

async function doRegister() {
  errorMsg.value = '';
  successMsg.value = '';
  if (!readerId.value.trim() || !name.value.trim() || !phone.value.trim() || !password.value.trim()) {
    errorMsg.value = '请填写完整信息';
    return;
  }
  loading.value = true;
  try {
    const resp = await api.post('/readers', {
      readerId: readerId.value.trim(),
      name: name.value.trim(),
      phone: phone.value.trim(),
      password: password.value,
    });
    if (resp.code === 0) {
      successMsg.value = '注册成功';
      readerId.value = '';
      name.value = '';
      phone.value = '';
      password.value = '';
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
.register-form { max-width: 400px; }
</style>