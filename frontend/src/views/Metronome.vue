<template>
  <div class="page">
    <div class="hd">
      <h1>⏱ 节拍器</h1>
      <p class="sub">极简模式：BPM、拍号、强拍变调一应俱全。</p>
    </div>

    <!-- BPM 大数字 -->
    <div class="bpm-display">
      <span class="bpm-num">{{ bpm }}</span>
      <span class="bpm-unit">BPM</span>
    </div>

    <!-- 大圆形播放/停止按钮 -->
    <button class="play-btn" :class="{ playing: isPlaying }" @click="togglePlay">
      <span class="icon" v-if="!isPlaying">▶</span>
      <span class="icon stop" v-else></span>
    </button>

    <!-- 拍点小圆 -->
    <div class="beats">
      <span
        v-for="n in beatsPerBar"
        :key="n"
        class="dot"
        :class="{ on: isPlaying && currentBeat === n, strong: n === 1 }"
      />
    </div>

    <!-- BPM 滑块 + ± 按钮 -->
    <div class="bpm-controls">
      <el-button circle text @click="changeBpm(-5)" class="bpm-step">-5</el-button>
      <el-button circle text @click="changeBpm(-1)" class="bpm-step">-1</el-button>
      <el-slider
        v-model="bpm"
        :min="30" :max="250" :step="1"
        @change="onBpmChange"
        class="bpm-slider"
      />
      <el-button circle text @click="changeBpm(1)" class="bpm-step">+1</el-button>
      <el-button circle text @click="changeBpm(5)" class="bpm-step">+5</el-button>
    </div>

    <!-- 拍号切换（次要） -->
    <div class="ts-row">
      <el-radio-group v-model="tsIndex" size="small" @change="onTsChange">
        <el-radio-button v-for="(t, i) in timeSignatures" :key="t.name" :label="i">{{ t.name }}</el-radio-button>
      </el-radio-group>
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
  source.playbackRate.value = next === 1 ? 1.5 : 1.0
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
.page {
  padding: 40px 24px;
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.hd { margin-bottom: 24px; }
.hd h1 { margin: 0 0 4px; }
.sub { color: var(--lyra-text-muted); margin: 0; font-size: 13px; }

/* BPM 大数字 */
.bpm-display {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 36px;
}
.bpm-num {
  font-size: 80px;
  font-weight: 200;          /* 极细 */
  color: var(--lyra-text);
  line-height: 1;
  letter-spacing: -2px;
  font-family: "Georgia", serif;
}
.bpm-unit {
  font-size: 14px;
  color: var(--lyra-text-dim);
  letter-spacing: 3px;
}

/* 大圆播放按钮：参考极简风 */
.play-btn {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: transparent;
  border: 2px solid var(--lyra-text);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.18s;
  margin-bottom: 30px;
}
.play-btn:hover {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
}
.play-btn.playing {
  background: var(--lyra-text);
  border-color: var(--lyra-text);
}
.play-btn .icon {
  font-size: 38px;
  color: var(--lyra-text);
  line-height: 1;
  margin-left: 4px;     /* 视觉居中三角 */
}
.play-btn.playing .icon {
  margin-left: 0;
  display: inline-block;
  width: 24px;
  height: 28px;
  background: #fff;
  border-radius: 2px;
}

/* 拍点：小灰圆 */
.beats {
  display: flex;
  gap: 14px;
  margin-bottom: 28px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.12);
  transition: 0.06s;
}
.dot.on { background: var(--el-color-primary); transform: scale(1.4); }
.dot.on.strong { background: var(--el-color-primary-dark-2); box-shadow: 0 0 8px var(--el-color-primary); }

/* BPM 控件横排 */
.bpm-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  margin-bottom: 18px;
}
.bpm-step { color: var(--lyra-text-muted); font-size: 13px; }
.bpm-slider { flex: 1; }

/* 拍号 */
.ts-row { margin-top: 4px; }
</style>
