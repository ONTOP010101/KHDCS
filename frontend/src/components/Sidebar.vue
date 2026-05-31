<template>
  <aside class="sidebar">
    <div class="sidebar-shell">
      <div class="brand-area">
        <div class="brand-card">
          <div class="logo-box">
            <img src="/logo.png" alt="LOGO">
          </div>
          <div class="brand-info">
            <h1>拍照管理系统</h1>
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
              :class="{ active: currentRoute === 'Gallery' }"
              @click="navigateTo('Gallery')"
            >
              <ImageIcon class="w-[18px] h-[18px]" />
              <span>择样图库</span>
              <span class="item-badge">3.6k</span>
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
  Home, Folder, ChevronDown, Database, Image as ImageIcon, Store,
  Users, UsersRound, Settings, ShieldCheck, FileClock, LogOut
} from 'lucide-vue-next'
import { useAuth } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const { state, clearAuth, getUserInitials } = useAuth()

const dataOpen = ref(true)
const systemOpen = ref(false)

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
  const dataRoutes = ['Sample', 'Gallery', 'Manufacturer']
  const systemRoutes = ['Users', 'Roles', 'Logs']
  if (dataRoutes.includes(name)) dataOpen.value = true
  if (systemRoutes.includes(name)) systemOpen.value = true
}, { immediate: true })
</script>
