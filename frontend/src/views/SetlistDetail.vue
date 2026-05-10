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

      <el-card v-for="it in data.items" :key="it.id" class="item" shadow="hover" @click="$router.push(`/scores/${it.scoreId}`)">
        <div class="row">
          <img :src="resolveImg(it.scoreImageUrl)" class="thumb" />
          <div class="meta">
            <div class="title">{{ it.scoreTitle }}</div>
            <div class="artist">{{ it.scoreArtist || '—' }}</div>
          </div>
          <el-button size="small" type="danger" plain @click.stop="onRemove(it)">移除</el-button>
        </div>
      </el-card>
    </div>

    <ScorePickerDialog
      v-model="showAdd"
      :exclude-ids="existingScoreIds"
      :submitting="adding"
      title="添加乐谱到歌单（可多选）"
      confirm-text="收录到歌单"
      @confirm="onAddItems"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSetlist, addSetlistItem, removeSetlistItem } from '@/api/setlist'
import ScorePickerDialog from '@/components/ScorePickerDialog.vue'

const route = useRoute()
const data = ref(null)
const loading = ref(false)
const showAdd = ref(false)
const adding = ref(false)

// 当前歌单已收录的 scoreId 列表，传给选择器用于排除
const existingScoreIds = computed(() =>
  (data.value?.items || []).map(it => it.scoreId)
)

function resolveImg(url) {
  if (!url) return ''
  // 多图字段：取第一张作为封面
  const first = url.split(',')[0]?.trim() || ''
  if (!first) return ''
  if (first.startsWith('http')) return first
  return `http://localhost:8080/api${first}`
}

async function load() {
  loading.value = true
  try { data.value = await getSetlist(route.params.id) } finally { loading.value = false }
}

function openAdd() {
  showAdd.value = true
}

// 批量收录：并发请求
async function onAddItems(scoreIds) {
  if (!scoreIds || scoreIds.length === 0) return
  adding.value = true
  try {
    await Promise.all(scoreIds.map(id => addSetlistItem(route.params.id, id)))
    ElMessage.success(`已收录 ${scoreIds.length} 首`)
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
.item { margin-bottom: 8px; cursor: pointer; }
.row { display: flex; align-items: center; gap: 16px; }
.thumb { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.meta { flex: 1; }
.title { font-weight: 600; }
.artist { color: #999; font-size: 13px; }
</style>
