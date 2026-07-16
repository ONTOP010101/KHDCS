<template>
  <div class="csd-page sms-page">
    <!-- 顶部信息栏 -->
    <div class="csd-info-bar">
      <div class="csd-info-item">
        <span class="csd-info-label">厂商列表</span>
        <span class="csd-info-value code">
          合计厂商：<strong>{{ totalManufacturerCount }}</strong> 个，当前已选中：<strong>{{ manufacturerList.length }}</strong> 个厂商
        </span>
      </div>
      <span class="csd-info-sep"></span>
      <div class="csd-info-item">
        <span class="csd-info-label">客户名称</span>
        <span class="csd-info-value">{{ record?.clientName || '-' }}</span>
      </div>
      <span class="csd-info-sep"></span>
      <div class="csd-info-item">
        <span class="csd-info-label">本次代号</span>
        <span class="csd-info-value code">{{ record?.codeName || codeName || '-' }}</span>
      </div>
    </div>

    <!-- 主内容区：左表格 + 右面板 -->
    <div class="sms-content">
      <!-- 左侧厂商列表 -->
      <div class="sms-left">
        <div class="sms-left-title">厂商列表</div>
        <vxe-grid
          id="sms-manufacturer-grid"
          ref="gridRef"
          :columns="tableColumns"
          :data="manufacturerList"
          :height="tableHeight"
          :row-config="{ isHover: true, isCurrent: true, keyField: 'itemId' }"
          :header-cell-config="{ height: 80 }"
          :header-cell-style="{ background: '#ffffff', color: '#1d1d1f', fontSize: '24px', fontWeight: 600, textAlign: 'center' }"
          :cell-style="{ textAlign: 'center', fontSize: '22px' }"
          :border="true"
          :column-auto-width="true"
          :scroll-y="{ enabled: true, gt: 0 }"
        />
      </div>

      <!-- 右侧预览面板 -->
      <div class="sms-right">
        <div class="sms-panel">
          <div class="sms-panel-title">预览信息内容</div>
          <div class="sms-preview-box">
            <div class="sms-preview-content">
              <template v-if="selectedCount === 0">
                <span class="sms-placeholder">请选择厂商查看预览</span>
              </template>
              <template v-else>
                <div class="sms-preview-item">
                  <span class="sms-preview-text">{{ previewText }}</span>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部模板区：短信 + 微信 + 企业微信 左右布局 -->
    <div class="sms-bottom-row">
      <div class="sms-bottom third" v-show="notifyType === 'all' || notifyType === 'sms'">
        <div class="sms-left-title sms-template-title">
          <span>短信模板</span>
          <div class="sms-template-actions">
            <input class="sms-filter-input" placeholder="搜索模板" />
            <button class="csd-btn csd-btn-primary sms-add-btn" @click="showAddModal('sms')">新增模板</button>
          </div>
        </div>
        <div class="sms-bottom-table-wrap">
          <vxe-grid
            id="sms-template-sms-grid"
            :columns="templateColumns"
            :data="smsTemplates"
            :height="700"
            :cell-config="{ height: 60 }"
            :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
            :header-cell-style="{ background: '#ffffff', color: '#1d1d1f', fontSize: '24px', fontWeight: 600, textAlign: 'center' }"
            :cell-style="{ textAlign: 'center', fontSize: '22px' }"
            :border="true"
            :column-config="{ resizable: true, drag: true, isMaximized: true }"
            :custom-config="{ storage: true }"
            :sort-config="{ remote: false, trigger: 'cell' }"
            @column-dragstart="onColDragStart"
            @column-dragend="onColDragEnd"
            @custom="onCustom"
            @cell-click="onTemplateRowClick"
            empty-text="暂无数据"
          >
            <template #template_actions="{ row }">
              <button v-if="row.type === 'sms'" class="csd-btn csd-btn-ghost" style="height:36px;padding:0 12px;font-size:18px;margin-right:8px" @click="handleSyncStatus(row.id)">同步</button>
              <button class="csd-btn csd-btn-ghost" style="height:36px;padding:0 12px;font-size:18px;margin-right:8px" @click="showEditModal(row)">编辑</button>
              <button class="csd-btn csd-btn-ghost" style="height:36px;padding:0 12px;font-size:18px;margin-right:8px" @click="handleDeleteTemplate(row.id)">删除</button>
            </template>
            <template #template_status="{ row }">
              <span v-if="row.type !== 'sms'">-</span>
              <span v-else-if="row.status === 1" style="color:#22c55e">审核通过</span>
              <span v-else-if="row.status === 2" style="color:#ef4444" :title="row.refuseReason || ''">审核驳回</span>
              <span v-else style="color:#f59e0b">待审核</span>
            </template>
          </vxe-grid>
        </div>
      </div>

      <div class="sms-bottom third" v-show="notifyType === 'all' || notifyType === 'wechat'">
        <div class="sms-left-title sms-template-title">
          <span>微信模板</span>
          <div class="sms-template-actions">
            <input class="sms-filter-input" placeholder="搜索模板" />
            <button class="csd-btn csd-btn-primary sms-add-btn" @click="showAddModal('wechat')">新增模板</button>
          </div>
        </div>
        <div class="sms-bottom-table-wrap">
          <vxe-grid
            id="sms-template-wechat-grid"
            :columns="templateColumns"
            :data="wechatTemplates"
            :height="700"
            :cell-config="{ height: 60 }"
            :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
            :header-cell-style="{ background: '#ffffff', color: '#1d1d1f', fontSize: '24px', fontWeight: 600, textAlign: 'center' }"
            :cell-style="{ textAlign: 'center', fontSize: '22px' }"
            :border="true"
            :column-config="{ resizable: true, drag: true, isMaximized: true }"
            :custom-config="{ storage: true }"
            :sort-config="{ remote: false, trigger: 'cell' }"
            @column-dragstart="onColDragStart"
            @column-dragend="onColDragEnd"
            @custom="onCustom"
            @cell-click="onTemplateRowClick"
            empty-text="暂无数据"
          >
            <template #template_actions="{ row }">
              <button v-if="row.type === 'sms'" class="csd-btn csd-btn-ghost" style="height:36px;padding:0 12px;font-size:18px;margin-right:8px" @click="handleSyncStatus(row.id)">同步</button>
              <button class="csd-btn csd-btn-ghost" style="height:36px;padding:0 12px;font-size:18px;margin-right:8px" @click="showEditModal(row)">编辑</button>
              <button class="csd-btn csd-btn-ghost" style="height:36px;padding:0 12px;font-size:18px;margin-right:8px" @click="handleDeleteTemplate(row.id)">删除</button>
            </template>
            <template #template_status="{ row }">
              <span v-if="row.type !== 'sms'">-</span>
              <span v-else-if="row.status === 1" style="color:#22c55e">审核通过</span>
              <span v-else-if="row.status === 2" style="color:#ef4444" :title="row.refuseReason || ''">审核驳回</span>
              <span v-else style="color:#f59e0b">待审核</span>
            </template>
          </vxe-grid>
        </div>
      </div>

      <div class="sms-bottom third" v-show="notifyType === 'all' || notifyType === 'wework'">
        <div class="sms-left-title sms-template-title">
          <span>企业微信模板</span>
          <div class="sms-template-actions">
            <input class="sms-filter-input" placeholder="搜索模板" />
            <button class="csd-btn csd-btn-primary sms-add-btn" @click="showAddModal('wework')">新增模板</button>
          </div>
        </div>
        <div class="sms-bottom-table-wrap">
          <vxe-grid
            id="sms-template-wework-grid"
            :columns="templateColumns"
            :data="weworkTemplates"
            :height="700"
            :cell-config="{ height: 60 }"
            :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
            :header-cell-style="{ background: '#ffffff', color: '#1d1d1f', fontSize: '24px', fontWeight: 600, textAlign: 'center' }"
            :cell-style="{ textAlign: 'center', fontSize: '22px' }"
            :border="true"
            :column-config="{ resizable: true, drag: true, isMaximized: true }"
            :custom-config="{ storage: true }"
            :sort-config="{ remote: false, trigger: 'cell' }"
            @column-dragstart="onColDragStart"
            @column-dragend="onColDragEnd"
            @custom="onCustom"
            @cell-click="onTemplateRowClick"
            empty-text="暂无数据"
          >
            <template #template_actions="{ row }">
              <button v-if="row.type === 'sms'" class="csd-btn csd-btn-ghost" style="height:36px;padding:0 12px;font-size:18px;margin-right:8px" @click="handleSyncStatus(row.id)">同步</button>
              <button class="csd-btn csd-btn-ghost" style="height:36px;padding:0 12px;font-size:18px;margin-right:8px" @click="showEditModal(row)">编辑</button>
              <button class="csd-btn csd-btn-ghost" style="height:36px;padding:0 12px;font-size:18px;margin-right:8px" @click="handleDeleteTemplate(row.id)">删除</button>
            </template>
            <template #template_status="{ row }">
              <span v-if="row.type !== 'sms'">-</span>
              <span v-else-if="row.status === 1" style="color:#22c55e">审核通过</span>
              <span v-else-if="row.status === 2" style="color:#ef4444" :title="row.refuseReason || ''">审核驳回</span>
              <span v-else style="color:#f59e0b">待审核</span>
            </template>
          </vxe-grid>
        </div>
      </div>
    </div>

    <!-- 底部工具栏 -->
    <div class="sms-bottom-toolbar-wrap">
      <div class="sms-bottom-toolbar">
        <div class="sms-toolbar-left">
          <div class="sms-toolbar-row">
            <span class="sms-toolbar-label">通知类型：</span>
            <label class="sms-radio"><input type="radio" name="notifyType" value="all" v-model="notifyType" /> 全部</label>
            <label class="sms-radio"><input type="radio" name="notifyType" value="sms" v-model="notifyType" /> 短信</label>
            <label class="sms-radio"><input type="radio" name="notifyType" value="wechat" v-model="notifyType" /> 微信</label>
            <label class="sms-radio"><input type="radio" name="notifyType" value="wework" v-model="notifyType" /> 企业微信</label>
          </div>
          <div class="sms-toolbar-row">
            <span class="sms-toolbar-label">发送类型：</span>
            <label class="sms-radio"><input type="radio" name="sendType" value="all" checked /> 全部</label>
            <label class="sms-radio"><input type="radio" name="sendType" value="exhibition" /> 展厅已补</label>
            <label class="sms-radio"><input type="radio" name="sendType" value="borrow" /> 借样</label>
            <label class="sms-radio"><input type="radio" name="sendType" value="other" /> 其他</label>
          </div>
        </div>
        <div class="sms-toolbar-right">
          <button class="csd-btn csd-btn-ghost toolbar-btn" @click="showSendRecords">发送记录</button>
          <button class="csd-btn csd-btn-primary toolbar-btn" @click="onConfirmSend" :disabled="sending">{{ sending ? '发送中...' : '确认发送' }}</button>
        </div>
      </div>
    </div>

    <!-- 新增模板模态框 -->
    <div class="sms-modal-overlay" v-if="modalVisible">
      <div class="sms-modal">
        <div class="sms-modal-header">
          <span>{{ modalTitle }}</span>
          <button class="sms-modal-close" @click="closeModal">&times;</button>
        </div>
        <div class="sms-modal-body">
          <!-- 短信模板表单 -->
          <template v-if="modalType === 'sms'">
            <div class="sms-form-item">
              <label class="sms-form-label required">模板名称</label>
              <input class="sms-form-input" v-model="form.templateName" placeholder="请输入模板名称" />
            </div>
            <div class="sms-form-item">
              <label class="sms-form-label required">短信类型</label>
              <div class="sms-form-radio-group">
                <label class="sms-radio"><input type="radio" v-model="form.smsType" value="通知" /> 通知</label>
                <label class="sms-radio">
                  <input type="radio" v-model="form.smsType" value="营销" />
                  营销 <span class="sms-marketing-tip">（内容包含手机,电话,卡号,引导类属于营销）</span>
                </label>
              </div>
            </div>
            <div class="sms-form-item">
              <label class="sms-form-label required">前缀签名</label>
              <input class="sms-form-input" value="【悦之翔安拓】" readonly style="background:#f5f5f5;color:#888" />
            </div>
            <div class="sms-form-item">
              <label class="sms-form-label required">用途类型</label>
              <input class="sms-form-input" value="B06-其他业务管理服务类" readonly style="background:#f5f5f5;color:#888" />
            </div>
            <div class="sms-form-item">
              <label class="sms-form-label required">变量类型</label>
              <select v-model="form.variableType" class="sms-form-input">
                <option value="number_letter">数字+字母</option>
                <option value="letter">仅字母</option>
              </select>
            </div>
            <div class="sms-form-item">
              <label class="sms-form-label required">模板ID</label>
              <input class="sms-form-input" v-model="form.templateId" placeholder="请输入模板ID" />
            </div>
            <div class="sms-form-item">
              <label class="sms-form-label required">模板内容</label>
              <textarea class="sms-form-textarea" v-model="form.templateContent" placeholder="请输入模板内容" rows="5" ref="templateContentRef"></textarea>
            </div>
          </template>

          <!-- 微信/企业微信模板表单 -->
          <template v-else>
            <div class="sms-form-item">
              <label class="sms-form-label required">模板名称</label>
              <input class="sms-form-input" v-model="form.templateName" placeholder="请输入模板名称" />
            </div>
            <div class="sms-form-item">
              <label class="sms-form-label required">模板类型</label>
              <div class="sms-form-radio-group">
                  <select class="sms-form-select" v-model="form.templateType">
                    <option value="exhibition">展厅已补</option>
                    <option value="borrow">借样</option>
                    <option value="other">其他</option>
                  </select>
                </div>
            </div>
            <div class="sms-form-item">
              <label class="sms-form-label required">模板内容</label>
              <textarea class="sms-form-textarea" v-model="form.templateContent" placeholder="请输入模板内容" rows="5" ref="templateContentRef"></textarea>
            </div>
          </template>

          <!-- 添加变量 -->
          <div class="sms-form-item">
            <label class="sms-form-label">添加变量</label>
            <div class="sms-var-btns">
              <button class="sms-var-btn" @click="addVar('{%厂商名称%}')">{%厂商名称%}</button>
              <button class="sms-var-btn" @click="addVar('{%出厂货号%}')">{%出厂货号%}</button>
              <button class="sms-var-btn" @click="addVar('{%摊位号%}')">{%摊位号%}</button>
              <button class="sms-var-btn" @click="addVar('{%洽谈室号%}')">{%洽谈室号%}</button>
              <button class="sms-var-btn" @click="addVar('{%日期%}')">{%日期%}</button>
            </div>
          </div>
        </div>
        <div class="sms-modal-footer">
          <button class="csd-btn csd-btn-ghost" @click="closeModal">取消</button>
          <button class="csd-btn csd-btn-primary" @click="saveTemplate">保存</button>
        </div>
      </div>
    </div>

    <!-- 发送记录模态框 -->
    <div class="sms-modal-overlay" v-if="sendRecordsVisible">
      <div class="sms-modal sms-send-modal">
        <div class="sms-modal-header">
          <span>发送记录</span>
          <button class="sms-modal-close" @click="sendRecordsVisible = false">&times;</button>
        </div>
        <div class="sms-modal-body">
          <div class="sr-toolbar">
            <div class="sr-toolbar-row">
              <input class="sms-form-input sr-search" v-model="srSearch" placeholder="搜索" />
              <span class="sr-filter-label">通知类型</span>
              <select class="sms-form-select sr-select" v-model="srNotifyType">
                <option value="">全部</option>
                <option value="sms">短信</option>
                <option value="wechat">微信</option>
                <option value="wework">企业微信</option>
              </select>
              <SimpleDatePicker class="sr-date-picker" v-model="srStartDate" placeholder="开始日期" @change="onSrDateFilter" />
              <span class="sr-date-sep">至</span>
              <SimpleDatePicker class="sr-date-picker" v-model="srEndDate" placeholder="结束日期" @change="onSrDateFilter" />
              <span class="sr-filter-label">发送状态</span>
              <select class="sms-form-select sr-select" v-model="srSendStatus">
                <option value="">全部状态</option>
                <option value="success">发送成功</option>
                <option value="fail">发送失败</option>
                <option value="pending">待发送</option>
              </select>
            </div>
            <div class="sr-toolbar-row">
              <button class="sr-btn sr-btn-resend" @click="resendSelected">重新发送</button>
              <button class="sr-btn" @click="refreshRecords">刷新</button>
            </div>
          </div>
          <div class="sr-table-wrap">
            <vxe-grid
              id="sms-send-records-grid"
              ref="srGridRef"
              :columns="srColumns"
              :data="srData"
              :height="750"
              :cell-config="{ height: 80 }"
              :header-cell-config="{ height: 80 }"
              :header-cell-style="{ background: '#ffffff', color: '#1d1d1f', fontSize: '24px', fontWeight: 600, textAlign: 'center' }"
              :cell-style="{ textAlign: 'center', fontSize: '22px' }"
              :border="true"
              :sort-config="{ trigger: 'cell' }"
              :scroll-x="{ enabled: true, gt: 0 }"
              :column-config="{ resizable: true, drag: true, isMaximized: true }"
              :custom-config="{ storage: true }"
              @column-dragstart="onSrColumnDragStart"
              @column-dragend="onSrColumnDragEnd"
              @custom="onSrCustom"
              empty-text="暂无数据"
            >
              <template #sr_send_status="{ row }">
                <span v-if="row.sendStatus === 'success'" style="color:#22c55e;font-weight:700">发送成功</span>
                <span v-else-if="row.sendStatus === 'fail'" style="color:#ef4444;font-weight:700">发送失败</span>
                <span v-else style="color:#f59e0b">待发送</span>
              </template>
            </vxe-grid>
          </div>
        </div>
        <div class="sr-footer">
          <div class="sr-footer-left">
            <button class="sr-btn sr-btn-blue" @click="selectAll">全选</button>
            <button class="sr-btn sr-btn-blue" @click="invertSelect">反选</button>
            <button class="sr-btn sr-btn-blue" @click="clearSelect">清除</button>
          </div>
          <div class="sr-footer-center">
            <span>共计 {{ srTotal }} 条，已选择 {{ srSelectedCount }} 条</span>
          </div>
          <div class="sr-footer-right">
            <span class="sr-page-size-label">每页</span>
            <select class="sr-page-size-select" v-model="srPageSize" @change="onPageSizeChange">
              <option :value="500">500 条</option>
              <option :value="1000">1000 条</option>
              <option :value="2000">2000 条</option>
              <option :value="5000">5000 条</option>
            </select>
            <button class="sr-btn" @click="prevPage" :disabled="srPage <= 1">上一页</button>
            <span class="sr-page-info">{{ srPage }} / {{ srTotalPages }}</span>
            <button class="sr-btn" @click="nextPage" :disabled="srPage >= srTotalPages">下一页</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onActivated, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import VXETable from 'vxe-table'
