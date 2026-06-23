import { ref, reactive } from 'vue'

export function useToast() {
  // ===== Toast 提示 =====
  const toast = reactive({ show: false, message: '', type: 'info' })
  let toastTimer = null
  function showToast(msg, type = 'info') {
    toast.message = msg
    toast.type = type
    toast.show = true
    clearTimeout(toastTimer)
    toastTimer = setTimeout(() => { toast.show = false }, 2500)
  }

  // ===== 确认弹窗（返回 Promise） =====
  const showConfirm = ref(false)
  const confirmMessage = ref('')
  let confirmResolve = null

  const showConfirmDialog = (msg) => {
    return new Promise((resolve) => {
      confirmMessage.value = msg
      confirmResolve = resolve
      showConfirm.value = true
    })
  }

  const onConfirmOk = () => {
    showConfirm.value = false
    if (confirmResolve) confirmResolve(true)
  }

  const onConfirmCancel = () => {
    showConfirm.value = false
    if (confirmResolve) confirmResolve(false)
  }

  // ===== 提示弹窗（返回 Promise） =====
  const showAlert = ref(false)
  const alertMessage = ref('')
  const alertType = ref('info')
  let alertResolve = null

  const showAlertDialog = (msg, type = 'info') => {
    return new Promise((resolve) => {
      alertMessage.value = msg
      alertType.value = type
      alertResolve = resolve
      showAlert.value = true
    })
  }

  const onAlertClose = () => {
    showAlert.value = false
    if (alertResolve) alertResolve()
  }

  return {
    toast,
    showToast,
    showConfirm,
    confirmMessage,
    showConfirmDialog,
    onConfirmOk,
    onConfirmCancel,
    showAlert,
    alertMessage,
    alertType,
    showAlertDialog,
    onAlertClose,
  }
}
