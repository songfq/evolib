# be-standards — 后端开发规范

## 目标

定义后端所有 Java 代码的统一规范：分包、分层、命名、格式、注释。

---

## 一、架构规范

### 1.1 分层规则

```
Controller  ──→  Service  ──→  Mapper
   │                │              │
   │  参数校验       │  业务逻辑      │  SQL
   │  权限校验       │  事务管理      │
   │  调用 Service   │  调用 Mapper   │
   │  封装 Result    │               │
   │                │              │
   ✗ 不含业务逻辑    ✗ 不含 SQL     ✗ 不含业务逻辑
   ✗ 不注入 Mapper   ✓ 可调用多个 Mapper
```

### 1.2 模块划分

包结构按**业务域**垂直划分，不要按技术层横切：

```
com.evolib.module/
├── auth/       # 认证：POST /auth/login
├── book/       # 图书：GET /books/search、GET /books/{isbn}
├── reader/     # 读者：POST /readers、PUT /readers/{id}/phone
├── borrow/     # 借阅：POST /borrow-records、PUT /borrow-records/{id}/return
├── admin/      # 管理：POST /admin/books、DELETE /admin/books/{isbn}
└── audit/      # 审计：@Auditable 注解 + AOP
```

> 好处：Phase 3 拆分微服务时，每个 `module/` 目录可以直接提升为独立子项目。

### 1.3 依赖方向

```
Controller ──→ Service 接口 ──→ ServiceImpl ──→ Mapper
                    ↑                ↑
              跨模块调用只能调接口       可以调多个 Mapper
```

**禁止**：
- Controller 直接注入 Mapper
- ServiceImpl 跨模块调用另一个 ServiceImpl（应调接口）
- Mapper 中包含业务逻辑

### 1.4 事务管理

```java
// 仅在「写操作」的 Service 方法上加 @Transactional
// 读操作（GET 请求）不加事务，减少数据库连接占用

@Transactional
public BorrowResponse borrow(BorrowRequest request) {
    // 借书流程 10 步，任一步失败自动回滚
}
```

### 1.5 并发控制（借书库存扣减）

```java
// 使用 PostgreSQL 行锁，不引入 Redis 分布式锁
// 原理：UPDATE 同一个 isbn 时，数据库行锁自动串行化

int rows = bookMapper.updateStock(request.getIsbn());  
// SQL: UPDATE books SET available_stock = available_stock - 1 
//      WHERE isbn = ? AND available_stock > 0

if (rows == 0) {
    throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);  // affectedRows=0 → 库存不足
}
```

---

## 二、编码规范

### 2.1 命名规范

| 元素 | 规则 | 示例 |
| --- | --- | --- |
| 类名 | 大驼峰，含义明确 | `BorrowController`, `BookServiceImpl` |
| 方法名 | 小驼峰，动词开头 | `listBooks()`, `createBorrow()`, `updatePhone()` |
| 变量名 | 小驼峰，含义明确 | `readerId`, `isbn`, `dueDate` |
| 常量 | 全大写+下划线 | `MAX_BORROW_COUNT`, `TOKEN_EXPIRATION` |
| 包名 | 全小写，单数 | `com.evolib.module.book.controller` |

### 2.2 类命名后缀

| 类型 | 后缀 | 示例 |
| --- | --- | --- |
| Controller | `XxxController` | `BookController` |
| Service 接口 | `XxxService` | `BookService` |
| Service 实现 | `XxxServiceImpl` | `BookServiceImpl` |
| Mapper | `XxxMapper` | `BookMapper` |
| Entity | 实体名直接使用 | `Book` |
| 请求 DTO | `XxxRequest` | `BorrowRequest`, `BookSearchRequest` |
| 响应 DTO | `XxxResponse` | `BorrowResponse`, `LoginResponse` |
| 异常类 | `XxxException` | `BusinessException` |
| 配置类 | `XxxConfig` | `SecurityConfig` |

### 2.3 注解顺序

类级别的注解按以下顺序排列：

```java
@RestController                           // 1. 标识类角色
@RequestMapping("/api/v1/books")          // 2. 路径映射
@RequiredArgsConstructor                  // 3. Lombok 构造器注入
@Slf4j                                    // 4. 日志
public class BookController {
```

### 2.4 日志规范

```java
// 使用 Lombok @Slf4j 注解，无需手动创建 Logger

// 关键操作 → info 级别
log.info("借书操作, readerId={}, isbn={}", readerId, isbn);

// 参数校验失败 → warn 级别
log.warn("借书参数为空, readerId={}, isbn={}", readerId, isbn);

// 业务异常 → 不记日志（GlobalExceptionHandler 统一处理）
// 未知异常 → error 级别（在 GlobalExceptionHandler 兜底分支记录）
```

### 2.5 依赖注入

使用 `@RequiredArgsConstructor` + `private final`（构造器注入），不用 `@Autowired` 字段注入：

```java
@RestController
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;  // ← 构造器注入，final 确保不可变
    // 不要写：
    // @Autowired
    // private BookService bookService;      ← 字段注入，不利于单元测试
}
```

### 2.6 注释规范

```java
/**
 * 执行借书操作
 * 
 * 流程：校验读者 → 校验图书 → 扣库存 → 创建借阅记录 → 记录审计日志
 * 并发：通过 UPDATE WHERE available_stock > 0 防超卖
 * 事务：@Transactional，任一步失败则全部回滚
 * 
 * @param request 包含 readerId（借阅证号）和 isbn（图书 ISBN）
 * @return 借阅成功回执（含借阅日期、应还日期）
 * @throws BusinessException 超期(4001)、上限(4002)、库存不足(4003)、读者不存在(4004)、图书不存在(4005)、重复借阅(4009)
 */
@Transactional
public BorrowResponse borrow(BorrowRequest request) {
    // Step 1: 校验参数格式
    if (StringUtils.isBlank(request.getReaderId()) || StringUtils.isBlank(request.getIsbn())) {
        throw new BusinessException(ErrorCode.INVALID_PARAM);  // 4007
    }
    
    // Step 2: 校验读者存在性（应用层 FK 校验 → 替代数据库外键）
    Reader reader = readerMapper.selectById(request.getReaderId());
    if (reader == null) {
        throw new BusinessException(ErrorCode.READER_NOT_FOUND);  // 4004
    }
    
    // Step 3: 检查超期状态
    // Step 4: 检查借阅上限
    // ...（后续步骤参照 rules/be-api.md）
}
```

---

## 三、代码模板

创建新类时，从 `templates/be/` 选择对应模板粘贴：
- `controller.java.tmpl` → Controller
- `service.java.tmpl` → Service 接口
- `service-impl.java.tmpl` → ServiceImpl
- `mapper.java.tmpl` → Mapper
- `entity.java.tmpl` → Entity
- `dto-request.java.tmpl` → 请求 DTO
- `dto-response.java.tmpl` → 响应 DTO
