<template>
  <div class="page">
    <div class="hd">
      <h1>找谱搜索</h1>
      <p class="sub">输入简体即可，繁体站会自动转换。结果在原站新标签打开。</p>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <div class="search-row">
        <el-input
          v-model="query"
          placeholder="输入歌名或歌手，例如：青花瓷 周杰伦"
          clearable
          size="large"
          class="search-input"
          @keyup.enter="searchAll"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button
          type="primary"
          size="large"
          :disabled="!hasQuery"
          @click="searchAll"
        >
          一键多站搜（{{ SITES.length }}）
        </el-button>
      </div>

      <!-- 简繁转换预览（仅在有需要繁体的站点时显示） -->
      <div v-if="hasQuery && hasTradSite" class="preview">
        <div class="preview-line">
          <span class="preview-label">简体：</span>
          <span class="preview-text">{{ query }}</span>
        </div>
        <div class="preview-line">
          <span class="preview-label">繁体：</span>
          <span class="preview-text trad">{{ traditional }}</span>
          <span class="preview-tag">提交到 91pu 时使用</span>
        </div>
      </div>

      <p class="hint" v-if="!hasQuery">
        💡 提示：首次使用「一键多站搜」时，浏览器可能会拦截弹窗——请按地址栏右侧的提示允许本站弹窗后再试。
      </p>
    </el-card>

    <!-- 站点卡片 -->
    <div class="site-grid">
      <el-card v-for="s in SITES" :key="s.id" class="site-card" shadow="hover">
        <div class="site-head">
          <span class="site-icon">{{ s.icon }}</span>
          <div class="site-meta">
            <div class="site-name">
              {{ s.name }}
              <el-tag v-if="s.needTrad" type="warning" size="small">繁体</el-tag>
            </div>
            <div class="site-domain">{{ s.fullName }}</div>
          </div>
        </div>

        <div class="site-actions">
          <el-button
            type="primary"
            :disabled="!hasQuery"
            @click="searchOne(s)"
          >
            搜索
          </el-button>
          <el-button @click="openHome(s)">首页</el-button>
        </div>

        <!-- 单站搜索时的最终 URL 预览（debug-friendly） -->
        <div v-if="hasQuery" class="url-preview">
          → {{ s.buildUrl(s.needTrad ? traditional : query) }}
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import * as OpenCC from 'opencc-js'

// 简 → 台湾繁体（带词组词典，质量最佳）
const converter = OpenCC.Converter({ from: 'cn', to: 'twp' })

const SITES = [
  {
    id: '91pu',
    name: '91pu',
    fullName: '91pu 繁体吉他谱',
    icon: '🎸',
    home: 'https://www.91pu.com.tw',
    needTrad: true,
    buildUrl: (q) => `https://www.91pu.com.tw/plus/search.php?keyword=${encodeURIComponent(q)}&type=`,
  },
  {
    id: 'tan8',
    name: '弹琴吧',
    fullName: 'tan8.com',
    icon: '🎵',
    home: 'https://www.tan8.com',
    needTrad: false,
    buildUrl: (q) => `https://www.tan8.com/search-1-1-0.php?keyword=${encodeURIComponent(q)}`,
  },
  {
    id: 'flybuxiu',
    name: '不休吉他',
    fullName: 'flybuxiu.com',
    icon: '🌐',
    home: 'https://www.flybuxiu.com',
    needTrad: false,
    buildUrl: (q) => `https://www.flybuxiu.com/?s=${encodeURIComponent(q)}`,
  },
]

const query = ref('')
const hasQuery = computed(() => query.value.trim().length > 0)
const hasTradSite = computed(() => SITES.some(s => s.needTrad))
const traditional = computed(() => hasQuery.value ? converter(query.value) : '')

function searchOne(site) {
  if (!hasQuery.value) return
  const q = site.needTrad ? traditional.value : query.value
  window.open(site.buildUrl(q), '_blank', 'noopener')
}

function openHome(site) {
  window.open(site.home, '_blank', 'noopener')
}

function searchAll() {
  if (!hasQuery.value) return
  for (const s of SITES) {
    const q = s.needTrad ? traditional.value : query.value
    window.open(s.buildUrl(q), '_blank', 'noopener')
  }
}
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; padding: 24px; }
.hd { margin-bottom: 16px; }
.hd h1 { margin: 0 0 4px; }
.sub { color: var(--lyra-text-muted); margin: 0; font-size: 13px; }

.search-card { margin-bottom: 20px; }
.search-row {
  display: flex;
  gap: 12px;
  align-items: center;
}
.search-input { flex: 1; }

.preview {
  margin-top: 14px;
  padding: 12px 16px;
  background: var(--el-color-primary-light-9);
  border-radius: 6px;
  border: 1px dashed var(--el-color-primary-light-7);
  font-size: 14px;
}
.preview-line { display: flex; align-items: center; gap: 6px; margin: 2px 0; }
.preview-label { color: var(--lyra-text-muted); width: 48px; }
.preview-text { color: var(--lyra-text); font-weight: 500; }
.preview-text.trad { color: var(--el-color-primary); }
.preview-tag {
  margin-left: auto;
  color: var(--lyra-text-dim);
  font-size: 12px;
}

.hint {
  margin: 12px 0 0;
  color: var(--lyra-text-muted);
  font-size: 12px;
}

.site-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}
.site-card { padding: 4px; }
.site-head { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.site-icon { font-size: 32px; flex-shrink: 0; }
.site-meta { flex: 1; }
.site-name {
  font-weight: 600;
  font-size: 15px;
  color: var(--lyra-text);
  display: flex;
  align-items: center;
  gap: 6px;
}
.site-domain {
  color: var(--lyra-text-muted);
  font-size: 12px;
  margin-top: 2px;
}
.site-actions { display: flex; gap: 8px; }
.url-preview {
  margin-top: 10px;
  font-size: 11px;
  color: var(--lyra-text-dim);
  word-break: break-all;
  font-family: ui-monospace, "SF Mono", Menlo, Consolas, monospace;
}
</style>
