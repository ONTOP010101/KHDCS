<template>
  <div class="warehouse-app">
    <router-view />

    <!-- 全局 Toast -->
    <div class="wh-toast" :class="{ show: toast.show }">{{ toast.message }}</div>
  </div>
</template>

<script setup>
import { useWarehouse } from '@/composables/useWarehouse'

const { toast } = useWarehouse()
</script>
