package com.yyyouth.model.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-16
 *
 * 管理端举报列表项
 */
@Data
public class AdminReportListItemVO {

    private Long id;

    private Long userId;

    private String userName;

    private String userEmail;

    private Integer type;

    private Long targetId;

    private String targetName;

    private String targetUrl;

    private Long uploaderId;

    private String uploaderName;

    private String reason;

    private List<String> images;

    private Integer status;

    private String handleResult;

    private Long handlerId;

    private String handlerName;

    private LocalDateTime handleTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
