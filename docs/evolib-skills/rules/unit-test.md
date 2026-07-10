# unit-test — 单元测试规范

## 目标

定义 Service 层单元测试的编写标准、模板、命名规范和覆盖率要求。

---

## 依赖配置

`pom.xml` 需包含（Spring Boot 2.7.18 默认已含）：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<!-- Mockito 已包含在 spring-boot-starter-test 中，无需额外引入 -->
```

---

## 测试文件位置与命名

| 被测试类 | 测试类位置 | 测试类命名 |
| --- | --- | --- |
| `BorrowServiceImpl.java` | `src/test/java/com/evolib/module/borrow/service/impl/` | `BorrowServiceImplTest.java` |
| `BookServiceImpl.java` | `src/test/java/com/evolib/module/book/service/impl/` | `BookServiceImplTest.java` |

规则：`{被测试类名}Test.java`，放在与被测类相同的包路径下（`test/java/` 镜像 `main/java/`）

---

## 测试方法命名规范

```
should_{预期行为}_when_{触发条件}
```

示例：
- `shouldReturnBorrowResponse_when_ValidInput`
- `shouldThrowOverdueException_when_ReaderHasOverdueBooks`
- `shouldThrowStockNotEnough_when_BookAvailableStockIsZero`

---

## 三段式模板

```java
/**
 * BorrowServiceImpl 单元测试
 * 
 * 测试策略：Mock 所有 Mapper（不连真实数据库），验证 Service 的业务逻辑是否正确。
 * 为什么要 Mock Mapper？
 *   单元测试只测 Service 层的逻辑判断，不测数据库。
 *   数据库操作的正确性由集成测试（rules/integration-test.md）保证。
 */
@ExtendWith(MockitoExtension.class)                       // ★ 启用 Mockito
class BorrowServiceImplTest {

    @Mock
    private ReaderMapper readerMapper;                     // Mock：假的 ReaderMapper

    @Mock
    private BookMapper bookMapper;                         // Mock：假的 BookMapper

    @Mock
    private BorrowRecordMapper borrowMapper;               // Mock：假的 BorrowRecordMapper

    @Mock
    private AuditService auditService;                     // Mock：假的 AuditService

    @InjectMocks                                            // ★ 将 Mock 注入到被测对象中
    private BorrowServiceImpl borrowService;

    // ==================== 正常路径测试 ====================

    @Test
    @DisplayName("借书成功 — 正常借出一本书")
    void shouldReturnBorrowResponse_when_ValidInput() {
        // 1. Arrange — 准备测试数据、设定 Mock 行为
        BorrowRequest request = new BorrowRequest("R001", "978-7-111-1");
        
        Reader reader = new Reader();
        reader.setReaderId("R001");
        reader.setCurrentBorrowCount(0);
        reader.setMaxBorrowCount(3);
        
        Book book = new Book();
        book.setIsbn("978-7-111-1");
        book.setIsActive(true);

        when(readerMapper.selectById("R001")).thenReturn(reader);   // ← Mock：查读者返回 reader
        when(borrowMapper.countOverdue("R001")).thenReturn(0);     // ← Mock：无超期记录
        when(bookMapper.selectById("978-7-111-1")).thenReturn(book); // ← Mock：查图书返回 book
        when(bookMapper.decrementStock("978-7-111-1")).thenReturn(1); // ← Mock：扣库存成功
        when(borrowMapper.countDuplicate("R001", "978-7-111-1")).thenReturn(0); // ← Mock：无重复借阅

        // 2. Act — 执行被测方法
        BorrowResponse response = borrowService.borrow(request);

        // 3. Assert — 验证结果
        assertNotNull(response);                          // 应返回非空结果
        assertNotNull(response.getDueDate());             // 应有应还日期
        verify(borrowMapper).insert(any());               // ← 验证确实调了 insert
        verify(readerMapper).updateById(any());           // ← 验证确实更新了读者
    }

    // ==================== 异常路径测试 ====================

    @Test
    @DisplayName("读者不存在 — 应抛出 4004 异常")
    void shouldThrowReaderNotFound_when_ReaderIdNotExist() {
        // Arrange — 设定查读者返回 null（不存在）
        when(readerMapper.selectById("R999")).thenReturn(null);

        // Act & Assert — 验证抛出正确的异常
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            borrowService.borrow(new BorrowRequest("R999", "978-7-111-1"));
        });
        assertEquals(4004, ex.getErrorCode().getCode());  // 错误码应为 4004
        assertEquals("读者不存在", ex.getErrorCode().getMessage());
    }

    @Test
    @DisplayName("借阅超期 — 应抛出 4001 异常")
    void shouldThrowOverdue_when_ReaderHasOverdueBooks() {
        Reader reader = new Reader();
        reader.setCurrentBorrowCount(0);
        reader.setMaxBorrowCount(3);

        when(readerMapper.selectById("R001")).thenReturn(reader);
        when(borrowMapper.countOverdue("R001")).thenReturn(1);  // ← 有 1 条超期记录

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            borrowService.borrow(new BorrowRequest("R001", "978-7-111-1"));
        });
        assertEquals(4001, ex.getErrorCode().getCode());
    }

    @Test
    @DisplayName("库存不足 — 应抛出 4003 异常")
    void shouldThrowStockNotEnough_when_UpdateReturnsZero() {
        // Arrange — 设定所有前置校验通过，但库存扣减返回 0（无库存）
        when(readerMapper.selectById("R001")).thenReturn(new Reader());
        when(borrowMapper.countOverdue("R001")).thenReturn(0);
        
        Book book = new Book();
        book.setIsActive(true);
        when(bookMapper.selectById("978-7-111-1")).thenReturn(book);
        when(bookMapper.decrementStock("978-7-111-1")).thenReturn(0);  // ← 扣库存失败

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            borrowService.borrow(new BorrowRequest("R001", "978-7-111-1"));
        });
        assertEquals(4003, ex.getErrorCode().getCode());
    }
}
```

---

## 覆盖率要求

| 指标 | 目标值 |
| --- | --- |
| 行覆盖率 | ≥ 70% |
| 分支覆盖率 | ≥ 60% |
| 核心模块（borrow / auth） | ≥ 85% |

> 说明：覆盖率不是目的，覆盖逻辑分支才是。每个 `if / throw / return` 都应该有一个对应的测试用例。

---

## 应该测什么 vs 不该测什么

| 应该测 | 不该测 |
| --- | --- |
| Service 层的业务判断逻辑 | Mapper 的 SQL（那是 MyBatis-Plus 的事） |
| 正常输入 → 正确输出 | Spring Boot 自动配置 |
| 异常输入 → 正确异常类型 + 错误码 | 数据库连接 |
| 边界值（如库存 = 1、借阅数 = 2） | Controller 的 HTTP 参数绑定 |

---

## 运行测试

```bash
# 运行全部单元测试
mvn test

# 运行指定测试类
mvn test -Dtest=BorrowServiceImplTest

# 运行指定测试方法
mvn test -Dtest=BorrowServiceImplTest#shouldReturnBorrowResponse_when_ValidInput
```
