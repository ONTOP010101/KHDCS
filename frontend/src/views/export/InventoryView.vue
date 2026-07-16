<template>
  <div class="inventory-page">
    <!-- 工具栏 -->
    <div class="inventory-card inventory-toolbar-card">
      <div class="inventory-toolbar-row">
        <div class="inventory-search">
          <Search :size="16" />
          <input v-model="searchKeyword" placeholder="输入公司编号搜索" @keydown.enter="onSearch" />
        </div>
        <button class="inventory-btn inventory-btn-primary" @click="onSearch">
          <Search :size="14" /> 查询
        </button>
        <button class="inventory-btn inventory-btn-ghost" @click="clearSearch">
          <X :size="14" /> 清除
        </button>
        <span class="inventory-toolbar-sep"></span>
        <button :class="['inventory-btn', filterOnDisplay === 'zero' ? 'inventory-btn-primary' : 'inventory-btn-ghost']" @click="toggleFilter('zero')">
          在展为 0
        </button>
        <button :class="['inventory-btn', filterOnDisplay === 'negative' ? 'inventory-btn-danger' : 'inventory-btn-ghost']" @click="toggleFilter('negative')">
          在展为负数
        </button>
        <span class="inventory-toolbar-sep"></span>
        <button class="inventory-btn inventory-btn-primary adv-search-btn" @click="openAdvancedSearch">
          综合查询
          <span v-if="hasAdvancedSearch" class="adv-badge">●</span>
        </button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="inventory-card inventory-table-card">
      <div ref="tableWrapRef" class="inventory-table-wrap">
        <vxe-grid
          v-if="prefReady"
          ref="gridRef"
          :id="gridStorageKey"
          :columns="columns"
          :data="filteredTableData"
          :loading="tableLoading"
          :height="tableWrapHeight"
          :toolbar-config="{ custom: true, zoom: true }"
          :custom-config="{ storage: true }"
          :column-config="{ resizable: true, maxFixedSize: 0, drag: true }"
          :row-config="{ isHover: true, isCurrent: true, keyField: '_rowKey' }"
          :row-class-name="rowClassName"
          :cell-config="{ height: 80 }"
          :sort-config="{ trigger: 'header', remote: true }"
          :scroll-x="{ enabled: true, gt: 0 }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 30, rSize: 80, rHeight: 80 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 24 }, scrollY: { gt: 0, oSize: 30, rSize: 80, rHeight: 80 } }"
          :border="true"
          :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontSize: '30px', fontWeight: 600, textAlign: 'center' }"
          :cell-style="gridCellStyle"
          @sort-change="onSortChange"
          @resizable-change="saveGridPrefs"
          @custom="onCustomChange"
          @column-dragstart="onColumnDragStart"
          @column-dragend="onColumnDragEnd"
        >
          <template #image="{ row }">
            <img v-if="row.image" :src="row.image" class="inv-thumb" @error="e => e.target.style.display='none'" />
            <span v-else class="inv-no-img">-</span>
          </template>
          <template #source="{ row }">
            <span class="source-tag" :class="'source-' + getSourceCode(row.codeName).toLowerCase()">{{ getSourceLabel(row.codeName) }}</span>
          </template>
          <template #col_action="{ row }">
            <button class="inventory-btn inventory-btn-primary" style="padding:10px 32px;font-size:20px;font-weight:600" @click="showDetail(row)">详情</button>
          </template>
        </vxe-grid>
      </div>
      <div class="inventory-statusbar">
        <div class="inventory-status-info">
          共 <strong>{{ totalRecords }}</strong> 条 · 在展合计 <strong>{{ totalOnDisplay }}</strong>
        </div>
        <div class="inventory-pagination">
          <span class="inventory-page-size-label">每页</span>
          <select class="inventory-page-size-select" v-model.number="pageSize">
            <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
          <span class="inventory-page-size-label">条</span>
          <button class="inventory-btn inventory-btn-ghost" :disabled="currentPage <= 1" @click="goPage(1)">
            <ChevronsLeft :size="14" />
          </button>
          <button class="inventory-btn inventory-btn-ghost" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">
            <ChevronLeft :size="14" />
          </button>
          <span class="inventory-page-text">{{ currentPage }} / {{ totalPages }}</span>
          <button class="inventory-btn inventory-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">
            <ChevronRight :size="14" />
          </button>
          <button class="inventory-btn inventory-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(totalPages)">
            <ChevronsRight :size="14" />
          </button>
        </div>
      </div>
    </div>

    <!-- 综合查询弹窗 -->
    <div v-if="advancedSearchVisible" class="modal-overlay" @click.self="closeAdvancedSearch">
      <div class="modal-dialog" style="max-width:560px">
        <div class="modal-header">
          <strong>综合查询</strong>
          <button class="modal-close-btn" @click="closeAdvancedSearch"><X :size="16" /></button>
        </div>
        <div class="modal-body">
          <div class="fm-row fm-row-2">
            <div class="fm-field">
              <span class="fm-label">摊位号</span>
              <input class="fm-input" v-model="advBoothNo" placeholder="输入摊位号" />
            </div>
            <div class="fm-field">
              <span class="fm-label">手机号</span>
              <input class="fm-input" v-model="advMobile" placeholder="输入手机号" />
            </div>
          </div>
          <div class="fm-row fm-row-2">
            <div class="fm-field">
              <span class="fm-label">厂商名称</span>
              <input class="fm-input" v-model="advManufacturerName" placeholder="输入厂商名称" />
            </div>
            <div class="fm-field">
              <span class="fm-label">楼层</span>
              <input class="fm-input" v-model="advFloor" placeholder="输入楼层" />
            </div>
          </div>
        </div>
        <div class="form-modal-footer">
          <button class="inventory-btn inventory-btn-ghost" @click="closeAdvancedSearch">取消</button>
          <button class="inventory-btn inventory-btn-primary" @click="onAdvancedSearch">查询</button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="detailVisible" class="inv-modal-mask" @click.self="detailVisible = false">
      <div class="inv-modal">
        <div class="inv-modal-header">
          <span class="inv-modal-title">{{ detailCompanyCode }} - 出入库明细</span>
          <button class="inv-modal-close" @click="detailVisible = false"><X :size="28" /></button>
        </div>
        <div class="inv-modal-toolbar">
          <div class="inv-modal-tabs">
            <button :class="['inv-tab', { 'inv-tab-active': detailTab === 'inbound' }]" @click="detailTab = 'inbound'">
              入库明细
              <span class="inv-tab-count">{{ filteredInbound.length }}</span>
            </button>
            <button :class="['inv-tab', { 'inv-tab-active': detailTab === 'outbound' }]" @click="detailTab = 'outbound'">
              出库明细
              <span class="inv-tab-count">{{ filteredOutbound.length }}</span>
            </button>
          </div>
          <div class="inv-modal-filter">
            <div class="inv-modal-date-range">
              <VxeDatePicker v-model="detailStartDate" type="date" value-type="string" format="yyyy-MM-dd" placeholder="开始日期" transfer clearable class="inv-modal-date-picker" />
              <span class="inv-modal-date-sep">~</span>
              <VxeDatePicker v-model="detailEndDate" type="date" value-type="string" format="yyyy-MM-dd" placeholder="结束日期" transfer clearable class="inv-modal-date-picker" />
            </div>
            <button class="inv-modal-clear-btn" @click="clearDetailFilter">清除筛选</button>
          </div>
        </div>
        <div class="inv-modal-body" v-show="detailTab === 'inbound'">
          <vxe-table
            ref="inboundTableRef"
            id="inventory-detail-inbound"
            :data="filteredInbound"
            border
            :row-config="{ height: 60 }"
            :header-row-config="{ height: 150 }"
            :column-config="{ resizable: true, drag: true }"
            :sort-config="{ trigger: 'header', showIcon: true }"
            :custom-config="{ storage: true }"
            :header-cell-style="{ background: '#f8fafc', color: '#1d1d1f', fontSize: '18px', fontWeight: 600, textAlign: 'center' }"
            :cell-style="{ textAlign: 'center', fontSize: '15px' }"
            @resizable-change="onDetailResizableChange('inbound')"
            @column-dragend="onDetailColumnDragEnd('inbound')"
          >
            <vxe-column type="seq" title="序号" width="60" />
            <vxe-column field="inventoryCode" title="库存编号" min-width="180" show-overflow sortable />
            <vxe-column field="codeName" title="来源" width="110">
              <template #default="{ row }">{{ getDetailSourceLabel(row.codeName, true) }}</template>
            </vxe-column>
            <vxe-column field="stockInTime" title="入库时间" min-width="200" sortable>
              <template #default="{ row }">{{ formatTime(row.stockInTime) }}</template>
            </vxe-column>
            <vxe-column field="factoryNo" title="出厂货号" width="140" show-overflow sortable />
            <vxe-column field="sampleName" title="样品名称" width="160" show-overflow sortable />
            <vxe-column field="manufacturerName" title="厂商名称" width="160" show-overflow sortable />
            <vxe-column field="boothNumber" title="摊位号" width="100" sortable />
            <vxe-column field="floor" title="楼层" width="80" sortable />
            <vxe-column field="creator" title="登记人" width="100" sortable />
          </vxe-table>
        </div>
        <div class="inv-modal-body" v-show="detailTab === 'outbound'">
          <vxe-table
            ref="outboundTableRef"
            id="inventory-detail-outbound"
            :data="filteredOutbound"
            border
            :row-config="{ height: 60 }"
            :header-row-config="{ height: 100 }"
            :column-config="{ resizable: true, drag: true }"
            :sort-config="{ trigger: 'header', showIcon: true }"
            :custom-config="{ storage: true }"
            :header-cell-style="{ background: '#f8fafc', color: '#1d1d1f', fontSize: '18px', fontWeight: 600, textAlign: 'center' }"
            :cell-style="{ textAlign: 'center', fontSize: '15px' }"
            @resizable-change="onDetailResizableChange('outbound')"
            @column-dragend="onDetailColumnDragEnd('outbound')"
          >
            <vxe-column type="seq" title="序号" width="60" />
            <vxe-column field="outboundCode" title="出库编号" min-width="180" show-overflow sortable />
            <vxe-column field="codeName" title="来源" width="110">
              <template #default="{ row }">{{ getDetailSourceLabel(row.codeName, false) }}</template>
            </vxe-column>
            <vxe-column field="stockOutTime" title="出库时间" min-width="200" sortable>
              <template #default="{ row }">{{ formatTime(row.stockOutTime) }}</template>
            </vxe-column>
            <vxe-column field="factoryNo" title="出厂货号" width="140" show-overflow sortable />
            <vxe-column field="sampleName" title="样品名称" width="160" show-overflow sortable />
            <vxe-column field="manufacturerName" title="厂商名称" width="160" show-overflow sortable />
            <vxe-column field="boothNumber" title="摊位号" width="100" sortable />
            <vxe-column field="floor" title="楼层" width="80" sortable />
            <vxe-column field="creator" title="登记人" width="100" sortable />
          </vxe-table>
        </div>
        <div class="inv-modal-footer">
          <span>共 <strong>{{ detailTab === 'inbound' ? filteredInbound.length : filteredOutbound.length }}</strong> 条记录</span>
          <span class="inv-modal-footer-hint">可拖拽表头调整列宽和排序，点击表头排序</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onActivated, nextTick, watch } from 'vue'
