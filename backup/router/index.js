import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import MainLayout from '@/layouts/MainLayout.vue';

// 定义路由规则
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'training',
        name: 'Training',
        component: () => import('@/views/Training.vue')
      },
      {
        path: 'competition/:id',
        name: 'Competition',
        component: () => import('@/views/Competition.vue'),
        props: true
      },
      {
        path: 'report/:id',
        name: 'Report',
        component: () => import('@/views/Report.vue'),
        props: true
      }
    ]
  },
  {
    // 404 Not Found 路由
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
];

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局前置守卫
router.beforeEach((to, from) => {
  // 检查该路由是否需要登录权限
  if (to.meta.requiresAuth || to.matched.some(record => record.meta.requiresAuth)) {
    // 获取用户状态
    const userStore = useUserStore();
    
    // 如果用户未登录，重定向到登录页
    if (!userStore.token) {
      return { name: 'Login' };
    }
  }
  
  // 允许访问
  return true;
});

export default router; 