package com.yyyouth.service.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.common.constants.AuthErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.LoginUserProfileQueryDTO;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.user.UserProfile;
import com.yyyouth.model.pojo.user.UserSetting;
import com.yyyouth.model.vo.user.LoginUserProfileVO;
import com.yyyouth.model.vo.user.UserBasicInfoVO;
import com.yyyouth.model.vo.user.UserDetailProfileVO;
import com.yyyouth.model.vo.user.UserPreferenceSettingVO;
import com.yyyouth.service.mapper.auth.UserAccountMapper;
import com.yyyouth.service.mapper.user.UserProfileMapper;
import com.yyyouth.service.mapper.user.UserSettingMapper;
import com.yyyouth.service.user.impl.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.ObjectUtils;

/**
 * @author yyyouth zg
 * @date 2026-03-18
 *
 * 登录用户资料服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserAccountMapper userAccountMapper;

    private final UserProfileMapper userProfileMapper;

    private final UserSettingMapper userSettingMapper;

    /**
     * 聚合查询登录用户资料
     *
     * @return 登录用户资料
     */
    @Override
    public LoginUserProfileVO queryLoginUserProfile() {
        StpUtil.checkLogin();
        LoginUserProfileQueryDTO queryDTO = new LoginUserProfileQueryDTO();
        queryDTO.setUserId(StpUtil.getLoginIdAsLong());

        UserAccount userAccount = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getId, queryDTO.getUserId())
                .eq(UserAccount::getDeleted, 0)
                .last("limit 1"));
        if (userAccount == null) {
            throw new BusinessException(AuthErrorCode.USER_DISABLED.getCode(), AuthErrorCode.USER_DISABLED.getMessage());
        }

        UserProfile userProfile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, queryDTO.getUserId())
                .eq(UserProfile::getDeleted, 0)
                .last("limit 1"));

        UserSetting userSetting = userSettingMapper.selectOne(new LambdaQueryWrapper<UserSetting>()
                .eq(UserSetting::getUserId, queryDTO.getUserId())
                .last("limit 1"));

        LoginUserProfileVO resultVO = new LoginUserProfileVO();
        resultVO.setUser(copyOrNull(userAccount, UserBasicInfoVO.class));
        resultVO.setProfile(copyOrNull(userProfile, UserDetailProfileVO.class));
        resultVO.setSetting(copyOrNull(userSetting, UserPreferenceSettingVO.class));
        return resultVO;
    }

    /**
     * 复制对象属性，空对象直接返回空
     *
     * @param source 源对象
     * @param targetClass 目标类型
     * @return 复制结果
     * @param <T> 目标泛型
     */
    private <T> T copyOrNull(Object source, Class<T> targetClass) {
        if (ObjectUtils.isEmpty(source)) {
            return null;
        }
        return BeanUtil.copyProperties(source, targetClass);
    }
}
