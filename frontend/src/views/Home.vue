<template>
  <div class="home">
    <h1>🎼 LyraScore 云端谱仓</h1>
    <p class="welcome">
      <template v-if="userStore.isLoggedIn">
        欢迎回来，<strong>{{ userStore.userInfo?.username || '...' }}</strong>
      </template>
      <template v-else>请先登录</template>
    </p>

    <div class="modules">
      <el-card class="mod" shadow="hover" @click="$router.push('/scores')">
        <h3>🎼 我的谱仓</h3>
        <p>上传、管理你的吉他谱</p>
      </el-card>
      <el-card class="mod" shadow="hover" @click="$router.push('/plans')">
        <h3>📅 练习计划</h3>
        <p>组织阶段性练习目标</p>
      </el-card>
      <el-card class="mod" shadow="hover" @click="$router.push('/logs')">
        <h3>📊 练习日志</h3>
        <p>记录练习时长 + 看统计</p>
      </el-card>
      <el-card class="mod" shadow="hover" @click="$router.push('/setlists')">
        <h3>📋 歌单</h3>
        <p>多对多收藏你的乐谱</p>
      </el-card>
      <el-card class="mod" shadow="hover" @click="$router.push('/friends')">
        <h3>👥 社交</h3>
        <p>添加好友、互相关注</p>
      </el-card>
      <el-card class="mod" shadow="hover" @click="$router.push('/song-requests')">
        <h3>🎤 AI 点歌</h3>
        <p>自由文本→AI 清洗成曲名/歌手</p>
      </el-card>
    </div>

    <div class="footer">
      <el-button v-if="!userStore.isLoggedIn" type="primary" @click="$router.push('/login')">去登录</el-button>
      <el-button v-if="userStore.isLoggedIn" type="danger" plain @click="logout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function logout() {
  userStore.logout()
  ElMessage.success('已退出')
  router.push('/login')
}

// 已登录但 userInfo 还是 null（页面刷新场景）→ 拉一下 /me
onMounted(() => {
  if (userStore.isLoggedIn && !userStore.userInfo) {
    userStore.fetchMe().catch(() => {})
  }
})
</script>

<style scoped>
.home { padding: 40px; max-width: 1100px; margin: 0 auto; }
.welcome { color: #666; margin-bottom: 24px; }
.modules { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.mod { cursor: pointer; }
.mod h3 { margin: 0 0 8px; }
.mod p { margin: 0; color: #999; font-size: 13px; }
.mod.disabled { cursor: not-allowed; opacity: 0.5; }
.footer { margin-top: 32px; }
</style>
