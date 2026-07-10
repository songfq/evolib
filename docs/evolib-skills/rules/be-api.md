# be-api — 后端 API 开发 SOP（7 步）

## 目标

新增一个后端 API 端点时，按以下 7 步流程操作。每一步都有对应的代码模板（`templates/be/`）和验收标准。

---

## 前置条件

加载本规则前，确保已：
- ✅ 后端项目骨架已通过 `rules/be-scaffold.md` 搭建
- ✅ 数据库表已通过 `rules/db-standards.md` 创建
- ✅ 理解 `rules/be-standards.md` 的命名/分层规范
- ✅ 理解 `rules/error.md` 的异常处理机制

---

## 标准 7 步流程

### 第 1 步：确定 REQ 编号 + 定义 DTO

根据 `docs/SRS_v1.0.md` 确定本次 API 对应的 REQ 编号，定义请求和响应 DTO。

> **为什么先写 DTO？** DTO 是前后端契约的 Java 表达。先确定数据形状（什么字段、什么类型），后续的 Service 和 Controller 都围绕 DTO 展开。

```java
// 参考模板: templates/be/dto-request.java.tmpl
// 位置: module/{域}/dto/XxxRequest.java

@Data
public class BorrowRequest {
    @NotBlank(message = "读者ID不能为空")     // ← JSR-303 校验注解，Controller 用 @Valid 触发
    private String readerId;               // 借阅证号
    
    @NotBlank(message = "ISBN不能为空")
    private String isbn;                   // 图书 ISBN
}
```

```java
// 参考模板: templates/be/dto-response.java.tmpl
// 位置: module/{域}/dto/XxxResponse.java

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowResponse {
    private Long borrowId;                 // 借阅记录 ID
    private LocalDate borrowDate;          // 借阅日期
    private LocalDate dueDate;             // 应还日期
}
```

**第 1 步验收**：
- [ ] DTO 字段与 SRS 的「输入」「输出」列一一对应
- [ ] Request DTO 上的校验注解完整（`@NotBlank` / `@NotNull` / `@Pattern`）
- [ ] 文件放在正确的 `module/{域}/dto/` 目录

---

### 第 2 步：确认 Entity 映射

检查 `module/{域}/entity/` 下是否已有对应的 Entity 类。如果没有，用 `templates/be/entity.java.tmpl` 创建。

> **为什么 Entity 和 DTO 要分开？** Entity 对应数据库表，DTO 对应 API 契约。表结构变化时只改 Entity，API 兼容性靠 DTO 层屏蔽——前端不受影响。

```java
// 参考模板: templates/be/entity.java.tmpl
// 位置: module/{域}/entity/Xxx.java

@Data
@TableName("borrow_records")              // ← MyBatis-Plus 注解：映射到数据库表
public class BorrowRecord {
    @TableId(type = IdType.AUTO)          // 自增主键
    private Long id;
    private String readerId;               // 数据库的 reader_id → 自动映射为 readerId
    private String isbn;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;                 // BORROWED / RETURNED / OVERDUE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**第 2 步验收**：
- [ ] `@TableName` 注解的值与数据库表名一致
- [ ] `@TableId(type = IdType.AUTO)` 标注主键
- [ ] 字段名（驼峰）与数据库列名（下划线）的映射关系正确

---

### 第 3 步：编写 Mapper 接口

```java
// 参考模板: templates/be/mapper.java.tmpl
// 位置: module/{域}/mapper/XxxMapper.java

/**
 * 借阅记录 Mapper — 继承 MyBatis-Plus BaseMapper 自动获得 CRUD 方法
 * 
 * 内置方法（无需手写 SQL）：
 *   insert(entity)         — 新增
 *   selectById(id)         — 按主键查
 *   selectList(wrapper)    — 条件查询
 *   updateById(entity)     — 按主键更新
 *   deleteById(id)         — 按主键删除
 * 
 * 仅当内置方法无法满足时，才在此自定义 SQL
 */
