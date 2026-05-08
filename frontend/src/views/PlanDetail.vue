<template>
  <div class="detail-page" v-loading="loading">
    <div class="header">
      <el-button @click="$router.push('/plans')">← 返回计划列表</el-button>
      <div v-if="data">
        <el-button @click="onToggleStatus">
          {{ data.status === 0 ? '标记为已完成' : '标记为进行中' }}
        </el-button>
      </div>
    </div>

    <div v-if="data" class="content">
      <h1>{{ data.title }}</h1>
      <div class="meta">
        {{ data.startDate }} → {{ data.endDate }}
        <el-tag v-if="data.status === 0" type="primary" class="ml">进行中</el-tag>
        <el-tag v-else type="success" class="ml">已完成</el-tag>
      </div>

      <div class="section">
        <div class="section-header">
          <h3>关联乐谱（{{ data.items?.length || 0 }}）</h3>
          <el-button type="primary" size="small" @click="openAddDialog">+ 添加乐谱</el-button>
        </div>

        <el-empty v-if="!data.items || data.items.length === 0" description="还没添加乐谱" />

        <el-card v-for="it in data.items" :key="it.id" class="item-card" shadow="hover" @click="$router.push(`/scores/${it.scoreId}`)">
          <div class="item-row">
            <img :src="resolveImg(it.scoreImageUrl)" class="thumb" />
            <div class="item-meta">
              <div class="item-title">{{ it.scoreTitle }}</div>
              <div class="item-artist">{{ it.scoreArtist || '—' }}</div>
            </div>
            <div class="item-actions">
              <el-button size="small" type="success" @click.stop="openLogDialog(it)">记录练习</el-button>
              <el-button size="small" type="danger" plain @click.stop="onRemoveItem(it)">移除</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 添加乐谱 modal -->
    <el-dialog v-model="showAdd" title="从我的谱仓选择乐谱" width="500">
      <el-select v-model="selectedScoreId" placeholder="选择乐谱" filterable style="width: 100%">
        <el-option
          v-for="s in availableScores"
          :key="s.id"
          :label="`${s.title} - ${s.artist || '—'}`"
          :value="s.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="onAddItem">添加</el-button>
      </template>
    </el-dialog>

    <!-- 记录练习 modal -->
    <el-dialog v-model="showLog" :title="`记录练习 - ${currentScore?.scoreTitle || ''}`" width="500">
      <el-form :model="logForm" label-width="100px">
        <el-form-item label="时长（分钟）">
          <el-input-number v-model="logForm.durationMins" :min="1" :max="1440" />
        </el-form-item>
        <el-form-item label="本次 BPM">
          <el-input-number v-model="logForm.currentBpm" :min="20" :max="300" />
        </el-form-item>
        <el-form-item label="心得">
          <el-input v-model="logForm.thoughts" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLog = false">取消</el-button>
        <el-button type="primary" :loading="logging" @click="onSubmitLog">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPlan, addPlanItem, removePlanItem, updatePlanStatus } from '@/api/plan'
import { listScores } from '@/api/score'
import { createLog } from '@/api/log'

const route = useRoute()
const router = useRouter()

const data = ref(null)
const loading = ref(false)

const showAdd = ref(false)
const adding = ref(false)
const availableScores = ref([])
const selectedScoreId = ref(null)

const showLog = ref(false)
const logging = ref(false)
const currentScore = ref(null)
const logForm = reactive({ durationMins: 30, currentBpm: null, thoughts: '' })

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
  try {
    data.value = await getPlan(route.params.id)
  } finally {
    loading.value = false
  }
}

async function openAddDialog() {
  if (availableScores.value.length === 0) {
    availableScores.value = await listScores()
  }
  selectedScoreId.value = null
  showAdd.value = true
}

async function onAddItem() {
  if (!selectedScoreId.value) { ElMessage.warning('请选择乐谱'); return }
  adding.value = true
  try {
    await addPlanItem(route.params.id, selectedScoreId.value)
    ElMessage.success('已添加')
    showAdd.value = false
    load()
  } finally {
    adding.value = false
  }
}

async function onRemoveItem(it) {
  try {
    await ElMessageBox.confirm(`从计划移除《${it.scoreTitle}》？`, '确认', { type: 'warning' })
  } catch { return }
  await removePlanItem(route.params.id, it.scoreId)
  ElMessage.success('已移除')
  load()
}

async function onToggleStatus() {
  const next = data.value.status === 0 ? 1 : 0
  await updatePlanStatus(route.params.id, next)
  ElMessage.success(next === 1 ? '已完成' : '已重新开始')
  load()
}

function openLogDialog(it) {
  currentScore.value = it
  logForm.durationMins = 30
  logForm.currentBpm = null
  logForm.thoughts = ''
  showLog.value = true
}

async function onSubmitLog() {
  if (!logForm.durationMins || logForm.durationMins < 1) { ElMessage.warning('时长至少 1 分钟'); return }
  logging.value = true
  try {
    await createLog({
      scoreId: currentScore.value.scoreId,
      durationMins: logForm.durationMins,
      currentBpm: logForm.currentBpm,
      thoughts: logForm.thoughts,
    })
    ElMessage.success('已记录练习')
    showLog.value = false
  } finally {
    logging.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.detail-page { padding: 24px; max-width: 900px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; margin-bottom: 16px; }
.content h1 { margin: 0 0 4px; }
.meta { color: #666; margin-bottom: 24px; }
.ml { margin-left: 8px; }
.section { margin-top: 24px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.item-card { margin-bottom: 8px; cursor: pointer; }
.item-row { display: flex; align-items: center; gap: 16px; }
.thumb { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; flex-shrink: 0; }
.item-meta { flex: 1; }
.item-title { font-weight: 600; }
.item-artist { color: #999; font-size: 13px; margin-top: 2px; }
.item-actions { display: flex; gap: 6px; }
</style>
