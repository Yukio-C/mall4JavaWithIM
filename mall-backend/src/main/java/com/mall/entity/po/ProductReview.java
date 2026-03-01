package com.mall.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class ProductReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Long orderId;
    private Long orderItemId;
    private Integer rating;
    private String content;
    private String images;
    private Integer isAnonymous;
    private String reply; // 商家回复
    private LocalDateTime createdAt;
}
