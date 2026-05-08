<template>
  <div class="scores-page">
    <div class="header">
      <h2>🎼 我的谱仓</h2>
      <el-button type="primary" @click="$router.push('/scores/upload')">+ 上传乐谱</el-button>
    </div>

    <!-- 搜索 + 筛选条 -->
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="🔍 搜索曲名或歌手"
        clearable
        class="search"
      />

      <div class="chip-group">
        <span class="chip-label">乐器：</span>
        <el-tag
          v-for="opt in instrumentOpts"
          :key="opt"
          :type="filter.instrument === opt ? 'warning' : 'info'"
          :effect="filter.instrument === opt ? 'dark' : 'plain'"
          class="chip"
          @click="filter.instrument = opt"
        >{{ opt }}</el-tag>
      </div>

      <div class="chip-group">
        <span class="chip-label">风格：</span>
        <el-tag
          v-for="opt in styleOpts"
          :key="opt"
          :type="filter.style === opt ? 'warning' : 'info'"
          :effect="filter.style === opt ? 'dark' : 'plain'"
          class="chip"
          @click="filter.style = opt"
        >{{ opt }}</el-tag>
      </div>

      <div class="chip-group">
        <span class="chip-label">状态：</span>
        <el-tag
          v-for="opt in statusOpts"
          :key="opt.label"
          :type="filter.status === opt.value ? 'warning' : 'info'"
          :effect="filter.status === opt.value ? 'dark' : 'plain'"
          class="chip"
          @click="filter.status = opt.value"
        >{{ opt.label }}</el-tag>
      </div>

      <div class="chip-group">
        <span class="chip-label">排序：</span>
        <el-tag
          v-for="opt in sortOpts"
          :key="opt.value"
          :type="sort === opt.value ? 'warning' : 'info'"
          :effect="sort === opt.value ? 'dark' : 'plain'"
          class="chip"
          @click="sort = opt.value"
        >{{ opt.label }}</el-tag>
      </div>
    </div>

    <p class="result-hint">共 {{ filtered.length }} 张乐谱</p>

    <el-empty v-if="!loading && filtered.length === 0" description="没有匹配的乐谱" />

    <div v-loading="loading" class="grid">
      <el-card v-for="item in filtered" :key="item.id" class="card" shadow="hover" @click="goDetail(item.id)">
        <div class="status-corner" :class="`s-${item.practiceStatus || 0}`">
          {{ statusText(item.practiceStatus) }}
        </div>
        <img :src="resolveImg(coverOf(item))" class="cover" />
        <div class="meta">
          <div class="title">{{ item.title }}</div>
          <div class="artist">{{ item.artist || '—' }}</div>
          <div class="tags">
            <el-tag v-if="item.instrument" size="small">{{ item.instrument }}</el-tag>
            <el-tag v-if="item.style" size="small" type="info">{{ item.style }}</el-tag>
            <el-tag v-if="imageCount(item) > 1" size="small" type="success">{{ imageCount(item) }} 图</el-tag>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listScores, deleteScore } from '@/api/score'

const router = useRouter()
const list = ref([])
const loading = ref(false)

const keyword = ref('')
const filter = reactive({ instrument: '全部', style: '全部', status: -1 })
const sort = ref('newest')

const instrumentOpts = ['全部', '吉他', '尤克里里', '其他']
const styleOpts = ['全部', '弹唱', '指弹', '其他']
const statusOpts = [
  { label: '全部',   value: -1 },
  { label: '未开始', value: 0  },
  { label: '正在练', value: 1  },
  { label: '已练完', value: 2  },
]
const sortOpts = [
  { label: '最新存',     value: 'newest' },
  { label: '最早存',     value: 'oldest' },
  { label: '按名 A-Z',   value: 'title'  },
]

function resolveImg(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080/api${url}`
}

function coverOf(item) {
  return (item.imageUrl || '').split(',')[0]?.trim() || ''
}
function imageCount(item) {
  return (item.imageUrl || '').split(',').filter(Boolean).length
}

function statusText(s) {
  if (s === 1) return '正在练'
  if (s === 2) return '已练完'
  return '未开始'
}

const filtered = computed(() => {
  let arr = [...list.value]
  if (filter.instrument !== '全部') arr = arr.filter(s => s.instrument === filter.instrument)
  if (filter.style !== '全部')      arr = arr.filter(s => s.style === filter.style)
  if (filter.status !== -1)         arr = arr.filter(s => (s.practiceStatus || 0) === filter.status)
  const kw = keyword.value.trim().toLowerCase()
  if (kw) {
    arr = arr.filter(s =>
      (s.title || '').toLowerCase().includes(kw) ||
      (s.artist || '').toLowerCase().includes(kw)
    )
  }
  if (sort.value === 'newest') arr.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
  if (sort.value === 'oldest') arr.sort((a, b) => (a.createdAt || '').localeCompare(b.createdAt || ''))
  if (sort.value === 'title')  arr.sort((a, b) => (a.title || '').localeCompare(b.title || '', 'zh'))
  return arr
})

async function load() {
  loading.value = true
  try { list.value = await listScores() } finally { loading.value = false }
}

function goDetail(id) { router.push(`/scores/${id}`) }

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
.scores-page { padding: 24px; max-width: 1200px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }

.toolbar {
  background: #fff;
  border: 1px solid var(--lyra-border);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.search { max-width: 100%; }
.chip-group { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; }
.chip-label { color: var(--lyra-text-muted); font-size: 13px; min-width: 48px; }
.chip {
  cursor: pointer;
  user-select: none;
  transition: 0.15s;
}

.result-hint { color: var(--lyra-text-muted); font-size: 13px; margin: 8px 0 16px; }

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.card {
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.status-corner {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: 600;
  color: #fff;
}
.status-corner.s-0 { background: #909399; }
.status-corner.s-1 { background: #d97706; }
.status-corner.s-2 { background: #67c23a; }

.cover { width: 100%; height: 160px; object-fit: cover; border-radius: 4px; }
.meta { padding-top: 8px; }
.title { font-weight: 600; font-size: 15px; }
.artist { color: #999; font-size: 13px; margin-top: 2px; }
.tags { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 4px; }
.del-btn { position: absolute; top: 12px; right: 12px; }
</style>
