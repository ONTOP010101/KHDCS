<template>
  <div class="tab-bar-wrap">
    <div class="tab-bar" ref="tabBarRef">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: tab.key === activeKey }"
        @click="switchTab(tab)"
      >
        <component :is="getIcon(tab.icon)" class="w-3.5 h-3.5" />
        <span>{{ tab.title }}</span>
        <span
          v-if="tab.key !== 'Home'"
          class="tab-close"
          title="关闭"
          @click.stop="closeTab(tab.key)"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Home, Database, Image, Store, Users, UsersRound, ShieldCheck, FileClock, Search, ClipboardCheck, Contact, Package, ArchiveX, ChartBar, Send
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()

const iconMap = {
  'home': Home,
  'database': Database,
  'image': Image,
  'store': Store,
  'users': Users,
  'users-round': UsersRound,
  'shield-check': ShieldCheck,
  'file-clock': FileClock,
  'search': Search,
  'clipboard-check': ClipboardCheck,
  'contact': Contact,
  'package': Package,
  'archive-x': ArchiveX,
  'chart-bar': ChartBar,
  'send': Send
}

const tabBarRef = ref(null)

const STORAGE_KEY = 'tabbar_tabs'

function loadTabs() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (raw) {
      const saved = JSON.parse(raw)
      if (Array.isArray(saved) && saved.length > 0) {
        return saved
      }
    }
  } catch {
    // ignore
  }
  return null
}

function saveTabs(data) {
  try {
    const serialized = data.map(t => ({
      key: t.key,
      title: t.title,
      icon: t.icon,
      routeName: t.routeName,
      manufacturerCode: t.manufacturerCode || null,
      codeName: t.codeName || null
    }))
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(serialized))
  } catch {
    // ignore
  }
}

const tabMeta = {
  Home: { title: '首页', icon: 'home' },
  Sample: { title: '样品资料', icon: 'database' },
  SampleManufacturer: { title: '样品资料', icon: 'database' },
  Gallery: { title: '择样图库', icon: 'image' },
  Manufacturer: { title: '厂商资料', icon: 'store' },
  Friends: { title: '好友列表', icon: 'users' },
  Users: { title: '用户管理', icon: 'users-round' },
  Roles: { title: '角色管理', icon: 'shield-check' },
  Logs: { title: '系统日志', icon: 'file-clock' },
  ImageSearch: { title: '图像搜索', icon: 'search' },
  ClientSample: { title: '客户择样', icon: 'clipboard-check' },
  ClientSampleCode: { title: '客户择样', icon: 'clipboard-check' },
  ClientSampleBatchAdd: { title: '批量添加', icon: 'clipboard-check' },
  ClientSampleSms: { title: '群发短信', icon: 'send' },
  Inventory: { title: '总库存', icon: 'package' },
  CustomerInfo: { title: '客户资料', icon: 'contact' },
  ManufacturerExport: { title: '入库管理', icon: 'package' },
  InventoryDetail: { title: '入库详情', icon: 'package' },
  ManufacturerOutbound: { title: '出库管理', icon: 'package' },
  OutboundDetail: { title: '出库详情', icon: 'package' }
}

const savedTabs = loadTabs()
const tabs = ref(
  savedTabs
    ? (savedTabs.some(t => t.key === 'Home') ? savedTabs : [{ key: 'Home', title: '首页', icon: 'home', routeName: 'Home' }, ...savedTabs])
    : [{ key: 'Home', title: '首页', icon: 'home', routeName: 'Home' }]
)

watch(tabs, (val) => {
  saveTabs(val)
}, { deep: true })

const activeKey = computed(() => {
  if (route.name === 'SampleManufacturer' && route.params.manufacturerCode) {
    return `SampleManufacturer_${route.params.manufacturerCode}`
  }
  if (route.name === 'ClientSampleCode' && route.params.codeName) {
    return `ClientSampleCode_${route.params.codeName}`
  }
  if (route.name === 'ClientSampleBatchAdd' && route.params.codeName) {
    return `ClientSampleBatchAdd_${route.params.codeName}`
  }
  if (route.name === 'ClientSampleSms' && route.params.codeName) {
    return `ClientSampleSms_${route.params.codeName}`
  }
  if (route.name === 'InventoryDetail' && route.params.codeName) {
    return `InventoryDetail_${route.params.codeName}`
  }
  if (route.name === 'OutboundDetail' && route.params.codeName) {
    return `OutboundDetail_${route.params.codeName}`
  }
  return route.name || ''
})

function getIcon(iconName) {
  return iconMap[iconName] || Home
}

function switchTab(tab) {
  try {
    if (tab.routeName === 'SampleManufacturer') {
      router.push({ name: 'SampleManufacturer', params: { manufacturerCode: tab.manufacturerCode } })
    } else if (tab.routeName === 'ClientSampleCode') {
      router.push({ name: 'ClientSampleCode', params: { codeName: tab.codeName } })
    } else if (tab.routeName === 'ClientSampleBatchAdd') {
      router.push({ name: 'ClientSampleBatchAdd', params: { codeName: tab.codeName } })
    } else if (tab.routeName === 'ClientSampleSms') {
      router.push({ name: 'ClientSampleSms', params: { codeName: tab.codeName } })
    } else if (tab.routeName === 'InventoryDetail') {
      router.push({ name: 'InventoryDetail', params: { codeName: tab.codeName } })
    } else if (tab.routeName === 'OutboundDetail') {
      router.push({ name: 'OutboundDetail', params: { codeName: tab.codeName } })
    } else if (tab.routeName) {
      router.push({ name: tab.routeName })
    }
  } catch {
    // ignore
  }
}

function closeTab(key) {
  const idx = tabs.value.findIndex(t => t.key === key)
  if (idx === -1) return

  const wasActive = tabs.value[idx].key === activeKey.value
  tabs.value.splice(idx, 1)

  if (wasActive) {
    const target = tabs.value[Math.min(idx, tabs.value.length - 1)]
    if (target) {
      switchTab(target)
    }
  }
}

watch(activeKey, (key) => {
  if (!key) return
  const exists = tabs.value.find(t => t.key === key)
  if (!exists) {
    const name = route.name
    const meta = tabMeta[name] || { title: name, icon: 'home' }
    let title = meta.title
    let manufacturerCode = null
    let codeName = null

    if (name === 'SampleManufacturer' && route.params.manufacturerCode) {
      manufacturerCode = route.params.manufacturerCode
      title = `样品资料-${manufacturerCode}`
    }
    if (name === 'ClientSampleCode' && route.params.codeName) {
      codeName = route.params.codeName
      title = `客户择样-${codeName}`
    }
    if (name === 'ClientSampleBatchAdd' && route.params.codeName) {
      codeName = route.params.codeName
      title = `批量添加-${codeName}`
    }
    if (name === 'ClientSampleSms' && route.params.codeName) {
      codeName = route.params.codeName
      title = `群发短信-${codeName}`
    }
    if (name === 'InventoryDetail' && route.params.codeName) {
      codeName = route.params.codeName
      title = `入库管理-${codeName}`
    }
    if (name === 'OutboundDetail' && route.params.codeName) {
      codeName = route.params.codeName
      title = `出库管理-${codeName}`
    }

    tabs.value.push({
      key,
      title,
      icon: meta.icon,
      routeName: name,
      manufacturerCode,
      codeName
    })
  }
  nextTick(() => {
    const activeTab = tabBarRef.value?.querySelector('.tab-item.active')
    if (activeTab) {
      activeTab.scrollIntoView({ behavior: 'smooth', inline: 'nearest', block: 'nearest' })
    }
  })
}, { immediate: true })
</script>
