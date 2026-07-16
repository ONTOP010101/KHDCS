import { ref, reactive } from 'vue'

const MODAL_W = 2300
const MODAL_H = 1200

export function useModalDrag() {
  const photoModalPos = reactive({ x: 0, y: 0 })
  const photoModalW = ref(MODAL_W)
  const photoModalH = ref(MODAL_H)

  const photoModalInit = () => {
    photoModalW.value = MODAL_W
    photoModalH.value = MODAL_H
    photoModalPos.x = Math.max(0, Math.round((window.innerWidth - MODAL_W) / 2))
    photoModalPos.y = Math.max(0, Math.round((window.innerHeight - MODAL_H) / 2))
  }

  let dragStart = null
  const startDragModal = (e) => {
    dragStart = { x: e.clientX - photoModalPos.x, y: e.clientY - photoModalPos.y }
    const onMove = (ev) => {
      photoModalPos.x = Math.max(0, Math.min(ev.clientX - dragStart.x, window.innerWidth - photoModalW.value))
      photoModalPos.y = Math.max(0, Math.min(ev.clientY - dragStart.y, window.innerHeight - photoModalH.value))
    }
    const onUp = () => {
      document.removeEventListener('mousemove', onMove)
      document.removeEventListener('mouseup', onUp)
    }
    document.addEventListener('mousemove', onMove)
    document.addEventListener('mouseup', onUp)
  }

  return {
    photoModalPos,
    photoModalW,
    photoModalH,
    photoModalInit,
    startDragModal,
  }
}
