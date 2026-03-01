<template>
  <div class="checkout-page">
    <div class="checkout-container">
      <div class="checkout-header">
        <h1 class="main-title">确认订单</h1>
        <div class="stepper">
          <span class="step active">1. 确认订单</span>
          <span class="step-line"></span>
          <span class="step">2. 完成支付</span>
        </div>
      </div>

      <div class="main-layout">
        <!-- 左侧：核心信息 -->
        <div class="content-left">
          <!-- 地址选择 -->
          <div class="section-card">
            <div class="section-title">收货人信息</div>
            <div v-if="selectedAddress" class="address-box" @click="addressDialogVisible = true">
              <div class="addr-icon"><el-icon><Location /></el-icon></div>
              <div class="addr-info">
                <div class="user-meta">
                  <span class="name">{{ selectedAddress.name }}</span>
                  <span class="phone">{{ selectedAddress.phone }}</span>
                </div>
                <div class="addr-text">
                  {{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detail }}
                </div>
              </div>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
            <div v-else class="empty-address-trigger" @click="addressDialogVisible = true">
              <el-icon><Plus /></el-icon> <span>请选择收货地址</span>
            </div>
          </div>

          <!-- 商品列表 -->
          <div class="section-card">
            <div class="section-title">商品清单</div>
            <div class="items-list">
              <div v-for="item in cartItems" :key="item.id" class="item-row">
                <div class="img-box">
                  <el-image :src="item.productCover" fit="cover" class="product-thumb" />
                  <span class="item-count-badge">{{ item.count }}</span>
                </div>
                <div class="item-meta">
                  <div class="item-name">{{ item.productTitle }}</div>
                  <div class="item-desc">七天无理由退货</div>
                </div>
                <div class="item-price">¥{{ (item.productPrice * item.count).toFixed(2) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：结算卡片 -->
        <div class="content-right">
          <div class="summary-card">
            <h3 class="summary-title">订单摘要</h3>
            <div class="summary-row"><span>商品合计</span><span>¥{{ totalPrice.toFixed(2) }}</span></div>
            <div class="summary-row"><span>运费</span><span class="free">免运费</span></div>
            <div class="summary-divider"></div>
            <div class="total-row">
              <span>实付款</span>
              <span class="final-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <el-button type="primary" class="submit-button" @click="submitOrder" :loading="submitting">
              提交订单
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 收银台弹窗 -->
    <el-dialog v-model="payVisible" title="收银台" width="400px" center class="custom-dialog">
      <div class="pay-modal">
        <div class="pay-price">¥{{ totalPrice.toFixed(2) }}</div>
        <div class="pay-method" @click="payType = 'balance'" :class="{ active: payType === 'balance' }">
          <div class="method-name">余额支付</div>
          <el-icon v-if="payType === 'balance'" color="#ff5000"><Check /></el-icon>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" class="pay-confirm-btn" @click="confirmPay" :loading="paying">立即支付</el-button>
      </template>
    </el-dialog>

    <!-- 地址选择弹窗 -->
    <el-dialog v-model="addressDialogVisible" title="选择地址" width="500px">
      <div class="addr-picker">
        <div v-for="addr in addresses" :key="addr.id" class="picker-item" 
             :class="{ selected: selectedAddressId === addr.id }" @click="selectedAddressId = addr.id">
          <div class="picker-info">
            <div class="p-user"><strong>{{ addr.name }}</strong> {{ addr.phone }}</div>
            <div class="p-detail">{{ addr.province }}{{ addr.city }}{{ addr.detail }}</div>
          </div>
          <el-icon v-if="selectedAddressId === addr.id" color="#ff5000"><Check /></el-icon>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="addressDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from '../api'
import { ElMessage } from 'element-plus'
import { Location, ArrowRight, Plus, Check } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const addresses = ref<any[]>([])
const selectedAddressId = ref<number>()
const cartItems = ref<any[]>([])
const submitting = ref(false)
const payVisible = ref(false)
const payType = ref('balance')
const paying = ref(false)
const currentOrderNo = ref('')
const addressDialogVisible = ref(false)
const orderToken = ref('')

const selectedAddress = computed(() => addresses.value.find(a => a.id === selectedAddressId.value))

onMounted(async () => {
  try {
    const res = await api.getOrderToken()
    orderToken.value = (res && typeof res === 'object') ? res.orderToken : res
  } catch (e) {}

  try {
    const res = await api.getAddresses()
    addresses.value = res.list || []
    if (addresses.value.length > 0) selectedAddressId.value = addresses.value[0].id
  } catch (e) {}

  const selectedIds = route.query.ids ? (route.query.ids as string).split(',').map(Number) : []
  try {
    const res = await api.getCart()
    cartItems.value = (res.list || []).filter((item: any) => selectedIds.includes(item.id))
  } catch (e) {}
})

const totalPrice = computed(() => cartItems.value.reduce((sum, item) => sum + item.productPrice * item.count, 0))

const submitOrder = async () => {
  if (!selectedAddressId.value) return ElMessage.warning('请选择收货地址')
  if (!orderToken.value) return ElMessage.error('页面已过期，请刷新')
  submitting.value = true
  try {
    const payload = {
      addressId: selectedAddressId.value,
      items: cartItems.value.map(item => ({ productId: item.productId, count: item.count })),
      orderToken: orderToken.value
    }
    const order = await api.createOrder(payload)
    orderToken.value = ''
    currentOrderNo.value = order.orderNo
    payVisible.value = true
  } catch (e: any) {
    ElMessage.error(e.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

const confirmPay = async () => {
  paying.value = true
  try {
    await api.payment.pay(currentOrderNo.value, 3)
    ElMessage.success('支付成功')
    router.push('/user/orders')
  } catch (e: any) {
    ElMessage.error(e.message || '支付失败')
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.checkout-page { background: #f7f8fa; min-height: 100vh; }
.checkout-container { max-width: 1200px; margin: 0 auto; padding: 40px 20px; }
.checkout-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.main-title { font-size: 24px; font-weight: bold; color: #333; }
.stepper { display: flex; align-items: center; font-size: 14px; color: #999; }
.step.active { color: #ff5000; font-weight: bold; }
.step-line { width: 40px; height: 1px; background: #ddd; margin: 0 12px; }

.main-layout { display: grid; grid-template-columns: 1fr 380px; gap: 24px; }
.section-card { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.03); }
.section-title { font-size: 16px; font-weight: bold; margin-bottom: 20px; border-left: 4px solid #ff5000; padding-left: 12px; }

.address-box { display: flex; align-items: center; cursor: pointer; padding: 15px; background: #fffaf8; border-radius: 8px; border: 1px dashed #ffdacb; }
.addr-icon { background: #ff5000; color: #fff; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-right: 15px; }
.user-meta .name { font-size: 18px; font-weight: bold; margin-right: 10px; }
.user-meta .phone { color: #666; }
.addr-text { color: #444; font-size: 14px; margin-top: 5px; }

.item-row { display: flex; align-items: center; padding: 15px 0; border-bottom: 1px solid #f5f5f5; }
.product-thumb { width: 90px; height: 90px; border-radius: 8px; margin-right: 20px; border: 1px solid #eee; }
.img-box { position: relative; }
.item-count-badge { position: absolute; top: -5px; right: 15px; background: #999; color: #fff; padding: 0 6px; border-radius: 10px; font-size: 11px; }
.item-name { font-size: 15px; font-weight: 500; margin-bottom: 5px; }
.item-desc { font-size: 12px; color: #999; }
.item-price { margin-left: auto; font-weight: bold; color: #333; }

.summary-card { background: #fff; border-radius: 12px; padding: 24px; position: sticky; top: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
.summary-row { display: flex; justify-content: space-between; margin-bottom: 15px; color: #666; font-size: 14px; }
.summary-divider { height: 1px; background: #eee; margin: 15px 0; }
.final-price { font-size: 28px; color: #ff5000; font-weight: bold; }
.submit-button { width: 100%; height: 50px; font-size: 18px; border-radius: 25px; background: linear-gradient(90deg, #ff9000, #ff5000); border: none; margin-top: 20px; }

.pay-price { font-size: 32px; color: #ff5000; font-weight: bold; text-align: center; margin-bottom: 30px; }
.pay-method { display: flex; justify-content: space-between; padding: 15px; border: 1px solid #eee; border-radius: 8px; cursor: pointer; }
.pay-method.active { border-color: #ff5000; background: #fffaf8; }
.pay-confirm-btn { width: 100%; height: 44px; border-radius: 22px; }
</style>
