package com.yyyouth.service.admin.tag;

import com.yyyouth.model.dto.admin.AdminTagBatchDeleteDTO;
import com.yyyouth.model.dto.admin.AdminTagCreateDTO;
import com.yyyouth.model.dto.admin.AdminTagEditDTO;
import com.yyyouth.model.dto.admin.AdminTagQueryDTO;
import com.yyyouth.model.vo.admin.AdminTagPageVO;
import com.yyyouth.model.vo.admin.AdminTagStatsVO;

/**
 * @author yyyouth zg
 * @date 2026-04-03
 *
 * 管理端标签服务
 */
public interface AdminTagService {

    /**
     * 查询标签分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    AdminTagPageVO queryTagPage(AdminTagQueryDTO queryDTO);

    /**
     * 查询标签统计
     *
     * @return 标签统计
     */
    AdminTagStatsVO queryTagStats();

    /**
     * 创建标签
     *
     * @param createDTO 创建参数
     * @return 标签ID
     */
    Long createTag(AdminTagCreateDTO createDTO);

    /**
     * 编辑标签
     *
     * @param tagId 标签ID
     * @param editDTO 编辑参数
     */
    void editTag(Long tagId, AdminTagEditDTO editDTO);

    /**
     * 删除标签（逻辑删除）
     *
     * @param tagId 标签ID
     */
    void deleteTag(Long tagId);

    /**
     * 批量删除标签（逻辑删除）
     *
     * @param batchDeleteDTO 批量参数
     * @return 删除数量
     */
    int batchDeleteTags(AdminTagBatchDeleteDTO batchDeleteDTO);
}