import {
  Search, X,
  ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight
} from 'lucide-vue-next'
import { api } from '@/api'
import { useGridPrefSync } from '@/composables/useGridPrefSync'
import '@/styles/manufacturer-export.css'

// ========== 状态 ==========
const gridRef = ref(null)
const tableWrapRef = ref(null)
const tableWrapHeight = ref(600)
let resizeObserver = null
let resizeRafId = null
let lastObservedHeight = 0

// ========== 列区域选取 ==========
const areaDragging = ref(false)
const areaDragField = ref('')
const areaDragColId = ref('')
const areaDragStartRowId = ref(null)
const areaDragEndRowId = ref(null)
const areaDragMoved = ref(false)
const areaDragStartY = ref(0)
const areaSelectedColumn = ref('')
const areaSelectedColId = ref('')
const areaSelectedStartRowId = ref(null)
const areaSelectedEndRowId = ref(null)
const areaRenderTick = ref(0)
const isColumnDragging = ref(false)
const extDragging = ref(false)
let areaHandleEl = null
let _areaRaf = null
const areaSelectedCount = computed(() => {
  if (!areaSelectedColumn.value) return 0
  const data = filteredTableData.value
  const sIdx = data.findIndex(r => String(r._rowKey) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r._rowKey) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return 0
  return Math.abs(eIdx - sIdx) + 1
})

