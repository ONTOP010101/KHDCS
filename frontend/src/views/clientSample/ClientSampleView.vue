<template>
  <div class="client-sample-page">
    <!-- 顶部数据展示区（只读） -->
    <div class="client-sample-card client-sample-form-card">
      <div class="client-sample-form-top">
        <div class="client-sample-form-actions">
          <button class="client-sample-btn client-sample-btn-ghost" title="展开/收起" @click="formExpanded = !formExpanded">
            <component :is="formExpanded ? ChevronsUp : ChevronsDown" :size="14" />
          </button>
        </div>
      </div>
      <Transition name="form-expand">
        <div v-show="formExpanded" class="client-sample-form-body">
          <div class="client-sample-form-scroll">
            <div class="client-sample-form-grid">
              <div class="client-sample-form-row client-sample-form-row-6">
                <div class="client-sample-form-field-pair">
                  <div class="client-sample-form-field">
                    <label class="client-sample-form-label">本次代号</label>
                    <input class="client-sample-form-input" style="width:380px" :value="currentRecord?.codeName || ''" readonly />
                  </div>
                  <div class="client-sample-form-field">
                    <label class="client-sample-form-label">客户名称</label>
                    <input class="client-sample-form-input" style="width:380px" :value="currentRecord?.clientName || ''" readonly />
                  </div>
                </div>
                <div class="client-sample-form-field">
                  <label class="client-sample-form-label">客户编号</label>
                  <input class="client-sample-form-input" :value="currentRecord?.clientCode || ''" readonly />
                </div>
                <div class="client-sample-form-field">
                  <label class="client-sample-form-label">择样编号</label>
                  <input class="client-sample-form-input" style="width:380px" :value="currentRecord?.selectionId || ''" readonly />
                </div>
                <div class="client-sample-form-field">
                  <label class="client-sample-form-label">录单人员</label>
                  <input class="client-sample-form-input" :value="currentRecord?.recorder || ''" readonly />
                </div>
                <div class="client-sample-form-field client-sample-form-field-expand">
                  <label class="client-sample-form-label">录单日期</label>
                  <input class="client-sample-form-input client-sample-form-input-expand" :value="currentRecord?.recordDate || ''" readonly />
                </div>
              </div>
              <div class="client-sample-form-row client-sample-form-row-6">
                <div class="client-sample-form-field">
                  <label class="client-sample-form-label">择样日期</label>
                  <input class="client-sample-form-input" style="width:380px" :value="currentRecord?.selectionDate || ''" readonly />
                </div>
                <div class="client-sample-form-field">
                  <label class="client-sample-form-label">下单手机</label>
                  <input class="client-sample-form-input" style="width:380px" :value="currentRecord?.orderPhone || ''" readonly />
                </div>
                <div class="client-sample-form-field">
                  <label class="client-sample-form-label">修改人员</label>
                  <input class="client-sample-form-input" :value="currentRecord?.modifier || ''" readonly />
                </div>
                <div class="client-sample-form-field">
                  <label class="client-sample-form-label">修改日期</label>
                  <input class="client-sample-form-input" style="width:380px" :value="currentRecord?.modifyDate || ''" readonly />
                </div>
                <div class="client-sample-form-field">
                  <label class="client-sample-form-label">折扣</label>
                  <span class="client-sample-input-unit">
                    <input class="client-sample-form-input" :value="currentRecord?.discount || ''" readonly />
                    <span class="client-sample-unit">%</span>
                  </span>
                </div>
                <div class="client-sample-form-field client-sample-form-field-expand">
                  <label class="client-sample-form-label">样品/厂商</label>
                  <input class="client-sample-form-input client-sample-form-input-expand" style="text-align:center" :value="(currentRecord?.sampleCount || 0) + '/' + (currentRecord?.manufacturerCount || 0)" readonly />
                </div>
              </div>
              <div class="client-sample-form-row client-sample-form-row-1">
                <div class="client-sample-form-field">
                  <label class="client-sample-form-label">备注</label>
                  <input class="client-sample-form-input" :value="currentRecord?.remark || ''" readonly />
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </div>

    <div class="client-sample-card client-sample-toolbar-card">
      <div class="client-sample-toolbar-row">
          <div class="client-sample-search">
            <Search :size="24" />
            <input v-model="searchKeyword" placeholder="搜索客户名称/择样编号/本次代号/备注" @keydown.enter="onSearch" />
          </div>
          <button class="client-sample-btn client-sample-btn-primary" @click="onSearch">
            <Search :size="22" /> 查询
          </button>
          <button class="client-sample-btn client-sample-btn-ghost" @click="clearSearch">
            <X :size="22" /> 清除
          </button>
          <span class="toolbar-sep"></span>
          <button class="client-sample-btn client-sample-btn-primary" @click="startAdd">
            <Plus :size="22" /> 添加择样
          </button>
          <button class="client-sample-btn client-sample-btn-ghost" :disabled="!currentRecord" @click="startEdit">
            <Pencil :size="22" /> 修改
          </button>
          <button class="client-sample-btn client-sample-btn-danger" :disabled="!currentRecord" @click="deleteCurrent">
            <Trash2 :size="22" /> 删除
          </button>
          <span class="toolbar-sep"></span>
          <SimpleDatePicker class="client-sample-toolbar-date" v-model="dateFrom" placeholder="开始日期" @change="onDateFilter" />
          <span class="client-sample-date-sep">—</span>
          <SimpleDatePicker class="client-sample-toolbar-date" v-model="dateTo" placeholder="结束日期" @change="onDateFilter" />
          <span class="toolbar-sep"></span>
          <button class="client-sample-btn client-sample-btn-ghost" @click="importData">
            <Upload :size="22" /> 导入
          </button>
          <button class="client-sample-btn client-sample-btn-ghost" @click="exportData">
            <Download :size="22" /> 导出
          </button>
        </div>
    </div>

    <div class="client-sample-card client-sample-table-card">
      <div ref="tableWrapRef" class="client-sample-table-wrap">
        <vxe-grid
          v-if="prefReady"
          ref="gridRef"
          :id="gridStorageKey"
          :columns="allColumns"
          :data="filteredTableData"
          :loading="tableLoading"
          :height="tableWrapHeight"
          :toolbar-config="{ custom: true, zoom: true }"
          :custom-config="{ storage: true }"
          :column-config="{ resizable: true, drag: true, isMaximized: true }"
          :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
          :checkbox-config="{ highlight: true, checkField: 'checkbox' }"
          :cell-config="{ height: 80 }"
          :sort-config="{ trigger: 'header', remote: true }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 30, rSize: 80, rHeight: 80 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 24 }, scrollY: { gt: 0, oSize: 30, rSize: 80, rHeight: 80 } }"
          :border="true"
          :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontSize: '30px', fontWeight: 600, textAlign: 'center' }"
          :cell-style="{ textAlign: 'center', fontSize: '26px' }"
          @cell-click="onCellClick"
          @sort-change="onSortChange"
          @checkbox-change="onCheckboxChange"
          @checkbox-all="onCheckboxAll"
          @resizable-change="saveGridPrefs"
          @custom="onCustomChange"
          @column-dragend="onColumnDragEnd"
        >
          <template #col_codeName="{ row }">
            <a
              class="client-sample-code-link"
              @click.stop="openCodeTab(row)"
              :title="`打开客户择样-${row.codeName}`"
            >{{ row.codeName }}</a>
          </template>
          <template #col_actions="{ row }">
            <button class="client-sample-table-action" @click.stop="copyRecord(row)">复制</button>
            <button
              v-if="!lockedMap[row.id]"
              class="client-sample-table-action"
              @click.stop="openLockModal(row)"
            >锁定</button>
            <button
              v-else
              class="client-sample-table-action client-sample-table-action-locked"
              @click.stop="openUnlockModal(row)"
            >解锁</button>
          </template>
        </vxe-grid>
      </div>
      <div class="client-sample-statusbar">
        <div class="client-sample-status-info">
          共 <strong>{{ totalRecords }}</strong> 条
        </div>
        <div class="client-sample-pagination">
          <span class="client-sample-page-size-label">每页</span>
          <select class="client-sample-page-size-select" v-model.number="pageSize">
            <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
          <span class="client-sample-page-size-label">条</span>
          <button class="client-sample-btn client-sample-btn-ghost" :disabled="currentPage <= 1" @click="goPage(1)">
            <ChevronsLeft :size="14" />
          </button>
          <button class="client-sample-btn client-sample-btn-ghost" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">
            <ChevronLeft :size="14" />
          </button>
          <span class="client-sample-page-text">{{ currentPage }} / {{ totalPages }}</span>
          <button class="client-sample-btn client-sample-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">
            <ChevronRight :size="14" />
          </button>
          <button class="client-sample-btn client-sample-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(totalPages)">
            <ChevronsRight :size="14" />
          </button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑表单弹窗 -->
    <Transition name="form-modal-fade">
      <div v-if="showFormModal" class="modal-overlay form-modal-overlay" @click.self="cancelEdit">
        <div class="form-modal" :style="formDrag.active ? {transform: `translate(${formDrag.x}px, ${formDrag.y}px)`} : {}">
          <!-- 头部 -->
          <div class="form-modal-header" @mousedown="startDrag($event, formDrag)" style="cursor:move;user-select:none">
            <div class="form-modal-title-wrap">
              <span class="form-modal-icon">{{ formMode === 'add' ? '+' : '✎' }}</span>
              <h3 class="form-modal-title">{{ formMode === 'add' ? '新增客户择样' : '编辑客户择样' }}</h3>
            </div>
            <button class="form-modal-close" @click="cancelEdit" @mousedown.stop><X :size="18" /></button>
          </div>

          <!-- 表单体 -->
          <div class="form-modal-body">
            <!-- 第一行：代号 + 编号 -->
            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>本次代号</label>
                <input class="fm-input fm-input-auto" :value="formData.codeName" readonly :placeholder="formData.codeName ? '' : '加载中...'" />
              </div>
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>择样编号</label>
                <input class="fm-input fm-input-auto" :value="formData.selectionId" readonly :placeholder="formData.selectionId ? '' : '加载中...'" />
              </div>
            </div>

            <!-- 第二行：客户编号 + 客户名称 -->
            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>客户编号</label>
                <div class="fm-input-with-btn">
                  <input class="fm-input" v-model="formData.clientCode" placeholder="请输入客户编号" style="flex:1" />
                  <button class="fm-input-btn" title="选择客户" @click="openCustomerPicker">选择</button>
                </div>
              </div>
              <div class="fm-field">
                <label class="fm-label">客户名称</label>
                <input class="fm-input" v-model="formData.clientName" placeholder="请输入客户名称" />
              </div>
            </div>

            <!-- 第三行：折扣 + 择样日期 -->
            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label">折扣</label>
                <div class="fm-input-unit">
                  <input class="fm-input" v-model="formData.discount" placeholder="折扣" />
                  <span class="fm-unit">%</span>
                </div>
              </div>
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>择样日期</label>
                <VxeDatePicker class="fm-input" v-model="formData.selectionDate" type="datetime" value-type="string" format="yyyy-MM-dd HH:mm:ss" transfer clearable />
              </div>
            </div>

            <!-- 第四行：录单人员 + 录单日期 -->
            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label">录单人员</label>
                <input class="fm-input" v-model="formData.recorder" readonly />
              </div>
              <div class="fm-field">
                <label class="fm-label">录单日期</label>
                <VxeDatePicker class="fm-input" v-model="formData.recordDate" type="datetime" value-type="string" format="yyyy-MM-dd HH:mm:ss" transfer clearable />
              </div>
            </div>

            <!-- 第五行：修改人员 + 修改日期 -->
            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label">修改人员</label>
                <input class="fm-input" v-model="formData.modifier" readonly placeholder="请输入修改人员" />
              </div>
              <div class="fm-field">
                <label class="fm-label">修改日期</label>
                <VxeDatePicker class="fm-input" v-model="formData.modifyDate" type="datetime" value-type="string" format="yyyy-MM-dd HH:mm:ss" readonly transfer />
              </div>
            </div>

            <!-- 第六行：下单手机（整行） -->
            <div class="fm-row fm-row-1">
              <div class="fm-field">
                <label class="fm-label">下单手机</label>
                <input class="fm-input fm-input-full" v-model="formData.orderPhone" placeholder="请输入下单人手机号" />
              </div>
            </div>

            <!-- 第七行：备注（整行） -->
            <div class="fm-row fm-row-1">
              <div class="fm-field">
                <label class="fm-label">备注</label>
                <textarea class="fm-textarea" v-model="formData.remark" rows="2" placeholder="请输入备注"></textarea>
              </div>
            </div>
          </div>

          <!-- 底部按钮 -->
          <div class="form-modal-footer">
            <button class="client-sample-btn client-sample-btn-ghost" @click="cancelEdit">取消</button>
            <button class="client-sample-btn client-sample-btn-primary" @click="saveClientSample">
              <Save :size="14" /> 确定
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 复制弹窗（独立） -->
    <Transition name="form-modal-fade">
      <div v-if="showCopyModal" class="modal-overlay form-modal-overlay" @click.self="cancelCopy">
        <div class="form-modal" :style="copyDrag.active ? {transform: `translate(${copyDrag.x}px, ${copyDrag.y}px)`} : {}">
          <div class="form-modal-header" @mousedown="startDrag($event, copyDrag)" style="cursor:move;user-select:none">
            <div class="form-modal-title-wrap">
              <span class="form-modal-icon" style="background: linear-gradient(135deg, #34c759, #30d158)">⎘</span>
              <h3 class="form-modal-title">复制客户择样</h3>
            </div>
            <button class="form-modal-close" @click="cancelCopy" @mousedown.stop><X :size="18" /></button>
          </div>

          <div class="form-modal-body">
            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>本次代号</label>
                <input class="fm-input fm-input-auto" :value="copyData.codeName" readonly :placeholder="copyData.codeName ? '' : '加载中...'" />
              </div>
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>择样编号</label>
                <input class="fm-input fm-input-auto" :value="copyData.selectionId" readonly :placeholder="copyData.selectionId ? '' : '加载中...'" />
              </div>
            </div>

            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>客户编号</label>
                <div class="fm-input-with-btn">
                  <input class="fm-input" v-model="copyData.clientCode" placeholder="请输入客户编号" style="flex:1" @blur="onCopyClientCodeBlur" />
                  <button class="fm-input-btn" title="选择客户" @click="openCustomerPickerForCopy">选择</button>
                </div>
              </div>
              <div class="fm-field">
                <label class="fm-label">客户名称</label>
                <input class="fm-input" v-model="copyData.clientName" placeholder="请输入客户名称" />
              </div>
            </div>

            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label">折扣</label>
                <div class="fm-input-unit">
                  <input class="fm-input" v-model="copyData.discount" placeholder="折扣" />
                  <span class="fm-unit">%</span>
                </div>
              </div>
              <div class="fm-field">
                <label class="fm-label"><span class="fm-required">*</span>择样日期</label>
                <VxeDatePicker class="fm-input" v-model="copyData.selectionDate" type="datetime" value-type="string" format="yyyy-MM-dd HH:mm:ss" transfer clearable />
              </div>
            </div>

            <div class="fm-row fm-row-2">
              <div class="fm-field">
                <label class="fm-label">录单人员</label>
                <input class="fm-input" v-model="copyData.recorder" readonly />
              </div>
              <div class="fm-field">
                <label class="fm-label">录单日期</label>
                <VxeDatePicker class="fm-input" v-model="copyData.recordDate" type="datetime" value-type="string" format="yyyy-MM-dd HH:mm:ss" transfer clearable />
              </div>
            </div>

            <div class="fm-row fm-row-1">
              <div class="fm-field">
                <label class="fm-label">下单手机</label>
                <input class="fm-input fm-input-full" v-model="copyData.orderPhone" placeholder="请输入下单人手机号" />
              </div>
            </div>

            <div class="fm-row fm-row-1">
              <div class="fm-field">
                <label class="fm-label">备注</label>
                <textarea class="fm-textarea" v-model="copyData.remark" rows="2" placeholder="请输入备注"></textarea>
              </div>
            </div>

            <div class="fm-row fm-row-2" style="border-top:1px solid rgba(0,122,255,0.08);padding-top:18px">
              <div class="fm-field">
                <label class="fm-label" style="display:flex;align-items:center;gap:10px;cursor:pointer">
                  <input type="checkbox" v-model="copyPrice" style="width:24px;height:24px;cursor:pointer" />
                  复制报价
                </label>
              </div>
              <div class="fm-field">
                <label class="fm-label" style="display:flex;align-items:center;gap:10px;cursor:pointer">
                  <input type="checkbox" v-model="copyItems" style="width:24px;height:24px;cursor:pointer" />
                  复制数据
                </label>
              </div>
            </div>
          </div>

          <div class="form-modal-footer">
            <button class="client-sample-btn client-sample-btn-ghost" @click="cancelCopy">取消</button>
            <button class="client-sample-btn client-sample-btn-primary" @click="saveCopy">
              <Save :size="14" /> 确定
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 导入弹窗 -->
    <div v-if="showImportModal" class="modal-overlay" @click.self="showImportModal = false">
      <div class="modal-dialog" style="max-width:450px">
        <div class="modal-header">
          <strong>导入数据</strong>
          <button class="modal-close-btn" @click="showImportModal = false"><X :size="16" /></button>
        </div>
        <div class="modal-body">
          <p style="font-size:13px;color:#666;margin-bottom:12px">请选择 Excel 文件（.xlsx 或 .xls）</p>
          <input
            type="file"
            accept=".xlsx,.xls"
            @change="onImportFileChange"
            style="display:block;width:100%;padding:6px;border:1px solid #ddd;border-radius:6px;font-size:13px"
          />
          <div style="margin-top:16px;display:flex;justify-content:flex-end;gap:8px">
            <button class="client-sample-btn client-sample-btn-ghost" @click="showImportModal = false">取消</button>
            <button class="client-sample-btn client-sample-btn-primary" :disabled="importUploading || !importFile" @click="doImport">
              {{ importUploading ? '上传中...' : '确定导入' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 导出弹窗 -->
    <div v-if="showExportModal" class="modal-overlay" @click.self="showExportModal = false">
      <div class="modal-dialog" style="max-width:500px">
        <div class="modal-header">
          <strong>导出字段选择</strong>
          <button class="modal-close-btn" @click="showExportModal = false"><X :size="16" /></button>
        </div>
        <div class="modal-body">
          <p style="font-size:13px;color:#666;margin-bottom:8px">已选择 {{ checkedRows.length }} 条数据，请选择导出字段：</p>
          <div style="display:flex;flex-wrap:wrap;gap:8px;margin-bottom:16px">
            <label v-for="f in exportFields" :key="f.field" style="display:flex;align-items:center;gap:4px;font-size:13px;cursor:pointer">
              <input type="checkbox" v-model="f.checked" style="accent-color:#007aff" />
              {{ f.title }}
            </label>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:8px">
            <button class="client-sample-btn client-sample-btn-ghost" @click="showExportModal = false">取消</button>
            <button class="client-sample-btn client-sample-btn-primary" @click="doExport">确认导出</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 客户选择弹窗 -->
    <div v-if="showCustomerModal" class="modal-overlay" style="background:transparent;backdrop-filter:none" @click.self="showCustomerModal = false">
      <div class="modal-dialog" style="max-width:1700px;height:70vh;display:flex;flex-direction:column;border-radius:28px">
        <div class="modal-header">
          <strong style="font-size:22px">选择客户</strong>
          <button class="modal-close-btn" @click="showCustomerModal = false"><X :size="22" /></button>
        </div>
        <div class="modal-body" style="flex:1;overflow:hidden;padding:20px;display:flex;flex-direction:column">
          <div style="display:flex;gap:12px;margin-bottom:14px">
            <input v-model="customerSearch" placeholder="搜索客户编号/名称" style="flex:1;height:60px;padding:0 20px;border:1px solid #e2e8f0;border-radius:16px;font-size:22px;outline:none" @keydown.enter="loadCustomers" />
            <button class="client-sample-btn client-sample-btn-primary" @click="loadCustomers" style="height:60px;font-size:22px;padding:0 28px">查询</button>
          </div>
          <div style="flex:1;min-height:0">
            <vxe-grid
              ref="customerGridRef"
              :columns="customerColumns"
              :data="customerList"
              :loading="customerLoading"
              height="100%"
              :row-config="{ isHover: true, isCurrent: true, keyField: 'id', height: 100 }"
              :header-row-config="{ height: 120 }"
              :column-config="{ resizable: true }"
              :border="true"
              :scroll-x="{ enabled: true, gt: 0 }"
              :header-cell-style="{ background: '#f8fafd', borderColor: '#e0e4ea', color: '#000', fontSize: '18px', fontWeight: 600, textAlign: 'center' }"
              :cell-style="{ fontSize: '18px', textAlign: 'center', lineHeight: '30px' }"
              @cell-click="onCustomerRowClick"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 锁定密码弹窗 -->
    <div v-if="showLockPwdModal" class="modal-overlay" style="background:transparent;backdrop-filter:none">
      <div class="modal-dialog" style="max-width:680px">
        <div class="modal-header">
          <strong style="font-size:24px">{{ lockMode === 'lock' ? '锁定记录' : '解锁记录' }}</strong>
          <button class="modal-close-btn" @click="cancelLockPwd"><X :size="26" /></button>
        </div>
        <div class="modal-body">
          <p style="font-size:22px;color:#666;margin-bottom:14px">
            {{ lockMode === 'lock' ? '请输入密码锁定该记录' : '请输入密码解锁该记录' }}
          </p>
          <input
            type="password"
            v-model="lockPwdInput"
            placeholder="请输入密码"
            style="width:100%;height:60px;padding:0 18px;border:1px solid #e2e8f0;border-radius:14px;font-size:26px;outline:none;box-sizing:border-box"
            @keydown.enter="confirmLockPwd"
          />
          <p v-if="lockPwdError" style="color:#ff3b30;font-size:18px;margin-top:10px">{{ lockPwdError }}</p>
          <div style="margin-top:24px;display:flex;justify-content:flex-end;gap:14px">
            <button class="client-sample-btn client-sample-btn-ghost" @click="cancelLockPwd">取消</button>
            <button class="client-sample-btn client-sample-btn-primary" @click="confirmLockPwd">确定</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 查看密码弹窗（锁定状态点击代号时弹出） -->
    <div v-if="showViewPwdModal" class="modal-overlay" style="background:transparent;backdrop-filter:none">
      <div class="modal-dialog" style="max-width:680px">
        <div class="modal-header">
          <strong style="font-size:24px">验证密码</strong>
          <button class="modal-close-btn" @click="cancelViewPwd"><X :size="26" /></button>
        </div>
        <div class="modal-body">
          <p style="font-size:22px;color:#666;margin-bottom:14px">该记录已锁定，请输入密码查看</p>
          <input
            type="password"
            v-model="viewPwdInput"
            placeholder="请输入密码"
            style="width:100%;height:60px;padding:0 18px;border:1px solid #e2e8f0;border-radius:14px;font-size:26px;outline:none;box-sizing:border-box"
            @keydown.enter="confirmViewPwd"
          />
          <p v-if="viewPwdError" style="color:#ff3b30;font-size:18px;margin-top:10px">{{ viewPwdError }}</p>
          <div style="margin-top:24px;display:flex;justify-content:flex-end;gap:14px">
            <button class="client-sample-btn client-sample-btn-ghost" @click="cancelViewPwd">取消</button>
            <button class="client-sample-btn client-sample-btn-primary" @click="confirmViewPwd">确定</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Alert 弹窗 -->
    <div v-if="showAlert" class="modal-overlay" style="background:transparent;backdrop-filter:none" @click.self="showAlert = false">
      <div class="modal-dialog" style="max-width:520px;border-radius:24px">
        <div class="modal-body" style="text-align:center;padding:44px 36px">
          <p style="font-size:20px;color:#1d1d1f;margin-bottom:28px">{{ alertMessage }}</p>
          <button class="client-sample-btn client-sample-btn-primary" @click="showAlert = false" style="font-size:20px;padding:10px 32px">知道了</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, onActivated, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search, Plus, Pencil, Trash2, Save, X,
  ChevronsUp, ChevronsDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  Upload, Download, Lock, Unlock
} from 'lucide-vue-next'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { useGridPrefSync } from '@/composables/useGridPrefSync'
import SimpleDatePicker from '@/components/SimpleDatePicker.vue'
import '@/styles/client-sample.css'

const auth = useAuth()

function getCurrentUser() {
  return auth.state?.userInfo?.realName || auth.state?.userInfo?.username || ''
}

// ========== 状态 ==========
const formExpanded = ref(true)
const formMode = ref('readonly') // 'readonly' | 'add' | 'edit'
const showFormModal = ref(false)
const formData = reactive({})
// 复制弹窗独立状态
const showCopyModal = ref(false)
const copyData = reactive({})
const copySourceRow = ref(null)
const copyPrice = ref(false)
const copyItems = ref(false)
// 模态框拖动状态
const formDrag = reactive({ x: 0, y: 0, active: false, sx: 0, sy: 0, ox: 0, oy: 0 })
const copyDrag = reactive({ x: 0, y: 0, active: false, sx: 0, sy: 0, ox: 0, oy: 0 })
const currentRecord = ref(null)
const gridRef = ref(null)
const router = useRouter()
const tableWrapRef = ref(null)
const tableWrapHeight = ref(600)
let resizeObserver = null
let resizeRafId = null
let lastObservedHeight = 0

const searchKeyword = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const currentPage = ref(1)
const pageSize = ref(500)
const pageSizeOptions = [100, 200, 500, 1000, 2000]
const totalRecords = ref(0)
const tableLoading = ref(false)

const sortField = ref('')
const sortOrder = ref('')
const checkedRows = ref([])

const list = ref([])

// ========== Alert 弹窗 ==========
const showAlert = ref(false)
const alertMessage = ref('')

function showAlertDialog(msg) {
  alertMessage.value = msg
  showAlert.value = true
}

// ========== 锁定/解锁 ==========
const lockedMap = reactive({})
const showLockPwdModal = ref(false)
const lockMode = ref('lock') // 'lock' | 'unlock'
const lockPwdInput = ref('')
const lockPwdError = ref('')
const pendingLockRow = ref(null)

const showViewPwdModal = ref(false)
const viewPwdInput = ref('')
const viewPwdError = ref('')
const pendingViewRow = ref(null)

function copyRecord(row) {
  copySourceRow.value = row
  Object.keys(copyData).forEach(k => delete copyData[k])
  copyData.clientCode = row.clientCode || ''
  copyData.clientName = row.clientName || ''
  copyData.discount = row.discount || '100'
  copyData.orderPhone = row.orderPhone || ''
  copyData.remark = row.remark || ''
  const now = new Date()
  const fullTime = now.getFullYear() + '-' +
    String(now.getMonth() + 1).padStart(2, '0') + '-' +
    String(now.getDate()).padStart(2, '0') + ' ' +
    String(now.getHours()).padStart(2, '0') + ':' +
    String(now.getMinutes()).padStart(2, '0') + ':' +
    String(now.getSeconds()).padStart(2, '0')
  copyData.recordDate = fullTime
  copyData.selectionDate = fullTime
  copyData.recorder = getCurrentUser()
  showCopyModal.value = true
  fetchCopyCode()
}

// ── 模态框拖动 ──
function startDrag(e, state) {
  state.active = true
  state.sx = e.clientX
  state.sy = e.clientY
  state.ox = state.x
  state.oy = state.y
  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragUp)
}

