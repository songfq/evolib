package com.evolib.module.book.dto;

import lombok.Data;

@Data
public class BookSearchRequest {
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}