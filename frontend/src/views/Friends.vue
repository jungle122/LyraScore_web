<template>
  <div class="page">
    <div class="header">
      <h2>👥 社交</h2>
    </div>

    <el-tabs v-model="active">
      <el-tab-pane label="我的好友" name="friends">
        <el-empty v-if="friends.length === 0" description="还没有好友" />
        <el-card v-for="f in friends" :key="f.id" class="row-card" shadow="never">
          <div class="row">
            <div class="avatar">{{ (f.otherUsername || '?').charAt(0).toUpperCase() }}</div>
            <div class="meta"><div class="name">{{ f.otherUsername }}</div></div>
            <el-button size="small" type="danger" plain @click="onDelete(f)">删除</el-button>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane :label="`待处理（${pendings.length}）`" name="pending">
        <el-empty v-if="pendings.length === 0" description="没有待处理的申请" />
        <el-card v-for="p in pendings" :key="p.id" class="row-card" shadow="never">
          <div class="row">
            <div class="avatar">{{ (p.otherUsername || '?').charAt(0).toUpperCase() }}</div>
            <div class="meta">
              <div class="name">{{ p.otherUsername }}</div>
              <div class="hint">向你发起了好友申请</div>
            </div>
            <el-button size="small" type="success" @click="onAccept(p)">接受</el-button>
            <el-button size="small" type="danger" plain @click="onDelete(p)">拒绝</el-button>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="添加好友" name="add">
        <el-card shadow="never">
          <el-form :model="form" label-width="100px">
            <el-form-item label="对方用户名">
              <el-input v-model="form.username" placeholder="输入对方注册用户名" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="sending" @click="onSendRequest">发送申请</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myFriends, pending, requestFriend, acceptFriend, deleteFriend } from '@/api/friend'

const active = ref('friends')
const friends = ref([])
const pendings = ref([])
const form = reactive({ username: '' })
const sending = ref(false)

async function loadAll() {
  const [f, p] = await Promise.all([myFriends(), pending()])
  friends.value = f
  pendings.value = p
}

async function onSendRequest() {
  if (!form.username) { ElMessage.warning('请输入对方用户名'); return }
  sending.value = true
  try {
    await requestFriend(form.username.trim())
    ElMessage.success('申请已发出')
    form.username = ''
  } finally { sending.value = false }
}

async function onAccept(p) {
  await acceptFriend(p.id)
  ElMessage.success('已添加为好友')
  loadAll()
}

async function onDelete(record) {
  try {
    await ElMessageBox.confirm('确认删除？', '确认', { type: 'warning' })
  } catch { return }
  await deleteFriend(record.id)
  ElMessage.success('已删除')
  loadAll()
}

onMounted(loadAll)
</script>

<style scoped>
.page { padding: 24px; max-width: 800px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.row-card { margin-bottom: 8px; }
.row { display: flex; align-items: center; gap: 12px; }
.avatar { width: 40px; height: 40px; border-radius: 50%; background: var(--el-color-primary); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 600; }
.meta { flex: 1; }
.name { font-weight: 600; }
.hint { color: #999; font-size: 13px; }
</style>
