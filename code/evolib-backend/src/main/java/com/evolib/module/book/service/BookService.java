package com.evolib.module.book.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolib.module.book.dto.BookDTO;
import com.evolib.module.book.dto.BookSearchRequest;

public interface BookService {
    BookDTO getByIsbn(String isbn);
    IPage<BookDTO> search(BookSearchRequest request);
}