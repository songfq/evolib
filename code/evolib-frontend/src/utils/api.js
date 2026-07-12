import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

http.interceptors.request.use(config => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

http.interceptors.response.use(
  resp => resp.data,
  error => {
    if (error.response?.status === 401) {
      useAuthStore().logout();
    } else if (error.response?.status === 403) {
      alert('无权限访问');
    }
    return Promise.reject(error);
  }
);

export const api = http;