# EvoLib - 数据库设计（PostgreSQL MVP版）

> 遵循馆长建议：超期自动冻结使用数据库触发器或应用层逻辑实现。

## 1. 读者表（readers）

| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| reader_id | VARCHAR(20) | PRIMARY KEY | 借阅证号，业务主键 |
| name | VARCHAR(50) | NOT NULL | 读者姓名 |
| phone | VARCHAR(11) | NOT NULL, UNIQUE | 手机号 |
| password | VARCHAR(255) | NOT NULL | 加密后的密码 |
| borrow_count | INT | DEFAULT 0 | 当前在借数量（由触发器自动维护） |
| max_borrow_limit | INT | DEFAULT 3 | 借阅上限（暂固定） |
| is_frozen | BOOLEAN | DEFAULT FALSE | 是否被冻结（超期自动冻结） |
| created_at | TIMESTAMP | DEFAULT NOW() | 注册时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |

## 2. 图书表（books）

| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| isbn | VARCHAR(20) | PRIMARY KEY | ISBN作为业务主键 |
| title | VARCHAR(200) | NOT NULL | 书名 |
| author | VARCHAR(100) | NOT NULL | 作者 |
| total_stock | INT | NOT NULL, CHECK(>=0) | 总库存 |
| available_stock | INT | NOT NULL, CHECK(>=0) | 可借数量（由触发器自动维护） |
| location | VARCHAR(50) | | 书架位置/架位号 |
| created_at | TIMESTAMP | DEFAULT NOW() | 上架时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |

## 3. 借阅记录表（borrow_records）

| 字段名 | 类型 | 约束 | 说明 |
| :--- | :--- | :--- | :--- |
| id | BIGSERIAL | PRIMARY KEY | 自增主键 |
| reader_id | VARCHAR(20) | FOREIGN KEY (readers.reader_id) | 借阅人 |
| isbn | VARCHAR(20) | FOREIGN KEY (books.isbn) | 图书ISBN |
| borrow_date | DATE | NOT NULL | 借书日期 |
| due_date | DATE | NOT NULL | 应还日期（= borrow_date + 借阅天数） |
| return_date | DATE | | 实际还书日期（NULL表示未还） |
| overdue_days | INT | DEFAULT 0 | 超期天数（还书时自动计算） |
| status | VARCHAR(10) | DEFAULT 'BORROWED' | BORROWED / RETURNED |
| created_at | TIMESTAMP | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT NOW() | 更新时间 |

## 关键触发器/函数（PostgreSQL）

### 借书时：减少 available_stock，增加 borrow_count
```sql
-- 伪代码逻辑，实际建表时由Trae生成
-- 1. 检查 reader.is_frozen = FALSE
-- 2. 检查 reader.borrow_count < reader.max_borrow_limit
-- 3. 检查 books.available_stock > 0
-- 4. 插入 borrow_records
-- 5. UPDATE books SET available_stock = available_stock - 1
-- 6. UPDATE readers SET borrow_count = borrow_count + 1