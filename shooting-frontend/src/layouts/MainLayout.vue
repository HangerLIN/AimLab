<template>
  <div class="main-layout">
    <!-- 系统公告 -->
    <div v-if="systemSettings.systemAnnouncement && showAnnouncement" class="system-announcement">
      <div class="announcement-content">
        <svg class="announcement-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
        </svg>
        <span class="announcement-text">{{ systemSettings.systemAnnouncement }}</span>
      </div>
      <button class="announcement-close" @click="closeAnnouncement">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>
    
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <div class="logo">
          <router-link to="/">
            <img v-if="systemSettings.systemLogo" :src="systemSettings.systemLogo" alt="Logo" class="logo-image" />
            <div v-else class="logo-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <circle cx="12" cy="12" r="6"/>
                <circle cx="12" cy="12" r="2"/>
              </svg>
            </div>
            <span class="logo-text">{{ systemSettings.systemName }}</span>
          </router-link>
        </div>
        
        <nav class="nav-links">
          <router-link to="/" class="nav-link" exact-active-class="active">
            <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
            <span>首页</span>
          </router-link>
          <router-link to="/training" class="nav-link" active-class="active">
            <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
            <span>训练</span>
          </router-link>
          <router-link to="/profile" class="nav-link" active-class="active">
            <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <span>我的档案</span>
          </router-link>
          <el-dropdown v-if="isAdmin" trigger="hover" class="admin-dropdown">
            <span class="nav-link admin-nav">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z"/>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1Z"/>
              </svg>
              <span>管理后台</span>
              <svg class="dropdown-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <router-link to="/admin/dashboard" class="dropdown-link">数据统计</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/admin/users" class="dropdown-link">用户管理</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/admin/athletes" class="dropdown-link">运动员管理</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/admin/settings" class="dropdown-link">系统设置</router-link>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="hover" class="analytics-dropdown">
            <span class="nav-link analytics-nav">
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="20" x2="18" y2="10"/>
                <line x1="12" y1="20" x2="12" y2="4"/>
                <line x1="6" y1="20" x2="6" y2="14"/>
              </svg>
              <span>数据分析</span>
              <svg class="dropdown-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <router-link to="/ranking" class="dropdown-link">排行榜</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/statistics" class="dropdown-link">统计报表</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/analytics" class="dropdown-link">数据分析概览</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/analytics/compare" class="dropdown-link">运动员对比</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/analytics/trend" class="dropdown-link">趋势分析</router-link>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>
        
        <div class="user-section">
          <template v-if="userStore.isAuthenticated">
            <div class="user-info">
              <div class="avatar-wrapper">
                <img 
                  v-if="userStore.userInfo.avatar" 
                  :src="userStore.userInfo.avatar" 
                  alt="用户头像" 
                  class="avatar"
                />
                <div v-else class="avatar-placeholder">
                  {{ (userStore.userInfo.name || userStore.userInfo.username || 'U').charAt(0).toUpperCase() }}
                </div>
              </div>
              <span class="username">{{ userStore.userInfo.name || userStore.userInfo.username }}</span>
            </div>
            <router-link to="/messages" class="message-btn" title="消息中心">
              <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
              </svg>
              <span v-if="unreadMessageCount > 0" class="message-badge">{{ unreadMessageCount > 99 ? '99+' : unreadMessageCount }}</span>
            </router-link>
            <router-link to="/settings" class="settings-btn" title="账户设置">
              <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"/>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
              </svg>
            </router-link>
            <button @click="logout" class="logout-btn">
              <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
              <span>退出</span>
            </button>
          </template>
          <template v-else>
            <router-link to="/login" class="login-btn">
              登录
            </router-link>
          </template>
        </div>
        
        <!-- 移动端菜单按钮 -->
        <button class="mobile-menu-btn" @click="toggleMobileMenu">
          <svg v-if="!mobileMenuOpen" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="3" y1="12" x2="21" y2="12"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
      
      <!-- 移动端导航菜单 -->
      <transition name="slide-down">
        <div v-if="mobileMenuOpen" class="mobile-menu">
          <router-link to="/" class="mobile-nav-link" @click="closeMobileMenu">首页</router-link>
          <router-link to="/training" class="mobile-nav-link" @click="closeMobileMenu">训练</router-link>
          <router-link to="/profile" class="mobile-nav-link" @click="closeMobileMenu">我的档案</router-link>
          <div v-if="isAdmin" class="mobile-admin-section">
            <div class="mobile-section-title">管理后台</div>
            <router-link to="/admin/dashboard" class="mobile-nav-link" @click="closeMobileMenu">数据统计</router-link>
            <router-link to="/admin/users" class="mobile-nav-link" @click="closeMobileMenu">用户管理</router-link>
            <router-link to="/admin/athletes" class="mobile-nav-link" @click="closeMobileMenu">运动员管理</router-link>
            <router-link to="/admin/settings" class="mobile-nav-link" @click="closeMobileMenu">系统设置</router-link>
          </div>
          <div class="mobile-analytics-section">
            <div class="mobile-section-title">数据分析</div>
            <router-link to="/ranking" class="mobile-nav-link" @click="closeMobileMenu">排行榜</router-link>
            <router-link to="/statistics" class="mobile-nav-link" @click="closeMobileMenu">统计报表</router-link>
            <router-link to="/analytics" class="mobile-nav-link" @click="closeMobileMenu">数据分析概览</router-link>
            <router-link to="/analytics/compare" class="mobile-nav-link" @click="closeMobileMenu">运动员对比</router-link>
            <router-link to="/analytics/trend" class="mobile-nav-link" @click="closeMobileMenu">趋势分析</router-link>
          </div>
          <router-link to="/settings" class="mobile-nav-link" @click="closeMobileMenu">账户设置</router-link>
          <div class="mobile-menu-divider"></div>
          <button v-if="userStore.isAuthenticated" @click="logout" class="mobile-logout-btn">
            退出登录
          </button>
          <router-link v-else to="/login" class="mobile-login-btn" @click="closeMobileMenu">
            登录
          </router-link>
        </div>
      </transition>
    </header>
    
    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="content-container">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
    
    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-brand">
          <div class="footer-logo">
            <img v-if="systemSettings.systemLogo" :src="systemSettings.systemLogo" alt="Logo" class="footer-logo-image" />
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <circle cx="12" cy="12" r="6"/>
              <circle cx="12" cy="12" r="2"/>
            </svg>
          </div>
          <p class="copyright">&copy; {{ currentYear }} {{ systemSettings.systemName }}</p>
        </div>
        <div class="footer-contact" v-if="systemSettings.contactEmail || systemSettings.contactPhone">
          <div v-if="systemSettings.contactEmail" class="contact-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
            <a :href="'mailto:' + systemSettings.contactEmail">{{ systemSettings.contactEmail }}</a>
          </div>
          <div v-if="systemSettings.contactPhone" class="contact-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
            </svg>
            <a :href="'tel:' + systemSettings.contactPhone">{{ systemSettings.contactPhone }}</a>
          </div>
        </div>
        <div class="footer-links">
          <a href="#" class="footer-link">关于我们</a>
          <a href="#" class="footer-link">联系方式</a>
          <a href="#" class="footer-link">帮助中心</a>
          <a href="#" class="footer-link">隐私政策</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import { computed, ref, onMounted, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { getPublicSettings } from '@/api/admin';
import { getUnreadCount } from '@/api/message';

export default {
  name: 'MainLayout',
  
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const mobileMenuOpen = ref(false);
    
    // 系统设置
    const systemSettings = reactive({
      systemName: '射击训练平台',
      systemLogo: '',
      contactEmail: '',
      contactPhone: '',
      systemAnnouncement: '',
      maintenanceMode: false
    });
    const showAnnouncement = ref(true);
    
    // 未读消息数量
    const unreadMessageCount = ref(0);
    
    const currentYear = computed(() => new Date().getFullYear());
    
    const isAdmin = computed(() => {
      return userStore.userInfo?.role?.toUpperCase() === 'ADMIN';
    });
    
    // 加载系统设置
    const loadSystemSettings = async () => {
      try {
        const res = await getPublicSettings();
        if (res.success && res.data) {
          Object.assign(systemSettings, res.data);
        }
      } catch (error) {
        console.error('加载系统设置失败:', error);
      }
    };
    
    const closeAnnouncement = () => {
      showAnnouncement.value = false;
    };
    
    // 加载未读消息数量
    const loadUnreadCount = async () => {
      if (!userStore.isAuthenticated) return;
      try {
        const res = await getUnreadCount();
        if (res.success) {
          unreadMessageCount.value = res.count || 0;
        }
      } catch (error) {
        console.error('获取未读消息数量失败:', error);
      }
    };
    
    const logout = async () => {
      await userStore.logout();
      mobileMenuOpen.value = false;
      router.push('/login');
    };
    
    const toggleMobileMenu = () => {
      mobileMenuOpen.value = !mobileMenuOpen.value;
    };
    
    const closeMobileMenu = () => {
      mobileMenuOpen.value = false;
    };
    
    onMounted(() => {
      loadSystemSettings();
      loadUnreadCount();
      // 每60秒刷新一次未读消息数量
      setInterval(loadUnreadCount, 60000);
    });
    
    // 监听用户登录状态变化
    watch(() => userStore.isAuthenticated, (isAuth) => {
      if (isAuth) {
        loadUnreadCount();
      } else {
        unreadMessageCount.value = 0;
      }
    });
    
    return {
      userStore,
      currentYear,
      isAdmin,
      logout,
      mobileMenuOpen,
      toggleMobileMenu,
      closeMobileMenu,
      systemSettings,
      showAnnouncement,
      closeAnnouncement,
      unreadMessageCount
    };
  }
};
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f8fafc;
}

