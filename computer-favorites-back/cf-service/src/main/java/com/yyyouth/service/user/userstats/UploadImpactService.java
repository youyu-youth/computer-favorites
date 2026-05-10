package com.yyyouth.service.user.userstats;

import com.yyyouth.model.vo.userstats.UploadImpactVO;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * 上传网站影响力服务（user-15 扩展）。
 *
 * 与 {@link ProfileDashboardService} 区分：本服务统计「<strong>他人对当前用户上传网站</strong>」的行为，
 * 而 ProfileDashboardService 统计「当前用户自身的行为」。
 */
public interface UploadImpactService {

    /**
     * 查询用户上传网站影响力。
     *
     * @param userId 网站上传者用户 ID
     * @param range  时间范围：7d / 30d / 90d / all
     * @return 含 totals / rangeCounts / delta / points 的聚合 VO
     * @throws com.yyyouth.common.exception.BusinessException range 非法时抛 400
     */
    UploadImpactVO getUploadImpact(Long userId, String range);
}
