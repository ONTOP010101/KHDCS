<template>
  <div class="inventory-page">
    <!-- 顶部数据展示区（只读） -->
    <div class="inventory-card inventory-form-card">
      <div class="inventory-form-body" style="display:flex;align-items:center;gap:20px;flex-wrap:nowrap">
        <div class="inventory-form-scroll" style="flex:1;overflow-y:auto;min-width:0">
          <div class="inventory-form-grid">
            <div class="inventory-form-row inventory-form-row-6">
              <div class="inventory-form-field">
                <label class="inventory-form-label">公司编号</label>
                <input class="inventory-form-input" style="width:380px" :value="currentRecord?.companyCode || ''" readonly />
              </div>
              <div class="inventory-form-field">
                <label class="inventory-form-label">出厂货号</label>
                <input class="inventory-form-input" style="width:380px" :value="currentRecord?.factoryNo || ''" readonly />
              </div>
              <div class="inventory-form-field">
                <label class="inventory-form-label">中文包装</label>
                <input class="inventory-form-input" :value="currentRecord?.chinesePackage || ''" readonly />
              </div>
              <div class="inventory-form-field">
                <label class="inventory-form-label">摊位号</label>
                <input class="inventory-form-input" style="width:380px" :value="currentRecord?.boothNumber || ''" readonly />
              </div>
              <div class="inventory-form-field" style="flex:1; min-width:0">
                <label class="inventory-form-label">样品名称</label>
                <input class="inventory-form-input" style="flex:1; min-width:0" :value="currentRecord?.sampleName || ''" readonly />
              </div>
            </div>
            <div class="inventory-form-row inventory-form-row-6">
              <div class="inventory-form-field">
                <label class="inventory-form-label">出库编号</label>
                <input class="inventory-form-input" style="width:380px" :value="currentRecord?.inventoryCode || ''" readonly />
              </div>
              <div class="inventory-form-field">
                <label class="inventory-form-label">本次代号</label>
                <input class="inventory-form-input" style="width:380px" :value="currentRecord?.codeName || ''" readonly />
              </div>
              <div class="inventory-form-field">
                <label class="inventory-form-label">楼层</label>
                <input class="inventory-form-input" :value="currentRecord?.floor || ''" readonly />
              </div>
              <div class="inventory-form-field">
                <label class="inventory-form-label">创建日期</label>
                <input class="inventory-form-input" style="width:380px" :value="currentRecord?.createDate || ''" readonly />
              </div>
              <div class="inventory-form-field" style="flex:1; min-width:0">
                <label class="inventory-form-label">手机</label>
                <input class="inventory-form-input" style="flex:1; min-width:0" :value="currentRecord?.mobile || ''" readonly />
              </div>
            </div>
            <div class="inventory-form-row inventory-form-row-6">
              <div class="inventory-form-field">
                <label class="inventory-form-label">厂商名称</label>
                <input class="inventory-form-input" style="width:380px" :value="currentRecord?.manufacturerName || ''" readonly />
              </div>
              <div class="inventory-form-field">
                <label class="inventory-form-label">电话</label>
                <input class="inventory-form-input" style="width:380px" :value="currentRecord?.telephone || ''" readonly />
              </div>
              <div class="inventory-form-field">
                <label class="inventory-form-label">登记人</label>
                <input class="inventory-form-input" :value="currentRecord?.creator || ''" readonly />
              </div>
              <div class="inventory-form-field">
                <label class="inventory-form-label">入库时间</label>
                <input class="inventory-form-input" style="width:380px" :value="currentRecord?.stockInTime || ''" readonly />
              </div>
              <div class="inventory-form-field">
                <label class="inventory-form-label">修改时间</label>
                <input class="inventory-form-input" style="width:380px" :value="currentRecord?.updateTime || ''" readonly />
              </div>
              <div class="inventory-form-field" style="flex:1; min-width:0">
                <label class="inventory-form-label">厂商编号</label>
                <input class="inventory-form-input" style="flex:1; min-width:0" :value="currentRecord?.manufacturerCode || ''" readonly />
              </div>
            </div>
            <div class="inventory-form-row inventory-form-row-6">
              <div class="inventory-form-field" style="flex:1; min-width:0">
                <label class="inventory-form-label">备注</label>
                <input class="inventory-form-input" style="flex:1; min-width:0" :value="currentRecord?.remark || ''" readonly />
              </div>
            </div>
          </div>
        </div>
        <div class="inventory-image-box" style="flex-shrink:0">
          <img v-if="currentRecord?.image" :src="currentRecord.image" alt="图片" style="width:100%;aspect-ratio:1/1;object-fit:contain;border-radius:8px;border:1px solid #e5e7eb;cursor:pointer" @click="previewImage(currentRecord)" />
          <div v-else style="width:100%;aspect-ratio:1/1;min-width:150px;border:1px dashed #d1d5db;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#9ca3af;font-size:14px">暂无图片</div>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="inventory-card inventory-toolbar-card">
      <div class="inventory-toolbar-row">
        <div class="inventory-search inventory-search-company">
          <span v-if="!addCompanyCode" class="inventory-input-label">公司编号</span>
          <input v-model="addCompanyCode" placeholder="" @keydown.enter="addMaterial" />
        </div>
        <button class="inventory-btn inventory-btn-primary" @click="addMaterial" :disabled="!addCompanyCode">
          <Plus :size="14" /> 添加
        </button>
        <button class="inventory-btn inventory-btn-ghost" @click="showBatchAddModal = true">
          <Plus :size="14" /> 批量添加
        </button>
        <span class="inventory-toolbar-sep"></span>
        <div class="inventory-search">
          <Search :size="16" />
          <input v-model="searchKeyword" placeholder="搜索公司编号/出厂货号/厂商名称/摊位号" @keydown.enter="onSearch" />
        </div>
        <button class="inventory-btn inventory-btn-primary" @click="onSearch">
          <Search :size="14" /> 查询
        </button>
        <button class="inventory-btn inventory-btn-ghost" @click="clearSearch">
          <X :size="14" /> 清除
        </button>
        <span class="inventory-toolbar-sep"></span>
        <button class="inventory-btn inventory-btn-ghost" :disabled="!currentRecord" @click="startEdit">
          <Pencil :size="14" /> 修改
        </button>
        <button class="inventory-btn inventory-btn-danger" :disabled="!currentRecord" @click="deleteCurrent">
          <Trash2 :size="14" /> 删除
        </button>
        <button class="inventory-btn inventory-btn-danger" :disabled="checkedRows.length === 0" @click="batchDelete">
          <Trash2 :size="14" /> 批量删除
        </button>
        <span class="inventory-toolbar-sep"></span>
        <button class="inventory-btn inventory-btn-success" :disabled="checkedRows.length === 0 || submitting" @click="batchSubmit">
          <Send :size="14" /> {{ submitting ? '提交中...' : `提交 (${checkedRows.length})` }}
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
          :cell-style="gridCellStyle"
          @cell-click="onCellClick"
          @sort-change="onSortChange"
          @checkbox-change="onCheckboxChange"
          @checkbox-all="onCheckboxAll"
          @resizable-change="saveGridPrefs"
          @custom="onCustomChange"
          @column-dragstart="onColumnDragStart"
          @column-dragend="onColumnDragEnd"
        >
          <template #image="{ row }">
            <img v-if="row.image" :src="row.image" class="inventory-thumb" @mouseenter="onThumbMouseEnter($event, row)" @mouseleave="onThumbMouseLeave" />
            <span v-else class="inventory-no-thumb">-</span>
          </template>
          <template #col_submitted="{ row }">
            <span :style="{ color: row.submitted === 1 ? '#16a34a' : '#9ca3af' }">
              {{ row.submitted === 1 ? '已提交' : '未提交' }}
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
              <h3 class="form-modal-title">{{ formMode === 'add' ? '新增库存' : '编辑库存' }}</h3>
            </div>
            <button class="form-modal-close" @click="cancelEdit"><X :size="18" /></button>
          </div>

          <div class="form-modal-body">
            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label">库存编号</label>
                <input class="fm-input fm-input-auto" :value="formData.inventoryCode" readonly placeholder="自动生成" />
              </div>
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>本次代号</label>
                <input class="fm-input fm-input-auto" :value="formData.codeName" readonly :placeholder="formData.codeName ? '' : '加载中...'" />
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
            <button class="inventory-btn inventory-btn-primary" @click="saveInventory">
              <Save :size="14" /> 确定
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Alert 弹窗 -->
    <div v-if="showAlert" class="modal-overlay" style="background:transparent;backdrop-filter:none" @click.self="showAlert = false">
      <div class="modal-dialog" style="max-width:520px">
        <div class="modal-body" style="text-align:center;padding:48px 36px">
          <p style="font-size:24px;color:#1d1d1f;margin-bottom:20px">{{ alertMessage }}</p>
          <button class="inventory-btn inventory-btn-primary" @click="showAlert = false">知道了</button>
        </div>
      </div>
    </div>

    <!-- 批量添加弹窗 -->
    <div v-if="showBatchAddModal" class="modal-overlay" style="background:transparent;backdrop-filter:none" @click.self="showBatchAddModal = false">
      <div class="modal-dialog" style="max-width:1500px;width:96vw;max-height:65vh;display:flex;flex-direction:column">
        <div class="modal-header">
          <strong style="font-size:30px">批量添加入库</strong>
          <button class="modal-close-btn" @click="showBatchAddModal = false"><X :size="30" /></button>
        </div>
        <div class="modal-body" style="padding:28px 32px;display:flex;gap:28px;height:70vh;overflow:hidden">
          <!-- 左：输入区 -->
          <div style="flex:1;display:flex;flex-direction:column">
            <p style="font-size:26px;color:#374151;margin-bottom:12px">输入公司编号（每行/逗号/空格分隔）：</p>
            <textarea ref="batchInputRef" v-model="batchCompanyCodes" rows="16" style="width:100%;flex:1;padding:14px;border:1px solid #d1d5db;border-radius:8px;font-size:26px;resize:none;font-family:inherit;min-height:360px" placeholder="YX18251030&#10;YX18251031&#10;YX18251032"></textarea>
            <div style="margin-top:16px;display:flex;gap:12px">
              <button class="inventory-btn inventory-btn-ghost" style="flex:1;font-size:26px;padding:14px" @click="showBatchAddModal = false">取消</button>
              <button class="inventory-btn inventory-btn-primary" style="flex:1;font-size:26px;padding:14px" @click="doBatchAdd" :disabled="batchPreview.length === 0 || batchAdding">
                {{ batchAdding ? '添加中...' : '确定添加 (' + batchPreview.length + ')' }}
              </button>
            </div>
          </div>
          <!-- 右：预览区 -->
          <div style="flex:1.3;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;display:flex;flex-direction:column">
            <div style="padding:14px 18px;background:#f9fafb;border-bottom:1px solid #e5e7eb;font-size:26px;font-weight:600;color:#374151">
              预览（共 {{ batchPreview.length }} 条）
            </div>
            <div style="flex:1;overflow-y:auto;padding:8px">
              <div v-if="batchPreview.length === 0" style="text-align:center;color:#9ca3af;padding:40px;font-size:22px">请输入公司编号后点击"解析预览"</div>
              <div v-for="(item, idx) in batchPreview" :key="item.sampleCode" style="display:flex;gap:14px;padding:12px;border-bottom:1px solid #f3f4f6;align-items:center;position:relative">
                <button @click="removeBatchItem(idx)" style="position:absolute;top:6px;right:6px;width:28px;height:28px;border-radius:50%;border:1px solid #e5e7eb;background:#fff;color:#ef4444;cursor:pointer;display:flex;align-items:center;justify-content:center;font-size:20px;line-height:1" title="移除此条">✕</button>
                <div style="width:200px;height:200px;border-radius:8px;overflow:hidden;flex-shrink:0;background:#f3f4f6;display:flex;align-items:center;justify-content:center">
                  <img v-if="item.sampleThumbnail" :src="item.sampleThumbnail" style="width:100%;height:100%;object-fit:contain" @error="$event.target.style.display='none'" />
                  <span v-else style="font-size:20px;color:#9ca3af">无图片</span>
                </div>
                <div style="flex:1;min-width:0;font-size:22px;line-height:1.8">
                  <div><span style="color:#1d1d1f">公司编号：</span><span style="color:#2563eb;font-weight:600">{{ item.sampleCode }}</span></div>
                  <div><span style="color:#1d1d1f">出厂货号：</span><span style="color:#2563eb">{{ item.factoryCode || '-' }}</span></div>
                  <div><span style="color:#1d1d1f">样品名称：</span><span style="color:#2563eb">{{ item.sampleName || '-' }}</span></div>
                  <div><span style="color:#1d1d1f">中文包装：</span><span style="color:#2563eb">{{ item.packagingCn || '-' }}</span></div>
                </div>
                <div style="flex-shrink:0;text-align:center">
                  <div style="font-size:22px;color:#6b7280;margin-bottom:4px">添加条数</div>
                  <input type="number" v-model.number="item.quantity" min="1" max="999" style="width:60px;padding:6px;border:1px solid #d1d5db;border-radius:6px;text-align:center;font-size:22px" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 确认弹窗 -->
    <div v-if="showConfirm" class="modal-overlay" style="background:transparent;backdrop-filter:none" @click.self="onConfirmCancel" @keydown.enter.prevent="onConfirmOk">
      <div class="modal-dialog" style="max-width:560px">
        <div class="modal-body" style="text-align:center;padding:48px 40px">
          <p style="font-size:17px;color:#1d1d1f;margin-bottom:32px;line-height:1.6">{{ confirmMessage }}</p>
          <div style="display:flex;gap:20px;justify-content:center">
            <button class="inventory-btn inventory-btn-ghost" style="padding:12px 48px;font-size:16px" @click="onConfirmCancel">取消</button>
            <button class="inventory-btn inventory-btn-primary" style="padding:12px 48px;font-size:16px" @click="onConfirmOk">确认</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片预览弹窗 -->
    <Teleport to="body">
      <Transition name="form-modal-fade">
        <div v-if="showImagePreview" class="modal-overlay image-preview-overlay" @click.self="closeImagePreview">
          <div class="image-preview-container">
            <button class="image-preview-close" @click="closeImagePreview"><X :size="24" /></button>
            <img :src="previewImageUrl" alt="预览图片" class="image-preview-img" />
          </div>
        </div>
      </Transition>
    </Teleport>
  <!-- 鼠标悬浮缩略图大图预览 -->
    <Teleport to="body">
      <Transition name="hover-preview-fade">
        <div v-if="hoverPreview.show" class="sr-hover-preview" :style="{ left: hoverPreview.x + 'px', top: hoverPreview.y + 'px' }">
          <img :src="hoverPreview.src" @error="hoverPreview.fallback && hoverPreview.src !== hoverPreview.fallback ? (hoverPreview.src = hoverPreview.fallback) : (hoverPreview.show = false)" />
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, onActivated, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  Search, Plus, Pencil, Trash2, Save, X,
  ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  Upload, Download, Send
} from 'lucide-vue-next'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { useGridPrefSync } from '@/composables/useGridPrefSync'
import '@/styles/manufacturer-export.css'

