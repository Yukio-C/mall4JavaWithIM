export interface PageResult<T> {
  pageNum: number;
  pageSize: number;
  total: number;
  totalPages: number;
  list: T[];
}

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
}

export interface ProductDetail extends Product {
  sliderImgs: string[];
  detailHtml: string;
  specs: Record<string, string>;
  serviceInfo: string;
}

export interface User {
// ... existing User interface
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  phone?: string;
  balance: number;
}

export interface Address {
  id: number;
  name: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  isDefault: boolean;
  tag: string;
}

export interface CartItem {
  id: number;
  productId: number;
  title: string;
  price: number;
  cover: string;
  count: number;
  selected: boolean;
}

export interface Order {
  id: string;
  status: 'pending' | 'paid' | 'shipped' | 'completed' | 'refunded';
  totalPrice: number;
  createTime: string;
  items: CartItem[];
  address: Address;
}

// 模拟数据库
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
      rating: 4.9
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
      rating: 4.7
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
      rating: 4.8
    },
    {
      id: 4,
      title: "特级有机初榨橄榄油 500ml",
      price: 128,
      cover: "https://via.placeholder.com/300x300/4caf50/ffffff?text=Olive+Oil",
      category: "food",
      sales: 88,
      stock: 300,
      description: "健康生活，从油开始",
      rating: 5.0
    },
    {
      id: 5,
      title: "复古机械键盘 青轴",
      price: 359,
      cover: "https://via.placeholder.com/300x300/673ab7/ffffff?text=Keyboard",
      category: "electronics",
      sales: 450,
      stock: 12,
      description: "打字如飞，手感极佳",
      rating: 4.6
    },
    { id: 6, title: "智能空气净化器", price: 1299, cover: "https://via.placeholder.com/300x300/eee/333?text=Purifier", category: "home", sales: 120, stock: 50, description: "呼吸新鲜空气", rating: 4.8 },
    { id: 7, title: "极简实木餐桌", price: 2500, cover: "https://via.placeholder.com/300x300/8d6e63/fff?text=Table", category: "home", sales: 45, stock: 10, description: "原木质感", rating: 4.9 },
    { id: 8, title: "运动级筋膜枪", price: 499, cover: "https://via.placeholder.com/300x300/263238/fff?text=Massager", category: "electronics", sales: 800, stock: 100, description: "深度放松肌肉", rating: 4.7 },
    { id: 9, title: "全自动咖啡机", price: 3200, cover: "https://via.placeholder.com/300x300/4e342e/fff?text=Coffee", category: "home", sales: 30, stock: 5, description: "一键研磨", rating: 4.5 },
    { id: 10, title: "无线手持吸尘器", price: 1599, cover: "https://via.placeholder.com/300x300/78909c/fff?text=Vacuum", category: "home", sales: 200, stock: 30, description: "大吸力更干净", rating: 4.6 },
    { id: 11, title: "4K超清投影仪", price: 3999, cover: "https://via.placeholder.com/300x300/1a237e/fff?text=Projector", category: "electronics", sales: 150, stock: 20, description: "私人影院体验", rating: 4.8 },
    { id: 12, title: "人体工学电脑椅", price: 899, cover: "https://via.placeholder.com/300x300/546e7a/fff?text=Chair", category: "home", sales: 400, stock: 50, description: "久坐不累", rating: 4.7 },
    { id: 13, title: "大容量双开门冰箱", price: 5999, cover: "https://via.placeholder.com/300x300/cfd8dc/333?text=Fridge", category: "home", sales: 88, stock: 15, description: "一级能效", rating: 4.9 },
    { id: 14, title: "静音无叶电风扇", price: 699, cover: "https://via.placeholder.com/300x300/e1f5fe/333?text=Fan", category: "home", sales: 1200, stock: 200, description: "凉爽静谧", rating: 4.6 },
    { id: 15, title: "多功能电压力锅", price: 459, cover: "https://via.placeholder.com/300x300/ffe0b2/333?text=Cooker", category: "home", sales: 560, stock: 80, description: "美食一键达", rating: 4.7 },
    { id: 16, title: "智能扫地机器人 X", price: 2999, cover: "https://via.placeholder.com/300x300/f5f5f5/333?text=Robot", category: "home", sales: 340, stock: 25, description: "全屋规划清扫", rating: 4.8 },
    { id: 17, title: "降噪无线耳塞 Pro", price: 1299, cover: "https://via.placeholder.com/300x300/212121/fff?text=Earbuds", category: "electronics", sales: 2100, stock: 150, description: "纯净音质", rating: 4.9 },
    { id: 18, title: "便携式移动电源 20k", price: 199, cover: "https://via.placeholder.com/300x300/ff5722/fff?text=Powerbank", category: "electronics", sales: 8900, stock: 1000, description: "大容量快充", rating: 4.5 },
    { id: 19, title: "4K专业显示器 27寸", price: 1899, cover: "https://via.placeholder.com/300x300/37474f/fff?text=Monitor", category: "electronics", sales: 670, stock: 45, description: "设计专用色准", rating: 4.8 },
    { id: 20, title: "折叠式健身跑步机", price: 2499, cover: "https://via.placeholder.com/300x300/424242/fff?text=Treadmill", category: "home", sales: 230, stock: 12, description: "轻便好收纳", rating: 4.7 },
    { id: 21, title: "真无线电竞鼠标", price: 599, cover: "https://via.placeholder.com/300x300/6200ea/fff?text=Mouse", category: "electronics", sales: 1500, stock: 300, description: "毫秒级响应", rating: 4.9 },
    { id: 22, title: "超薄智能电视 65寸", price: 4299, cover: "https://via.placeholder.com/300x300/01579b/fff?text=TV", category: "home", sales: 450, stock: 20, description: "巨幕影院", rating: 4.8 },
    { id: 23, title: "不锈钢电水壶", price: 99, cover: "https://via.placeholder.com/300x300/9e9e9e/fff?text=Kettle", category: "home", sales: 5600, stock: 500, description: "快速烧水", rating: 4.6 },
    { id: 24, title: "机械感光感台灯", price: 259, cover: "https://via.placeholder.com/300x300/fff176/333?text=Lamp", category: "home", sales: 1200, stock: 100, description: "护眼无频闪", rating: 4.7 }
  ] as Product[],

  // 模拟商品详情数据 (一对一)
  productDetails: {
    1: {
      sliderImgs: [
        "https://via.placeholder.com/800x800/ff5000/ffffff?text=Phone+Detail+1",
        "https://via.placeholder.com/800x800/ff9000/ffffff?text=Phone+Detail+2"
      ],
      detailHtml: "<h3>核心卖点</h3><p>搭载最新骁龙8 Gen 3处理器，性能强劲。</p><p>120Hz LTPO屏幕，流畅省电。</p><img src='https://via.placeholder.com/800x400/eeeeee/333333?text=Feature+Image'>",
      specs: { "处理器": "Snapdragon 8 Gen 3", "屏幕尺寸": "6.7英寸", "电池容量": "5000mAh", "重量": "210g" },
      serviceInfo: "7天无理由退货 · 一年质保 · 送货上门"
    },
    2: {
      sliderImgs: ["https://via.placeholder.com/800x800/333333/ffffff?text=Shirt+Detail"],
      detailHtml: "<p>精选新疆长绒棉，亲肤舒适。</p>",
      specs: { "材质": "100%棉", "版型": "修身", "适用季节": "四季" },
      serviceInfo: "支持30天退换货"
    }
  } as Record<number, Omit<ProductDetail, keyof Product>>,

  user: {
    id: 1,
    username: "user",
    nickname: "淘气值999",
    avatar: "https://via.placeholder.com/100/ff5000/ffffff?text=User",
    balance: 9999
  } as User,

  addresses: [
    { id: 1, name: "张三", phone: "13800138000", province: "北京市", city: "北京市", district: "朝阳区", detail: "科技园路88号", isDefault: true, tag: "家" },
    { id: 2, name: "李四", phone: "13900139000", province: "上海市", city: "上海市", district: "浦东新区", detail: "软件园", isDefault: false, tag: "公司" }
  ] as Address[],

  regions: [
    {
      value: '北京市', label: '北京市',
      children: [{ value: '北京市', label: '北京市', children: [{ value: '东城区', label: '东城区' }, { value: '西城区', label: '西城区' }, { value: '朝阳区', label: '朝阳区' }] }]
    },
    {
      value: '上海市', label: '上海市',
      children: [{ value: '上海市', label: '上海市', children: [{ value: '黄浦区', label: '黄浦区' }, { value: '徐汇区', label: '徐汇区' }, { value: '浦东新区', label: '浦东新区' }] }]
    },
    {
      value: '广东省', label: '广东省',
      children: [
        { value: '深圳市', label: '深圳市', children: [{ value: '南山区', label: '南山区' }, { value: '福田区', label: '福田区' }] },
        { value: '广州市', label: '广州市', children: [{ value: '天河区', label: '天河区' }] }
      ]
    },
    {
      value: '浙江省', label: '浙江省',
      children: [
        { value: '杭州市', label: '杭州市', children: [{ value: '西湖区', label: '西湖区' }, { value: '拱墅区', label: '拱墅区' }, { value: '余杭区', label: '余杭区' }] }
      ]
    }
  ],

  cart: [] as CartItem[],
  
  orders: [] as Order[]
};
