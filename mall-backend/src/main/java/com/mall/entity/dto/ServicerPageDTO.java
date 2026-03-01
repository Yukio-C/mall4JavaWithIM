package com.mall.entity.dto;

import com.mall.entity.po.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客服分页查询参数")
public class ServicerPageDTO extends Page {

    @Schema(description = "用户名 (支持模糊查询)")
    private String username;
}
