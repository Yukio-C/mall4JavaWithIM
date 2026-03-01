package com.mall.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@TableName(value = "products_detail", autoResultMap = true)
public class ProductDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 关联商品ID
     */
    @TableId(value = "product_id", type = IdType.INPUT)
    private Long productId;

    /**
     * 轮播大图，JSON数组格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> sliderImgs;

    /**
     * 商品图文详情 (富文本)
     */
    private String detailHtml;

    /**
     * 规格参数 (JSON格式)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> specs;

    /**
     * 售后服务说明
     */
    private String serviceInfo;
}
