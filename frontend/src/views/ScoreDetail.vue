<template>
  <div class="detail-page" v-loading="loading">
    <div class="header">
      <el-button @click="$router.push('/scores')">← 返回谱仓</el-button>
      <div v-if="data">
        <el-button type="primary" plain @click="openEdit">编辑</el-button>
        <el-button type="danger" plain @click="onDelete">删除</el-button>
      </div>
    </div>

    <div v-if="data" class="content">
      <h1>{{ data.title }}</h1>
      <div class="artist">{{ data.artist || '未注明歌手' }}</div>

      <div class="tags">
        <el-tag v-if="data.instrument">{{ data.instrument }}</el-tag>
        <el-tag v-if="data.style" type="info">{{ data.style }}</el-tag>
        <el-tag v-if="data.tuning">{{ data.tuning }}</el-tag>
        <el-tag v-if="data.capo" type="warning">变调夹 {{ data.capo }}</el-tag>
        <el-tag v-if="data.bpm" type="info">{{ data.bpm }} BPM</el-tag>
        <el-tag v-if="data.isPublic === 1" type="success">公开</el-tag>
      </div>

      <!-- 状态切换 -->
      <div class="status-row">
        <span class="status-label">练习状态：</span>
        <el-radio-group v-model="data.practiceStatus" @change="onStatusChange">
          <el-radio-button :label="0">未开始</el-radio-button>
          <el-radio-button :label="1">正在练</el-radio-button>
          <el-radio-button :label="2">已练完</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 阅读工具条 -->
      <div class="viewer-toolbar" v-if="imageList.length > 0">
        <el-button-group>
          <el-button :type="viewMode === 'single' ? 'primary' : ''" @click="viewMode = 'single'">1 页显示</el-button>
          <el-button :type="viewMode === 'double' ? 'primary' : ''" @click="viewMode = 'double'">2 页显示</el-button>
        </el-button-group>
        <el-button @click="openPreviewAt(0)">🔍 大图预览</el-button>
        <el-button @click="toggleFullscreen">⛶ {{ isFullscreen ? '退出全屏' : '全屏显示' }}</el-button>
      </div>

      <!-- 多图：1页 / 2页 切换 -->
      <div ref="imagesEl" class="images" :class="`mode-${viewMode}`">
        <img
          v-for="(url, i) in imageList"
          :key="i"
          :src="resolveImg(url)"
          class="score-img"
          @click="openPreviewAt(i)"
        />
      </div>

      <!-- 大图预览 lightbox -->
      <el-image-viewer
        v-if="previewVisible"
        :url-list="absoluteImageList"
        :initial-index="previewIndex"
        @close="previewVisible = false"
        teleported
      />

      <div v-if="data.memo" class="memo">
        <h3>备忘录</h3>
        <pre>{{ data.memo }}</pre>
      </div>
    </div>

    <!-- 编辑 modal -->
    <el-dialog v-model="showEdit" title="编辑乐谱信息" width="540">
      <el-form :model="form" label-width="100px">
        <el-form-item label="歌曲名">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="原唱/歌手">
          <el-input v-model="form.artist" maxlength="100" />
        </el-form-item>
        <el-form-item label="乐器">
          <el-radio-group v-model="form.instrument">
            <el-radio-button label="吉他" />
            <el-radio-button label="尤克里里" />
            <el-radio-button label="其他" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="风格">
          <el-radio-group v-model="form.style">
            <el-radio-button label="弹唱" />
            <el-radio-button label="指弹" />
            <el-radio-button label="其他" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调式">
          <el-input v-model="form.tuning" maxlength="50" />
        </el-form-item>
        <el-form-item label="变调夹">
          <el-input-number v-model="form.capo" :min="0" :max="12" />
        </el-form-item>
        <el-form-item label="速度 BPM">
          <el-input-number v-model="form.bpm" :min="20" :max="300" />
        </el-form-item>
        <el-form-item label="是否公开">
          <el-switch v-model="form.isPublic" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备忘录">
          <el-input v-model="form.memo" type="textarea" :rows="3" />
        </el-form-item>
        <p class="dialog-tip">📝 当前编辑只覆盖元数据，<b>不修改图片</b>。如需换图请删除后重新上传。</p>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElImageViewer } from 'element-plus'
