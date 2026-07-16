<template>
  <div class="warehouse-page wh-sample-page">
    <div class="wh-sample-top-actions">
      <button class="wh-top-action-btn setting" @click="goToCodePage">设置</button>
      <button class="wh-top-action-btn query" @click="goToOtherFunction">查询</button>
    </div>

    <div class="wh-scan-section">
      <div class="wh-sample-search-row">
        <input
          v-model="sampleInput"
          class="wh-scan-input"
          placeholder="输入或扫描样品编号"
          @keydown.enter="searchSample"
        >
        <button class="wh-btn-search" @click="searchSample">查询</button>
      </div>
    </div>

    <div class="wh-data-panel">
      <div v-if="!sampleData.length" class="wh-empty-data">无数据</div>
      <div v-else class="wh-data-list">
        <div v-for="item in sampleData" :key="item.number" class="wh-sample-card">
          <div class="wh-sample-main">
            <img class="wh-sample-thumb" :src="item.image" alt="样品缩略图">
            <div class="wh-sample-info">
              <div class="wh-sample-row"><span class="wh-sample-label">编号:</span><span class="wh-sample-value">{{ item.number }}</span></div>
              <div class="wh-sample-row"><span class="wh-sample-label">货号:</span><span class="wh-sample-value">{{ item.itemNo }}</span></div>
              <div class="wh-sample-row"><span class="wh-sample-label">价格①:</span><span class="wh-sample-value">{{ item.priceOne }}</span></div>
              <div class="wh-sample-row"><span class="wh-sample-label">价格②:</span><span class="wh-sample-value">{{ item.priceTwo }}</span></div>
              <div class="wh-sample-row"><span class="wh-sample-label">装量:</span><span class="wh-sample-value">{{ item.packingQty }}</span></div>
              <div class="wh-sample-row"><span class="wh-sample-label">品名:</span><span class="wh-sample-value">{{ item.productName }}</span></div>
            </div>
          </div>
          <div class="wh-sample-detail-grid">
            <div class="wh-sample-row"><span class="wh-sample-label">包装方式:</span><span class="wh-sample-value">{{ item.packageWay }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">体积/材积:</span><span class="wh-sample-value">{{ item.volume }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">外箱毛/净:</span><span class="wh-sample-value">{{ item.cartonWeight }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">样品毛/净:</span><span class="wh-sample-value">{{ item.sampleWeight }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">外箱规格:</span><span class="wh-sample-value">{{ item.cartonSize }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">包装规格:</span><span class="wh-sample-value">{{ item.packageSize }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">样品规格:</span><span class="wh-sample-value">{{ item.sampleSize }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">摊位号:</span><span class="wh-sample-value">{{ item.boothNo }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">产品认证:</span><span class="wh-sample-value">{{ item.productCert }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">厂商认证:</span><span class="wh-sample-value">{{ item.factoryCert }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">样品日期:</span><span class="wh-sample-value">{{ item.sampleDate }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">箱数:</span><span class="wh-sample-value">{{ item.boxCount }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">中文备注:</span><span class="wh-sample-value">{{ item.chineseRemark }}</span></div>
            <div class="wh-sample-row"><span class="wh-sample-label">其他备注:</span><span class="wh-sample-value">{{ item.otherRemark }}</span></div>
          </div>
        </div>
      </div>
    </div>

    <div class="wh-sample-bottom-bar">
      <div class="wh-bottom-summary">
        <span>本次代号:{{ currentCode || '******' }}</span>
        <span>合计数量:{{ bottomTotal }}</span>
        <span class="wh-bottom-company">"{{ currentCompanyName || '公司名称' }}"</span>
      </div>
      <div class="wh-bottom-actions">
        <label class="wh-bottom-checkbox">
          <input v-model="fillBoxCount" type="checkbox"><span>填写箱数</span>
        </label>
        <label class="wh-bottom-checkbox">
          <input v-model="autoSaveEnabled" type="checkbox" @change="onToggleSaveMode"><span>自动保存</span>
        </label>
        <button class="wh-bottom-save-btn" @click="manualSave">保存</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useWarehouse } from '@/composables/useWarehouse'

const router = useRouter()
const {
  showToast,
  currentCode,
  currentCompanyName,
  sampleData,
  autoSaveEnabled,
  pendingSample
} = useWarehouse()

const sampleInput = ref('')
const fillBoxCount = ref(false)

const bottomTotal = computed(() => {
  if (!sampleData.value.length) return '******'
  return sampleData.value.reduce((sum, item) => sum + Number(item.boxCount || 0), 0)
})

function createSampleRecord(sampleId) {
  return {
    image: '/api/placeholder/80/80',
    number: sampleId || 'YX1802716',
    itemNo: '623',
    priceOne: '10.2',
    priceTwo: '11.2',
    packingQty: '2/48',
    productName: '实色台球',
    packageWay: '吸板',
    volume: '0.15/5.53',
    cartonWeight: '20/18',
    sampleWeight: '2/1',
    cartonSize: '66_33_*72',
    packageSize: '66_33_*72',
    sampleSize: '66_33_*72',
    boothNo: '555022',
    productCert: 'EN71,62115',
    factoryCert: 'EN71,62115,10P,GCC',
    sampleDate: '2026-06-26',
    boxCount: '2',
    chineseRemark: '是的是第四',
    otherRemark: '今晚下一条柜',
    saved: false
  }
}

function searchSample() {
  const sampleId = sampleInput.value.trim()
  if (!sampleId) {
    alert('请输入样品编号')
    return
  }

  const record = createSampleRecord(sampleId)
  sampleData.value.unshift(record)

  if (autoSaveEnabled.value) {
    record.saved = true
    pendingSample.value = null
    saveRecord(record.number)
    showToast('查询成功，已自动保存')
  } else {
    pendingSample.value = record
    showToast('查询成功，请点击保存')
  }

  sampleInput.value = ''
}

function manualSave() {
  if (!pendingSample.value) {
    showToast('暂无待保存数据')
    return
  }

  pendingSample.value.saved = true
  saveRecord(pendingSample.value.number)
  showToast('保存成功')
  pendingSample.value = null
}

function onToggleSaveMode() {
  if (autoSaveEnabled.value) pendingSample.value = null
}

function saveRecord(sampleId) {
  console.log('保存记录:', sampleId)
}

function goToCodePage() {
  router.push('/warehouse/code')
}

function goToOtherFunction() {
  alert('查询功能待确认，后续接入其他功能页面')
}
</script>
