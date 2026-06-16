<template>
  <div class="manufacturer-page">
    <div class="manufacturer-card manufacturer-form-card" :class="{ expanded: formExpanded }">
      <div class="manufacturer-form-top">
        <div class="manufacturer-form-actions">
          <template v-if="formMode === 'readonly' && currentManufacturer">
            <button class="manufacturer-btn manufacturer-btn-ghost" title="重置" @click="resetForm">
              <RotateCcw :size="14" />
            </button>
          </template>
          <button class="manufacturer-btn manufacturer-btn-ghost" title="字段设置" @click="toggleFieldSettings">
            <Settings :size="14" />
          </button>
          <button class="manufacturer-btn manufacturer-btn-ghost" :title="formExpanded ? '收起' : '展开'" @click="formExpanded = !formExpanded">
            <component :is="formExpanded ? ChevronsUp : ChevronsDown" :size="14" />
          </button>
        </div>
      </div>

      <div v-if="showFieldSettings" class="manufacturer-field-settings">
        <div class="field-settings-header">
          <span class="field-settings-title">字段显示设置</span>
          <button class="field-settings-close" @click="showFieldSettings = false">
            <X :size="14" />
          </button>
        </div>
        <div class="field-settings-grid">
          <label v-for="f in allFormFields" :key="f.key" class="field-settings-chip">
            <input
              class="field-order-input"
              type="number"
              min="1"
              :value="fieldOrder[f.key]"
              @input="e => fieldOrder[f.key] = Number(e.target.value)"
              @click.stop
            />
            <input type="checkbox" v-model="fieldVisible[f.key]" />
            {{ f.label }}
          </label>
        </div>
      </div>

      <div class="manufacturer-form-body">
        <div class="manufacturer-form-scroll">
          <div class="manufacturer-form-grid">
            <div
              v-for="f in visibleFormFields"
              :key="f.key"
              class="manufacturer-form-field"
              :style="{ flex: '0 0 auto' }"
            >
              <label class="manufacturer-form-label" :style="{ flex: '0 0 84px', textAlign: 'justify', textAlignLast: 'justify' }">{{ f.label }}</label>
              <input
                class="manufacturer-form-input"
                v-model="formData[f.key]"
                :readonly="formMode === 'readonly'"
                :placeholder="formMode !== 'readonly' ? '请输入' + f.label : ''"
                :style="{ flex: '0 0 ' + (f.key === 'smsNumber' || f.key === 'certificate' ? '470px' : '180px') }"
              />
            </div>
          </div>
        </div>

        <div class="manufacturer-image-strip">
          <template v-if="certificateImage">
            <div class="manufacturer-image-strip-single">
              <img
                :src="certificateImage.startsWith('http') ? certificateImage : '/images/' + certificateImage"
                @click="viewCertificate"
                style="cursor:pointer"
                @error="certificateImage = null"
              />
              <span class="spm-strip-counter">营业执照</span>
            </div>
          </template>
          <template v-else>
            <span class="manufacturer-image-strip-empty">暂无营业执照</span>
          </template>
          <label v-if="formMode === 'edit' || formMode === 'add'" class="manufacturer-image-upload-btn">
            <Upload :size="14" /> 上传
            <input type="file" accept="image/*" hidden @change="onCertificateUpload" />
          </label>
        </div>
      </div>
    </div>

    <div class="manufacturer-card manufacturer-toolbar-card">
      <div class="manufacturer-toolbar-row">
        <div class="manufacturer-search">
          <Search :size="16" />
          <input v-model="searchKeyword" placeholder="搜索厂商名称/摊位号/手机号/短信号码" @keydown.enter="onSearch" />
        </div>
        <button class="manufacturer-btn manufacturer-btn-primary" @click="onSearch">
          <Search :size="14" /> 查询
        </button>
        <button class="manufacturer-btn manufacturer-btn-ghost" @click="clearSearch">
          <X :size="14" /> 清除
        </button>
        <span class="toolbar-sep"></span>
        <button class="manufacturer-btn manufacturer-btn-primary" @click="startAdd">
          <Plus :size="14" /> 添加厂商
        </button>
        <template v-if="formMode === 'edit' || formMode === 'add'">
          <button class="manufacturer-btn manufacturer-btn-primary" @click="saveManufacturer">
            <Save :size="14" /> 保存
          </button>
          <button class="manufacturer-btn manufacturer-btn-ghost" @click="cancelEdit">
            <X :size="14" /> 取消
          </button>
        </template>
        <button v-else class="manufacturer-btn manufacturer-btn-ghost" :disabled="!currentManufacturer" @click="startEdit">
          <Pencil :size="14" /> 修改
        </button>
        <button class="manufacturer-btn manufacturer-btn-danger" :disabled="!currentManufacturer" @click="deleteCurrent">
          <Trash2 :size="14" /> 删除
        </button>
        <button class="manufacturer-btn manufacturer-btn-ghost" @click="importData">
          <Upload :size="14" /> 导入资料
        </button>
      </div>
    </div>

    <div class="manufacturer-card manufacturer-table-card">
      <div ref="tableWrapRef" class="manufacturer-table-wrap">
        <vxe-grid
          ref="gridRef"
          :columns="allColumns"
          :data="filteredTableData"
          :loading="tableLoading"
          :height="tableWrapHeight"
          :toolbar-config="{ custom: true, zoom: true }"
          :column-config="{ resizable: true, drag: true }"
          :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
          :checkbox-config="{ highlight: true, checkField: 'checkbox' }"
          :cell-config="{ height: 44 }"
          :sort-config="{ trigger: 'header', remote: false }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 0, rSize: 60, rHeight: 44 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 24 }, scrollY: { gt: 0, oSize: 0, rSize: 60, rHeight: 44 } }"
          :border="true"
          :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontWeight: 600, textAlign: 'center' }"
          :cell-style="{ textAlign: 'center' }"
          @cell-click="onCellClick"
        >
          <template #col_manufacturerCode="{ row }">
            <a class="cell-link" href="javascript:void(0)" @click.stop="openSamplePage(row.manufacturerCode)">{{ row.manufacturerCode }}</a>
          </template>
        </vxe-grid>
      </div>
      <div class="manufacturer-statusbar">
        <div class="manufacturer-status-info">
          共 <strong>{{ totalRecords }}</strong> 条
        </div>
        <div class="manufacturer-pagination">
          <span class="manufacturer-page-size-label">每页</span>
          <select class="manufacturer-page-size-select" v-model.number="pageSize">
            <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
          <span class="manufacturer-page-size-label">条</span>
          <button class="manufacturer-btn manufacturer-btn-ghost" :disabled="currentPage <= 1" @click="goPage(1)">
            <ChevronsLeft :size="14" />
          </button>
          <button class="manufacturer-btn manufacturer-btn-ghost" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">
            <ChevronLeft :size="14" />
          </button>
          <span class="manufacturer-page-text">{{ currentPage }} / {{ totalPages }}</span>
          <button class="manufacturer-btn manufacturer-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">
            <ChevronRight :size="14" />
          </button>
          <button class="manufacturer-btn manufacturer-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(totalPages)">
            <ChevronsRight :size="14" />
          </button>
        </div>
      </div>
    </div>
  </div>

  <Teleport to="body">
  <div v-if="showBatchResultModal" class="batch-image-modal-overlay" @click.self="showBatchResultModal = false">
    <div class="batch-result-modal">
      <div class="batch-result-header">
        <strong>系统提示</strong>
        <button class="modal-close-btn" @click="showBatchResultModal = false"><X :size="16" /></button>
      </div>
      <div class="batch-result-body">
        <p class="br-summary">
          <template v-if="batchResult.duplicateCount > 0 || batchResult.failCount > 0">
            导入成功 <span class="br-ok">{{ batchResult.successCount }}</span> 条
            <template v-if="batchResult.updatedCount > 0">，覆盖更新 <span class="br-updated">{{ batchResult.updatedCount }}</span> 条</template>
            <template v-if="batchResult.duplicateCount > 0">，跳过重复 <span class="br-warn">{{ batchResult.duplicateCount }}</span> 条</template>
            <template v-if="batchResult.failCount > 0">，导入失败 <span class="br-err">{{ batchResult.failCount }}</span> 条</template>
          </template>
          <template v-else>
            导入成功 <span class="br-ok">{{ batchResult.successCount }}</span> 条数据
            <template v-if="batchResult.unmatchedCount > 0">，未匹配 <span class="br-warn">{{ batchResult.unmatchedCount }}</span> 条数据</template>
          </template>
          。
        </p>
        <template v-if="batchResult.failedRows && batchResult.failedRows.length > 0">
          <p class="br-section-title">
            异常记录 ({{ batchResult.failedRows.length }}条)
            <button class="manufacturer-btn manufacturer-btn-ghost" style="font-size:11px;padding:1px 8px;height:22px;margin-left:8px" @click="exportImportFailedRows">
              <FileDown :size="12" /> 导出
            </button>
          </p>
          <div class="br-detail-list br-failed-table-wrap">
            <table class="br-failed-table">
              <thead><tr><th>行号</th><th>厂商编号</th><th>厂商名称</th><th>原因</th><th>类型</th></tr></thead>
              <tbody>
                <tr v-for="(row, i) in batchResult.failedRows" :key="'fr'+i" :class="{ 'br-dup-row': row.类型 === '重复' }">
                  <td>{{ row.row }}</td>
                  <td>{{ row['厂商编号'] }}</td>
                  <td>{{ row['厂商名称'] }}</td>
                  <td class="br-reason-cell">{{ row['失败原因'] }}</td>
                  <td><span :class="row.类型 === '重复' ? 'br-warn-text' : 'br-err-text'">{{ row.类型 }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
        <template v-if="batchResult.failList && batchResult.failList.length > 0">
          <p class="br-section-title">失败原因：</p>
          <div class="br-detail-list">
            <p v-for="(err, i) in batchResult.failList" :key="'f'+i" class="br-detail-item br-err-text">{{ err }}</p>
          </div>
        </template>
        <template v-if="batchResult.unmatchedList && batchResult.unmatchedList.length > 0">
          <p class="br-section-title">未匹配数据：</p>
          <div class="br-detail-list">
            <p v-for="(name, i) in batchResult.unmatchedList" :key="'u'+i" class="br-detail-item br-warn-text">{{ name }}: 未匹配到厂商</p>
          </div>
        </template>
      </div>
      <div class="modal-footer">
        <button class="manufacturer-btn manufacturer-btn-primary" @click="showBatchResultModal = false">知道了</button>
      </div>
    </div>
  </div>
  </Teleport>

  <Teleport to="body">
  <div v-if="showImportConfirmModal" class="batch-image-modal-overlay import-confirm-overlay" @click.self="showImportConfirmModal = false">
    <div class="import-confirm-dialog">
      <div class="import-confirm-header">
        <strong>确认导入</strong>
        <button class="modal-close-btn" @click="showImportConfirmModal = false"><X :size="16" /></button>
      </div>
      <div class="import-confirm-body">
        <p>确认导入 <strong>{{ importConfirmCount }}</strong> 条数据？</p>
        <p class="import-confirm-hint">重复厂商编号的资料将被自动跳过</p>
      </div>
      <div class="import-confirm-footer">
        <button class="manufacturer-btn manufacturer-btn-ghost" @click="showImportConfirmModal = false">取消</button>
        <button class="manufacturer-btn manufacturer-btn-primary" @click="executeImport">确定</button>
      </div>
    </div>
  </div>
  </Teleport>

  <Teleport to="body">
  <div v-if="showImportModal" class="batch-image-modal-overlay" @click.self="showImportModal = false">
    <div class="batch-image-modal">
      <div class="batch-image-modal-header">
        <strong>导入厂商数据</strong>
        <button class="modal-close-btn" @click="showImportModal = false">
          <X :size="16" />
        </button>
      </div>
      <div class="batch-image-modal-body">
        <div class="upload-area" @click="$refs.importFileInput.click()" @dragover.prevent="onDragOver" @dragleave="onDragLeave" @drop.prevent="onImportDrop">
          <div class="upload-icon"><FileSpreadsheet :size="48" /></div>
          <div class="upload-text">点击或拖拽上传 .xlsx 文件</div>
          <div class="upload-hint">支持 Excel 2007+ 格式，选择后将预览数据</div>
          <input ref="importFileInput" type="file" accept=".xlsx,.xls" hidden @change="onImportFileChange" />
        </div>
        <div v-if="importFile" class="file-list show">
          <div class="file-list-header">
            <span>已选择文件</span>
            <span class="file-count">1 个文件</span>
          </div>
          <div class="file-list-items">
            <div class="file-item">
              <span class="file-item-icon"><FileSpreadsheet :size="14" /></span>
              <span class="file-item-name">{{ importFile.name }}</span>
              <span class="file-item-size">{{ formatFileSize(importFile.size) }}</span>
              <span class="file-item-remove" @click="importFile = null">
                <X :size="14" />
              </span>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button class="manufacturer-btn manufacturer-btn-ghost" @click="showImportModal = false">取消</button>
      </div>
    </div>
  </div>
  </Teleport>

  <Teleport to="body">
  <div v-if="showImportPreview" class="batch-image-modal-overlay" @click.self="cancelImportPreview">
    <div class="batch-image-modal import-preview-modal">
      <div class="batch-image-modal-header">
        <strong>导入预览 - {{ importFile?.name || 'Excel数据' }}</strong>
        <button class="modal-close-btn" @click="cancelImportPreview">
          <X :size="16" />
        </button>
      </div>
      <div class="batch-image-modal-body import-preview-body">
        <div class="import-preview-summary">
          <span class="import-stat">共 <strong>{{ importPreviewData.length }}</strong> 条数据</span>
          <span class="import-stat">已选 <strong>{{ importSelectedRows.length }}</strong> 条</span>
          <button class="manufacturer-btn manufacturer-btn-ghost" style="font-size:11px;padding:2px 10px;height:26px" :disabled="importSelectedRows.length === 0" @click="deleteSelectedPreviewRows">
            <Trash2 :size="13" /> 批量删除
          </button>
        </div>

        <div class="import-preview-table-wrap">
          <vxe-grid
            ref="importPreviewGridRef"
            :columns="IMPORT_PREVIEW_ALL_COLUMNS"
            :data="importPreviewData"
            :height="380"
            :toolbar-config="{ custom: true, refresh: true, zoom: true }"
            :column-config="{ resizable: true }"
            :row-config="{ isHover: true, keyField: '_rowIndex' }"
            :checkbox-config="{ highlight: true, range: true }"
            :edit-config="{ mode: 'cell', trigger: 'dblclick', showStatus: true, enabled: true }"
            :sort-config="{ multiple: true }"
            :virtual-y-config="{ enabled: true, gt: 15 }"
            :border="true"
            :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontWeight: 600, textAlign: 'center' }"
            :cell-style="{ textAlign: 'center' }"
            @checkbox-change="onImportPreviewCheckChange"
            @checkbox-all="onImportPreviewCheckChange"
          >
            <template #import_action="{ row }">
              <div style="display:flex;gap:4px;justify-content:center">
                <button class="manufacturer-table-action" style="color:#007aff;font-size:11px;padding:2px 8px;height:24px" @click.stop="restorePreviewRow(row)">还原</button>
                <button class="manufacturer-table-action" style="color:#ff3b30;font-size:11px;padding:2px 8px;height:24px" @click.stop="deletePreviewRow(row)">删除</button>
              </div>
            </template>
          </vxe-grid>
        </div>
      </div>
      <div class="modal-footer import-preview-footer">
        <div class="import-toolbar-left">
          <button class="manufacturer-btn manufacturer-btn-ghost" @click="downloadTemplate">
            <Download :size="14" /> 下载模板
          </button>
          <button class="manufacturer-btn manufacturer-btn-ghost" :disabled="importSelectedRows.length === 0" @click="exportSelectedRows">
            <FileDown :size="14" /> 导入选中
          </button>
        </div>
        <div class="import-toolbar-right">
          <label class="import-update-mode-label" :class="{ active: importUpdateMode }">
            <input type="checkbox" v-model="importUpdateMode" :disabled="importUploading" />
            覆盖已有数据
          </label>
          <button class="manufacturer-btn manufacturer-btn-ghost" @click="cancelImportPreview" :disabled="importUploading">
            取消导入
          </button>
          <button class="manufacturer-btn manufacturer-btn-danger" :disabled="importPreviewData.length === 0 || importUploading" @click="doConfirmImport('all')">
            <Upload :size="14" /> {{ importUploading ? '导入中...' : `全选导入(${importPreviewData.length})` }}
          </button>
          <button class="manufacturer-btn manufacturer-btn-primary" :disabled="importSelectedRows.length === 0 || importUploading" @click="doConfirmImport('selected')">
            <Upload :size="14" /> {{ importUploading ? '导入中...' : `确认导入(${importSelectedRows.length})` }}
          </button>
        </div>
      </div>
      <div v-if="importUploading" class="import-progress-area">
        <div class="import-progress-bar-wrap">
          <div class="import-progress-bar" :style="{ width: importProgress + '%' }"></div>
        </div>
        <span class="import-progress-text">{{ importProgressText }}</span>
      </div>
    </div>
  </div>
  </Teleport>

  <Teleport to="body">
  <div v-if="showConfirm" class="batch-image-modal-overlay" @click.self="onConfirmCancel">
    <div class="batch-result-modal" style="max-width:420px">
      <div class="batch-result-header">
        <strong>确认操作</strong>
        <button class="modal-close-btn" @click="onConfirmCancel"><X :size="16" /></button>
      </div>
      <div class="batch-result-body" style="text-align:center;padding:24px 20px">
        <AlertTriangle :size="40" style="color:#ff9500;margin-bottom:12px" />
        <p style="font-size:14px;color:#1d1d1f;line-height:1.6;white-space:pre-wrap">{{ confirmMessage }}</p>
      </div>
      <div class="modal-footer">
        <button class="manufacturer-btn manufacturer-btn-ghost" @click="onConfirmCancel">取消</button>
        <button class="manufacturer-btn manufacturer-btn-danger" @click="onConfirmOk">确定</button>
      </div>
    </div>
  </div>
  </Teleport>

  <Teleport to="body">
  <div v-if="showAlert" class="batch-image-modal-overlay" @click.self="showAlert = false">
    <div class="batch-result-modal" style="max-width:400px">
      <div class="batch-result-header">
        <strong>系统提示</strong>
        <button class="modal-close-btn" @click="showAlert = false"><X :size="16" /></button>
      </div>
      <div class="batch-result-body" style="text-align:center;padding:24px 20px">
        <Info :size="40" style="color:#007aff;margin-bottom:12px" />
        <p style="font-size:14px;color:#1d1d1f;line-height:1.6;white-space:pre-wrap">{{ alertMessage }}</p>
      </div>
      <div class="modal-footer">
        <button class="manufacturer-btn manufacturer-btn-primary" @click="showAlert = false">知道了</button>
      </div>
    </div>
  </div>
  </Teleport>

