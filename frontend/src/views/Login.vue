<template>
<div class="login-container">
    <el-card class="login-card">
    <h2 style="text-align: center; margin-bottom: 24px;">🎼 LyraScore 登录</h2>

    <!-- el-form 是 Element Plus 的表单容器 -->
    <el-form :model="form" label-width="0">

        <el-form-item>
        <!-- v-model="form.username" 双向绑定 form 对象的 username 字段 -->
        <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>

        <el-form-item>
        <!-- type="password" 让输入变成圆点 -->
        <el-input v-model="form.password" type="password" placeholder="密码"
show-password />
        </el-form-item>

        <el-form-item>
        <!-- :loading 是 v-bind:loading 的简写，把 loading 这个 ref 的值传给按钮
-->
        <el-button type="primary" style="width: 100%;" :loading="loading"
@click="handleLogin">
            登录
        </el-button>
        </el-form-item>
    </el-form>

    <div style="text-align: center; color: #999;">
        还没账号？<router-link to="/register">去注册</router-link>
    </div>
    </el-card>
</div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

// reactive 是 ref 的"对象版本"——整个对象每个字段都是响应式的
// 这样 form.username 和 form.password 都能跟 input 双向绑定
const form = reactive({
username: '',
password: '',
})

// loading 用 ref：因为是单一布尔值，不是对象
const loading = ref(false)

// 拿到路由对象，用来登录成功后跳转
const router = useRouter()

// 点击"登录"按钮触发的函数
async function handleLogin() {
// TODO 1: 简单校验：username 和 password 都不能为空
//         空就 ElMessage.warning('请输入用户名和密码') 然后 return
//         （注意：在 <script> 里访问 reactive 字段不需要 .value）
if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
      return
}

// TODO 2: loading.value = true   （ref 在 script 里要写 .value）
loading.value = true
// TODO 3: try / catch / finally 三段式：
try {
const data = await login(form)   // 调 api，await 等结果
// data 长啥样？响应拦截器已经剥壳了，所以 data === { token, userId,username, role }
localStorage.setItem('token', data.token)
ElMessage.success('登录成功')
router.push('/')
} catch (e) {
// 错误提示拦截器已经弹了，这里不用再弹
// 留空就行（或者 console.log(e) 看一眼）
console.log(e)
} finally {
loading.value = false   // 不管成功失败都要关 loading
}
}
</script>

<style scoped>
.login-container {
height: 100vh;
display: flex;
align-items: center;
justify-content: center;
background: #f5f7fa;
}

.login-card {
width: 380px;
}
</style>