const auth = useAuth()
const route = useRoute()

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
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return 0
  return Math.abs(eIdx - sIdx) + 1
})

const searchKeyword = ref('')

const addCompanyCode = ref('')
const addMaterial = async () => {
  const code = addCompanyCode.value.trim()
  if (!code) return
  const isDuplicate = list.value.some(item => item.companyCode === code)
  if (isDuplicate) {
    showConfirmDialog('该代号下已存在相同的公司编号，是否继续添加？', async () => {
      await doAddMaterial()
    })
  } else {
    await doAddMaterial()
  }
}
const doAddMaterial = async () => {
  try {
    const codeName = route.params.codeName
    const body = { companyCode: addCompanyCode.value.trim(), creator: getCurrentUser() }
    const res = await api(`/inventory/${codeName}/items`, {
      method: 'POST',
      body: JSON.stringify(body)
    })
    if (res && res.code === 200) {
      addCompanyCode.value = ''
      await loadData()
    } else {
      showAlertDialog(res?.message || '添加失败')
    }
  } catch (e) {
    showAlertDialog(e.message || '添加资料失败')
  }
}

const currentPage = ref(1)
const pageSize = ref(200)
const pageSizeOptions = [100, 200, 500, 1000, 2000]
const totalRecords = ref(0)
const tableLoading = ref(false)

