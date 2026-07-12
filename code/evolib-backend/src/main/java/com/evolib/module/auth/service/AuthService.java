package com.evolib.module.auth.service;

import com.evolib.module.auth.dto.LoginRequest;
import com.evolib.module.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}