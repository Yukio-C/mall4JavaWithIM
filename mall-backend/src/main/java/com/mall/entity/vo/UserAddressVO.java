package com.mall.entity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class UserAddressVO {
    // id 地址id
    private Long id;
    // name 收件人姓名
    private String name;
    // phone 收件人电话
    private String phone;
    // province 省份
    private String province;
    // city 城市
    private String city;
    // district 区县
    private String district;
    // detail 详细地址
    private String detail;
    // tag 标签
    private String tag;
    // isDefault 是否默认地址 0-非默认 1-默认
    private Integer isDefault;
}