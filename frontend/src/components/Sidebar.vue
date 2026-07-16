<template>
  <aside class="sidebar" :class="{ collapsed }">
    <div class="sidebar-shell">
      <div class="brand-area">
        <div class="brand-card">
          <div class="logo-box">
            <img src="/logo.png" alt="LOGO">
          </div>
          <div class="brand-info">
            <h1>资料管理系统</h1>
            <p>ON TOP</p>
          </div>
        </div>

        <div class="system-pill">
          <span class="online-dot"></span>
          <span>System Online</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-section">
          <div class="section-title">Overview</div>
          <div
            class="sidebar-item"
            :class="{ active: currentRoute === 'Home' }"
            @click="navigateTo('Home')"
          >
            <Home class="w-5 h-5" />
            <span>首页</span>
            <span class="item-badge">NEW</span>
          </div>
        </div>

        <div class="nav-section">
          <div class="section-title">Data</div>
          <button class="sidebar-item" @click="dataOpen = !dataOpen">
            <Folder class="w-5 h-5" />
            <span class="flex-1 text-left">资料管理</span>
            <ChevronDown class="chevron" :style="{ transform: dataOpen ? 'rotate(180deg)' : 'rotate(0deg)' }" />
          </button>

          <div class="sub-menu sidebar-sub" :class="{ open: dataOpen }">
            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'Sample' }"
              @click="navigateTo('Sample')"
            >
              <Database class="w-[18px] h-[18px]" />
              <span>样品资料</span>
              <span class="item-badge">128</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'Manufacturer' }"
              @click="navigateTo('Manufacturer')"
            >
              <Store class="w-[18px] h-[18px]" />
              <span>厂商资料</span>
              <span class="item-badge">56</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'StallInfo' }"
              @click="navigateTo('StallInfo')"
            >
              <Store class="w-[18px] h-[18px]" />
              <span>摊位资料</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'StallOverview' }"
              @click="navigateTo('StallOverview')"
            >
              <ChartBar class="w-[18px] h-[18px]" />
              <span>摊位概况</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'CustomerInfo' }"
              @click="navigateTo('CustomerInfo')"
            >
              <Contact class="w-[18px] h-[18px]" />
              <span>客户资料</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'RemovedManufacturer' }"
              @click="navigateTo('RemovedManufacturer')"
            >
              <ArchiveX class="w-[18px] h-[18px]" />
              <span>下架厂商资料</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'RemovedSample' }"
              @click="navigateTo('RemovedSample')"
            >
              <ArchiveX class="w-[18px] h-[18px]" />
              <span>下架样品资料</span>
            </div>
          </div>
        </div>

        <div class="nav-section">
          <div class="section-title">Business</div>
          <button class="sidebar-item" @click="businessOpen = !businessOpen">
            <Briefcase class="w-5 h-5" />
            <span class="flex-1 text-left">业务管理</span>
            <ChevronDown class="chevron" :style="{ transform: businessOpen ? 'rotate(180deg)' : 'rotate(0deg)' }" />
          </button>

          <div class="sub-menu sidebar-sub" :class="{ open: businessOpen }">
            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'ClientSample' }"
              @click="navigateTo('ClientSample')"
            >
              <ClipboardCheck class="w-[18px] h-[18px]" />
              <span>客户择样</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'Gallery' }"
              @click="navigateTo('Gallery')"
            >
              <ImageIcon class="w-[18px] h-[18px]" />
              <span>择样图库</span>
              <span class="item-badge">3.6k</span>
            </div>
          </div>
        </div>

        <div class="nav-section">
          <div class="section-title">Inventory</div>
          <button class="sidebar-item" @click="inventoryOpen = !inventoryOpen">
            <Package class="w-5 h-5" />
            <span class="flex-1 text-left">库存管理</span>
            <ChevronDown class="chevron" :style="{ transform: inventoryOpen ? 'rotate(180deg)' : 'rotate(0deg)' }" />
          </button>

          <div class="sub-menu sidebar-sub" :class="{ open: inventoryOpen }">
            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'Inventory' }"
              @click="navigateTo('Inventory')"
            >
              <Package class="w-[18px] h-[18px]" />
              <span>总库存</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'ManufacturerExport' }"
              @click="navigateTo('ManufacturerExport')"
            >
              <Package class="w-[18px] h-[18px]" />
              <span>入库管理</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'ManufacturerOutbound' }"
              @click="navigateTo('ManufacturerOutbound')"
            >
              <Package class="w-[18px] h-[18px]" />
              <span>出库管理</span>
            </div>
          </div>
        </div>

        <div class="nav-section">
          <div class="section-title">Social</div>
          <div
            class="sidebar-item"
            :class="{ active: currentRoute === 'Friends' }"
            @click="navigateTo('Friends')"
          >
            <Users class="w-5 h-5" />
            <span>好友列表</span>
            <div class="avatar-stack">
              <div class="avatar-mini avatar-a">A</div>
              <div class="avatar-mini avatar-b">B</div>
              <div class="avatar-mini avatar-more">+22</div>
            </div>
          </div>
        </div>

        <div class="nav-section">
          <div class="section-title">System</div>
          <button class="sidebar-item" @click="systemOpen = !systemOpen">
            <Settings class="w-5 h-5" />
            <span class="flex-1 text-left">系统管理</span>
            <ChevronDown class="chevron" :style="{ transform: systemOpen ? 'rotate(180deg)' : 'rotate(0deg)' }" />
          </button>

          <div class="sub-menu sidebar-sub" :class="{ open: systemOpen }">
            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'Users' }"
              @click="navigateTo('Users')"
            >
              <UsersRound class="w-[18px] h-[18px]" />
              <span>用户管理</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'Roles' }"
              @click="navigateTo('Roles')"
            >
              <ShieldCheck class="w-[18px] h-[18px]" />
              <span>角色管理</span>
            </div>

            <div
              class="sidebar-item"
              :class="{ active: currentRoute === 'Logs' }"
              @click="navigateTo('Logs')"
            >
              <FileClock class="w-[18px] h-[18px]" />
              <span>系统日志</span>
              <span class="pulse-dot"></span>
            </div>
          </div>
        </div>
      </nav>

      <div class="sidebar-collapse-area">
        <button class="collapse-toggle" @click="collapsed = !collapsed" title="折叠侧栏">
          <ChevronLeft v-if="!collapsed" class="w-5 h-5" />
          <ChevronRight v-else class="w-5 h-5" />
          <span>折叠侧栏</span>
        </button>
      </div>

      <div class="sidebar-footer">
        <div class="user-card">
          <div class="user-avatar">
            <div class="user-avatar-inner">{{ userInitials }}</div>
            <div class="user-online"></div>
          </div>
          <div class="user-info">
            <div class="user-name">{{ displayName }}</div>
            <div class="user-role">{{ roleName }}</div>
          </div>
          <button class="logout-btn" title="退出登录" @click="handleLogout">
            <LogOut class="w-[17px] h-[17px]" />
          </button>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Home, Folder, ChevronDown, ChevronLeft, ChevronRight, Database, Image as ImageIcon, Store,
  Users, UsersRound, Settings, ShieldCheck, FileClock, LogOut,
  Briefcase, ClipboardCheck, ChartBar, Contact, ArchiveX, FileText, ListChecks, Package
} from 'lucide-vue-next'
import { useAuth } from '@/stores/auth'
import { useSidebar } from '@/composables/useSidebar'

