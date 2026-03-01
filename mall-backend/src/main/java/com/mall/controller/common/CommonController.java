package com.mall.controller.common;

import com.mall.common.Result;
import com.mall.entity.vo.CategoryVO;
import com.mall.entity.vo.RegionNodeVO;
import com.mall.service.ICategoryService;
import com.mall.service.IRegionService;
import com.mall.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/common")
@RestController
@RequiredArgsConstructor
@Tag(name = "通用工具接口")
@Validated
public class CommonController {

    private final AliOssUtil aliOssUtil;
    private final IRegionService regionService;
    private final ICategoryService categoryService;

    @PostMapping("/upload")
    @Operation(summary = "文件上传接口")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename());
        return Result.success(url);
    }

    @GetMapping("/region/tree")
    @Operation(summary = "获取行政区划树")
    public Result<List<RegionNodeVO>> getRegionTree() {
        return Result.success(regionService.getRegionTree());
    }

    @GetMapping("/category/list")
    @Operation(summary = "获取全量商品分类列表")
    public Result<Map<Integer, String>> getCategoryList() {
        return Result.success(categoryService.getCategoryMap());
    }

    @GetMapping("/category/tree")
    @Operation(summary = "获取全量分类树 (结构化)")
    public Result<List<CategoryVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }
}