# error — 异常处理规范

## 目标

定义 EvoLib 项目中「后端如何创建和抛出异常 → 前端如何接收和展示错误」的完整链路。

---

## 一、核心四件套

| 类 | 位置 | 职责 |
| --- | --- | --- |
| `Result<T>` | `com.evolib.common` | 统一响应体外壳 `{code, message, data}` |
| `ErrorCode` | `com.evolib.common` | 错误码枚举，定义 4001~4009 十种错误 |
| `BusinessException` | `com.evolib.common` | 业务异常类，携带 ErrorCode |
| `GlobalExceptionHandler` | `com.evolib.common` | 全局异常拦截，统一转换为 Result |

---

## 二、Result\<T\> 实现

```java
/**
 * 统一响应体 — 所有 API 返回值的包装类
 * 
 * 泛型 T 是实际业务数据的类型：
 *   Result<BorrowResponse> → data 是借阅回执
 *   Result<PageResult<BookDTO>> → data 是分页列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;        // 业务状态码：0=成功，非0=错误
    private String message;  // 提示信息：成功时 "success"，错误时 "读者不存在"
    private T data;          // 业务数据：成功时为结果对象，错误时为 null
    
    // ---- 静态工厂方法（简化创建） ----
    
    /** 成功响应 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }
    
    /** 业务异常 */
    public static <T> Result<T> fail(ErrorCode code) {
        return new Result<>(code.getCode(), code.getMessage(), null);
    }
    
    /** 业务异常（带详细信息） */
    public static <T> Result<T> fail(ErrorCode code, String detail) {
        return new Result<>(code.getCode(), detail, null);
    }
}
```

---

## 三、ErrorCode 枚举

```java
/**
 * 业务错误码枚举
 * 
 * 每个错误码的含义和触发条件：
 *   4001 — 读者有超期未还的图书记录，禁止借书
 *   4002 — 读者当前借书数已达上限（默认 3 本）
 *   4003 — 图书可借库存为 0（被借完或已下架）
 *   4004 — 输入的 readerId 在 readers 表中查不到
 *   4005 — 输入的 isbn 在 books 表中查不到
 *   4006 — 还书时发现该书不是该读者借的（读者ID + ISBN 不匹配）
 *   4007 — readerId 为空 / ISBN 为空 / 手机号格式不是 11 位
 *   4008 — 操作的图书 is_active = false（已下架）
 *   4009 — 该读者已经借了这本书且尚未归还（防止重复借同一本）
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    
    SUCCESS(0, "操作成功"),
    
    OVERDUE(4001, "读者存在超期未还图书，无法借书"),
    BORROW_LIMIT_EXCEEDED(4002, "读者已达到借阅上限"),
    STOCK_NOT_ENOUGH(4003, "图书可借数量不足"),
    READER_NOT_FOUND(4004, "读者不存在"),
    BOOK_NOT_FOUND(4005, "图书不存在"),
    NOT_BORROWED_BY_READER(4006, "该书未被该读者借阅"),
    INVALID_PARAM(4007, "参数格式不合法"),
    BOOK_NOT_ACTIVE(4008, "图书已下架，不可操作"),
    DUPLICATE_BORROW(4009, "读者已借阅此书");
    
    private final int code;
    private final String message;
}
```

---

## 四、BusinessException

```java
/**
 * 业务异常 — 在 Service 层抛出，由 GlobalExceptionHandler 统一捕获
 * 
 * 使用方式：
 *   throw new BusinessException(ErrorCode.READER_NOT_FOUND);
 *   throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
 * 
 * 不要：
 *   throw new RuntimeException("库存不足");  ← 错误信息不规范，前端无法按错误码处理
 */
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

---

## 五、GlobalExceptionHandler

```java
/**
 * 全局异常处理器
 * 
 * 四种异常分支：
 *   1. BusinessException → 返回业务错误码（4001~4009）
 *   2. MethodArgumentNotValidException → 返回 4007（参数格式不合法）
 *   3. AccessDeniedException → 返回 403（权限不足）
 *   4. Exception（兜底） → 返回 500（未知错误，不暴露堆栈）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /** 业务异常 → 返回预设的错误码和消息 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.fail(e.getErrorCode());       // 从不记日志—业务异常是正常流程的一部分
    }
    
    /** 参数校验失败 → 返回 4007 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return Result.fail(ErrorCode.INVALID_PARAM);
    }
    
    /** 权限不足 → 返回 403 */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("无权限访问: {}", e.getMessage());
        Result<Void> result = new Result<>();
        result.setCode(403);
        result.setMessage("无权限访问");
        return result;
    }
    
    /** 未知异常（兜底） → 返回 500，不暴露堆栈 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknown(Exception e) {
        log.error("未知异常", e);                   // ← 仅此处记 error 级别日志
        Result<Void> result = new Result<>();
        result.setCode(500);
        result.setMessage("系统内部错误");
        return result;
    }
}
```

---

## 六、异常抛出时机速查

| 场景 | 在哪个类抛出 | 抛出什么 |
| --- | --- | --- |
| 读者 ID 为空或 ISBN 为空 | Controller 或 Service 开头 | `BusinessException(ErrorCode.INVALID_PARAM)` |
| 校验读者不存在 | Service 中，`readerMapper.selectById()` 返回 null | `BusinessException(ErrorCode.READER_NOT_FOUND)` |
| 校验图书不存在 | Service 中，`bookMapper.selectById()` 返回 null | `BusinessException(ErrorCode.BOOK_NOT_FOUND)` |
| 借书库存扣减失败 | Service 中，`affectedRows == 0` | `BusinessException(ErrorCode.STOCK_NOT_ENOUGH)` |
| 借书时该读者存在超期记录 | Service 中，查 borrow_records 有逾期 | `BusinessException(ErrorCode.OVERDUE)` |
| 借书时 current_borrow_count >= max_borrow_count | Service 中 | `BusinessException(ErrorCode.BORROW_LIMIT_EXCEEDED)` |
| 还书时 (readerId, isbn) 无借阅记录 | Service 中 | `BusinessException(ErrorCode.NOT_BORROWED_BY_READER)` |
| 下架时 is_active=false | Service 中 | `BusinessException(ErrorCode.BOOK_NOT_ACTIVE)` |
| 借书时已借过同一本 | Service 中 | `BusinessException(ErrorCode.DUPLICATE_BORROW)` |

---

## 七、前端错误处理

前端 Axios 拦截器（`utils/api.js`）已封装统一错误处理：

```javascript
// 后端返回 { code: 4003, message: "库存不足", data: null }
// → axios 拦截器自动传递 resp.data
// → 业务代码直接判断 resp.code

async function doBorrow() {
    const resp = await api.post('/borrow-records', { readerId, isbn });
    if (resp.code === 0) {
        ElMessage.success('借书成功');
    } else {
        ElMessage.error(resp.message);   // ← 直接显示后端的中文错误消息
    }
}
```

> 说明：前端不需要维护错误码映射表——后端返回的 `message` 已经是可直接展示的中文。前端的 ElMessage 直接显示即可。