// ── 拖拽选区预计算 Set（O(1) 查找代替 O(n) findIndex）──
const areaDragRowIdSet = computed(() => {
  if (!areaDragging.value || !areaDragField.value) return null
  const data = filteredTableData.value
  const sIdx = data.findIndex(r => String(r._rowKey) === String(areaDragStartRowId.value))
  const eIdx = data.findIndex(r => String(r._rowKey) === String(areaDragEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return null
  const min = Math.min(sIdx, eIdx); const max = Math.max(sIdx, eIdx)
  const set = new Set()
  for (let i = min; i <= max; i++) set.add(data[i]._rowKey)
  return set
})

const areaSelectedRowIdSet = computed(() => {
  if (!areaSelectedColumn.value) return null
  const data = filteredTableData.value
  const sIdx = data.findIndex(r => String(r._rowKey) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r._rowKey) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return null
  const min = Math.min(sIdx, eIdx); const max = Math.max(sIdx, eIdx)
  const set = new Set()
  for (let i = min; i <= max; i++) set.add(data[i]._rowKey)
  return set
})

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(200)
const pageSizeOptions = [100, 200, 500, 1000, 2000]
const totalRecords = ref(0)
const tableLoading = ref(false)

const sortField = ref('')
const sortOrder = ref('')

const tableData = ref([])
const filterOnDisplay = ref(null)

// ========== 综合查询 ==========
const advancedSearchVisible = ref(false)
const advBoothNo = ref('')
const advMobile = ref('')
const advManufacturerName = ref('')
const advFloor = ref('')
const activeBoothNo = ref('')
const activeMobile = ref('')
const activeManufacturerName = ref('')
const activeFloor = ref('')
const hasAdvancedSearch = computed(() => {
  return !!(activeBoothNo.value || activeMobile.value || activeManufacturerName.value || activeFloor.value)
})

const totalPages = computed(() => Math.max(1, Math.ceil(totalRecords.value / pageSize.value)))

const rowClassName = ({ row }) => {
  if (row.onDisplayCount != null && row.onDisplayCount < 0) {
    return 'row-negative'
  }
  return ''
}

const totalOnDisplay = computed(() => {
  return tableData.value.reduce((sum, row) => sum + (row.onDisplayCount || 0), 0)
})

// ========== 筛选 ==========
const toggleFilter = (type) => {
  filterOnDisplay.value = filterOnDisplay.value === type ? null : type
}

const filteredTableData = computed(() => {
  if (!filterOnDisplay.value) return tableData.value
  if (filterOnDisplay.value === 'zero') {
    return tableData.value.filter(row => (row.onDisplayCount ?? 0) === 0)
  }
  if (filterOnDisplay.value === 'negative') {
    return tableData.value.filter(row => (row.onDisplayCount ?? 0) < 0)
  }
  return tableData.value
})

// ========== 列定义 ==========
const columns = [
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'image', title: '图片', width: 100, slots: { default: 'image' } },
  { field: 'companyCode', title: '公司编号', minWidth: 130, showOverflow: true, sortable: true },
  { field: 'codeName', title: '来源', width: 110, slots: { default: 'source' } },
  { field: 'factoryNo', title: '出厂货号', minWidth: 120, showOverflow: true },
  { field: 'sampleName', title: '样品名称', minWidth: 140, showOverflow: true },
  { field: 'chinesePackage', title: '中文包装', minWidth: 120 },
  { field: 'boothNumber', title: '摊位号', minWidth: 100 },
  { field: 'manufacturerName', title: '厂商名称', minWidth: 140, showOverflow: true },
  { field: 'mobile', title: '手机', minWidth: 120 },
  { field: 'floor', title: '楼层', width: 80 },
  { field: 'stockInTime', title: '入库时间', minWidth: 160, sortable: true },
  { field: 'lastOutboundTime', title: '出库时间', minWidth: 160, sortable: true },
  { field: 'inboundCount', title: '入库次数', width: 90, sortable: true },
  { field: 'outboundCount', title: '出库次数', width: 90, sortable: true },
  { field: 'onDisplayCount', title: '在展数量', width: 100, sortable: true },
  { field: '__action', title: '操作', width: 120, fixed: 'right', slots: { default: 'col_action' } }
]

// ========== 列设置同步 ==========
const { fullKey: gridStorageKey, saveToBackend: saveGridPrefs, ready: prefReady } = useGridPrefSync(gridRef, 'inventory-summary', columns)

const onCustomChange = ({ type }) => {
  if (type === 'confirm' || type === 'reset') setTimeout(() => saveGridPrefs(), 50)
}

const onColumnDragStart = () => {
  isColumnDragging.value = true
}

const onColumnDragEnd = () => {
  isColumnDragging.value = false
  setTimeout(() => saveGridPrefs(), 100)
}

// ========== 操作 ==========
const onSearch = () => {
  currentPage.value = 1
  loadData()
}

const clearSearch = () => {
  searchKeyword.value = ''
  filterOnDisplay.value = null
  currentPage.value = 1
  loadData()
}

// ========== 综合查询方法 ==========
const openAdvancedSearch = () => {
  advancedSearchVisible.value = true
}

const closeAdvancedSearch = () => {
  advancedSearchVisible.value = false
}

const onAdvancedSearch = () => {
  activeBoothNo.value = advBoothNo.value
  activeMobile.value = advMobile.value
  activeManufacturerName.value = advManufacturerName.value
  activeFloor.value = advFloor.value
  advancedSearchVisible.value = false
  currentPage.value = 1
  loadData()
}

const clearAdvancedSearch = () => {
  advBoothNo.value = ''
  advMobile.value = ''
  advManufacturerName.value = ''
  advFloor.value = ''
  activeBoothNo.value = ''
  activeMobile.value = ''
  activeManufacturerName.value = ''
  activeFloor.value = ''
  currentPage.value = 1
  loadData()
}

watch(pageSize, () => {
  currentPage.value = 1
  loadData()
})

const goPage = (page) => {
  currentPage.value = page
  loadData()
}

const onSortChange = ({ field, order }) => {
  sortField.value = field || ''
  sortOrder.value = order || ''
  currentPage.value = 1
  loadData()
}

// ========== 详情弹窗 ==========
const detailVisible = ref(false)
const detailCompanyCode = ref('')
const detailTab = ref('inbound')
const detailInbound = ref([])
const detailOutbound = ref([])
const detailStartDate = ref('')
const detailEndDate = ref('')
const inboundTableRef = ref(null)
const outboundTableRef = ref(null)

const formatTime = (val) => {
  if (!val) return '-'
  return String(val).replace('T', ' ')
}

const SOURCE_MAP = { Y: '择样', R: '厂商入库', C: '厂商出库' }
const getSourceCode = (codeName) => {
  if (!codeName) return ''
  const s = String(codeName)
  return s.slice(-1).toUpperCase()
}
const getSourceLabel = (codeName) => SOURCE_MAP[getSourceCode(codeName)] || codeName || '-'
const getDetailSourceLabel = (codeName, isInbound) => {
  const code = getSourceCode(codeName)
  if (code === 'Y') return isInbound ? '择样入库' : '择样出库'
  return SOURCE_MAP[code] || codeName || '-'
}

const filteredInbound = computed(() => {
  let list = [...detailInbound.value]
  const sd = detailStartDate.value
  const ed = detailEndDate.value
  if (sd) {
    list = list.filter(r => r.stockInTime && String(r.stockInTime) >= sd)
  }
  if (ed) {
    list = list.filter(r => r.stockInTime && String(r.stockInTime).substring(0, 10) <= ed)
  }
  // 按入库时间降序
  list.sort((a, b) => {
    const ta = a.stockInTime || ''
    const tb = b.stockInTime || ''
    return tb.localeCompare(ta)
  })
  return list
})

const filteredOutbound = computed(() => {
  let list = [...detailOutbound.value]
  const sd = detailStartDate.value
  const ed = detailEndDate.value
  if (sd) {
    list = list.filter(r => r.stockOutTime && String(r.stockOutTime) >= sd)
  }
  if (ed) {
    list = list.filter(r => r.stockOutTime && String(r.stockOutTime).substring(0, 10) <= ed)
  }
  // 按出库时间降序
  list.sort((a, b) => {
    const ta = a.stockOutTime || ''
    const tb = b.stockOutTime || ''
    return tb.localeCompare(ta)
  })
  return list
})

const showDetail = async (row) => {
  detailCompanyCode.value = row.companyCode
  detailVisible.value = true
  detailInbound.value = []
  detailOutbound.value = []
  detailStartDate.value = ''
  detailEndDate.value = ''
  try {
    const res = await api(`/inventory/detail/${encodeURIComponent(row.companyCode)}`)
    const data = res.data || res
    detailInbound.value = Array.isArray(data.inbound) ? data.inbound : []
    detailOutbound.value = Array.isArray(data.outbound) ? data.outbound : []
  } catch (e) {
    console.error('加载明细失败:', e)
    detailInbound.value = []
    detailOutbound.value = []
  }
}

const clearDetailFilter = () => {
  detailStartDate.value = ''
  detailEndDate.value = ''
}

const onDetailResizableChange = (table) => {
  const tableRef = table === 'inbound' ? inboundTableRef.value : outboundTableRef.value
  if (tableRef) {
    setTimeout(() => tableRef.saveCustomStatus?.(), 100)
  }
}

const onDetailColumnDragEnd = (table) => {
  const tableRef = table === 'inbound' ? inboundTableRef.value : outboundTableRef.value
  if (tableRef) {
    setTimeout(() => tableRef.saveCustomStatus?.(), 100)
  }
}

// ========== 数据加载 ==========
const loadData = async () => {
  try {
    tableLoading.value = true
    const params = [`current=${currentPage.value}`, `size=${pageSize.value}`]
    if (sortField.value) { params.push(`sortField=${sortField.value}`); params.push(`sortOrder=${sortOrder.value}`) }
    if (searchKeyword.value) params.push(`keyword=${encodeURIComponent(searchKeyword.value)}`)
    if (activeBoothNo.value) params.push(`boothNo=${encodeURIComponent(activeBoothNo.value)}`)
    if (activeMobile.value) params.push(`mobile=${encodeURIComponent(activeMobile.value)}`)
    if (activeManufacturerName.value) params.push(`manufacturerName=${encodeURIComponent(activeManufacturerName.value)}`)
    if (activeFloor.value) params.push(`floor=${encodeURIComponent(activeFloor.value)}`)
    const res = await api(`/inventory/summary?${params.join('&')}`)
    const result = res.data || res || {}
    const records = result.records || result.list
    const rawData = Array.isArray(records) ? records : (Array.isArray(result) ? result : [])
    // 为每行生成唯一 _rowKey，避免 companyCode 重复导致 vxe-grid duplicate key 警告
    const offset = (currentPage.value - 1) * pageSize.value
    tableData.value = rawData.map((row, i) => ({ ...row, _rowKey: `row-${offset + i}` }))
    totalRecords.value = result.total || tableData.value.length
  } catch (e) {
    console.error('加载数据失败:', e)
    tableData.value = []
    totalRecords.value = 0
  } finally {
    tableLoading.value = false
  }
}

// ========== 生命周期 ==========
// 预定义的样式常量（避免每次调用创建新对象）
const _styleDrag = { textAlign: 'center', fontSize: '26px', background: '#e3f2fd', outline: '2px solid #4285f4', outlineOffset: '-2px' }
const _styleSelected = { textAlign: 'center', fontSize: '26px', background: '#dceefb', outline: '2px solid #4285f4', outlineOffset: '-2px' }
const _styleDefault = { textAlign: 'center', fontSize: '26px' }

const gridCellStyle = ({ row, column }) => {
  if (isColumnDragging.value) return _styleDefault
  if (!areaDragging.value && !areaSelectedColumn.value) return _styleDefault
  void areaRenderTick.value
  const field = (column && (column.field || column.type)) || ''
  // 拖拽中的高亮 — O(1) Set 查找
  if (areaDragging.value && field === areaDragField.value) {
    const set = areaDragRowIdSet.value
    if (set && row && set.has(row._rowKey)) return _styleDrag
  }
  // 已确认选区高亮 — O(1) Set 查找
  if (areaSelectedColumn.value && field === areaSelectedColumn.value) {
    const set = areaSelectedRowIdSet.value
    if (set && row && set.has(row._rowKey)) return _styleSelected
  }
  return _styleDefault
}

const getRowIdAndField = (el) => {
  const td = el.closest('td.vxe-body--column')
  if (!td) { const wrapper = el.closest('.vxe-body-cell--wrapper'); if (!wrapper) return null; const cid = wrapper.getAttribute('colid'); const rid = wrapper.getAttribute('rowid'); if (!cid || !rid) return null; return { rowId: rid, field: cid } }
  const colid = td.getAttribute('colid'); if (!colid) return null
  const tr = td.closest('tr'); if (!tr) return null
  const rowid = tr.getAttribute('rowid'); if (!rowid) return null
  return { rowId: rowid, field: colid }
}

const getFieldByColId = (colId) => { const grid = gridRef.value; if (!grid) return colId; const cols = grid.getColumns() || []; const col = cols.find(c => c.id === colId); return col ? col.field : colId }

const onTableWrapMouseDown = (e) => {
  if (e.button !== 0) return
  if (e.target.closest('.inv-area-handle')) return
  if (!tableWrapRef.value?.contains(e.target)) return
  const info = getRowIdAndField(e.target); if (!info) return
  areaDragStartRowId.value = info.rowId; areaDragEndRowId.value = info.rowId
  areaDragColId.value = info.field; areaDragField.value = getFieldByColId(info.field)
  areaDragging.value = false; areaDragMoved.value = false; areaDragStartY.value = e.clientY
  areaSelectedColumn.value = ''; areaSelectedColId.value = ''
  areaSelectedStartRowId.value = null; areaSelectedEndRowId.value = null
  areaRenderTick.value++
  document.addEventListener('mousemove', onDocMouseMove); document.addEventListener('mouseup', onDocMouseUp)
  e.preventDefault()
}

const onDocMouseMove = (e) => {
  if (!areaDragging.value && !areaDragMoved.value) { if (Math.abs(e.clientY - areaDragStartY.value) < 6) return; areaDragging.value = true; areaDragMoved.value = true; document.body.classList.add('inv-area-selecting') }
  if (!areaDragging.value) return
  const target = document.elementFromPoint(e.clientX, e.clientY); if (!target) return
  const info = getRowIdAndField(target); if (!info || info.field !== areaDragColId.value) return
  areaDragEndRowId.value = info.rowId
  if (!_areaRaf) { _areaRaf = requestAnimationFrame(() => { _areaRaf = null; areaRenderTick.value++ }) }
}

const onDocMouseUp = () => {
  document.removeEventListener('mousemove', onDocMouseMove); document.removeEventListener('mouseup', onDocMouseUp)
  document.body.classList.remove('inv-area-selecting')
  if (_areaRaf) { cancelAnimationFrame(_areaRaf); _areaRaf = null }
  if (!areaDragging.value) { if (areaDragField.value) { areaSelectedColumn.value = areaDragField.value; areaSelectedColId.value = areaDragColId.value; areaSelectedStartRowId.value = areaDragStartRowId.value; areaSelectedEndRowId.value = areaDragEndRowId.value; areaRenderTick.value++; attachAreaHandle() } return }
  areaDragging.value = false; areaSelectedColumn.value = areaDragField.value; areaSelectedColId.value = areaDragColId.value
  areaSelectedStartRowId.value = areaDragStartRowId.value; areaSelectedEndRowId.value = areaDragEndRowId.value
  areaRenderTick.value++; attachAreaHandle()
}

const clearAreaSelection = () => { removeAreaHandle(); areaSelectedColumn.value = ''; areaSelectedColId.value = ''; areaSelectedStartRowId.value = null; areaSelectedEndRowId.value = null; areaDragging.value = false; if (_areaRaf) { cancelAnimationFrame(_areaRaf); _areaRaf = null }; areaRenderTick.value++ }

const attachAreaHandle = () => {
  removeAreaHandle(); if (!areaSelectedColId.value) return
  const wrapper = tableWrapRef.value; if (!wrapper) return
  const data = filteredTableData.value
  const sIdx = data.findIndex(r => String(r._rowKey) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r._rowKey) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return
  const lastIdx = Math.max(sIdx, eIdx); const lastId = String(data[lastIdx]._rowKey)
  requestAnimationFrame(() => {
    const cellEl = wrapper.querySelector(`[rowid="${lastId}"] [colid="${areaSelectedColId.value}"]`); if (!cellEl) return
    const td = cellEl.tagName === 'TD' ? cellEl : cellEl.closest('td'); if (!td) return
    const h = document.createElement('div'); h.className = 'inv-area-handle'
    Object.assign(h.style, { position: 'absolute', right: '-6px', bottom: '-6px', width: '14px', height: '14px', background: '#4285f4', border: '2px solid #fff', borderRadius: '2px', boxShadow: '0 0 0 2px #4285f4', cursor: 'crosshair', zIndex: '10', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: '12px', fontWeight: 'bold', lineHeight: '1', userSelect: 'none' })
    h.textContent = '+'; h.addEventListener('mousedown', onHandleMouseDown); td.style.position = 'relative'; td.appendChild(h); areaHandleEl = h
  })
}

const removeAreaHandle = () => { if (areaHandleEl) { areaHandleEl.removeEventListener('mousedown', onHandleMouseDown); if (areaHandleEl.parentNode) areaHandleEl.parentNode.removeChild(areaHandleEl); areaHandleEl = null } }

const onHandleMouseDown = (e) => { e.stopPropagation(); e.preventDefault(); extDragging.value = true; document.body.classList.add('inv-area-selecting'); document.addEventListener('mousemove', onExtMouseMove); document.addEventListener('mouseup', onExtMouseUp) }

const onExtMouseMove = (e) => { if (!extDragging.value) return; const target = document.elementFromPoint(e.clientX, e.clientY); if (!target) return; const info = getRowIdAndField(target); if (!info) return; areaSelectedEndRowId.value = info.rowId; if (!_areaRaf) { _areaRaf = requestAnimationFrame(() => { _areaRaf = null; areaRenderTick.value++ }) } }

const onExtMouseUp = () => { extDragging.value = false; document.body.classList.remove('inv-area-selecting'); document.removeEventListener('mousemove', onExtMouseMove); document.removeEventListener('mouseup', onExtMouseUp); if (_areaRaf) { cancelAnimationFrame(_areaRaf); _areaRaf = null }; attachAreaHandle() }

const getAreaSelectedValues = () => {
  if (!areaSelectedColumn.value) return []
  const data = filteredTableData.value
  const sIdx = data.findIndex(r => String(r._rowKey) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r._rowKey) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return []
  const min = Math.min(sIdx, eIdx); const max = Math.max(sIdx, eIdx)
  return data.slice(min, max + 1).map(r => ({ _rowKey: r._rowKey, value: r[areaSelectedColumn.value] }))
}

const writeClipboard = (text) => { const ta = document.createElement('textarea'); ta.value = text; ta.style.cssText = 'position:absolute;left:-9999px;top:0'; document.body.appendChild(ta); ta.focus(); ta.select(); try { document.execCommand('copy'); return true } catch { return false } finally { document.body.removeChild(ta) } }

let areaCopyTextarea = null
const onAreaCopyKey = (e) => { if (!(e.ctrlKey || e.metaKey) || e.key !== 'c') return; if (!areaSelectedColumn.value) return; const vals = getAreaSelectedValues(); if (vals.length === 0) return; const text = vals.map(v => v.value != null ? String(v.value) : '').join('\n'); areaCopyTextarea = document.createElement('textarea'); areaCopyTextarea.value = text; areaCopyTextarea.style.cssText = 'position:absolute;left:-9999px;top:0'; document.body.appendChild(areaCopyTextarea); areaCopyTextarea.focus(); areaCopyTextarea.select() }

const onAreaCopyEvent = (e) => { if (!areaSelectedColumn.value) return; const vals = getAreaSelectedValues(); if (vals.length === 0) return; e.clipboardData.setData('text/plain', vals.map(v => v.value != null ? String(v.value) : '').join('\n')); e.preventDefault(); if (areaCopyTextarea && document.body.contains(areaCopyTextarea)) { document.body.removeChild(areaCopyTextarea); areaCopyTextarea = null } }

onMounted(() => {
  if (tableWrapRef.value) {
    resizeObserver = new ResizeObserver((entries) => {
      const entry = entries[0]
      if (entry) {
        const h = entry.contentRect.height
        if (h > 0 && Math.abs(h - lastObservedHeight) > 1) {
          lastObservedHeight = h
          if (resizeRafId) cancelAnimationFrame(resizeRafId)
          resizeRafId = requestAnimationFrame(() => { tableWrapHeight.value = h })
        }
      }
    })
    resizeObserver.observe(tableWrapRef.value)
    let handleScrollTimer = null
    tableWrapRef.value.addEventListener('scroll', () => { if (!areaHandleEl || !document.contains(areaHandleEl)) { if (handleScrollTimer) clearTimeout(handleScrollTimer); handleScrollTimer = setTimeout(attachAreaHandle, 150) } }, { passive: true })
  }
  document.addEventListener('click', onDocClick)
  window.addEventListener('keydown', onAreaCopyKey, true)
  document.addEventListener('copy', onAreaCopyEvent, true)
  document.addEventListener('mousedown', onTableWrapMouseDown, true)
  loadData()
})

const onDocClick = (e) => { if (areaSelectedColumn.value && tableWrapRef.value && !tableWrapRef.value.contains(e.target)) clearAreaSelection() }

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
  window.removeEventListener('keydown', onAreaCopyKey, true)
  document.removeEventListener('copy', onAreaCopyEvent, true)
  document.removeEventListener('mousedown', onTableWrapMouseDown, true)
  document.removeEventListener('mousemove', onDocMouseMove)
  document.removeEventListener('mouseup', onDocMouseUp)
  removeAreaHandle()
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
})

