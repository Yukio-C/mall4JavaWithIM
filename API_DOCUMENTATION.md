# 商城系统 API 接口文档 (v2.2 整合版)

## 1. 核心规范 (Core Standards)

### 1.1 基础信息
- **Base URL**: `http://localhost:9090` (后端服务端口)
- **Global Prefix**: 无 (根据当前 Controller 配置，如 `UserController` 映射为 `/user`)
- **Content-Type**: `application/json`
- **字符编码**: `UTF-8`

### 1.2 统一响应结构 (Result Envelope)
所有接口均返回以下 JSON 结构：

```json
{
  "code": 200,          // Integer: 状态码 (200=成功, 500=系统异常, 401=未登录, 403=无权限)
  "message": "success", // String: 提示信息
  "data": { ... }       // T: 业务数据
}
```

### 1.3 鉴权机制
- **用户端**: 需携带用户 Token (Header: `Authorization: Bearer <token>`)
- **商家端**: 需携带管理员 Token (Header: `Authorization: Bearer <token>`)

---

## 2. 数据模型 (Data Models)

### User (用户)
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long | 用户ID |
| `username` | String | 用户名 |
| `nickname` | String | 昵称 |
| `avatar` | String | 头像URL |
| `phone` | String | 手机号 |
| `balance` | BigDecimal | 余额 |
| `role` | String | 角色 |

### Address (地址)
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long | 地址ID |
| `name` | String | 收货人姓名 |
| `phone` | String | 收货人电话 |
| `province` | String | 省份名称 (如: "广东省") |
| `city` | String | 城市名称 (如: "深圳市") |
| `district` | String | 区/县名称 (如: "南山区") |
| `detail` | String | 详细地址 (街道门牌号) |
| `tag` | String | 标签 (如: "家", "公司") |
| `isDefault` | Integer | 是否默认: `1`=是, `0`=否 |

### Product (商品基础信息 - 用于列表)
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long | 商品ID |
| `title` | String | 商品标题 |
| `description` | String | 商品简介 (短) |
| `price` | BigDecimal | 当前售价 |
| `originalPrice` | BigDecimal | 原价 |
| `cover` | String | 封面图URL |
| `stock` | Integer | 库存 |
| `sales` | Integer | 销量 |
| `rating` | Double | 评分 (0-5.0) |
| `category` | String | 分类名称/ID |

### AfterSalesRecord (售后主记录)
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long | 记录ID |
| `orderNo` | String | 关联订单号 |
| `status` | Integer | 进度: 1=待审核, 2=处理中, 3=已完成, 4=已拒绝 |
| `type` | Integer | 类型: 1=仅退款, 2=退货退款, 3=换货 |
| `reason` | String | 申请原因 |
| `description` | String | 详细描述 |
| `images` | List<String> | 凭证图片列表 |
| `applyTime` | String | 申请时间 |
| `items` | List<AfterSaleItemVO> | **售后商品明细列表** |

### AfterSaleItemVO (售后商品明细)
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `productId` | Long | 商品ID |
| `productTitle` | String | 商品标题快照 |
| `productCover` | String | 商品封面快照 |
| `productPrice` | BigDecimal | 购买单价 |
| `count` | Integer | 售后数量 |

### OrderVO (订单展示对象)
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long | 订单ID |
| `orderNo` | String | 订单号 |
| `totalAmount` | BigDecimal | 总金额 |
| `payAmount` | BigDecimal | 实付金额 |
| `status` | Integer | 状态: 0待付款, 1待发货, 2已发货, 3已完成, 4已关闭, 5售后中 |
| `payType` | Integer | 支付方式: 1支付宝, 2微信, 3余额 |
| `payTime` | String | 支付时间 (ISO-8601) |
| `createdAt` | String | 创建时间 |
| `receiverName` | String | 收货人姓名 |
| `receiverPhone` | String | 收货人电话 |
| `receiverAddress` | String | 完整收货地址 |
| `logisticsCompany` | String | **物流公司名称** |
| `logisticsNo` | String | **物流单号** |
| `deliveryTime` | String | **发货时间** |
| `cancelReason` | String | **取消原因** |
| `cancelTime` | String | **取消时间** |
| `items` | List<OrderItem> | 订单商品详情列表 |

