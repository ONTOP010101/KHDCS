<template>
  <div class="image-search-page">
    <div class="isp-body">
      <div class="isp-image-strip">
        <div class="isp-strip-inner">
          <div v-for="(img, idx) in imageSearchImages" :key="idx"
               class="isp-thumb-card"
               :class="{ 'is-active': idx === imageSearchSelectedIdx }"
               @click="selectSearchImage(idx)">
            <img :src="img.url" />
            <button class="isp-thumb-del" @click.stop="removeSearchImage(idx)"><X :size="12" /></button>
            <div v-if="idx === 0 || idx === imageSearchSelectedIdx" class="isp-crop-badge" @click.stop="showCropModal = true; selectSearchImage(idx)">
              <Crop :size="11" /> 裁剪
            </div>
          </div>
          <div class="isp-upload-btn" @click="$refs.imageSearchInput.click()">
            <Plus :size="20" />
            <span>上传图片<br/><small>(多图)</small></span>
          </div>
        </div>
        <input ref="imageSearchInput" type="file" accept="image/*" multiple hidden @change="onImageSearchFilesChange" />
      </div>

      <div class="isp-toolbar">
        <div class="isp-search-row">
          <label>图像搜索:</label>
          <select v-model="imageSearchThreshold" class="isp-threshold-select">
            <option :value="5">严格</option>
            <option :value="10">适中</option>
            <option :value="15">宽松</option>
          </select>
          <button class="isp-btn-primary" @click="doImageSearch" :disabled="!imageSearchSelectedImg || imageSearching">
            <Search :size="14" /> {{ imageSearching ? '搜索中...' : '搜索匹配' }}
          </button>
        </div>
      </div>

      <div v-if="imageSearchError" class="isp-error-banner">{{ imageSearchError }}</div>

      <div class="isp-results-area">
        <div v-if="filteredResults.length > 0">
          <div class="isp-filter-bar">
            <span class="isp-filter-bar-hint">匹配 {{ imageSearchResults.length }} 条，显示 {{ filteredResults.length }} 条（≥{{ Math.round(displayThreshold * 100) }}%）</span>
          </div>
          <div class="isp-card-grid">
            <div v-for="(item, idx) in pagedResults" :key="idx"
                 class="isp-card" @click="viewImageSearchResult(item)">
              <div class="isp-card-img">
                <img v-if="item.filePath" :src="'/images/' + item.filePath" loading="lazy" decoding="async" />
                <div v-else class="isp-card-no-img"><ImageIcon :size="32" /></div>
                <span class="isp-card-score" :class="{ 'high': item.similarity >= 0.8, 'mid': item.similarity >= 0.6 && item.similarity < 0.8 }">
                  {{ item.similarity ? Math.round(item.similarity * 100) : Math.round((1 - item.distance / 64) * 100) }}%
                </span>
              </div>
              <div class="isp-card-body">
                <div class="isp-card-name">{{ item.sampleName || '--' }}</div>
                <div class="isp-card-code">{{ item.sampleCode || '' }}</div>
                <div class="isp-card-meta">
                  <span>{{ item.category || '-' }}</span>
                  <span v-if="item.price" class="isp-card-price">¥{{ item.price }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="isp-pagination" v-if="totalPages > 1">
            <button class="isp-page-btn" :disabled="resultPage <= 1" @click="resultPage--">上一页</button>
            <span class="isp-page-info">{{ resultPage }} / {{ totalPages }}</span>
            <button class="isp-page-btn" :disabled="resultPage >= totalPages" @click="resultPage++">下一页</button>
          </div>
        </div>
        <div v-else-if="imageSearchDone && !imageSearching" class="isp-empty">
          <ImageIcon :size="40" />
          <p v-if="imageSearchResults.length > 0">所有结果相似度低于 {{ Math.round(displayThreshold * 100) }}%</p>
          <p v-else>未找到相似图片</p>
          <p class="isp-empty-hint">尝试调整相似度阈值或更换图片后重新搜索</p>
        </div>
        <div v-else-if="!imageSearchDone && !imageSearching && imageSearchImages.length === 0" class="isp-empty">
          <ImageIcon :size="40" />
          <p>上传图片开始搜索</p>
          <p class="isp-empty-hint">支持上传多张图片，选择其中一张进行裁剪区域搜索</p>
        </div>
      </div>

      <Teleport to="body">
        <div v-if="showCropModal && imageSearchSelectedImg" class="crop-modal-overlay" @click.self="showCropModal = false">
          <div class="crop-modal">
            <div class="crop-modal-header">
              <span><Crop :size="15" /> 裁剪搜索区域</span>
              <button @click="showCropModal = false"><X :size="16" /></button>
            </div>
            <div class="crop-modal-body">
              <div class="crop-editor-wrap" ref="cropEditorRef"
                   @mousedown.prevent="onCropMouseDown"
                   @mousemove="onCropMouseMove"
                   @mouseup="onCropMouseUp"
                   @mouseleave="onCropMouseUp">
                <img :src="imageSearchSelectedImg.url" ref="cropImgRef" @load="onCropImgLoad" />
                <div class="crop-overlay" :style="cropOverlayStyle"></div>
                <div class="crop-select-box" v-show="cropSelecting || cropDone" :style="cropBoxStyle">
                  <div class="crop-handle tl" data-handle="tl" @mousedown.stop.prevent="onHandleDown($event,'tl')"></div>
                  <div class="crop-handle tr" data-handle="tr" @mousedown.stop.prevent="onHandleDown($event,'tr')"></div>
                  <div class="crop-handle bl" data-handle="bl" @mousedown.stop.prevent="onHandleDown($event,'bl')"></div>
                  <div class="crop-handle br" data-handle="br" @mousedown.stop.prevent="onHandleDown($event,'br')"></div>
                </div>
              </div>
              <div class="crop-hint-bar">
                <span v-if="!cropDone">按住鼠标拖拽框选产品搜索区域</span>
                <span v-else class="ready">已选区域 {{ cropW }}×{{ cropH }}px · 点击下方按钮完成</span>
                <button v-if="cropDone" class="crop-confirm-btn" @click="showCropModal = false">确认裁剪</button>
                <button v-if="cropDone" class="crop-reset-btn" @click="resetCrop">重选</button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api'
import { Search, Plus, X, Crop, Image as ImageIcon } from 'lucide-vue-next'

const router = useRouter()

const imageSearchImages = ref([])
const imageSearchSelectedIdx = ref(-1)
const imageSearchSelectedImg = computed(() => {
  const idx = imageSearchSelectedIdx.value
  return idx >= 0 ? imageSearchImages.value[idx] : null
})
const imageSearchThreshold = ref(10)
const imageSearchResults = ref([])
const imageSearchDone = ref(false)
const imageSearching = ref(false)
const showCropModal = ref(false)
const resultPage = ref(1)
const resultPageSize = 100

const displayThreshold = computed(() => 0.55)

const filteredResults = computed(() => {
  return imageSearchResults.value.filter(item => {
    const sim = item.similarity || (1 - (item.distance || 0) / 64)
    return sim >= displayThreshold.value
  })
})

const pagedResults = computed(() => {
  const start = (resultPage.value - 1) * resultPageSize
  return filteredResults.value.slice(start, start + resultPageSize)
})

const totalPages = computed(() => Math.ceil(filteredResults.value.length / resultPageSize) || 1)

const cropEditorRef = ref(null)
const cropImgRef = ref(null)
const cropState = reactive({ startX: 0, startY: 0, x: 0, y: 0, w: 0, h: 0, active: false, done: false })
const cropDraggingHandle = ref('')
const cropImgNaturalW = ref(0)
const cropImgNaturalH = ref(0)
const cropDisplayScale = ref(1)
const cropSelecting = computed(() => cropState.active && !cropState.done)
const cropDone = computed(() => cropState.done)
const cropX = computed(() => Math.round(cropState.x))
const cropY = computed(() => Math.round(cropState.y))
const cropW = computed(() => Math.round(Math.abs(cropState.w)))
const cropH = computed(() => Math.round(Math.abs(cropState.h)))
const cropOverlayStyle = computed(() => ({
  display: (cropSelecting.value || cropDone.value) ? 'block' : 'none'
}))
const cropBoxStyle = computed(() => {
  const x = cropState.w < 0 ? cropState.x + cropState.w : cropState.x
  const y = cropState.h < 0 ? cropState.y + cropState.h : cropState.y
  return {
    left: x + 'px',
    top: y + 'px',
    width: Math.abs(cropState.w) + 'px',
    height: Math.abs(cropState.h) + 'px'
  }
})

const autoBackfillDhash = async () => {
  try {
    const res = await api('/images/backfill-dhash', { method: 'POST' })
    if (res.code === 200 && res.data) {
      localStorage.setItem('dhash_backfilled', '1')
    }
  } catch (e) {}
}

const imageSearchError = ref('')

const onImageSearchFilesChange = (e) => {
  const files = e.target.files
  if (!files || files.length === 0) return
  let skipped = 0
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    if (!file.type.startsWith('image/')) {
      skipped++
      continue
    }
    const url = URL.createObjectURL(file)
    imageSearchImages.value.push({ file, url, name: file.name })
  }
  if (skipped > 0) {
    imageSearchError.value = skipped + ' 个非图片文件已跳过'
  } else {
    imageSearchError.value = ''
  }
  if (imageSearchSelectedIdx.value < 0 && imageSearchImages.value.length > 0) {
    imageSearchSelectedIdx.value = 0
  }
  imageSearchResults.value = []
  imageSearchDone.value = false
  resetCropState()
  e.target.value = ''
}

