import { ref, reactive } from 'vue'

// ===== 全局 Toast 提示（模块级，跨页面共享） =====
const toast = reactive({ show: false, message: '' })
let toastTimer = null
function showToast(message) {
  toast.message = message
  toast.show = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.show = false }, 2000)
}

// ===== 仓储业务共享状态 =====
const currentCode = ref('')
const currentCompanyName = ref('')
const sampleData = ref([])
const autoSaveEnabled = ref(false)
const pendingSample = ref(null)

export function useWarehouse() {
  return {
    toast,
    showToast,
    currentCode,
    currentCompanyName,
    sampleData,
    autoSaveEnabled,
    pendingSample
  }
}
