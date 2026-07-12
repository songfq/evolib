package com.evolib.module.borrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolib.common.BusinessException;
import com.evolib.common.ErrorCode;
import com.evolib.module.book.entity.Book;
import com.evolib.module.book.mapper.BookMapper;
import com.evolib.module.borrow.dto.BorrowRequest;
import com.evolib.module.borrow.dto.BorrowResponse;
import com.evolib.module.borrow.dto.ReturnResponse;
import com.evolib.module.borrow.entity.BorrowRecord;
import com.evolib.module.borrow.mapper.BorrowRecordMapper;
import com.evolib.module.borrow.service.BorrowService;
import com.evolib.module.reader.entity.Reader;
import com.evolib.module.reader.mapper.ReaderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {
    
    private final ReaderMapper readerMapper;
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowMapper;
    
    @Value("${library.max.borrow.days:30}")
    private int borrowDays;
    
    @Override
    @Transactional
    public BorrowResponse borrow(BorrowRequest request) {
        if (request.getReaderId() == null || request.getReaderId().trim().isEmpty() || request.getIsbn() == null || request.getIsbn().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM);
        }
        
        Reader reader = readerMapper.selectById(request.getReaderId());
        if (reader == null) {
            throw new BusinessException(ErrorCode.READER_NOT_FOUND);
        }
        
        int overdueCount = borrowMapper.countOverdue(request.getReaderId());
        if (overdueCount > 0) {
            throw new BusinessException(ErrorCode.OVERDUE);
        }
        
        if (reader.getCurrentBorrowCount() >= reader.getMaxBorrowCount()) {
            throw new BusinessException(ErrorCode.BORROW_LIMIT_EXCEEDED);
        }
        
        Book book = bookMapper.selectById(request.getIsbn());
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        if (!book.getIsActive()) {
            throw new BusinessException(ErrorCode.BOOK_NOT_ACTIVE);
        }
        
        int rows = bookMapper.decrementStock(request.getIsbn());
        if (rows == 0) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        
        int existing = borrowMapper.countDuplicate(request.getReaderId(), request.getIsbn());
        if (existing > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_BORROW);
        }
        
        BorrowRecord record = new BorrowRecord();
        record.setReaderId(request.getReaderId());
        record.setIsbn(request.getIsbn());
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(borrowDays));
        record.setStatus("BORROWED");
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        borrowMapper.insert(record);
        
        reader.setCurrentBorrowCount(reader.getCurrentBorrowCount() + 1);
        readerMapper.updateById(reader);
        
        log.info("借书成功: readerId={}, isbn={}, borrowId={}", request.getReaderId(), request.getIsbn(), record.getId());
        
        return new BorrowResponse(record.getId(), record.getBorrowDate(), record.getDueDate());
    }
    
    @Override
    @Transactional
    public ReturnResponse returnBook(Long recordId) {
        BorrowRecord record = borrowMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_BORROWED_BY_READER);
        }
        
        if (!"BORROWED".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_BORROWED_BY_READER);
        }
        
        LocalDate returnDate = LocalDate.now();
        int overdueDays = (int) ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
        if (overdueDays < 0) {
            overdueDays = 0;
        }
        
        record.setReturnDate(returnDate);
        record.setStatus(overdueDays > 0 ? "OVERDUE" : "RETURNED");
        record.setUpdatedAt(LocalDateTime.now());
        borrowMapper.updateById(record);
        
        bookMapper.incrementStock(record.getIsbn());
        
        Reader reader = readerMapper.selectById(record.getReaderId());
        if (reader != null) {
            reader.setCurrentBorrowCount(reader.getCurrentBorrowCount() - 1);
            readerMapper.updateById(reader);
        }
        
        log.info("还书成功: recordId={}, isbn={}, overdueDays={}", recordId, record.getIsbn(), overdueDays);
        
        return new ReturnResponse(record.getId(), returnDate, overdueDays);
    }
    
    @Override
    public IPage<Object> getBorrowsByReader(String readerId, Integer pageNum, Integer pageSize) {
        Page<BorrowRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getReaderId, readerId);
        wrapper.eq(BorrowRecord::getStatus, "BORROWED");
        wrapper.orderByDesc(BorrowRecord::getBorrowDate);
        
        IPage<BorrowRecord> recordPage = borrowMapper.selectPage(page, wrapper);
        
        return recordPage.convert(record -> {
            Map<String, Object> result = new HashMap<>();
            result.put("recordId", record.getId());
            result.put("isbn", record.getIsbn());
            result.put("borrowDate", record.getBorrowDate());
            result.put("dueDate", record.getDueDate());
            
            Book book = bookMapper.selectById(record.getIsbn());
            if (book != null) {
                result.put("title", book.getTitle());
                result.put("author", book.getAuthor());
            }
            
            return result;
        });
    }
}