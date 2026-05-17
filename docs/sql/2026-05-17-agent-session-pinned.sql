-- 为 t_agent_session 表增加置顶字段
-- 用于 AI Agent 会话列表置顶功能，允许用户将重要会话固定在列表顶部
-- 字段位置：放在 status 字段之后
ALTER TABLE t_agent_session
    ADD COLUMN pinned TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶 0否 1是' AFTER status;
