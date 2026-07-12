import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { api } from '@/utils/api';
import router from '@/router';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('evolib_token') || '');
  const role = ref(localStorage.getItem('evolib_role') || '');
  const readerName = ref(localStorage.getItem('evolib_name') || '');

  const isLoggedIn = computed(() => !!token.value);

  const homePath = computed(() => ({
    'ROLE_READER': '/reader/search',
    'ROLE_CIRCULATION': '/circulation/borrow',
    'ROLE_ADMIN': '/admin/add-book',
  }[role.value] || '/login'));

  function hasRole(required) {
    return role.value === required;
  }

  async function login(readerId, password) {
    const resp = await api.post('/auth/login', { readerId, password });
    if (resp.code === 0) {
      token.value = resp.data.token;
      role.value = resp.data.role;
      readerName.value = resp.data.name || readerId;
      localStorage.setItem('evolib_token', resp.data.token);
      localStorage.setItem('evolib_role', resp.data.role);
      localStorage.setItem('evolib_name', readerName.value);
      router.push(homePath.value);
    }
    return resp;
  }

  function logout() {
    token.value = '';
    role.value = '';
    readerName.value = '';
    localStorage.clear();
    router.push('/login');
  }

  return { token, role, readerName, isLoggedIn, homePath, hasRole, login, logout };
});