package com.evolib.module.auth.controller;

import com.evolib.common.Result;
import com.evolib.module.auth.dto.LoginRequest;
import com.evolib.module.auth.dto.LoginResponse;
import com.evolib.module.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("登录请求: readerId={}", request.getReaderId());
        LoginResponse response = authService.login(request);
        return Result.ok(response);
    }
}