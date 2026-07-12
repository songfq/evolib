package com.evolib.module.admin.service.impl;

import com.evolib.common.BusinessException;
import com.evolib.common.ErrorCode;
import com.evolib.module.admin.service.AdminService;
import com.evolib.module.book.dto.BookDTO;
import com.evolib.module.book.entity.Book;
import com.evolib.module.book.mapper.BookMapper;
import com.evolib.module.borrow.mapper.BorrowRecordMapper;
import com.evolib.module.reader.entity.Reader;
import com.evolib.module.reader.mapper.ReaderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final BookMapper bookMapper;
    private final ReaderMapper readerMapper;
    private final BorrowRecordMapper borrowMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public BookDTO addBook(BookDTO bookDTO) {
        if (bookMapper.selectById(bookDTO.getIsbn()) != null) {
            throw new BusinessException(ErrorCode.INVALID_PARAM);
        }
        
        Book book = new Book();
        book.setIsbn(bookDTO.getIsbn());
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setTotalStock(bookDTO.getTotalStock());
        book.setAvailableStock(bookDTO.getAvailableStock());
        book.setShelfLocation(bookDTO.getShelfLocation());
        book.setDescription(bookDTO.getDescription());
        book.setIsActive(true);
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        
        bookMapper.insert(book);
        log.info("上架图书: isbn={}, title={}", bookDTO.getIsbn(), bookDTO.getTitle());
        
        return bookDTO;
    }
    
    @Override
    @Transactional
    public void removeBook(String isbn) {
        Book book = bookMapper.selectById(isbn);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        
        int borrowedCount = borrowMapper.countCurrentBorrows(isbn);
        if (borrowedCount > 0) {
            throw new BusinessException(ErrorCode.BOOK_NOT_ACTIVE);
        }
        
        book.setIsActive(false);
        book.setUpdatedAt(LocalDateTime.now());
        bookMapper.updateById(book);
        log.info("下架图书: isbn={}, title={}", isbn, book.getTitle());
    }
    
    @Override
    @Transactional
    public void resetPassword(String readerId) {
        Reader reader = readerMapper.selectById(readerId);
        if (reader == null) {
            throw new BusinessException(ErrorCode.READER_NOT_FOUND);
        }
        
        String defaultPassword = reader.getPhone().substring(reader.getPhone().length() - 6);
        reader.setPasswordHash(passwordEncoder.encode(defaultPassword));
        readerMapper.updateById(reader);
        log.info("重置密码: readerId={}", readerId);
    }
}