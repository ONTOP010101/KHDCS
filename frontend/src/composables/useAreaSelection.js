import { ref, computed, nextTick } from 'vue'

/**
 * vxe-grid 列区域选取 composable
 * 
 * @param {Object} options
 * @param {import('vue').Ref} options.wrapperRef    - 表格外层容器 ref
 * @param {import('vue').Ref} options.gridRef        - vxe-grid 实例 ref
 * @param {import('vue').Ref} options.tableData      - 全量数据 ref（Array）
 * @param {string}           options.keyField        - 行唯一标识字段，默认 'id'
 * @param {string}           options.handleClass     - 把手 CSS class，默认 'area-handle'
 * @param {string}           options.selectingClass  - 拖拽时加在 body 上的 class，默认 'area-selecting'
 * @param {boolean}          options.existingListeners - 是否已有 document mousedown capture，默认 false
 */
export function useAreaSelection({
  wrapperRef,
  gridRef,
  tableData,
  keyField = 'id',
  handleClass = 'area-handle',
  selectingClass = 'area-selecting',
  existingListeners = false
}) {
  // ===== 状态 =====
  const areaDragging = ref(false)
  const areaDragField = ref('')       // 真实字段名（用于 cell-style 匹配）
  const areaDragColId = ref('')       // DOM colid（用于 querySelector）
  const areaDragStartRowId = ref(null)
  const areaDragEndRowId = ref(null)
  const areaDragMoved = ref(false)
  const areaDragStartY = ref(0)
  const areaSelectedColumn = ref('')   // 已确认的选区列 — 真实字段名
  const areaSelectedColId = ref('')    // 已确认的选区列 — DOM colid
  const areaSelectedStartRowId = ref(null)
  const areaSelectedEndRowId = ref(null)
  const areaRenderTick = ref(0)
  const extDragging = ref(false)
  let areaHandleEl = null
  let handleScrollTimer = null

  const areaSelectedCount = computed(() => {
    if (!areaSelectedColumn.value) return 0
    const data = tableData.value
    const sIdx = data.findIndex(r => String(r[keyField]) === String(areaSelectedStartRowId.value))
    const eIdx = data.findIndex(r => String(r[keyField]) === String(areaSelectedEndRowId.value))
    if (sIdx === -1 || eIdx === -1) return 0
    return Math.abs(eIdx - sIdx) + 1
  })

  // ===== 辅助函数 =====
  const getColIdByField = (field) => {
    const grid = gridRef.value
    if (!grid) return field
    const cols = grid.getColumns() || []
    const col = cols.find(c => c.field === field)
    return col ? col.id : field
  }

  const getFieldByColId = (colId) => {
    const grid = gridRef.value
    if (!grid) return colId
    const cols = grid.getColumns() || []
    const col = cols.find(c => c.id === colId)
    return col ? col.field : colId
  }

  const getRowIdAndField = (el) => {
    if (!el) return null
    const colidEl = el.getAttribute('colid') ? el : el.querySelector('[colid]') || el.closest('[colid]')
    if (!colidEl) return null
    const colid = colidEl.getAttribute('colid')
    if (!colid) return null
    const row = colidEl.closest('[rowid]') || colidEl.closest('tr')
    if (!row) return null
    const rowid = row.getAttribute('rowid') || ''
    return { rowId: rowid, field: colid }
  }

  // ===== 事件处理 =====
  const onWrapperMouseDown = (e) => {
    if (e.button !== 0) return
    if (e.target.closest(`.${handleClass}`)) return  // 跳过把手拖拽
    if (!wrapperRef.value?.contains(e.target)) return
    const info = getRowIdAndField(e.target)
    if (!info) return
    areaDragStartRowId.value = info.rowId
    areaDragEndRowId.value = info.rowId
    areaDragColId.value = info.field
    areaDragField.value = getFieldByColId(info.field)
    areaDragging.value = false
    areaDragMoved.value = false
    areaDragStartY.value = e.clientY
    // 清除之前选区
    areaSelectedColumn.value = ''
    areaSelectedColId.value = ''
    areaSelectedStartRowId.value = null
    areaSelectedEndRowId.value = null
    areaRenderTick.value++
    document.addEventListener('mousemove', onDocMouseMove)
    document.addEventListener('mouseup', onDocMouseUp)
    e.preventDefault()
  }

  const onDocMouseMove = (e) => {
    if (!areaDragging.value && !areaDragMoved.value) {
      if (Math.abs(e.clientY - areaDragStartY.value) < 6) return
      areaDragging.value = true
      areaDragMoved.value = true
      document.body.classList.add(selectingClass)
    }
    if (!areaDragging.value) return
    const target = document.elementFromPoint(e.clientX, e.clientY)
    if (!target) return
    const info = getRowIdAndField(target)
    if (!info || info.field !== areaDragColId.value) return
    areaDragEndRowId.value = info.rowId
    areaRenderTick.value++
  }

  const onDocMouseUp = () => {
    document.removeEventListener('mousemove', onDocMouseMove)
    document.removeEventListener('mouseup', onDocMouseUp)
    document.body.classList.remove(selectingClass)
    if (!areaDragging.value) {
      // 单击单个单元格
      if (areaDragField.value) {
        areaSelectedColumn.value = areaDragField.value
        areaSelectedColId.value = areaDragColId.value
        areaSelectedStartRowId.value = areaDragStartRowId.value
        areaSelectedEndRowId.value = areaDragEndRowId.value
        areaRenderTick.value++
        attachAreaHandle()
      }
      return
    }
    areaDragging.value = false
    areaSelectedColumn.value = areaDragField.value
    areaSelectedColId.value = areaDragColId.value
    areaSelectedStartRowId.value = areaDragStartRowId.value
    areaSelectedEndRowId.value = areaDragEndRowId.value
    areaRenderTick.value++
    attachAreaHandle()
  }

  // ===== cell-style 回调 =====
  const cellAreaStyle = ({ row, column }) => {
    void areaRenderTick.value
    const field = (column && (column.field || column.type)) || ''
    // 拖拽中的高亮
    if (areaDragging.value && field === areaDragField.value) {
      const data = tableData.value
      const sIdx = data.findIndex(r => String(r[keyField]) === String(areaDragStartRowId.value))
      const eIdx = data.findIndex(r => String(r[keyField]) === String(areaDragEndRowId.value))
      if (sIdx !== -1 && eIdx !== -1) {
        const min = Math.min(sIdx, eIdx)
        const max = Math.max(sIdx, eIdx)
        const rIdx = data.findIndex(r => r[keyField] === row[keyField])
        if (rIdx >= min && rIdx <= max) {
          return { textAlign: 'center', background: '#e3f2fd', outline: '2px solid #4285f4', outlineOffset: '-2px' }
        }
      }
    }
    // 已确认选区高亮
    if (areaSelectedColumn.value && field === areaSelectedColumn.value) {
      const data = tableData.value
      const sIdx = data.findIndex(r => String(r[keyField]) === String(areaSelectedStartRowId.value))
      const eIdx = data.findIndex(r => String(r[keyField]) === String(areaSelectedEndRowId.value))
      if (sIdx !== -1 && eIdx !== -1) {
        const min = Math.min(sIdx, eIdx)
        const max = Math.max(sIdx, eIdx)
        const rIdx = data.findIndex(r => r[keyField] === row[keyField])
        if (rIdx >= min && rIdx <= max) {
          return { textAlign: 'center', background: '#dceefb', outline: '2px solid #4285f4', outlineOffset: '-2px' }
        }
      }
    }
    return { textAlign: 'center' }
  }

  // ===== 选区把手 =====
  const attachAreaHandle = () => {
    removeAreaHandle()
    if (!areaSelectedColId.value) return
    const wrapper = wrapperRef.value
    if (!wrapper) return
    const data = tableData.value
    const sIdx = data.findIndex(r => String(r[keyField]) === String(areaSelectedStartRowId.value))
    const eIdx = data.findIndex(r => String(r[keyField]) === String(areaSelectedEndRowId.value))
    if (sIdx === -1 || eIdx === -1) return
    const lastIdx = Math.max(sIdx, eIdx)
    const lastId = String(data[lastIdx][keyField])
    requestAnimationFrame(() => {
      const cellEl = wrapper.querySelector(`[rowid="${lastId}"] [colid="${areaSelectedColId.value}"]`)
      if (!cellEl) return
      const td = cellEl.tagName === 'TD' ? cellEl : cellEl.closest('td')
      if (!td) return
      const h = document.createElement('div')
      h.className = handleClass
      Object.assign(h.style, {
        position: 'absolute', right: '-6px', bottom: '-6px',
        width: '14px', height: '14px',
        background: '#4285f4', border: '2px solid #fff',
        borderRadius: '2px', boxShadow: '0 0 0 2px #4285f4',
        cursor: 'crosshair', zIndex: '10',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        color: '#fff', fontSize: '12px', fontWeight: 'bold', lineHeight: '1',
        userSelect: 'none'
      })
      h.textContent = '+'
      h.addEventListener('mousedown', onHandleMouseDown)
      td.style.position = 'relative'
      td.appendChild(h)
      areaHandleEl = h
    })
  }

  const removeAreaHandle = () => {
    if (areaHandleEl) {
      areaHandleEl.removeEventListener('mousedown', onHandleMouseDown)
      if (areaHandleEl.parentNode) areaHandleEl.parentNode.removeChild(areaHandleEl)
      areaHandleEl = null
    }
  }

  const onHandleMouseDown = (e) => {
    e.stopPropagation()
    e.preventDefault()
    extDragging.value = true
    document.body.classList.add(selectingClass)
    document.addEventListener('mousemove', onExtMouseMove)
    document.addEventListener('mouseup', onExtMouseUp)
  }

  const onExtMouseMove = (e) => {
    if (!extDragging.value) return
    const target = document.elementFromPoint(e.clientX, e.clientY)
    if (!target) return
    const info = getRowIdAndField(target)
    if (!info) return
    areaSelectedEndRowId.value = info.rowId
    areaRenderTick.value++
  }

  const onExtMouseUp = () => {
    extDragging.value = false
    document.body.classList.remove(selectingClass)
    document.removeEventListener('mousemove', onExtMouseMove)
    document.removeEventListener('mouseup', onExtMouseUp)
    attachAreaHandle()
  }

  // ===== 选区操作 =====
  const clearAreaSelection = () => {
    removeAreaHandle()
    areaSelectedColumn.value = ''
    areaSelectedColId.value = ''
    areaSelectedStartRowId.value = null
    areaSelectedEndRowId.value = null
    areaDragging.value = false
    areaRenderTick.value++
  }

  const getAreaSelectedValues = () => {
    if (!areaSelectedColumn.value) return []
    const data = tableData.value
    const sIdx = data.findIndex(r => String(r[keyField]) === String(areaSelectedStartRowId.value))
    const eIdx = data.findIndex(r => String(r[keyField]) === String(areaSelectedEndRowId.value))
    if (sIdx === -1 || eIdx === -1) return []
    const min = Math.min(sIdx, eIdx)
    const max = Math.max(sIdx, eIdx)
    const field = areaSelectedColumn.value
    return data.slice(min, max + 1).map(r => ({
      id: r[keyField],
      value: r[field]
    }))
  }

  // ===== 复制 =====
  let copyTextarea = null

  const writeClipboard = (text) => {
    copyTextarea = document.createElement('textarea')
    copyTextarea.value = text
    copyTextarea.style.position = 'absolute'
    copyTextarea.style.left = '-9999px'
    copyTextarea.style.top = '0'
    copyTextarea.style.opacity = '0.01'
    document.body.appendChild(copyTextarea)
    copyTextarea.focus()
    copyTextarea.select()
    copyTextarea.setSelectionRange(0, copyTextarea.value.length)
    const clean = () => {
      if (copyTextarea) {
        document.body.removeChild(copyTextarea)
        copyTextarea = null
      }
    }
    setTimeout(clean, 200)
  }

  const onAreaCopyKey = (e) => {
    if (!(e.ctrlKey || e.metaKey) || e.key !== 'c') return
    if (!areaSelectedColumn.value) return
    const vals = getAreaSelectedValues()
    if (vals.length === 0) return
    const text = vals.map(v => v.value != null ? String(v.value) : '').join('\n')
    e.preventDefault()
    // 通过 copy 事件写入剪贴板
    const onCopy = (ce) => {
      ce.clipboardData.setData('text/plain', text)
      ce.preventDefault()
      document.removeEventListener('copy', onCopy)
    }
    document.addEventListener('copy', onCopy)
    document.execCommand('copy')
  }

  // ===== 生命周期 =====
  const setup = () => {
    document.addEventListener('mousedown', onWrapperMouseDown, true)
    document.addEventListener('keydown', onAreaCopyKey)
    // 滚动时重新挂把手
    const wrapper = wrapperRef.value
    if (wrapper) {
      wrapper.addEventListener('scroll', () => {
        if (!areaHandleEl || !document.contains(areaHandleEl)) {
          if (handleScrollTimer) clearTimeout(handleScrollTimer)
          handleScrollTimer = setTimeout(attachAreaHandle, 150)
        }
      }, { passive: true })
    }
  }

  const cleanup = () => {
    document.removeEventListener('mousedown', onWrapperMouseDown, true)
    document.removeEventListener('keydown', onAreaCopyKey)
    document.removeEventListener('mousemove', onDocMouseMove)
    document.removeEventListener('mouseup', onDocMouseUp)
    removeAreaHandle()
    if (handleScrollTimer) {
      clearTimeout(handleScrollTimer)
      handleScrollTimer = null
    }
  }

  // ===== 点击外部清除选区 =====
  const onDocClick = (e) => {
    if (areaSelectedColumn.value && wrapperRef.value && !wrapperRef.value.contains(e.target)) {
      clearAreaSelection()
    }
  }

  return {
    // 状态（只读）
    areaSelectedColumn,
    areaSelectedCount,
    areaRenderTick,
    // 方法
    cellAreaStyle,
    getAreaSelectedValues,
    clearAreaSelection,
    setup,
    cleanup,
    onDocClick,
    writeClipboard,
    onAreaCopyKey,
  }
}
