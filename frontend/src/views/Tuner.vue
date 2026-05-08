<template>
  <div class="page">
    <h2 class="title">🎸 吉他定音器</h2>
    <p class="hint">点击琴钮播放对应弦的标准音；也可以选预设调弦或整体降调。</p>

    <!-- 预设调弦 -->
    <div class="control-card">
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
    </div>

    <!-- 琴头 + 6 弦 -->
    <div class="guitar">
      <div class="head">Lyra</div>

      <div class="strings">
        <div v-for="n in 6" :key="n" class="string" :class="`s${n}`"></div>
      </div>

      <!-- 左侧：6/5/4 弦 -->
      <div class="pegs left">
        <div
          v-for="i in [0, 1, 2]" :key="`l${i}`"
          class="peg-row"
          @click="playString(i)"
        >
          <div class="peg" :class="{ active: displayStrings[i]?.isPlaying }">
            <span class="note">{{ displayStrings[i]?.note }}</span>
          </div>
          <span class="string-num">{{ 6 - i }}</span>
        </div>
      </div>

      <!-- 右侧：3/2/1 弦 -->
      <div class="pegs right">
        <div
          v-for="i in [3, 4, 5]" :key="`r${i}`"
          class="peg-row"
          @click="playString(i)"
        >
          <span class="string-num">{{ 6 - i }}</span>
          <div class="peg" :class="{ active: displayStrings[i]?.isPlaying }">
            <span class="note">{{ displayStrings[i]?.note }}</span>
          </div>
        </div>
      </div>
    </div>

    <p class="footer">Lyra Tuner · Web Audio Edition</p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const NOTES = ['C', 'C#', 'D', 'Eb', 'E', 'F', 'F#', 'G', 'Ab', 'A', 'Bb', 'B']
// baseIndex = 半音绝对值（C0=0），用来 mod 12 算音名
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
  setTimeout(() => { s.isPlaying = false }, 500)
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
.page { padding: 32px 24px; max-width: 900px; margin: 0 auto; }
.title { margin: 0 0 4px; }
.hint { color: var(--lyra-text-muted); margin: 0 0 20px; }

.control-card {
  background: #fff;
  border: 1px solid var(--lyra-border);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
}
.control-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 8px 0;
}
.control-row .label { min-width: 70px; color: var(--lyra-text-muted); font-size: 13px; }
.offset-text { color: var(--el-color-primary); font-weight: 600; min-width: 90px; text-align: right; }

.guitar {
  position: relative;
  height: 380px;
  background: linear-gradient(180deg, #8b5a2b 0%, #6b3f1d 100%);
  border-radius: 14px;
  box-shadow: inset 0 0 30px rgba(0,0,0,0.3), 0 8px 20px rgba(0,0,0,0.15);
  overflow: hidden;
  padding: 24px 0;
}
.head {
  text-align: center;
  font-family: "Georgia", serif;
  color: #fef3c7;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 4px;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

/* 6 根琴弦：横向贯穿，从中央向上下展开 */
.strings {
  position: absolute;
  top: 80px;
  left: 0;
  right: 0;
  bottom: 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  pointer-events: none;
}
.string {
  height: 2px;
  background: linear-gradient(90deg, #c0c0c0 0%, #fff 50%, #c0c0c0 100%);
  box-shadow: 0 1px 2px rgba(0,0,0,0.2);
}
.string.s1 { height: 1.2px; opacity: 0.8; }
.string.s2 { height: 1.4px; }
.string.s3 { height: 1.6px; }
.string.s4 { height: 2px; }
.string.s5 { height: 2.6px; }
.string.s6 { height: 3.2px; }

/* 琴钮列 */
.pegs {
  position: absolute;
  top: 80px;
  bottom: 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  z-index: 2;
}
.pegs.left  { left: 24px; }
.pegs.right { right: 24px; }
.peg-row { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.peg {
  width: 50px; height: 50px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #fef3c7, #d97706);
  border: 2px solid #92400e;
  box-shadow: 0 3px 6px rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.18s;
}
.peg:hover { transform: scale(1.08); }
.peg.active {
  background: radial-gradient(circle at 30% 30%, #fff, #fbbf24);
  box-shadow: 0 0 16px rgba(251, 191, 36, 0.8), 0 3px 6px rgba(0,0,0,0.3);
}
.note {
  font-weight: 700;
  font-size: 16px;
  color: #7c2d12;
  font-family: "Georgia", serif;
}
.string-num {
  color: #fef3c7;
  font-size: 13px;
  width: 12px;
  text-align: center;
}

.footer { text-align: center; color: var(--lyra-text-muted); font-size: 12px; margin-top: 24px; letter-spacing: 1px; }
</style>