---

## 3. 用户模块 (User Module)

**Prefix**: `/user`

### 3.1 登录与注册

#### 3.1.1 用户登录
- **URL**: `/login`
- **Method**: `POST`
- **Body**:
  ```json
  { "username": "admin", "password": "123" }
  ```
- **Response**: `Result<UserLoginVo>`

#### 3.1.2 用户注册
- **URL**: `/register`
- **Method**: `POST`
- **Body**:
  ```json
  { "username": "test", "password": "123", "checkPassword": "123" }
  ```

### 3.2 个人信息

#### 3.2.1 获取用户信息
- **URL**: `/info`
- **Method**: `GET`
- **Response**: `Result<User>`
  ```json
  {
    "id": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "avatar": "url",
    "phone": "13800138000",
    "balance": 1000.00,
    "role": "ADMIN"
  }
  ```

#### 3.2.2 修改用户信息
- **URL**: `/update`
- **Method**: `PUT`
- **Body**:
  ```json
  {
    "nickname": "新昵称",
    "avatar": "新头像URL",
    "phone": "13900139000"
  }
  ```

#### 3.2.3 退出登录
- **URL**: `/logout`
- **Method**: `POST`
- **Description**: 清除 Redis 中的登录状态，使当前 Token 失效。

### 3.3 地址管理

#### 3.3.1 获取地址列表
- **URL**: `/address/list`
- **Method**: `GET`
- **Response**: `Result<PageResult<Address>>`
  ```json
  {
    "code": 200,
    "data": {
      "list": [
        { "id": 1, "name": "张三", "phone": "13800138000", "province": "广东省", "city": "深圳市", "district": "南山区", "detail": "粤海街道", "tag": "家", "isDefault": 1 }
      ],
      "total": 1
    }
  }
  ```

#### 3.3.2 新增地址
- **URL**: `/address/add`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "name": "张三",
    "phone": "13800138000",
    "province": "广东省",
    "city": "深圳市",
    "district": "南山区",
    "detail": "粤海街道",
    "tag": "家",
    "isDefault": 1
  }
  ```

#### 3.3.3 修改地址
- **URL**: `/address/update`
- **Method**: `PUT`
- **Body**: 
  ```json
  {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "province": "广东省",
    "city": "深圳市",
    "district": "南山区",
    "detail": "粤海街道",
    "tag": "公司",
    "isDefault": 0
  }
  ```

#### 3.3.4 删除地址
- **URL**: `/address/delete/{id}`
- **Method**: `DELETE`
- **Path Variable**: `id` (地址ID)


---

## 4. 商品模块 (Product Module)

**Prefix**: `/product`

#### 4.1 获取商品列表
- **URL**: `/list`
- **Method**: `GET`
- **Params**: `page`, `pageSize`, `keyword`, `categoryId`
- **Response**: `Result<PageResult<Product>>`
  ```json
  {
    "code": 200,
    "data": {
      "list": [
        { "id": 1, "title": "商品A", "price": 99.00, "cover": "url", "stock": 100 }
      ],
      "total": 50
    }
  }
  ```

#### 4.2 获取商品详情
- **URL**: `/detail`
- **Method**: `GET`
- **Params**: `id`
- **Response**: `Result<ProductDetail>`
  ```json
  {
    "code": 200,
    "data": {
      "id": 1,
      "title": "商品标题",
      "sliderImgs": ["url1", "url2"],
      "detailHtml": "<p>图文详情</p>",
      "serviceInfo": "服务说明",
      "specs": {"颜色": "黑色"}
    }
  }
  ```

---

## 5. 购物车模块 (Cart Module)

**Prefix**: `/cart`

#### 5.1 获取购物车列表
- **URL**: `/list`
- **Method**: `GET`
- **Response**: `Result<List<CartItem>>`
  ```json
  {
    "code": 200,
    "data": [
      { "id": 1, "productId": 101, "productTitle": "商品名", "productPrice": 99.00, "count": 2 }
    ]
  }
  ```

#### 5.2 添加购物车
- **URL**: `/add`
- **Method**: `POST`
- **Body**:
  ```json
  { "productId": 1, "count": 1 }
  ```

#### 5.3 更新购物车数量
- **URL**: `/update`
- **Method**: `POST`
- **Body**:
  ```json
  { "id": 1, "count": 2 }
  ```

#### 5.4 删除购物车商品
- **URL**: `/delete/{id}`
- **Method**: `DELETE`

---

## 6. 订单模块 (Order Module)

**Prefix**: `/order`

#### 6.1 获取下单令牌
- **URL**: `/token`
- **Method**: `GET`
- **Response**: `Result<{ "token": "uuid" }>`
  ```json
  { "code": 200, "data": { "token": "550e8400-e29b-41d4-a716-446655440000" } }
  ```

#### 6.2 创建订单
- **URL**: `/create`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "items": [
      { "productId": 1, "count": 1 }
    ],
    "addressId": 1,
    "orderToken": "uuid"
  }
  ```
