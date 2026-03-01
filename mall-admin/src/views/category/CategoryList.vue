<template>
  <div class="category-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分类管理 (最高 3 层)</span>
          <el-button type="primary" @click="openDialog()">新增顶级分类</el-button>
        </div>
      </template>

      <el-table
        :data="tableData"
        style="width: 100%; margin-bottom: 20px"
        row-key="id"
        border
        default-expand-all
        v-loading="loading"
      >
        <el-table-column prop="name" label="分类名称" width="350" />
        <el-table-column prop="id" label="ID" width="120" align="center" />
        <el-table-column prop="sort" label="排序权重" width="120" align="center" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button 
              size="small" 
              type="success" 
              link 
              v-if="getDepth(scope.row) < 3"
              @click="openDialog(undefined, scope.row)"
            >
              新增子类
            </el-button>
            <el-button size="small" link @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="父级分类" v-if="parentName">
          <el-tag type="info">{{ parentName }}</el-tag>
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="form.sort" :min="0" />
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
import { api } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)

const parentName = ref('')
const form = ref({
  id: undefined,
  parentId: 0,
  name: '',
  sort: 0
})

const fetchTree = async () => {
  loading.value = true
  try {
    tableData.value = await api.getCategoryTree()
  } finally {
    loading.value = false
  }
}

onMounted(fetchTree)

// 递归计算节点深度
const getDepth = (node: any): number => {
  let depth = 1
  const findParent = (id: number, currentData: any[]): any => {
    for (const item of currentData) {
      if (item.id === id) return item
      if (item.children) {
        const found = findParent(id, item.children)
        if (found) return found
      }
    }
    return null
  }

  let current = node
  while (current && current.parentId !== 0) {
    depth++
    current = findParent(current.parentId, tableData.value)
  }
  return depth
}

const openDialog = (row?: any, parent?: any) => {
  if (row) {
    // 编辑
    isEdit.value = true
    form.value = { ...row, children: undefined }
    parentName.value = ''
  } else if (parent) {
    // 新增子类
    isEdit.value = false
    parentName.value = parent.name
    form.value = {
      id: undefined,
      parentId: parent.id,
      name: '',
      sort: 0
    }
  } else {
    // 新增顶级
    isEdit.value = false
    parentName.value = ''
    form.value = {
      id: undefined,
      parentId: 0,
      name: '',
      sort: 0
    }
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.name) return ElMessage.warning('名称不能为空')
  submitting.value = true
  try {
    if (isEdit.value) {
      await api.updateCategory(form.value)
      ElMessage.success('更新成功')
    } else {
      await api.addCategory(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchTree()
  } finally {
    submitting.value = false
  }
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定删除吗？如果包含子分类将被一并删除（依后端逻辑而定）。', '警告', {
    type: 'error'
  }).then(async () => {
    await api.deleteCategory(id)
    ElMessage.success('已删除')
    fetchTree()
  })
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
