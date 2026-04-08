package com.yyyouth.service.admin.category;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.admin.AdminCategoryCreateDTO;
import com.yyyouth.model.dto.admin.AdminCategoryEditDTO;
import com.yyyouth.model.pojo.website.Website;
import com.yyyouth.model.pojo.website.WebsiteCategory;
import com.yyyouth.model.vo.admin.AdminCategoryTreeItemVO;
import com.yyyouth.service.admin.category.impl.AdminCategoryServiceImpl;
import com.yyyouth.service.mapper.website.CategoryMapper;
import com.yyyouth.service.mapper.website.WebsiteMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 管理端分类服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminCategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private WebsiteMapper websiteMapper;

    @InjectMocks
    private AdminCategoryServiceImpl adminCategoryService;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     */
    @BeforeAll
    static void initMybatisLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(builderAssistant, WebsiteCategory.class);
        TableInfoHelper.initTableInfo(builderAssistant, Website.class);
    }

    /**
     * 查询分类树应返回父子结构
     */
    @Test
    void shouldQueryCategoryTreeSuccessfully() {
        WebsiteCategory rootCategory = new WebsiteCategory();
        rootCategory.setId(1L);
        rootCategory.setName("前端开发");
        rootCategory.setParentId(0L);
        rootCategory.setSort(0);
        rootCategory.setStatus(1);
        rootCategory.setDeleted(0);

        WebsiteCategory childCategory = new WebsiteCategory();
        childCategory.setId(2L);
        childCategory.setName("Vue");
        childCategory.setParentId(1L);
        childCategory.setSort(0);
        childCategory.setStatus(1);
        childCategory.setDeleted(0);

        when(categoryMapper.selectList(any())).thenReturn(List.of(rootCategory, childCategory));

        List<AdminCategoryTreeItemVO> treeList = adminCategoryService.queryCategoryTree(null);

        assertThat(treeList).hasSize(1);
        assertThat(treeList.get(0).getName()).isEqualTo("前端开发");
        assertThat(treeList.get(0).getChildren()).hasSize(1);
        assertThat(treeList.get(0).getChildren().get(0).getName()).isEqualTo("Vue");
    }

    /**
     * 创建分类时父分类禁用应拒绝
     */
    @Test
    void shouldThrowWhenCreateCategoryWithDisabledParent() {
        WebsiteCategory disabledParent = new WebsiteCategory();
        disabledParent.setId(10L);
        disabledParent.setParentId(0L);
        disabledParent.setStatus(0);
        disabledParent.setDeleted(0);

        when(categoryMapper.selectOne(any())).thenReturn(disabledParent, null);

        AdminCategoryCreateDTO createDTO = new AdminCategoryCreateDTO();
        createDTO.setName("React");
        createDTO.setParentId(10L);
        createDTO.setSort(1);

        assertThatThrownBy(() -> adminCategoryService.createCategory(createDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("父分类已禁用，无法创建子分类");
    }

    /**
     * 删除分类时被网站引用应拒绝
     */
    @Test
    void shouldThrowWhenDeleteCategoryReferencedByWebsite() {
        WebsiteCategory category = new WebsiteCategory();
        category.setId(20L);
        category.setParentId(0L);
        category.setStatus(1);
        category.setDeleted(0);

        when(categoryMapper.selectOne(any())).thenReturn(category);
        when(categoryMapper.selectCount(any())).thenReturn(0L);
        when(websiteMapper.selectCount(any())).thenReturn(2L);

        assertThatThrownBy(() -> adminCategoryService.deleteCategory(20L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分类已被网站引用，无法删除");
    }

    /**
     * 更新排序后应触发同级重排
     */
    @Test
    void shouldReorderSiblingsWhenUpdateCategorySort() {
        WebsiteCategory currentCategory = new WebsiteCategory();
        currentCategory.setId(30L);
        currentCategory.setParentId(9L);
        currentCategory.setStatus(1);
        currentCategory.setDeleted(0);

        WebsiteCategory siblingA = new WebsiteCategory();
        siblingA.setId(30L);
        siblingA.setParentId(9L);
        siblingA.setSort(5);
        siblingA.setDeleted(0);

        WebsiteCategory siblingB = new WebsiteCategory();
        siblingB.setId(31L);
        siblingB.setParentId(9L);
        siblingB.setSort(1);
        siblingB.setDeleted(0);

        when(categoryMapper.selectOne(any())).thenReturn(currentCategory);
        when(categoryMapper.update(any(WebsiteCategory.class), any())).thenReturn(1, 1, 1);
        when(categoryMapper.selectList(any())).thenReturn(List.of(siblingB, siblingA));

        adminCategoryService.updateCategorySort(30L, 10);

        verify(categoryMapper, atLeast(2)).update(any(WebsiteCategory.class), any());
    }

    /**
     * 编辑分类时设置父分类为自身后代应拒绝
     */
    @Test
    void shouldThrowWhenEditCategoryParentIsDescendant() {
        WebsiteCategory currentCategory = new WebsiteCategory();
        currentCategory.setId(1L);
        currentCategory.setParentId(0L);
        currentCategory.setStatus(1);
        currentCategory.setDeleted(0);

        WebsiteCategory childCategory = new WebsiteCategory();
        childCategory.setId(2L);
        childCategory.setParentId(1L);
        childCategory.setStatus(1);
        childCategory.setDeleted(0);

        when(categoryMapper.selectOne(any())).thenReturn(currentCategory, childCategory, childCategory);

        AdminCategoryEditDTO editDTO = new AdminCategoryEditDTO();
        editDTO.setName("前端开发");
        editDTO.setParentId(2L);
        editDTO.setSort(0);
        editDTO.setStatus(1);

        assertThatThrownBy(() -> adminCategoryService.editCategory(1L, editDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("父子关系不合法，存在循环引用");
    }
}