- **Response**: `Result<OrderVO>`

#### 6.3 订单列表
- **URL**: `/list`
- **Method**: `GET`
- **Params**: `pageNum`, `pageSize`, `status`
- **Response**: `Result<PageResult<OrderVO>>`
  ```json
  {
    "code": 200,
    "data": {
      "list": [
        { "id": 1, "orderNo": "202403010001", "payAmount": 198.00, "status": 0 }
      ],
      "total": 5
    }
  }
  ```

#### 6.4 取消订单
- **URL**: `/cancel`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "orderId": 123,
    "reason": "不想买了"
  }
  ```
- **Description**: 仅限状态为 `0 (待付款)` 的订单。成功后状态变为 `4 (已关闭)`，并回滚商品库存。

---

## 7. 支付模块 (Payment Module)

**Prefix**: `/payment`

#### 7.1 提交支付
- **URL**: `/pay`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "orderNo": "202403010001",
    "payType": 3
  }
  ```
- **Response**: `Result<PaymentVO>`

#### 7.2 查询支付状态
- **URL**: `/status`
- **Method**: `GET`
- **Params**: `orderNo`
- **Response**: `Result<PaymentVO>`
  ```json
  {
    "orderNo": "202403010001",
    "tradeNo": "uuid",
    "amount": 99.00,
    "status": 1,
    "payTypeName": "余额支付",
    "payTime": "..."
  }
  ```

---

## 8. 售后模块 (AfterSales Module)

**Prefix**: `/user/after-sales`

#### 8.1 申请售后
- **URL**: `/apply`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "orderId": 123,
    "orderNo": "ORD123",
    "type": 1, 
    "reason": "商品破损",
    "items": [
      { "orderItemId": 1001, "count": 1 }
    ],
    "description": "...",
    "images": []
  }
  ```

#### 8.2 获取售后记录 (分页)
- **URL**: `/list`
- **Method**: `GET`
- **Params**: `pageNum`, `pageSize`
- **Response**: `Result<PageResult<AfterSaleVO>>`

#### 8.3 售后明细数据查询

- **URL**: `/user/after-sales/list`
- **Response**: 返回 `PageResult<AfterSaleVO>`，其中包含 `items` (List<AfterSaleItemVO>)。

---

## 9. 公共接口 (Common Module)

**Prefix**: `/common`

#### 9.1 行政区划树
- **URL**: `/region/tree`
- **Method**: `GET`
- **Response**: `Result<List<RegionVO>>`

#### 9.2 文件上传
- **URL**: `/upload`
- **Method**: `POST`
- **Body**: `FormData (file)`
- **Response**: `Result<String>` (返回上传后的文件URL)

#### 9.3 实时聊天 (WebSocket/STOMP)

##### 9.3.1 基础配置与鉴权 (Infrastructure)
- **握手地址**: `ws://localhost:9090/ws-chat` (SockJS 支持)
- **身份验证**: 
  - **CONNECT 阶段**: 必须在 STOMP CONNECT 帧的 Header 中携带 `Authorization: Bearer <token>`。
  - **SEND 阶段**: 建议在消息帧 Header 中同样携带 `Authorization` 以增强稳定性。