</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, onActivated, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/api'
import { useSidebar } from '@/composables/useSidebar'
import {
  Store, Search, Plus, Pencil, Trash2, Save, X, Settings,
  ChevronsUp, ChevronsDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  RotateCcw, Upload, Download, FileDown, FileSpreadsheet,
  AlertTriangle, Info
} from 'lucide-vue-next'
import * as XLSX from 'xlsx'
import '@/styles/manufacturer.css'

const router = useRouter()
const { collapsed } = useSidebar()

const allFormFields = [
  { key: 'manufacturerCode', label: '厂商编号' },
  { key: 'name', label: '厂商名称' },
  { key: 'boothNo', label: '摊位号' },
  { key: 'boothType', label: '摊位类型' },
  { key: 'boothMeters', label: '摊位米数' },
  { key: 'mobile1', label: '手机1' },
  { key: 'phone1', label: '电话1' },
  { key: 'contact1', label: '联系人1' },
  { key: 'visitorMobile', label: '见客手机' },
  { key: 'qq', label: 'QQ' },
  { key: 'mobile2', label: '手机2' },
  { key: 'phone2', label: '电话2' },
  { key: 'contact2', label: '联系人2' },
  { key: 'smsNumber', label: '短信号码' },
  { key: 'mobile3', label: '手机3' },
  { key: 'phone3', label: '电话3' },
  { key: 'contact3', label: '联系人3' },
  { key: 'certificate', label: '厂家证书' },
  { key: 'registrant', label: '登记人' },
  { key: 'modifier', label: '修改人' },
  { key: 'remark', label: '备注' },
  { key: 'lastExpiry', label: '上次到期' },
  { key: 'expiryDate', label: '到期日期' },
  { key: 'floorArea', label: '楼层区位' },
  { key: 'mainCard', label: '主卡ID' },
  { key: 'subCard', label: '副卡ID' },
  { key: 'createTime', label: '登记日期' },
  { key: 'updateTime', label: '修改日期' },
  { key: 'boothArea', label: '摊位区域' },
  { key: 'television', label: '电视' },
  { key: 'canInvoice', label: '能否发票' }
]

