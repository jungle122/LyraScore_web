<template>
  <div class="detail-page" v-loading="loading">
    <div class="header">
      <el-button @click="$router.push('/scores')">← 返回谱仓</el-button>
      <el-button type="danger" plain @click="onDelete" v-if="data">删除</el-button>
    </div>

    <div v-if="data" class="content">
      <h1>{{ data.title }}</h1>
      <div class="artist">{{ data.artist || '未注明歌手' }}</div>

      <div class="tags">
        <el-tag v-if="data.tuning">{{ data.tuning }}</el-tag>
        <el-tag v-if="data.capo" type="warning">变调夹 {{ data.capo }}</el-tag>
        <el-tag v-if="data.bpm" type="info">{{ data.bpm }} BPM</el-tag>
        <el-tag v-if="data.isPublic === 1" type="success">公开</el-tag>
      </div>

      <img :src="resolveImg(data.imageUrl)" class="score-img" />

      <div v-if="data.memo" class="memo">
        <h3>备忘录</h3>
        <pre>{{ data.memo }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScore, deleteScore } from '@/api/score'

const route = useRoute()
const router = useRouter()
const data = ref(null)
const loading = ref(false)

function resolveImg(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  // 后端 context-path 是 /api，所以静态资源真实路径是 /api/uploads/...
  return `http://localhost:8080/api${url}`
}

async function load() {
  loading.value = true
  try {
    data.value = await getScore(route.params.id)
  } finally {
    loading.value = false
  }
}

async function onDelete() {
  try {
    await ElMessageBox.confirm(`确定删除《${data.value.title}》吗？`, '删除确认', { type: 'warning' })
  } catch { return }
  await deleteScore(data.value.id)
  ElMessage.success('已删除')
  router.push('/scores')
}

onMounted(load)
</script>

<style scoped>
.detail-page { padding: 24px; max-width: 900px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; margin-bottom: 16px; }
.content h1 { margin: 0 0 4px; }
.artist { color: #999; margin-bottom: 12px; }
.tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 16px; }
.score-img { width: 100%; border: 1px solid #eee; border-radius: 4px; }
.memo { margin-top: 24px; }
.memo h3 { margin-bottom: 8px; }
.memo pre { background: #f5f7fa; padding: 12px; border-radius: 4px; white-space: pre-wrap; font-family: inherit; }
</style>
