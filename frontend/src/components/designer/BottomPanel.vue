<template>
  <div class="bottom-panel">
    <div class="bottom-tabs">
      <button class="bottom-tab active">
        <Table2 :size="14" /> 数据预览
      </button>
    </div>
    <div class="bottom-content">
      <div class="data-area">
        <div v-if="loading" class="data-state"><Loader2 :size="18" class="spin" /> 加载中...</div>
        <div v-else-if="!previewData.length" class="data-state"><span>暂无数据，点击右上角「加载预览」获取数据</span></div>
        <div v-else class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th class="th-index">#</th>
                <th v-for="col in visibleColumns" :key="col.field">{{ col.title }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, ri) in previewData" :key="ri">
                <td class="td-index">{{ ri + 1 }}</td>
                <td v-for="col in visibleColumns" :key="col.field">{{ row[col.field] ?? '' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Table2, Loader2 } from 'lucide-vue-next'

defineProps({
  previewData: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  visibleColumns: {
    type: Array,
    default: () => [],
  },
})
</script>

<style scoped>
.bottom-panel {
  border-top: 2px solid #e5e7eb;
  background: #fff;
  display: flex;
  flex-direction: column;
  height: 180px;
}

.bottom-tabs {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
  background: #fafbfc;
}

.bottom-tab {
  padding: 8px 16px;
  border: none;
  background: transparent;
  font-size: 12px;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: inherit;
  border-bottom: 2px solid transparent;
  transition: all 0.15s;
}

.bottom-tab:hover {
  color: #374151;
}

.bottom-tab.active {
  color: #2f6ef2;
  border-bottom-color: #2f6ef2;
  font-weight: 500;
}

.bottom-content {
  flex: 1;
  overflow: auto;
}

.data-area {
  height: 100%;
}

.data-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9ca3af;
  font-size: 13px;
  gap: 8px;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.data-table-wrap {
  height: 100%;
  overflow: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.data-table th {
  position: sticky;
  top: 0;
  background: #f8fafc;
  padding: 6px 10px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 2px solid #e5e7eb;
  white-space: nowrap;
}

.data-table td {
  padding: 5px 10px;
  border-bottom: 1px solid #f0f1f3;
  color: #4b5563;
}

.data-table tbody tr:hover {
  background: #f8fafc;
}

.th-index,
.td-index {
  width: 40px;
  text-align: center;
  color: #9ca3af;
}
</style>
