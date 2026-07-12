package com.evolib.module.admin.service;

import com.evolib.module.book.dto.BookDTO;

public interface AdminService {
    BookDTO addBook(BookDTO bookDTO);
    void removeBook(String isbn);
    void resetPassword(String readerId);
}