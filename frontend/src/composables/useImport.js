import { ref, reactive, computed, markRaw } from 'vue'
import { api } from '@/api'
import ExcelParserWorker from '@/workers/excelParser.worker.js?worker'
import { useAreaSelection } from '@/composables/useAreaSelection'

const HEADER_TO_FIELD = {
  '厂商编号': 'manufacturerCode', '公司编号': 'sampleCode', '种类编号': 'categoryCode',
  '种类名称': 'category', '样品名称': 'sampleName', '英文名称': 'englishName',
  '出厂货号': 'factoryCode', '货号': 'factoryCode',
  '样品单位': 'sampleUnit', '样品英文单位': 'sampleUnitEn',
  '中文包装': 'originalPackagingCn', '原始中文包装': 'originalPackagingCn', '英文包装': 'packagingEn', '包装编号': 'packageCode',
  '出厂价': 'factoryPrice', '价格': 'factoryPrice', '单价': 'factoryPrice',
  '税点价': 'taxPrice', '样品长度': 'sampleLength', '样品宽度': 'sampleWidth', '样品高度': 'sampleHeight',
  '样品毛重': 'sampleGrossWeight', '样品净重': 'sampleNetWeight',
  '外箱长度': 'cartonLength', '外箱宽度': 'cartonWidth', '外箱高度': 'cartonHeight',
  '内盒个数': 'innerBoxCount', '内盒': 'innerBoxCount',
  '外箱装量': 'cartonCapacity', '装箱量': 'cartonCapacity', '装量': 'cartonCapacity', '装箱数': 'cartonCapacity',
  '内盒/装箱': '_innerBoxCapacity', '内盒/装箱数': '_innerBoxCapacity', '内盒/装箱量': '_innerBoxCapacity', '内盒个数/外箱装量': '_innerBoxCapacity', '内盒数/外箱装量': '_innerBoxCapacity', '内盒数/装量': '_innerBoxCapacity',
  '装箱单位': 'packingUnit', '外箱毛重': 'cartonGrossWeight', '外箱净重': 'cartonNetWeight',
  '毛重': 'cartonGrossWeight',
  '包装长度': 'packageLength', '包装宽度': 'packageWidth', '包装高度': 'packageHeight',
  '产品认证': 'certification', '证书': 'certification', '认证总数': 'certificationCount', '颜色': 'color',
  '英文颜色': 'colorEn', '备注': 'remark', '英文备注': 'remarkEn',
  '登记人': 'registrant', '修改人': 'modifier', '侵权': 'infringement',
  '登记日期': 'createTime', '录入日期': 'createTime',
  '电池信息': 'batteryInfo', '电话/信息': 'phone1',
  '不在小竹熊显示': 'hideFromXzx', '是否不在小竹熊显示': 'hideFromXzx',
  '品名': 'sampleName', '产品名称': 'sampleName',
  '包装': 'originalPackagingCn',
  '包装规格': '_pkgDimensions', '包装尺寸': '_pkgDimensions',
  '外箱规格': '_cartonDimensions', '外箱尺寸': '_cartonDimensions', '规格': '_cartonDimensions', '箱规': '_cartonDimensions',
  '产品规格': '_productDimensions', '产品尺寸': '_productDimensions', '尺寸': '_productDimensions', '样品尺寸': '_productDimensions',
  '毛/净重': '_grossNetWeight', '毛净重': '_grossNetWeight', '外箱毛重/净重': '_grossNetWeight', '外箱毛/净重': '_grossNetWeight',
  '产品毛/净重': '_sampleGrossNetWeight', '产品毛重': 'sampleGrossWeight', '产品净重': 'sampleNetWeight',
}

function resolveHeader(rawHeader) {
  const cleaned = rawHeader.replace(/\s+/g, '')
  if (HEADER_TO_FIELD[cleaned]) return HEADER_TO_FIELD[cleaned]
  if (HEADER_TO_FIELD[rawHeader]) return HEADER_TO_FIELD[rawHeader]
  // 含 / 的复合表头模糊匹配
  if (cleaned.includes('/')) {
    const parts = cleaned.split('/')
    // 内盒/装箱类：包含"内盒"和"装"→_innerBoxCapacity
    if (parts.some(p => p.includes('内盒')) && parts.some(p => p.includes('装'))) return '_innerBoxCapacity'
    // 毛/净重类：包含"毛"和"净"→_grossNetWeight（排除"产品"）
    if (!cleaned.includes('产品') && parts.some(p => p.includes('毛')) && parts.some(p => p.includes('净'))) return '_grossNetWeight'
    // 产品毛/净重
    if (cleaned.includes('产品') && cleaned.includes('毛') && cleaned.includes('净')) return '_sampleGrossNetWeight'
  }
  return null
}

function splitDimensions(raw) {
  if (!raw) return null
  const cleaned = raw.toString().trim().replace(/cm/gi, '')
  const parts = cleaned.split(/[*xX×\/]/).map(s => s.trim()).filter(Boolean)
  if (parts.length >= 3) {
    const [l, w, h] = parts.map(Number)
    if (!isNaN(l) && !isNaN(w) && !isNaN(h)) return { l, w, h }
  }
  return null
}

function stripUnit(raw) {
  if (raw == null || raw === '') return raw
  // 去除常见单位后缀：数量/重量/价格
  return String(raw).trim().replace(/^(.*?)\s*(pcs|个|条|只|件|套|双|对|箱|包|袋|盒|张|片|付|打|kg|Kg|KG|kgs|Kgs|KGS|kG|g|G|元|￥)\s*$/i, '$1')
}

function splitGrossNet(raw) {
  if (!raw) return null
  const parts = raw.toString().trim().split('/').map(s => stripUnit(s.trim())).filter(Boolean)
  if (parts.length >= 2) {
    const [a, b] = [Number(parts[0]), Number(parts[1])]
    if (!isNaN(a) && !isNaN(b)) {
      // 自动纠正：毛重始终取大值，净重取小值
      return { gross: Math.max(a, b), net: Math.min(a, b) }
    }
  }
  return null
}

function splitInnerBoxCapacity(raw) {
  if (!raw) return null
  const parts = raw.toString().trim().split('/').map(s => stripUnit(s.trim())).filter(Boolean)
  if (parts.length === 1) {
    // 单值默认填入外箱装量
    const v = Number(parts[0])
    if (!isNaN(v)) return { innerBox: undefined, capacity: v }
    return null
  }
  if (parts.length >= 2) {
    const [a, b] = [Number(parts[0]), Number(parts[1])]
    if (!isNaN(a) && !isNaN(b)) {
      // 自动纠正：内盒数取小值，装箱量取大值
      return { innerBox: Math.min(a, b), capacity: Math.max(a, b) }
    }
  }
  return null
}

