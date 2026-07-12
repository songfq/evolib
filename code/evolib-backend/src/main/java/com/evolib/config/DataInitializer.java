package com.evolib.config;

import com.evolib.module.audit.entity.AuditLog;
import com.evolib.module.audit.mapper.AuditLogMapper;
import com.evolib.module.book.entity.Book;
import com.evolib.module.book.mapper.BookMapper;
import com.evolib.module.reader.entity.Reader;
import com.evolib.module.reader.mapper.ReaderMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BookMapper bookMapper;
    private final ReaderMapper readerMapper;
    private final AuditLogMapper auditLogMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(BookMapper bookMapper, ReaderMapper readerMapper,
                           AuditLogMapper auditLogMapper, PasswordEncoder passwordEncoder) {
        this.bookMapper = bookMapper;
        this.readerMapper = readerMapper;
        this.auditLogMapper = auditLogMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (bookMapper.selectCount(null) == 0) {
            initBooks();
        }
        if (readerMapper.selectCount(null) == 0) {
            initReaders();
        }
        if (auditLogMapper.selectCount(null) == 0) {
            initAuditLogs();
        }
    }

    private void initBooks() {
        Book[] books = {
            new Book("978-7-5327-8000-1", "Java编程思想", "Bruce Eckel", 5, 5, "A区-1排-01", "Java学习经典著作"),
            new Book("978-7-115-42857-7", "Vue.js设计与实现", "霍春阳", 3, 3, "A区-2排-05", "深入理解Vue.js内部机制"),
            new Book("978-7-111-49645-6", "深入理解计算机系统", "Randal E.Bryant", 2, 2, "B区-1排-03", "计算机系统导论"),
            new Book("978-7-115-36530-7", "图解HTTP", "上野宣", 4, 4, "B区-2排-08", "HTTP协议入门"),
            new Book("978-7-5086-6033-0", "思考，快与慢", "Daniel Kahneman", 3, 3, "C区-1排-02", "行为经济学经典")
        };
        Arrays.stream(books).forEach(book -> {
            book.setIsActive(true);
            book.setCreatedAt(LocalDateTime.now());
            book.setUpdatedAt(LocalDateTime.now());
            bookMapper.insert(book);
        });
    }

    private void initReaders() {
        Reader reader1 = new Reader();
        reader1.setReaderId("R001");
        reader1.setName("张三");
        reader1.setPhone("13800138001");
        reader1.setPasswordHash(passwordEncoder.encode("138001"));
        reader1.setCurrentBorrowCount(0);
        reader1.setMaxBorrowCount(3);
        reader1.setRole("ROLE_READER");
        reader1.setCreatedAt(LocalDateTime.now());
        readerMapper.insert(reader1);

        Reader reader2 = new Reader();
        reader2.setReaderId("R002");
        reader2.setName("李四");
        reader2.setPhone("13800138002");
        reader2.setPasswordHash(passwordEncoder.encode("138002"));
        reader2.setCurrentBorrowCount(0);
        reader2.setMaxBorrowCount(3);
        reader2.setRole("ROLE_CIRCULATION");
        reader2.setCreatedAt(LocalDateTime.now());
        readerMapper.insert(reader2);

        Reader reader3 = new Reader();
        reader3.setReaderId("R003");
        reader3.setName("王五");
        reader3.setPhone("13800138003");
        reader3.setPasswordHash(passwordEncoder.encode("138003"));
        reader3.setCurrentBorrowCount(0);
        reader3.setMaxBorrowCount(3);
        reader3.setRole("ROLE_ADMIN");
        reader3.setCreatedAt(LocalDateTime.now());
        readerMapper.insert(reader3);
    }

    private void initAuditLogs() {
        AuditLog log = new AuditLog();
        log.setOperatorId("SYSTEM");
        log.setAction("INIT");
        log.setTarget("SYSTEM");
        log.setDetail("{\"message\": \"系统初始化完成\"}");
        log.setIpAddress("127.0.0.1");
        log.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
    }
}