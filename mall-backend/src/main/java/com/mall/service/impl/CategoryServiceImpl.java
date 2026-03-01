package com.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.entity.dto.CategoryDTO;
import com.mall.entity.po.Category;
import com.mall.entity.vo.CategoryVO;
import com.mall.mapper.CategoryMapper;
import com.mall.service.ICategoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private final CategoryMapper categoryMapper;

    // 通用缓存
    private final Map<Integer, String> categoryCache = new ConcurrentHashMap<>();

    @PostConstruct
    @Override
    public void refreshCache() {
        log.info("正在刷新分类缓存...");
        try {
            List<Category> list = categoryMapper.selectList(null);
            categoryCache.clear();
            if (list != null) {
                list.forEach(c -> categoryCache.put(c.getId(), c.getName()));
            }
        } catch (Exception e) {
            log.error("刷新分类缓存失败", e);
        }
    }

    @Override
    public List<CategoryVO> getCategoryTree() {
        List<Category> all = this.list();
        if (all == null || all.isEmpty()) return new ArrayList<>();

        // 1. 转为 VO
        List<CategoryVO> voList = all.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());

        // 2. 递归构建
        return voList.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .peek(c -> c.setChildren(getChildren(c, voList)))
                .collect(Collectors.toList());
    }

    private List<CategoryVO> getChildren(CategoryVO root, List<CategoryVO> all) {
        return all.stream()
                .filter(c -> root.getId().equals(c.getParentId()))
                .peek(c -> c.setChildren(getChildren(c, all)))
                .collect(Collectors.toList());
    }

    @Override
    public void addCategory(CategoryDTO categoryDTO) {
        // 1. 深度校验
        checkDepth(categoryDTO.getParentId());

        // 2. 转换并保存
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        this.save(category);

        // 3. 刷新缓存
        refreshCache();
    }

    @Override
    public void updateCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        this.updateById(category);
        refreshCache();
    }

    private void checkDepth(Integer parentId) {
        if (parentId == null || parentId == 0) return; // 根节点，没问题

        int depth = 1;
        Integer currentParentId = parentId;
        while (currentParentId != null && currentParentId != 0) {
            Category p = this.getById(currentParentId);
            if (p == null) break;
            depth++;
            currentParentId = p.getParentId();
            
            if (depth > 3) {
                throw new RuntimeException("分类层级不能超过 3 层！");
            }
        }
    }

    @Override
    public String getNameById(Integer id) {
        if (id == null) return "未分类";
        return categoryCache.getOrDefault(id, "其他分类");
    }

    @Override
    public Map<Integer, String> getCategoryMap() {
        return Collections.unmodifiableMap(categoryCache);
    }
}
