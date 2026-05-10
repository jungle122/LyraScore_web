<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    :title="title"
    width="80%"
    top="5vh"
    @open="onOpen"
    @closed="onClosed"
  >
    <div class="picker">
      <!-- 搜索 + 筛选条 -->
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="🔍 搜索曲名或歌手" clearable class="search" />

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
      </div>

      <p class="result-hint">
        共 <b>{{ filtered.length }}</b> 张可选 · 已选 <b>{{ selected.size }}</b> 首
        <el-button v-if="selected.size > 0" link type="warning" @click="selected.clear()">清空选择</el-button>
      </p>

      <el-empty
        v-if="!loading && filtered.length === 0"
        :description="excludeIds.length > 0 ? '没有更多可添加的乐谱了' : '没有匹配的乐谱'"
      />

      <div v-loading="loading" class="grid">
        <div
          v-for="item in filtered"
          :key="item.id"
          class="card"
          :class="{ selected: selected.has(item.id) }"
          @click="toggle(item.id)"
        >
          <div class="check-corner">
            <el-icon v-if="selected.has(item.id)"><Check /></el-icon>
          </div>
          <img :src="resolveImg(coverOf(item))" class="cover" />
          <div class="meta">
            <div class="title">{{ item.title }}</div>
            <div class="artist">{{ item.artist || '—' }}</div>
            <div class="tags">
              <el-tag v-if="item.instrument" size="small">{{ item.instrument }}</el-tag>
              <el-tag v-if="item.style" size="small" type="info">{{ item.style }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button
        type="primary"
        :disabled="selected.size === 0"
        :loading="loading || submitting"
        @click="onConfirm"
      >
        {{ confirmText }}{{ selected.size > 0 ? ` (${selected.size})` : '' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { listScores } from '@/api/score'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  excludeIds: { type: Array, default: () => [] },
  title: { type: String, default: '选择乐谱' },
  confirmText: { type: String, default: '添加选中' },
  submitting: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue', 'confirm'])

const list = ref([])
const loading = ref(false)

const keyword = ref('')
const filter = reactive({ instrument: '全部', style: '全部', status: -1 })

const instrumentOpts = ['全部', '吉他', '尤克里里', '其他']
const styleOpts = ['全部', '弹唱', '指弹', '其他']
const statusOpts = [
  { label: '全部',   value: -1 },
  { label: '未开始', value: 0  },
  { label: '正在练', value: 1  },
  { label: '已练完', value: 2  },
]

const selected = reactive(new Set())
const excludeSet = computed(() => new Set(props.excludeIds))

const filtered = computed(() => {
  let arr = list.value.filter(s => !excludeSet.value.has(s.id))
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
  return arr.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
})

function resolveImg(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080/api${url}`
}
function coverOf(item) {
  return (item.imageUrl || '').split(',')[0]?.trim() || ''
}

function toggle(id) {
  if (selected.has(id)) selected.delete(id)
  else selected.add(id)
}

async function onOpen() {
  selected.clear()
  keyword.value = ''
  filter.instrument = '全部'
  filter.style = '全部'
  filter.status = -1
  loading.value = true
  try { list.value = await listScores() } finally { loading.value = false }
}

function onClosed() {
  selected.clear()
}

function onConfirm() {
  if (selected.size === 0) return
  emit('confirm', Array.from(selected))
}
</script>

<style scoped>
.picker { max-height: 70vh; overflow-y: auto; }

.toolbar {
  background: var(--lyra-bg-soft, #fafafa);
  border: 1px solid var(--lyra-border, #ebeef5);
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: sticky;
  top: 0;
  z-index: 5;
}
.chip-group { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; }
.chip-label { color: var(--lyra-text-muted, #909399); font-size: 13px; min-width: 48px; }
.chip { cursor: pointer; user-select: none; transition: 0.15s; }

.result-hint { color: var(--lyra-text-muted, #909399); font-size: 13px; margin: 6px 4px 12px; }

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
}

.card {
  position: relative;
  background: #fff;
  border: 2px solid var(--lyra-border, #ebeef5);
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
  transition: 0.15s;
  overflow: hidden;
}
.card:hover {
  border-color: var(--el-color-primary-light-5, #f6c69a);
  transform: translateY(-1px);
}
.card.selected {
  border-color: var(--el-color-primary, #d97706);
  box-shadow: 0 0 0 3px var(--el-color-primary-light-7, rgba(217, 119, 6, 0.18));
}

.check-corner {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--el-color-primary, #d97706);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  z-index: 2;
  opacity: 0;
  transform: scale(0.5);
  transition: 0.15s;
}
.card.selected .check-corner { opacity: 1; transform: scale(1); }

.cover {
  width: 100%;
  height: 130px;
  object-fit: cover;
  border-radius: 6px;
  background: #f5f5f5;
}
.meta { padding-top: 6px; }
.title { font-weight: 600; font-size: 14px; line-height: 1.3; word-break: break-word; }
.artist { color: #999; font-size: 12px; margin-top: 2px; }
.tags { margin-top: 6px; display: flex; flex-wrap: wrap; gap: 4px; }
</style>
