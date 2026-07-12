package com.evolib.module.borrow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowResponse {
    private Long borrowId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
}