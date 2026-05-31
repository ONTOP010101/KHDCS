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
          <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.8" stroke-linecap="round">
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
  Home, Database, Image, Store, Users, UsersRound, ShieldCheck, FileClock, Search
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
  'search': Search
}

const tabBarRef = ref(null)

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
  ImageSearch: { title: '图像搜索', icon: 'search' }
}

const tabs = ref([
  { key: 'Home', title: '首页', icon: 'home', routeName: 'Home' }
])

const activeKey = computed(() => {
  if (route.name === 'SampleManufacturer' && route.params.manufacturerCode) {
    return `SampleManufacturer_${route.params.manufacturerCode}`
  }
  return route.name || ''
})

function getIcon(iconName) {
  return iconMap[iconName] || Home
}

function switchTab(tab) {
  if (tab.routeName === 'SampleManufacturer') {
    router.push({ name: 'SampleManufacturer', params: { manufacturerCode: tab.manufacturerCode } })
  } else {
    router.push({ name: tab.routeName })
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

    if (name === 'SampleManufacturer' && route.params.manufacturerCode) {
      manufacturerCode = route.params.manufacturerCode
      title = `样品资料-${manufacturerCode}`
    }

    tabs.value.push({
      key,
      title,
      icon: meta.icon,
      routeName: name,
      manufacturerCode
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
