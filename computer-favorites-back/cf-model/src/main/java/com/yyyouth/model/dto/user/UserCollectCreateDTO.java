package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-04-25
 *
 * 收藏网站参数
 */
@Data
public class UserCollectCreateDTO {

    @NotNull(message = "网站ID不能为空")
    @Positive(message = "网站ID不合法")
    private Long websiteId;

    @Positive(message = "文件夹ID不合法")
    private Long folderId;
}
