import { ref, computed, reactive } from 'vue'

const EXPORT_FIELD_CONFIG = [
  { key: 'sampleCode', label: '公司编号', default: true },
  { key: 'factoryCode', label: '出厂货号', default: true },
  { key: 'manufacturerCode', label: '厂商编号' },
  { key: 'category', label: '种类名称' },
  { key: 'sampleName', label: '样品名称', default: true },
  { key: 'englishName', label: '英文名称' },
  { key: 'factoryPrice', label: '出厂价', default: true },
  { key: 'taxPrice', label: '报出价' },
  { key: 'packagingCn', label: '包装规格' },
  { key: 'packagingEn', label: '包装规格(英)' },
  { key: 'packingUnit', label: '包装单位' },
  { key: 'innerBoxCount', label: '内盒数' },
  { key: 'cartonCapacity', label: '装箱量' },
  { key: 'cartonLength', label: '外箱长' },
  { key: 'cartonWidth', label: '外箱宽' },
  { key: 'cartonHeight', label: '外箱高' },
  { key: 'cartonGrossWeight', label: '外箱毛重' },
  { key: 'cartonNetWeight', label: '外箱净重' },
  { key: 'sampleLength', label: '产品长' },
  { key: 'sampleWidth', label: '产品宽' },
  { key: 'sampleHeight', label: '产品高' },
  { key: 'sampleGrossWeight', label: '产品毛重' },
  { key: 'sampleNetWeight', label: '产品净重' },
  { key: 'cartonVolume', label: '体积' },
  { key: 'cartonMaterialVolume', label: '材积' },
  { key: 'boothNo', label: '摊位号' },
  { key: 'name', label: '厂商名称' },
  { key: 'contact1', label: '联系人' },
  { key: 'phone1', label: '联系电话' },
  { key: 'mobile1', label: '手机' },
  { key: 'fax', label: '传真' },
  { key: 'qq', label: 'QQ' },
  { key: 'material', label: '材料' },
  { key: 'color', label: '颜色' },
  { key: 'colorEn', label: '颜色(英)' },
  { key: 'size', label: '尺寸' },
  { key: 'origin', label: '原产地' },
  { key: 'sampleUnit', label: '样品单位' },
  { key: 'sampleUnitEn', label: '样品单位(英)' },
  { key: 'certification', label: '认证' },
  { key: 'certificationCount', label: '认证数量' },
  { key: 'batteryInfo', label: '电池信息' },
  { key: 'infringement', label: '侵权信息' },
  { key: 'remark', label: '中文备注' },
  { key: 'remarkEn', label: '备注(英)' },
  { key: 'registrant', label: '登记人' },
  { key: 'modifier', label: '修改人' },
  { key: 'createTime', label: '登记时间' },
  { key: 'updateTime', label: '修改时间' },
]