function onDragMove(e) {
  if (formDrag.active) {
    formDrag.x = formDrag.ox + (e.clientX - formDrag.sx)
    formDrag.y = formDrag.oy + (e.clientY - formDrag.sy)
  }
  if (copyDrag.active) {
    copyDrag.x = copyDrag.ox + (e.clientX - copyDrag.sx)
    copyDrag.y = copyDrag.oy + (e.clientY - copyDrag.sy)
  }
}

function onDragUp() {
  formDrag.active = false
  copyDrag.active = false
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragUp)
}

const cancelCopy = () => {
  showCopyModal.value = false
  copyDrag.x = 0; copyDrag.y = 0
}

const onCopyClientCodeBlur = async () => {
  const code = copyData.clientCode?.trim()
  if (!code) return
  try {
    const res = await api(`/customers?current=1&size=1&keyword=${encodeURIComponent(code)}`)
    const data = res?.data ?? res ?? {}
    const arr = data?.records ?? data?.list
    if (arr && arr.length > 0 && arr[0].customerName) {
      copyData.clientName = arr[0].customerName
    }
  } catch (e) {
    console.error('自动匹配客户名称失败:', e)
  }
}

const fetchCopyCode = async () => {
  try {
    const res = await api('/client-samples/next-code')
    if (res && res.code === 200 && res.data) {
      copyData.codeName = res.data.codeName || ''
      copyData.selectionId = res.data.selectionId || ''
      return
    }
  } catch (e) {
    console.error('获取下一代号失败:', e)
  }
  copyData.codeName = ''
  copyData.selectionId = ''
}

