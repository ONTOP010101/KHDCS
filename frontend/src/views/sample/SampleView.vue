<template>
  <div class="sample-page sample-samples-page">
    <div class="sample-card sample-form-card" :class="{ expanded: formExpanded }">
      <div class="sample-form-top">
        <div class="sample-form-title">
        </div>
        <div class="sample-form-actions">
          <template v-if="formMode === 'readonly' && currentSample">
            <button class="sample-btn sample-btn-ghost" title="重置" @click="resetForm">
              <RotateCcw :size="14" />
            </button>
          </template>
          <button class="sample-btn sample-btn-ghost" title="字段设置" @click="toggleFieldSettings">
            <Settings :size="14" />
          </button>
          <button class="sample-btn sample-btn-ghost" :title="formExpanded ? '收起' : '展开'" @click="formExpanded = !formExpanded">
            <component :is="formExpanded ? ChevronsUp : ChevronsDown" :size="14" />
          </button>
          <button v-if="currentSample && currentSample.id && sampleVideos.length > 0" class="sample-btn sample-btn-ghost" :title="'查看视频 (' + sampleVideos.length + ')'" @click="showVideoPreviewModal = true">
            <VideoIcon :size="14" />
          </button>
        </div>
      </div>

      <div v-if="showFieldSettings" class="sample-field-settings">
        <div class="field-settings-header">
          <span class="field-settings-title">字段显示设置</span>
          <button class="field-settings-close" @click="showFieldSettings = false">
            <X :size="14" />
          </button>
        </div>
        <div class="field-settings-grid">
          <label v-for="f in allFormFields" :key="f.key || f.label" class="field-settings-chip">
            <input type="checkbox" v-model="fieldVisible[f.key]" />
            {{ f.label }}
          </label>
        </div>
      </div>

      <div class="sample-form-body">
        <div class="sample-form-scroll">
          <div class="sample-form-grid">
            <template v-for="f in visibleFormFields" :key="f.key || f.label">
              <div v-if="f.group" class="sample-form-field sample-form-group" :style="f.width || f.fields.some(sf => sf.width) ? { flex: '0 0 auto' } : {}">
                <label class="sample-form-label" :style="{ ...(f.labelWidth ? { flex: '0 0 ' + f.labelWidth + 'px' } : {}), ...(f.labelJustify ? { textAlign: 'justify', textAlignLast: 'justify' } : {}) }">{{ f.label }}</label>
                <div class="sample-form-group-inputs">
                  <input v-for="sf in f.fields" :key="sf.key"
                    class="sample-form-input"
                    :readonly="formMode === 'readonly'"
                    :placeholder="sf.placeholder"
                    :style="sf.width ? { flex: '0 0 ' + sf.width + 'px', minWidth: sf.width + 'px' } : {}"
                    v-model="formData[sf.key]"
                  />
                </div>
              </div>
              <div v-else class="sample-form-field" :style="f.width ? { flex: '0 0 auto' } : {}">
                <label class="sample-form-label" :style="{ ...(f.labelWidth ? { flex: '0 0 ' + f.labelWidth + 'px' } : {}), ...(f.labelJustify ? { textAlign: 'justify', textAlignLast: 'justify' } : {}), ...(f.color ? { color: f.color } : {}) }">{{ f.label }}</label>
                <input
                  class="sample-form-input"
                  :readonly="formMode === 'readonly'"
                  :placeholder="formMode === 'readonly' ? '' : f.label"
                  :style="{ ...(f.width ? { flex: '0 0 ' + f.width + 'px' } : {}), ...(f.color ? { color: f.color } : {}) }"
                  v-model="formData[f.key]"
                />
              </div>
            </template>
          </div>
        </div>

        <div class="sample-image-strip">
          <template v-if="currentSampleImages.length > 0">
            <div class="sample-image-strip-single">
              <img
                :src="currentSampleImages[stripIndex]?.thumbnailPath ? '/thumbnails/' + currentSampleImages[stripIndex]?.thumbnailPath : '/images/' + currentSampleImages[stripIndex]?.filePath"
                @click="viewOriginal"
                style="cursor:pointer"
              />
              <button v-if="currentSampleImages.length > 1" class="spm-strip-nav spm-strip-prev" @click="stripPrev">
                <ChevronLeft :size="14" />
              </button>
              <button v-if="currentSampleImages.length > 1" class="spm-strip-nav spm-strip-next" @click="stripNext">
                <ChevronRight :size="14" />
              </button>
              <span class="spm-strip-counter">{{ stripIndex + 1 }} / {{ currentSampleImages.length }}</span>
            </div>
          </template>
          <template v-else>
            <span class="sample-image-strip-empty">暂无图片</span>
          </template>
          <label v-if="formMode === 'edit' || formMode === 'add'" class="sample-image-upload-btn">
            <Upload :size="14" /> 上传
            <input type="file" accept="image/*" hidden @change="onImageUpload" />
          </label>
        </div>
      </div>
    </div>

    <div class="sample-card sample-toolbar-card">
      <div class="sample-toolbar-row">
        <div class="sample-search">
          <Search :size="14" />
          <input
            v-model="searchKeyword"
            placeholder="模糊搜索..."
            @keyup.enter="onSearch"
          />
        </div>
        <button class="sample-btn sample-btn-primary" @click="onSearch">
          <Search :size="14" /> 查询
        </button>
        <div class="sample-search">
          <Crosshair :size="14" />
          <input
            v-model="locateKeyword"
            placeholder="定位搜索..."
            @keyup.enter="onLocate"
          />
        </div>
        <button class="sample-btn sample-btn-ghost" @click="onLocate">
          <MapPin :size="14" /> 定位
        </button>
        <button v-if="searchKeyword || locateKeyword" class="sample-btn sample-btn-ghost" @click="clearSearch">
          <X :size="14" /> 清除
        </button>
        <div class="toolbar-sep"></div>
        <button class="sample-btn sample-btn-primary" @click="startAdd">
          <Plus :size="14" /> 添加资料
        </button>
        <template v-if="formMode === 'edit' || formMode === 'add'">
          <button class="sample-btn sample-btn-primary" @click="saveSample">
            <Save :size="14" /> 保存
          </button>
          <button class="sample-btn sample-btn-ghost" @click="cancelEdit">
            <X :size="14" /> 取消
          </button>
        </template>
        <button v-else class="sample-btn sample-btn-ghost" :disabled="!currentSample" @click="startEdit">
          <Pencil :size="14" /> 修改
        </button>
        <button class="sample-btn sample-btn-danger" :disabled="selectedIds.length === 0" @click="onDeleteSelected">
          <Trash2 :size="14" /> 删除
        </button>
        <button class="sample-btn sample-btn-ghost" @click="openImportModal">
          <Upload :size="14" /> 导入资料
        </button>
        <div class="sample-more-dropdown" style="position:relative">
          <button class="sample-btn sample-btn-ghost" @click.stop="toggleMoreDropdown">
            <MoreHorizontal :size="14" /> 其他功能
          </button>
        </div>
        <Teleport to="body">
          <div v-if="showMoreDropdown" class="sample-more-dropdown-panel" :style="moreDropdownStyle">
            <div class="sample-more-group-label"><Package :size="13" /> 数据操作</div>
            <div class="sample-more-item" @click="downloadTemplate"><Download :size="16" /> 下载导入模板</div>
            <div class="sample-more-item" @click="openBatchImageModal"><ImagePlus :size="16" /> 批量导入图片</div>
            <div class="sample-more-item" @click="openBatchVideoModal"><VideoIcon :size="16" /> 批量导入视频</div>
            <div class="sample-more-sep"></div>
            <div class="sample-more-group-label"><RotateCcw :size="13" /> 数据恢复</div>
            <div class="sample-more-item" @click="openRestoreDeletedModal"><RotateCcw :size="16" /> 恢复误删数据</div>
            <div class="sample-more-sep"></div>
            <div class="sample-more-group-label"><Search :size="13" /> 批量查询</div>
            <div class="sample-more-item" @click="openMainBatchQuery"><List :size="16" /> 按编号批量查询</div>
            <div class="sample-more-sep"></div>
            <div class="sample-more-group-label"><DollarSign :size="13" /> 价格操作</div>
            <div class="sample-more-item" @click="batchSetPrice"><Coins :size="16" /> 批量设置价格</div>
            <div class="sample-more-sep"></div>
            <div class="sample-more-group-label"><FileSpreadsheet :size="13" /> 导入导出</div>
            <div class="sample-more-item" @click="openImportModal"><FileUp :size="16" /> 导入Excel</div>
            <div class="sample-more-item" @click="exportExcel"><FileDown :size="16" /> 导出Excel</div>
          </div>
        </Teleport>
        <div class="toolbar-sep"></div>
        <button class="sample-btn sample-btn-ghost" @click="openAdvancedSearch">
          <Filter :size="14" /> 高级搜索
        </button>
        <div class="toolbar-sep"></div>
        <button class="sample-btn sample-btn-ghost" @click="doPrintTable">
          <Printer :size="14" /> 大条码打印
        </button>
        <button class="sample-btn sample-btn-blue" @click="doPrintQuarterTable">
          <Printer :size="14" /> 小条码打印
        </button>
        <button class="sample-btn sample-btn-blue" @click="openScanPrintModal">
          <Crosshair :size="14" /> 扫码打印
        </button>
        <div class="sample-more-dropdown" style="position:relative">
          <button class="sample-btn sample-btn-ghost" @click.stop="togglePrintDropdown">
            <Printer :size="14" /> 其他打印 <ChevronsDown :size="12" />
          </button>
        </div>
        <div class="toolbar-sep"></div>
        <button class="sample-btn sample-btn-ghost" @click="router.push({ name: 'ImageSearch' })">
          <ImageIcon :size="14" /> 图像搜索
        </button>
      </div>
    </div>

    <div class="sample-card sample-table-card">
      <div ref="tableWrapRef" class="sample-table-wrap">
        <vxe-grid
          ref="gridRef"
          :columns="allColumns"
          :data="tableData"
          :loading="tableLoading"
          :height="tableWrapHeight"
          :toolbar-config="{ custom: true, zoom: true }"
          :column-config="{ resizable: true, drag: true }"
          :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
          :cell-config="{ height: 44 }"
          :checkbox-config="{ highlight: true, range: true }"
          :sort-config="{ trigger: 'header', remote: true, defaultSort: { field: 'createTime', order: 'desc' } }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 0, rSize: 60, rHeight: 44 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 24 }, scrollY: { gt: 0, oSize: 0, rSize: 60, rHeight: 44 } }"
          :border="true"
          :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontWeight: 600, textAlign: 'center' }"
          :cell-style="{ textAlign: 'center' }"
          @checkbox-change="onCheckboxChange"
          @checkbox-all="onCheckboxAll"
          @cell-click="onCellClick"
          @sort-change="onSortChange"
        >
          <template #image_default="{ row }">
            <div style="display:flex;align-items:center;justify-content:center;height:100%">
              <img
                v-if="row.thumbnail"
                :src="'/thumbnails/' + row.thumbnail"
                loading="lazy"
                style="width:48px;height:36px;object-fit:cover;border-radius:6px;cursor:pointer"
                @click.stop="openPhotoModalFor(row)"
              />
              <span v-else style="color:rgba(29,29,31,0.25);font-size:11px">无图</span>
            </div>
          </template>
          <template #action_default="{ row }">
            <div style="display:flex;gap:4px;justify-content:center">
              <button class="sample-table-action" @click.stop="editRow(row)">编辑</button>
              <button class="sample-table-action" @click.stop="deleteRow(row)" style="color:#ff3b30">删除</button>
            </div>
          </template>
        </vxe-grid>
      </div>
      <div class="sample-statusbar">
        <div class="sample-row-select-box">
          <span class="sample-row-select-label">选中行:</span>
          <input v-model.number="rowSelectFrom" placeholder="起始" />
          <span style="font-size:11px;color:rgba(29,29,31,0.4)">-</span>
          <input v-model.number="rowSelectTo" placeholder="结束" />
          <button class="sample-btn sample-btn-ghost" @click="selectRowRange">选择</button>
          <button class="sample-btn sample-btn-ghost" @click="selectAllRows">全选</button>
          <button class="sample-btn sample-btn-ghost" @click="invertSelection">反选</button>
          <button class="sample-btn sample-btn-ghost" @click="clearSelection" :disabled="selectedIds.length === 0">清除</button>
        </div>
        <div class="sample-status-info">
          已选 <strong>{{ selectedIds.length }}</strong> 条，共 <strong>{{ totalRecords }}</strong> 条
        </div>
        <div class="sample-pagination">
          <span class="sample-page-size-label">每页</span>
          <select class="sample-page-size-select" v-model.number="pageSize">
            <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
          <span class="sample-page-size-label">条</span>
          <button class="sample-btn sample-btn-ghost" :disabled="currentPage <= 1" @click="goPage(1)">
            <ChevronsLeft :size="14" />
          </button>
          <button class="sample-btn sample-btn-ghost" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">
            <ChevronLeft :size="14" />
          </button>
          <span class="sample-page-text">{{ currentPage }} / {{ totalPages }}</span>
          <button class="sample-btn sample-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">
            <ChevronRight :size="14" />
          </button>
          <button class="sample-btn sample-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(totalPages)">
            <ChevronsRight :size="14" />
          </button>
        </div>
      </div>
    </div>

    <Teleport to="body">
    <div v-if="showRestoreDeletedModal" class="batch-image-modal-overlay" @click.self="showRestoreDeletedModal = false">
      <div class="batch-image-modal" style="width:960px;max-height:85vh;display:flex;flex-direction:column">
        <div class="batch-image-modal-header" style="flex-shrink:0">
          <strong>恢复误删数据</strong>
          <button class="modal-close-btn" @click="showRestoreDeletedModal = false"><X :size="16" /></button>
        </div>
        <div class="batch-image-modal-body" style="padding:0;flex:1;overflow:hidden;display:flex;flex-direction:column">
          <div style="display:flex;gap:8px;padding:10px 12px;border-bottom:1px solid #e5e5ea;background:#fafafa;align-items:center;flex-shrink:0">
            <select v-model="deletedFilterField" style="height:32px;border:1px solid #d1d1d6;border-radius:6px;padding:0 8px;font-size:13px;background:#fff;color:#333">
              <option value="">全部字段</option>
              <option value="sampleCode">公司编号</option>
              <option value="manufacturerCode">厂商编号</option>
              <option value="updateTime">删除日期</option>
            </select>
            <input v-model="deletedFilterKeyword" placeholder="输入关键词搜索..." style="flex:1;height:32px;border:1px solid #d1d1d6;border-radius:6px;padding:0 12px;font-size:13px;outline:none" @keyup.enter="doDeletedFilter" />
            <button class="sample-btn sample-btn-primary" style="height:32px;padding:0 16px;font-size:13px;border-radius:6px" @click="doDeletedFilter">筛选查询</button>
            <button class="sample-btn sample-btn-ghost" style="height:32px;padding:0 14px;font-size:12px;border-radius:6px" @click="doDeletedResetFilter" v-if="deletedFilterActive">清除</button>
          </div>
          <vxe-grid ref="deletedGridRef" :columns="deletedGridColumns" :data="deletedData" :loading="deletedLoading"
            :checkbox-config="{ highlight: true, range: true }"
            :pager-config="{ enabled: true, pageSize: 1000, pageSizes: [100, 500, 1000, 2000, 5000], layouts: ['Total', 'PrevJump', 'PrevPage', 'Number', 'NextPage', 'NextJump', 'Sizes', 'FullJump'] }"
            :toolbar-config="{ custom: true, refresh: true, zoom: true, slots: { buttons: 'deletedToolbarBtns' } }"
            :column-config="{ resizable: true, drag: true }"
            :sort-config="{ multiple: true, remote: false, sortMethod: deletedSortMethod }"
            :row-config="{ isHover: true, keyField: 'id' }"
            :cell-config="{ height: 44 }"
            :scroll-y="{ enabled: true, gt: 0, oSize: 0, rSize: 60, rHeight: 44 }"
            :virtual-y-config="{ enabled: true, gt: 0 }"
            :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 24 }, scrollY: { gt: 0, oSize: 0, rSize: 60, rHeight: 44 } }"
            :border="true"
            :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontWeight: 600, textAlign: 'center' }"
            :cell-style="{ textAlign: 'center' }"
            :max-height="deletedGridMaxHeight"
            style="flex:1;min-height:0"
            @checkbox-change="onDeletedCheckChange" @checkbox-all="onDeletedCheckChange"
            @zoom="onDeletedZoom" @toolbar-button-click="onDeletedToolbarClick">
            <template #deletedToolbarBtns>
              <div v-if="deletedFullscreen" style="display:flex;align-items:center;gap:6px;margin-left:8px">
                <input v-model="deletedFullscreenSearch" placeholder="搜索全部字段..."
                  style="width:220px;height:28px;border:1px solid #c7c7cc;border-radius:6px;padding:0 10px;font-size:12px;outline:none;transition:border-color 0.15s"
                  @input="onDeletedFullscreenSearch" @keyup.enter="onDeletedFullscreenSearch" />
                <button v-if="deletedFullscreenSearch"
                  style="height:26px;padding:0 10px;font-size:11px;border-radius:4px;border:1px solid #d1d1d6;background:#f5f5f7;color:#555;cursor:pointer"
                  @click="clearDeletedFullscreenSearch">清除</button>
                <button
                  style="height:28px;padding:0 12px;font-size:12px;border-radius:5px;border:1px solid #007aff;background:#007aff;color:#fff;cursor:pointer;white-space:nowrap"
                  @click="openDeletedBatchQuery">批量查询</button>
              </div>
              <button class="sample-btn sample-btn-primary" style="height:30px;padding:0 16px;font-size:13px;border-radius:6px;margin-left:10px" :disabled="deletedSelected.length === 0"
                @click="doRestoreDeleted">恢复选中 ({{ deletedSelected.length }})</button>
            </template>
          </vxe-grid>
        </div>
        <div class="modal-footer" style="border-top:none;padding:8px 16px">
          <button class="sample-btn sample-btn-ghost" @click="showRestoreDeletedModal = false">关闭</button>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showMainBatchQuery" class="batch-image-modal-overlay" @click.self="showMainBatchQuery = false">
      <div class="batch-image-modal" style="width:460px">
        <div class="batch-image-modal-header">
          <strong>按编号批量查询</strong>
          <button class="modal-close-btn" @click="showMainBatchQuery = false"><X :size="16" /></button>
        </div>
        <div class="batch-image-modal-body" style="display:flex;flex-direction:column;gap:12px">
          <div style="display:flex;gap:16px;align-items:center">
            <label style="display:flex;align-items:center;gap:6px;font-size:13px;cursor:pointer">
              <input type="radio" v-model="mainBatchField" value="sampleCode" style="accent-color:#007aff" />
              公司编号
            </label>
            <label style="display:flex;align-items:center;gap:6px;font-size:13px;cursor:pointer">
              <input type="radio" v-model="mainBatchField" value="factoryCode" style="accent-color:#007aff" />
              出厂货号
            </label>
          </div>
          <textarea v-model="mainBatchInput" placeholder="每行一个编号，可输入多个..."
            style="width:100%;height:180px;border:1px solid #d1d1d6;border-radius:8px;padding:10px 12px;font-size:13px;line-height:1.6;resize:vertical;outline:none;box-sizing:border-box"
          ></textarea>
          <div style="display:flex;gap:8px;justify-content:flex-end">
            <button class="sample-btn sample-btn-ghost" style="height:32px;padding:0 16px;font-size:13px;border-radius:6px" @click="showMainBatchQuery = false">取消</button>
            <button class="sample-btn sample-btn-primary" style="height:32px;padding:0 20px;font-size:13px;border-radius:6px" @click="doMainBatchQuery">查询</button>
          </div>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showDeletedBatchQuery" class="batch-image-modal-overlay" @click.self="showDeletedBatchQuery = false">
      <div class="batch-image-modal" style="width:460px">
        <div class="batch-image-modal-header">
          <strong>批量查询</strong>
          <button class="modal-close-btn" @click="showDeletedBatchQuery = false"><X :size="16" /></button>
        </div>
        <div class="batch-image-modal-body" style="display:flex;flex-direction:column;gap:12px">
          <div style="display:flex;gap:16px;align-items:center">
            <label style="display:flex;align-items:center;gap:6px;font-size:13px;cursor:pointer">
              <input type="radio" v-model="deletedBatchField" value="sampleCode" style="accent-color:#007aff" />
              公司编号
            </label>
            <label style="display:flex;align-items:center;gap:6px;font-size:13px;cursor:pointer">
              <input type="radio" v-model="deletedBatchField" value="factoryCode" style="accent-color:#007aff" />
              出厂货号
            </label>
          </div>
          <textarea v-model="deletedBatchInput" placeholder="每行一个编号，可输入多个..."
            style="width:100%;height:180px;border:1px solid #d1d1d6;border-radius:8px;padding:10px 12px;font-size:13px;line-height:1.6;resize:vertical;outline:none;box-sizing:border-box"
          ></textarea>
          <div style="display:flex;gap:8px;justify-content:flex-end">
            <button class="sample-btn sample-btn-ghost" style="height:32px;padding:0 16px;font-size:13px;border-radius:6px" @click="showDeletedBatchQuery = false">取消</button>
            <button class="sample-btn sample-btn-primary" style="height:32px;padding:0 20px;font-size:13px;border-radius:6px" @click="doDeletedBatchQuery">查询</button>
          </div>
        </div>
      </div>
    </div>
    </Teleport>

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
              <button class="sample-btn sample-btn-ghost" style="font-size:11px;padding:1px 8px;height:22px;margin-left:8px" @click="exportImportFailedRows">
                <FileDown :size="12" /> 导出
              </button>
            </p>
            <div class="br-detail-list br-failed-table-wrap">
              <table class="br-failed-table">
                <thead><tr><th>行号</th><th>公司编号</th><th>样品名称</th><th>原因</th><th>类型</th></tr></thead>
                <tbody>
                  <tr v-for="(row, i) in batchResult.failedRows" :key="'fr'+i" :class="{ 'br-dup-row': row.类型 === '重复' }">
                    <td>{{ row.row }}</td>
                    <td>{{ row['公司编号'] }}</td>
                    <td>{{ row['样品名称'] }}</td>
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
            <p class="br-section-title">未匹配图片：</p>
            <div class="br-detail-list">
              <p v-for="(name, i) in batchResult.unmatchedList" :key="'u'+i" class="br-detail-item br-warn-text">{{ name }}: 未匹配到样品</p>
            </div>
          </template>
        </div>
        <div class="modal-footer">
          <button class="sample-btn sample-btn-primary" @click="showBatchResultModal = false">知道了</button>
        </div>
      </div>
    </div>

    <div v-if="showScanPrintModal" class="batch-image-modal-overlay" @click.self="showScanPrintModal = false">
      <div class="batch-image-modal scan-print-modal" style="width:760px;max-height:80vh;display:flex;flex-direction:column">
        <div class="batch-image-modal-header">
          <span>扫码打印</span>
          <button class="modal-close-btn" @click="showScanPrintModal = false"><X :size="16" /></button>
        </div>
        <div class="batch-image-modal-body" style="flex:1;overflow-y:auto;padding:16px 20px">
          <div style="display:flex;gap:20px;margin-bottom:16px">
            <div style="flex-shrink:0;width:320px">
              <label style="font-size:13px;font-weight:600;color:#1d1d1f;display:block;margin-bottom:6px">公司编号</label>
              <div style="display:flex;gap:8px">
                <input type="text" ref="scanPrintInputRef" v-model="scanPrintCode" placeholder="输入或扫码公司编号..."
                  style="flex:1;height:38px;border-radius:10px;border:1px solid rgba(0,122,255,0.2);outline:none;padding:0 12px;font-size:14px;font-weight:600;color:#1d1d1f;"
                  @keyup.enter="searchScanPrint" />
                <button class="sample-btn sample-btn-blue" style="height:38px;font-size:13px" @click="searchScanPrint">查询</button>
              </div>
              <div style="margin-top:8px;display:flex;align-items:center;gap:6px">
                <label style="display:flex;align-items:center;gap:4px;cursor:pointer;font-size:12px;color:rgba(29,29,31,0.6);user-select:none" @click="scanPrintContinuous = !scanPrintContinuous">
                  <span style="width:16px;height:16px;border-radius:4px;border:2px solid;display:flex;align-items:center;justify-content:center;transition:all 0.15s"
                    :style="scanPrintContinuous ? 'background:#007aff;border-color:#007aff;color:#fff' : 'border-color:rgba(29,29,31,0.25)'">
                    <span v-if="scanPrintContinuous" style="font-size:10px;line-height:1">✓</span>
                  </span>
                  连续打印（查询后自动打印）
                </label>
              </div>
              <div v-if="scanPrintResult" style="margin-top:14px;padding:14px;border-radius:12px;border:1px solid rgba(0,122,255,0.12);background:rgba(0,122,255,0.03)">
                <div style="font-size:14px;font-weight:700;color:#1d1d1f;margin-bottom:8px">{{ scanPrintResult.sampleCode || '-' }}</div>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:6px;font-size:12px;color:rgba(29,29,31,0.7)">
                  <div><span style="color:rgba(29,29,31,0.4)">出厂货号：</span>{{ scanPrintResult.factoryCode || scanPrintResult.packageCode || '-' }}</div>
                  <div><span style="color:rgba(29,29,31,0.4)">摊位号：</span>{{ scanPrintResult.boothNo || '-' }}</div>
                  <div><span style="color:rgba(29,29,31,0.4)">中文包装：</span>{{ scanPrintResult.packagingCn || '-' }}</div>
                  <div><span style="color:rgba(29,29,31,0.4)">内盒/装箱：</span>{{ scanPrintResult.innerBoxCount || '-' }}/{{ scanPrintResult.cartonCapacity || '-' }}</div>
                </div>
                <div style="margin-top:6px;font-size:12px;color:rgba(29,29,31,0.7)">
                  <span style="color:rgba(29,29,31,0.4)">样品名称：</span>{{ scanPrintResult.sampleName || '-' }}
                </div>
              </div>
              <div v-if="scanPrintError" style="margin-top:14px;padding:10px 14px;border-radius:10px;background:rgba(255,59,48,0.06);color:#ff3b30;font-size:13px;font-weight:600">
                {{ scanPrintError }}
              </div>
            </div>
            <div style="flex:1;display:flex;align-items:center;justify-content:center;border-radius:12px;border:1px solid rgba(0,122,255,0.12);min-height:240px;background:rgba(0,0,0,0.02)">
              <div v-if="scanPrintImageSrc" style="padding:12px">
                <img :src="scanPrintImageSrc" style="max-width:100%;max-height:280px;object-fit:contain;border-radius:8px" />
              </div>
              <div v-else style="text-align:center;color:rgba(29,29,31,0.3);font-size:14px">
                <ImageIcon :size="48" style="margin-bottom:8px;opacity:0.3" />
                <div>输入公司编号查询后显示图片</div>
              </div>
            </div>
          </div>

          <div v-if="scanPrintResult || scanPrintContinuous" style="border-top:1px solid rgba(0,122,255,0.08);padding-top:14px;display:flex;align-items:center;justify-content:space-between">
            <div style="display:flex;align-items:center;gap:16px">
              <label class="radio-item-mp" :class="{active:scanPrintType==='barcode'}" @click="scanPrintType='barcode'">
                <span class="radio-dot"></span> 大条码 (50×40mm)
              </label>
              <label class="radio-item-mp" :class="{active:scanPrintType==='quarter'}" @click="scanPrintType='quarter'">
                <span class="radio-dot"></span> 小条码 (25×25mm)
              </label>
              <div style="display:flex;align-items:center;gap:6px;margin-left:4px">
                <label style="font-size:12px;font-weight:600;color:rgba(29,29,31,0.5);white-space:nowrap">打印张数</label>
                <input type="number" v-model.number="scanPrintCount" min="1" max="99"
                  style="width:60px;height:32px;border-radius:8px;border:1px solid rgba(0,122,255,0.2);outline:none;text-align:center;font-size:13px;font-weight:600;color:#007aff;padding:0 4px" />
              </div>
            </div>
            <button class="sample-btn sample-btn-primary" @click="doScanPrint">立即打印</button>
          </div>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showBatchVideoModal" class="batch-image-modal-overlay" @click.self="closeBatchVideoModal">
      <div class="batch-image-modal">
        <div class="batch-image-modal-header">
          <strong>批量导入视频</strong>
          <button class="modal-close-btn" @click="closeBatchVideoModal"><X :size="16" /></button>
        </div>
        <div class="batch-image-modal-body">
          <div class="upload-type-group">
            <span class="upload-type-label">编号类型</span>
            <div class="radio-group">
              <label class="radio-item">
                <input type="radio" v-model="batchVideoType" value="company-code" />
                <span>公司编号</span>
              </label>
              <label class="radio-item">
                <input type="radio" v-model="batchVideoType" value="factory-code" />
                <span>出厂货号</span>
              </label>
              <label class="radio-item">
                <input type="radio" v-model="batchVideoType" value="custom" />
                <span>自定义</span>
              </label>
            </div>
          </div>

          <div v-if="batchVideoType === 'custom'" class="custom-match-panel">
            <div class="custom-match-row">
              <span class="cm-label">匹配方式</span>
              <div class="radio-group">
                <label class="radio-item">
                  <input type="radio" v-model="customMatchSubType" value="company-code" />
                  <span>公司编号</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="customMatchSubType" value="factory-code" />
                  <span>出厂货号</span>
                </label>
              </div>
            </div>
            <div v-if="customMatchSubType === 'factory-code'" class="custom-match-row">
              <span class="cm-label">厂商编号</span>
              <input type="text" class="cm-input" v-model="customManufacturerCode" placeholder="请输入厂商编号" />
            </div>
            <div class="custom-match-row cm-row-textarea">
              <span class="cm-label">{{ customMatchSubType === 'company-code' ? '公司编号' : '出厂货号' }}</span>
              <textarea class="cm-textarea" v-model="customCodesText" :placeholder="customMatchSubType === 'company-code' ? '输入多个公司编号，用逗号或换行分隔\n例：YX18236594, YX18236644' : '输入多个出厂货号，用逗号或换行分隔\n例：555-99, 555-100'" rows="3"></textarea>
            </div>
          </div>

          <div v-if="!videoUploading && batchVideoMatched.length === 0"
            class="upload-area"
            @click="$refs.batchVideoFileInput.click()"
            @dragover.prevent="onVideoDragOver"
            @dragleave="onDragLeave"
            @drop.prevent="onVideoDrop"
          >
            <div class="upload-icon"><VideoIcon :size="48" /></div>
            <div class="upload-text">点击或拖拽上传视频文件</div>
            <div class="upload-hint" v-if="batchVideoType !== 'custom'">支持 MP4 / MOV，文件名需包含编号</div>
            <div class="upload-hint" v-else>支持 MP4 / MOV，视频将按顺序与输入的编号匹配</div>
            <input ref="batchVideoFileInput" type="file" accept=".mp4,.mov,.MP4,.MOV" multiple hidden @change="onVideoFileChange" />
          </div>

          <div v-if="videoMatchLoading" style="text-align:center;padding:20px;color:#999">正在匹配样品...</div>

          <div v-else-if="batchVideoMatched.length > 0" class="batch-match-results">
            <div class="batch-nav">
              <button class="batch-nav-btn" :disabled="videoCurrentIndex === 0" @click="goToVideoPrev">
                <ChevronLeft :size="18" />
              </button>
              <span class="batch-nav-counter">第 {{ videoCurrentIndex + 1 }} / {{ batchVideoMatched.length }} 项</span>
              <button class="batch-nav-btn" :disabled="videoCurrentIndex >= batchVideoMatched.length - 1" @click="goToVideoNext">
                <ChevronRight :size="18" />
              </button>
            </div>

            <div class="batch-match-card-single">
              <template v-if="batchVideoMatched[videoCurrentIndex].matched">
                <div style="width:100%;max-height:240px;background:#000;border-radius:8px;overflow:hidden;margin-bottom:12px">
                  <video
                    :src="batchVideoMatched[videoCurrentIndex].previewUrl"
                    controls
                    style="width:100%;max-height:240px;display:block"
                    preload="metadata"
                  ></video>
                </div>
                <div class="bmc-meta">
                  <span>文件名: <strong>{{ batchVideoMatched[videoCurrentIndex].file.name }}</strong></span>
                  <span>公司编号: <strong>{{ batchVideoMatched[videoCurrentIndex].sampleCode || '-' }}</strong></span>
                  <span>样品名称: <strong>{{ batchVideoMatched[videoCurrentIndex].sampleName || '-' }}</strong></span>
                  <span>出厂货号: <strong>{{ batchVideoMatched[videoCurrentIndex].factoryCode || '-' }}</strong></span>
                </div>
                <div class="bmc-actions">
                  <button class="sample-btn sample-btn-ghost bmc-btn" :class="{ active: batchVideoMatched[videoCurrentIndex].action === 'skip' }" @click="batchVideoMatched[videoCurrentIndex].action = 'skip'">跳过</button>
                  <button class="sample-btn sample-btn-ghost bmc-btn" :class="{ active: batchVideoMatched[videoCurrentIndex].action === 'cover' }" @click="batchVideoMatched[videoCurrentIndex].action = 'cover'">覆盖</button>
                  <button class="sample-btn sample-btn-ghost bmc-btn" :class="{ active: batchVideoMatched[videoCurrentIndex].action === 'append' }" @click="batchVideoMatched[videoCurrentIndex].action = 'append'">追加</button>
                  <span class="bmc-remove" @click="removeVideoFile(videoCurrentIndex)"><X :size="14" /></span>
                </div>
              </template>
              <template v-else>
                <div class="bmc-unmatched">
                  <ImageIcon :size="20" />
                  <span>{{ batchVideoMatched[videoCurrentIndex].file.name }} — 未匹配到样品</span>
                  <span class="bmc-remove" @click="removeVideoFile(videoCurrentIndex)"><X :size="14" /></span>
                </div>
              </template>
            </div>

            <div class="batch-match-footer">
              <div class="bmf-left">
                <span>已匹配 {{ batchVideoMatched.filter(m => m.matched).length }} / {{ batchVideoMatched.length }} 项</span>
              </div>
              <div class="bmf-right">
                <button class="sample-btn sample-btn-ghost" @click="setVideoActionAll('skip')">全部跳过</button>
                <button class="sample-btn sample-btn-ghost" @click="setVideoActionAll('cover')">全部覆盖</button>
                <button class="sample-btn sample-btn-ghost" @click="setVideoActionAll('append')">全部追加</button>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="sample-btn sample-btn-ghost" @click="closeBatchVideoModal">取消</button>
          <button class="sample-btn sample-btn-primary" :disabled="batchVideoMatched.filter(m => m.matched && m.action !== 'skip').length === 0 || videoUploading" @click="doBatchVideoUpload">
            <Upload :size="14" /> {{ videoUploading ? `上传中 ${videoUploadProgress.done}/${videoUploadProgress.total} (成功${videoUploadProgress.success} 失败${videoUploadProgress.fail})` : `开始上传 (${batchVideoMatched.filter(m => m.matched && m.action !== 'skip').length})` }}
          </button>
        </div>
        <div v-if="videoUploading" class="video-upload-progress-bar">
          <div class="vupb-info">
            <span class="vupb-filename">{{ videoUploadProgress.currentFileName }}</span>
            <span class="vupb-percent">{{ videoUploadProgress.currentProgress }}%</span>
          </div>
          <div class="vupb-track">
            <div class="vupb-fill" :style="{ width: videoUploadProgress.currentProgress + '%' }"></div>
          </div>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showVideoPreviewModal" class="batch-image-modal-overlay" @click.self="showVideoPreviewModal = false">
      <div class="batch-image-modal">
        <div class="batch-image-modal-header">
          <strong>视频预览</strong>
          <button class="modal-close-btn" @click="showVideoPreviewModal = false"><X :size="16" /></button>
        </div>
        <div class="batch-image-modal-body">
          <div v-if="sampleVideos.length === 0" style="text-align:center;padding:40px;color:#999">暂无视频</div>
          <div v-else class="video-preview-list">
            <div v-for="(video, idx) in sampleVideos" :key="video.id" class="video-preview-item" :class="{ active: videoPreviewIndex === idx }" @click="videoPreviewIndex = idx">
              <button class="video-preview-del" @click.stop="deleteSampleVideo(video.id, idx)"><X :size="12" /></button>
              <video :src="'/videos/file/' + video.filePath" preload="metadata" class="video-preview-thumb"></video>
              <div class="video-preview-name">{{ video.fileName }}</div>
              <div class="video-preview-size">{{ formatFileSize(video.fileSize) }}</div>
            </div>
          </div>
          <div v-if="sampleVideos.length > 0" class="video-preview-player">
            <video :src="'/videos/file/' + sampleVideos[videoPreviewIndex].filePath" controls style="width:100%;max-height:400px;display:block;background:#000"></video>
            <div class="video-preview-info">
              <span>{{ sampleVideos[videoPreviewIndex].fileName }}</span>
              <button class="sample-btn sample-btn-ghost" style="font-size:11px;padding:2px 10px;height:24px" @click="deleteSampleVideo(sampleVideos[videoPreviewIndex].id, videoPreviewIndex)"><X :size="12" /> 删除</button>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="sample-btn sample-btn-ghost" @click="showVideoPreviewModal = false">关闭</button>
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
          <p class="import-confirm-hint">重复公司编号的资料将被自动跳过</p>
        </div>
        <div class="import-confirm-footer">
          <button class="sample-btn sample-btn-ghost" @click="showImportConfirmModal = false">取消</button>
          <button class="sample-btn sample-btn-primary" @click="executeImport">确定</button>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showImagePreview" class="image-preview-overlay" @click.self="showImagePreview = false">
      <div class="image-preview-dialog">
        <div class="ip-header">
          <div class="ip-header-left">
            <ImageIcon :size="18" />
            <span>图片预览</span>
            <span class="ip-count" v-if="imagePreviewList.length > 1">{{ imagePreviewIndex + 1 }} / {{ imagePreviewList.length }}</span>
          </div>
          <div class="ip-header-right">
            <label class="ip-upload-btn" :class="{ disabled: ipUploading }" :title="ipUploading ? '上传中...' : '上传图片'">
              <ImagePlus :size="15" /> {{ ipUploading ? `上传中 ${ipUploadDone}/${ipUploadTotal}` : '上传' }}
              <input type="file" accept="image/*" multiple hidden @change="onPreviewUpload" :disabled="ipUploading" />
            </label>
            <label class="ip-select-all" v-if="imagePreviewList.length > 1" @click.stop>
              <input type="checkbox" :checked="isAllImageSelected" @change="toggleSelectAllImages" />
              <span>全选</span>
            </label>
            <button v-if="imagePreviewSelected.size > 0" class="ip-delete-btn" @click="deleteSelectedImages">
              <Trash2 :size="15" /> 删除选中({{ imagePreviewSelected.size }})
            </button>
            <button class="ip-delete-btn" @click="deletePreviewImage"><Trash2 :size="15" /></button>
            <button class="ip-close" @click="showImagePreview = false"><X :size="20" /></button>
          </div>
        </div>
        <div class="ip-body">
          <div class="ip-main">
            <img :src="currentPreviewSrc" />
            <button v-if="imagePreviewList.length > 1" class="ip-nav ip-prev" @click="imagePreviewIndex = imagePreviewIndex > 0 ? imagePreviewIndex - 1 : imagePreviewList.length - 1"><ChevronLeft :size="24" /></button>
            <button v-if="imagePreviewList.length > 1" class="ip-nav ip-next" @click="imagePreviewIndex = imagePreviewIndex < imagePreviewList.length - 1 ? imagePreviewIndex + 1 : 0"><ChevronRight :size="24" /></button>
          </div>
          <div v-if="imagePreviewList.length > 1" class="ip-thumbs">
            <div
              v-for="(img, idx) in imagePreviewList" :key="img.hash || idx"
              class="ip-thumb" :class="{ active: idx === imagePreviewIndex, 'ip-thumb-popout': posPickerIdx === idx }"
              @click="imagePreviewIndex = idx"
            >
              <div class="ip-pos-badge" @click.stop="togglePosPicker(idx)" :title="'当前位置: ' + posLabel(idx) + ', 点击更改'">
                {{ posLabel(idx) }}
              </div>
              <div class="ip-thumb-check" :class="{ checked: imagePreviewSelected.has(idx) }" @click.stop="toggleSelectImage(idx)">
                <span v-if="imagePreviewSelected.has(idx)">✓</span>
              </div>
              <img :src="img.thumbnailPath ? '/thumbnails/' + img.thumbnailPath : '/images/' + img.filePath" />
              <div v-if="posPickerIdx === idx" class="ip-pos-picker" @click.stop>
                <button class="ip-pos-item" :class="{ active: idx === 0 }" @click.stop="setImagePosition(idx, 0)">设为 主图</button>
                <button v-for="p in Math.max(imagePreviewList.length, 6)" :key="p" class="ip-pos-item" :class="{ active: idx === p }" @click.stop="setImagePosition(idx, p)">位置 {{ p }}</button>
              </div>
            </div>
          </div>
        </div>
        <div class="ip-footer">
          <span class="ip-name">{{ imagePreviewList[imagePreviewIndex]?.originalName || '图片' }}</span>
          <span class="ip-info" v-if="imagePreviewList[imagePreviewIndex]?.fileSize">{{ formatFileSize(imagePreviewList[imagePreviewIndex].fileSize) }}</span>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showPhotoModal" class="sample-photo-modal" :style="photoModalStyle">
      <div class="spm-header" @mousedown="startDragModal">
        <span class="spm-header-title">照片预览</span>
        <button class="spm-header-close" @click="showPhotoModal = false">&times;</button>
      </div>
      <div class="spm-body">
        <div class="spm-body-left">
          <div class="spm-main-img-wrap">
            <img v-if="photoModalImages.length > 0"
                 :src="photoModalImages[photoModalIndex]?.thumbnailPath ? '/thumbnails/' + photoModalImages[photoModalIndex]?.thumbnailPath : '/images/view/hash/' + photoModalImages[photoModalIndex]?.hash" />
            <span v-else class="spm-no-img">无图片</span>
            <button v-if="photoModalImages.length > 1" class="spm-main-img-nav spm-main-img-prev" @click="photoModalPrev">&#10094;</button>
            <button v-if="photoModalImages.length > 1" class="spm-main-img-nav spm-main-img-next" @click="photoModalNext">&#10095;</button>
          </div>
          <div class="spm-thumb-strip">
            <div
              v-for="(img, idx) in photoModalImages"
              :key="img.hash || idx"
              class="spm-thumb-item"
              :class="{ active: idx === photoModalIndex }"
              @click="photoModalIndex = idx"
            >
              <img :src="img.thumbnailPath ? '/thumbnails/' + img.thumbnailPath : ''" />
            </div>
          </div>
        </div>
        <div class="spm-body-right" v-html="photoModalDetailHtml"></div>
      </div>
      <div class="spm-footer">
        <div class="spm-toggle-group">
          <label class="spm-toggle"><input type="checkbox" v-model="hideFactoryPrice" @change="rebuildDetailHtml" /> 隐藏出厂价</label>
          <label class="spm-toggle"><input type="checkbox" v-model="hideSupplierInfo" @change="rebuildDetailHtml" /> 隐藏厂商信息</label>
        </div>
        <button class="spm-btn-close" @click="showPhotoModal = false">关闭</button>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showImportModal" class="batch-image-modal-overlay" @click.self="showImportModal = false">
      <div class="batch-image-modal">
        <div class="batch-image-modal-header">
          <strong>导入样品数据</strong>
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
          <button class="sample-btn sample-btn-ghost" @click="showImportModal = false">取消</button>
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
            <button class="sample-btn sample-btn-ghost" style="font-size:11px;padding:2px 10px;height:26px" :disabled="importSelectedRows.length === 0" @click="deleteSelectedPreviewRows">
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
                  <button class="sample-table-action" style="color:#007aff;font-size:11px;padding:2px 8px;height:24px" @click.stop="restorePreviewRow(row)">还原</button>
                  <button class="sample-table-action" style="color:#ff3b30;font-size:11px;padding:2px 8px;height:24px" @click.stop="deletePreviewRow(row)">删除</button>
                </div>
              </template>
            </vxe-grid>
          </div>
        </div>
        <div class="modal-footer import-preview-footer">
          <div class="import-toolbar-left">
            <button class="sample-btn sample-btn-ghost" @click="downloadTemplate">
              <Download :size="14" /> 下载模板
            </button>
            <button class="sample-btn sample-btn-ghost" :disabled="importSelectedRows.length === 0" @click="exportSelectedRows">
              <FileDown :size="14" /> 导入选中
            </button>
          </div>
          <div class="import-toolbar-right">
            <label class="import-update-mode-label" :class="{ active: importUpdateMode }">
              <input type="checkbox" v-model="importUpdateMode" :disabled="importUploading" />
              覆盖已有数据
            </label>
            <button class="sample-btn sample-btn-ghost" @click="cancelImportPreview" :disabled="importUploading">
              取消导入
            </button>
            <button class="sample-btn sample-btn-danger" :disabled="importPreviewData.length === 0 || importUploading" @click="doConfirmImport('all')">
              <Upload :size="14" /> {{ importUploading ? '导入中...' : `全选导入(${importPreviewData.length})` }}
            </button>
            <button class="sample-btn sample-btn-primary" :disabled="importSelectedRows.length === 0 || importUploading" @click="doConfirmImport('selected')">
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
    <div v-if="showBatchImageModal" class="batch-image-modal-overlay" @click.self="closeBatchModal">
      <div class="batch-image-modal">
        <div class="batch-image-modal-header">
          <strong>批量导入图片</strong>
          <button class="modal-close-btn" @click="closeBatchModal">
            <X :size="16" />
          </button>
        </div>
        <div class="batch-image-modal-body">
          <div class="upload-type-group">
            <span class="upload-type-label">编号类型</span>
            <div class="radio-group">
              <label class="radio-item">
                <input type="radio" v-model="batchImageType" value="company-code" />
                <span>公司编号</span>
              </label>
              <label class="radio-item">
                <input type="radio" v-model="batchImageType" value="factory-code" />
                <span>出厂货号</span>
              </label>
            </div>
          </div>
          <div v-if="batchMatched.length === 0"
            class="upload-area"
            @click="$refs.batchFileInput.click()"
            @dragover.prevent="onDragOver"
            @dragleave="onDragLeave"
            @drop.prevent="onBatchDrop"
          >
            <div class="upload-icon"><ImagePlus :size="48" /></div>
            <div class="upload-text">点击或拖拽上传图片文件</div>
            <div class="upload-hint">支持 JPG / PNG，文件名需包含编号</div>
            <input ref="batchFileInput" type="file" accept="image/*" multiple hidden @change="onBatchFileChange" />
          </div>

          <div v-if="batchMatchLoading" style="text-align:center;padding:20px;color:#999">
            正在匹配样品...
          </div>

          <div v-else-if="batchMatched.length > 0" class="batch-match-results">
            <div class="batch-nav">
              <button class="batch-nav-btn" :disabled="batchCurrentIndex === 0" @click="goToPrev">
                <ChevronLeft :size="18" />
              </button>
              <span class="batch-nav-counter">第 {{ batchCurrentIndex + 1 }} / {{ batchMatched.length }} 项</span>
              <button class="batch-nav-btn" :disabled="batchCurrentIndex >= batchMatched.length - 1" @click="goToNext">
                <ChevronRight :size="18" />
              </button>
            </div>

            <div class="batch-match-card-single">
              <template v-if="batchMatched[batchCurrentIndex].matched">
                <div class="bmc-images">
                  <div class="bmc-img-col bmc-new">
                    <div class="bmc-img-label">即将导入的</div>
                    <img :src="batchMatched[batchCurrentIndex].previewUrl" />
                    <div class="bmc-info-row">
                      <span>产品编号: {{ batchMatched[batchCurrentIndex].code }}</span>
                    </div>
                  </div>
                  <div class="bmc-img-col bmc-old">
                    <div class="bmc-img-label">系统原有的</div>
                    <template v-if="batchMatched[batchCurrentIndex].hasExisting && batchMatched[batchCurrentIndex].existingThumb">
                      <img :src="'/thumbnails/' + batchMatched[batchCurrentIndex].existingThumb" />
                      <div class="bmc-info-row">
                        <span>产品编号: {{ batchMatched[batchCurrentIndex].sampleCode || batchMatched[batchCurrentIndex].code }}</span>
                      </div>
                    </template>
                    <div v-else class="bmc-no-image">
                      <ImageIcon :size="24" /> 暂无图片
                    </div>
                  </div>
                </div>
                <div class="bmc-meta">
                  <span>公司编号: <strong>{{ batchMatched[batchCurrentIndex].sampleCode || '-' }}</strong></span>
                  <span>样品名称: <strong>{{ batchMatched[batchCurrentIndex].sampleName || '-' }}</strong></span>
                  <span>出厂货号: <strong>{{ batchMatched[batchCurrentIndex].factoryCode || '-' }}</strong></span>
                </div>
                <div class="bmc-actions">
                  <button class="sample-btn sample-btn-ghost bmc-btn" :class="{ active: batchMatched[batchCurrentIndex].action === 'skip' }" @click="batchMatched[batchCurrentIndex].action = 'skip'">跳过</button>
                  <button class="sample-btn sample-btn-ghost bmc-btn" :class="{ active: batchMatched[batchCurrentIndex].action === 'cover' }" @click="batchMatched[batchCurrentIndex].action = 'cover'">覆盖</button>
                  <button class="sample-btn sample-btn-primary bmc-btn" :class="{ active: batchMatched[batchCurrentIndex].action === 'append' }" @click="batchMatched[batchCurrentIndex].action = 'append'">追加</button>
                  <span class="bmc-remove" @click="removeBatchFile(batchCurrentIndex)"><X :size="14" /></span>
                </div>
              </template>
              <template v-else>
                <div class="bmc-unmatched">
                  <ImageIcon :size="20" />
                  <span>{{ batchMatched[batchCurrentIndex].file.name }} — 未匹配到样品</span>
                  <span class="bmc-remove" @click="removeBatchFile(batchCurrentIndex)"><X :size="14" /></span>
                </div>
              </template>
            </div>

            <div class="batch-match-footer">
              <div class="bmf-left">
                <span>已匹配 {{ batchMatched.filter(m => m.matched).length }} / {{ batchMatched.length }} 项</span>
              </div>
              <div class="bmf-right">
                <button class="sample-btn sample-btn-ghost" @click="setBatchActionAll('skip')">全部跳过</button>
                <button class="sample-btn sample-btn-ghost" @click="setBatchActionAll('cover')">全部覆盖</button>
                <button class="sample-btn sample-btn-primary" @click="setBatchActionAll('append')">全部追加</button>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="sample-btn sample-btn-ghost" @click="closeBatchModal">取消</button>
          <button class="sample-btn sample-btn-primary" :disabled="batchMatched.filter(m => m.matched && m.action !== 'skip').length === 0 || batchUploading" @click="doBatchImageUpload">
            <Upload :size="14" /> {{ batchUploading ? `上传中 ${batchUploadProgress.done}/${batchUploadProgress.total} (成功${batchUploadProgress.success} 失败${batchUploadProgress.fail})` : `开始上传 (${batchMatched.filter(m => m.matched && m.action !== 'skip').length})` }}
          </button>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showAdvancedSearch" class="batch-image-modal-overlay" @click.self="showAdvancedSearch = false">
      <div class="batch-image-modal" style="width:680px">
        <div class="batch-image-modal-header">
          <strong>高级搜索</strong>
          <button class="modal-close-btn" @click="showAdvancedSearch = false">
            <X :size="16" />
          </button>
        </div>
        <div class="batch-image-modal-body">
          <div v-for="(cond, idx) in advSearchConditions" :key="idx" style="display:flex;gap:8px;margin-bottom:10px;align-items:center">
            <select v-model="cond.field" style="flex:1;height:34px;border:1px solid rgba(0,122,255,0.12);border-radius:8px;padding:0 8px;font-size:13px">
              <option v-for="f in allFormFields.filter(x => !x.group)" :key="f.key" :value="f.key">{{ f.label }}</option>
            </select>
            <select v-model="cond.operator" style="width:100px;height:34px;border:1px solid rgba(0,122,255,0.12);border-radius:8px;padding:0 8px;font-size:13px">
              <option value="eq">等于</option>
              <option value="ne">不等于</option>
              <option value="like">包含</option>
              <option value="gt">大于</option>
              <option value="lt">小于</option>
            </select>
            <input v-model="cond.value" style="flex:1;height:34px;border:1px solid rgba(0,122,255,0.12);border-radius:8px;padding:0 8px;font-size:13px" placeholder="值" />
            <button class="sample-btn sample-btn-danger" style="height:34px" @click="advSearchConditions.splice(idx, 1)">
              <Trash2 :size="14" />
            </button>
          </div>
          <button class="sample-btn sample-btn-ghost" @click="advSearchConditions.push({ field: 'sampleName', operator: 'like', value: '' })">
            <Plus :size="14" /> 添加条件
          </button>
        </div>
        <div class="modal-footer">
          <button class="sample-btn sample-btn-ghost" @click="showAdvancedSearch = false">取消</button>
          <button class="sample-btn sample-btn-primary" @click="doAdvancedSearch">
            <Search :size="14" /> 搜索
          </button>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showPrintDropdown" class="sample-more-dropdown-panel" :style="printDropdownStyle">
      <div class="sample-more-item" @click="doPrintMultiCopies">
        <Printer :size="16" /> 多款打印
      </div>
    </div>

    <div v-if="showMultiPrintModal" class="batch-image-modal-overlay" @click.self="showMultiPrintModal = false">
      <div class="batch-image-modal multi-print-modal" style="width:820px;max-height:75vh;display:flex;flex-direction:column">
        <div class="batch-image-modal-header">
          <span>多款打印设置</span>
          <button class="modal-close-btn" @click="showMultiPrintModal = false"><X :size="16" /></button>
        </div>
        <div class="batch-image-modal-body" style="flex:1;overflow:hidden;display:flex;flex-direction:column;padding:16px 20px">
          <div style="display:flex;align-items:center;gap:16px;margin-bottom:12px;flex-shrink:0">
            <div style="display:flex;align-items:center;gap:8px">
              <label style="font-size:13px;font-weight:600;color:#1d1d1f;white-space:nowrap">批量设置张数</label>
              <input type="number" v-model.number="multiPrintBatchCopies" min="1" max="99"
                style="width:64px;height:34px;border-radius:10px;border:1px solid rgba(0,122,255,0.15);outline:none;font-size:14px;font-weight:600;text-align:center;color:#007aff;background:rgba(0,122,255,0.04);padding:0 6px"
                placeholder="1" />
              <button class="sample-btn sample-btn-blue" style="height:34px;font-size:12px" @click="batchSetCopies">应用</button>
            </div>
            <div style="display:flex;align-items:center;gap:10px;margin-left:auto">
              <label class="radio-item-mp" :class="{active:multiPrintType==='barcode'}" @click="multiPrintType='barcode'">
                <span class="radio-dot"></span> 大条码 (50×40mm)
              </label>
              <label class="radio-item-mp" :class="{active:multiPrintType==='quarter'}" @click="multiPrintType='quarter'">
                <span class="radio-dot"></span> 小条码 (25×25mm)
              </label>
            </div>
          </div>
          <div style="flex:1;min-height:0;margin-bottom:12px">
            <vxe-grid
              ref="mpGridRef"
              :columns="mpColumns"
              :data="multiPrintRecords"
              :max-height="480"
              :row-config="{ isHover: true, keyField: 'sampleCode' }"
              :cell-config="{ height: 42 }"
              :header-cell-style="{ background: '#f0f7ff', borderColor: 'rgba(0,122,255,0.12)', color: 'rgba(29,29,31,0.5)', fontWeight: 700, fontSize: '12px', textAlign: 'center' }"
              :cell-style="{ textAlign: 'center' }"
              :border="true"
              :toolbar-config="{ custom: true, refresh: true, zoom: true }"
              :optimization="{ animat: false }"
            >
              <template #copies_edit="{ row }">
                <input type="number" v-model.number="row.copies" min="1" max="99"
                  style="width:56px;height:30px;border-radius:6px;border:1px solid rgba(0,122,255,0.2);text-align:center;font-size:13px;font-weight:600;color:#007aff;outline:none;padding:0 4px"
                  @click.stop />
              </template>
            </vxe-grid>
          </div>
          <div style="flex-shrink:0;text-align:right;font-size:13px;font-weight:700;color:#007aff">
            共 {{ totalPrintPages }} 张标签
          </div>
        </div>
        <div class="modal-footer" style="border-top:1px solid rgba(0,122,255,0.08);padding:12px 20px">
          <button class="sample-btn sample-btn-ghost" @click="showMultiPrintModal = false">取消</button>
          <button class="sample-btn sample-btn-primary" @click="confirmMultiPrint">确定打印</button>
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
          <button class="sample-btn sample-btn-ghost" @click="onConfirmCancel">取消</button>
          <button class="sample-btn sample-btn-danger" @click="onConfirmOk">确定</button>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showAlert" class="batch-image-modal-overlay" @click.self="onAlertClose">
      <div class="batch-result-modal" style="max-width:420px">
        <div class="batch-result-header">
          <strong>提示</strong>
          <button class="modal-close-btn" @click="onAlertClose"><X :size="16" /></button>
        </div>
        <div class="batch-result-body" style="text-align:center;padding:24px 20px">
          <CheckCircle :size="40" style="color:#34c759;margin-bottom:12px" v-if="alertType === 'success'" />
          <AlertCircle :size="40" style="color:#ff3b30;margin-bottom:12px" v-else-if="alertType === 'error'" />
          <AlertTriangle :size="40" style="color:#ff9500;margin-bottom:12px" v-else />
          <p style="font-size:14px;color:#1d1d1f;line-height:1.6;white-space:pre-wrap">{{ alertMessage }}</p>
        </div>
        <div class="modal-footer" style="justify-content:center">
          <button class="sample-btn sample-btn-primary" @click="onAlertClose">知道了</button>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showImageSearchModal" class="batch-image-modal-overlay" @click.self="showImageSearchModal = false">
      <div class="image-search-modal">
        <div class="ism-header">
          <div class="ism-title"><ImageIcon :size="18" /> 图像搜索</div>
          <button class="modal-close-btn" @click="showImageSearchModal = false"><X :size="16" /></button>
        </div>
        <div class="ism-body">
          <div class="ism-upload-section">
            <div class="ism-thumb-grid" ref="thumbGridRef">
              <div v-for="(img, idx) in imageSearchImages" :key="idx"
                   class="ism-thumb-item" :class="{ 'selected': idx === imageSearchSelectedIdx }"
                   @click="selectSearchImage(idx)">
                <img :src="img.url" />
                <div class="thumb-check" v-if="idx === imageSearchSelectedIdx"><CheckCircle :size="16" /></div>
                <button class="thumb-remove" @click.stop="removeSearchImage(idx)"><X :size="12" /></button>
              </div>
              <div class="ism-thumb-add" @click="$refs.imageSearchInput.click()">
                <Plus :size="22" style="color:rgba(0,122,255,0.35)" />
                <span>上传图片<br/>(多图)</span>
                <span class="thumb-count" v-if="imageSearchImages.length">已上传{{ imageSearchImages.length }}张</span>
              </div>
            </div>
            <input ref="imageSearchInput" type="file" accept="image/*" multiple hidden @change="onImageSearchFilesChange" />
          </div>

          <div class="ism-crop-section" v-if="imageSearchSelectedImg">
            <div class="ism-crop-label">拖拽选择搜索区域</div>
            <div class="ism-crop-editor" ref="cropEditorRef"
                 @mousedown.prevent="onCropMouseDown"
                 @mousemove="onCropMouseMove"
                 @mouseup="onCropMouseUp"
                 @mouseleave="onCropMouseUp">
              <img :src="imageSearchSelectedImg.url" ref="cropImgRef" @load="onCropImgLoad" />
              <div class="crop-overlay" :style="cropOverlayStyle"></div>
              <div class="crop-select-box" v-show="cropSelecting || cropDone" :style="cropBoxStyle">
                <div class="crop-handle tl" data-handle="tl" @mousedown.stop.prevent="onHandleDown($event,'tl')"></div>
                <div class="crop-handle tr" data-handle="tr" @mousedown.stop.prevent="onHandleDown($event,'tr')"></div>
                <div class="crop-handle bl" data-handle="bl" @mousedown.stop.prevent="onHandleDown($event,'bl')"></div>
                <div class="crop-handle br" data-handle="br" @mousedown.stop.prevent="onHandleDown($event,'br')"></div>
              </div>
            </div>
            <div class="ism-crop-actions">
              <button class="sample-btn sample-btn-ghost ism-crop-reset-btn" @click="resetCrop" v-if="cropDone">重选区域</button>
              <span class="ism-crop-hint" v-if="!cropDone">按住鼠标拖拽框选产品区域</span>
              <span class="ism-crop-hint ready" v-else>已选区域 {{ cropW }}×{{ cropH }}px · 点击搜索匹配</span>
            </div>
          </div>

          <div class="ism-controls">
            <div class="ism-threshold-group">
              <label>相似度阈值</label>
              <select v-model="imageSearchThreshold">
                <option :value="5">严格 (≤5)</option>
                <option :value="10">适中 (≤10)</option>
                <option :value="15">宽松 (≤15)</option>
              </select>
            </div>
            <button class="sample-btn sample-btn-primary ism-search-btn" @click="doImageSearch"
                    :disabled="!imageSearchSelectedImg || imageSearching">
              <Search :size="14" /> {{ imageSearching ? '搜索中...' : '搜索匹配' }}
            </button>
          </div>

          <div v-if="imageSearchError" style="padding:10px 16px;margin-bottom:12px;border-radius:10px;background:rgba(255,59,48,0.06);color:#ff3b30;font-size:13px;font-weight:600;border:1px solid rgba(255,59,48,0.15)">{{ imageSearchError }}</div>

          <div v-if="imageSearchResults.length > 0" class="ism-results">
            <div class="ism-results-header">
              <span>匹配结果</span>
              <span class="ism-result-count">{{ imageSearchResults.length }} 条</span>
            </div>
            <div class="ism-result-list">
              <div v-for="(item, idx) in imageSearchResults" :key="idx" class="ism-result-card" @click="viewImageSearchResult(item)">
                <img v-if="item.thumbnailPath" :src="'/thumbnails/' + item.thumbnailPath" />
                <div v-else class="ism-no-thumb"><ImageIcon :size="18" /></div>
                <div class="ism-result-info">
                  <div class="ism-result-name">{{ item.sampleCode || '--' }} {{ item.sampleName || '' }}</div>
                  <div class="ism-result-cat">{{ item.category || '-' }}</div>
                </div>
                <div class="ism-score" :class="{ 'high': item.similarity >= 0.8, 'mid': item.similarity >= 0.6 && item.similarity < 0.8 }">
                  {{ item.similarity ? Math.round(item.similarity * 100) : Math.round((1 - item.distance / 64) * 100) }}%
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="imageSearchDone && !imageSearching" class="ism-empty">
            未找到相似图片
          </div>
        </div>
        <div class="ism-footer">
          <button class="sample-btn sample-btn-ghost" @click="showImageSearchModal = false">关闭</button>
        </div>
      </div>
    </div>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount, onActivated, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api'