function formatDateForApi(raw) {
  if (raw == null || raw === '') return null
  // Excel 日期可能是数字(序列号)或字符串
  let d
  if (typeof raw === 'number') {
    // Excel 日期序列号 (1900-01-01 为第1天)
    d = new Date((raw - 25569) * 86400 * 1000)
  } else {
    d = new Date(raw)
  }
  if (isNaN(d.getTime())) return null
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${mi}:${s}`
}

function applySplits(rowObj) {
  if (rowObj._pkgDimensions) {
    const dim = splitDimensions(rowObj._pkgDimensions)
    if (dim) { rowObj.packageLength = dim.l; rowObj.packageWidth = dim.w; rowObj.packageHeight = dim.h; rowObj.originalPackagingCn = rowObj.originalPackagingCn || rowObj._pkgDimensions }
    delete rowObj._pkgDimensions
  }
  if (rowObj._cartonDimensions) {
    const dim = splitDimensions(rowObj._cartonDimensions)
    if (dim) { rowObj.cartonLength = dim.l; rowObj.cartonWidth = dim.w; rowObj.cartonHeight = dim.h }
    delete rowObj._cartonDimensions
  }
  if (rowObj._productDimensions) {
    const dim = splitDimensions(rowObj._productDimensions)
    if (dim) { rowObj.sampleLength = dim.l; rowObj.sampleWidth = dim.w; rowObj.sampleHeight = dim.h }
    delete rowObj._productDimensions
  }
  if (rowObj._grossNetWeight) {
    const gn = splitGrossNet(rowObj._grossNetWeight)
    if (gn) { rowObj.cartonGrossWeight = gn.gross; rowObj.cartonNetWeight = gn.net }
    delete rowObj._grossNetWeight
  }
  if (rowObj._sampleGrossNetWeight) {
    const gn = splitGrossNet(rowObj._sampleGrossNetWeight)
    if (gn) { rowObj.sampleGrossWeight = gn.gross; rowObj.sampleNetWeight = gn.net }
    delete rowObj._sampleGrossNetWeight
  }
  if (rowObj._innerBoxCapacity) {
    const ib = splitInnerBoxCapacity(rowObj._innerBoxCapacity)
    if (ib) { if (ib.innerBox !== undefined) rowObj.innerBoxCount = ib.innerBox; rowObj.cartonCapacity = ib.capacity }
    delete rowObj._innerBoxCapacity
  }
}

const EDIT_RENDER = { name: 'input' }

const IMPORT_PREVIEW_ALL_COLUMNS = [
  { type: 'checkbox', width: 44, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'manufacturerCode', title: '厂商编号', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'sampleCode', title: '公司编号', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'categoryCode', title: '种类编号', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'category', title: '种类名称', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'sampleName', title: '样品名称', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'englishName', title: '英文名称', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'factoryCode', title: '出厂货号', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'infringement', title: '侵权', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'batteryInfo', title: '电池信息', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'hideFromXzx', title: '不在小竹熊显示', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packageCode', title: '包装编号', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packagingCn', title: '中文包装', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packagingEn', title: '英文包装', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'factoryPrice', title: '价格', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'sampleLength', title: '样品长度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'sampleWidth', title: '样品宽度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'sampleHeight', title: '样品高度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packageLength', title: '包装长度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packageWidth', title: '包装宽度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packageHeight', title: '包装高度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonLength', title: '外箱长度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonWidth', title: '外箱宽度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonHeight', title: '外箱高度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'sampleGrossWeight', title: '样品毛重', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'sampleNetWeight', title: '样品净重', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonMaterialVolume', title: '外箱材积', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonVolume', title: '外箱体积', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'innerBoxCount', title: '内盒个数', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonCapacity', title: '外箱装量', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonGrossWeight', title: '外箱毛重', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonNetWeight', title: '外箱净重', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'certification', title: '产品认证', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'remark', title: '备注', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'originalPackagingCn', title: '原始中文包装', width: 200, showOverflow: true, className: 'col-original-pkg', editRender: EDIT_RENDER, visible: false },
  { field: 'sampleUnit', title: '样品单位', width: 200, visible: false },
  { field: 'sampleUnitEn', title: '英文单位', width: 200, visible: false },
  { field: 'taxPrice', title: '税点价', width: 200, visible: false },
  { field: 'color', title: '颜色', width: 200, visible: false },
  { field: 'colorEn', title: '英文颜色', width: 200, visible: false },
  { field: 'packingUnit', title: '装箱单位', width: 200, visible: false },
  { field: 'name', title: '厂商名称', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true, visible: false },
  { field: 'boothNo', title: '摊位号', width: 200, visible: false },
  { field: 'contact1', title: '联系人', width: 200, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'phone1', title: '电话', width: 200, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'mobile1', title: '手机', width: 200, visible: false },
  { field: 'visitorMobile', title: '见客手机', width: 200, visible: false },
  { field: 'fax', title: '传真', width: 200, visible: false },
  { field: 'qq', title: 'QQ', width: 200, visible: false },
  { field: 'certificationCount', title: '认证数', width: 200, visible: false },
  { field: 'remarkEn', title: '英文备注', width: 200, visible: false },
  { field: 'registrant', title: '登记人', width: 200, visible: false },
  { title: '校验警告', minWidth: 140, fixed: 'right', slots: { default: 'import_warnings' } },
  { field: '__action', title: '操作', width: 150, fixed: 'right', slots: { default: 'import_action' } }
]

const INFRINGEMENT_MAP = { '1': '侵权', '2': '不侵权' }
const HEADER_FIELD_KEYS = Object.keys(HEADER_TO_FIELD).filter(h => !HEADER_TO_FIELD[h].startsWith('_'))
const BATCH_SIZE = 50
const IMPORT_PREVIEW_EDIT_CONFIG = { trigger: 'dblclick', mode: 'cell', showStatus: true, enabled: true, keepSource: true }

const batchEditFields = [
  { value: 'packagingCn', label: '包装方式' },
  { value: 'category', label: '种类名称' },
  { value: 'factoryCode', label: '出厂货号' },
  { value: 'factoryPrice', label: '出厂价' },
  { value: 'cartonCapacity', label: '装箱量' },
  { value: 'innerBoxCount', label: '内盒数' },
  { value: 'certification', label: '产品认证' },
  { value: 'infringement', label: '是否侵权' },
  { value: 'batteryInfo', label: '电池信息' },
  { value: 'manufacturerCode', label: '厂商编号' },
  { value: 'sampleName', label: '样品名称' }
]

export function useImport(loadTableData, showBatchResultModal, batchResult, showToast, baiduTranslateBatch) {
  const showImportModal = ref(false)
  const importFile = ref(null)
  const importUploading = ref(false)
  const showImportPreview = ref(false)
  const importPreviewAllRows = ref([])
  const importPreviewData = ref([])
  const importOriginalData = ref([])
  const importPreviewHeaders = ref([])
  const importSelectedRows = ref([])
  const importSelectedRowIndexes = ref(new Set())
  const importPreviewGridRef = ref(null)
  const importPreviewWrapRef = ref(null)
  const dupDetailGridRef = ref(null)
  const importPreviewPage = ref(1)
  const importPreviewPageSize = ref(3000)

  const importArea = useAreaSelection({
    wrapperRef: importPreviewWrapRef,
    gridRef: importPreviewGridRef,
    tableData: importPreviewAllRows,
    keyField: '_rowIndex',
    handleClass: 'import-area-handle',
    selectingClass: 'import-area-selecting'
  })

  const batchEditField = ref('packagingCn')
  const batchEditValue = ref('')
  const batchEditDropdownOpen = ref(false)

  const showImportConfirmModal = ref(false)
  const importConfirmCount = ref(0)
  const importProgress = ref(0)
  const importUpdateMode = ref(false)
  const importParsing = ref(false)
  const importParsingProgress = ref(0)
  const importParsingStage = ref('')
  const importProgressText = ref('')
  const importPreviewCatFilter = ref(false)
  const importPreviewPkgFilter = ref(false)
  const importPreviewDupFilter = ref(false)

  const showDupDetailModal = ref(false)
  const dupDetailRows = ref([])
  const dupDetailCurrentRow = ref(null)
  const dupDetailSelectedId = ref(null)
  const showDupImageModal = ref(false)
  const dupImageUrl = ref('')

  const DUP_DETAIL_COLUMNS = [
    { type: 'checkbox', width: 60, fixed: 'left' },
    { field: '_thumb', title: '图片', width: 80, fixed: 'left', slots: { default: 'dup_detail_image' } },
  ]
  // 基于导入预览全字段构建，默认隐藏大部分，列管理可选
  const DUP_VISIBLE_DEFAULT = new Set(['_thumb', 'manufacturerCode', 'sampleCode', 'factoryCode', 'sampleName', 'category', 'packagingCn', 'factoryPrice'])
  IMPORT_PREVIEW_ALL_COLUMNS.forEach(col => {
    if (col.type === 'checkbox' || col.type === 'seq') return
    if (!col.field && col.type !== 'checkbox') return // 校验警告/操作列跳过
    if (col.field) {
      const isVisible = DUP_VISIBLE_DEFAULT.has(col.field)
      DUP_DETAIL_COLUMNS.push({
        field: col.field,
        title: col.title,
        width: 160,
        align: 'center',
        showOverflow: 'tooltip',
        visible: col.visible !== undefined ? (isVisible ? true : (col.visible === true)) : isVisible,
        sortable: false
      })
    }
  })

  const importValidCatNames = ref(new Set())
  const importValidPkgNames = ref(new Set())
  const importPkgList = ref([])
  const importCatList = ref([])

  const importPreviewDisplayData = computed(() => importPreviewData.value)

  const importPreviewFilteredCount = computed(() => {
    let list = importPreviewAllRows.value
    if (importPreviewCatFilter.value) list = list.filter(r => r._status === 'cat_error')
    if (importPreviewPkgFilter.value) list = list.filter(r => r._status === 'pkg_warning' || r._status === 'cat_error')
    if (importPreviewDupFilter.value) list = list.filter(r => r._status === 'dup_warning')
    return list.length
  })

  const importPreviewTotalPages = computed(() =>
    Math.ceil(importPreviewFilteredCount.value / importPreviewPageSize.value) || 1
  )

  const syncPreviewPage = () => {
    let list = importPreviewAllRows.value
    if (importPreviewCatFilter.value) list = list.filter(r => r._status === 'cat_error')
    if (importPreviewPkgFilter.value) list = list.filter(r => r._status === 'pkg_warning' || r._status === 'cat_error')
    if (importPreviewDupFilter.value) list = list.filter(r => r._status === 'dup_warning')
    const totalPages = Math.ceil(list.length / importPreviewPageSize.value) || 1
    if (importPreviewPage.value > totalPages) importPreviewPage.value = totalPages
    const start = (importPreviewPage.value - 1) * importPreviewPageSize.value
    importPreviewData.value = list.slice(start, start + importPreviewPageSize.value)
  }

  const onPreviewPageSizeChange = (size) => {
    importPreviewPageSize.value = size
    importPreviewPage.value = 1
    syncPreviewPage()
  }

  const onPreviewPageChange = (page) => {
    importPreviewPage.value = page
    syncPreviewPage()
  }

  const onTogglePreviewFilter = (type) => {
    if (type === 'cat') importPreviewCatFilter.value = !importPreviewCatFilter.value
    else if (type === 'pkg') importPreviewPkgFilter.value = !importPreviewPkgFilter.value
    else if (type === 'dup') importPreviewDupFilter.value = !importPreviewDupFilter.value
    importPreviewPage.value = 1
    syncPreviewPage()
  }

  const parseExcelFile = (file, parsingStartTime) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = async (e) => {
        const arrayBuf = e.target.result
        const worker = new ExcelParserWorker()

        worker.onmessage = async (ev) => {
          const msg = ev.data
          if (msg.type === 'progress') {
            importParsingStage.value = msg.stage
            importParsingProgress.value = msg.progress
          } else if (msg.type === 'error') {
            worker.terminate()
            importParsing.value = false
            importParsingProgress.value = 0
            importParsingStage.value = ''
            showToast(msg.message || 'Excel 解析失败', 'warn')
            resolve()
          } else if (msg.type === 'result') {
            worker.terminate()
            const { jsonData } = msg

            try {
              importParsingStage.value = '正在检测表头...'
              importParsingProgress.value = 55
              if (jsonData.length === 0) {
                importParsing.value = false
                importParsingProgress.value = 0
                importParsingStage.value = ''
                showToast('Excel 文件为空', 'warn')
                resolve()
                return
              }

              let bestRow = 0, bestMatch = 0
              let bestHeaders = []
              const scanLimit = Math.min(5, jsonData.length)
              for (let r = 0; r < scanLimit; r++) {
                const candidate = jsonData[r]
                if (!candidate || candidate.every(c => !c)) continue
                let match = 0
                const tentative = candidate.map(c => String(c || '').trim())
                tentative.forEach(h => { if (h && resolveHeader(h)) match++ })
                if (match > bestMatch) {
                  bestMatch = match
                  bestRow = r
                  bestHeaders = tentative
                }
              }
              const headers = bestHeaders
              const dataStartRow = bestRow + 1
              importPreviewHeaders.value = headers

              const rows = []
              const totalDataRows = jsonData.length - dataStartRow
              importParsingStage.value = `正在提取数据 (${totalDataRows} 行)...`
              importParsingProgress.value = 65
              for (let i = dataStartRow; i < jsonData.length; i++) {
                const rawRow = jsonData[i]
                if (!rawRow || rawRow.every(cell => !cell && cell !== 0)) continue
                const rowObj = { _rowIndex: i, _status: 'pending', _warnings: [] }
                headers.forEach((header, idx) => {
                  const fieldName = resolveHeader(header)
                  if (fieldName) rowObj[fieldName] = rawRow[idx] != null ? String(rawRow[idx]).trim() : ''
                })
                applySplits(rowObj)
                if (rowObj.innerBoxCount != null && rowObj.innerBoxCount !== '') rowObj.innerBoxCount = stripUnit(rowObj.innerBoxCount)
                if (rowObj.cartonCapacity != null && rowObj.cartonCapacity !== '') rowObj.cartonCapacity = stripUnit(rowObj.cartonCapacity)
                if (rowObj.sampleGrossWeight != null && rowObj.sampleGrossWeight !== '') rowObj.sampleGrossWeight = stripUnit(rowObj.sampleGrossWeight)
                if (rowObj.sampleNetWeight != null && rowObj.sampleNetWeight !== '') rowObj.sampleNetWeight = stripUnit(rowObj.sampleNetWeight)
                if (rowObj.cartonGrossWeight != null && rowObj.cartonGrossWeight !== '') rowObj.cartonGrossWeight = stripUnit(rowObj.cartonGrossWeight)
                if (rowObj.cartonNetWeight != null && rowObj.cartonNetWeight !== '') rowObj.cartonNetWeight = stripUnit(rowObj.cartonNetWeight)
                if (rowObj.factoryPrice != null && rowObj.factoryPrice !== '') rowObj.factoryPrice = stripUnit(rowObj.factoryPrice)
                if (rowObj.taxPrice != null && rowObj.taxPrice !== '') rowObj.taxPrice = stripUnit(rowObj.taxPrice)
                // 自动纠正单独列：毛重始终 ≥ 净重，内盒数始终 ≤ 装箱量
                const rawCGW = Number(rowObj.cartonGrossWeight), rawCNW = Number(rowObj.cartonNetWeight)
                if (!isNaN(rawCGW) && !isNaN(rawCNW) && rawCGW < rawCNW) { rowObj.cartonGrossWeight = rawCNW; rowObj.cartonNetWeight = rawCGW }
                const rawSGW = Number(rowObj.sampleGrossWeight), rawSNW = Number(rowObj.sampleNetWeight)
                if (!isNaN(rawSGW) && !isNaN(rawSNW) && rawSGW < rawSNW) { rowObj.sampleGrossWeight = rawSNW; rowObj.sampleNetWeight = rawSGW }
                const rawIB = Number(rowObj.innerBoxCount), rawCap = Number(rowObj.cartonCapacity)
                if (!isNaN(rawIB) && !isNaN(rawCap) && rawIB > rawCap) { rowObj.innerBoxCount = rawCap; rowObj.cartonCapacity = rawIB }
                if (rowObj.innerBoxCount == null || rowObj.innerBoxCount === '') rowObj.innerBoxCount = 0
                if (rowObj.cartonCapacity == null || rowObj.cartonCapacity === '') rowObj.cartonCapacity = 0
                if (rowObj.sampleLength == null || rowObj.sampleLength === '') rowObj.sampleLength = 0
                if (rowObj.sampleWidth == null || rowObj.sampleWidth === '') rowObj.sampleWidth = 0
                if (rowObj.sampleHeight == null || rowObj.sampleHeight === '') rowObj.sampleHeight = 0
                if (rowObj.packageLength == null || rowObj.packageLength === '') rowObj.packageLength = 0
                if (rowObj.packageWidth == null || rowObj.packageWidth === '') rowObj.packageWidth = 0
                if (rowObj.packageHeight == null || rowObj.packageHeight === '') rowObj.packageHeight = 0
                if (rowObj.cartonLength == null || rowObj.cartonLength === '') rowObj.cartonLength = 0
                if (rowObj.cartonWidth == null || rowObj.cartonWidth === '') rowObj.cartonWidth = 0
                if (rowObj.cartonHeight == null || rowObj.cartonHeight === '') rowObj.cartonHeight = 0
                // 材积/体积忽略Excel导入值，始终根据外箱规格自动计算
                const cl = Number(rowObj.cartonLength), cw = Number(rowObj.cartonWidth), ch = Number(rowObj.cartonHeight)
                const hasCartonDims = !isNaN(cl) && !isNaN(cw) && !isNaN(ch)
                const allZero = cl === 0 || cw === 0 || ch === 0
                if (allZero) {
                  rowObj.cartonVolume = 0
                  rowObj.cartonMaterialVolume = 0
                } else if (hasCartonDims) {
                  rowObj.cartonVolume = Math.round((cl * cw * ch) / 1000000 * 100) / 100
                  rowObj.cartonMaterialVolume = Math.round((cl * cw * ch) / 28339.2 * 100) / 100
                }
                rows.push(markRaw(rowObj))
              }

              // 根据厂商编号查询厂商库，补全厂商名称/摊位号/联系人/电话/手机/传真/QQ
              // Excel 中的厂商信息列已被屏蔽，全部以厂商库数据为准
              const mfrCodes = [...new Set(rows.map(r => r.manufacturerCode).filter(Boolean))]
              if (mfrCodes.length > 0) {
                importParsingStage.value = '正在加载厂商信息...'
                importParsingProgress.value = 68
                try {
                  const mfrRes = await api('/manufacturers?current=1&size=10000')
                  if (mfrRes?.code === 200 && mfrRes.data?.records) {
                    const mfrMap = {}
                    mfrRes.data.records.forEach(m => {
                      if (m.manufacturerCode) mfrMap[m.manufacturerCode] = m
                    })
                    let enriched = 0
                    let notFound = 0
                    rows.forEach(row => {
                      if (!row.manufacturerCode) return
                      const mfr = mfrMap[row.manufacturerCode]
                      if (!mfr) {
                        row._warnings.push(`厂商编号「${row.manufacturerCode}」不存在，此条资料将无法导入`)
                        row._status = 'cat_error'
                        notFound++
                        return
                      }
                      row.name = mfr.name || ''
                      row.boothNo = mfr.boothNo || ''
                      row.contact1 = mfr.contact1 || ''
                      row.phone1 = mfr.phone1 || ''
                      row.mobile1 = mfr.mobile1 || ''
                      row.visitorMobile = mfr.visitorMobile || ''
                      row.qq = mfr.qq || ''
                      enriched++
                    })
                    if (enriched > 0) showToast(`已从厂商库补全 ${enriched} 条厂商信息`, 'success')
                    if (notFound > 0) showToast(`${notFound} 条厂商编号在厂商库中不存在，已标记为不可导入`, 'warning')
                  }
                } catch (e) { console.warn('厂商信息加载失败', e) }
              }

              // 检查货号是否与该厂商已有资料重复（仅针对：有厂商编号但无公司编号的新增行）
              importParsingStage.value = '正在检查货号重复...'
              importParsingProgress.value = 69
              const rowsToCheckDup = rows.filter(r => r.manufacturerCode && !r.sampleCode && r.factoryCode && r._status !== 'cat_error')
              if (rowsToCheckDup.length > 0) {
                const mfrFactoryCodes = {}
                rowsToCheckDup.forEach(r => {
                  const mfr = r.manufacturerCode
                  const fc = String(r.factoryCode).trim()
                  if (!mfrFactoryCodes[mfr]) mfrFactoryCodes[mfr] = new Set()
                  mfrFactoryCodes[mfr].add(fc)
                })
                let dupCount = 0
                for (const [mfrCode, fcSet] of Object.entries(mfrFactoryCodes)) {
                  try {
                    const matchRes = await api('/samples/match-by-codes', {
                      method: 'POST',
                      body: JSON.stringify({ type: 'factoryCode', manufacturerCode: mfrCode, codes: [...fcSet] })
                    })
                    if (matchRes?.code === 200 && Array.isArray(matchRes.data)) {
                      const existingFcs = new Set(matchRes.data.map(s => s.factoryCode?.trim()).filter(Boolean))
                      rowsToCheckDup.forEach(row => {
                        if (row.manufacturerCode !== mfrCode) return
                        const fc = String(row.factoryCode).trim()
                        if (existingFcs.has(fc)) {
                          row._warnings.push(`货号「${fc}」与该厂商已有资料重复`)
                          row._status = 'dup_warning'
                          row._dupRecords = matchRes.data.filter(s => s.factoryCode?.trim() === fc)
                          dupCount++
                        }
                      })
                    }
                  } catch (e) { console.warn('货号重复检查失败', mfrCode, e) }
                }
                if (dupCount > 0) showToast(`${dupCount} 条数据的货号与厂商现有资料重复，已标记为绿色高亮`, 'info')
              }

              // 检查公司编号是否已有重复（所有带公司编号的行）
              importParsingStage.value = '正在检查公司编号重复...'
              importParsingProgress.value = 69
              const rowsWithSampleCode = rows.filter(r => r.sampleCode && String(r.sampleCode).trim())
              if (rowsWithSampleCode.length > 0) {
                const sampleCodes = [...new Set(rowsWithSampleCode.map(r => String(r.sampleCode).trim()))]
                try {
                  const matchRes = await api('/samples/match-by-codes', {
                    method: 'POST',
                    body: JSON.stringify({ type: 'sampleCode', codes: sampleCodes })
                  })
                  if (matchRes?.code === 200 && Array.isArray(matchRes.data)) {
                    const existingScs = new Set(matchRes.data.map(s => s.sampleCode?.trim()).filter(Boolean))
                    let scDupCount = 0
                    rowsWithSampleCode.forEach(row => {
                      const sc = String(row.sampleCode).trim()
                      if (existingScs.has(sc)) {
                        row._warnings.push(`公司编号「${sc}」已存在，导入后将覆盖已有数据`)
                        if (row._status !== 'cat_error') row._status = 'dup_warning'
                        row._dupRecords = matchRes.data.filter(s => s.sampleCode?.trim() === sc)
                        scDupCount++
                      }
                    })
                    if (scDupCount > 0) showToast(`${scDupCount} 条数据的公司编号已存在，将覆盖已有数据`, 'info')
                  }
                } catch (e) { console.warn('公司编号重复检查失败', e) }
              }

              importParsingStage.value = '正在加载种类/包装对照表...'
              importParsingProgress.value = 70
              let catList = [], pkgList = []
              try {
                const [catRes, pkgRes] = await Promise.all([
                  api('/product-categories/all'),
                  api('/packaging-methods/all')
                ])
                catList = Array.isArray(catRes?.data) ? catRes.data : []
                pkgList = Array.isArray(pkgRes?.data) ? pkgRes.data : []
              } catch (e) { console.warn('对照表加载失败', e) }
              const catKwsIndex = catList.map(cat => {
                const rawKws = cat.keywords || cat.name || ''
                let kws = rawKws.split(/[,，]/).map(k => k.trim().toLowerCase()).filter(Boolean)
                if (!cat.keywords && cat.name) {
                  for (let i = 0; i < cat.name.length - 1; i++) {
                    const sub = cat.name.substring(i, i + 2).toLowerCase()
                    if (!kws.includes(sub)) kws.push(sub)
                  }
                }
                return { ...cat, _kws: kws }
              })

              importParsingStage.value = '正在自动匹配种类...'
            importParsingProgress.value = 80
              let autoMatched = 0
              if (catKwsIndex.length > 0) {
                rows.forEach(row => {
                  if (row.category && row.category.trim()) return
                  const pname = (row.sampleName || '').trim().toLowerCase()
                  if (!pname) return
                  let bestMatch = null, bestLen = 0
                  for (const cat of catKwsIndex) {
                    for (const kw of cat._kws) {
                      if (pname.includes(kw) && kw.length > bestLen) {
                        bestMatch = cat
                        bestLen = kw.length
                      }
                    }
                  }
                  if (bestMatch) {
                    row.category = bestMatch.name
                    row.categoryCode = bestMatch.code
                    autoMatched++
                  }
                })
                if (autoMatched > 0) showToast(`已自动匹配 ${autoMatched} 条种类`, 'success')
              }

              importParsingStage.value = '正在自动匹配包装...'
            importParsingProgress.value = 85
              let pkgAutoMatched = 0
              if (pkgList.length > 0) {
                const pkgKwsIndex = pkgList.map(pkg => {
                  const name = (pkg.name || '').trim()
                  const kws = [name.toLowerCase()]
                  for (let i = 0; i < name.length - 1; i++) {
                    const sub = name.substring(i, i + 2).toLowerCase()
                    if (!kws.includes(sub)) kws.push(sub)
                  }
                  return { ...pkg, _kws: kws }
                })
                rows.forEach(row => {
                  if (row.packagingCn && row.packagingCn.trim()) return
                  const orig = (row.originalPackagingCn || '').trim().toLowerCase()
                  if (!orig) return
                  let bestMatch = null, bestLen = 0
                  for (const pkg of pkgKwsIndex) {
                    for (const kw of pkg._kws) {
                      if (orig.includes(kw) && kw.length > bestLen) {
                        bestMatch = pkg
                        bestLen = kw.length
                      }
                    }
                  }
                  if (bestMatch) {
                    row.packagingCn = bestMatch.name
                    row.packageCode = bestMatch.code
                    pkgAutoMatched++
                  }
                  if (!row.packagingCn) row.packagingCn = row.originalPackagingCn
                })
                if (pkgAutoMatched > 0) showToast(`已自动匹配 ${pkgAutoMatched} 条包装`, 'success')
            }

            // 百度翻译：英文名称 + 英文包装 + 英文备注（批量请求一次搞定）
            importParsingStage.value = '正在自动翻译英文...'
            importParsingProgress.value = 88
            const translateTexts = []
            const translateTargets = []
            rows.forEach(row => {
              if (!row.englishName || !row.englishName.trim()) {
                const src = (row.sampleName || '').trim()
                if (src) {
                  translateTexts.push(src)
                  translateTargets.push([row, 'englishName'])
                }
              }
              if (!row.packagingEn || !row.packagingEn.trim()) {
                const src = (row.packagingCn || row.originalPackagingCn || '').trim()
                if (src) {
                  translateTexts.push(src)
                  translateTargets.push([row, 'packagingEn'])
                }
              }
              if (!row.remarkEn || !row.remarkEn.trim()) {
                const src = (row.remark || '').trim()
                if (src) {
                  translateTexts.push(src)
                  translateTargets.push([row, 'remarkEn'])
                }
              }
            })
            if (translateTexts.length > 0) {
              importParsingStage.value = `正在自动翻译英文 (${translateTexts.length} 条)...`
              const translated = await baiduTranslateBatch(translateTexts)
              if (translated && translated.length === translateTexts.length) {
                translateTargets.forEach(([row, field], i) => {
                  row[field] = translated[i]
                })
                showToast(`已自动翻译 ${translated.length} 条英文`, 'success')
              } else {
                showToast('翻译接口异常，已跳过', 'warning')
              }
            }

            // 校验：必须要有厂商编号或公司编号
            let missingCodeCount = 0
            rows.forEach(row => {
              const hasMfrCode = row.manufacturerCode && String(row.manufacturerCode).trim()
              const hasSampleCode = row.sampleCode && String(row.sampleCode).trim()
              if (!hasMfrCode && !hasSampleCode) {
                row._warnings.push('缺少厂商编号和公司编号，至少需要一个')
                row._status = 'cat_error'
                missingCodeCount++
              }
            })
            if (missingCodeCount > 0) showToast(`${missingCodeCount} 条数据缺少厂商编号和公司编号，已标记为不可导入`, 'warning')

            // Validate categories and packaging
              importParsingStage.value = '正在校验种类/包装...'
              importParsingProgress.value = 90
              const validCatNames = new Set()
              const validPkgNames = new Set()
              catList.forEach(c => { validCatNames.add(c.name); validCatNames.add(c.name.toLowerCase()) })
              pkgList.forEach(p => { validPkgNames.add(p.name); validPkgNames.add(p.name.toLowerCase()) })
              importValidCatNames.value = validCatNames
              importValidPkgNames.value = validPkgNames
              importPkgList.value = pkgList
              importCatList.value = catList
              let catErrorCount = 0, pkgWarnCount = 0
              rows.forEach(row => {
                const catName = row.category
                const pkgName = row.packagingCn
                let hasCatErr = false, hasPkgWarn = false
                if (catName && !validCatNames.has(catName)) {
                  row._warnings.push(`种类名称「${catName}」不在对照表中`)
                  row._status = 'cat_error'
                  hasCatErr = true
                }
                if (pkgName && !validPkgNames.has(pkgName)) {
                  row._warnings.push(`中文包装「${pkgName}」不在对照表中`)
                  if (!hasCatErr && row._status !== 'dup_warning') row._status = 'pkg_warning'
                  hasPkgWarn = true
                }
                if (hasCatErr) catErrorCount++
                if (hasPkgWarn) pkgWarnCount++
              })

              importPreviewAllRows.value = rows
              importOriginalData.value = rows.map(r => markRaw({ ...r, _warnings: [...(r._warnings || [])] }))
              importParsingStage.value = '解析完成，正在渲染预览...'
              importParsingProgress.value = 95
              importSelectedRows.value = []
              importSelectedRowIndexes.value = new Set()
              importPreviewCatFilter.value = false
              importPreviewPkgFilter.value = false
              importPreviewDupFilter.value = false
              importPreviewPage.value = 1
              syncPreviewPage()
              importParsingProgress.value = 100
              importParsingStage.value = '完成'
              const elapsed = Date.now() - parsingStartTime
              const minDelay = Math.max(0, 600 - elapsed)
              setTimeout(() => {
                importParsing.value = false
                showImportModal.value = false
                showImportPreview.value = true
              }, minDelay)

              if (catErrorCount > 0 || pkgWarnCount > 0) {
                const msgs = []
                if (catErrorCount > 0) msgs.push(`${catErrorCount} 行种类名称不符`)
                if (pkgWarnCount > 0) msgs.push(`${pkgWarnCount} 行中文包装不符`)
                showToast(msgs.join('，') + '，请核实', 'warn')
              }
              resolve()
            } catch (err) {
              importParsing.value = false
              importParsingProgress.value = 0
              importParsingStage.value = ''
              console.error('解析 Excel 失败:', err)
              showToast('解析 Excel 文件失败: ' + err.message, 'error')
              reject(err)
            }
          }
        }

        worker.onerror = (err) => {
          worker.terminate()
          importParsing.value = false
          importParsingProgress.value = 0
          importParsingStage.value = ''
          console.error('Worker 错误:', err)
          showToast('Excel 解析失败', 'error')
          reject(new Error('Worker error'))
        }

        worker.postMessage({ type: 'parse', buffer: arrayBuf }, [arrayBuf])
      }
      reader.onerror = () => {
        importParsing.value = false
        importParsingProgress.value = 0
        importParsingStage.value = ''
        showToast('文件读取失败', 'error')
        reject(new Error('文件读取失败'))
      }
      reader.readAsArrayBuffer(file)
    })
  }

  const openImportModal = () => {
    importFile.value = null
    showImportModal.value = true
  }

  const onImportFileChange = async (e) => {
    const file = e.target.files[0]
    if (!file || importParsing.value) return
    importFile.value = file
    importParsing.value = true
    importParsingStage.value = '正在读取文件...'
    importParsingProgress.value = 5
    const parsingStartTime = Date.now()
    try {
      await parseExcelFile(file, parsingStartTime)
    } catch (err) {
      importParsing.value = false
      importParsingProgress.value = 0
      importParsingStage.value = ''
    }
  }

  const onImportDrop = (e) => {
    const files = e.dataTransfer.files
    if (!files || !files.length) return
    importFile.value = files[0]
    importParsing.value = true
    importParsingStage.value = '正在读取文件...'
    importParsingProgress.value = 5
    const parsingStartTime = Date.now()
    parseExcelFile(files[0], parsingStartTime).catch(() => {
      importParsing.value = false
      importParsingProgress.value = 0
      importParsingStage.value = ''
    })
  }

  const doImport = async () => {
    if (!importFile.value) return
    importUploading.value = true
    try {
      const fd = new FormData()
      fd.append('file', importFile.value)
      const res = await api('/samples/import', { method: 'POST', body: fd })
      showImportModal.value = false
      importFile.value = null
      await loadTableData()
    } catch (e) {
      console.error(e)
    } finally {
      importUploading.value = false
    }
  }

  const restorePreviewRow = (row) => {
    const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
    if (idx >= 0) {
      const orig = importOriginalData.value.find(r => r._rowIndex === row._rowIndex)
      if (orig) {
        importPreviewAllRows.value.splice(idx, 1, markRaw({ ...orig }))
        syncPreviewPage()
      }
    }
  }

  const deletePreviewRow = (row) => {
    const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
    if (idx >= 0) {
      importPreviewAllRows.value.splice(idx, 1)
      importSelectedRowIndexes.value.delete(row._rowIndex)
      importSelectedRows.value = [...importSelectedRowIndexes.value]
      syncPreviewPage()
      onImportPreviewCheckChange()
    }
  }

  const deleteSelectedPreviewRows = () => {
    if (!importPreviewGridRef.value) return
    const selectedRecords = importPreviewGridRef.value.getCheckboxRecords()
    if (selectedRecords.length === 0) return
    const rowIndexes = new Set(selectedRecords.map(r => r._rowIndex))
    importPreviewAllRows.value = importPreviewAllRows.value.filter(r => !rowIndexes.has(r._rowIndex))
    importPreviewGridRef.value.clearCheckboxRow()
    importSelectedRows.value = []
    importSelectedRowIndexes.value = new Set()
    syncPreviewPage()
  }

  const batchEditRun = async () => {
    const val = batchEditValue.value.trim()
    const field = batchEditField.value
    if (!val) return
    const selected = importPreviewAllRows.value.filter(r => importSelectedRowIndexes.value.has(r._rowIndex))
    if (selected.length === 0) return
    let matchedPkg = null
    if (field === 'packagingCn') matchedPkg = importPkgList.value.find(p => p.name.toLowerCase() === val.toLowerCase())
    let matchedCat = null
    if (field === 'category') matchedCat = importCatList.value.find(c => c.name === val)
    selected.forEach(row => {
      row[field] = val
      if (field === 'packagingCn' && matchedPkg) {
        row.packageCode = matchedPkg.code
        row.packagingEn = matchedPkg.nameEn || ''
      }
      if (field === 'category' && matchedCat) {
        row.categoryCode = matchedCat.code
      }
      // 批量修改样品名称时，自动匹配种类
      if (field === 'sampleName' && importCatList.value.length > 0) {
        const pname = val.toLowerCase()
        const catKwsIndex = importCatList.value.map(cat => {
          const rawKws = cat.keywords || cat.name || ''
          let kws = rawKws.split(/[,，]/).map(k => k.trim().toLowerCase()).filter(Boolean)
          if (!cat.keywords && cat.name) {
            for (let i = 0; i < cat.name.length - 1; i++) {
              const sub = cat.name.substring(i, i + 2).toLowerCase()
              if (!kws.includes(sub)) kws.push(sub)
            }
          }
          return { ...cat, _kws: kws }
        })
        let bestMatch = null, bestLen = 0
        for (const cat of catKwsIndex) {
          for (const kw of cat._kws) {
            if (pname.includes(kw) && kw.length > bestLen) {
              bestMatch = cat
              bestLen = kw.length
            }
          }
        }
        if (bestMatch) {
          row.category = bestMatch.name
          row.categoryCode = bestMatch.code
        }
      }
      if (field === 'packagingCn') {
        row._warnings = row._warnings.filter(w => !w.startsWith('中文包装'))
      } else if (field === 'category') {
        row._warnings = row._warnings.filter(w => !w.startsWith('种类'))
      }
      if (field === 'category') {
        const hasCatErr = val && !importValidCatNames.value.has(val)
        if (hasCatErr) {
          row._warnings.push(`种类「${val}」不在对照表中`)
          row._status = 'cat_error'
        } else {
          row._status = 'pending'
        }
      } else if (field === 'packagingCn') {
        const hasPkgWarn = val && !importValidPkgNames.value.has(val)
        if (hasPkgWarn && row._status !== 'cat_error') {
          row._warnings.push(`包装「${val}」不在对照表中`)
          row._status = 'pkg_warning'
        } else if (!hasPkgWarn && row._status === 'pkg_warning') {
          row._status = 'pending'
        }
      }
      const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
      if (idx >= 0) importPreviewAllRows.value.splice(idx, 1, markRaw({ ...row }))
    })
    syncPreviewPage()
    showToast(`已批量设置 ${selected.length} 行`, 'success')
  }

  const onImportPreviewCheckChange = () => {
    if (importPreviewGridRef.value) {
      const records = importPreviewGridRef.value.getCheckboxRecords()
      const currentPageIndexes = new Set(records.map(r => r._rowIndex))
      const currentPageRows = importPreviewData.value
      currentPageRows.forEach(r => importSelectedRowIndexes.value.delete(r._rowIndex))
      currentPageIndexes.forEach(idx => importSelectedRowIndexes.value.add(idx))
      importSelectedRows.value = [...importSelectedRowIndexes.value]
    }
  }

  const onImportCellEdit = async ({ row, column }) => {
    const field = column?.field || column?.property
    if (field !== 'category' && field !== 'packagingCn' && field !== 'originalPackagingCn'
        && field !== 'manufacturerCode' && field !== 'factoryCode' && field !== 'sampleCode'
        && field !== 'categoryCode' && field !== 'packageCode' && field !== 'sampleName') return
    if (field === 'originalPackagingCn') {
      const orig = (row.originalPackagingCn || '').trim().toLowerCase()
      const pkgList = importPkgList.value
      if (orig && pkgList.length > 0) {
        const pkgKwsIndex = pkgList.map(pkg => {
          const name = (pkg.name || '').trim()
          const kws = [name.toLowerCase()]
          for (let i = 0; i < name.length - 1; i++) {
            const sub = name.substring(i, i + 2).toLowerCase()
            if (!kws.includes(sub)) kws.push(sub)
          }
          return { ...pkg, _kws: kws }
        })
        let bestMatch = null, bestLen = 0
        for (const pkg of pkgKwsIndex) {
          for (const kw of pkg._kws) {
            if (orig.includes(kw) && kw.length > bestLen) {
              bestMatch = pkg
              bestLen = kw.length
            }
          }
        }
        if (bestMatch) {
          row.packagingCn = bestMatch.name
          row.packageCode = bestMatch.code
        }
        if (!row.packagingCn) row.packagingCn = row.originalPackagingCn
      }
    }
    // 清除对应类型的旧警告
    if (field === 'manufacturerCode' || field === 'factoryCode' || field === 'sampleCode') {
      row._warnings = row._warnings.filter(w => !w.startsWith('货号'))
      const mfr = (row.manufacturerCode || '').trim()
      const sc = (row.sampleCode || '').trim()
      const fc = (row.factoryCode || '').trim()
      if (mfr && !sc && fc) {
        importParsingStage.value = '正在重新检查货号...'
        try {
          const matchRes = await api('/samples/match-by-codes', {
            method: 'POST',
            body: JSON.stringify({ type: 'factoryCode', manufacturerCode: mfr, codes: [fc] })
          })
          if (matchRes?.code === 200 && Array.isArray(matchRes.data) && matchRes.data.length > 0) {
            row._warnings.push(`货号「${fc}」与该厂商已有资料重复`)
          }
        } catch (e) { console.warn('货号重复检查失败', e) }
        importParsingStage.value = ''
      }
    } else {
      row._warnings = row._warnings.filter(w => !w.startsWith('种类名称') && !w.startsWith('中文包装'))
    }

    // 编辑种类/包装/编号后自动重匹配
    if (field === 'category' || field === 'packagingCn' || field === 'categoryCode' || field === 'packageCode') {
      if (field === 'category') {
        const matchedCat = importCatList.value.find(c => c.name.toLowerCase() === (row.category || '').toLowerCase())
        if (matchedCat) row.categoryCode = matchedCat.code
      }
      if (field === 'categoryCode') {
        const matchedCat = importCatList.value.find(c => c.code === row.categoryCode)
        if (matchedCat) {
          row.category = matchedCat.name
          row.categoryCode = matchedCat.code
        }
      }
      if (field === 'packagingCn') {
        const matchedPkg = importPkgList.value.find(p => p.name.toLowerCase() === (row.packagingCn || '').toLowerCase())
        if (matchedPkg) {
          row.packageCode = matchedPkg.code
          row.packagingEn = matchedPkg.nameEn || ''
        }
      }
      if (field === 'packageCode') {
        const matchedPkg = importPkgList.value.find(p => p.code === row.packageCode)
        if (matchedPkg) {
          row.packagingCn = matchedPkg.name
          row.packageCode = matchedPkg.code
          row.packagingEn = matchedPkg.nameEn || ''
        }
      }
    }

    // 编辑样品名称时，如果当前没手动设过种类，重新用关键词匹配
    if (field === 'sampleName') {
      const pname = (row.sampleName || '').trim().toLowerCase()
      if (pname && importCatList.value.length > 0) {
        const catKwsIndex = importCatList.value.map(cat => {
          const rawKws = cat.keywords || cat.name || ''
          let kws = rawKws.split(/[,，]/).map(k => k.trim().toLowerCase()).filter(Boolean)
          if (!cat.keywords && cat.name) {
            for (let i = 0; i < cat.name.length - 1; i++) {
              const sub = cat.name.substring(i, i + 2).toLowerCase()
              if (!kws.includes(sub)) kws.push(sub)
            }
          }
          return { ...cat, _kws: kws }
        })
        let bestMatch = null, bestLen = 0
        for (const cat of catKwsIndex) {
          for (const kw of cat._kws) {
            if (pname.includes(kw) && kw.length > bestLen) {
              bestMatch = cat
              bestLen = kw.length
            }
          }
        }
        if (bestMatch) {
          row.category = bestMatch.name
          row.categoryCode = bestMatch.code
        }
      }
    }

    const catName = row.category
    const pkgName = row.packagingCn
    let hasCatErr = false, hasPkgWarn = false
    if (catName && !importValidCatNames.value.has(catName) && !importValidCatNames.value.has(catName.toLowerCase())) {
      row._warnings.push(`种类名称「${catName}」不在对照表中`)
      hasCatErr = true
    }
    if (pkgName && !importValidPkgNames.value.has(pkgName) && !importValidPkgNames.value.has(pkgName.toLowerCase())) {
      row._warnings.push(`中文包装「${pkgName}」不在对照表中`)
      if (!hasCatErr) hasPkgWarn = true
    }
    const hasDupWarning = row._warnings.some(w => w.startsWith('货号'))
    row._status = hasCatErr ? 'cat_error' : (hasDupWarning ? 'dup_warning' : (hasPkgWarn ? 'pkg_warning' : 'pending'))
    const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
    if (idx >= 0) importPreviewAllRows.value.splice(idx, 1, markRaw({ ...row }))
    // 不调用 syncPreviewPage()，避免编辑单元格时重建整个数组导致表格全量重渲染
    // importPreviewFilteredCount 依赖 importPreviewAllRows，splice 已触发其更新
  }

  const openDupDetail = (row) => {
    dupDetailCurrentRow.value = row
    dupDetailRows.value = (row._dupRecords || []).map(r => ({
      ...r,
      _thumb: '',
      thumbnail: r.thumbnail || r._thumb || '',
      firstImageHash: r.firstImageHash || ''
    }))
    dupDetailSelectedId.value = row._overwriteId || null
    showDupDetailModal.value = true
  }

  const confirmDupOverwrite = () => {
    if (dupDetailCurrentRow.value && dupDetailSelectedId.value) {
      const selected = dupDetailRows.value.find(r => r.id === dupDetailSelectedId.value)
      const row = dupDetailCurrentRow.value
      if (selected) {
        row.sampleCode = selected.sampleCode || ''
      }
      row._overwriteId = dupDetailSelectedId.value
      row._status = 'pending'
      row._warnings = (row._warnings || []).filter(w => !w.includes('重复') && !w.includes('已存在'))
      row._dupRecords = []
      const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
      if (idx >= 0) importPreviewAllRows.value.splice(idx, 1, markRaw({ ...row }))
      syncPreviewPage()
      showDupDetailModal.value = false
      dupDetailCurrentRow.value = null
      dupDetailRows.value = []
    } else if (!dupDetailSelectedId.value) {
      showToast('请先勾选一条要更新的记录', 'warn')
    }
  }

  const batchAbandonUpdate = () => {
    let count = 0
    const rows = importPreviewAllRows.value
    for (let i = 0; i < rows.length; i++) {
      const r = rows[i]
      if (r._status === 'dup_warning' && !r._overwriteId) {
        r._status = 'pending'
        r._warnings = (r._warnings || []).filter(w => !w.includes('重复') && !w.includes('已存在'))
        r._dupRecords = []
        r._overwriteId = undefined
        rows.splice(i, 1, markRaw({ ...r }))
        count++
      }
    }
    if (count > 0) {
      syncPreviewPage()
      showToast(`已放弃 ${count} 条重复覆盖，将以新增模式导入`, 'info')
    } else {
      showToast('没有需要放弃的重复覆盖数据', 'info')
    }
  }

  const cancelDupDetail = () => {
    if (dupDetailCurrentRow.value) {
      const row = dupDetailCurrentRow.value
      const _rowIndex = row._rowIndex
      row._status = 'pending'
      row._warnings = (row._warnings || []).filter(w => !w.includes('重复') && !w.includes('已存在'))
      row._dupRecords = []
      row._overwriteId = undefined
      // 替换数组中的引用以触发 Vue 响应式更新（行数据被 markRaw）
      const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === _rowIndex)
      if (idx >= 0) importPreviewAllRows.value.splice(idx, 1, markRaw({ ...row }))
      syncPreviewPage()
    }
    showDupDetailModal.value = false
    dupDetailCurrentRow.value = null
    dupDetailRows.value = []
  }

  const onDupDetailCheckChange = ({ records, row }) => {
    if (records.length > 1 && dupDetailGridRef.value) {
      records.forEach(r => {
        if (r.id !== row.id) dupDetailGridRef.value.setCheckboxRow(r, false)
      })
    }
    dupDetailSelectedId.value = records.length === 0 ? null : row.id
  }

  const openDupImage = (firstImageHash, thumbnail) => {
    if (firstImageHash) {
      dupImageUrl.value = '/images/view/hash/' + firstImageHash
    } else if (thumbnail) {
      dupImageUrl.value = '/thumbnails/' + thumbnail
    } else {
      return
    }
    showDupImageModal.value = true
  }

  const closeDupImage = () => {
    showDupImageModal.value = false
    dupImageUrl.value = ''
  }

  const exportSelectedRows = () => {
    if (importSelectedRows.value.length === 0) {
      showToast('请先选择要导出的行', 'warn')
      return
    }
    showToast('已选择 ' + importSelectedRows.value.length + ' 行数据准备导出（功能开发中）', 'info')
  }

  const selectAllPreviewRows = () => {
    let list = importPreviewAllRows.value
    if (importPreviewCatFilter.value) list = list.filter(r => r._status === 'cat_error')
    if (importPreviewPkgFilter.value) list = list.filter(r => r._status === 'pkg_warning' || r._status === 'cat_error')
    if (importPreviewDupFilter.value) list = list.filter(r => r._status === 'dup_warning')
    list.forEach(r => importSelectedRowIndexes.value.add(r._rowIndex))
    importSelectedRows.value = [...importSelectedRowIndexes.value]
    if (importPreviewGridRef.value) {
      importPreviewGridRef.value.setAllCheckboxRow(true)
    }
  }

  const clearPreviewSelection = () => {
    importSelectedRowIndexes.value.clear()
    importSelectedRows.value = []
    if (importPreviewGridRef.value) {
      importPreviewGridRef.value.clearCheckboxRow()
    }
  }

  const cancelImportPreview = () => {
    showImportPreview.value = false
    importPreviewAllRows.value = []
    importPreviewData.value = []
    importPreviewHeaders.value = []
    importSelectedRows.value = []
    importSelectedRowIndexes.value = new Set()
    importOriginalData.value = []
    importPkgList.value = []
    importCatList.value = []
    importPreviewCatFilter.value = false
    importPreviewPkgFilter.value = false
    importPreviewDupFilter.value = false
    importPreviewPage.value = 1
    batchEditValue.value = ''
    importFile.value = null
  }

  const buildSamplesToSend = (records) => {
    return records.map(row => {
      const sample = {}
      HEADER_FIELD_KEYS.forEach(header => {
        const field = HEADER_TO_FIELD[header]
        if (row[field] !== undefined && row[field] !== '') {
          const val = String(row[field]).trim()
          if (field === 'infringement') {
            sample[field] = INFRINGEMENT_MAP[val] || '其他'
          } else {
            sample[field] = row[field]
          }
        }
      })
      // 厂商信息字段（来自厂商库匹配，不从 Excel 提取）
      ;['name','boothNo','contact1','phone1','mobile1','visitorMobile','qq'].forEach(f => {
        if (row[f]) sample[f] = row[f]
      })
      // 内盒个数默认为 0
      sample.innerBoxCount = (row.innerBoxCount != null && row.innerBoxCount !== '') ? row.innerBoxCount : 0
      // 登记日期：有值则用导入值（转 yyyy-MM-dd HH:mm:ss），空则不传让后端自动生成
      if (row.createTime != null && row.createTime !== '') {
        sample.createTime = formatDateForApi(row.createTime)
      }
      if (row.packagingCn) sample.packagingCn = row.packagingCn
      return sample
    })
  }

  const executeImport = async () => {
    showImportConfirmModal.value = false
    const filteredData = (() => {
      let list = importPreviewAllRows.value
      if (importPreviewCatFilter.value) list = list.filter(r => r._status === 'cat_error')
      if (importPreviewPkgFilter.value) list = list.filter(r => r._status === 'pkg_warning' || r._status === 'cat_error')
      if (importPreviewDupFilter.value) list = list.filter(r => r._status === 'dup_warning')
      return list
    })()
    const recordsToImport = importConfirmCount.value === filteredData.length
      ? filteredData
      : filteredData.filter(r => importSelectedRowIndexes.value.has(r._rowIndex))
    const allSamples = buildSamplesToSend(recordsToImport)
    const total = allSamples.length

    importUploading.value = true
    importProgress.value = 0
    const isUpdateMode = importUpdateMode.value
    importProgressText.value = `准备${isUpdateMode ? '更新' : '导入'} ${total} 条数据...`

    let totalSuccess = 0, totalFail = 0, totalDuplicate = 0, totalUpdated = 0
    const allFailedRows = []

    try {
      for (let i = 0; i < allSamples.length; i += BATCH_SIZE) {
        const batch = allSamples.slice(i, i + BATCH_SIZE)
        const batchNum = Math.floor(i / BATCH_SIZE) + 1
        const totalBatches = Math.ceil(allSamples.length / BATCH_SIZE)
        importProgressText.value = `正在${isUpdateMode ? '更新' : '导入'}第 ${batchNum}/${totalBatches} 批 (${i + 1}-${Math.min(i + BATCH_SIZE, total)}/${total})...`

        const res = await api(`/samples/batch-import?updateMode=${isUpdateMode}`, {
          method: 'POST',
          body: JSON.stringify(batch),
          headers: { 'Content-Type': 'application/json' }
        })

        if (res.code === 200 || res.success) {
          totalSuccess += (res.data?.successCount || 0)
          totalFail += (res.data?.failCount || 0)
          totalDuplicate += (res.data?.duplicateCount || 0)
          totalUpdated += (res.data?.updatedCount || 0)
          if (res.data?.failedRows) allFailedRows.push(...res.data.failedRows)
        } else {
          totalFail += batch.length
          batch.forEach((s, idx) => {
            allFailedRows.push({
              row: String(i + idx + 1),
              公司编号: s.sampleCode || '',
              样品名称: s.sampleName || '',
              失败原因: res.message || '服务端返回错误',
              类型: '异常'
            })
          })
        }

        importProgress.value = Math.round(((i + batch.length) / total) * 100)
        if (i + BATCH_SIZE < allSamples.length) {
          await new Promise(r => setTimeout(r, 150))
        }
      }

      importProgressText.value = `${isUpdateMode ? '更新' : '导入'}完成！成功 ${totalSuccess} 条${totalUpdated > 0 ? `，更新 ${totalUpdated} 条` : ''}`
      importProgress.value = 100

      batchResult.successCount = totalSuccess
      batchResult.failCount = totalFail
      batchResult.duplicateCount = totalDuplicate
      batchResult.updatedCount = totalUpdated
      batchResult.unmatchedCount = 0
      batchResult.failedRows = allFailedRows
      batchResult.failList = []
      batchResult.unmatchedList = []

      setTimeout(() => {
        showBatchResultModal.value = true
        showImportPreview.value = false
        importPreviewData.value = []
        importSelectedRows.value = []
        importOriginalData.value = []
        importProgress.value = 0
        importProgressText.value = ''
        importUploading.value = false
        loadTableData()
      }, 500)
    } catch (e) {
      console.error(e)
      importUploading.value = false
      importProgress.value = 0
      showToast('导入失败: ' + (e.message || '未知错误'), 'error')
    }
  }

  const doConfirmImport = (mode) => {
    const filteredData = (() => {
      let list = importPreviewAllRows.value
      if (importPreviewCatFilter.value) list = list.filter(r => r._status === 'cat_error')
      if (importPreviewPkgFilter.value) list = list.filter(r => r._status === 'pkg_warning' || r._status === 'cat_error')
      if (importPreviewDupFilter.value) list = list.filter(r => r._status === 'dup_warning')
      return list
    })()
    const count = mode === 'all' ? filteredData.length : importSelectedRowIndexes.value.size
    if (count === 0) {
      showToast('请至少选择一行数据进行导入', 'warn')
      return
    }
    importConfirmCount.value = count
    showImportConfirmModal.value = true
  }

  const exportImportFailedRows = () => {
    const rows = batchResult.failedRows
    if (!rows || rows.length === 0) return
    const headers = ['行号', '公司编号', '样品名称', '失败原因', '类型']
    const csvLines = [headers.join(',')]
    rows.forEach(r => {
      const line = [
        `"${r.row || ''}"`,
        `"${(r['公司编号'] || '').replace(/"/g, '""')}"`,
        `"${(r['样品名称'] || '').replace(/"/g, '""')}"`,
        `"${(r['失败原因'] || '').replace(/"/g, '""')}"`,
        `"${(r['类型'] || '')}"`
      ].join(',')
      csvLines.push(line)
    })
    const BOM = '\uFEFF'
    const csvContent = BOM + csvLines.join('\n')
    const dataUrl = 'data:text/csv;charset=utf-8,' + encodeURIComponent(csvContent)
    const a = document.createElement('a')
    a.href = dataUrl
    a.download = `导入失败记录_${new Date().toLocaleDateString()}.csv`
    a.click()
  }

  return {
    showImportModal,
    importFile,
    importUploading,
    showImportPreview,
    importPreviewAllRows,
    importPreviewData,
    importOriginalData,
    importPreviewHeaders,
    importSelectedRows,
    importSelectedRowIndexes,
    importPreviewGridRef,
    importPreviewWrapRef,
    importPreviewPage,
    importPreviewPageSize,
    importArea,
    batchEditField,
    batchEditValue,
    batchEditDropdownOpen,
    batchEditFields,
    showImportConfirmModal,
    importConfirmCount,
    importProgress,
    importUpdateMode,
    importParsing,
    importParsingProgress,
    importParsingStage,
    IMPORT_PREVIEW_EDIT_CONFIG,
    importProgressText,
    importPreviewCatFilter,
    importPreviewPkgFilter,
    importPreviewDupFilter,
    importValidCatNames,
    importValidPkgNames,
    importPkgList,
    importCatList,
    importPreviewDisplayData,
    importPreviewFilteredCount,
    importPreviewTotalPages,
    IMPORT_PREVIEW_ALL_COLUMNS,
    openImportModal,
    onImportFileChange,
    onImportDrop,
    parseExcelFile,
    doConfirmImport,
    executeImport,
    buildSamplesToSend,
    exportImportFailedRows,
    doImport,
    syncPreviewPage,
    onPreviewPageSizeChange,
    onPreviewPageChange,
    onTogglePreviewFilter,
    restorePreviewRow,
    deletePreviewRow,
    deleteSelectedPreviewRows,
    batchEditRun,
    onImportPreviewCheckChange,
    onImportCellEdit,
    cancelImportPreview,
    selectAllPreviewRows,
    clearPreviewSelection,
    exportSelectedRows,
    showDupDetailModal,
    dupDetailRows,
    dupDetailGridRef,
    dupDetailSelectedId,
    showDupImageModal,
    dupImageUrl,
    DUP_DETAIL_COLUMNS,
    openDupDetail,
    confirmDupOverwrite,
    cancelDupDetail,
    batchAbandonUpdate,
    onDupDetailCheckChange,
    openDupImage,
    closeDupImage,
  }
}
