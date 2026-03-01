package com.mall.entity.dto;

import com.mall.entity.po.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品分页查询参数")
public class ProductPageDTO extends Page {

    @Schema(description = "商品关键词")
    private String keyword;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "排序字段: sales(销量), rating(评分), createdAt(最新)")
    private String sortField;

    @Schema(description = "排序方向: asc(升序), desc(降序)")
    private String sortOrder = "desc";
}