##### 9.3.2 辅助 REST 接口 (Chat REST API)

- **查询客服在线状态**
  - **URL**: `/common/chat/status`
  - **Method**: `GET`
  - **Response**: `Result<Boolean>` (true=有客服在线, false=全部离线)

- **获取客服当前活跃会话 (商家端)**
  - **URL**: `/common/chat/sessions`
  - **Method**: `GET`
  - **Response**: `Result<List<String>>`
    ```json
    { "code": 200, "data": ["202403010001", "202403010002"] }
    ```

- **获取订单聊天历史**
  - **URL**: `/common/chat/history/{orderNo}`
  - **Method**: `GET`
  - **Path Variable**: `orderNo` (关联的业务订单号)
  - **Response**: `Result<List<ChatMessage>>`
    ```json
    {
      "code": 200,
      "data": [
        { "fromUserId": "1", "content": "你好", "createdAt": "2024-03-01T10:00:00" }
      ]
    }
    ```

##### 9.3.3 消息交互协议 (STOMP Protocol)

- **发送消息 (Client -> Server)**:
  - **Destination**: `/app/chat.send`
  - **Body**:
    ```json
    {
      "orderNo": "202402271001",  // String: 订单号
      "toUserId": "admin",        // String: 接收者 (固定为 admin)
      "content": "您好，咨询售后",  // String: 消息文本
      "msgType": 1                // Integer: 1-文本, 2-图片
    }
    ```
- **接收消息 (Server -> Client)**:
  - **Subscription**: `/user/queue/chat` (用户私有订阅通道)
  - **Message**: 包含 `fromUserId`, `createdAt`, `isRead` 等完整字段。

- **系统状态通知**:
  - 当客服接入或处于离线留言模式时，系统会推送 `fromUserId: "SYSTEM"` 的消息至订阅通道。

#### 9.4 全量商品分类列表
- **URL**: `/common/category/list`
- **Method**: `GET`
- **Description**: 获取数据库中所有分类的 ID 与名称映射（带缓存）。
- **Response**: `Result<Map<Integer, String>>` (如: `{ "1": "电子数码", "2": "服装服饰" }`)

#### 9.5 获取全量分类树 (结构化)
- **URL**: `/common/category/tree`
- **Method**: `GET`
- **Description**: 返回递归嵌套的分类树状结构，适用于导航菜单或分类管理展示。
- **Response**: `Result<List<CategoryVO>>`

---

## 10. 商家管理模块 (Admin Module)

**Global Prefix**: `/admin`
**权限要求**: 所有 `/admin/**` 接口（登录除0外）均需 Header `Authorization: Bearer <token>` 且用户角色为 `ADMIN`。

### 10.1 管理员认证

#### 10.1.1 管理员登录
- **URL**: `/admin/login`
- **Method**: `POST`
- **Body**:
  ```json
  { "username": "admin", "password": "..." }
  ```
- **Response**: `Result<UserLoginVo>`
  ```json
  {
    "token": "uuid",
    "userId": 1,
    "username": "admin",
    "balance": 0.00,
    "role": "ADMIN",
    "permissions": ["product", "order", "chat"]
  }
  ```

#### 10.1.2 管理员退出
- **URL**: `/admin/logout`
- **Method**: `POST`
- **Description**: 清除管理员的 Redis 登录状态。

### 10.2 商品管理 (Product Management)

#### 10.2.1 获取商品详情
- **URL**: `/admin/product/detail/{id}`
- **Method**: `GET`
- **Path Variable**: `id` (商品ID)
- **Description**: 获取商品完整详情，返回 `ProductDetailVO`。

#### 10.2.2 获取商品列表 (分页)
- **URL**: `/admin/product/list`
- **Method**: `GET`
- **Params**: `pageNum`, `pageSize`, `keyword`, `categoryId`
- **Description**: 支持全量搜索，返回 `ProductInfoVO` 结构，其中 `categoryId` 为分类名称字符串。
- **Response**: `Result<PageResult<ProductInfoVO>>`
  ```json
  {
    "code": 200,
    "data": {
      "list": [
        { "id": 1, "title": "商品名", "categoryName": "数码", "status": 1 }
      ],
      "total": 100
    }
  }
  ```

