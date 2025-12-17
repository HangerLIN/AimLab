<template>
  <div class="admin-users">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>用户管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>管理后台</el-breadcrumb-item>
          <el-breadcrumb-item>用户管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalUsers }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.activeUsers }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon admin">
          <el-icon><Avatar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.adminUsers }}</div>
          <div class="stat-label">管理员</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon disabled">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.disabledUsers }}</div>
          <div class="stat-label">已禁用</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="filterRole" placeholder="角色筛选" clearable style="width: 140px">
          <el-option label="全部角色" value="" />
          <el-option label="管理员" value="ADMIN" />
          <el-option label="普通用户" value="USER" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 140px">
          <el-option label="全部状态" value="" />
          <el-option label="正常" value="active" />
          <el-option label="禁用" value="disabled" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <div class="filter-right">
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 用户列表表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="username" label="用户名" min-width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.avatar">
                {{ row.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="athleteName" label="关联运动员" min-width="120">
          <template #default="{ row }">
            <span v-if="row.athleteName">{{ row.athleteName }}</span>
            <span v-else class="text-muted">未关联</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180">
          <template #default="{ row }">
            <span v-if="row.lastLoginTime">{{ formatDateTime(row.lastLoginTime) }}</span>
            <span v-else class="text-muted">从未登录</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click="showEditDialog(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="warning" @click="handleResetPassword(row)">
                <el-icon><Key /></el-icon>
                重置密码
              </el-button>
              <el-dropdown trigger="click" @command="(cmd) => handleMoreAction(cmd, row)">
                <el-button size="small">
                  更多
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="kick" :disabled="row.role === 'ADMIN'">
                      <el-icon><SwitchButton /></el-icon>
                      强制下线
                    </el-dropdown-item>
                    <el-dropdown-item 
                      command="toggle-status"
                      :disabled="row.role === 'ADMIN'"
                    >
                      <el-icon><Switch /></el-icon>
                      {{ row.status === 'active' ? '禁用账号' : '启用账号' }}
                    </el-dropdown-item>
                    <el-dropdown-item 
                      command="delete" 
                      divided 
                      :disabled="row.role === 'ADMIN'"
                    >
                      <el-icon><Delete /></el-icon>
                      <span class="text-danger">删除用户</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="userForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userForm.username" 
            :disabled="isEdit"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input 
            v-model="userForm.password" 
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio value="active">正常</el-radio>
            <el-radio value="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
    >
      <div class="delete-confirm">
        <el-icon class="warning-icon"><WarningFilled /></el-icon>
        <p>确定要删除用户 <strong>{{ currentUser?.username }}</strong> 吗？</p>
        <p class="warning-text">此操作不可恢复，请谨慎操作！</p>
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="deleting" @click="confirmDelete">
          确认删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  User, CircleCheck, Avatar, CircleClose, Search, Refresh, Plus,
  Edit, Key, ArrowDown, SwitchButton, Switch, Delete, WarningFilled
} from '@element-plus/icons-vue';
import {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  resetUserPassword,
  kickUser,
  getDashboardStats
} from '@/api/admin';

// 统计数据
const stats = reactive({
  totalUsers: 0,
  activeUsers: 0,
  adminUsers: 0,
  disabledUsers: 0
});

// 列表数据
const userList = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);

// 筛选条件
const searchKeyword = ref('');
const filterRole = ref('');
const filterStatus = ref('');

// 对话框
const dialogVisible = ref(false);
const deleteDialogVisible = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const deleting = ref(false);
const currentUser = ref(null);
const selectedUsers = ref([]);

// 表单
const formRef = ref(null);
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  role: 'USER',
  status: 'active'
});

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
};

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getDashboardStats();
    if (res.success) {
      // 后端返回 metrics.userStats 对象
      const metrics = res.metrics || {};
      const userStats = metrics.userStats || {};
      stats.totalUsers = userStats.totalUsers || 0;
      stats.activeUsers = userStats.activeUsers || 0;
      stats.adminUsers = userStats.adminUsers || 0;
      stats.disabledUsers = userStats.disabledUsers || 0;
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
  }
};