const openCustomerPickerForCopy = () => {
  customerSearch.value = ''
  showCustomerModal.value = true
  loadCustomers()
}

const saveCopy = async () => {
  if (!copyData.clientCode) {
    showAlertDialog('客户编号为必填项')
    return
  }
  if (!copyData.clientName) {
    try {
      const res = await api(`/customers?current=1&size=1&keyword=${encodeURIComponent(copyData.clientCode)}`)
      if (res && res.code === 200) {
        const data = res?.data ?? res ?? {}
        const arr = data?.records ?? data?.list
        if (arr && arr.length > 0 && arr[0].customerName) {
          copyData.clientName = arr[0].customerName
        }
      }
    } catch (e) {
      console.error('自动匹配客户名称失败:', e)
    }
  }
  const payload = { ...copyData }
  delete payload.id
  delete payload.createTime
  delete payload.updateTime
  payload.copyPrice = copyPrice.value
  payload.copyItems = copyItems.value
  if (copySourceRow.value?.id) {
    payload.sourceId = copySourceRow.value.id
  }

  try {
    const res = await api('/client-samples', { method: 'POST', body: JSON.stringify(payload) })
    if (res && res.code === 200) {
      showAlertDialog('复制成功')
      showCopyModal.value = false
      copyDrag.x = 0; copyDrag.y = 0
      await loadData()
    } else {
      showAlertDialog(res?.message || '复制失败')
    }
  } catch (e) {
    console.error('复制失败:', e)
    showAlertDialog('复制失败: ' + (e.message || '网络错误'))
  }
}

