<template>
  <div v-if="currentView === 'list'" class="gallery-page">
    <div class="gallery-card gallery-form-card">
      <div class="gallery-form-top">
        <div class="gallery-section-title">
          <div class="gallery-section-icon">
            <FileText :size="16" />
          </div>
          <strong>择样图库</strong>
          <span>数据录入</span>
        </div>
        <div class="gallery-form-actions">
          <span class="gallery-mode-pill">新增模式</span>
          <button class="gallery-btn gallery-btn-ghost" @click="resetForm">
            <RotateCcw :size="14" /> 重置
          </button>
          <button class="gallery-btn gallery-btn-primary" @click="saveForm">
            <Save :size="14" /> 保存
          </button>
        </div>
      </div>
      <div class="gallery-form-grid">
        <div class="gallery-form-field">
          <label class="gallery-form-label">择样日期</label>
          <input type="datetime-local" class="gallery-form-input" v-model="formData.sampleDate" />
        </div>
        <div class="gallery-form-field">
          <label class="gallery-form-label">本次代号</label>
          <input type="text" class="gallery-form-input" v-model="formData.code" placeholder="请输入代号" />
        </div>
        <div class="gallery-form-field">
          <label class="gallery-form-label">客户名称</label>
          <input type="text" class="gallery-form-input" v-model="formData.customer" placeholder="请输入客户名称" />
        </div>
        <div class="gallery-form-field">
          <label class="gallery-form-label">拍摄人员</label>
          <input type="text" class="gallery-form-input" v-model="formData.photographer" placeholder="请输入拍摄人员" />
        </div>
        <div class="gallery-form-field full">
          <label class="gallery-form-label">备注</label>
          <textarea class="gallery-form-textarea" v-model="formData.remark" placeholder="请输入备注"></textarea>
        </div>
      </div>
    </div>

    <div class="gallery-card gallery-toolbar-card">
      <div class="gallery-toolbar-row search-row">
        <div class="gallery-search">
          <Search :size="14" />
          <input v-model="searchKeyword" placeholder="搜索代号、客户名称..." @keyup.enter="onQuery" />
        </div>
        <input type="date" class="gallery-date-input" v-model="dateFrom" />
        <span class="gallery-date-separator">至</span>
        <input type="date" class="gallery-date-input" v-model="dateTo" />
        <button class="gallery-btn gallery-btn-primary" @click="onQuery">
          <Search :size="14" /> 查询
        </button>
        <div></div>
      </div>
      <div class="gallery-toolbar-row action-row">
        <button class="gallery-btn gallery-btn-primary" @click="addCode">
          <Plus :size="14" /> 添加代号
        </button>
        <button class="gallery-btn gallery-btn-danger" :disabled="selectedIds.length === 0" @click="deleteCode">
          <Trash2 :size="14" /> 删除代号
        </button>
        <button class="gallery-btn gallery-btn-ghost" @click="exportList">
          <Download :size="14" /> 导出列表
        </button>
      </div>
    </div>

    <div class="gallery-card gallery-table-card">
      <div class="gallery-table-wrap">
        <table class="gallery-data-table">
          <thead>
            <tr>
              <th style="width:44px">
                <input type="checkbox" :checked="isAllSelected" @change="toggleAll" />
              </th>
              <th>ID</th>
              <th>择样日期</th>
              <th>代号</th>
              <th>客户名称</th>
              <th>拍摄人员</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pagedData" :key="row.id" :class="{ selected: selectedIds.includes(row.id) }">
              <td>
                <input type="checkbox" :checked="selectedIds.includes(row.id)" @change="toggleRow(row.id)" />
              </td>
              <td>{{ row.id }}</td>
              <td>{{ row.sampleDate }}</td>
              <td>
                <a class="gallery-code-link" @click.prevent="openDetail(row)">{{ row.code }}</a>
              </td>
              <td>{{ row.customer }}</td>
              <td>{{ row.photographer }}</td>
              <td>
                <span class="gallery-encrypt-badge" :class="row.encrypted ? 'locked' : 'open'">
                  {{ row.encrypted ? '加密' : '未加密' }}
                </span>
              </td>
              <td>
                <div class="gallery-row-actions">
                  <button class="gallery-row-btn" @click="openDetail(row)">
                    <Eye :size="14" /> 详情
                  </button>
                  <button class="gallery-row-btn" @click="editRow(row)">
                    <Pencil :size="14" /> 编辑
                  </button>
                  <button class="gallery-row-btn danger" @click="deleteRow(row)">
                    <Trash2 :size="14" /> 删除
                  </button>
                  <button v-if="!row.encrypted" class="gallery-row-btn lock" @click="encryptRow(row)">
                    <Lock :size="14" /> 加密
                  </button>
                  <button v-else class="gallery-row-btn unlock" @click="decryptRow(row)">
                    <Unlock :size="14" /> 解密
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="gallery-statusbar">
        <button class="gallery-btn gallery-btn-ghost" @click="selectAll">
          <CheckSquare :size="14" /> 全选
        </button>
        <button class="gallery-btn gallery-btn-ghost" @click="invertSelection">
          <Shuffle :size="14" /> 反选
        </button>
        <button class="gallery-btn gallery-btn-ghost" @click="clearSelection">
          <Square :size="14" /> 清空
        </button>
        <div class="gallery-status-info">
          已选 <strong>{{ selectedIds.length }}</strong> 条，共 <strong>{{ tableData.length }}</strong> 条
        </div>
        <select class="gallery-page-size" v-model.number="pageSize">
          <option :value="10">10 条/页</option>
          <option :value="20">20 条/页</option>
          <option :value="50">50 条/页</option>
          <option :value="100">100 条/页</option>
        </select>
        <div style="display:flex;align-items:center;gap:4px">
          <button class="gallery-btn gallery-btn-ghost" :disabled="currentPage <= 1" @click="currentPage--">
            ‹
          </button>
          <span style="font-size:12px;font-weight:700;color:rgba(29,29,31,0.56)">{{ currentPage }} / {{ totalPages }}</span>
          <button class="gallery-btn gallery-btn-ghost" :disabled="currentPage >= totalPages" @click="currentPage++">
            ›
          </button>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="gallery-detail-page">
    <div class="gallery-card gallery-detail-top">
      <div class="gallery-detail-left">
        <button class="gallery-btn gallery-btn-ghost" @click="currentView = 'list'">
          <ArrowLeft :size="14" /> 返回
        </button>
        <div class="gallery-detail-context">
          <span class="gallery-detail-chip"><Hash :size="12" /> {{ detailData.code }}</span>
          <span class="gallery-detail-chip"><Building2 :size="12" /> {{ detailData.customer }}</span>
          <span class="gallery-detail-chip"><Camera :size="12" /> {{ detailData.photographer }}</span>
        </div>
      </div>
      <div class="gallery-detail-right">
        <button class="gallery-btn gallery-btn-ghost" :class="{ 'gallery-btn-primary': thumbSize === 'small' }" @click="thumbSize = 'small'">
          <LayoutGrid :size="14" /> 小图
        </button>
        <button class="gallery-btn gallery-btn-ghost" :class="{ 'gallery-btn-primary': thumbSize === 'medium' }" @click="thumbSize = 'medium'">
          <LayoutGrid :size="14" /> 中图
        </button>
        <button class="gallery-btn gallery-btn-ghost" :class="{ 'gallery-btn-primary': thumbSize === 'large' }" @click="thumbSize = 'large'">
          <Maximize2 :size="14" /> 大图
        </button>
        <button class="gallery-btn gallery-btn-ghost" @click="exportImages">
          <Archive :size="14" /> 导出图片
        </button>
        <button class="gallery-btn gallery-btn-ghost" @click="exportExcel">
          <FileSpreadsheet :size="14" /> 导出Excel
        </button>
        <button class="gallery-btn gallery-btn-ghost" @click="importImages">
          <FileUp :size="14" /> 导入
        </button>
      </div>
    </div>

    <div class="gallery-detail-main">
      <div class="gallery-info-grid">
        <div class="gallery-info-card">
          <div class="gallery-info-label"><Calendar :size="14" /> 择样日期</div>
          <div class="gallery-info-value">{{ detailData.sampleDate }}</div>
        </div>
        <div class="gallery-info-card">
          <div class="gallery-info-label"><Hash :size="14" /> 本次代号</div>
          <div class="gallery-info-value">{{ detailData.code }}</div>
        </div>
        <div class="gallery-info-card">
          <div class="gallery-info-label"><Building2 :size="14" /> 客户名称</div>
          <div class="gallery-info-value">{{ detailData.customer }}</div>
        </div>
        <div class="gallery-info-card">
          <div class="gallery-info-label"><Camera :size="14" /> 拍摄人员</div>
          <div class="gallery-info-value">{{ detailData.photographer }}</div>
        </div>
        <div class="gallery-info-card">
          <div class="gallery-info-label"><Clock :size="14" /> 创建时间</div>
          <div class="gallery-info-value">{{ detailData.createTime }}</div>
        </div>
        <div class="gallery-info-card">
          <div class="gallery-info-label"><RefreshCw :size="14" /> 修改时间</div>
          <div class="gallery-info-value">{{ detailData.modifyTime }}</div>
        </div>
      </div>

      <div class="gallery-stat-grid">
        <div class="gallery-stat-card">
          <div class="gallery-stat-icon green"><Upload :size="20" /></div>
          <div class="gallery-stat-text">
            <span>已导出数据</span>
            <strong>{{ detailData.exportedCount }}</strong>
          </div>
        </div>
        <div class="gallery-stat-card">
          <div class="gallery-stat-icon orange"><CircleAlert :size="20" /></div>
          <div class="gallery-stat-text">
            <span>未导出数据</span>
            <strong>{{ detailData.unexportedCount }}</strong>
          </div>
        </div>
      </div>

      <div class="gallery-card gallery-image-table-card">
        <div class="gallery-image-table-head">
          <h3><LayoutGrid :size="16" /> 图片列表</h3>
          <div style="display:flex;gap:8px">
            <button class="gallery-btn gallery-btn-ghost" @click="refreshImages">
              <RefreshCw :size="14" /> 刷新
            </button>
          </div>
        </div>
        <div class="gallery-detail-scroll">
          <table class="gallery-image-table">
            <thead>
              <tr>
                <th style="width:44px">
                  <input type="checkbox" :checked="isAllDetailSelected" @change="toggleAllDetail" />
                </th>
                <th>序号</th>
                <th>公司编号</th>
                <th>出厂货号</th>
                <th>图片区</th>
                <th>拍摄人</th>
                <th>修改人</th>
                <th>上传时间</th>
                <th>修改时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(img, idx) in detailImages" :key="img.id">
                <td>
                  <input type="checkbox" :checked="selectedImageIds.includes(img.id)" @change="toggleDetailRow(img.id)" />
                </td>
                <td>{{ idx + 1 }}</td>
                <td>{{ img.companyCode }}</td>
                <td>{{ img.factoryCode }}</td>
                <td class="gallery-img-strip">
                  <div class="gallery-thumb-item" v-for="(color, ci) in img.colors" :key="ci">
                    <div
                      class="gallery-thumb-box"
                      :style="{
                        width: thumbPx + 'px',
                        height: thumbPx + 'px',
                        background: color
                      }"
                    ></div>
                    <span class="gallery-thumb-label">{{ img.labels[ci] }}</span>
                  </div>
                </td>
                <td>{{ img.photographer }}</td>
                <td>{{ img.modifier }}</td>
                <td>{{ img.uploadTime }}</td>
                <td>{{ img.modifyTime }}</td>
                <td class="gallery-sticky-action">
                  <div class="gallery-row-actions">
                    <button class="gallery-row-btn"><Eye :size="14" /> 查看</button>
                    <button class="gallery-row-btn"><Pencil :size="14" /> 编辑</button>
                    <button class="gallery-row-btn danger"><Trash2 :size="14" /> 删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="gallery-detail-footer">
          <button class="gallery-btn gallery-btn-ghost" @click="selectAllDetail">
            <CheckSquare :size="14" /> 全选
          </button>
          <button class="gallery-btn gallery-btn-ghost" @click="invertDetailSelection">
            <Shuffle :size="14" /> 反选
          </button>
          <button class="gallery-btn gallery-btn-ghost" @click="clearDetailSelection">
            <Square :size="14" /> 清空
          </button>
          <div class="gallery-status-info">
            共 <strong>{{ detailImages.length }}</strong> 条记录
          </div>
          <select class="gallery-page-size" v-model.number="detailPageSize">
            <option :value="10">10 条/页</option>
            <option :value="20">20 条/页</option>
            <option :value="50">50 条/页</option>
          </select>
          <div style="display:flex;align-items:center;gap:4px">
            <button class="gallery-btn gallery-btn-ghost" :disabled="detailCurrentPage <= 1" @click="detailCurrentPage--">
              ‹
            </button>
            <span style="font-size:12px;font-weight:700;color:rgba(29,29,31,0.56)">{{ detailCurrentPage }} / {{ detailTotalPages }}</span>
            <button class="gallery-btn gallery-btn-ghost" :disabled="detailCurrentPage >= detailTotalPages" @click="detailCurrentPage++">
              ›
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import '@/styles/gallery.css'
import {
  FileText, RotateCcw, Save, Search, Plus, Trash2, Download,
  CheckSquare, Shuffle, Square, ArrowLeft, Hash, Building2, Camera,
  LayoutGrid, Maximize2, Archive, FileSpreadsheet, FileUp, Upload,
  Eye, Pencil, Lock, Unlock, Calendar, Clock, RefreshCw, CircleAlert
} from 'lucide-vue-next'

