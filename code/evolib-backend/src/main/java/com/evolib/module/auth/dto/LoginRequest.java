package com.evolib.module.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    
    @NotBlank(message = "读者ID不能为空")
    private String readerId;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}