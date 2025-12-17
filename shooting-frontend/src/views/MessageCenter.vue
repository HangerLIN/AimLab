<template>
  <div class="message-center">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>消息中心</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>消息中心</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="openSendDialog">
          <el-icon><Edit /></el-icon>
          发送消息
        </el-button>
        <el-button @click="markAllAsRead" :disabled="unreadCount === 0">
          <el-icon><Check /></el-icon>
          全部标记已读
        </el-button>
        <el-button type="danger" @click="deleteSelected" :disabled="selectedIds.length === 0">
          <el-icon><Delete /></el-icon>
          删除选中
        </el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterType" @change="loadMessages">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="APPROVAL">审核通知</el-radio-button>
        <el-radio-button label="SYSTEM">系统通知</el-radio-button>
        <el-radio-button label="MANUAL">管理员消息</el-radio-button>
      </el-radio-group>
      <el-radio-group v-model="filterRead" @change="loadMessages" style="margin-left: 20px;">
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button :label="false">未读</el-radio-button>
        <el-radio-button :label="true">已读</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 消息列表 -->
    <div class="message-list" v-loading="loading">
      <el-empty v-if="messages.length === 0 && !loading" description="暂无消息" />
      
      <div v-else class="message-items">
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="{ unread: !message.isRead }"
          @click="viewMessage(message)"
        >
          <el-checkbox
            v-model="selectedIds"
            :value="message.id"
            @click.stop
          />
          <div class="message-icon">
            <el-icon v-if="message.type === 'APPROVAL'" class="approval"><Bell /></el-icon>
            <el-icon v-else-if="message.type === 'SYSTEM'" class="system"><InfoFilled /></el-icon>
            <el-icon v-else class="manual"><Message /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-title">{{ message.title }}</span>
              <el-tag v-if="!message.isRead" type="danger" size="small">未读</el-tag>
              <el-tag v-if="message.type === 'APPROVAL'" type="warning" size="small">审核</el-tag>
              <el-tag v-else-if="message.type === 'SYSTEM'" type="info" size="small">系统</el-tag>
              <el-tag v-else type="primary" size="small">消息</el-tag>
            </div>
            <div class="message-preview">{{ getPreview(message.content) }}</div>
            <div class="message-meta">
              <span class="message-sender" v-if="message.senderName">来自: {{ message.senderName }}</span>
              <span class="message-time">{{ formatTime(message.createdAt) }}</span>
            </div>
          </div>
          <div class="message-actions">
            <el-button link type="primary" @click.stop="viewMessage(message)">查看</el-button>
            <el-button link type="danger" @click.stop="deleteOne(message.id)">删除</el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="loadMessages"
          @current-change="loadMessages"
        />
      </div>
    </div>

    <!-- 消息详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentMessage?.title"
      width="600px"
      class="message-dialog"
    >
      <div class="message-detail" v-if="currentMessage">
        <div class="detail-meta">
          <el-tag v-if="currentMessage.type === 'APPROVAL'" type="warning">审核通知</el-tag>
          <el-tag v-else-if="currentMessage.type === 'SYSTEM'" type="info">系统通知</el-tag>
          <el-tag v-else type="primary">管理员消息</el-tag>
          <span class="detail-time">{{ formatTime(currentMessage.createdAt) }}</span>
          <span class="detail-sender" v-if="currentMessage.senderName">
            发送者: {{ currentMessage.senderName }}
          </span>
        </div>
        <div class="detail-content">
          <pre>{{ currentMessage.content }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="danger" @click="deleteOne(currentMessage?.id); dialogVisible = false">
          删除消息
        </el-button>
      </template>
    </el-dialog>

    <!-- 发送消息对话框 -->
    <el-dialog
      v-model="sendDialogVisible"
      :title="isAdmin ? '发送消息' : '联系管理员'"
      width="550px"
      class="send-dialog"
    >
      <el-form :model="sendForm" label-width="80px" :rules="sendRules" ref="sendFormRef">
        <!-- 管理员可以选择接收者 -->
        <el-form-item v-if="isAdmin" label="接收者" prop="receiverId">
          <el-select
            v-model="sendForm.receiverId"
            filterable
            remote
            reserve-keyword
            placeholder="搜索用户名"
            :remote-method="searchUsers"
            :loading="searchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.username} (${user.role === 'ADMIN' ? '管理员' : '用户'})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-else label="接收者">
          <el-input value="系统管理员" disabled />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="sendForm.title" placeholder="请输入消息标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="sendForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入消息内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendMessage" :loading="sendLoading">
          发送
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Bell, InfoFilled, Message, Check, Delete, Edit } from '@element-plus/icons-vue';
import { useUserStore } from '@/store/modules/user';
import {
  getMessageList,
  getUnreadCount,
  getMessageDetail,
  markAsRead,
  markAllAsRead as markAllAsReadApi,
  deleteMessage,
  deleteMessages,
  adminSendMessage,
  sendToAdmin,
  getMessageableUsers
} from '@/api/message';