#### 10.2.3 更新商品信息
- **URL**: `/admin/product/update`
- **Method**: `PUT`
- **Body (`ProductDTO`)**:
  ```json
  {
    "id": 1, 
    "title": "新款智能手机",
    "description": "简短描述",
    "categoryId": 5,
    "cover": "封面图",
    "price": 2999.00,
    "originalPrice": 3499.00,
    "stock": 100,
    "status": 1,
    "sliderImgs": ["轮播图1", "轮播图2"],
    "detailHtml": "<p>富文本详情</p>",
    "serviceInfo": "服务说明",
    "specs": {"颜色": "星空黑", "内存": "12G+256G"}
  }
  ```
- **Description**: 新增 (id为空) 或修改商品，同步保存详情表。

#### 10.2.3 更新商品状态 (上/下架)
- **URL**: `/admin/product/status/{id}`
- **Method**: `PUT`
- **Path Variable**: `id` (商品ID)
- **Params**: `status` (Integer: 1=上架, 0=下架)
- **Example**: `/admin/product/status/1?status=0`

#### 10.2.4 删除商品
- **URL**: `/admin/product/delete/{id}`
- **Method**: `DELETE`
- **Path Variable**: `id` (商品ID)
- **Description**: 物理删除商品记录。

### 10.4 分类管理 (Category Management)

#### 10.4.1 新增分类
- **URL**: `/admin/category/add`
- **Method**: `POST`
- **Body (`CategoryDTO`)**: 
  ```json
  { 
    "parentId": 0, 
    "name": "新分类", 
    "sort": 1 
  }
  ```
- **Constraints**: 
  - `name`: 必填。
  - `parentId`: 0 或 null 表示一级分类。
  - 后端会校验层级深度，最高支持 3 层。

#### 10.4.2 修改分类
- **URL**: `/admin/category/update`
- **Method**: `PUT`
- **Body (`CategoryDTO`)**: 
  ```json
  { 
    "id": 1,
    "parentId": 0, 
    "name": "修改后的名称", 
    "sort": 10 
  }
  ```
- **Note**: `id` 为必填项。

#### 10.4.3 删除分类
- **URL**: `/admin/category/delete/{id}`
- **Method**: `DELETE`
- **Note**: 删除操作会同步刷新全系统的分类名称缓存。

### 10.5 首页统计报表 (Dashboard Statistics)

用于商家端首页（Home.vue）的数据可视化展示。

#### 10.5.1 热销商品排行 (Top 10)
- **URL**: `/admin/stats/sales`
- **Method**: `GET`
- **Description**: 获取销量排名前 10 的商品。
- **Response**: `Result<List<ProductStatVO>>`
  ```json
  [
    { "id": 1, "title": "华为 Mate 60 Pro", "sales": 500, "rating": 4.9 },
    { "id": 2, "title": "iPhone 15 Pro Max 钛金属版", "sales": 450, "rating": 4.7 }
  ]
  ```

#### 10.5.2 售后分布排行 (售后频次最高)
- **URL**: `/admin/stats/after-sale`
- **Method**: `GET`
- **Description**: 获取售后申请最频繁的商品分布（用于饼图）。
- **Response**: `Result<List<ProductStatVO>>`
  ```json
  [
    { "id": 1, "title": "华为 Mate 60 Pro", "afterSaleCount": 15 },
    { "id": 3, "title": "Xiaomi 14 Pro", "afterSaleCount": 8 }
  ]
  ```

#### 10.5.3 商品口碑排行 (综合评价得分)
- **URL**: `/admin/stats/rating`
- **Method**: `GET`
- **Description**: 获取评价得分最高的商品数据（用于折线图）。
- **Response**: `Result<List<ProductStatVO>>`
  ```json
  [
    { "id": 1, "title": "华为 Mate 60 Pro", "rating": 4.9 },
    { "id": 4, "title": "Sony WH-1000XM5", "rating": 4.8 }
  ]
  ```

