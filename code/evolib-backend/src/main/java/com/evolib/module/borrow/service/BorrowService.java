package com.evolib.module.borrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolib.module.borrow.dto.BorrowRequest;
import com.evolib.module.borrow.dto.BorrowResponse;
import com.evolib.module.borrow.dto.ReturnResponse;

public interface BorrowService {
    BorrowResponse borrow(BorrowRequest request);
    ReturnResponse returnBook(Long recordId);
    IPage<Object> getBorrowsByReader(String readerId, Integer pageNum, Integer pageSize);
}