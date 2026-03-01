export interface Product {
  id: number;
  title: string;
  price: number;
  originalPrice?: number;
  cover: string;
  category: string;
  sales: number;
  stock: number;
  description: string;
  rating: number;
  status: boolean; // true: 上架, false: 下架
}

export interface Order {
  id: string;
  status: 'pending' | 'paid' | 'shipped' | 'completed' | 'refunded';
  totalPrice: number;
  createTime: string;
  items: any[];
  address: any;
  logistics?: string;
}

// 模拟数据库 (Admin 独立一份，实际开发应共享后端)
export const MOCK_DB = {
  products: [
    {
      id: 1,
      title: "2024新款智能手机 Pro Max 5G全网通",
      price: 5999,
      originalPrice: 6999,
      cover: "https://via.placeholder.com/300x300/ff5000/ffffff?text=Phone+Pro",
      category: "electronics",
      sales: 1024,
      stock: 100,
      description: "超强性能，极致摄影体验",
      rating: 4.9,
      status: true
    },
    {
      id: 2,
      title: "男士商务休闲纯棉衬衫",
      price: 199,
      originalPrice: 399,
      cover: "https://via.placeholder.com/300x300/333333/ffffff?text=Shirt",
      category: "clothing",
      sales: 500,
      stock: 200,
      description: "舒适透气，免烫工艺",
      rating: 4.7,
      status: true
    },
    {
      id: 3,
      title: "无线降噪蓝牙耳机",
      price: 899,
      cover: "https://via.placeholder.com/300x300/cccccc/000000?text=Headset",
      category: "electronics",
      sales: 2300,
      stock: 50,
      description: "静若处子，动若脱兔",
      rating: 4.8,
      status: false
    }
  ] as Product[],

  orders: [
    {
       id: 'ORDER-1704720000000',
       status: 'paid', // 待发货
       totalPrice: 6198,
       createTime: '2026/1/8 10:00:00',
       items: [
         { title: "2024新款智能手机 Pro Max", price: 5999, count: 1 },
         { title: "男士商务休闲纯棉衬衫", price: 199, count: 1 }
       ],
       address: { name: "张三", phone: "13800138000", detail: "北京市朝阳区..." }
    },
    {
       id: 'ORDER-1704720001111',
       status: 'shipped', // 已发货
       totalPrice: 899,
       createTime: '2026/1/7 15:30:00',
       items: [
         { title: "无线降噪蓝牙耳机", price: 899, count: 1 }
       ],
       address: { name: "李四", phone: "13900139000", detail: "上海市..." },
       logistics: "SF1234567890"
    }
  ] as Order[]
};
