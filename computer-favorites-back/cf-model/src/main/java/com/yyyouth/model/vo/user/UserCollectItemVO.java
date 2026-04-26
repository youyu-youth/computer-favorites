package com.yyyouth.model.vo.user;

import lombok.Builder;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 收藏列表条目
 */
@Data
@Builder
public class UserCollectItemVO {

    private Long id;

    private Long websiteId;

    private String websiteName;

    private String websiteUrl;

    private String websiteIcon;

    private String websiteSummary;

    private String websiteTags;

    private Integer likeCount;

    private String collectTime;

    private Long folderId;

    private String folderName;
}