const sortField = ref('')
const sortOrder = ref('')
const checkedRows = ref([])
const submitting = ref(false)

const list = ref([])

// ========== Alert 弹窗 ==========
const showAlert = ref(false)
const alertMessage = ref('')
function showAlertDialog(msg) {
  alertMessage.value = msg
  showAlert.value = true
}

// ========== 确认弹窗 ==========
const showConfirm = ref(false)
const confirmMessage = ref('')
let confirmCallback = null
let confirmCancelCallback = null

// 批量添加
const showBatchAddModal = ref(false)
const batchCompanyCodes = ref('')
const batchInputRef = ref(null)
const batchParsing = ref(false)
const batchAdding = ref(false)
const batchPreview = ref([])
const prevCodeCount = ref({})

// 预览图片 URL 转换（sampleThumbnail 是 imageId，需转成 /images/thumbnail/xxx）
const toPreviewThumbUrl = (thumbnail, imageId) => {
  // 优先用 webp 缩略图路径
  if (thumbnail) return `/thumbnails/${thumbnail}`
  if (imageId) return `/images/thumbnail/${imageId}`
  return ''
}

const parseBatchCodes = async () => {
  const raw = batchCompanyCodes.value.trim()
  if (!raw) { batchPreview.value = []; prevCodeCount.value = {}; return }
  const codes = raw.split(/[\n,，\s]+/).filter(Boolean)
  if (codes.length === 0) { batchPreview.value = []; prevCodeCount.value = {}; return }
  
  // 统计输入中同一编号的出现次数
  const codeCount = {}
  codes.forEach(c => { codeCount[c] = (codeCount[c] || 0) + 1 })
  
  // 保留已有预览中手动修改的条数
  const existingQty = {}
  batchPreview.value.forEach(item => { existingQty[item.sampleCode] = item.quantity })
  const oldCount = prevCodeCount.value || {}
  
  batchParsing.value = true
  // 输入去重后请求
  const uniqueCodes = [...new Set(codes)]
  try {
    const res = await api('/samples/match-by-codes', {
      method: 'POST',
      body: JSON.stringify({ type: 'sampleCode', codes: uniqueCodes, manufacturerCode: null })
    })
    if (res && res.code === 200 && res.data) {
      // 按最后出现位置倒序（新扫的在顶部）
      const lastPos = {}
      codes.forEach((c, i) => { lastPos[c] = i + 1 })
      batchPreview.value = (res.data || []).map(s => {
        const newCount = codeCount[s.sampleCode] || 1
        // 已有预览条目：累加增量；新条目：用 codeCount
        const qty = existingQty[s.sampleCode] != null
          ? existingQty[s.sampleCode] + Math.max(0, newCount - (oldCount[s.sampleCode] || 0))
          : newCount
        return {
          ...s,
          quantity: qty,
          sampleThumbnail: toPreviewThumbUrl(s.thumbnail, s.firstImageId)
        }
      }).sort((a, b) => (lastPos[b.sampleCode] || 0) - (lastPos[a.sampleCode] || 0))
      prevCodeCount.value = codeCount
    } else {
      batchPreview.value = []
      prevCodeCount.value = {}
    }
  } catch (e) {
    batchPreview.value = []
    prevCodeCount.value = {}
  }
  batchParsing.value = false
}

