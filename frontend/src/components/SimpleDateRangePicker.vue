<template>
  <div class="simple-date-range-picker" ref="rootRef">
    <div class="sdrp-input" ref="inputRef" @click="toggle">
      <span v-if="modelValue && modelValue[0]" class="sdrp-value">
        {{ modelValue[0] }} ~ {{ modelValue[1] || '...' }}
      </span>
      <span v-else class="sdrp-placeholder">{{ placeholder }}</span>
      <svg v-if="modelValue && modelValue[0] && clearable" class="sdrp-clear" @click.stop="clear" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/><path d="m15 9-6 6m0-6 6 6"/>
      </svg>
      <svg class="sdrp-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>
      </svg>
    </div>
    <Teleport to="body">
      <Transition name="sdrp-fade">
        <div v-if="open" class="sdrp-panel" :style="panelStyle">
          <div class="sdrp-header">
            <button class="sdrp-nav" @click="prevMonth">&lt;</button>
            <span class="sdrp-month">{{ year }}年 {{ month }}月</span>
            <button class="sdrp-nav" @click="nextMonth">&gt;</button>
          </div>
          <div class="sdrp-weekdays">
            <span v-for="w in weekdays" :key="w" class="sdrp-wd">{{ w }}</span>
          </div>
          <div class="sdrp-days">
            <button
              v-for="(d, i) in days"
              :key="i"
              class="sdrp-day"
              :class="{
                'sdrp-day--empty': !d,
                'sdrp-day--range-start': d && isRangeStart(d),
                'sdrp-day--range-end': d && isRangeEnd(d),
                'sdrp-day--in-range': d && isInRange(d),
                'sdrp-day--today': d && isToday(d)
              }"
              :disabled="!d"
              @click="d && select(d)"
            >{{ d }}</button>
          </div>
          <div class="sdrp-footer">
            <button class="sdrp-footer-btn" @click="clear">清除</button>
            <button class="sdrp-footer-btn sdrp-footer-btn--ok" @click="confirm">确定</button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  placeholder: { type: String, default: '选择日期范围' },
  clearable: { type: Boolean, default: true },
})
const emit = defineEmits(['update:modelValue', 'change'])

const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const open = ref(false)
const rootRef = ref(null)
const inputRef = ref(null)
const viewDate = ref(new Date())
const pickStep = ref(0)
const tempStart = ref(null)
const panelStyle = ref({})

const year = computed(() => viewDate.value.getFullYear())
const month = computed(() => viewDate.value.getMonth() + 1)

const days = computed(() => {
  const y = viewDate.value.getFullYear()
  const m = viewDate.value.getMonth()
  const firstDay = new Date(y, m, 1).getDay()
  const lastDate = new Date(y, m + 1, 0).getDate()
  const arr = []
  for (let i = 0; i < firstDay; i++) arr.push(null)
  for (let d = 1; d <= lastDate; d++) arr.push(d)
  return arr
})

