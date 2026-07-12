package com.evolib.module.borrow.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BorrowRequest {
    
    @NotBlank(message = "读者ID不能为空")
    private String readerId;
    
    @NotBlank(message = "ISBN不能为空")
    private String isbn;
}