import { getSmsTemplates, createSmsTemplate, updateSmsTemplate, deleteSmsTemplate, syncSmsTemplateStatus, sendSms, sendPersonalSms, sendWework, getSmsRecords, syncSmsStatus, getSmsSigns, getSmsManufacturerCount } from '@/api'
import { useToast } from '@/composables/useToast'
import SimpleDatePicker from '@/components/SimpleDatePicker.vue'
import '@/styles/client-sample-detail.css'
import '@/styles/sample.css'

VXETable.setup({ table: { headerCellConfig: { height: 80 } } })

const route = useRoute()
const codeName = computed(() => route.params.codeName || '')

const templateContentRef = ref(null)
const { showToast } = useToast()

const gridRef = ref(null)
const tableHeight = ref(600)
const record = ref(null)
const manufacturerList = ref([])
const totalManufacturerCount = ref(0)
const notifyType = ref('wechat')
const sending = ref(false)

// 模板数据
const smsTemplates = ref([])
const wechatTemplates = ref([])
const weworkTemplates = ref([])
const loadingTemplates = ref(false)
const signList = ref([])
const defaultSignId = ref(null)  // 页面初始化时从联麓获取签名ID

// 当前选中的模板行
const selectedTemplate = ref(null)

// 模态框相关
const modalVisible = ref(false)
const modalType = ref('sms')
const editingId = ref(null)
const modalTitle = computed(() => {
  const prefix = editingId.value ? '编辑' : '新增'
  if (modalType.value === 'sms') return prefix + '短信模板'
  if (modalType.value === 'wechat') return prefix + '微信模板'
  return prefix + '企业微信模板'
})