const fieldVisible = reactive({})
allFormFields.forEach(f => { fieldVisible[f.key] = true })

const fieldOrder = reactive({})
allFormFields.forEach((f, i) => { fieldOrder[f.key] = i + 1 })

const HEADER_TO_FIELD = {
  '厂商编号': 'manufacturerCode', '厂商名称': 'name', '摊位号': 'boothNo',
  '摊位类型': 'boothType', '摊位米数': 'boothMeters',
  '手机1': 'mobile1', '电话1': 'phone1', '联系人1': 'contact1',
  '见客手机': 'visitorMobile', 'QQ': 'qq',
  '手机2': 'mobile2', '电话2': 'phone2', '联系人2': 'contact2',
  '短信号码': 'smsNumber',
  '手机3': 'mobile3', '电话3': 'phone3', '联系人3': 'contact3',
  '厂家证书': 'certificate',
  '楼层区位': 'floorArea', '摊位区域': 'boothArea', '备注': 'remark',
  '上次到期': 'lastExpiry', '到期日期': 'expiryDate', '登记人': 'registrant',
  '主卡ID': 'mainCard', '副卡ID': 'subCard',
  '登记日期': 'createTime', '修改日期': 'updateTime', '修改人': 'modifier',
  '电视': 'television', '能否发票': 'canInvoice'
}