let parseTimer = null
watch(batchCompanyCodes, () => {
  clearTimeout(parseTimer)
  parseTimer = setTimeout(parseBatchCodes, 500)
})
watch(showBatchAddModal, (val) => {
  if (!val) { batchCompanyCodes.value = ''; batchPreview.value = []; prevCodeCount.value = {} }
  else { nextTick(() => batchInputRef.value?.focus()) }
})

const removeBatchItem = (idx) => {
  const code = batchPreview.value[idx]?.sampleCode
  batchPreview.value.splice(idx, 1)
  // 同步清除输入框中对应公司编号
  if (code) {
    const lines = batchCompanyCodes.value.split(/[\n,，]+/)
    batchCompanyCodes.value = lines.filter(line => line.trim() !== code).join('\n')
  }
}

const doBatchAdd = async () => {
  if (batchPreview.value.length === 0) return
  batchAdding.value = true
  const codeName = route.params.codeName
  try {
    const items = batchPreview.value.map(item => ({
      companyCode: item.sampleCode,
      quantity: Math.max(1, Math.min(999, item.quantity || 1))
    }))
    const res = await api(`/inventory/${codeName}/items/batch`, {
      method: 'POST',
      body: JSON.stringify({ items, creator: getCurrentUser() })
    })
    if (res && res.code === 200 && res.data) {
      const { success, fail } = res.data
      if (fail === 0) showAlertDialog(`批量添加完成，成功 ${success} 条`)
      else showAlertDialog(`批量添加完成，成功 ${success} 条，失败 ${fail} 条`)
    } else {
      showAlertDialog(res?.message || '批量添加失败')
    }
  } catch (e) {
    showAlertDialog('批量添加失败: ' + (e.message || '网络错误'))
  }
  batchAdding.value = false
  showBatchAddModal.value = false
  batchCompanyCodes.value = ''
  batchPreview.value = []
  await loadData()
}
function showConfirmDialog(msg, callback, cancelCallback) {
  confirmMessage.value = msg
  confirmCallback = callback
  confirmCancelCallback = cancelCallback || null
  showConfirm.value = true
}
function onConfirmOk() {
  showConfirm.value = false
  if (confirmCallback) {
    confirmCallback()
    confirmCallback = null
  }
  confirmCancelCallback = null
}
function onConfirmCancel() {
  showConfirm.value = false
  if (confirmCancelCallback) {
    confirmCancelCallback()
    confirmCancelCallback = null
  }
  confirmCallback = null
}