const toDate = (y, m, d) => new Date(y, m - 1, d)
const fmt = (y, m, d) => `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
const parseDate = (str) => {
  if (!str) return null
  const [y, m, d] = str.split('-').map(Number)
  return new Date(y, m - 1, d)
}

const isRangeStart = (d) => {
  const v = parseDate(props.modelValue?.[0])
  return !!v && v.getFullYear() === year.value && v.getMonth() + 1 === month.value && v.getDate() === d
}
const isRangeEnd = (d) => {
  const v = parseDate(props.modelValue?.[1])
  return !!v && v.getFullYear() === year.value && v.getMonth() + 1 === month.value && v.getDate() === d
}
const isInRange = (d) => {
  const start = parseDate(props.modelValue?.[0])
  const end = parseDate(props.modelValue?.[1])
  if (!start || !end) return false
  const cur = toDate(year.value, month.value, d).getTime()
  return cur > start.getTime() && cur < end.getTime()
}
const isToday = (d) => {
  const now = new Date()
  return now.getFullYear() === year.value && now.getMonth() + 1 === month.value && now.getDate() === d
}

const updatePanelPos = () => {
  if (!inputRef.value) return
  const rect = inputRef.value.getBoundingClientRect()
  const panelH = 400
  const spaceBelow = window.innerHeight - rect.bottom
  if (spaceBelow >= panelH || spaceBelow > rect.top) {
    panelStyle.value = { position: 'fixed', zIndex: 99999, top: rect.bottom + 4 + 'px', left: rect.left + 'px' }
  } else {
    panelStyle.value = { position: 'fixed', zIndex: 99999, bottom: (window.innerHeight - rect.top + 4) + 'px', left: rect.left + 'px' }
  }
}

const toggle = () => {
  open.value = !open.value
  if (open.value) nextTick(updatePanelPos)
}

const select = (d) => {
  const val = fmt(year.value, month.value, d)
  if (pickStep.value === 0) {
    tempStart.value = val
    pickStep.value = 1
  } else {
    const s = tempStart.value
    const e = val
    if (toDate(year.value, month.value, d) < parseDate(s)) {
      emit('update:modelValue', [e, s])
      emit('change', [e, s])
    } else {
      emit('update:modelValue', [s, e])
      emit('change', [s, e])
    }
    pickStep.value = 0
    tempStart.value = null
    open.value = false
  }
}

const confirm = () => {
  if (tempStart.value) {
    emit('update:modelValue', [tempStart.value, tempStart.value])
    emit('change', [tempStart.value, tempStart.value])
  }
  pickStep.value = 0
  tempStart.value = null
  open.value = false
}

const clear = () => {
  emit('update:modelValue', [])
  emit('change', [])
  pickStep.value = 0
  tempStart.value = null
  open.value = false
}

const prevMonth = () => { viewDate.value = new Date(year.value, month.value - 2, 1) }
const nextMonth = () => { viewDate.value = new Date(year.value, month.value, 1) }

const onClickOutside = (e) => {
  if (!open.value) return
  if (rootRef.value && rootRef.value.contains(e.target)) return
  const panel = document.querySelector('.sdrp-panel')
  if (panel && panel.contains(e.target)) return
  open.value = false
  pickStep.value = 0
  tempStart.value = null
}

onMounted(() => {
  document.addEventListener('click', onClickOutside, true)
  window.addEventListener('scroll', updatePanelPos, true)
  window.addEventListener('resize', updatePanelPos)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', onClickOutside, true)
  window.removeEventListener('scroll', updatePanelPos, true)
  window.removeEventListener('resize', updatePanelPos)
})
</script>

<style scoped>
.sdrp-panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(0,0,0,0.18);
  padding: 14px 16px;
  min-width: 360px;
}
.sdrp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.sdrp-month { font-size: 24px; font-weight: 600; color: #1d1d1f; }
.sdrp-nav {
  width: 40px; height: 40px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  background: #f5f5f5;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.sdrp-nav:hover { background: #e8e8e8; }
.sdrp-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 6px;
}
.sdrp-wd {
  text-align: center;
  font-size: 18px;
  color: #999;
  height: 36px;
  line-height: 36px;
}
.sdrp-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}
.sdrp-day {
  width: 48px;
  height: 48px;
  font-size: 22px;
  border: none;
  border-radius: 12px;
  background: transparent;
  cursor: pointer;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  justify-self: center;
}
.sdrp-day:hover { background: #f0f4ff; color: #007aff; }
.sdrp-day--empty { visibility: hidden; }
.sdrp-day--range-start,
.sdrp-day--range-end { background: #007aff; color: #fff; font-weight: 600; }
.sdrp-day--range-start:hover,
.sdrp-day--range-end:hover { background: #0056cc; color: #fff; }
.sdrp-day--in-range { background: #e6f0ff; color: #007aff; }
.sdrp-day--today { border: 2px solid #007aff; }
.sdrp-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}
.sdrp-footer-btn {
  padding: 6px 18px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  background: #fff;
  font-size: 18px;
  cursor: pointer;
}
.sdrp-footer-btn--ok { background: #007aff; color: #fff; border-color: #007aff; }
.sdrp-fade-enter-active, .sdrp-fade-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.sdrp-fade-enter-from, .sdrp-fade-leave-to { opacity: 0; transform: translateY(-6px); }
</style>