const EDIT_RENDER = { name: 'input' }

const IMPORT_PREVIEW_ALL_COLUMNS = [
  { type: 'checkbox', width: 44, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'manufacturerCode', title: '厂商编号', width: 110, showOverflow: true, editRender: EDIT_RENDER, sortable: true, slots: { default: 'col_manufacturerCode' } },
  { field: 'name', title: '厂商名称', width: 140, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'boothNo', title: '摊位号', width: 100, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'boothType', title: '摊位类型', width: 100, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'boothMeters', title: '摊位米数', width: 100, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'mobile1', title: '手机1', width: 120, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'phone1', title: '电话1', width: 120, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'contact1', title: '联系人1', width: 100, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'visitorMobile', title: '见客手机', width: 120, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'qq', title: 'QQ', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'mobile2', title: '手机2', width: 120, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'phone2', title: '电话2', width: 120, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'contact2', title: '联系人2', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'smsNumber', title: '短信号码', width: 120, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'mobile3', title: '手机3', width: 120, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'phone3', title: '电话3', width: 120, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'contact3', title: '联系人3', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'certificate', title: '厂家证书', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'floorArea', title: '楼层区位', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'boothArea', title: '摊位区域', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'remark', title: '备注', width: 140, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'lastExpiry', title: '上次到期', width: 110, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'expiryDate', title: '到期日期', width: 110, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'registrant', title: '登记人', width: 90, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'mainCard', title: '主卡ID', width: 80, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'subCard', title: '副卡ID', width: 80, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'createTime', title: '登记日期', width: 110, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'updateTime', title: '修改日期', width: 110, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'modifier', title: '修改人', width: 90, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'television', title: '电视', width: 80, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'canInvoice', title: '能否发票', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { title: '操作', width: 120, fixed: 'right', slots: { default: 'import_action' } }
]

const visibleFormFields = computed(() => {
  const sorted = allFormFields
    .filter(f => fieldVisible[f.key])
    .sort((a, b) => (fieldOrder[a.key] || 999) - (fieldOrder[b.key] || 999))

  // 侧栏未折叠：保持原顺序
  if (!collapsed.value) return sorted

  // 侧栏折叠后：把摊位区域→QQ右侧、电视→短信号码右侧、能否发票→厂家证书右侧
  const result = sorted.filter(f => !['boothArea', 'television', 'canInvoice'].includes(f.key))

  const certIdx = result.findIndex(f => f.key === 'certificate')
  const smsIdx = result.findIndex(f => f.key === 'smsNumber')
  const qqIdx = result.findIndex(f => f.key === 'qq')

  const canInvoice = sorted.find(f => f.key === 'canInvoice')
  const television = sorted.find(f => f.key === 'television')
  const boothArea = sorted.find(f => f.key === 'boothArea')

  // 从后往前插入，避免索引偏移
  if (canInvoice && certIdx >= 0) result.splice(certIdx + 1, 0, canInvoice)
  if (television && smsIdx >= 0) result.splice(smsIdx + 1, 0, television)
  if (boothArea && qqIdx >= 0) result.splice(qqIdx + 1, 0, boothArea)

  return result
})

const showFieldSettings = ref(false)
const toggleFieldSettings = () => { showFieldSettings.value = !showFieldSettings.value }

const formExpanded = ref(true)
const formMode = ref('readonly')
const formData = reactive({})
const currentManufacturer = ref(null)
const gridRef = ref(null)
const tableWrapRef = ref(null)
const tableWrapHeight = ref(600)
let resizeObserver = null
let resizeRafId = null
let lastObservedHeight = 0

const searchKeyword = ref('')
const certificateImage = ref('')
const currentPage = ref(1)
const pageSize = ref(2000)
const pageSizeOptions = [500, 1000, 2000, 4000]
const totalRecords = ref(0)
const tableLoading = ref(false)

const list = ref([])

const totalPages = computed(() => Math.max(1, Math.ceil(totalRecords.value / pageSize.value)))

const filteredTableData = computed(() => list.value)

const allColumns = [
  { type: 'checkbox', title: '#', width: 50, fixed: 'left', sortable: true },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'manufacturerCode', title: '厂商编号', width: 150, showOverflow: true, sortable: true, slots: { default: 'col_manufacturerCode' } },
  { field: 'name', title: '厂商名称', width: 200, showOverflow: true },
  { field: 'boothNo', title: '摊位号', width: 180, showOverflow: true, sortable: true },
  { field: 'boothType', title: '摊位类型', minWidth: 100, sortable: true, visible: false },
  { field: 'boothMeters', title: '摊位米数', width: 150, sortable: true },
  { field: 'mobile1', title: '手机1', width: 200, showOverflow: true, sortable: true },
  { field: 'phone1', title: '电话1', width: 150, showOverflow: true, sortable: true },
  { field: 'contact1', title: '联系人1', width: 120, showOverflow: true },
  { field: 'visitorMobile', title: '见客手机', minWidth: 120, showOverflow: true, visible: false },
  { field: 'qq', title: 'QQ', width: 200, showOverflow: true, sortable: true },
  { field: 'mobile2', title: '手机2', minWidth: 120, showOverflow: true, visible: false },
  { field: 'phone2', title: '电话2', minWidth: 130, showOverflow: true, visible: false },
  { field: 'contact2', title: '联系人2', minWidth: 100, showOverflow: true, visible: false },
  { field: 'smsNumber', title: '短信号码', width: 300, showOverflow: true, sortable: true },
  { field: 'mobile3', title: '手机3', minWidth: 120, showOverflow: true, visible: false },
  { field: 'phone3', title: '电话3', minWidth: 130, showOverflow: true, visible: false },
  { field: 'contact3', title: '联系人3', minWidth: 100, showOverflow: true, visible: false },
  { field: 'certificate', title: '厂家证书', minWidth: 100, showOverflow: true, visible: false },
  { field: 'floorArea', title: '楼层区位', minWidth: 100, visible: false },
  { field: 'boothArea', title: '摊位区域', minWidth: 100, visible: false },
  { field: 'remark', title: '备注', width: 300, showOverflow: true },
  { field: 'lastExpiry', title: '上次到期', minWidth: 110, visible: false, formatter: ({ cellValue }) => cellValue ? String(cellValue).replace('T', ' ') : '' },
  { field: 'expiryDate', title: '到期日期', width: 300, sortable: true, formatter: ({ cellValue }) => cellValue ? String(cellValue).replace('T', ' ') : '' },
  { field: 'registrant', title: '登记人', width: 130, sortable: true },
  { field: 'mainCard', title: '主卡ID', minWidth: 80, visible: false },
  { field: 'subCard', title: '副卡ID', minWidth: 80, visible: false },
  { field: 'createTime', title: '登记日期', width: 300, sortable: true, formatter: ({ cellValue }) => cellValue ? String(cellValue).replace('T', ' ') : '' },
  { field: 'updateTime', title: '修改日期', width: 300, sortable: true, formatter: ({ cellValue }) => cellValue ? String(cellValue).replace('T', ' ') : '' },
  { field: 'modifier', title: '修改人', minWidth: 100, visible: false },
  { field: 'television', title: '电视', minWidth: 80, visible: false },
  { field: 'canInvoice', title: '能否发票', minWidth: 100, visible: false }
]