const selectSearchImage = (idx) => {
  imageSearchSelectedIdx.value = idx
  resetCropState()
  imageSearchResults.value = []
  imageSearchDone.value = false
}

const removeSearchImage = (idx) => {
  URL.revokeObjectURL(imageSearchImages.value[idx].url)
  imageSearchImages.value.splice(idx, 1)
  if (imageSearchImages.value.length === 0) {
    imageSearchSelectedIdx.value = -1
    resetCropState()
  } else if (imageSearchSelectedIdx.value >= imageSearchImages.value.length) {
    imageSearchSelectedIdx.value = imageSearchImages.value.length - 1
    resetCropState()
  } else {
    resetCropState()
  }
}

function resetCropState() {
  cropState.startX = 0
  cropState.startY = 0
  cropState.x = 0
  cropState.y = 0
  cropState.w = 0
  cropState.h = 0
  cropState.active = false
  cropState.done = false
  cropDraggingHandle.value = ''
  cropImgNaturalW.value = 0
  cropImgNaturalH.value = 0
  cropDisplayScale.value = 1
}

const resetCrop = () => { resetCropState() }

const onCropImgLoad = () => {
  const img = cropImgRef.value
  if (!img) return
  cropImgNaturalW.value = img.naturalWidth
  cropImgNaturalH.value = img.naturalHeight
  if (img.offsetWidth > 0) {
    cropDisplayScale.value = img.naturalWidth / img.offsetWidth
  }
}

