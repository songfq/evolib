package com.evolib.module.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("books")
public class Book {
    
    @TableId(type = IdType.INPUT)
    private String isbn;
    
    private String title;
    
    private String author;
    
    private Integer totalStock;
    
    private Integer availableStock;
    
    private String shelfLocation;
    
    private String description;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    public Book() {
    }

    public Book(String isbn, String title, String author, Integer totalStock,
                Integer availableStock, String shelfLocation, String description) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.totalStock = totalStock;
        this.availableStock = availableStock;
        this.shelfLocation = shelfLocation;
        this.description = description;
    }
}