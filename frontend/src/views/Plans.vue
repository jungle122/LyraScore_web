<template>
  <div class="plans-page">
    <div class="header">
      <h2>📅 我的练习计划</h2>
      <div>
        <el-button @click="$router.push('/')">← 返回首页</el-button>
        <el-button type="primary" @click="showCreate = true">+ 新建计划</el-button>
      </div>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="还没有计划，新建一个吧" />

    <el-card v-for="p in list" :key="p.id" class="plan-card" shadow="hover" @click="goDetail(p.id)">
      <div class="plan-row">
        <div>
          <div class="title">{{ p.title }}</div>
          <div class="meta">{{ p.startDate }} → {{ p.endDate }}</div>
        </div>
        <div class="actions">
          <el-tag v-if="p.status === 0" type="primary">进行中</el-tag>
          <el-tag v-else type="success">已完成</el-tag>
          <el-button size="small" type="danger" plain @click.stop="onDelete(p)">删除</el-button>
        </div>
      </div>
    </el-card>

    <!-- 创建 modal -->
    <el-dialog v-model="showCreate" title="新建练习计划" width="500">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="100" placeholder="如：本周指弹" />
        </el-form-item>
        <el-form-item label="起止日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
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
import { listPlans, createPlan, deletePlan } from '@/api/plan'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const showCreate = ref(false)
const creating = ref(false)
const form = reactive({ title: '' })
const dateRange = ref([])

async function load() {
  loading.value = true
  try {
    list.value = await listPlans()
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/plans/${id}`)
}

async function onCreate() {
  if (!form.title) { ElMessage.warning('请填写标题'); return }
  if (!dateRange.value || dateRange.value.length !== 2) { ElMessage.warning('请选择起止日期'); return }
  creating.value = true
  try {
    await createPlan({ title: form.title, startDate: dateRange.value[0], endDate: dateRange.value[1] })
    ElMessage.success('创建成功')
    showCreate.value = false
    form.title = ''
    dateRange.value = []
    load()
  } finally {
    creating.value = false
  }
}

async function onDelete(p) {
  try {
    await ElMessageBox.confirm(`确定删除计划「${p.title}」吗？`, '删除确认', { type: 'warning' })
  } catch { return }
  await deletePlan(p.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.plans-page { padding: 24px; max-width: 900px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.plan-card { margin-bottom: 12px; cursor: pointer; }
.plan-row { display: flex; justify-content: space-between; align-items: center; }
.title { font-weight: 600; font-size: 16px; }
.meta { color: #999; font-size: 13px; margin-top: 4px; }
.actions { display: flex; gap: 8px; align-items: center; }
</style>