const getCropEditorPos = (e) => {
  const rect = cropEditorRef.value.getBoundingClientRect()
  return { x: e.clientX - rect.left, y: e.clientY - rect.top }
}

const onCropMouseDown = (e) => {
  if (cropDraggingHandle.value) return
  const pos = getCropEditorPos(e)
  cropState.startX = pos.x
  cropState.startY = pos.y
  cropState.x = pos.x
  cropState.y = pos.y
  cropState.w = 0
  cropState.h = 0
  cropState.active = true
  cropState.done = false
}

const onCropMouseMove = (e) => {
  if (!cropState.active) return
  const pos = getCropEditorPos(e)
  if (cropDraggingHandle.value) {
    handleResize(pos)
  } else {
    cropState.w = pos.x - cropState.startX
    cropState.h = pos.y - cropState.startY
  }
}

const onCropMouseUp = () => {
  if (!cropState.active) return
  if (Math.abs(cropState.w) > 10 && Math.abs(cropState.h) > 10) {
    cropState.done = true
  } else {
    resetCropState()
  }
  cropState.active = false
  cropDraggingHandle.value = ''
}

const onHandleDown = (e, handle) => {
  e.preventDefault()
  cropDraggingHandle.value = handle
  cropState.active = true
  const pos = getCropEditorPos(e)
  cropState.startX = pos.x
  cropState.startY = pos.y
}

