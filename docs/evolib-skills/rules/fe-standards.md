# fe-standards — 前端开发规范

## 目标

定义 Vue 3 + Element Plus 前端项目的统一规范：SFC 结构、目录约定、组件使用、路由/Store 模式。

---

## 一、Vue SFC 结构规范

每个 `.vue` 文件必须按以下顺序组织：

```vue
<!-- 1. <script setup> 在最前 — 逻辑先行，方便快速理解数据流 -->
<script setup>
// ① imports（Vue API → 第三方库 → 自有模块）
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';       // Element Plus 消息提示
import { api } from '@/utils/api';              // Axios 封装
import { useAuthStore } from '@/stores/auth';   // Pinia Store

// ② 响应式状态（ref / reactive / computed）
const keyword = ref('');
const tableData = ref([]);
const loading  = ref(false);

// ③ 方法（业务逻辑函数）
async function doSearch() {
  loading.value = true;
  try {
    const resp = await api.get('/books/search', {
      params: { keyword: keyword.value, page: 1, size: 10 }
    });
    tableData.value = resp.data.list || [];
  } catch (err) {
    ElMessage.error('搜索失败');                // Element Plus 错误提示
  } finally {
    loading.value = false;
  }
}

// ④ 生命周期钩子
onMounted(() => { doSearch(); });
</script>

<!-- 2. <template> 在中间 — 纯 Element Plus 组件拼装 -->
<template>
  <el-container>
    <el-header>EvoLib 图书馆管理系统</el-header>
    <el-main>
      <el-input v-model="keyword" placeholder="搜索书名/作者/ISBN" />
      <el-button type="primary" @click="doSearch">搜索</el-button>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="title" label="书名" />
        <el-table-column prop="author" label="作者" />
        <el-table-column prop="isbn" label="ISBN" />
      </el-table>
    </el-main>
  </el-container>
</template>

<!-- 3. <style scoped> 在最后 — 仅写页面特有样式，全局样式走 theme.css -->
<style scoped>
.el-header {
  background-color: var(--color-primary);  /* ← 引用 theme.css 主题变量 */
  color: white;
}
</style>
```

### 严禁写法

- ❌ `<div style="color: red">` — 禁止内联样式，必须用 class 或 CSS 变量
- ❌ `<el-button onclick="xxx">` — 禁止 HTML 事件属性，必须用 `@click="xxx"`
- ❌ `<script>` 中包含直接操作 DOM 的代码（`document.getElementById`）— 用 Vue 响应式

---

## 二、目录结构约定

```
src/
├── views/              # 页面（每个路由对应一个 .vue）
│   ├── LoginView.vue
│   ├── reader/         # 读者端页面
│   ├── circulation/    # 馆员端页面
│   └── admin/          # 管理员端页面
│
├── router/
│   └── index.js        # 唯一路由文件（路由表 + 守卫都在这里）
│
├── stores/
│   ├── index.js        # Pinia 实例创建
│   └── auth.js         # 认证 Store（token / role / login / logout）
│
├── utils/
│   └── api.js          # Axios 实例 + JWT 拦截器
│
└── styles/
    ├── theme.css       # ★ 主题变量（CSS 变量 → 换肤入口）
    └── common.css      # reset + 全局排版
```

> 说明：不再需要 `components/` 目录。Evo* 自研组件已被 Element Plus 替代，页面直接用 `<el-button>` 等即可。

---

## 三、Element Plus 组件使用约定

### 常用组件速查

| 用途 | Element Plus 组件 | 2 行用法 |
| --- | --- | --- |
| 按钮 | `<el-button>` | `<el-button type="primary" @click="fn">主按钮</el-button>` |
| 输入框 | `<el-input>` | `<el-input v-model="val" placeholder="请输入" />` |
| 下拉选择 | `<el-select>` | `<el-select v-model="val"><el-option v-for="..." /></el-select>` |
| 表格 | `<el-table>` | `<el-table :data="list"><el-table-column prop="name" label="名称" /></el-table>` |
| 分页 | `<el-pagination>` | `<el-pagination :total="total" :page-size="10" @current-change="fn" />` |
| 弹窗 | `<el-dialog>` | `<el-dialog v-model="visible" title="提示">内容</el-dialog>` |
| 表单 | `<el-form>` | `<el-form :model="form"><el-form-item label="名称"><el-input v-model="form.name" /></el-form-item></el-form>` |
| 布局 | `<el-container>` | `<el-container><el-header /><el-main /><el-footer /></el-container>` |
| 消息提示 | `ElMessage` | `import { ElMessage } from 'element-plus'; ElMessage.success('成功');` |
| 加载动画 | `v-loading` | `<el-table v-loading="loading" :data="list">` |

### 主题变量运用

所有颜色通过 `var(--color-xxx)` 引用——如果已在 `theme.css` 中定义了对应的 Element Plus CSS 变量（如 `--el-color-primary`），Element Plus 组件**自动生效**，无需额外配置。

仅在需要覆盖 Element Plus 默认行为时，才在 `<style scoped>` 中写：

```css
.el-header { background-color: var(--color-primary); }
.el-table .warning-row { background: var(--color-warning); }
```

---

## 四、路由模式

### 路由表编写

```javascript
// 每个功能页面对应一条路由
{
  path: '/circulation/borrow',                           // URL 路径
  name: 'Borrow',                                        // 路由名称
  component: () => import('@/views/circulation/BorrowView.vue'),  // 懒加载
  meta: { role: 'ROLE_CIRCULATION' }                      // 权限标注
}
```

### 路由守卫三步逻辑

```javascript
router.beforeEach((to, from, next) => {
  // 第 1 步：公开页面（如 /login）→ 直接放行
  if (to.meta.auth === false) return next();
  
  // 第 2 步：未登录 → 跳转登录页
  if (!authStore.isLoggedIn) return next('/login');
  
  // 第 3 步：角色不匹配 → 跳回自己的首页
  if (to.meta.role && !authStore.hasRole(to.meta.role)) return next(authStore.homePath);
  
  next();
});
```

---

## 五、Pinia Store 模式

```javascript
// stores/auth.js
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { api } from '@/utils/api';
import router from '@/router';

// setup 语法（类似 Vue 组件的 <script setup>）
export const useAuthStore = defineStore('auth', () => {
  // State — 响应式状态
  const token = ref(localStorage.getItem('evolib_token') || '');
  const role  = ref(localStorage.getItem('evolib_role') || '');

  // Getters — 计算属性
  const isLoggedIn = computed(() => !!token.value);

  // Actions — 方法
  async function login(readerId, password) { /* ... */ }
  function logout() { /* ... */ }
  function hasRole(required) { return role.value === required; }

  return { token, role, isLoggedIn, login, logout, hasRole };
});
```

---

## 六、Axios 模式

```javascript
// utils/api.js
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
});

// 请求拦截 — 自动注入 JWT
http.interceptors.request.use(config => {
  const auth = useAuthStore();
  if (auth.token) config.headers.Authorization = `Bearer ${auth.token}`;
  return config;
});

// 响应拦截 — 401 → 登出，403 → 提示
http.interceptors.response.use(
  resp => resp.data,          // 自动解包：返回 {code, data, message}
  error => {
    if (error.response?.status === 401) useAuthStore().logout();
    return Promise.reject(error);
  }
);

export const api = http;
```
