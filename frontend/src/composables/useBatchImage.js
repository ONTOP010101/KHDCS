import { ref } from 'vue'
import { api } from '@/api'

export function useBatchImage(showMoreDropdown, showBatchImageModal, batchImageType, showBatchResultModal, batchResult, showToast, tableData, manufacturerCode, currentSample) {
  const batchFiles = ref([])
  const batchUploading = ref(false)
  const batchUploadProgress = ref({ done: 0, total: 0, success: 0, fail: 0 })
  const batchMatched = ref([])
  const batchMatchLoading = ref(false)
  const batchCurrentIndex = ref(0)
  const showBatchConflictModal = ref(false)
  const batchConflicts = ref([])
  const batchConflictSelected = ref({})
  let _pendingSamples = []
  let _pendingFiles = []
  let _pendingCodeField = ''

  const goToPrev = () => {
    if (batchCurrentIndex.value > 0) batchCurrentIndex.value--
  }
  const goToNext = () => {
    if (batchCurrentIndex.value < batchMatched.value.length - 1) batchCurrentIndex.value++
  }

  const openBatchImageModal = () => {
    showMoreDropdown.value = false
    batchFiles.value = []
    batchMatched.value = []
    batchConflictSelected.value = {}
    showBatchImageModal.value = true
  }

  const onDragOver = (e) => {
    e.currentTarget.classList.add('drag-over')
  }

  const onDragLeave = (e) => {
    e.currentTarget.classList.remove('drag-over')
  }

  const onBatchDrop = (e) => {
    e.currentTarget.classList.remove('drag-over')
    const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'))
    batchFiles.value = files
    if (files.length > 0) doBatchMatch()
  }

  const onBatchFileChange = (e) => {
    batchFiles.value = Array.from(e.target.files)
    if (batchFiles.value.length > 0) doBatchMatch()
  }

  const removeBatchFile = (idx) => {
    batchFiles.value.splice(idx, 1)
    batchMatched.value.splice(idx, 1)
    if (batchMatched.value.length === 0) {
      batchCurrentIndex.value = 0
    } else if (batchCurrentIndex.value >= batchMatched.value.length) {
      batchCurrentIndex.value = batchMatched.value.length - 1
    }
  }

  const doBatchMatch = async () => {
    if (batchFiles.value.length === 0) return
    batchMatchLoading.value = true
    batchCurrentIndex.value = 0
    batchMatched.value = []
    try {
      const codeField = batchImageType.value === 'company-code' ? 'sampleCode' : 'factoryCode'
      const extractCode = (filename) => {
        return filename.replace(/\.[^.]+$/, '').replace(/\(\d+\)$/, '').trim()
      }
      const codes = batchFiles.value.map(f => extractCode(f.name))

      const matchRes = await api('/samples/match-by-codes', {
        method: 'POST',
        body: JSON.stringify({
          codes,
          type: codeField === 'factoryCode' ? 'factoryCode' : 'sampleCode',
          manufacturerCode: (manufacturerCode && manufacturerCode.value) || (currentSample && currentSample.value && currentSample.value.manufacturerCode) || ''
        })
      })
      const samples = Array.isArray(matchRes.data) ? matchRes.data : (Array.isArray(matchRes) ? matchRes : [])

      // 检测重复：同一 code 对应多条样品
      const codeToSamples = {}
      samples.forEach(s => {
        const c = s[codeField]
        if (!codeToSamples[c]) codeToSamples[c] = []
        codeToSamples[c].push(s)
      })
      const conflictCodes = Object.keys(codeToSamples).filter(c => codeToSamples[c].length > 1)

      if (conflictCodes.length > 0) {
        // 有冲突，弹窗让用户选择
        _pendingSamples = samples
        _pendingFiles = [...batchFiles.value]
        _pendingCodeField = codeField

        // 加载冲突样品的缩略图
        const allIds = samples.map(s => s.id).filter(Boolean)
        if (allIds.length > 0) {
          try {
            const imgRes = await api('/images/sample-images', { method: 'POST', body: JSON.stringify(allIds) })
            const imgMap = imgRes.data || imgRes || {}
            samples.forEach(s => {
              const imgs = imgMap[s.id]
              if (imgs) {
                s._thumb = imgs.thumbnailPath || imgs.filePath || null
              }
            })
          } catch (e) { console.error(e) }
        }

        batchMatchLoading.value = false
        batchConflicts.value = conflictCodes.map(c => {
          const uploadFile = _pendingFiles.find(f => extractCode(f.name) === c)
          return {
            code: c,
            samples: codeToSamples[c],
            uploadPreviewUrl: uploadFile ? URL.createObjectURL(uploadFile) : null
          }
        })
        showBatchConflictModal.value = true
        return
      }

      // 无冲突，正常匹配
      doFinalMatch(samples, codeField)
    } catch (e) {
      console.error(e)
      batchMatched.value = batchFiles.value.map(f => ({ file: f, code: extractCode(f.name), matched: false, previewUrl: URL.createObjectURL(f) }))
    } finally {
      batchMatchLoading.value = false
    }
  }

  const doFinalMatch = async (samples, codeField) => {
    const extractCode = (filename) => {
      return filename.replace(/\.[^.]+$/, '').replace(/\(\d+\)$/, '').trim()
    }
    // 支持一对多：同一个 code 可能对应多个样品
    const sampleMap = {}
    samples.forEach(s => {
      const key = s[codeField]
      if (!sampleMap[key]) sampleMap[key] = []
      sampleMap[key].push(s)
    })

    const files = _pendingFiles.length > 0 ? _pendingFiles : batchFiles.value
    const matchedIds = []
    const results = []
    files.forEach(f => {
      const code = extractCode(f.name)
      const matches = sampleMap[code]
      if (!matches || matches.length === 0) {
        results.push({ file: f, code, matched: false, previewUrl: URL.createObjectURL(f) })
        return
      }
      matches.forEach(sample => {
        matchedIds.push(sample.id)
        results.push({
          file: f, code, matched: true,
          sampleId: sample.id, sampleCode: sample.sampleCode,
          factoryCode: sample.factoryCode, sampleName: sample.sampleName,
          manufacturerCode: sample.manufacturerCode, name: sample.name,
          boothNo: sample.boothNo,
          action: 'cover', hasExisting: false, existingThumb: null,
          previewUrl: URL.createObjectURL(f)
        })
      })
    })

    if (matchedIds.length > 0) {
      try {
        const imgRes = await api('/images/sample-images', { method: 'POST', body: JSON.stringify(matchedIds) })
        const imgMap = imgRes.data || imgRes || {}
        results.forEach(r => {
          if (r.matched && imgMap[r.sampleId]) {
            r.hasExisting = true
            r.existingThumb = imgMap[r.sampleId].thumbnailPath || null
          }
        })
      } catch (e) { console.error(e) }
    }

    _pendingSamples = []
    _pendingFiles = []
    _pendingCodeField = ''
    batchMatched.value = results.sort((a, b) => b.matched - a.matched)
  }

  const resolveBatchConflicts = async (selectedMap) => {
    showBatchConflictModal.value = false
    batchMatchLoading.value = true
    try {
      // 用用户选择的样品 + 无冲突的样品重组 samples（支持多选：selectedMap[c] 为数组）
      const resolvedSamples = []
      _pendingSamples.forEach(s => {
        const c = s[_pendingCodeField]
        const sel = selectedMap[c]
        if (sel != null && sel.length > 0) {
          if (sel.some(id => String(s.id) === String(id))) {
            resolvedSamples.push(s)
          }
        } else {
          resolvedSamples.push(s)
        }
      })
      batchMatchLoading.value = false
      await doFinalMatch(resolvedSamples, _pendingCodeField)
    } catch (e) {
      console.error(e)
      batchMatchLoading.value = false
      batchMatched.value = _pendingFiles.map(f => ({ file: f, code: extractCode(f.name), matched: false, previewUrl: URL.createObjectURL(f) }))
    }
  }

  const cancelBatchConflict = () => {
    showBatchConflictModal.value = false
    _pendingSamples = []
    _pendingFiles = []
    _pendingCodeField = ''
    batchConflicts.value = []
  }

  const removeConflictCode = async (code) => {
    // 从冲突列表移除
    const idx = batchConflicts.value.findIndex(c => c.code === code)
    if (idx > -1) batchConflicts.value.splice(idx, 1)

    // 移除对应的上传文件
    const _extractCode = (filename) => filename.replace(/\.[^.]+$/, '').replace(/\(\d+\)$/, '').trim()
    _pendingFiles = _pendingFiles.filter(f => _extractCode(f.name) !== code)
    batchFiles.value = batchFiles.value.filter(f => _extractCode(f.name) !== code)

    // 移除对应的样品记录
    _pendingSamples = _pendingSamples.filter(s => s[_pendingCodeField] !== code)

    // 全部移除则关闭冲突框，继续处理剩余
    if (batchConflicts.value.length === 0) {
      showBatchConflictModal.value = false
      if (_pendingSamples.length > 0) {
        batchMatchLoading.value = true
        await doFinalMatch(_pendingSamples, _pendingCodeField)
      } else {
        batchMatchLoading.value = false
        batchMatched.value = []
      }
    }
  }

  const setBatchActionAll = (action) => {
    batchMatched.value.forEach(r => { if (r.matched) r.action = action })
  }

  const closeBatchModal = () => {
    showBatchImageModal.value = false
    batchFiles.value = []
    batchMatched.value = []
    batchCurrentIndex.value = 0
    batchUploading.value = false
  }

  const doBatchImageUpload = async () => {
    const uploadList = batchMatched.value.filter(m => m.matched && m.action !== 'skip')
    const unmatchedEntries = batchMatched.value.filter(m => !m.matched)
    const unmatchedList = unmatchedEntries.map(m => m.file.name)
    const unmatchedFiles = unmatchedEntries.filter(m => m.file).map(m => ({ name: m.file.name, file: m.file }))
    if (uploadList.length === 0 && unmatchedList.length > 0) {
      batchResult.successCount = 0
      batchResult.failCount = 0
      batchResult.unmatchedCount = unmatchedList.length
      batchResult.failList = []
      batchResult.failFiles = []
      batchResult.unmatchedList = unmatchedList
      batchResult.unmatchedFiles = unmatchedFiles
      showBatchResultModal.value = true
      return
    }
    if (uploadList.length === 0) {
      showToast('没有需要上传的图片', 'warn')
      return
    }

    let successCount = 0
    let failCount = 0
    let submitDone = 0
    const failList = []
    const failFiles = []

    batchUploading.value = true
    batchUploadProgress.value = { done: 0, total: uploadList.length, success: 0, fail: 0 }

    try {
      const taskItems = []
      for (const item of uploadList) {
        if (!item.file || item.file.size === 0) {
          failCount++
          submitDone++
          failList.push(`${item.file?.name || '未知文件'}: 文件不能为空`)
          if (item.file) failFiles.push({ name: item.file.name, file: item.file })
          batchUploadProgress.value = { done: submitDone, total: uploadList.length, success: successCount, fail: failCount }
          continue
        }

        if (item.action === 'cover' && item.hasExisting) {
          try {
            await api(`/images/sample/${item.sampleId}`, { method: 'DELETE' })
          } catch (e) {
            console.warn('[批量上传] 清除旧图片失败:', item.file.name, e.message)
          }
        }
        try {
          const fd = new FormData()
          fd.append('file', item.file)
          fd.append('sampleId', item.sampleId)
          const res = await api('/images/upload/async', { method: 'POST', body: fd })
          if (res && res.code === 200 && res.data) {
            taskItems.push({
              taskId: res.data.taskId,
              sampleId: item.sampleId,
              fileName: item.file.name
            })
          } else {
            failCount++
            failList.push(`${item.file.name}: 提交失败`)
            failFiles.push({ name: item.file.name, file: item.file })
          }
        } catch (e) {
          failCount++
          failList.push(`${item.file.name}: ${e.message || '提交失败'}`)
          failFiles.push({ name: item.file.name, file: item.file })
        }
        submitDone++
        batchUploadProgress.value = { done: submitDone, total: uploadList.length, success: successCount, fail: failCount }
      }

      if (taskItems.length === 0) {
        batchResult.successCount = 0
        batchResult.failCount = failCount
        batchResult.unmatchedCount = unmatchedList.length
        batchResult.failList = failList
        batchResult.failFiles = failFiles
        batchResult.unmatchedList = unmatchedList
        batchResult.unmatchedFiles = unmatchedFiles
        showBatchResultModal.value = true
        closeBatchModal()
        return
      }

      let pendingIds = taskItems.map(t => t.taskId)
      const MAX_POLL_TIME = 10 * 60 * 1000
      const POLL_INTERVAL = 2000
      const startTime = Date.now()

      while (pendingIds.length > 0 && (Date.now() - startTime) < MAX_POLL_TIME) {
        await new Promise(r => setTimeout(r, POLL_INTERVAL))
        try {
          const pollRes = await api('/images/upload/progress-batch', {
            method: 'POST',
            body: JSON.stringify(pendingIds)
          })
          if (pollRes && pollRes.code === 200 && pollRes.data) {
            const tasks = pollRes.data
            const newPending = []
            for (const t of tasks) {
              if (t.status === 'SUCCESS') {
                successCount++
                const info = taskItems.find(i => i.taskId === t.taskId)
                if (info) {
                  const row = tableData.value.find(r => r.id === info.sampleId)
                  if (row) {
                    row.thumbnail = t.thumbnailPath
                    row.firstImageId = t.imageId
                  }
                }
              } else if (t.status === 'FAILED') {
                failCount++
                const info = taskItems.find(i => i.taskId === t.taskId)
                failList.push(`${info?.fileName || t.taskId}: ${t.errorMsg || '失败'}`)
              } else {
                newPending.push(t.taskId)
              }
            }
            pendingIds = newPending
          }
        } catch (e) {
          console.warn('[批量上传] 轮询失败:', e.message)
        }
        batchUploadProgress.value.done = successCount + failCount
        batchUploadProgress.value.success = successCount
        batchUploadProgress.value.fail = failCount
      }

      if (pendingIds.length > 0) {
        failCount += pendingIds.length
        pendingIds.forEach(id => {
          const info = taskItems.find(i => i.taskId === id)
          failList.push(`${info?.fileName || id}: 超时未完成`)
        })
      }

      batchResult.successCount = successCount
      batchResult.failCount = failCount
      batchResult.unmatchedCount = unmatchedList.length
      batchResult.failList = failList
      batchResult.failFiles = failFiles
      batchResult.unmatchedList = unmatchedList
      batchResult.unmatchedFiles = unmatchedFiles
      showBatchResultModal.value = true
      closeBatchModal()
    } finally {
      batchUploading.value = false
    }
  }

  return {
    batchFiles,
    batchUploading,
    batchUploadProgress,
    batchMatched,
    batchMatchLoading,
    batchCurrentIndex,
    showBatchConflictModal,
    batchConflicts,
    batchConflictSelected,
    resolveBatchConflicts,
    cancelBatchConflict,
    removeConflictCode,
    goToPrev,
    goToNext,
    openBatchImageModal,
    onDragOver,
    onDragLeave,
    onBatchDrop,
    onBatchFileChange,
    removeBatchFile,
    doBatchMatch,
    setBatchActionAll,
    closeBatchModal,
    doBatchImageUpload,
  }
}