const handleResize = (pos) => {
  const h = cropDraggingHandle.value
  const dx = pos.x - cropState.startX
  const dy = pos.y - cropState.startY
  const boxX = cropState.w < 0 ? cropState.x + cropState.w : cropState.x
  const boxY = cropState.h < 0 ? cropState.y + cropState.h : cropState.y
  const boxW = Math.abs(cropState.w)
  const boxH = Math.abs(cropState.h)
  if (h.includes('r')) { cropState.w = boxW + dx; cropState.x = boxX }
  if (h.includes('l')) { cropState.w = boxW - dx; cropState.x = boxX + dx }
  if (h.includes('b')) { cropState.h = boxH + dy; cropState.y = boxY }
  if (h.includes('t')) { cropState.h = boxH - dy; cropState.y = boxY + dy }
  cropState.startX = pos.x
  cropState.startY = pos.y
}

const getCroppedFile = () => {
  return new Promise((resolve) => {
    const img = imageSearchSelectedImg.value
    if (!img) { resolve(null); return }
    if (!cropDone.value) {
      resolve(img.file)
      return
    }
    const imageEl = new Image()
    imageEl.onload = () => {
      const sx = Math.max(0, Math.round(cropX.value * cropDisplayScale.value))
      const sy = Math.max(0, Math.round(cropY.value * cropDisplayScale.value))
      const sw = Math.min(Math.round(cropW.value * cropDisplayScale.value), imageEl.naturalWidth - sx)
      const sh = Math.min(Math.round(cropH.value * cropDisplayScale.value), imageEl.naturalHeight - sy)
      if (sw <= 0 || sh <= 0) { resolve(img.file); return }
      const canvas = document.createElement('canvas')
      canvas.width = sw
      canvas.height = sh
      const ctx = canvas.getContext('2d')
      ctx.imageSmoothingEnabled = true
      ctx.imageSmoothingQuality = 'high'
      ctx.drawImage(imageEl, sx, sy, sw, sh, 0, 0, sw, sh)
      canvas.toBlob((blob) => {
        if (!blob) { resolve(img.file); return }
        const croppedFile = new File([blob], img.name.replace(/\.[^.]+$/, '.jpg'), { type: 'image/jpeg', lastModified: Date.now() })
        resolve(croppedFile)
      }, 'image/jpeg', 0.95)
    }
    imageEl.onerror = () => {
      resolve(img.file)
    }
    imageEl.src = img.url
  })
}

const doImageSearch = async () => {
  showCropModal.value = false
  imageSearching.value = true
  imageSearchDone.value = false
  imageSearchResults.value = []
  imageSearchError.value = ''
  resultPage.value = 1
  const searchFile = await getCroppedFile()
  if (!searchFile) {
    imageSearching.value = false
    return
  }
  try {
    const formData = new FormData()
    formData.append('file', searchFile)
    const res = await api('/images/search-by-image?maxDistance=' + imageSearchThreshold.value, {
      method: 'POST',
      body: formData
    })
    if (res.code === 200) {
      imageSearchResults.value = res.data || []
      const needBackfill = localStorage.getItem('dhash_backfilled') !== '1'
      if (imageSearchResults.value.length === 0 && needBackfill) {
        await autoBackfillDhash()
        const retryRes = await api('/images/search-by-image?maxDistance=' + imageSearchThreshold.value, {
          method: 'POST',
          body: formData
        })
        if (retryRes.code === 200) {
          imageSearchResults.value = retryRes.data || []
        }
      }
    } else {
      imageSearchError.value = res.msg || '搜索失败，请重试'
    }
  } catch (e) {
    imageSearchError.value = '网络错误，请检查连接后重试'
    console.error('图像搜索失败:', e)
  } finally {
    imageSearching.value = false
    imageSearchDone.value = true
  }
}

const viewImageSearchResult = (item) => {
  if (item.sampleId) {
    router.push({ name: 'Sample', query: { sampleId: item.sampleId, sampleCode: item.sampleCode || '' } })
  }
}