const onSearch = () => {
  currentPage.value = 1
  loadManufacturers()
}

const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadManufacturers()
}

watch(pageSize, () => {
  currentPage.value = 1
  loadManufacturers()
})

const goPage = (page) => {
  currentPage.value = page
  loadManufacturers()
}

const onCellClick = ({ row }) => {
  selectManufacturer(row)
}

const openSamplePage = (manufacturerCode) => {
  router.push({ name: 'SampleManufacturer', params: { manufacturerCode } })
}

const selectManufacturer = (row) => {
  currentManufacturer.value = row
  Object.keys(formData).forEach(k => delete formData[k])
  if (row) {
    Object.assign(formData, { ...row })
    formatFormDataDates()
    certificateImage.value = row.certificate || ''
  }
  if (formMode.value !== 'readonly') {
    formMode.value = 'readonly'
  }
}

const viewCertificate = () => {
  if (certificateImage.value) {
    window.open(certificateImage.value.startsWith('http') ? certificateImage.value : '/images/' + certificateImage.value, '_blank')
  }
}

const onCertificateUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const id = currentManufacturer.value?.id
  if (!id) return
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await api(`/manufacturers/${id}/certificate`, { method: 'POST', body: fd })
    if (res && res.code === 200 && res.data) {
      certificateImage.value = res.data.filePath
      if (currentManufacturer.value) {
        currentManufacturer.value.certificate = res.data.filePath
      }
      formData.certificate = res.data.filePath
    }
  } catch (err) {
    console.error('上传营业执照失败:', err)
    showAlertDialog('上传失败: ' + (err.message || '未知错误'))
  }
  e.target.value = ''
}

