<template>
  <div class="image-search-page" @paste="onPaste" @dragover.prevent @drop.prevent="onDrop">
    <div class="isp-body">
      <!-- 工具栏行：搜图位 + 图片列表 + 操作按钮 -->
      <div class="isp-toolbar-row">
        <!-- 搜图位 -->
        <div class="isp-search-slot">
          <div v-if="imageSearchSelectedImg" class="isp-search-slot-content">
            <div class="isp-search-slot-preview">
              <img :src="imageSearchSelectedImg._croppedUrl || imageSearchSelectedImg.url" draggable="true" @dragstart="onImgDragStart" />
              <button class="crop-toggle-overlay" @click.stop="openCropModal" title="裁剪">
                <Crop :size="14" /> 裁剪
              </button>
            </div>
          </div>
          <div v-else class="isp-search-slot-empty">
            <ImageIcon :size="28" />
            <p>点击图片选取</p>
          </div>
        </div>

        <!-- 图片列表 -->
        <div class="isp-image-strip-inline">
          <div class="isp-strip-inner">
            <div v-for="(img, idx) in imageSearchImages" :key="idx"
                 class="isp-thumb-card"
                 :class="{ 'is-active': idx === imageSearchSelectedIdx }"
                 @click="selectSearchImage(idx)">
              <img :src="img.url" draggable="true" @dragstart="onImgDragStart" />
              <button class="isp-thumb-del" @click.stop="removeSearchImage(idx)"><X :size="12" /></button>
            </div>
            <div class="isp-upload-btn" @click="$refs.imageSearchInput.click()">
              <Plus :size="20" />
              <span>上传图片<br/><small>(拖拽 / Ctrl+V)</small></span>
            </div>
          </div>
          <input ref="imageSearchInput" type="file" accept="image/*" multiple hidden @change="onImageSearchFilesChange" />
        </div>
      </div>

      <div v-if="imageSearchError" class="isp-error-banner">{{ imageSearchError }}</div>

      <div class="isp-results-area" ref="resultAreaRef" @scroll="onVirtualScroll">
        <div v-if="imageSearching" class="isp-searching">
          <Loader :size="32" class="isp-spin" />
          <p>正在搜索相似图片...</p>
        </div>
        <div v-else-if="filteredResults.length > 0">
          <div class="isp-filter-bar">
            <span class="isp-filter-bar-hint">匹配 {{ imageSearchResults.length }} 条，显示 {{ filteredResults.length }} 条（≥{{ Math.round(displayThreshold * 100) }}%）</span>
          </div>
          <div class="isp-virtual-outer" :style="{ height: totalVirtualHeight + 'px' }">
            <div class="isp-virtual-inner" :style="{ transform: 'translateY(' + virtualOffsetY + 'px)' }">
              <div class="isp-card-grid" :style="cardGridStyle">
                <div v-for="item in visibleItems" :key="item.imageId" class="isp-card" @dblclick.stop="addToBatchSample(item)">
                  <label class="isp-card-checkbox" @click.stop>
                    <input type="checkbox" :checked="selectedResultIds.has(item.sampleId || item.id)" @change="toggleResultSelect(item)" />
                    <Check :size="12" />
                  </label>
                  <div class="isp-card-img" draggable="true" @dragstart="onCardImgDragStart">
                    <img v-if="item.thumbnailPath" :src="'/thumbnails/' + item.thumbnailPath" decoding="async"
                         @load="onCardImgLoad" />
                    <img v-else :src="'/images/' + item.filePath" decoding="async"
                         @load="onCardImgLoad" />
                    <div class="isp-card-img-actions">
                      <button class="isp-card-img-btn" @click.stop="findSimilar(item)" title="找相似">
                        <Search :size="14" /> 找相似
                      </button>
                      <button class="isp-card-img-btn" @click.stop="viewImageSearchResult(item)" title="信息预览">
                        <ImageIcon :size="14" /> 信息预览
                      </button>
                    </div>
                    <span class="isp-card-score" :class="{ 'high': item.similarity >= 0.8, 'mid': item.similarity >= 0.6 && item.similarity < 0.8 }">
                      {{ item.similarity ? Math.round(item.similarity * 100) : Math.round((1 - item.distance / 64) * 100) }}%
                    </span>
                  </div>
                  <div class="isp-card-body">
                    <div class="isp-card-name" :title="item.sampleName">{{ item.sampleName || '-' }}</div>
                    <div class="isp-card-fields">
                      <span class="isp-card-val isp-card-val-copy isp-card-code" :title="item.sampleCode">
                        {{ item.sampleCode || '-' }}
                        <button class="isp-card-copy-btn" @click.stop="copyCardCode(item.sampleCode)"><Copy :size="16" /></button>
                      </span>
                      <span class="isp-card-val" :title="item.factoryCode">{{ item.factoryCode || '-' }}</span>
                      <span class="isp-card-val" :title="(item.innerBoxCount || '-') + ' / ' + (item.cartonCapacity || '-')">{{ item.innerBoxCount || '-' }} / {{ item.cartonCapacity || '-' }}</span>
                      <span class="isp-card-val" :title="(item.cartonGrossWeight || '-') + ' / ' + (item.cartonNetWeight || '-')">{{ item.cartonGrossWeight || '-' }} / {{ item.cartonNetWeight || '-' }}</span>
                      <span class="isp-card-val" :title="(item.cartonMaterialVolume || '-') + ' / ' + (item.cartonVolume || '-')">{{ item.cartonMaterialVolume || '-' }} / {{ item.cartonVolume || '-' }}</span>
                      <span class="isp-card-val" :title="item.boothNo">{{ item.boothNo || '-' }}</span>
                      <span class="isp-card-val isp-card-price" :title="item.factoryPrice ? '¥' + item.factoryPrice : '-'">{{ item.factoryPrice ? '¥' + item.factoryPrice : '-' }}</span>
                    </div>
                    <div class="isp-card-divider"></div>
                    <div class="isp-card-fields isp-card-fields-single">
                      <span class="isp-card-val" :title="item.name">{{ item.name || '-' }}</span>
                      <span class="isp-card-val" :title="item.mobile1">{{ item.mobile1 || '-' }}</span>
                      <span class="isp-card-val" :title="item.createTime">{{ item.createTime || '-' }}</span>
                      <div class="isp-card-last-row">
                        <span class="isp-card-val" :title="item.updateTime">{{ item.updateTime || '-' }}</span>
                        <button class="isp-card-add-btn" @click.stop="addToBatchSample(item)">
                          <Plus :size="14" />
                          选取
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
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
          <p class="isp-empty-hint">支持上传多张图片，选择其中一张进行搜索</p>
        </div>
      </div>
    </div>
  </div>

  <!-- 裁剪模态框 -->
  <div v-if="showCropModal" class="crop-modal-wrapper">
    <div class="crop-modal">
      <div class="crop-modal-header">
        <span><Crop :size="16" /> 图片裁剪</span>
        <button @click="closeCropModal"><X :size="16" /></button>
      </div>
      <div class="crop-modal-body">
        <div class="crop-editor-wrap crop-editor-modal" ref="cropEditorRef"
             @mousedown.prevent="onCropMouseDown"
             @mousemove="onCropMouseMove"
             @mouseup="onCropMouseUp"
             @mouseleave="onCropMouseUp"
             @wheel="onCropWheel">
          <img :src="imageSearchSelectedImg ? imageSearchSelectedImg.url : ''" ref="cropImgRef" @load="onCropImgLoad"
               :style="{ transform: 'translate(' + panX + 'px, ' + panY + 'px) scale(' + cropZoom + ')', transformOrigin: '50% 50%' }" draggable="true" @dragstart="onImgDragStart" />
          <div class="crop-overlay" :style="cropOverlayStyle"></div>
          <div class="crop-select-box" v-show="cropDone" :style="cropBoxStyle">
            <div v-for="h in ['tl','tm','tr','rm','br','bm','bl','lm']" :key="h"
                 :class="['crop-handle', h]" :data-handle="h"
                 @mousedown.stop.prevent="onHandleDown($event, h)"></div>
          </div>
        </div>
        <div class="crop-hint-bar">
          <span>框内移动 · 框外平移 · 滚轮{{ Math.round(cropZoom * 100) }}%</span>
          <div class="crop-hint-actions">
            <span class="ready">{{ cropW }}×{{ cropH }}px</span>
            <button class="crop-confirm-btn" @click="confirmCropAndClose">确认裁剪</button>
            <button class="crop-reset-btn" @click="resetCrop">重选</button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 图片预览模态框 - 样品信息预览 -->
  <Teleport to="body">
  <div v-if="showPhotoModal" class="sample-photo-modal" :style="photoModalStyle">
    <div class="spm-header" @mousedown="startDragModal">
      <span class="spm-header-title">样品信息预览</span>
      <button class="spm-header-close" @click="closePhotoModal">&times;</button>
    </div>
    <div class="spm-body">
      <div class="spm-top-card" v-if="photoModalSample" style="visibility:hidden">
        <div class="spm-top-card-field"><span>样品名称</span><strong>{{ photoModalSample.sampleName || '-' }}</strong></div>
        <div class="spm-top-card-field"><span>公司编号</span><strong>{{ photoModalSample.sampleCode || '-' }}</strong></div>
        <div class="spm-top-card-field"><span>出厂货号</span><strong>{{ photoModalSample.factoryCode || '-' }}</strong></div>
        <div class="spm-top-card-field" v-if="!hideFactoryPrice"><span>出厂价</span><strong class="spm-price">{{ photoModalSample.factoryPrice || '-' }}</strong></div>
      </div>
      <div class="spm-body-main">
      <div class="spm-body-left">
        <div class="spm-main-img-wrap">
          <img v-if="photoModalImages.length > 0"
               :src="photoModalImages[photoModalIndex]?.hash ? '/images/view/hash/' + photoModalImages[photoModalIndex]?.hash : '/thumbnails/' + photoModalImages[photoModalIndex]?.thumbnailPath"
               :data-thumb="photoModalImages[photoModalIndex]?.thumbnailPath"
               @error="onModalImgError"
               @click="openFullPreview" draggable="true" @dragstart="onImgDragStart" />
          <span v-else class="spm-no-img">无图片</span>
          <button v-if="photoModalImages.length > 1" class="spm-main-img-nav spm-main-img-prev" @click="photoModalPrev">&#10094;</button>
          <button v-if="photoModalImages.length > 1" class="spm-main-img-nav spm-main-img-next" @click="photoModalNext">&#10095;</button>
        </div>
        <div v-if="photoModalImages.length > 1" class="spm-thumb-strip">
          <div
            v-for="(img, idx) in photoModalImages"
            :key="img.hash || idx"
            class="spm-thumb-item"
            :class="{ active: idx === photoModalIndex }"
            @click="photoModalIndex = idx"
          >
            <img v-if="img.thumbnailPath" :src="'/thumbnails/' + img.thumbnailPath" draggable="true" @dragstart="onImgDragStart" />
          </div>
        </div>
      </div>
      <div class="spm-body-right" v-if="photoModalSample">
        <div class="spm-section-title">详细信息</div>
        <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">样品名称</span><span class="spm-field-value" :title="photoModalSample.sampleName || '-'">{{ photoModalSample.sampleName || '-' }}</span></div></div>
        <div class="spm-field-row">
          <div class="spm-field"><span class="spm-field-label">公司编号</span><span class="spm-field-value" :title="photoModalSample.sampleCode || '-'">{{ photoModalSample.sampleCode || '-' }}</span></div>
          <div class="spm-field"><span class="spm-field-label">出厂货号</span><span class="spm-field-value" :title="photoModalSample.factoryCode || '-'">{{ photoModalSample.factoryCode || '-' }}</span></div>
        </div>
        <div class="spm-field-row" :class="{ 'spm-hidden': hideFactoryPrice }">
          <div class="spm-field" v-if="!hideFactoryPrice"><span class="spm-field-label">出厂价</span><span class="spm-field-value spm-price" :title="photoModalSample.factoryPrice || '-'">{{ photoModalSample.factoryPrice || '-' }}</span></div>
          <div class="spm-field"><span class="spm-field-label">包装方式</span><span class="spm-field-value" :title="photoModalSample.packagingCn || '-'">{{ photoModalSample.packagingCn || '-' }}</span></div>
        </div>
        <div class="spm-field-row">
          <div class="spm-field"><span class="spm-field-label">内盒/装箱量</span><span class="spm-field-value" :title="(photoModalSample.innerBoxCount != null ? photoModalSample.innerBoxCount : '0') + ' / ' + (photoModalSample.cartonCapacity||'-')">{{ (photoModalSample.innerBoxCount != null ? photoModalSample.innerBoxCount : '0') + ' / ' + (photoModalSample.cartonCapacity||'-') }}</span></div>
        </div>
        <div class="spm-field-row">
          <div class="spm-field"><span class="spm-field-label">外箱规格</span><span class="spm-field-value" :title="fmt3(photoModalSample.cartonLength,photoModalSample.cartonWidth,photoModalSample.cartonHeight)+' CM'">{{ fmt3(photoModalSample.cartonLength,photoModalSample.cartonWidth,photoModalSample.cartonHeight) }} CM</span></div>
          <div class="spm-field"><span class="spm-field-label">外箱毛/净重</span><span class="spm-field-value" :title="(photoModalSample.cartonGrossWeight||'0')+' / '+(photoModalSample.cartonNetWeight||'0')+' KG'">{{ (photoModalSample.cartonGrossWeight||'0') + ' / ' + (photoModalSample.cartonNetWeight||'0') + ' KG' }}</span></div>
        </div>
        <div class="spm-field-row">
          <div class="spm-field"><span class="spm-field-label">包装规格</span><span class="spm-field-value" :title="fmt3(photoModalSample.packageLength,photoModalSample.packageWidth,photoModalSample.packageHeight)+' CM'">{{ fmt3(photoModalSample.packageLength,photoModalSample.packageWidth,photoModalSample.packageHeight) }} CM</span></div>
          <div class="spm-field"><span class="spm-field-label">英文包装</span><span class="spm-field-value" :title="photoModalSample.packagingEn || '-'">{{ photoModalSample.packagingEn || '-' }}</span></div>
        </div>
        <div class="spm-field-row">
          <div class="spm-field"><span class="spm-field-label">产品规格</span><span class="spm-field-value" :title="fmt3(photoModalSample.sampleLength,photoModalSample.sampleWidth,photoModalSample.sampleHeight)+' CM'">{{ fmt3(photoModalSample.sampleLength,photoModalSample.sampleWidth,photoModalSample.sampleHeight) }} CM</span></div>
          <div class="spm-field"><span class="spm-field-label">产品毛/净重</span><span class="spm-field-value" :title="(photoModalSample.sampleGrossWeight||'0')+' / '+(photoModalSample.sampleNetWeight||'0')+' KG'">{{ (photoModalSample.sampleGrossWeight||'0') + ' / ' + (photoModalSample.sampleNetWeight||'0') + ' KG' }}</span></div>
        </div>
        <div class="spm-field-row">
          <div class="spm-field"><span class="spm-field-label">体积/材积</span><span class="spm-field-value" :title="(photoModalSample.cartonVolume||'-')+' / '+(photoModalSample.cartonMaterialVolume||'-')">{{ (photoModalSample.cartonVolume||'-') + ' / ' + (photoModalSample.cartonMaterialVolume||'-') }}</span></div>
          <div class="spm-field"><span class="spm-field-label">电池信息</span><span class="spm-field-value" :title="photoModalSample.batteryInfo || '-'">{{ photoModalSample.batteryInfo || '-' }}</span></div>
        </div>
        <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">摊位号</span><span class="spm-field-value" :title="photoModalSample.boothNo || '-'">{{ photoModalSample.boothNo || '-' }}</span></div></div>
        <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">产品认证</span><span class="spm-field-value" :title="photoModalSample.certification || '-'">{{ photoModalSample.certification || '-' }}</span></div></div>
        <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">中文备注</span><span class="spm-field-value" :title="photoModalSample.remark || '-'">{{ photoModalSample.remark || '-' }}</span></div></div>
        <template v-if="!hideSupplierInfo">
          <div class="spm-section-title">厂商信息</div>
          <div class="spm-field-row">
            <div class="spm-field"><span class="spm-field-label">厂商编号</span><span class="spm-field-value" :title="photoModalSample.manufacturerCode || '-'">{{ photoModalSample.manufacturerCode || '-' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">厂商名称</span><span class="spm-field-value" :title="photoModalSample.name || '-'">{{ photoModalSample.name || '-' }}</span></div>
          </div>
          <div class="spm-field-row">
            <div class="spm-field"><span class="spm-field-label">联系人</span><span class="spm-field-value" :title="photoModalSample.contact1 || '-'">{{ photoModalSample.contact1 || '-' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">电话</span><span class="spm-field-value" :title="photoModalSample.phone1 || '-'">{{ photoModalSample.phone1 || '-' }}</span></div>
          </div>
          <div class="spm-field-row">
            <div class="spm-field"><span class="spm-field-label">手机</span><span class="spm-field-value" :title="photoModalSample.mobile1 || '-'">{{ photoModalSample.mobile1 || '-' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">QQ</span><span class="spm-field-value" :title="photoModalSample.qq || '-'">{{ photoModalSample.qq || '-' }}</span></div>
          </div>
        </template>
      </div>
      </div>
    </div>
    <div class="spm-footer">
      <div class="spm-toggle-group">
        <label class="spm-toggle"><input type="checkbox" v-model="hideFactoryPrice" /> 隐藏出厂价</label>
        <label class="spm-toggle"><input type="checkbox" v-model="hideSupplierInfo" /> 隐藏厂商信息</label>
      </div>
      <div class="spm-toggle-group" style="gap:8px; margin-left: auto">
        <button class="spm-btn-close" @click="closePhotoModal">关闭</button>
      </div>
    </div>
  </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api'
import { Search, Plus, X, Crop, Copy, Image as ImageIcon, Check, CheckCircle, Loader } from 'lucide-vue-next'
import { useModalDrag } from '@/composables/useModalDrag'

const router = useRouter()

const imageSearchImages = ref([])
const imageSearchSelectedIdx = ref(-1)
const imageSearchSelectedImg = computed(() => {
  const idx = imageSearchSelectedIdx.value
  return idx >= 0 ? imageSearchImages.value[idx] : null
})
const imageSearchThreshold = 10
const imageSearchResults = ref([])
const imageSearchDone = ref(false)
const imageSearching = ref(false)
const showCropModal = ref(false)
const showCropOverlay = ref(false)
const selectedResultIds = ref(new Set())

const toggleResultSelect = (item) => {
  const id = item.sampleId || item.id
  if (!id) return
  if (selectedResultIds.value.has(id)) {
    selectedResultIds.value.delete(id)
  } else {
    selectedResultIds.value.add(id)
  }
  selectedResultIds.value = new Set(selectedResultIds.value)
}

const batchAddSelected = () => {
  const itemsToAdd = filteredResults.value.filter(item => {
    const id = item.sampleId || item.id
    return id && selectedResultIds.value.has(id) && item.sampleId
  })
  if (itemsToAdd.length > 0) {
    emit('addSamples', itemsToAdd)
    selectedResultIds.value = new Set()
  }
}

const displayThreshold = computed(() => 0.35)

const filteredResults = computed(() => {
  return imageSearchResults.value.filter(item => {
    const sim = item.similarity || (1 - (item.distance || 0) / 64)
    return sim >= displayThreshold.value
  })
})

// ── 虚拟滚动 ──
const CARD_COLS = 6
const GAP = 14
const CARD_BODY_H = 220

const resultAreaRef = ref(null)
const containerWidth = ref(800)
const scrollTop = ref(0)
const containerHeight = ref(600)
const measuredRowHeight = ref(0)

const colsPerRow = computed(() => CARD_COLS)

const cardWidth = computed(() => Math.floor((containerWidth.value - GAP * (CARD_COLS - 1)) / CARD_COLS))

const rowHeight = computed(() => {
  if (measuredRowHeight.value > 0) return measuredRowHeight.value
  return cardWidth.value + CARD_BODY_H + GAP
})

const totalRows = computed(() => Math.ceil(filteredResults.value.length / CARD_COLS))

const totalVirtualHeight = computed(() => {
  if (totalRows.value === 0) return 0
  return totalRows.value * rowHeight.value + GAP
})

const startRow = computed(() => Math.max(0, Math.floor(scrollTop.value / rowHeight.value) - 1))

const visibleRows = computed(() => Math.ceil(containerHeight.value / rowHeight.value) + 3)

const endRow = computed(() => Math.min(totalRows.value, startRow.value + visibleRows.value))

const visibleItems = computed(() => {
  const startIdx = startRow.value * CARD_COLS
  const endIdx = Math.min(filteredResults.value.length, endRow.value * CARD_COLS)
  return filteredResults.value.slice(startIdx, endIdx)
})

const virtualOffsetY = computed(() => startRow.value * rowHeight.value + GAP)

const cardGridStyle = computed(() => ({
  display: 'grid',
  gridTemplateColumns: `repeat(${CARD_COLS}, 1fr)`,
  gap: GAP + 'px',
  padding: '16px'
}))

function onVirtualScroll() {
  if (resultAreaRef.value) {
    scrollTop.value = resultAreaRef.value.scrollTop
  }
}

let resizeObserver = null

onMounted(() => {
  if (resultAreaRef.value) {
    containerHeight.value = resultAreaRef.value.clientHeight
    containerWidth.value = resultAreaRef.value.clientWidth
    resizeObserver = new ResizeObserver(() => {
      if (resultAreaRef.value) {
        containerWidth.value = resultAreaRef.value.clientWidth
        containerHeight.value = resultAreaRef.value.clientHeight
      }
    })
    resizeObserver.observe(resultAreaRef.value)
    // 延迟测量实际行高
    nextTick(() => {
      requestAnimationFrame(() => measureRowHeight())
    })
  }
})

onBeforeUnmount(() => {
  if (resizeObserver) resizeObserver.disconnect()
  imageSearchImages.value.forEach(img => {
    if (img.url && img.url.startsWith('blob:')) {
      URL.revokeObjectURL(img.url)
    }
    if (img._croppedUrl) {
      URL.revokeObjectURL(img._croppedUrl)
    }
  })
  imageSearchImages.value = []
})

const cropEditorRef = ref(null)
const cropImgRef = ref(null)
const cropState = reactive({ startX: 0, startY: 0, x: 0, y: 0, w: 0, h: 0, active: false, done: false })
const cropDraggingHandle = ref('')
const cropImgNaturalW = ref(0)
const cropImgNaturalH = ref(0)
const cropDisplayScale = ref(1)
const imgDisplayW = ref(0)
const imgDisplayH = ref(0)

// 计算 object-fit:contain 下图片内容的实际显示尺寸
const updateImgDisplaySize = () => {
  const img = cropImgRef.value
  if (!img || !img.naturalWidth) return
  const cw = img.offsetWidth
  const ch = img.offsetHeight
  const nw = img.naturalWidth
  const nh = img.naturalHeight
  if (nw / nh > cw / ch) {
    imgDisplayW.value = cw
    imgDisplayH.value = Math.round(cw * nh / nw)
  } else {
    imgDisplayH.value = ch
    imgDisplayW.value = Math.round(ch * nw / nh)
  }
  // object-fit:contain 会居中内容，记录内容在元素内的偏移
  imgContentOffsetX.value = Math.round((cw - imgDisplayW.value) / 2)
  imgContentOffsetY.value = Math.round((ch - imgDisplayH.value) / 2)
}
const imgContentOffsetX = ref(0)
const imgContentOffsetY = ref(0)
const imgOffsetX = ref(0)
const imgOffsetY = ref(0)
const cropZoom = ref(1)
const panX = ref(0)
const panY = ref(0)
const isPanning = ref(false)
const panStartMouse = ref({ x: 0, y: 0 })
const panStartTranslate = ref({ x: 0, y: 0 })
const isMovingFixedBox = ref(false)
const fixedBoxMoveStart = ref({ x: 0, y: 0, boxX: 0, boxY: 0 })
const cropSelecting = computed(() => cropState.active && !cropState.done)
const cropDone = computed(() => cropState.done)
const cropX = computed(() => Math.round(cropState.x))
const cropY = computed(() => Math.round(cropState.y))
const cropW = computed(() => Math.round(Math.abs(cropState.w)))
const cropH = computed(() => Math.round(Math.abs(cropState.h)))
const cropOverlayStyle = computed(() => ({
  display: cropDone.value ? 'block' : 'none'
}))
const cropBoxStyle = computed(() => {
  const x = cropState.w < 0 ? cropState.x + cropState.w : cropState.x
  const y = cropState.h < 0 ? cropState.y + cropState.h : cropState.y
  return {
    left: (x + imgOffsetX.value) + 'px',
    top: (y + imgOffsetY.value) + 'px',
    width: Math.abs(cropState.w) + 'px',
    height: Math.abs(cropState.h) + 'px'
  }
})

const initFixedBox = () => {
  if (cropState.done) return
  const displayW = imgDisplayW.value || 800
  const displayH = imgDisplayH.value || 800
  const boxW = Math.round(displayW)
  const boxH = Math.round(displayH)
  cropState.x = Math.round((displayW - boxW) / 2)
  cropState.y = Math.round((displayH - boxH) / 2)
  cropState.w = boxW
  cropState.h = boxH
  cropState.done = true
  cropState.active = false
}

const autoBackfillDhash = async () => {
  try {
    const res = await api('/images/backfill-dhash', { method: 'POST' })
    if (res.code === 200 && res.data) {
      localStorage.setItem('dhash_backfilled', '1')
    }
  } catch (e) {}
}

const imageSearchError = ref('')
const imgFileCache = new Map()

// 图片加载完成后自动 fetch 为 File 对象并缓存
const onCardImgLoad = async (e) => {
  const src = e.target.currentSrc || e.target.src
  if (!src || src.startsWith('blob:') || imgFileCache.has(src)) return
  try {
    const res = await fetch(src)
    if (!res.ok) return
    const blob = await res.blob()
    const fileName = src.split('/').pop().split('?')[0] || 'image.jpg'
    const file = new File([blob], fileName, { type: blob.type || 'image/jpeg' })
    imgFileCache.set(src, file)
  } catch { /* ignore */ }
}

// 拖拽时从 div 内找 img，追加 File 到拖拽数据
const onCardImgDragStart = (e) => {
  const img = e.target.tagName === 'IMG' ? e.target : e.target.querySelector('img')
  if (!img) return
  const src = img.currentSrc || img.src
  if (!src) return
  const file = imgFileCache.get(src)
  if (file) {
    e.dataTransfer.items.clear()
    e.dataTransfer.items.add(file)
    e.dataTransfer.setDragImage(img, img.offsetWidth / 2, img.offsetHeight / 2)
  }
  e.dataTransfer.effectAllowed = 'copy'
}

const addSearchImages = (files) => {
  if (!files || files.length === 0) return
  let skipped = 0
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    if (!file.type.startsWith('image/')) {
      skipped++
      continue
    }
    const url = URL.createObjectURL(file)
    imageSearchImages.value.push({ file, url, name: file.name || 'pasted-image.png' })
  }
  if (skipped > 0) {
    imageSearchError.value = skipped + ' 个非图片文件已跳过'
  } else {
    imageSearchError.value = ''
  }
  const isFirstUpload = imageSearchSelectedIdx.value < 0
  if (isFirstUpload && imageSearchImages.value.length > 0) {
    imageSearchSelectedIdx.value = 0
  }
  imageSearchResults.value = []
  imageSearchDone.value = false
  selectedResultIds.value = new Set()
  resetCropState()
  if (isFirstUpload && imageSearchImages.value.length > 0) {
    doImageSearch()
  }
}

const onImageSearchFilesChange = (e) => {
  addSearchImages(e.target.files)
  e.target.value = ''
}

const onPaste = (e) => {
  const items = e.clipboardData?.items
  if (!items) return
  const imageFiles = []
  for (let i = 0; i < items.length; i++) {
    if (items[i].type.startsWith('image/')) {
      const file = items[i].getAsFile()
      if (file) imageFiles.push(file)
    }
  }
  if (imageFiles.length > 0) {
    e.preventDefault()
    addSearchImages(imageFiles)
  }
}

const onDrop = (e) => {
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return
  addSearchImages(Array.from(files))
}

const selectSearchImage = (idx) => {
  // 切换图片时清除之前选中图片的裁剪数据
  const prev = imageSearchImages.value[imageSearchSelectedIdx.value]
  if (prev && prev._croppedUrl) {
    URL.revokeObjectURL(prev._croppedUrl)
    delete prev._croppedFile
    delete prev._croppedUrl
  }
  imageSearchSelectedIdx.value = idx
  resetCropState()
  doImageSearch()
}

const removeSearchImage = (idx) => {
  const img = imageSearchImages.value[idx]
  URL.revokeObjectURL(img.url)
  if (img._croppedUrl) {
    URL.revokeObjectURL(img._croppedUrl)
  }
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
  imgDisplayW.value = 0
  imgDisplayH.value = 0
  imgContentOffsetX.value = 0
  imgContentOffsetY.value = 0
  imgOffsetX.value = 0
  imgOffsetY.value = 0
  cropZoom.value = 1
  panX.value = 0
  panY.value = 0
}

const resetCrop = () => {
  resetCropState()
  initFixedBox()
}

const openCropModal = () => {
  if (!imageSearchSelectedImg.value) return
  resetCropState()
  showCropModal.value = true
  nextTick(() => {
    const img = cropImgRef.value
    if (img && img.complete) {
      onCropImgLoad()
    }
  })
}

const closeCropModal = () => {
  showCropModal.value = false
  resetCropState()
}

const confirmCropAndClose = async () => {
  await confirmCrop()
  closeCropModal()
  doImageSearch()
}

const onCropImgLoad = () => {
  const img = cropImgRef.value
  if (!img) return
  cropImgNaturalW.value = img.naturalWidth
  cropImgNaturalH.value = img.naturalHeight
  updateImgDisplaySize()
  if (imgDisplayW.value > 0) {
    cropDisplayScale.value = img.naturalWidth / imgDisplayW.value
  }
  const editorRect = cropEditorRef.value.getBoundingClientRect()
  const imgRect = img.getBoundingClientRect()
  imgOffsetX.value = imgRect.left - editorRect.left + imgContentOffsetX.value
  imgOffsetY.value = imgRect.top - editorRect.top + imgContentOffsetY.value
  initFixedBox()
}

const onCropWheel = (e) => {
  if (!showCropModal.value) return
  e.preventDefault()
  const delta = e.deltaY > 0 ? -0.1 : 0.1
  cropZoom.value = Math.max(0.3, Math.min(3, +(cropZoom.value + delta).toFixed(1)))
}

const getCropEditorPos = (e) => {
  const rect = cropImgRef.value.getBoundingClientRect()
  const z = cropZoom.value
  return { x: (e.clientX - rect.left) / z, y: (e.clientY - rect.top) / z }
}

const onCropMouseDown = (e) => {
  if (!showCropModal.value) return
  if (cropDraggingHandle.value) return
  const pos = getCropEditorPos(e)
  // 判断点击是否在选框内（坐标统一到 img 元素坐标系）
  const bx = (cropState.w < 0 ? cropState.x + cropState.w : cropState.x) + imgOffsetX.value
  const by = (cropState.h < 0 ? cropState.y + cropState.h : cropState.y) + imgOffsetY.value
  const bw = Math.abs(cropState.w)
  const bh = Math.abs(cropState.h)
  if (cropState.done && pos.x >= bx && pos.x <= bx + bw && pos.y >= by && pos.y <= by + bh) {
    // 框内拖拽 → 移动选框
    isMovingFixedBox.value = true
    fixedBoxMoveStart.value = { x: e.clientX, y: e.clientY, boxX: cropState.x, boxY: cropState.y }
    return
  }
  // 框外拖拽 → 平移图片
  isPanning.value = true
  panStartMouse.value = { x: e.clientX, y: e.clientY }
  panStartTranslate.value = { x: panX.value, y: panY.value }
}

const onCropMouseMove = (e) => {
  if (isMovingFixedBox.value) {
    const z = cropZoom.value || 1
    const dx = (e.clientX - fixedBoxMoveStart.value.x) / z
    const dy = (e.clientY - fixedBoxMoveStart.value.y) / z
    const bw = Math.abs(cropState.w)
    const bh = Math.abs(cropState.h)
    cropState.x = fixedBoxMoveStart.value.boxX + dx
    cropState.y = fixedBoxMoveStart.value.boxY + dy
    cropState.w = bw
    cropState.h = bh
    return
  }
  if (isPanning.value) {
    panX.value = panStartTranslate.value.x + (e.clientX - panStartMouse.value.x)
    panY.value = panStartTranslate.value.y + (e.clientY - panStartMouse.value.y)
    return
  }
  if (!cropState.active) return
  const pos = getCropEditorPos(e)
  if (cropDraggingHandle.value) {
    handleResize(pos)
  }
}

const onCropMouseUp = () => {
  if (isPanning.value) {
    isPanning.value = false
    return
  }
  if (isMovingFixedBox.value) {
    isMovingFixedBox.value = false
    return
  }
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
    // 如果已存储裁剪文件，直接使用
    if (img._croppedFile) {
      resolve(img._croppedFile)
      return
    }
    if (!cropDone.value) {
      resolve(img.file)
      return
    }
    const imageEl = new Image()
    imageEl.onload = () => {
      const displayW = imgDisplayW.value || 800
      const displayH = imgDisplayH.value || 800
      const z = cropZoom.value || 1
      // 将编辑器坐标映射到图像显示坐标系（考虑缩放和平移）
      const displayCx = displayW / 2
      const displayCy = displayH / 2
      const sx = Math.max(0, Math.round((displayCx + (cropX.value - imgOffsetX.value - displayCx - panX.value) / z) * (imageEl.naturalWidth / displayW)))
      const sy = Math.max(0, Math.round((displayCy + (cropY.value - imgOffsetY.value - displayCy - panY.value) / z) * (imageEl.naturalHeight / displayH)))
      const sw = Math.min(Math.round(cropW.value / z * (imageEl.naturalWidth / displayW)), imageEl.naturalWidth - sx)
      const sh = Math.min(Math.round(cropH.value / z * (imageEl.naturalHeight / displayH)), imageEl.naturalHeight - sy)
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

const confirmCrop = async () => {
  const croppedFile = await getCroppedFile()
  if (croppedFile) {
    const img = imageSearchImages.value[imageSearchSelectedIdx.value]
    if (img) {
      if (img._croppedFile) {
        URL.revokeObjectURL(img._croppedUrl)
      }
      img._croppedFile = croppedFile
      img._croppedUrl = URL.createObjectURL(croppedFile)
    }
  }
}

const doImageSearch = async () => {
  if (imageSearching.value) return
  imageSearching.value = true
  imageSearchDone.value = false
  imageSearchResults.value = []
  imageSearchError.value = ''
  const searchFile = await getCroppedFile()
  if (!searchFile) {
    imageSearching.value = false
    return
  }
  try {
    const formData = new FormData()
    formData.append('file', searchFile)
    const res = await api('/images/search-by-image?maxDistance=' + imageSearchThreshold, {
      method: 'POST',
      body: formData
    })
    if (res.code === 200) {
      imageSearchResults.value = res.data || []
      const needBackfill = localStorage.getItem('dhash_backfilled') !== '1'
      if (imageSearchResults.value.length === 0 && needBackfill) {
        await autoBackfillDhash()
        const retryRes = await api('/images/search-by-image?maxDistance=' + imageSearchThreshold, {
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

const findSimilar = async (item) => {
  if (!item.filePath) return
  try {
    const response = await fetch('/images/' + item.filePath)
    if (!response.ok) throw new Error('Failed to fetch image')
    const blob = await response.blob()
    const fileName = item.filePath.split('/').pop() || 'similar.jpg'
    const file = new File([blob], fileName, { type: blob.type || 'image/jpeg', lastModified: Date.now() })
    const url = URL.createObjectURL(file)
    // 清除之前选中图片的裁剪数据
    const prev = imageSearchImages.value[imageSearchSelectedIdx.value]
    if (prev && prev._croppedUrl) {
      URL.revokeObjectURL(prev._croppedUrl)
      delete prev._croppedFile
      delete prev._croppedUrl
    }
    imageSearchImages.value.push({ file, url, name: fileName })
    imageSearchSelectedIdx.value = imageSearchImages.value.length - 1
    imageSearchResults.value = []
    imageSearchDone.value = false
    resetCropState()
    doImageSearch()
  } catch (e) {
    imageSearchError.value = '获取图片失败'
    console.error('Find similar failed:', e)
  }
}

const viewImageSearchResult = async (item) => {
  photoModalSample.value = item
  photoModalIndex.value = 0
  const temp = {}
  if (item.firstImageHash) temp.hash = item.firstImageHash
  if (item.thumbnailPath) temp.thumbnailPath = item.thumbnailPath
  else if (item.thumbnail) temp.thumbnailPath = item.thumbnail
  photoModalImages.value = (item.firstImageHash || item.thumbnailPath || item.thumbnail) ? [temp] : []
  photoModalInit()
  showPhotoModal.value = true
  fetchPhotoModalImages(item.sampleId || item.id)
}

const showPhotoModal = ref(false)
const photoModalSample = ref(null)
const photoModalImages = ref([])
const photoModalIndex = ref(0)
const hideFactoryPrice = ref(false)
const hideSupplierInfo = ref(false)
const { photoModalPos, photoModalW, photoModalH, photoModalInit, startDragModal } = useModalDrag()

const photoModalStyle = computed(() => ({
  width: photoModalW.value + 'px',
  height: photoModalH.value + 'px',
  top: photoModalPos.y + 'px',
  left: photoModalPos.x + 'px',
  position: 'fixed'
}))

const fmt3 = (a, b, c) => {
  if ((a == null || a === '') && (b == null || b === '') && (c == null || c === '')) return '0'
  return [(a != null && a !== '' ? a : '0'), (b != null && b !== '' ? b : '0'), (c != null && c !== '' ? c : '0')].join('x')
}

const fetchPhotoModalImages = async (sampleId) => {
  if (!sampleId) return
  try {
    const res = await api(`/images/sample/${sampleId}`)
    const raw = (res.data || res) || []
    const images = Array.isArray(raw) ? raw : []
    if (images.length > 0) {
      photoModalImages.value = images
    }
  } catch {
    // 保持初始图片
  }
}

const closePhotoModal = () => {
  showPhotoModal.value = false
}

const photoModalPrev = () => {
  if (photoModalIndex.value > 0) photoModalIndex.value--
}

const photoModalNext = () => {
  if (photoModalIndex.value < photoModalImages.value.length - 1) photoModalIndex.value++
}

const openFullPreview = () => {
  const img = photoModalImages.value[photoModalIndex.value]
  if (img) {
    const src = img.hash ? '/images/view/hash/' + img.hash : '/thumbnails/' + img.thumbnailPath
    window.open(src, '_blank')
  }
}

const onModalImgError = (e) => {
  const img = photoModalImages.value[photoModalIndex.value]
  if (img?.thumbnailPath && e.target.src !== '/thumbnails/' + img.thumbnailPath) {
    e.target.src = '/thumbnails/' + img.thumbnailPath
  }
}

// 测量实际卡片行高
const measureRowHeight = () => {
  if (measuredRowHeight.value > 0) return
  const grid = resultAreaRef.value?.querySelector('.isp-card-grid')
  if (!grid) return
  const items = grid.querySelectorAll('.isp-card')
  if (items.length < colsPerRow.value) return
  const firstTop = items[0].getBoundingClientRect().top
  const lastInRow = items[Math.min(colsPerRow.value - 1, items.length - 1)]
  const rowBottom = lastInRow.getBoundingClientRect().bottom
  const measured = rowBottom - firstTop + GAP
  if (measured > 50) {
    measuredRowHeight.value = measured
  }
}

// 复制公司编号
const copyCardCode = async (code) => {
  if (!code) return
  try {
    await navigator.clipboard.writeText(code)
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = code
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try { document.execCommand('copy') } catch { /* ignore */ }
    document.body.removeChild(textarea)
  }
}

// 从图像搜索结果选取样品（发送事件给父组件 BatchAddView）
const emit = defineEmits(['addSample', 'addSamples'])
const addToBatchSample = (item) => {
  if (item.sampleId) {
    emit('addSample', item)
  }
}
defineExpose({ batchAddSelected, selectedResultIds, filteredResults })

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
  overflow: hidden;
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.isp-toolbar-row {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  gap: 16px;
  padding: 12px 16px;
  flex-wrap: nowrap;
}

.isp-search-slot {
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.isp-search-slot-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.isp-search-slot-preview {
  position: relative;
  width: 260px;
  height: 260px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #dde0e6;
  background: #f5f5f7;
}

.isp-search-slot-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.isp-search-slot-empty {
  width: 260px;
  height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(29,29,31,0.38);
  gap: 8px;
  border: 1px dashed #dde0e6;
  border-radius: 8px;
  font-size: 18px;
}

.isp-slot-quick {
  display: flex;
  gap: 6px;
}

.isp-btn-small {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 28px;
  padding: 0 10px;
  border-radius: 6px;
  border: 1px solid #dde0e6;
  background: #fff;
  color: #555;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.isp-btn-small:hover { background: #f0f0f5; border-color: #c0c0c4; }

.isp-image-strip-inline {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.isp-toolbar-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  align-self: center;
}

.crop-toggle-overlay {
  position: absolute;
  bottom: 4px;
  right: 4px;
  z-index: 10;
  height: 42px;
  padding: 0 16px;
  border: none;
  border-radius: 5px;
  background: #3b82f6;
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: background 0.15s;
}
.crop-toggle-overlay:hover { background: #2563eb; }

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

.isp-strip-inner::-webkit-scrollbar { height: 10px; }
.isp-strip-inner::-webkit-scrollbar-thumb { background: rgba(0,122,255,0.18); border-radius: 99px; }

.isp-thumb-card {
  width: 220px;
  height: 220px;
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
  object-fit: contain;
  display: block;
}

.isp-thumb-del {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #007aff;
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.isp-thumb-del:hover {
  background: #0056cc;
}

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
  width: 220px;
  height: 220px;
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
  gap: 6px;
  font-size: 18px;
  color: rgba(29,29,31,0.45);
  text-align: center;
  line-height: 1.35;
}

.isp-upload-btn small {
  font-size: 14px;
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

.isp-btn-large {
  height: 38px;
  padding: 0 20px;
  font-size: 14px;
}

.isp-results-area {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  background: #f7f8fa;
  border-radius: 0 0 12px 12px;
}

.isp-virtual-outer {
  position: relative;
  width: 100%;
}

.isp-virtual-inner {
  will-change: transform;
}

.isp-filter-bar {
  display: flex;
  align-items: center;
  padding: 14px 16px 0;
}

.isp-filter-bar-hint {
  font-size: 18px;
  color: rgba(29,29,31,0.45);
}

/* ── 卡片 ── */
.isp-card {
  content-visibility: auto;
  contain-intrinsic-size: auto 500px;
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  position: relative;
  user-select: none;
}

.isp-card:hover {
  border-color: rgba(0,122,255,0.3);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  transform: translateY(-3px);
}

.isp-card-checkbox {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 10;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: rgba(255,255,255,0.9);
  border: 2px solid rgba(0,0,0,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
  backdrop-filter: blur(4px);
}

.isp-card-checkbox input {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
  margin: 0;
}

.isp-card-checkbox svg {
  opacity: 0;
  transition: opacity 0.15s ease;
  color: #fff;
}

.isp-card-checkbox input:checked + svg {
  opacity: 1;
}

.isp-card-checkbox:has(input:checked) {
  background: #007aff;
  border-color: #007aff;
}

.isp-card-checkbox:hover {
  border-color: #007aff;
  transform: scale(1.1);
}

.isp-card-img {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.isp-card-img img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  background: #fafafa;
}

.isp-card-img-actions {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
  z-index: 2;
  pointer-events: none;
}

.isp-card:hover .isp-card-img-actions {
  opacity: 1;
  pointer-events: auto;
}

.isp-card-img-btn {
  height: 48px;
  padding: 0 22px;
  border-radius: 8px;
  background: rgba(0,0,0,0.55);
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 18px;
  font-weight: 600;
  transition: background 0.15s;
}

.isp-card-img-btn:hover {
  background: rgba(0,0,0,0.8);
}

.isp-card-score {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 18px;
  font-weight: 800;
  color: rgba(255,255,255,0.95);
  background: rgba(0,0,0,0.45);
  backdrop-filter: blur(4px);
  padding: 4px 10px;
  border-radius: 5px;
  line-height: 1.3;
  z-index: 1;
}

.isp-card-score.high { background: rgba(48,209,88,0.85); }
.isp-card-score.mid { background: rgba(0,122,255,0.85); }

.isp-card-body {
  padding: 16px 18px 18px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.isp-card-name {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 12px;
}

.isp-card-fields {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px 10px;
  align-items: start;
  margin-top: 6px;
}

.isp-card-fields-single {
  grid-template-columns: 1fr;
  margin-top: 0;
}

.isp-card-val {
  font-size: 30px;
  color: #000;
  font-family: "SimSun", "宋体", serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.isp-card-val-copy {
  display: flex;
  align-items: center;
  gap: 4px;
}

.isp-card-copy-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 3px;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transition: color 0.15s, background 0.15s;
}

.isp-card-copy-btn:hover {
  color: #007aff;
  background: rgba(0,122,255,0.08);
}

.isp-card-price {
  font-size: 28px !important;
  font-weight: 700;
  color: #e11d48;
}

.isp-card-code {
  color: #007aff;
}

.isp-card-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 8px 0;
}

.isp-card-last-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.isp-card-add-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 44px;
  padding: 0 20px;
  font-size: 20px;
  font-weight: 600;
  border: 1px solid #007aff;
  border-radius: 8px;
  background: #007aff;
  color: #fff;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;
}

.isp-card-add-btn:hover {
  background: #0056cc;
  border-color: #0056cc;
}

.isp-searching {
  text-align: center;
  padding: 60px 0 40px;
  color: rgba(29,29,31,0.45);
}

.isp-searching p {
  margin: 14px 0 0;
  font-size: 18px;
}

.isp-spin {
  animation: isp-spin 1s linear infinite;
}

@keyframes isp-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
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

.crop-modal-wrapper {
  position: fixed;
  inset: 0;
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.crop-modal {
  pointer-events: auto;
  width: 1200px;
  max-width: 98vw;
  max-height: 85vh;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 24px 80px rgba(0,0,0,0.18);
  overflow: hidden;
  display: flex;
  flex-direction: column;
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
  padding: 24px 32px 20px;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.crop-editor-wrap.crop-editor-modal {
  width: 100%;
  height: 800px;
  max-height: 80vh;
}

.crop-editor-wrap {
  position: relative;
  width: 240px;
  height: 240px;
  margin: 0 auto;
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
  width: 100%;
  height: 100%;
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

.crop-handle.tl { top: -5px; left: -5px; cursor: nw-resize; }
.crop-handle.tm { top: -5px; left: 50%; margin-left: -5px; cursor: n-resize; }
.crop-handle.tr { top: -5px; right: -5px; cursor: ne-resize; }
.crop-handle.rm { top: 50%; right: -5px; margin-top: -5px; cursor: e-resize; }
.crop-handle.br { bottom: -5px; right: -5px; cursor: se-resize; }
.crop-handle.bm { bottom: -5px; left: 50%; margin-left: -5px; cursor: s-resize; }
.crop-handle.bl { bottom: -5px; left: -5px; cursor: sw-resize; }
.crop-handle.lm { top: 50%; left: -5px; margin-top: -5px; cursor: w-resize; }

.crop-hint-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
  font-size: 12px;
  color: rgba(29,29,31,0.38);
}

.crop-hint-bar .ready {
  color: #30d158;
  font-weight: 600;
}

.crop-hint-text {
  font-size: 12px;
  color: rgba(29,29,31,0.38);
}

.crop-hint-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
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

<style scoped>
/* ── sample-photo-modal 模态框样式（独立,图像搜索页专用） ── */

.sample-photo-modal {
  position: fixed;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  background: #f5f6f8;
  border-radius: 16px;
  box-shadow: 0 24px 80px rgba(0,0,0,0.30), 0 4px 20px rgba(0,0,0,0.12), inset 0 1px 0 rgba(255,255,255,0.8);
  font-size: 13px;
  color: #1d1d1f;
  user-select: none;
  overflow: hidden !important;
}

.spm-header { height: 56px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between; border-radius: 16px 16px 0 0; background: linear-gradient(180deg, #fff, #f7f9fc); border-bottom: 1px solid rgba(0,122,255,0.10); cursor: move; flex-shrink: 0; }
.spm-header-title { font-size: 24px; font-weight: 720; letter-spacing: -0.01em; }
.spm-header-close { width: 36px; height: 36px; font-size: 22px; display: inline-flex; align-items: center; justify-content: center; border-radius: 50%; border: none; background: transparent; color: rgba(29,29,31,0.40); cursor: pointer; transition: all 0.15s; }
.spm-header-close:hover { background: rgba(255,59,48,0.10); color: #ff3b30; }

.spm-body { display: flex; flex-direction: column; gap: 1px; background: #fff; }
.spm-body-main { display: flex; flex: 1; min-height: 0; gap: 1px; }

.spm-top-card { gap: 30px; padding: 28px 36px; display: flex; background: #fff; border-bottom: 1px solid #e2e4ea; flex-wrap: wrap; flex-shrink: 0; }
.spm-top-card-field { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.spm-top-card-field span { font-size: 18px; color: rgba(29,29,31,0.46); font-weight: 500; }
.spm-top-card-field strong { font-size: 24px; color: #1d1d1f; font-weight: 700; }
.spm-top-card-field strong.spm-price { color: #e53e3e; }

.spm-body-left { width: 1280px; min-width: 1280px; display: flex; flex-direction: column; justify-content: center; background: #fff; padding: 18px; gap: 14px; flex-shrink: 0; }

.spm-main-img-wrap { width: 1200px; height: 900px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; background: #fafafa; border-radius: 10px; overflow: hidden; cursor: grab; position: relative; }
.spm-main-img-wrap img { max-width: 100%; max-height: 100%; object-fit: contain; user-select: none; -webkit-user-drag: none; }

.spm-main-img-nav { position: absolute; top: 50%; transform: translateY(-50%); width: 40px; height: 40px; border-radius: 50%; border: none; background: rgba(0,0,0,0.45); color: #fff; font-size: 22px; cursor: pointer; display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.15s; }
.spm-main-img-wrap:hover .spm-main-img-nav { opacity: 1; }
.spm-main-img-prev { left: 8px; }
.spm-main-img-next { right: 8px; }

.spm-thumb-strip { display: flex; gap: 8px; overflow-x: auto; padding: 3px 0; flex-shrink: 0; }
.spm-thumb-strip::-webkit-scrollbar { height: 4px; }
.spm-thumb-strip::-webkit-scrollbar-thumb { border-radius: 999px; background: rgba(0,122,255,0.18); }

.spm-thumb-item { flex-shrink: 0; width: 96px; height: 72px; border-radius: 6px; overflow: hidden; border: 2px solid transparent; cursor: pointer; background: #eee; transition: all 0.15s; position: relative; }
.spm-thumb-item.active { border-color: #007aff; box-shadow: 0 0 0 2px rgba(0,122,255,0.15); }
.spm-thumb-item:hover:not(.active) { border-color: rgba(0,122,255,0.35); }
.spm-thumb-item img { width: 100%; height: 100%; object-fit: cover; pointer-events: none; }

.spm-body-right { flex: 1; min-width: 480px; background: #fff; padding: 24px 28px; display: flex; flex-direction: column; gap: 0; }

.spm-field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0; }
.spm-field { display: flex; align-items: baseline; gap: 32px; padding: 12px 18px; border-bottom: 1px solid #f0f2f5; line-height: 1.4; }
.spm-field:nth-child(odd) { border-right: 1px solid #f0f2f5; }
.spm-field.spm-field-full { grid-column: 1 / -1; border-right: none !important; }

.spm-field-label { width: 120px; font-size: 28px; color: rgba(29,29,31,0.46); white-space: nowrap; flex-shrink: 0; font-weight: 600; text-align: left; }

.spm-field-value { font-size: 24px; font-weight: 600; color: #1d1d1f; word-break: break-all; flex: 1; text-align: left; }
.spm-field-value.spm-price { color: #ff3b30; font-weight: 750; }

.spm-field-dim { display: inline-flex; align-items: center; gap: 4px; font-size: 18px; color: #86868b; }

.spm-section-title { font-size: 22px; font-weight: 700; color: rgba(29,29,31,0.55); padding: 10px 14px 6px; margin-top: 4px; border-top: 1px dashed #e2e4ea; }

.spm-input { flex: 1; height: 48px; min-width: 0; border: 1px solid #d1d5db; border-radius: 8px; padding: 0 12px; font-size: 20px; color: #1d1d1f; background: #fff; outline: none; text-align: center; transition: border-color 0.15s, box-shadow 0.15s; }
.spm-input:focus { border-color: #007aff; box-shadow: 0 0 0 3px rgba(0,122,255,0.12); }
.spm-input-sm { width: 88px; flex: none; text-align: center; padding: 0 6px; }
.spm-input-ro { flex: 1; min-width: 0; font-size: 20px; font-weight: 600; color: #6b7280; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.spm-btn-edit { height: 46px; padding: 0 28px; border-radius: 10px; border: none; background: #007aff; color: #fff; font-size: 18px; font-weight: 650; cursor: pointer; transition: all 0.15s; white-space: nowrap; }
.spm-btn-edit:hover { background: #0066d6; }

.spm-btn-save { height: 46px; padding: 0 24px; border-radius: 10px; border: none; background: #007aff; color: #fff; font-size: 18px; font-weight: 650; cursor: pointer; transition: all 0.15s; white-space: nowrap; }
.spm-btn-save:hover { background: #0066d6; }

.spm-footer { display: flex; align-items: center; justify-content: space-between; padding: 18px 28px; background: linear-gradient(180deg, #f7f9fc, #f0f2f7); border-top: 1px solid rgba(0,122,255,0.08); border-radius: 0 0 16px 16px; gap: 12px; flex-shrink: 0; }

.spm-toggle-group { display: flex; gap: 14px; }
.spm-toggle { display: inline-flex; align-items: center; gap: 8px; font-size: 18px; color: rgba(29,29,31,0.55); cursor: pointer; user-select: none; white-space: nowrap; font-weight: 600; transition: color 0.15s; }
.spm-toggle:hover { color: #ff3b30; }
.spm-toggle input[type="checkbox"] { accent-color: #ff3b30; width: 18px; height: 18px; cursor: pointer; }

.spm-btn-close { height: 46px; padding: 0 24px; border-radius: 10px; border: 1px solid rgba(0,122,255,0.15); background: #fff; color: rgba(29,29,31,0.65); font-size: 18px; font-weight: 650; cursor: pointer; transition: all 0.15s; white-space: nowrap; }
.spm-btn-close:hover { background: rgba(0,122,255,0.06); border-color: rgba(0,122,255,0.25); color: #007aff; }

.spm-hidden { display: none !important; }

.spm-no-img { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; font-size: 24px; font-weight: 700; color: rgba(29,29,31,0.22); letter-spacing: 0.1em; }
</style>

