package com.yyyouth.model.pojo.website;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 网站点赞实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_website_like")
public class WebsiteLike {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 网站ID
     */
    @TableField("website_id")
    private Long websiteId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