// 确认弹窗回车确认 & ESC 取消（setTimeout 推迟到下一轮宏任务，防止当前 Enter 事件被复用）
watch(showConfirm, (val) => {
  if (val) {
    setTimeout(() => {
      const handler = (e) => {
        if (e.key === 'Enter') {
          e.preventDefault()
          onConfirmOk()
        } else if (e.key === 'Escape') {
          e.preventDefault()
          onConfirmCancel()
        }
      }
      window.addEventListener('keydown', handler)
      // 弹窗关闭时移除
      const stopWatch = watch(showConfirm, (v) => {
        if (!v) {
          window.removeEventListener('keydown', handler)
          stopWatch()
        }
      })
    }, 0)
  }
})

// ========== 鼠标悬浮缩略图大图预览 ==========
const hoverPreview = reactive({ show: false, src: '', fallback: '', x: 0, y: 0 })
let hoverTimer = null

const onThumbMouseEnter = (e, row) => {
  if (!row.image) return
  clearTimeout(hoverTimer)
  const thumbSrc = row.image
  // 新格式: /thumbnails/xx/xx/{hash}_thumb.webp
  const hashMatch = thumbSrc.match(/\/([a-f0-9]+)_thumb\.webp$/i)
  const src = hashMatch
    ? '/images/view/hash/' + hashMatch[1]
    : (thumbSrc.match(/\/thumbnail\/(\d+)$/)
        ? '/images/view/' + thumbSrc.match(/\/thumbnail\/(\d+)$/)[1]
        : (row.imageId ? '/images/view/' + row.imageId : thumbSrc))
  hoverTimer = setTimeout(() => {
    const gap = 12
    const previewSize = 620
    const rect = e.target.getBoundingClientRect()
    let left = rect.right + gap
    let top = rect.top
    if (left + previewSize > window.innerWidth) {
      left = rect.left - previewSize - gap
    }
    if (top + previewSize > window.innerHeight) {
      top = window.innerHeight - previewSize - gap
    }
    if (left < gap) left = gap
    if (top < gap) top = gap
    hoverPreview.src = src
    hoverPreview.fallback = thumbSrc
    hoverPreview.x = left
    hoverPreview.y = top
    hoverPreview.show = true
  }, 300)
}