import { getScore, deleteScore, updateScoreStatus, updateScore } from '@/api/score'

const route = useRoute()
const router = useRouter()
const data = ref(null)
const loading = ref(false)

const showEdit = ref(false)
const saving = ref(false)
const form = reactive({
  title: '', artist: '', instrument: '', style: '',
  tuning: '', capo: null, bpm: null, isPublic: 0, memo: '',
})

const imageList = computed(() =>
  (data.value?.imageUrl || '').split(',').map(s => s.trim()).filter(Boolean)
)
const absoluteImageList = computed(() => imageList.value.map(resolveImg))

// 阅读模式
const viewMode = ref('single')          // 'single' | 'double'
const previewVisible = ref(false)
const previewIndex = ref(0)
const isFullscreen = ref(false)
const imagesEl = ref(null)

function openPreviewAt(i) {
  previewIndex.value = i
  previewVisible.value = true
}

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    imagesEl.value?.requestFullscreen?.()
  } else {
    document.exitFullscreen?.()
  }
}

function onFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
}

function resolveImg(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
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

async function onStatusChange(newStatus) {
  try {
    await updateScoreStatus(data.value.id, newStatus)
    ElMessage.success('已更新状态')
  } catch (e) {
    load()
  }
}

function openEdit() {
  Object.assign(form, {
    title: data.value.title,
    artist: data.value.artist || '',
    instrument: data.value.instrument || '',
    style: data.value.style || '',
    tuning: data.value.tuning || '',
    capo: data.value.capo,
    bpm: data.value.bpm,
    isPublic: data.value.isPublic || 0,
    memo: data.value.memo || '',
  })
  showEdit.value = true
}

async function onSave() {
  if (!form.title) { ElMessage.warning('歌曲名不能为空'); return }
  saving.value = true
  try {
    await updateScore(data.value.id, { ...form })
    ElMessage.success('已保存')
    showEdit.value = false
    load()
  } finally {
    saving.value = false
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

onMounted(() => {
  load()
  document.addEventListener('fullscreenchange', onFullscreenChange)
})
onUnmounted(() => {
  document.removeEventListener('fullscreenchange', onFullscreenChange)
})
</script>

<style scoped>
.detail-page { padding: 24px; max-width: 900px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; margin-bottom: 16px; }
.content h1 { margin: 0 0 4px; }
.artist { color: #999; margin-bottom: 12px; }
.tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 16px; }
.status-row {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--el-color-primary-light-9);
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.status-label { color: var(--lyra-text); font-weight: 500; }
.viewer-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px 14px;
  background: var(--el-color-primary-light-9);
  border-radius: 8px;
  flex-wrap: wrap;
}

.images { display: grid; gap: 12px; }
.images.mode-single { grid-template-columns: 1fr; }
.images.mode-double { grid-template-columns: 1fr 1fr; }

.score-img {
  width: 100%;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: zoom-in;
  background: #fff;
}

/* 全屏模式 */
.images:fullscreen {
  background: #1f2937;
  padding: 24px;
  overflow-y: auto;
}
.images:fullscreen .score-img {
  background: #fff;
}
.memo { margin-top: 24px; }
.memo h3 { margin-bottom: 8px; }
.memo pre { background: #f5f7fa; padding: 12px; border-radius: 4px; white-space: pre-wrap; font-family: inherit; }
.dialog-tip { color: var(--lyra-text-muted); font-size: 13px; margin: 8px 0 0; padding: 8px 12px; background: var(--el-color-primary-light-9); border-radius: 6px; }
</style>