const form = ref({
  templateName: '',
  smsType: '通知',
  templateType: '',
  templateId: '',
  templateContent: '',
  applyPurpose: 'B06-其他业务管理服务类',
  variableType: 'number_letter'
})

// 加载模板
const loadTemplates = async () => {
  loadingTemplates.value = true
  try {
    const res = await getSmsTemplates()
    const all = res.data || []
    smsTemplates.value = all.filter(t => t.type === 'sms')
    wechatTemplates.value = all.filter(t => t.type === 'wechat')
    weworkTemplates.value = all.filter(t => t.type === 'wework')
  } catch (e) {
    console.error('加载模板失败:', e)
  } finally {
    loadingTemplates.value = false
  }
}

const showAddModal = (type) => {
  editingId.value = null
  modalType.value = type
  form.value = {
    templateName: '',
    smsType: '通知',
    templateType: 'exhibition',
    templateId: '',
    templateContent: '',
    applyPurpose: 'B06-其他业务管理服务类',
    variableType: 'number_letter'
  }
  if (type === 'sms') loadSigns()
  modalVisible.value = true
}

const loadSigns = async (smsType) => {
  try {
    const type = smsType || form.value.smsType || '通知'
    const res = await getSmsSigns(type)
    if (res.code === 200 && res.data) {
      const list = res.data.data || res.data.list || []
      signList.value = Array.isArray(list) ? list : []
      // 自动匹配默认签名 【悦之翔安拓】
      const match = signList.value.find(s => {
        const c = (s.content || s.sign || '').replace(/【|】/g, '')
        return c.includes('悦之翔安拓')
      })
      if (match) {
        defaultSignId.value = match.SignId
      }
    }
  } catch (e) {
    console.error('加载签名列表失败:', e)
  }
}