const onThumbMouseLeave = () => {
  clearTimeout(hoverTimer)
  hoverPreview.show = false
}

// ========== 图片预览 ==========
const showImagePreview = ref(false)
const previewImageUrl = ref('')

function previewImage(row) {
  if (!row || !row.image) return
  const url = row.image
  // 新格式: /thumbnails/xx/xx/{hash}_thumb.webp → /images/view/hash/{hash}
  const hashMatch = url.match(/\/([a-f0-9]+)_thumb\.webp$/i)
  if (hashMatch) {
    previewImageUrl.value = `/images/view/hash/${hashMatch[1]}`
    showImagePreview.value = true
    return
  }
  // 旧格式: /images/thumbnail/{id} → /images/view/{id}
  const idMatch = url.match(/\/thumbnail\/(\d+)$/)
  if (idMatch) {
    previewImageUrl.value = `/images/view/${idMatch[1]}`
    showImagePreview.value = true
    return
  }
  if (row.imageId) {
    previewImageUrl.value = `/images/view/${row.imageId}`
    showImagePreview.value = true
    return
  }
  previewImageUrl.value = url
  showImagePreview.value = true
}

function closeImagePreview() {
  showImagePreview.value = false
  previewImageUrl.value = ''
}

const totalPages = computed(() => Math.max(1, Math.ceil(totalRecords.value / pageSize.value)))
const filteredTableData = computed(() => list.value)
const isEditing = computed(() => formMode.value === 'add' || formMode.value === 'edit')

