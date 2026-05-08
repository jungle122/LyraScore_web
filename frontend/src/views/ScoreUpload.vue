<template>
  <div class="upload-page">
    <div class="header">
      <h2>📤 上传乐谱</h2>
      <el-button @click="$router.push('/scores')">← 返回谱仓</el-button>
    </div>

    <el-card class="form-card">
      <el-form :model="form" label-width="100px">
        <el-form-item label="乐谱图片" required>
          <el-upload
            :auto-upload="false"
            :limit="12"
            :on-change="onFileChange"
            :on-remove="onFileRemove"
            list-type="picture-card"
            accept="image/*"
            multiple
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">支持多张 jpg/png/gif/webp，最多 12 张，单张最大 10MB。</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="歌曲名" required>
          <el-input v-model="form.title" placeholder="必填，最多 100 字" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="原唱/歌手">
          <el-input v-model="form.artist" placeholder="选填" maxlength="100" />
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
          <el-input v-model="form.tuning" placeholder="如：标准调弦 / Drop D" maxlength="50" />
        </el-form-item>

        <el-form-item label="变调夹">
          <el-input-number v-model="form.capo" :min="0" :max="12" placeholder="0-12 品" />
        </el-form-item>

        <el-form-item label="速度 BPM">
          <el-input-number v-model="form.bpm" :min="20" :max="300" />
        </el-form-item>

        <el-form-item label="是否公开">
          <el-switch v-model="form.isPublic" :active-value="1" :inactive-value="0" />
          <span class="hint">公开后其他用户也能看到</span>
        </el-form-item>

        <el-form-item label="备忘录">
          <el-input v-model="form.memo" type="textarea" :rows="3" placeholder="比如练习要点、和弦标记等" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">提交</el-button>
          <el-button @click="$router.push('/scores')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createScore } from '@/api/score'

const router = useRouter()
const files = ref([])         // 多张：File 对象数组
const loading = ref(false)
const form = reactive({
  title: '',
  artist: '',
  instrument: '吉他',
  style: '弹唱',
  tuning: '',
  capo: null,
  bpm: null,
  isPublic: 0,
  memo: '',
})

function onFileChange(uploadFile, fileList) {
  // fileList 是当前所有 el-upload 的文件项；提取 raw File 对象
  files.value = fileList.map(f => f.raw).filter(Boolean)
}

function onFileRemove(uploadFile, fileList) {
  files.value = fileList.map(f => f.raw).filter(Boolean)
}

async function submit() {
  if (files.value.length === 0) {
    ElMessage.warning('请至少选择一张图片')
    return
  }
  if (!form.title) {
    ElMessage.warning('请输入歌曲名')
    return
  }
  const fd = new FormData()
  // 多图：同 name 多条 → MultipartFile[]
  files.value.forEach(f => fd.append('files', f))
  fd.append('title', form.title)
  if (form.artist) fd.append('artist', form.artist)
  if (form.instrument) fd.append('instrument', form.instrument)
  if (form.style) fd.append('style', form.style)
  if (form.tuning) fd.append('tuning', form.tuning)
  if (form.capo != null) fd.append('capo', form.capo)
  if (form.bpm != null) fd.append('bpm', form.bpm)
  fd.append('isPublic', form.isPublic)
  if (form.memo) fd.append('memo', form.memo)

  loading.value = true
  try {
    const id = await createScore(fd)
    ElMessage.success('上传成功')
    router.push(`/scores/${id}`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.upload-page { padding: 24px; max-width: 720px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.form-card { padding: 16px; }
.hint { color: #999; margin-left: 12px; font-size: 13px; }
</style>
