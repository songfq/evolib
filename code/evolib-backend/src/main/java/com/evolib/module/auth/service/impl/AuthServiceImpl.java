package com.evolib.module.auth.service.impl;

import com.evolib.common.BusinessException;
import com.evolib.common.ErrorCode;
import com.evolib.module.auth.dto.LoginRequest;
import com.evolib.module.auth.dto.LoginResponse;
import com.evolib.module.auth.service.AuthService;
import com.evolib.module.reader.entity.Reader;
import com.evolib.module.reader.mapper.ReaderMapper;
import com.evolib.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final ReaderMapper readerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        Reader reader = readerMapper.selectById(request.getReaderId());
        if (reader == null) {
            throw new BusinessException(ErrorCode.READER_NOT_FOUND);
        }
        
        if (!passwordEncoder.matches(request.getPassword(), reader.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM);
        }
        
        String token = tokenProvider.generateToken(reader.getReaderId(), reader.getRole());
        log.info("用户登录成功: readerId={}, role={}", reader.getReaderId(), reader.getRole());
        
        return new LoginResponse(token, reader.getRole(), reader.getName());
    }
}