const userStore = useUserStore();
const isAdmin = computed(() => userStore.userInfo?.role?.toUpperCase() === 'ADMIN');

// 状态
const loading = ref(false);
const messages = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const filterType = ref('');
const filterRead = ref(null);
const selectedIds = ref([]);
const unreadCount = ref(0);

// 消息详情
const dialogVisible = ref(false);
const currentMessage = ref(null);

// 发送消息相关
const sendDialogVisible = ref(false);
const sendLoading = ref(false);
const searchLoading = ref(false);
const sendFormRef = ref(null);
const userOptions = ref([]);
const sendForm = ref({
  receiverId: null,
  title: '',
  content: ''
});
const sendRules = {
  receiverId: [{ required: true, message: '请选择接收者', trigger: 'change' }],
  title: [{ required: true, message: '请输入消息标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入消息内容', trigger: 'blur' }]
};

// 加载消息列表
const loadMessages = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (filterType.value) {
      params.type = filterType.value;
    }
    if (filterRead.value !== null) {
      params.isRead = filterRead.value;
    }
    
    const res = await getMessageList(params);
    if (res.success) {
      messages.value = res.data.list || [];
      total.value = res.data.total || 0;
    } else {
      ElMessage.error(res.message || '加载消息失败');
    }
  } catch (error) {
    console.error('加载消息失败:', error);
    ElMessage.error('加载消息失败');
  } finally {
    loading.value = false;
  }
};

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount();
    if (res.success) {
      unreadCount.value = res.count || 0;
    }
  } catch (error) {
    console.error('获取未读数量失败:', error);
  }
};

// 查看消息详情
const viewMessage = async (message) => {
  try {
    const res = await getMessageDetail(message.id);
    if (res.success) {
      currentMessage.value = res.data;
      dialogVisible.value = true;
      
      // 如果消息之前是未读的，更新列表中的状态
      if (!message.isRead) {
        message.isRead = true;
        unreadCount.value = Math.max(0, unreadCount.value - 1);
      }
    } else {
      ElMessage.error(res.message || '获取消息详情失败');
    }
  } catch (error) {
    console.error('获取消息详情失败:', error);
    ElMessage.error('获取消息详情失败');
  }
};

