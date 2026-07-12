package com.evolib.module.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolib.common.BusinessException;
import com.evolib.common.ErrorCode;
import com.evolib.module.book.dto.BookDTO;
import com.evolib.module.book.dto.BookSearchRequest;
import com.evolib.module.book.entity.Book;
import com.evolib.module.book.mapper.BookMapper;
import com.evolib.module.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    
    private final BookMapper bookMapper;
    
    @Override
    public BookDTO getByIsbn(String isbn) {
        Book book = bookMapper.selectById(isbn);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        return convertToDTO(book);
    }
    
    @Override
    public IPage<BookDTO> search(BookSearchRequest request) {
        Page<Book> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getIsActive, true);
        
        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = "%" + request.getKeyword() + "%";
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        
        IPage<Book> bookPage = bookMapper.selectPage(page, wrapper);
        return bookPage.convert(this::convertToDTO);
    }
    
    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setTotalStock(book.getTotalStock());
        dto.setAvailableStock(book.getAvailableStock());
        dto.setShelfLocation(book.getShelfLocation());
        dto.setDescription(book.getDescription());
        return dto;
    }
}