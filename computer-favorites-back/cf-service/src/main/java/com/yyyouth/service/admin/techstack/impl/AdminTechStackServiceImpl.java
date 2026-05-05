package com.yyyouth.service.admin.techstack.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminTechStackBatchDeleteDTO;
import com.yyyouth.model.dto.admin.AdminTechStackCreateDTO;
import com.yyyouth.model.dto.admin.AdminTechStackEditDTO;
import com.yyyouth.model.dto.admin.AdminTechStackQueryDTO;
import com.yyyouth.model.pojo.user.TechStack;
import com.yyyouth.model.vo.admin.AdminTechStackIconUploadVO;
import com.yyyouth.model.vo.admin.AdminTechStackListItemVO;
import com.yyyouth.model.vo.admin.AdminTechStackPageVO;
import com.yyyouth.model.vo.admin.AdminTechStackStatsVO;
import com.yyyouth.model.vo.file.MinioUploadVO;
import com.yyyouth.service.admin.techstack.AdminTechStackService;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.mapper.user.TechStackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author yyyouth zg
 * @date 2026-05-05
 *
 * 管理端技术栈服务实现
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AdminTechStackServiceImpl implements AdminTechStackService {

    private static final int NOT_DELETED = 0;

    private static final int DELETED = 1;

    private static final int SORT_ASC = 1;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 8;

    private static final String SORT_FIELD_SORT = "sort";

    private static final String SORT_FIELD_NAME = "name";

    private static final String SORT_FIELD_STATUS = "status";

    private static final String SORT_FIELD_CREATED_AT = "createdAt";

    private static final String SORT_FIELD_UPDATED_AT = "updatedAt";

    private static final long MAX_ICON_FILE_SIZE = 2 * 1024 * 1024;

    private static final String ICON_PATH_MODULE = "admin";

    private static final String ICON_PATH_BUSINESS = "tech-stack";

    private static final String ICON_PATH_PURPOSE = "icon";

    private static final String ICON_OBJECT_KEY_PREFIX = ICON_PATH_MODULE + "/" + ICON_PATH_BUSINESS + "/" + ICON_PATH_PURPOSE + "/";

    private final TechStackMapper techStackMapper;

    private final MinioFileService minioFileService;

    /**
     * 查询技术栈分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @Override
    public AdminTechStackPageVO queryTechStackPage(AdminTechStackQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null ? DEFAULT_PAGE_NUM : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? DEFAULT_PAGE_SIZE : queryDTO.getPageSize();

        String keyword = StringUtils.hasText(queryDTO.getKeyword()) ? queryDTO.getKeyword().trim() : null;
        long total = techStackMapper.countTechStackPage(keyword, queryDTO.getStatus());

        AdminTechStackPageVO pageVO = new AdminTechStackPageVO();
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalPages(calcTotalPages(total, pageSize));

        if (total == 0) {
            pageVO.setRecords(Collections.emptyList());
            return pageVO;
        }

        int offset = (pageNum - 1) * pageSize;
        String sortSql = buildSortSql(queryDTO.getSortField(), queryDTO.getSortOrder());

        List<Map<String, Object>> rows = techStackMapper.selectTechStackPageWithUserCount(
                keyword, queryDTO.getStatus(), sortSql, offset, pageSize);

        List<AdminTechStackListItemVO> itemVOS = rows.stream()
                .map(this::mapRowToListItemVO)
                .toList();
        pageVO.setRecords(itemVOS);
        return pageVO;
    }

    /**
     * 查询技术栈统计
     *
     * @return 技术栈统计
     */
    @Override
    public AdminTechStackStatsVO queryTechStackStats() {
        long total = countByCondition(null);
        long enabled = countByCondition(wrapper -> wrapper.eq(TechStack::getStatus, 1));
        long disabled = Math.max(total - enabled, 0);

        AdminTechStackStatsVO statsVO = new AdminTechStackStatsVO();
        statsVO.setTotal(total);
        statsVO.setEnabled(enabled);
        statsVO.setDisabled(disabled);
        return statsVO;
    }

    /**
     * 创建技术栈
     *
     * @param createDTO 创建参数
     * @return 技术栈ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTechStack(AdminTechStackCreateDTO createDTO) {
        String normalizedName = normalizeName(createDTO.getName());

        TechStack existedTechStack = techStackMapper.selectOne(new LambdaQueryWrapper<TechStack>()
                .eq(TechStack::getName, normalizedName)
                .last("limit 1"));
        if (existedTechStack != null) {
            if (Objects.equals(existedTechStack.getDeleted(), NOT_DELETED)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "技术栈名称已存在");
            }

            int restoredRows = techStackMapper.update(new TechStack(), new LambdaUpdateWrapper<TechStack>()
                    .eq(TechStack::getId, existedTechStack.getId())
                    .eq(TechStack::getDeleted, DELETED)
                    .set(TechStack::getDeleted, NOT_DELETED)
                    .set(TechStack::getIconPng, createDTO.getIconPng())
                    .set(TechStack::getOfficialUrl, createDTO.getOfficialUrl())
                    .set(TechStack::getDescription, createDTO.getDescription())
                    .set(TechStack::getColor, createDTO.getColor())
                    .set(TechStack::getSort, createDTO.getSort())
                    .set(TechStack::getStatus, 1)
                    .set(TechStack::getUpdateTime, LocalDateTime.now()));
            if (restoredRows != 1) {
                throw new BusinessException(HttpStatus.ERROR, "技术栈恢复失败，请稍后重试");
            }
            return existedTechStack.getId();
        }

        TechStack techStack = new TechStack();
        LocalDateTime now = LocalDateTime.now();
        techStack.setName(normalizedName);
        techStack.setIconPng(createDTO.getIconPng());
        techStack.setOfficialUrl(createDTO.getOfficialUrl());
        techStack.setDescription(createDTO.getDescription());
        techStack.setColor(createDTO.getColor());
        techStack.setSort(createDTO.getSort());
        techStack.setStatus(1);
        techStack.setDeleted(NOT_DELETED);
        techStack.setCreateTime(now);
        techStack.setUpdateTime(now);

        int insertedRows = techStackMapper.insert(techStack);
        if (insertedRows != 1 || techStack.getId() == null) {
            throw new BusinessException(HttpStatus.ERROR, "技术栈创建失败，请稍后重试");
        }
        return techStack.getId();
    }

    /**
     * 编辑技术栈
     *
     * @param id 技术栈ID
     * @param editDTO 编辑参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editTechStack(Long id, AdminTechStackEditDTO editDTO) {
        TechStack currentTechStack = queryAvailableTechStackById(id);

        String normalizedName = normalizeName(editDTO.getName());

        if (!Objects.equals(currentTechStack.getName(), normalizedName)) {
            TechStack duplicatedTechStack = techStackMapper.selectOne(new LambdaQueryWrapper<TechStack>()
                    .eq(TechStack::getName, normalizedName)
                    .ne(TechStack::getId, id)
                    .last("limit 1"));
            if (duplicatedTechStack != null) {
                if (Objects.equals(duplicatedTechStack.getDeleted(), NOT_DELETED)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "技术栈名称已存在");
                }
                throw new BusinessException(HttpStatus.BAD_REQUEST, "技术栈名称已被历史记录占用，请使用其他名称");
            }
        }

        if (Objects.equals(currentTechStack.getName(), normalizedName)
                && Objects.equals(currentTechStack.getIconPng(), editDTO.getIconPng())
                && Objects.equals(currentTechStack.getOfficialUrl(), editDTO.getOfficialUrl())
                && Objects.equals(currentTechStack.getDescription(), editDTO.getDescription())
                && Objects.equals(currentTechStack.getColor(), editDTO.getColor())
                && Objects.equals(currentTechStack.getSort(), editDTO.getSort())
                && Objects.equals(currentTechStack.getStatus(), editDTO.getStatus())) {
            return;
        }

        int updatedRows = techStackMapper.update(new TechStack(), new LambdaUpdateWrapper<TechStack>()
                .eq(TechStack::getId, id)
                .eq(TechStack::getDeleted, NOT_DELETED)
                .set(TechStack::getName, normalizedName)
                .set(TechStack::getIconPng, editDTO.getIconPng())
                .set(TechStack::getOfficialUrl, editDTO.getOfficialUrl())
                .set(TechStack::getDescription, editDTO.getDescription())
                .set(TechStack::getColor, editDTO.getColor())
                .set(TechStack::getSort, editDTO.getSort())
                .set(TechStack::getStatus, editDTO.getStatus())
                .set(TechStack::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "技术栈更新失败，请稍后重试");
        }
    }

    /**
     * 更新技术栈状态
     *
     * @param id 技术栈ID
     * @param status 目标状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTechStackStatus(Long id, Integer status) {
        TechStack currentTechStack = queryAvailableTechStackById(id);

        if (Objects.equals(currentTechStack.getStatus(), status)) {
            log.info("技术栈状态未变化，跳过更新，id={}，status={}", id, status);
            return;
        }

        int updatedRows = techStackMapper.update(new TechStack(), new LambdaUpdateWrapper<TechStack>()
                .eq(TechStack::getId, id)
                .eq(TechStack::getDeleted, NOT_DELETED)
                .set(TechStack::getStatus, status)
                .set(TechStack::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "技术栈状态更新失败，请稍后重试");
        }
    }

    /**
     * 删除技术栈（逻辑删除）
     *
     * @param id 技术栈ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTechStack(Long id) {
        queryAvailableTechStackById(id);

        int updatedRows = techStackMapper.update(new TechStack(), new LambdaUpdateWrapper<TechStack>()
                .eq(TechStack::getId, id)
                .eq(TechStack::getDeleted, NOT_DELETED)
                .set(TechStack::getDeleted, DELETED)
                .set(TechStack::getUpdateTime, LocalDateTime.now()));
        if (updatedRows != 1) {
            throw new BusinessException(HttpStatus.ERROR, "技术栈删除失败，请刷新后重试");
        }
    }

    /**
     * 批量删除技术栈（逻辑删除）
     *
     * @param batchDeleteDTO 批量参数
     * @return 删除数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteTechStacks(AdminTechStackBatchDeleteDTO batchDeleteDTO) {
        List<Long> normalizedIds = batchDeleteDTO.getTechStackIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollUtil.isEmpty(normalizedIds)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "技术栈ID列表不能为空");
        }

        List<TechStack> techStackList = techStackMapper.selectList(new LambdaQueryWrapper<TechStack>()
                .in(TechStack::getId, normalizedIds)
                .eq(TechStack::getDeleted, NOT_DELETED));
        if (techStackList.size() != normalizedIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "存在无效或已删除的技术栈，无法批量删除");
        }

        int deletedCount = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Long id : normalizedIds) {
            int affectedRows = techStackMapper.update(new TechStack(), new LambdaUpdateWrapper<TechStack>()
                    .eq(TechStack::getId, id)
                    .eq(TechStack::getDeleted, NOT_DELETED)
                    .set(TechStack::getDeleted, DELETED)
                    .set(TechStack::getUpdateTime, now));
            if (affectedRows != 1) {
                throw new BusinessException(HttpStatus.ERROR, "批量删除失败，请刷新后重试");
            }
            deletedCount++;
        }
        return deletedCount;
    }

    /**
     * 上传技术栈图标
     *
     * @param file 图标文件
     * @return 上传结果
     */
    @Override
    public AdminTechStackIconUploadVO uploadTechStackIcon(MultipartFile file) {
        MinioUploadVO uploadVO = minioFileService.uploadImageByMonth(
                file,
                ICON_PATH_MODULE,
                ICON_PATH_BUSINESS,
                ICON_PATH_PURPOSE,
                MAX_ICON_FILE_SIZE
        );
        AdminTechStackIconUploadVO iconUploadVO = new AdminTechStackIconUploadVO();
        iconUploadVO.setObjectKey(uploadVO.getObjectKey());
        iconUploadVO.setIconUrl(uploadVO.getFileUrl());
        return iconUploadVO;
    }

    /**
     * 删除技术栈图标
     *
     * @param objectKey 对象键
     */
    @Override
    public void deleteTechStackIcon(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        String normalizedObjectKey = objectKey.trim();
        if (!normalizedObjectKey.startsWith(ICON_OBJECT_KEY_PREFIX)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "图标对象键不合法");
        }
        minioFileService.deleteByObjectKey(normalizedObjectKey);
    }

    /**
     * 构建排序SQL片段（白名单映射，防止SQL注入）
     *
     * @param sortField 排序字段
     * @param sortOrder 排序方向
     * @return 安全的排序SQL片段
     */
    private String buildSortSql(String sortField, Integer sortOrder) {
        int order = (sortOrder != null && sortOrder == SORT_ASC) ? SORT_ASC : -1;
        String direction = order == SORT_ASC ? "ASC" : "DESC";
        String field = StringUtils.hasText(sortField) ? sortField.trim() : SORT_FIELD_SORT;

        return switch (field) {
            case SORT_FIELD_SORT -> "ts.sort ASC, ts.create_time DESC";
            case SORT_FIELD_NAME -> "ts.name " + direction;
            case SORT_FIELD_STATUS -> "ts.status " + direction + ", ts.sort ASC";
            case SORT_FIELD_CREATED_AT -> "ts.create_time " + direction;
            case SORT_FIELD_UPDATED_AT -> "ts.update_time " + direction;
            default -> "ts.sort ASC, ts.create_time DESC";
        };
    }

    /**
     * 将数据库查询结果行映射为列表项VO
     *
     * @param row 数据库查询结果行
     * @return 列表项VO
     */
    private AdminTechStackListItemVO mapRowToListItemVO(Map<String, Object> row) {
        AdminTechStackListItemVO vo = new AdminTechStackListItemVO();
        vo.setId(toLong(row.get("id")));
        vo.setName((String) row.get("name"));
        vo.setIconPng((String) row.get("icon_png"));
        vo.setOfficialUrl((String) row.get("official_url"));
        vo.setDescription((String) row.get("description"));
        vo.setColor((String) row.get("color"));
        vo.setStatus(toInteger(row.get("status")));
        vo.setSort(toInteger(row.get("sort")));
        vo.setUserCount(toLong(row.get("user_count")));
        vo.setCreateTime(toLocalDateTime(row.get("create_time")));
        vo.setUpdateTime(toLocalDateTime(row.get("update_time")));
        return vo;
    }

    /**
     * 查询可操作的技术栈
     *
     * @param id 技术栈ID
     * @return 技术栈
     */
    private TechStack queryAvailableTechStackById(Long id) {
        TechStack techStack = techStackMapper.selectOne(new LambdaQueryWrapper<TechStack>()
                .eq(TechStack::getId, id)
                .eq(TechStack::getDeleted, NOT_DELETED)
                .last("limit 1"));
        if (techStack == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "技术栈不存在或已删除");
        }
        return techStack;
    }

    /**
     * 统计技术栈数量
     *
     * @param conditionAppender 额外条件
     * @return 数量
     */
    private long countByCondition(java.util.function.Consumer<LambdaQueryWrapper<TechStack>> conditionAppender) {
        LambdaQueryWrapper<TechStack> queryWrapper = new LambdaQueryWrapper<TechStack>()
                .eq(TechStack::getDeleted, NOT_DELETED);
        if (conditionAppender != null) {
            conditionAppender.accept(queryWrapper);
        }
        return techStackMapper.selectCount(queryWrapper);
    }

    /**
     * 标准化名称
     *
     * @param name 原始名称
     * @return 标准化名称
     */
    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
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

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long l) {
            return l;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        return null;
    }
}
