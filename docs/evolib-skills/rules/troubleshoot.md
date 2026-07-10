# troubleshoot — 错误修正日志规范

## 目标

定义开发过程中遇到的所有错误（含环境、编译、运行、逻辑错误）的记录格式和索引方式。每次修正后追加一条，方便后续遇到同类问题时快速定位。

---

## 日志存放

单文件 `docs/learning/ERROR_JOURNAL.md`，所有错误都追加到此文件。

**追加规则**：最新错误在最前面（倒序），自动维护目录锚点。

---

## 日志模板（五段落固定格式）

```markdown
### {日期} — {错误简述}

---

#### 一、错误现象

（报错原文 / 截图描述 / 用户操作行为描述）

示例：
```
启动 Spring Boot 时报错：
org.postgresql.util.PSQLException: FATAL: database "evolib" does not exist
```

---

#### 二、根因分析

（为什么出错——是缺少了什么、配错了什么、顺序错了什么）

示例：
- 根因：PostgreSQL 中还没有创建 evolib 数据库
- 为什么：启动 Spring Boot 时，application.yml 中的 datasource.url 指向了不存在的数据库
- 为什么之前没发现：Docker PostgreSQL 容器启动后只创建了默认数据库，没有手动执行 `CREATE DATABASE evolib`

---

#### 三、修正方案

（具体改了什么文件、改前改后对比）

| 文件 | 改前 | 改后 |
| --- | --- | --- |
| 数据库 | 不存在 evolib 库 | `CREATE DATABASE evolib;` |
| 文档 | 无 Docker 启动说明 | 在 deploy.md 中新增 Docker 快速启动命令 |

修正步骤：
1. `docker exec -it evolib-db psql -U evolib -c "CREATE DATABASE evolib;"`
2. 重启 Spring Boot → 启动成功

---

#### 四、如何避免

（下次开发时的预防 Check）

- [ ] 新环境首次启动前，确认数据库已创建
- [ ] 执行 `psql -l` 列出所有数据库，确认 evolib 存在
- [ ] Docker Compose 方案中加 `POSTGRES_DB=evolib` 环境变量，自动创建

---

#### 五、关联学习笔记

- 无（环境问题） / 见 `docs/learning/be/auth/2026-07-10-JWT登录接口.md`
```

---

## 错误分类索引

文件顶部维护以下索引（每次追加新条目后更新关键词）：

```markdown
# 错误修正日志

> 按关键词索引：  
> **数据库**：#1 #5 | **Spring Boot**：#1 #3 | **Maven**：#2 | **Vue**：#4 | **Element Plus**：#4 | **JWT**：#3 | **npm**：#4
```

---

## 记录范围（全包含）

| 错误类型 | 是否记录 | 示例 |
| --- | --- | --- |
| 环境问题 | ✅ | PostgreSQL 未启动、数据库不存在、JDK 版本不匹配 |
| 依赖问题 | ✅ | Maven 依赖冲突、npm install 失败、版本不兼容 |
| 编译错误 | ✅ | 缺少 import、类找不到、语法错误 |
| 运行时错误 | ✅ | NPE、SQL 报错、端口占用 |
| 逻辑错误 | ✅ | 借书没有校验库存就扣减、还书没有解冻 |
| 配置错误 | ✅ | application.yml 格式错误、环境变量未设置 |
| 前端错误 | ✅ | 路由 404、Axios 跨域、组件属性写错 |
| 测试错误 | ✅ | Mock 不生效、断言写反、覆盖率不够 |

---

## 生成时机

每次遇到错误 → 修正 → 验证通过后，由当前 Skill 自动追加一条到 `ERROR_JOURNAL.md`。

> 注意：同一个错误只记录一次（通过搜索现有条目避免重复）。如果修正多次才解决，记录最终修正方案即可。
