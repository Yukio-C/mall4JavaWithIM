<template>
  <div class="servicer-list">
    <!-- 搜索筛选栏 -->
    <el-card class="search-card" style="margin-bottom: 20px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input 
            v-model="searchForm.username" 
            placeholder="搜索用户名" 
            clearable 
            @keyup.enter="handleSearch"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>客服账号管理</span>
          <el-button type="primary" size="small" @click="handleAdd">新增客服</el-button>
        </div>
      </template>

      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column label="权限页面">
          <template #default="scope">
            <el-tag 
              v-for="p in scope.row.permissions" 
              :key="p" 
              size="small" 
              style="margin-right: 5px"
            >
              {{ getPermLabel(p) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑权限</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; text-align: right">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑客服' : '新增客服'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="密码" :required="!form.id">
          <el-input v-model="form.password" type="password" placeholder="不填则默认为 123456" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="客服显示名称" />
        </el-form-item>
        <el-form-item label="页面权限">
          <el-checkbox-group v-model="form.permissions">
            <el-checkbox label="product">商品管理</el-checkbox>
            <el-checkbox label="category">分类管理</el-checkbox>
            <el-checkbox label="order">订单管理</el-checkbox>
            <el-checkbox label="chat">在线客服</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)

// 分页状态
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = ref({
  username: ''
})

const form = ref({
  id: null as number | null,
  username: '',
  password: '',
  nickname: '',
  permissions: [] as string[]
})

const permMap: any = {
  product: '商品管理',
  category: '分类管理',
  order: '订单管理',
  chat: '在线客服'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await api.getServicers({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      username: searchForm.value.username || undefined
    })
    tableData.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

const resetSearch = () => {
  searchForm.value.username = ''
  handleSearch()
}

const getPermLabel = (key: string) => permMap[key] || key

const handleAdd = () => {
  form.value = { id: null, username: '', password: '', nickname: '', permissions: ['order'] }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  form.value = {
    id: row.id,
    username: row.username,
    password: '',
    nickname: row.nickname,
    // 关键修复：直接赋值后端解析好的 List，实现回显
    permissions: row.permissions || []
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.username) return ElMessage.warning('用户名不能为空')
  
  submitting.value = true
  try {
    await api.saveServicer(form.value)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
