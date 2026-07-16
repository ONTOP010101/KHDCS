<template>
  <div v-if="currentView === 'list'" class="gallery-page">
    <div class="gallery-card gallery-form-card" :class="{ expanded: formExpanded }" v-show="formVisible">
      <div class="gallery-form-top">
        <div class="gallery-form-title">
        </div>
        <div class="gallery-form-actions" style="margin-right:180px">
          <button class="gallery-btn gallery-btn-ghost" title="重置" @click="resetForm">
            <RotateCcw :size="14" />
          </button>
          <button class="gallery-btn gallery-btn-ghost" title="字段设置" @click="toggleFieldSettings">
            <Settings :size="14" />
          </button>
          <button class="gallery-btn gallery-btn-ghost" :title="formExpanded ? '收起' : '展开'" @click="formExpanded = !formExpanded">
            <component :is="formExpanded ? ChevronsUp : ChevronsDown" :size="14" />
          </button>
          <button class="gallery-btn gallery-btn-ghost" :title="formVisible ? '隐藏展示区' : '显示展示区'" @click="formVisible = !formVisible">
            <EyeOff v-if="formVisible" :size="14" />
            <Eye v-else :size="14" />
          </button>
        </div>
      </div>

      <div v-if="showFieldSettings" class="gallery-field-settings">
        <div class="field-settings-header">
          <span class="field-settings-title">字段显示设置</span>
          <button class="field-settings-close" @click="showFieldSettings = false">
            <X :size="14" />
          </button>
        </div>
        <div class="field-settings-grid">
          <label v-for="f in galleryFormFields" :key="f.key" class="field-settings-chip">
            <input type="checkbox" v-model="fieldVisible[f.key]" />
            {{ f.label }}
          </label>
        </div>
      </div>

      <div class="gallery-form-body">
        <div class="gallery-form-scroll">
          <div class="gallery-form-grid">
            <template v-for="f in visibleFormFields" :key="f.key">
              <div class="gallery-form-field" :style="f.width ? { flex: '0 0 auto' } : {}">
                <label class="gallery-form-label" :style="{ ...(f.labelWidth ? { flex: '0 0 ' + f.labelWidth + 'px' } : {}), ...(f.labelJustify ? { textAlign: 'justify', textAlignLast: 'justify' } : {}), ...(f.color ? { color: f.color } : {}) }">{{ f.label }}</label>
                <input
                  class="gallery-form-input"
                  :readonly="formMode === 'readonly'"
                  :placeholder="formMode === 'readonly' ? '' : f.label"
                  :title="formData[f.key] || ''"
                  :style="{ ...(f.width ? { flex: '0 0 ' + f.width + 'px' } : {}), ...(f.color ? { color: f.color } : {}) }"
                  v-model="formData[f.key]"
                />
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div class="gallery-card gallery-toolbar-card">
      <div class="gallery-toolbar-row">
        <!-- 展示区隐藏时，在搜索框左侧显示恢复按钮 -->
        <button v-if="!formVisible" class="gallery-btn gallery-btn-primary" style="font-size:11px;height:30px;flex-shrink:0;margin-right:6px" @click="formVisible = true" title="显示展示区">
          <Eye :size="13" />
        </button>
        <div class="gallery-search">
          <Search :size="14" />
          <input v-model="searchKeyword" placeholder="搜索代号、客户名称..." @keyup.enter="onQuery" />
        </div>
        <input type="date" class="gallery-date-input" v-model="dateFrom" />
        <span class="gallery-date-separator">至</span>
        <input type="date" class="gallery-date-input" v-model="dateTo" />
        <button class="gallery-btn gallery-btn-primary" @click="onQuery">
          <Search :size="14" /> 查询
        </button>
        <div class="toolbar-sep"></div>
        <button class="gallery-btn gallery-btn-primary" @click="addCode">
          <Plus :size="14" /> 添加代号
        </button>
        <button v-if="formMode === 'edit' || formMode === 'add'" class="gallery-btn gallery-btn-primary" @click="saveForm">
          <Save :size="14" /> 保存
        </button>
        <button v-if="formMode === 'edit' || formMode === 'add'" class="gallery-btn gallery-btn-ghost" @click="cancelEdit">
          <X :size="14" /> 取消
        </button>
        <button class="gallery-btn gallery-btn-danger" :disabled="selectedIds.length === 0" @click="deleteCode">
          <Trash2 :size="14" /> 删除代号
        </button>
        <button class="gallery-btn gallery-btn-ghost" @click="exportList">
          <Download :size="14" /> 导出列表
        </button>
      </div>
    </div>

    <div class="gallery-card gallery-table-card">
      <div ref="tableWrapRef" class="gallery-table-wrap">
        <vxe-grid
          ref="gridRef"
          :columns="listColumns"
          :data="pagedData"
          :loading="tableLoading"
          :height="tableWrapHeight"
          :toolbar-config="{ custom: true, zoom: true }"
          :column-config="{ resizable: true, drag: true }"
          :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
          :checkbox-config="{ highlight: true, checkField: 'checkbox' }"
          :cell-config="{ height: 44 }"
          :sort-config="{ trigger: 'header', remote: true }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 0, rSize: 60, rHeight: 44 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 24 }, scrollY: { gt: 0, oSize: 0, rSize: 60, rHeight: 44 } }"
          :border="true"
          :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontWeight: 600, textAlign: 'center' }"
          :cell-style="{ textAlign: 'center' }"
          @cell-click="onCellClick"
          @sort-change="onSortChange"
        >
          <template #col_code="{ row }">
            <span style="color:#0066cc;cursor:pointer;text-decoration:underline;" @click.stop="openDetail(row)">{{ row.code }}</span>
          </template>
          <template #col_status="{ row }">
            <span class="gallery-encrypt-badge" :class="row.encrypted ? 'locked' : 'open'">
              {{ row.encrypted ? '加密' : '未加密' }}
            </span>
          </template>
          <template #col_action="{ row }">
            <div class="gallery-row-actions">
              <button class="gallery-row-btn" @click.stop="openDetail(row)">
                <Eye :size="14" /> 详情
              </button>
              <button class="gallery-row-btn" @click.stop="editRow(row)">
                <Pencil :size="14" /> 编辑
              </button>
              <button class="gallery-row-btn danger" @click.stop="deleteRow(row)">
                <Trash2 :size="14" /> 删除
              </button>
              <button v-if="!row.encrypted" class="gallery-row-btn lock" @click.stop="encryptRow(row)">
                <Lock :size="14" /> 加密
              </button>
              <button v-else class="gallery-row-btn unlock" @click.stop="decryptRow(row)">
                <Unlock :size="14" /> 解密
              </button>
            </div>
          </template>
        </vxe-grid>
      </div>
      <div class="gallery-statusbar">
        <div class="gallery-status-info">
          共 <strong>{{ tableData.length }}</strong> 条
        </div>
        <div class="gallery-pagination">
          <span class="gallery-page-size-label">每页</span>
          <select class="gallery-page-size-select" v-model.number="pageSize">
            <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
          <span class="gallery-page-size-label">条</span>
          <button class="gallery-btn gallery-btn-ghost" :disabled="currentPage <= 1" @click="goPage(1)">
            <ChevronsLeft :size="14" />
          </button>
          <button class="gallery-btn gallery-btn-ghost" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">
            <ChevronLeft :size="14" />
          </button>
          <span class="gallery-page-text">{{ currentPage }} / {{ totalPages }}</span>
          <button class="gallery-btn gallery-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">
            <ChevronRight :size="14" />
          </button>
          <button class="gallery-btn gallery-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(totalPages)">
            <ChevronsRight :size="14" />
          </button>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="gallery-detail-page">
    <div class="gallery-card gallery-detail-top">
      <div class="gallery-detail-left">
        <button class="gallery-btn gallery-btn-ghost" @click="currentView = 'list'">
          <ArrowLeft :size="14" /> 返回
        </button>
        <div class="gallery-detail-context">
          <span class="gallery-detail-chip"><Hash :size="12" /> {{ detailData.code }}</span>
          <span class="gallery-detail-chip"><Building2 :size="12" /> {{ detailData.customer }}</span>
          <span class="gallery-detail-chip"><Camera :size="12" /> {{ detailData.photographer }}</span>
        </div>
      </div>
      <div class="gallery-detail-right">
        <button class="gallery-btn gallery-btn-ghost" :class="{ 'gallery-btn-primary': thumbSize === 'small' }" @click="thumbSize = 'small'">
          <LayoutGrid :size="14" /> 小图
        </button>
        <button class="gallery-btn gallery-btn-ghost" :class="{ 'gallery-btn-primary': thumbSize === 'medium' }" @click="thumbSize = 'medium'">
          <LayoutGrid :size="14" /> 中图
        </button>
        <button class="gallery-btn gallery-btn-ghost" :class="{ 'gallery-btn-primary': thumbSize === 'large' }" @click="thumbSize = 'large'">
          <Maximize2 :size="14" /> 大图
        </button>
        <button class="gallery-btn gallery-btn-ghost" @click="exportImages">
          <Archive :size="14" /> 导出图片
        </button>
        <button class="gallery-btn gallery-btn-ghost" @click="exportExcel">
          <FileSpreadsheet :size="14" /> 导出Excel
        </button>
        <button class="gallery-btn gallery-btn-ghost" @click="importImages">
          <FileUp :size="14" /> 导入
        </button>
      </div>
    </div>

    <div class="gallery-detail-main">
      <div class="gallery-info-grid">
        <div class="gallery-info-card">
          <div class="gallery-info-label"><Calendar :size="14" /> 择样日期</div>
          <div class="gallery-info-value">{{ detailData.sampleDate }}</div>
        </div>
        <div class="gallery-info-card">
          <div class="gallery-info-label"><Hash :size="14" /> 本次代号</div>
          <div class="gallery-info-value">{{ detailData.code }}</div>
        </div>
        <div class="gallery-info-card">
          <div class="gallery-info-label"><Building2 :size="14" /> 客户名称</div>
          <div class="gallery-info-value">{{ detailData.customer }}</div>
        </div>
        <div class="gallery-info-card">
          <div class="gallery-info-label"><Camera :size="14" /> 拍摄人员</div>
          <div class="gallery-info-value">{{ detailData.photographer }}</div>
        </div>
        <div class="gallery-info-card">
          <div class="gallery-info-label"><Clock :size="14" /> 创建时间</div>
          <div class="gallery-info-value">{{ detailData.createTime }}</div>
        </div>
        <div class="gallery-info-card">
          <div class="gallery-info-label"><RefreshCw :size="14" /> 修改时间</div>
          <div class="gallery-info-value">{{ detailData.modifyTime }}</div>
        </div>
      </div>

      <div class="gallery-stat-grid">
        <div class="gallery-stat-card">
          <div class="gallery-stat-icon green"><Upload :size="20" /></div>
          <div class="gallery-stat-text">
            <span>已导出数据</span>
            <strong>{{ detailData.exportedCount }}</strong>
          </div>
        </div>
        <div class="gallery-stat-card">
          <div class="gallery-stat-icon orange"><CircleAlert :size="20" /></div>
          <div class="gallery-stat-text">
            <span>未导出数据</span>
            <strong>{{ detailData.unexportedCount }}</strong>
          </div>
        </div>
      </div>

      <div class="gallery-card gallery-image-table-card">
        <div class="gallery-image-table-head">
          <h3><LayoutGrid :size="16" /> 图片列表</h3>
          <div style="display:flex;gap:8px">
            <button class="gallery-btn gallery-btn-ghost" @click="refreshImages">
              <RefreshCw :size="14" /> 刷新
            </button>
          </div>
        </div>
        <div class="gallery-detail-scroll">
          <table class="gallery-image-table">
            <thead>
              <tr>
                <th style="width:44px">
                  <input type="checkbox" :checked="isAllDetailSelected" @change="toggleAllDetail" />
                </th>
                <th>序号</th>
                <th>公司编号</th>
                <th>出厂货号</th>
                <th>图片区</th>
                <th>拍摄人</th>
                <th>修改人</th>
                <th>上传时间</th>
                <th>修改时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(img, idx) in detailImages" :key="img.id">
                <td>
                  <input type="checkbox" :checked="selectedImageIds.includes(img.id)" @change="toggleDetailRow(img.id)" />
                </td>
                <td>{{ idx + 1 }}</td>
                <td>{{ img.companyCode }}</td>
                <td>{{ img.factoryCode }}</td>
                <td class="gallery-img-strip">
                  <div class="gallery-thumb-item" v-for="(color, ci) in img.colors" :key="ci">
                    <div
                      class="gallery-thumb-box"
                      :style="{
                        width: thumbPx + 'px',
                        height: thumbPx + 'px',
                        background: color
                      }"
                    ></div>
                    <span class="gallery-thumb-label">{{ img.labels[ci] }}</span>
                  </div>
                </td>
                <td>{{ img.photographer }}</td>
                <td>{{ img.modifier }}</td>
                <td>{{ img.uploadTime }}</td>
                <td>{{ img.modifyTime }}</td>
                <td class="gallery-sticky-action">
                  <div class="gallery-row-actions">
                    <button class="gallery-row-btn"><Eye :size="14" /> 查看</button>
                    <button class="gallery-row-btn"><Pencil :size="14" /> 编辑</button>
                    <button class="gallery-row-btn danger"><Trash2 :size="14" /> 删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="gallery-detail-footer">
          <button class="gallery-btn gallery-btn-ghost" @click="selectAllDetail">
            <CheckSquare :size="14" /> 全选
          </button>
          <button class="gallery-btn gallery-btn-ghost" @click="invertDetailSelection">
            <Shuffle :size="14" /> 反选
          </button>
          <button class="gallery-btn gallery-btn-ghost" @click="clearDetailSelection">
            <Square :size="14" /> 清空
          </button>
          <div class="gallery-status-info">
            共 <strong>{{ detailImages.length }}</strong> 条记录
          </div>
          <select class="gallery-page-size" v-model.number="detailPageSize">
            <option :value="10">10 条/页</option>
            <option :value="20">20 条/页</option>
            <option :value="50">50 条/页</option>
          </select>
          <div style="display:flex;align-items:center;gap:4px">
            <button class="gallery-btn gallery-btn-ghost" :disabled="detailCurrentPage <= 1" @click="detailCurrentPage--">
              ‹
            </button>
            <span style="font-size:12px;font-weight:700;color:rgba(29,29,31,0.56)">{{ detailCurrentPage }} / {{ detailTotalPages }}</span>
            <button class="gallery-btn gallery-btn-ghost" :disabled="detailCurrentPage >= detailTotalPages" @click="detailCurrentPage++">
              ›
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import '@/styles/gallery.css'
import {
  FileText, RotateCcw, Save, Search, Plus, Trash2, Download,
  CheckSquare, Shuffle, Square, ArrowLeft, Hash, Building2, Camera,
  LayoutGrid, Maximize2, Archive, FileSpreadsheet, FileUp, Upload,
  Eye, Pencil, Lock, Unlock, Calendar, Clock, RefreshCw, CircleAlert,
  ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  ChevronsUp, ChevronsDown, EyeOff, Settings, X
} from 'lucide-vue-next'

