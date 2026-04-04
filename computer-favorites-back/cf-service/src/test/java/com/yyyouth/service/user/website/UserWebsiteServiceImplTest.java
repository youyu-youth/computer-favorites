package com.yyyouth.service.user.website;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserWebsiteQueryDTO;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.user.UserWebsiteCategoryVO;
import com.yyyouth.model.vo.user.UserWebsiteDetailVO;
import com.yyyouth.model.vo.user.UserWebsitePageVO;
import com.yyyouth.service.mapper.website.CategoryMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    }

    /**
     * 查询网站分页列表应返回分类与标签映射后的结果
     */
    @Test
    void shouldQueryWebsitePageSuccessfully() {
        UserWebsiteQueryDTO queryDTO = new UserWebsiteQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(12);

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
        website.setTags("Vue3, Tailwind, Vite,Vue3");

        WebsiteCategory category = new WebsiteCategory();
        category.setId(10L);
        category.setName("Web & Frontend Development");

        when(websiteMapper.selectCount(any())).thenReturn(1L);
        when(websiteMapper.selectList(any())).thenReturn(List.of(website));
        when(categoryMapper.selectBatchIds(any())).thenReturn(List.of(category));

        UserWebsitePageVO pageVO = userWebsiteService.queryWebsitePage(queryDTO);

        assertThat(pageVO.getTotal()).isEqualTo(1L);
        assertThat(pageVO.getPageNum()).isEqualTo(1);
        assertThat(pageVO.getPageSize()).isEqualTo(12);
        assertThat(pageVO.getTotalPages()).isEqualTo(1L);
        assertThat(pageVO.getRecords()).hasSize(1);
        assertThat(pageVO.getRecords().get(0).getCategoryName()).isEqualTo("Web & Frontend Development");
        assertThat(pageVO.getRecords().get(0).getTags()).containsExactly("Vue3", "Tailwind", "Vite");
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
        website.setTags("Vue, UI, Components");

        WebsiteCategory category = new WebsiteCategory();
        category.setId(30L);
        category.setName("UI Library");

        when(websiteMapper.selectOne(any())).thenReturn(website);
        when(categoryMapper.selectOne(any())).thenReturn(category);

        UserWebsiteDetailVO detailVO = userWebsiteService.queryWebsiteDetail(100L);

        assertThat(detailVO.getId()).isEqualTo(100L);
        assertThat(detailVO.getCategoryName()).isEqualTo("UI Library");
        assertThat(detailVO.getTags()).containsExactly("Vue", "UI", "Components");
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
