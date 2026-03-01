package com.mall.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "用户评价展示对象")
public class ProductReviewVO {
    private Long id;
    
    @Schema(description = "商品ID")
    private Long productId;
    
    @Schema(description = "商品标题")
    private String productTitle;
    
    @Schema(description = "商品封面")
    private String productCover;
    
    @Schema(description = "评分")
    private Integer rating;
    
    @Schema(description = "评价内容")
    private String content;
    
    @Schema(description = "评价图片列表")
    private List<String> images;
    
    @Schema(description = "商家回复")
    private String reply;
    
    @Schema(description = "评价时间")
    private LocalDateTime createdAt;
}