function openLockModal(row) {
  pendingLockRow.value = row
  lockMode.value = 'lock'
  lockPwdInput.value = ''
  lockPwdError.value = ''
  showLockPwdModal.value = true
}

function openUnlockModal(row) {
  pendingLockRow.value = row
  lockMode.value = 'unlock'
  lockPwdInput.value = ''
  lockPwdError.value = ''
  showLockPwdModal.value = true
}

function cancelLockPwd() {
  showLockPwdModal.value = false
  pendingLockRow.value = null
}

function confirmLockPwd() {
  if (!lockPwdInput.value) {
    lockPwdError.value = '密码不能为空'
    return
  }
  if (lockPwdInput.value.length < 4) {
    lockPwdError.value = '密码至少4位'
    return
  }
  if (lockMode.value === 'lock') {
    lockedMap[pendingLockRow.value.id] = lockPwdInput.value
  } else {
    if (lockedMap[pendingLockRow.value.id] !== lockPwdInput.value) {
      lockPwdError.value = '密码错误'
      return
    }
    delete lockedMap[pendingLockRow.value.id]
  }
  showLockPwdModal.value = false
  pendingLockRow.value = null
}

function cancelViewPwd() {
  showViewPwdModal.value = false
  pendingViewRow.value = null
  viewPwdError.value = ''
}