/* ========== 系统公告 ========== */
.system-announcement {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: #78350f;
  padding: 10px 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  position: relative;
}

.announcement-content {
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 1200px;
}

.announcement-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.announcement-text {
  font-size: 0.95rem;
  font-weight: 500;
}

.announcement-close {
  position: absolute;
  right: 20px;
  background: rgba(255, 255, 255, 0.3);
  border: none;
  padding: 4px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.announcement-close:hover {
  background: rgba(255, 255, 255, 0.5);
}

.announcement-close svg {
  width: 16px;
  height: 16px;
  color: #78350f;
}

/* ========== 顶部导航栏 ========== */
.header {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.25);
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  height: 72px;
}

/* Logo */
.logo a {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  text-decoration: none;
  transition: transform 0.3s ease;
}

.logo a:hover {
  transform: scale(1.02);
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.logo-icon svg {
  width: 24px;
  height: 24px;
}

.logo-image {
  height: 40px;
  max-width: 120px;
  object-fit: contain;
  border-radius: 8px;
}

.logo-text {
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.5px;
}

/* 导航链接 */
.nav-links {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-weight: 500;
  padding: 10px 18px;
  border-radius: 12px;
  transition: all 0.25s ease;
  position: relative;
}

.nav-icon {
  width: 18px;
  height: 18px;
}

.nav-link:hover {
  color: white;
  background: rgba(255, 255, 255, 0.15);
}

.nav-link.active {
  color: white;
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 数据分析下拉菜单 */
.analytics-dropdown {
  display: flex;
  align-items: center;
}

.analytics-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.dropdown-arrow {
  width: 14px;
  height: 14px;
  margin-left: 2px;
}

.dropdown-link {
  color: #374151;
  text-decoration: none;
  display: block;
  width: 100%;
  padding: 4px 0;
}

.dropdown-link:hover {
  color: #10b981;
}

/* 用户区域 */
.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: border-color 0.3s ease;
}

.avatar-wrapper:hover {
  border-color: rgba(255, 255, 255, 0.6);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 1.1rem;
}

.username {
  color: white;
  font-weight: 500;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 8px 16px;
  border-radius: 10px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-1px);
}

