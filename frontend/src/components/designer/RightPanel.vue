<template>
  <div class="right-panel">
    <!-- 顶部 Tab 切换 -->
    <div class="right-tabs">
      <button class="right-tab" :class="{ active: activeTab === 'palette' }" @click="activeTab = 'palette'">组件</button>
      <button class="right-tab" :class="{ active: activeTab === 'properties' }" @click="activeTab = 'properties'">属性</button>
      <button class="right-tab" :class="{ active: activeTab === 'config' }" @click="activeTab = 'config'">配置</button>
    </div>

    <!-- 组件面板 -->
    <div v-show="activeTab === 'palette'" class="right-content">
      <div class="palette-group">
        <div class="palette-group-title">基本元素</div>
        <div class="palette-items">
          <div v-for="item in basicElements" :key="item.type" class="palette-item" draggable="true" @dragstart="onDragStart($event, item)">
            <component :is="item.icon" :size="18" />
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>
      <div class="palette-group">
        <div class="palette-group-title">复合元素</div>
        <div class="palette-items">
          <div v-for="item in compositeElements" :key="item.type" class="palette-item" draggable="true" @dragstart="onDragStart($event, item)">
            <component :is="item.icon" :size="18" />
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 属性面板 -->
    <div v-show="activeTab === 'properties'" class="right-content">
      <div v-if="!selectedPanel" class="empty-tip">
        <MousePointer2 :size="20" class="empty-icon" />
        <p>选中画布上的元素<br/>查看属性</p>
      </div>
      <div v-else class="props-list">
        <div class="prop-item">
          <label>类型</label>
          <input type="text" :value="typeLabelMap[selectedPanel.type] || '-'" readonly disabled />
        </div>
        <div class="prop-item">
          <label>X 坐标</label>
          <input type="number" :value="Math.round(selectedPanel.x || 0)" readonly disabled />
        </div>
        <div class="prop-item">
          <label>Y 坐标</label>
          <input type="number" :value="Math.round(selectedPanel.y || 0)" readonly disabled />
        </div>
        <div class="prop-item">
          <label>宽度</label>
          <input type="number" :value="Math.round(selectedPanel.w || 0)" readonly disabled />
        </div>
        <div class="prop-item">
          <label>高度</label>
          <input type="number" :value="Math.round(selectedPanel.h || 0)" readonly disabled />
        </div>
      </div>
    </div>

    <!-- 配置面板 (slot) -->
    <div v-show="activeTab === 'config'" class="right-content">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  Type,
  Database,
  Image,
  Minus,
  Square,
  Hash,
  Calendar,
  Layers,
  MousePointer2,
} from 'lucide-vue-next'

defineProps({
  selectedPanel: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['element-drag-start'])

const activeTab = ref('palette')

const typeLabelMap = { static: '静态文本', dynamic: '动态字段' }

const basicElements = [
  { type: 'static', label: '静态文本', icon: Type },
  { type: 'dynamic', label: '动态字段', icon: Database },
  { type: 'image', label: '图片', icon: Image },
  { type: 'line', label: '直线', icon: Minus },
  { type: 'rectangle', label: '矩形', icon: Square },
]

const compositeElements = [
  { type: 'page-number', label: '页码', icon: Hash },
  { type: 'current-date', label: '当前日期', icon: Calendar },
  { type: 'total-pages', label: '总页数', icon: Layers },
]

function onDragStart(event, item) {
  event.dataTransfer.effectAllowed = 'copy'
  event.dataTransfer.setData('application/designer-palette', item.type)
  emit('element-drag-start', item.type)
}
</script>

<style scoped>
.right-panel {
  width: 260px;
  height: 100%;
  background: #fff;
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  font: 13px system-ui;
}

.right-tabs {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.right-tab {
  flex: 1;
  height: 38px;
  border: none;
  background: transparent;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  font-family: inherit;
  border-bottom: 2px solid transparent;
  transition: all 0.15s;
}

.right-tab:hover {
  color: #374151;
  background: #f9fafb;
}

.right-tab.active {
  color: #2f6ef2;
  font-weight: 600;
  border-bottom-color: #2f6ef2;
}

.right-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.palette-group {
  margin-bottom: 16px;
}

.palette-group-title {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  margin-bottom: 8px;
  text-transform: none;
  letter-spacing: 0.5px;
}

.palette-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.palette-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: grab;
  user-select: none;
  transition: all 0.15s;
  color: #4b5563;
}
.palette-item:active { cursor: grabbing; }
.palette-item:hover {
  border-color: #93b4f5;
  background: #eef2ff;
  color: #2f6ef2;
}
.palette-item span {
  font-size: 13px;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #9ca3af;
  text-align: center;
  gap: 12px;
  font-size: 13px;
  line-height: 1.6;
}

.empty-icon {
  opacity: 0.3;
}

.props-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.prop-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.prop-item label {
  width: 42px;
  font-size: 12px;
  color: #6b7280;
  flex-shrink: 0;
}

.prop-item input {
  flex: 1;
  height: 32px;
  padding: 0 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  color: #374151;
  background: #f9fafb;
  outline: none;
  font-family: inherit;
}
</style>