onBeforeUnmount(() => {
  imageSearchImages.value.forEach(img => {
    if (img.url && img.url.startsWith('blob:')) {
      URL.revokeObjectURL(img.url)
    }
  })
  imageSearchImages.value = []
})
</script>

<style scoped>
.image-search-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
}

.isp-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px;
}

.isp-image-strip {
  margin-bottom: 14px;
}

.isp-strip-inner {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  overflow-x: auto;
  padding-bottom: 6px;
}

.isp-strip-inner::-webkit-scrollbar { height: 4px; }
.isp-strip-inner::-webkit-scrollbar-thumb { background: rgba(0,122,255,0.18); border-radius: 99px; }

.isp-thumb-card {
  width: 110px;
  height: 110px;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  border: 2px solid #e8eaed;
  transition: all 0.2s ease;
  flex-shrink: 0;
  background: #f7f8fa;
}

.isp-thumb-card:hover {
  border-color: rgba(0,122,255,0.35);
  box-shadow: 0 3px 12px rgba(0,0,0,0.08);
}

.isp-thumb-card.is-active {
  border-color: #007aff;
  box-shadow: 0 0 0 2px rgba(0,122,255,0.12), 0 4px 14px rgba(0,122,255,0.12);
}

.isp-thumb-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.isp-thumb-del {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0,0,0,0.55);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.15s;
  z-index: 2;
}

.isp-thumb-card:hover .isp-thumb-del { opacity: 1; }

.isp-crop-badge {
  position: absolute;
  bottom: 6px;
  left: 50%;
  transform: translateX(-50%);
  background: #007aff;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 6px;
  cursor: pointer;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 3px;
  white-space: nowrap;
  transition: all 0.15s;
}

.isp-crop-badge:hover {
  background: #0066d6;
  transform: translateX(-50%) scale(1.05);
}

.isp-upload-btn {
  width: 110px;
  height: 110px;
  border-radius: 10px;
  border: 2px dashed #c8ccd4;
  background: linear-gradient(135deg, #fafbfd 0%, #f0f4f9 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  gap: 4px;
  font-size: 12px;
  color: rgba(29,29,31,0.45);
  text-align: center;
  line-height: 1.35;
}

.isp-upload-btn small {
  font-size: 10px;
  color: rgba(29,29,31,0.32);
}

.isp-upload-btn:hover {
  border-color: #007aff;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f2ff 100%);
  color: #007aff;
}

.isp-toolbar {
  background: #f8f9fb;
  border-radius: 12px;
  padding: 12px 18px;
  margin-bottom: 16px;
  border: 1px solid #ebeef2;
}

.isp-search-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.isp-search-row label {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.isp-error-banner {
  padding: 10px 16px;
  margin-bottom: 12px;
  border-radius: 10px;
  background: rgba(255,59,48,0.06);
  color: #ff3b30;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid rgba(255,59,48,0.15);
}

.isp-threshold-select {
  height: 32px;
  border-radius: 8px;
  border: 1px solid #dcdfe5;
  outline: none;
  padding: 0 24px 0 10px;
  font-size: 13px;
  color: #333;
  background: #fff;
  cursor: pointer;
}

.isp-btn-primary {
  height: 32px;
  min-width: 90px;
  padding: 0 16px;
  border-radius: 8px;
  background: #007aff;
  color: #fff;
  border: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: all 0.15s;
}

.isp-btn-primary:hover:not(:disabled) {
  background: #0066d6;
  transform: translateY(-1px);
}

.isp-btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.isp-results-area {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.isp-filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
}

.isp-filter-bar-hint {
  font-size: 12px;
  color: rgba(29,29,31,0.45);
}

.isp-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.isp-card {
  content-visibility: auto;
  contain-intrinsic-size: auto 320px;
  background: #fff;
  border: 1px solid #eaecef;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
}

.isp-card:hover {
  border-color: rgba(0,122,255,0.3);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  transform: translateY(-3px);
}

.isp-card-img {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f7f8fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.isp-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.isp-card-no-img {
  color: rgba(29,29,31,0.12);
}

.isp-card-score {
  position: absolute;
  top: 6px;
  right: 6px;
  font-size: 12px;
  font-weight: 800;
  color: rgba(255,255,255,0.95);
  background: rgba(0,0,0,0.45);
  backdrop-filter: blur(4px);
  padding: 2px 7px;
  border-radius: 5px;
  line-height: 1.3;
}

.isp-card-score.high { background: rgba(48,209,88,0.85); }
.isp-card-score.mid { background: rgba(0,122,255,0.85); }

.isp-card-body {
  padding: 10px 12px 12px;
}

.isp-card-name {
  font-size: 13px;
  font-weight: 600;
  color: #1d1d1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.35;
}

.isp-card-code {
  font-size: 11px;
  color: rgba(29,29,31,0.38);
  margin-top: 2px;
  font-family: monospace;
}

.isp-card-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 6px;
  font-size: 11px;
  color: rgba(29,29,31,0.34);
}

.isp-card-price {
  font-size: 15px;
  font-weight: 800;
  color: #e03e2d;
}

.isp-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid #f0f0f2;
}

.isp-page-btn {
  height: 30px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid #ddd;
  background: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
  color: #333;
}

.isp-page-btn:hover:not(:disabled) { background: #f5f5f7; border-color: #ccc; }
.isp-page-btn:disabled { opacity: 0.35; cursor: not-allowed; }

.isp-page-info {
  font-size: 13px;
  color: #666;
  min-width: 60px;
  text-align: center;
}

.isp-empty {
  text-align: center;
  padding: 60px 0 40px;
  color: rgba(29,29,31,0.25);
}

.isp-empty p {
  margin: 10px 0 0;
  font-size: 15px;
  font-weight: 600;
}

.isp-empty-hint {
  font-size: 13px !important;
  font-weight: 400 !important;
  color: rgba(29,29,31,0.22) !important;
  margin-top: 4px !important;
}

.crop-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}

.crop-modal {
  width: 700px;
  max-width: 92vw;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 24px 80px rgba(0,0,0,0.18);
  overflow: hidden;
}

.crop-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid #eeeef2;
  font-size: 14px;
  font-weight: 700;
  color: #1d1d1f;
  gap: 8px;
}

