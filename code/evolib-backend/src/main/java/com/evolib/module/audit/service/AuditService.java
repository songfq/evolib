package com.evolib.module.audit.service;

import com.evolib.module.audit.entity.AuditLog;

public interface AuditService {
    void log(String operatorId, String action, String target, String detail);
}