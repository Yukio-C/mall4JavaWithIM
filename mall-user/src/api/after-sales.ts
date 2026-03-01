import request from '@/utils/request';

export interface AfterSalesApplyReq {
  orderId: string | number;
  orderNo: string;
  type: number; // 1: 仅退款, 2: 退货退款, 3: 换货
  reason: string;
  items: {
    orderItemId: number | string;
    count: number;
  }[];
  description?: string;
  images?: string[];
}

export const applyAfterSales = (data: AfterSalesApplyReq) => {
  return request.post('/user/after-sales/apply', data);
};

export const getAfterSalesStatus = (orderId: string | number) => {
  return request.get(`/user/after-sales/status/${orderId}`);
};
