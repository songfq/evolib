package com.evolib.module.borrow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolib.common.Result;
import com.evolib.module.borrow.dto.BorrowRequest;
import com.evolib.module.borrow.dto.BorrowResponse;
import com.evolib.module.borrow.dto.ReturnResponse;
import com.evolib.module.borrow.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class BorrowController {
    
    private final BorrowService borrowService;
    
    @PostMapping("/borrow-records")
    @PreAuthorize("hasRole('ROLE_CIRCULATION')")
    public Result<BorrowResponse> borrow(@RequestBody @Valid BorrowRequest request) {
        log.info("借书请求: readerId={}, isbn={}", request.getReaderId(), request.getIsbn());
        BorrowResponse response = borrowService.borrow(request);
        return Result.ok(response);
    }
    
    @PutMapping("/borrow-records/{recordId}/return")
    @PreAuthorize("hasRole('ROLE_CIRCULATION')")
    public Result<ReturnResponse> returnBook(@PathVariable Long recordId) {
        log.info("还书请求: recordId={}", recordId);
        ReturnResponse response = borrowService.returnBook(recordId);
        return Result.ok(response);
    }
    
    @GetMapping("/readers/{readerId}/borrows")
    @PreAuthorize("hasRole('ROLE_CIRCULATION')")
    public Result<IPage<Object>> getBorrows(
            @PathVariable String readerId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("查询在借清单: readerId={}", readerId);
        IPage<Object> result = borrowService.getBorrowsByReader(readerId, pageNum, pageSize);
        return Result.ok(result);
    }
}