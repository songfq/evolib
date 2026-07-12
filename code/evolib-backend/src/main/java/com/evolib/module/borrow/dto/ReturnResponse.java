package com.evolib.module.borrow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {
    private Long borrowId;
    private LocalDate returnDate;
    private Integer overdueDays;
}