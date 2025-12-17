<template>
  <div class="admin-settings">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>系统设置</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>管理后台</el-breadcrumb-item>
          <el-breadcrumb-item>系统设置</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 设置内容 -->
    <div class="settings-container">
      <!-- 基本设置 -->
      <div class="settings-panel">
        <h2>基本设置</h2>
        <el-form
          ref="generalFormRef"
          :model="generalForm"
          :rules="generalRules"
          label-width="140px"
          v-loading="loading"
        >
          <el-form-item label="系统名称" prop="systemName">
            <el-input v-model="generalForm.systemName" placeholder="请输入系统名称" />
          </el-form-item>
          <el-form-item label="系统Logo">
            <el-upload
              class="logo-uploader"
              action="#"
              :show-file-list="false"
              :auto-upload="true"
              :before-upload="beforeLogoUpload"
              :http-request="handleLogoUpload"
            >
              <img v-if="generalForm.systemLogo" :src="generalForm.systemLogo" class="logo-preview" />
              <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">建议尺寸: 200x60px, 支持 PNG/JPG 格式, 最大16MB</div>
          </el-form-item>
          <el-form-item label="联系邮箱" prop="contactEmail">
            <el-input v-model="generalForm.contactEmail" placeholder="请输入联系邮箱" />
          </el-form-item>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="generalForm.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="系统公告">
            <el-input
              v-model="generalForm.systemAnnouncement"
              type="textarea"
              :rows="4"
              placeholder="请输入系统公告内容"
            />
          </el-form-item>
          <el-form-item label="维护模式">
            <el-switch
              v-model="generalForm.maintenanceMode"
              active-text="开启"
              inactive-text="关闭"
            />
            <div class="form-tip">开启后普通用户将无法访问系统</div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSettings" :loading="saving">
              保存设置
            </el-button>
            <el-button @click="resetSettings">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getBasicSettings, updateBasicSettings, uploadSystemLogo } from '@/api/admin';

// 加载状态
const loading = ref(false);
const saving = ref(false);

// 基本设置表单
const generalFormRef = ref(null);
const generalForm = reactive({
  systemName: '射击训练管理系统',
  systemLogo: '',
  contactEmail: '',
  contactPhone: '',
  systemAnnouncement: '',
  maintenanceMode: false
});

// 保存原始数据用于重置
const originalData = ref({});

const generalRules = {
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
};

// Logo上传验证
const beforeLogoUpload = (file) => {
  const isImage = file.type === 'image/png' || file.type === 'image/jpeg';
  const isLt16M = file.size / 1024 / 1024 < 16;
  
  if (!isImage) {
    ElMessage.error('只能上传 PNG 或 JPG 格式的图片!');
    return false;
  }
  if (!isLt16M) {
    ElMessage.error('图片大小不能超过 16MB!');
    return false;
  }
  return true;
};

const handleLogoUpload = async (options) => {
  try {
    const formData = new FormData();
    formData.append('file', options.file);
    
    const res = await uploadSystemLogo(formData);
    if (res.success) {
      generalForm.systemLogo = res.url;
      ElMessage.success('Logo上传成功');
    } else {
      ElMessage.error(res.message || 'Logo上传失败');
    }
  } catch (error) {
    console.error('Logo上传失败:', error);
    ElMessage.error('Logo上传失败');
  }
};

// 加载设置
const loadSettings = async () => {
  loading.value = true;
  try {
    const res = await getBasicSettings();
    if (res.success && res.data) {
      Object.assign(generalForm, res.data);
      // 保存原始数据
      originalData.value = { ...res.data };
    }
  } catch (error) {
    console.error('加载设置失败:', error);
    ElMessage.error('加载设置失败');
  } finally {
    loading.value = false;
  }
};

// 保存设置
const saveSettings = async () => {
  if (!generalFormRef.value) return;
  
  await generalFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    saving.value = true;
    try {
      const res = await updateBasicSettings(generalForm);
      if (res.success) {
        ElMessage.success('设置保存成功');
        // 更新原始数据
        originalData.value = { ...generalForm };
      } else {
        ElMessage.error(res.message || '保存失败');
      }
    } catch (error) {
      console.error('保存失败:', error);
      ElMessage.error('保存失败');
    } finally {
      saving.value = false;
    }
  });
};

// 重置设置
const resetSettings = () => {
  Object.assign(generalForm, originalData.value);
  ElMessage.info('已重置为上次保存的设置');
};

onMounted(() => {
  loadSettings();
});
</script>

<style scoped>
.admin-settings {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.header-left h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.settings-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 24px;
  max-width: 800px;
}

.settings-panel h2 {
  margin: 0 0 24px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.logo-uploader {
  width: 200px;
  height: 60px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
  overflow: hidden;
}

.logo-uploader:hover {
  border-color: #409eff;
}

.logo-preview {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-settings {
    padding: 12px;
  }
  
  .settings-container {
    padding: 16px;
  }
}
</style>