const resetForm = () => {
  if (currentManufacturer.value) {
    Object.keys(formData).forEach(k => delete formData[k])
    Object.assign(formData, { ...currentManufacturer.value })
    formatFormDataDates()
  }
}

const startAdd = () => {
  formMode.value = 'add'
  currentManufacturer.value = null
  Object.keys(formData).forEach(k => delete formData[k])
}

const startEdit = () => {
  if (!currentManufacturer.value) return
  formMode.value = 'edit'
  Object.keys(formData).forEach(k => delete formData[k])
  Object.assign(formData, { ...currentManufacturer.value })
  formatFormDataDates()
}

const cancelEdit = () => {
  formMode.value = 'readonly'
  if (currentManufacturer.value) {
    Object.keys(formData).forEach(k => delete formData[k])
    Object.assign(formData, { ...currentManufacturer.value })
    formatFormDataDates()
  }
}

const editRow = (row) => {
  selectManufacturer(row)
  startEdit()
}

const saveManufacturer = () => {
  if (!formData.name || !formData.manufacturerCode) {
    showAlertDialog('厂商名称和厂商编号为必填项')
    return
  }
  if (formMode.value === 'add') {
    const maxId = list.value.reduce((max, i) => Math.max(max, i.id), 0)
    const newItem = { ...formData, id: maxId + 1, createTime: new Date().toISOString().slice(0, 10) }
    list.value.unshift(newItem)
    currentManufacturer.value = newItem
  } else if (formMode.value === 'edit') {
    const idx = list.value.findIndex(i => i.id === currentManufacturer.value.id)
    if (idx !== -1) {
      list.value[idx] = { ...formData, id: currentManufacturer.value.id }
      currentManufacturer.value = list.value[idx]
    }
  }
  formMode.value = 'readonly'
  Object.keys(formData).forEach(k => delete formData[k])
  Object.assign(formData, { ...currentManufacturer.value })
  formatFormDataDates()
}

const deleteRow = async (row) => {
  const ok = await showConfirmDialog(`确定要删除厂商「${row.name}」吗？\n此操作不可恢复。`)
  if (!ok) return
  try {
    await api(`/manufacturers/${row.id}`, { method: 'DELETE' })
    list.value = list.value.filter(i => i.id !== row.id)
    if (currentManufacturer.value?.id === row.id) {
      currentManufacturer.value = null
      Object.keys(formData).forEach(k => delete formData[k])
    }
  } catch (e) {
    console.error('删除厂商失败:', e)
    showAlertDialog('删除失败: ' + (e.message || '未知错误'))
  }
}

const deleteCurrent = () => {
  if (!currentManufacturer.value) return
  deleteRow(currentManufacturer.value)
}

const loadManufacturers = async () => {
  try {
    tableLoading.value = true
    const res = await api(`/manufacturers?current=${currentPage.value}&size=${pageSize.value}&keyword=${encodeURIComponent(searchKeyword.value)}`)
    const data = res.data || res || {}
    list.value = data.records || data.list || data || []
    totalRecords.value = data.total || list.value.length
    nextTick(() => {
      if (list.value.length > 0 && gridRef.value) {
        gridRef.value.setCurrentRow(list.value[0])
        selectManufacturer(list.value[0])
      }
    })
  } catch (e) {
    console.error('加载厂商数据失败:', e)
  } finally {
    tableLoading.value = false
  }
}

const importData = () => {
  openImportModal()
}

const openImportModal = () => {
  importFile.value = null
  showImportModal.value = true
}

const onImportFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  importFile.value = file
  await parseExcelFile(file)
}

const DATE_FIELDS = new Set(['createTime', 'updateTime', 'lastExpiry', 'expiryDate'])

const formatFormDataDates = () => {
  DATE_FIELDS.forEach(key => {
    if (formData[key]) formData[key] = String(formData[key]).replace('T', ' ')
  })
}

const parseDateValue = (val) => {
  if (val == null || val === '') return ''
  const s = String(val).trim()
  if (/^\d{4}[-\/]\d{1,2}[-\/]\d{1,2}/.test(s)) {
    const d = new Date(s)
    if (!isNaN(d.getTime())) return d.toISOString().slice(0, 19).replace('T', ' ')
    }
    if (/^\d{1,2}[\/\-]\d{1,2}[\/\-]\d{2,4}/.test(s)) {
      const d = new Date(s)
      if (!isNaN(d.getTime())) return d.toISOString().slice(0, 19).replace('T', ' ')
    }
    const num = Number(s)
    if (!isNaN(num) && num > 1 && num < 100000) {
      const d = new Date((num - 25569) * 86400000)
      if (!isNaN(d.getTime())) return d.toISOString().slice(0, 19).replace('T', ' ')
  }
  return s
}

