package com.yyyouth.service.admin.profile;

import com.yyyouth.model.dto.admin.AdminPasswordUpdateDTO;
import com.yyyouth.model.dto.admin.AdminProfileUpdateDTO;
import com.yyyouth.model.vo.admin.AdminProfileVO;

/**
 * @author yyyouth zg
 * @date 2026-03-31
 *
 * 管理员资料服务接口
 */
public interface AdminProfileService {

    /**
     * 查询登录管理员资料
     *
     * @return 管理员资料
     */
    AdminProfileVO queryLoginAdminProfile();

    /**
     * 更新登录管理员资料
     *
     * @param updateDTO 更新参数
     */
    void updateLoginAdminProfile(AdminProfileUpdateDTO updateDTO);

    /**
     * 更新登录管理员密码
     *
     * @param updateDTO 修改密码参数
     */
    void updateLoginAdminPassword(AdminPasswordUpdateDTO updateDTO);
}