// 加载用户列表
const loadUserList = async () => {
  loading.value = true;
  try {
    // 后端期望 username 而不是 keyword，status 需要是数字
    const statusMap = { 'active': 1, 'disabled': 0 };
    const res = await getUserList({
      page: currentPage.value,
      size: pageSize.value,
      username: searchKeyword.value || undefined,
      role: filterRole.value || undefined,
      status: filterStatus.value ? statusMap[filterStatus.value] : undefined
    });
    if (res.success) {
      // 后端返回 items 和 total 在根级别
      userList.value = (res.items || []).map(u => ({
        ...u,
        status: u.status === 1 ? 'active' : 'disabled'
      }));
      total.value = res.total || userList.value.length;
    } else {
      ElMessage.error(res.message || '加载用户列表失败');
    }
  } catch (error) {
    console.error('加载用户列表失败:', error);
    ElMessage.error('加载用户列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  loadUserList();
};

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = '';
  filterRole.value = '';
  filterStatus.value = '';
  currentPage.value = 1;
  loadUserList();
};

// 刷新数据
const refreshData = () => {
  loadStats();
  loadUserList();
};

// 分页
const handleSizeChange = (size) => {
  pageSize.value = size;
  loadUserList();
};

const handlePageChange = (page) => {
  currentPage.value = page;
  loadUserList();
};

// 选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection;
};

// 显示新增对话框
const showCreateDialog = () => {
  isEdit.value = false;
  Object.assign(userForm, {
    id: null,
    username: '',
    password: '',
    role: 'USER',
    status: 'active'
  });
  dialogVisible.value = true;
};

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true;
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    password: '',
    role: row.role,
    status: row.status
  });
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      let res;
      const statusMap = { 'active': 1, 'disabled': 0 };
      if (isEdit.value) {
        res = await updateUser(userForm.id, {
          role: userForm.role,
          status: statusMap[userForm.status]
        });
      } else {
        res = await createUser({
          ...userForm,
          status: statusMap[userForm.status]
        });
      }
      
      if (res.success) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
        dialogVisible.value = false;
        refreshData();
      } else {
        ElMessage.error(res.message || '操作失败');
      }
    } catch (error) {
      console.error('操作失败:', error);
      ElMessage.error('操作失败');
    } finally {
      submitting.value = false;
    }
  });
};

// 重置密码
const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${row.username}" 的密码吗？密码将被重置为默认密码。`,
      '重置密码',
      { type: 'warning' }
    );
    
    const res = await resetUserPassword(row.id);
    if (res.success) {
      ElMessage.success('密码重置成功，新密码为: 123456');
    } else {
      ElMessage.error(res.message || '重置失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error);
      ElMessage.error('重置密码失败');
    }
  }
};

// 更多操作
const handleMoreAction = async (command, row) => {
  switch (command) {
    case 'kick':
      try {
        await ElMessageBox.confirm(
          `确定要强制下线用户 "${row.username}" 吗？`,
          '强制下线',
          { type: 'warning' }
        );
        const res = await kickUser(row.id);
        if (res.success) {
          ElMessage.success('用户已被强制下线');
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('操作失败');
        }
      }
      break;
      
    case 'toggle-status':
      try {
        const newStatus = row.status === 'active' ? 'disabled' : 'active';
        const statusMap = { 'active': 1, 'disabled': 0 };
        const actionText = newStatus === 'active' ? '启用' : '禁用';
        await ElMessageBox.confirm(
          `确定要${actionText}用户 "${row.username}" 吗？`,
          `${actionText}账号`,
          { type: 'warning' }
        );
        const res = await updateUser(row.id, { status: statusMap[newStatus] });
        if (res.success) {
          ElMessage.success(`账号已${actionText}`);
          loadUserList();
        } else {
          ElMessage.error(res.message || '操作失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('操作失败');
        }
      }
      break;
      
    case 'delete':
      currentUser.value = row;
      deleteDialogVisible.value = true;
      break;
  }
};

// 确认删除
const confirmDelete = async () => {
  if (!currentUser.value) return;
  
  deleting.value = true;
  try {
    const res = await deleteUser(currentUser.value.id);
    if (res.success) {
      ElMessage.success('删除成功');
      deleteDialogVisible.value = false;
      refreshData();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (error) {
    console.error('删除失败:', error);
    ElMessage.error('删除失败');
  } finally {
    deleting.value = false;
  }
};

onMounted(() => {
  loadStats();
  loadUserList();
});
</script>

<style scoped>
.admin-users {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stat-icon.active {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
}

.stat-icon.admin {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.stat-icon.disabled {
  background: linear-gradient(135deg, #868f96 0%, #596164 100%);
  color: #fff;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.filter-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: 500;
}

.text-muted {
  color: #909399;
}

.text-danger {
  color: #f56c6c;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.delete-confirm {
  text-align: center;
  padding: 20px;
}

.warning-icon {
  font-size: 48px;
  color: #e6a23c;
  margin-bottom: 16px;
}

.warning-text {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-users {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-right {
    width: 100%;
  }
  
  .header-right .el-button {
    width: 100%;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .filter-bar {
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-left {
    width: 100%;
  }
  
  .filter-left .el-input,
  .filter-left .el-select {
    width: 100% !important;
  }
  
  .filter-right {
    width: 100%;
  }
  
  .filter-right .el-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
  
  .stat-value {
    font-size: 24px;
  }
}
</style>