// 监听短信类型切换，重新加载对应类型的签名列表
watch(() => form.value.smsType, (newType) => {
  if (modalVisible.value && modalType.value === 'sms' && newType) {
    loadSigns(newType)
  }
})

const showEditModal = (row) => {
  editingId.value = row.id
  modalType.value = row.type
  form.value = {
    templateName: row.templateName || '',
    smsType: row.smsType || '通知',
    templateType: 'exhibition',
    templateId: row.lianluTemplateId ? String(row.lianluTemplateId) : '',
    templateContent: row.content || '',
    applyPurpose: row.applyPurpose || 'B06-其他业务管理服务类',
    variableType: row.variableType || 'number_letter'
  }
  if (row.type === 'sms') loadSigns()
  modalVisible.value = true
}

const closeModal = () => {
  modalVisible.value = false
  editingId.value = null
}

const addVar = (varStr) => {
  form.value.templateContent += varStr
}

const saveTemplate = async () => {
  const data = {
    type: modalType.value,
    templateName: form.value.templateName,
    content: form.value.templateContent
  }
  if (modalType.value === 'sms') {
    data.smsType = form.value.smsType
    data.signId = defaultSignId.value ? Number(defaultSignId.value) : null
    data.applyPurpose = form.value.applyPurpose || 'B06-其他业务管理服务类'
    data.variableType = form.value.variableType || 'number_letter'
  }
  try {
    if (editingId.value) {
      await updateSmsTemplate(editingId.value, data)
      showToast('模板更新成功', 'success')
    } else {
      await createSmsTemplate(data)
      showToast('模板创建成功', 'success')
    }
    closeModal()
    loadTemplates()
  } catch (e) {
    showToast((editingId.value ? '更新' : '创建') + '失败: ' + e.message, 'error')
  }
}

// 删除模板
const handleDeleteTemplate = async (id) => {
  if (!confirm('确认删除此模板？')) return
  try {
    await deleteSmsTemplate(id)
    loadTemplates()
  } catch (e) {
    console.error('删除失败:', e)
  }
}

// 同步审核状态
const handleSyncStatus = async (id) => {
  try {
    await syncSmsTemplateStatus(id)
    showToast('状态已同步', 'success')
    loadTemplates()
  } catch (e) {
    showToast('同步失败: ' + e.message, 'error')
  }
}

// 根据 notifyType 获取当前显示的模板
const currentTemplates = computed(() => {
  if (notifyType.value === 'sms') return smsTemplates.value
  if (notifyType.value === 'wechat') return wechatTemplates.value
  if (notifyType.value === 'wework') return weworkTemplates.value
  return [] // 'all' 时三个 tab 各自显示
})

// === 发送记录 ===
const sendRecordsVisible = ref(false)
const srGridRef = ref(null)
const srSearch = ref('')
const srNotifyType = ref('')
const srStartDate = ref('')
const srEndDate = ref('')
const srSendStatus = ref('')
const srPage = ref(1)
const srPageSize = ref(500)
const srTotal = ref(0)
const srData = ref([])

