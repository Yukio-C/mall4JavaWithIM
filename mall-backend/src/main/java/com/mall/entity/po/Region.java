package com.mall.entity.po;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_regions")
public class Region implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 行政区划代码
     */
    @TableId(value = "code")
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 父级行政区划代码
     */
    private String parentCode;

    /**
     * 行政区划等级（1-省/直辖市，2-市，3-区/县）
     */
    private Integer level;
}
