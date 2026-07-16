<template>
  <div class="inventory-page">
    <!-- 顶部数据展示区（只读） -->
    <div class="inventory-card inventory-form-card">
      <div class="inventory-form-body">
        <div class="inventory-form-scroll">
          <div class="inventory-form-grid">
              <div class="inventory-form-row inventory-form-row-6">
                <div class="inventory-form-field-pair">
                  <div class="inventory-form-field">
                    <label class="inventory-form-label">出库编号</label>
                    <input class="inventory-form-input" style="width:380px" :value="currentRecord?.outboundCode || ''" readonly />
                  </div>
                  <div class="inventory-form-field">
                    <label class="inventory-form-label">本次代号</label>
                    <input class="inventory-form-input" :value="currentRecord?.codeName || ''" readonly />
                  </div>
                </div>
                <div class="inventory-form-field">
                  <label class="inventory-form-label">创建日期</label>
                  <input class="inventory-form-input" style="width:380px" :value="currentRecord?.createDate || ''" readonly />
                </div>
                <div class="inventory-form-field">
                  <label class="inventory-form-label">登记人</label>
                  <input class="inventory-form-input" :value="currentRecord?.creator || ''" readonly />
                </div>
                <div class="inventory-form-field">
                  <label class="inventory-form-label">楼层</label>
                  <input class="inventory-form-input" :value="currentRecord?.floor || ''" readonly />
                </div>
                <div class="inventory-form-field" style="flex:1; min-width:0">
                    <label class="inventory-form-label">备注</label>
                    <input class="inventory-form-input" style="flex:1; min-width:0" :value="currentRecord?.remark || ''" readonly />
                  </div>
              </div>
        </div>
      </div>
    </div>
    </div>

    <!-- 工具栏 -->
    <div class="inventory-card inventory-toolbar-card">
      <div class="inventory-toolbar-row">
        <div class="inventory-search">
          <Search :size="16" />
          <input v-model="searchKeyword" placeholder="搜索出库编号/本次代号" @keydown.enter="onSearch" />
        </div>
        <button class="inventory-btn inventory-btn-primary" @click="onSearch">
          <Search :size="14" /> 查询
        </button>
        <button class="inventory-btn inventory-btn-ghost" @click="clearSearch">
          <X :size="14" /> 清除
        </button>
        <span class="inventory-toolbar-sep"></span>
        <button class="inventory-btn inventory-btn-primary" @click="startAdd">
          <Plus :size="14" /> 新增出库
        </button>
        <button class="inventory-btn inventory-btn-ghost" :disabled="!currentRecord" @click="startEdit">
          <Pencil :size="14" /> 修改
        </button>
        <button class="inventory-btn inventory-btn-danger" :disabled="!currentRecord" @click="deleteCurrent">
          <Trash2 :size="14" /> 删除
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
          :columns="allColumns"
          :data="filteredTableData"
          :loading="tableLoading"
          :height="tableWrapHeight"
          :toolbar-config="{ custom: true, zoom: true }"
          :custom-config="{ storage: true }"
          :column-config="{ resizable: true, drag: true, isMaximized: true }"
          :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
          :checkbox-config="{ highlight: true, checkField: 'checkbox' }"
          :cell-config="{ height: 80 }"
          :sort-config="{ trigger: 'header', remote: true }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 30, rSize: 80, rHeight: 80 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 24 }, scrollY: { gt: 0, oSize: 30, rSize: 80, rHeight: 80 } }"
          :border="true"
          :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontSize: '30px', fontWeight: 600, textAlign: 'center' }"
          :cell-style="{ textAlign: 'center', fontSize: '26px' }"
          @cell-click="onCellClick"
          @sort-change="onSortChange"
          @checkbox-change="onCheckboxChange"
          @checkbox-all="onCheckboxAll"
          @resizable-change="saveGridPrefs"
          @custom="onCustomChange"
          @column-dragend="onColumnDragEnd"
        >
          <template #col_codeName="{ row }">
            <a class="cell-link" @click.stop="openOutboundDetail(row)">{{ row.codeName }}</a>
          </template>
          <template #col_submitStatus="{ row }">
            <span :style="{ color: (row.submittedCount || 0) === (row.totalCount || 0) && (row.totalCount || 0) > 0 ? '#16a34a' : '#d97706' }">
              {{ row.submittedCount || 0 }} / {{ row.totalCount || 0 }}
            </span>
          </template>
        </vxe-grid>
      </div>
      <div class="inventory-statusbar">
        <div class="inventory-status-info">
          共 <strong>{{ totalRecords }}</strong> 条
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

    <!-- 新增/编辑表单弹窗 -->
    <Transition name="form-modal-fade">
      <div v-if="showFormModal" class="modal-overlay form-modal-overlay" @click.self="cancelEdit">
        <div class="form-modal">
          <div class="form-modal-header">
            <div class="form-modal-title-wrap">
              <span class="form-modal-icon">{{ formMode === 'add' ? '+' : '✎' }}</span>
              <h3 class="form-modal-title">{{ formMode === 'add' ? '新增出库' : '编辑出库' }}</h3>
            </div>
            <button class="form-modal-close" @click="cancelEdit"><X :size="18" /></button>
          </div>

          <div class="form-modal-body">
            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label">出库编号</label>
                <input class="fm-input fm-input-auto" :value="formData.outboundCode" readonly placeholder="保存时自动生成" />
              </div>
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>本次代号</label>
                <input class="fm-input fm-input-auto" :value="formData.codeName" readonly placeholder="自动生成" />
              </div>
            </div>

            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label">创建日期</label>
                <VxeDatePicker class="fm-input" v-model="formData.createDate" type="datetime" value-type="string" format="yyyy-MM-dd HH:mm:ss" transfer clearable />
              </div>
              <div class="fm-field">
                <label class="fm-label">登记人</label>
                <input class="fm-input" v-model="formData.creator" readonly />
              </div>
            </div>

            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>楼层</label>
                <select class="fm-input" v-model="formData.floor">
                  <option value="">请选择楼层</option>
                  <option value="2楼">2楼</option>
                  <option value="3楼">3楼</option>
                  <option value="4楼">4楼</option>
                  <option value="5楼">5楼</option>
                  <option value="6楼">6楼</option>
                </select>
              </div>
              <div class="fm-field"></div>
            </div>

            <div class="fm-row fm-row-1">
              <div class="fm-field">
                <label class="fm-label">备注</label>
                <textarea class="fm-textarea" v-model="formData.remark" rows="2" placeholder="请输入备注"></textarea>
              </div>
            </div>
          </div>

          <div class="form-modal-footer">
            <button class="inventory-btn inventory-btn-ghost" @click="cancelEdit">取消</button>
            <button class="inventory-btn inventory-btn-primary" @click="saveOutbound">
              <Save :size="14" /> 确定
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Alert 弹窗 -->
    <div v-if="showAlert" class="modal-overlay" style="background:transparent;backdrop-filter:none" @click.self="showAlert = false">
      <div class="modal-dialog" style="max-width:380px">
        <div class="modal-body" style="text-align:center;padding:32px 24px">
          <p style="font-size:15px;color:#1d1d1f;margin-bottom:20px">{{ alertMessage }}</p>
          <button class="inventory-btn inventory-btn-primary" @click="showAlert = false">知道了</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, onActivated, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search, Plus, Pencil, Trash2, Save, X,
  ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight
} from 'lucide-vue-next'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { useGridPrefSync } from '@/composables/useGridPrefSync'
import '@/styles/manufacturer-export.css'

