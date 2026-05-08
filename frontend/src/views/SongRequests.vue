<template>
  <div class="page">
    <div class="header">
      <h2>🎤 AI 智能点歌</h2>
      <el-button type="primary" @click="openCreate">+ 给好友点歌</el-button>
    </div>

    <el-tabs v-model="active">
      <el-tab-pane label="我点的" name="sent">
        <el-empty v-if="sent.length === 0" description="还没点过歌" />
        <el-card v-for="r in sent" :key="r.id" class="row-card" shadow="never">
          <div class="row-head">
            <span>给 <b>{{ r.receiverName }}</b></span>
            <el-tag v-if="r.status === 0" type="primary" size="small">未弹</el-tag>
            <el-tag v-else-if="r.status === 1" type="success" size="small">已还愿 🎵</el-tag>
            <el-tag v-else type="warning" size="small">AI 失败 / 待人工审核</el-tag>
            <span class="time">{{ r.createdAt }}</span>
          </div>
          <div class="ai" v-if="r.aiSong || r.aiArtist">
            🤖 <b>{{ r.aiSong || '?' }}</b> — {{ r.aiArtist || '?' }}
          </div>
          <div class="raw">原文：{{ r.rawInput }}</div>
          <div v-if="r.requestMessage" class="msg">💬 {{ r.requestMessage }}</div>
          <div v-if="r.status !== 1" class="actions">
            <el-button size="small" :loading="retrying === r.id" @click="onRetry(r)">🔄 重新清洗</el-button>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane :label="`我收到的（${pendingCount}）`" name="received">
        <el-empty v-if="received.length === 0" description="还没收到点歌" />
        <el-card v-for="r in received" :key="r.id" class="row-card" shadow="never">
          <div class="row-head">
            <span><b>{{ r.senderName }}</b> 点了一首</span>
            <el-tag v-if="r.status === 0" type="primary" size="small">未弹</el-tag>
            <el-tag v-else-if="r.status === 1" type="success" size="small">已还愿</el-tag>
            <el-tag v-else type="warning" size="small">AI 失败 / 待人工审核</el-tag>
            <span class="time">{{ r.createdAt }}</span>
          </div>
          <div class="ai" v-if="r.aiSong || r.aiArtist">
            🤖 <b>{{ r.aiSong || '?' }}</b> — {{ r.aiArtist || '?' }}
          </div>
          <div class="raw">原文：{{ r.rawInput }}</div>
          <div v-if="r.requestMessage" class="msg">💬 {{ r.requestMessage }}</div>
          <div v-if="r.status !== 1" class="actions">
            <el-button size="small" type="success" @click="onFulfill(r)">标记已弹</el-button>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 点歌弹窗 -->
    <el-dialog v-model="showCreate" title="给好友点歌" width="520">
      <el-form :model="form" label-width="100px">
        <el-form-item label="选好友">
          <el-select v-model="form.receiverId" placeholder="选择好友" style="width: 100%" filterable>
            <el-option v-for="f in friends" :key="f.otherUserId" :label="f.otherUsername" :value="f.otherUserId" />
          </el-select>
        </el-form-item>
        <el-form-item label="自由文本">
          <el-input
            v-model="form.rawInput"
            type="textarea"
            :rows="3"
            placeholder="比如：许嵩的半城烟沙好听爆了，求弹奏"
            maxlength="500"
            show-word-limit
          />
          <div class="hint">AI 会自动从这段文本里识别歌曲名和歌手</div>
        </el-form-item>
        <el-form-item label="快捷短语">
          <el-select v-model="form.requestMessage" placeholder="选一句快捷短语（可选）" style="width: 100%" clearable>
            <el-option v-for="d in phrases" :key="d.id" :label="d.dictValue" :value="d.dictValue" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="onCreate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createSongRequest, sentRequests, receivedRequests, fulfillRequest, retryRequest, getDictionary } from '@/api/song'
import { myFriends } from '@/api/friend'

const active = ref('sent')
const sent = ref([])
const received = ref([])
const friends = ref([])
const phrases = ref([])

const showCreate = ref(false)
const creating = ref(false)
const form = reactive({ receiverId: null, rawInput: '', requestMessage: '' })

const pendingCount = computed(() => received.value.filter(r => r.status === 0).length)

async function loadAll() {
  const [s, r] = await Promise.all([sentRequests(), receivedRequests()])
  sent.value = s
  received.value = r
}

async function openCreate() {
  if (friends.value.length === 0) friends.value = await myFriends()
  if (phrases.value.length === 0) phrases.value = await getDictionary('quick_dm')
  form.receiverId = null
  form.rawInput = ''
  form.requestMessage = ''
  showCreate.value = true
}

async function onCreate() {
  if (!form.receiverId) { ElMessage.warning('请选择好友'); return }
  if (!form.rawInput) { ElMessage.warning('请输入歌曲文本'); return }
  creating.value = true
  try {
    await createSongRequest({ ...form })
    ElMessage.success('已发出点歌')
    showCreate.value = false
    loadAll()
  } finally { creating.value = false }
}

async function onFulfill(r) {
  await fulfillRequest(r.id)
  ElMessage.success('已标记')
  loadAll()
}

const retrying = ref(null)
async function onRetry(r) {
  retrying.value = r.id
  try {
    await retryRequest(r.id)
    ElMessage.success('已重新清洗')
    loadAll()
  } finally {
    retrying.value = null
  }
}

onMounted(loadAll)
</script>

<style scoped>
.page { padding: 24px; max-width: 900px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.row-card { margin-bottom: 8px; }
.row-head { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.time { color: #999; font-size: 12px; margin-left: auto; }
.ai { font-size: 16px; margin: 8px 0; padding: 8px 12px; background: #ecf5ff; border-radius: 4px; }
.raw { color: #666; font-size: 13px; }
.msg { color: #409eff; font-size: 13px; margin-top: 4px; }
.actions { margin-top: 8px; }
.hint { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
