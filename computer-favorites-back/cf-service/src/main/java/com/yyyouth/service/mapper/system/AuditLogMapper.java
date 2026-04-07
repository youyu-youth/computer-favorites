package com.yyyouth.service.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.system.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 审计日志 Mapper
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
