import { computed, nextTick, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useAuth } from '@/stores/auth'
import { api } from '@/api/index'
import { VxeUI } from 'vxe-table'

// vxe 内置的 localStorage key，仅用作 vxe 和后端之间的数据桥梁，不作为持久化存储
const VXE_CUSTOM_STORE = 'VXE_CUSTOM_STORE'
// vxe 版本号，localStorage 中必须包含 _v 字段才能通过 vxe 内部校验
const VXE_CONFIG_VERSION = VxeUI.getConfig().version

/**
 * vxe-grid 列设置跨设备同步
 * localStorage 仅作为 vxe 和后端 API 之间的数据中转，真正的持久化在后端
 *
 * 数据流:
 *   加载: GET 后端 → 写入 localStorage → grid 挂载 → vxe restoreCustomStorage 读取 localStorage
 *         → 自动应用 visibleData / resizableData / sortData（含列顺序） / fixedData
 *   保存: 用户操作 → vxe 写入 localStorage → 读取 → POST 后端
 *
 * 关键设计：在 grid 挂载之前将后端数据写入 localStorage，
 * 利用 vxe 内置的 restoreCustomStorage 一次性恢复所有列设置（含拖拽列顺序）。
 */
export function useGridPrefSync(gridRef, pageKey, columnsRef, options = {}) {
  const auth = useAuth()
  const ready = ref(false)
  let synced = false
  let saving = false
  let saveTimer = null // 防抖定时器，合并短时间内多次 saveToBackend 调用
  let initializing = false // 初始化期间禁止保存，防止 @resizable-change 等事件触发错误写入
  let lastFullPref = null // 缓存最近一次完整的偏好数据，保存时用于补全 vxe 可能丢失的字段（如 sortData）
  const prefVersion = options.prefVersion ?? 0 // 偏好版本号，变更时自动清除旧缓存

  const fullKey = computed(() => {
    const uid = auth.state?.userInfo?.userId || 'default'
    return `${pageKey}-${uid}`
  })

  function getVxeStoreEntry() {
    try {
      const raw = localStorage.getItem(VXE_CUSTOM_STORE)
      if (!raw) return null
      const maps = JSON.parse(raw)
      return maps[fullKey.value] || null
    } catch { return null }
  }

  function setVxeStoreEntry(data) {
    try {
      const raw = localStorage.getItem(VXE_CUSTOM_STORE)
      let maps = raw ? JSON.parse(raw) : {}
      if (data) {
        maps[fullKey.value] = data
      } else {
        delete maps[fullKey.value]
      }
      maps._v = VXE_CONFIG_VERSION
      localStorage.setItem(VXE_CUSTOM_STORE, JSON.stringify(maps))
    } catch {}
  }

  /**
   * 从后端拉取列设置，预先写入 localStorage。
   * grid 挂载后 vxe 的 restoreCustomStorage 会自动从 localStorage 读取并应用所有列设置。
   */
  async function syncFromBackend() {
    if (!auth.state?.userInfo?.userId) {
      return
    }
    try {
      const url = `/users/preferences/${fullKey.value}?_t=${Date.now()}`
      const res = await api(url, {
        headers: { 'Cache-Control': 'no-cache', 'Pragma': 'no-cache' }
      })
      const data = res?.data || res
      if (data?.preferenceJson) {
        const pref = JSON.parse(data.preferenceJson)
        // 版本号不匹配时，丢弃旧缓存，使用新的默认列设置
        if (pref._prefVersion !== prefVersion) {
          setVxeStoreEntry(null)
        } else {
          lastFullPref = { ...pref }
          setVxeStoreEntry(pref)
        }
      }
      // 无后端数据时不写 localStorage，vxe 使用默认列设置
    } catch (e) {
      // 网络异常时也不写，让 vxe 用默认值
    }
    synced = true
    ready.value = true
  }

  /**
   * 从 vxe localStorage 桥梁读取，保存到后端
   */
  async function saveToBackend() {
    // 防抖：300ms 内多次调用只执行最后一次，避免 @resizable-change 在 @custom 确认后立即触发不完整保存
    if (saveTimer) clearTimeout(saveTimer)
    saveTimer = setTimeout(() => doSaveToBackend(), 300)
  }

  async function doSaveToBackend() {
    if (saving) return
    if (initializing) { return }
    if (!auth.state?.userInfo?.userId) return
    if (!gridRef.value) return

    await nextTick()
    const entry = getVxeStoreEntry()
    if (!entry) {
      return
    }

    // 用缓存的完整数据补全 vxe localStorage 中可能丢失的字段
    if (lastFullPref) {
      if (lastFullPref.sortData && !entry.sortData) {
        entry.sortData = lastFullPref.sortData
      }
    }

    saving = true
    // 写入版本号，便于未来版本变更时自动失效旧缓存
    entry._prefVersion = prefVersion
    const bodyJson = JSON.stringify({ preferenceJson: JSON.stringify(entry) })
    try {
      await api(`/users/preferences/${fullKey.value}`, {
        method: 'POST',
        body: bodyJson,
        keepalive: true
      })
      // 保存成功后更新缓存
      lastFullPref = JSON.parse(JSON.stringify(entry))
    } catch (e) {
    } finally {
      saving = false
    }
  }

  // 监听 userId 变化，触发首次同步
  const stopWatch = watch(
    () => auth.state?.userInfo?.userId,
    (uid) => {
      if (uid && !synced) {
        syncFromBackend()
      } else if (!uid) {
        ready.value = true
      }
    },
    { immediate: true }
  )

  // grid 渲染完成后，等待 vxe 内部初始化完成，加初始化锁防误保存
  watch(ready, (isReady) => {
    if (!isReady) return
    tryApply(0)
  })

  function tryApply(retries) {
    const grid = gridRef.value
    if (!grid) { setTimeout(() => tryApply(retries + 1), 100); return }

    const cols = grid.getColumns?.() || []
    if (!cols.length && retries < 10) {
      setTimeout(() => tryApply(retries + 1), 100)
      return
    }

    // grid 已就绪，加初始化锁防止 @resizable-change 等事件过早触发保存
    initializing = true

    // 等待 vxe 内部稳定后解除初始化锁
    // 所有列设置（visibleData / resizableData / sortData）已由 vxe 的 restoreCustomStorage 自动应用
    setTimeout(() => {
      // 更新缓存，确保后续补全逻辑有最新数据
      const entry = getVxeStoreEntry()
      if (entry) {
        lastFullPref = { ...entry }
      }
      setTimeout(() => {
        initializing = false
        if (saveTimer) { clearTimeout(saveTimer); saveTimer = null }
      }, 500)
    }, 300)
  }

  onMounted(() => {
    window.addEventListener('beforeunload', saveToBackend)
  })

  onBeforeUnmount(() => {
    saveToBackend()
    stopWatch()
    window.removeEventListener('beforeunload', saveToBackend)
  })

  return { fullKey, saveToBackend, ready }
}
