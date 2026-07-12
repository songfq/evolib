package com.evolib.module.reader.controller;

import com.evolib.common.Result;
import com.evolib.module.reader.dto.RegisterRequest;
import com.evolib.module.reader.dto.ReaderDTO;
import com.evolib.module.reader.service.ReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/v1/readers")
@RequiredArgsConstructor
@Slf4j
public class ReaderController {
    
    private final ReaderService readerService;
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_CIRCULATION')")
    public Result<ReaderDTO> register(@RequestBody @Valid RegisterRequest request) {
        log.info("注册读者: readerId={}, name={}", request.getReaderId(), request.getName());
        ReaderDTO reader = readerService.register(request);
        return Result.ok(reader);
    }
    
    @PutMapping("/{readerId}/phone")
    @PreAuthorize("hasRole('ROLE_CIRCULATION')")
    public Result<Void> updatePhone(
            @PathVariable String readerId,
            @RequestBody @Valid @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {
        log.info("修改手机号: readerId={}, phone={}", readerId, phone);
        readerService.updatePhone(readerId, phone);
        return Result.ok(null);
    }
}