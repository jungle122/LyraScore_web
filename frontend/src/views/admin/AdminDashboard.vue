<template>
  <div class="page" v-loading="loading">
    <div class="hd">
      <h1>📊 数据大盘</h1>
      <p class="sub">全站聚合统计——基于多张表的 COUNT / SUM 查询。</p>
    </div>

    <div class="grid">
      <el-card class="tile" v-for="t in tiles" :key="t.label" shadow="never">
        <div class="tile-icon">{{ t.icon }}</div>
        <div class="tile-num">{{ t.value }}</div>
        <div class="tile-label">{{ t.label }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAdminDashboard } from '@/api/admin'

const data = ref({})
const loading = ref(false)

const tiles = computed(() => [
  { label: '注册用户',     icon: '👥', value: data.value.userCount         ?? 0 },
  { label: '上传乐谱',     icon: '🎼', value: data.value.scoreCount        ?? 0 },
  { label: '练习计划',     icon: '📅', value: data.value.planCount         ?? 0 },
  { label: '练习日志',     icon: '📝', value: data.value.logCount          ?? 0 },
  { label: '累计练琴(分钟)', icon: '⏱', value: data.value.totalPracticeMinutes ?? 0 },
  { label: '歌单',         icon: '📋', value: data.value.setlistCount      ?? 0 },
  { label: '好友对数',     icon: '🤝', value: data.value.friendshipCount   ?? 0 },
  { label: '点歌请求',     icon: '🎤', value: data.value.songRequestCount  ?? 0 },
  { label: '已颁徽章',     icon: '🏅', value: data.value.badgeAwardedCount ?? 0 },
])

async function load() {
  loading.value = true
  try { data.value = await getAdminDashboard() } finally { loading.value = false }
}

onMounted(load)
</script>

<style scoped>
.page { padding: 24px; max-width: 1200px; margin: 0 auto; }
.hd { margin-bottom: 20px; }
.hd h1 { margin: 0 0 4px; }
.sub { color: var(--lyra-text-muted); margin: 0; font-size: 13px; }
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}
.tile {
  text-align: center;
  background: linear-gradient(135deg, #fff 0%, #fffaf0 100%);
}
.tile-icon { font-size: 28px; margin: 6px 0; }
.tile-num {
  font-size: 30px;
  font-weight: 700;
  color: var(--el-color-primary);
  font-family: "Georgia", serif;
}
.tile-label { color: var(--lyra-text-muted); font-size: 13px; margin-top: 4px; }
</style>