const parseExcelFile = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target.result)
        const workbook = XLSX.read(data, { type: 'array' })
        const sheetName = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[sheetName]
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1, raw: false, defval: '' })

        if (jsonData.length === 0) {
          showAlertDialog('Excel 文件为空')
          resolve()
          return
        }

        const headers = jsonData[0].map(h => String(h || '').trim())
        importPreviewHeaders.value = headers

        const rows = []
        for (let i = 1; i < jsonData.length; i++) {
          const rawRow = jsonData[i]
          if (!rawRow || rawRow.every(cell => !cell && cell !== 0)) continue

          const rowObj = { _rowIndex: i, _status: 'pending' }
          headers.forEach((header, idx) => {
            const fieldName = HEADER_TO_FIELD[header]
            if (fieldName) {
              const raw = rawRow[idx] != null ? String(rawRow[idx]).trim() : ''
              rowObj[fieldName] = DATE_FIELDS.has(fieldName) ? parseDateValue(raw) : raw
            }
          })
          rows.push(rowObj)
        }

        importPreviewData.value = rows
        importOriginalData.value = JSON.parse(JSON.stringify(rows))
        importSelectedRows.value = []
        showImportModal.value = false
        showImportPreview.value = true
        resolve()
      } catch (err) {
        console.error('解析 Excel 失败:', err)
        showAlertDialog('解析 Excel 文件失败: ' + err.message)
        reject(err)
      }
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsArrayBuffer(file)
  })
}

const onImportDrop = (e) => {
  const files = e.dataTransfer.files
  if (files.length > 0 && (files[0].name.endsWith('.xlsx') || files[0].name.endsWith('.xls'))) {
    importFile.value = files[0]
  }
}

const onDragOver = (e) => {
  e.currentTarget.classList.add('drag-over')
}

const onDragLeave = (e) => {
  e.currentTarget.classList.remove('drag-over')
}

const restorePreviewRow = (row) => {
  const original = importOriginalData.value.find(r => r._rowIndex === row._rowIndex)
  if (original) {
    Object.assign(row, JSON.parse(JSON.stringify(original)))
  }
}

const deletePreviewRow = (row) => {
  const idx = importPreviewData.value.findIndex(r => r._rowIndex === row._rowIndex)
  if (idx >= 0) {
    importPreviewData.value.splice(idx, 1)
    onImportPreviewCheckChange()
  }
}

const deleteSelectedPreviewRows = () => {
  if (!importPreviewGridRef.value) return
  const selectedRecords = importPreviewGridRef.value.getCheckboxRecords()
  if (selectedRecords.length === 0) return
  const rowIndexes = new Set(selectedRecords.map(r => r._rowIndex))
  importPreviewData.value = importPreviewData.value.filter(r => !rowIndexes.has(r._rowIndex))
  importPreviewGridRef.value.clearCheckboxRow()
  importSelectedRows.value = []
}

const onImportPreviewCheckChange = () => {
  if (importPreviewGridRef.value) {
    importSelectedRows.value = importPreviewGridRef.value.getCheckboxRecords().map(r => r._rowIndex)
  }
}

const exportSelectedRows = () => {
  if (importSelectedRows.value.length === 0) {
    showAlertDialog('请先选择要导出的行')
    return
  }
  showAlertDialog(`已选择 ${importSelectedRows.value.length} 行数据准备导出（功能开发中）`)
}

const cancelImportPreview = () => {
  showImportPreview.value = false
  importPreviewData.value = []
  importPreviewHeaders.value = []
  importSelectedRows.value = []
  importFile.value = null
}

const doConfirmImport = (mode) => {
  if (!importPreviewGridRef.value) return
  const selectedRecords = importPreviewGridRef.value.getCheckboxRecords()
  const count = mode === 'all' ? importPreviewData.value.length : selectedRecords.length
  if (count === 0) {
    showAlertDialog('请至少选择一行数据进行导入')
    return
  }
  importConfirmCount.value = count
  showImportConfirmModal.value = true
}

const buildManufacturersToSend = (records) => {
  return records.map(row => {
    const manufacturer = {}
    Object.keys(HEADER_TO_FIELD).forEach(header => {
      const field = HEADER_TO_FIELD[header]
      if (row[field] !== undefined && row[field] !== '') {
        manufacturer[field] = String(row[field]).trim()
      }
    })
    return manufacturer
  })
}

const BATCH_SIZE = 50

