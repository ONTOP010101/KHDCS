import { ref, reactive, computed } from 'vue'

export function useCrop() {
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

  return {
    cropEditorRef,
    cropImgRef,
    cropSelecting,
    cropDone,
    cropX,
    cropY,
    cropW,
    cropH,
    cropOverlayStyle,
    cropBoxStyle,
    cropDisplayScale,
    resetCropState,
    resetCrop,
    onCropImgLoad,
    onCropMouseDown,
    onCropMouseMove,
    onCropMouseUp,
    onHandleDown,
  }
}
