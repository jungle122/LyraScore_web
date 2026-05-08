<template>
  <div class="page" v-loading="loading">
    <div class="hd">
      <h1>👥 用户管理</h1>
      <p class="sub">查看全站用户、冻结/解冻违规账号。</p>
    </div>

    <el-table :data="users" stripe style="width: 100%">
      <el-table-column prop="id"        label="ID"       width="80" />
      <el-table-column prop="username"  label="用户名" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
            {{ row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'warning'" size="small">
            {{ row.status === 0 ? '正常' : '已冻结' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="200">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            size="small"
            type="warning"
            :disabled="row.role === 'admin'"
            @click="onFreeze(row, 1)"
          >
            冻结
          </el-button>
          <el-button
            v-else
            size="small"
            type="success"
            @click="onFreeze(row, 0)"
          >
            解冻
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <p class="hint">
      管理员账号无法被冻结。冻结后用户无法新登录（已登录的 JWT 在过期前仍可用——是无状态认证的固有限制）。
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListUsers, adminSetUserStatus } from '@/api/admin'

const users = ref([])
const loading = ref(false)

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 19)
}

async function load() {
  loading.value = true
  try { users.value = await adminListUsers() } finally { loading.value = false }
}

async function onFreeze(user, status) {
  const verb = status === 1 ? '冻结' : '解冻'
  try {
    await ElMessageBox.confirm(`确定${verb}用户「${user.username}」吗？`, '操作确认', { type: 'warning' })
  } catch { return }
  await adminSetUserStatus(user.id, status)
  ElMessage.success(`已${verb}`)
  load()
}

onMounted(load)
</script>

<style scoped>
.page { padding: 24px; max-width: 1100px; margin: 0 auto; }
.hd { margin-bottom: 20px; }
.hd h1 { margin: 0 0 4px; }
.sub { color: var(--lyra-text-muted); margin: 0; font-size: 13px; }
.hint { color: var(--lyra-text-muted); font-size: 12px; margin-top: 12px; }
</style>
