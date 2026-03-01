package com.mall.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品详细信息")
public class ProductDetailVO extends ProductInfoVO {
    
    @Schema(description = "轮播图列表")
    private List<String> sliderImgs;

    @Schema(description = "图文详情(HTML)")
    private String detailHtml;

    @Schema(description = "规格参数(Key-Value)")
    private Map<String, String> specs;

    @Schema(description = "售后服务信息")
    private String serviceInfo;
}
