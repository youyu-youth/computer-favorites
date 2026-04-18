package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈列表项
 */
@Data
public class AdminFeedbackListItemVO {

    private Long id;

    private Long userId;

    private String userName;

    private String userEmail;

    private String avatar;

    private String contact;

    private Integer type;

    private String content;

    private List<String> images;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
