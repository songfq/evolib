# EvoLib 业务错误码速查表

> 后端错误码定义：`com.evolib.common.ErrorCode` 枚举
> 前端使用时：`resp.code === 0` 表示成功，其他 code 直接取 `resp.message` 显示给用户

| 错误码 | 枚举常量 | 中文含义 | 触发场景 | 前端展示方式 |
| --- | --- | --- | --- | --- |
| 0 | `SUCCESS` | 操作成功 | 正常返回 | `ElMessage.success()` 或静默 |
| 4001 | `OVERDUE` | 读者存在超期未还图书，无法借书 | 借书时该读者有 `status='BORROWED' AND due_date < NOW()` 的记录 | `ElMessage.error(resp.message)` |
| 4002 | `BORROW_LIMIT_EXCEEDED` | 读者已达到借阅上限 | 借书时 `current_borrow_count >= max_borrow_count` | 同上 |
| 4003 | `STOCK_NOT_ENOUGH` | 图书可借数量不足 | 借书时 `available_stock = 0` 或并发扣减失败 | 同上 |
| 4004 | `READER_NOT_FOUND` | 读者不存在 | 输入了一个 readers 表中不存在的 readerId | 同上 |
| 4005 | `BOOK_NOT_FOUND` | 图书不存在 | 输入了一个 books 表中不存在的 isbn | 同上 |
| 4006 | `NOT_BORROWED_BY_READER` | 该书未被该读者借阅 | 还书时 (readerId, isbn) 在 borrow_records 中没有 BORROWED 记录 | 同上 |
| 4007 | `INVALID_PARAM` | 参数格式不合法 | readerId 为空 / ISBN 为空 / 手机号格式不是 11 位 | 同上 |
| 4008 | `BOOK_NOT_ACTIVE` | 图书已下架，不可操作 | 对 `is_active = false` 的书执行借阅或查询操作 | 同上 |
| 4009 | `DUPLICATE_BORROW` | 读者已借阅此书 | 借书时该读者已经借了同一本且未还 | 同上 |

---

## 新增错误码规则

1. 错误码范围：4000~4999（业务错误），500 留给系统异常
2. 新错误码编号从 4010 开始递增
3. 新增时同步更新：`ErrorCode.java` + 此文件 + `docs/SRS_v1.0.md` §7.1