onActivated(() => {
  loadData()
  nextTick(() => {
    if (tableWrapRef.value) {
      const rect = tableWrapRef.value.getBoundingClientRect()
      if (rect.height > 0) tableWrapHeight.value = rect.height
    }
  })
})
</script>

<style scoped>
.inv-thumb {
  width: 76px;
  height: 76px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}
.inv-no-img {
  color: #c0c4cc;
  font-size: 14px;
}

.source-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}
.source-y { background: #e0f2fe; color: #0369a1; }
.source-r { background: #dcfce7; color: #15803d; }
.source-c { background: #fef3c7; color: #a16207; }

/* 总库存表格数据字号 */
.inventory-table-card :deep(.vxe-body--column),
.inventory-table-card :deep(.vxe-cell) {
  font-size: 26px;
}

/* 在展数量为负数的行标红 */
.inventory-table-card :deep(.row-negative) {
  background-color: #fef2f2 !important;
}

/* 详情按钮加大 */
.inventory-table-card :deep(.inventory-btn-primary) {
  height: auto !important;
  min-height: auto !important;
  padding: 10px 32px !important;
  font-size: 20px !important;
}

/* 详情按钮所在单元格允许撑开 */
.inventory-table-card :deep(.vxe-body--column.col--col_action) {
  padding: 2px 4px !important;
}

.inventory-table-card :deep(.col--col_action .vxe-cell) {
  padding: 2px 4px !important;
}

.inv-modal-mask {
  position: fixed;
  inset: 0;
  background: transparent;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.inv-modal {
  background: #fff;
  border-radius: 16px;
  width: 1500px;
  max-width: 96vw;
  height: 88vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 24px 80px rgba(0,0,0,0.25);
  overflow: hidden;
}
.inv-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
  background: #fafbfc;
}
.inv-modal-title {
    font-size: 36px;
   font-weight: 700;
   color: #1d1d1f;
 }
 .inv-modal-close {
   border: 0;
   background: none;
   cursor: pointer;
   color: #999;
   padding: 6px;
   border-radius: 6px;
   display: flex;
   align-items: center;
   justify-content: center;
   transition: all 0.15s;
 }
.inv-modal-close:hover {
  background: #f0f0f5;
  color: #333;
}

/* 工具栏区：tabs + 筛选 */
.inv-modal-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
  gap: 16px;
}
.inv-modal-tabs {
  display: flex;
  gap: 0;
}
.inv-tab {
   padding: 14px 24px;
   font-size: 28px;
    font-weight: 500;
   color: #6b7280;
   background: none;
   border: none;
   border-bottom: 2px solid transparent;
   margin-bottom: -1px;
   cursor: pointer;
   transition: color 0.2s, border-color 0.2s;
   display: flex;
   align-items: center;
   gap: 8px;
 }
.inv-tab:hover { color: #374151; }
.inv-tab-active {
  color: #2563eb;
  border-bottom-color: #2563eb;
  font-weight: 600;
}
.inv-tab-count {
   display: inline-flex;
   align-items: center;
   justify-content: center;
   min-width: 24px;
   height: 24px;
   padding: 0 8px;
   border-radius: 12px;
   font-size: 20px;
  color: #2563eb;
}
.inv-tab-active .inv-tab-count {
  background: #2563eb;
  color: #fff;
}

.inv-modal-filter {
   display: flex;
   align-items: center;
   gap: 10px;
   flex-shrink: 0;
 }
 .inv-modal-date-range {
  display: flex;
  align-items: center;
  gap: 4px;
}
.inv-modal-date-picker {
   width: 260px;
   display: inline-flex;
   align-items: center;
 }
 .inv-modal-date-picker :deep(.vxe-date-picker) {
   height: 80px !important;
   min-height: 80px !important;
   width: 100% !important;
   font-size: 26px !important;
   border-radius: 12px !important;
   border-color: #d1d5db !important;
   box-sizing: border-box !important;
   align-items: center !important;
 }
 .inv-modal-date-picker :deep(.vxe-date-picker--inner) {
   font-size: 26px !important;
   height: 80px !important;
   line-height: 80px !important;
   padding-left: 16px !important;
 }
 .inv-modal-date-picker :deep(.vxe-date-picker--suffix) {
   height: 80px !important;
   display: flex !important;
   align-items: center !important;
 }
 .inv-modal-date-picker :deep(.vxe-date-picker--suffix-icon),
 .inv-modal-date-picker :deep(.vxe-date-picker--clear-icon),
 .inv-modal-date-picker :deep(.vxe-date-picker--control-icon) {
   font-size: 28px !important;
   padding-right: 14px !important;
   line-height: 1 !important;
   display: flex !important;
   align-items: center !important;
   height: 100% !important;
 }
 .inv-modal-date-sep {
      font-size: 26px;
      color: #999;
    }
 
 .inv-modal-clear-btn {
    height: 48px;
    padding: 0 20px;
    border-radius: 10px;
     border: 1px solid #d1d5db;
     background: #fff;
     color: #6b7280;
     font-size: 24px;
     cursor: pointer;
     white-space: nowrap;
     transition: all 0.15s;
     flex-shrink: 0;
     display: inline-flex;
     align-items: center;
     box-sizing: border-box;
     line-height: 1;
   }
 .inv-modal-clear-btn:hover {
   border-color: #ef4444;
   color: #ef4444;
   background: #fef2f2;
 }

 .inv-modal-body {
  padding: 0;
  overflow: auto;
  flex: 1;
  display: flex;
}
.inv-modal-body .vxe-table {
  height: 100%;
}

.inv-modal-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 24px;
    border-top: 1px solid #e5e7eb;
    flex-shrink: 0;
    font-size: 24px;
    color: #6b7280;
    background: #fafbfc;
  }
  .inv-modal-footer strong {
    color: #1d1d1f;
    font-weight: 700;
  }
  .inv-modal-footer-hint {
    color: #b0b7c3;
    font-size: 22px;
  }

/* 综合查询按钮角标 */
.adv-search-btn {
  position: relative;
}
.adv-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  font-size: 10px;
  color: #ff3b30;
  line-height: 1;
  pointer-events: none;
}
</style>

<style>
/* 日期面板等比放大 */
.vxe-date-picker--panel .vxe-date-picker--layout-all-wrapper {
  transform: scale(1.4);
  transform-origin: top left;
  margin-bottom: 60px;
  margin-right: 100px;
}
</style>
