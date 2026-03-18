package com.yyyouth.service.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyyouth.model.pojo.user.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 用户资料Mapper
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