const executeImport = async () => {
  showImportConfirmModal.value = false
  const selectedRecords = importPreviewGridRef.value.getCheckboxRecords()
  const recordsToImport = importConfirmCount.value === importPreviewData.value.length
    ? importPreviewData.value
    : selectedRecords
  const allManufacturers = buildManufacturersToSend(recordsToImport)
  const total = allManufacturers.length

  importUploading.value = true
  importProgress.value = 0
  const isUpdateMode = importUpdateMode.value
  importProgressText.value = `准备${isUpdateMode ? '更新' : '导入'} ${total} 条数据...`

  let totalSuccess = 0
  let totalFail = 0
  let totalDuplicate = 0
  let totalUpdated = 0
  const allFailedRows = []

  try {
    for (let i = 0; i < allManufacturers.length; i += BATCH_SIZE) {
      const batch = allManufacturers.slice(i, i + BATCH_SIZE)
      const batchNum = Math.floor(i / BATCH_SIZE) + 1
      const totalBatches = Math.ceil(allManufacturers.length / BATCH_SIZE)
      importProgressText.value = `正在${isUpdateMode ? '更新' : '导入'}第 ${batchNum}/${totalBatches} 批 (${i + 1}-${Math.min(i + BATCH_SIZE, total)}/${total})...`

      const res = await api(`/manufacturers/batch-import?updateMode=${isUpdateMode}`, {
        method: 'POST',
        body: JSON.stringify(batch),
        headers: { 'Content-Type': 'application/json' }
      })

      if (res.code === 200 || res.success) {
        totalSuccess += (res.data?.successCount || 0)
        totalFail += (res.data?.failCount || 0)
        totalDuplicate += (res.data?.duplicateCount || 0)
        totalUpdated += (res.data?.updatedCount || 0)
        if (res.data?.failedRows) {
          allFailedRows.push(...res.data.failedRows)
        }
      } else {
        totalFail += batch.length
        batch.forEach((m, idx) => {
          allFailedRows.push({
            row: String(i + idx + 1),
            厂商编号: m.manufacturerCode || '',
            厂商名称: m.name || '',
            失败原因: res.message || '服务端返回错误',
            类型: '异常'
          })
        })
      }

      importProgress.value = Math.round(((i + batch.length) / total) * 100)
      if (i + BATCH_SIZE < allManufacturers.length) {
        await new Promise(r => setTimeout(r, 150))
      }
    }

    importProgressText.value = `${isUpdateMode ? '更新' : '导入'}完成！成功 ${totalSuccess} 条${totalUpdated > 0 ? `，更新 ${totalUpdated} 条` : ''}`
    importProgress.value = 100

    batchResult.successCount = totalSuccess
    batchResult.failCount = totalFail
    batchResult.duplicateCount = totalDuplicate
    batchResult.updatedCount = totalUpdated
    batchResult.unmatchedCount = 0
    batchResult.failedRows = allFailedRows
    batchResult.failList = []
    batchResult.unmatchedList = []

    setTimeout(() => {
      showBatchResultModal.value = true
      showImportPreview.value = false
      importPreviewData.value = []
      importSelectedRows.value = []
      importOriginalData.value = []
      importProgress.value = 0
      importProgressText.value = ''
      importUploading.value = false
      loadManufacturers()
    }, 500)
  } catch (e) {
    console.error(e)
    importUploading.value = false
    importProgress.value = 0
    showAlertDialog('导入失败: ' + (e.message || '未知错误'))
  }
}

const exportImportFailedRows = () => {
  const rows = batchResult.failedRows
  if (!rows || rows.length === 0) return
  const headers = ['行号', '厂商编号', '厂商名称', '失败原因', '类型']
  const csvLines = [headers.join(',')]
  rows.forEach(r => {
    const line = [
      `"${r.row || ''}"`,
      `"${(r['厂商编号'] || '').replace(/"/g, '""')}"`,
      `"${(r['厂商名称'] || '').replace(/"/g, '""')}"`,
      `"${(r['失败原因'] || '').replace(/"/g, '""')}"`,
      `"${(r['类型'] || '')}"`
    ].join(',')
    csvLines.push(line)
  })
  const BOM = '\uFEFF'
  const csvContent = BOM + csvLines.join('\n')
  const dataUrl = 'data:text/csv;charset=utf-8,' + encodeURIComponent(csvContent)
  const a = document.createElement('a')
  a.href = dataUrl
  a.download = `导入失败记录_${new Date().toLocaleDateString()}.csv`
  a.click()
}

const downloadTemplate = () => {
  const headers = ['厂商编号', '厂商名称', '摊位号', '摊位类型', '摊位米数', '手机1', '电话1', '联系人1', '见客手机', 'QQ', '手机2', '电话2', '联系人2', '短信号码', '手机3', '电话3', '联系人3', '厂家证书', '楼层区位', '摊位区域', '备注', '上次到期', '到期日期', '登记人', '主卡ID', '副卡ID', '登记日期', '修改日期', '修改人', '电视', '能否发票']
  const ws = XLSX.utils.aoa_to_sheet([headers])
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '厂商资料')
  XLSX.writeFile(wb, '厂商导入模板.xlsx')
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}

const showImportModal = ref(false)
const importFile = ref(null)
const importUploading = ref(false)
const showBatchResultModal = ref(false)
const batchResult = reactive({ successCount: 0, failCount: 0, duplicateCount: 0, updatedCount: 0, unmatchedCount: 0, failedRows: [], failList: [], unmatchedList: [] })

const showImportPreview = ref(false)
const importPreviewData = ref([])
const importOriginalData = ref([])
const importPreviewHeaders = ref([])
const importSelectedRows = ref([])
const importPreviewGridRef = ref(null)

const showImportConfirmModal = ref(false)
const importConfirmCount = ref(0)
const importProgress = ref(0)
const importProgressText = ref('')
const importUpdateMode = ref(false)

const showConfirm = ref(false)
const confirmMessage = ref('')
let confirmResolve = null
const showAlert = ref(false)
const alertMessage = ref('')

const showConfirmDialog = (msg) => {
  return new Promise((resolve) => {
    confirmMessage.value = msg
    confirmResolve = resolve
    showConfirm.value = true
  })
}

const onConfirmOk = () => {
  showConfirm.value = false
  if (confirmResolve) confirmResolve(true)
}

const onConfirmCancel = () => {
  showConfirm.value = false
  if (confirmResolve) confirmResolve(false)
}

const showAlertDialog = (msg) => {
  alertMessage.value = msg
  showAlert.value = true
}

onMounted(() => {
  if (tableWrapRef.value) {
    resizeObserver = new ResizeObserver((entries) => {
      const entry = entries[0]
      if (entry) {
        const h = entry.contentRect.height
        if (h > 0 && Math.abs(h - lastObservedHeight) > 1) {
          lastObservedHeight = h
          if (resizeRafId) cancelAnimationFrame(resizeRafId)
          resizeRafId = requestAnimationFrame(() => { tableWrapHeight.value = h })
        }
      }
    })
    resizeObserver.observe(tableWrapRef.value)
  }
  loadManufacturers()
})

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
})

onActivated(() => {
  requestAnimationFrame(() => {
    nextTick(() => {
      if (tableWrapRef.value) {
        const rect = tableWrapRef.value.getBoundingClientRect()
        if (rect.height > 0) tableWrapHeight.value = rect.height
      }
    })
  })
})
</script>
