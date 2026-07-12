package com.evolib.module.borrow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("borrow_records")
public class BorrowRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String readerId;
    
    private String isbn;
    
    private LocalDate borrowDate;
    
    private LocalDate dueDate;
    
    private LocalDate returnDate;
    
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}