// 标记所有为已读
const markAllAsRead = async () => {
  try {
    const res = await markAllAsReadApi();
    if (res.success) {
      ElMessage.success(res.message || '已全部标记为已读');
      loadMessages();
      loadUnreadCount();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (error) {
    console.error('标记已读失败:', error);
    ElMessage.error('操作失败');
  }
};

// 删除单条消息
const deleteOne = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '确认删除', {
      type: 'warning'
    });
    
    const res = await deleteMessage(id);
    if (res.success) {
      ElMessage.success('删除成功');
      loadMessages();
      loadUnreadCount();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

// 批量删除
const deleteSelected = async () => {
  if (selectedIds.value.length === 0) return;
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedIds.value.length} 条消息吗？`,
      '确认删除',
      { type: 'warning' }
    );
    
    const res = await deleteMessages(selectedIds.value);
    if (res.success) {
      ElMessage.success(res.message || '删除成功');
      selectedIds.value = [];
      loadMessages();
      loadUnreadCount();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

// 获取消息预览
const getPreview = (content) => {
  if (!content) return '';
  const text = content.replace(/\n/g, ' ');
  return text.length > 80 ? text.substring(0, 80) + '...' : text;
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前';
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 打开发送消息对话框
const openSendDialog = async () => {
  sendForm.value = { receiverId: null, title: '', content: '' };
  sendDialogVisible.value = true;
  
  // 如果是管理员，加载用户列表
  if (isAdmin.value) {
    await searchUsers('');
  }
};

// 搜索用户（管理员使用）
const searchUsers = async (keyword) => {
  searchLoading.value = true;
  try {
    const res = await getMessageableUsers(keyword);
    if (res.success) {
      userOptions.value = res.data || [];
    }
  } catch (error) {
    console.error('搜索用户失败:', error);
  } finally {
    searchLoading.value = false;
  }
};

// 发送消息
const sendMessage = async () => {
  if (!sendFormRef.value) return;
  
  // 非管理员不需要验证receiverId
  if (!isAdmin.value) {
    if (!sendForm.value.title || !sendForm.value.content) {
      ElMessage.warning('请填写完整信息');
      return;
    }
  } else {
    try {
      await sendFormRef.value.validate();
    } catch {
      return;
    }
  }
  
  sendLoading.value = true;
  try {
    let res;
    if (isAdmin.value) {
      // 管理员发送给指定用户
      res = await adminSendMessage({
        receiverId: sendForm.value.receiverId,
        title: sendForm.value.title,
        content: sendForm.value.content
      });
    } else {
      // 普通用户发送给管理员
      res = await sendToAdmin({
        title: sendForm.value.title,
        content: sendForm.value.content
      });
    }
    
    if (res.success) {
      ElMessage.success(res.message || '发送成功');
      sendDialogVisible.value = false;
    } else {
      ElMessage.error(res.message || '发送失败');
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error('发送失败');
  } finally {
    sendLoading.value = false;
  }
};

onMounted(() => {
  loadMessages();
  loadUnreadCount();
});
</script>

<style scoped>
.message-center {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-bar {
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.message-list {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  min-height: 400px;
}

.message-items {
  padding: 8px;
}

.message-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.message-item:last-child {
  border-bottom: none;
}

.message-item:hover {
  background: #f5f7fa;
}

.message-item.unread {
  background: #ecf5ff;
}

.message-item.unread:hover {
  background: #d9ecff;
}

.message-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-icon .approval {
  color: #e6a23c;
  background: #fdf6ec;
  padding: 8px;
  border-radius: 50%;
}

.message-icon .system {
  color: #909399;
  background: #f4f4f5;
  padding: 8px;
  border-radius: 50%;
}

.message-icon .manual {
  color: #409eff;
  background: #ecf5ff;
  padding: 8px;
  border-radius: 50%;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.message-title {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.message-preview {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.message-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.pagination {
  padding: 16px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #f0f0f0;
}

/* 消息详情弹窗 */
.message-detail {
  padding: 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.detail-time {
  color: #909399;
  font-size: 13px;
}

.detail-sender {
  color: #606266;
  font-size: 13px;
}

.detail-content {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.detail-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
}

/* 响应式 */
@media (max-width: 768px) {
  .message-center {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-actions {
    flex-wrap: wrap;
  }
  
  .filter-bar {
    overflow-x: auto;
  }
  
  .message-item {
    flex-wrap: wrap;
  }
  
  .message-actions {
    width: 100%;
    justify-content: flex-end;
    margin-top: 8px;
  }
}

/* 发送消息对话框 */
.send-dialog :deep(.el-dialog__body) {
  padding-top: 20px;
}
</style>
