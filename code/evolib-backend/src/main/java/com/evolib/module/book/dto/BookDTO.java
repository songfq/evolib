package com.evolib.module.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private String isbn;
    private String title;
    private String author;
    private Integer totalStock;
    private Integer availableStock;
    private String shelfLocation;
    private String description;
}