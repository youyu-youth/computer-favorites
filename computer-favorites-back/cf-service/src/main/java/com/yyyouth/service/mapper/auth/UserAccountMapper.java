package com.yyyouth.service.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.auth.UserAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yyyouth zg
 * @date 2026-03-16
 *
 * 用户账号Mapper
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {
}