function confirmViewPwd() {
  if (!viewPwdInput.value) {
    viewPwdError.value = '密码不能为空'
    return
  }
  if (viewPwdInput.value !== lockedMap[pendingViewRow.value.id]) {
    viewPwdError.value = '密码错误'
    return
  }
  showViewPwdModal.value = false
  // 密码正确，执行跳转
  if (pendingViewRow.value) {
    router.push({ name: 'ClientSampleCode', params: { codeName: pendingViewRow.value.codeName } })
  }
  pendingViewRow.value = null
  viewPwdError.value = ''
}

function openCodeTab(row) {
  if (!row || !row.codeName) return
  // 检查是否锁定
  if (lockedMap[row.id]) {
    pendingViewRow.value = row
    viewPwdInput.value = ''
    viewPwdError.value = ''
    showViewPwdModal.value = true
    return
  }
  router.push({ name: 'ClientSampleCode', params: { codeName: row.codeName } })
}

// ========== 客户选择弹窗 ==========
const showCustomerModal = ref(false)
const customerList = ref([])
const customerLoading = ref(false)
const customerSearch = ref('')
const customerGridRef = ref(null)

const customerColumns = [
  { type: 'seq', title: '序号', width: 100, fixed: 'left' },
  { field: 'customerCode', title: '客户编号', width: 300 },
  { field: 'customerName', title: '客户名称', width: 400 },
  { field: 'country', title: '国家', width: 220 },
  { field: 'address', title: '地址', width: 500 },
  { field: 'contactPerson1', title: '联系人', width: 250 },
  { field: 'mobile1', title: '手机', width: 250 }
]

