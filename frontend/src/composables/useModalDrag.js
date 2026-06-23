import { ref, reactive } from 'vue'

export function useModalDrag() {
  const photoModalPos = reactive({ x: 0, y: 0 })
  const photoModalW = ref(1000)
  const photoModalH = ref(680)

  const photoModalInit = () => {
    photoModalW.value = Math.min(1000, window.innerWidth - 40)
    photoModalH.value = Math.min(680, window.innerHeight - 40)
    photoModalPos.x = Math.round((window.innerWidth - photoModalW.value) / 2)
    photoModalPos.y = Math.round((window.innerHeight - photoModalH.value) / 2)
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