const columns = [
  { type: 'checkbox', title: '', width: 50, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'companyCode', title: '公司编号', minWidth: 120, showOverflow: true },
  { field: 'image', title: '图片', minWidth: 100, slots: { default: 'image' } },
  { field: 'factoryNo', title: '出厂货号', minWidth: 140, showOverflow: true },
  { field: 'sampleName', title: '样品名称', minWidth: 140, showOverflow: true },
  { field: 'manufacturerName', title: '厂商名称', minWidth: 140, showOverflow: true },
  { field: 'createDate', title: '创建日期', minWidth: 160, sortable: true },
  { field: 'stockInTime', title: '入库时间', minWidth: 160, sortable: true },
  { field: 'updateTime', title: '修改时间', minWidth: 160, sortable: true },
  { field: 'inventoryCode', title: '库存编号', minWidth: 160, showOverflow: true, sortable: true },
  { field: 'codeName', title: '本次代号', minWidth: 120, showOverflow: true, sortable: true },
  { field: 'chinesePackage', title: '中文包装', minWidth: 120, showOverflow: true },
  { field: 'boothNumber', title: '摊位号', minWidth: 100 },
  { field: 'mobile', title: '手机', minWidth: 120 },
  { field: 'telephone', title: '电话', minWidth: 120 },
  { field: 'manufacturerCode', title: '厂商编号', minWidth: 120, showOverflow: true },
  { field: 'creator', title: '登记人', minWidth: 100 },
  { field: 'floor', title: '楼层', minWidth: 80 },
  { field: 'remark', title: '备注', minWidth: 200, showOverflow: true },
  { field: 'submitted', title: '提交状态', width: 100, slots: { default: 'col_submitted' } }
]

const { fullKey: gridStorageKey, saveToBackend: saveGridPrefs, ready: prefReady } = useGridPrefSync(gridRef, 'inventory-detail', columns)

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

const onCellClick = ({ row }) => {
  if (isEditing.value) return
  selectRecord(row)
}

const onCheckboxChange = ({ records }) => { if (!submitting.value) checkedRows.value = records }
const onCheckboxAll = ({ records }) => { if (!submitting.value) checkedRows.value = records }

const onSortChange = ({ field, order }) => {
  sortField.value = field || ''
  sortOrder.value = order || ''
  currentPage.value = 1
  loadData()
}

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
  formData.codeName = route.params.codeName
  formData.remark = ''
  formData.creator = getCurrentUser()
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  formData.createDate = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  showFormModal.value = true
}