export function useExport(
  selectedIds,
  gridRef,
  showToast,
  showAlertDialog,
  showMoreDropdown,
  showPrintDropdown
) {
  const getToken = () => sessionStorage.getItem('token') || localStorage.getItem('token') || ''
  const authHeader = () => ({ 'Authorization': 'Bearer ' + getToken() })

  // ========== 下载模板 ==========
  const downloadTemplate = () => {
    showMoreDropdown.value = false
    const a = document.createElement('a')
    a.href = '/samples/template'
    a.download = 'template.csv'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  }

  // ========== 导出 Excel ==========
  const showExportModal = ref(false)
  const exportFields = ref([])
  const dragIndex = ref(-1)

  const initExportFields = () => {
    const saved = localStorage.getItem('export_template_last')
    if (saved) {
      try {
        const last = JSON.parse(saved)
        const keySet = new Set(last.fields)
        exportFields.value = EXPORT_FIELD_CONFIG.map(f => ({ ...f, checked: keySet.has(f.key) }))
        currentTemplate.value = { name: last.name, fields: last.fields }
        return
      } catch (e) {}
    }
    exportFields.value = EXPORT_FIELD_CONFIG.map(f => ({ ...f }))
    currentTemplate.value = null
  }

  const checkedExportFieldCount = computed(() => exportFields.value.filter(f => f.checked).length)

  // 模板
  const templateName = ref('')
  const currentTemplate = ref(null)
  const exportTemplates = ref([])
  const showTplMenu = ref(false)
  const showTplSaveInput = ref(false)
  const tplSaveRef = ref(null)

  const loadExportTemplates = () => {
    try {
      exportTemplates.value = JSON.parse(localStorage.getItem('export_templates') || '[]')
    } catch (e) { exportTemplates.value = [] }
  }

  const saveExportTemplate = () => {
    const name = templateName.value.trim()
    if (!name) return
    const checked = exportFields.value.filter(f => f.checked).map(f => f.key)
    if (checked.length === 0) return
    const templates = JSON.parse(localStorage.getItem('export_templates') || '[]')
    const idx = templates.findIndex(t => t.name === name)
    const obj = { name, fields: checked }
    if (idx >= 0) templates[idx] = obj
    else templates.push(obj)
    localStorage.setItem('export_templates', JSON.stringify(templates))
    exportTemplates.value = templates
    currentTemplate.value = obj
    templateName.value = ''
    showTplSaveInput.value = false
  }

  const loadExportTemplate = (t) => {
    const keySet = new Set(t.fields)
    exportFields.value = EXPORT_FIELD_CONFIG.map(f => ({ ...f, checked: keySet.has(f.key) }))
    currentTemplate.value = t
  }

  const deleteExportTemplate = () => {
    if (!currentTemplate.value) return
    const templates = JSON.parse(localStorage.getItem('export_templates') || '[]').filter(t => t.name !== currentTemplate.value.name)
    localStorage.setItem('export_templates', JSON.stringify(templates))
    exportTemplates.value = templates
    currentTemplate.value = null
  }

  const selectAllExportFields = () => exportFields.value.forEach(f => f.checked = true)
  const deselectAllExportFields = () => exportFields.value.forEach(f => f.checked = false)

  // 拖拽排序
  const onExportDragStart = (e, i) => {
    dragIndex.value = i
    e.dataTransfer.effectAllowed = 'move'
  }
  const onExportDragOver = (e, i) => {
    if (dragIndex.value === -1 || dragIndex.value === i) return
    const arr = [...exportFields.value]
    const [removed] = arr.splice(dragIndex.value, 1)
    arr.splice(i, 0, removed)
    exportFields.value = arr
    dragIndex.value = i
  }
  const onExportDrop = () => { dragIndex.value = -1 }
  const onExportDragEnd = () => { dragIndex.value = -1 }

  const doExport = async () => {
    const selected = exportFields.value.filter(f => f.checked).map(f => f.key)
    if (selected.length === 0 || selectedIds.value.length === 0) return
    try {
      const resp = await fetch('/samples/export', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeader() },
        body: JSON.stringify({ fields: selected, ids: selectedIds.value })
      })
      if (!resp.ok) throw new Error('导出失败')
      const blob = await resp.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      const today = new Date()
      const dateStr = today.getFullYear() + String(today.getMonth() + 1).padStart(2, '0') + String(today.getDate()).padStart(2, '0')
      a.download = '样品资料' + dateStr + '.xlsx'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(url)
      if (currentTemplate.value) {
        localStorage.setItem('export_template_last', JSON.stringify(currentTemplate.value))
      } else {
        localStorage.setItem('export_template_last', JSON.stringify({ name: '_auto', fields: selected }))
      }
      showExportModal.value = false
      selectedIds.value = []
      if (gridRef.value) gridRef.value.setCheckboxRow([], false)
    } catch (e) {
      console.error('导出失败', e)
    }
  }

  const exportExcel = () => {
    showMoreDropdown.value = false
    loadExportTemplates()
    initExportFields()
    showExportModal.value = true
  }

  // ========== 厂商确认表 ==========
  const showVendorConfirmModal = ref(false)
  const vcExporting = ref(false)
  const vcLogoInputRef = ref(null)

  const vcConfig = reactive({
    logoBase64: '',
    companyName: '',
    address: '',
    phone: '',
    title: '厂商确认表'
  })

  const vcFields = ref([])
  const defaultVcKeys = ['sampleCode', 'factoryCode', 'sampleName', 'factoryPrice', 'packagingCn', 'cartonCapacity', 'name', 'boothNo', 'remark']

  const initVcFields = () => {
    vcFields.value = EXPORT_FIELD_CONFIG.map(f => ({
      ...f,
      checked: defaultVcKeys.includes(f.key)
    }))
    loadVcConfigFromLocal()
  }

  const visibleVcFields = computed(() => vcFields.value.filter(f => f.checked))
  const checkedVcFieldCount = computed(() => vcFields.value.filter(f => f.checked).length)

  const selectAllVcFields = () => vcFields.value.forEach(f => f.checked = true)
  const deselectAllVcFields = () => vcFields.value.forEach(f => f.checked = false)

  const vcPreviewData = computed(() => {
    if (!gridRef.value || selectedIds.value.length === 0) return []
    const records = gridRef.value.getCheckboxRecords() || []
    return records.map(r => ({ id: r.id, ...r }))
  })

  const onVcLogoUpload = (e) => {
    const file = e.target.files[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = (ev) => { vcConfig.logoBase64 = ev.target.result }
    reader.readAsDataURL(file)
    e.target.value = ''
  }

  const saveVcConfigToLocal = () => {
    const obj = { ...vcConfig, fields: vcFields.value.filter(f => f.checked).map(f => f.key) }
    localStorage.setItem('vendor_confirm_config', JSON.stringify(obj))
    showToast('配置已保存', 'success')
  }

  const loadVcConfigFromLocal = () => {
    try {
      const saved = JSON.parse(localStorage.getItem('vendor_confirm_config') || '{}')
      if (saved.companyName) vcConfig.companyName = saved.companyName
      if (saved.address) vcConfig.address = saved.address
      if (saved.phone) vcConfig.phone = saved.phone
      if (saved.title) vcConfig.title = saved.title
      if (saved.logoBase64) vcConfig.logoBase64 = saved.logoBase64
      if (saved.fields && saved.fields.length > 0) {
        const keySet = new Set(saved.fields)
        vcFields.value.forEach(f => { f.checked = keySet.has(f.key) })
      }
    } catch (e) {}
  }

  // 打开厂商确认报表（新标签页）
  const showTemplateSelect = ref(false)
  const availableTemplates = ref([])
  const selectedTemplateId = ref('')
  const templateSearchKeyword = ref('')
  const vcSessionLoading = ref(false)

  const filteredTemplates = computed(() => {
    const kw = templateSearchKeyword.value.trim().toLowerCase()
    if (!kw) return availableTemplates.value
    return availableTemplates.value.filter(tpl =>
      (tpl.title || '').toLowerCase().includes(kw) ||
      (tpl.description || '').toLowerCase().includes(kw)
    )
  })

  const openVendorConfirmReport = async () => {
    const records = gridRef.value?.getCheckboxRecords() || []
    if (records.length === 0) {
      showAlertDialog('请先勾选要打印的样品', 'warn')
      return
    }
    let templates = []
    try {
      const resp = await fetch('/report-templates/all', {
        headers: { ...authHeader() }
      })
      const data = await resp.json()
      if (data.code === 200) {
        templates = data.data || []
      }
    } catch (e) {
      console.error('加载模板失败', e)
    }
    if (templates.length === 0) {
      showAlertDialog('未找到报表模板，请先在报表设计器中设计模板并「保存为模板」', 'warn')
      return
    }
    availableTemplates.value = templates
    selectedTemplateId.value = ''
    templateSearchKeyword.value = ''
    showPrintDropdown.value = false
    showTemplateSelect.value = true
  }

  const confirmTemplateAndOpen = async () => {
    if (!selectedTemplateId.value) return
    const records = gridRef.value?.getCheckboxRecords() || []
    const sampleIds = records.map(r => r.id)
    // 先同步打开窗口（必须在用户点击上下文内），再异步获取 session key
    const win = window.open('', '_blank')
    if (!win) {
      showAlertDialog('请允许弹出窗口后重试', 'warning')
      return
    }
    showTemplateSelect.value = false
    vcSessionLoading.value = true
    try {
      const resp = await fetch('/samples/vendor-confirm-session', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeader() },
        body: JSON.stringify({ sampleIds })
      })
      const result = await resp.json()
      const cacheKey = result.key
      const params = new URLSearchParams()
      const t = sessionStorage.getItem('token') || localStorage.getItem('token')
      if (t) params.set('token', t)
      params.set('key', cacheKey)
      params.set('templateId', selectedTemplateId.value)
      params.set('viewOnly', '1')
      win.location.href = `/#/report/designer?${params.toString()}`
    } catch (e) {
      console.error('厂商确认表打开失败', e)
      win.close()
      showAlertDialog('打开失败，请稍后重试', 'error')
    } finally {
      vcSessionLoading.value = false
    }
  }

  const doVendorConfirmExport = async () => {
    if (checkedVcFieldCount.value === 0 || selectedIds.value.length === 0) return
    vcExporting.value = true
    try {
      const fields = visibleVcFields.value.map(f => f.key)
      const resp = await fetch('/samples/vendor-confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeader() },
        body: JSON.stringify({
          ids: selectedIds.value,
          fields,
          header: {
            companyName: vcConfig.companyName,
            address: vcConfig.address,
            phone: vcConfig.phone,
            title: vcConfig.title,
            logoBase64: vcConfig.logoBase64 ? vcConfig.logoBase64.split(',')[1] : ''
          }
        })
      })
      if (!resp.ok) throw new Error('导出失败')
      const blob = await resp.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      const today = new Date()
      const dateStr = today.getFullYear() + String(today.getMonth() + 1).padStart(2, '0') + String(today.getDate()).padStart(2, '0')
      a.download = '厂商确认表' + dateStr + '.xlsx'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(url)
      showVendorConfirmModal.value = false
      selectedIds.value = []
      if (gridRef.value) gridRef.value.setCheckboxRow([], false)
    } catch (e) {
      console.error('厂商确认表导出失败', e)
      showToast('导出失败，请重试', 'error')
    } finally {
      vcExporting.value = false
    }
  }

  return {
    // 下载模板
    downloadTemplate,
    // 导出 Excel
    showExportModal,
    exportFields,
    dragIndex,
    checkedExportFieldCount,
    templateName,
    currentTemplate,
    exportTemplates,
    showTplMenu,
    showTplSaveInput,
    tplSaveRef,
    initExportFields,
    loadExportTemplates,
    saveExportTemplate,
    loadExportTemplate,
    deleteExportTemplate,
    selectAllExportFields,
    deselectAllExportFields,
    onExportDragStart,
    onExportDragOver,
    onExportDrop,
    onExportDragEnd,
    doExport,
    exportExcel,
    // 厂商确认表
    showVendorConfirmModal,
    vcExporting,
    vcLogoInputRef,
    vcConfig,
    vcFields,
    visibleVcFields,
    checkedVcFieldCount,
    vcPreviewData,
    initVcFields,
    selectAllVcFields,
    deselectAllVcFields,
    onVcLogoUpload,
    saveVcConfigToLocal,
    doVendorConfirmExport,
    showTemplateSelect,
    availableTemplates,
    selectedTemplateId,
    templateSearchKeyword,
    vcSessionLoading,
    filteredTemplates,
    openVendorConfirmReport,
    confirmTemplateAndOpen,
  }
}
