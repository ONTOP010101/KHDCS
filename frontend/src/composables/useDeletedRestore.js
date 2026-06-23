import { ref, computed, nextTick } from 'vue'
import { api } from '@/api'

export function useDeletedRestore(showRestoreDeletedModal, showMoreDropdown, showAlertDialog, showConfirmDialog, loadTableData) {
  // ===================== 回收站删除数据恢复 =====================
  const deletedGridRef = ref(null)
  const deletedData = ref([])
  const deletedLoading = ref(false)
  const deletedTotal = ref(0)
  const deletedSelected = ref([])

  const deletedAllData = ref([])
  const deletedFilterField = ref('')
  const deletedFilterKeyword = ref('')
  const deletedFullscreen = ref(false)
  const deletedFullscreenSearch = ref('')
  const deletedGridMaxHeight = computed(() => deletedFullscreen.value ? window.innerHeight - 120 : 480)
  const showDeletedBatchQuery = ref(false)
  const deletedBatchField = ref('sampleCode')
  const deletedBatchInput = ref('')
  const deletedFilterActive = ref(false)

  const deletedSortMethod = ({ data, sortList }) => {
    if (!sortList || sortList.length === 0) return data
    const { field, order } = sortList[0]
    return data.sort((a, b) => {
      const va = String(a[field] ?? '')
      const vb = String(b[field] ?? '')
      const cmp = va.localeCompare(vb)
      return order === 'asc' ? cmp : -cmp
    })
  }

  const deletedGridColumns = [
    { type: 'checkbox', width: 44, fixed: 'left' },
    { field: 'id', title: 'ID', width: 70, sortable: true, sortType: 'number' },
    { field: 'sampleCode', title: '公司编号', width: 130, showOverflow: true, sortable: true },
    { field: 'manufacturerCode', title: '厂商编号', width: 100, sortable: true },
    { field: 'sampleName', title: '样品名称', minWidth: 180, showOverflow: true, sortable: true },
    { field: 'category', title: '种类', width: 110, sortable: true },
    { field: 'categoryCode', title: '种类编号', width: 90, sortable: true },
    { field: 'factoryCode', title: '出厂货号', width: 110, sortable: true },
    { field: 'registrant', title: '登记人', width: 90, sortable: true },
    { field: 'updateTime', title: '删除时间', width: 160, sortable: true, formatter: ({ cellValue }) => cellValue ? new Date(cellValue).toLocaleString('zh-CN', {year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit'}) : '-' }
  ]

  const applyDeletedFilter = () => {
    const all = deletedAllData.value || []
    const field = deletedFilterField.value
    const keyword = deletedFilterKeyword.value.trim()
    if (!keyword) {
      deletedFilterActive.value = false
      deletedData.value = all
      deletedTotal.value = all.length
      return
    }
    deletedFilterActive.value = true
    let filtered = all
    if (field === 'updateTime') {
      filtered = all.filter(item => {
        const t = item.updateTime
        if (!t) return false
        const d = new Date(t)
        const ds = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
        return ds.includes(keyword)
      })
    } else if (field) {
      const kw = keyword.toLowerCase()
      filtered = all.filter(item => {
        const val = item[field]
        return val != null && String(val).toLowerCase().includes(kw)
      })
    } else {
      const kw = keyword.toLowerCase()
      filtered = all.filter(item => {
        return ['sampleCode', 'manufacturerCode', 'sampleName', 'category', 'factoryCode', 'registrant'].some(f => {
          const val = item[f]
          return val != null && String(val).toLowerCase().includes(kw)
        })
      })
    }
    deletedData.value = filtered
    deletedTotal.value = filtered.length
    deletedSelected.value = []
    if (deletedGridRef.value) {
      deletedGridRef.value.clearCheckboxRow()
    }
  }

  const fetchDeletedSamples = async () => {
    deletedLoading.value = true
    try {
      const res = await api('/samples/deleted?current=1&size=99999')
      if (res.code === 200 && res.data) {
        deletedAllData.value = res.data.records || []
        deletedTotal.value = res.data.total || 0
        applyDeletedFilter()
      }
    } catch (e) {
      showAlertDialog('获取已删除数据失败: ' + (e.message || '未知错误'), 'error')
    } finally {
      deletedLoading.value = false
    }
  }

  const openRestoreDeletedModal = () => {
    showMoreDropdown.value = false
    showRestoreDeletedModal.value = true
    deletedSelected.value = []
    deletedFilterField.value = ''
    deletedFilterKeyword.value = ''
    deletedFilterActive.value = false
    fetchDeletedSamples()
  }

  const doDeletedFilter = () => {
    applyDeletedFilter()
  }

  const doDeletedResetFilter = () => {
    deletedFilterField.value = ''
    deletedFilterKeyword.value = ''
    applyDeletedFilter()
  }

  const onDeletedZoom = ({ type }) => {
    deletedFullscreen.value = (type === 'max')
    if (!deletedFullscreen.value) {
      deletedFullscreenSearch.value = ''
      doDeletedResetFilter()
      nextTick(() => {
        if (deletedGridRef.value) {
          deletedGridRef.value.refreshColumn()
          deletedGridRef.value.refreshScroll()
        }
      })
    }
  }

  const onDeletedToolbarClick = ({ code }) => {
    if (code === 'refresh') {
      fetchDeletedSamples()
    }
  }

  const onDeletedFullscreenSearch = () => {
    const keyword = deletedFullscreenSearch.value.trim().toLowerCase()
    if (!keyword) {
      doDeletedResetFilter()
      return
    }
    const all = deletedAllData.value || []
    deletedFilterActive.value = true
    deletedData.value = all.filter(item =>
      Object.values(item).some(v => String(v || '').toLowerCase().includes(keyword))
    )
  }

  const clearDeletedFullscreenSearch = () => {
    deletedFullscreenSearch.value = ''
    doDeletedResetFilter()
  }

  const openDeletedBatchQuery = () => {
    showDeletedBatchQuery.value = true
  }

  const doDeletedBatchQuery = () => {
    const raw = deletedBatchInput.value.trim()
    if (!raw) {
      showAlertDialog('请输入至少一个编号', 'warning')
      return
    }
    const codes = raw.split(/[\n,，]+/).map(s => s.trim()).filter(Boolean)
    if (codes.length === 0) {
      showAlertDialog('请输入至少一个编号', 'warning')
      return
    }
    const field = deletedBatchField.value
    const all = deletedAllData.value || []
    const codeSet = new Set(codes)
    deletedFilterActive.value = true
    deletedData.value = all.filter(item => codeSet.has(String(item[field] || '').trim()))
    deletedFullscreenSearch.value = ''
    deletedBatchInput.value = ''
    showDeletedBatchQuery.value = false
  }

  const onDeletedCheckChange = () => {
    const grid = deletedGridRef.value
    if (grid) {
      deletedSelected.value = grid.getCheckboxRecords()
    }
  }

  const doRestoreDeleted = async () => {
    if (deletedSelected.value.length === 0) return
    if (!(await showConfirmDialog(`确定恢复选中的 ${deletedSelected.value.length} 条记录吗？`))) return
    const ids = deletedSelected.value.map(r => r.id)
    try {
      const res = await api('/samples/restore', { method: 'POST', body: JSON.stringify(ids) })
      if (res.code === 200) {
        showAlertDialog(`成功恢复 ${res.data || ids.length} 条记录`, 'success')
        deletedSelected.value = []
        fetchDeletedSamples()
        loadTableData()
      } else {
        showAlertDialog('恢复失败: ' + (res.message || '未知错误'), 'error')
      }
    } catch (e) {
      showAlertDialog('恢复失败: ' + (e.message || '未知错误'), 'error')
    }
  }

  return {
    deletedGridRef,
    deletedData,
    deletedLoading,
    deletedTotal,
    deletedSelected,
    deletedAllData,
    deletedFilterField,
    deletedFilterKeyword,
    deletedFullscreen,
    deletedFullscreenSearch,
    deletedGridMaxHeight,
    showDeletedBatchQuery,
    deletedBatchField,
    deletedBatchInput,
    deletedFilterActive,
    deletedSortMethod,
    deletedGridColumns,
    openRestoreDeletedModal,
    fetchDeletedSamples,
    applyDeletedFilter,
    doDeletedFilter,
    doDeletedResetFilter,
    onDeletedZoom,
    onDeletedToolbarClick,
    openDeletedBatchQuery,
    doDeletedBatchQuery,
    onDeletedCheckChange,
    doRestoreDeleted,
    onDeletedFullscreenSearch,
    clearDeletedFullscreenSearch,
  }
}
