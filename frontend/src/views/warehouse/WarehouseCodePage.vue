<template>
  <div class="warehouse-page wh-code-page">

    <div class="wh-search-section">
      <div class="wh-code-query-row">
        <label class="wh-search-label">本次代号:</label>
        <input
          v-model="codeInput"
          class="wh-search-input"
          placeholder="请输入本次代号"
          @keydown.enter="searchCode"
        >
      </div>
      <button class="wh-btn-search" @click="searchCode">查询</button>
    </div>

    <div v-if="resultVisible" class="wh-result-card">
      <div class="wh-quote-grid">
        <div>
          <label class="wh-quote-label">货币名称:</label>
          <input v-model="quote.currencyName" class="wh-readonly-input" readonly>
        </div>
        <div>
          <label class="wh-quote-label">货币缩写:</label>
          <input v-model="quote.currencyCode" class="wh-readonly-input" readonly>
        </div>
        <div>
          <label class="wh-quote-label">货币符号:</label>
          <input v-model="quote.currencySymbol" class="wh-readonly-input" readonly>
        </div>
        <div>
          <label class="wh-quote-label">货币汇率:</label>
          <input v-model="quote.currencyRate" class="wh-readonly-input" readonly>
        </div>
        <div>
          <label class="wh-quote-label">总费用:</label>
          <input v-model="quote.totalCost" class="wh-readonly-input" readonly>
        </div>
        <div>
          <label class="wh-quote-label">货柜类型:</label>
          <input v-model="quote.containerType" class="wh-readonly-input" readonly>
        </div>
        <div>
          <label class="wh-quote-label">报价单位:</label>
          <input v-model="quote.quoteUnit" class="wh-readonly-input" readonly>
        </div>
        <div>
          <label class="wh-quote-label">报价利润:</label>
          <input v-model="quote.quoteProfit" class="wh-readonly-input" readonly>
        </div>
        <div>
          <label class="wh-quote-label">报价加价:</label>
          <input v-model="quote.quoteMarkup" class="wh-readonly-input" readonly>
        </div>
        <div>
          <label class="wh-quote-label">小数位数:</label>
          <input v-model="quote.decimalPlaces" class="wh-readonly-input" readonly>
        </div>
      </div>

      <div class="wh-rounding-section">
        <div class="wh-rounding-title">取舍方式</div>
        <div class="wh-rounding-options">
          <label class="wh-option-checkbox">
            <input type="checkbox" :checked="rounding.halfUp" disabled><span>四舍五入</span>
          </label>
          <label class="wh-option-checkbox">
            <input type="checkbox" :checked="rounding.up" disabled><span>全收</span>
          </label>
          <label class="wh-option-checkbox">
            <input type="checkbox" :checked="rounding.down" disabled><span>全舍</span>
          </label>
        </div>
      </div>

      <div class="wh-formula-section">
        <div class="wh-formula-title">报价公式</div>
        <input v-model="quote.quoteFormula" class="wh-readonly-input" readonly>
      </div>

      <button class="wh-btn-confirm" @click="confirmCode">确认加入</button>
    </div>

    <button class="wh-back-btn" @click="goToMenu">返回</button>

    <!-- 确认弹窗 -->
    <div class="wh-modal-mask" :class="{ show: confirmVisible }">
      <div class="wh-modal-card">
        <div class="wh-modal-title">切换业务代号</div>
        <div class="wh-modal-message">您确认要切换为“{{ currentCompanyName }}”业务代号吗？</div>
        <div class="wh-modal-actions">
          <button class="wh-modal-cancel" @click="confirmVisible = false">取消</button>
          <button class="wh-modal-confirm" @click="confirmSwitchBusiness">我确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useWarehouse } from '@/composables/useWarehouse'

const router = useRouter()
const { showToast, currentCode, currentCompanyName } = useWarehouse()

const codeInput = ref('')
const resultVisible = ref(false)
const confirmVisible = ref(false)

const quote = reactive({
  currencyName: '',
  currencyCode: '',
  currencySymbol: '',
  currencyRate: '',
  totalCost: '',
  containerType: '',
  quoteUnit: '',
  quoteProfit: '',
  quoteMarkup: '',
  decimalPlaces: '',
  quoteFormula: ''
})

const rounding = reactive({ halfUp: false, up: false, down: false })

function searchCode() {
  const code = codeInput.value.trim()
  if (!code) {
    alert('请输入本次代号')
    return
  }

  currentCode.value = code
  currentCompanyName.value = '示例公司名称'

  quote.currencyName = '美元'
  quote.currencyCode = 'USD'
  quote.currencySymbol = '$'
  quote.currencyRate = '7.2000'
  quote.totalCost = '12800.00'
  quote.containerType = '40HQ'
  quote.quoteUnit = 'PCS'
  quote.quoteProfit = '12%'
  quote.quoteMarkup = '0.35'
  quote.decimalPlaces = '2'
  quote.quoteFormula = '报价 = (总费用 ÷ 数量 + 报价加价) × (1 + 报价利润)'

  rounding.halfUp = true
  rounding.up = false
  rounding.down = false

  resultVisible.value = true
}

function confirmCode() {
  if (!currentCompanyName.value) {
    alert('请先查询本次代号')
    return
  }
  confirmVisible.value = true
}

function confirmSwitchBusiness() {
  confirmVisible.value = false
  router.push('/warehouse/sample')
  showToast('已切换业务代号')
}

function goToMenu() {
  router.push('/warehouse/menu')
}
</script>