const openCustomerPicker = () => {
  customerSearch.value = ''
  showCustomerModal.value = true
  loadCustomers()
}

const loadCustomers = async () => {
  try {
    customerLoading.value = true
    const params = ['current=1', 'size=200']
    if (customerSearch.value) params.push(`keyword=${encodeURIComponent(customerSearch.value)}`)
    const res = await api(`/customers?${params.join('&')}`)
    const data = res?.data ?? res ?? {}
    const arr = data?.records ?? data?.list
    customerList.value = Array.isArray(arr) ? arr : (Array.isArray(data) ? data : [])
  } catch (e) {
    console.error('加载客户列表失败:', e)
    customerList.value = []
  } finally {
    customerLoading.value = false
  }
}

const onCustomerRowClick = ({ row }) => {
  if (!row) return
  if (showCopyModal.value) {
    copyData.clientCode = row.customerCode || ''
    copyData.clientName = row.customerName || ''
  } else {
    formData.clientCode = row.customerCode || ''
    formData.clientName = row.customerName || ''
  }
  showCustomerModal.value = false
}

// ========== 导入 ==========
const showImportModal = ref(false)
const importFile = ref(null)
const importUploading = ref(false)

// ========== 导出 ==========
const showExportModal = ref(false)
const exportFields = ref([
  { field: 'codeName', title: '本次代号', checked: true },
  { field: 'selectionId', title: '择样编号', checked: true },
  { field: 'clientCode', title: '客户编号', checked: true },
  { field: 'clientName', title: '客户名称', checked: true },
  { field: 'selectionDate', title: '择样日期', checked: true },
  { field: 'orderPhone', title: '下单人手机', checked: true },
  { field: 'recorder', title: '录单人员', checked: true },
  { field: 'recordDate', title: '录单日期', checked: true },
  { field: 'modifier', title: '修改人员', checked: true },
  { field: 'modifyDate', title: '修改日期', checked: true },
  { field: 'remark', title: '备注', checked: true },
  { field: 'discount', title: '折扣', checked: true }
])

