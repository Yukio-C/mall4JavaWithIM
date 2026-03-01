package com.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户更新DTO")
public class UserUpdateDTO {
    /**
     * TODO 增加昵称的违法过滤
     */
    // Nickname 昵称
    @NotNull(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度必须在2到20个字符之间")
    private String nickname;
    // Avatar 头像链接
    @Size(max = 255, message = "头像链接过长")
    private String avatar;
}
