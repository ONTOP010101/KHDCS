<template>
  <div class="simple-date-picker" ref="rootRef">
    <div class="sdp-input" ref="inputRef" @click="toggle">
      <span v-if="modelValue" class="sdp-value">{{ modelValue }}</span>
      <span v-else class="sdp-placeholder">{{ placeholder }}</span>
      <svg v-if="modelValue && clearable" class="sdp-clear" @click.stop="clear" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/><path d="m15 9-6 6m0-6 6 6"/>
      </svg>
      <svg class="sdp-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>
      </svg>
    </div>
    <Teleport to="body">
      <Transition name="sdp-fade">
        <div v-if="open" class="sdp-panel" :style="panelStyle">
          <div class="sdp-header">
            <button class="sdp-nav" @click="prevMonth">&lt;</button>
            <span class="sdp-month">{{ year }}年 {{ month }}月</span>
            <button class="sdp-nav" @click="nextMonth">&gt;</button>
          </div>
          <div class="sdp-weekdays">
            <span v-for="w in weekdays" :key="w" class="sdp-wd">{{ w }}</span>
          </div>
          <div class="sdp-days">
            <button
              v-for="(d, i) in days"
              :key="i"
              class="sdp-day"
              :class="{
                'sdp-day--empty': !d,
                'sdp-day--selected': d && isSelected(d),
                'sdp-day--today': d && isToday(d)
              }"
              :disabled="!d"
              @click="d && select(d)"
            >{{ d }}</button>
          </div>
          <div class="sdp-footer">
            <button class="sdp-footer-btn" @click="clear">清除</button>
            <button class="sdp-footer-btn sdp-footer-btn--ok" @click="open = false">确定</button>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '选择日期' },
  clearable: { type: Boolean, default: true },
})
const emit = defineEmits(['update:modelValue', 'change'])

const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const open = ref(false)
const rootRef = ref(null)
const inputRef = ref(null)
const viewDate = ref(new Date())
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

const parseDate = () => {
  if (!props.modelValue) return null
  const [y, m, d] = props.modelValue.split('-').map(Number)
  return new Date(y, m - 1, d)
}

const isSelected = (d) => {
  const v = parseDate()
  if (!v) return false
  return v.getFullYear() === year.value && v.getMonth() + 1 === month.value && v.getDate() === d
}

const isToday = (d) => {
  const now = new Date()
  return now.getFullYear() === year.value && now.getMonth() + 1 === month.value && now.getDate() === d
}

const updatePanelPos = () => {
  if (!inputRef.value) return
  const rect = inputRef.value.getBoundingClientRect()
  const panelH = 400 // 预估面板高度
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
  const m = String(month.value).padStart(2, '0')
  const dd = String(d).padStart(2, '0')
  const val = `${year.value}-${m}-${dd}`
  emit('update:modelValue', val)
  emit('change', val)
  open.value = false
}

const clear = () => {
  emit('update:modelValue', '')
  emit('change', '')
  open.value = false
}

const prevMonth = () => { viewDate.value = new Date(year.value, month.value - 2, 1) }
const nextMonth = () => { viewDate.value = new Date(year.value, month.value, 1) }

const onClickOutside = (e) => {
  if (!open.value) return
  if (rootRef.value && rootRef.value.contains(e.target)) return
  // check if click is inside teleported panel
  const panel = document.querySelector('.sdp-panel')
  if (panel && panel.contains(e.target)) return
  open.value = false
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
.sdp-panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(0,0,0,0.18);
  padding: 14px 16px;
  min-width: 340px;
}
.sdp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.sdp-month { font-size: 24px; font-weight: 600; color: #1d1d1f; }
.sdp-nav {
  width: 36px; height: 36px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  background: #f5f5f5;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.sdp-nav:hover { background: #e8e8e8; }
.sdp-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 6px;
}
.sdp-wd {
  text-align: center;
  font-size: 18px;
  color: #999;
  height: 36px;
  line-height: 36px;
}
.sdp-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}
.sdp-day {
  width: 50px;
  height: 50px;
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
.sdp-day:hover { background: #f0f4ff; color: #007aff; }
.sdp-day--empty { visibility: hidden; }
.sdp-day--selected { background: #007aff; color: #fff; font-weight: 600; }
.sdp-day--selected:hover { background: #0056cc; color: #fff; }
.sdp-day--today { border: 2px solid #007aff; }
.sdp-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}
.sdp-footer-btn {
  padding: 6px 18px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  background: #fff;
  font-size: 18px;
  cursor: pointer;
}
.sdp-footer-btn--ok { background: #007aff; color: #fff; border-color: #007aff; }
.sdp-fade-enter-active, .sdp-fade-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.sdp-fade-enter-from, .sdp-fade-leave-to { opacity: 0; transform: translateY(-6px); }
</style>
