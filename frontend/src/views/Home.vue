<template>
  <div class="home">
    <!-- 欢迎条 -->
    <div class="welcome">
      <div>
        <h1>{{ greeting }}，{{ userStore.userInfo?.username || '...' }}</h1>
        <p class="welcome-sub">把每一次练习，存进你的云端谱仓。</p>
      </div>
    </div>

    <!-- 概览统计（dashboard tile） -->
    <div class="stat-row" v-loading="statsLoading">
      <el-card class="stat-card" shadow="never">
        <div class="stat-label">总练习时长</div>
        <div class="stat-value">{{ stats.totalMins || 0 }} <span class="stat-unit">分钟</span></div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-label">本周练习</div>
        <div class="stat-value">{{ stats.weekMins || 0 }} <span class="stat-unit">分钟</span></div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-label">乐谱数</div>
        <div class="stat-value">{{ scoreCount }} <span class="stat-unit">张</span></div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-label">解锁徽章</div>
        <div class="stat-value">{{ badgeCount.unlocked }} <span class="stat-unit">/ {{ badgeCount.total }}</span></div>
      </el-card>
    </div>

    <!-- 模块快捷入口 -->
    <div class="section">
      <h2 class="section-title">核心模块</h2>
      <div class="grid">
        <el-card v-for="m in modules" :key="m.path" class="mod" shadow="hover" @click="$router.push(m.path)">
          <div class="mod-row">
            <el-icon class="mod-icon" :style="{ color: m.color }">
              <component :is="m.icon" />
            </el-icon>
            <div class="mod-text">
              <div class="mod-name">{{ m.name }}</div>
              <div class="mod-desc">{{ m.desc }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 小工具 -->
    <div class="section">
      <h2 class="section-title">小工具</h2>
      <div class="grid">
        <el-card v-for="m in tools" :key="m.path" class="mod" shadow="hover" @click="$router.push(m.path)">
          <div class="mod-row">
            <el-icon class="mod-icon" :style="{ color: m.color }">
              <component :is="m.icon" />
            </el-icon>
            <div class="mod-text">
              <div class="mod-name">{{ m.name }}</div>
              <div class="mod-desc">{{ m.desc }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import {
  Folder, Calendar, TrendCharts, Tickets, User, Microphone, Medal,
  Setting, AlarmClock,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getStats } from '@/api/log'
import { listScores } from '@/api/score'
import { listBadges } from '@/api/badge'

const userStore = useUserStore()

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6)  return '凌晨练琴'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const stats = ref({})
const scoreCount = ref(0)
const badgeCount = ref({ unlocked: 0, total: 0 })
const statsLoading = ref(false)

const modules = [
  { path: '/scores',        name: '我的谱仓',  desc: '上传管理乐谱',     icon: markRaw(Folder),       color: '#d97706' },
  { path: '/plans',         name: '练习计划',  desc: '阶段性练习目标',   icon: markRaw(Calendar),     color: '#722ed1' },
  { path: '/logs',          name: '练习日志',  desc: '记录与统计',       icon: markRaw(TrendCharts),  color: '#13c2c2' },
  { path: '/setlists',      name: '歌单',      desc: '多对多收藏',       icon: markRaw(Tickets),      color: '#fa8c16' },
  { path: '/friends',       name: '社交',      desc: '加好友互关注',     icon: markRaw(User),         color: '#52c41a' },
  { path: '/song-requests', name: 'AI 点歌',   desc: 'AI 清洗自由文本',  icon: markRaw(Microphone),   color: '#eb2f96' },
  { path: '/badges',        name: '成就徽章',  desc: '事件驱动发牌',     icon: markRaw(Medal),        color: '#faad14' },
]
const tools = [
  { path: '/tuner',     name: '吉他定音器', desc: '5 种调弦预设',         icon: markRaw(Setting),     color: '#d97706' },
  { path: '/metronome', name: '节拍器',     desc: '30-250 BPM',           icon: markRaw(AlarmClock),  color: '#13c2c2' },
]

onMounted(async () => {
  statsLoading.value = true
  try {
    const [s, scores, badges] = await Promise.all([
      getStats(), listScores(), listBadges(),
    ])
    stats.value = s
    scoreCount.value = scores.length
    badgeCount.value = {
      unlocked: badges.filter(b => b.unlocked).length,
      total: badges.length,
    }
  } catch (e) {
    // 忽略：可能未登录或后端未起
  } finally {
    statsLoading.value = false
  }
})
</script>

<style scoped>
.home { max-width: 1200px; margin: 0 auto; padding: 24px; }

.welcome {
  background: #fff;
  border: 1px solid var(--lyra-border-soft);
  border-radius: 8px;
  padding: 24px 28px;
  margin-bottom: 16px;
}
.welcome h1 { margin: 0 0 6px; }
.welcome-sub { margin: 0; color: var(--lyra-text-muted); font-size: 13px; }

.stat-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 24px;
}
.stat-card { padding: 4px 0; }
.stat-label { color: var(--lyra-text-muted); font-size: 13px; }
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--lyra-text);
  margin-top: 6px;
  line-height: 1.2;
}
.stat-unit { font-size: 13px; color: var(--lyra-text-dim); font-weight: 400; }

.section { margin-bottom: 24px; }
.section-title {
  font-size: 14px;
  margin: 0 0 12px;
  color: var(--lyra-text);
  font-weight: 600;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}
.mod { cursor: pointer; transition: transform 0.15s; }
.mod:hover { transform: translateY(-1px); }
.mod-row { display: flex; align-items: center; gap: 14px; }
.mod-icon { font-size: 28px; flex-shrink: 0; }
.mod-text { flex: 1; }
.mod-name { font-weight: 600; font-size: 14px; color: var(--lyra-text); }
.mod-desc { color: var(--lyra-text-muted); font-size: 12px; margin-top: 4px; }
</style>