const auth = useAuth()

function getCurrentUser() {
  return auth.state?.userInfo?.realName || auth.state?.userInfo?.username || ''
}

// ========== 状态 ==========
const formMode = ref('readonly')
const showFormModal = ref(false)
const formData = reactive({})
const currentRecord = ref(null)
const gridRef = ref(null)
const tableWrapRef = ref(null)
const tableWrapHeight = ref(600)
let resizeObserver = null
let resizeRafId = null
let lastObservedHeight = 0

const searchKeyword = ref('')

const currentPage = ref(1)
const pageSize = ref(500)
const pageSizeOptions = [100, 200, 500, 1000, 2000]
const totalRecords = ref(0)
const tableLoading = ref(false)

const sortField = ref('')
const sortOrder = ref('')
const checkedRows = ref([])

const list = ref([])

// ========== Alert 弹窗 ==========
const showAlert = ref(false)
const alertMessage = ref('')

function showAlertDialog(msg) {
  alertMessage.value = msg
  showAlert.value = true
}

const totalPages = computed(() => Math.max(1, Math.ceil(totalRecords.value / pageSize.value)))

const filteredTableData = computed(() => list.value)

const isEditing = computed(() => formMode.value === 'add' || formMode.value === 'edit')

const allColumns = [
  { type: 'checkbox', title: '', width: 50, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'outboundCode', title: '出库编号', minWidth: 160, showOverflow: true, sortable: true },
  { field: 'codeName', title: '本次代号', minWidth: 120, showOverflow: true, sortable: true, slots: { default: 'col_codeName' } },
  { field: 'createDate', title: '创建日期', minWidth: 160, sortable: true },
  { field: 'creator', title: '登记人', minWidth: 100 },
  { field: 'floor', title: '楼层', minWidth: 80 },
  { field: 'remark', title: '备注', minWidth: 200, showOverflow: true },
  { field: 'submitted', title: '提交状态', width: 120, slots: { default: 'col_submitStatus' } }
]

