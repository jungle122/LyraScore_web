<template>
  <!-- 未登录时（登录/注册页）：直接渲染，不要外壳 -->
  <router-view v-if="!userStore.isLoggedIn" />

  <!-- 已登录：套一层带顶部导航的 Layout -->
  <div v-else class="app-shell">
    <header class="topbar">
      <div class="topbar-inner">
        <router-link to="/" class="brand">
          <span class="brand-emoji">🎼</span>
          <span class="brand-name">LyraScore</span>
        </router-link>

        <nav class="nav">
          <router-link to="/scores"        class="nav-item">谱仓</router-link>
          <router-link to="/plans"         class="nav-item">计划</router-link>
          <router-link to="/logs"          class="nav-item">日志</router-link>
          <router-link to="/setlists"      class="nav-item">歌单</router-link>
          <router-link to="/friends"       class="nav-item">社交</router-link>
          <router-link to="/song-requests" class="nav-item">点歌</router-link>
          <router-link to="/badges"        class="nav-item">徽章</router-link>
        </nav>

        <el-dropdown @command="onUserCmd">
          <span class="user">
            <span class="avatar">{{ avatarLetter }}</span>
            {{ userStore.userInfo?.username || '...' }}
            <el-icon class="caret"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="home">回到首页</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()

const avatarLetter = computed(() =>
  (userStore.userInfo?.username || '?').charAt(0).toUpperCase()
)

// 已登录但 userInfo 还为空（刷新场景）：拉一次 /me
function ensureUserInfo() {
  if (userStore.isLoggedIn && !userStore.userInfo) {
    userStore.fetchMe().catch(() => {})
  }
}
onMounted(ensureUserInfo)
watch(() => userStore.isLoggedIn, ensureUserInfo)

function onUserCmd(cmd) {
  if (cmd === 'home') router.push('/')
  if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出')
    router.push('/login')
  }
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--lyra-bg-soft);
}
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 251, 245, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--lyra-border);
}
.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 32px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: var(--lyra-text);
  font-weight: 700;
  font-size: 18px;
  letter-spacing: 0.5px;
  font-family: "Georgia", "Songti SC", serif;
}
.brand-emoji { font-size: 22px; }
.nav {
  flex: 1;
  display: flex;
  gap: 4px;
}
.nav-item {
  text-decoration: none;
  color: var(--lyra-text-muted);
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.15s;
}
.nav-item:hover {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.nav-item.router-link-active {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  font-weight: 600;
}
.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: var(--lyra-text);
  outline: none;
}
.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--el-color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
}
.caret { font-size: 12px; color: var(--lyra-text-muted); }
</style>
