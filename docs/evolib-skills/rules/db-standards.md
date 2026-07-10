# db-standards — 数据库设计规范

## 目标

定义 EvoLib 项目所有数据库相关操作的统一规范：表设计、命名、索引、禁止外键、DDL 变更流程。

---

## 命名规范

### 表名

| 规则 | 示例 | 说明 |
| --- | --- | --- |
| 全小写 | `books` | 不要 `Books` 或 `BOOKS` |
| 用下划线连接多单词 | `borrow_records` | 不要 `borrowRecords` |
| 使用名词复数 | `readers` | 不要 `reader` |
| 前缀统一 | 无前缀 | 不要加 `t_` / `tbl_` 前缀 |

### 字段名

| 规则 | 示例 | 说明 |
| --- | --- | --- |
| 全小写 + 下划线 | `reader_id` | 不要 `readerId` |
| 时间字段用 `_at` 结尾 | `created_at`, `updated_at` | 不要 `createTime` |
| 布尔字段用 `is_` 开头 | `is_active` | 不要 `active` |
| 外键字段用 `表名_id` | `reader_id`, `book_isbn` | 虽然数据库层面不建外键，但字段名仍要能看出关联关系 |

### 索引名

```
idx_{表名缩写}_{字段名}
```

示例：`idx_books_title`、`idx_br_reader_id`（`br` = borrow_records 的缩写）

---

## 字段类型选择

| 数据类型 | 使用场景 | PostgreSQL 类型 |
| --- | --- | --- |
| ID（字符串，人工指定） | ISBN、借阅证号 | `VARCHAR(20)` |
| ID（自增，系统生成） | borrow_records.id、audit_logs.id | `BIGSERIAL` |
| 短文本 | 姓名、手机号、角色 | `VARCHAR(50)` ~ `VARCHAR(11)` |
| 长文本 | 书名、作者 | `VARCHAR(100)` ~ `VARCHAR(200)` |
| 超长文本 | 简介 | `TEXT` |
| 整数 | 库存、数量 | `INT` |
| 布尔 | 是否激活 | `BOOLEAN` |
| 时间 | 创建/更新时间 | `TIMESTAMP` |
| 日期 | 借阅/应还日期 | `DATE` |
| 半结构化 | 审计日志详情 | `JSONB` |

---

## 核心原则：禁止外键

### 规则

**所有表不允许使用 `REFERENCES` / `FOREIGN KEY` 约束。** 关联完整性由应用层 Service 校验。

### 原因

1. **微服务演进**：Phase 3 拆分后 readers 和 books 可能在不同数据库，物理外键无法跨库
2. **错误信息精确**：应用层校验可以返回 `4004（读者不存在）` 而非数据库外键冲突的英文报错
3. **避免级联副作用**：防止误删读者时级联删除借阅记录

### 替代方案

在 Service 层显式校验（详见 `rules/be-api.md`）：

```java
// 借书前校验读者存在性 ← 替代 FOREIGN KEY (reader_id) REFERENCES readers(reader_id)
Reader reader = readerMapper.selectById(request.getReaderId());
if (reader == null) {
    throw new BusinessException(ErrorCode.READER_NOT_FOUND);  // 返回 4004
}

// 借书前校验图书存在性 ← 替代 FOREIGN KEY (isbn) REFERENCES books(isbn)
Book book = bookMapper.selectById(request.getIsbn());
if (book == null) {
    throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);    // 返回 4005
}
```

### PR 审查 Check

- [ ] DDL 中无 `REFERENCES` 关键字
- [ ] 所有写入 borrow_records 的 Service 方法均有前置存在性校验
- [ ] 所有涉及读者/图书存在的操作（还书、下架、重置密码）均有 Service 层校验

---

## 索引策略

### 必须建索引的字段

| 表 | 字段 | 原因 |
| --- | --- | --- |
| books | `title` | 模糊搜索 LIKE |
| books | `author` | 模糊搜索 LIKE |
| borrow_records | `reader_id` | 查询某读者的所有借阅记录 |
| borrow_records | `isbn` | 查询某图书是否被借阅 |
| borrow_records | `status` | 筛选在借/已还记录 |
| audit_logs | `created_at` | 按时间查审计日志 |
| audit_logs | `operator_id` | 查某操作人的日志 |

### 不需要建索引的字段

- 字段值区分度极低的（如 `role` 只有 3 种值）
- 几乎不参与 WHERE 查询的（如 `description`）

---

## DDL 变更流程

新增或修改表结构时，按以下步骤操作：

1. 在本机 PostgreSQL 中手工执行变更 SQL，验证通过
2. 将变更 SQL 保存为 `docs/migrations/V{序号}__{描述}.sql`（如 `V001__init.sql`）
3. 更新 `docs/SRS_v1.0.md` 的 §5 数据模型章节
4. 更新 `docs/ARCHITECTURE_DESIGN.md` 的 §6 DDL
5. 提交代码

---

## CHECK 约束

以下字段必须加 CHECK 约束：

| 表 | 字段 | CHECK | 原因 |
| --- | --- | --- | --- |
| books | `total_stock` | `>= 0` | 库存不能为负 |
| books | `available_stock` | `>= 0` | 可借数不能为负 |
| readers | `role` | `IN ('ROLE_READER', 'ROLE_CIRCULATION', 'ROLE_ADMIN')` | 角色只能是这三种 |
| borrow_records | `status` | `IN ('BORROWED', 'RETURNED', 'OVERDUE')` | 状态只能是这三种 |

---

## 完整 DDL 模板

使用模板 `templates/db/create-table.sql.tmpl`，该模板已包含所有约束、索引、CHECK。