const totalPages = computed(() => Math.max(1, Math.ceil(totalRecords.value / pageSize.value)))

const filteredTableData = computed(() => list.value)

const isEditing = computed(() => formMode.value === 'add' || formMode.value === 'edit')

const allColumns = [
  { type: 'checkbox', title: '', width: 50, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'codeName', title: '本次代号', width: 160, showOverflow: true, sortable: true, slots: { default: 'col_codeName' } },
  { field: 'selectionId', title: '择样编号', width: 160, showOverflow: true },
  { field: 'clientCode', title: '客户编号', width: 150, showOverflow: true },
  { field: 'clientName', title: '客户名称', width: 200, showOverflow: true, sortable: true },
  { field: 'selectionDate', title: '择样日期', width: 160, sortable: true },
  { field: 'orderPhone', title: '下单人手机', width: 160 },
  { field: 'recorder', title: '录单人员', width: 110 },
  { field: 'recordDate', title: '录单日期', width: 160, sortable: true },
  { field: 'modifier', title: '修改人员', width: 110 },
  { field: 'modifyDate', title: '修改日期', width: 160, sortable: true },
  { field: 'remark', title: '备注', width: 360, showOverflow: true },
  { field: 'discount', title: '折扣', width: 100 },
  { field: 'actions', title: '操作', width: 140, fixed: 'right', slots: { default: 'col_actions' } }
]

// 表格列设置跨设备同步
const { fullKey: gridStorageKey, saveToBackend: saveGridPrefs, ready: prefReady } = useGridPrefSync(gridRef, 'client-sample', allColumns)

// ========== CRUD 操作 ==========
const onSearch = () => {
  currentPage.value = 1
  loadData()
}

const clearSearch = () => {
  searchKeyword.value = ''
  dateFrom.value = ''
  dateTo.value = ''
  currentPage.value = 1
  loadData()
}

const onDateFilter = () => {
  currentPage.value = 1
  loadData()
}

watch(pageSize, () => {
  currentPage.value = 1
  loadData()
})

const goPage = (page) => {
  currentPage.value = page
  loadData()
}

const onCellClick = ({ row }) => {
  if (isEditing.value) return
  selectRecord(row)
}

const onCheckboxChange = ({ records }) => {
  checkedRows.value = records
}

const onCheckboxAll = ({ records }) => {
  checkedRows.value = records
}

const onSortChange = ({ field, order }) => {
  sortField.value = field || ''
  sortOrder.value = order || ''
  currentPage.value = 1
  loadData()
}

const onCustomChange = ({ type }) => {
  if (type === 'confirm' || type === 'reset') {
    setTimeout(() => saveGridPrefs(), 50)
  }
}

const onColumnDragEnd = () => {
  setTimeout(() => saveGridPrefs(), 100)
}

const selectRecord = (row) => {
  currentRecord.value = row
  Object.keys(formData).forEach(k => delete formData[k])
  if (row) {
    Object.assign(formData, { ...row })
    formatFormDataDates()
  }
}

const resetForm = () => {
  if (currentRecord.value) {
    Object.keys(formData).forEach(k => delete formData[k])
    Object.assign(formData, { ...currentRecord.value })
    formatFormDataDates()
  }
}

const startAdd = () => {
  formMode.value = 'add'
  currentRecord.value = null
  Object.keys(formData).forEach(k => delete formData[k])
  formData.discount = '100'
  formData.remark = ''
  const now = new Date()
    const fullTime = now.getFullYear() + '-' +
      String(now.getMonth() + 1).padStart(2, '0') + '-' +
      String(now.getDate()).padStart(2, '0') + ' ' +
      String(now.getHours()).padStart(2, '0') + ':' +
      String(now.getMinutes()).padStart(2, '0') + ':' +
      String(now.getSeconds()).padStart(2, '0')
    formData.recordDate = fullTime
    formData.selectionDate = fullTime
  formData.recorder = getCurrentUser()
  showFormModal.value = true
  fetchNextCode()
}

const fetchNextCode = async () => {
  try {
    const res = await api('/client-samples/next-code')
    if (res && res.code === 200 && res.data) {
      formData.codeName = res.data.codeName || ''
      formData.selectionId = res.data.selectionId || ''
      return
    }
  } catch (e) {
    console.error('获取下一代号失败:', e)
  }
  // 后端不可用时留空，由保存时后端生成
  formData.codeName = ''
  formData.selectionId = ''
}

const startEdit = () => {
  if (!currentRecord.value) {
    showAlertDialog('请先在表格中选择一条记录')
    return
  }
  formMode.value = 'edit'
  Object.keys(formData).forEach(k => delete formData[k])
  Object.assign(formData, { ...currentRecord.value })
  formatFormDataDates()
  showFormModal.value = true
}

const cancelEdit = () => {
  showFormModal.value = false
  formDrag.x = 0; formDrag.y = 0
  formMode.value = 'readonly'
  if (currentRecord.value) {
    Object.keys(formData).forEach(k => delete formData[k])
    Object.assign(formData, { ...currentRecord.value })
    formatFormDataDates()
  } else {
    Object.keys(formData).forEach(k => delete formData[k])
  }
}

