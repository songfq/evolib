# api-design — RESTful API 设计规范

## 目标

定义前后端共同遵守的 API 契约规则：URL 怎么拼、方法怎么选、错误码怎么用、分页怎么约定。

---

## 基本原则

1. **resource 用复数名词** → `/api/v1/books`，不要 `/api/v1/getBook` 或 `/api/v1/book`
2. **一个 resource 对应一个 Controller**，功能相近的放同一个 Controller
3. **接口一经发布不可删除字段**，新增字段只能追加（保证前端兼容性）
4. **错误码不重复使用**，4001~4009 每个只表示一种错误

---

## URL 设计规则

### URL 结构

```
/api/v1/{resource}[/{id}][/{sub-resource}]
  │    │      │        │         │
  │    │      │        │         └── 子资源（如 /readers/R001/borrows）
  │    │      │        └── 资源标识符
  │    │      └── 资源名（复数，小写，用 - 连接多单词）
  │    └── 版本号
  └── 固定前缀
```

### HTTP 方法选择

| 操作 | 方法 | URL 示例 | 说明 |
| --- | --- | --- | --- |
| 查询列表 | `GET` | `/books/search?keyword=Java&type=title&page=1&size=10` | 条件用 Query 参数 |
| 查看详情 | `GET` | `/books/978-7-111-12345-6` | 标识符放在 URL 路径中 |
| 新增 | `POST` | `/readers` | 数据放在 Request Body（JSON） |
| 更新（全量） | `PUT` | `/readers/R001/phone` | |
| 更新（局部操作） | `PUT` | `/borrow-records/1001/return` | 动作放 URL 末尾 |
| 删除 | `DELETE` | `/admin/books/978-7-111-12345-6` | |

### ❌ 禁止的写法

```
GET  /api/v1/getBooks         ← 不要在 URL 中加动词 "get"
POST /api/v1/createReader     ← 不要加 "create"
GET  /api/v1/readers/search   ← search 路径可以用，但复杂查询走 Query 参数更好
```

---

## Params 参数规范

### Query 参数（GET 请求）

| 参数名 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `keyword` | string | 必填 | 搜索关键词 |
| `type` | enum | `title` | 搜索类型：`title` / `author` / `isbn` |
| `page` | int | `1` | 页码（从 1 开始） |
| `size` | int | `10` | 每页条数 |

### 路径参数

一律用 `{驼峰命名}` 包裹，如 `GET /books/{isbn}`、`PUT /readers/{readerId}/phone`

---

## 统一响应格式

所有接口的响应都必须包裹在统一外壳中：

```json
{
  "code": 0,              // 0 = 成功，其他 = 错误码
  "message": "success",   // 成功提示或错误原因
  "data": { ... }         // 实际业务数据
}
```

### 成功示例（列表）

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 25,
    "list": [
      { "isbn": "978-7-111-1", "title": "Java 编程思想", "author": "Bruce Eckel" }
    ]
  }
}
```

### 错误示例

```json
{
  "code": 4003,
  "message": "图书可借数量不足",
  "data": null
}
```

---

## 错误码体系

全部错误码定义见 `references/error-codes.md`，此处列出速查：

| 错误码 | 说明 | 触发场景 |
| --- | --- | --- |
| 4001 | 读者超期未还 | 借书时发现该读者有逾期未还图书记录 |
| 4002 | 借阅数量超限 | 借书时 current_borrow_count >= max_borrow_count（默认 3） |
| 4003 | 库存不足 | 借书时 available_stock = 0 |
| 4004 | 读者不存在 | 输入的 readerId 在 readers 表中查不到 |
| 4005 | 图书不存在 | 输入的 isbn 在 books 表中查不到 |
| 4006 | 书未被该读者借阅 | 还书时 (readerId, isbn) 没有对应的 BORROWED 记录 |
| 4007 | 参数格式不合法 | readerId 或 ISBN 为空或格式错误 |
| 4008 | 图书已下架 | 操作的图书 is_active = false |
| 4009 | 重复借阅 | 该读者已经借了这本书且尚未归还 |

---

## 分页规范

所有列表类接口统一使用以下参数和响应格式：

### 请求

```
GET /api/v1/books/search?keyword=Java&page=1&size=10
```

### 响应

```json
{
  "code": 0,
  "data": {
    "total": 25,     // 总记录数
    "list": [ ... ]  // 当前页数据
  }
}
```

> 说明：前后端用 `page`（从 1 开始）+ `size`，后端 MyBatis-Plus 的 `Page` 对象自动处理分页。

---

## 新增接口 Check List

开发新 API 前，对照以下条目自检：

- [ ] URL 使用复数资源名（`/books` 不是 `/book`）
- [ ] HTTP 方法正确（GET 查 / POST 增 / PUT 改 / DELETE 删）
- [ ] 响应包裹在 `{code, message, data}` 中
- [ ] 错误时返回业务错误码（4001~4009），不返回裸 500
- [ ] 分页接口使用 `page` + `size` + `total`
- [ ] 接口路径已在 `references/api-endpoints.md` 中登记
- [ ] 新增字段是追加不是修改（不删旧字段）