@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {
    
    // 自定义查询：检查读者是否有超期未还的图书记录
    @Select("SELECT COUNT(*) FROM borrow_records " +
            "WHERE reader_id = #{readerId} AND status = 'BORROWED' AND due_date < NOW()")
    int countOverdue(@Param("readerId") String readerId);
    
    // 自定义查询：读者当前在借数量
    @Select("SELECT COUNT(*) FROM borrow_records " +
            "WHERE reader_id = #{readerId} AND status = 'BORROWED'")
    int countCurrentBorrows(@Param("readerId") String readerId);
}
```

**第 3 步验收**：
- [ ] 继承 `BaseMapper<Entity>`
- [ ] 标注 `@Mapper`
- [ ] 自定义 SQL 使用 `@Select` / `@Update` 注解（简单查询不允许用 XML）

---

### 第 4 步：实现 Service（核心）

这是最关键的一步。Service 包含全部业务逻辑：校验、事务、审计、异常。

> **为什么借书要在 Service 中做这么多校验？** 数据库层面不建外键（`rules/db-standards.md`），所有关联完整性由 Service 校验。这样错误时能返回精确的中文错误码，且微服务拆分时不依赖物理外键。

```java
// 参考模板: templates/be/service-impl.java.tmpl
// 位置: module/{域}/service/impl/XxxServiceImpl.java

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowServiceImpl implements BorrowService {
    
    private final ReaderMapper readerMapper;
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowMapper;
    private final AuditService auditService;
    
    /**
     * 执行借书操作
     * 
     * 流程：校验读者 → 校验图书 → 扣库存 → 创建记录 → 更新计数 → 审计
     * 并发：通过 UPDATE WHERE available_stock > 0 防超卖（PostgreSQL 行锁）
     * 事务：@Transactional，任一步失败时所有数据库变更自动回滚
     * 
     * @param request 包含 readerId 和 isbn
     * @return 借阅成功回执（含借阅日期、应还日期）
     */
    @Override
    @Transactional                           // ★ 事务：全流程原子性
    @Auditable(action = "BORROW")            // ★ 审计：自动记录操作日志
    public BorrowResponse borrow(BorrowRequest request) {
        
        // Step 1: 参数校验
        if (StringUtils.isBlank(request.getReaderId()) || StringUtils.isBlank(request.getIsbn())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM);
        }
        
        // Step 2: 校验读者存在性（应用层 FK → 替代数据库外键）
        Reader reader = readerMapper.selectById(request.getReaderId());
        if (reader == null) {
            throw new BusinessException(ErrorCode.READER_NOT_FOUND);
        }
        
        // Step 3: 检查超期状态
        int overdueCount = borrowMapper.countOverdue(request.getReaderId());
        if (overdueCount > 0) {
            throw new BusinessException(ErrorCode.OVERDUE);    // 超期 → 禁止借书
        }
        
        // Step 4: 检查借阅上限
        if (reader.getCurrentBorrowCount() >= reader.getMaxBorrowCount()) {
            throw new BusinessException(ErrorCode.BORROW_LIMIT_EXCEEDED);
        }
        
        // Step 5: 校验图书存在性（应用层 FK → 替代数据库外键）
        Book book = bookMapper.selectById(request.getIsbn());
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        if (!book.getIsActive()) {
            throw new BusinessException(ErrorCode.BOOK_NOT_ACTIVE);
        }
        
        // Step 6: 行锁扣减库存（防并发超卖）
        //   UPDATE books SET available_stock = available_stock - 1
        //   WHERE isbn = ? AND available_stock > 0
        //   → affectedRows = 0 表示库存不足（被其他请求抢先或有并发）
        int rows = bookMapper.decrementStock(request.getIsbn());
        if (rows == 0) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        
        // Step 7: 防止重复借阅
        int existing = borrowMapper.countDuplicate(request.getReaderId(), request.getIsbn());
        if (existing > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_BORROW);
        }
        
        // Step 8: 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setReaderId(request.getReaderId());
        record.setIsbn(request.getIsbn());
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(30));     // 借阅天数从 application.yml 读取
        record.setStatus("BORROWED");
        borrowMapper.insert(record);
        
        // Step 9: 更新读者在借数量
        reader.setCurrentBorrowCount(reader.getCurrentBorrowCount() + 1);
        readerMapper.updateById(reader);
        
        // Step 10: 审计日志已由 @Auditable 注解 + AOP 切面自动记录
        
        // 返回
        return new BorrowResponse(record.getId(), record.getBorrowDate(), record.getDueDate());
    }
}
```

**第 4 步验收**：
- [ ] 写操作加 `@Transactional`
- [ ] 所有「关联字段」都有前置存在性校验（应用层 FK）
- [ ] 借书/还书加了 `@Auditable` 注解
- [ ] 每一步失败都抛出明确的 `BusinessException` 而非返回 null
- [ ] Service 接口已定义（`BorrowService.java`），Impl 实现它

---

### 第 5 步：编写 Controller

```java
// 参考模板: templates/be/controller.java.tmpl
// 位置: module/{域}/controller/XxxController.java