.settings-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  text-decoration: none;
}

.settings-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-1px);
}

.message-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  text-decoration: none;
  position: relative;
}

.message-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-1px);
}

.message-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #ef4444;
  color: white;
  font-size: 10px;
  font-weight: 600;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  border: 2px solid #10b981;
}

.btn-icon {
  width: 16px;
  height: 16px;
}

.login-btn {
  background: white;
  color: #10b981;
  padding: 10px 24px;
  border-radius: 10px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 移动端菜单按钮 */
.mobile-menu-btn {
  display: none;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.25s ease;
}

.mobile-menu-btn svg {
  width: 24px;
  height: 24px;
  color: white;
}

.mobile-menu-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

/* 移动端菜单 */
.mobile-menu {
  display: none;
  flex-direction: column;
  padding: 16px 32px 24px;
  background: rgba(0, 0, 0, 0.1);
}

.mobile-nav-link {
  color: white;
  text-decoration: none;
  padding: 14px 0;
  font-weight: 500;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: padding-left 0.25s ease;
}

.mobile-nav-link:hover {
  padding-left: 8px;
}

.mobile-menu-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.2);
  margin: 8px 0;
}

.mobile-logout-btn,
.mobile-login-btn {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: none;
  padding: 14px;
  border-radius: 10px;
  font-weight: 500;
  cursor: pointer;
  text-align: center;
  text-decoration: none;
  margin-top: 8px;
}

