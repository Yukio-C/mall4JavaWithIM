package com.mall.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSessionVO {
    private String orderNo;      // 订单号
    private Integer unreadCount; // 未读消息数
    private String lastContent;  // 最后一条消息内容预览
}
