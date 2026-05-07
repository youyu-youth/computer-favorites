package com.yyyouth.model.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author yyyouth zg
 * @date 2026-03-23
 *
 * 用户偏好设置更新参数
 */
@Data
public class UserPreferenceSettingUpdateDTO {

    /**
     * 主题模式
     */
    @NotBlank(message = "主题模式不能为空")
    @Pattern(regexp = "^(light|dark|system)$", message = "主题模式仅支持 light、dark、system")
    private String theme;

    /**
     * 语言
     */
    @NotBlank(message = "系统语言不能为空")
    @Pattern(regexp = "^(zh-CN|en-US)$", message = "系统语言仅支持 zh-CN、en-US")
    private String language;

    /**
     * 邮件通知
     */
    @NotNull(message = "邮件通知不能为空")
    @Min(value = 0, message = "邮件通知取值非法")
    @Max(value = 1, message = "邮件通知取值非法")
    private Integer emailNotice;

    /**
     * 收藏通知
     */
    @NotNull(message = "收藏通知不能为空")
    @Min(value = 0, message = "收藏通知取值非法")
    @Max(value = 1, message = "收藏通知取值非法")
    private Integer collectNotice;

    /**
     * 评论通知
     */
    @NotNull(message = "评论通知不能为空")
    @Min(value = 0, message = "评论通知取值非法")
    @Max(value = 1, message = "评论通知取值非法")
    private Integer commentNotice;

    /**
     * 首页样式
     */
    @NotBlank(message = "首页样式不能为空")
    @Pattern(regexp = "^(card|list)$", message = "首页样式仅支持 card、list")
    private String homepageStyle;

    /**
     * 收藏夹访问密码
     */
    @Size(max = 64, message = "收藏夹访问密码长度不能超过64个字符")
    private String favoritesHidePassword;

    /**
     * 每页条数
     */
    @NotNull(message = "分页大小不能为空")
    private Integer pageSize;

    /**
     * 校验分页大小是否在允许范围
     *
     * @return 校验结果
     */
    @AssertTrue(message = "分页大小仅支持 10、20、50")
    public boolean isPageSizeValid() {
        if (pageSize == null) {
            return false;
        }
        return pageSize == 10 || pageSize == 20 || pageSize == 50;
    }
}
