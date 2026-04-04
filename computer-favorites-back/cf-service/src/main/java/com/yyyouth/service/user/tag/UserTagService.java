package com.yyyouth.service.user.tag;

import com.yyyouth.model.dto.user.UserTagQueryDTO;
import com.yyyouth.model.vo.user.UserTagPageVO;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端标签服务
 */
public interface UserTagService {

    /**
     * 查询标签分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    UserTagPageVO queryTagPage(UserTagQueryDTO queryDTO);
}