<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="container">
        <div class="logo">
          <router-link to="/">
            <h1>射击训练平台</h1>
          </router-link>
        </div>
        
        <nav class="nav-links">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/training" class="nav-link">训练</router-link>
        </nav>
        
        <div class="user-actions" v-if="userStore.isAuthenticated">
          <div class="user-info">
            <img 
              v-if="userStore.userInfo.avatar" 
              :src="userStore.userInfo.avatar" 
              alt="用户头像" 
              class="avatar"
            />
            <span class="username">{{ userStore.userInfo.name || userStore.userInfo.username }}</span>
          </div>
          <button @click="logout" class="logout-btn">退出登录</button>
        </div>
      </div>
    </header>
    
    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="container">
        <router-view />
      </div>
    </main>
    
    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <p>&copy; {{ currentYear }} 射击训练平台 - 版权所有</p>
        <div class="footer-links">
          <a href="#" class="footer-link">关于我们</a>
          <a href="#" class="footer-link">联系方式</a>
          <a href="#" class="footer-link">帮助中心</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';

export default {
  name: 'MainLayout',
  
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    
    // 获取当前年份
    const currentYear = computed(() => new Date().getFullYear());
    
    // 退出登录
    const logout = async () => {
      await userStore.logout();
      router.push('/login');
    };
    
    return {
      userStore,
      currentYear,
      logout
    };
  }
};
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部导航栏样式 */
.header {
  background-color: #4CAF50;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
}

.logo a {
  color: white;
  text-decoration: none;
}

.logo h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-links {
  display: flex;
  gap: 20px;
}

.nav-link {
  color: white;
  text-decoration: none;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover,
.nav-link.router-link-active {
  background-color: rgba(255, 255, 255, 0.2);
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid white;
}

.username {
  font-weight: 500;
}

.logout-btn {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

/* 主要内容区域样式 */
.main-content {
  flex: 1;
  padding: 30px 0;
  background-color: #f5f5f5;
}

/* 页脚样式 */
.footer {
  background-color: #333;
  color: #ccc;
  padding: 30px 0;
}

.footer .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer p {
  margin: 0;
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-link {
  color: #ccc;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-link:hover {
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header .container {
    flex-direction: column;
    height: auto;
    padding: 15px;
  }
  
  .nav-links {
    margin: 15px 0;
  }
  
  .user-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .footer .container {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
}
</style> 