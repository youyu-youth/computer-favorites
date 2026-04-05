package com.yyyouth.service.user.website;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserWebsiteQueryDTO;
import com.yyyouth.model.pojo.admin.AdminAccount;
import com.yyyouth.model.pojo.auth.UserAccount;
import com.yyyouth.model.pojo.website.Tag;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.user.UserWebsiteCategoryVO;
import com.yyyouth.model.vo.user.UserWebsiteDetailVO;
import com.yyyouth.model.vo.user.UserWebsitePageVO;
import com.yyyouth.model.vo.user.UserWebsiteTagItemVO;
import com.yyyouth.service.mapper.admin.auth.AdminAccountMapper;
import com.yyyouth.service.mapper.user.auth.UserAccountMapper;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.TagMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import com.yyyouth.service.user.website.impl.UserWebsiteServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-04-04
 *
 * 用户端网站资源服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserWebsiteServiceImplTest {

    @Mock
    private WebsiteMapper websiteMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private AdminAccountMapper adminAccountMapper;

    @InjectMocks
    private UserWebsiteServiceImpl userWebsiteService;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, Website.class);
        TableInfoHelper.initTableInfo(builderAssistant, WebsiteCategory.class);
        TableInfoHelper.initTableInfo(builderAssistant, Tag.class);
        TableInfoHelper.initTableInfo(builderAssistant, UserAccount.class);
        TableInfoHelper.initTableInfo(builderAssistant, AdminAccount.class);
    }

    /**
     * 查询网站分页列表应返回分类与标签映射后的结果
     */
    @Test
    void shouldQueryWebsitePageSuccessfully() {
        UserWebsiteQueryDTO queryDTO = new UserWebsiteQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(12);
        queryDTO.setTagIds(List.of(101L));

        Website website = new Website();
        website.setId(1L);
        website.setName("Vue.js");
        website.setUrl("https://vuejs.org");
        website.setSummary("渐进式 JavaScript 框架");
        website.setDescription("Vue 3 框架官网");
        website.setCategoryId(10L);
        website.setClickCount(1200);
        website.setLikeCount(560);
        website.setCollectCount(340);
        website.setCommentCount(86);
        website.setScore(new BigDecimal("4.8"));
        website.setTags("101, 105, 106,101");

        WebsiteCategory category = new WebsiteCategory();
        category.setId(10L);
        category.setName("Web & Frontend Development");

        Tag vueTag = new Tag();
        vueTag.setId(101L);
        vueTag.setName("Vue3");
        vueTag.setColor("#42B883");

        Tag tailwindTag = new Tag();
        tailwindTag.setId(105L);
        tailwindTag.setName("Tailwind");
        tailwindTag.setColor("#38BDF8");

        Tag viteTag = new Tag();
        viteTag.setId(106L);
        viteTag.setName("Vite");
        viteTag.setColor("#646CFF");

        when(websiteMapper.selectCount(any())).thenReturn(1L);
        when(tagMapper.selectList(any())).thenReturn(List.of(vueTag, tailwindTag, viteTag));
        when(websiteMapper.selectList(any())).thenReturn(List.of(website));
        when(categoryMapper.selectBatchIds(any())).thenReturn(List.of(category));

        UserWebsitePageVO pageVO = userWebsiteService.queryWebsitePage(queryDTO);

        assertThat(pageVO.getTotal()).isEqualTo(1L);
        assertThat(pageVO.getPageNum()).isEqualTo(1);
        assertThat(pageVO.getPageSize()).isEqualTo(12);
        assertThat(pageVO.getTotalPages()).isEqualTo(1L);
        assertThat(pageVO.getRecords()).hasSize(1);
        assertThat(pageVO.getRecords().get(0).getCategoryName()).isEqualTo("Web & Frontend Development");
        assertThat(pageVO.getRecords().get(0).getTags())
            .extracting(UserWebsiteTagItemVO::getId, UserWebsiteTagItemVO::getName, UserWebsiteTagItemVO::getColor)
            .containsExactly(
                tuple(101L, "Vue3", "#42B883"),
                tuple(105L, "Tailwind", "#38BDF8"),
                tuple(106L, "Vite", "#646CFF")
            );
    }

    /**
     * 查询分类统计应补齐未命中的分类数量
     */
    @Test
    void shouldQueryCategoryStatsSuccessfully() {
        WebsiteCategory frontendCategory = new WebsiteCategory();
        frontendCategory.setId(10L);
        frontendCategory.setName("Web & Frontend Development");

        WebsiteCategory devopsCategory = new WebsiteCategory();
        devopsCategory.setId(20L);
        devopsCategory.setName("DevOps & Cloud");

        Map<String, Object> row = new HashMap<>();
        row.put("category_id", 10L);
        row.put("total", 5L);

        when(categoryMapper.selectList(any())).thenReturn(List.of(frontendCategory, devopsCategory));
        when(websiteMapper.selectMaps(any())).thenReturn(List.of(row));

        List<UserWebsiteCategoryVO> categoryVOS = userWebsiteService.queryCategoryStats();

        assertThat(categoryVOS).hasSize(2);
        assertThat(categoryVOS.get(0).getId()).isEqualTo(10L);
        assertThat(categoryVOS.get(0).getCount()).isEqualTo(5L);
        assertThat(categoryVOS.get(1).getId()).isEqualTo(20L);
        assertThat(categoryVOS.get(1).getCount()).isEqualTo(0L);
    }

    /**
     * 查询详情成功时应回显分类名称与标签列表
     */
    @Test
    void shouldQueryWebsiteDetailSuccessfully() {
        Website website = new Website();
        website.setId(100L);
        website.setName("PrimeVue");
        website.setUrl("https://primevue.org");
        website.setCategoryId(30L);
        website.setSource(1);
        website.setIsOfficial(1);
        website.setIsRecommend(1);
        website.setSubmitterId(101L);
        website.setShelfTime(LocalDateTime.of(2026, 4, 5, 9, 30, 0));
        website.setTags("205,206,207");

        UserAccount submitter = new UserAccount();
        submitter.setId(101L);
        submitter.setNickname("前端架构师");
        submitter.setUsername("frontend_pro");
        submitter.setDeleted(0);

        Tag primeVueTag = new Tag();
        primeVueTag.setId(205L);
        primeVueTag.setName("PrimeVue");
        primeVueTag.setColor("#10B981");

        Tag uiTag = new Tag();
        uiTag.setId(206L);
        uiTag.setName("UI");
        uiTag.setColor("#334155");

        Tag componentTag = new Tag();
        componentTag.setId(207L);
        componentTag.setName("Component");
        componentTag.setColor("#0EA5E9");

        WebsiteCategory category = new WebsiteCategory();
        category.setId(30L);
        category.setName("UI Library");

        when(websiteMapper.selectOne(any())).thenReturn(website);
        when(categoryMapper.selectOne(any())).thenReturn(category);
        when(tagMapper.selectList(any())).thenReturn(List.of(primeVueTag, uiTag, componentTag));
        when(userAccountMapper.selectOne(any())).thenReturn(submitter);

        UserWebsiteDetailVO detailVO = userWebsiteService.queryWebsiteDetail(100L);

        assertThat(detailVO.getId()).isEqualTo(100L);
        assertThat(detailVO.getCategoryName()).isEqualTo("UI Library");
        assertThat(detailVO.getIsOfficial()).isEqualTo(1);
        assertThat(detailVO.getIsRecommend()).isEqualTo(1);
        assertThat(detailVO.getSubmitterId()).isEqualTo(101L);
        assertThat(detailVO.getProviderName()).isEqualTo("前端架构师");
        assertThat(detailVO.getShelfTime()).isEqualTo(LocalDateTime.of(2026, 4, 5, 9, 30, 0));
        assertThat(detailVO.getTags())
            .extracting(UserWebsiteTagItemVO::getId, UserWebsiteTagItemVO::getName, UserWebsiteTagItemVO::getColor)
            .containsExactly(
                tuple(205L, "PrimeVue", "#10B981"),
                tuple(206L, "UI", "#334155"),
                tuple(207L, "Component", "#0EA5E9")
            );
    }

    /**
     * 管理员来源且提交ID缺失时应回显管理员提供者名称
     */
    @Test
    void shouldFallbackToAdminProviderNameWhenAdminSourceAndSubmitterIdMissing() {
        Website website = new Website();
        website.setId(200L);
        website.setName("Spring 官网");
        website.setUrl("https://spring.io");
        website.setCategoryId(30L);
        website.setSource(0);
        website.setSubmitterId(0L);

        WebsiteCategory category = new WebsiteCategory();
        category.setId(30L);
        category.setName("后端框架");

        when(websiteMapper.selectOne(any())).thenReturn(website);
        when(categoryMapper.selectOne(any())).thenReturn(category);

        UserWebsiteDetailVO detailVO = userWebsiteService.queryWebsiteDetail(200L);

        assertThat(detailVO.getProviderName()).isEqualTo("管理员");
    }

    /**
     * 查询详情失败时应抛出业务异常
     */
    @Test
    void shouldThrowWhenQueryWebsiteDetailNotFound() {
        when(websiteMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> userWebsiteService.queryWebsiteDetail(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("网站不存在或已下架");

        verify(websiteMapper).selectOne(any());
    }
}
