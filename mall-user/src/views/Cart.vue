<template>
  <div class="cart-container">
    <h2>我的购物车</h2>
    
    <div v-if="cartList.length > 0">
      <el-table :data="cartList" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column label="商品" width="400">
          <template #default="{ row }">
            <div class="goods-info" @click="goDetail(row.productId)">
              <img :src="row.productCover" width="60" height="60" />
              <span>{{ row.productTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="scope">¥{{ scope.row.productPrice }}</template>
        </el-table-column>
        <el-table-column label="数量" width="200">
          <template #default="scope">
            <el-input-number 
              v-model="scope.row.count" 
              :min="1" 
              size="small" 
              @change="(val: number) => handleCountChange(scope.row.id, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计">
          <template #default="scope">
            <span class="subtotal">¥{{ (scope.row.productPrice * scope.row.count).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-popconfirm 
              title="确定要从购物车中移除这件宝贝吗？" 
              @confirm="handleDelete(scope.row.id)"
              confirm-button-text="移除"
              cancel-button-text="留着"
              confirm-button-type="danger"
            >
              <template #reference>
                <el-button type="text" class="delete-btn">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer">
        <div class="left">
          <el-button type="text">清空购物车</el-button>
        </div>
        <div class="right">
          <span>已选商品 <span class="highlight">{{ selectedCount }}</span> 件</span>
          <span class="total-label">合计：</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          <el-button type="primary" size="large" class="checkout-btn" @click="checkout" :disabled="selectedCount === 0">结 算</el-button>
        </div>
      </div>
    </div>
    
    <el-empty v-else description="购物车空空如也" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import type { CartItem } from '../api/mock'

const router = useRouter()
const cartList = ref<CartItem[]>([])
const multipleSelection = ref<CartItem[]>([])

onMounted(async () => {
  const res = await api.getCart()
  cartList.value = res.list || []
})

const handleSelectionChange = (val: CartItem[]) => {
  multipleSelection.value = val
}

let debounceTimer: any = null
const handleCountChange = (id: number, count: number) => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(async () => {
    try {
      await api.updateCart(id, count)
    } catch (error) {
      console.error('Failed to update cart count:', error)
    }
  }, 500)
}

const handleDelete = async (id: number) => {
  try {
    await api.deleteCart(id)
    // 重新获取列表
    const res = await api.getCart()
    cartList.value = res.list || []
  } catch (error) {
    console.error('Failed to delete cart item:', error)
  }
}

const selectedCount = computed(() => multipleSelection.value.length)
const totalPrice = computed(() => {
  return multipleSelection.value.reduce((sum, item) => {
    // 适配后端字段名 productPrice
    const price = (item as any).productPrice || 0
    return sum + price * item.count
  }, 0)
})

const checkout = () => {
  if (multipleSelection.value.length === 0) return
  const selectedIds = multipleSelection.value.map(item => item.id).join(',')
  router.push({
    path: '/checkout',
    query: { ids: selectedIds }
  })
}
</script>

<style scoped>
.cart-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  min-height: 400px;
}

.goods-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.goods-title {
  font-size: 14px;
}

.subtotal {
  color: #ff5000;
  font-weight: bold;
}

.delete-btn {
  color: #999;
}

.delete-btn:hover {
  color: #ff5000;
}

.cart-footer {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.highlight {
  color: #ff5000;
  font-size: 18px;
  font-weight: bold;
  margin: 0 5px;
}

.total-label {
  margin-left: 20px;
}

.total-price {
  color: #ff5000;
  font-size: 24px;
  font-weight: bold;
  margin-right: 20px;
}

.checkout-btn {
  background-color: #ff5000;
  border-color: #ff5000;
  width: 120px;
}
</style>
