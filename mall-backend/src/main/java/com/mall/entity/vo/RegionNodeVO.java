package com.mall.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class RegionNodeVO {
    private String value; // 存储区域名称，如 "广东省"
    private String label; // 存储区域名称，如 "广东省"
    private List<RegionNodeVO> children;
}