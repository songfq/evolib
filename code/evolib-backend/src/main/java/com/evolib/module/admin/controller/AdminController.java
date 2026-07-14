package com.evolib.module.admin.controller;

import com.evolib.common.Result;
import com.evolib.module.admin.service.AdminService;
import com.evolib.module.book.dto.BookDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final AdminService adminService;
    
    @PostMapping("/books")
    public Result<BookDTO> addBook(@RequestBody BookDTO bookDTO) {
        log.info("上架图书: isbn={}, title={}", bookDTO.getIsbn(), bookDTO.getTitle());
        BookDTO result = adminService.addBook(bookDTO);
        return Result.ok(result);
    }
    
    @DeleteMapping("/books/{isbn}")
    public Result<Void> removeBook(@PathVariable String isbn) {
        log.info("下架图书: isbn={}", isbn);
        adminService.removeBook(isbn);
        return Result.ok(null);
    }
    
    @PutMapping("/readers/{readerId}/reset-password")
    public Result<Void> resetPassword(@PathVariable String readerId) {
        log.info("重置密码: readerId={}", readerId);
        adminService.resetPassword(readerId);
        return Result.ok(null);
    }
}