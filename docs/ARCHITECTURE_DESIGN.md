# EvoLib 架构设计文档

| 项目 | 内容 |
| --- | --- |
| 项目名称 | EvoLib 图书馆管理 MVP 系统 |
| 文档版本 | 1.0（初稿） |
| 日期 | 2026年7月9日 |
| 拟制 | 系统架构师 |
| 对应 SRS | [SRS_v1.0.md](./SRS_v1.0.md) |
| 对应 BRD | [BRD_v1.0.md](./BRD_v1.0.md) |

---

## 目录

1. [架构目标与原则](#1-架构目标与原则)
2. [系统上下文](#2-系统上下文)
3. [技术选型详述](#3-技术选型详述)
4. [前端架构](#4-前端架构)
5. [后端架构](#5-后端架构)
6. [数据库设计（DDL）](#6-数据库设计ddl)
7. [API 契约设计](#7-api-契约设计)
8. [安全架构](#8-安全架构)
9. [部署架构（Phase 1 MVP）](#9-部署架构phase-1-mvp)
10. [架构演进路线图](#10-架构演进路线图)
11. [关键架构决策（ADR）](#11-关键架构决策adr)

---

## 1. 架构目标与原则

### 1.1 架构目标

| 目标 | 描述 |
| --- | --- |
| **快速交付** | MVP 1 周内完成，单体架构优先，不引入不必要的分布式复杂度 |
| **可演进性** | 代码分层清晰，业务边界明确，为后续拆分为微服务预留清晰的切割面 |
| **前后端分离** | RESTful API 契约驱动，前端 Vue 3 SPA 与后端 Spring Boot 独立开发、独立构建、独立部署 |
| **界面一致性** | 白底-蓝按钮-灰边框三色基准，通过 CSS 变量模板统一管控，支持后续一键换肤 |
| **安全基线** | JWT + BCrypt + RBAC 三道防线，审计日志全量覆盖 |

### 1.2 架构原则

| 编号 | 原则 | 说明 |
| --- | --- | --- |
| AP-01 | **YAGNI（不做过度设计）** | MVP 阶段仅实现 SRS 明确要求的功能，不为「可能的需求」预留代码 |
| AP-02 | **关注点分离** | Vue SFC（`.vue` 单文件组件）天然分离 template / script / style；组件按职责拆分，不出现「上帝组件」 |
| AP-03 | **契约优先** | 前后端以 REST API JSON 契约为准，任何一端的变更必须同步更新契约文档 |
| AP-04 | **分层隔离** | 后端 Controller → Service → Mapper 三层，上层不感知下层实现细节；前端 View → Store → API 三层同理 |
| AP-05 | **演进预留** | 前后端均按业务域（book / reader / borrow / auth / admin）垂直划分目录，便于未来拆服务 |

---

## 2. 系统上下文

### 2.1 系统上下文图

```
┌──────────────────────────────────────────────────────┐
│                    EvoLib 系统边界                     │
│                                                      │
│  ┌──────────┐         HTTP/JSON         ┌──────────┐ │
│  │ 浏览器     │ ◄──────────────────────► │           │ │
│  │ (读者/馆员 │     REST API             │  Spring   │ │
│  │  /管理员) │                          │  Boot     │ │
│  │          │                           │  2.7.18   │ │
│  │  Vue 3   │    /api/v1/*  ←───→       │  单体      │ │
│  │  SPA     │                           │  + 内嵌    │ │
│  │  (Vite   │    /* 静态文件  ←───       │  Tomcat   │ │
│  │  构建产物)│                           │           │ │
│  └──────────┘                           │    │      │ │
│                                         │    │ JDBC │ │
│                                         │    ▼      │ │
│                                         │ ┌───────┐ │ │
│                                         │ │Postgre│ │ │
│                                         │ │SQL 14+│ │ │
│                                         │ └───────┘ │ │
│                                         └──────────┘ │
│                                                      │
│  ┌──────────┐                                       │
│  │ 管理员     │ ──► 直接修改 application.yml           │
│  │ (配置参数) │     (借阅天数/上限，MVP 临时方案)        │
│  └──────────┘                                       │
└──────────────────────────────────────────────────────┘
```

### 2.2 用户角色与访问矩阵

| 角色 | 标识 | 可访问页面 | 可调用 API 前缀 |
| --- | --- | --- | --- |
| 未认证用户 | — | 登录页 | `POST /auth/login` |
| 普通读者 | `ROLE_READER` | 图书检索、图书详情 | `GET /books/**` |
| 流通馆员 | `ROLE_CIRCULATION` | 借书、还书、注册读者、在借清单 | `POST /borrow-records`, `PUT /borrow-records/**`, `POST /readers`, `PUT /readers/**/phone`, `GET /readers/**/borrows` |
| 系统管理员 | `ROLE_ADMIN` | 上架、下架、重置密码 | `POST /admin/books`, `DELETE /admin/books/**`, `PUT /admin/readers/**/reset-password` |

---

## 3. 技术选型详述

### 3.1 后端技术栈

| 组件 | 选型 | 版本 | 选型理由 |
| --- | --- | --- | --- |
| JDK | OpenJDK | 1.8.0_401 | 当前环境已安装且稳定，Maven 编译目标 `1.8` |
| 框架 | Spring Boot | 2.7.18 | 2.x 最终稳定版，完美支持 JDK 8，不依赖 JDK 17 |
| Spring 生态 | Spring Framework | 5.3.31（自带） | Boot 2.7.18 内置 |
| 安全 | Spring Security | 5.7.x（自带） | 与 JWT 深度集成 |
| ORM | MyBatis-Plus | 3.5.5 | 兼容 JDK 8，提供 Lambda 查询、分页、乐观锁 |
| 数据库 | PostgreSQL | 14+ | SRS 明确指定 |
| 连接池 | HikariCP | Spring Boot 默认 | 零配置、性能最优 |
| JWT | jjwt | 0.11.5 | 轻量、JDK 8 兼容 |
| 密码加密 | BCrypt | Spring Security 自带 | SRS NFR-07 要求 |
| JSON | Jackson | Spring Boot 默认 | 无需额外引入 |
| 构建 | Maven | 3.6+ | 稳定，依赖管理成熟 |

### 3.2 前端技术栈

| 组件 | 选型 | 版本 | 选型理由 |
| --- | --- | --- | --- |
| 框架 | Vue 3 | 3.4+ | Composition API + `<script setup>` 语法，组件化开发，生态成熟 |
| 构建工具 | Vite | 5.x | 极速冷启动 + HMR，Vue 官方推荐，替代 Webpack |
| 路由 | Vue Router | 4.x | SPA 页面导航 + 路由守卫（登录拦截 + RBAC 权限校验） |
| 状态管理 | Pinia | 2.x | Vue 3 官方推荐，TypeScript 友好，轻量替代 Vuex |
| HTTP 客户端 | Axios | 1.x | 拦截器机制天然适配 JWT 注入 + 401 统一处理 |
| CSS 方案 | Scoped CSS + CSS 变量 | — | 组件样式隔离（`<style scoped>`），全局主题变量（`theme.css`） |
| UI 组件库 | 自研（MVP） | — | 可选 Element Plus（Phase 2），自研轻量、无额外依赖 |
| 图标 | 内联 SVG 组件 | — | 不引入图标库，减少依赖 |
| Node.js | 最低 18.x | 18+ | Vite 5 运行时要求 |

---

## 4. 前端架构

### 4.1 整体分层

```
┌────────────────────────────────────────────────────────┐
│                    Vue 3 SPA 前端                       │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │  视图层 (Views)                                   │  │
│  │  LoginView  BookSearchView  BookDetailView        │  │
│  │  BorrowView  ReturnView  RegisterReaderView       │  │
│  │  ReaderBorrowsView  AddBookView  RemoveBookView   │  │
│  │  ResetPasswordView                               │  │
│  │                                                  │  │
│  │  职责：组装通用组件，处理页面级交互，调用 Store     │  │
│  └───────────────────────┬──────────────────────────┘  │
│                          │                             │
│  ┌───────────────────────▼──────────────────────────┐  │
│  │  组件层 (Components)                              │  │
│  │  EvoButton  EvoInput  EvoTable  EvoModal         │  │
│  │  EvoPagination  EvoSelect  AppHeader  AppSidebar │  │
│  │                                                  │  │
│  │  职责：可复用的 UI 原子/组合组件，引用 CSS 变量     │  │
│  └───────────────────────┬──────────────────────────┘  │
│                          │                             │
│  ┌───────────────────────▼──────────────────────────┐  │
│  │  状态层 (Pinia Stores)                            │  │
│  │  useAuthStore  (token / role / login / logout)    │  │
│  │                                                  │  │
│  │  职责：全局共享状态（认证信息），驱动路由守卫       │  │
│  └───────────────────────┬──────────────────────────┘  │
│                          │                             │
│  ┌───────────────────────▼──────────────────────────┐  │
│  │  工具层 (Utils)                                   │  │
│  │  api.js   (Axios 封装 + JWT 拦截器)               │  │
│  │  router/  (Vue Router + beforeEach 守卫)          │  │
│  │                                                  │  │
│  │  职责：HTTP 通信、路由导航与权限拦截               │  │
│  └──────────────────────────────────────────────────┘  │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │  样式体系                                          │  │
│  │  theme.css  (CSS 变量：白底/蓝按钮/灰边框)         │  │
│  │  common.css (reset + 全局排版)                    │  │
│  │  组件内 <style scoped> (组件级样式隔离)            │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

### 4.2 项目目录结构

```
evolib-frontend/
├── index.html                     # Vite 入口 HTML
├── package.json
├── vite.config.js                 # Vite 配置（代理 /api → 后端 :8080）
│
├── public/
│   └── favicon.ico
│
└── src/
    ├── main.js                    # 创建 App、挂载 Router + Pinia
    ├── App.vue                    # 根组件：<router-view /> 占位
    │
    ├── styles/
    │   ├── theme.css              # ★ CSS 变量主题（唯一换肤入口）
    │   └── common.css             # reset + 全局排版 + 工具类
    │
    ├── router/
    │   └── index.js               # 路由表 + beforeEach 守卫
    │
    ├── stores/
    │   └── auth.js                # Pinia：token / role / login / logout
    │
    ├── utils/
    │   └── api.js                 # Axios 实例：baseURL + JWT 拦截器
    │
    ├── components/                # 通用 UI 组件
    │   ├── common/
    │   │   ├── EvoButton.vue
    │   │   ├── EvoInput.vue
    │   │   ├── EvoSelect.vue
    │   │   ├── EvoTable.vue
    │   │   ├── EvoModal.vue
    │   │   └── EvoPagination.vue
    │   └── layout/
    │       ├── AppHeader.vue       # 顶部导航栏（用户名 + 退出）
    │       ├── AppSidebar.vue      # 侧边导航菜单（馆员/管理员）
    │       └── AppLayout.vue       # 通用布局壳（header + sidebar + main）
    │
    └── views/                      # 页面视图（每个页面对应一个路由）
        │
        ├── LoginView.vue           # P-01
        │
        ├── reader/
        │   ├── BookSearchView.vue  # P-02
        │   └── BookDetailView.vue  # P-03
        │
        ├── circulation/
        │   ├── BorrowView.vue      # P-04
        │   ├── ReturnView.vue      # P-05
        │   ├── RegisterReaderView.vue  # P-06
        │   └── ReaderBorrowsView.vue   # P-07
        │
        └── admin/
            ├── AddBookView.vue     # P-08
            ├── RemoveBookView.vue  # P-09
            └── ResetPasswordView.vue   # P-10
```

### 4.3 样式模板系统（主题变量）

与之前设计一致，CSS 自定义属性集中在 `theme.css`，Vue 组件的 `<style scoped>` 内通过 `var(--xxx)` 引用。换肤仅需替换该文件。

#### 4.3.1 基准主题变量（`theme.css`）

```css
/* ========================================
   EvoLib 主题模板 — 基准白底蓝按钮灰边框
   修改此文件即可完成全局换肤
   ======================================== */

:root {
  /* ---- 主色调 ---- */
  --color-primary:        #1890FF;   /* 主按钮、链接、选中态 */
  --color-primary-hover:  #40A9FF;   /* 按钮 hover */
  --color-primary-active: #096DD9;   /* 按钮 active/pressed */

  /* ---- 背景 ---- */
  --color-bg-page:        #FFFFFF;   /* 页面背景（白底） */
  --color-bg-container:   #FAFAFA;   /* 卡片/面板背景 */
  --color-bg-input:       #FFFFFF;   /* 输入框背景 */
  --color-bg-hover:       #F5F5F5;   /* 表格行 hover */

  /* ---- 边框 ---- */
  --color-border:         #D9D9D9;   /* 默认边框（灰边框） */
  --color-border-light:   #E8E8E8;   /* 轻边框 */
  --color-border-input:   #D9D9D9;   /* 输入框边框 */

  /* ---- 文字 ---- */
  --color-text:           #333333;   /* 正文 */
  --color-text-secondary: #666666;   /* 次要文字/标签 */
  --color-text-disabled:  #BFBFBF;   /* 禁用态文字 */

  /* ---- 语义色 ---- */
  --color-error:          #FF4D4F;   /* 错误提示 */
  --color-success:        #52C41A;   /* 成功提示 */
  --color-warning:        #FAAD14;   /* 警告提示 */
  --color-overdue:        #FFF1F0;   /* 超期行高亮背景 */

  /* ---- 排版 ---- */
  --font-family:          "Microsoft YaHei", "PingFang SC", -apple-system, sans-serif;
  --font-size-base:       14px;
  --font-size-sm:         12px;
  --font-size-lg:         16px;
  --font-size-xl:         20px;
  --line-height:          1.5;

  /* ---- 间距 ---- */
  --spacing-xs:  4px;
  --spacing-sm:  8px;
  --spacing-md:  16px;
  --spacing-lg:  24px;
  --spacing-xl:  32px;

  /* ---- 圆角 ---- */
  --border-radius:        4px;
  --border-radius-lg:     8px;

  /* ---- 阴影 ---- */
  --shadow-sm:  0 1px 2px  rgba(0, 0, 0, 0.06);
  --shadow-md:  0 2px 8px  rgba(0, 0, 0, 0.10);
  --shadow-lg:  0 4px 16px rgba(0, 0, 0, 0.12);

  /* ---- 尺寸 ---- */
  --btn-height:       36px;
  --input-height:      36px;
  --table-row-height:  40px;
  --sidebar-width:     220px;
  --header-height:     56px;
}
```

#### 4.3.2 Bootstrap 场景：Element Plus 对接主题变量

若后续引入 Element Plus 组件库（Phase 2 可选），只需在 `vite.config.js` 中配置 SCSS 变量映射：

```javascript
// vite.config.js — 将 EvoLib 主题变量映射为 Element Plus 的 SCSS 变量
css: {
  preprocessorOptions: {
    scss: {
      additionalData: `
        $--color-primary: var(--color-primary);
        $--color-success: var(--color-success);
        $--color-warning: var(--color-warning);
        $--color-danger:  var(--color-error);
      `
    }
  }
}
```

一套 CSS 变量，同时驱动自研组件和第三方组件库，真正实现**一次定义，全局生效**。

### 4.4 通用组件设计（部分示例）

#### `EvoButton.vue`

```vue
<template>
  <button
    :class="['evo-btn', `evo-btn--${type}`]"
    :disabled="disabled || loading"
    @click="$emit('click')"
  >
    <span v-if="loading" class="evo-btn__spinner"></span>
    <slot />
  </button>
</template>

<script setup>
defineProps({
  type:    { type: String, default: 'primary' },  // primary | default | danger
  disabled:{ type: Boolean, default: false },
  loading: { type: Boolean, default: false },
});
defineEmits(['click']);
</script>

<style scoped>
.evo-btn {
  height: var(--btn-height);
  padding: 0 var(--spacing-md);
  border-radius: var(--border-radius);
  font-size: var(--font-size-base);
  cursor: pointer;
  transition: background 0.2s;
  display: inline-flex; align-items: center; gap: 6px;
}
.evo-btn--primary {
  background: var(--color-primary); color: #fff;
  border: 1px solid var(--color-primary);
}
.evo-btn--primary:hover  { background: var(--color-primary-hover); }
.evo-btn--primary:active { background: var(--color-primary-active); }

.evo-btn--default {
  background: var(--color-bg-page); color: var(--color-text);
  border: 1px solid var(--color-border);
}
.evo-btn--default:hover { border-color: var(--color-primary); color: var(--color-primary); }

.evo-btn--danger {
  background: var(--color-error); color: #fff;
  border: 1px solid var(--color-error);
}

.evo-btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
```

#### `EvoTable.vue`

```vue
<template>
  <table class="evo-table">
    <thead>
      <tr>
        <th v-for="col in columns" :key="col.key" :style="{ width: col.width }">
          {{ col.title }}
        </th>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="(row, idx) in data"
        :key="rowKey ? row[rowKey] : idx"
        :class="{ 'row-overdue': row._overdue }"
      >
        <td v-for="col in columns" :key="col.key">
          <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
            {{ row[col.key] }}
          </slot>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup>
defineProps({
  columns: { type: Array, required: true },  // [{ key, title, width }]
  data:    { type: Array, required: true },
  rowKey:  { type: String, default: '' },    // 可选的行唯一标识字段
});
</script>

<style scoped>
.evo-table { width: 100%; border-collapse: collapse; background: var(--color-bg-page); font-size: var(--font-size-base); }
.evo-table th { height: var(--table-row-height); padding: 0 var(--spacing-md); background: var(--color-bg-container); color: var(--color-text); border-bottom: 2px solid var(--color-border); text-align: left; font-weight: 600; }
.evo-table td { height: var(--table-row-height); padding: 0 var(--spacing-md); border-bottom: 1px solid var(--color-border-light); color: var(--color-text); }
.evo-table tbody tr:hover { background: var(--color-bg-hover); }
.evo-table .row-overdue { background: var(--color-overdue); }
</style>
```

#### `EvoModal.vue`

```vue
<template>
  <Teleport to="body">
    <div v-if="visible" class="evo-modal-overlay" @click.self="$emit('close')">
      <div class="evo-modal">
        <div class="evo-modal__header">
          <h3>{{ title }}</h3>
        </div>
        <div class="evo-modal__body"><slot /></div>
        <div class="evo-modal__footer">
          <EvoButton type="default" @click="$emit('close')">取消</EvoButton>
          <EvoButton type="primary" @click="$emit('confirm')">{{ confirmText }}</EvoButton>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  visible:     { type: Boolean, default: false },
  title:       { type: String,  default: '提示' },
  confirmText:{ type: String,  default: '确认' },
});
defineEmits(['close', 'confirm']);
</script>

<style scoped>
.evo-modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.evo-modal { background: var(--color-bg-page); border-radius: var(--border-radius-lg); box-shadow: var(--shadow-lg); min-width: 400px; max-width: 520px; }
.evo-modal__header { padding: var(--spacing-lg) var(--spacing-lg) 0; }
.evo-modal__header h3 { margin: 0; font-size: var(--font-size-lg); color: var(--color-text); }
.evo-modal__body   { padding: var(--spacing-lg); color: var(--color-text-secondary); }
.evo-modal__footer { padding: 0 var(--spacing-lg) var(--spacing-lg); display: flex; justify-content: flex-end; gap: var(--spacing-sm); }
</style>
```

### 4.5 路由设计（Vue Router + 守卫）

#### 路由表

```javascript
// router/index.js
import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  // 公开
  { path: '/login',  name: 'Login',  component: () => import('@/views/LoginView.vue'),      meta: { auth: false } },

  // 读者端
  { path: '/reader/search', name: 'BookSearch', component: () => import('@/views/reader/BookSearchView.vue'),   meta: { role: 'ROLE_READER' } },
  { path: '/reader/detail/:isbn', name: 'BookDetail', component: () => import('@/views/reader/BookDetailView.vue'), meta: { role: 'ROLE_READER' } },

  // 馆员端
  { path: '/circulation/borrow',   name: 'Borrow',    component: () => import('@/views/circulation/BorrowView.vue'),   meta: { role: 'ROLE_CIRCULATION' } },
  { path: '/circulation/return',   name: 'Return',    component: () => import('@/views/circulation/ReturnView.vue'),   meta: { role: 'ROLE_CIRCULATION' } },
  { path: '/circulation/register', name: 'Register',  component: () => import('@/views/circulation/RegisterReaderView.vue'), meta: { role: 'ROLE_CIRCULATION' } },
  { path: '/circulation/borrows',  name: 'Borrows',   component: () => import('@/views/circulation/ReaderBorrowsView.vue'),  meta: { role: 'ROLE_CIRCULATION' } },

  // 管理员端
  { path: '/admin/add-book',       name: 'AddBook',       component: () => import('@/views/admin/AddBookView.vue'),          meta: { role: 'ROLE_ADMIN' } },
  { path: '/admin/remove-book',    name: 'RemoveBook',    component: () => import('@/views/admin/RemoveBookView.vue'),       meta: { role: 'ROLE_ADMIN' } },
  { path: '/admin/reset-password', name: 'ResetPassword', component: () => import('@/views/admin/ResetPasswordView.vue'),    meta: { role: 'ROLE_ADMIN' } },

  // 兜底：未匹配路径重定向
  { path: '/:pathMatch(.*)*', redirect: '/login' },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});
```

#### 路由守卫（登录拦截 + RBAC）

```javascript
// router/index.js（续）
import { useAuthStore } from '@/stores/auth';

router.beforeEach((to, from, next) => {
  const auth = useAuthStore();

  // 1. 公开页面 → 直接放行
  if (to.meta.auth === false) return next();

  // 2. 未登录 → 跳转登录页
  if (!auth.isLoggedIn) return next('/login');

  // 3. 已登录但角色不匹配 → 按角色重定向到对应首页
  if (to.meta.role && !auth.hasRole(to.meta.role)) {
    return next(auth.homePath);
  }

  next();
});
```

### 4.6 Pinia 认证状态管理

```javascript
// stores/auth.js
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { api } from '@/utils/api';
import router from '@/router';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('evolib_token') || '');
  const role  = ref(localStorage.getItem('evolib_role')  || '');
  const readerName = ref(localStorage.getItem('evolib_name') || '');

  const isLoggedIn = computed(() => !!token.value);

  // 角色 → 首页路径映射
  const homePath = computed(() => ({
    'ROLE_READER':        '/reader/search',
    'ROLE_CIRCULATION':   '/circulation/borrow',
    'ROLE_ADMIN':         '/admin/add-book',
  }[role.value] || '/login'));

  function hasRole(required) {
    return role.value === required;
  }

  async function login(readerId, password) {
    const resp = await api.post('/auth/login', { readerId, password });
    if (resp.code === 0) {
      token.value = resp.data.token;
      role.value  = resp.data.role;
      readerName.value = resp.data.name || readerId;
      localStorage.setItem('evolib_token', resp.data.token);
      localStorage.setItem('evolib_role',  resp.data.role);
      localStorage.setItem('evolib_name',  readerName.value);
      router.push(homePath.value);
    }
    return resp;
  }

  function logout() {
    token.value = '';
    role.value  = '';
    readerName.value = '';
    localStorage.clear();
    router.push('/login');
  }

  return { token, role, readerName, isLoggedIn, homePath, hasRole, login, logout };
});
```

### 4.7 Axios 封装（JWT 拦截器）

```javascript
// utils/api.js
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// 请求拦截器：自动注入 JWT
http.interceptors.request.use(config => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

// 响应拦截器：401 → 登出；403 → 提示无权限
http.interceptors.response.use(
  resp => resp.data,                                     // 直接返回 { code, data, message }
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
```

### 4.8 Vite 开发配置（代理到后端）

```javascript
// vite.config.js
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': resolve(__dirname, 'src') },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8080',  // 默认开发环境，可由环境变量覆盖
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: path.resolve(__dirname, '../evolib-backend/src/main/resources/static'),  // 构建产物输出到后端 static/
    emptyOutDir: true,
  },
});
```

> **关键设计**：`vite build` 的输出目录通过 `path.resolve(__dirname, ...)` 解析为绝对路径，避免工作目录不一致导致输出目录错误；`proxy.target` 通过环境变量 `VITE_API_TARGET` 可配置，避免开发/生产/团队环境硬编码后端地址。Phase 1 部署时产物直接落到后端 `static/`，只需一个 JAR 包即可同时服务前端和 REST API。

### 4.9 页面示例：`BorrowView.vue`（馆员借书操作台）

```vue
<template>
  <AppLayout>
    <div class="borrow-page">
      <h2>借书操作台</h2>

      <!-- 输入区 -->
      <div class="borrow-form">
        <EvoInput v-model="readerId" placeholder="请输入读者借阅证号" label="读者ID" />
        <EvoInput v-model="isbn"     placeholder="请输入图书 ISBN"     label="图书ISBN" />
        <EvoButton type="primary" :loading="loading" @click="doBorrow">确认借书</EvoButton>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <!-- 借书成功后：显示当前读者在借清单 -->
      <div v-if="borrowList.length > 0" class="borrow-list">
        <h3>当前在借清单（{{ readerId }}）</h3>
        <EvoTable :columns="columns" :data="borrowList" rowKey="recordId" />
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref } from 'vue';
import { api } from '@/utils/api';
import AppLayout from '@/components/layout/AppLayout.vue';

const readerId = ref('');
const isbn     = ref('');
const loading  = ref(false);
const errorMsg = ref('');
const borrowList = ref([]);

const columns = [
  { key: 'title',       title: '书名',  width: 'auto' },
  { key: 'isbn',        title: 'ISBN',  width: '180px' },
  { key: 'borrowDate',  title: '借阅日期', width: '120px' },
  { key: 'dueDate',     title: '应还日期', width: '120px' },
];

async function doBorrow() {
  errorMsg.value = '';
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
      // 借书成功 → 刷新在借清单（UI-05 要求自动刷新）
      const listResp = await api.get(`/readers/${readerId.value.trim()}/borrows`);
      borrowList.value = listResp.data || [];
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
.borrow-page   { padding: var(--spacing-lg); }
.borrow-form   { display: flex; gap: var(--spacing-md); align-items: flex-end; margin-bottom: var(--spacing-md); }
.error-msg     { color: var(--color-error); margin: var(--spacing-sm) 0; }
.borrow-list   { margin-top: var(--spacing-lg); }
</style>
```

---

## 5. 后端架构

### 5.1 分层架构

```
┌──────────────────────────────────────────────────┐
│                   表示层 (Controller)              │
│  AuthController  BookController  BorrowController  │
│  ReaderController  AdminController                │
│                                                   │
│  职责：参数校验、JWT 角色注解校验、调用 Service、     │
│        封装统一响应体 Result<T>                     │
├──────────────────────────────────────────────────┤
│                   业务层 (Service)                  │
│  AuthService  BookService  BorrowService          │
│  ReaderService  AuditService                      │
│                                                   │
│  职责：业务逻辑编排、事务管理 @Transactional、        │
│        借阅规则判断（超期/上限/库存）、乐观锁并发控制  │
├──────────────────────────────────────────────────┤
│                   数据层 (Mapper)                   │
│  BookMapper  ReaderMapper  BorrowRecordMapper     │
│  AuditLogMapper                                   │
│                                                   │
│  职责：SQL 映射（MyBatis-Plus BaseMapper 自动 CRUD）、│
│        自定义复杂查询、分页                          │
├──────────────────────────────────────────────────┤
│                   基础设施                           │
│  Spring Security + JWT Filter                    │
│  GlobalExceptionHandler (统一异常处理)             │
│  AuditInterceptor (操作日志)                       │
│  HikariCP (连接池)                                │
└──────────────────────────────────────────────────┘
```

### 5.2 后端项目与包结构

```
evolib-backend/                          # 后端项目（Maven）
├── pom.xml
├── src/main/java/com/evolib/
│   ├── EvolibApplication.java           # Spring Boot 入口
│   │
│   ├── config/                          # 配置层
│   │   ├── SecurityConfig.java          #   Spring Security + 路径白名单 + CORS
│   │   ├── JwtProperties.java           #   JWT secret / expiration
│   │   ├── MyBatisPlusConfig.java       #   乐观锁插件、分页插件
│   │   └── WebMvcConfig.java            #   拦截器注册 + 静态资源映射
│   │
│   ├── common/                          # 公共层
│   │   ├── Result.java                  #   统一响应体 { code, message, data }
│   │   ├── ErrorCode.java               #   业务错误码枚举（4001~4009）
│   │   ├── BusinessException.java       #   业务异常
│   │   └── GlobalExceptionHandler.java  #   @RestControllerAdvice
│   │
│   ├── security/                        # 安全层
│   │   ├── JwtTokenProvider.java        #   生成/解析/校验 JWT
│   │   ├── JwtAuthenticationFilter.java #   OncePerRequestFilter
│   │   └── UserPrincipal.java           #   readerId + role
│   │
│   └── module/                          # 业务模块（按域垂直划分）
│       ├── auth/                        #   认证模块
│       │   ├── controller/AuthController.java
│       │   ├── service/AuthService.java
│       │   ├── service/impl/AuthServiceImpl.java
│       │   └── dto/LoginRequest.java, LoginResponse.java
│       │
│       ├── book/                        #   图书模块
│       │   ├── controller/BookController.java
│       │   ├── service/BookService.java
│       │   ├── service/impl/BookServiceImpl.java
│       │   ├── mapper/BookMapper.java
│       │   ├── entity/Book.java
│       │   └── dto/BookDTO.java, BookSearchRequest.java
│       │
│       ├── reader/                      #   读者模块
│       │   ├── controller/ReaderController.java
│       │   ├── service/ReaderService.java
│       │   ├── service/impl/ReaderServiceImpl.java
│       │   ├── mapper/ReaderMapper.java
│       │   ├── entity/Reader.java
│       │   └── dto/ReaderDTO.java, RegisterRequest.java
│       │
│       ├── borrow/                      #   借阅模块
│       │   ├── controller/BorrowController.java
│       │   ├── service/BorrowService.java
│       │   ├── service/impl/BorrowServiceImpl.java
│       │   ├── mapper/BorrowRecordMapper.java
│       │   ├── entity/BorrowRecord.java
│       │   └── dto/BorrowRequest.java, BorrowResponse.java, ReturnResponse.java
│       │
│       ├── admin/                       #   管理员模块
│       │   ├── controller/AdminController.java
│       │   └── service/AdminService.java
│       │
│       └── audit/                       #   审计模块
│           ├── entity/AuditLog.java
│           ├── mapper/AuditLogMapper.java
│           ├── service/AuditService.java
│           └── annotation/Auditable.java
│
└── src/main/resources/
    ├── application.yml
    ├── application-dev.yml
    └── application-prod.yml
```

### 5.3 统一响应体设计

```java
// Result.java — 所有 API 的统一外壳
public class Result<T> {
    private int code;        // 0 = 成功，非 0 = 业务错误码
    private String message;  // 提示信息
    private T data;          // 业务数据

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }
    public static <T> Result<T> fail(ErrorCode code) {
        return new Result<>(code.getCode(), code.getMessage(), null);
    }
    public static <T> Result<T> fail(ErrorCode code, String detail) {
        return new Result<>(code.getCode(), detail, null);
    }
}
```

### 5.4 事务与并发控制（借书核心流程）

```
┌──────────────────────────────────────────────────────────┐
│ BorrowService.borrow()  —  @Transactional                 │
│                                                          │
│  1. 参数校验（readerId 格式 / ISBN 格式）                   │
│  2. SELECT reader WHERE reader_id = ?                      │
│     → 不存在 → throw BusinessException(4004)               │
│  3. 检查读者超期状态                                        │
│     → 存在超期 → throw BusinessException(4001)              │
│  4. 检查借阅上限                                           │
│     → current_borrow_count >= max_borrow_count              │
│     → throw BusinessException(4002)                        │
│  5. SELECT book WHERE isbn = ? AND is_active = true         │
│     → 不存在 → throw BusinessException(4005)               │
│     → is_active=false → throw BusinessException(4008)      │
│  6. 行锁扣减库存                                            │
│     UPDATE books SET available_stock = available_stock - 1  │
│     WHERE isbn = ? AND available_stock > 0                  │
│     → affectedRows = 0 → throw BusinessException(4003)     │
│  7. 检查重复借阅 → throw BusinessException(4009)            │
│  8. INSERT borrow_record (status='BORROWED')               │
│  9. UPDATE readers SET current_borrow_count = current_borrow_count + 1 │
│ 10. INSERT audit_log                                       │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 5.5 异常处理统一流程

```
Controller 抛出异常
       │
       ▼
┌──────────────────────────┐
│ GlobalExceptionHandler    │
│ @RestControllerAdvice     │
│                          │
│ BusinessException         │
│   → Result.fail(code)    │   ---- 业务异常：显示中文错误提示
│ MethodArgumentNotValid    │
│   → Result.fail(4007)    │   ---- 参数校验失败
│ AccessDeniedException     │
│   → 403 FORBIDDEN        │   ---- 权限不足
│ Exception (兜底)          │
│   → Result.fail(500)     │   ---- 未知异常，不暴露堆栈
└──────────────────────────┘
```

### 5.6 前端错误映射表

后端返回 `{ code, message, data }`，前端 Axios 拦截器已封装解析：

| 后端业务码 | 含义 | 前端显示 |
| --- | --- | --- |
| 4001 | 超期未还 | "读者存在超期未还图书，无法借书" |
| 4002 | 达借阅上限 | "读者已达到借阅上限（3本）" |
| 4003 | 无库存 | "图书可借数量不足" |
| 4004 | 读者不存在 | "读者不存在，请检查借阅证号" |
| 4005 | 图书不存在 | "图书不存在，请检查 ISBN" |
| 4007 | 读者 ID 或 ISBN 格式不合法 | "读者ID或ISBN格式不合法" |
| 4009 | 重复借阅 | "该读者已借阅此书" |

---

## 6. 数据库设计（DDL）

```sql
-- ============================================
-- 数据库：evolib  |  引擎：PostgreSQL 14+
-- ============================================

-- 6.1 图书表
CREATE TABLE books (
    isbn             VARCHAR(20)   PRIMARY KEY,
    title            VARCHAR(200)  NOT NULL,
    author           VARCHAR(100)  NOT NULL,
    total_stock      INT           NOT NULL CHECK (total_stock >= 0),
    available_stock  INT           NOT NULL CHECK (available_stock >= 0),
    shelf_location   VARCHAR(50),
    description      TEXT,
    is_active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_books_title  ON books (title);
CREATE INDEX idx_books_author ON books (author);
CREATE INDEX idx_books_isbn   ON books (isbn);

-- 6.2 读者表
CREATE TABLE readers (
    reader_id            VARCHAR(20)   PRIMARY KEY,
    name                 VARCHAR(50)   NOT NULL,
    phone                VARCHAR(11)   NOT NULL,
    password_hash        VARCHAR(255)  NOT NULL,
    current_borrow_count INT           NOT NULL DEFAULT 0,
    max_borrow_count     INT           NOT NULL DEFAULT 3,
    role                 VARCHAR(20)   NOT NULL DEFAULT 'ROLE_READER',
    created_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_role CHECK (role IN ('ROLE_READER', 'ROLE_CIRCULATION', 'ROLE_ADMIN'))
);

-- 6.3 借阅记录表
-- 注：无 FOREIGN KEY 约束，reader_id / isbn 的关联完整性由应用层 Service 校验。
--      参见 ADR-006：数据库层不建外键，为后续微服务跨库拆分预留空间。
CREATE TABLE borrow_records (
    id           BIGSERIAL    PRIMARY KEY,
    reader_id    VARCHAR(20)  NOT NULL,   -- 应用层：借书前校验读者存在性
    isbn         VARCHAR(20)  NOT NULL,   -- 应用层：借书前校验图书存在性
    borrow_date  DATE         NOT NULL,
    due_date     DATE         NOT NULL,
    return_date  DATE,
    status       VARCHAR(10)  NOT NULL DEFAULT 'BORROWED',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_status CHECK (status IN ('BORROWED', 'RETURNED', 'OVERDUE'))
);
CREATE INDEX idx_br_reader_id ON borrow_records (reader_id);
CREATE INDEX idx_br_isbn      ON borrow_records (isbn);
CREATE INDEX idx_br_status    ON borrow_records (status);

-- 6.4 操作日志表
CREATE TABLE audit_logs (
    id           BIGSERIAL    PRIMARY KEY,
    operator_id  VARCHAR(20)  NOT NULL,
    action       VARCHAR(50)  NOT NULL,
    target       VARCHAR(50),
    detail       JSONB,
    ip_address   VARCHAR(45),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_created  ON audit_logs (created_at);
CREATE INDEX idx_audit_operator ON audit_logs (operator_id);
```

### 6.5 关联完整性：应用层校验机制

由于 MVP 阶段明确**不使用数据库外键约束**，`borrow_records.reader_id` 与 `borrow_records.isbn` 的合法性统一在 Service 层校验：

| 写入操作 | 应用层校验点 | 不通过返回 |
| --- | --- | --- |
| 新增借阅记录 | `BorrowService` 先 `SELECT` 校验 `readers.reader_id` 存在性 | `BusinessException(4004)` |
| 新增借阅记录 | `BorrowService` 先 `SELECT` 校验 `books.isbn` 存在性 | `BusinessException(4005)` |
| 还书 | `BorrowService` 校验该 `(reader_id, isbn)` 是否存在未还的借阅记录 | `BusinessException(4006)` |
| 下架图书 | `AdminService` 校验该 `isbn` 是否还有 `status='BORROWED'` 的记录 | `BusinessException(4008)` 或业务提示「请先归还全部在借图书」 |
| 重置密码 | `AdminService` 校验 `readers.reader_id` 存在性 | `BusinessException(4004)` |

> 此设计配合 ADR-006：物理外键在微服务拆分时会成为跨库障碍，且应用层校验可返回更精确的中文业务错误码（UI-04 要求）。

---

## 7. API 契约设计

### 7.1 接口总览

| 编号 | 方法 | 路径 | REQ | 权限 | 说明 |
| --- | --- | --- | --- | --- | --- |
| API-00 | POST | `/api/v1/auth/login` | REQ-00 | 无 | 登录，返回 JWT |
| API-01 | GET | `/api/v1/books/search` | REQ-01 | READER | 图书检索 |
| API-02 | GET | `/api/v1/books/{isbn}` | REQ-02 | READER | 图书详情 |
| API-03 | POST | `/api/v1/borrow-records` | REQ-03 | CIRCULATION | 借书 |
| API-04 | PUT | `/api/v1/borrow-records/{recordId}/return` | REQ-04 | CIRCULATION | 还书 |
| API-05 | GET | `/api/v1/readers/{readerId}/borrows` | REQ-05 | CIRCULATION | 在借清单 |
| API-06 | POST | `/api/v1/readers` | REQ-06 | CIRCULATION | 注册读者 |
| API-07 | PUT | `/api/v1/readers/{readerId}/phone` | REQ-07 | CIRCULATION | 修改手机号 |
| API-08 | PUT | `/api/v1/admin/readers/{readerId}/reset-password` | REQ-08 | ADMIN | 重置密码 |
| API-09 | POST | `/api/v1/admin/books` | REQ-10 | ADMIN | 上架图书 |
| API-10 | DELETE | `/api/v1/admin/books/{isbn}` | REQ-11 | ADMIN | 下架图书 |

### 7.2 前后端契约映射

```
前端 View                        后端 Controller              HTTP
───────────────────────────────────────────────────────────────────
LoginView.vue               →   AuthController            POST /auth/login
BookSearchView.vue          →   BookController            GET  /books/search
BookDetailView.vue          →   BookController            GET  /books/{isbn}
BorrowView.vue              →   BorrowController          POST /borrow-records
ReturnView.vue              →   BorrowController          PUT  /borrow-records/{id}/return
ReaderBorrowsView.vue       →   BorrowController          GET  /readers/{id}/borrows
RegisterReaderView.vue      →   ReaderController          POST /readers
(嵌入式修改手机号)             →   ReaderController          PUT  /readers/{id}/phone
AddBookView.vue             →   AdminController           POST /admin/books
RemoveBookView.vue          →   AdminController           DELETE /admin/books/{isbn}
ResetPasswordView.vue       →   AdminController           PUT  /admin/readers/{id}/reset-password
```

---

## 8. 安全架构

### 8.1 认证流程

```
┌──────────┐        ┌──────────────┐        ┌──────────────┐
│  Vue 3    │ ①POST  │ Spring Boot   │ ②SELECT│ PostgreSQL   │
│ SPA       │───────►│ AuthService   │───────►│ readers      │
│ LoginView │        │               │        │              │
│          │        │ ③bcrypt.matches(password, hash)       │
│          │        │ ④生成 JWT (readerId + role, 8h)       │
│          │◄───────│ ⑤返回 { token, role }                 │
└──────────┘        └──────────────┘        └──────────────┘
         │
         ▼ ⑥ Pinia auth store 保存 token → localStorage
┌──────────┐        ┌──────────────────┐
│ Axios     │ ⑦每次 │JwtAuthFilter     │ ⑧解析 JWT → 注入 SecurityContext
│ 拦截器    │───────►│(OncePerRequest)  │ ⑨@PreAuthorize 校验角色
│ 自动注入  │        │                  │
│ Bearer    │        │                  │
└──────────┘        └──────────────────┘
```

### 8.2 RBAC 路径权限矩阵

| 路径模式 | 角色要求 | 说明 |
| --- | --- | --- |
| `POST /api/v1/auth/login` | `permitAll()` | 登录公开 |
| `GET /api/v1/books/**` | `ROLE_READER` 及以上 | 读者检索 |
| `POST /api/v1/borrow-records` | `ROLE_CIRCULATION` | 馆员借书 |
| `PUT /api/v1/borrow-records/**/return` | `ROLE_CIRCULATION` | 馆员还书 |
| `POST /api/v1/readers` | `ROLE_CIRCULATION` | 馆员注册 |
| `PUT /api/v1/readers/**/phone` | `ROLE_CIRCULATION` | 馆员改电话 |
| `GET /api/v1/readers/**/borrows` | `ROLE_CIRCULATION` | 馆员查借阅 |
| `POST /api/v1/admin/**` | `ROLE_ADMIN` | 管理员 |
| `PUT /api/v1/admin/**` | `ROLE_ADMIN` | 管理员 |
| `DELETE /api/v1/admin/**` | `ROLE_ADMIN` | 管理员 |

---

## 9. 部署架构（Phase 1 MVP）

### 9.1 构建与部署流程

```
┌──────────────────┐     ┌──────────────────┐
│ evolib-frontend/  │     │ evolib-backend/   │
│ (Vue 3 + Vite)   │     │ (Spring Boot)    │
│                  │     │                  │
│  npm run build   │     │  mvn package     │
│       │          │     │       ▲          │
│       ▼          │     │       │          │
│  dist/ 静态文件   │────►│ static/ 目录     │
│                  │copy │                  │
└──────────────────┘     └────────┬─────────┘
                                  │
                                  ▼
                    ┌──────────────────────┐
                    │  evolib.jar          │
                    │                      │
                    │  Tomcat :8080        │
                    │  ├── /api/v1/*  API  │
                    │  └── /*         静态  │
                    └──────────┬───────────┘
                               │ JDBC
                               ▼
                    ┌──────────────────────┐
                    │  PostgreSQL :5432     │
                    └──────────────────────┘
```

**Phase 1 一键构建脚本**：

```bash
#!/bin/bash
# build.sh — 构建 Evolib MVP

echo "=== 1. 构建前端 ==="
cd evolib-frontend
npm install
npm run build                         # 输出到 ../evolib-backend/src/main/resources/static/

echo "=== 2. 构建后端 ==="
cd ../evolib-backend
mvn clean package -DskipTests

echo "=== 3. 启动 ==="
java -jar target/evolib-1.0.0.jar
# 访问 http://localhost:8080 → Vue SPA
# API 端点 http://localhost:8080/api/v1/*
```

### 9.2 开发模式

```bash
# 终端 1：启动后端（热重载需 devtools）
cd evolib-backend
mvn spring-boot:run

# 终端 2：启动前端（Vite HMR，修改即刷新）
cd evolib-frontend
npm run dev
# 浏览器打开 http://localhost:3000
# /api 请求自动代理到 localhost:8080
```

### 9.3 application.yml 关键配置

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/evolib
    username: evolib
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  mvc:
    static-path-pattern: /**            # 静态资源映射（Vue 构建产物）

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: is_active
      logic-delete-value: false
      logic-not-delete-value: true

jwt:
  secret: ${JWT_SECRET}
  expiration: 28800000   # 8h

library:
  max:
    borrow:
      days: 30
      count: 3
  default:
    password: phone-last-6
```

---

## 10. 架构演进路线图

```
Phase 1 (MVP)              Phase 2 (优化)            Phase 3 (服务化)            Phase 4 (微服务)
2026-07                    1~3个月后                 3~6个月后                   6~12个月后
    │                          │                         │                           │
    ▼                          ▼                         ▼                           ▼
┌──────────────┐         ┌────────────────┐        ┌──────────────────┐        ┌──────────────────┐
│ 单体 JAR      │         │ 前后端分离部署   │        │ 垂直拆服务         │        │ 全分布式微服务     │
│               │         │                │        │                   │        │                   │
│ Vue SPA 构建  │         │ Nginx :80      │        │ API Gateway       │        │ API Gateway       │
│ 产物 → static │   →     │  ├→ Vue 静态    │   →    │ (SC Gateway)      │   →   │ (SC Gateway)      │
│ + Spring Boot │         │  └→ API :8080  │        │  ├→ book-svc      │        │  ├→ book-service   │
│ + PostgreSQL  │         │                │        │  ├→ reader-svc    │        │  ├→ reader-service │
│               │         │ + Redis 缓存    │        │  ├→ borrow-svc    │        │  ├→ borrow-service │
│               │         │ + Element Plus │        │  └→ auth-svc      │        │  ├→ auth-service   │
│               │         │                │        │                   │        │  └→ audit-service  │
│               │         │                │        │ DB 单库分 schema  │        │                   │
│               │         │                │        │                   │        │ DB 按域独立拆分    │
└──────────────┘         └────────────────┘        └──────────────────┘        └──────────────────┘
                                                                               + Nacos (注册/配置)
                                                                               + Sentinel (限流熔断)
                                                                               + Prometheus/Grafana
                                                                               + ELK (日志)
```

### 10.1 各阶段关键动作

| 阶段 | 触发条件 | 架构动作 | 代码改动量 |
| --- | --- | --- | --- |
| **Phase 1** | MVP 上线 | Vue 3 SPA 构建到 `static/`，Spring Boot 单体 JAR | — |
| **Phase 2** | 用户量上升 / 前端需独立迭代 | 前端独立部署到 Nginx；后端开放 CORS；引入 Redis 缓存热门检索结果；可选引入 Element Plus 替换自研组件 | 改 `vite.config.js` 的 `outDir` + `proxy.target`；后端加 `spring-boot-starter-data-redis` |
| **Phase 3** | 单体性能瓶颈 / 某模块需独立扩缩 | 按 **包结构已有的业务域边界**（`module/{book,reader,borrow,auth}`）拆为 4 个独立 Spring Boot 服务；引入 Spring Cloud Gateway | 模块间 `@Autowired` → Feign；数据库分 schema |
| **Phase 4** | 多团队并行 / 弹性伸缩 / 灰度 | Nacos + Sentinel + 独立 DB + ELK + Prometheus | 基础架构搭建多，业务代码改动小（接口不变） |

### 10.2 演进友好性预埋

| 预埋设计 | 位置 | 作用 |
| --- | --- | --- |
| 前端 Views 按业务域分目录 | `src/views/{reader,circulation,admin}/` | Phase 3 可按域拆为微前端子应用 |
| CSS 变量框架无关 | `theme.css` | 引入 Element Plus 等任何 UI 库时，直接映射变量 |
| 后端包按域划分 | `com.evolib.module.{book,reader,borrow,auth}` | Phase 3 直接提 module 为独立子项目 |
| Service 接口与实现分离 | `service/` + `service/impl/` | 拆服务时接口可提取为 API 契约模块 |
| Controller 不直接注入 Mapper | Controller → Service → Mapper | Service 间调用可平滑切换为 Feign RPC |
| Vue Router 懒加载 | `() => import(...)` | 每个页面独立 chunk，首屏加载快，未来按域拆微前端时天然支持 |

---

## 11. 关键架构决策（ADR）

### ADR-001：MVP 采用 Vue 3 + Vite，不因「简单」而降级为原生 HTML/JS

- **决策**：前端使用 Vue 3 Composition API + Vite + Vue Router + Pinia 全家桶。
- **理由**：
  1. SRS 定义了 10 个页面 + 3 种角色布局 + 路由守卫 + 模态弹窗 + 表格分页等交互——原生 JS 实现这些需要手写大量 DOM 操作和状态同步代码，开发效率反而更低。
  2. Vue SFC（`.vue` 单文件组件）天然实现 template / script / style 的物理分离，比原生 HTML+JS+CSS 文件分散更符合 AP-02 原则。
  3. Vite 的 HMR 实现「修改即刷新」，开发体验远超手动刷新浏览器。
  4. Vue Router 的 `beforeEach` 守卫是实现 SRS UI-01/UI-02（未登录拦截 + 权限拦截）的最自然方式。
  5. 10 个页面规模已跨过「框架不值当」的门槛。
- **后果**：
  - ✅ 组件化开发、状态管理、路由守卫均为开箱即用。
  - ✅ Vite 构建产物经 tree-shaking 后体积小（首屏 < 200KB gzip）。
  - ⚠️ 需要 Node.js 18+ 开发环境（仅开发时需要，生产环境为静态文件）。

### ADR-002：Phase 1 前后端部署不分离，但代码工程独立

- **决策**：Vue 项目构建产物输出到 Spring Boot 的 `static/` 目录，打包为一个 JAR。但 `evolib-frontend/` 和 `evolib-backend/` 是两个独立的工程目录，各自有独立的 `package.json` / `pom.xml`。
- **理由**：MVP 阶段避免引入 Nginx 反向代理的运维复杂度；但代码边界已明确，Phase 2 只需改 `vite.config.js` 的 `outDir` 并引入 Nginx，无需修改任何业务代码。
- **后果**：✅ Phase 2 迁移成本极低（仅部署拓扑变化）。

### ADR-003：借书并发控制使用 PostgreSQL 行锁，不引入分布式锁

- **决策**：利用 PostgreSQL 的 MVCC 行级锁：`UPDATE books SET available_stock = available_stock - 1 WHERE isbn = ? AND available_stock > 0`，通过 `affectedRows=0` 判定库存不足。MVP 不引入 Redis/Redisson。
- **理由**：单数据库实例的行锁天然串行化同 ISBN 的 UPDATE，借书 QPS < 10，性能足够。
- **后果**：✅ 零外部依赖；⚠️ Phase 3 分库后需改用分布式锁（届时再引入）。

### ADR-004：操作日志使用 @Auditable 注解 + AOP，不在业务代码中手动调用

- **决策**：自定义 `@Auditable(action = "BORROW")` 注解，通过 Spring AOP 自动记录操作人、时间、IP。
- **理由**：NFR-08 要求全量审计，手动调用容易遗漏。AOP 切面统一保证覆盖所有 Controller 入口。
- **后果**：✅ 审计零遗漏，业务代码无侵入。

### ADR-005：MVP 自研组件库（EvoButton/EvoTable/EvoModal），不引入 Element Plus

- **决策**：Phase 1 自研约 6 个通用组件，引用 `theme.css` 的 CSS 变量，样式完全自主可控。
- **理由**：MVP 所需组件简单（按钮/输入框/表格/弹窗/分页/下拉框），自研成本极低（每个组件 < 50 行）。引入 Element Plus 会增加约 200KB gzip 体积，且需要额外做主题变量映射。
- **后果**：✅ 体积小、完全自主；⚠️ Phase 2 若需要复杂组件（日期选择器、树形控件等），建议引入 Element Plus，CSS 变量体系已预留在 4.3.2 节的对接方案。

---

### ADR-006：数据库层不建外键约束，关联完整性由应用层统一校验

- **决策**：所有表不创建 `FOREIGN KEY` 约束。`borrow_records.reader_id` / `isbn` 等关联字段通过 Service 层显式校验存在性（参见 6.5 节）。
- **理由**：
  1. **微服务演进**：Phase 3 拆分后，`readers` 与 `books` 可能位于不同数据库实例，物理外键无法跨库生效。提前移除外键，避免拆分前清理大量级联约束。
  2. **业务错误码精确**：数据库外键违反通常返回统一的 DB 错误，难以映射为 `4004` / `4005` 这样的中文业务提示；应用层校验可直接抛出 `BusinessException(4004/4005)`，满足 UI-04 的友好提示要求。
  3. **避免级联副作用**：数据库级联删除/更新可能导致非预期数据变化（如误删图书时连带影响历史借阅记录）。应用层控制可显式处理：图书下架仅标记 `is_active=false`，不影响历史记录。
- **实现要点**：
  - 所有写入 `borrow_records` 的地方必须前置 `SELECT` 校验 `reader_id` 和 `isbn` 存在性。
  - 所有依赖读者/图书存在性的操作（还书、重置密码、下架）在 Service 中显式校验。
  - 单元测试必须覆盖「关联不存在」的异常分支。
- **后果**：
  - ✅ 微服务跨库拆分无障碍。
  - ✅ 错误提示可控、用户友好。
  - ⚠️ 开发时需确保所有写入点都校验关联存在性；建议通过代码审查 + 单元测试覆盖，避免遗漏。

---

> 本文档为架构设计初稿，需经技术评审通过后作为开发实施依据。项目分为两个独立工程目录：`evolib-frontend/`（Vue 3 + Vite）和 `evolib-backend/`（Spring Boot 2.7.18 + Maven），共用同一个 GitHub 仓库。

---
