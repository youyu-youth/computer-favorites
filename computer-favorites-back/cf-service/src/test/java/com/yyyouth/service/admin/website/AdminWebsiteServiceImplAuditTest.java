package com.yyyouth.service.admin.website;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.model.dto.admin.AdminWebsiteAuditDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteBatchAuditDTO;
import com.yyyouth.model.dto.admin.AdminWebsiteEditDTO;
import com.yyyouth.model.dto.notification.NotifyEvent;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.admin.AdminWebsiteBatchAuditResultVO;
import com.yyyouth.service.admin.website.impl.AdminWebsiteServiceImpl;
import com.yyyouth.service.file.MinioFileService;
import com.yyyouth.service.notification.MessageNotifyService;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.TagMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.auth.support.StpAdminUtil;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-04-05
 *
 * 管理端网站审核服务测试
 */
@ExtendWith(MockitoExtension.class)
class AdminWebsiteServiceImplAuditTest {

    private static final long ADMIN_ID = 9001L;

    @Mock
    private WebsiteMapper websiteMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private MinioFileService minioFileService;

    @Mock
    private MessageNotifyService messageNotifyService;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private AdminAccountMapper adminAccountMapper;

    @InjectMocks
    private AdminWebsiteServiceImpl adminWebsiteService;

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
     * 单条通过审核应自动上架并写通知与审计
     */
    @Test
    void shouldApproveSubmissionAndAutoOnline() {
        Website pendingWebsite = new Website();
        pendingWebsite.setId(101L);
        pendingWebsite.setDeleted(0);
        pendingWebsite.setSource(1);
        pendingWebsite.setSubmitterId(1001L);
        pendingWebsite.setAuditStatus(0);
        pendingWebsite.setStatus(0);

        when(websiteMapper.selectOne(any())).thenReturn(pendingWebsite);
        when(websiteMapper.updateById(any(Website.class))).thenReturn(1);
        AdminWebsiteAuditDTO auditDTO = new AdminWebsiteAuditDTO();
        auditDTO.setAction(1);

        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::getLoginIdAsLong).thenReturn(ADMIN_ID);
            adminWebsiteService.auditWebsite(101L, auditDTO);
        }

        ArgumentCaptor<Website> updateCaptor = ArgumentCaptor.forClass(Website.class);
        verify(websiteMapper).updateById(updateCaptor.capture());

        Website updateEntity = updateCaptor.getValue();
        assertThat(updateEntity.getAuditStatus()).isEqualTo(1);
        assertThat(updateEntity.getStatus()).isEqualTo(1);
        assertThat(updateEntity.getAuditAdminId()).isEqualTo(Math.toIntExact(ADMIN_ID));

        verify(messageNotifyService).send(any(NotifyEvent.class));
    }

    /**
     * 单条驳回审核应写入驳回原因并保持下架
     */
    @Test
    void shouldRejectSubmissionWithRemark() {
        Website pendingWebsite = new Website();
        pendingWebsite.setId(102L);
        pendingWebsite.setDeleted(0);
        pendingWebsite.setSource(1);
        pendingWebsite.setSubmitterId(1002L);
        pendingWebsite.setAuditStatus(0);
        pendingWebsite.setStatus(0);

        when(websiteMapper.selectOne(any())).thenReturn(pendingWebsite);
        when(websiteMapper.updateById(any(Website.class))).thenReturn(1);
        AdminWebsiteAuditDTO auditDTO = new AdminWebsiteAuditDTO();
        auditDTO.setAction(2);
        auditDTO.setRemark("内容质量不符合规范");

        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::getLoginIdAsLong).thenReturn(ADMIN_ID);
            adminWebsiteService.auditWebsite(102L, auditDTO);
        }

        ArgumentCaptor<Website> updateCaptor = ArgumentCaptor.forClass(Website.class);
        verify(websiteMapper).updateById(updateCaptor.capture());

        Website updateEntity = updateCaptor.getValue();
        assertThat(updateEntity.getAuditStatus()).isEqualTo(2);
        assertThat(updateEntity.getStatus()).isEqualTo(0);
        assertThat(updateEntity.getAuditRemark()).isEqualTo("内容质量不符合规范");
    }

    /**
     * 批量审核应返回成功与失败明细
     */
    @Test
    void shouldBatchAuditWithFailedItems() {
        Website pendingWebsite = new Website();
        pendingWebsite.setId(201L);
        pendingWebsite.setDeleted(0);
        pendingWebsite.setSource(1);
        pendingWebsite.setSubmitterId(1101L);
        pendingWebsite.setAuditStatus(0);

        Website approvedWebsite = new Website();
        approvedWebsite.setId(202L);
        approvedWebsite.setDeleted(0);
        approvedWebsite.setSource(1);
        approvedWebsite.setSubmitterId(1102L);
        approvedWebsite.setAuditStatus(1);

        when(websiteMapper.selectList(any())).thenReturn(List.of(pendingWebsite, approvedWebsite));
        when(websiteMapper.updateById(any(Website.class))).thenReturn(1);
        AdminWebsiteBatchAuditDTO batchAuditDTO = new AdminWebsiteBatchAuditDTO();
        batchAuditDTO.setWebsiteIds(List.of(201L, 202L, 203L));
        batchAuditDTO.setAction(1);

        AdminWebsiteBatchAuditResultVO resultVO;
        try (MockedStatic<StpAdminUtil> stpAdminUtilMock = org.mockito.Mockito.mockStatic(StpAdminUtil.class)) {
            stpAdminUtilMock.when(StpAdminUtil::getLoginIdAsLong).thenReturn(ADMIN_ID);
            resultVO = adminWebsiteService.batchAuditWebsite(batchAuditDTO);
        }

        assertThat(resultVO.getSuccessCount()).isEqualTo(1);
        assertThat(resultVO.getFailedCount()).isEqualTo(2);
        assertThat(resultVO.getFailItems()).hasSize(2);
    }

    /**
     * 待审核配置保存时沿用原 Logo 地址不应触发 Logo 前缀校验
     */
    @Test
    void shouldEditPendingFieldsWithExternalIconWhenIconUnchanged() {
        Website pendingWebsite = new Website();
        pendingWebsite.setId(301L);
        pendingWebsite.setDeleted(0);
        pendingWebsite.setAuditStatus(0);
        pendingWebsite.setIcon("https://cdn.example.com/logo.png");

        WebsiteCategory enabledCategory = new WebsiteCategory();
        enabledCategory.setId(11L);
        enabledCategory.setStatus(1);

        when(websiteMapper.selectOne(any())).thenReturn(pendingWebsite);
        when(categoryMapper.selectOne(any())).thenReturn(enabledCategory);
        when(websiteMapper.updateById(any(Website.class))).thenReturn(1);

        AdminWebsiteEditDTO editDTO = new AdminWebsiteEditDTO();
        editDTO.setName("示例站点");
        editDTO.setUrl("https://example.com");
        editDTO.setIcon("https://cdn.example.com/logo.png");
        editDTO.setSummary("摘要");
        editDTO.setDescription("描述");
        editDTO.setCategoryId(11L);
        editDTO.setTags("1,2");
        editDTO.setIsTop(Boolean.FALSE);
        editDTO.setIsRecommend(Boolean.TRUE);
        editDTO.setIsOfficial(Boolean.TRUE);
        editDTO.setAuditRemark("待审核备注");
        editDTO.setSort(10);

        assertThatCode(() -> adminWebsiteService.editWebsite(301L, editDTO))
                .doesNotThrowAnyException();

        verify(websiteMapper).updateById(any(Website.class));
    }
}