const currentView = ref('list')

const formData = reactive({
  sampleDate: '',
  code: '',
  customer: '',
  photographer: '',
  remark: ''
})

const resetForm = () => {
  formData.sampleDate = ''
  formData.code = ''
  formData.customer = ''
  formData.photographer = ''
  formData.remark = ''
}

const saveForm = () => {}

const searchKeyword = ref('')
const dateFrom = ref('')
const dateTo = ref('')

const onQuery = () => {}

const tableData = ref([
  { id: 1, sampleDate: '2026-05-20', code: 'A001', customer: '杭州锦程贸易', photographer: '张伟', encrypted: true },
  { id: 2, sampleDate: '2026-05-18', code: 'B002', customer: '深圳华创科技', photographer: '李娜', encrypted: false },
  { id: 3, sampleDate: '2026-05-15', code: 'C003', customer: '上海盛达实业', photographer: '王磊', encrypted: true },
  { id: 4, sampleDate: '2026-05-12', code: 'D004', customer: '广州永信商贸', photographer: '赵敏', encrypted: false },
  { id: 5, sampleDate: '2026-05-10', code: 'E005', customer: '北京中天集团', photographer: '陈静', encrypted: true },
  { id: 6, sampleDate: '2026-05-08', code: 'F006', customer: '成都瑞丰贸易', photographer: '刘洋', encrypted: false }
])

