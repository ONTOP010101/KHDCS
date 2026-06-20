<template>
  <div class="repository-panel">
    <div class="repo-header">
      <span class="repo-title">数据源 / 模板</span>
      <div class="repo-actions">
        <button class="repo-action-btn" @click="collapseAll" title="全部折叠"><ChevronsDownUp :size="14" /></button>
      </div>
    </div>
    <div class="repo-body">
      <div v-for="node in treeData" :key="node.id" class="tree-node-wrapper">
        <!-- 父节点 -->
        <div class="tree-node tree-folder" @click="toggleNode(node)" :style="{ paddingLeft: (node.depth * 16 + 8) + 'px' }">
          <ChevronRight :size="14" class="tree-chevron" :class="{ expanded: node.expanded }" />
          <Folder :size="14" class="tree-icon folder-icon" />
          <span class="tree-label">{{ node.label }}</span>
        </div>
        <!-- 子节点 -->
        <div v-if="node.expanded">
          <div
            v-for="child in node.children"
            :key="child.id"
            class="tree-node tree-leaf"
            :class="{ active: child.selected }"
            @click="selectLeaf(child)"
            :style="{ paddingLeft: ((node.depth + 1) * 16 + 8) + 'px' }"
          >
            <FileText v-if="child.icon === 'file'" :size="14" class="tree-icon leaf-icon" />
            <Database v-else-if="child.icon === 'db'" :size="14" class="tree-icon leaf-icon" />
            <span class="tree-label">{{ child.label }}</span>
            <Circle v-if="child.selected" :size="6" class="tree-dot" fill="#2f6ef2" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ChevronRight, Folder, FileText, Database, Circle, ChevronsDownUp } from 'lucide-vue-next'

const emit = defineEmits(['template-select'])

const treeData = reactive([
  {
    id: 'ds',
    label: '数据源',
    expanded: false,
    depth: 0,
    children: [
      { id: 'ds1', label: 'SampleDB', icon: 'db', selected: false }
    ]
  },
  {
    id: 'tpl',
    label: '报表模板',
    expanded: true,
    depth: 0,
    children: [
      { id: 'rpt1', label: '厂商确认表', icon: 'file', selected: true },
      { id: 'rpt2', label: '参展商统计', icon: 'file', selected: false }
    ]
  },
  {
    id: 'sty',
    label: '报表样式',
    expanded: false,
    depth: 0,
    children: [
      { id: 'sty1', label: '默认样式', icon: 'file', selected: false }
    ]
  }
])

function toggleNode(node) {
  node.expanded = !node.expanded
}

function selectLeaf(child) {
  for (const node of treeData) {
    if (node.children) {
      for (const c of node.children) {
        c.selected = c.id === child.id
      }
    }
  }
  emit('template-select', child.label)
}

function collapseAll() {
  for (const node of treeData) {
    node.expanded = false
  }
}
</script>

<style scoped>
.repository-panel {
  width: 220px;
  height: 100%;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.repo-header {
  height: 40px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f1f3;
}

.repo-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.repo-action-btn {
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.repo-action-btn:hover {
  background: #f3f4f6;
  color: #4b5563;
}

.repo-body {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 32px;
  padding-right: 12px;
  cursor: pointer;
  user-select: none;
  transition: background 0.12s;
}

.tree-node:hover {
  background: #f3f4f6;
}

.tree-chevron {
  color: #9ca3af;
  flex-shrink: 0;
  transition: transform 0.15s;
}

.tree-chevron.expanded {
  transform: rotate(90deg);
}

.tree-icon.folder-icon {
  color: #f59e0b;
}

.tree-icon.leaf-icon {
  color: #6b7280;
}

.tree-label {
  font-size: 13px;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-leaf.active {
  background: #eef2ff;
  color: #2f6ef2;
}

.tree-leaf.active .tree-label {
  color: #2f6ef2;
  font-weight: 500;
}

.tree-dot {
  color: #2f6ef2;
  flex-shrink: 0;
  margin-left: auto;
}
</style>
