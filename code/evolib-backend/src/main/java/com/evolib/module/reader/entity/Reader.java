package com.evolib.module.reader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("readers")
public class Reader {
    
    @TableId(type = IdType.INPUT)
    private String readerId;
    
    private String name;
    
    private String phone;
    
    private String passwordHash;
    
    private Integer currentBorrowCount;
    
    private Integer maxBorrowCount;
    
    private String role;
    
    private LocalDateTime createdAt;
}