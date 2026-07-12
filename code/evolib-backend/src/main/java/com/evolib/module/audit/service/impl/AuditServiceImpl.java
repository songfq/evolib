package com.evolib.module.audit.service.impl;

import com.evolib.module.audit.entity.AuditLog;
import com.evolib.module.audit.mapper.AuditLogMapper;
import com.evolib.module.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    
    private final AuditLogMapper auditLogMapper;
    
    @Override
    public void log(String operatorId, String action, String target, String detail) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setTarget(target);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
    }
}