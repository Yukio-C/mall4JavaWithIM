package com.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.entity.po.Region;
import com.mall.entity.vo.RegionNodeVO;
import com.mall.mapper.RegionMapper;
import com.mall.service.IRegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements IRegionService {
    
    // 1. 定义静态缓存
    private static List<RegionNodeVO> REGION_CACHE = null;


    private final RegionMapper regionMapper;

    /**
     * 获取行政区划树（带缓存）
     */
    public List<RegionNodeVO> getRegionTree() {
        // 双重检查锁 (Double-Check Locking) 保证线程安全且只加载一次
        if (REGION_CACHE == null) {
            synchronized (RegionServiceImpl.class) {
                if (REGION_CACHE == null) {
                    initCache();
                }
            }
        }
        return REGION_CACHE;
    }

    private void initCache() {
        // 1. 一次性查出平铺列表
        List<Region> all = regionMapper.selectList(null);

        // 2. 构建树形结构 (O(n) 复杂度)
        Map<String, RegionNodeVO> map = new LinkedHashMap<>();

        // 第一次遍历：创建所有节点并存入 map
        all.forEach(r -> {
            RegionNodeVO vo = new RegionNodeVO();
            vo.setValue(r.getName());
            vo.setLabel(r.getName());
            vo.setChildren(new ArrayList<>());
            map.put(r.getCode(), vo);
        });

        List<RegionNodeVO> roots = new ArrayList<>();
        // 第二次遍历：组装父子关系
        all.forEach(r -> {
            RegionNodeVO node = map.get(r.getCode());
            if (r.getParentCode() == null || r.getParentCode().isEmpty()) {
                roots.add(node);
            } else {
                RegionNodeVO parent = map.get(r.getParentCode());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        });

        // 3. 递归清理空子列表
        cleanChildren(roots);
        REGION_CACHE = roots;
    }

    private void cleanChildren(List<RegionNodeVO> nodes) {
        for (RegionNodeVO node : nodes) {
            if (node.getChildren().isEmpty()) {
                node.setChildren(null); // 设为 null，前端识别为叶子节点
            } else {
                cleanChildren(node.getChildren());
            }
        }
    }
}