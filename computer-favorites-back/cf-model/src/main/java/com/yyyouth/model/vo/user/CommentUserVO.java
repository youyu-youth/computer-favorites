package com.yyyouth.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yyyouth zg
 * @date 2026-05-04
 *
 * 评论用户信息视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;
}