const startEdit = () => {
  if (!currentRecord.value) { showAlertDialog('请先在表格中选择一条记录'); return }
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

const saveInventory = async () => {
  if (!formData.codeName) { showAlertDialog('本次代号为必填项'); return }
  if (!formData.floor) { showAlertDialog('楼层为必选项'); return }

  const payload = { ...formData }
  delete payload.id
  // 确保 codeName 使用当前路由的代号
  if (!payload.codeName) {
    payload.codeName = route.params.codeName
  }

  try {
    if (formMode.value === 'add') {
      const res = await api('/inventory', { method: 'POST', body: JSON.stringify(payload) })
      if (res && res.code === 200) {
        showAlertDialog('添加成功')
        showFormModal.value = false
        formMode.value = 'readonly'
        currentRecord.value = res.data || null
        await loadData()
      } else { showAlertDialog(res?.message || '添加失败') }
    } else {
      const id = currentRecord.value?.id
      if (!id) { showAlertDialog('未选择记录'); return }
      const res = await api(`/inventory/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
      if (res && res.code === 200) {
        showAlertDialog('修改成功')
        showFormModal.value = false
        formMode.value = 'readonly'
        await loadData()
      } else { showAlertDialog(res?.message || '修改失败') }
    }
  } catch (e) {
    console.error('操作失败:', e)
    showAlertDialog('操作失败: ' + (e.message || '网络错误'))
  }
}

const deleteCurrent = async () => {
  if (!currentRecord.value) { showAlertDialog('请先在表格中选择一条记录'); return }
  if (!confirm(`确定要删除该库存记录吗？\n库存编号: ${currentRecord.value.inventoryCode}\n此操作不可恢复。`)) return
  try {
    const res = await api(`/inventory/${currentRecord.value.id}`, { method: 'DELETE' })
    if (res && res.code === 200) {
      showAlertDialog('删除成功')
      await loadData()
    } else { showAlertDialog(res?.message || '删除失败') }
  } catch (e) {
    console.error('删除失败:', e)
    showAlertDialog('删除失败: ' + (e.message || '网络错误'))
  }
}

const batchDelete = () => {
  if (checkedRows.value.length === 0) { showAlertDialog('请先在表格中勾选要删除的数据'); return }
  const codeName = route.params.codeName
  showConfirmDialog(`确定要删除选中的 ${checkedRows.value.length} 条记录吗？\n此操作不可恢复。`, async () => {
    try {
      const ids = checkedRows.value.map(r => r.id)
      const res = await api(`/inventory/${codeName}/items`, {
        method: 'DELETE',
        body: JSON.stringify(ids)
      })
      if (res && res.code === 200) showAlertDialog(`批量删除成功，共删除 ${ids.length} 条记录`)
      else showAlertDialog(res?.message || '批量删除失败')
      await loadData()
    } catch (e) {
      showAlertDialog('批量删除失败: ' + (e.message || '网络错误'))
    }
  })
}

const batchSubmit = () => {
  if (checkedRows.value.length === 0) { showAlertDialog('请先勾选要提交的记录'); return }
  const unsubmitted = checkedRows.value.filter(r => r.submitted !== 1)
  const alreadySubmitted = checkedRows.value.length - unsubmitted.length
  if (unsubmitted.length === 0) {
    showAlertDialog(`选中的 ${checkedRows.value.length} 条记录均已提交过，无需重复提交`)
    return
  }
  let msg = `本次共 ${unsubmitted.length} 条需要提交`
  if (alreadySubmitted > 0) {
    msg += `，${alreadySubmitted} 条已提交过（将跳过）`
  }
  msg += `\n提交后将计入总库存。`
  showConfirmDialog(msg, async () => {
    submitting.value = true
    try {
      const ids = unsubmitted.map(r => r.id)
      const res = await api('/inventory/submit', { method: 'PUT', body: JSON.stringify(ids) })
      if (res && res.code === 200) {
        checkedRows.value = []
        showAlertDialog(res.message || `提交成功，共 ${ids.length} 条`)
        await loadData()
      } else {
        showAlertDialog(res?.message || '提交失败')
      }
    } catch (e) {
      showAlertDialog('提交失败: ' + (e.message || '网络错误'))
    } finally {
      submitting.value = false
    }
  })
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
    const codeName = route.params.codeName
    if (codeName) params.push(`codeName=${encodeURIComponent(codeName)}`)
    if (searchKeyword.value) params.push(`keyword=${encodeURIComponent(searchKeyword.value)}`)
    const res = await api(`/inventory?${params.join('&')}`)
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
const areaDragRowIdSet = computed(() => {
  if (!areaDragging.value || !areaDragField.value) return null
  const data = filteredTableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaDragStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaDragEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return null
  const min = Math.min(sIdx, eIdx); const max = Math.max(sIdx, eIdx)
  const set = new Set()
  for (let i = min; i <= max; i++) set.add(data[i].id)
  return set
})
const areaSelectedRowIdSet = computed(() => {
  if (!areaSelectedColumn.value) return null
  const data = filteredTableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return null
  const min = Math.min(sIdx, eIdx); const max = Math.max(sIdx, eIdx)
  const set = new Set()
  for (let i = min; i <= max; i++) set.add(data[i].id)
  return set
})
const STYLE_DEFAULT = { textAlign: 'center', fontSize: '26px' }
const STYLE_DRAG = { textAlign: 'center', fontSize: '26px', background: '#e3f2fd', outline: '2px solid #4285f4', outlineOffset: '-2px' }
const STYLE_SELECTED = { textAlign: 'center', fontSize: '26px', background: '#dceefb', outline: '2px solid #4285f4', outlineOffset: '-2px' }
const gridCellStyle = ({ row, column }) => {
  if (isColumnDragging.value) return STYLE_DEFAULT
  if (!areaDragging.value && !areaSelectedColumn.value) return STYLE_DEFAULT
  void areaRenderTick.value
  const field = (column && (column.field || column.type)) || ''
  if (areaDragging.value && field === areaDragField.value) {
    const set = areaDragRowIdSet.value
    if (set && row && set.has(row.id)) return STYLE_DRAG
  }
  if (areaSelectedColumn.value && field === areaSelectedColumn.value) {
    const set = areaSelectedRowIdSet.value
    if (set && row && set.has(row.id)) return STYLE_SELECTED
  }
  return STYLE_DEFAULT
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
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return
  const lastIdx = Math.max(sIdx, eIdx); const lastId = String(data[lastIdx].id)
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
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return []
  const min = Math.min(sIdx, eIdx); const max = Math.max(sIdx, eIdx)
  return data.slice(min, max + 1).map(r => ({ id: r.id, value: r[areaSelectedColumn.value] }))
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
  if (resizeObserver) { resizeObserver.disconnect(); resizeObserver = null }
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

<style scoped>
.inventory-form-card {
  padding-top: 70px;
}
.inventory-image-box {
  margin-top: -54px;
}
</style>
