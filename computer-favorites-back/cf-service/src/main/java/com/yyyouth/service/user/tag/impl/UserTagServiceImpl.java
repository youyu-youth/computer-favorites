package com.yyyouth.service.user.tag.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yyyouth.model.dto.user.UserTagQueryDTO;
import com.yyyouth.model.pojo.website.Tag;
import com.yyyouth.model.vo.user.UserTagListItemVO;
import com.yyyouth.model.vo.user.UserTagPageVO;
import com.yyyouth.service.mapper.website.TagMapper;
import com.yyyouth.service.user.tag.UserTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端标签服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserTagServiceImpl implements UserTagService {

    private static final int NOT_DELETED = 0;

    private static final int SORT_ASC = 1;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 30;

    private static final String SORT_FIELD_ID = "id";

    private static final String SORT_FIELD_NAME = "name";

    private static final String SORT_FIELD_COLOR = "color";

    private static final String SORT_FIELD_USE_COUNT = "useCount";

    private static final String SORT_FIELD_CREATE_TIME = "createTime";

    private final TagMapper tagMapper;

    /**
     * 查询标签分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public UserTagPageVO queryTagPage(UserTagQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null ? DEFAULT_PAGE_NUM : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? DEFAULT_PAGE_SIZE : queryDTO.getPageSize();

        LambdaQueryWrapper<Tag> baseQueryWrapper = buildTagQueryWrapper(queryDTO);
        long total = tagMapper.selectCount(baseQueryWrapper);

        UserTagPageVO pageVO = new UserTagPageVO();
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
        List<UserTagListItemVO> itemVOS = tagList.stream()
                .map(tag -> BeanUtil.copyProperties(tag, UserTagListItemVO.class))
                .toList();
        pageVO.setRecords(itemVOS);
        return pageVO;
    }

    /**
     * 构建查询条件
     *
     * @param queryDTO 查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<Tag> buildTagQueryWrapper(UserTagQueryDTO queryDTO) {
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
        boolean asc = SORT_ASC == normalizeSortOrder(sortOrder);
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
     * 标准化排序字段
     *
     * @param sortField 原始排序字段
     * @return 标准化字段
     */
    private String normalizeSortField(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return SORT_FIELD_USE_COUNT;
        }
        return sortField.trim();
    }

    /**
     * 标准化排序方向
     *
     * @param sortOrder 原始排序方向
     * @return 标准化结果
     */
    private int normalizeSortOrder(Integer sortOrder) {
        if (sortOrder == null) {
            return -1;
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
    private Long calcTotalPages(long total, int pageSize) {
        return (total + pageSize - 1L) / pageSize;
    }
}