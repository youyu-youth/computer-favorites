package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-04-11
 *
 * 举报提交响应视图对象
 */
@Data
@Builder
public class ReportSubmitVO {

    /**
     * 举报ID
     */
    private Long reportId;

    /**
     * 处理状态：0=待处理
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
