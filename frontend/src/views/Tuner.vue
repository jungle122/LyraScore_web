<template>
  <div class="page">
    <div class="hd">
      <h1>🎸 吉他定音器</h1>
      <p class="sub">点击琴弦播放标准音；下方可切换预设调弦或整体降调。</p>
    </div>

    <!-- 调弦控件 -->
    <el-card class="control-card" shadow="never">
      <div class="control-row">
        <span class="label">调弦模式</span>
        <el-radio-group v-model="presetIndex" @change="recalc">
          <el-radio-button v-for="(p, i) in presets" :key="p.name" :label="i">{{ p.name }}</el-radio-button>
        </el-radio-group>
      </div>

      <div class="control-row">
        <span class="label">整体降调</span>
        <el-slider
          v-model="globalOffset"
          :min="-6" :max="0" :step="1"
          :marks="{ 0: '标准', '-3': '↓3', '-6': '↓6' }"
          @change="recalc"
          style="flex: 1; margin-left: 12px; max-width: 420px;"
        />
        <span class="offset-text">
          {{ globalOffset === 0 ? '标准音高' : `降 ${-globalOffset} 半音` }}
        </span>
      </div>
    </el-card>

    <!-- 指板 -->
    <div class="fretboard">
      <!-- 琴枕 -->
      <div class="nut"></div>

      <!-- 品丝 -->
      <div class="frets">
        <div class="fret" v-for="n in 4" :key="n"></div>
      </div>

      <!-- 6 根弦 -->
      <div class="strings">
        <div
          v-for="(s, i) in displayStrings"
          :key="i"
          class="string-col"
          :class="{ active: s.isPlaying }"
          @click="playString(i)"
        >
          <div class="string-line" :style="{ width: stringWidths[i] }"></div>
          <div class="string-label">{{ s.note }}</div>
          <div class="string-num">{{ 6 - i }}</div>
        </div>
      </div>
    </div>

    <p class="footer">{{ presets[presetIndex].name }} · {{ globalOffset === 0 ? '标准音高' : `降 ${-globalOffset} 半音` }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const NOTES = ['C', 'C#', 'D', 'Eb', 'E', 'F', 'F#', 'G', 'Ab', 'A', 'Bb', 'B']
const BASE_STRINGS = [
  { id: 0, file: 'E2.mp3', baseIndex: 4 + 12 * 2 },
  { id: 1, file: 'A2.mp3', baseIndex: 9 + 12 * 2 },
  { id: 2, file: 'D3.mp3', baseIndex: 2 + 12 * 3 },
  { id: 3, file: 'G3.mp3', baseIndex: 7 + 12 * 3 },
  { id: 4, file: 'B3.mp3', baseIndex: 11 + 12 * 3 },
  { id: 5, file: 'E4.mp3', baseIndex: 4 + 12 * 4 },
]
const presets = [
  { name: 'Standard', offsets: [0, 0, 0, 0, 0, 0] },
  { name: 'Drop D',   offsets: [-2, 0, 0, 0, 0, 0] },
  { name: 'Open D',   offsets: [-2, 0, 0, -1, -2, -2] },
  { name: 'Open G',   offsets: [-2, -2, 0, 0, 0, -2] },
  { name: 'DADGAD',   offsets: [-2, 0, 0, 0, -2, -2] },
]

const stringWidths = ['5px', '4px', '3.5px', '3px', '2.5px', '2px']

const presetIndex = ref(0)
const globalOffset = ref(0)
const displayStrings = ref([])

let audioCtx = null
const buffers = {}

async function preload() {
  audioCtx = new (window.AudioContext || window.webkitAudioContext)()
  for (const item of BASE_STRINGS) {
    try {
      const resp = await fetch(`/audio/${item.file}`)
      const ab = await resp.arrayBuffer()
      buffers[item.file] = await audioCtx.decodeAudioData(ab)
    } catch (e) {
      console.error('音频加载失败', item.file, e)
    }
  }
}

function recalc() {
  const preset = presets[presetIndex.value]
  const shift = globalOffset.value
  displayStrings.value = BASE_STRINGS.map((base, i) => {
    const totalOffset = preset.offsets[i] + shift
    const noteIdx = ((base.baseIndex + totalOffset) % 12 + 12) % 12
    const rate = Math.pow(2, totalOffset / 12)
    return { ...base, note: NOTES[noteIdx], rate, isPlaying: false }
  })
}

function playString(i) {
  const s = displayStrings.value[i]
  const buf = buffers[s.file]
  if (!buf) {
    ElMessage.info('音频还在加载，请稍候')
    return
  }
  if (audioCtx.state === 'suspended') audioCtx.resume()
  const source = audioCtx.createBufferSource()
  source.buffer = buf
  source.playbackRate.value = s.rate
  source.connect(audioCtx.destination)
  source.start()

  s.isPlaying = true
  setTimeout(() => { s.isPlaying = false }, 600)
}

onMounted(async () => {
  recalc()
  await preload()
})

onUnmounted(() => {
  if (audioCtx) audioCtx.close()
})
</script>

<style scoped>
.page { padding: 32px 24px; max-width: 760px; margin: 0 auto; }
.hd { margin-bottom: 16px; text-align: center; }
.hd h1 { margin: 0 0 4px; }
.sub { color: var(--lyra-text-muted); margin: 0; font-size: 13px; }

.control-card { margin-bottom: 20px; }
.control-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 8px 0;
}
.control-row .label { min-width: 70px; color: var(--lyra-text-muted); font-size: 13px; }
.offset-text { color: var(--el-color-primary); font-weight: 600; min-width: 90px; text-align: right; }

