package com.yyyouth.service.user.website;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserWebsiteSubmissionCreateDTO;
import com.yyyouth.model.dto.user.UserWebsiteSubmissionQueryDTO;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.file.MinioUploadVO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionPageVO;
import com.yyyouth.model.vo.user.UserWebsiteSubmissionIconUploadVO;
import com.yyyouth.model.vo.user.UserWebsiteTagItemVO;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.website.impl.UserWebsiteSubmissionServiceImpl;
import com.yyyouth.service.user.website.support.UserWebsiteTagSupport;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 用户投稿网站服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserWebsiteSubmissionServiceImplTest {

    private static final Long LOGIN_USER_ID = 1001L;

    @Mock
    private WebsiteMapper websiteMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private MinioFileService minioFileService;

    @Mock
    private UserWebsiteTagSupport userWebsiteTagSupport;

    @InjectMocks
    private UserWebsiteSubmissionServiceImpl userWebsiteSubmissionService;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, Website.class);
        TableInfoHelper.initTableInfo(builderAssistant, WebsiteCategory.class);
    }

    /**
     * 投稿成功时应写入待审核数据并返回网站ID
     */
    @Test
    void shouldSubmitWebsiteSuccessfully() {
        UserWebsiteSubmissionCreateDTO createDTO = new UserWebsiteSubmissionCreateDTO();
        createDTO.setName("前端导航");
        createDTO.setUrl("https://example.com");
        createDTO.setIcon("https://example.com/logo.png");
        createDTO.setSummary("前端工具合集");
        createDTO.setDescription("收录常用前端站点");
        createDTO.setCategoryId(1L);
        createDTO.setTags("101, 102, 103");

        WebsiteCategory category = new WebsiteCategory();
        category.setId(1L);
        category.setStatus(1);
        category.setDeleted(0);

        when(categoryMapper.selectOne(any())).thenReturn(category);
        when(websiteMapper.insert(any(Website.class))).thenAnswer(invocation -> {
            Website website = invocation.getArgument(0);
            website.setId(3001L);
            return 1;
        });

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            Long websiteId = userWebsiteSubmissionService.submitWebsite(createDTO);
            assertThat(websiteId).isEqualTo(3001L);
        }

        ArgumentCaptor<Website> websiteCaptor = ArgumentCaptor.forClass(Website.class);
        verify(websiteMapper).insert(websiteCaptor.capture());

        Website insertedWebsite = websiteCaptor.getValue();
        assertThat(insertedWebsite.getName()).isEqualTo("前端导航");
        assertThat(insertedWebsite.getSource()).isEqualTo(1);
        assertThat(insertedWebsite.getSubmitterId()).isEqualTo(LOGIN_USER_ID);
        assertThat(insertedWebsite.getAuditStatus()).isEqualTo(0);
        assertThat(insertedWebsite.getStatus()).isEqualTo(0);
        assertThat(insertedWebsite.getDeleted()).isEqualTo(0);
    }

    /**
     * 上传投稿图标成功时应返回对象键与访问地址
     */
    @Test
    void shouldUploadSubmissionIconSuccessfully() {
        org.springframework.web.multipart.MultipartFile file = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);

        MinioUploadVO uploadVO = new MinioUploadVO();
        uploadVO.setObjectKey("user/website/icon/202604/test.png");
        uploadVO.setFileUrl("https://minio.local/user/website/icon/202604/test.png");
        when(minioFileService.uploadImageByMonth(file, "user", "website", "icon", 1024 * 1024L))
                .thenReturn(uploadVO);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            UserWebsiteSubmissionIconUploadVO result = userWebsiteSubmissionService.uploadSubmissionIcon(file);
            assertThat(result.getObjectKey()).isEqualTo("user/website/icon/202604/test.png");
            assertThat(result.getIconUrl()).isEqualTo("https://minio.local/user/website/icon/202604/test.png");
        }
    }

    /**
     * 删除投稿图标时对象键前缀不合法应抛出业务异常
     */
    @Test
    void shouldThrowWhenDeleteSubmissionIconWithInvalidObjectKey() {
        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            assertThatThrownBy(() -> userWebsiteSubmissionService.deleteSubmissionIcon("admin/website/logo/test.png"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("图标对象键不合法");
        }
    }

    /**
     * 删除投稿图标成功时应调用 MinIO 删除
     */
    @Test
    void shouldDeleteSubmissionIconSuccessfully() {
        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            userWebsiteSubmissionService.deleteSubmissionIcon(" user/website/icon/202604/test.png ");
        }

        verify(minioFileService).deleteByObjectKey("user/website/icon/202604/test.png");
    }

    /**
     * 重提时若记录并非已拒绝应抛出业务异常
     */
    @Test
    void shouldThrowWhenResubmitNonRejectedSubmission() {
        Website website = new Website();
        website.setId(1L);
        website.setSource(1);
        website.setDeleted(0);
        website.setSubmitterId(LOGIN_USER_ID);
        website.setAuditStatus(1);

        when(websiteMapper.selectOne(any())).thenReturn(website);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            assertThatThrownBy(() -> userWebsiteSubmissionService.resubmitMySubmission(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("仅已拒绝的投稿可重新提交");
        }
    }

    /**
     * 已拒绝投稿应允许重提并回到待审核状态
     */
    @Test
    void shouldResubmitRejectedSubmissionSuccessfully() {
        Website website = new Website();
        website.setId(10L);
        website.setSource(1);
        website.setDeleted(0);
        website.setSubmitterId(LOGIN_USER_ID);
        website.setAuditStatus(2);

        when(websiteMapper.selectOne(any())).thenReturn(website);
        when(websiteMapper.updateById(any(Website.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);
            userWebsiteSubmissionService.resubmitMySubmission(10L);
        }

        ArgumentCaptor<Website> websiteCaptor = ArgumentCaptor.forClass(Website.class);
        verify(websiteMapper).updateById(websiteCaptor.capture());

        Website updateEntity = websiteCaptor.getValue();
        assertThat(updateEntity.getId()).isEqualTo(10L);
        assertThat(updateEntity.getAuditStatus()).isEqualTo(0);
        assertThat(updateEntity.getStatus()).isEqualTo(0);
        assertThat(updateEntity.getAuditAdminId()).isEqualTo(0);
        assertThat(updateEntity.getAuditRemark()).isEqualTo("");
    }

    /**
     * 待审核投稿应允许编辑并更新基础字段
     */
    @Test
    void shouldEditPendingSubmissionSuccessfully() {
        UserWebsiteSubmissionCreateDTO editDTO = new UserWebsiteSubmissionCreateDTO();
        editDTO.setName("更新后网站");
        editDTO.setUrl("https://updated.example.com");
        editDTO.setIcon("https://updated.example.com/logo.png");
        editDTO.setGithubUrl("https://github.com/example/updated");
        editDTO.setSummary("更新后的摘要");
        editDTO.setDescription("更新后的详细描述");
        editDTO.setCategoryId(3L);
        editDTO.setTags("301, 302");

        Website submission = new Website();
        submission.setId(40L);
        submission.setSource(1);
        submission.setDeleted(0);
        submission.setSubmitterId(LOGIN_USER_ID);
        submission.setAuditStatus(0);

        WebsiteCategory category = new WebsiteCategory();
        category.setId(3L);
        category.setStatus(1);
        category.setDeleted(0);

        when(websiteMapper.selectOne(any())).thenReturn(submission);
        when(categoryMapper.selectOne(any())).thenReturn(category);
        when(websiteMapper.updateById(any(Website.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);
            userWebsiteSubmissionService.editMySubmission(40L, editDTO);
        }

        ArgumentCaptor<Website> websiteCaptor = ArgumentCaptor.forClass(Website.class);
        verify(websiteMapper).updateById(websiteCaptor.capture());

        Website updateEntity = websiteCaptor.getValue();
        assertThat(updateEntity.getId()).isEqualTo(40L);
        assertThat(updateEntity.getName()).isEqualTo("更新后网站");
        assertThat(updateEntity.getUrl()).isEqualTo("https://updated.example.com");
        assertThat(updateEntity.getCategoryId()).isEqualTo(3L);
        assertThat(updateEntity.getTags()).isEqualTo("301,302");
        assertThat(updateEntity.getUpdateTime()).isNotNull();
    }

    /**
     * 已拒绝投稿编辑应被拦截
     */
    @Test
    void shouldThrowWhenEditRejectedSubmission() {
        UserWebsiteSubmissionCreateDTO editDTO = new UserWebsiteSubmissionCreateDTO();
        editDTO.setName("更新网站");
        editDTO.setUrl("https://edit.example.com");
        editDTO.setSummary("更新摘要");
        editDTO.setCategoryId(1L);

        Website submission = new Website();
        submission.setId(41L);
        submission.setSource(1);
        submission.setDeleted(0);
        submission.setSubmitterId(LOGIN_USER_ID);
        submission.setAuditStatus(2);

        when(websiteMapper.selectOne(any())).thenReturn(submission);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            assertThatThrownBy(() -> userWebsiteSubmissionService.editMySubmission(41L, editDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("仅待审核的投稿可编辑");
        }

        verify(categoryMapper, never()).selectOne(any());
        verify(websiteMapper, never()).updateById(any(Website.class));
    }

    /**
     * 已通过投稿编辑应被拦截
     */
    @Test
    void shouldThrowWhenEditApprovedSubmission() {
        UserWebsiteSubmissionCreateDTO editDTO = new UserWebsiteSubmissionCreateDTO();
        editDTO.setName("更新网站");
        editDTO.setUrl("https://edit.example.com");
        editDTO.setSummary("更新摘要");
        editDTO.setCategoryId(1L);

        Website submission = new Website();
        submission.setId(42L);
        submission.setSource(1);
        submission.setDeleted(0);
        submission.setSubmitterId(LOGIN_USER_ID);
        submission.setAuditStatus(1);

        when(websiteMapper.selectOne(any())).thenReturn(submission);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            assertThatThrownBy(() -> userWebsiteSubmissionService.editMySubmission(42L, editDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("仅待审核的投稿可编辑");
        }

        verify(categoryMapper, never()).selectOne(any());
        verify(websiteMapper, never()).updateById(any(Website.class));
    }

    /**
     * 投稿编辑更新失败时应抛出业务异常
     */
    @Test
    void shouldThrowWhenEditSubmissionUpdateFailed() {
        UserWebsiteSubmissionCreateDTO editDTO = new UserWebsiteSubmissionCreateDTO();
        editDTO.setName("更新网站");
        editDTO.setUrl("https://edit.example.com");
        editDTO.setSummary("更新摘要");
        editDTO.setCategoryId(4L);
        editDTO.setTags("401,402");

        Website submission = new Website();
        submission.setId(43L);
        submission.setSource(1);
        submission.setDeleted(0);
        submission.setSubmitterId(LOGIN_USER_ID);
        submission.setAuditStatus(0);

        WebsiteCategory category = new WebsiteCategory();
        category.setId(4L);
        category.setStatus(1);
        category.setDeleted(0);

        when(websiteMapper.selectOne(any())).thenReturn(submission);
        when(categoryMapper.selectOne(any())).thenReturn(category);
        when(websiteMapper.updateById(any(Website.class))).thenReturn(0);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            assertThatThrownBy(() -> userWebsiteSubmissionService.editMySubmission(43L, editDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("投稿编辑失败，请稍后重试");
        }
    }

    /**
     * 仅待审核和已拒绝投稿允许取消，已通过投稿取消应抛出异常
     */
    @Test
    void shouldThrowWhenCancelApprovedSubmission() {
        Website website = new Website();
        website.setId(20L);
        website.setSource(1);
        website.setDeleted(0);
        website.setSubmitterId(LOGIN_USER_ID);
        website.setAuditStatus(1);

        when(websiteMapper.selectOne(any())).thenReturn(website);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            assertThatThrownBy(() -> userWebsiteSubmissionService.cancelMySubmission(20L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("仅待审核和已拒绝的投稿可取消");
        }

        verify(websiteMapper, never()).updateById(any(Website.class));
    }

    /**
     * 待审核投稿取消成功时应执行逻辑删除
     */
    @Test
    void shouldCancelPendingSubmissionSuccessfully() {
        Website website = new Website();
        website.setId(21L);
        website.setSource(1);
        website.setDeleted(0);
        website.setSubmitterId(LOGIN_USER_ID);
        website.setAuditStatus(0);

        when(websiteMapper.selectOne(any())).thenReturn(website);
        when(websiteMapper.updateById(any(Website.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);
            userWebsiteSubmissionService.cancelMySubmission(21L);
        }

        ArgumentCaptor<Website> websiteCaptor = ArgumentCaptor.forClass(Website.class);
        verify(websiteMapper).updateById(websiteCaptor.capture());

        Website updateEntity = websiteCaptor.getValue();
        assertThat(updateEntity.getId()).isEqualTo(21L);
        assertThat(updateEntity.getDeleted()).isEqualTo(1);
        assertThat(updateEntity.getUpdateTime()).isNotNull();
    }

    /**
     * 已拒绝投稿取消成功时应执行逻辑删除
     */
    @Test
    void shouldCancelRejectedSubmissionSuccessfully() {
        Website website = new Website();
        website.setId(22L);
        website.setSource(1);
        website.setDeleted(0);
        website.setSubmitterId(LOGIN_USER_ID);
        website.setAuditStatus(2);

        when(websiteMapper.selectOne(any())).thenReturn(website);
        when(websiteMapper.updateById(any(Website.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);
            userWebsiteSubmissionService.cancelMySubmission(22L);
        }

        ArgumentCaptor<Website> websiteCaptor = ArgumentCaptor.forClass(Website.class);
        verify(websiteMapper).updateById(websiteCaptor.capture());

        Website updateEntity = websiteCaptor.getValue();
        assertThat(updateEntity.getId()).isEqualTo(22L);
        assertThat(updateEntity.getDeleted()).isEqualTo(1);
        assertThat(updateEntity.getUpdateTime()).isNotNull();
    }

    /**
     * 取消投稿更新失败时应抛出业务异常
     */
    @Test
    void shouldThrowWhenCancelSubmissionUpdateFailed() {
        Website website = new Website();
        website.setId(23L);
        website.setSource(1);
        website.setDeleted(0);
        website.setSubmitterId(LOGIN_USER_ID);
        website.setAuditStatus(0);

        when(websiteMapper.selectOne(any())).thenReturn(website);
        when(websiteMapper.updateById(any(Website.class))).thenReturn(0);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            assertThatThrownBy(() -> userWebsiteSubmissionService.cancelMySubmission(23L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("取消投稿失败，请稍后重试");
        }
    }

    /**
     * 投稿分页记录标签字段应返回标签对象列表而不是标签ID字符串
     */
    @Test
    void shouldReturnTagObjectsInSubmissionPageRecord() throws Exception {
        Website website = new Website();
        website.setId(30L);
        website.setSource(1);
        website.setDeleted(0);
        website.setSubmitterId(LOGIN_USER_ID);
        website.setName("测试网站");
        website.setCategoryId(1L);
        website.setTags("101,102");

        WebsiteCategory category = new WebsiteCategory();
        category.setId(1L);
        category.setName("编程语言");

        UserWebsiteSubmissionQueryDTO queryDTO = new UserWebsiteSubmissionQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(12);

        when(websiteMapper.selectCount(any())).thenReturn(1L);
        when(websiteMapper.selectList(any())).thenReturn(List.of(website));
        when(categoryMapper.selectBatchIds(any())).thenReturn(List.of(category));

        UserWebsiteTagItemVO firstTag = new UserWebsiteTagItemVO();
        firstTag.setId(101L);
        firstTag.setName("编译工具");
        firstTag.setColor("#409EFF");

        UserWebsiteTagItemVO secondTag = new UserWebsiteTagItemVO();
        secondTag.setId(102L);
        secondTag.setName("测试框架");
        secondTag.setColor("#67C23A");

        List<UserWebsiteTagItemVO> tagItems = List.of(firstTag, secondTag);
        when(userWebsiteTagSupport.parseTagIds(any())).thenReturn(List.of(101L, 102L));
        when(userWebsiteTagSupport.buildTagItemMap(any(java.util.Set.class))).thenReturn(
            java.util.Map.of(
                101L, firstTag,
                102L, secondTag
            )
        );
        when(userWebsiteTagSupport.buildWebsiteTagItems(any(), any())).thenReturn(tagItems);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);
            UserWebsiteSubmissionPageVO pageVO = userWebsiteSubmissionService.queryMySubmissionPage(queryDTO);
            Object firstRecord = pageVO.getRecords().get(0);
            Field tagsField = firstRecord.getClass().getDeclaredField("tags");
            tagsField.setAccessible(true);
            Object tagsValue = tagsField.get(firstRecord);
            assertThat(tagsValue)
                    .withFailMessage("投稿分页 tags 字段应为标签对象列表，而不是标签ID字符串")
                    .isInstanceOf(List.class);

                List<?> tagList = (List<?>) tagsValue;
                assertThat(tagList).isNotEmpty();
                Object firstTagRecord = tagList.get(0);
                Field tagNameField = firstTagRecord.getClass().getDeclaredField("name");
                tagNameField.setAccessible(true);
                assertThat(tagNameField.get(firstTagRecord)).isEqualTo("编译工具");
        }
    }

    /**
     * 投稿详情标签字段应返回标签对象列表而不是标签ID字符串
     */
    @Test
    void shouldReturnTagObjectsInSubmissionDetail() throws Exception {
        Website website = new Website();
        website.setId(31L);
        website.setSource(1);
        website.setDeleted(0);
        website.setSubmitterId(LOGIN_USER_ID);
        website.setName("详情网站");
        website.setCategoryId(2L);
        website.setTags("103,104");

        WebsiteCategory category = new WebsiteCategory();
        category.setId(2L);
        category.setName("开发工具");

        when(websiteMapper.selectOne(any())).thenReturn(website);
        when(categoryMapper.selectById(2L)).thenReturn(category);

        UserWebsiteTagItemVO firstTag = new UserWebsiteTagItemVO();
        firstTag.setId(103L);
        firstTag.setName("前端框架");
        firstTag.setColor("#409EFF");

        UserWebsiteTagItemVO secondTag = new UserWebsiteTagItemVO();
        secondTag.setId(104L);
        secondTag.setName("样式工具");
        secondTag.setColor("#67C23A");

        List<UserWebsiteTagItemVO> tagItems = List.of(firstTag, secondTag);
        when(userWebsiteTagSupport.parseTagIds(any())).thenReturn(List.of(103L, 104L));
        when(userWebsiteTagSupport.buildTagItemMap(any(java.util.List.class))).thenReturn(
            java.util.Map.of(
                103L, firstTag,
                104L, secondTag
            )
        );
        when(userWebsiteTagSupport.buildWebsiteTagItems(any(), any())).thenReturn(tagItems);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(LOGIN_USER_ID);

            Object detailVO = userWebsiteSubmissionService.queryMySubmissionDetail(31L);
            Field tagsField = detailVO.getClass().getDeclaredField("tags");
            tagsField.setAccessible(true);
            Object tagsValue = tagsField.get(detailVO);
            assertThat(tagsValue)
                    .withFailMessage("投稿详情 tags 字段应为标签对象列表，而不是标签ID字符串")
                    .isInstanceOf(List.class);

            List<?> tagList = (List<?>) tagsValue;
            assertThat(tagList).isNotEmpty();
            Object firstTagRecord = tagList.get(0);
            Field tagNameField = firstTagRecord.getClass().getDeclaredField("name");
            tagNameField.setAccessible(true);
            assertThat(tagNameField.get(firstTagRecord)).isEqualTo("前端框架");
        }
    }
}