const srColumns = [
  { type: 'checkbox', width: 50, fixed: 'left', align: 'center' },
  { type: 'seq', title: '序号', width: 70, fixed: 'left', align: 'center', sortable: true },
  { field: 'lianluTemplateId', title: '模板序号', width: 120, align: 'center', sortable: true },
  { field: 'type', title: '类型', width: 100, align: 'center', sortable: true,
    formatter: ({ cellValue }) => {
      const map = { sms: '短信', wechat: '微信', wework: '企业微信' }
      return map[cellValue] || cellValue
    }
  },
  { field: 'templateName', title: '模板名称', width: 160, align: 'center', sortable: true },
  { field: 'manufacturerName', title: '厂商名称', width: 180, align: 'center', sortable: true },
  { field: 'content', title: '模板内容', width: 200, align: 'center', sortable: true },
  { field: 'boothNo', title: '摊位号', width: 120, align: 'center', sortable: true },
  { field: 'phone', title: '手机号', width: 150, align: 'center', sortable: true },
  { field: 'codeName', title: '本次代号', width: 130, align: 'center', sortable: true },
  { field: 'sendTime', title: '发送时间', width: 180, align: 'center', sortable: true },
  { field: 'sendStatus', title: '发送状态', width: 120, fixed: 'right', align: 'center', sortable: true,
    slots: { default: 'sr_send_status' }
  },
  { field: 'readStatus', title: '是否阅读', width: 120, visible: false, align: 'center', sortable: true,
    formatter: ({ cellValue }) => cellValue === 1 ? '已读' : '未读'
  }
]

const srTotalPages = computed(() => Math.max(1, Math.ceil(srTotal.value / srPageSize.value)))
const srSelectedCount = computed(() => srData.value.filter(r => r.checkbox).length)

const showSendRecords = () => {
  sendRecordsVisible.value = true
  refreshRecords()
}

const onSrDateFilter = () => { srPage.value = 1; refreshRecords() }

const refreshRecords = async () => {
  try {
    // 先同步发送状态（从联麓拉取报告）
    await syncSmsStatus(codeName.value).catch(() => {})
    const params = {
      codeName: codeName.value,
      pageNo: srPage.value,
      pageSize: srPageSize.value,
      type: srNotifyType.value || undefined,
      sendStatus: srSendStatus.value || undefined,
      search: srSearch.value || undefined
    }
    if (srStartDate.value && srEndDate.value) {
      params.startDate = srStartDate.value
      params.endDate = srEndDate.value
    } else {
      if (srStartDate.value) params.startDate = srStartDate.value
      if (srEndDate.value) params.endDate = srEndDate.value
    }
    const res = await getSmsRecords(params)
    if (res.code === 200 && res.data) {
      srData.value = res.data.records || []
      srTotal.value = res.data.total || 0
    } else {
      srData.value = []
      srTotal.value = 0
    }
  } catch (e) {
    console.error('加载发送记录失败:', e)
    srData.value = []
    srTotal.value = 0
  }
}

const selectAll = () => {
  srData.value.forEach(r => { r.checkbox = true })
  srData.value = [...srData.value]
}

const invertSelect = () => {
  srData.value.forEach(r => { r.checkbox = !r.checkbox })
  srData.value = [...srData.value]
}

const clearSelect = () => {
  srData.value.forEach(r => { r.checkbox = false })
  srData.value = [...srData.value]
}

const resendSelected = () => {
  const selected = srData.value.filter(r => r.checkbox)
  console.log('重新发送:', selected)
}

const onSrColumnDragStart = (params) => {
  console.log('column drag start:', params)
}

const onSrColumnDragEnd = (params) => {
  console.log('column drag end:', params)
}

const onSrCustom = (params) => {
  console.log('列管理变更:', params)
}

const prevPage = () => {
  if (srPage.value > 1) {
    srPage.value--
    refreshRecords()
  }
}

const onPageSizeChange = () => {
  srPage.value = 1
  refreshRecords()
}

const nextPage = () => {
  if (srPage.value < srTotalPages.value) {
    srPage.value++
    refreshRecords()
  }
}

// 模板表格列拖拽/排序
const onColDragStart = (params) => {
  console.log('column drag start:', params)
}

const onColDragEnd = (params) => {
  console.log('column drag end:', params)
}

const onCustom = (params) => {
  console.log('列管理变更:', params)
}

// 点击模板行
const onTemplateRowClick = ({ row }) => {
  selectedTemplate.value = row
}

const tableColumns = [
  { type: 'seq', title: '序号', width: 70, fixed: 'left', align: 'center' },
  { field: 'manufacturerCode', title: '厂商编号', width: 200, align: 'center' },
  { field: 'manufacturerName', title: '厂商名称', width: 500, align: 'center' },
  { field: 'mobile1', title: '短信号码', align: 'center' }
]

const templateColumns = [
  { field: 'id', title: 'ID', width: 100, align: 'center', sortable: true },
  { field: 'templateName', title: '模板名称', align: 'center', sortable: true },
  { field: 'smsType', title: '类型', width: 100, align: 'center', sortable: true,
    formatter: ({ row }) => {
      if (row.type === 'sms') return row.smsType || '通知'
      if (row.type === 'wechat') return '微信'
      if (row.type === 'wework') return '企业微信'
      return ''
    }
  },
  { field: 'content', title: '模板内容', align: 'center', sortable: true },
  { field: 'status', title: '审核状态', width: 120, align: 'center', sortable: true,
    slots: { default: 'template_status' }
  },
  { title: '操作', field: '_action', width: 280, align: 'center',
    slots: { default: 'template_actions' }
  }
]

// 从 sessionStorage 读取勾选数据
const initData = async () => {
  try {
    const raw = sessionStorage.getItem('sms_checked_rows')
    if (raw) {
      const parsed = JSON.parse(raw)
      const map = new Map()
      parsed.forEach(item => {
        if (!map.has(item.itemId)) {
          map.set(item.itemId, item)
        }
      })
      manufacturerList.value = Array.from(map.values())
      // 保留 sessionStorage 数据以支持刷新页面
      // 默认选中第一行
      if (manufacturerList.value.length > 0) {
        await nextTick()
        gridRef.value?.setCurrentRow(manufacturerList.value[0], true)
      }
    }
    const rawRecord = sessionStorage.getItem('sms_record_info')
    if (rawRecord) {
      record.value = JSON.parse(rawRecord)
    }
    // 从后端获取代号下的厂商总数
    if (codeName.value) {
      const res = await getSmsManufacturerCount(codeName.value)
      if (res.code === 200 && res.data) {
        totalManufacturerCount.value = res.data.total || 0
      }
    }
  } catch (e) {
    console.error('读取短信发送数据失败:', e)
  }
}

// 选中项
// 全部厂商默认选中（无复选框）
const checkedRows = computed(() => manufacturerList.value)
const selectedCount = computed(() => manufacturerList.value.length)

// 当前选中的模板（点击模板行设置）
const activeTemplate = computed(() => selectedTemplate.value)

const todayStr = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
})

