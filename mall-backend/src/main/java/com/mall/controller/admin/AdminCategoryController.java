package com.mall.controller.admin;

import com.mall.common.Result;
import com.mall.entity.dto.CategoryDTO;
import com.mall.entity.vo.CategoryVO;
import com.mall.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
@Tag(name = "商家分类管理")
@PreAuthorize("@ss.hasPerm('category')")
public class AdminCategoryController {

    private final ICategoryService categoryService;

    /**
     * 新增分类
     */
    @PostMapping("/add")
    @Operation(summary = "新增分类")
    public Result<String> add(@RequestBody @Valid CategoryDTO categoryDTO) {
        categoryService.addCategory(categoryDTO);
        return Result.success("新增成功");
    }

    /**
     * 修改分类
     */
    @PutMapping("/update")
    @Operation(summary = "修改分类")
    public Result<String> update(@RequestBody @Valid CategoryDTO categoryDTO) {
        categoryService.updateCategory(categoryDTO);
        return Result.success("修改成功");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除分类")
    public Result<String> delete(@PathVariable Integer id) {
        // 简单逻辑：如果是树结构，删除时建议先检查是否有子类
        categoryService.removeById(id);
        categoryService.refreshCache();
        return Result.success("删除成功");
    }
}
