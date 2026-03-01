package com.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "分类请求DTO")
public class CategoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID (修改时必填)")
    private Integer id;

    @Schema(description = "父分类ID (0或null表示一级分类)")
    private Integer parentId;

    @NotBlank(message = "分类名称不能为空")
    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序值")
    private Integer sort;
}