// 表格列设置跨设备同步
const { fullKey: gridStorageKey, saveToBackend: saveGridPrefs, ready: prefReady } = useGridPrefSync(gridRef, 'manufacturer-outbound', allColumns)

// ========== CRUD 操作 ==========
const onSearch = () => {
  currentPage.value = 1
  loadData()
}

const clearSearch = () => {
  searchKeyword.value = ''
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

const router = useRouter()

const openOutboundDetail = (row) => {
  if (!row || !row.codeName) return
  router.push({ name: 'OutboundDetail', params: { codeName: row.codeName } })
}

const onCellClick = ({ row }) => {
  if (isEditing.value) return
  selectRecord(row)
}

const onCheckboxChange = ({ records }) => {
  checkedRows.value = records
}

const onCheckboxAll = ({ records }) => {
  checkedRows.value = records
}

const onSortChange = ({ field, order }) => {
  sortField.value = field || ''
  sortOrder.value = order || ''
  currentPage.value = 1
  loadData()
}

const onCustomChange = ({ type }) => {
  if (type === 'confirm' || type === 'reset') {
    setTimeout(() => saveGridPrefs(), 50)
  }
}

const onColumnDragEnd = () => {
  setTimeout(() => saveGridPrefs(), 100)
}

const selectRecord = (row) => {
  currentRecord.value = row
  Object.keys(formData).forEach(k => delete formData[k])
  if (row) {
    Object.assign(formData, { ...row })
    formatFormDataDates()
  }
}

const resetForm = () => {
  if (currentRecord.value) {
    Object.keys(formData).forEach(k => delete formData[k])
    Object.assign(formData, { ...currentRecord.value })
    formatFormDataDates()
  }
}

const startAdd = () => {
  formMode.value = 'add'
  currentRecord.value = null
  Object.keys(formData).forEach(k => delete formData[k])
  formData.remark = ''
  formData.creator = getCurrentUser()
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  formData.createDate = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  showFormModal.value = true
  generateOutboundCodes()
}

const generateOutboundCodes = () => {
  let maxCode = 100000
  if (list.value && list.value.length > 0) {
    list.value.forEach(row => {
      if (row.codeName) {
        // 兼容旧数据：去掉已有的 R/C 后缀再比较数字
        const num = parseInt(String(row.codeName).replace(/[RC]$/i, ''), 10)
        if (!isNaN(num) && num >= maxCode) maxCode = num + 1
      }
    })
  }
  const nextCode = String(maxCode).padStart(6, '0')
  const today = new Date().toISOString().slice(0, 10)
  formData.outboundCode = `${today}-${nextCode}C`
  formData.codeName = `${nextCode}C`
}

const startEdit = () => {
  if (!currentRecord.value) {
    showAlertDialog('请先在表格中选择一条记录')
    return
  }
  formMode.value = 'edit'
  Object.keys(formData).forEach(k => delete formData[k])
  Object.assign(formData, { ...currentRecord.value })
  formatFormDataDates()
  showFormModal.value = true
}

const cancelEdit = () => {
  showFormModal.value = false
  formMode.value = 'readonly'
  if (currentRecord.value) {
    Object.keys(formData).forEach(k => delete formData[k])
    Object.assign(formData, { ...currentRecord.value })
    formatFormDataDates()
  } else {
    Object.keys(formData).forEach(k => delete formData[k])
  }
}

const saveOutbound = async () => {
  if (!formData.codeName) {
    showAlertDialog('本次代号为必填项')
    return
  }
  if (!formData.floor) {
    showAlertDialog('楼层为必选项')
    return
  }

  const payload = { ...formData }
  delete payload.id

  try {
    if (formMode.value === 'add') {
      const res = await api('/outbound-codes', { method: 'POST', body: JSON.stringify(payload) })
      if (res && res.code === 200) {
        showAlertDialog('添加成功')
        showFormModal.value = false
        formMode.value = 'readonly'
        currentRecord.value = res.data || null
        await loadData()
      } else {
        showAlertDialog(res?.message || '添加失败')
      }
    } else {
      const id = currentRecord.value?.id
      if (!id) { showAlertDialog('未选择记录'); return }
      const res = await api(`/outbound-codes/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
      if (res && res.code === 200) {
        showAlertDialog('修改成功')
        showFormModal.value = false
        formMode.value = 'readonly'
        await loadData()
      } else {
        showAlertDialog(res?.message || '修改失败')
      }
    }
  } catch (e) {
    console.error('操作失败:', e)
    showAlertDialog('操作失败: ' + (e.message || '网络错误'))
  }
}

const deleteCurrent = async () => {
  if (!currentRecord.value) {
    showAlertDialog('请先在表格中选择一条记录')
    return
  }
  if (!confirm(`确定要删除该出库记录吗？\n出库编号: ${currentRecord.value.outboundCode}\n此操作不可恢复。`)) return
  try {
    const res = await api(`/outbound-codes/${currentRecord.value.id}`, { method: 'DELETE' })
    if (res && res.code === 200) {
      showAlertDialog('删除成功')
      currentRecord.value = null
      Object.keys(formData).forEach(k => delete formData[k])
      await loadData()
    } else {
      showAlertDialog(res?.message || '删除失败')
    }
  } catch (e) {
    console.error('删除失败:', e)
    showAlertDialog('删除失败: ' + (e.message || '网络错误'))
  }
}

// ========== 数据加载 ==========
const DATE_FIELDS = new Set(['createDate'])

const formatFormDataDates = () => {
  DATE_FIELDS.forEach(key => {
    if (formData[key]) formData[key] = String(formData[key]).replace('T', ' ')
  })
}

const loadData = async () => {
  try {
    tableLoading.value = true
    const params = [`current=${currentPage.value}`, `size=${pageSize.value}`]
    if (sortField.value) { params.push(`sortField=${sortField.value}`); params.push(`sortOrder=${sortOrder.value}`) }
    if (searchKeyword.value) params.push(`keyword=${encodeURIComponent(searchKeyword.value)}`)
    const res = await api(`/outbound-codes?${params.join('&')}`)
    const result = res.data || res || {}
    const records = result.records || result.list
    const rawList = Array.isArray(records) ? records : (Array.isArray(result) ? result : [])
    rawList.forEach(row => {
      DATE_FIELDS.forEach(key => {
        if (row[key]) row[key] = String(row[key]).replace('T', ' ')
      })
    })
    list.value = rawList
    totalRecords.value = result.total || list.value.length
  } catch (e) {
    console.error('加载数据失败:', e)
    list.value = []
    totalRecords.value = 0
  } finally {
    tableLoading.value = false
    nextTick(() => {
      if (list.value.length > 0 && gridRef.value) {
        gridRef.value.setCurrentRow(list.value[0])
        selectRecord(list.value[0])
      }
    })
  }
}

// ========== 生命周期 ==========
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
  loadData()
})

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
})

onActivated(() => {
  requestAnimationFrame(() => {
    nextTick(() => {
      if (tableWrapRef.value) {
        const rect = tableWrapRef.value.getBoundingClientRect()
        if (rect.height > 0) tableWrapHeight.value = rect.height
      }
    })
  })
})
</script>
