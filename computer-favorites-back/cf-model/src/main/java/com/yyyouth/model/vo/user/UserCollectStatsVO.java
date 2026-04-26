package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 收藏统计结果
 */
@Data
@Builder
public class UserCollectStatsVO {

    private Long collectCount;

    private Long folderCount;
}
