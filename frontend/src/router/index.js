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
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return '/login'
  }
  if (to.meta.guestOnly && userStore.isLoggedIn) {
    return '/'
  }
})

export default router
