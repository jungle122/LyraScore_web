<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-inner">
        <h1>
          <span class="hero-greeting">{{ greeting }}，</span>
          <span class="hero-name">{{ userStore.userInfo?.username || '...' }}</span>
        </h1>
        <p class="hero-sub">把每一次练习，存进你的云端谱仓。</p>
      </div>
    </section>

    <!-- 功能模块 -->
    <section class="modules">
      <h2 class="section-title">核心模块</h2>
      <div class="grid">
        <el-card class="mod" shadow="hover" @click="$router.push('/scores')">
          <div class="mod-emoji">🎼</div>
          <div class="mod-name">我的谱仓</div>
          <div class="mod-desc">上传、管理你的吉他谱</div>
        </el-card>
        <el-card class="mod" shadow="hover" @click="$router.push('/plans')">
          <div class="mod-emoji">📅</div>
          <div class="mod-name">练习计划</div>
          <div class="mod-desc">组织阶段性练习目标</div>
        </el-card>
        <el-card class="mod" shadow="hover" @click="$router.push('/logs')">
          <div class="mod-emoji">📊</div>
          <div class="mod-name">练习日志</div>
          <div class="mod-desc">记录练习时长 + 看统计</div>
        </el-card>
        <el-card class="mod" shadow="hover" @click="$router.push('/setlists')">
          <div class="mod-emoji">📋</div>
          <div class="mod-name">歌单</div>
          <div class="mod-desc">多对多收藏你的乐谱</div>
        </el-card>
        <el-card class="mod" shadow="hover" @click="$router.push('/friends')">
          <div class="mod-emoji">👥</div>
          <div class="mod-name">社交</div>
          <div class="mod-desc">添加好友、互相关注</div>
        </el-card>
        <el-card class="mod" shadow="hover" @click="$router.push('/song-requests')">
          <div class="mod-emoji">🎤</div>
          <div class="mod-name">AI 点歌</div>
          <div class="mod-desc">自由文本 → AI 清洗成曲名/歌手</div>
        </el-card>
        <el-card class="mod" shadow="hover" @click="$router.push('/badges')">
          <div class="mod-emoji">🏅</div>
          <div class="mod-name">成就徽章</div>
          <div class="mod-desc">事件驱动自动发牌</div>
        </el-card>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6)  return '凌晨练琴'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
</script>

<style scoped>
.home { padding-bottom: 60px; }

.hero {
  background: var(--lyra-bg-warm);
  border-bottom: 1px solid var(--lyra-border);
}
.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 64px 24px 56px;
}
.hero h1 {
  margin: 0;
  font-size: 38px;
  font-weight: 700;
  color: var(--lyra-text);
}
.hero-greeting { color: var(--lyra-text-muted); font-weight: 400; }
.hero-name { color: var(--el-color-primary); }
.hero-sub {
  margin-top: 12px;
  color: var(--lyra-text-muted);
  font-size: 16px;
  letter-spacing: 0.5px;
}

.modules {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px 0;
}
.section-title {
  font-size: 20px;
  margin: 0 0 20px;
  color: var(--lyra-text);
  font-weight: 600;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.mod {
  cursor: pointer;
  text-align: center;
  padding: 8px 4px 4px;
  transition: transform 0.18s;
}
.mod:hover { transform: translateY(-2px); }
.mod-emoji {
  font-size: 44px;
  margin: 8px 0 12px;
}
.mod-name {
  font-weight: 600;
  font-size: 15px;
  color: var(--lyra-text);
}
.mod-desc {
  margin-top: 6px;
  color: var(--lyra-text-muted);
  font-size: 13px;
}
</style>
