<template>
  <div class="customer-info-page">
    <div class="customer-info-card customer-info-form-card" :class="{ expanded: formExpanded }" v-show="formVisible">
      <div class="customer-info-form-top">
        <div class="customer-info-form-actions">
          <button class="customer-info-btn customer-info-btn-ghost" title="重置" @click="resetForm">
            <RotateCcw :size="14" />
          </button>
          <button class="customer-info-btn customer-info-btn-ghost" title="展开/收起" @click="formExpanded = !formExpanded">
            <component :is="formExpanded ? ChevronsUp : ChevronsDown" :size="14" />
          </button>
          <button class="customer-info-btn customer-info-btn-ghost" :title="formVisible ? '隐藏展示区' : '显示展示区'" @click="formVisible = !formVisible">
            <EyeOff v-if="formVisible" :size="14" />
            <Eye v-else :size="14" />
          </button>
        </div>
      </div>

      <div class="customer-info-form-body">
        <div class="customer-info-form-scroll">
          <!-- 第一行 -->
          <div class="customer-info-form-row customer-info-form-row-4">
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">客户编号</label>
              <input class="customer-info-form-input" style="width: 330px" v-model="formData.customerCode" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">客户名称</label>
              <input class="customer-info-form-input" style="width: 480px" v-model="formData.customerName" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">国家</label>
              <input class="customer-info-form-input" style="width: 380px" v-model="formData.country" readonly />
            </div>
            <div class="customer-info-form-field" style="flex: 1 1 auto">
              <label class="customer-info-form-label">地址</label>
              <input class="customer-info-form-input" style="flex: 1 1 auto; min-width: 120px; width: auto" v-model="formData.address" readonly />
            </div>
          </div>
          <!-- 第二行 -->
          <div class="customer-info-form-row customer-info-form-row-4">
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">联系人1</label>
              <input class="customer-info-form-input" style="width: 330px" v-model="formData.contactPerson1" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">手机1</label>
              <input class="customer-info-form-input" style="width: 480px" v-model="formData.mobile1" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">电话1</label>
              <input class="customer-info-form-input" style="width: 380px" v-model="formData.phone1" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">邮箱</label>
              <input class="customer-info-form-input" style="width: 380px" v-model="formData.email" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">QQ</label>
              <input class="customer-info-form-input" style="width: 380px" v-model="formData.qq" readonly />
            </div>
            <div class="customer-info-form-field" style="flex: 1 1 auto">
              <label class="customer-info-form-label">短信号码</label>
              <input class="customer-info-form-input" style="flex: 1 1 auto; min-width: 120px; width: auto" v-model="formData.smsNumber" readonly />
            </div>
          </div>
          <!-- 第三行 -->
          <div class="customer-info-form-row customer-info-form-row-4" v-show="formExpanded">
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">联系人2</label>
              <input class="customer-info-form-input" style="width: 330px" v-model="formData.contactPerson2" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">手机2</label>
              <input class="customer-info-form-input" style="width: 480px" v-model="formData.mobile2" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">电话2</label>
              <input class="customer-info-form-input" style="width: 380px" v-model="formData.phone2" readonly />
            </div>
            <div class="customer-info-form-field" style="flex: 1 1 auto">
              <label class="customer-info-form-label">备注1</label>
              <input class="customer-info-form-input" style="flex: 1 1 auto; min-width: 120px; width: auto" v-model="formData.remark1" readonly />
            </div>
          </div>
          <!-- 第四行 -->
          <div class="customer-info-form-row customer-info-form-row-4" v-show="formExpanded">
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">修改日期</label>
              <input class="customer-info-form-input" style="width: 330px" :value="formData.modifyDate || ''" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">手机3</label>
              <input class="customer-info-form-input" style="width: 480px" v-model="formData.mobile3" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">电话3</label>
              <input class="customer-info-form-input" style="width: 380px" v-model="formData.phone3" readonly />
            </div>
            <div class="customer-info-form-field" style="flex: 1 1 auto">
              <label class="customer-info-form-label">备注2</label>
              <input class="customer-info-form-input" style="flex: 1 1 auto; min-width: 120px; width: auto" v-model="formData.remark2" readonly />
            </div>
          </div>
          <!-- 第五行 -->
          <div class="customer-info-form-row customer-info-form-row-4" v-show="formExpanded">
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">登记日期</label>
              <input class="customer-info-form-input" style="width: 330px" :value="formData.registerDate || ''" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">登记人</label>
              <input class="customer-info-form-input" v-model="formData.registrant" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">修改人</label>
              <input class="customer-info-form-input" style="width: 170px" v-model="formData.modifier" readonly />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">地区</label>
              <input class="customer-info-form-input" style="width: 380px" v-model="formData.region" readonly />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="customer-info-card customer-info-toolbar-card">
      <div class="customer-info-toolbar-row">
        <div class="customer-info-search">
          <Search :size="16" />
          <input v-model="searchKeyword" placeholder="搜索客户名称/客户编号/短信号码" @keydown.enter="onSearch" />
        </div>
        <button class="customer-info-btn customer-info-btn-primary" @click="onSearch">
          <Search :size="14" /> 查询
        </button>
        <button class="customer-info-btn customer-info-btn-ghost" @click="clearSearch">
          <X :size="14" /> 清除
        </button>
        <span class="toolbar-sep"></span>
        <button class="customer-info-btn customer-info-btn-primary" @click="startAdd">
          <Plus :size="14" /> 添加客户
        </button>
        <button class="customer-info-btn customer-info-btn-ghost" :disabled="!currentRecord" @click="openEditModal">
          <Pencil :size="14" /> 修改
        </button>
        <button class="customer-info-btn customer-info-btn-danger" :disabled="!currentRecord" @click="deleteCurrent">
          <Trash2 :size="14" /> 删除
        </button>
        <button v-if="currentRecord" class="customer-info-btn customer-info-btn-ghost customer-info-upload-toolbar-btn" @click="$refs.certFileInput?.click()">
          <Upload :size="14" /> 上传营业执照
        </button>
        <input ref="certFileInput" type="file" accept="image/*" hidden @change="onCertificateUpload" />
        <button class="customer-info-btn customer-info-btn-ghost" @click="importData">
          <Upload :size="14" /> 导入
        </button>
        <button class="customer-info-btn customer-info-btn-ghost" @click="exportData">
          <Download :size="14" /> 导出
        </button>
        <button v-if="!formVisible" class="customer-info-btn customer-info-btn-primary" style="flex-shrink:0;margin-left:auto" @click="formVisible = true" title="显示展示区">
          <Eye :size="14" /> 显示
        </button>
      </div>
    </div>

    <div class="customer-info-card customer-info-table-card">
      <div ref="tableWrapRef" class="customer-info-table-wrap">
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
          :cell-config="{ height: 120 }"
          :sort-config="{ trigger: 'header', remote: true }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 0, rSize: 120, rHeight: 120 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 24 }, scrollY: { gt: 0, oSize: 0, rSize: 120, rHeight: 120 } }"
          :border="true"
          :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontWeight: 600, fontSize: '30px', textAlign: 'center' }"
          :cell-style="{ textAlign: 'center', fontSize: '26px' }"
          @cell-click="onCellClick"
          @sort-change="onSortChange"
          @resizable-change="saveGridPrefs"
          @custom="onCustomChange"
          @column-dragend="onColumnDragEnd"
        >
          <template #col_customerCode="{ row }">
            <span style="color:#0066cc;cursor:pointer;text-decoration:underline;font-weight:600;">{{ row.customerCode }}</span>
          </template>
          <template #col_certificate="{ row }">
            <template v-if="row.certificate">
              <img
                :src="`/customers/${row.id}/certificate/image?t=${row.certificate || Date.now()}`"
                style="width: 100px; height: 100px; object-fit: cover; border-radius: 6px; cursor: pointer;"
                @click.stop="viewCertificate(row)"
                @error="e => e.target.style.display = 'none'"
              />
            </template>
            <span v-else style="font-size: 11px; color: rgba(29,29,31,0.36);">暂无</span>
          </template>
        </vxe-grid>
      </div>
      <div class="customer-info-statusbar">
        <div class="customer-info-status-info">
          共 <strong>{{ totalRecords }}</strong> 条
        </div>
        <div class="customer-info-pagination">
          <span class="customer-info-page-size-label">每页</span>
          <select class="customer-info-page-size-select" v-model.number="pageSize">
            <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
          <span class="customer-info-page-size-label">条</span>
          <button class="customer-info-btn customer-info-btn-ghost" :disabled="currentPage <= 1" @click="goPage(1)">
            <ChevronsLeft :size="14" />
          </button>
          <button class="customer-info-btn customer-info-btn-ghost" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">
            <ChevronLeft :size="14" />
          </button>
          <span class="customer-info-page-text">{{ currentPage }} / {{ totalPages }}</span>
          <button class="customer-info-btn customer-info-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">
            <ChevronRight :size="14" />
          </button>
          <button class="customer-info-btn customer-info-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(totalPages)">
            <ChevronsRight :size="14" />
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 自定义提示弹窗 -->
  <Teleport to="body">
    <div v-if="showAlert" class="customer-info-alert-overlay" @click.self="showAlert = false">
      <div class="customer-info-alert-modal">
        <div class="customer-info-alert-header">
          <strong>系统提示</strong>
          <button class="customer-info-alert-close" @click="showAlert = false"><X :size="16" /></button>
        </div>
        <div class="customer-info-alert-body">
          <Info :size="52" style="color:#007aff;margin-bottom:12px" />
          <p style="font-size:22px;color:#1d1d1f;line-height:1.6;white-space:pre-wrap">{{ alertMessage }}</p>
        </div>
        <div class="customer-info-alert-footer">
          <button class="customer-info-btn customer-info-btn-primary" @click="showAlert = false">知道了</button>
        </div>
      </div>
    </div>
  </Teleport>
  <!-- 添加客户模态框 -->
  <Teleport to="body">
    <div v-if="showAddModal" class="customer-info-add-overlay">
      <div class="customer-info-add-modal">
        <div class="customer-info-alert-header">
          <strong>添加客户</strong>
          <button class="customer-info-alert-close" @click="closeAddModal"><X :size="16" /></button>
        </div>
        <div class="customer-info-add-body" @keydown.enter="saveAddCustomer" @keydown.escape="closeAddModal">
          <div class="customer-info-add-grid">
            <div class="customer-info-form-field" style="grid-column: span 2">
              <label class="customer-info-form-label">客户名称</label>
              <input class="customer-info-form-input" style="flex: 1; width: auto" v-model="addFormData.customerName" placeholder="必填" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">国家</label>
              <input class="customer-info-form-input" v-model="addFormData.country" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">地区</label>
              <input class="customer-info-form-input" v-model="addFormData.region" />
            </div>
            <div class="customer-info-form-field" style="grid-column: 1 / -1">
              <label class="customer-info-form-label">地址</label>
              <input class="customer-info-form-input" style="flex: 1; width: auto" v-model="addFormData.address" />
            </div>

            <div class="customer-info-form-field">
              <label class="customer-info-form-label">联系人1</label>
              <input class="customer-info-form-input" v-model="addFormData.contactPerson1" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">手机1</label>
              <input class="customer-info-form-input" v-model="addFormData.mobile1" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">电话1</label>
              <input class="customer-info-form-input" v-model="addFormData.phone1" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">邮箱</label>
              <input class="customer-info-form-input" v-model="addFormData.email" />
            </div>

            <div class="customer-info-form-field">
              <label class="customer-info-form-label">联系人2</label>
              <input class="customer-info-form-input" v-model="addFormData.contactPerson2" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">手机2</label>
              <input class="customer-info-form-input" v-model="addFormData.mobile2" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">电话2</label>
              <input class="customer-info-form-input" v-model="addFormData.phone2" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">QQ</label>
              <input class="customer-info-form-input" v-model="addFormData.qq" />
            </div>

            <div class="customer-info-form-field">
              <label class="customer-info-form-label">联系人3</label>
              <input class="customer-info-form-input" v-model="addFormData.contactPerson3" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">手机3</label>
              <input class="customer-info-form-input" v-model="addFormData.mobile3" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">电话3</label>
              <input class="customer-info-form-input" v-model="addFormData.phone3" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">短信号码</label>
              <input class="customer-info-form-input" v-model="addFormData.smsNumber" />
            </div>

            <div class="customer-info-form-field" style="grid-column: 1 / -1">
              <label class="customer-info-form-label">备注1</label>
              <input class="customer-info-form-input" style="flex: 1; width: auto" v-model="addFormData.remark1" />
            </div>
            <div class="customer-info-form-field" style="grid-column: 1 / -1">
              <label class="customer-info-form-label">备注2</label>
              <input class="customer-info-form-input" style="flex: 1; width: auto" v-model="addFormData.remark2" />
            </div>

            <div style="grid-column: 1 / -1; margin-top: 12px;">
              <label class="customer-info-form-label" style="margin-bottom: 20px;">营业执照</label>
              <div
                class="customer-info-add-upload-zone"
                :class="{ 'customer-info-add-upload-zone--over': certDragOver, 'customer-info-add-upload-zone--has-file': addCertFile }"
                @dragover.prevent="certDragOver = true"
                @dragleave.prevent="certDragOver = false"
                @drop.prevent="onCertDrop"
                @click="$refs.addCertInput?.click()"
              >
                <template v-if="addCertFile">
                  <img v-if="addCertPreview" :src="addCertPreview" class="customer-info-add-upload-preview" />
                  <div class="customer-info-add-upload-file-info">
                    <span class="customer-info-add-upload-file-name">{{ addCertFile.name }}</span>
                    <button class="customer-info-add-upload-remove" @click.stop="removeAddCert">✕</button>
                  </div>
                </template>
                <template v-else>
                  <Upload :size="28" style="color: rgba(0,122,255,0.5);" />
                  <span style="font-size: 18px; color: rgba(29,29,31,0.45);">拖拽图片到此处，或点击选择</span>
                </template>
              </div>
              <input ref="addCertInput" type="file" accept="image/*" hidden @change="onAddCertSelect" />
            </div>
          </div>
        </div>
        <div class="customer-info-add-footer">
          <button class="customer-info-btn customer-info-btn-primary" @click="saveAddCustomer">
            <Save :size="14" /> 保存
          </button>
          <button class="customer-info-btn customer-info-btn-ghost" @click="closeAddModal">
            <X :size="14" /> 取消
          </button>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- 编辑客户模态框 -->
  <Teleport to="body">
    <div v-if="showEditModal" class="customer-info-add-overlay">
      <div class="customer-info-add-modal">
        <div class="customer-info-alert-header">
          <strong>编辑客户</strong>
          <button class="customer-info-alert-close" @click="closeEditModal"><X :size="16" /></button>
        </div>
        <div class="customer-info-add-body" @keydown.enter="saveEditCustomer" @keydown.escape="closeEditModal">
          <div class="customer-info-add-grid">
            <div class="customer-info-form-field" style="grid-column: span 2">
              <label class="customer-info-form-label">客户名称</label>
              <input class="customer-info-form-input" style="flex: 1; width: auto" v-model="editFormData.customerName" placeholder="必填" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">国家</label>
              <input class="customer-info-form-input" v-model="editFormData.country" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">地区</label>
              <input class="customer-info-form-input" v-model="editFormData.region" />
            </div>
            <div class="customer-info-form-field" style="grid-column: 1 / -1">
              <label class="customer-info-form-label">地址</label>
              <input class="customer-info-form-input" style="flex: 1; width: auto" v-model="editFormData.address" />
            </div>

            <div class="customer-info-form-field">
              <label class="customer-info-form-label">联系人1</label>
              <input class="customer-info-form-input" v-model="editFormData.contactPerson1" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">手机1</label>
              <input class="customer-info-form-input" v-model="editFormData.mobile1" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">电话1</label>
              <input class="customer-info-form-input" v-model="editFormData.phone1" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">邮箱</label>
              <input class="customer-info-form-input" v-model="editFormData.email" />
            </div>

            <div class="customer-info-form-field">
              <label class="customer-info-form-label">联系人2</label>
              <input class="customer-info-form-input" v-model="editFormData.contactPerson2" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">手机2</label>
              <input class="customer-info-form-input" v-model="editFormData.mobile2" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">电话2</label>
              <input class="customer-info-form-input" v-model="editFormData.phone2" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">QQ</label>
              <input class="customer-info-form-input" v-model="editFormData.qq" />
            </div>

            <div class="customer-info-form-field">
              <label class="customer-info-form-label">联系人3</label>
              <input class="customer-info-form-input" v-model="editFormData.contactPerson3" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">手机3</label>
              <input class="customer-info-form-input" v-model="editFormData.mobile3" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">电话3</label>
              <input class="customer-info-form-input" v-model="editFormData.phone3" />
            </div>
            <div class="customer-info-form-field">
              <label class="customer-info-form-label">短信号码</label>
              <input class="customer-info-form-input" v-model="editFormData.smsNumber" />
            </div>

            <div class="customer-info-form-field" style="grid-column: 1 / -1">
              <label class="customer-info-form-label">备注1</label>
              <input class="customer-info-form-input" style="flex: 1; width: auto" v-model="editFormData.remark1" />
            </div>
            <div class="customer-info-form-field" style="grid-column: 1 / -1">
              <label class="customer-info-form-label">备注2</label>
              <input class="customer-info-form-input" style="flex: 1; width: auto" v-model="editFormData.remark2" />
            </div>
          </div>
        </div>
        <div class="customer-info-add-footer">
          <button class="customer-info-btn customer-info-btn-primary" @click="saveEditCustomer">
            <Save :size="14" /> 保存
          </button>
          <button class="customer-info-btn customer-info-btn-ghost" @click="closeEditModal">
            <X :size="14" /> 取消
          </button>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- 营业执照预览 -->
  <Teleport to="body">
    <Transition name="cert-fade">
      <div v-if="certPreviewVisible" class="customer-info-cert-overlay" @click.self="certPreviewVisible = false">
        <div class="customer-info-cert-modal" @click.stop>
          <div class="customer-info-cert-modal-header">
            <span>营业执照</span>
            <button class="customer-info-cert-close" @click="certPreviewVisible = false">✕</button>
          </div>
          <div class="customer-info-cert-modal-body">
            <img :src="certPreviewUrl" alt="营业执照" />
          </div>
          <div class="customer-info-cert-modal-footer">
            <label class="customer-info-btn customer-info-btn-primary" style="cursor:pointer;">
              <Upload :size="14" /> 上传替换
              <input type="file" accept="image/*" style="display:none" @change="onCertPreviewUpload" />
            </label>
            <button class="customer-info-btn customer-info-btn-danger" @click="onCertDelete">删除图片</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- 右侧客户详情面板 -->
  <Teleport to="body">
    <Transition name="panel-slide">
      <div v-if="sidePanelVisible" class="customer-info-side-overlay" @click.self="sidePanelVisible = false">
        <div class="customer-info-side-panel">
          <div class="customer-info-side-header">
            <div class="customer-info-side-title-row">
              <span class="customer-info-side-icon">&#9745;</span>
              <span>客户资料</span>
            </div>
            <button class="customer-info-side-close" @click="sidePanelVisible = false">✕</button>
          </div>
          <div class="customer-info-side-body">
            <!-- 基本信息 -->
            <div class="customer-info-side-section">
              <div class="customer-info-side-section-title">基本信息</div>
              <div class="customer-info-side-table customer-info-grid-4">
                <div class="customer-info-side-label"><em>客户编号</em></div>
                <div>{{ sidePanelGroups.basic.customerCode || '-' }}</div>
                <div class="customer-info-side-label"><em>客户名称</em></div>
                <div>{{ sidePanelGroups.basic.customerName || '-' }}</div>
                <div class="customer-info-side-label"><em>国家</em></div>
                <div>{{ sidePanelGroups.basic.country || '-' }}</div>
                <div class="customer-info-side-label"><em>地区</em></div>
                <div>{{ sidePanelGroups.basic.region || '-' }}</div>
                <div class="customer-info-side-label"><em>地址</em></div>
                <div class="customer-info-side-colspan-3" style="word-break:break-all">{{ sidePanelGroups.basic.address || '-' }}</div>
                <div class="customer-info-side-label"><em>短信号码</em></div>
                <div>{{ sidePanelGroups.basic.smsNumber || '-' }}</div>
                <div class="customer-info-side-label"><em>邮箱</em></div>
                <div>{{ sidePanelGroups.contact.email || '-' }}</div>
              </div>
            </div>

            <!-- 联系方式 -->
            <div class="customer-info-side-section">
              <div class="customer-info-side-section-title">联系方式</div>
              <div class="customer-info-side-table customer-info-grid-6">
                <div class="customer-info-side-label"><em>联系人1</em></div>
                <div>{{ sidePanelGroups.contact.contactPerson1 || '-' }}</div>
                <div class="customer-info-side-label"><em>手机1</em></div>
                <div>{{ sidePanelGroups.contact.mobile1 || '-' }}</div>
                <div class="customer-info-side-label"><em>电话1</em></div>
                <div>{{ sidePanelGroups.contact.phone1 || '-' }}</div>

                <div class="customer-info-side-label"><em>联系人2</em></div>
                <div>{{ sidePanelGroups.contact.contactPerson2 || '-' }}</div>
                <div class="customer-info-side-label"><em>手机2</em></div>
                <div>{{ sidePanelGroups.contact.mobile2 || '-' }}</div>
                <div class="customer-info-side-label"><em>电话2</em></div>
                <div>{{ sidePanelGroups.contact.phone2 || '-' }}</div>

                <div class="customer-info-side-label"><em>联系人3</em></div>
                <div>{{ sidePanelGroups.contact.contactPerson3 || '-' }}</div>
                <div class="customer-info-side-label"><em>手机3</em></div>
                <div>{{ sidePanelGroups.contact.mobile3 || '-' }}</div>
                <div class="customer-info-side-label"><em>电话3</em></div>
                <div>{{ sidePanelGroups.contact.phone3 || '-' }}</div>

                <div class="customer-info-side-label"><em>QQ</em></div>
                <div>{{ sidePanelGroups.contact.qq || '-' }}</div>
                <div class="customer-info-side-label"><em>备注1</em></div>
                <div class="customer-info-side-colspan-3" style="word-break:break-all">{{ sidePanelGroups.contact.remark1 || '-' }}</div>

                <div class="customer-info-side-label"><em>备注2</em></div>
                <div class="customer-info-side-colspan-5" style="word-break:break-all">{{ sidePanelGroups.contact.remark2 || '-' }}</div>
              </div>
            </div>

            <!-- 系统信息 -->
            <div class="customer-info-side-section">
              <div class="customer-info-side-section-title">系统信息</div>
              <div class="customer-info-side-table customer-info-grid-6">
                <div class="customer-info-side-label"><em>登记日期</em></div>
                <div>{{ sidePanelGroups.system.registerDate || '-' }}</div>
                <div class="customer-info-side-label"><em>登记人</em></div>
                <div>{{ sidePanelGroups.system.registrant || '-' }}</div>
                <div class="customer-info-side-label"><em>修改日期</em></div>
                <div>{{ sidePanelGroups.system.modifyDate || '-' }}</div>
                <div class="customer-info-side-label"><em>修改人</em></div>
                <div>{{ sidePanelGroups.system.modifier || '-' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, onActivated, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/stores/auth'
import {
  Search, Plus, Pencil, Trash2, Save, X,
  ChevronsUp, ChevronsDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  RotateCcw, Upload, Download, AlertTriangle, Eye, EyeOff, Info
} from 'lucide-vue-next'
import { api } from '@/api'
import { useGridPrefSync } from '@/composables/useGridPrefSync'
import '@/styles/customer-info.css'

const router = useRouter()
const { state: authState } = useAuth()
const formExpanded = ref(true)
const formVisible = ref(true)
const formData = reactive({})
const currentRecord = ref(null)
const sidePanelVisible = ref(false)
const gridRef = ref(null)
const tableWrapRef = ref(null)
const tableWrapHeight = ref(600)
let resizeObserver = null
let resizeRafId = null
let lastObservedHeight = 0

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(500)
const pageSizeOptions = [100, 200, 500, 1000, 2000]
const totalRecords = ref(0)
const tableLoading = ref(false)

// 自定义提示弹窗
const showAlert = ref(false)
const alertMessage = ref('')
const certPreviewVisible = ref(false)
const certPreviewUrl = ref('')
const certPreviewRow = ref(null)

// 添加客户模态框
const showAddModal = ref(false)
const addFormData = reactive({})
const addSaving = ref(false)
const addCertFile = ref(null)
const addCertPreview = ref('')
const certDragOver = ref(false)
const addCertInput = ref(null)

// 编辑客户模态框
const showEditModal = ref(false)
const editFormData = reactive({})
const editSaving = ref(false)

const openEditModal = () => {
  if (!currentRecord.value) return
  Object.keys(editFormData).forEach(k => delete editFormData[k])
  Object.assign(editFormData, { ...currentRecord.value })
  delete editFormData.customerCode
  delete editFormData.id
  delete editFormData.createTime
  delete editFormData.updateTime
  delete editFormData.registerDate
  delete editFormData.modifyDate
  delete editFormData.registrant
  delete editFormData.modifier
  delete editFormData.certificate
  showEditModal.value = true
}

const closeEditModal = () => {
  if (editSaving.value) return
  showEditModal.value = false
}

const saveEditCustomer = async () => {
  if (editSaving.value) return
  if (!editFormData.customerName) {
    showAlertDialog('客户名称为必填项')
    return
  }
  editSaving.value = true
  const payload = { ...editFormData }
  try {
    const id = currentRecord.value?.id
    if (!id) { showAlertDialog('未选择客户'); editSaving.value = false; return }
    const res = await api(`/customers/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
    if (res && res.code === 200) {
      showEditModal.value = false
      showAlertDialog('修改成功')
      await loadData()
    } else {
      showAlertDialog(res?.message || '修改失败')
    }
  } catch (e) {
    console.error('修改客户失败:', e)
    showAlertDialog('修改失败: ' + (e.message || '未知错误'))
  } finally {
    editSaving.value = false
  }
}

const openAddModal = () => {
  Object.keys(addFormData).forEach(k => delete addFormData[k])
  addSaving.value = false
  addCertFile.value = null
  addCertPreview.value = ''
  certDragOver.value = false
  showAddModal.value = true
}

const closeAddModal = () => {
  if (addSaving.value) return
  showAddModal.value = false
}

const onCertDrop = (e) => {
  certDragOver.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) setAddCertFile(file)
}

const onAddCertSelect = (e) => {
  const file = e.target?.files?.[0]
  if (file) setAddCertFile(file)
  e.target.value = ''
}

const setAddCertFile = (file) => {
  if (!file.type.startsWith('image/')) {
    showAlertDialog('请选择图片文件')
    return
  }
  addCertFile.value = file
  const reader = new FileReader()
  reader.onload = (ev) => { addCertPreview.value = ev.target.result }
  reader.readAsDataURL(file)
}

const removeAddCert = () => {
  addCertFile.value = null
  addCertPreview.value = ''
}

const saveAddCustomer = async () => {
  if (addSaving.value) return
  if (!addFormData.customerName) {
    showAlertDialog('客户名称为必填项')
    return
  }
  addSaving.value = true
  const payload = { ...addFormData }
  try {
    const res = await api('/customers', { method: 'POST', body: JSON.stringify(payload) })
    if (res && res.code === 200 && res.data) {
      // 上传营业执照
      const newCustomer = res.data
      if (addCertFile.value && newCustomer.id) {
        const form = new FormData()
        form.append('file', addCertFile.value)
        try {
          const certRes = await api(`/customers/${newCustomer.id}/certificate`, { method: 'POST', body: form })
          if (certRes && certRes.code === 200) {
            newCustomer.certificate = certRes.data?.filePath || ''
          }
        } catch (e) {
          console.error('营业执照上传失败:', e)
        }
      }
      showAddModal.value = false
      showAlertDialog('添加成功')
      await loadData()
    } else {
      showAlertDialog(res?.message || '添加失败')
    }
  } catch (e) {
    console.error('添加客户失败:', e)
    showAlertDialog('添加失败: ' + (e.message || '未知错误'))
  } finally {
    addSaving.value = false
  }
}

const showAlertDialog = (msg) => {
  alertMessage.value = msg
  showAlert.value = true
}

const sortField = ref('')
const sortOrder = ref('')

const list = ref([])

const totalPages = computed(() => Math.max(1, Math.ceil(totalRecords.value / pageSize.value)))

const filteredTableData = computed(() => list.value)

const allColumns = [
  { type: 'checkbox', title: '#', width: 50, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'certificate', title: '营业执照', width: 100, slots: { default: 'col_certificate' } },
  { field: 'customerCode', title: '客户编号', width: 150, showOverflow: true, sortable: true, slots: { default: 'col_customerCode' } },
  { field: 'customerName', title: '客户名称', width: 180, showOverflow: true, sortable: true },
  { field: 'country', title: '国家', width: 100, showOverflow: true },
  { field: 'address', title: '地址', width: 200, showOverflow: true },
  { field: 'contactPerson1', title: '联系人1', width: 110, showOverflow: true },
  { field: 'mobile1', title: '手机1', width: 130 },
  { field: 'phone1', title: '电话1', width: 130 },
  { field: 'qq', title: 'QQ', width: 120 },
  { field: 'contactPerson2', title: '联系人2', width: 110, showOverflow: true },
  { field: 'mobile2', title: '手机2', width: 130 },
  { field: 'phone2', title: '电话2', width: 130 },
  { field: 'remark1', title: '备注1', width: 150, showOverflow: true },
  { field: 'contactPerson3', title: '联系人3', width: 110, showOverflow: true },
  { field: 'mobile3', title: '手机3', width: 130 },
  { field: 'phone3', title: '电话3', width: 130 },
  { field: 'remark2', title: '备注2', width: 150, showOverflow: true },
  { field: 'smsNumber', title: '短信号码', width: 140 },
  { field: 'region', title: '地区', width: 100 },
  { field: 'email', title: '邮箱', width: 180, showOverflow: true },
  { field: 'registrant', title: '登记人', width: 110 },
  { field: 'modifier', title: '修改人', width: 110 },
  { field: 'registerDate', title: '登记日期', width: 130, sortable: true },
  { field: 'modifyDate', title: '修改日期', width: 130, sortable: true }
]

// 表格列设置跨设备同步
const { fullKey: gridStorageKey, saveToBackend: saveGridPrefs, ready: prefReady } = useGridPrefSync(gridRef, 'customer-info', allColumns)

// ========== 表格事件处理 ==========
const onCustomChange = ({ type }) => {
  if (type === 'confirm' || type === 'reset') {
    setTimeout(() => saveGridPrefs(), 50)
  }
}

const onColumnDragEnd = () => {
  setTimeout(() => saveGridPrefs(), 100)
}

const onSearch = () => {
  currentPage.value = 1
  loadData()
}

const clearSearch = () => {
  searchKeyword.value = ''
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

const onCellClick = ({ row, column }) => {
  if (column?.field === 'customerCode') {
    openSidePanel(row)
    return
  }
  selectRecord(row)
}

const onSortChange = ({ field, order }) => {
  sortField.value = field || ''
  sortOrder.value = order || ''
  currentPage.value = 1
  loadData()
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
  } else {
    Object.keys(formData).forEach(k => delete formData[k])
  }
}

const startAdd = () => {
  openAddModal()
}

const deleteCurrent = async () => {
  if (!currentRecord.value) return
  if (!confirm(`确定要删除客户「${currentRecord.value.customerName || currentRecord.value.customerCode}」吗？`)) return
  try {
    const res = await api(`/customers/${currentRecord.value.id}`, { method: 'DELETE' })
    if (res && res.code === 200) {
      currentRecord.value = null
      Object.keys(formData).forEach(k => delete formData[k])
      showAlertDialog('删除成功')
      await loadData()
    } else {
      showAlertDialog(res?.message || '删除失败')
    }
  } catch (e) {
    console.error('删除客户失败:', e)
    showAlertDialog('删除失败: ' + (e.message || '未知错误'))
  }
}

const importData = () => {
  showAlertDialog('导入功能将在后续实现')
}

const exportData = () => {
  showAlertDialog('导出功能将在后续实现')
}

const FIELD_LABELS = {
  customerCode: '客户编号', customerName: '客户名称', country: '国家', address: '地址',
  contactPerson1: '联系人1', mobile1: '手机1', phone1: '电话1', email: '邮箱', qq: 'QQ',
  contactPerson2: '联系人2', mobile2: '手机2', phone2: '电话2', remark1: '备注1',
  contactPerson3: '联系人3', mobile3: '手机3', phone3: '电话3', remark2: '备注2',
  region: '地区', smsNumber: '短信号码',
  registerDate: '登记日期', modifyDate: '修改日期', registrant: '登记人', modifier: '修改人'
}

const sidePanelGroups = reactive({
  basic: {},
  contact: {},
  system: {}
})

const openSidePanel = (row) => {
  const r = row || {}
  sidePanelGroups.basic = {
    customerCode: r.customerCode,
    customerName: r.customerName,
    country: r.country,
    region: r.region,
    address: r.address,
    smsNumber: r.smsNumber
  }
  sidePanelGroups.contact = {
    contactPerson1: r.contactPerson1, mobile1: r.mobile1, phone1: r.phone1,
    contactPerson2: r.contactPerson2, mobile2: r.mobile2, phone2: r.phone2,
    contactPerson3: r.contactPerson3, mobile3: r.mobile3, phone3: r.phone3,
    email: r.email, qq: r.qq,
    remark1: r.remark1, remark2: r.remark2
  }
  sidePanelGroups.system = {
    registerDate: (r.registerDate || '').replace('T', ' '),
    registrant: r.registrant,
    modifyDate: (r.modifyDate || '').replace('T', ' '),
    modifier: r.modifier
  }
  sidePanelVisible.value = true
}

const viewCertificate = (row) => {
  if (row?.certificate && row?.id) {
    certPreviewRow.value = row
    certPreviewUrl.value = `/customers/${row.id}/certificate/image`
    certPreviewVisible.value = true
  }
}

const onCertificateUpload = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  if (!currentRecord.value?.id) {
    showAlertDialog('请先保存客户资料后再上传营业执照')
    e.target.value = ''
    return
  }
  const form = new FormData()
  form.append('file', file)
  try {
    const res = await api(`/customers/${currentRecord.value.id}/certificate`, {
      method: 'POST',
      body: form
    })
    if (res && res.code === 200 && res.data) {
      formData.certificate = res.data.filePath
      currentRecord.value.certificate = res.data.filePath
      showAlertDialog('营业执照上传成功')
    } else {
      showAlertDialog(res?.message || '上传失败')
    }
  } catch (err) {
    console.error('上传营业执照失败:', err)
    showAlertDialog('上传失败: ' + (err.message || '未知错误'))
  }
  e.target.value = ''
}

const onCertPreviewUpload = async (e) => {
  const file = e.target.files?.[0]
  if (!file || !certPreviewRow.value?.id) return
  const form = new FormData()
  form.append('file', file)
  try {
    const res = await api(`/customers/${certPreviewRow.value.id}/certificate`, {
      method: 'POST',
      body: form
    })
    if (res && res.code === 200 && res.data) {
      certPreviewRow.value.certificate = res.data.filePath
      formData.certificate = res.data.filePath
      certPreviewUrl.value = `/customers/${certPreviewRow.value.id}/certificate/image?t=${Date.now()}`
      await loadData()
      showAlertDialog('营业执照更新成功')
    } else {
      showAlertDialog(res?.message || '上传失败')
    }
  } catch (err) {
    showAlertDialog('上传失败: ' + (err.message || '未知错误'))
  }
  e.target.value = ''
}

const onCertDelete = async () => {
  if (!certPreviewRow.value?.id) return
  if (!confirm('确定删除该营业执照图片？')) return
  try {
    const res = await api(`/customers/${certPreviewRow.value.id}/certificate`, { method: 'DELETE' })
    if (res && res.code === 200) {
      certPreviewRow.value.certificate = ''
      formData.certificate = ''
      certPreviewVisible.value = false
      await loadData()
      showAlertDialog('营业执照已删除')
    } else {
      showAlertDialog(res?.message || '删除失败')
    }
  } catch (err) {
    showAlertDialog('删除失败: ' + (err.message || '未知错误'))
  }
}

const DATE_FIELDS = new Set(['registerDate', 'modifyDate'])

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
    const res = await api(`/customers?${params.join('&')}`)
    const data = res?.data ?? res ?? {}
    const arr = data?.records ?? data?.list
    list.value = Array.isArray(arr) ? arr : (Array.isArray(data) ? data : [])
    totalRecords.value = data?.total ?? list.value.length
    await nextTick()
    if (list.value.length > 0 && gridRef.value) {
      if (!currentRecord.value || !list.value.find(r => r?.id === currentRecord.value?.id)) {
        gridRef.value.setCurrentRow(list.value[0])
        selectRecord(list.value[0])
      }
    }
  } catch (e) {
    console.error('加载客户资料数据失败:', e)
  } finally {
    tableLoading.value = false
  }
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
