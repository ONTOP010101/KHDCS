<template>
  <header class="top-bar">
    <div class="page-heading">
      <h2>{{ pageTitle }}</h2>
      <p>{{ pageSubtitle }}</p>
    </div>

    <div class="top-actions">
      <div class="search-box">
        <Search class="w-4 h-4" />
        <span>搜索样品、图库、厂商...</span>
      </div>

      <button class="top-btn" title="通知">
        <Bell class="w-[18px] h-[18px]" />
        <span class="notif-dot"></span>
      </button>

      <button class="top-btn" title="新建">
        <Plus class="w-[18px] h-[18px]" />
      </button>

      <div class="top-avatar" :title="displayName">
        <div class="top-avatar-inner">{{ userInitials }}</div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Search, Bell, Plus } from 'lucide-vue-next'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const { state, getUserInitials } = useAuth()

const pageMeta = {
  Home: { title: '首页概览', subtitle: '拍摄任务、照片资产与系统状态总览' },
  Sample: { title: '样品资料', subtitle: '管理样品信息、拍摄记录与资料归档' },
  Gallery: { title: '择样图库', subtitle: '管理择样图片资料、代号、客户与拍摄记录' },
  Manufacturer: { title: '厂商资料', subtitle: '维护合作厂商资料与业务信息' },
  Friends: { title: '好友列表', subtitle: '好友会话、群聊、消息与文件沟通' },
  Users: { title: '用户管理', subtitle: '管理系统用户、账号状态与角色分配' },
  Roles: { title: '角色管理', subtitle: '管理系统角色、数据范围与功能权限' },
  Logs: { title: '系统日志', subtitle: '查看系统登录日志、操作日志与异常记录' }
}

const pageTitle = computed(() => pageMeta[route.name]?.title || route.meta.title || '页面')
const pageSubtitle = computed(() => pageMeta[route.name]?.subtitle || route.meta.subtitle || '')
const displayName = computed(() => state.userInfo?.realName || state.userInfo?.username || 'Admin')
const userInitials = computed(() => getUserInitials())
</script>
