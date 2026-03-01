<template>
  <div class="address-container">
    <div class="header">
      <div class="title-section">
        <h3>收货地址</h3>
        <span class="subtitle">管理您的收货地址信息</span>
      </div>
      <el-button type="primary" @click="openDialog('add')">
        <el-icon class="el-icon--left"><Plus /></el-icon>新增地址
      </el-button>
    </div>

    <el-empty v-if="list.length === 0" description="暂无收货地址" />

    <div class="addr-grid" v-else>
      <el-card v-for="addr in list" :key="addr.id" class="addr-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <span class="name">{{ addr.name }}</span>
              <el-tag v-if="addr.tag" size="small" :type="getTagType(addr.tag)" effect="plain">
                {{ addr.tag }}
              </el-tag>
              <el-tag v-if="isDefaultAddress(addr)" size="small" type="danger" effect="dark" class="default-tag">默认</el-tag>
            </div>
            <div class="actions">
              <el-button type="primary" link :icon="Edit" @click="openDialog('edit', addr)">编辑</el-button>
              <el-divider direction="vertical" />
              <el-button type="danger" link :icon="Delete" @click="handleDelete(addr.id)">删除</el-button>
            </div>
          </div>
        </template>
        <div class="card-content">
          <div class="info-row">
            <el-icon><Iphone /></el-icon>
            <span class="info-text">{{ addr.phone }}</span>
          </div>
          <div class="info-row address-detail">
            <el-icon><Location /></el-icon>
            <span class="info-text">
              <span class="region-text">{{ addr.province }} {{ addr.city }} {{ addr.district }}</span>
              <div class="detail-text">{{ addr.detail }}</div>
            </span>
          </div>
        </div>
      </el-card>
    </div>

    <div class="pagination-container" v-if="list.length > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[4, 8, 12, 20]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog 
      v-model="visible" 
      :title="dialogType === 'add' ? '新增收货地址' : '编辑收货地址'" 
      width="560px"
      destroy-on-close
    >
      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px" class="address-form">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="form.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入11位手机号码" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-if="regionOptions.length > 0"
            v-model="form.region"
            :options="regionOptions"
            placeholder="请选择省/市/区"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input 
            v-model="form.detail" 
            type="textarea" 
            :rows="3"
            placeholder="请输入街道、门牌号等详细信息" 
          />
        </el-form-item>
        <el-form-item label="标签" prop="tag">
          <el-radio-group v-model="form.tag">
            <el-radio-button label="家" value="家" />
            <el-radio-button label="公司" value="公司" />
            <el-radio-button label="学校" value="学校" />
          </el-radio-group>
          <!-- 允许自定义标签 -->
          <div class="custom-tag">
             <el-input v-if="isCustomTag" v-model="form.tag" placeholder="自定义标签" size="small" style="width: 120px; margin-left: 10px;" />
          </div>
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="visible = false">取消</el-button>
          <el-button type="primary" @click="submit" :loading="submitting">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { api } from '../../api'
import type { Address } from '../../api/mock'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Iphone, Location } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const list = ref<Address[]>([])
const visible = ref(false)
const submitting = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const regionOptions = ref<any[]>([])

const pagination = ref({
  pageNum: 1,
  pageSize: 4,
  total: 0
})

interface AddressForm {
  id?: number
  name: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  tag: string
  isDefault: boolean
  region: string[] // UI select value
}

const form = ref<AddressForm>({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  tag: '',
  isDefault: false,
  region: []
})

const rules = ref<FormRules>({
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  region: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
})

const isCustomTag = computed(() => {
  return !['家', '公司', '学校', ''].includes(form.value.tag)
})

const isDefaultAddress = (addr: any) => {
  return (
    String(addr.isDefault) === 'true' || 
    String(addr.isDefault) === '1' || 
    String(addr.is_default) === 'true' || 
    String(addr.is_default) === '1'
  )
}

const fetchList = async () => {
  try {
    const res = await api.getAddresses({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    })
    // 兼容返回结构
    if (res && res.list) {
      list.value = res.list
      pagination.value.total = res.total
    } else if (Array.isArray(res)) {
      // 兼容旧接口直接返回数组的情况 (如 mock 未更新)
      list.value = res
      pagination.value.total = res.length
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  fetchList()
}

const handleCurrentChange = (val: number) => {
  pagination.value.pageNum = val
  fetchList()
}

const fetchRegions = async () => {
  try {
    regionOptions.value = await api.getRegions()
  } catch (error) {
    console.error('Failed to fetch regions', error)
  }
}

onMounted(() => {
  fetchList()
  fetchRegions()
})

const getTagType = (tag: string) => {
  switch (tag) {
    case '家': return 'success'
    case '公司': return 'primary'
    case '学校': return 'warning'
    default: return 'info'
  }
}

const openDialog = (type: 'add' | 'edit', addr?: Address) => {
  dialogType.value = type
  
  if (type === 'edit' && addr) {
    form.value = { 
      id: addr.id,
      name: addr.name,
      phone: addr.phone,
      province: addr.province,
      city: addr.city,
      district: addr.district,
      detail: addr.detail,
      tag: addr.tag || '',
      isDefault: isDefaultAddress(addr),
      region: [addr.province, addr.city, addr.district].filter(Boolean)
    }
  } else {
    form.value = { 
      name: '', 
      phone: '', 
      province: '', 
      city: '', 
      district: '', 
      detail: '', 
      tag: '家', 
      isDefault: false,
      region: []
    }
  }
  visible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 由于级联选择器绑定的是数组 [省, 市, 区] 且 options 的 value 就是名称
        const [province, city, district] = form.value.region
        const payload = {
          ...form.value,
          province,
          city,
          district,
          isDefault: form.value.isDefault ? 1 : 0
        }
        delete (payload as any).region // 移除前端 UI 专用字段
        
        if (dialogType.value === 'edit') {
          await api.updateAddress(payload)
          ElMessage.success('修改成功')
        } else {
          await api.addAddress(payload)
          ElMessage.success('添加成功')
        }
        visible.value = false
        fetchList()
      } catch (error) {
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定删除该地址吗？此操作不可恢复', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.deleteAddress(id)
      ElMessage.success('已删除')
      fetchList()
    } catch (error) {
       console.error(error)
    }
  }).catch(() => {})
}
</script>

<style scoped>
.address-container {
  padding: 20px;
  background-color: #fff;
  min-height: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.title-section h3 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.subtitle {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
  display: block;
}

.addr-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.addr-card {
  transition: all 0.3s;
  border: 1px solid #ebeef5;
}

.addr-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.default-tag {
  transform: scale(0.9);
}

.card-content {
  padding-top: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  color: #606266;
  font-size: 14px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .el-icon {
  margin-right: 8px;
  color: #909399;
  font-size: 16px;
}

.region-text {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
  display: block;
}

.detail-text {
  color: #303133;
  font-weight: 500;
}

.address-detail {
  align-items: flex-start;
  line-height: 1.5;
}

.address-detail .el-icon {
  margin-top: 2px;
}

.custom-tag {
  display: inline-flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>