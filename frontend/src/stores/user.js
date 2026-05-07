// Pinia user store —— 全局用户态的唯一数据源
//
// 设计要点（看懂这里再动手填 actions）：
// 1) token 初值从 localStorage 读 → 解决「刷新页面后内存丢失」问题
// 2) userInfo 初值是 null → 刷新后需要靠 fetchMe() 重新拉
// 3) 登录/登出时，token 要「双写」：store + localStorage
//    userInfo 不进 localStorage（避免 JSON.stringify 麻烦，且 /me 接口能随时拿到）

import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/auth'
import service from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,   // 登录/刷新后填充：{ userId, username, role }
  }),

  getters: {
    // 模板里写 userStore.isLoggedIn 比 !!userStore.token 更语义化
    isLoggedIn: (state) => !!state.token,
    isAdmin:    (state) => state.userInfo?.role === 'admin',
  },

  actions: {
    // ─────────────────────────────────────────────────────────────
    // TODO 1: login(form)
    //   入参：form = { username, password }
    //   步骤：
    //     a) const data = await loginApi(form)
    //        响应拦截器已剥壳，data === { token, userId, username, role }
    //     b) this.token = data.token
    //     c) this.userInfo = { userId, username, role }
    //        （不要把整个 data 塞进去——token 已经单独存了，避免冗余）
    //     d) localStorage.setItem('token', data.token)   ← 持久化备份
    //   注意：不要在这里 try/catch；让错误冒到 Login.vue 里好让按钮 loading 收掉
    // ─────────────────────────────────────────────────────────────
    async login(form) {
      // 你来填
      const data = await loginApi(form)
      this.token = data.token
      const { userId, username, role } = data
      this.userInfo = { userId, username, role }
      localStorage.setItem('token', data.token)
    },

    // ─────────────────────────────────────────────────────────────
    // TODO 2: fetchMe()
    //   场景：刷新页面后 token 还在但 userInfo 是 null，需要重新拉一次
    //   步骤：
    //     a) const data = await service.get('/user/me')
    //        拦截器剥壳后 data === { userId, username, role }
    //     b) this.userInfo = data
    //   注意：不需要传参；token 由 request.js 拦截器自动从 store 拿（7D-2 改）
    // ─────────────────────────────────────────────────────────────
    async fetchMe() {
      // 你来填
      const data = await service.get('/user/me')
      this.userInfo = data
    },

    // ─────────────────────────────────────────────────────────────
    // TODO 3: logout()
    //   不是 async，没网络请求，纯本地清理
    //   步骤：
    //     a) this.token = ''
    //     b) this.userInfo = null
    //     c) localStorage.removeItem('token')
    //   注意：不在这里跳路由——跳路由是「调用方」的事（Home.vue 或路由守卫）
    //         store 只管数据，UI 流程不归它管（关注点分离）
    // ─────────────────────────────────────────────────────────────
    logout() {
      // 你来填
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    },
  },
})