const previewText = computed(() => {
  const row = checkedRows.value[0]
  if (!row) return ''
  if (!selectedTemplate.value) return `已选 ${selectedCount.value} 个厂商，请点击模板行查看预览`
  const templateContent = selectedTemplate.value.content || ''
  if (!templateContent) return `已选 ${selectedCount.value} 个厂商，该模板暂无内容`
  return templateContent
    .replace(/{%厂商名称%}/g, row.manufacturerName || '-')
    .replace(/{%出厂货号%}/g, row.factoryCode || '-')
    .replace(/{%摊位号%}/g, row.boothNo || row.boothNumber || '-')
    .replace(/{%洽谈室号%}/g, row.boothNo || row.boothNumber || '-')
    .replace(/{%日期%}/g, todayStr.value)
})

// 确认发送
const extractVars = (content) => {
  const re = /\{%([^%]+)%\}/g
  const vars = []
  let m
  while ((m = re.exec(content)) !== null) {
    vars.push(m[1])
  }
  return vars
}

// 根据变量名为厂商行生成变量值
const fillVarValue = (varName, row) => {
  switch (varName) {
    case '厂商名称': return row.manufacturerName || '-'
    case '出厂货号': return row.factoryCode || '-'
    case '摊位号': return row.boothNo || row.boothNumber || '-'
    case '洽谈室号': return row.boothNo || row.boothNumber || '-'
    case '日期': return todayStr.value
    default: return ''
  }
}

// 确认发送
const onConfirmSend = async () => {
  const checked = checkedRows.value
  if (checked.length === 0) {
    alert('请选择至少一个厂商')
    return
  }
  const phones = checked.map(r => r.mobile1 || r.phone1 || '').filter(Boolean)
  if (phones.length === 0) {
    alert('选中厂商没有可用的短信号码')
    return
  }
  // 获取当前选中的模板
  const tmpl = selectedTemplate.value
  if (!tmpl) {
    alert('请先点击模板行选择要使用的模板')
    return
  }
  if (tmpl.type === 'wechat' || tmpl.type === 'wework') {
    if (tmpl.type === 'wechat') {
      alert('微信发送功能暂未开放')
      return
    }
    // 企业微信发送
    sending.value = true
    try {
      const manufacturers = checked.map(r => ({
        manufacturerCode: r.manufacturerCode,
        manufacturerName: r.manufacturerName,
        boothNo: r.boothNo || r.boothNumber || '',
        factoryCode: r.factoryCode || ''
      }))
      const data = {
        codeName: codeName.value,
        clientName: record.value?.clientName || '',
        templateName: tmpl.templateName,
        templateContent: tmpl.content,
        manufacturers: manufacturers,
        tag: codeName.value
      }
      const res = await sendWework(data)
      if (res.code === 200) {
        const d = res.data
        showToast(`发送完成！成功 ${d.successCount} 条，失败 ${d.failCount} 条`, d.failCount > 0 ? 'error' : 'success')
        if (d.failedNames && d.failedNames.length > 0) {
          showToast('发送失败厂商（未绑定企业微信）: ' + d.failedNames.join(', '), 'error')
        }
      } else {
        showToast('发送失败: ' + (res.message || '未知错误'), 'error')
      }
    } catch (e) {
      console.error('企业微信发送失败:', e)
      showToast('发送失败: ' + (e.message || '网络错误'), 'error')
    } finally {
      sending.value = false
    }
    return
  }
  if (!tmpl.lianluTemplateId) {
    alert('该模板尚未在联麓平台审核通过，无法发送')
    return
  }

  sending.value = true
  try {
    // 从模板内容提取变量名
    const varNames = extractVars(tmpl.content || '')
    // 为每个厂商构建 ContextParamSet: [[手机号, 变量1, 变量2, ...], ...]
    const validRows = checked.filter(r => r.mobile1 || r.phone1)
    const contextList = validRows.map(r => {
      const phone = r.mobile1 || r.phone1
      const varValues = varNames.map(name => fillVarValue(name, r))
      return [phone, ...varValues]
    })

    const data = {
      codeName: codeName.value,
      clientName: record.value?.clientName || '',
      smsType: tmpl.smsType || '通知',
      lianluTemplateId: tmpl.lianluTemplateId,
      templateName: tmpl.templateName,
      templateContent: tmpl.content,
      contextList: contextList,
      manufacturerNames: validRows.map(r => r.manufacturerName || ''),
      manufacturerCodes: validRows.map(r => r.manufacturerCode || ''),
      boothNos: validRows.map(r => r.boothNo || r.boothNumber || ''),
      tag: codeName.value
    }
    const res = await sendPersonalSms(data)
    if (res.code === 200) {
      const taskId = res.data.taskId
      const cnt = res.data.count || contextList.length
      showToast(`发送成功！任务ID: ${taskId}，共 ${cnt} 条`, 'success')
    } else {
      showToast('发送失败: ' + (res.message || '未知错误'), 'error')
    }
  } catch (e) {
    console.error('发送失败:', e)
    showToast('发送失败: ' + (e.message || '网络错误'), 'error')
  } finally {
    sending.value = false
  }
}

onActivated(() => {
  initData()
  loadTemplates()
  loadSigns()
})

onBeforeUnmount(() => {
})
</script>

<style scoped>
.sms-page {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  background: #f5f7fa;
}

/* 顶部信息栏 */
.sms-page :deep(.csd-info-bar) {
  min-height: 60px;
}

.sms-page :deep(.csd-info-item) {
   align-items: baseline;
 }

.sms-page :deep(.csd-info-label) {
  font-size: 24px;
}

.sms-content {
  display: flex;
  padding: 12px 16px 0;
  gap: 16px;
}

.sms-left {
  flex: 0 0 50%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  align-self: flex-start;
}