const selectedIds = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

const totalPages = computed(() => Math.max(1, Math.ceil(tableData.value.length / pageSize.value)))
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

const isAllSelected = computed(() => pagedData.value.length > 0 && pagedData.value.every(r => selectedIds.value.includes(r.id)))

const toggleAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = selectedIds.value.filter(id => !pagedData.value.some(r => r.id === id))
  } else {
    const ids = new Set(selectedIds.value)
    pagedData.value.forEach(r => ids.add(r.id))
    selectedIds.value = [...ids]
  }
}

const toggleRow = (id) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) selectedIds.value.splice(idx, 1)
  else selectedIds.value.push(id)
}

const selectAll = () => {
  selectedIds.value = tableData.value.map(r => r.id)
}

const invertSelection = () => {
  selectedIds.value = tableData.value.filter(r => !selectedIds.value.includes(r.id)).map(r => r.id)
}

const clearSelection = () => {
  selectedIds.value = []
}

const addCode = () => {}
const deleteCode = () => {}
const exportList = () => {}

const openDetail = (row) => {
  detailData.value = { ...row, createTime: row.sampleDate + ' 09:00:00', modifyTime: row.sampleDate + ' 14:30:00', exportedCount: 12, unexportedCount: 5 }
  currentView.value = 'detail'
}

