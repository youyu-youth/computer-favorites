package com.yyyouth.model.vo.user;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-11
 *
 * 举报列表项视图对象
 */
@Data
public class ReportListItemVO {

    /**
     * 举报ID
     */
    private Long id;

    /**
     * 举报类型：1=网站，2=评论
     */
    private Integer type;

    /**
     * 举报目标ID
     */
    private Long targetId;

    /**
     * 举报目标名称
     */
    private String targetName;

    /**
     * 举报原因
     */
    private String reason;

    /**
     * 截图证据
     */
    private List<String> images;

    /**
     * 处理状态：0=待处理，1=已处理，2=已驳回
     */
    private Integer status;

    /**
     * 处理结果说明
     */
    private String handleResult;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
