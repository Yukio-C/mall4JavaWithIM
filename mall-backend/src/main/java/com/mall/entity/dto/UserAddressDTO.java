package com.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "用户收货地址传输对象")
public class UserAddressDTO {

    @Schema(description = "地址ID (修改时必填)")
    private Long id;

    @Schema(description = "收件人姓名")
    @NotBlank(message = "收件人姓名不能为空")
    private String name;

    @Schema(description = "收件人电话")
    @NotBlank(message = "收件人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "省份名称")
    @NotBlank(message = "省份不能为空")
    private String province;

    @Schema(description = "城市名称")
    @NotBlank(message = "城市不能为空")
    private String city;

    @Schema(description = "区县名称")
    @NotBlank(message = "区县不能为空")
    private String district;

    @Schema(description = "详细地址")
    @NotBlank(message = "详细地址不能为空")
    private String detail;

    @Schema(description = "标签 (如：家、公司、学校)")
    private String tag;

    @Schema(description = "是否默认 (0:否, 1:是)")
    @NotNull(message = "是否默认地址状态不能为空")
    private Integer isDefault;
}
