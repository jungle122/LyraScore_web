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
            :limit="1"
            :on-change="onFileChange"
            :on-remove="onFileRemove"
            list-type="picture"
            accept="image/*"
          >
            <el-button type="primary">选择图片</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 jpg/png/gif/webp，最大 10MB。</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="歌曲名" required>
          <el-input v-model="form.title" placeholder="必填，最多 100 字" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="原唱/歌手">
          <el-input v-model="form.artist" placeholder="选填" maxlength="100" />
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
import { createScore } from '@/api/score'

const router = useRouter()
const file = ref(null)
const loading = ref(false)
const form = reactive({
  title: '',
  artist: '',
  tuning: '',
  capo: null,
  bpm: null,
  isPublic: 0,
  memo: '',
})

function onFileChange(uploadFile) {
  // el-upload 的 file 对象，真实 File 在 uploadFile.raw
  file.value = uploadFile.raw
}

function onFileRemove() {
  file.value = null
}

async function submit() {
  if (!file.value) {
    ElMessage.warning('请选择图片')
    return
  }
  if (!form.title) {
    ElMessage.warning('请输入歌曲名')
    return
  }
  const fd = new FormData()
  fd.append('file', file.value)
  fd.append('title', form.title)
  if (form.artist) fd.append('artist', form.artist)
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
