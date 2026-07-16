<template>
  <div class="designer-toolbar">
    <div class="toolbar-left">
      <button class="tb-btn tb-btn-primary" @click="$emit('preview')">
        <Eye :size="32" /> <span>预览</span>
      </button>
      <div class="tb-sep"></div>
      <button class="tb-btn" @click="$emit('save')"><Save :size="30" /> <span>保存</span></button>
      <button class="tb-btn" @click="$emit('print')"><Printer :size="30" /> <span>打印</span></button>
      <button class="tb-btn" @click="$emit('export')"><Download :size="30" /> <span>导出</span></button>
      <div class="tb-sep"></div>
      <button class="tb-btn" @click="$emit('new')"><FilePlus :size="30" /> <span>新建</span></button>
      <div class="tb-sep"></div>
      <button class="tb-btn" @click="$emit('drafts')"><FolderOpen :size="30" /> <span>草稿箱</span></button>
    </div>
    <div class="toolbar-center">
      <span class="tb-title">{{ reportTitle }}</span>
    </div>
    <div class="toolbar-right">
      <button class="tb-btn tb-btn-icon" @click="props.zoomOut()" :disabled="props.zoomLevel <= 0.25" title="缩小">
        <ZoomOut :size="32" />
      </button>
      <span class="tb-zoom-text">{{ Math.round(props.zoomLevel * 100) }}%</span>
      <button class="tb-btn tb-btn-icon" @click="props.zoomIn()" :disabled="props.zoomLevel >= 3" title="放大">
        <ZoomIn :size="32" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { Eye, Save, Printer, Download, FilePlus, FolderOpen, ZoomOut, ZoomIn } from 'lucide-vue-next'

const props = defineProps({
  zoomLevel: {
    type: Number,
    required: true,
  },
  zoomIn: {
    type: Function,
    required: true,
  },
  zoomOut: {
    type: Function,
    required: true,
  },
  reportTitle: {
    type: String,
    default: '未命名报表',
  },
})

defineEmits(['save', 'preview', 'print', 'export', 'new', 'drafts'])
</script>

<style scoped>
.designer-toolbar {
  height: 88px;
  background: #fff;
  border-bottom: 2px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding: 0 32px;
  font-family: -apple-system, BlinkMacSystemFont, 'Microsoft YaHei', sans-serif;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 0;
}

.toolbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.tb-btn {
  height: 64px;
  padding: 0 24px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: #4b5563;
  font-size: 26px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: inherit;
  transition: all 0.15s;
}

.tb-btn:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.tb-btn-primary {
  background: #2f6ef2;
  color: #fff;
}

.tb-btn-primary:hover {
  background: #2563eb;
}

.tb-btn-icon {
  padding: 0 12px;
  width: 64px;
  justify-content: center;
}

.tb-btn:disabled {
  opacity: 0.35;
  cursor: default;
}

.tb-btn:disabled:hover {
  background: transparent;
  color: #4b5563;
}

.tb-sep {
  width: 2px;
  height: 40px;
  background: #e5e7eb;
  margin: 0 8px;
}

.tb-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
}

.tb-zoom-text {
  font-size: 24px;
  color: #6b7280;
  min-width: 80px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}
</style>
