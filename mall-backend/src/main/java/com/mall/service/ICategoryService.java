package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.entity.dto.CategoryDTO;
import com.mall.entity.po.Category;

import com.mall.entity.vo.CategoryVO;

import java.util.List;
import java.util.Map;

public interface ICategoryService extends IService<Category> {
    /**
     * 获取树状结构的分类列表
     */
    List<CategoryVO> getCategoryTree();

    /**
     * 新增分类 (含 3 层深度校验)
     */
    void addCategory(CategoryDTO categoryDTO);

    /**
     * 修改分类
     */
    void updateCategory(CategoryDTO categoryDTO);

    /**
     * 根据 ID 获取分类名称 (从缓存获取)
     */
    String getNameById(Integer id);

    /**
     * 获取全量分类缓存映射
     */
    Map<Integer, String> getCategoryMap();

    /**
     * 刷新缓存
     */
    void refreshCache();
}
