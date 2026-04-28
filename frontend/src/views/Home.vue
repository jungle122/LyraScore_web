<template>
  <div style="padding: 40px; text-align: center;">
    <h1>🎼 LyraScore 云端谱仓</h1>
    <p>骨架已就位，等待垂直切片
1（用户注册/登录）落地。</p>

    <!-- token 状态显示 -->
    <p>当前 token：<code>{{ token ? token.slice(0, 30) +'...' : '（未登录）' }}</code></p>

    <!-- 三个按钮 -->
    <el-button v-if="!token" type="primary"
@click="goLogin">去登录</el-button>
    <el-button v-if="token" @click="testMe">测试 /me
接口</el-button>
    <el-button v-if="token" type="danger"
@click="logout">退出登录</el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import service from '@/utils/request'

const router = useRouter()

// 把 localStorage 里的 token 读出来变成响应式 ref
// 这样退出后清掉 token，模板会自动更新
const token = ref(localStorage.getItem('token'))

// TODO 1: goLogin —— 跳 /login
function goLogin() {
  // 用 router.push('/login')
  router.push('/login')
}

// TODO 2: logout —— 清 token + 弹消息 + 跳 /login
function logout() {
  localStorage.removeItem('token')
  token.value = null   
  ElMessage.success('已退出')
  router.push('/login')
}

// TODO 3: testMe —— 调 /user/me 接口验证 token 有效
async function testMe() {
  // 用 await service.get('/user/me')
  // 拿到的就是 { userId, username, role }（拦截器已剥壳）
  // 用 ElMessageBox.alert(JSON.stringify(data, null, 2),'用户信息')
  //   显示一个弹窗（比 ElMessage横幅更醒目，能看清完整内容）
  // try/catch 包一下
  try {
    const data = await service.get('/user/me')
    ElMessageBox.alert(JSON.stringify(data, null, 2),'用户信息')
  } catch (e) {
    console.log(e)
  }
}
</script>