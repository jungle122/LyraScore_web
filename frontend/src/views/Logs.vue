<template>
  <div class="logs-page">
    <div class="header">
      <h2>📊 练习日志 & 统计</h2>
      <el-button type="primary" @click="openLogDialog">+ 记录一次练习</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid" v-loading="statsLoading">
      <el-card class="stat-card">
        <div class="stat-label">累计练习时长</div>
        <div class="stat-value">{{ stats.totalMins || 0 }} <span class="unit">分钟</span></div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-label">本周练习时长</div>
        <div class="stat-value">{{ stats.weekMins || 0 }} <span class="unit">分钟</span></div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-label">练习次数</div>
        <div class="stat-value">{{ logs.length || 0 }} <span class="unit">次</span></div>
      </el-card>
    </div>

    <!-- AI 练习周报 -->
    <el-card class="section ai-report-card" shadow="never">
      <template #header>
        <div class="ai-header">
          <b>🤖 AI 练习周报</b>
          <el-button
            type="primary"
            size="small"
            :loading="reportLoading"
            @click="loadReport"
          >
            {{ report ? '重新生成' : '生成本周总结' }}
          </el-button>
        </div>
      </template>

      <el-empty v-if="!report && !reportLoading" description="点击右上角生成你的专属练习总结" />

      <div v-else-if="report" class="ai-report-body">
        <p class="ai-text">{{ report.text }}</p>
        <div class="ai-meta">
          <el-tag v-if="report.aiPowered" type="success" size="small">AI 生成</el-tag>
          <el-tag v-else type="info" size="small">本地降级</el-tag>
          <span class="ai-time">{{ report.generatedAt }}</span>
        </div>
      </div>
    </el-card>

    <!-- 按乐谱聚合 -->
    <el-card class="section" shadow="never">
      <template #header><b>各曲累计时长（GROUP BY 乐谱）</b></template>
      <el-empty v-if="!stats.byScore || stats.byScore.length === 0" description="暂无数据" />
      <el-table v-else :data="stats.byScore" stripe>
        <el-table-column prop="scoreTitle" label="曲名" />
        <el-table-column prop="scoreArtist" label="歌手" />
        <el-table-column prop="sessions" label="练习次数" width="120" />
        <el-table-column prop="totalMins" label="累计分钟" width="140" />
      </el-table>
    </el-card>

    <!-- 近 7 日 -->
    <el-card class="section" shadow="never">
      <template #header><b>近 7 日练习时长（GROUP BY 日期）</b></template>
      <el-empty v-if="!stats.byDay || stats.byDay.length === 0" description="近 7 天还没有练习" />
      <el-table v-else :data="stats.byDay" stripe>
        <el-table-column prop="day" label="日期" />
        <el-table-column prop="totalMins" label="分钟数" />
      </el-table>
    </el-card>

    <!-- 日志列表 -->
    <el-card class="section" shadow="never">
      <template #header><b>最近练习记录</b></template>
      <el-empty v-if="logs.length === 0" description="还没有记录" />
      <el-table v-else :data="logs" stripe>
        <el-table-column prop="logTime" label="时间" width="180" />
        <el-table-column label="乐谱">
          <template #default="{ row }">{{ row.scoreTitle }}{{ row.scoreArtist ? ` - ${row.scoreArtist}` : '' }}</template>
        </el-table-column>
        <el-table-column prop="durationMins" label="时长（分钟）" width="120" />
        <el-table-column prop="currentBpm" label="BPM" width="80" />
        <el-table-column prop="thoughts" label="心得" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" type="danger" link @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 记录练习 modal -->
    <el-dialog v-model="showLog" title="记录一次练习" width="500">
      <el-form :model="logForm" label-width="100px">
        <el-form-item label="选择乐谱">
          <el-select v-model="logForm.scoreId" placeholder="选择乐谱" filterable style="width: 100%">
            <el-option v-for="s in scoresList" :key="s.id" :label="`${s.title} - ${s.artist || '—'}`" :value="s.id" />
          </el-select>
        </el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { listLogs, createLog, deleteLog, getStats, getReport } from '@/api/log'
import { listScores } from '@/api/score'

const stats = ref({})
const logs = ref([])
const statsLoading = ref(false)

const report = ref(null)         // { text, aiPowered, generatedAt }
const reportLoading = ref(false)

const showLog = ref(false)
const logging = ref(false)
const scoresList = ref([])
const logForm = reactive({ scoreId: null, durationMins: 30, currentBpm: null, thoughts: '' })

async function loadReport() {
  reportLoading.value = true
  try {
    const data = await getReport()
    report.value = {
      text: data.report,
      aiPowered: data.aiPowered,
      generatedAt: new Date().toLocaleString('zh-CN'),
    }
  } catch (e) {
    ElMessage.error('生成失败，请稍后再试')
  } finally {
    reportLoading.value = false
  }
}

async function loadAll() {
  statsLoading.value = true
  try {
    const [s, l] = await Promise.all([getStats(), listLogs(50)])
    stats.value = s
    logs.value = l
  } finally {
    statsLoading.value = false
  }
}

async function openLogDialog() {
  if (scoresList.value.length === 0) {
    scoresList.value = await listScores()
  }
  logForm.scoreId = null
  logForm.durationMins = 30
  logForm.currentBpm = null
  logForm.thoughts = ''
  showLog.value = true
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除这条练习日志吗？（${row.scoreTitle} - ${row.durationMins} 分钟）`,
      '删除确认',
      { type: 'warning' }
    )
  } catch { return }
  await deleteLog(row.id)
  ElMessage.success('已删除')
  loadAll()
}

async function onSubmitLog() {
  if (!logForm.scoreId) { ElMessage.warning('请选择乐谱'); return }
  if (!logForm.durationMins || logForm.durationMins < 1) { ElMessage.warning('时长至少 1 分钟'); return }
  logging.value = true
  try {
    await createLog({ ...logForm })
    ElMessage.success('已记录')
    showLog.value = false
    loadAll()
  } finally {
    logging.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.logs-page { padding: 24px; max-width: 1100px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; margin-bottom: 20px; }
.stat-card { text-align: center; padding: 4px; }
.stat-label { color: #999; font-size: 13px; }
.stat-value { font-size: 32px; font-weight: 600; color: var(--el-color-primary); margin-top: 6px; }
.unit { font-size: 14px; color: #999; font-weight: normal; margin-left: 4px; }
.section { margin-bottom: 20px; }

.ai-report-card { background: linear-gradient(135deg, #fffbf0 0%, #fef6e0 100%); }
.ai-header { display: flex; justify-content: space-between; align-items: center; }
.ai-report-body { padding: 4px 0; }
.ai-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--lyra-text);
  margin: 0 0 12px;
  white-space: pre-wrap;
}
.ai-meta { display: flex; align-items: center; gap: 8px; }
.ai-time { color: var(--lyra-text-dim); font-size: 12px; }
</style>