const editRow = (row) => {
  formData.sampleDate = row.sampleDate
  formData.code = row.code
  formData.customer = row.customer
  formData.photographer = row.photographer
  formData.remark = ''
}

const deleteRow = (row) => {
  tableData.value = tableData.value.filter(r => r.id !== row.id)
  selectedIds.value = selectedIds.value.filter(id => id !== row.id)
}

const encryptRow = (row) => { row.encrypted = true }
const decryptRow = (row) => { row.encrypted = false }

const detailData = ref({
  sampleDate: '', code: '', customer: '', photographer: '',
  createTime: '', modifyTime: '', exportedCount: 0, unexportedCount: 0
})

const thumbSize = ref('small')
const thumbPx = computed(() => thumbSize.value === 'small' ? 40 : thumbSize.value === 'medium' ? 64 : 96)

const palette = [
  '#5ac8fa', '#34c759', '#ff9f0a', '#ff3b30', '#af52de', '#5856d6',
  '#007aff', '#30d158', '#ff6482', '#ffcc00', '#64d2ff', '#bf5af2'
]

const generateColors = (seed) => {
  const colors = []
  const labels = []
  for (let i = 0; i < 6; i++) {
    colors.push(palette[(seed + i) % palette.length])
    labels.push(`IMG-${String(seed * 6 + i + 1).padStart(3, '0')}`)
  }
  return { colors, labels }
}