const currentView = ref('list')

// 表单状态
const formExpanded = ref(false)
const formVisible = ref(true)
const formMode = ref('add') // 'add' | 'edit' | 'readonly'
const showFieldSettings = ref(false)

const galleryFormFields = [
  { key: 'sampleDate', label: '择样日期' },
  { key: 'code', label: '本次代号' },
  { key: 'customer', label: '客户名称' },
  { key: 'photographer', label: '拍摄人员' },
  { key: 'remark', label: '备注' }
]

const fieldVisible = reactive({
  sampleDate: true,
  code: true,
  customer: true,
  photographer: true,
  remark: true
})

const visibleFormFields = computed(() =>
  galleryFormFields.filter(f => fieldVisible[f.key])
)

const toggleFieldSettings = () => {
  showFieldSettings.value = !showFieldSettings.value
}

const formData = reactive({
  sampleDate: '',
  code: '',
  customer: '',
  photographer: '',
  remark: ''
})

const resetForm = () => {
  formData.sampleDate = ''
  formData.code = ''
  formData.customer = ''
  formData.photographer = ''
  formData.remark = ''
}

const saveForm = () => {}

const searchKeyword = ref('')
const dateFrom = ref('')
const dateTo = ref('')

const onQuery = () => {}

const tableData = ref([
  { id: 1, sampleDate: '2026-05-20', code: 'A001', customer: '杭州锦程贸易', photographer: '张伟', encrypted: true },
  { id: 2, sampleDate: '2026-05-18', code: 'B002', customer: '深圳华创科技', photographer: '李娜', encrypted: false },
  { id: 3, sampleDate: '2026-05-15', code: 'C003', customer: '上海盛达实业', photographer: '王磊', encrypted: true },
  { id: 4, sampleDate: '2026-05-12', code: 'D004', customer: '广州永信商贸', photographer: '赵敏', encrypted: false },
  { id: 5, sampleDate: '2026-05-10', code: 'E005', customer: '北京中天集团', photographer: '陈静', encrypted: true },
  { id: 6, sampleDate: '2026-05-08', code: 'F006', customer: '成都瑞丰贸易', photographer: '刘洋', encrypted: false }
])

