import request from '../utils/request'
import { MOCK_DB } from './mock'

// 简单的路由匹配逻辑
request.defaults.adapter = async (config) => {
  // 模拟网络延迟
  await new Promise(resolve => setTimeout(resolve, 300 + Math.random() * 500));

  const { url, method, data, params } = config;
  const body = data ? JSON.parse(data) : {};
  let responseData: any = null;

  console.log(`[Mock API] ${method?.toUpperCase()} ${url}`, body || params);

  try {
    // --- 用户模块 ---
    if (url === '/user/login') {
      const { username, password } = body;
      if ((username === 'user' && password === '123456') || (window as any)._tempUser?.username === username) {
        responseData = { 
          token: 'mock-token-' + Date.now(), 
          user: (window as any)._tempUser || MOCK_DB.user 
        };
      } else {
        throw new Error('用户名或密码错误');
      }
    }
    else if (url === '/user/register') {
      const { username } = body;
      if (username === 'exist') throw new Error('用户名已存在');
      (window as any)._tempUser = {
        id: Date.now(),
        username,
        nickname: '新用户' + username.slice(-4),
        avatar: 'https://via.placeholder.com/100/409EFF/ffffff?text=New',
        balance: 0
      };
      responseData = {};
    }
    else if (url === '/user/info') {
      responseData = (window as any)._tempUser || MOCK_DB.user;
    }
    else if (url === '/user/update') {
      const user = (window as any)._tempUser || MOCK_DB.user;
      Object.assign(user, body);
      responseData = user;
    }
    else if (url === '/user/address/list') {
      const pageNum = Number(params?.pageNum) || 1;
      const pageSize = Number(params?.pageSize) || 10;
      const total = MOCK_DB.addresses.length;
      const totalPages = Math.ceil(total / pageSize);
      const start = (pageNum - 1) * pageSize;
      const list = MOCK_DB.addresses.slice(start, start + pageSize);

      responseData = {
        pageNum,
        pageSize,
        total,
        totalPages,
        list
      };
    }
    else if (url === '/user/address/add') {
      const newAddr = { ...body, id: Date.now() };
      MOCK_DB.addresses.push(newAddr);
      responseData = newAddr;
    }
    else if (url === '/user/address/update') {
      const idx = MOCK_DB.addresses.findIndex(a => a.id === body.id);
      if (idx !== -1) {
        MOCK_DB.addresses[idx] = { ...MOCK_DB.addresses[idx], ...body };
        responseData = MOCK_DB.addresses[idx];
      }
    }
    else if (url?.startsWith('/user/address/delete/')) {
      const id = Number(url.split('/').pop());
      const idx = MOCK_DB.addresses.findIndex(a => a.id === id);
      if (idx !== -1) MOCK_DB.addresses.splice(idx, 1);
      responseData = {};
    }
    else if (url === '/common/region/tree') {
      responseData = (MOCK_DB as any).regions;
    }
    else if (url === '/user/after-sales/list') {
      const pageNum = Number(params?.page) || 1;
      const pageSize = Number(params?.pageSize) || 10;
      // 模拟返回数据
      const list = [
        { 
          id: 1, 
          orderNo: 'TEST-ORD-001', 
          productTitle: '测试商品 - 无线蓝牙耳机',
          productCover: 'https://via.placeholder.com/100/409EFF/ffffff?text=Product',
          type: 1, 
          reason: '拍错了', 
          status: 1, 
          applyTime: '2026-02-26 10:00:00' 
        }
      ];
      responseData = {
        pageNum,
        pageSize,
        total: list.length,
        list
      };
    }
    else if (url === '/user/after-sales/apply') {
      responseData = {};
    }

    // --- 商品模块 ---
    else if (url === '/product/list') {
      let list = [...MOCK_DB.products];
      if (params?.keyword) {
        const k = params.keyword.toLowerCase();
        list = list.filter(p => p.title.toLowerCase().includes(k) || p.description.toLowerCase().includes(k));
      }
      
      const pageNum = Number(params?.pageNum) || 1;
      const pageSize = Number(params?.pageSize) || 10;
      const total = list.length;
      const totalPages = Math.ceil(total / pageSize);
      const start = (pageNum - 1) * pageSize;
      const paginatedList = list.slice(start, start + pageSize);

      responseData = {
        pageNum,
        pageSize,
        total,
        totalPages,
        list: paginatedList
      };
    }
    else if (url?.startsWith('/product/detail')) {
      const id = Number(params?.id) || Number(url.split('/').pop());
      const baseProduct = MOCK_DB.products.find(p => p.id === id);
      const detailInfo = (MOCK_DB as any).productDetails?.[id];
      
      if (baseProduct) {
         responseData = {
           ...baseProduct,
           sliderImgs: detailInfo?.sliderImgs || [baseProduct.cover], // 默认 fallback 到封面
           detailHtml: detailInfo?.detailHtml || `<p>${baseProduct.description}</p>`,
           specs: detailInfo?.specs || {},
           serviceInfo: detailInfo?.serviceInfo || '暂无售后说明'
         };
      } else {
        throw new Error('商品不存在');
      }
    }

    // --- 购物车 & 订单 ---
    else if (url === '/cart/list') {
      responseData = MOCK_DB.cart;
    }
    else if (url === '/cart/add') {
      const { productId, count } = body;
      const product = MOCK_DB.products.find(p => p.id === productId);
      if (product) {
        const exist = MOCK_DB.cart.find(c => c.productId === productId);
        if (exist) {
          exist.count += count;
        } else {
          MOCK_DB.cart.push({
            id: Date.now(),
            productId: product.id,
            title: product.title,
            price: product.price,
            cover: product.cover,
            count,
            selected: true
          });
        }
      }
      responseData = {};
    }
    else if (url === '/order/create') {
      const { items, addressId } = body;
      const address = MOCK_DB.addresses.find(a => a.id === addressId);
      if (!address) throw new Error('地址错误');
      
      const newOrder = {
        id: 'ORDER-' + Date.now(),
        status: 'pending',
        totalPrice: items.reduce((sum:any, item:any) => sum + item.price * item.count, 0),
        createTime: new Date().toLocaleString(),
        items: [...items],
        address
      };
      MOCK_DB.orders.unshift(newOrder as any);
      MOCK_DB.cart = []; // 清空购物车
      responseData = newOrder;
    }
    else if (url === '/order/pay') {
      const order = MOCK_DB.orders.find(o => o.id === body.orderId);
      if (order) order.status = 'paid';
      responseData = {};
    }
    else if (url === '/order/list') {
      responseData = MOCK_DB.orders;
    }
    else {
      return { status: 404, statusText: 'Not Found', headers: {}, config, data: { code: 404, message: '接口未定义' } };
    }

    // 成功返回
    return {
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
      data: {
        code: 200,
        message: 'success',
        data: responseData
      }
    };

  } catch (e: any) {
    return {
      status: 200, // 业务错误也通常返回 200，通过 code 区分
      statusText: 'OK',
      headers: {},
      config,
      data: {
        code: 500,
        message: e.message || 'Server Error',
        data: null
      }
    };
  }
}
