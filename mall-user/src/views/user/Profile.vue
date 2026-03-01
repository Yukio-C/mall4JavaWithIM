<template>
  <div class="profile-container">
    <h3>个人资料</h3>
    <el-form :model="form" label-width="80px" style="max-width: 400px; margin-top: 20px;">
      <el-form-item label="头像">
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :http-request="handleUpload"
          :before-upload="beforeAvatarUpload"
        >
          <img :src="form.avatar || defaultAvatar" class="avatar" />
        </el-upload>
        <div class="avatar-tip">点击头像可更换</div>
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="form.username" disabled />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" disabled />
      </el-form-item>
      <el-form-item label="余额">
        <span class="balance">¥{{ form.balance }}</span>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save" :loading="saving">保存修改</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import type { User } from '../../api/mock'

const form = ref<User>({} as User)
const saving = ref(false)
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

onMounted(async () => {
  // 强制刷新：确保在个人资料页看到的数据是最新的
  const data = await userStore.fetchUserInfo(true)
  form.value = { ...data }
})

const handleUpload = async (options: UploadRequestOptions) => {
  const file = options.file
  try {
    // 调用上传接口
    const url = await api.uploadFile(file)
    form.value.avatar = url
    ElMessage.success('上传成功')
  } catch (error) {
    // 错误处理通常在 request.ts 中已经有了，这里可以补充逻辑
    console.error(error)
  }
}

const beforeAvatarUpload = (rawFile: File) => {
  const isImage = rawFile.type.startsWith('image/')
  const isLt2M = rawFile.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('头像必须是图片格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

const save = async () => {
  saving.value = true
  try {
    // 安全优化：仅提交允许修改的字段，防止篡改余额等敏感信息
    const updateData = {
      nickname: form.value.nickname,
      avatar: form.value.avatar
    }
    await api.updateProfile(updateData)
    // 更新成功后，更新 Store 里的数据，保持一致
    await userStore.fetchUserInfo(true) 
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
  transition: opacity 0.3s;
}

.avatar-uploader .avatar:hover {
  opacity: 0.8;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  line-height: 0; /* 防止图片下方出现微小间隙 */
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
}

.balance {
  color: #ff5000;
  font-weight: bold;
}

.avatar-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
  line-height: 1;
}
</style>