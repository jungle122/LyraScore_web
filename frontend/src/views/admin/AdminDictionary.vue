<template>
  <div class="page" v-loading="loading">
    <div class="hd">
      <div>
        <h1>📚 字典管理</h1>
        <p class="sub">维护快捷短语等系统配置；点歌时弹窗里的备选短语就来自这里。</p>
      </div>
      <el-button type="primary" @click="openCreate">+ 新建条目</el-button>
    </div>

    <el-table :data="list" stripe style="width: 100%">
      <el-table-column prop="id"         label="ID"           width="80"  />
      <el-table-column label="分类"      width="160">
        <template #default="{ row }">{{ typeLabel(row.dictType) }}</template>
      </el-table-column>
      <el-table-column prop="dictValue"  label="展示文字" />
      <el-table-column prop="dictKey"    label="内部编号"     width="110" />
      <el-table-column prop="sortOrder"  label="排序"         width="80"  />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isActive === 1 ? 'success' : 'info'" size="small">
            {{ row.isActive === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建 / 编辑 弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑字典条目' : '新建字典条目'" width="540">
      <el-form :model="form" label-width="120px">
        <el-form-item label="所属分类">
          <el-select v-model="form.dictType" placeholder="选择字典类型" style="width: 100%">
            <el-option
              v-for="t in DICT_TYPES"
              :key="t.value"
              :label="`${t.label}（${t.value}）`"
              :value="t.value"
            />
          </el-select>
          <div class="hint">用来分组同类配置；目前系统只用了「点歌快捷短语」这一类</div>
        </el-form-item>

        <el-form-item label="内部编号">
          <el-input v-model="form.dictKey" :placeholder="suggestedKey" maxlength="50" />
          <div class="hint">程序内部识别用的唯一编号（推荐：{{ suggestedKey }}），不会展示给用户</div>
        </el-form-item>

        <el-form-item label="展示文字">
          <el-input v-model="form.dictValue" placeholder="例如：大佬带带我" maxlength="255" />
          <div class="hint">这是用户在点歌弹窗里能看到的实际文字</div>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" :step="10" />
          <div class="hint">数字越小越靠前；建议用 10 / 20 / 30 …，方便以后插队</div>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-switch v-model="form.isActive" :active-value="1" :inactive-value="0" />
          <span class="hint" style="margin-left: 8px;">关闭后，该短语不再出现在用户的下拉中</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListDict, adminCreateDict, adminUpdateDict, adminDeleteDict } from '@/api/admin'

// 字典分类清单：未来扩展时往这里加
const DICT_TYPES = [
  { value: 'quick_dm', label: '点歌快捷短语' },
]

const list = ref([])
const loading = ref(false)

const showDialog = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const form = reactive({
  id: null, dictType: 'quick_dm', dictKey: '', dictValue: '', sortOrder: 10, isActive: 1,
})

// 根据当前分类已有条目数 + 1，建议下一个 dict_key
const suggestedKey = computed(() => {
  const prefix = form.dictType === 'quick_dm' ? 'dm' : (form.dictType + '_')
  const existing = list.value
    .filter(d => d.dictType === form.dictType)
    .map(d => d.dictKey)
  let n = existing.length + 1
  while (existing.includes(`${prefix}${n}`)) n++
  return `${prefix}${n}`
})

function resetForm() {
  Object.assign(form, { id: null, dictType: 'quick_dm', dictKey: '', dictValue: '', sortOrder: nextSortOrder('quick_dm'), isActive: 1 })
}

// 自动算一个比该分类内最大值再大 10 的排序值
function nextSortOrder(type) {
  const arr = list.value.filter(d => d.dictType === type).map(d => d.sortOrder ?? 0)
  return arr.length ? Math.max(...arr) + 10 : 10
}

// 把 dict_type 翻译成人话
function typeLabel(t) {
  return DICT_TYPES.find(x => x.value === t)?.label || t
}

async function load() {
  loading.value = true
  try { list.value = await adminListDict() } finally { loading.value = false }
}

function openCreate() {
  resetForm()
  isEdit.value = false
  showDialog.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  isEdit.value = true
  showDialog.value = true
}

async function onSubmit() {
  // 用户没填 dictKey 时自动用建议值
  if (!form.dictKey) form.dictKey = suggestedKey.value
  if (!form.dictType || !form.dictValue) {
    ElMessage.warning('分类、展示文字不能为空')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await adminUpdateDict(form.id, { ...form })
      ElMessage.success('已更新')
    } else {
      await adminCreateDict({ ...form })
      ElMessage.success('已创建')
    }
    showDialog.value = false
    load()
  } finally { saving.value = false }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.dictValue}」吗？`, '删除确认', { type: 'warning' })
  } catch { return }
  await adminDeleteDict(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.page { padding: 24px; max-width: 1100px; margin: 0 auto; }
.hd { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.hd h1 { margin: 0 0 4px; }
.sub { color: var(--lyra-text-muted); margin: 0; font-size: 13px; }
.hint { color: var(--lyra-text-dim); font-size: 12px; margin-top: 4px; line-height: 1.5; }
</style>
