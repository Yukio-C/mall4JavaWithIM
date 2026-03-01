package com.mall.entity.po;
                                                                                     
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 聊天消息持久化对象 (PO)
* 与数据库 chat_messages 表字段严格对应
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_messages")
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号，关联订单表的 order_no 字段
     */
    private String orderNo;

    /**
     * 消息发送者用户ID，关联用户表的 id 字段
     */
    private String fromUserId;

    /**
     * 消息接收者用户ID，关联用户表的 id 字段
     */
    private String toUserId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1:文字, 2:图片
     */
    private Integer msgType;

    /**
     * 消息是否已读：0:未读, 1:已读
     */
    private Integer isRead;

    /**
     * 消息发送时间
     */
    private LocalDateTime createdAt;
}