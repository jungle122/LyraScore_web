<template>
  <div class="scores-page">
    <div class="header">
      <h2>🎼 我的谱仓</h2>
      <div>
        <el-button @click="$router.push('/')">← 返回首页</el-button>
        <el-button type="primary" @click="$router.push('/scores/upload')">+ 上传乐谱</el-button>
      </div>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="还没有乐谱，去上传一张吧" />

    <div v-loading="loading" class="grid">
      <el-card v-for="item in list" :key="item.id" class="card" shadow="hover" @click="goDetail(item.id)">
        <img :src="resolveImg(item.imageUrl)" class="cover" />
        <div class="meta">
          <div class="title">{{ item.title }}</div>
          <div class="artist">{{ item.artist || '—' }}</div>
          <div class="tags">
            <el-tag v-if="item.tuning" size="small">{{ item.tuning }}</el-tag>
            <el-tag v-if="item.capo" size="small" type="warning">变调夹 {{ item.capo }}</el-tag>
            <el-tag v-if="item.bpm" size="small" type="info">{{ item.bpm }} BPM</el-tag>
            <el-tag v-if="item.isPublic === 1" size="small" type="success">公开</el-tag>
          </div>
        </div>
        <el-button size="small" type="danger" plain class="del-btn" @click.stop="onDelete(item)">
          删除
        </el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listScores, deleteScore } from '@/api/score'

const router = useRouter()
const list = ref([])
const loading = ref(false)

// image_url 是相对路径如 /uploads/scores/xx.jpg；后端在 8080 提供静态资源，但 vite 已代理 /api，没代理 /uploads
// 简单起见：直接拼后端域名
function resolveImg(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  // 后端 context-path 是 /api，所以静态资源真实路径是 /api/uploads/...
  return `http://localhost:8080/api${url}`
}

async function load() {
  loading.value = true
  try {
    list.value = await listScores()
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/scores/${id}`)
}

async function onDelete(item) {
  try {
    await ElMessageBox.confirm(`确定删除《${item.title}》吗？`, '删除确认', { type: 'warning' })
  } catch { return }
  await deleteScore(item.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.scores-page { padding: 24px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.card { cursor: pointer; position: relative; }
.cover { width: 100%; height: 160px; object-fit: cover; border-radius: 4px; }
.meta { padding-top: 8px; }
.title { font-weight: 600; font-size: 15px; }
.artist { color: #999; font-size: 13px; margin-top: 2px; }
.tags { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 4px; }
.del-btn { position: absolute; top: 12px; right: 12px; }
</style>
