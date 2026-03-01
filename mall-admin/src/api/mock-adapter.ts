import request from '../utils/request'
import { MOCK_DB } from './mock'

request.defaults.adapter = async (config) => {
  await new Promise(resolve => setTimeout(resolve, 300));
  const { url, method, data } = config;
  const body = data ? JSON.parse(data) : {};
  let resData: any = null;

  console.log(`[Admin Mock] ${method?.toUpperCase()} ${url}`, body);

  try {
    if (url === '/admin/login') {
      if (body.username === 'admin' && body.password === 'admin') {
        resData = { token: 'admin-token', name: 'Super Admin' };
      } else {
        throw new Error('账号或密码错误');
      }
    }
    else if (url === '/admin/product/list') {
      resData = MOCK_DB.products;
    }
    else if (url === '/admin/product/update') {
      const p = body;
      const idx = MOCK_DB.products.findIndex(i => i.id === p.id);
      if (idx !== -1) {
        MOCK_DB.products[idx] = { ...p };
      } else {
        p.id = Date.now();
        MOCK_DB.products.push(p);
      }
      resData = {};
    }
    else if (url === '/admin/product/status') {
      const p = MOCK_DB.products.find(i => i.id === body.id);
      if (p) p.status = body.status;
      resData = {};
    }
    else if (url === '/admin/product/delete') {
      const idx = MOCK_DB.products.findIndex(i => i.id === body.id);
      if (idx !== -1) MOCK_DB.products.splice(idx, 1);
      resData = {};
    }
    else if (url === '/admin/order/list') {
      resData = MOCK_DB.orders;
    }
    else if (url === '/admin/order/ship') {
      const o = MOCK_DB.orders.find(i => i.id === body.orderId);
      if (o) {
        o.status = 'shipped';
        o.logistics = body.logisticsNo;
      }
      resData = {};
    }
    else {
      return { status: 404, config, headers: {}, data: { code: 404, message: 'NotFound' } };
    }

    return {
      status: 200,
      config,
      headers: {},
      data: { code: 200, message: 'success', data: resData }
    };
  } catch (e: any) {
    return {
      status: 200,
      config,
      headers: {},
      data: { code: 500, message: e.message, data: null }
    };
  }
}
