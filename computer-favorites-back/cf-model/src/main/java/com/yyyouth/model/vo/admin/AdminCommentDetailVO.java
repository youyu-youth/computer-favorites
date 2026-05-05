package com.yyyouth.model.vo.admin;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 管理端评论详情
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminCommentDetailVO extends AdminCommentListItemVO {

    /**
     * 回复列表
     */
    private List<AdminCommentReplyItemVO> replies;

    /**
     * 最近管理操作描述
     */
    private String lastAdminAction;

    /**
     * 最近管理操作时间
     */
    private LocalDateTime lastAdminActionTime;
}
