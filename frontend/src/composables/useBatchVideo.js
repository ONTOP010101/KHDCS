import { ref, watch } from 'vue'
import { api } from '@/api'

const MAX_VIDEO_FILES = 10
const MAX_VIDEO_FILE_SIZE = 50 * 1024 * 1024 // 50MB

export function useBatchVideo(showBatchVideoModal, batchVideoType, showAlertDialog) {
  const batchVideoFiles = ref([])
  const batchVideoMatched = ref([])
  const videoMatchLoading = ref(false)
  const videoUploading = ref(false)
  const videoUploadProgress = ref({ done: 0, total: 0, success: 0, fail: 0, currentFileName: '', currentProgress: 0 })
  const videoCurrentIndex = ref(0)
  const customMatchSubType = ref('company-code')
  const customManufacturerCode = ref('')
  const customCodesText = ref('')

  const extractVideoCode = (filename) => {
    return filename.replace(/\.[^.]+$/, '').replace(/\(\d+\)$/, '').trim()
  }

  const matchVideosToSamples = async (files) => {
    if (!files || files.length === 0) return
    videoMatchLoading.value = true
    try {
      let type, codes

      if (batchVideoType.value === 'custom') {
        codes = customCodesText.value
          .replace(/，/g, ',')
          .split(/[\n,]/)
          .map(s => s.trim())
          .filter(Boolean)

        if (codes.length > 50) {
          showAlertDialog(`最多支持50个编号，当前输入了 ${codes.length} 个`, 'warning')
          videoMatchLoading.value = false
          return
        }
        type = customMatchSubType.value
      } else {
        codes = files.map(f => extractVideoCode(f.name))
        type = batchVideoType.value
      }

      const res = await api('/samples/match-by-codes', {
        method: 'POST',
        body: JSON.stringify({ type, codes })
      })
      const matchedSamples = (res.data || res || [])

      const lookup = {}
      matchedSamples.forEach(s => {
        const key = type === 'company-code' ? s.sampleCode : s.factoryCode
        if (key) lookup[key] = s
      })

      const buildResults = (codeToFileFn) => {
        const results = []
        codes.forEach((code, idx) => {
          const f = codeToFileFn(idx)
          const matched = lookup[code]
          results.push({
            file: f, code, matched: !!matched,
            action: !!matched ? 'append' : 'skip',
            previewUrl: URL.createObjectURL(f),
            ...(matched ? { sampleId: matched.id, sampleCode: matched.sampleCode, sampleName: matched.sampleName, factoryCode: matched.factoryCode } : {})
          })
        })
        return results
      }

      if (batchVideoType.value === 'custom') {
        if (customMatchSubType.value === 'factory-code') {
          const mfrCode = customManufacturerCode.value.trim()
          if (mfrCode) {
            Object.keys(lookup).forEach(key => {
              if (lookup[key].manufacturerCode !== mfrCode) delete lookup[key]
            })
          }
        }
        batchVideoMatched.value = buildResults(idx => files[idx % files.length])
      } else {
        batchVideoMatched.value = buildResults(idx => files[idx]).sort((a, b) => b.matched - a.matched)
      }
    } catch (e) {
      console.error('匹配视频失败:', e)
      batchVideoMatched.value = files.map(f => ({ file: f, code: extractVideoCode(f.name), matched: false, action: 'skip', previewUrl: URL.createObjectURL(f) }))
    } finally {
      videoMatchLoading.value = false
    }
  }

  const openBatchVideoModal = () => {
    batchVideoMatched.value = []
    batchVideoFiles.value = []
    videoCurrentIndex.value = 0
    videoUploading.value = false
    showBatchVideoModal.value = true
  }

  const closeBatchVideoModal = () => {
    showBatchVideoModal.value = false
    batchVideoFiles.value = []
    batchVideoMatched.value = []
    videoCurrentIndex.value = 0
    videoUploading.value = false
    customManufacturerCode.value = ''
    customCodesText.value = ''
  }

  const onVideoDragOver = (e) => { e.dataTransfer.dropEffect = 'copy' }

  const onVideoDrop = (e) => {
    const files = Array.from(e.dataTransfer.files).filter(f =>
      /\.(mp4|mov)$/i.test(f.name)
    )
    if (files.length === 0) return
    if (files.length > MAX_VIDEO_FILES) {
      showAlertDialog(`最多只能导入 ${MAX_VIDEO_FILES} 个视频，当前选择了 ${files.length} 个`, 'warning')
      return
    }
    const oversized = files.filter(f => f.size > MAX_VIDEO_FILE_SIZE)
    if (oversized.length > 0) {
      showAlertDialog(`以下视频超过50MB限制，已跳过：\n${oversized.map(f => f.name).join('\n')}`, 'warning')
    }
    const validFiles = files.filter(f => f.size <= MAX_VIDEO_FILE_SIZE)
    if (validFiles.length === 0) return
    batchVideoFiles.value = validFiles
    matchVideosToSamples(validFiles)
  }

  const onVideoFileChange = (e) => {
    const files = Array.from(e.target.files)
    if (files.length > MAX_VIDEO_FILES) {
      showAlertDialog(`最多只能导入 ${MAX_VIDEO_FILES} 个视频，当前选择了 ${files.length} 个`, 'warning')
      e.target.value = ''
      return
    }
    const oversized = files.filter(f => f.size > MAX_VIDEO_FILE_SIZE)
    if (oversized.length > 0) {
      showAlertDialog(`以下视频超过50MB限制，已跳过：\n${oversized.map(f => f.name).join('\n')}`, 'warning')
    }
    const validFiles = files.filter(f => f.size <= MAX_VIDEO_FILE_SIZE)
    batchVideoFiles.value = validFiles
    if (validFiles.length > 0) matchVideosToSamples(validFiles)
    e.target.value = ''
  }

  const goToVideoPrev = () => {
    if (videoCurrentIndex.value > 0) videoCurrentIndex.value--
  }
  const goToVideoNext = () => {
    if (videoCurrentIndex.value < batchVideoMatched.value.length - 1) videoCurrentIndex.value++
  }

  const removeVideoFile = (idx) => {
    batchVideoFiles.value.splice(idx, 1)
    batchVideoMatched.value.splice(idx, 1)
    if (batchVideoMatched.value.length === 0) return
    else if (videoCurrentIndex.value >= batchVideoMatched.value.length) videoCurrentIndex.value = batchVideoMatched.value.length - 1
  }

  const setVideoActionAll = (action) => {
    batchVideoMatched.value.forEach(r => { if (r.matched) r.action = action })
  }

  const doBatchVideoUpload = async () => {
    const uploadList = batchVideoMatched.value.filter(m => m.matched && m.action !== 'skip')
    if (uploadList.length === 0) return

    const UPLOAD_TIMEOUT_MS = 120000
    let successCount = 0
    let failCount = 0
    const failList = []

    videoUploading.value = true
    videoUploadProgress.value = { done: 0, total: uploadList.length, success: 0, fail: 0, currentFileName: '', currentProgress: 0 }

    const coverSampleIds = [...new Set(uploadList.filter(m => m.action === 'cover').map(m => m.sampleId))]
    for (const sampleId of coverSampleIds) {
      try { await api(`/videos/sample/${sampleId}`, { method: 'DELETE' }) } catch (e) {}
    }

    const fileGroups = {}
    uploadList.forEach(item => {
      const key = item.file.name + '_' + item.file.size
      if (!fileGroups[key]) fileGroups[key] = { file: item.file, items: [], sampleCodes: [] }
      fileGroups[key].items.push(item)
      fileGroups[key].sampleCodes.push(item.sampleCode || item.sampleId)
    })

    const tasks = Object.values(fileGroups).map(g => ({
      file: g.file,
      sampleIds: g.items.map(i => i.sampleId),
      sampleCodes: g.sampleCodes,
      displayName: g.items.length === 1 ? (g.items[0].sampleCode || g.file.name) : `${g.file.name} → ${g.sampleCodes.join(', ')}`
    }))

    const uploadTask = async (task) => {
      let lastError = null
      for (let attempt = 0; attempt <= 1; attempt++) {
        if (attempt > 0) await new Promise(r => setTimeout(r, 2000))
        try {
          videoUploadProgress.value.currentFileName = task.displayName
          videoUploadProgress.value.currentProgress = 0

          const xhr = new XMLHttpRequest()
          const fd = new FormData()
          fd.append('file', task.file)

          const isBatch = task.sampleIds.length > 1
          if (isBatch) {
            fd.append('sampleIds', task.sampleIds.join(','))
          } else {
            fd.append('sampleId', task.sampleIds[0])
          }

          const timeoutId = setTimeout(() => xhr.abort(), UPLOAD_TIMEOUT_MS * (isBatch ? 5 : 1))

          xhr.upload.addEventListener('progress', (e) => {
            if (e.lengthComputable) {
              videoUploadProgress.value.currentProgress = Math.round((e.loaded / e.total) * 100)
            }
          })

          const response = await new Promise((resolve, reject) => {
            xhr.onload = () => {
              clearTimeout(timeoutId)
              if (xhr.status >= 200 && xhr.status < 300) {
                try { resolve(JSON.parse(xhr.responseText)) } catch { reject(new Error('响应解析失败')) }
              } else { reject(new Error(`HTTP ${xhr.status}`)) }
            }
            xhr.onerror = () => { clearTimeout(timeoutId); reject(new Error('网络错误')) }
            xhr.onabort = () => { clearTimeout(timeoutId); reject(new Error('上传超时')) }
            xhr.open('POST', isBatch ? '/videos/batch-upload' : '/videos/upload')
            xhr.send(fd)
          })

          if (response && response.code === 200) {
            successCount += task.sampleIds.length
            return true
          }
          lastError = new Error(response?.message || '服务端返回非200')
        } catch (e) {
          lastError = e
        }
      }
      failCount += task.sampleIds.length
      failList.push(`${task.displayName}: ${lastError?.message}`)
      return false
    }

    try {
      for (let i = 0; i < tasks.length; i++) {
        await uploadTask(tasks[i])
        videoUploadProgress.value.done = i + 1
        videoUploadProgress.value.success = successCount
        videoUploadProgress.value.fail = failCount
      }

      const hasFailures = failCount > 0
      await showAlertDialog(`视频导入完成：成功 ${successCount} 个，失败 ${failCount} 个${failList.length ? '\n失败文件：\n' + failList.join('\n') : ''}`, hasFailures ? 'warning' : 'success')
      closeBatchVideoModal()
    } finally {
      videoUploading.value = false
    }
  }

  let customMatchDebounce = null
  watch([customCodesText, customManufacturerCode], () => {
    if (batchVideoType.value !== 'custom') return
    if (batchVideoFiles.value.length === 0) return
    if (videoMatchLoading.value) return
    clearTimeout(customMatchDebounce)
    customMatchDebounce = setTimeout(() => {
      if (!customCodesText.value.trim()) return
      if (customMatchSubType.value === 'factory-code' && !customManufacturerCode.value.trim()) return
      matchVideosToSamples(batchVideoFiles.value)
    }, 600)
  })

  return {
    batchVideoFiles,
    batchVideoMatched,
    videoMatchLoading,
    videoUploading,
    videoUploadProgress,
    videoCurrentIndex,
    customMatchSubType,
    customManufacturerCode,
    customCodesText,
    openBatchVideoModal,
    closeBatchVideoModal,
    onVideoDragOver,
    onVideoDrop,
    onVideoFileChange,
    goToVideoPrev,
    goToVideoNext,
    removeVideoFile,
    setVideoActionAll,
    doBatchVideoUpload,
  }
}
