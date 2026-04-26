package com.yyyouth.service.user.collect;

import com.yyyouth.model.dto.user.UserCollectCreateDTO;
import com.yyyouth.model.dto.user.UserCollectPageDTO;
import com.yyyouth.model.vo.user.UserCollectItemVO;
import com.yyyouth.model.vo.user.UserCollectPageVO;
import com.yyyouth.model.vo.user.UserCollectStatsVO;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 用户收藏服务接口
 */
public interface UserCollectService {

    /**
     * 收藏网站
     *
     * @param createDTO 收藏参数
     * @return 收藏条目ID
     */
    Long collect(UserCollectCreateDTO createDTO);

    /**
     * 取消收藏
     *
     * @param websiteId 网站ID
     */
    void cancelCollect(Long websiteId);

    /**
     * 分页查询收藏列表
     *
     * @param pageDTO 分页参数
     * @return 分页结果
     */
    UserCollectPageVO pageCollectList(UserCollectPageDTO pageDTO);

    /**
     * 获取当前用户收藏统计
     *
     * @return 收藏统计
     */
    UserCollectStatsVO getCollectStats();
}
