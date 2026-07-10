# EvoLib 开发任务清单

| 项目 | 内容 |
| --- | --- |
| 项目名称 | EvoLib 图书馆管理 MVP 系统 |
| 文档版本 | 1.0 |
| 日期 | 2026年7月10日 |
| 对应架构 | [ARCHITECTURE_DESIGN.md](./ARCHITECTURE_DESIGN.md) |
| 对应需求 | [SRS_v1.0.md](./SRS_v1.0.md) |

---

## 目录

1. [任务概览](#1-任务概览)
2. [Phase 0：项目初始化与环境搭建](#2-phase-0项目初始化与环境搭建)
3. [Phase 1：数据库](#3-phase-1数据库)
4. [Phase 2：后端公共层与安全层](#4-phase-2后端公共层与安全层)
5. [Phase 3：后端业务模块](#5-phase-3后端业务模块)
6. [Phase 4：前端基础设施](#6-phase-4前端基础设施)
7. [Phase 5：前端通用组件](#7-phase-5前端通用组件)
8. [Phase 6：前端页面](#8-phase-6前端页面)
9. [Phase 7：集成测试](#9-phase-7集成测试)
10. [Phase 8：部署与交付](#10-phase-8部署与交付)
11. [依赖关系图](#11-依赖关系图)

---

## 1. 任务概览

| 统计项 | 数量 |
| --- | --- |
| 总任务数 | 50 |
| Phase 总数 | 9 |
| 后端任务 | 24 |
| 前端任务 | 19 |
| 测试 & 部署任务 | 7 |

---

## 2. Phase 0：项目初始化与环境搭建

**目标**：完成前后端工程骨架搭建，确保 `npm run dev` 和 `mvn spring-boot:run` 均可正常启动。

| ID | 任务 | 关联 REQ | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-INF-01** | **初始化 Spring Boot 2.7.18 项目** | — | — | `evolib-backend/` 目录、`pom.xml` 含 Spring Boot 2.7.18 + MyBatis-Plus 3.5.5 + jjwt 0.11.5 + PostgreSQL 驱动依赖；`mvn compile` 成功 |
| **T-INF-02** | **初始化 Vue 3 + Vite 项目** | — | — | `evolib-frontend/` 目录、`package.json` 含 vue@3、vue-router@4、pinia@2、axios@1、vite@5；`npm install && npm run dev` 成功 |
| **T-INF-03** | **配置 Vite 代理与构建输出** | — | T-INF-02 | `vite.config.js` 含：`@` 别名、`/api` 代理到 `process.env.VITE_API_TARGET`、`outDir` 用 `path.resolve(__dirname, ...)` 输出到后端 `static/` |
| **T-INF-04** | **配置 application.yml** | — | T-INF-01 | PostgreSQL 连接、JWT 配置（secret/env、8h）、借阅参数（max_days=30、max_count=3、默认密码规则）；开发/生产环境 profile 分离 |

---

## 3. Phase 1：数据库

**目标**：建库建表，插入种子数据（至少包含 3 种角色的测试账户和 5 本测试图书）。

| ID | 任务 | 关联 REQ | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-DB-01** | **执行建库建表 DDL** | — | — | PostgreSQL 数据库 `evolib` 创建；`books`、`readers`、`borrow_records`、`audit_logs` 四张表创建；表结构与架构文档 6.1~6.4 节一致，无一外键约束 |
| **T-DB-02** | **创建索引** | NFR-04 | T-DB-01 | 架构文档 6 节列出的所有索引均已创建 |
| **T-DB-03** | **插入种子数据** | — | T-DB-01 | 测试读者（ROLE_READER: R001/张三，ROLE_CIRCULATION: R002/李四，ROLE_ADMIN: R003/王五）+ 5 本测试图书；密码为 BCrypt 加密后的手机号后六位 |

---

## 4. Phase 2：后端公共层与安全层

**目标**：搭建统一响应体、异常处理、JWT 安全三大基础设施，使 `POST /auth/login` 可正常返回 JWT。

| ID | 任务 | 关联 REQ | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-BE-01** | **统一响应体 Result\<T\>** | NFR-10 | T-INF-01 | `Result<T>` 含 `code`/`message`/`data`；`Result.ok(data)` 和 `Result.fail(code, msg)` 静态方法可用 |
| **T-BE-02** | **业务错误码枚举 ErrorCode** | 附录7.1 | T-INF-01 | `ErrorCode` 枚举覆盖 4001~4009，每条含 `code` + `message` |
| **T-BE-03** | **业务异常类 BusinessException** | NFR-10 | T-BE-02 | `BusinessException(ErrorCode)` 构造，携带错误码和提示信息 |
| **T-BE-04** | **全局异常处理器 GlobalExceptionHandler** | NFR-10 | T-BE-01, T-BE-03 | `@RestControllerAdvice`；BusinessException → `Result.fail(code)`；参数校验异常 → 4007；AccessDeniedException → 403；兜底 Exception → 500 不暴露堆栈 |
| **T-BE-05** | **JWT Token 生成与解析** | REQ-00 | T-INF-04 | `JwtTokenProvider` 可生成 token（含 readerId+role，8h 有效期）、可解析并校验 token |
| **T-BE-06** | **JWT 认证过滤器** | NFR-05 | T-BE-05 | `JwtAuthenticationFilter`（OncePerRequestFilter）；从 `Authorization: Bearer xxx` 解析 token → 注入 SecurityContext |
| **T-BE-07** | **Spring Security 配置** | NFR-05, NFR-06 | T-BE-06 | `SecurityConfig`：登录 `/auth/login` 放行、`/books/**` 需 READER 及以上、`/borrow-records` + `/readers` 需 CIRCULATION、`/admin/**` 需 ADMIN；禁用 session（无状态）；BCrypt 作为密码编码器 |
| **T-BE-08** | **CORS 配置** | — | T-INF-01 | 开发阶段允许 `localhost:3000` 跨域；生产环境按需收紧 |
| **T-BE-09** | **实体类 Entity** | — | T-DB-01 | `Book`、`Reader`、`BorrowRecord`、`AuditLog` 四实体；MyBatis-Plus `@TableName` / `@TableId` 注解；字段与 DDL 一一对应 |
| **T-BE-10** | **MyBatis-Plus 配置** | — | T-INF-01 | 分页插件（PaginationInnerInterceptor）；驼峰映射；逻辑删除字段 `is_active`；开发环境 SQL 日志打印 |

---

## 5. Phase 3：后端业务模块

**目标**：按架构文档的模块划分（auth / book / reader / borrow / admin / audit），逐一实现 Controller + Service + Mapper，完成全部 11 个 API 端点。

### 5.1 认证模块（auth）

| ID | 任务 | 关联 REQ | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-BE-11** | **登录接口 POST /auth/login** | REQ-00 | T-BE-07, T-BE-09 | 接收 `{readerId, password}` → 查询 readers 表 → BCrypt 比对 → 返回 `{token, role}`；凭证错误返回「用户名或密码错误」（不暴露具体原因） |

### 5.2 图书模块（book）

| ID | 任务 | 关联 REQ | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-BE-12** | **图书检索接口 GET /books/search** | REQ-01 | T-BE-10, T-DB-02 | 接收 `keyword` + `type`(title/author/isbn) + `page` + `size` → 模糊 LIKE 查询 `is_active=true` 的图书 → 分页返回 `{total, list}`；无结果时返回空列表 |
| **T-BE-13** | **图书详情接口 GET /books/{isbn}** | REQ-02 | T-BE-10 | 返回图书完整字段（含 total_stock、available_stock、shelf_location、description）；不存在返回 4005 |

### 5.3 读者模块（reader）

| ID | 任务 | 关联 REQ | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-BE-14** | **注册读者接口 POST /readers** | REQ-06 | T-BE-10 | 接收 `{readerId, name, phone}` → 证号唯一性校验 → phone 11位格式校验 → 初始密码为手机号后六位（BCrypt 加密）→ current_borrow_count=0, max_borrow_count=3, role=ROLE_READER → 返回读者信息 + 默认密码提示 |
| **T-BE-15** | **修改手机号接口 PUT /readers/{readerId}/phone** | REQ-07 | T-BE-10, T-BE-14 | 接收 `{phone}` → 校验读者存在性 → 新手机号格式合法 → 更新 phone 字段 |

### 5.4 借阅模块（borrow）

| ID | 任务 | 关联 REQ | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-BE-16** | **借书接口 POST /borrow-records** | REQ-03 | T-BE-10, T-BE-12, T-BE-14 | `@Transactional`；完整借书流程——①校验 readerId 存在性（应用层 FK）→ ②检查超期 → ③检查借阅上限 → ④校验 isbn 存在性（应用层 FK）→ ⑤ `UPDATE SET available_stock = available_stock - 1 WHERE available_stock > 0` 行锁 → ⑥检查重复借阅 → ⑦INSERT borrow_record → ⑧UPDATE readers.current_borrow_count+1 → ⑨INSERT audit_log；任一失败回滚 + 返回对应错误码 |
| **T-BE-17** | **还书接口 PUT /borrow-records/{recordId}/return** | REQ-04 | T-BE-16 | `@Transactional`；接收 `{isbn, readerId}` → 校验该读者确实借了该书 → UPDATE borrow_record 设 return_date + status='RETURNED' → UPDATE books.available_stock+1 → UPDATE readers.current_borrow_count-1 → 若该读者已无超期记录，自动解除冻结 → INSERT audit_log；返回 `{returnDate, overdueDays}` |
| **T-BE-18** | **读者在借清单 GET /readers/{readerId}/borrows** | REQ-05 | T-BE-16 | 查询该读者所有 status='BORROWED' 的借阅记录 → 联查 books 表获取书名 → 标注超期状态（due_date < NOW()）→ 返回列表；结果为空时返回空列表 |

### 5.5 管理员模块（admin）

| ID | 任务 | 关联 REQ | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-BE-19** | **上架图书接口 POST /admin/books** | REQ-10 | T-BE-10 | 接收 `{isbn, title, author, totalStock, shelfLocation, description}` → ISBN 唯一性校验 → totalStock≥1 → available_stock=totalStock → INSERT；重复 ISBN 返回明确提示 |
| **T-BE-20** | **下架图书接口 DELETE /admin/books/{isbn}** | REQ-11 | T-BE-19 | 校验图书存在性 → 检查是否有 status='BORROWED' 的记录（应用层 FK 校验）→ 有则返回错误「请先归还全部在借图书」→ 无则 UPDATE is_active=false, available_stock=0 |
| **T-BE-21** | **重置读者密码接口 PUT /admin/readers/{readerId}/reset-password** | REQ-08 | T-BE-14 | 校验读者存在性（应用层 FK）→ 密码重置为手机号后六位（BCrypt 加密）→ 返回新密码明文（仅管理员可见） |
| **T-BE-22** | **借阅规则配置（硬编码）** | REQ-09 | T-INF-04 | `application.yml` 中 `library.max.borrow.days=30`、`library.max.borrow.count=3`、`library.default.password=phone-last-6`；Service 层通过 `@Value` 读取；重启生效 |

### 5.6 审计模块（audit）

| ID | 任务 | 关联 REQ | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-BE-23** | **@Auditable 注解** | NFR-08 | T-BE-10 | 自定义注解 `@Auditable(action)`，作用于 Controller 方法 |
| **T-BE-24** | **审计 AOP 切面** | NFR-08 | T-BE-23 | 切面拦截 `@Auditable` 方法 → 获取当前登录用户 readerId、请求 IP（`HttpServletRequest.getRemoteAddr()`）、操作类型 → 写入 audit_logs 表；日志保留至少 90 天 |

---

## 6. Phase 4：前端基础设施

**目标**：搭建 Vue 3 全家桶骨架，使 `npm run dev` 打开后可看到空白的登录页（路由 + 主题生效）。

| ID | 任务 | 关联 UI | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-FE-01** | **主题变量 theme.css** | — | T-INF-02 | `src/styles/theme.css` 定义全部 CSS 变量（主色 #1890FF、白底 #FFF、灰边框 #D9D9D9、语义色、字体、间距、圆角、阴影、尺寸）；`:root` 作用域 |
| **T-FE-02** | **公共样式 common.css** | — | T-FE-01 | `src/styles/common.css`：reset（`* { box-sizing; margin:0 }`）、body 基础排版、全局工具类 |
| **T-FE-03** | **Vue Router 路由表** | UI-01 | T-INF-02 | 10 条路由（含 `/login` + 9 个功能页面），全部 `() => import()` 懒加载；`meta.role` 标注权限；`/:pathMatch(.*)*` 兜底重定向 |
| **T-FE-04** | **路由守卫 beforeEach** | UI-01, UI-02 | T-FE-03 | 三步守卫——①公开页面放行；②未登录跳 `/login`；③角色不匹配跳对应首页 |
| **T-FE-05** | **Pinia 认证 Store** | — | T-INF-02 | `useAuthStore` 含 `token` / `role` / `readerName` 响应式状态 + `login()` / `logout()` / `hasRole()` / `homePath` 计算属性；token 持久化到 localStorage |
| **T-FE-06** | **Axios 封装 api.js** | — | T-INF-02 | `axios.create({ baseURL: '/api/v1' })`；请求拦截器自动注入 `Bearer token`；响应拦截器处理 401（登出）和 403（提示无权限） |
| **T-FE-07** | **根组件 App.vue** | — | T-INF-02 | `<router-view />` 占位，引入 `theme.css` 和 `common.css` |

---

## 7. Phase 5：前端通用组件

**目标**：完成 6 个通用 UI 组件 + 3 个布局组件，所有组件通过 `var(--xxx)` 引用主题变量。

| ID | 任务 | 关联 UI | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-FE-08** | **EvoButton 组件** | — | T-FE-01 | `type` 属性支持 primary/default/danger；`disabled` / `loading` 状态；引用 `--color-primary` 等 CSS 变量 |
| **T-FE-09** | **EvoInput 组件** | — | T-FE-01 | `v-model` 双向绑定；`placeholder` / `label` / `type` 属性；`:focus` 时边框颜色引用 `--color-primary` |
| **T-FE-10** | **EvoSelect 组件** | — | T-FE-01 | `v-model` + `options` 数组；下拉样式引用主题变量 |
| **T-FE-11** | **EvoTable 组件** | — | T-FE-01 | `columns`（{key,title,width}）+ `data` 属性；`rowKey` 可选；具名插槽 `cell-{key}` 支持自定义列渲染；表头灰底、表体白底、hover 高亮、overdue 行红底 |
| **T-FE-12** | **EvoModal 组件** | — | T-FE-01 | `visible` / `title` / `confirmText` 属性；`close` / `confirm` 事件；`<Teleport to="body">`；背景半透明遮罩 + 居中白色卡片 + 圆角阴影 |
| **T-FE-13** | **EvoPagination 组件** | — | T-FE-01 | `current` / `total` / `pageSize` 属性；`change` 事件；上一页/下一页/页码按钮 |
| **T-FE-14** | **AppHeader 组件** | — | T-FE-01 | 顶部导航栏：系统名称（左）+ 当前用户名 + 退出按钮（右）；高度 `--header-height` |
| **T-FE-15** | **AppSidebar 组件** | — | T-FE-01 | 左侧导航菜单：根据角色动态显示菜单项（读者无、馆员显示借还+注册+在借清单、管理员显示上架+下架+重置密码）；宽度 `--sidebar-width`；当前页面高亮 |
| **T-FE-16** | **AppLayout 组件** | — | T-FE-14, T-FE-15 | 通用布局壳：`<AppHeader>` + `<AppSidebar>` + `<main><slot /></main>`；body 宽度 calc(100% - var(--sidebar-width))，左偏移 var(--sidebar-width) |

---

## 8. Phase 6：前端页面

**目标**：完成全部 10 个页面，每个页面对接对应后端 API，满足 UI 交互规则。

### 8.1 未认证页面

| ID | 任务 | 关联 UI | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-FE-17** | **登录页 LoginView（P-01）** | UI-01 | T-FE-04, T-FE-05, T-FE-08, T-FE-09 | 借阅证号 + 密码输入框 + 登录按钮；调用 `authStore.login()`；失败时红色错误提示「用户名或密码错误」；成功跳转对应角色首页 |

### 8.2 读者端页面

| ID | 任务 | 关联 UI | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-FE-18** | **图书检索页 BookSearchView（P-02）** | UI-01 | T-FE-16, T-FE-08, T-FE-09, T-FE-10, T-FE-11, T-FE-13 | 关键词输入框 + 搜索类型下拉（书名/作者/ISBN）+ 搜索按钮；EvoTable 展示结果（书名/作者/ISBN/可借数量/架位号）；EvoPagination 分页；每行「查看详情」链接 → 跳转 `/reader/detail/:isbn` |
| **T-FE-19** | **图书详情页 BookDetailView（P-03）** | UI-01 | T-FE-16 | GET `/books/{isbn}` 展示全部字段（书名/作者/ISBN/总库存/可借数/架位号/简介）；底部「返回检索页」链接 |

### 8.3 馆员端页面

| ID | 任务 | 关联 UI | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-FE-20** | **借书操作台 BorrowView（P-04）** | UI-01, UI-04, UI-05 | T-FE-16, T-FE-08, T-FE-09 | 读者 ID + ISBN + 借书按钮；借书成功后自动调用 `GET /readers/{id}/borrows` 刷新在借清单（EvoTable）；失败时显示业务错误码对应中文提示 |
| **T-FE-21** | **还书操作台 ReturnView（P-05）** | UI-01, UI-04 | T-FE-16, T-FE-08, T-FE-09, T-FE-12 | 读者 ID + ISBN + 还书按钮；成功后弹窗显示「还书成功」+ 超期天数；若因超期被冻结，还书后提示「已解除借阅冻结」 |
| **T-FE-22** | **读者注册页 RegisterReaderView（P-06）** | UI-03, UI-04 | T-FE-16, T-FE-08, T-FE-09, T-FE-12 | 借阅证号 + 姓名 + 手机号 + 注册按钮；前端校验必填 + 手机号 11 位；成功后弹窗显示「注册成功」+ 默认密码（手机号后六位） |
| **T-FE-23** | **在借清单查询页 ReaderBorrowsView（P-07）** | UI-01 | T-FE-16, T-FE-09, T-FE-11, T-FE-13 | 读者 ID + 查询按钮；EvoTable 展示结果（书名/ISBN/借阅日期/应还日期）；超期行红色背景高亮 |

### 8.4 管理员端页面

| ID | 任务 | 关联 UI | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-FE-24** | **上架图书页 AddBookView（P-08）** | UI-03, UI-04 | T-FE-16, T-FE-08, T-FE-09 | ISBN/书名/作者/总库存/架位号/简介 表单；前端校验必填 + 总库存 ≥ 1；ISBN 重复时提示「该 ISBN 已存在」 |
| **T-FE-25** | **下架图书页 RemoveBookView（P-09）** | UI-04, UI-06 | T-FE-16, T-FE-08, T-FE-09, T-FE-12 | ISBN 输入框 + 查询按钮（回显图书信息）+ 确认下架按钮；点击下架弹出 EvoModal 二次确认「确认将图书[书名]下架？」；成功后提示「下架完成」 |
| **T-FE-26** | **重置密码页 ResetPasswordView（P-10）** | UI-04 | T-FE-16, T-FE-08, T-FE-09, T-FE-12 | 读者 ID + 重置按钮；成功后弹窗显示新默认密码（手机号后六位） |

---

## 9. Phase 7：集成测试

**目标**：覆盖核心业务闭环、并发、权限、错误码，确保所有 REQ 验收标准通过。

| ID | 任务 | 关联 | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-TEST-01** | **核心流程集成测试** | 全部 REQ | T-FE-26 | 测试完整故事：张三登录 → 检索「Java」→ 点击详情 → 馆员注册李四 → 管理员上架图书 → 李四借书 → 查看在借清单 → 还书 → 张三超期被拒 → 还书后解冻。全部通过 |
| **T-TEST-02** | **借书并发测试** | REQ-03 | T-BE-16 | 模拟多线程同时借最后一本书 → 仅一人成功，其余返回 4003（库存不足）；无超卖 |
| **T-TEST-03** | **角色权限测试** | NFR-06 | T-BE-07 | 读者访问馆员/管理员页面 → 跳转首页；未登录访问功能页 → 跳转登录页；携带过期 token → 401 → 登出 |
| **T-TEST-04** | **错误码覆盖测试** | 附录7.1 | T-BE-04 | 触发 4001~4009 每种错误 → 前端均显示对应中文提示，不暴露技术堆栈 |
| **T-TEST-05** | **搜索性能测试** | NFR-01, NFR-02 | T-BE-12 | 检索 95% 请求 ≤ 2s；借还书 95% 请求 ≤ 1s；并发 50 用户 |

---

## 10. Phase 8：部署与交付

**目标**：打包为一个可运行 JAR，附带部署说明。

| ID | 任务 | 关联 | 依赖 | 产出物 / 验收标准 |
| --- | --- | --- | --- | --- |
| **T-DEP-01** | **一键构建脚本 build.sh** | — | T-TEST-05 | `evolib-frontend/` → `npm run build`；`evolib-backend/` → `mvn clean package`；输出确认 |
| **T-DEP-02** | **Docker PostgreSQL 启动脚本** | — | — | 脚本/说明：`docker run postgres:14` + 创建 evolib 库 + 导入 DDL |
| **T-DEP-03** | **数据库备份脚本** | NFR-11 | T-DB-01 | `pg_dump evolib` 脚本 + crontab 每日凌晨 3 点执行 + 保留最近 7 天 |
| **T-DEP-04** | **交付物清单** | — | T-DEP-01 | `evolib-1.0.0.jar` + `application-prod.yml` 模板 + README 部署说明 |
| **T-DEP-05** | **Postman Collection** | 全部 API | T-TEST-01 | 导出为 `evolib-api.postman_collection.json`，含 11 个接口的请求/响应示例 |

---

## 11. 依赖关系图

```
Phase 0: 项目初始化
  ├── T-INF-01 (Spring Boot)
  ├── T-INF-02 (Vue 3 + Vite)
  ├── T-INF-03 (Vite 配置)
  └── T-INF-04 (application.yml)
         │
Phase 1: 数据库 ─────────────────────────────────────┐
  ├── T-DB-01 (DDL)                                 │
  ├── T-DB-02 (索引) ← T-DB-01                      │
  └── T-DB-03 (种子数据) ← T-DB-01                  │
         │                                          │
Phase 2: 后端公共层 ──────────────────────────┐      │
  ├── T-BE-01~04 (Result/Error/BizEx/Handler)  │      │
  ├── T-BE-05~07 (JWT + Filter + Security)     │      │
  ├── T-BE-08 (CORS)                          │      │
  ├── T-BE-09 (Entity) ← T-DB-01              │      │
  └── T-BE-10 (MyBatis-Plus 配置)              │      │
         │                                     │      │
Phase 3: 后端业务模块 ─────────────────────┐   │      │
  ├── auth: T-BE-11 ← T-BE-07,09          │   │      │
  ├── book: T-BE-12~13 ← T-BE-10, T-DB-02 │   │      │
  ├── reader: T-BE-14~15 ← T-BE-10         │   │      │
  ├── borrow: T-BE-16~18 ← T-BE-12,14,10  │   │      │
  ├── admin: T-BE-19~22 ← T-BE-10,14      │   │      │
  └── audit: T-BE-23~24 ← T-BE-10          │   │      │
         │                                  │   │      │
Phase 4: 前端基础设施 ──────────────┐        │   │      │
  ├── T-FE-01~02 (CSS 主题) ← T-INF-02    │   │      │
  ├── T-FE-03~04 (路由) ← T-INF-02        │   │      │
  ├── T-FE-05 (Pinia Store) ← T-INF-02    │   │      │
  ├── T-FE-06 (Axios) ← T-INF-02          │   │      │
  └── T-FE-07 (App.vue) ← T-INF-02        │   │      │
         │                                 │   │      │
Phase 5: 前端通用组件 ────────────┐        │   │      │
  ├── T-FE-08~13 (6 个 UI 组件) ← T-FE-01 │   │      │
  └── T-FE-14~16 (3 个布局组件) ← T-FE-01 │   │      │
         │                                 │   │      │
Phase 6: 前端页面 ────────┐                │   │      │
  ├── T-FE-17 (登录) ← T-FE-04,05,08,09   │   │      │
  ├── T-FE-18~19 (读者端) ← T-FE-16       │   │      │
  ├── T-FE-20~23 (馆员端) ← T-FE-16       │   │      │
  └── T-FE-24~26 (管理员) ← T-FE-16       │   │      │
         │                                 │   │      │
Phase 7: 集成测试 ──────────────────────────────┐      │
  └── T-TEST-01~05 ← Phase 3 + Phase 6 ──────┘      │
         │                                          │
Phase 8: 部署与交付 ─────────────────────────────────────┘
  └── T-DEP-01~05 ← T-TEST-05
```

---

> **建议执行顺序**：Phase 0 → Phase 1 → Phase 2 → Phase 3 + Phase 4 并行 → Phase 5 → Phase 6 → Phase 7 → Phase 8。
> 
> **并行策略**：后端开发（Phase 2 + 3）与前端开发（Phase 4 + 5）可由不同开发者在 API 契约确定后并行推进，接口联调在 Phase 6 开始阶段实施。