const router = useRouter()
const route = useRoute()
const { state, clearAuth, getUserInitials } = useAuth()

const { collapsed } = useSidebar()

const dataOpen = ref(true)
const businessOpen = ref(false)
const systemOpen = ref(false)
const inventoryOpen = ref(true)

const currentRoute = computed(() => route.name)

const displayName = computed(() => state.userInfo?.realName || state.userInfo?.username || 'Admin')
const roleName = computed(() => state.userInfo?.roleName || '系统管理员')
const userInitials = computed(() => getUserInitials())

function navigateTo(name) {
  router.push({ name })
}

function handleLogout() {
  clearAuth()
  router.push('/login')
}

watch(currentRoute, (name) => {
  const dataRoutes = ['Sample', 'Manufacturer', 'StallInfo', 'StallOverview', 'CustomerInfo', 'RemovedManufacturer', 'RemovedSample', 'ReferenceData']
  const businessRoutes = ['ClientSample', 'Gallery']
  const inventoryRoutes = ['Inventory', 'ManufacturerExport', 'InventoryDetail', 'ManufacturerOutbound', 'OutboundDetail']
  const systemRoutes = ['Users', 'Roles', 'Logs']
  if (dataRoutes.includes(name)) dataOpen.value = true
  if (businessRoutes.includes(name)) businessOpen.value = true
  if (inventoryRoutes.includes(name)) inventoryOpen.value = true
  if (systemRoutes.includes(name)) systemOpen.value = true
}, { immediate: true })
</script>
