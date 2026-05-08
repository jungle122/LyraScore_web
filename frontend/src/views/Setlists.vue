<template>
  <div class="page">
    <div class="header">
      <h2>📋 我的歌单</h2>
      <el-button type="primary" @click="showCreate = true">+ 新建歌单</el-button>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="还没有歌单" />

    <div class="grid">
      <el-card v-for="s in list" :key="s.id" class="card" shadow="hover" @click="goDetail(s.id)">
        <div class="cover">📋</div>
        <div class="meta">
          <div class="title">{{ s.name }}</div>
          <div class="desc">{{ s.description || '—' }}</div>
        </div>
        <el-button size="small" type="danger" plain class="del" @click.stop="onDelete(s)">删除</el-button>
      </el-card>
    </div>

    <el-dialog v-model="showCreate" title="新建歌单" width="500">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="onCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSetlists, createSetlist, deleteSetlist } from '@/api/setlist'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const showCreate = ref(false)
const creating = ref(false)
const form = reactive({ name: '', description: '' })

async function load() {
  loading.value = true
  try { list.value = await listSetlists() } finally { loading.value = false }
}

function goDetail(id) { router.push(`/setlists/${id}`) }

async function onCreate() {
  if (!form.name) { ElMessage.warning('请填写名称'); return }
  creating.value = true
  try {
    await createSetlist({ name: form.name, description: form.description })
    ElMessage.success('已创建')
    showCreate.value = false
    form.name = ''; form.description = ''
    load()
  } finally { creating.value = false }
}

async function onDelete(s) {
  try {
    await ElMessageBox.confirm(`确定删除歌单「${s.name}」吗？`, '删除确认', { type: 'warning' })
  } catch { return }
  await deleteSetlist(s.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.page { padding: 24px; max-width: 1100px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.card { cursor: pointer; position: relative; }
.cover { font-size: 60px; text-align: center; padding: 24px 0; background: #f5f7fa; border-radius: 4px; }
.meta { padding-top: 8px; }
.title { font-weight: 600; }
.desc { color: #999; font-size: 13px; margin-top: 2px; }
.del { position: absolute; top: 12px; right: 12px; }
</style>
