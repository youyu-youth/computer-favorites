package com.yyyouth.service.mapper.user.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.auth.UserSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 用户会话Mapper
 */
@Mapper
public interface UserSessionMapper extends BaseMapper<UserSession> {
}
