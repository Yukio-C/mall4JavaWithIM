<template>
  <div class="product-list">
    <!-- 1. 搜索筛选栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索商品标题" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable style="width: 150px">
            <el-option 
              v-for="(name, id) in categories" 
              :key="id" 
              :label="name" 
              :value="Number(id)" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>商品数据</span>
          <el-button type="primary" @click="openDialog()">发布商品</el-button>
        </div>
      </template>

      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <el-image :src="scope.row.cover" style="width: 50px; height: 50px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="商品名称" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="sales" label="销量" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch 
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(scope.row.id, val as number)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 编辑/发布弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '发布商品'" width="700px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基础信息" name="basic">
          <el-form :model="form" label-width="100px" style="margin-top: 20px">
            <el-form-item label="商品名称" required>
              <el-input v-model="form.title" placeholder="请输入商品标题" />
            </el-form-item>
            <el-form-item label="分类" required>
              <el-select v-model="form.categoryId" placeholder="请选择商品分类" style="width: 100%">
                <el-option 
                  v-for="(name, id) in categories" 
                  :key="id" 
                  :label="name" 
                  :value="Number(id)" 
                />
              </el-select>
            </el-form-item>
            <el-row>
              <el-col :span="12">
                <el-form-item label="价格 (元)" required>
                  <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="原价 (元)">
                  <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="库存数量" required>
              <el-input-number v-model="form.stock" :min="0" :step="1" />
            </el-form-item>
            
            <el-form-item label="商品封面">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :http-request="handleUpload"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="form.cover" :src="form.cover" class="avatar-preview" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>

            <el-form-item label="简短描述">
              <el-input v-model="form.description" type="textarea" :rows="2" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="图文详情" name="detail">
          <el-form :model="form" label-width="100px" style="margin-top: 20px">
            <el-form-item label="轮播大图">
              <el-upload
                action="#"
                list-type="picture-card"
                :file-list="sliderFileList"
                :http-request="handleSliderUpload"
                :on-remove="handleSliderRemove"
                multiple
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">建议上传 3-5 张，每张不超过 2MB</div>
            </el-form-item>

            <el-form-item label="图文详情">
              <el-input 
                v-model="form.detailHtml" 
                type="textarea" 
                :rows="10" 
                placeholder="此处支持 HTML 源码，用于在移动端展示详情" 
              />
            </el-form-item>

            <el-form-item label="服务说明">
              <el-input v-model="form.serviceInfo" placeholder="如：顺丰包邮 | 7天无理由退货" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { api } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const activeTab = ref('basic')

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 动态分类数据
const categories = ref<Record<string, string>>({})

// 搜索表单
const searchForm = ref({
  keyword: '',
  categoryId: undefined as number | undefined
})

// 自动推导反向映射表 (Name -> ID)
const categoryNameMap = computed(() => {
  const map: Record<string, number> = {}
  Object.entries(categories.value).forEach(([id, name]) => {
    map[name] = Number(id)
  })
  return map
})

const form = ref<any>({
  id: null,
  title: '',
  price: 0,
  originalPrice: 0,
  stock: 0,
  categoryId: undefined,
  description: '',
  cover: '',
  status: 1,
  sliderImgs: [] as string[],
  detailHtml: '',
  serviceInfo: '',
  specs: {}
})

// 轮播图文件列表
const sliderFileList = ref<any[]>([])

const fetchList = async () => {
  loading.value = true
  try {
    const res = await api.getProducts({ 
      pageNum: pageNum.value, 
      pageSize: pageSize.value,
      keyword: searchForm.value.keyword || undefined,
      categoryId: searchForm.value.categoryId
    })
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error('获取列表失败:', e)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    categoryId: undefined
  }
  handleSearch()
}

// 加载动态分类
const loadCategories = async () => {
  try {
    categories.value = await api.getCategories()
  } catch (e) {
    console.error('加载分类失败:', e)
  }
}

onMounted(() => {
  fetchList()
  loadCategories()
})

// 图片上传处理
const handleUpload = async (options: any) => {
  try {
    const url = await api.uploadFile(options.file)
    form.value.cover = url
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const beforeAvatarUpload = (rawFile: any) => {
  const isImg = ['image/jpeg', 'image/png', 'image/webp'].includes(rawFile.type)
  if (!isImg) {
    ElMessage.error('图片格式必须是 JPG/PNG/WEBP!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleStatusChange = async (id: number, val: number) => {
  await api.updateProductStatus(id, val)
  ElMessage.success(val === 1 ? '已上架' : '已下架')
  const product = tableData.value.find(p => p.id === id)
  if (product) product.status = val
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确认删除该商品吗?', '提示', {
    type: 'warning'
  }).then(async () => {
    await api.deleteProduct(id)
    ElMessage.success('删除成功')
    fetchList()
  })
}

const openDialog = async (row?: any) => {
  activeTab.value = 'basic'
  sliderFileList.value = []
  
  if (row) {
    isEdit.value = true
    loading.value = true
    try {
      // 关键：从后端拉取包含详情的完整数据
      const fullData = await api.getProductDetail(row.id)
      form.value = { ...fullData }
      
      // 初始化轮播图回显
      if (form.value.sliderImgs) {
        sliderFileList.value = form.value.sliderImgs.map((url: string) => ({ url }))
      }
    } catch (e) {
      ElMessage.error('加载商品详情失败')
    } finally {
      loading.value = false
    }
  } else {
    isEdit.value = false
    const firstId = Object.keys(categories.value)[0]
    form.value = {
      id: null,
      title: '',
      price: 0,
      originalPrice: 0,
      stock: 100,
      categoryId: firstId ? Number(firstId) : undefined,
      description: '',
      cover: '',
      status: 1,
      sliderImgs: [],
      detailHtml: '',
      serviceInfo: '顺丰包邮 | 7天无理由退货',
      specs: {}
    }
  }
  dialogVisible.value = true
}

// 轮播图上传处理
const handleSliderUpload = async (options: any) => {
  try {
    const url = await api.uploadFile(options.file)
    if (!form.value.sliderImgs) form.value.sliderImgs = []
    form.value.sliderImgs.push(url)
    sliderFileList.value.push({ url })
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const handleSliderRemove = (file: any) => {
  const url = file.url
  form.value.sliderImgs = form.value.sliderImgs.filter((item: string) => item !== url)
  sliderFileList.value = sliderFileList.value.filter(item => item.url !== url)
}

const submitForm = async () => {
  if (!form.value.categoryId) return ElMessage.warning('请选择商品分类')
  
  submitting.value = true
  try {
    await api.updateProduct(form.value)
    ElMessage.success(isEdit.value ? '更新成功' : '发布成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error('提交失败:', e)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.product-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-card {
  background-color: #fff;
}

.search-form {
  display: flex;
  align-items: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 上传组件样式 */
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