const detailImages = ref(
  Array.from({ length: 3 }, (_, ri) => ({
    id: ri + 1,
    companyCode: `CP-${String(ri + 1).padStart(4, '0')}`,
    factoryCode: `FC-${String(ri + 1).padStart(3, '0')}`,
    colors: generateColors(ri).colors,
    labels: generateColors(ri).labels,
    photographer: '张伟',
    modifier: '李娜',
    uploadTime: '2026-05-20 10:30',
    modifyTime: '2026-05-21 15:45'
  }))
)

const selectedImageIds = ref([])
const detailCurrentPage = ref(1)
const detailPageSize = ref(10)
const detailTotalPages = computed(() => Math.max(1, Math.ceil(detailImages.value.length / detailPageSize.value)))

const isAllDetailSelected = computed(() => detailImages.value.length > 0 && detailImages.value.every(r => selectedImageIds.value.includes(r.id)))

const toggleAllDetail = () => {
  if (isAllDetailSelected.value) {
    selectedImageIds.value = []
  } else {
    selectedImageIds.value = detailImages.value.map(r => r.id)
  }
}

const toggleDetailRow = (id) => {
  const idx = selectedImageIds.value.indexOf(id)
  if (idx >= 0) selectedImageIds.value.splice(idx, 1)
  else selectedImageIds.value.push(id)
}

const selectAllDetail = () => {
  selectedImageIds.value = detailImages.value.map(r => r.id)
}

const invertDetailSelection = () => {
  selectedImageIds.value = detailImages.value.filter(r => !selectedImageIds.value.includes(r.id)).map(r => r.id)
}

const clearDetailSelection = () => {
  selectedImageIds.value = []
}

const exportImages = () => {}
const exportExcel = () => {}
const importImages = () => {}
const refreshImages = () => {}
</script>
