package com.evolib.module.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_logs")
public class AuditLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String operatorId;
    
    private String action;
    
    private String target;
    
    private String detail;
    
    private String ipAddress;
    
    private LocalDateTime createdAt;
}