/**
 * 借阅管理 Controller
 * 
 * 为什么 Controller 这么薄？
 *   业务逻辑全部在 Service 中，Controller 只是一个"适配器"：
 *   接收 HTTP 请求 → 校验参数 → 调用 Service → 包装为 Result<T> → 返回 JSON
 *   Controller 本身不含任何业务判断。
 */
@RestController
@RequestMapping("/api/v1/borrow-records")
@RequiredArgsConstructor
@Slf4j
public class BorrowController {
    
    private final BorrowService borrowService;
    
    /**
     * 借书接口
     * POST /api/v1/borrow-records
     * 
     * @PreAuthorize 校验当前用户必须是 ROLE_CIRCULATION 角色
     * @Valid 触发 JSR-303 校验（BorrowRequest 上的 @NotBlank 等注解）
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_CIRCULATION')")
    public Result<BorrowResponse> borrow(@RequestBody @Valid BorrowRequest request) {
        log.info("借书请求: readerId={}, isbn={}", request.getReaderId(), request.getIsbn());
        BorrowResponse response = borrowService.borrow(request);
        return Result.ok(response);
    }
    
    /**
     * 还书接口
     * PUT /api/v1/borrow-records/{recordId}/return
     */
    @PutMapping("/{recordId}/return")
    @PreAuthorize("hasRole('ROLE_CIRCULATION')")
    public Result<ReturnResponse> returnBook(
            @PathVariable Long recordId,
            @RequestBody @Valid ReturnRequest request) {
        log.info("还书请求: recordId={}, isbn={}", recordId, request.getIsbn());
        ReturnResponse response = borrowService.returnBook(recordId, request);
        return Result.ok(response);
    }
}
```

**第 5 步验收**：
- [ ] `@RequestMapping` 路径符合 `rules/api-design.md` 规范
- [ ] 类或方法上有 `@PreAuthorize` 角色注解
- [ ] Request Body 参数有 `@Valid`
- [ ] 返回的是 `Result<T>` 而非裸对象
- [ ] Controller 方法不超过 15 行（核心逻辑在 Service）

---

### 第 6 步：编写单元测试

参照 `rules/unit-test.md`，为 Service 的每个分支编写测试。

**第 6 步验收**：
- [ ] 正常路径有测试
- [ ] 每种异常分支都有测试（超期/上限/无库存/读者不存在/图书不存在/重复借阅）

---

### 第 7 步：生成学习笔记

本次 API 开发完成后，生成学习笔记 `docs/learning/be/{模块}/{日期}-{功能}.md`。

格式参照 `rules/learning.md`，四个段落：
1. 做了什么（功能概述 + 涉及文件清单）
2. 代码逻辑（关键代码块 + 中文注释解释为什么这样写）
3. 前后端如何联动（数据流）
4. 关键技术点（@Transactional 原理 / 行锁原理 / AOP 审计原理等）
