<template>
  <div class="page">
    <h2 class="title">⏱ 节拍器</h2>
    <p class="hint">支持 30-250 BPM，4 种拍号；强拍音色更亮。</p>

    <!-- 拍号 -->
    <div class="ts-row">
      <span class="ts-label">拍号</span>
      <el-radio-group v-model="tsIndex" @change="onTsChange">
        <el-radio-button v-for="(t, i) in timeSignatures" :key="t.name" :label="i">{{ t.name }}</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 主面板 -->
    <div class="panel">
      <div class="bpm-display">
        <span class="bpm-num">{{ bpm }}</span>
        <span class="bpm-unit">BPM</span>
      </div>

      <!-- 节拍指示灯 -->
      <div class="beats">
        <div
          v-for="n in beatsPerBar"
          :key="n"
          class="beat-dot"
          :class="{ on: isPlaying && currentBeat === n, strong: n === 1 }"
        />
      </div>

      <!-- BPM 控制 -->
      <div class="bpm-controls">
        <el-button circle size="large" @click="changeBpm(-5)">-5</el-button>
        <el-button circle size="large" @click="changeBpm(-1)">-1</el-button>
        <el-button type="primary" size="large" round @click="togglePlay" class="play-btn">
          {{ isPlaying ? '■ 停止' : '▶ 开始' }}
        </el-button>
        <el-button circle size="large" @click="changeBpm(1)">+1</el-button>
        <el-button circle size="large" @click="changeBpm(5)">+5</el-button>
      </div>

      <el-slider
        v-model="bpm"
        :min="30" :max="250" :step="1"
        :marks="{ 30: '30', 90: '90', 120: '120', 180: '180', 250: '250' }"
        @change="onBpmChange"
        style="margin-top: 28px;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const bpm = ref(90)
const isPlaying = ref(false)
const currentBeat = ref(0)
const timeSignatures = [
  { name: '4/4', beats: 4 },
  { name: '3/4', beats: 3 },
  { name: '2/4', beats: 2 },
  { name: '6/8', beats: 6 },
]
const tsIndex = ref(0)
const beatsPerBar = computed(() => timeSignatures[tsIndex.value].beats)

let audioCtx = null
let tickBuffer = null
let timer = null

async function preload() {
  audioCtx = new (window.AudioContext || window.webkitAudioContext)()
  try {
    const resp = await fetch('/audio/tick.mp3')
    const ab = await resp.arrayBuffer()
    tickBuffer = await audioCtx.decodeAudioData(ab)
  } catch (e) {
    console.error('节拍器音频加载失败', e)
  }
}

function playTick() {
  let next = currentBeat.value + 1
  if (next > beatsPerBar.value) next = 1
  currentBeat.value = next

  if (!tickBuffer) return
  const source = audioCtx.createBufferSource()
  source.buffer = tickBuffer
  source.playbackRate.value = next === 1 ? 1.5 : 1.0   // 强拍更高音
  source.connect(audioCtx.destination)
  source.start()
}

function start() {
  if (!tickBuffer) {
    ElMessage.info('音频还在加载，请稍候')
    return
  }
  stop()
  if (audioCtx.state === 'suspended') audioCtx.resume()
  isPlaying.value = true
  currentBeat.value = 0
  playTick()
  timer = setInterval(playTick, 60000 / bpm.value)
}

function stop() {
  if (timer) clearInterval(timer)
  timer = null
  isPlaying.value = false
  currentBeat.value = 0
}

function togglePlay() {
  isPlaying.value ? stop() : start()
}

function changeBpm(delta) {
  let v = bpm.value + delta
  if (v < 30) v = 30
  if (v > 250) v = 250
  bpm.value = v
  if (isPlaying.value) start()
}

function onBpmChange() { if (isPlaying.value) start() }
function onTsChange()  { if (isPlaying.value) start() }

onMounted(preload)
onUnmounted(() => {
  stop()
  if (audioCtx) audioCtx.close()
})
</script>

<style scoped>
.page { padding: 32px 24px; max-width: 720px; margin: 0 auto; }
.title { margin: 0 0 4px; }
.hint { color: var(--lyra-text-muted); margin: 0 0 24px; }

.ts-row { display: flex; align-items: center; gap: 14px; margin-bottom: 20px; }
.ts-label { color: var(--lyra-text-muted); font-size: 13px; }

.panel {
  background: var(--lyra-bg-accent);
  border: 1px solid var(--lyra-border);
  border-radius: 16px;
  padding: 36px 24px;
  text-align: center;
}

.bpm-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
}
.bpm-num {
  font-size: 96px;
  font-weight: 700;
  color: var(--el-color-primary);
  font-family: "Georgia", serif;
  line-height: 1;
}
.bpm-unit {
  font-size: 18px;
  color: var(--lyra-text-muted);
  letter-spacing: 2px;
}

.beats {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 28px;
}
.beat-dot {
  width: 16px; height: 16px;
  border-radius: 50%;
  background: rgba(217, 119, 6, 0.18);
  transition: 0.06s;
}
.beat-dot.on { background: var(--el-color-primary); transform: scale(1.4); }
.beat-dot.on.strong { background: #b45309; box-shadow: 0 0 12px var(--el-color-primary); }

.bpm-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.play-btn { min-width: 140px; font-size: 16px; }
</style>
