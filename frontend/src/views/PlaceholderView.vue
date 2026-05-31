<template>
  <div class="empty-page">
    <div>
      <div class="empty-icon">
        <component :is="iconComponent" class="w-8 h-8" />
      </div>
      <h3>{{ title }}</h3>
      <p>{{ subtitle }}</p>
      <p style="margin-top: 12px; font-size: 12px; color: rgba(29,29,31,0.36);">该功能将在后续阶段迁移</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Database, Image, Store, Users, UsersRound, ShieldCheck, FileClock
} from 'lucide-vue-next'

const route = useRoute()

const iconMap = {
  'database': Database,
  'image': Image,
  'store': Store,
  'users': Users,
  'users-round': UsersRound,
  'shield-check': ShieldCheck,
  'file-clock': FileClock
}

const title = computed(() => route.meta.title || route.name)
const subtitle = computed(() => route.meta.subtitle || '当前功能页面')
const iconComponent = computed(() => iconMap[route.meta.icon] || Database)
</script>