### 10.3 订单管理 (Order Management)

#### 10.3.1 获取订单列表 (分页)
- **URL**: `/admin/order/list`
- **Method**: `GET`
- **Params**: 
  - `pageNum`: 页码 (默认1)
  - `pageSize`: 每页条数 (默认10)
  - `status`: 订单状态过滤
  - `keyword`: **查询关键字 (仅支持订单号前缀匹配)**
  - `userId`: 用户ID过滤
- **Description**: 获取全量用户订单列表。
- **Response**: `Result<PageResult<OrderVO>>`
  ```json
  {
    "code": 200,
    "data": {
      "list": [
        { "id": 1, "orderNo": "202403010001", "payAmount": 99.00, "status": 1 }
      ],
      "total": 200
    }
  }
  ```

#### 10.3.2 订单发货
- **URL**: `/admin/order/ship/{id}`
- **Method**: `POST`
- **Path Variable**: `id` (订单数据库ID)
- **Body (`OrderShipDTO`)**:
  ```json
  {
    "logisticsNo": "SF123456789",
    "logisticsCompany": "顺丰速运"
  }
  ```
- **Description**: 将订单状态由 1(待发货) 改为 2(已发货)。

#### 10.3.3 取消订单
- **URL**: `/admin/order/cancel`
- **Method**: `POST`
- **Body (`OrderCancelDTO`)**:
  ```json
  {
    "orderId": 123,
    "reason": "商家缺货"
  }
  ```
- **Description**: 商家手动取消订单，仅限待支付状态。成功后状态变为 `4 (已关闭)`，并回滚商品库存。

---

## 11. 客服管理模块 (Servicer Module - ADMIN ONLY)

### 11.1 客服账号列表
- **URL**: `/admin/servicer/list`
- **Method**: `GET`
- **Params**: `pageNum`, `pageSize`, `username` (可选，模糊查询)
- **Description**: 分页获取所有客服账号的基本信息。
- **Response**: `Result<PageResult<UserInfoVO>>`
  ```json
  {
    "code": 200,
    "data": {
      "list": [
        { "id": 2, "username": "kefu01", "nickname": "客服A", "role": "SERVICER" }
      ],
      "total": 1
    }
  }
  ```

### 11.2 创建/更新客服账号
- **URL**: `/admin/servicer/save`
- **Method**: `POST`
- **Body (`ServicerDTO`)**:
  ```json
  {
    "id": 1, 
    "username": "kefu01",
    "password": "password",
    "nickname": "客服小美",
    "permissions": ["product", "order"]
  }
  ```
- **Description**: 如果 `id` 为空则新增，不为空则更新。`permissions` 为客服可操作的页面标识符列表。

---

## 12. 评价模块 (Review Module)

**Prefix**: `/review`

### 12.1 提交商品评价
- **URL**: `/submit`
- **Method**: `POST`
- **Body (`ProductReviewDTO`)**:
  ```json
  {
    "orderId": 123,
    "reviews": [
      {
        "orderItemId": 1001,
        "productId": 201,
        "rating": 5,
        "content": "商品质量非常好！",
        "images": ["url1", "url2"],
        "isAnonymous": false
      }
    ]
  }
  ```
- **Description**: 仅限 `status=3` (已完成) 且 `is_commented=0` 的订单/订单明细可进行评价。支持多商品批量评价或单品追加评价。评价后会自动同步对应 `order_items` 和 `orders` 表的评价状态。

### 12.2 获取我的评价列表
- **URL**: `/my`
- **Method**: `GET`
- **Params**: `pageNum`, `pageSize`
- **Response**: `Result<PageResult<ProductReviewVO>>`
  ```json
  {
    "code": 200,
    "data": {
      "list": [
        {
          "id": 1,
          "productId": 201,
          "productTitle": "商品名",
          "productCover": "url",
          "rating": 5,
          "content": "评价内容",
          "images": ["url1"],
          "reply": "商家回复内容",
          "createdAt": "2024-03-01T12:00:00"
        }
      ],
      "total": 1
    }
  }
  ```

