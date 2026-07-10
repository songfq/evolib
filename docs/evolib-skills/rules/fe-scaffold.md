# fe-scaffold — 前端 Vue 3 + Element Plus 项目搭建

## 目标

一键生成 `evolib-frontend/` 工程骨架，含 Vue 3 + Vite + Vue Router + Pinia + Axios + Element Plus 全家桶。

---

## 技术栈版本

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Vue | 3.4+ | Composition API + `<script setup>` |
| Vite | 5.x | 构建工具，替代 Webpack |
| Vue Router | 4.x | SPA 路由 + 守卫 |
| Pinia | 2.x | 状态管理（替代 Vuex） |
| Axios | 1.x | HTTP 客户端 + 拦截器 |
| Element Plus | 2.x | UI 组件库（按钮/表格/弹窗/表单等） |
| Node.js | 18+ | 运行环境 |

---

## 生成步骤

### 第一步：初始化 Vite 项目

```bash
# 使用 Vite 创建 Vue 3 项目
npm create vite@latest evolib-frontend -- --template vue

# 进入项目目录
cd evolib-frontend

# 安装所有依赖
npm install vue-router@4 pinia@2 axios@1 element-plus@2
```

### 第二步：注册 Element Plus

在 `src/main.js` 中：

```javascript
// main.js — Vue 应用入口
import { createApp } from 'vue';
import ElementPlus from 'element-plus';     // ★ UI 组件库
import 'element-plus/dist/index.css';       // Element Plus 默认样式
import App from './App.vue';
import router from './router';
import pinia from './stores';
import '@/styles/theme.css';                // ★ 主题变量（放最后，覆盖 Element Plus 默认色）

const app = createApp(App);
app.use(ElementPlus);   // 注册 Element Plus，此后可在任何 .vue 中直接用 <el-button> 等
app.use(router);        // 注册路由
app.use(pinia);         // 注册状态管理
app.mount('#app');      // 挂载到 index.html 的 <div id="app">
```

> 关键：`theme.css` 放在 Element Plus CSS **之后**引入，利用 CSS 层叠规则，同名变量（如 `--el-color-primary`）会被你的值覆盖。

### 第三步：创建完整目录树

```
evolib-frontend/
├── index.html
├── package.json
├── vite.config.js
│
├── public/
│   └── favicon.ico
│
└── src/
    ├── main.js                            # 应用入口
    ├── App.vue                            # 根组件
    │
    ├── styles/
    │   ├── theme.css                      # ★ 主题变量（唯一换肤入口）
    │   └── common.css                     # reset + 全局排版
    │
    ├── router/
    │   └── index.js                       # 路由表 + beforeEach 守卫
    │
    ├── stores/
    │   ├── index.js                       # Pinia 实例创建
    │   └── auth.js                        # 认证 Store（token / role / login / logout）
    │
    ├── utils/
    │   └── api.js                         # Axios 实例 + JWT 拦截器
    │
    └── views/                             # 页面视图（按角色分目录）
        │
        ├── LoginView.vue                  # P-01 登录页
        │
        ├── reader/
        │   ├── BookSearchView.vue         # P-02 图书检索
        │   └── BookDetailView.vue         # P-03 图书详情
        │
        ├── circulation/
        │   ├── BorrowView.vue             # P-04 借书操作台
        │   ├── ReturnView.vue             # P-05 还书操作台
        │   ├── RegisterReaderView.vue     # P-06 读者注册
        │   └── ReaderBorrowsView.vue      # P-07 在借清单
        │
        └── admin/
            ├── AddBookView.vue            # P-08 图书上架
            ├── RemoveBookView.vue         # P-09 图书下架
            └── ResetPasswordView.vue      # P-10 重置密码
```

### 第四步：theme.css 主题变量

```css
/* theme.css — 核心主题变量，同时也是 Element Plus 覆盖入口
   修改此文件即可全局换肤，无需改任何组件代码 */

:root {
  /* 主色调 — 同时覆盖 Element Plus 的 --el-color-primary 系列 */
  --color-primary:        #1890FF;
  --color-primary-hover:  #40A9FF;
  --color-primary-active: #096DD9;
  --el-color-primary:     #1890FF;            /* ← Element Plus 主色 */

  /* 背景 */
  --color-bg-page:        #FFFFFF;
  --color-bg-container:   #FAFAFA;

  /* 边框 — 同时覆盖 Element Plus 的 --el-border-color-base */
  --color-border:         #D9D9D9;
  --el-border-color-base: #D9D9D9;

  /* 文字 */
  --color-text:           #333333;
  --color-text-secondary: #666666;
  --el-text-color-primary: #333333;

  /* 语义色 */
  --color-error:          #FF4D4F;
  --color-success:        #52C41A;
  --color-warning:        #FAAD14;
  --el-color-danger:      #FF4D4F;

  /* 排版 */
  --font-family:          "Microsoft YaHei", "PingFang SC", -apple-system, sans-serif;
  --font-size-base:       14px;

  /* 尺寸 */
  --sidebar-width:        220px;
  --header-height:        56px;
}
```

