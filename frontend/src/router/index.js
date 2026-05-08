import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      component: () => import('@/views/Login.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/register',
      component: () => import('@/views/Register.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/scores',
      component: () => import('@/views/Scores.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/scores/upload',
      component: () => import('@/views/ScoreUpload.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/scores/:id',
      component: () => import('@/views/ScoreDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/plans',
      component: () => import('@/views/Plans.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/plans/:id',
      component: () => import('@/views/PlanDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/logs',
      component: () => import('@/views/Logs.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/setlists',
      component: () => import('@/views/Setlists.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/setlists/:id',
      component: () => import('@/views/SetlistDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/friends',
      component: () => import('@/views/Friends.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/song-requests',
      component: () => import('@/views/SongRequests.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/badges',
      component: () => import('@/views/Badges.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/tuner',
      component: () => import('@/views/Tuner.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/metronome',
      component: () => import('@/views/Metronome.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/find-tab',
      component: () => import('@/views/FindTab.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/users',
      component: () => import('@/views/admin/AdminUsers.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/dictionary',
      component: () => import('@/views/admin/AdminDictionary.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return '/login'
  }
  if (to.meta.guestOnly && userStore.isLoggedIn) {
    return userStore.userInfo?.role === 'admin' ? '/admin' : '/'
  }
  // 管理员路由：仅 role=admin 可访问；普通用户即使知道 URL 直访也会被踢回首页
  if (to.meta.requiresAdmin && userStore.userInfo?.role !== 'admin') {
    return '/'
  }
  // 管理员访问普通首页 → 重定向到数据大盘
  if (to.path === '/' && userStore.userInfo?.role === 'admin') {
    return '/admin'
  }
})

export default router
