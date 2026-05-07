<template>
  <div class="page" v-loading="loading">
    <div class="header">
      <el-button @click="$router.push('/setlists')">← 返回歌单列表</el-button>
    </div>

    <div v-if="data" class="content">
      <h1>{{ data.name }}</h1>
      <p class="desc">{{ data.description || '—' }}</p>

      <div class="section-header">
        <h3>收录乐谱（{{ data.items?.length || 0 }}）</h3>
        <el-button type="primary" size="small" @click="openAdd">+ 添加乐谱</el-button>
      </div>

      <el-empty v-if="!data.items || data.items.length === 0" description="还没添加乐谱" />

      <el-card v-for="it in data.items" :key="it.id" class="item" shadow="never">
        <div class="row">
          <img :src="resolveImg(it.scoreImageUrl)" class="thumb" />
          <div class="meta">
            <div class="title">{{ it.scoreTitle }}</div>
            <div class="artist">{{ it.scoreArtist || '—' }}</div>
          </div>
          <el-button size="small" type="danger" plain @click="onRemove(it)">移除</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="showAdd" title="添加乐谱到歌单" width="500">
      <el-select v-model="selectedId" placeholder="选择乐谱" filterable style="width: 100%">
        <el-option v-for="s in scores" :key="s.id" :label="`${s.title} - ${s.artist || '—'}`" :value="s.id" />
      </el-select>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="onAdd">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSetlist, addSetlistItem, removeSetlistItem } from '@/api/setlist'
import { listScores } from '@/api/score'

const route = useRoute()
const data = ref(null)
const loading = ref(false)
const showAdd = ref(false)
const adding = ref(false)
const scores = ref([])
const selectedId = ref(null)

function resolveImg(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080/api${url}`
}

async function load() {
  loading.value = true
  try { data.value = await getSetlist(route.params.id) } finally { loading.value = false }
}

async function openAdd() {
  if (scores.value.length === 0) scores.value = await listScores()
  selectedId.value = null
  showAdd.value = true
}

async function onAdd() {
  if (!selectedId.value) { ElMessage.warning('请选择乐谱'); return }
  adding.value = true
  try {
    await addSetlistItem(route.params.id, selectedId.value)
    ElMessage.success('已添加')
    showAdd.value = false
    load()
  } finally { adding.value = false }
}

async function onRemove(it) {
  try {
    await ElMessageBox.confirm(`从歌单移除《${it.scoreTitle}》？`, '确认', { type: 'warning' })
  } catch { return }
  await removeSetlistItem(route.params.id, it.scoreId)
  ElMessage.success('已移除')
  load()
}

onMounted(load)
</script>

<style scoped>
.page { padding: 24px; max-width: 900px; margin: 0 auto; }
.header { margin-bottom: 16px; }
.content h1 { margin: 0 0 4px; }
.desc { color: #666; margin-bottom: 24px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin: 16px 0 12px; }
.item { margin-bottom: 8px; }
.row { display: flex; align-items: center; gap: 16px; }
.thumb { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.meta { flex: 1; }
.title { font-weight: 600; }
.artist { color: #999; font-size: 13px; }
</style>
