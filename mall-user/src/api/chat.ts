export interface ChatMessage {
    id?: number;
    orderId: string | number; // 关联订单号
    fromUserId: string | number;
    toUserId: string | number;
    content: string;
    msgType: number; // 1: 文字, 2: 图片
    createTime?: string;
    isMine?: boolean; // 前端辅助字段，用于区分气泡左右
}
