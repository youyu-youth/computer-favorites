package com.yyyouth.service.admin.category.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.CategoryConstants;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminCategoryCreateDTO;
import com.yyyouth.model.dto.admin.AdminCategoryEditDTO;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.admin.AdminCategoryTreeItemVO;
import com.yyyouth.service.admin.category.AdminCategoryService;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 管理端分类服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private static final int MAX_PARENT_CHECK_DEPTH = 1000;

    private final CategoryMapper categoryMapper;

    private final WebsiteMapper websiteMapper;

    /**
     * 查询分类树
     *
     * @param status 分类状态（可选）
     * @return 分类树
     */
    @Override
    public List<AdminCategoryTreeItemVO> queryCategoryTree(Integer status) {
        Integer normalizedStatus = normalizeStatusFilter(status);

        LambdaQueryWrapper<WebsiteCategory> queryWrapper = new LambdaQueryWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getDeleted, CategoryConstants.NOT_DELETED)
                .orderByAsc(WebsiteCategory::getSort)
                .orderByAsc(WebsiteCategory::getId);
        if (normalizedStatus != null) {
            queryWrapper.eq(WebsiteCategory::getStatus, normalizedStatus);
        }

        List<WebsiteCategory> categoryList = categoryMapper.selectList(queryWrapper);
        if (categoryList.isEmpty()) {
            return Collections.emptyList();
        }
        return buildCategoryTree(categoryList);
    }

    /**
     * 创建分类
     *
     * @param createDTO 创建参数
     * @return 分类ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(AdminCategoryCreateDTO createDTO) {
        Long parentId = normalizeParentId(createDTO.getParentId());
        validateParentForCreate(parentId);

        String normalizedName = normalizeName(createDTO.getName());
        validateDuplicateName(parentId, normalizedName, null);

        WebsiteCategory category = BeanUtil.copyProperties(createDTO, WebsiteCategory.class);
        LocalDateTime now = LocalDateTime.now();
        category.setName(normalizedName);
        category.setIcon(normalizeOptionalText(createDTO.getIcon()));
        category.setDescription(normalizeOptionalText(createDTO.getDescription()));
        category.setParentId(parentId);
        category.setSort(normalizeSort(createDTO.getSort()));
        category.setStatus(CategoryConstants.STATUS_ENABLED);
        category.setDeleted(CategoryConstants.NOT_DELETED);
        category.setCreateTime(now);
        category.setUpdateTime(now);

        int insertedRows = categoryMapper.insert(category);
        if (insertedRows != 1 || category.getId() == null) {
            throw new BusinessException(HttpStatus.ERROR, "分类创建失败，请稍后重试");
        }

        reorderSiblingSort(parentId);
        return category.getId();
    }

    /**
     * 编辑分类
     *
     * @param categoryId 分类ID
     * @param editDTO 编辑参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editCategory(Long categoryId, AdminCategoryEditDTO editDTO) {
        WebsiteCategory currentCategory = queryAvailableCategoryById(categoryId);
        Long originParentId = normalizeParentId(currentCategory.getParentId());
        Long targetParentId = normalizeParentId(editDTO.getParentId());

        validateParentForEdit(categoryId, targetParentId);

        String normalizedName = normalizeName(editDTO.getName());
        validateDuplicateName(targetParentId, normalizedName, categoryId);

        WebsiteCategory updateEntity = new WebsiteCategory();
        updateEntity.setId(categoryId);
        updateEntity.setName(normalizedName);
        updateEntity.setIcon(normalizeOptionalText(editDTO.getIcon()));
        updateEntity.setDescription(normalizeOptionalText(editDTO.getDescription()));
        updateEntity.setParentId(targetParentId);
        updateEntity.setSort(normalizeSort(editDTO.getSort()));
        updateEntity.setStatus(editDTO.getStatus());
        updateEntity.setUpdateTime(LocalDateTime.now());

        int updatedRows = categoryMapper.update(updateEntity, new LambdaUpdateWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getId, categoryId)
                .eq(WebsiteCategory::getDeleted, CategoryConstants.NOT_DELETED));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "分类更新失败，请稍后重试");
        }

        if (!Objects.equals(originParentId, targetParentId)) {
            reorderSiblingSort(originParentId);
        }
        reorderSiblingSort(targetParentId);
    }

    /**
     * 更新分类排序
     *
     * @param categoryId 分类ID
     * @param sort 排序值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategorySort(Long categoryId, Integer sort) {
        WebsiteCategory currentCategory = queryAvailableCategoryById(categoryId);

        int updatedRows = categoryMapper.update(new WebsiteCategory(), new LambdaUpdateWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getId, categoryId)
                .eq(WebsiteCategory::getDeleted, CategoryConstants.NOT_DELETED)
                .set(WebsiteCategory::getSort, normalizeSort(sort))
                .set(WebsiteCategory::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "分类排序更新失败，请稍后重试");
        }

        reorderSiblingSort(currentCategory.getParentId());
    }

    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        WebsiteCategory currentCategory = queryAvailableCategoryById(categoryId);

        long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getParentId, categoryId)
                .eq(WebsiteCategory::getDeleted, CategoryConstants.NOT_DELETED));
        if (childCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类存在子分类，无法删除");
        }

        long websiteCount = websiteMapper.selectCount(new LambdaQueryWrapper<Website>()
                .eq(Website::getCategoryId, categoryId)
                .eq(Website::getDeleted, CategoryConstants.NOT_DELETED));
        if (websiteCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类已被网站引用，无法删除");
        }

        int updatedRows = categoryMapper.update(new WebsiteCategory(), new LambdaUpdateWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getId, categoryId)
                .eq(WebsiteCategory::getDeleted, CategoryConstants.NOT_DELETED)
                .set(WebsiteCategory::getDeleted, CategoryConstants.DELETED)
                .set(WebsiteCategory::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "分类删除失败，请稍后重试");
        }

        reorderSiblingSort(currentCategory.getParentId());
    }

    /**
     * 构建分类树
     *
     * @param categoryList 分类列表
     * @return 分类树
     */
    private List<AdminCategoryTreeItemVO> buildCategoryTree(List<WebsiteCategory> categoryList) {
        Map<Long, AdminCategoryTreeItemVO> nodeMap = new LinkedHashMap<>(categoryList.size());
        for (WebsiteCategory category : categoryList) {
            AdminCategoryTreeItemVO node = BeanUtil.copyProperties(category, AdminCategoryTreeItemVO.class);
            node.setChildren(new ArrayList<>());
            nodeMap.put(category.getId(), node);
        }

        List<AdminCategoryTreeItemVO> rootList = new ArrayList<>();
        for (WebsiteCategory category : categoryList) {
            AdminCategoryTreeItemVO currentNode = nodeMap.get(category.getId());
            Long parentId = normalizeParentId(category.getParentId());
            if (Objects.equals(parentId, CategoryConstants.ROOT_PARENT_ID)) {
                rootList.add(currentNode);
                continue;
            }

            AdminCategoryTreeItemVO parentNode = nodeMap.get(parentId);
            if (parentNode == null) {
                rootList.add(currentNode);
                continue;
            }
            parentNode.getChildren().add(currentNode);
        }
        return rootList;
    }

    /**
     * 校验并规范状态筛选值
     *
     * @param status 分类状态
     * @return 规范化后的状态值
     */
    private Integer normalizeStatusFilter(Integer status) {
        if (status == null) {
            return null;
        }
        if (!Objects.equals(status, CategoryConstants.STATUS_DISABLED)
                && !Objects.equals(status, CategoryConstants.STATUS_ENABLED)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类状态参数不合法");
        }
        return status;
    }

    /**
     * 校验父级分类（创建）
     *
     * @param parentId 父分类ID
     */
    private void validateParentForCreate(Long parentId) {
        if (Objects.equals(parentId, CategoryConstants.ROOT_PARENT_ID)) {
            return;
        }

        WebsiteCategory parentCategory = queryAvailableCategoryById(parentId);
        if (!Objects.equals(parentCategory.getStatus(), CategoryConstants.STATUS_ENABLED)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "父分类已禁用，无法创建子分类");
        }
    }

    /**
     * 校验父级分类（编辑）
     *
     * @param categoryId 分类ID
     * @param targetParentId 目标父分类ID
     */
    private void validateParentForEdit(Long categoryId, Long targetParentId) {
        if (Objects.equals(categoryId, targetParentId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "父分类不能是当前分类");
        }
        if (Objects.equals(targetParentId, CategoryConstants.ROOT_PARENT_ID)) {
            return;
        }

        WebsiteCategory parentCategory = queryAvailableCategoryById(targetParentId);
        if (!Objects.equals(parentCategory.getStatus(), CategoryConstants.STATUS_ENABLED)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "父分类已禁用，无法挂载到该分类");
        }

        validateParentChain(categoryId, targetParentId);
    }

    /**
     * 校验父级链路，防止出现循环关系
     *
     * @param categoryId 分类ID
     * @param targetParentId 目标父分类ID
     */
    private void validateParentChain(Long categoryId, Long targetParentId) {
        Long currentParentId = targetParentId;
        int checkDepth = 0;

        while (!Objects.equals(currentParentId, CategoryConstants.ROOT_PARENT_ID)) {
            if (Objects.equals(currentParentId, categoryId)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "父子关系不合法，存在循环引用");
            }

            WebsiteCategory parentCategory = queryAvailableCategoryById(currentParentId);
            currentParentId = normalizeParentId(parentCategory.getParentId());
            checkDepth++;

            if (checkDepth > MAX_PARENT_CHECK_DEPTH) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "父子关系层级异常，请检查分类数据");
            }
        }
    }

    /**
     * 校验同级分类重名
     *
     * @param parentId 父分类ID
     * @param categoryName 分类名称
     * @param excludeCategoryId 排除分类ID
     */
    private void validateDuplicateName(Long parentId, String categoryName, Long excludeCategoryId) {
        LambdaQueryWrapper<WebsiteCategory> queryWrapper = new LambdaQueryWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getParentId, parentId)
                .eq(WebsiteCategory::getName, categoryName)
                .eq(WebsiteCategory::getDeleted, CategoryConstants.NOT_DELETED)
                .last("limit 1");
        if (excludeCategoryId != null) {
            queryWrapper.ne(WebsiteCategory::getId, excludeCategoryId);
        }

        WebsiteCategory duplicateCategory = categoryMapper.selectOne(queryWrapper);
        if (duplicateCategory != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "同级分类名称已存在");
        }
    }

    /**
     * 查询可操作分类
     *
     * @param categoryId 分类ID
     * @return 分类实体
     */
    private WebsiteCategory queryAvailableCategoryById(Long categoryId) {
        WebsiteCategory category = categoryMapper.selectOne(new LambdaQueryWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getId, categoryId)
                .eq(WebsiteCategory::getDeleted, CategoryConstants.NOT_DELETED)
                .last("limit 1"));
        if (category == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类不存在或已删除");
        }
        return category;
    }

    /**
     * 同级重排排序值
     *
     * @param parentId 父分类ID
     */
    private void reorderSiblingSort(Long parentId) {
        Long normalizedParentId = normalizeParentId(parentId);

        List<WebsiteCategory> siblingList = categoryMapper.selectList(new LambdaQueryWrapper<WebsiteCategory>()
                .eq(WebsiteCategory::getParentId, normalizedParentId)
                .eq(WebsiteCategory::getDeleted, CategoryConstants.NOT_DELETED)
                .orderByAsc(WebsiteCategory::getSort)
                .orderByAsc(WebsiteCategory::getId));

        for (int index = 0; index < siblingList.size(); index++) {
            WebsiteCategory sibling = siblingList.get(index);
            if (Objects.equals(sibling.getSort(), index)) {
                continue;
            }

            int updatedRows = categoryMapper.update(new WebsiteCategory(), new LambdaUpdateWrapper<WebsiteCategory>()
                    .eq(WebsiteCategory::getId, sibling.getId())
                    .eq(WebsiteCategory::getDeleted, CategoryConstants.NOT_DELETED)
                    .set(WebsiteCategory::getSort, index)
                    .set(WebsiteCategory::getUpdateTime, LocalDateTime.now()));
            if (updatedRows != 1) {
                throw new BusinessException(HttpStatus.ERROR, "分类排序重排失败，请稍后重试");
            }
        }
    }

    /**
     * 规范化分类名称
     *
     * @param categoryName 分类名称
     * @return 规范化后的名称
     */
    private String normalizeName(String categoryName) {
        String normalizedName = categoryName == null ? "" : categoryName.trim();
        if (!StringUtils.hasText(normalizedName)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类名称不能为空");
        }
        return normalizedName;
    }

    /**
     * 规范化可选文本字段
     *
     * @param value 原始值
     * @return 规范化后的值
     */
    private String normalizeOptionalText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 规范化父分类ID
     *
     * @param parentId 原始父分类ID
     * @return 规范化后的父分类ID
     */
    private Long normalizeParentId(Long parentId) {
        return parentId == null ? CategoryConstants.ROOT_PARENT_ID : parentId;
    }

    /**
     * 规范化排序值
     *
     * @param sort 原始排序值
     * @return 规范化后的排序值
     */
    private Integer normalizeSort(Integer sort) {
        if (sort == null) {
            return CategoryConstants.DEFAULT_SORT;
        }
        if (sort < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "排序值不能小于0");
        }
        return sort;
    }
}
