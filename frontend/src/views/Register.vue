<template>
<div class="register-container">
    <el-card class="register-card">
    <h2 style="text-align: center; margin-bottom: 24px;">🎼 LyraScore 注册</h2>

    <el-form :model="form" label-width="0">
        <el-form-item>
        <el-input v-model="form.username" placeholder="用户名（3-50 字符）" />
        </el-form-item>

        <el-form-item>
        <el-input v-model="form.password" type="password" placeholder="密码（6-50 字符）" show-password />
        </el-form-item>

        <el-form-item>
        <el-input v-model="form.passwordConfirm" type="password" placeholder="再输一次密码" show-password />
        </el-form-item>

        <el-form-item>
        <el-button type="primary" style="width: 100%;" :loading="loading" @click="handleRegister">
            注册
        </el-button>
        </el-form-item>
    </el-form>

    <div style="text-align: center; color: #999;">
        已有账号？<router-link to="/login">去登录</router-link>
    </div>
    </el-card>
</div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const form = reactive({
  username: '',
  password: '',
  passwordConfirm: '',
})
const loading = ref(false)
const router = useRouter()

async function handleRegister() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (form.username.length < 3 || form.username.length > 50) {
    ElMessage.warning('用户名长度需 3-50 字符')
    return
  }
  if (form.password.length < 6 || form.password.length > 50) {
    ElMessage.warning('密码长度需 6-50 字符')
    return
  }
  if (form.password !== form.passwordConfirm) {
    ElMessage.warning('两次密码不一致')
    return
  }

  loading.value = true
  try {
    await register({ username: form.username, password: form.password })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    console.log(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.register-card {
  width: 380px;
}
</style>
