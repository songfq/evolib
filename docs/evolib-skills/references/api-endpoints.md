# EvoLib API 端点速查表

> 参考：`docs/ARCHITECTURE_DESIGN.md` §7.1

| # | 方法 | 完整路径 | 对应 REQ | 权限 | 前端页面 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| API-00 | POST | `/api/v1/auth/login` | REQ-00 | 无 | P-01 登录 | 登录，返回 JWT + role |
| API-01 | GET | `/api/v1/books/search` | REQ-01 | READER+ | P-02 检索 | 分页搜索：keyword/type/page/size |
| API-02 | GET | `/api/v1/books/{isbn}` | REQ-02 | READER+ | P-03 详情 | 返回图书全部字段 |
| API-03 | POST | `/api/v1/borrow-records` | REQ-03 | CIRCULATION | P-04 借书 | 借书申请：readerId + isbn |
| API-04 | PUT | `/api/v1/borrow-records/{recordId}/return` | REQ-04 | CIRCULATION | P-05 还书 | 还书：isbn + readerId |
| API-05 | GET | `/api/v1/readers/{readerId}/borrows` | REQ-05 | CIRCULATION | P-07 在借清单 | 返回该读者所有在借图书 |
| API-06 | POST | `/api/v1/readers` | REQ-06 | CIRCULATION | P-06 注册 | 注册新读者 |
| API-07 | PUT | `/api/v1/readers/{readerId}/phone` | REQ-07 | CIRCULATION | P-06 嵌入 | 修改手机号 |
| API-08 | PUT | `/api/v1/admin/readers/{readerId}/reset-password` | REQ-08 | ADMIN | P-10 重置密码 | 重置为默认密码 |
| API-09 | POST | `/api/v1/admin/books` | REQ-10 | ADMIN | P-08 上架 | 上架新图书 |
| API-10 | DELETE | `/api/v1/admin/books/{isbn}` | REQ-11 | ADMIN | P-09 下架 | 逻辑删除（is_active=false） |

**约定**：
- 基础路径：`/api/v1`
- 请求/响应格式：JSON
- 认证方式：`Authorization: Bearer {jwt_token}`
- 统一响应体：`{ "code": 0, "message": "success", "data": {...} }`
