package com.yyyouth.model.vo.admin;

import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端批量评论状态变更结果
 */
@Data
public class AdminCommentBatchStatusResultVO {

    /**
     * 请求处理总数
     */
    private Integer total;

    /**
     * 成功数
     */
    private Integer success;

    /**
     * 跳过数（不存在/已删除/已是目标状态）
     */
    private Integer skipped;

    /**
     * 汇总信息
     */
    private String message;
}