/* 指板：深色木纹俯视 */
.fretboard {
  position: relative;
  height: 480px;
  background: linear-gradient(180deg, #2c2c1f 0%, #1f1f15 100%);
  border-radius: 10px;
  box-shadow: inset 0 0 60px rgba(0, 0, 0, 0.5), 0 8px 24px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

/* 琴枕：奶白色横条 */
.nut {
  position: absolute;
  top: 30px;
  left: 0; right: 0;
  height: 16px;
  background: linear-gradient(180deg, #faf5e6 0%, #e8dfc4 100%);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  z-index: 3;
}

/* 品丝 */
.frets {
  position: absolute;
  top: 46px;
  left: 0; right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
}
.fret {
  flex: 1;
  border-bottom: 2px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}
.fret:last-child { border-bottom: none; }

/* 6 根弦 */
.strings {
  position: absolute;
  top: 46px;
  left: 0; right: 0;
  bottom: 0;
  display: flex;
  z-index: 5;
}
.string-col {
  flex: 1;
  position: relative;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-top: 16px;
}
.string-line {
  position: absolute;
  top: -16px;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(180deg, #e8a04c 0%, #f5b76a 50%, #e8a04c 100%);
  box-shadow: 0 0 4px rgba(232, 160, 76, 0.4);
  transition: 0.18s;
  z-index: 1;
}
.string-col.active .string-line {
  background: linear-gradient(180deg, #fff7d6 0%, #fde68a 50%, #fff7d6 100%);
  box-shadow: 0 0 18px rgba(255, 220, 100, 0.95);
}
.string-col:hover .string-line {
  filter: brightness(1.15);
}

.string-label {
  position: relative;
  z-index: 4;
  background: rgba(0, 0, 0, 0.65);
  color: #fde68a;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  font-family: "Georgia", serif;
}
.string-col.active .string-label {
  background: var(--el-color-primary);
  color: #fff;
}

.string-num {
  position: absolute;
  bottom: 10px;
  color: rgba(255, 255, 255, 0.35);
  font-size: 11px;
  z-index: 2;
}

.footer {
  text-align: center;
  color: var(--lyra-text-muted);
  font-size: 12px;
  margin-top: 16px;
  letter-spacing: 1px;
}
</style>
