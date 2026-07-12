---
name: evolib-skills
description: 'EvoLib 图书馆管理 MVP 项目开发总入口 Skill。根据用户意图关键词自动识别并路由到对应的开发规则/流程——。涵盖项目搭建、API 设计、数据库规范、前后端编码、安全与异常处理、API 开发流程、组件开发流程、测试、部署、学习笔记与错误记录。'
---

# evolib-skills — EvoLib 开发 Skill 总入口

## 职责

接收用户的开发意图，识别关键词，加载对应的 `rules/*.md` 分支规则。不承载具体开发规范——具体规范一律在 `rules/` 目录下。

---

## 路由规则

匹配到关键词后，立即加载对应 `rules/` 文件，按该文件的流程执行。

### 🏗 项目搭建

| 用户意图关键词 | 加载 | 触发示例 |
| --- | --- | --- |
| 搭建/初始化/创建/脚手架 + 后端/Spring Boot/Java | `rules/be-scaffold.md` | "帮我搭建后端项目" |
| 搭建/初始化/创建/脚手架 + 前端/Vue/Vite/Element | `rules/fe-scaffold.md` | "初始化前端工程" |

### 📐 设计规范

| 用户意图关键词 | 加载 | 触发示例 |
| --- | --- | --- |
| API/接口 + 设计/规范/URL/错误码/分页/RESTful | `rules/api-design.md` | "这个接口 URL 怎么设计" |
| 数据库/表/DDL/建表/字段/索引/外键 | `rules/db-standards.md` | "borrow_records 表怎么建" |

### 📝 编码规范

| 用户意图关键词 | 加载 | 触发示例 |
| --- | --- | --- |
| 规范/标准/命名/格式/注释 + 后端/Java/Spring | `rules/be-standards.md` | "后端 Controller 怎么命名" |
| 规范/标准/命名/格式/注释 + 前端/Vue/组件 | `rules/fe-standards.md` | "Vue 组件 import 顺序是什么" |

### 🔒 横切面

| 用户意图关键词 | 加载 | 触发示例 |
| --- | --- | --- |
| 安全/登录/JWT/token/权限/角色/RBAC/认证 | `rules/security.md` | "JWT 登录怎么实现" |
| 异常/错误处理/Result/BusinessException/ErrorCode/错误码 | `rules/error.md` | "借书失败怎么返回错误" |

### 🔧 开发流程

| 用户意图关键词 | 加载 | 触发示例 |
| --- | --- | --- |
| 新增/开发/实现/写 + 接口/API/Controller/Service | `rules/be-api.md` | "我要新增一个还书接口" |
| 新增/开发/实现/写 + 页面/组件/View/前端 | `rules/fe-component.md` | "我要写一个图书检索页面" |

### 🧪 测试

| 用户意图关键词 | 加载 | 触发示例 |
| --- | --- | --- |
| 单元测试/Mockito/JUnit | `rules/unit-test.md` | "怎么写 Service 的单元测试" |
| 集成测试/全链路/端到端/E2E/验收 | `rules/integration-test.md` | "集成测试要测哪些场景" |

### 🚀 交付

| 用户意图关键词 | 加载 | 触发示例 |
| --- | --- | --- |
| 部署/打包/构建/上线/发布/Docker | `rules/deploy.md` | "怎么打包部署" |

### 📖 学习

| 用户意图关键词 | 加载 | 触发示例 |
| --- | --- | --- |
| 学习/笔记/注释/为什么/讲一下代码 | `rules/learning.md` | "这段代码为什么这样写" |
| 错误/报错/Bug/修了/修正/排查 | `rules/troubleshoot.md` | "刚才那个错误怎么修的" |

### ✅ 任务管理

| 用户意图关键词 | 加载 | 触发示例 |
| --- | --- | --- |
| 标记/更新/tasklist/完成/回滚 | `rules/task-marking.md` | "标记 Phase 1 完成" |

---

## 串联规则

当用户意图同时涉及前后端时，串联加载两个 rules：

| 触发示例 | 执行顺序 |
| --- | --- |
| "开发借书功能" | ① `rules/be-api.md`（后端接口）→ ② `rules/fe-component.md`（前端页面） |
| "实现图书检索" | 同上：先 API 后页面 |
| "完成读者注册模块" | 同上 |

---

## 回退策略

无法匹配任何关键词时，列出 15 个 rules 供用户选择：

```
无法识别你的意图。以下是可用的开发规则：

🏗 搭建：  搭建后端项目 / 初始化前端工程
📐 设计：  API 怎么设计 / 数据库表怎么建
📝 编码：  后端命名规范 / 前端编码规范
🔒 安全：  JWT 登录实现 / 权限怎么配置 / 异常怎么处理
🔧 开发：  新增接口 / 写一个页面
🧪 测试：  写单元测试 / 跑集成测试
🚀 交付：  打包部署
📖 学习：  代码学习笔记 / 错误记录
✅ 任务：  标记任务完成 / 更新 tasklist
```

---

## 模板与参考文件

- **代码模板** → `templates/` 目录（`controller.java.tmpl` / `list-page.vue.tmpl` 等）
- **速查表** → `references/` 目录（错误码 / RBAC 矩阵 / Element Plus 组件 / API 端点）
- **脚本** → `scripts/` 目录（构建 / 开发 / 备份）
