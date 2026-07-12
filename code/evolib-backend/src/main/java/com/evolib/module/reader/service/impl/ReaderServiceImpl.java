package com.evolib.module.reader.service.impl;

import com.evolib.common.BusinessException;
import com.evolib.common.ErrorCode;
import com.evolib.module.reader.dto.RegisterRequest;
import com.evolib.module.reader.dto.ReaderDTO;
import com.evolib.module.reader.entity.Reader;
import com.evolib.module.reader.mapper.ReaderMapper;
import com.evolib.module.reader.service.ReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {
    
    private final ReaderMapper readerMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public ReaderDTO register(RegisterRequest request) {
        if (readerMapper.selectById(request.getReaderId()) != null) {
            throw new BusinessException(ErrorCode.INVALID_PARAM);
        }
        
        Reader reader = new Reader();
        reader.setReaderId(request.getReaderId());
        reader.setName(request.getName());
        reader.setPhone(request.getPhone());
        
        String defaultPassword = request.getPhone().substring(request.getPhone().length() - 6);
        reader.setPasswordHash(passwordEncoder.encode(defaultPassword));
        reader.setCurrentBorrowCount(0);
        reader.setMaxBorrowCount(3);
        reader.setRole("ROLE_READER");
        reader.setCreatedAt(LocalDateTime.now());
        
        readerMapper.insert(reader);
        log.info("读者注册成功: readerId={}, name={}", request.getReaderId(), request.getName());
        
        return convertToDTO(reader);
    }
    
    @Override
    public ReaderDTO getById(String readerId) {
        Reader reader = readerMapper.selectById(readerId);
        if (reader == null) {
            throw new BusinessException(ErrorCode.READER_NOT_FOUND);
        }
        return convertToDTO(reader);
    }
    
    @Override
    @Transactional
    public void updatePhone(String readerId, String phone) {
        Reader reader = readerMapper.selectById(readerId);
        if (reader == null) {
            throw new BusinessException(ErrorCode.READER_NOT_FOUND);
        }
        
        reader.setPhone(phone);
        readerMapper.updateById(reader);
        log.info("更新读者手机号: readerId={}, phone={}", readerId, phone);
    }
    
    private ReaderDTO convertToDTO(Reader reader) {
        ReaderDTO dto = new ReaderDTO();
        dto.setReaderId(reader.getReaderId());
        dto.setName(reader.getName());
        dto.setPhone(reader.getPhone());
        dto.setCurrentBorrowCount(reader.getCurrentBorrowCount());
        dto.setMaxBorrowCount(reader.getMaxBorrowCount());
        dto.setRole(reader.getRole());
        return dto;
    }
}