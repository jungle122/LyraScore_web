<template>
  <!-- 未登录：直接渲染（登录/注册页全屏） -->
  <router-view v-if="!userStore.isLoggedIn" />

  <!-- 已登录：左侧导航 + 顶栏 + 内容区 -->
  <el-container v-else class="app-shell">
    <!-- ───────── 侧边栏 ───────── -->
    <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
      <div class="brand">
        <span class="brand-emoji">🎼</span>
        <span class="brand-name" v-if="!collapsed">LyraScore</span>
      </div>

      <el-menu
        :default-active="$route.path"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        class="side-menu"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <div class="menu-group" v-if="!collapsed">核心模块</div>
        <el-menu-item index="/scores">
          <el-icon><Folder /></el-icon>
          <template #title>谱仓</template>
        </el-menu-item>
        <el-menu-item index="/plans">
          <el-icon><Calendar /></el-icon>
          <template #title>练习计划</template>
        </el-menu-item>
        <el-menu-item index="/logs">
          <el-icon><TrendCharts /></el-icon>
          <template #title>练习日志</template>
        </el-menu-item>
        <el-menu-item index="/setlists">
          <el-icon><Tickets /></el-icon>
          <template #title>歌单</template>
        </el-menu-item>
        <el-menu-item index="/friends">
          <el-icon><User /></el-icon>
          <template #title>社交</template>
        </el-menu-item>
        <el-menu-item index="/song-requests">
          <el-icon><Microphone /></el-icon>
          <template #title>AI 点歌</template>
        </el-menu-item>
        <el-menu-item index="/badges">
          <el-icon><Medal /></el-icon>
          <template #title>成就徽章</template>
        </el-menu-item>

        <div class="menu-group" v-if="!collapsed">小工具</div>
        <el-menu-item index="/tuner">
          <el-icon><Setting /></el-icon>
          <template #title>吉他定音器</template>
        </el-menu-item>
        <el-menu-item index="/metronome">
          <el-icon><AlarmClock /></el-icon>
          <template #title>节拍器</template>
        </el-menu-item>
        <el-menu-item index="/find-tab">
          <el-icon><Search /></el-icon>
          <template #title>找谱搜索</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- ───────── 右侧主容器 ───────── -->
    <el-container>
      <!-- 顶栏 -->
      <el-header class="topbar">
        <el-button text :icon="collapsed ? Expand : Fold" @click="collapsed = !collapsed" />

        <el-breadcrumb separator="/" class="crumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentCrumb">{{ currentCrumb }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="topbar-right">
          <el-dropdown @command="onUserCmd">
            <span class="user">
              <span class="avatar">{{ avatarLetter }}</span>
              <span class="username">{{ userStore.userInfo?.username || '...' }}</span>
              <el-icon class="caret"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">
                  <el-icon><HomeFilled /></el-icon>回到首页
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  HomeFilled, Folder, Calendar, TrendCharts, Tickets, User, Microphone, Medal,
  Setting, AlarmClock, ArrowDown, SwitchButton, Expand, Fold, Search,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const avatarLetter = computed(() =>
  (userStore.userInfo?.username || '?').charAt(0).toUpperCase()
)

const CRUMB_MAP = {
  '/': '首页',
  '/scores': '谱仓',
  '/scores/upload': '上传乐谱',
  '/plans': '练习计划',
  '/logs': '练习日志',
  '/setlists': '歌单',
  '/friends': '社交',
  '/song-requests': 'AI 点歌',
  '/badges': '成就徽章',
  '/tuner': '吉他定音器',
  '/metronome': '节拍器',
  '/find-tab': '找谱搜索',
}
const currentCrumb = computed(() => {
  const p = route.path
  if (p === '/') return ''
  if (CRUMB_MAP[p]) return CRUMB_MAP[p]
  // 动态路径如 /scores/123、/plans/456
  if (p.startsWith('/scores/')) return '乐谱详情'
  if (p.startsWith('/plans/'))  return '计划详情'
  if (p.startsWith('/setlists/')) return '歌单详情'
  return ''
})

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
  height: 100vh;
}

/* ───── 侧边栏 ───── */
.sidebar {
  background: var(--lyra-sidebar-bg);
  border-right: 1px solid var(--lyra-border);
  transition: width 0.18s;
  overflow-x: hidden;
}
.brand {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  border-bottom: 1px solid var(--lyra-border-soft);
  font-weight: 700;
  font-size: 16px;
  color: var(--el-color-primary);
  letter-spacing: 0.5px;
}
.brand-emoji { font-size: 22px; }

.menu-group {
  padding: 12px 24px 6px;
  font-size: 12px;
  color: var(--lyra-text-dim);
  text-transform: uppercase;
  letter-spacing: 0.6px;
}

.side-menu {
  border-right: none !important;
}

/* ───── 顶栏 ───── */
.topbar {
  background: var(--lyra-topbar-bg);
  border-bottom: 1px solid var(--lyra-border);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  height: 60px;
}
.crumb { flex: 1; }
.topbar-right { display: flex; align-items: center; }

.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
  padding: 6px 12px;
  border-radius: 6px;
  transition: 0.15s;
}
.user:hover { background: var(--el-color-primary-light-9); }
.avatar {
  width: 30px; height: 30px;
  border-radius: 50%;
  background: var(--el-color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
}
.username { color: var(--lyra-text); font-size: 14px; }
.caret { font-size: 12px; color: var(--lyra-text-muted); }

/* ───── 主内容区 ───── */
/* 内层页面各自带 padding，main 仅做背景 + 滚动容器 */
.main {
  background: var(--lyra-bg-page);
  padding: 0;
  overflow-y: auto;
}
</style>