### 第五步：vite.config.js 配置

```javascript
// vite.config.js — Vite 配置
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';                 // Node.js 路径处理

export default defineConfig({
  plugins: [vue()],
  
  resolve: {
    alias: { '@': resolve(__dirname, 'src') },  // @ 别名 → 指向 src/
  },

  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8080',
        changeOrigin: true,
        // 说明：开发时前端请求 /api/xxx → 代理到后端 http://localhost:8080/api/xxx
      },
    },
  },

  build: {
    // 构建产物直接输出到后端的 static/ 目录
    outDir: resolve(__dirname, '../evolib-backend/src/main/resources/static'),
    emptyOutDir: true,
  },
});
```

### 第六步：router/index.js 路由骨架

```javascript
// router/index.js — Vue Router 路由表 + 守卫
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes = [
  // 公开页面 — auth: false 表示无需登录
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { auth: false } },

  // 读者端 — role 表示该页面需要的角色
  { path: '/reader/search',         name: 'BookSearch',  component: () => import('@/views/reader/BookSearchView.vue'),  meta: { role: 'ROLE_READER' } },
  { path: '/reader/detail/:isbn',   name: 'BookDetail',  component: () => import('@/views/reader/BookDetailView.vue'),  meta: { role: 'ROLE_READER' } },

  // 馆员端
  { path: '/circulation/borrow',   name: 'Borrow',   component: () => import('@/views/circulation/BorrowView.vue'),   meta: { role: 'ROLE_CIRCULATION' } },
  { path: '/circulation/return',   name: 'Return',   component: () => import('@/views/circulation/ReturnView.vue'),   meta: { role: 'ROLE_CIRCULATION' } },
  { path: '/circulation/register', name: 'Register', component: () => import('@/views/circulation/RegisterReaderView.vue'), meta: { role: 'ROLE_CIRCULATION' } },
  { path: '/circulation/borrows',  name: 'Borrows',  component: () => import('@/views/circulation/ReaderBorrowsView.vue'),  meta: { role: 'ROLE_CIRCULATION' } },

  // 管理员端
  { path: '/admin/add-book',       name: 'AddBook',       component: () => import('@/views/admin/AddBookView.vue'),       meta: { role: 'ROLE_ADMIN' } },
  { path: '/admin/remove-book',    name: 'RemoveBook',    component: () => import('@/views/admin/RemoveBookView.vue'),    meta: { role: 'ROLE_ADMIN' } },
  { path: '/admin/reset-password', name: 'ResetPassword', component: () => import('@/views/admin/ResetPasswordView.vue'), meta: { role: 'ROLE_ADMIN' } },

  // 兜底：未匹配的路径全部重定向到登录
  { path: '/:pathMatch(.*)*', redirect: '/login' },
];

const router = createRouter({ history: createWebHistory(), routes });

// ---- 路由守卫（每次页面跳转前执行） ----
router.beforeEach((to, from, next) => {
  const auth = useAuthStore();

  if (to.meta.auth === false) return next();    // 1. 公开页面 → 放行
  if (!auth.isLoggedIn) return next('/login');  // 2. 未登录 → 强制跳登录
  if (to.meta.role && !auth.hasRole(to.meta.role)) {
    return next(auth.homePath);                  // 3. 角色不匹配 → 跳回自己的首页
  }
  next(); // 4. 通过所有检查 → 正常进入页面
});

export default router;
```

---

## 验收标准

1. `npm install` 无报错
2. `npm run dev` 启动成功，浏览器打开 `http://localhost:3000` 显示 Vite 默认页
3. 引入 Element Plus 后，在 `App.vue` 中写 `<el-button>测试</el-button>` 可正常渲染蓝色按钮
4. 生成的目录结构与上方**完全一致**，无缺漏目录