const selectedIds = ref([])
const currentPage = ref(1)
const pageSize = ref(50)
const pageSizeOptions = [10, 20, 50, 100, 200]
const tableLoading = ref(false)

const totalPages = computed(() => Math.max(1, Math.ceil(tableData.value.length / pageSize.value)))
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

const listColumns = [
  { type: 'checkbox', title: '#', minWidth: 60, fixed: 'left' },
  { type: 'seq', title: '序号', minWidth: 80, fixed: 'left' },
  { field: 'sampleDate', title: '择样日期', minWidth: 180, sortable: true },
  { field: 'code', title: '代号', minWidth: 160, showOverflow: true, sortable: true, slots: { default: 'col_code' } },
  { field: 'customer', title: '客户名称', minWidth: 260, showOverflow: true, sortable: true },
  { field: 'photographer', title: '拍摄人员', minWidth: 180, showOverflow: true },
  { field: 'status', title: '状态', minWidth: 140, slots: { default: 'col_status' } },
  { field: 'action', title: '操作', minWidth: 420, slots: { default: 'col_action' } }
]

const gridRef = ref(null)
const tableWrapRef = ref(null)
const tableWrapHeight = ref(600)
let resizeObserver = null
let resizeRafId = null
let lastObservedHeight = 0

const goPage = (page) => {
  currentPage.value = page
}

