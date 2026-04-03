package com.yyyouth.service.admin.tag.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminTagBatchDeleteDTO;
import com.yyyouth.model.dto.admin.AdminTagCreateDTO;
import com.yyyouth.model.dto.admin.AdminTagEditDTO;
import com.yyyouth.model.dto.admin.AdminTagQueryDTO;
import com.yyyouth.model.pojo.website.Tag;
import com.yyyouth.model.vo.admin.AdminTagListItemVO;
import com.yyyouth.model.vo.admin.AdminTagPageVO;
import com.yyyouth.model.vo.admin.AdminTagStatsVO;
import com.yyyouth.service.admin.tag.AdminTagService;
import com.yyyouth.service.mapper.website.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yyyouth zg
 * @date 2026-04-03
 *
 * 管理端标签服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class AdminTagServiceImpl implements AdminTagService {

    private static final int NOT_DELETED = 0;

    private static final int DELETED = 1;

    private static final int SORT_ASC = 1;

    private static final int SORT_DESC = -1;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 8;

    private static final String SORT_FIELD_ID = "id";

    private static final String SORT_FIELD_NAME = "name";

    private static final String SORT_FIELD_COLOR = "color";

    private static final String SORT_FIELD_USE_COUNT = "useCount";

    private static final String SORT_FIELD_CREATE_TIME = "createTime";

    private static final String SORT_FIELD_UPDATE_TIME = "updateTime";

    private final TagMapper tagMapper;

    /**
     * 查询标签分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @Override
    public AdminTagPageVO queryTagPage(AdminTagQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null ? DEFAULT_PAGE_NUM : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? DEFAULT_PAGE_SIZE : queryDTO.getPageSize();

        LambdaQueryWrapper<Tag> baseQueryWrapper = buildTagQueryWrapper(queryDTO);
        long total = tagMapper.selectCount(baseQueryWrapper);

        AdminTagPageVO pageVO = new AdminTagPageVO();
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(calcTotalPages(total, pageSize));

        if (total == 0) {
            pageVO.setRecords(Collections.emptyList());
            return pageVO;
        }

        int offset = (pageNum - 1) * pageSize;
        LambdaQueryWrapper<Tag> listQueryWrapper = buildTagQueryWrapper(queryDTO);
        appendSortCondition(listQueryWrapper, queryDTO.getSortField(), queryDTO.getSortOrder());
        listQueryWrapper.last("limit " + offset + "," + pageSize);

        List<Tag> tagList = tagMapper.selectList(listQueryWrapper);
        List<AdminTagListItemVO> itemVOS = tagList.stream()
                .map(tag -> BeanUtil.copyProperties(tag, AdminTagListItemVO.class))
                .toList();
        pageVO.setRecords(itemVOS);
        return pageVO;
    }

    /**
     * 查询标签统计
     *
     * @return 标签统计
     */
    @Override
    public AdminTagStatsVO queryTagStats() {
        long total = countByCondition(null);
        long inUse = countByCondition(wrapper -> wrapper.gt(Tag::getUseCount, 0));
        long updatedToday = countByCondition(wrapper -> wrapper.apply("DATE(update_time) = CURDATE()"));

        AdminTagStatsVO statsVO = new AdminTagStatsVO();
        statsVO.setTotal(total);
        statsVO.setInUse(inUse);
        statsVO.setUnused(Math.max(total - inUse, 0));
        statsVO.setUpdatedToday(updatedToday);
        return statsVO;
    }

    /**
     * 创建标签
     *
     * @param createDTO 创建参数
     * @return 标签ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTag(AdminTagCreateDTO createDTO) {
        String normalizedName = normalizeName(createDTO.getName());
        String normalizedColor = normalizeColor(createDTO.getColor());

        Tag existedTag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, normalizedName)
                .last("limit 1"));
        if (existedTag != null) {
            if (Objects.equals(existedTag.getDeleted(), NOT_DELETED)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "标签名称已存在");
            }

            int restoredRows = tagMapper.update(new Tag(), new LambdaUpdateWrapper<Tag>()
                    .eq(Tag::getId, existedTag.getId())
                    .eq(Tag::getDeleted, DELETED)
                    .set(Tag::getDeleted, NOT_DELETED)
                    .set(Tag::getColor, normalizedColor)
                    .set(Tag::getUpdateTime, LocalDateTime.now()));
            if (restoredRows != 1) {
                throw new BusinessException(HttpStatus.ERROR, "标签恢复失败，请稍后重试");
            }
            return existedTag.getId();
        }

        Tag tag = new Tag();
        LocalDateTime now = LocalDateTime.now();
        tag.setName(normalizedName);
        tag.setColor(normalizedColor);
        tag.setUseCount(0);
        tag.setDeleted(NOT_DELETED);
        tag.setCreateTime(now);
        tag.setUpdateTime(now);

        int insertedRows = tagMapper.insert(tag);
        if (insertedRows != 1 || tag.getId() == null) {
            throw new BusinessException(HttpStatus.ERROR, "标签创建失败，请稍后重试");
        }
        return tag.getId();
    }

    /**
     * 编辑标签
     *
     * @param tagId 标签ID
     * @param editDTO 编辑参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editTag(Long tagId, AdminTagEditDTO editDTO) {
        Tag currentTag = queryAvailableTagById(tagId);

        String normalizedName = normalizeName(editDTO.getName());
        String normalizedColor = normalizeColor(editDTO.getColor());

        if (!Objects.equals(currentTag.getName(), normalizedName)) {
            Tag duplicatedTag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getName, normalizedName)
                    .ne(Tag::getId, tagId)
                    .last("limit 1"));
            if (duplicatedTag != null) {
                if (Objects.equals(duplicatedTag.getDeleted(), NOT_DELETED)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "标签名称已存在");
                }
                throw new BusinessException(HttpStatus.BAD_REQUEST, "标签名称已被历史记录占用，请使用其他名称");
            }
        }

        if (Objects.equals(currentTag.getName(), normalizedName)
                && Objects.equals(currentTag.getColor(), normalizedColor)) {
            return;
        }

        int updatedRows = tagMapper.update(new Tag(), new LambdaUpdateWrapper<Tag>()
                .eq(Tag::getId, tagId)
                .eq(Tag::getDeleted, NOT_DELETED)
                .set(Tag::getName, normalizedName)
                .set(Tag::getColor, normalizedColor)
                .set(Tag::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "标签更新失败，请稍后重试");
        }
    }

    /**
     * 删除标签（逻辑删除）
     *
     * @param tagId 标签ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long tagId) {
        Tag currentTag = queryAvailableTagById(tagId);
        if (currentTag.getUseCount() != null && currentTag.getUseCount() > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标签正在使用中，无法删除");
        }

        int updatedRows = tagMapper.update(new Tag(), new LambdaUpdateWrapper<Tag>()
                .eq(Tag::getId, tagId)
                .eq(Tag::getDeleted, NOT_DELETED)
                .eq(Tag::getUseCount, 0)
                .set(Tag::getDeleted, DELETED)
                .set(Tag::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "标签删除失败，请刷新后重试");
        }
    }

    /**
     * 批量删除标签（逻辑删除）
     *
     * @param batchDeleteDTO 批量参数
     * @return 删除数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteTags(AdminTagBatchDeleteDTO batchDeleteDTO) {
        List<Long> normalizedTagIds = batchDeleteDTO.getTagIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(normalizedTagIds)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标签ID列表不能为空");
        }

        List<Tag> tagList = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .in(Tag::getId, normalizedTagIds)
                .eq(Tag::getDeleted, NOT_DELETED));
        if (tagList.size() != normalizedTagIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "存在无效或已删除标签，无法批量删除");
        }

        List<String> inUseTagNames = tagList.stream()
                .filter(tag -> tag.getUseCount() != null && tag.getUseCount() > 0)
                .map(Tag::getName)
                .toList();
        if (CollUtil.isNotEmpty(inUseTagNames)) {
            String joinedNames = inUseTagNames.stream().limit(10).collect(Collectors.joining("、"));
            throw new BusinessException(HttpStatus.BAD_REQUEST, "以下标签正在使用中，无法删除：" + joinedNames);
        }

        int deletedCount = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Long tagId : normalizedTagIds) {
            int affectedRows = tagMapper.update(new Tag(), new LambdaUpdateWrapper<Tag>()
                    .eq(Tag::getId, tagId)
                    .eq(Tag::getDeleted, NOT_DELETED)
                    .eq(Tag::getUseCount, 0)
                    .set(Tag::getDeleted, DELETED)
                    .set(Tag::getUpdateTime, now));
            if (affectedRows != 1) {
                throw new BusinessException(HttpStatus.ERROR, "批量删除失败，请刷新后重试");
            }
            deletedCount++;
        }
        return deletedCount;
    }

    /**
     * 构建标签查询条件
     *
     * @param queryDTO 查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<Tag> buildTagQueryWrapper(AdminTagQueryDTO queryDTO) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getDeleted, NOT_DELETED);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String normalizedKeyword = queryDTO.getKeyword().trim();
            queryWrapper.like(Tag::getName, normalizedKeyword);
        }
        return queryWrapper;
    }

    /**
     * 添加排序条件
     *
     * @param queryWrapper 查询条件
     * @param sortField 排序字段
     * @param sortOrder 排序方向
     */
    private void appendSortCondition(LambdaQueryWrapper<Tag> queryWrapper, String sortField, Integer sortOrder) {
        int normalizedSortOrder = normalizeSortOrder(sortOrder);
        boolean asc = normalizedSortOrder == SORT_ASC;
        String normalizedSortField = normalizeSortField(sortField);

        switch (normalizedSortField) {
            case SORT_FIELD_ID -> queryWrapper.orderBy(true, asc, Tag::getId);
            case SORT_FIELD_NAME -> queryWrapper.orderBy(true, asc, Tag::getName);
            case SORT_FIELD_COLOR -> queryWrapper.orderBy(true, asc, Tag::getColor);
            case SORT_FIELD_USE_COUNT -> queryWrapper.orderBy(true, asc, Tag::getUseCount);
            case SORT_FIELD_CREATE_TIME -> queryWrapper.orderBy(true, asc, Tag::getCreateTime);
            default -> queryWrapper.orderBy(true, asc, Tag::getUpdateTime);
        }
    }

    /**
     * 查询可操作标签
     *
     * @param tagId 标签ID
     * @return 标签
     */
    private Tag queryAvailableTagById(Long tagId) {
        Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getId, tagId)
                .eq(Tag::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (tag == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标签不存在或已删除");
        }
        return tag;
    }

    /**
     * 统计标签数量
     *
     * @param conditionAppender 额外条件
     * @return 数量
     */
    private long countByCondition(java.util.function.Consumer<LambdaQueryWrapper<Tag>> conditionAppender) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getDeleted, NOT_DELETED);
        if (conditionAppender != null) {
            conditionAppender.accept(queryWrapper);
        }
        return tagMapper.selectCount(queryWrapper);
    }

    /**
     * 标准化标签名称
     *
     * @param name 原始名称
     * @return 标准化名称
     */
    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }

    /**
     * 标准化颜色值
     *
     * @param color 原始颜色
     * @return 标准化颜色
     */
    private String normalizeColor(String color) {
        return color == null ? "" : color.trim().toUpperCase();
    }

    /**
     * 标准化排序字段
     *
     * @param sortField 排序字段
     * @return 排序字段
     */
    private String normalizeSortField(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return SORT_FIELD_UPDATE_TIME;
        }
        return sortField.trim();
    }

    /**
     * 标准化排序方向
     *
     * @param sortOrder 排序方向
     * @return 排序方向
     */
    private int normalizeSortOrder(Integer sortOrder) {
        if (sortOrder == null) {
            return SORT_DESC;
        }
        if (sortOrder != SORT_ASC && sortOrder != SORT_DESC) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "排序方向不合法");
        }
        return sortOrder;
    }

    /**
     * 计算总页数
     *
     * @param total 总条数
     * @param pageSize 每页大小
     * @return 总页数
     */
    private long calcTotalPages(long total, int pageSize) {
        if (total == 0) {
            return 0;
        }
        return (total + pageSize - 1L) / pageSize;
    }
}