.sms-left-title {
   font-size: 28px;
   font-weight: 600;
  color: #1d1d1f;
  padding: 14px 20px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

 .sms-right {
  flex: 0 0 50%;
  min-width: 0;
}

.sms-panel {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sms-panel-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  padding: 14px 20px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.sms-preview-box {
  flex: 1;
  padding: 0;
  overflow-y: auto;
  height: 600px;
}

.sms-preview-label {
  font-size: 20px;
  color: #6b7280;
  margin-bottom: 16px;
}

.sms-preview-content {
  min-height: 600px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px dashed #d1d5db;
}

.sms-placeholder {
  color: #9ca3af;
  font-size: 22px;
}

.sms-preview-item {
  font-size: 22px;
  line-height: 1.8;
  color: #1d1d1f;
}

.sms-preview-tag {
  color: #007aff;
  font-weight: 500;
}

.sms-preview-text {
  color: #374151;
}

.sms-bottom {
  padding: 8px 16px 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.sms-bottom-row {
  display: flex;
  padding: 8px 16px 16px;
  gap: 16px;
}

.sms-bottom.half {
  flex: 1;
  min-width: 0;
  padding: 0;
}

.sms-bottom.third {
  flex: 1;
  min-width: 0;
  padding: 0;
}

.sms-bottom-toolbar-wrap {
  padding: 0 16px 16px;
  margin-top: -20px;
}

.sms-bottom-table-wrap {
  flex: 1;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.sms-bottom-toolbar {
  min-height: 80px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-top: 8px;
}

.sms-toolbar-left {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sms-toolbar-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sms-toolbar-label {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
}

.sms-radio {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 28px;
  color: #374151;
  cursor: pointer;
}

.sms-radio input[type="radio"] {
  width: 28px;
  height: 28px;
  min-width: 28px;
  min-height: 28px;
  accent-color: #007aff;
  cursor: pointer;
  margin: 0;
}

.sms-toolbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-btn {
  height: 52px !important;
  padding: 0 36px !important;
  font-size: 22px !important;
  font-weight: 600 !important;
  letter-spacing: 0.02em;
  border-radius: 26px !important;
  transition: all 0.2s ease;
}

.toolbar-btn.csd-btn-ghost {
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  color: #475569;
}

.toolbar-btn.csd-btn-ghost:hover {
  background: #fff;
  border-color: #007aff;
  color: #007aff;
  box-shadow: 0 4px 14px rgba(0, 122, 255, 0.12);
}

.toolbar-btn.csd-btn-primary {
  box-shadow: 0 4px 16px rgba(0, 122, 255, 0.25);
}

.toolbar-btn.csd-btn-primary:hover {
  box-shadow: 0 6px 24px rgba(0, 122, 255, 0.35);
}

.sms-template-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sms-template-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.sms-filter-input {
  width: 260px;
  height: 52px;
  padding: 0 18px 0 44px;
  font-size: 22px;
  border: 1.5px solid #e2e8f0;
  border-radius: 26px;
  outline: none;
  background: #f8fafc url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='20' height='20' viewBox='0 0 24 24' fill='none' stroke='%2394a3b8' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='11' cy='11' r='8'/%3E%3Cline x1='21' y1='21' x2='16.65' y2='16.65'/%3E%3C/svg%3E") 16px center no-repeat;
  transition: all 0.2s ease;
  color: #1e293b;
}

.sms-filter-input::placeholder {
  color: #94a3b8;
  font-size: 20px;
}

.sms-filter-input:hover {
  border-color: #cbd5e1;
  background-color: #fff;
}

.sms-filter-input:focus {
  border-color: #007aff;
  background-color: #fff;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.08);
}

.sms-add-btn {
  height: 48px !important;
  padding: 0 32px !important;
  font-size: 20px !important;
  font-weight: 600 !important;
  letter-spacing: 0.02em;
  white-space: nowrap;
  border-radius: 24px !important;
}

/* 暂无数据字体 */
.sms-bottom :deep(.vxe-table--empty-content),
.sms-left :deep(.vxe-table--empty-content) {
  font-size: 24px;
  color: #9ca3af;
}

/* 表格滚动条 */
:deep(.vxe-table--body-wrapper) {
  overflow-y: auto !important;
}

:deep(.vxe-table--body-wrapper::-webkit-scrollbar) {
  width: 8px;
}

:deep(.vxe-table--body-wrapper::-webkit-scrollbar-track) {
  background: #f1f1f1;
  border-radius: 4px;
}

:deep(.vxe-table--body-wrapper::-webkit-scrollbar-thumb) {
  background: #c1c1c1;
  border-radius: 4px;
}

:deep(.vxe-table--body-wrapper::-webkit-scrollbar-thumb:hover) {
  background: #a0a0a0;
}

/* 厂商列表表格边框补全（全局样式只设了右侧+底部，缺顶部和左侧） */
.sms-left :deep(.vxe-header--column) {
  border: 1px solid #e5e7eb !important;
}
.sms-left :deep(.vxe-body--column) {
  border: 1px solid #e5e7eb !important;
}

.csd-btn-primary {
  padding: 10px 32px;
  font-size: 22px;
}

/* 模态框样式 */
.sms-modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.sms-modal {
  background: #fff;
  border-radius: 12px;
  width: 900px;
  min-height: 600px;
  max-height: 95vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.25);
  overflow: visible !important;
}

.sms-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 26px;
  font-weight: 600;
  color: #1d1d1f;
}

.sms-modal-close {
  background: none;
  border: none;
  font-size: 32px;
  color: #9ca3af;
  cursor: pointer;
  line-height: 1;
  padding: 0;
}

.sms-modal-body {
  padding: 24px 28px;
  overflow: visible;
  flex: 1;
}

.sms-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 28px;
  border-top: 1px solid #e5e7eb;
}

.sms-form-item {
  margin-bottom: 20px;
}

.sms-form-label {
  display: block;
  font-size: 26px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 10px;
}

.sms-form-label.required::before {
  content: '*';
  color: #ef4444;
  margin-right: 4px;
}

.sms-form-input {
  width: 100%;
  height: 56px;
  padding: 0 16px;
  font-size: 26px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  outline: none;
  box-sizing: border-box;
}

.sms-form-input:focus {
  border-color: #007aff;
}

.sms-form-select {
  width: 100%;
  height: 56px;
  padding: 0 16px;
  font-size: 26px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  outline: none;
  background: #fff;
}

.sms-form-textarea {
  width: 100%;
  min-height: 240px;
  padding: 14px 16px;
  font-size: 26px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  outline: none;
  resize: vertical;
  box-sizing: border-box;
  font-family: inherit;
}

.sms-form-textarea:focus {
  border-color: #007aff;
}

.sms-form-radio-group {
  display: flex;
  align-items: center;
  gap: 24px;
}

.sms-marketing-tip {
  color: #ef4444;
  font-size: 22px;
}

.sms-var-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sms-var-btn {
  padding: 10px 24px;
  font-size: 24px;
  border: 1px solid #007aff;
  border-radius: 6px;
  background: #e8f4fd;
  color: #007aff;
  cursor: pointer;
  transition: all 0.2s;
}

.sms-var-btn:hover {
  background: #007aff;
  color: #fff;
}

/* 发送记录模态框 */
.sms-send-modal {
  width: 98vw !important;
  max-width: 2200px;
  max-height: 98vh;
  overflow: visible !important;
  zoom: 1.15;
}

.sr-toolbar {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px 28px;
  margin-bottom: 16px;
  overflow: visible !important;
}

.sr-toolbar-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  overflow: visible !important;
}

.sr-toolbar-row:last-child {
  margin-bottom: 0;
  margin-top: 16px;
}

.sr-filter-label {
  font-size: 18px;
  font-weight: 600;
  color: #475569;
  white-space: nowrap;
  flex-shrink: 0;
  margin-right: 4px;
}

.sr-search {
  flex: 0 0 380px;
  min-width: 200px;
  height: 48px !important;
  padding-left: 48px !important;
  font-size: 20px !important;
  background: #fff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='22' height='22' viewBox='0 0 24 24' fill='none' stroke='%2394a3b8' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='11' cy='11' r='8'/%3E%3Cline x1='21' y1='21' x2='16.65' y2='16.65'/%3E%3C/svg%3E") 14px center no-repeat !important;
  border: 1.5px solid #e2e8f0 !important;
  border-radius: 0 !important;
  transition: all 0.2s ease;
}

.sr-search:hover {
  border-color: #cbd5e1 !important;
}

.sr-search:focus {
  border-color: #007aff !important;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.08) !important;
}

.sr-select {
  width: 210px !important;
  height: 48px !important;
  font-size: 20px !important;
  background: #fff url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%2394a3b8' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'/%3E%3C/svg%3E") right 12px center no-repeat !important;
  border: 1.5px solid #e2e8f0 !important;
  border-radius: 0 !important;
  padding: 0 36px 0 20px !important;
  transition: all 0.2s ease;
  cursor: pointer;
  color: #475569;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
}

.sr-select::-ms-expand {
  display: none;
}

.sr-select:hover {
  border-color: #cbd5e1 !important;
}

.sr-select:focus {
  border-color: #007aff !important;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.08) !important;
  outline: none;
}

.sr-date-range-picker {
  width: 600px;
}

.sr-btn-resend {
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%) !important;
  border-color: transparent !important;
  color: #fff !important;
  font-weight: 600 !important;
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.25) !important;
}