const onCellClick = ({ row, column }) => {
  // 可以根据需要添加单元格点击逻辑
}

const onSortChange = ({ field, order }) => {
  // 可以根据需要添加排序逻辑
}

watch(pageSize, () => {
  currentPage.value = 1
})

const addCode = () => {}
const deleteCode = () => {}
const exportList = () => {}

const openDetail = (row) => {
  detailData.value = { ...row, createTime: row.sampleDate + ' 09:00:00', modifyTime: row.sampleDate + ' 14:30:00', exportedCount: 12, unexportedCount: 5 }
  currentView.value = 'detail'
}

const editRow = (row) => {
  formData.sampleDate = row.sampleDate
  formData.code = row.code
  formData.customer = row.customer
  formData.photographer = row.photographer
  formData.remark = ''
}

const deleteRow = (row) => {
  tableData.value = tableData.value.filter(r => r.id !== row.id)
}

const encryptRow = (row) => { row.encrypted = true }
const decryptRow = (row) => { row.encrypted = false }

const detailData = ref({
  sampleDate: '', code: '', customer: '', photographer: '',
  createTime: '', modifyTime: '', exportedCount: 0, unexportedCount: 0
})

const thumbSize = ref('small')
const thumbPx = computed(() => thumbSize.value === 'small' ? 40 : thumbSize.value === 'medium' ? 64 : 96)

