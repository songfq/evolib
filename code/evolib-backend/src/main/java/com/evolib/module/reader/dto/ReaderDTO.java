package com.evolib.module.reader.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReaderDTO {
    private String readerId;
    private String name;
    private String phone;
    private Integer currentBorrowCount;
    private Integer maxBorrowCount;
    private String role;
}