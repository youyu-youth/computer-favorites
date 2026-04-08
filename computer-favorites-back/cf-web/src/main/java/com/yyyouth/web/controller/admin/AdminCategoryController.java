package com.yyyouth.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yyyouth.common.web.HttpResult;
import com.yyyouth.model.dto.admin.AdminCategoryCreateDTO;
import com.yyyouth.model.dto.admin.AdminCategoryEditDTO;
import com.yyyouth.model.dto.admin.AdminCategorySortDTO;
import com.yyyouth.model.dto.admin.AdminCategoryTreeQueryDTO;
import com.yyyouth.model.vo.admin.AdminCategoryTreeItemVO;
import com.yyyouth.service.admin.category.AdminCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 管理端分类管理控制器
 */
@Slf4j
@Api(tags = "管理端分类管理接口")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/category")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    /**
     * 查询分类树
     *
     * @param queryDTO 查询参数
     * @return 分类树
     */
    @ApiOperation(value = "查询分类树")
    @GetMapping("/tree")
    @SaCheckPermission(value = "admin:category:list", type = "admin")
    public HttpResult tree(@Valid AdminCategoryTreeQueryDTO queryDTO) {
        log.info("查询管理端分类树请求，status={}", queryDTO.getStatus());
        List<AdminCategoryTreeItemVO> treeList = adminCategoryService.queryCategoryTree(queryDTO.getStatus());
        log.info("查询管理端分类树成功，rootSize={}, totalSize={}", treeList.size(), countTreeSize(treeList));
        return HttpResult.success("查询成功", treeList);
    }

    /**
     * 创建分类
     *
     * @param createDTO 创建参数
     * @return 分类ID
     */
    @ApiOperation(value = "创建分类")
    @PostMapping({"", "/"})
    @SaCheckPermission(value = "admin:category:add", type = "admin")
    public HttpResult create(@RequestBody @Valid @NotNull AdminCategoryCreateDTO createDTO) {
        log.info("管理端创建分类请求，name={}, parentId={}", createDTO.getName(), createDTO.getParentId());
        Long categoryId = adminCategoryService.createCategory(createDTO);
        log.info("管理端创建分类成功，categoryId={}", categoryId);
        return HttpResult.success("创建成功", categoryId);
    }

    /**
     * 编辑分类
     *
     * @param id 分类ID
     * @param editDTO 编辑参数
     * @return 执行结果
     */
    @ApiOperation(value = "编辑分类")
    @PutMapping("/{id}")
    @SaCheckPermission(value = "admin:category:edit", type = "admin")
    public HttpResult edit(@PathVariable("id") @NotNull @Positive Long id,
                           @RequestBody @Valid @NotNull AdminCategoryEditDTO editDTO) {
        log.info("管理端编辑分类请求，id={}, name={}, parentId={}", id, editDTO.getName(), editDTO.getParentId());
        adminCategoryService.editCategory(id, editDTO);
        log.info("管理端编辑分类成功，id={}", id);
        return HttpResult.success("编辑成功");
    }

    /**
     * 更新分类排序
     *
     * @param id 分类ID
     * @param sortDTO 排序参数
     * @return 执行结果
     */
    @ApiOperation(value = "更新分类排序")
    @PutMapping("/{id}/sort")
    @SaCheckPermission(value = "admin:category:edit", type = "admin")
    public HttpResult updateSort(@PathVariable("id") @NotNull @Positive Long id,
                                 @RequestBody @Valid @NotNull AdminCategorySortDTO sortDTO) {
        log.info("管理端更新分类排序请求，id={}, sort={}", id, sortDTO.getSort());
        adminCategoryService.updateCategorySort(id, sortDTO.getSort());
        log.info("管理端更新分类排序成功，id={}", id);
        return HttpResult.success("排序更新成功");
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 执行结果
     */
    @ApiOperation(value = "删除分类")
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = "admin:category:delete", type = "admin")
    public HttpResult delete(@PathVariable("id") @NotNull @Positive Long id) {
        log.info("管理端删除分类请求，id={}", id);
        adminCategoryService.deleteCategory(id);
        log.info("管理端删除分类成功，id={}", id);
        return HttpResult.success("删除成功");
    }

    /**
     * 统计分类树节点总数
     *
     * @param treeList 分类树
     * @return 节点总数
     */
    private int countTreeSize(List<AdminCategoryTreeItemVO> treeList) {
        if (treeList == null || treeList.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (AdminCategoryTreeItemVO node : treeList) {
            total += 1;
            total += countTreeSize(node.getChildren());
        }
        return total;
    }
}
