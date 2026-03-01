package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.mall.entity.po.Region;
import com.mall.entity.vo.RegionNodeVO;

import java.util.List;

public interface IRegionService extends IService<Region> {

    /**
     * 获取区域树形结构
     */
    List<RegionNodeVO> getRegionTree();
}
