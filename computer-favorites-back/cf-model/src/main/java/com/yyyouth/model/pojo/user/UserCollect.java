package com.yyyouth.model.pojo.user;

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
 * @date 2026-04-25
 *
 * 用户收藏实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_collect")
public class UserCollect {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("website_id")
    private Long websiteId;

    @TableField("folder_id")
    private Long folderId;

    @TableField("create_time")
    private LocalDateTime createTime;
}
