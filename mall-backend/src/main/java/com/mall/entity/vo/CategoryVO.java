package com.mall.entity.vo;

import com.mall.entity.po.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分类树节点")
public class CategoryVO extends Category {
    @Schema(description = "子分类列表")
    private List<CategoryVO> children;
}
