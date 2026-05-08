<template>
  <div class="page">
    <div class="header">
      <h2>🏅 我的成就徽章</h2>
      <div>
        <el-button @click="$router.push('/')">← 返回首页</el-button>
        <el-button @click="onEvaluate" :loading="evaluating">手动评估</el-button>
      </div>
    </div>

    <p class="hint">
      徽章会在练习/上传乐谱时由 Spring <code>@EventListener</code> 自动评估并授予。
      若数据先于事件机制写入（比如开发期手动插入），可点「手动评估」补发。
    </p>

    <div class="grid" v-loading="loading">
      <el-card v-for="b in list" :key="b.id" class="badge" :class="{ locked: !b.unlocked }" shadow="hover">
        <div class="icon">{{ b.iconUrl || '🏅' }}</div>
        <div class="name">{{ b.name }}</div>
        <div class="desc">{{ b.description }}</div>
        <el-tag v-if="b.unlocked" type="success" size="small">已解锁</el-tag>
        <el-tag v-else type="info" size="small">未解锁</el-tag>
        <div v-if="b.unlocked && b.unlockedAt" class="time">{{ b.unlockedAt }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listBadges, evaluateBadges } from '@/api/badge'

const list = ref([])
const loading = ref(false)
const evaluating = ref(false)

async function load() {
  loading.value = true
  try { list.value = await listBadges() } finally { loading.value = false }
}

async function onEvaluate() {
  evaluating.value = true
  try {
    list.value = await evaluateBadges()
    ElMessage.success('已评估')
  } finally { evaluating.value = false }
}

onMounted(load)
</script>

<style scoped>
.page { padding: 24px; max-width: 1100px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.hint { color: #909399; font-size: 13px; margin-bottom: 16px; }
.hint code { background: #f4f4f5; padding: 2px 6px; border-radius: 3px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.badge { text-align: center; padding: 8px; transition: 0.2s; }
.badge.locked { opacity: 0.4; filter: grayscale(0.8); }
.icon { font-size: 60px; margin: 16px 0; }
.name { font-weight: 600; font-size: 16px; }
.desc { color: #999; font-size: 13px; margin: 6px 0 12px; min-height: 36px; }
.time { color: #999; font-size: 12px; margin-top: 6px; }
</style>