import '@/styles/sample.css'
import '@/styles/sample-form.css'
import * as XLSX from 'xlsx'
import QRCode from 'qrcode'
import {
  Database, Search, Plus, Pencil, Trash2, Save, X, Upload, Download,
  FileUp, FileDown, FileSpreadsheet, MoreHorizontal, Settings,
  ChevronsUp, ChevronsDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  MapPin, Crosshair, Filter, Columns3, ImagePlus, Coins, Package, DollarSign, Printer,
  Image as ImageIcon, RotateCcw, AlertTriangle, AlertCircle, CheckCircle, CheckCircle as CheckCircleIcon, Info, Video as VideoIcon, List
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()

const allFormFields = [
  { key: 'sampleCode', label: '公司编号', labelWidth: 84, labelJustify: true, width: 180 },
  { group: true, key: 'g-packaging', label: '包装方式', labelWidth: 84, labelJustify: true, width: 0, fields: [
    { key: 'packageCode', placeholder: '包装编号', width: 60 },
    { key: 'packagingCn', placeholder: '中文包装', width: 130 },
    { key: 'packagingEn', placeholder: '英文包装', width: 48 },
  ]},
  { key: 'category', label: '种类名称', labelWidth: 84, labelJustify: true, width: 150 },
  { key: 'sampleName', label: '样品名称', labelWidth: 84, labelJustify: true, width: 500 },
  { key: 'factoryCode', label: '出厂货号', labelWidth: 84, labelJustify: true, width: 180 },
  { group: true, key: 'g-cartonSize', label: '外箱规格', labelWidth: 84, labelJustify: true, width: 0, fields: [
    { key: 'cartonLength', placeholder: '外箱长', width: 80 },
    { key: 'cartonWidth', placeholder: '外箱宽', width: 79 },
    { key: 'cartonHeight', placeholder: '外箱高', width: 79 },
  ]},
  { key: 'factoryPrice', label: '出厂价', labelWidth: 84, labelJustify: true, color: 'red', width: 150 },
  { key: 'remark', label: '中文备注', labelWidth: 84, labelJustify: true, width: 500 },
  { group: true, key: 'g-innerBox', label: '内盒/外箱装量', labelWidth: 84, labelJustify: true, width: 0, fields: [
    { key: 'innerBoxCount', placeholder: '内盒数', width: 60 },
    { key: 'cartonCapacity', placeholder: '装箱量', width: 114 },
  ]},
  { group: true, key: 'g-sampleSize', label: '包装规格', labelWidth: 84, labelJustify: true, width: 0, fields: [
    { key: 'packageLength', placeholder: '包装长', width: 80 },
    { key: 'packageWidth', placeholder: '包装宽', width: 79 },
    { key: 'packageHeight', placeholder: '包装高', width: 79 },
  ]},
  { group: true, key: 'g-cartonVol', label: '材积/体积', labelWidth: 84, labelJustify: true, width: 0, fields: [
    { key: 'cartonMaterialVolume', placeholder: '材积', width: 72 },
    { key: 'cartonVolume', placeholder: '体积', width: 72 },
  ]},
  { key: 'certification', label: '产品认证', labelWidth: 84, labelJustify: true, width: 500 },
  { key: 'englishName', label: '英文名称', labelWidth: 84, labelJustify: true, width: 180 },
  { group: true, key: 'g-productSize', label: '产品规格', labelWidth: 84, labelJustify: true, width: 0, fields: [
    { key: 'sampleLength', placeholder: '产品长', width: 80 },
    { key: 'sampleWidth', placeholder: '产品宽', width: 79 },
    { key: 'sampleHeight', placeholder: '产品高', width: 79 },
  ]},
  { group: true, key: 'g-cartonWeight', label: '外箱毛/净重', labelWidth: 84, labelJustify: true, width: 0, fields: [
    { key: 'cartonGrossWeight', placeholder: '外箱毛重', width: 72 },
    { key: 'cartonNetWeight', placeholder: '外箱净重', width: 72 },
  ]},
  { key: 'batteryInfo', label: '电池信息', labelWidth: 84, labelJustify: true, width: 500 },
  { key: 'supplier', label: '厂商名称', labelWidth: 84, labelJustify: true, width: 180 },
  { key: 'boothNo', label: '摊位号', labelWidth: 84, labelJustify: true, width: 250 },
  { key: 'manufacturerCode', label: '厂商编号', labelWidth: 84, labelJustify: true, width: 150 },
  { key: 'createTime', label: '登记日期', labelWidth: 84, labelJustify: true, width: 195 },
  { key: 'updateTime', label: '修改日期', labelWidth: 84, labelJustify: true, width: 195 },
  { key: 'mobile', label: '手机', labelWidth: 84, labelJustify: true, width: 180 },
  { key: 'contactPhone', label: '电话', labelWidth: 84, labelJustify: true, width: 250 },
  { key: 'qq', label: 'QQ', labelWidth: 84, labelJustify: true, width: 150 },
  { key: 'modifier', label: '修改人', labelWidth: 84, labelJustify: true, width: 195 },
  { key: 'registrant', label: '登记人', labelWidth: 84, labelJustify: true, width: 195 },
  { key: 'contactPerson', label: '联系人', labelWidth: 84, labelJustify: true, width: 180 },
  { key: 'fax', label: '传真', labelWidth: 84, labelJustify: true, width: 250 },
  { key: 'taxPrice', label: '税点价', labelWidth: 84, labelJustify: true, color: 'red', width: 150 },
  { key: 'color', label: '颜色', labelWidth: 84, labelJustify: true, width: 195 },
]

const fieldVisible = reactive({})
allFormFields.forEach(f => { fieldVisible[f.key] = true })

const visibleFormFields = computed(() => allFormFields.filter(f => fieldVisible[f.key]))

const showFieldSettings = ref(false)
const toggleFieldSettings = () => { showFieldSettings.value = !showFieldSettings.value }

const showMultiPrintModal = ref(false)
const multiPrintType = ref('barcode')
const multiPrintBatchCopies = ref(1)
const multiPrintRecords = ref([])
const mpGridRef = ref(null)

const showScanPrintModal = ref(false)
const scanPrintCode = ref('')
const scanPrintResult = ref(null)
const scanPrintImageSrc = ref('')
const scanPrintError = ref('')
const scanPrintType = ref('barcode')
const scanPrintLoading = ref(false)
const scanPrintContinuous = ref(false)
const scanPrintCount = ref(1)
const scanPrintInputRef = ref(null)

const showImageSearchModal = ref(false)
const imageSearchImages = ref([])
const imageSearchSelectedIdx = ref(-1)
const imageSearchSelectedImg = computed(() => {
  const idx = imageSearchSelectedIdx.value
  return idx >= 0 ? imageSearchImages.value[idx] : null
})
const imageSearchThreshold = ref(10)
const imageSearchResults = ref([])
const imageSearchDone = ref(false)
const imageSearching = ref(false)

const thumbGridRef = ref(null)
const cropEditorRef = ref(null)
const cropImgRef = ref(null)
const cropState = reactive({ startX: 0, startY: 0, x: 0, y: 0, w: 0, h: 0, active: false, done: false })
const cropDraggingHandle = ref('')
const cropImgNaturalW = ref(0)
const cropImgNaturalH = ref(0)
const cropDisplayScale = ref(1)
const cropSelecting = computed(() => cropState.active && !cropState.done)
const cropDone = computed(() => cropState.done)
const cropX = computed(() => Math.round(cropState.x))
const cropY = computed(() => Math.round(cropState.y))
const cropW = computed(() => Math.round(Math.abs(cropState.w)))
const cropH = computed(() => Math.round(Math.abs(cropState.h)))
const cropOverlayStyle = computed(() => ({
  display: (cropSelecting.value || cropDone.value) ? 'block' : 'none'
}))
const cropBoxStyle = computed(() => {
  const x = cropState.w < 0 ? cropState.x + cropState.w : cropState.x
  const y = cropState.h < 0 ? cropState.y + cropState.h : cropState.y
  return {
    left: x + 'px',
    top: y + 'px',
    width: Math.abs(cropState.w) + 'px',
    height: Math.abs(cropState.h) + 'px'
  }
})

const mpColumns = [
  { type: 'seq', width: 60, title: '序号' },
  { field: 'sampleCode', title: '公司编号', width: 130, showOverflow: true },
  { field: 'factoryCode', title: '出厂货号', width: 150, showOverflow: true },
  { field: 'sampleName', title: '样品名称', minWidth: 180, showOverflow: true, ellipsis: true },
  { field: 'copies', title: '打印张数', width: 100, slots: { default: 'copies_edit' } }
]

const totalPrintPages = computed(() => {
  return multiPrintRecords.value.reduce((sum, r) => sum + (r.copies || 0), 0)
})

const batchSetCopies = () => {
  const n = multiPrintBatchCopies.value || 1
  multiPrintRecords.value.forEach(r => { r.copies = n })
}

const formExpanded = ref(true)
const formMode = ref('readonly')
const formData = reactive({})

const currentSample = ref(null)
const currentSampleImages = ref([])
const stripIndex = ref(0)

const tableData = ref([])
const tableLoading = ref(false)
const tableWrapHeight = ref(600)
const tableLoaded = ref(false)
const currentPage = ref(1)
const pageSize = ref(2000)
const pageSizeOptions = [500, 1000, 2000, 4000, 5000]
const totalRecords = ref(0)
const totalPages = computed(() => Math.max(1, Math.ceil(totalRecords.value / pageSize.value)))
const currentSortField = ref('createTime')
const currentSortOrder = ref('desc')
const selectedIds = ref([])
const searchKeyword = ref('')
const locateKeyword = ref('')
const manufacturerCode = ref('')

const rowSelectFrom = ref(null)
const rowSelectTo = ref(null)

const gridRef = ref(null)
const tableWrapRef = ref(null)
let resizeObserver = null
let resizeRafId = null
let lastObservedHeight = 0

const showMoreDropdown = ref(false)
const moreDropdownStyle = reactive({ position: 'fixed', zIndex: '99999', top: '0px', left: '0px' })

const showPrintDropdown = ref(false)
const printDropdownStyle = reactive({ position: 'fixed', zIndex: '99999', top: '0px', left: '0px' })

const showPhotoModal = ref(false)
const photoModalSample = ref(null)
const photoModalImages = ref([])
const photoModalIndex = ref(0)
const hideFactoryPrice = ref(false)
const hideSupplierInfo = ref(false)
const photoModalPos = reactive({ x: 0, y: 0 })
const photoModalW = ref(860)

const showImagePreview = ref(false)
const imagePreviewList = ref([])
const imagePreviewIndex = ref(0)
const imagePreviewSelected = ref(new Set())
const posPickerIdx = ref(null)
const ipUploading = ref(false)
const ipUploadDone = ref(0)
const ipUploadTotal = ref(0)
const isAllImageSelected = computed(() => {
  return imagePreviewList.value.length > 0 && imagePreviewSelected.value.size === imagePreviewList.value.length
})
const currentPreviewSrc = computed(() => {
  const img = imagePreviewList.value[imagePreviewIndex.value]
  if (!img) return ''
  return img.filePath ? '/images/' + img.filePath : '/thumbnails/' + img.thumbnailPath
})
const photoModalH = ref(540)
const photoModalInit = () => {
  photoModalW.value = Math.min(860, window.innerWidth - 40)
  photoModalH.value = Math.min(540, window.innerHeight - 40)
  photoModalPos.x = Math.round((window.innerWidth - photoModalW.value) / 2)
  photoModalPos.y = Math.round((window.innerHeight - photoModalH.value) / 2)
  rebuildDetailHtml()
}
const photoModalStyle = computed(() => ({
  display: showPhotoModal.value ? 'flex' : 'none',
  flexDirection: 'column',
  width: photoModalW.value + 'px',
  height: photoModalH.value + 'px',
  top: photoModalPos.y + 'px',
  left: photoModalPos.x + 'px',
  position: 'fixed'
}))
const photoModalDetailHtml = ref('')
const rebuildDetailHtml = () => {
  const d = photoModalSample.value
  if (!d) { photoModalDetailHtml.value = ''; return }
  const v = (k) => { const x = d[k]; return (x != null && x !== '') ? String(x) : ''; }
  const f = (label, val, cls) => `<div class="spm-field"><span class="spm-field-label">${label}</span><span class="spm-field-value${cls ? ' ' + cls : ''}">${val || '-'}</span></div>`
  const ff = (label, val, cls) => `<div class="spm-field spm-field-full"><span class="spm-field-label">${label}</span><span class="spm-field-value${cls ? ' ' + cls : ''}">${val || '-'}</span></div>`
  const row = (...args) => `<div class="spm-field-row">${args.join('')}</div>`
  const rowS = (section, ...args) => {
    const hidden = (section === 'factory-price' && hideFactoryPrice.value) || (section === 'supplier-info' && hideSupplierInfo.value)
    return `<div class="spm-field-row${hidden ? ' spm-hidden' : ''}" data-spm-section="${section}">${args.join('')}</div>`
  }
  photoModalDetailHtml.value =
    row(ff('样品名称', v('sampleName'))) +
    row(f('公司编号', v('sampleCode')), f('出厂货号', v('factoryCode'))) +
    rowS('factory-price', f('出厂价', v('factoryPrice'), 'spm-price'), f('报出价', v('taxPrice'), 'spm-price')) +
    row(f('包装规格', v('packagingCn')), f('内盒/装箱量', (v('innerBoxCount') || '-') + ' / ' + (v('cartonCapacity') || '-'))) +
    row(f('外箱规格', (v('cartonLength')||'-') + '×' + (v('cartonWidth')||'-') + '×' + (v('cartonHeight')||'-') + ' CM'), f('外箱毛/净重', (v('cartonGrossWeight')||'-') + ' / ' + (v('cartonNetWeight')||'-') + ' KG')) +
    row(f('产品规格', (v('sampleLength')||'-') + '×' + (v('sampleWidth')||'-') + '×' + (v('sampleHeight')||'-') + ' CM'), f('产品毛/净重', (v('sampleGrossWeight')||'-') + ' / ' + (v('sampleNetWeight')||'-') + ' KG')) +
    row(f('体积/材积', (v('cartonVolume')||'-') + ' / ' + (v('cartonMaterialVolume')||'-')), f('包装', v('packagingEn') || v('packagingCn') || '-')) +
    row(f('摊位号', v('boothNo')), f('电池信息', v('batteryInfo'))) +
    row(ff('产品认证', v('certification'))) +
    row(ff('产品备注', v('remark'))) +
    '<div class="spm-section-title">厂商信息</div>' +
    rowS('supplier-info', f('厂商编号', v('manufacturerCode')), f('厂商名称', v('supplier'))) +
    rowS('supplier-info', f('联系人', v('contactPerson')), f('电话', v('contactPhone'))) +
    rowS('supplier-info', f('手机', v('mobile')), f('QQ', v('qq')))
}
let dragStart = null
const startDragModal = (e) => {
  dragStart = { x: e.clientX - photoModalPos.x, y: e.clientY - photoModalPos.y }
  const onMove = (ev) => {
    photoModalPos.x = Math.max(0, Math.min(ev.clientX - dragStart.x, window.innerWidth - photoModalW.value))
    photoModalPos.y = Math.max(0, Math.min(ev.clientY - dragStart.y, window.innerHeight - photoModalH.value))
  }
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
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

const showRestoreDeletedModal = ref(false)
const deletedGridRef = ref(null)
const deletedData = ref([])
const deletedLoading = ref(false)
const deletedTotal = ref(0)
const deletedSelected = ref([])

const deletedAllData = ref([])
const deletedFilterField = ref('')
const deletedFilterKeyword = ref('')
const deletedFullscreen = ref(false)
const deletedFullscreenSearch = ref('')
const deletedGridMaxHeight = computed(() => deletedFullscreen.value ? window.innerHeight - 120 : 480)
const showDeletedBatchQuery = ref(false)
const deletedBatchField = ref('sampleCode')
const deletedBatchInput = ref('')
const showMainBatchQuery = ref(false)
const mainBatchField = ref('sampleCode')
const mainBatchInput = ref('')
const mainBatchQueryActive = ref(false)
const deletedSortMethod = ({ data, sortList }) => {
  if (!sortList || sortList.length === 0) return data
  const { field, order } = sortList[0]
  return data.sort((a, b) => {
    const va = String(a[field] ?? '')
    const vb = String(b[field] ?? '')
    const cmp = va.localeCompare(vb)
    return order === 'asc' ? cmp : -cmp
  })
}
const deletedFilterActive = ref(false)

const deletedGridColumns = [
  { type: 'checkbox', width: 44, fixed: 'left' },
  { field: 'id', title: 'ID', width: 70, sortable: true, sortType: 'number' },
  { field: 'sampleCode', title: '公司编号', width: 130, showOverflow: true, sortable: true },
  { field: 'manufacturerCode', title: '厂商编号', width: 100, sortable: true },
  { field: 'sampleName', title: '样品名称', minWidth: 180, showOverflow: true, sortable: true },
  { field: 'category', title: '种类', width: 110, sortable: true },
  { field: 'factoryCode', title: '出厂货号', width: 110, sortable: true },
  { field: 'registrant', title: '登记人', width: 90, sortable: true },
  { field: 'updateTime', title: '删除时间', width: 160, sortable: true, formatter: ({ cellValue }) => cellValue ? new Date(cellValue).toLocaleString('zh-CN', {year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit'}) : '-' }
]
const importProgressText = ref('')
const importUpdateMode = ref(false)

const showConfirm = ref(false)
const confirmMessage = ref('')
let confirmResolve = null

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

const showAlert = ref(false)
const alertMessage = ref('')
const alertType = ref('info')
let alertResolve = null

const showAlertDialog = (msg, type = 'info') => {
  return new Promise((resolve) => {
    alertMessage.value = msg
    alertType.value = type
    alertResolve = resolve
    showAlert.value = true
  })
}

const onAlertClose = () => {
  showAlert.value = false
  if (alertResolve) alertResolve()
}

const HEADER_TO_FIELD = {
  '厂商编号': 'manufacturerCode', '公司编号': 'sampleCode', '种类编号': 'category',
  '种类名称': 'category', '样品名称': 'sampleName', '英文名称': 'englishName',
  '出厂货号': 'factoryCode', '样品单位': 'sampleUnit', '样品英文单位': 'sampleUnitEn',
  '中文包装': 'packagingCn', '英文包装': 'packagingEn', '包装编号': 'packageCode', '出厂价': 'factoryPrice',
  '价格': 'factoryPrice', '税点价': 'taxPrice', '样品长度': 'sampleLength',
  '样品 长度': 'sampleLength', '样品宽度': 'sampleWidth', '样品高度': 'sampleHeight',
  '样品毛重': 'sampleGrossWeight', '样品净重': 'sampleNetWeight', '外箱长度': 'cartonLength',
  '外箱宽度': 'cartonWidth', '外箱高度': 'cartonHeight', '外箱材积': 'cartonMaterialVolume',
  '外箱体积': 'cartonVolume', '内盒个数': 'innerBoxCount', '外箱装量': 'cartonCapacity',
  '装箱单位': 'packingUnit', '外箱毛重': 'cartonGrossWeight', '外箱净重': 'cartonNetWeight',
  '包装长度': 'packageLength', '包装宽度': 'packageWidth', '包装高度': 'packageHeight',
  '产品认证': 'certification', '认证总数': 'certificationCount', '颜色': 'color',
  '英文颜色': 'colorEn', '备注': 'remark', '英文备注': 'remarkEn',
  '厂商名称': 'supplier', '摊位号': 'boothNo', '联系人': 'contactPerson',
  '电话': 'contactPhone', '手机': 'mobile', '传真': 'fax', 'QQ': 'qq',
  '登记人': 'registrant', '修改人': 'modifier', '侵权': 'infringement',
  '电池信息': 'batteryInfo', '电话/信息': 'contactPhone'
}

const EDIT_RENDER = { name: 'input' }

const IMPORT_PREVIEW_ALL_COLUMNS = [
  { type: 'checkbox', width: 44, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'manufacturerCode', title: '厂商编号', width: 110, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'sampleCode', title: '公司编号', width: 110, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'category', title: '种类名称', width: 110, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'sampleName', title: '样品名称', width: 140, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'englishName', title: '英文名称', width: 130, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'factoryCode', title: '出厂货号', width: 120, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'sampleUnit', title: '样品单位', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'sampleUnitEn', title: '英文单位', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'packagingCn', title: '中文包装', width: 100, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packageCode', title: '包装编号', width: 100, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packagingEn', title: '英文包装', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'factoryPrice', title: '出厂价', width: 100, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'taxPrice', title: '税点价', width: 100, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'color', title: '颜色', width: 80, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'colorEn', title: '英文颜色', width: 80, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'sampleLength', title: '样品长', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'sampleWidth', title: '样品宽', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'sampleHeight', title: '样品高', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'sampleGrossWeight', title: '毛重', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'sampleNetWeight', title: '净重', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'cartonLength', title: '箱长', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'cartonWidth', title: '箱宽', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'cartonHeight', title: '箱高', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'cartonGrossWeight', title: '箱毛重', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'cartonNetWeight', title: '箱净重', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'cartonMaterialVolume', title: '材积', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'cartonVolume', title: '体积', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'innerBoxCount', title: '内盒数', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'cartonCapacity', title: '装箱量', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'packingUnit', title: '装箱单位', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'packageLength', title: '包装长', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'packageWidth', title: '包装宽', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'packageHeight', title: '包装高', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'supplier', title: '厂商名称', width: 140, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'boothNo', title: '摊位号', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'contactPerson', title: '联系人', width: 90, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'contactPhone', title: '电话', width: 120, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'mobile', title: '手机', width: 120, editRender: EDIT_RENDER, visible: false },
  { field: 'fax', title: '传真', width: 120, editRender: EDIT_RENDER, visible: false },
  { field: 'qq', title: 'QQ', width: 90, editRender: EDIT_RENDER, visible: false },
  { field: 'certification', title: '产品认证', width: 100, editRender: EDIT_RENDER, visible: false },
  { field: 'certificationCount', title: '认证数', width: 70, editRender: EDIT_RENDER, visible: false },
  { field: 'remark', title: '备注', width: 140, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'remarkEn', title: '英文备注', width: 140, editRender: EDIT_RENDER, visible: false },
  { field: 'registrant', title: '登记人', width: 90, editRender: EDIT_RENDER, visible: false },
  { field: 'infringement', title: '侵权', width: 80, editRender: EDIT_RENDER, visible: false },
  { field: 'batteryInfo', title: '电池信息', width: 100, editRender: EDIT_RENDER, visible: false },
  { title: '操作', width: 120, fixed: 'right', slots: { default: 'import_action' } }
]

const showBatchImageModal = ref(false)
const batchImageType = ref('company-code')
const batchFiles = ref([])
const batchUploading = ref(false)
const batchUploadProgress = ref({ done: 0, total: 0, success: 0, fail: 0 })
const batchMatched = ref([])
const batchMatchLoading = ref(false)
const batchCurrentIndex = ref(0)

const showBatchVideoModal = ref(false)
const batchVideoFiles = ref([])
const batchVideoMatched = ref([])
const videoMatchLoading = ref(false)
const videoUploading = ref(false)
const videoUploadProgress = ref({ done: 0, total: 0, success: 0, fail: 0, currentFileName: '', currentProgress: 0 })
const videoCurrentIndex = ref(0)
const batchVideoType = ref('company-code')
const customMatchSubType = ref('company-code')
const customManufacturerCode = ref('')
const customCodesText = ref('')

const showVideoPreviewModal = ref(false)
const sampleVideos = ref([])
const videoPreviewIndex = ref(0)

const goToPrev = () => {
  if (batchCurrentIndex.value > 0) batchCurrentIndex.value--
}
const goToNext = () => {
  if (batchCurrentIndex.value < batchMatched.value.length - 1) batchCurrentIndex.value++
}

const showAdvancedSearch = ref(false)
const advSearchConditions = reactive([
  { field: 'sampleName', operator: 'like', value: '' }
])

const allColumns = [
  { type: 'checkbox', width: 44, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'image', title: '图片', width: 90, sortable: true, slots: { default: 'image_default' } },
  { field: 'manufacturerCode', title: '厂商编号', width: 110, showOverflow: true, sortable: true, visible: false },
  { field: 'sampleCode', title: '公司编号', width: 140, showOverflow: true, sortable: true },
  { field: 'category', title: '种类名称', width: 110, showOverflow: true, visible: false },
  { field: 'sampleName', title: '样品名称', width: 850, showOverflow: true, sortable: true },
  { field: 'englishName', title: '英文名称', width: 150, showOverflow: true, visible: false },
  { field: 'factoryCode', title: '出厂货号', width: 140, showOverflow: true, sortable: true },
  { field: 'sampleUnit', title: '样品单位', width: 100, showOverflow: true, visible: false },
  { field: 'sampleUnitEn', title: '英文单位', width: 100, showOverflow: true, visible: false },
  { field: 'packagingCn', title: '中文包装', width: 120, showOverflow: true, sortable: true },
  { field: 'packageCode', title: '包装编号', width: 100, showOverflow: true, visible: false },
  { field: 'packagingEn', title: '英文包装', width: 100, showOverflow: true, visible: false },
  { field: 'material', title: '材质', width: 80, showOverflow: true, visible: false },
  { field: 'color', title: '颜色', width: 80, showOverflow: true, visible: false },
  { field: 'colorEn', title: '英文颜色', width: 90, showOverflow: true, visible: false },
  { field: 'size', title: '规格尺寸', width: 100, showOverflow: true, visible: false },
  { field: 'weight', title: '样品重量', width: 100, showOverflow: true, visible: false },
  { field: 'origin', title: '产地', width: 80, showOverflow: true, visible: false },
  { field: 'factoryPrice', title: '出厂价', width: 100, showOverflow: true, cellStyle: { color: '#ff3b30' }, sortable: true },
  { field: 'taxPrice', title: '税点价', width: 100, showOverflow: true, visible: false },
  { field: 'sampleLength', title: '样品长', width: 120, showOverflow: true, sortable: true },
  { field: 'sampleWidth', title: '样品宽', width: 120, showOverflow: true, sortable: true },
  { field: 'sampleHeight', title: '样品高', width: 120, showOverflow: true, sortable: true },
  { field: 'sampleGrossWeight', title: '样品毛重', width: 120, showOverflow: true, sortable: true },
  { field: 'sampleNetWeight', title: '样品净重', width: 120, showOverflow: true, sortable: true },
  { field: 'cartonLength', title: '外箱长', width: 120, showOverflow: true, sortable: true },
  { field: 'cartonWidth', title: '外箱宽', width: 120, showOverflow: true, sortable: true },
  { field: 'cartonHeight', title: '外箱高', width: 120, showOverflow: true, sortable: true },
  { field: 'cartonMaterialVolume', title: '材积', width: 80, showOverflow: true, visible: false },
  { field: 'cartonVolume', title: '体积', width: 80, showOverflow: true, visible: false },
  { field: 'innerBoxCount', title: '内盒数', width: 110, showOverflow: true, sortable: true },
  { field: 'cartonCapacity', title: '装箱量', width: 110, showOverflow: true, sortable: true },
  { field: 'packingUnit', title: '装箱单位', width: 85, showOverflow: true, visible: false },
  { field: 'packageLength', title: '包装长', width: 120, showOverflow: true, sortable: true },
  { field: 'packageWidth', title: '包装宽', width: 120, showOverflow: true, sortable: true },
  { field: 'packageHeight', title: '包装高', width: 120, showOverflow: true, sortable: true },
  { field: 'cartonGrossWeight', title: '外箱毛重', width: 120, showOverflow: true, sortable: true },
  { field: 'cartonNetWeight', title: '外箱净重', width: 120, showOverflow: true, sortable: true },
  { field: 'supplier', title: '厂商名称', minWidth: 140, showOverflow: true, visible: false },
  { field: 'boothNo', title: '摊位号', width: 80, showOverflow: true, visible: false },
  { field: 'contactPerson', title: '联系人', width: 90, showOverflow: true, visible: false },
  { field: 'contactPhone', title: '电话', width: 120, showOverflow: true, visible: false },
  { field: 'mobile', title: '手机', width: 120, showOverflow: true, visible: false },
  { field: 'fax', title: '传真', width: 120, showOverflow: true, visible: false },
  { field: 'qq', title: 'QQ', width: 90, showOverflow: true, visible: false },
  { field: 'certification', title: '产品认证', width: 800, showOverflow: true, sortable: true },
  { field: 'certificationCount', title: '认证数', width: 70, showOverflow: true, visible: false },
  { field: 'description', title: '描述', width: 140, showOverflow: true, visible: false },
  { field: 'remark', title: '备注', width: 800, showOverflow: true, sortable: true },
  { field: 'remarkEn', title: '英文备注', width: 140, showOverflow: true, visible: false },
  { field: 'registrant', title: '登记人', width: 90, showOverflow: true, visible: false },
  { field: 'infringement', title: '侵权', width: 70, showOverflow: true, visible: false },
  { field: 'batteryInfo', title: '电池信息', width: 100, showOverflow: true, visible: false },
  { field: 'createTime', title: '登记日期', width: 114, formatter: ({ cellValue }) => cellValue ? String(cellValue).substring(0, 10) : '', visible: false },
  { field: 'action', title: '操作', width: 82, fixed: 'right', slots: { default: 'action_default' }, visible: false }
]

const loadTableData = async () => {
  tableLoading.value = true
  try {
    let endpoint = `/samples?current=${currentPage.value}&size=${pageSize.value}&sortField=${currentSortField.value}&sortOrder=${currentSortOrder.value}`
    if (manufacturerCode.value) endpoint += `&manufacturerCode=${encodeURIComponent(manufacturerCode.value)}`
    if (searchKeyword.value) endpoint += `&keyword=${encodeURIComponent(searchKeyword.value)}`
    const res = await api(endpoint)
    if (res.code === 200 || res.data) {
      const data = res.data || res
      tableData.value = data.records || data.list || data || []
      totalRecords.value = data.total || tableData.value.length
    } else {
      tableData.value = res.records || res.list || []
      totalRecords.value = res.total || tableData.value.length
    }
  } catch (e) {
    console.error(e)
  } finally {
    tableLoading.value = false
  }
}

const fetchImagesForSample = async (sampleId) => {
  try {
    const firstRes = await api(`/images/sample-images?ids=${sampleId}`)
    const firstRaw = firstRes.data || firstRes || []
    const firstImages = Array.isArray(firstRaw) ? firstRaw : []
    if (firstImages.length > 0) {
      currentSampleImages.value = firstImages
      stripIndex.value = 0
    }

    const fullRes = await api(`/images/sample/${sampleId}`)
    const fullRaw = fullRes.data || fullRes || []
    const fullImages = Array.isArray(fullRaw) ? fullRaw : []
    if (fullImages.length > 0) {
      currentSampleImages.value = fullImages
      stripIndex.value = 0
    }
  } catch (e) {
    if (currentSampleImages.value.length === 0) {
      currentSampleImages.value = []
    }
  }
}

const loadSampleVideos = async (sampleId) => {
  try {
    const res = await api(`/videos/sample/${sampleId}`)
    const data = res.data || res || []
    sampleVideos.value = Array.isArray(data) ? data : []
    videoPreviewIndex.value = 0
  } catch (e) {
    sampleVideos.value = []
  }
}

const deleteSampleVideo = async (videoId, idx) => {
  if (!(await showConfirmDialog('确定删除此视频？'))) return
  try {
    await api(`/videos/${videoId}`, { method: 'DELETE' })
    sampleVideos.value.splice(idx, 1)
    if (sampleVideos.value.length === 0) {
      showVideoPreviewModal.value = false
    } else if (videoPreviewIndex.value >= sampleVideos.value.length) {
      videoPreviewIndex.value = sampleVideos.value.length - 1
    }
  } catch (e) {
    showAlertDialog('删除失败: ' + (e.message || '未知错误'), 'error')
  }
}

const posLabel = (idx) => {
  if (idx === 0) return '主图'
  return String(idx)
}

const togglePosPicker = (idx) => {
  posPickerIdx.value = posPickerIdx.value === idx ? null : idx
}

const setImagePosition = async (idx, newPos) => {
  const img = imagePreviewList.value[idx]
  if (!img || !img.id) { posPickerIdx.value = null; return }
  posPickerIdx.value = null
  const list = [...imagePreviewList.value]
  const [moved] = list.splice(idx, 1)
  list.splice(newPos, 0, moved)
  const items = list.map(i => ({ id: i.id, hash: i.hash })).filter(it => it.id)
  const result = await api('/images/reorder', { method: 'POST', body: JSON.stringify(items) })
  if (result && result.code === 200) {
    showAlertDialog('位置已更新', 'success')
    if (currentSample.value?.id) {
      await fetchImagesForSample(currentSample.value.id)
    }
    imagePreviewList.value = [...currentSampleImages.value]
    imagePreviewIndex.value = 0
    imagePreviewSelected.value = new Set()
    const sampleId = currentSample.value?.id
    if (sampleId && currentSampleImages.value.length > 0) {
      const firstImg = currentSampleImages.value[0]
      const row = tableData.value.find(r => r.id === sampleId)
      if (row) {
        row.thumbnail = firstImg.thumbnailPath
        row.firstImageId = firstImg.id
      }
    }
  } else {
    showAlertDialog('设置失败', 'error')
  }
}

const toggleSelectImage = (idx) => {
  const next = new Set(imagePreviewSelected.value)
  if (next.has(idx)) {
    next.delete(idx)
  } else {
    next.add(idx)
  }
  imagePreviewSelected.value = next
}

const toggleSelectAllImages = () => {
  if (isAllImageSelected.value) {
    imagePreviewSelected.value = new Set()
  } else {
    imagePreviewSelected.value = new Set(imagePreviewList.value.map((_, i) => i))
  }
}

const deleteSelectedImages = async () => {
  const selected = [...imagePreviewSelected.value].sort((a, b) => b - a)
  if (selected.length === 0) return
  if (!(await showConfirmDialog(`确定删除选中的 ${selected.length} 张图片？`))) return

  const items = selected.map(i => {
    const img = imagePreviewList.value[i]
    return img ? { id: img.id, hash: img.hash } : null
  }).filter(Boolean)
  if (items.length === 0) return

  try {
    await api('/images/batch-delete', { method: 'POST', body: JSON.stringify(items) })
    selected.forEach(i => {
      const img = imagePreviewList.value[i]
      if (img) {
        currentSampleImages.value = currentSampleImages.value.filter(item => item.hash !== img.hash)
      }
    })
    imagePreviewList.value = imagePreviewList.value.filter((_, i) => !selected.includes(i))
    imagePreviewSelected.value = new Set()
    if (imagePreviewList.value.length === 0) {
      showImagePreview.value = false
    } else if (imagePreviewIndex.value >= imagePreviewList.value.length) {
      imagePreviewIndex.value = imagePreviewList.value.length - 1
    }
    const sampleId = currentSample.value?.id
    const row = sampleId && tableData.value.find(r => r.id === sampleId)
    if (row) {
      if (imagePreviewList.value.length > 0) {
        row.thumbnail = imagePreviewList.value[0].thumbnailPath
        row.firstImageId = imagePreviewList.value[0].id
      } else {
        row.thumbnail = null
        row.firstImageId = null
      }
    }
  } catch (e) {
    showAlertDialog('批量删除失败: ' + (e.message || '未知错误'), 'error')
  }
}

const onPreviewUpload = async (e) => {
  const files = e.target.files
  if (!files || files.length === 0) return
  const sampleId = currentSample.value?.id
  if (!sampleId) return
  ipUploading.value = true
  ipUploadTotal.value = files.length
  ipUploadDone.value = 0
  let successCount = 0
  for (const file of files) {
    try {
      const fd = new FormData()
      fd.append('file', file)
      fd.append('sampleId', sampleId)
      const res = await api('/images/upload', { method: 'POST', body: fd })
      if (res && res.code === 200) successCount++
    } catch (_) {}
    ipUploadDone.value++
  }
  ipUploading.value = false
  e.target.value = ''
  await fetchImagesForSample(sampleId)
  imagePreviewList.value = currentSampleImages.value
  imagePreviewIndex.value = 0
  imagePreviewSelected.value = new Set()
  if (successCount > 0 && successCount === ipUploadTotal.value) {
    showAlertDialog(`成功上传 ${successCount} 张图片`, 'success')
  } else if (successCount > 0) {
    showAlertDialog(`上传完成: 成功 ${successCount} 张, 失败 ${ipUploadTotal.value - successCount} 张`, 'info')
  } else {
    showAlertDialog('上传失败, 请重试', 'error')
  }
}

const viewOriginal = () => {
  if (!currentSample.value || currentSampleImages.value.length === 0) return
  imagePreviewList.value = currentSampleImages.value
  imagePreviewIndex.value = stripIndex.value
  imagePreviewSelected.value = new Set()
  showImagePreview.value = true
}

const deletePreviewImage = async () => {
  const img = imagePreviewList.value[imagePreviewIndex.value]
  if (!img || !img.id) return
  if (!(await showConfirmDialog('确定删除此图片？'))) return
  try {
    await api('/images/batch-delete', { method: 'POST', body: JSON.stringify([{ id: img.id, hash: img.hash }]) })
    imagePreviewList.value.splice(imagePreviewIndex.value, 1)
    currentSampleImages.value = currentSampleImages.value.filter(i => i.hash !== img.hash)
    if (imagePreviewList.value.length === 0) {
      showImagePreview.value = false
    } else if (imagePreviewIndex.value >= imagePreviewList.value.length) {
      imagePreviewIndex.value = imagePreviewList.value.length - 1
    }
    const sampleId = currentSample.value?.id
    const row = sampleId && tableData.value.find(r => r.id === sampleId)
    if (row) {
      if (imagePreviewList.value.length > 0) {
        row.thumbnail = imagePreviewList.value[0].thumbnailPath
        row.firstImageId = imagePreviewList.value[0].id
      } else {
        row.thumbnail = null
        row.firstImageId = null
      }
    }
  } catch (e) {
    showAlertDialog('删除失败: ' + (e.message || '未知错误'), 'error')
  }
}

const onSearch = () => {
  mainBatchQueryActive.value = false
  currentPage.value = 1
  loadTableData()
}

const onLocate = () => {
  const keyword = locateKeyword.value.trim()
  if (!keyword) return
  const idx = tableData.value.findIndex(r =>
    Object.values(r).some(v => String(v).toLowerCase().includes(keyword.toLowerCase()))
  )
  if (idx >= 0 && gridRef.value) {
    gridRef.value.setCurrentRow(tableData.value[idx])
    selectSample(tableData.value[idx])
    gridRef.value.scrollToRow(tableData.value[idx])
  }
}

const clearSearch = () => {
  mainBatchQueryActive.value = false
  searchKeyword.value = ''
  locateKeyword.value = ''
  currentPage.value = 1
  loadTableData()
}

const goPage = (p) => {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  loadTableData()
}

const onSortChange = ({ property, order }) => {
  if (property) {
    currentSortField.value = property === 'image' ? 'hasThumbnail' : property
  }
  currentSortOrder.value = order || 'desc'
  currentPage.value = 1
  loadTableData()
}

const onCheckboxChange = () => {
  updateSelectedIds()
}

const onCheckboxAll = () => {
  updateSelectedIds()
}

const updateSelectedIds = () => {
  if (!gridRef.value) return
  const records = gridRef.value.getCheckboxRecords()
  selectedIds.value = records.map(r => r.id)
}

const onCellClick = ({ row }) => {
  selectSample(row)
}

const selectSample = (row) => {
  currentSample.value = row
  sampleVideos.value = []
  videoPreviewIndex.value = 0
  Object.keys(formData).forEach(k => delete formData[k])
  if (row) {
    Object.assign(formData, formatFormDate(row))
    if (row.thumbnail) {
      currentSampleImages.value = [{ thumbnailPath: row.thumbnail, filePath: row.thumbnail }]
    } else {
      currentSampleImages.value = []
    }
    stripIndex.value = 0
    fetchImagesForSample(row.id)
    loadSampleVideos(row.id)
  } else {
    currentSampleImages.value = []
  }
  if (formMode.value === 'readonly') {
    formMode.value = 'readonly'
  }
}

const formatFormDate = (obj) => {
  const result = { ...obj };
  ['createTime', 'updateTime'].forEach(key => {
    if (result[key] && typeof result[key] === 'string') {
      result[key] = result[key].replace('T', ' ')
    }
  })
  return result
}

const startAdd = () => {
  formMode.value = 'add'
  currentSample.value = null
  Object.keys(formData).forEach(k => delete formData[k])
  currentSampleImages.value = []
  stripIndex.value = 0
}

const startEdit = () => {
  if (!currentSample.value) return
  formMode.value = 'edit'
}

const cancelEdit = () => {
  formMode.value = 'readonly'
  if (currentSample.value) {
    Object.keys(formData).forEach(k => delete formData[k])
    Object.assign(formData, formatFormDate(currentSample.value))
  }
}

const resetForm = () => {
  if (currentSample.value) {
    Object.keys(formData).forEach(k => delete formData[k])
    Object.assign(formData, formatFormDate(currentSample.value))
  }
}

const saveSample = async () => {
  try {
    const payload = { ...formData }
    if (formMode.value === 'add') {
      const res = await api('/samples', { method: 'POST', body: JSON.stringify(payload) })
      if (res.code === 200 || res.id) {
        formMode.value = 'readonly'
        await loadTableData()
      }
    } else if (formMode.value === 'edit') {
      const id = payload.id || currentSample.value?.id
      const res = await api(`/samples/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
      if (res.code === 200 || res.id) {
        formMode.value = 'readonly'
        await loadTableData()
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const editRow = async (row) => {
  try {
    const res = await api(`/samples/${row.id}`)
    if (res && res.code === 200 && res.data) {
      Object.assign(row, res.data)
    }
  } catch (e) { console.error(e) }
  selectSample(row)
  startEdit()
}

const deleteRow = async (row) => {
  const ok = await showConfirmDialog('确认删除该样品？')
  if (!ok) return
  try {
    await api(`/samples/${row.id}`, { method: 'DELETE' })
    if (currentSample.value?.id === row.id) {
      currentSample.value = null
      currentSampleImages.value = []
    }
    await loadTableData()
  } catch (e) {
    console.error(e)
  }
}

const onDeleteSelected = async () => {
  if (selectedIds.value.length === 0) return
  const ok = await showConfirmDialog(`确认删除选中的 ${selectedIds.value.length} 条记录？`)
  if (!ok) return
  try {
    await api('/samples/batch-delete', {
      method: 'POST',
      body: JSON.stringify(selectedIds.value)
    })
    selectedIds.value = []
    await loadTableData()
  } catch (e) {
    console.error(e)
  }
}

const selectRowRange = () => {
  if (!gridRef.value) return
  const from = rowSelectFrom.value || 0
  const to = rowSelectTo.value || tableData.value.length - 1
  const start = Math.max(0, Math.min(from, to) - 1)
  const end = Math.min(tableData.value.length - 1, Math.max(from, to) - 1)
  gridRef.value.clearCheckboxRow()
  for (let i = start; i <= end; i++) {
    gridRef.value.setCheckboxRow(tableData.value[i], true)
  }
  updateSelectedIds()
}

const selectAllRows = () => {
  if (!gridRef.value) return
  gridRef.value.setAllCheckboxRow(true)
  updateSelectedIds()
}

const invertSelection = () => {
  if (!gridRef.value) return
  tableData.value.forEach(row => {
    gridRef.value.setCheckboxRow(row, !gridRef.value.isCheckedByCheckboxRow(row))
  })
  updateSelectedIds()
}

const clearSelection = () => {
  if (!gridRef.value) return
  gridRef.value.clearCheckboxRow()
  selectedIds.value = []
}

const stripPrev = () => {
  if (stripIndex.value > 0) stripIndex.value--
}

const stripNext = () => {
  if (stripIndex.value < currentSampleImages.value.length - 1) stripIndex.value++
}

const onImageUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const sampleId = currentSample.value?.id || formData.id
  if (!sampleId) return
  try {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('sampleId', sampleId)
    const res = await api('/images/upload', { method: 'POST', body: fd })
    if (res && res.code === 200 && res.data) {
      const row = tableData.value.find(r => r.id === sampleId)
      if (row) {
        row.thumbnail = res.data.thumbnailPath
        row.firstImageId = res.data.id
      }
      if (!currentSample.value.thumbnail) {
        currentSample.value.thumbnail = res.data.thumbnailPath
      }
    }
    await fetchImagesForSample(sampleId)
  } catch (err) {
    console.error(err)
  }
  e.target.value = ''
}

const openPhotoModal = () => {
  if (!currentSample.value) return
  openPhotoModalFor(currentSample.value)
}

const openPhotoModalFor = (row) => {
  photoModalSample.value = row
  photoModalIndex.value = 0
  if (row.thumbnail) {
    photoModalImages.value = [{ thumbnailPath: row.thumbnail, filePath: row.thumbnail }]
  } else {
    photoModalImages.value = []
  }
  photoModalInit()
  showPhotoModal.value = true
  fetchPhotoModalImages(row.id)
}

const fetchPhotoModalImages = async (sampleId) => {
  try {
    const firstRes = await api(`/images/sample-images?ids=${sampleId}`)
    const firstRaw = firstRes.data || firstRes || []
    const firstImages = Array.isArray(firstRaw) ? firstRaw : []
    if (firstImages.length > 0) {
      photoModalImages.value = firstImages
    }

    const fullRes = await api(`/images/sample/${sampleId}`)
    const fullRaw = fullRes.data || fullRes || []
    const fullImages = Array.isArray(fullRaw) ? fullRaw : []
    if (fullImages.length > 0) {
      photoModalImages.value = fullImages
    }
  } catch (e) {
    if (photoModalImages.value.length === 0) {
      photoModalImages.value = []
    }
  }
}

const photoModalPrev = () => {
  if (photoModalIndex.value > 0) photoModalIndex.value--
}

const photoModalNext = () => {
  if (photoModalIndex.value < photoModalImages.value.length - 1) photoModalIndex.value++
}

const toggleMoreDropdown = (e) => {
  showMoreDropdown.value = !showMoreDropdown.value
  if (showMoreDropdown.value) {
    const rect = e.currentTarget.getBoundingClientRect()
    moreDropdownStyle.top = (rect.bottom + 4) + 'px'
    moreDropdownStyle.left = Math.max(4, rect.right - 280) + 'px'
  }
}

const togglePrintDropdown = (e) => {
  showPrintDropdown.value = !showPrintDropdown.value
  if (showPrintDropdown.value) {
    const rect = e.currentTarget.getBoundingClientRect()
    printDropdownStyle.top = (rect.bottom + 4) + 'px'
    printDropdownStyle.left = Math.max(4, rect.right - 220) + 'px'
  }
}

const closeDropdowns = (e) => {
  if (showMoreDropdown.value) {
    if (!e.target.closest('.sample-more-dropdown')) {
      showMoreDropdown.value = false
    }
  }
  if (showPrintDropdown.value) {
    if (!e.target.closest('.sample-more-dropdown')) {
      showPrintDropdown.value = false
    }
  }
}

const downloadTemplate = () => {
  showMoreDropdown.value = false
  const a = document.createElement('a')
  a.href = '/samples/template'
  a.download = 'template.csv'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

const batchSetPrice = () => {
  showMoreDropdown.value = false
  alert('批量设置价格功能开发中')
}

const openRestoreDeletedModal = () => {
  showMoreDropdown.value = false
  showRestoreDeletedModal.value = true
  deletedSelected.value = []
  deletedFilterField.value = ''
  deletedFilterKeyword.value = ''
  deletedFilterActive.value = false
  fetchDeletedSamples()
}

const fetchDeletedSamples = async () => {
  deletedLoading.value = true
  try {
    const res = await api('/samples/deleted?current=1&size=99999')
    if (res.code === 200 && res.data) {
      deletedAllData.value = res.data.records || []
      deletedTotal.value = res.data.total || 0
      applyDeletedFilter()
    }
  } catch (e) {
    showAlertDialog('获取已删除数据失败: ' + (e.message || '未知错误'), 'error')
  } finally {
    deletedLoading.value = false
  }
}

const applyDeletedFilter = () => {
  const all = deletedAllData.value || []
  const field = deletedFilterField.value
  const keyword = deletedFilterKeyword.value.trim()
  if (!keyword) {
    deletedFilterActive.value = false
    deletedData.value = all
    deletedTotal.value = all.length
    return
  }
  deletedFilterActive.value = true
  let filtered = all
  if (field === 'updateTime') {
    filtered = all.filter(item => {
      const t = item.updateTime
      if (!t) return false
      const d = new Date(t)
      const ds = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
      return ds.includes(keyword)
    })
  } else if (field) {
    const kw = keyword.toLowerCase()
    filtered = all.filter(item => {
      const val = item[field]
      return val != null && String(val).toLowerCase().includes(kw)
    })
  } else {
    const kw = keyword.toLowerCase()
    filtered = all.filter(item => {
      return ['sampleCode', 'manufacturerCode', 'sampleName', 'category', 'factoryCode', 'registrant'].some(f => {
        const val = item[f]
        return val != null && String(val).toLowerCase().includes(kw)
      })
    })
  }
  deletedData.value = filtered
  deletedTotal.value = filtered.length
  deletedSelected.value = []
  if (deletedGridRef.value) {
    deletedGridRef.value.clearCheckboxRow()
  }
}

const doDeletedFilter = () => {
  applyDeletedFilter()
}

const doDeletedResetFilter = () => {
  deletedFilterField.value = ''
  deletedFilterKeyword.value = ''
  applyDeletedFilter()
}

const onDeletedZoom = ({ type }) => {
  deletedFullscreen.value = (type === 'max')
  if (!deletedFullscreen.value) {
    deletedFullscreenSearch.value = ''
    doDeletedResetFilter()
    nextTick(() => {
      if (deletedGridRef.value) {
        deletedGridRef.value.refreshColumn()
        deletedGridRef.value.refreshScroll()
      }
    })
  }
}

const onDeletedToolbarClick = ({ code }) => {
  if (code === 'refresh') {
    fetchDeletedSamples()
  }
}

const onDeletedFullscreenSearch = () => {
  const keyword = deletedFullscreenSearch.value.trim().toLowerCase()
  if (!keyword) {
    doDeletedResetFilter()
    return
  }
  const all = deletedAllData.value || []
  deletedFilterActive.value = true
  deletedData.value = all.filter(item =>
    Object.values(item).some(v => String(v || '').toLowerCase().includes(keyword))
  )
}

const clearDeletedFullscreenSearch = () => {
  deletedFullscreenSearch.value = ''
  doDeletedResetFilter()
}

const openDeletedBatchQuery = () => {
  showDeletedBatchQuery.value = true
}

const doDeletedBatchQuery = () => {
  const raw = deletedBatchInput.value.trim()
  if (!raw) {
    showAlertDialog('请输入至少一个编号', 'warning')
    return
  }
  const codes = raw.split(/[\n,，]+/).map(s => s.trim()).filter(Boolean)
  if (codes.length === 0) {
    showAlertDialog('请输入至少一个编号', 'warning')
    return
  }
  const field = deletedBatchField.value
  const all = deletedAllData.value || []
  const codeSet = new Set(codes)
  deletedFilterActive.value = true
  deletedData.value = all.filter(item => codeSet.has(String(item[field] || '').trim()))
  deletedFullscreenSearch.value = ''
  deletedBatchInput.value = ''
  showDeletedBatchQuery.value = false
}

const openMainBatchQuery = () => {
  showMoreDropdown.value = false
  showMainBatchQuery.value = true
}

const doMainBatchQuery = async () => {
  const raw = mainBatchInput.value.trim()
  if (!raw) {
    showAlertDialog('请输入至少一个编号', 'warning')
    return
  }
  const codes = raw.split(/[\n,，]+/).map(s => s.trim()).filter(Boolean)
  if (codes.length === 0) {
    showAlertDialog('请输入至少一个编号', 'warning')
    return
  }
  const field = mainBatchField.value
  showMainBatchQuery.value = false
  tableLoading.value = true
  try {
    const res = await api(`/samples?current=1&size=99999`)
    if (res.code === 200 || res.data) {
      const allRecords = (res.data?.records || res.data?.list || res.data || [])
      const codeSet = new Set(codes)
      tableData.value = allRecords.filter(item => codeSet.has(String(item[field] || '').trim()))
      totalRecords.value = tableData.value.length
      currentPage.value = 1
      mainBatchQueryActive.value = true
    }
  } catch (e) {
    showAlertDialog('查询失败: ' + (e.message || '未知错误'), 'error')
  } finally {
    tableLoading.value = false
    mainBatchInput.value = ''
  }
}

const onDeletedCheckChange = () => {
  const grid = deletedGridRef.value
  if (grid) {
    deletedSelected.value = grid.getCheckboxRecords()
  }
}

const doRestoreDeleted = async () => {
  if (deletedSelected.value.length === 0) return
  if (!(await showConfirmDialog(`确定恢复选中的 ${deletedSelected.value.length} 条记录吗？`))) return
  const ids = deletedSelected.value.map(r => r.id)
  try {
    const res = await api('/samples/restore', { method: 'POST', body: JSON.stringify(ids) })
    if (res.code === 200) {
      showAlertDialog(`成功恢复 ${res.data || ids.length} 条记录`, 'success')
      deletedSelected.value = []
      fetchDeletedSamples()
      loadTableData()
    } else {
      showAlertDialog('恢复失败: ' + (res.message || '未知错误'), 'error')
    }
  } catch (e) {
    showAlertDialog('恢复失败: ' + (e.message || '未知错误'), 'error')
  }
}

const exportExcel = () => {
  showMoreDropdown.value = false
  const a = document.createElement('a')
  a.href = '/samples/export'
  a.download = 'samples.csv'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

const printTable = () => {
  showMoreDropdown.value = false
  window.print()
}

const doPrintTable = () => {
  showPrintDropdown.value = false
  const records = gridRef.value ? gridRef.value.getCheckboxRecords() : []
  if (!records || records.length === 0) {
    alert('请先勾选要打印的样品数据')
    return
  }
  generateBarcodeLabels(records)
}

const doPrintAllLabels = async () => {
  showPrintDropdown.value = false
  const ok = await showConfirmDialog('确认打印全部数据的大条码标签？')
  if (!ok) return
  const savedSize = pageSize.value
  const savedPage = currentPage.value
  pageSize.value = totalRecords.value || 5000
  currentPage.value = 1
  await loadTableData()
  setTimeout(() => {
    const records = tableData.value || []
    if (records.length > 0) {
      generateBarcodeLabels(records)
    } else {
      alert('没有数据可打印')
    }
    pageSize.value = savedSize
    currentPage.value = savedPage
    loadTableData()
  }, 1000)
}

const generateBarcodeLabels = async (records) => {
  const LABEL_W_MM = 50
  const LABEL_H_MM = 40
  const DPI = 96
  const MM_TO_PX = DPI / 25.4
  const LABEL_PX_W = Math.round(LABEL_W_MM * MM_TO_PX)
  const LABEL_PX_H = Math.round(LABEL_H_MM * MM_TO_PX)

  let html = '<!DOCTYPE html><html><head><meta charset="utf-8"><title>大条码标签</title>' +
    '<style>' +
    '*{margin:0;padding:0;box-sizing:border-box}' +
    'body{font-family:"SimSun","宋体",sans-serif;-webkit-text-stroke:0.5px}' +
    '.label{width:' + LABEL_PX_W + 'px;height:' + LABEL_PX_H + 'px;' +
    'background:#ff5733;color:#000;display:flex;flex-direction:column;position:relative;' +
    'padding:2px 3px;font-size:13px;font-weight:bold;line-height:1;overflow:hidden;page-break-after:always;' +
    '-webkit-text-stroke:0.35px}' +
    '.label:last-child{page-break-after:auto}' +
    '.lb-hdr{text-align:center;font-size:15px;font-weight:bold;line-height:1;letter-spacing:1px}' +
    '.lb-row{display:flex;align-items:center;font-weight:bold;line-height:1;margin-top:3px;padding-left:5px;min-height:13px}' +
    '.lb-code{font-weight:bold;font-size:13px;flex:1;min-width:0;line-height:1}' +
    '.lb-qr{position:absolute;top:24px;right:20px;width:60px;height:60px;z-index:1}' +
    '.lb-qr svg,.lb-qr img{width:100%;height:100%;display:block}' +
    '.lb-pair{flex:1;min-width:0;font-weight:bold;font-size:10px}' +
    '.lb-pack{font-weight:bold;font-size:13px;overflow:hidden;line-height:1}' +
    '.lb-box{white-space:nowrap;line-height:1;font-weight:bold;font-size:12px}' +
    '.lb-name{font-weight:bold;font-size:12px;white-space:normal;word-break:break-all;overflow:hidden;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;line-height:1;padding-left:5px;min-height:12px}' +
    '.lb-booth{font-weight:bold;font-size:12px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;line-height:1}' +
    '@media print{@page{size:' + LABEL_W_MM + 'mm ' + LABEL_H_MM + 'mm;margin:0;padding:0}' +
    'body{-webkit-print-color-adjust:exact;print-color-adjust:exact}}' +
    '</style></head><body>'

  for (let i = 0; i < records.length; i++) {
    const r = records[i]
    const code = r.sampleCode || ''
    const qrDataUrl = await QRCode.toDataURL(code, { width: 160, margin: 1, scale: 5 })

    const inner = r.innerBoxCount || ''
    const cap = r.cartonCapacity || ''
    const gw = r.cartonGrossWeight || ''
    const nw = r.cartonNetWeight || ''
    const cl = r.cartonLength || ''
    const cw = r.cartonWidth || ''
    const ch = r.cartonHeight || ''
    const cv = r.cartonMaterialVolume || ''
    const vol = r.cartonVolume || ''
    const booth = r.boothNo || ''
    const boothLen = booth.length
    const boothSize = boothLen > 21 ? '8px' : '12px'

    const factoryCodeText = r.factoryCode || r.packageCode || ''
    const factoryCodeLen = factoryCodeText.length
    const factoryCodeSize = factoryCodeLen > 23 ? '7px' : factoryCodeLen > 15 ? '8px' : factoryCodeLen > 12 ? '10px' : '13px'
    let factoryCodeHtml = esc(factoryCodeText)
    if (factoryCodeLen > 23) {
      const mid = Math.ceil(factoryCodeLen / 2)
      factoryCodeHtml = esc(factoryCodeText.slice(0, mid)) + '<br>' + esc(factoryCodeText.slice(mid))
    }

    const sampleNameText = r.sampleName || ''
    const sampleNameLen = sampleNameText.length
    const sampleNameSize = sampleNameLen > 39 ? '8px' : sampleNameLen > 20 ? '10px' : '12px'

    html += '<div class="label">' +
      '<div class="lb-hdr">新悦翔玩具展馆</div>' +
      '<div class="lb-row">' +
        '<div class="lb-code">' + esc(code) + '</div>' +
      '</div>'

    html += '<div class="lb-row"><span class="lb-pack" style="font-size:' + factoryCodeSize + '">' + factoryCodeHtml + '</span></div>'

    html += '<div class="lb-row">' +
        '<span class="lb-pair" style="flex:0 0 auto;margin-right:20px">' + esc(inner) + '/' + esc(cap) + '</span>' +
        '<span class="lb-pair" style="flex:0 0 auto">' + esc(gw) + '/' + esc(nw) + '</span>' +
      '</div>' +
      '<div class="lb-row"><span class="lb-pack">' + esc(r.packagingCn || '') + '</span></div>' +
      '<div class="lb-row">' +
        '<span class="lb-box" style="flex:0 0 auto;margin-right:25px">' + esc(cl) + '*' + esc(cw) + '*' + esc(ch) + '</span>' +
        '<span class="lb-box" style="flex:0 0 auto">' + esc(vol) + '/' + esc(cv) + '</span>' +
      '</div>' +
      '<div class="lb-row" style="margin-top:5px">' +
        '<span class="lb-booth" style="flex:0 0 auto;font-size:' + boothSize + '">' + esc(booth) + '</span>' +
        '<span class="lb-booth" style="flex:0 0 auto;margin-left:80px">B01</span>' +
      '</div>' +
      '<div class="lb-name" style="margin-top:5px;font-size:' + sampleNameSize + '" title="' + escAttr(sampleNameText) + '">' + esc(sampleNameText) + '</div>' +
      '<div class="lb-qr"><img src="' + qrDataUrl + '" /></div>' +
      '</div>'
  }

  html += '</body></html>'
  printHtml(html)
}

const doPrintQuarterTable = () => {
  showPrintDropdown.value = false
  const records = gridRef.value ? gridRef.value.getCheckboxRecords() : []
  if (!records || records.length === 0) {
    alert('请先勾选要打印的样品数据')
    return
  }
  generateQuarterLabels(records)
}

const generateQuarterLabels = async (records) => {
  const LABEL_W_MM = 25
  const LABEL_H_MM = 25
  const DPI = 96
  const MM_TO_PX = DPI / 25.4
  const LABEL_PX_W = Math.round(LABEL_W_MM * MM_TO_PX)
  const LABEL_PX_H = Math.round(LABEL_H_MM * MM_TO_PX)
  const QR_PX = Math.round(15 * MM_TO_PX)

  let html = '<!DOCTYPE html><html><head><meta charset="utf-8"><title>小条码标签</title>' +
    '<style>' +
    '*{margin:0;padding:0;box-sizing:border-box}' +
    'body{font-family:"SimSun","宋体",sans-serif;-webkit-text-stroke:0.5px}' +
    '.qlabel{width:' + LABEL_PX_W + 'px;height:' + LABEL_PX_H + 'px;' +
    'background:#fff;color:#000;display:flex;flex-direction:column;justify-content:center;overflow:hidden;' +
    'page-break-after:always}' +
    '.qlabel:last-child{page-break-after:auto}' +
    '.q-factory{text-align:center;font-weight:bold;font-size:8px;line-height:1.2;flex-shrink:0;margin-top:1px}' +
    '.q-mid{position:relative;flex:1;overflow:hidden}' +
    '.q-qr-wrap{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);width:' + QR_PX + 'px;height:' + QR_PX + 'px}' +
    '.q-qr-wrap img{width:100%;height:100%;display:block}' +
    '.q-pack-left{position:absolute;top:75%;left:6px;transform:translateY(-50%) rotate(-90deg);transform-origin:left center;font-weight:bold;font-size:8px;line-height:1;white-space:nowrap}' +
    '.q-code-right{position:absolute;top:82%;right:6px;transform:translateY(-50%) rotate(90deg);transform-origin:right center;font-weight:bold;font-size:8px;line-height:1;white-space:nowrap}' +
    '.q-booth{text-align:center;font-weight:bold;font-size:8px;line-height:1.2;flex-shrink:0}' +
    '@media print{@page{size:' + LABEL_W_MM + 'mm ' + LABEL_H_MM + 'mm;margin:0;padding:0}' +
    'body{-webkit-print-color-adjust:exact;print-color-adjust:exact}}' +
    '</style></head><body>'

  for (let i = 0; i < records.length; i++) {
    const r = records[i]
    const code = r.sampleCode || ''
    const qrDataUrl = await QRCode.toDataURL(code, { width: 80, margin: 0, scale: 4 })
    const factoryCodeText = r.factoryCode || r.packageCode || ''
    const factoryCodeLen = factoryCodeText.length
    const factorySize = factoryCodeLen > 8 ? '6px' : '8px'
    let factoryHtml = esc(factoryCodeText)
    if (factoryCodeLen > 8) {
      const mid = Math.ceil(factoryCodeLen / 2)
      factoryHtml = esc(factoryCodeText.slice(0, mid)) + '<br>' + esc(factoryCodeText.slice(mid))
    }
    const packagingText = r.packagingCn || ''
    const booth = r.boothNo || ''
    const boothLen = booth.length
    const boothSize = boothLen > 21 ? '6px' : '8px'
    let boothHtml = esc(booth)
    if (boothLen > 21) {
      const mid = Math.ceil(boothLen / 2)
      boothHtml = esc(booth.slice(0, mid)) + '<br>' + esc(booth.slice(mid))
    }

    html += '<div class="qlabel">' +
      '<div class="q-factory" style="font-size:' + factorySize + '">' + factoryHtml + '</div>' +
      '<div class="q-mid">' +
        '<div class="q-pack-left">' + esc(packagingText) + '</div>' +
        '<div class="q-qr-wrap"><img src="' + qrDataUrl + '" /></div>' +
        '<div class="q-code-right">' + esc(code) + '</div>' +
      '</div>' +
      '<div class="q-booth" style="font-size:' + boothSize + '">' + boothHtml + '</div>' +
      '</div>'
  }

  html += '</body></html>'
  printHtml(html)
}

const esc = (s) => { if (!s && s !== 0) return ''; return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;') }
const escAttr = (s) => { if (!s && s !== 0) return ''; return String(s).replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/</g,'&lt;').replace(/>/g,'&gt;') }

let printIframe = null
const printHtml = (html) => {
  return new Promise((resolve) => {
    if (!printIframe) {
      printIframe = document.createElement('iframe')
      printIframe.style.cssText = 'position:fixed;top:0;left:0;width:0;height:0;border:none;opacity:0;pointer-events:none'
      document.body.appendChild(printIframe)
    }
    let loaded = false
    printIframe.onload = () => {
      if (loaded) return
      loaded = true
      printIframe.contentWindow.print()
      printIframe.contentWindow.onafterprint = () => { resolve() }
    }
    printIframe.srcdoc = html
  })
}

const openScanPrintModal = () => {
  scanPrintCode.value = ''
  scanPrintResult.value = null
  scanPrintImageSrc.value = ''
  scanPrintError.value = ''
  scanPrintType.value = 'barcode'
  scanPrintLoading.value = false
  scanPrintCount.value = 1
  showScanPrintModal.value = true
  nextTick(() => scanPrintInputRef.value?.focus())
}

const searchScanPrint = async () => {
  const code = scanPrintCode.value.trim()
  if (!code) {
    scanPrintError.value = '请输入公司编号'
    return
  }
  scanPrintError.value = ''
  scanPrintResult.value = null
  scanPrintImageSrc.value = ''
  scanPrintLoading.value = true
  try {
    const res = await api('/samples/search?current=1&size=1', {
      method: 'POST',
      body: JSON.stringify({ sampleCode: code })
    })
    const data = res.data || res || {}
    const list = data.records || data.list || []
    if (list.length === 0) {
      scanPrintError.value = '未找到公司编号为 "' + code + '" 的样品'
      return
    }
    const r = list[0]
    scanPrintResult.value = r
    if (r.thumbnail || r.thumbnailName) {
      const imgName = r.thumbnail || r.thumbnailName
      scanPrintImageSrc.value = '/thumbnails/' + imgName
    }
    if (scanPrintContinuous.value) {
      nextTick(() => doScanPrint())
    }
  } catch (e) {
    scanPrintError.value = '查询失败：' + (e.message || '网络错误')
  } finally {
    scanPrintLoading.value = false
  }
}

const doScanPrint = () => {
  if (!scanPrintResult.value) return
  const count = scanPrintCount.value || 1
  const records = []
  for (let i = 0; i < count; i++) {
    records.push(scanPrintResult.value)
  }
  if (scanPrintContinuous.value) {
    scanPrintCode.value = ''
    scanPrintResult.value = null
    scanPrintImageSrc.value = ''
    scanPrintError.value = ''
    nextTick(() => scanPrintInputRef.value?.focus())
    if (scanPrintType.value === 'barcode') {
      generateBarcodeLabels(records)
    } else {
      generateQuarterLabels(records)
    }
  } else {
    showScanPrintModal.value = false
    if (scanPrintType.value === 'barcode') {
      generateBarcodeLabels(records)
    } else {
      generateQuarterLabels(records)
    }
  }
}

const openImageSearchModal = async () => {
  showImageSearchModal.value = true
  imageSearchImages.value = []
  imageSearchSelectedIdx.value = -1
  resetCropState()
  imageSearchResults.value = []
  imageSearchDone.value = false
  imageSearchThreshold.value = 10

  const needBackfill = localStorage.getItem('dhash_backfilled') !== '1'
  if (needBackfill) {
    try {
      const res = await api('/images/has-dhash')
      if (res.code === 200 && res.data === false) {
        await autoBackfillDhash()
      } else {
        localStorage.setItem('dhash_backfilled', '1')
      }
    } catch (e) {}
  }
}

const autoBackfillDhash = async () => {
  try {
    const res = await api('/images/backfill-dhash', { method: 'POST' })
    if (res.code === 200 && res.data) {
      localStorage.setItem('dhash_backfilled', '1')
    }
  } catch (e) {}
}

const imageSearchError = ref('')

const onImageSearchFilesChange = (e) => {
  const files = e.target.files
  if (!files || files.length === 0) return
  let skipped = 0
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    if (!file.type.startsWith('image/')) {
      skipped++
      continue
    }
    const url = URL.createObjectURL(file)
    imageSearchImages.value.push({ file, url, name: file.name })
  }
  if (skipped > 0) {
    imageSearchError.value = skipped + ' 个非图片文件已跳过'
  } else {
    imageSearchError.value = ''
  }
  if (imageSearchSelectedIdx.value < 0 && imageSearchImages.value.length > 0) {
    imageSearchSelectedIdx.value = 0
  }
  imageSearchResults.value = []
  imageSearchDone.value = false
  resetCropState()
  e.target.value = ''
}

const selectSearchImage = (idx) => {
  imageSearchSelectedIdx.value = idx
  resetCropState()
  imageSearchResults.value = []
  imageSearchDone.value = false
}

const removeSearchImage = (idx) => {
  URL.revokeObjectURL(imageSearchImages.value[idx].url)
  imageSearchImages.value.splice(idx, 1)
  if (imageSearchImages.value.length === 0) {
    imageSearchSelectedIdx.value = -1
    resetCropState()
  } else if (imageSearchSelectedIdx.value >= imageSearchImages.value.length) {
    imageSearchSelectedIdx.value = imageSearchImages.value.length - 1
    resetCropState()
  } else {
    resetCropState()
  }
}

function resetCropState() {
  cropState.startX = 0
  cropState.startY = 0
  cropState.x = 0
  cropState.y = 0
  cropState.w = 0
  cropState.h = 0
  cropState.active = false
  cropState.done = false
  cropDraggingHandle.value = ''
  cropImgNaturalW.value = 0
  cropImgNaturalH.value = 0
  cropDisplayScale.value = 1
}

const resetCrop = () => { resetCropState() }

const onCropImgLoad = () => {
  const img = cropImgRef.value
  if (!img) return
  cropImgNaturalW.value = img.naturalWidth
  cropImgNaturalH.value = img.naturalHeight
  if (img.offsetWidth > 0) {
    cropDisplayScale.value = img.naturalWidth / img.offsetWidth
  }
}

const getCropEditorPos = (e) => {
  const rect = cropEditorRef.value.getBoundingClientRect()
  return { x: e.clientX - rect.left, y: e.clientY - rect.top }
}

const onCropMouseDown = (e) => {
  if (cropDraggingHandle.value) return
  const pos = getCropEditorPos(e)
  cropState.startX = pos.x
  cropState.startY = pos.y
  cropState.x = pos.x
  cropState.y = pos.y
  cropState.w = 0
  cropState.h = 0
  cropState.active = true
  cropState.done = false
}

const onCropMouseMove = (e) => {
  if (!cropState.active) return
  const pos = getCropEditorPos(e)
  if (cropDraggingHandle.value) {
    handleResize(pos)
  } else {
    cropState.w = pos.x - cropState.startX
    cropState.h = pos.y - cropState.startY
  }
}

const onCropMouseUp = () => {
  if (!cropState.active) return
  if (Math.abs(cropState.w) > 10 && Math.abs(cropState.h) > 10) {
    cropState.done = true
  } else {
    resetCropState()
  }
  cropState.active = false
  cropDraggingHandle.value = ''
}

const onHandleDown = (e, handle) => {
  e.preventDefault()
  cropDraggingHandle.value = handle
  cropState.active = true
  const pos = getCropEditorPos(e)
  cropState.startX = pos.x
  cropState.startY = pos.y
}

const handleResize = (pos) => {
  const h = cropDraggingHandle.value
  const dx = pos.x - cropState.startX
  const dy = pos.y - cropState.startY
  const boxX = cropState.w < 0 ? cropState.x + cropState.w : cropState.x
  const boxY = cropState.h < 0 ? cropState.y + cropState.h : cropState.y
  const boxW = Math.abs(cropState.w)
  const boxH = Math.abs(cropState.h)
  if (h.includes('r')) { cropState.w = boxW + dx; cropState.x = boxX }
  if (h.includes('l')) { cropState.w = boxW - dx; cropState.x = boxX + dx }
  if (h.includes('b')) { cropState.h = boxH + dy; cropState.y = boxY }
  if (h.includes('t')) { cropState.h = boxH - dy; cropState.y = boxY + dy }
  cropState.startX = pos.x
  cropState.startY = pos.y
}

const getCroppedFile = () => {
  return new Promise((resolve) => {
    const img = imageSearchSelectedImg.value
    if (!img) { resolve(null); return }
    if (!cropDone.value) {
      resolve(img.file)
      return
    }
    const imageEl = new Image()
    imageEl.onload = () => {
      const sx = Math.max(0, Math.round(cropX.value * cropDisplayScale.value))
      const sy = Math.max(0, Math.round(cropY.value * cropDisplayScale.value))
      const sw = Math.min(Math.round(cropW.value * cropDisplayScale.value), imageEl.naturalWidth - sx)
      const sh = Math.min(Math.round(cropH.value * cropDisplayScale.value), imageEl.naturalHeight - sy)
      if (sw <= 0 || sh <= 0) { resolve(img.file); return }
      const canvas = document.createElement('canvas')
      canvas.width = sw
      canvas.height = sh
      const ctx = canvas.getContext('2d')
      ctx.imageSmoothingEnabled = true
      ctx.imageSmoothingQuality = 'high'
      ctx.drawImage(imageEl, sx, sy, sw, sh, 0, 0, sw, sh)
      canvas.toBlob((blob) => {
        if (!blob) { resolve(img.file); return }
        const croppedFile = new File([blob], img.name.replace(/\.[^.]+$/, '.jpg'), { type: 'image/jpeg', lastModified: Date.now() })
        resolve(croppedFile)
      }, 'image/jpeg', 0.95)
    }
    imageEl.onerror = () => {
      resolve(img.file)
    }
    imageEl.src = img.url
  })
}

const doImageSearch = async () => {
  imageSearching.value = true
  imageSearchDone.value = false
  imageSearchResults.value = []
  imageSearchError.value = ''
  const searchFile = await getCroppedFile()
  if (!searchFile) {
    imageSearching.value = false
    return
  }
  try {
    const formData = new FormData()
    formData.append('file', searchFile)
    const res = await api('/images/search-by-image?maxDistance=' + imageSearchThreshold.value, {
      method: 'POST',
      body: formData
    })
    if (res.code === 200) {
      imageSearchResults.value = res.data || []
      const needBackfill = localStorage.getItem('dhash_backfilled') !== '1'
      if (imageSearchResults.value.length === 0 && needBackfill) {
        await autoBackfillDhash()
        const retryRes = await api('/images/search-by-image?maxDistance=' + imageSearchThreshold.value, {
          method: 'POST',
          body: formData
        })
        if (retryRes.code === 200) {
          imageSearchResults.value = retryRes.data || []
        }
      }
    } else {
      imageSearchError.value = res.msg || '搜索失败，请重试'
    }
  } catch (e) {
    imageSearchError.value = '网络错误，请检查连接后重试'
    console.error('图像搜索失败:', e)
  } finally {
    imageSearching.value = false
    imageSearchDone.value = true
  }
}

const viewImageSearchResult = (item) => {
  if (item.sampleId) {
    showImageSearchModal.value = false
    api('/samples/' + item.sampleId).then(res => {
      if (res.code === 200 && res.data) {
        viewSampleDetail(res.data)
      }
    })
  }
}

const doPrintMultiCopies = () => {
  showPrintDropdown.value = false
  const records = gridRef.value ? gridRef.value.getCheckboxRecords() : []
  if (!records || records.length === 0) {
    alert('请先勾选要打印的样品数据')
    return
  }
  multiPrintBatchCopies.value = 1
  multiPrintType.value = 'barcode'
  multiPrintRecords.value = records.map(r => ({
    ...r,
    copies: 1,
    factoryCode: r.factoryCode || r.packageCode || ''
  }))
  showMultiPrintModal.value = true
}

const confirmMultiPrint = () => {
  const rows = multiPrintRecords.value
  if (!rows || rows.length === 0) {
    alert('没有要打印的数据')
    return
  }
  const repeatedRecords = []
  rows.forEach(r => {
    const copies = r.copies || 0
    for (let i = 0; i < copies; i++) {
      repeatedRecords.push(r)
    }
  })
  if (repeatedRecords.length === 0) {
    alert('没有有效的打印张数')
    return
  }
  showMultiPrintModal.value = false
  if (multiPrintType.value === 'barcode') {
    generateBarcodeLabels(repeatedRecords)
  } else {
    generateQuarterLabels(repeatedRecords)
  }
}

const doPrintWithImages = () => {
  showPrintDropdown.value = false
  const records = tableData.value || []
  let html = '<!DOCTYPE html><html><head><meta charset="utf-8"><title>打印含图片列表</title>' +
    '<style>' +
    'body{font-family:"Microsoft YaHei",sans-serif;padding:20px}' +
    'table{border-collapse:collapse;width:100%;font-size:12px}' +
    'td,th{border:1px solid #ccc;padding:6px 8px;text-align:left;vertical-align:top}' +
    'th{background:#f5f5f5;font-weight:600}' +
    'img{max-width:80px;max-height:80px;object-fit:contain}' +
    '@media print{body{margin:0;padding:10px}}' +
    '</style></head><body>' +
    '<h2 style="text-align:center">样品图片列表</h2>' +
    '<table><thead><tr>' +
    '<th>图片</th><th>公司编号</th><th>样品名称</th><th>厂商名称</th><th>种类</th><th>出厂价</th>' +
    '</tr></thead><tbody>'
  records.forEach(r => {
    const imgSrc = r.thumbnail ? '/thumbnails/' + r.thumbnail : ''
    html += '<tr>' +
      '<td>' + (imgSrc ? '<img src="' + imgSrc + '" />' : '') + '</td>' +
      '<td>' + (r.sampleCode || '') + '</td>' +
      '<td>' + (r.sampleName || '') + '</td>' +
      '<td>' + (r.supplier || '') + '</td>' +
      '<td>' + (r.category || '') + '</td>' +
      '<td>' + (r.factoryPrice || '') + '</td>' +
      '</tr>'
  })
  html += '</tbody></table></body></html>'
  printHtml(html)
}

const openImportModal = () => {
  showMoreDropdown.value = false
  importFile.value = null
  showImportModal.value = true
}

const openBatchImageModal = () => {
  showMoreDropdown.value = false
  batchFiles.value = []
  showBatchImageModal.value = true
}

const openAdvancedSearch = () => {
  showAdvancedSearch.value = true
}

const onImportFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  importFile.value = file
  await parseExcelFile(file)
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
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })

        if (jsonData.length === 0) {
          alert('Excel 文件为空')
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
              rowObj[fieldName] = rawRow[idx] != null ? String(rawRow[idx]).trim() : ''
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
        alert('解析 Excel 文件失败: ' + err.message)
        reject(err)
      }
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsArrayBuffer(file)
  })
}

const onImportDrop = (e) => {
  const files = e.dataTransfer.files
  if (files.length > 0 && files[0].name.endsWith('.xlsx')) {
    importFile.value = files[0]
  }
}

const onDragOver = (e) => {
  e.currentTarget.classList.add('drag-over')
}

const onDragLeave = (e) => {
  e.currentTarget.classList.remove('drag-over')
}

const onBatchDrop = (e) => {
  e.currentTarget.classList.remove('drag-over')
  const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'))
  batchFiles.value = files
  if (files.length > 0) doBatchMatch()
}

const onBatchFileChange = (e) => {
  batchFiles.value = Array.from(e.target.files)
  if (batchFiles.value.length > 0) doBatchMatch()
}

const removeBatchFile = (idx) => {
  batchFiles.value.splice(idx, 1)
  batchMatched.value.splice(idx, 1)
  if (batchMatched.value.length === 0) {
    batchCurrentIndex.value = 0
  } else if (batchCurrentIndex.value >= batchMatched.value.length) {
    batchCurrentIndex.value = batchMatched.value.length - 1
  }
}

const doBatchMatch = async () => {
  if (batchFiles.value.length === 0) return
  batchMatchLoading.value = true
  batchCurrentIndex.value = 0
  batchMatched.value = []
  try {
    const codeField = batchImageType.value === 'company-code' ? 'sampleCode' : 'factoryCode'
    const extractCode = (filename) => {
      return filename.replace(/\.[^.]+$/, '').replace(/\(\d+\)$/, '').trim()
    }
    const codes = batchFiles.value.map(f => extractCode(f.name))

    const matchRes = await api('/samples/match-by-codes', {
      method: 'POST',
      body: JSON.stringify({ codes, type: codeField === 'factoryCode' ? 'factoryCode' : 'sampleCode' })
    })
    const samples = Array.isArray(matchRes.data) ? matchRes.data : (Array.isArray(matchRes) ? matchRes : [])

    const sampleMap = {}
    samples.forEach(s => { sampleMap[s[codeField]] = s })

    const matchedIds = []
    const results = batchFiles.value.map(f => {
      const code = extractCode(f.name)
      const sample = sampleMap[code]
      if (!sample) return { file: f, code, matched: false }
      matchedIds.push(sample.id)
      return { file: f, code, matched: true, sampleId: sample.id, sampleCode: sample.sampleCode, factoryCode: sample.factoryCode, sampleName: sample.sampleName, action: 'append', hasExisting: false, existingThumb: null, previewUrl: URL.createObjectURL(f) }
    })

    if (matchedIds.length > 0) {
      try {
        const imgRes = await api('/images/sample-images', { method: 'POST', body: JSON.stringify(matchedIds) })
        const imgMap = imgRes.data || imgRes || {}
        results.forEach(r => {
          if (r.matched && imgMap[r.sampleId]) {
            r.hasExisting = true
            r.existingThumb = imgMap[r.sampleId].thumbnailPath || null
          }
        })
      } catch (e) { console.error(e) }
    }

    batchMatched.value = results.sort((a, b) => b.matched - a.matched)
  } catch (e) {
    console.error(e)
    batchMatched.value = batchFiles.value.map(f => ({ file: f, code: extractCode(f.name), matched: false, previewUrl: URL.createObjectURL(f) }))
  } finally {
    batchMatchLoading.value = false
  }
}

const doImport = async () => {
  if (!importFile.value) return
  importUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', importFile.value)
    const res = await api('/samples/import', { method: 'POST', body: fd })
    showImportModal.value = false
    importFile.value = null
    await loadTableData()
  } catch (e) {
    console.error(e)
  } finally {
    importUploading.value = false
  }
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
    alert('请先选择要导出的行')
    return
  }
  alert(`已选择 ${importSelectedRows.value.length} 行数据准备导出（功能开发中）`)
}

const selectAllPreviewRows = () => {
  if (importPreviewGridRef.value) {
    importPreviewGridRef.value.setAllCheckboxRow(true)
    onImportPreviewCheckChange()
  }
}

const clearPreviewSelection = () => {
  if (importPreviewGridRef.value) {
    importPreviewGridRef.value.clearCheckboxRow()
    importSelectedRows.value = []
  }
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
    alert('请至少选择一行数据进行导入')
    return
  }
  importConfirmCount.value = count
  showImportConfirmModal.value = true
}

const INFRINGEMENT_MAP = { '1': '侵权', '2': '不侵权' }

const buildSamplesToSend = (records) => {
  return records.map(row => {
    const sample = {}
    Object.keys(HEADER_TO_FIELD).forEach(header => {
      const field = HEADER_TO_FIELD[header]
      if (row[field] !== undefined && row[field] !== '') {
        const val = String(row[field]).trim()
        if (field === 'infringement') {
          sample[field] = INFRINGEMENT_MAP[val] || '其他'
        } else {
          sample[field] = row[field]
        }
      }
    })
    return sample
  })
}

const BATCH_SIZE = 50

const executeImport = async () => {
  showImportConfirmModal.value = false
  const selectedRecords = importPreviewGridRef.value.getCheckboxRecords()
  const recordsToImport = importConfirmCount.value === importPreviewData.value.length
    ? importPreviewData.value
    : selectedRecords
  const allSamples = buildSamplesToSend(recordsToImport)
  const total = allSamples.length

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
    for (let i = 0; i < allSamples.length; i += BATCH_SIZE) {
      const batch = allSamples.slice(i, i + BATCH_SIZE)
      const batchNum = Math.floor(i / BATCH_SIZE) + 1
      const totalBatches = Math.ceil(allSamples.length / BATCH_SIZE)
      importProgressText.value = `正在${isUpdateMode ? '更新' : '导入'}第 ${batchNum}/${totalBatches} 批 (${i + 1}-${Math.min(i + BATCH_SIZE, total)}/${total})...`

      const res = await api(`/samples/batch-import?updateMode=${isUpdateMode}`, {
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
        batch.forEach((s, idx) => {
          allFailedRows.push({
            row: String(i + idx + 1),
            公司编号: s.sampleCode || '',
            样品名称: s.sampleName || '',
            失败原因: res.message || '服务端返回错误',
            类型: '异常'
          })
        })
      }

      importProgress.value = Math.round(((i + batch.length) / total) * 100)
      if (i + BATCH_SIZE < allSamples.length) {
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
      loadTableData()
    }, 500)
  } catch (e) {
    console.error(e)
    importUploading.value = false
    importProgress.value = 0
    alert('导入失败: ' + (e.message || '未知错误'))
  }
}

const exportImportFailedRows = () => {
  const rows = batchResult.failedRows
  if (!rows || rows.length === 0) return
  const headers = ['行号', '公司编号', '样品名称', '失败原因', '类型']
  const csvLines = [headers.join(',')]
  rows.forEach(r => {
    const line = [
      `"${r.row || ''}"`,
      `"${(r['公司编号'] || '').replace(/"/g, '""')}"`,
      `"${(r['样品名称'] || '').replace(/"/g, '""')}"`,
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

const doBatchImageUpload = async () => {
  const uploadList = batchMatched.value.filter(m => m.matched && m.action !== 'skip')
  const unmatchedList = batchMatched.value.filter(m => !m.matched).map(m => m.file.name)
  if (uploadList.length === 0 && unmatchedList.length > 0) {
    batchResult.successCount = 0
    batchResult.failCount = 0
    batchResult.unmatchedCount = unmatchedList.length
    batchResult.failList = []
    batchResult.unmatchedList = unmatchedList
    showBatchResultModal.value = true
    return
  }
  if (uploadList.length === 0) {
    alert('没有需要上传的图片')
    return
  }

  const CONCURRENCY = 3
  const UPLOAD_TIMEOUT_MS = 60000
  const MAX_RETRIES = 2
  let idx = 0
  let successCount = 0
  let failCount = 0
  const failList = []

  batchUploading.value = true
  batchUploadProgress.value = { done: 0, total: uploadList.length, success: 0, fail: 0 }

  const uploadOneWithRetry = async (item) => {
    if (item.action === 'cover' && item.hasExisting) {
      try {
        await api(`/images/sample/${item.sampleId}`, { method: 'DELETE' })
      } catch (e) {
        console.warn('[批量上传] 清除旧图片失败:', item.file.name, e.message)
      }
    }

    let lastError = null
    for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      if (attempt > 0) {
        await new Promise(r => setTimeout(r, attempt * 1000))
      }
      try {
        const controller = new AbortController()
        const timeoutId = setTimeout(() => controller.abort(), UPLOAD_TIMEOUT_MS)
        const fd = new FormData()
        fd.append('file', item.file)
        fd.append('sampleId', item.sampleId)
        const res = await api('/images/upload', { method: 'POST', body: fd, signal: controller.signal })
        clearTimeout(timeoutId)
        if (res && res.code === 200) {
          successCount++
          const row = tableData.value.find(r => r.id === item.sampleId)
          if (row && res.data) {
            row.thumbnail = res.data.thumbnailPath
            row.firstImageId = res.data.id
          } else if (!row) {
            console.warn('[批量上传] 表格中未找到 sampleId:', item.sampleId, '文件:', item.file.name)
          } else {
            console.warn('[批量上传] 返回数据异常:', res, '文件:', item.file.name)
          }
          return true
        }
        lastError = new Error(res?.message || `服务端返回非200: ${res?.code}`)
      } catch (e) {
        lastError = e
        if (e.name === 'AbortError') {
          lastError = new Error('上传超时(60s)')
        }
        if (attempt < MAX_RETRIES) {
          continue
        }
      }
    }
    failCount++
    failList.push(`${item.file.name}: ${lastError?.message || '上传失败'}`)
    return false
  }

  try {
    const workerCount = Math.min(CONCURRENCY, uploadList.length)
    const workers = Array.from({ length: workerCount }, () =>
      (async () => {
        while (idx < uploadList.length) {
          const item = uploadList[idx++]
          await uploadOneWithRetry(item)
          batchUploadProgress.value.done++
          batchUploadProgress.value.success = successCount
          batchUploadProgress.value.fail = failCount
        }
      })()
    )
    await Promise.all(workers)

    batchResult.successCount = successCount
    batchResult.failCount = failCount
    batchResult.unmatchedCount = unmatchedList.length
    batchResult.failList = failList
    batchResult.unmatchedList = unmatchedList
    showBatchResultModal.value = true
    closeBatchModal()
  } finally {
    batchUploading.value = false
  }
}

const setBatchActionAll = (action) => {
  batchMatched.value.forEach(r => { if (r.matched) r.action = action })
}

const closeBatchModal = () => {
  showBatchImageModal.value = false
  batchFiles.value = []
  batchMatched.value = []
  batchCurrentIndex.value = 0
  batchUploading.value = false
}

const openBatchVideoModal = () => {
  batchVideoMatched.value = []
  batchVideoFiles.value = []
  videoCurrentIndex.value = 0
  videoUploading.value = false
  showBatchVideoModal.value = true
}

const closeBatchVideoModal = () => {
  showBatchVideoModal.value = false
  batchVideoFiles.value = []
  batchVideoMatched.value = []
  videoCurrentIndex.value = 0
  videoUploading.value = false
  customManufacturerCode.value = ''
  customCodesText.value = ''
}

const onVideoDragOver = (e) => { e.dataTransfer.dropEffect = 'copy' }

const extractVideoCode = (filename) => {
  return filename.replace(/\.[^.]+$/, '').replace(/\(\d+\)$/, '').trim()
}

const matchVideosToSamples = async (files) => {
  if (!files || files.length === 0) return
  videoMatchLoading.value = true
  try {
    let type, codes

    if (batchVideoType.value === 'custom') {
      codes = customCodesText.value
        .replace(/，/g, ',')
        .split(/[\n,]/)
        .map(s => s.trim())
        .filter(Boolean)

      if (codes.length > 50) {
        showAlertDialog(`最多支持50个编号，当前输入了 ${codes.length} 个`, 'warning')
        videoMatchLoading.value = false
        return
      }
      type = customMatchSubType.value
    } else {
      codes = files.map(f => extractVideoCode(f.name))
      type = batchVideoType.value
    }

    const res = await api('/samples/match-by-codes', {
      method: 'POST',
      body: JSON.stringify({ type, codes })
    })
    const matchedSamples = (res.data || res || [])

    const lookup = {}
    matchedSamples.forEach(s => {
      const key = type === 'company-code' ? s.sampleCode : s.factoryCode
      if (key) lookup[key] = s
    })

    const buildResults = (codeToFileFn) => {
      const results = []
      codes.forEach((code, idx) => {
        const f = codeToFileFn(idx)
        const matched = lookup[code]
        results.push({
          file: f, code, matched: !!matched,
          action: !!matched ? 'append' : 'skip',
          previewUrl: URL.createObjectURL(f),
          ...(matched ? { sampleId: matched.id, sampleCode: matched.sampleCode, sampleName: matched.sampleName, factoryCode: matched.factoryCode } : {})
        })
      })
      return results
    }

    if (batchVideoType.value === 'custom') {
      if (customMatchSubType.value === 'factory-code') {
        const mfrCode = customManufacturerCode.value.trim()
        if (mfrCode) {
          Object.keys(lookup).forEach(key => {
            if (lookup[key].manufacturerCode !== mfrCode) delete lookup[key]
          })
        }
      }
      batchVideoMatched.value = buildResults(idx => files[idx % files.length])
    } else {
      batchVideoMatched.value = buildResults(idx => files[idx]).sort((a, b) => b.matched - a.matched)
    }
  } catch (e) {
    console.error('匹配视频失败:', e)
    batchVideoMatched.value = files.map(f => ({ file: f, code: extractVideoCode(f.name), matched: false, action: 'skip', previewUrl: URL.createObjectURL(f) }))
  } finally {
    videoMatchLoading.value = false
  }
}

const MAX_VIDEO_FILES = 10
const MAX_VIDEO_FILE_SIZE = 50 * 1024 * 1024 // 50MB

const onVideoDrop = (e) => {
  const files = Array.from(e.dataTransfer.files).filter(f =>
    /\.(mp4|mov)$/i.test(f.name)
  )
  if (files.length === 0) return
  if (files.length > MAX_VIDEO_FILES) {
    showAlertDialog(`最多只能导入 ${MAX_VIDEO_FILES} 个视频，当前选择了 ${files.length} 个`, 'warning')
    return
  }
  const oversized = files.filter(f => f.size > MAX_VIDEO_FILE_SIZE)
  if (oversized.length > 0) {
    showAlertDialog(`以下视频超过50MB限制，已跳过：\n${oversized.map(f => f.name).join('\n')}`, 'warning')
  }
  const validFiles = files.filter(f => f.size <= MAX_VIDEO_FILE_SIZE)
  if (validFiles.length === 0) return
  batchVideoFiles.value = validFiles
  matchVideosToSamples(validFiles)
}

const onVideoFileChange = (e) => {
  const files = Array.from(e.target.files)
  if (files.length > MAX_VIDEO_FILES) {
    showAlertDialog(`最多只能导入 ${MAX_VIDEO_FILES} 个视频，当前选择了 ${files.length} 个`, 'warning')
    e.target.value = ''
    return
  }
  const oversized = files.filter(f => f.size > MAX_VIDEO_FILE_SIZE)
  if (oversized.length > 0) {
    showAlertDialog(`以下视频超过50MB限制，已跳过：\n${oversized.map(f => f.name).join('\n')}`, 'warning')
  }
  const validFiles = files.filter(f => f.size <= MAX_VIDEO_FILE_SIZE)
  batchVideoFiles.value = validFiles
  if (validFiles.length > 0) matchVideosToSamples(validFiles)
  e.target.value = ''
}

const goToVideoPrev = () => {
  if (videoCurrentIndex.value > 0) videoCurrentIndex.value--
}
const goToVideoNext = () => {
  if (videoCurrentIndex.value < batchVideoMatched.value.length - 1) videoCurrentIndex.value++
}

const removeVideoFile = (idx) => {
  batchVideoFiles.value.splice(idx, 1)
  batchVideoMatched.value.splice(idx, 1)
  if (batchVideoMatched.value.length === 0) return
  else if (videoCurrentIndex.value >= batchVideoMatched.value.length) videoCurrentIndex.value = batchVideoMatched.value.length - 1
}

const setVideoActionAll = (action) => {
  batchVideoMatched.value.forEach(r => { if (r.matched) r.action = action })
}

const doBatchVideoUpload = async () => {
  const uploadList = batchVideoMatched.value.filter(m => m.matched && m.action !== 'skip')
  if (uploadList.length === 0) return

  const UPLOAD_TIMEOUT_MS = 120000
  let successCount = 0
  let failCount = 0
  const failList = []

  videoUploading.value = true
  videoUploadProgress.value = { done: 0, total: uploadList.length, success: 0, fail: 0, currentFileName: '', currentProgress: 0 }

  const coverSampleIds = [...new Set(uploadList.filter(m => m.action === 'cover').map(m => m.sampleId))]
  for (const sampleId of coverSampleIds) {
    try { await api(`/videos/sample/${sampleId}`, { method: 'DELETE' }) } catch (e) {}
  }

  const fileGroups = {}
  uploadList.forEach(item => {
    const key = item.file.name + '_' + item.file.size
    if (!fileGroups[key]) fileGroups[key] = { file: item.file, items: [], sampleCodes: [] }
    fileGroups[key].items.push(item)
    fileGroups[key].sampleCodes.push(item.sampleCode || item.sampleId)
  })

  const tasks = Object.values(fileGroups).map(g => ({
    file: g.file,
    sampleIds: g.items.map(i => i.sampleId),
    sampleCodes: g.sampleCodes,
    displayName: g.items.length === 1 ? (g.items[0].sampleCode || g.file.name) : `${g.file.name} → ${g.sampleCodes.join(', ')}`
  }))

  const uploadTask = async (task) => {
    let lastError = null
    for (let attempt = 0; attempt <= 1; attempt++) {
      if (attempt > 0) await new Promise(r => setTimeout(r, 2000))
      try {
        videoUploadProgress.value.currentFileName = task.displayName
        videoUploadProgress.value.currentProgress = 0

        const xhr = new XMLHttpRequest()
        const fd = new FormData()
        fd.append('file', task.file)

        const isBatch = task.sampleIds.length > 1
        if (isBatch) {
          fd.append('sampleIds', task.sampleIds.join(','))
        } else {
          fd.append('sampleId', task.sampleIds[0])
        }

        const timeoutId = setTimeout(() => xhr.abort(), UPLOAD_TIMEOUT_MS * (isBatch ? 5 : 1))

        xhr.upload.addEventListener('progress', (e) => {
          if (e.lengthComputable) {
            videoUploadProgress.value.currentProgress = Math.round((e.loaded / e.total) * 100)
          }
        })

        const response = await new Promise((resolve, reject) => {
          xhr.onload = () => {
            clearTimeout(timeoutId)
            if (xhr.status >= 200 && xhr.status < 300) {
              try { resolve(JSON.parse(xhr.responseText)) } catch { reject(new Error('响应解析失败')) }
            } else { reject(new Error(`HTTP ${xhr.status}`)) }
          }
          xhr.onerror = () => { clearTimeout(timeoutId); reject(new Error('网络错误')) }
          xhr.onabort = () => { clearTimeout(timeoutId); reject(new Error('上传超时')) }
          xhr.open('POST', isBatch ? '/videos/batch-upload' : '/videos/upload')
          xhr.send(fd)
        })

        if (response && response.code === 200) {
          successCount += task.sampleIds.length
          return true
        }
        lastError = new Error(response?.message || '服务端返回非200')
      } catch (e) {
        lastError = e
      }
    }
    failCount += task.sampleIds.length
    failList.push(`${task.displayName}: ${lastError?.message}`)
    return false
  }

  try {
    for (let i = 0; i < tasks.length; i++) {
      await uploadTask(tasks[i])
      videoUploadProgress.value.done = i + 1
      videoUploadProgress.value.success = successCount
      videoUploadProgress.value.fail = failCount
    }

    const hasFailures = failCount > 0
    await showAlertDialog(`视频导入完成：成功 ${successCount} 个，失败 ${failCount} 个${failList.length ? '\n失败文件：\n' + failList.join('\n') : ''}`, hasFailures ? 'warning' : 'success')
    closeBatchVideoModal()
  } finally {
    videoUploading.value = false
  }
}

const doAdvancedSearch = async () => {
  try {
    const conditions = advSearchConditions.filter(c => c.value)
    const res = await api(`/samples/search?current=${currentPage.value}&size=${pageSize.value}`, {
      method: 'POST',
      body: JSON.stringify({ conditions })
    })
    const data = res.data || res || {}
    tableData.value = data.records || data.list || data || []
    totalRecords.value = data.total || tableData.value.length
    currentPage.value = 1
    showAdvancedSearch.value = false
  } catch (e) {
    console.error(e)
  }
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

const viewSampleDetail = (data) => {
  if (!data || !data.id) return
  selectSample(data)
}

watch(currentSampleImages, () => {
  if (stripIndex.value >= currentSampleImages.value.length) {
    stripIndex.value = 0
  }
})

watch(pageSize, () => {
  currentPage.value = 1
  loadTableData()
})

let customMatchDebounce = null
watch([customCodesText, customManufacturerCode], () => {
  if (batchVideoType.value !== 'custom') return
  if (batchVideoFiles.value.length === 0) return
  if (videoMatchLoading.value) return
  clearTimeout(customMatchDebounce)
  customMatchDebounce = setTimeout(() => {
    if (!customCodesText.value.trim()) return
    if (customMatchSubType.value === 'factory-code' && !customManufacturerCode.value.trim()) return
    matchVideosToSamples(batchVideoFiles.value)
  }, 600)
})

onMounted(() => {
  const route = useRoute()
  if (route.params.manufacturerCode) {
    manufacturerCode.value = route.params.manufacturerCode
  } else if (route.query.manufacturerCode) {
    manufacturerCode.value = route.query.manufacturerCode
  }
  document.addEventListener('click', closeDropdowns)
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
  if (!route.query.sampleCode) {
    loadTableData().then(() => {
      nextTick(() => {
        if (tableData.value.length > 0 && gridRef.value) {
          gridRef.value.setCurrentRow(tableData.value[0])
          selectSample(tableData.value[0])
        }
      })
    })
  }
})

watch(() => route.query.sampleId, (sampleId) => {
  if (sampleId && !isNaN(Number(sampleId))) {
    api('/samples/' + sampleId).then(res => {
      if (res.code === 200 && res.data) {
        viewSampleDetail(res.data)
      }
    })
  }
}, { immediate: true })

watch(() => route.query.sampleCode, (sampleCode) => {
  if (sampleCode) {
    searchKeyword.value = sampleCode
    onSearch()
  }
}, { immediate: true })

onBeforeUnmount(() => {
  document.removeEventListener('click', closeDropdowns)
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

<style scoped>
:deep(.vxe-pager) {
  justify-content: center;
}
</style>
