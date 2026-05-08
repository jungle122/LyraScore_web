<template>
  <div class="page">
    <div class="header">
      <h2>🏅 我的成就徽章</h2>
      <el-button @click="onEvaluate" :loading="evaluating">手动评估</el-button>
    </div>

    <p class="hint">
      徽章会在练习/上传乐谱时由 Spring <code>@EventListener</code> 自动评估并授予。
    </p>

    <!-- 头部统计：解锁数 / 总数 -->
    <div class="stat-row">
      <span class="stat-num">{{ unlockedCount }} / {{ list.length }}</span>
      <span class="stat-label">已解锁</span>
    </div>

    <div class="grid" v-loading="loading">
      <el-card v-for="b in list" :key="b.id" class="badge" :class="{ locked: !b.unlocked }" shadow="hover">
        <div class="icon">{{ b.iconUrl || '🏅' }}</div>
        <div class="name">{{ b.name }}</div>
        <div class="desc">{{ b.description }}</div>

        <el-tag v-if="b.unlocked" type="warning" size="small" effect="dark">已解锁</el-tag>
        <template v-else>
          <el-progress
            :percentage="progressOf(b)"
            :stroke-width="6"
            :show-text="false"
            color="#d97706"
            class="progress"
          />
          <div class="progress-text">{{ progressLabelOf(b) }}</div>
        </template>

        <div v-if="b.unlocked && b.unlockedAt" class="time">{{ formatTime(b.unlockedAt) }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listBadges, evaluateBadges } from '@/api/badge'
import { getStats } from '@/api/log'
import { listScores } from '@/api/score'

const list = ref([])
const loading = ref(false)
const evaluating = ref(false)
const totalMins = ref(0)
const scoreCount = ref(0)

const unlockedCount = computed(() => list.value.filter(b => b.unlocked).length)

async function load() {
  loading.value = true
  try {
    const [badges, stats, scores] = await Promise.all([listBadges(), getStats(), listScores()])
    list.value = badges
    totalMins.value = stats.totalMins || 0
    scoreCount.value = scores.length || 0
  } finally {
    loading.value = false
  }
}

function progressOf(b) {
  let cur = 0
  if (b.conditionType === 'first_log')     cur = totalMins.value > 0 ? 1 : 0
  if (b.conditionType === 'total_minutes') cur = totalMins.value
  if (b.conditionType === 'score_count')   cur = scoreCount.value
  const pct = Math.min(100, Math.round((cur / b.conditionValue) * 100))
  return pct
}

function progressLabelOf(b) {
  let cur = 0
  let unit = ''
  if (b.conditionType === 'first_log')     { cur = totalMins.value > 0 ? 1 : 0; unit = '次' }
  if (b.conditionType === 'total_minutes') { cur = totalMins.value; unit = '分钟' }
  if (b.conditionType === 'score_count')   { cur = scoreCount.value; unit = '张' }
  return `${cur} / ${b.conditionValue} ${unit}`
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function onEvaluate() {
  evaluating.value = true
  try {
    list.value = await evaluateBadges()
    ElMessage.success('已评估')
  } finally {
    evaluating.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: 32px 24px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.header h2 { margin: 0; font-size: 24px; }
.hint { color: var(--lyra-text-muted); font-size: 13px; margin: 0 0 20px; }
.hint code { background: var(--el-color-primary-light-9); color: var(--el-color-primary-dark-2); padding: 2px 6px; border-radius: 3px; font-size: 12px; }

.stat-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 16px 24px;
  background: var(--lyra-bg-warm);
  border-radius: 10px;
  border: 1px solid var(--lyra-border);
  margin-bottom: 24px;
}
.stat-num { font-size: 32px; font-weight: 700; color: var(--el-color-primary); }
.stat-label { color: var(--lyra-text-muted); }

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.badge {
  text-align: center;
  padding: 8px;
  transition: 0.2s;
  background: #fff;
}
.badge:not(.locked) {
  background: linear-gradient(160deg, #fff 0%, #fff8ed 100%);
  border-color: var(--el-color-primary-light-7) !important;
}
.badge.locked {
  opacity: 0.55;
  filter: grayscale(0.7);
}
.icon { font-size: 60px; margin: 16px 0 8px; }
.name { font-weight: 600; font-size: 16px; color: var(--lyra-text); }
.desc { color: var(--lyra-text-muted); font-size: 13px; margin: 6px 0 12px; min-height: 36px; }
.progress { margin: 4px 12px; }
.progress-text { font-size: 12px; color: var(--lyra-text-muted); margin-top: 6px; }
.time { color: var(--lyra-text-muted); font-size: 12px; margin-top: 6px; }
</style>