.sr-btn-resend:hover {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%) !important;
  color: #fff !important;
  box-shadow: 0 6px 24px rgba(245, 158, 11, 0.38) !important;
  transform: translateY(-1px);
}

.sr-btn-blue {
  color: #fff !important;
  background: linear-gradient(135deg, #0a84ff 0%, #007aff 52%, #00a6ff 100%) !important;
  border-color: transparent !important;
  font-weight: 600 !important;
  box-shadow: 0 12px 24px rgba(0, 122, 255, 0.22), inset 0 1px 0 rgba(255, 255, 255, 0.34) !important;
}

.sr-btn-blue:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 32px rgba(0, 122, 255, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.4) !important;
}

.sr-btn {
  height: 48px;
  padding: 0 32px;
  font-size: 22px;
  border: 1.5px solid #e2e8f0;
  border-radius: 22px;
  background: #fff;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.sr-btn:hover {
  border-color: #007aff;
  color: #007aff;
  background: #eff6ff;
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.1);
  transform: translateY(-1px);
}

.sr-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
  transform: none;
}

.sr-btn:disabled:hover {
  color: #475569;
  border-color: #e2e8f0;
  background: #fff;
  box-shadow: none;
  transform: none;
}

.sr-table-wrap {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

/* 发送记录表格强制字体 */
.sr-table-wrap :deep(.vxe-header--column),
.sr-table-wrap :deep(.vxe-header--column .vxe-cell) {
  font-size: 24px !important;
  font-weight: 600 !important;
  background: #f8fafc !important;
  color: #1e293b !important;
}

.sr-table-wrap :deep(.vxe-body--column),
.sr-table-wrap :deep(.vxe-body--column .vxe-cell) {
  font-size: 22px !important;
  color: #334155 !important;
}

.sr-table-wrap :deep(.vxe-body--row) {
  min-height: 80px !important;
}

/* 发送记录表格边框补全 */
 .sr-table-wrap :deep(.vxe-header--column) {
   border: 1px solid #e5e7eb !important;
   cursor: move;
 }
 .sr-table-wrap :deep(.vxe-body--column) {
   border: 1px solid #e5e7eb !important;
 }

.sr-table-wrap :deep(.vxe-body--row:nth-child(even)) {
  background: #f8fafc;
}

.sr-table-wrap :deep(.vxe-resizable) {
  min-width: 8px;
}

.sr-table-wrap :deep(.col--drag-handle) {
  display: inline-block;
}

:deep(.vxe-cell--wrapper) {
  font-size: inherit !important;
}

.sr-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  gap: 16px;
  margin-top: 16px;
  border-top: 1px solid #e2e8f0;
  margin-left: 28px;
  margin-right: 28px;
}

.sr-footer-left {
  display: flex;
  gap: 10px;
}

.sr-footer-center {
  font-size: 24px;
  color: #475569;
  white-space: nowrap;
  font-weight: 500;
}

.sr-footer-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sr-page-info {
  font-size: 24px;
  color: #475569;
  min-width: 100px;
  text-align: center;
  font-weight: 500;
}

.sr-page-size-label {
  margin-left: 16px;
  font-size: 22px;
  color: #475569;
}

.sr-page-size-select {
  height: 40px;
  font-size: 22px;
  border: 1.5px solid #e2e8f0;
  border-radius: 0;
  padding: 0 32px 0 12px;
  color: #475569;
  background: #fff;
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
}

.sms-modal-body :deep(.sms-form-input),
.sms-modal-body :deep(.sms-form-select) {
  border-color: #cbd5e1;
  border-radius: 8px;
}

.sms-modal-body :deep(.sms-form-input:focus),
.sms-modal-body :deep(.sms-form-select:focus) {
  border-color: #007aff;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}



/* 发送记录日期范围输入框 */
.sr-date-picker {
  width: 280px;
}

.sr-date-sep {
  font-size: 22px;
  color: #999;
  flex-shrink: 0;
}

/* SimpleDatePicker 在 SMS 页面中的样式 */
.sr-date-picker :deep(.sdp-input) {
  height: 48px;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 18px;
  border: 2px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  user-select: none;
  box-sizing: border-box;
}
.sr-date-picker :deep(.sdp-placeholder) { color: #999; flex: 1; }
.sr-date-picker :deep(.sdp-value) { color: #007aff; flex: 1; }
.sr-date-picker :deep(.sdp-icon) {
  width: 26px; height: 26px;
  color: #999;
  flex-shrink: 0;
}
.sr-date-picker :deep(.sdp-clear) {
  width: 24px; height: 24px;
  color: #ccc;
  flex-shrink: 0;
  cursor: pointer;
}
.sr-date-picker :deep(.sdp-clear:hover) { color: #999; }
</style>