const saveClientSample = async () => {
  if (!formData.clientCode) {
    showAlertDialog('客户编号为必填项')
    return
  }
  // 如果没填客户名称，自动根据客户编号匹配
  if (!formData.clientName) {
    try {
      const res = await api(`/customers?current=1&size=1&keyword=${encodeURIComponent(formData.clientCode)}`)
      if (res && res.code === 200) {
        const data = res?.data ?? res ?? {}
        const arr = data?.records ?? data?.list
        if (arr && arr.length > 0 && arr[0].customerName) {
          formData.clientName = arr[0].customerName
        }
      }
    } catch (e) {
      console.error('自动匹配客户名称失败:', e)
    }
  }
  const payload = { ...formData }
  delete payload.id
  delete payload.createTime
  delete payload.updateTime

  try {
    if (formMode.value === 'add') {
      const res = await api('/client-samples', { method: 'POST', body: JSON.stringify(payload) })
      if (res && res.code === 200) {
        showAlertDialog('添加成功')
        showFormModal.value = false
        formDrag.x = 0; formDrag.y = 0
        formMode.value = 'readonly'
        currentRecord.value = res.data || null
        await loadData()
      } else {
        showAlertDialog(res?.message || '添加失败')
      }
    } else {
      const id = currentRecord.value?.id
      if (!id) { showAlertDialog('未选择记录'); return }
      const res = await api(`/client-samples/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
      if (res && res.code === 200) {
        showAlertDialog('修改成功')
        showFormModal.value = false
        formMode.value = 'readonly'
        await loadData()
      } else {
        showAlertDialog(res?.message || '修改失败')
      }
    }
  } catch (e) {
    console.error('操作失败:', e)
    showAlertDialog('操作失败: ' + (e.message || '网络错误'))
  }
}

const deleteCurrent = async () => {
  if (!currentRecord.value) {
    showAlertDialog('请先在表格中选择一条记录')
    return
  }
  if (!confirm(`确定要删除该择样记录吗？\n本次代号: ${currentRecord.value.codeName}\n此操作不可恢复。`)) return
  try {
    const res = await api(`/client-samples/${currentRecord.value.id}`, { method: 'DELETE' })
    if (res && res.code === 200) {
      showAlertDialog('删除成功')
      currentRecord.value = null
      Object.keys(formData).forEach(k => delete formData[k])
      await loadData()
    } else {
      showAlertDialog(res?.message || '删除失败')
    }
  } catch (e) {
    console.error('删除失败:', e)
    showAlertDialog('删除失败: ' + (e.message || '网络错误'))
  }
}

// ========== 导入 ==========
const importData = () => {
  importFile.value = null
  showImportModal.value = true
}

const onImportFileChange = (e) => {
  importFile.value = e.target.files[0] || null
}

const doImport = async () => {
  if (!importFile.value) {
    showAlertDialog('请选择文件')
    return
  }
  importUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', importFile.value)
    const res = await api('/client-samples/import', { method: 'POST', body: fd })
    if (res && res.code === 200) {
      showAlertDialog(res.message || '导入成功')
      showImportModal.value = false
      importFile.value = null
      await loadData()
    } else {
      showAlertDialog(res?.message || '导入失败')
    }
  } catch (e) {
    console.error('导入失败:', e)
    showAlertDialog('导入失败: ' + (e.message || '网络错误'))
  } finally {
    importUploading.value = false
  }
}

// ========== 导出 ==========
const exportData = () => {
  const checked = checkedRows.value
  if (checked.length === 0) {
    showAlertDialog('请先在表格中勾选要导出的数据')
    return
  }
  showExportModal.value = true
}

const doExport = async () => {
  const fields = exportFields.value.filter(f => f.checked).map(f => f.field)
  if (fields.length === 0) {
    showAlertDialog('请至少选择一个导出字段')
    return
  }
  const ids = checkedRows.value.map(r => r.id).join(',')
  try {
    const url = `${window.electronAPI ? 'http://localhost:8080' : ''}/client-samples/export?ids=${ids}&fields=${fields.join(',')}`
    const token = sessionStorage.getItem('token') || localStorage.getItem('token')
    const res = await fetch(url, {
      headers: token ? { Authorization: 'Bearer ' + token } : {}
    })
    if (!res.ok) {
      showAlertDialog('导出失败')
      return
    }
    const blob = await res.blob()
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `客户择样_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    URL.revokeObjectURL(a.href)
    showExportModal.value = false
  } catch (e) {
    console.error('导出失败:', e)
    showAlertDialog('导出失败: ' + (e.message || '网络错误'))
  }
}

// ========== 数据加载 ==========
const DATE_FIELDS = new Set(['selectionDate', 'recordDate', 'modifyDate'])

const formatFormDataDates = () => {
  DATE_FIELDS.forEach(key => {
    if (formData[key]) formData[key] = String(formData[key]).replace('T', ' ')
  })
}

const loadData = async () => {
  try {
    tableLoading.value = true
    const params = [`current=${currentPage.value}`, `size=${pageSize.value}`]
    if (sortField.value) { params.push(`sortField=${sortField.value}`); params.push(`sortOrder=${sortOrder.value}`) }
    if (searchKeyword.value) params.push(`keyword=${encodeURIComponent(searchKeyword.value)}`)
    if (dateFrom.value) params.push(`dateFrom=${encodeURIComponent(dateFrom.value)}`)
    if (dateTo.value) params.push(`dateTo=${encodeURIComponent(dateTo.value)}`)
    const res = await api(`/client-samples?${params.join('&')}`)
    const result = res.data || res || {}
    const records = result.records || result.list
    const rawList = Array.isArray(records) ? records : (Array.isArray(result) ? result : [])
    // 把日期字段中的 T 转回空格
    rawList.forEach(row => {
      DATE_FIELDS.forEach(key => {
        if (row[key]) row[key] = String(row[key]).replace('T', ' ')
      })
    })
    list.value = rawList
    totalRecords.value = result.total || list.value.length
  } catch (e) {
    console.error('加载数据失败:', e)
    list.value = []
    totalRecords.value = 0
  } finally {
    tableLoading.value = false
    nextTick(() => {
      if (list.value.length > 0 && gridRef.value) {
        gridRef.value.setCurrentRow(list.value[0])
        selectRecord(list.value[0])
      }
    })
  }
}

// ========== 生命周期 ==========
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
  loadData()
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