/* 移动端管理后台区域 */
.mobile-admin-section {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.mobile-section-title {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.85rem;
  font-weight: 600;
  padding: 8px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 管理后台下拉菜单 */
.admin-dropdown {
  display: flex;
  align-items: center;
}

.admin-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

/* ========== 主内容区域 ========== */
.main-content {
  flex: 1;
  padding: 32px 0;
}

.content-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
}

/* ========== 页脚 ========== */
.footer {
  background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
  color: #9ca3af;
  padding: 48px 0;
  margin-top: auto;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 16px;
}

.footer-logo {
  width: 36px;
  height: 36px;
  background: rgba(16, 185, 129, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-logo svg {
  width: 20px;
  height: 20px;
  color: #10b981;
}

.footer-logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 6px;
}

.copyright {
  font-size: 0.95rem;
}

.footer-links {
  display: flex;
  gap: 32px;
}

.footer-link {
  color: #9ca3af;
  text-decoration: none;
  font-size: 0.95rem;
  transition: color 0.25s ease;
}

.footer-link:hover {
  color: white;
}

/* 页脚联系方式 */
.footer-contact {
  display: flex;
  gap: 24px;
  align-items: center;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #9ca3af;
}

.contact-item svg {
  width: 16px;
  height: 16px;
}

.contact-item a {
  color: #9ca3af;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.25s ease;
}

.contact-item a:hover {
  color: #10b981;
}

/* ========== 动画 ========== */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ========== 响应式设计 ========== */
@media (max-width: 992px) {
  .nav-links {
    display: none;
  }
  
  .user-section {
    display: none;
  }
  
  .mobile-menu-btn {
    display: block;
  }
  
  .mobile-menu {
    display: flex;
  }
  
  .header-container {
    padding: 0 20px;
  }
  
  .content-container {
    padding: 0 20px;
  }
  
  .footer-container {
    flex-direction: column;
    gap: 24px;
    text-align: center;
  }
  
  .footer-contact {
    flex-direction: column;
    gap: 12px;
  }
  
  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
    gap: 20px;
  }
  
  .system-announcement {
    padding: 10px 16px;
  }
  
  .announcement-text {
    font-size: 0.85rem;
  }
}

@media (max-width: 576px) {
  .header-container {
    height: 64px;
    padding: 0 16px;
  }
  
  .logo-text {
    font-size: 1.1rem;
  }
  
  .logo-icon {
    width: 36px;
    height: 36px;
  }
  
  .main-content {
    padding: 20px 0;
  }
  
  .content-container {
    padding: 0 16px;
  }
  
  .footer {
    padding: 32px 0;
  }
  
  .footer-container {
    padding: 0 16px;
  }
}
</style>