const palette = [
  '#5ac8fa', '#34c759', '#ff9f0a', '#ff3b30', '#af52de', '#5856d6',
  '#007aff', '#30d158', '#ff6482', '#ffcc00', '#64d2ff', '#bf5af2'
]

const generateColors = (seed) => {
  const colors = []
  const labels = []
  for (let i = 0; i < 6; i++) {
    colors.push(palette[(seed + i) % palette.length])
    labels.push(`IMG-${String(seed * 6 + i + 1).padStart(3, '0')}`)
  }
  return { colors, labels }
}

const detailImages = ref(
  Array.from({ length: 3 }, (_, ri) => ({
    id: ri + 1,
    companyCode: `CP-${String(ri + 1).padStart(4, '0')}`,
    factoryCode: `FC-${String(ri + 1).padStart(3, '0')}`,
    colors: generateColors(ri).colors,
    labels: generateColors(ri).labels,
    photographer: '张伟',
    modifier: '李娜',
    uploadTime: '2026-05-20 10:30',
    modifyTime: '2026-05-21 15:45'
  }))
)

const selectedImageIds = ref([])
const detailCurrentPage = ref(1)
const detailPageSize = ref(10)
const detailTotalPages = computed(() => Math.max(1, Math.ceil(detailImages.value.length / detailPageSize.value)))

const isAllDetailSelected = computed(() => detailImages.value.length > 0 && detailImages.value.every(r => selectedImageIds.value.includes(r.id)))

const toggleAllDetail = () => {
  if (isAllDetailSelected.value) {
    selectedImageIds.value = []
  } else {
    selectedImageIds.value = detailImages.value.map(r => r.id)
  }
}

const toggleDetailRow = (id) => {
  const idx = selectedImageIds.value.indexOf(id)
  if (idx >= 0) selectedImageIds.value.splice(idx, 1)
  else selectedImageIds.value.push(id)
}

const selectAllDetail = () => {
  selectedImageIds.value = detailImages.value.map(r => r.id)
}

const invertDetailSelection = () => {
  selectedImageIds.value = detailImages.value.filter(r => !selectedImageIds.value.includes(r.id)).map(r => r.id)
}

const clearDetailSelection = () => {
  selectedImageIds.value = []
}

const exportImages = () => {}
const exportExcel = () => {}
const importImages = () => {}
const refreshImages = () => {}

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
  }
})

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
})
</script>
