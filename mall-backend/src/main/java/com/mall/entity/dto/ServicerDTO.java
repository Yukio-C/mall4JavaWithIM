package com.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "客服账号创建/更新对象")
public class ServicerDTO {

    private Long id; // 更新时必填

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码 (仅创建时必填)")
    private String password;

    @Schema(description = "客服昵称")
    private String nickname;

    @Schema(description = "权限页面列表，例如 ['product', 'order']")
    private List<String> permissions;
}
