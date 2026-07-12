package com.evolib.module.book.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolib.common.Result;
import com.evolib.module.book.dto.BookDTO;
import com.evolib.module.book.dto.BookSearchRequest;
import com.evolib.module.book.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {
    
    private final BookService bookService;
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_READER')")
    public Result<IPage<BookDTO>> search(@RequestBody BookSearchRequest request) {
        log.info("图书检索: keyword={}", request.getKeyword());
        IPage<BookDTO> result = bookService.search(request);
        return Result.ok(result);
    }
    
    @GetMapping("/{isbn}")
    @PreAuthorize("hasRole('ROLE_READER')")
    public Result<BookDTO> getDetail(@PathVariable String isbn) {
        log.info("图书详情: isbn={}", isbn);
        BookDTO book = bookService.getByIsbn(isbn);
        return Result.ok(book);
    }
}