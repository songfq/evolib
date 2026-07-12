# learning — 学习笔记生成规范

## 目标

定义每次开发完成后的学习笔记格式、存放位置、代码注释标准。面向初学者——解释清楚「做了什么 + 为什么这样做 + 用到了什么技术」。

---

## 笔记存放位置

```
docs/learning/
├── ERROR_JOURNAL.md                     # 错误修正日志（持续追加，见 troubleshoot.md）
│
├── be/                                  # 后端学习笔记（功能开发）
│   ├── auth/
│   │   └── 2026-07-10-JWT登录接口.md
│   ├── book/
│   │   └── 2026-07-10-图书检索分页接口.md
│   ├── borrow/
│   │   └── 2026-07-10-借书事务并发控制.md
│   └── ...
│
├── fe/                                  # 前端学习笔记（功能开发）
│   ├── login/
│   │   └── 2026-07-10-登录页ElementPlus布局.md
│   ├── circulation/
│   │   └── 2026-07-10-借书操作台页面对接API.md
│   └── ...
│
└── general/                             # 通用学习笔记（框架原理、前后端交互等跨模块内容）
    ├── 2026-07-12-前后端交互原理与案例详解.md
    └── ...
```

文件命名规则：`{YYYY-MM-DD}-{功能简述}.md`

---

## 笔记类型与格式说明

### 功能开发笔记（be/、fe/ 目录）

每次完成具体功能开发后生成，**必须**遵循四段落固定格式。

### 框架级概览文档（general/ 目录）

用于系统讲解框架原理、技术架构、前后端交互等跨模块内容，**可以使用自定义结构**，但需包含以下核心要素：

- **原理讲解**：这是什么？为什么这么做？凭什么这么做？
- **技术实现**：核心组件、关键代码、配置说明
- **案例分析**：简单案例 + 复杂案例，详细讲解完整流程
- **核心要点总结**：技术点、选择理由、凭什么这么做

---

## 笔记模板（四段落固定格式）

```markdown
# {日期} — {功能名称}

---

## 一、做了什么

- 功能描述：（一句话说清楚这个功能是干什么的）
- 涉及文件：（列出本次新增/修改的所有文件路径）
- 对应需求：REQ-XX / P-XX

---

## 二、代码逻辑

### 文件 1: `XxxService.java`

\```java
// ← 每行代码都有中文注释，解释「为什么这样写」
// ← 关键逻辑块用 Step 1/2/3 标注
// ← 技术选���用「原理：xxx」注释说明
\```

> **为什么用 @Transactional？** 
> （解释事务的作用——借书涉及 INSERT + UPDATE + UPDATE 三条 SQL，任一条失败需要全部回滚）

### 文件 2: `XxxController.java`

\```java
// ...
\```

> **为什么 Controller 这么薄？**
> （解释分层架构的好处——业务逻辑集中在 Service，Controller 只是适配器）

---

## 三、前后端如何联动

用文字描述数据流：

```
用户操作（点击借书按钮）
  → 前端 BorrowView.vue 的 doBorrow() 方法
  → Axios POST /api/v1/borrow-records，自动附带 JWT token
  → 后端 JwtAuthenticationFilter 拦截、校验 token、注入 SecurityContext
  → SecurityConfig 校验角色（ROLE_CIRCULATION）
  → BorrowController.borrow() 接收请求、@Valid 校验参数
  → BorrowService.borrow() 执行借书 10 步流程（事务保护）
  → 返回 {code:0, data:{borrowId, dueDate}}
  → 前端 Axios 拦截器自动解包 resp.data
  → ElMessage.success("借书成功") 提示用户
```

---

## 四、关键技术点

逐个列出本次用到的关键技术和选择理由：

| 技术点 | 是什么 | 为什么用 |
| --- | --- | --- |
| `@Transactional` | Spring 声明式事务 | 多条 SQL 要么全成功要么全回滚，避免数据不一致 |
| `UPDATE WHERE available_stock > 0` | PostgreSQL 行锁 | 替代分布式锁，单机下防超卖并发限制 |
| `@PreAuthorize` | Spring Security 方法级权限 | 一行注解完成角色校验，不用在代码里写 if-else |
| `ElMessage.error()` | Element Plus 消息提示 | 一行代码弹出错误提示，不用自己写 Toast 组件 |
| `v-model` | Vue 双向绑定 | 输入框的值自动同步到 JS 变量，不用写 onInput 事件 |
```

---

## 代码注释标准

### 后端 Java 注释

每个公共方法上方必须有 Javadoc：

```java
/**
 * 执行借书操作
 * 
 * 流程：校验读者 → 校验图书 → 扣库存 → 创建记录 → 更新计数 → 审计
 * 并发：通过 UPDATE WHERE available_stock > 0 行锁防超卖
 * 事务：@Transactional，任一步失败全部回滚
 * 
 * @param request 包含 readerId（借阅证号）和 isbn（图书 ISBN）
 * @return 借阅成功回执（含 borrowId、borrowDate、dueDate）
 * @throws BusinessException 超期(4001) / 上限(4002) / 库存不足(4003) / 读者不存在(4004) / 图书不存在(4005) / 重复借阅(4009)
 */
```

复杂逻辑块每步标注：

```java
// Step 1: 参数校验 — 确保前端传来的数据不为空
// Step 2: 校验读者存在性 — 替代数据库外键，返回精确错误码 4004
// Step 3: 检查超期状态 — 查询 borrow_records 中该读者的逾期记录数
```

### 前端 Vue 注释

```vue
<script setup>
// ① 导入依赖 — Vue API / Element Plus / 自有模块
import { ref } from 'vue';                 // ref：让普通变量变成响应式的
import { ElMessage } from 'element-plus';  // Element Plus 消息提示（成功/错误/警告）
import { api } from '@/utils/api';         // Axios 封装（自动带 JWT token）

// ② 响应式状态 — 数据变了，页面自动更新
const keyword = ref('');                   // v-model 绑定到搜索输入框
const tableData = ref([]);                 // v-loading 绑定到表格数据

// ③ 业务方法 — 点击按钮时执行
async function doSearch() {
  // 原理：axios 自动在请求头加 Authorization: Bearer {token}
  //       后端返回 {code, data, message}，axios 拦截器自动解包为 resp = {code, data, message}
  const resp = await api.get('/books/search', { params: { keyword: keyword.value } });
}
</script>
```