.crop-modal-header button {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: 1px solid #e0e0e4;
  background: #f7f7f8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  transition: all 0.15s;
}

.crop-modal-header button:hover {
  background: #eeeff1;
  color: #333;
}

.crop-modal-body {
  padding: 18px 20px 16px;
}

.crop-editor-wrap {
  position: relative;
  width: 100%;
  max-height: 420px;
  overflow: hidden;
  border-radius: 10px;
  border: 1px solid #dde0e6;
  background: repeating-conic-gradient(#f5f5f7 0% 25%, #fff 0% 50%) 50% / 16px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
  cursor: crosshair;
}

.crop-editor-wrap img {
  max-width: 100%;
  max-height: 420px;
  display: block;
  object-fit: contain;
}

.crop-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.crop-select-box {
  position: absolute;
  border: 2px solid #007aff;
  z-index: 2;
  pointer-events: none;
  box-shadow: 0 0 0 9999px rgba(0,0,0,0.35);
}

.crop-handle {
  position: absolute;
  width: 12px;
  height: 12px;
  background: #007aff;
  border: 2px solid #fff;
  border-radius: 3px;
  z-index: 3;
  pointer-events: auto;
}

.crop-handle.tl { top: -6px; left: -6px; cursor: nw-resize; }
.crop-handle.tr { top: -6px; right: -6px; cursor: ne-resize; }
.crop-handle.bl { bottom: -6px; left: -6px; cursor: sw-resize; }
.crop-handle.br { bottom: -6px; right: -6px; cursor: se-resize; }

.crop-hint-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: rgba(29,29,31,0.38);
}

.crop-hint-bar .ready {
  color: #30d158;
  font-weight: 600;
}

.crop-confirm-btn {
  height: 30px;
  padding: 0 18px;
  border-radius: 8px;
  background: #007aff;
  color: #fff;
  border: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.crop-confirm-btn:hover { background: #0066d6; }

.crop-reset-btn {
  height: 30px;
  padding: 0 14px;
  border-radius: 8px;
  background: transparent;
  color: #666;
  border: 1px solid #ddd;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}

.crop-reset-btn:hover { background: #f5f5f7; }
</style>
