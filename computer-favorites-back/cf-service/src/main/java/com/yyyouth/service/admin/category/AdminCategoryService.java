package com.yyyouth.service.admin.category;

import com.yyyouth.model.dto.admin.AdminCategoryCreateDTO;
import com.yyyouth.model.dto.admin.AdminCategoryEditDTO;
import com.yyyouth.model.vo.admin.AdminCategoryTreeItemVO;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 管理端分类服务
 */
public interface AdminCategoryService {

    /**
     * 查询分类树
     *
     * @param status 分类状态（可选）
     * @return 分类树
     */
    List<AdminCategoryTreeItemVO> queryCategoryTree(Integer status);

    /**
     * 创建分类
     *
     * @param createDTO 创建参数
     * @return 分类ID
     */
    Long createCategory(AdminCategoryCreateDTO createDTO);

    /**
     * 编辑分类
     *
     * @param categoryId 分类ID
     * @param editDTO 编辑参数
     */
    void editCategory(Long categoryId, AdminCategoryEditDTO editDTO);

    /**
     * 更新分类排序
     *
     * @param categoryId 分类ID
     * @param sort 排序值
     */
    void updateCategorySort(Long categoryId, Integer sort);

    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     */
    void deleteCategory(Long categoryId);
}
