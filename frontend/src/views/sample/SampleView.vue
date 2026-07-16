<template>
  <div class="sample-page sample-samples-page">
    <div class="sample-card sample-form-card" :class="{ expanded: formExpanded }" v-show="formVisible">
      <div class="sample-form-top">
        <div class="sample-form-title">
        </div>
        <div class="sample-form-actions" style="margin-right:180px">
          <button v-if="currentSample && currentSample.id && sampleVideos.length > 0" class="sample-btn sample-btn-blue" :title="'查看视频 (' + sampleVideos.length + ')'" @click="showVideoPreviewModal = true">
            <VideoIcon :size="14" /> {{ sampleVideos.length }}
          </button>
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
          <button class="sample-btn sample-btn-ghost" :title="formVisible ? '隐藏展示区' : '显示展示区'" @click="formVisible = !formVisible">
            <EyeOff v-if="formVisible" :size="14" />
            <Eye v-else :size="14" />
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
              <div v-if="f.break" style="flex:0 0 100%;height:0"></div>
              <div v-else-if="f.group" class="sample-form-field sample-form-group" :style="f.width || f.fields.some(sf => sf.width) ? { flex: '0 0 auto' } : {}">
                <label class="sample-form-label" :style="{ ...(f.labelWidth ? { flex: '0 0 ' + f.labelWidth + 'px' } : {}), ...(f.labelJustify ? { textAlign: 'justify', textAlignLast: 'justify' } : {}) }">{{ f.label }}</label>
                <div class="sample-form-group-inputs">
                  <input v-for="sf in f.fields" :key="sf.key"
                    class="sample-form-input"
                    :readonly="formMode === 'readonly'"
                    :placeholder="sf.placeholder"
                    :title="formData[sf.key] || ''"
                    :style="sf.width ? { flex: '0 0 ' + sf.width + 'px', minWidth: sf.width + 'px' } : {}"
                    v-model="formData[sf.key]"
                    @input="onGroupInput(sf.key)"
                  />
                </div>
              </div>
              <div v-else class="sample-form-field" :style="f.width ? { flex: '0 0 auto' } : { flex: 1 }">
                <label class="sample-form-label" :style="{ ...(f.labelWidth ? { flex: '0 0 ' + f.labelWidth + 'px' } : {}), ...(f.labelJustify ? { textAlign: 'justify', textAlignLast: 'justify' } : {}), ...(f.color ? { color: f.color } : {}) }">{{ f.label }}</label>
                <input
                  class="sample-form-input"
                  :readonly="formMode === 'readonly'"
                  :placeholder="formMode === 'readonly' ? '' : f.label"
                  :title="formData[f.key] || ''"
                  :style="{ ...(f.width ? { flex: '0 0 ' + f.width + 'px' } : { flex: 1 }), ...(f.color ? { color: f.color } : {}) }"
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
                @click.stop="viewOriginal"
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
            <span class="sample-image-strip-empty" style="cursor:pointer" @click="viewOriginal">暂无图片</span>
          </template>
          <label v-if="formMode === 'edit' || formMode === 'add'" class="sample-image-upload-btn">
            <Upload :size="22" /> 上传
            <input type="file" accept="image/*" hidden @change="onImageUpload" />
          </label>
        </div>
      </div>
    </div>

    <div class="sample-card sample-toolbar-card">
      <div class="sample-toolbar-row">
        <!-- 展示区隐藏时，在搜索框左侧显示恢复按钮 -->
        <button v-if="!formVisible" class="sample-btn sample-btn-primary" style="font-size:11px;height:30px;flex-shrink:0;margin-right:6px" @click="formVisible = true" title="显示展示区">
          <Eye :size="22" />
        </button>
        <div class="sample-search">
          <Search :size="14" />
          <input
            v-model="searchKeyword"
            placeholder="模糊搜索..."
            @keyup.enter="onSearch"
          />
        </div>
        <button class="sample-btn sample-btn-primary" @click="onSearch">
          <Search :size="22" /> 查询
        </button>
        <div class="sample-search">
          <Crosshair :size="14" />
          <input
            v-model="locateKeyword"
            placeholder="定位搜索..."
            @keyup.enter="onLocate"
          />
          <button v-if="locateKeyword" class="sample-search-clear" @click="locateKeyword='';locateCursor=-1">&times;</button>
        </div>
        <button class="sample-btn sample-btn-ghost" @click="onLocate">
          <MapPin :size="22" /> 定位
        </button>
        <button v-if="searchKeyword || locateKeyword" class="sample-btn sample-btn-ghost" @click="clearSearch">
          <X :size="22" /> 清除
        </button>
        <div class="toolbar-sep"></div>
        <button class="sample-btn sample-btn-primary" @click="startAdd">
          <Plus :size="22" /> 添加资料
        </button>
        <template v-if="formMode === 'edit' || formMode === 'add'">
          <button class="sample-btn sample-btn-primary" @click="saveSample">
            <Save :size="22" /> 保存
          </button>
          <button class="sample-btn sample-btn-ghost" @click="cancelEdit">
            <X :size="22" /> 取消
          </button>
        </template>
        <button v-else class="sample-btn sample-btn-ghost" :disabled="!currentSample" @click="startEdit">
          <Pencil :size="22" /> 修改
        </button>
        <button class="sample-btn sample-btn-danger" :disabled="selectedIds.length === 0" @click="onDeleteSelected">
          <Trash2 :size="14" /> 删除
        </button>
        <button class="sample-btn sample-btn-ghost" @click="openImportModal">
          <Upload :size="14" /> 导入资料
        </button>
        <div class="sample-more-dropdown" style="position:relative">
          <button class="sample-btn sample-btn-ghost" @click.stop="toggleMoreDropdown">
            <MoreHorizontal :size="20" /> 其他功能
          </button>
        </div>
        <Teleport to="body">
          <div v-if="showMoreDropdown" class="sample-more-dropdown-panel" :style="moreDropdownStyle">
            <div class="sample-more-group-label"><PackageOpen :size="16" /> 导入</div>
            <div class="sample-more-item" @click="downloadTemplate"><Download :size="18" /> 下载导入模板</div>
            <div class="sample-more-item" @click="openBatchImageModal"><ImagePlus :size="18" /> 批量导入图片</div>
            <div class="sample-more-item" @click="openBatchVideoModal"><VideoIcon :size="18" /> 批量导入视频</div>
            <div class="sample-more-sep"></div>
            <div class="sample-more-group-label"><Database :size="16" /> 数据</div>
            <div class="sample-more-item" @click="openReferenceDataModal"><ListChecks :size="18" /> 对照资料管理</div>
            <div class="sample-more-item" @click="openRestoreDeletedModal"><RotateCcw :size="18" /> 恢复误删数据</div>
            <div class="sample-more-item" @click="openMainBatchQuery"><List :size="18" /> 按编号批量查询</div>
            <div class="sample-more-item" @click="batchSetPrice"><Coins :size="18" /> 批量设置价格</div>
            <div class="sample-more-sep"></div>
            <div class="sample-more-group-label"><FileOutput :size="16" /> 导出</div>
            <div class="sample-more-item" @click="exportExcel"><FileDown :size="18" /> 导出Excel</div>
            <div class="sample-more-sep"></div>
            <div class="sample-more-item sample-more-item-accent" @click="openReportDesigner"><LayoutGrid :size="18" /> 报表设计器</div>
          </div>
        </Teleport>
        <div class="toolbar-sep"></div>
        <button class="sample-btn sample-btn-ghost" @click="openAdvancedSearch">
          <Filter :size="14" /> 综合查询
        </button>
        <div class="toolbar-sep"></div>
        <button class="sample-btn sample-btn-ghost" @click="doPrintTable">
          <Printer :size="14" /> 大条码打印
        </button>
        <div class="sample-more-dropdown" style="position:relative">
          <button class="sample-btn sample-btn-ghost" @click.stop="togglePrintDropdown">
            <Printer :size="18" /> 其他打印 <ChevronsDown :size="16" />
          </button>
        </div>
        <button class="sample-btn sample-btn-primary" @click="openVendorConfirmReport">
          <FileSpreadsheet :size="14" /> 打印报价
        </button>

        <button class="sample-btn sample-btn-ghost" @click="router.push({ name: 'ImageSearch' })">
          <ImageIcon :size="14" /> 图像搜索
        </button>
        <div class="toolbar-sep"></div>
        <span v-if="searchElapsed != null" style="margin-left:auto;color:#3b82f6;font-size:20px;white-space:nowrap;align-self:center">
          查询耗时 {{ searchElapsed }} ms
        </span>
      </div>
    </div>

    <div class="sample-table-card">
      <div ref="tableWrapRef" class="sample-table-wrap">
        <vxe-grid v-if="prefReady"
          ref="gridRef"
          :id="gridStorageKey"
          :columns="allColumns"
          :data="tableData"
          :loading="tableLoading"
          :height="tableWrapHeight"
          :toolbar-config="gridToolbarConfig"
          :custom-config="{ storage: true }"
          :column-config="{ resizable: true, drag: true }"
          :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
          :cell-config="{ height: 100 }"
          :checkbox-config="{ highlight: true, range: true }"
          :sort-config="{ trigger: 'header', remote: true, defaultSort: { field: 'recent', order: 'desc' } }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 0, rSize: 100, rHeight: 100 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :virtual-x-config="{ enabled: true, gt: 0 }"
          :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 0 }, scrollY: { gt: 0, oSize: 0, rSize: 100, rHeight: 100 } }"
          :border="true"
          :header-cell-style="headerCellStyleFn"
          :cell-style="cellAreaStyle"
          @checkbox-change="onCheckboxChange"
          @checkbox-all="onCheckboxAll"
          @cell-click="onCellClick"
          @sort-change="onSortChange"
          @resizable-change="saveGridPrefs"
          @custom="onCustomChange"
          @column-dragstart="onColumnDragStart"
          @column-dragend="onColumnDragEnd"
        >
          <template #image_default="{ row }">
            <div style="display:flex;align-items:center;justify-content:center;height:100%;line-height:1">
              <img
                v-if="row.thumbnail"
                :src="'/thumbnails/' + row.thumbnail"
                loading="lazy"
                draggable="true"
                style="width:80px;height:80px;object-fit:cover;border-radius:6px;cursor:pointer;-webkit-user-drag:element"
                @click.stop="openPhotoModalFor(row)"
                @mouseenter="onThumbMouseEnter($event, row)"
                @mouseleave="onThumbMouseLeave"
                @dragstart="onSampleImgDragStart"
              />
              <span v-else style="display:flex;align-items:center;justify-content:center;width:64px;height:64px;color:rgba(29,29,31,0.25);font-size:11px;cursor:pointer;border-radius:6px;border:1px dashed rgba(0,0,0,0.1);background:rgba(0,0,0,0.02)" @click.stop="openPhotoModalFor(row)">无图</span>
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

      <!-- 卡片覆盖层：绝对定位，不影响布局 -->
      <div v-if="cardMode" ref="cardOverlayRef" class="sample-card-overlay" @scroll.passive="onCardScroll">
        <div class="sample-card-scroll-body" :style="{ paddingTop: cardSpacerTop + 'px', paddingBottom: cardSpacerBottom + 'px' }">
          <div class="sample-card-grid">
            <div v-for="item in cardVisibleItems" :key="item.id"
                 class="sample-card-item" :class="{ 'card-selected': isCardSelected(item) }" @click="onCellClick({ row: item })">
              <div class="sample-card-img" @click.stop="openPhotoModalFor(item)">
                <div class="card-checkbox" :class="{ checked: isCardSelected(item) }" @click.stop="toggleCardSelect(item)">
                  <Check v-if="isCardSelected(item)" :size="14" />
                </div>
                <img v-if="item.thumbnail || item.firstImageHash" :src="item.thumbnail ? '/thumbnails/' + item.thumbnail : '/images/view/hash/' + item.firstImageHash" :data-thumb="item.thumbnail" @error="onCardImgError" @click.stop="openPhotoModalFor(item)" @mouseenter="onThumbMouseEnter($event, item)" @mouseleave="onThumbMouseLeave" loading="lazy" decoding="async" draggable="true" @dragstart="onSampleImgDragStart" style="-webkit-user-drag:element" />
                <div v-else class="sample-card-no-img" @click.stop="openPhotoModalFor(item)"><ImageIcon :size="36" /></div>
              </div>
              <div class="sample-card-body">
                <div class="sample-card-name" :title="item.sampleName">{{ item.sampleName || '0' }}</div>
                <div class="sample-card-fields">
                  <span class="card-val card-val-copy card-code" :title="item.sampleCode">
                    {{ item.sampleCode || '0' }}
                    <button v-if="item.sampleCode" class="card-copy-btn" @click.stop="copyCardCode(item.sampleCode)" :title="'复制 ' + item.sampleCode"><Copy :size="16" /></button>
                  </span>
                  <span class="card-val" :title="item.factoryCode">{{ item.factoryCode || '0' }}</span>
                  <span class="card-val" :title="(item.innerBoxCount ?? '0') + ' / ' + (item.cartonCapacity ?? '0')">{{ item.innerBoxCount ?? '0' }} / {{ item.cartonCapacity ?? '0' }}</span>
                  <span class="card-val" :title="(item.cartonGrossWeight ?? '0') + ' / ' + (item.cartonNetWeight ?? '0')">{{ item.cartonGrossWeight ?? '0' }} / {{ item.cartonNetWeight ?? '0' }}</span>
                  <span class="card-val" :title="(item.cartonMaterialVolume ?? '0') + ' / ' + (item.cartonVolume ?? '0')">{{ item.cartonMaterialVolume ?? '0' }} / {{ item.cartonVolume ?? '0' }}</span>
                  <span class="card-val" :title="item.boothNo">{{ item.boothNo || '0' }}</span>
                  <span class="card-val card-price" :title="item.factoryPrice != null ? '¥' + item.factoryPrice : '0'">{{ item.factoryPrice != null ? '¥' + item.factoryPrice : '0' }}</span>
                </div>
                <div class="sample-card-divider"></div>
                <div class="sample-card-fields" style="grid-template-columns:1fr">
                  <span class="card-val" :title="item.name">{{ item.name || '0' }}</span>
                  <span class="card-val" :title="item.mobile1">{{ item.mobile1 || '0' }}</span>
                  <span class="card-val" :title="formatCardDate(item.createTime)">{{ formatCardDate(item.createTime) || '0' }}</span>
                  <span class="card-val" :title="formatCardDate(item.updateTime)">{{ formatCardDate(item.updateTime) || '0' }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="thumbSortClone.show" class="ip-sort-clone" :style="{ left: thumbSortClone.x + 'px', top: thumbSortClone.y + 'px', width: thumbSortClone.width + 'px', height: thumbSortClone.height + 'px' }">
            <img :src="thumbSortClone.src" />
          </div>
        </div>
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
          <button
            class="sample-btn sample-btn-card-toggle"
            :title="cardMode ? '切换到表格模式' : '切换到卡片模式'"
            @click="cardMode = !cardMode"
            style="margin-right:8px"
          >
            <LayoutGrid :size="14" />
            <span>{{ cardMode ? '表格' : '卡片' }}</span>
          </button>
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
    <div v-if="showRestoreDeletedModal" class="batch-image-modal-overlay import-modal-overlay" style="background:transparent;backdrop-filter:none">
      <div class="batch-image-modal" style="width:85vw;height:85vh;display:flex;flex-direction:column">
        <div class="batch-image-modal-header" style="flex-shrink:0">
          <strong style="font-size:24px;">恢复误删数据</strong>
          <div style="display:flex;align-items:center;gap:12px;">
            <select v-model="deletedFilterField" style="height:48px;border:1px solid #d1d1d6;border-radius:8px;padding:0 14px;font-size:20px;background:#fff;color:#333">
              <option value="">全部字段</option>
              <option value="sampleCode">公司编号</option>
              <option value="manufacturerCode">厂商编号</option>
              <option value="updateTime">删除日期</option>
            </select>
            <input v-model="deletedFilterKeyword" placeholder="搜索..." style="width:240px;height:48px;border:1px solid #d1d1d6;border-radius:8px;padding:0 14px;font-size:20px;outline:none" @keyup.enter="doDeletedFilter" />
            <button class="sample-btn sample-btn-primary" style="height:44px;padding:0 20px;font-size:18px;border-radius:8px" @click="doDeletedFilter">筛选</button>
            <button v-if="deletedFilterActive" class="sample-btn sample-btn-ghost" style="height:44px;padding:0 18px;font-size:18px;border-radius:8px" @click="doDeletedResetFilter">清除</button>
            <button class="sample-btn sample-btn-ghost" style="font-size:18px;padding:8px 18px;border-radius:8px" @click="doDeletedFilter">刷新</button>
            <button class="sample-btn sample-btn-ghost" style="font-size:18px;padding:8px 18px" @click="openDeletedBatchQuery">批量查询</button>
            <button class="sample-btn sample-btn-ghost" style="font-size:20px;padding:10px 24px;" @click="showRestoreDeletedModal = false">关闭</button>
          </div>
        </div>
        <div class="batch-image-modal-body" style="padding:16px 24px;flex:1;overflow:hidden;display:flex;flex-direction:column;">
          <vxe-grid id="deletedGrid" ref="deletedGridRef" :columns="deletedGridColumns" :data="deletedData" :loading="deletedLoading"
            height="100%"
            :checkbox-config="{ highlight: true, range: true }"
            :pager-config="{ enabled: true, pageSize: 50, pageSizes: [50, 100, 200, 500], layouts: ['Total', 'PrevPage', 'PrevJump', 'Number', 'NextPage', 'NextJump', 'Sizes', 'FullJump'] }"
            :toolbar-config="{ custom: true }"
            :column-config="{ resizable: true, drag: true }"
            :sort-config="{ trigger: 'header', remote: false, sortMethod: deletedSortMethod }"
            :row-config="{ isHover: true, keyField: 'id' }"
            :cell-config="{ height: 80 }"
            :virtual-y-config="{ enabled: true }"
            :scroll-y="{ enabled: true, gt: 0, oSize: 0 }"
            :scroll-x="{ enabled: true, gt: 0, oSize: 0 }"
            :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0 }, scrollY: { gt: 0, oSize: 0, rSize: 80, rHeight: 80 } }"
            :border="true"
            :header-cell-style="{ background: '#e8f0fe', borderColor: '#d0d5dd', fontSize: '16px', fontWeight: 600, textAlign: 'center', borderBottom: '2px solid #d0d5dd' }"
            :cell-style="{ textAlign: 'center', fontSize: '15px' }"
            @checkbox-change="onDeletedCheckChange" @checkbox-all="onDeletedCheckChange">
          </vxe-grid>
        </div>
        <div class="modal-footer" style="display:flex;align-items:center;justify-content:space-between;padding:10px 24px;border-top:1px solid #e5e5ea;">
          <div style="display:flex;align-items:center;gap:16px;font-size:18px;color:#666;">
            已选 <strong>{{ deletedSelected.length }}</strong> / 共 <strong>{{ deletedTotal }}</strong> 条
          </div>
          <button class="sample-btn sample-btn-primary" style="font-size:18px;padding:8px 28px;" :disabled="deletedSelected.length === 0"
            @click="doRestoreDeleted">恢复选中 ({{ deletedSelected.length }})</button>
        </div>
      </div>
    </div>

    </Teleport>

    <!-- 货号重复冲突解决模态框 -->
    <Teleport to="body">
    <div v-if="showBatchConflictModal" class="batch-conflict-modal-overlay" @click.self="cancelBatchConflict">
      <div class="batch-conflict-modal">
        <div class="batch-conflict-modal-header">
          <strong>货号重复 - 请选择目标样品</strong>
          <span class="bm-info-hint" style="margin-left:12px">以下货号在厂商内存在多条记录，请为每个货号指定目标</span>
          <div style="flex:1"></div>
          <button class="modal-close-btn" @click="cancelBatchConflict"><X :size="16" /></button>
        </div>
        <div class="batch-conflict-modal-body">
          <div v-for="conflict in batchConflicts" :key="conflict.code" class="batch-conflict-group">
            <div class="batch-conflict-group-header">
              <img v-if="conflict.uploadPreviewUrl" :src="conflict.uploadPreviewUrl" class="batch-conflict-upload-img" @click="conflictPreviewSrc = conflict.uploadPreviewUrl" />
              <div style="flex:1">
                <span class="batch-conflict-code-label">货号: <strong>{{ conflict.code }}</strong></span>
                <span class="batch-conflict-count">共 {{ conflict.samples.length }} 条匹配</span>
              </div>
              <button class="batch-conflict-remove-btn" @click="removeConflictCode(conflict.code)"><Trash2 :size="14" /> 移除本条</button>
            </div>
            <vxe-grid
              :columns="conflictGridColumns"
              :data="conflict.samples"
              :row-config="{ isCurrent: false, isHover: true }"
              :checkbox-config="{ highlight: true, range: true }"
              @checkbox-change="({ row }) => toggleConflictRow(conflict, row)"
              @checkbox-all="({ records }) => toggleConflictAll(conflict, records)"
              max-height="280"
              size="small"
              border
              class="batch-conflict-table"
              header-align="center"
              :toolbar-config="{ zoom: true, custom: true }"
            >
              <template #conflictImage="{ row }">
                <img
                  v-if="row._thumb || row.thumbnail || row.firstImageHash"
                  :src="row._thumb ? '/thumbnails/' + row._thumb : (row.thumbnail ? '/thumbnails/' + row.thumbnail : '/images/view/hash/' + row.firstImageHash)"
                  style="width:48px;height:48px;object-fit:cover;border-radius:4px;cursor:pointer"
                  @click="conflictPreviewSrc = row.firstImageHash ? '/images/view/hash/' + row.firstImageHash : (row._thumb ? '/thumbnails/' + row._thumb : (row.thumbnail ? '/thumbnails/' + row.thumbnail : ''))"
                />
                <span v-else style="color:#ccc;font-size:12px">暂无</span>
              </template>
            </vxe-grid>
          </div>
        </div>
        <div class="modal-footer">
          <span v-if="conflictValidationMsg" class="conflict-validation-msg">{{ conflictValidationMsg }}</span>
          <button class="sample-btn sample-btn-ghost" @click="cancelBatchConflict">取消</button>
          <button class="sample-btn sample-btn-primary" @click="confirmConflictSelection">
            确认选择并继续匹配
          </button>
        </div>
      </div>
    </div>
    </Teleport>

    <!-- 冲突图片预览 -->
    <Teleport to="body">
    <div v-if="conflictPreviewSrc" class="conflict-img-preview-overlay" @click.self="conflictPreviewSrc = ''">
      <button class="conflict-img-preview-close" @click="conflictPreviewSrc = ''"><X :size="24" /></button>
      <img :src="conflictPreviewSrc" style="max-width:90vw;max-height:90vh;object-fit:contain;border-radius:8px;box-shadow:0 8px 32px rgba(0,0,0,0.3)" />
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showMainBatchQuery" class="batch-image-modal-overlay import-modal-overlay" style="background:transparent">
      <div class="batch-image-modal" style="width:900px">
        <div class="batch-image-modal-header">
          <strong>按编号批量查询</strong>
          <button class="modal-close-btn" @click="showMainBatchQuery = false"><X :size="22" /></button>
        </div>
        <div class="batch-image-modal-body" style="display:flex;flex-direction:column;gap:16px">
          <div style="display:flex;gap:20px;align-items:center">
            <label style="display:flex;align-items:center;gap:8px;font-size:22px;cursor:pointer">
              <input type="radio" v-model="mainBatchField" value="sampleCode" style="accent-color:#007aff;width:20px;height:20px" />
              公司编号
            </label>
            <label style="display:flex;align-items:center;gap:8px;font-size:22px;cursor:pointer">
              <input type="radio" v-model="mainBatchField" value="factoryCode" style="accent-color:#007aff;width:20px;height:20px" />
              出厂货号
            </label>
          </div>
          <textarea v-model="mainBatchInput" placeholder="每行一个编号，可输入多个..."
            style="width:100%;height:360px;border:1px solid #d1d1d6;border-radius:8px;padding:14px 16px;font-size:22px;line-height:1.6;resize:vertical;outline:none;box-sizing:border-box"
          ></textarea>
          <div style="display:flex;gap:12px;justify-content:flex-end">
            <button class="sample-btn sample-btn-ghost" style="height:44px;padding:0 20px;font-size:20px;border-radius:8px" @click="showMainBatchQuery = false">取消</button>
            <button class="sample-btn sample-btn-primary" style="height:44px;padding:0 24px;font-size:20px;border-radius:8px" @click="doMainBatchQuery">查询</button>
          </div>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showDeletedBatchQuery" class="batch-image-modal-overlay" style="background:transparent;backdrop-filter:none" @click.self="showDeletedBatchQuery = false">
      <div class="batch-image-modal" style="width:600px">
        <div class="batch-image-modal-header">
          <strong style="font-size:22px;">批量查询</strong>
          <button class="sample-btn sample-btn-ghost" style="font-size:18px;padding:6px 16px;" @click="showDeletedBatchQuery = false">关闭</button>
        </div>
        <div class="batch-image-modal-body" style="display:flex;flex-direction:column;gap:16px;padding:20px 24px;">
          <div style="display:flex;gap:24px;align-items:center">
            <label style="display:flex;align-items:center;gap:8px;font-size:18px;cursor:pointer">
              <input type="radio" v-model="deletedBatchField" value="sampleCode" style="accent-color:#007aff;width:18px;height:18px" />
              公司编号
            </label>
            <label style="display:flex;align-items:center;gap:8px;font-size:18px;cursor:pointer">
              <input type="radio" v-model="deletedBatchField" value="factoryCode" style="accent-color:#007aff;width:18px;height:18px" />
              出厂货号
            </label>
          </div>
          <textarea v-model="deletedBatchInput" placeholder="每行一个编号，可输入多个..."
            style="width:100%;height:240px;border:1px solid #d1d1d6;border-radius:8px;padding:14px 16px;font-size:18px;line-height:1.8;resize:vertical;outline:none;box-sizing:border-box"
          ></textarea>
          <div style="display:flex;gap:12px;justify-content:flex-end">
            <button class="sample-btn sample-btn-ghost" style="font-size:18px;padding:8px 20px;border-radius:8px" @click="showDeletedBatchQuery = false">取消</button>
            <button class="sample-btn sample-btn-primary" style="font-size:18px;padding:8px 24px;border-radius:8px" @click="doDeletedBatchQuery">查询</button>
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
          <template v-if="(batchResult.failFiles?.length || 0) + (batchResult.unmatchedFiles?.length || 0) > 0">
            <button class="sample-btn sample-btn-primary" @click="downloadFailedZip">
              <Download :size="14" /> 下载失败/未匹配图片 ({{ (batchResult.failFiles?.length || 0) + (batchResult.unmatchedFiles?.length || 0) }} 张 ZIP)
            </button>
          </template>
          <button class="sample-btn sample-btn-ghost" @click="showBatchResultModal = false">关闭</button>
        </div>
      </div>
    </div>

    <div v-if="showScanPrintModal" class="batch-image-modal-overlay import-modal-overlay" style="background:transparent">
      <div class="batch-image-modal scan-print-modal" style="width:1600px;max-height:92vh;display:flex;flex-direction:column">
        <div class="batch-image-modal-header">
          <span style="font-size:28px;font-weight:700">扫码打印</span>
          <button class="modal-close-btn" @click="showScanPrintModal = false"><X :size="28" /></button>
        </div>
        <div class="batch-image-modal-body" style="flex:1;overflow-y:auto;padding:28px 32px">
          <div style="display:flex;gap:32px;margin-bottom:20px">
            <div style="flex-shrink:0;width:520px">
              <label style="font-size:28px;font-weight:600;color:#1d1d1f;display:block;margin-bottom:10px">公司编号</label>
              <div style="display:flex;gap:12px">
                <input type="text" ref="scanPrintInputRef" v-model="scanPrintCode" placeholder="输入或扫码公司编号..."
                  style="flex:1;height:60px;border-radius:12px;border:1px solid rgba(0,122,255,0.2);outline:none;padding:0 18px;font-size:28px;font-weight:600;color:#1d1d1f;"
                  @keyup.enter="searchScanPrint" />
                <button class="sample-btn sample-btn-blue" style="height:60px;font-size:26px;padding:0 24px" @click="searchScanPrint">查询</button>
              </div>
              <div style="margin-top:12px;display:flex;align-items:center;gap:8px">
                <label style="display:flex;align-items:center;gap:8px;cursor:pointer;font-size:24px;color:rgba(29,29,31,0.6);user-select:none" @click="scanPrintContinuous = !scanPrintContinuous">
                  <span style="width:28px;height:28px;border-radius:6px;border:2px solid;display:flex;align-items:center;justify-content:center;transition:all 0.15s"
                    :style="scanPrintContinuous ? 'background:#007aff;border-color:#007aff;color:#fff' : 'border-color:rgba(29,29,31,0.25)'">
                    <span v-if="scanPrintContinuous" style="font-size:16px;line-height:1">✓</span>
                  </span>
                  连续打印（查询后自动打印）
                </label>
              </div>
              <div v-if="scanPrintResult" style="margin-top:20px;padding:22px;border-radius:14px;border:1px solid rgba(0,122,255,0.12);background:rgba(0,122,255,0.03)">
                <div style="font-size:28px;font-weight:700;color:#1d1d1f;margin-bottom:12px">{{ scanPrintResult.sampleCode || '-' }}</div>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;font-size:24px;color:rgba(29,29,31,0.7)">
                  <div><span style="color:rgba(29,29,31,0.4)">出厂货号：</span>{{ scanPrintResult.factoryCode || scanPrintResult.packageCode || '-' }}</div>
                  <div><span style="color:rgba(29,29,31,0.4)">摊位号：</span><span style="color:#007aff;font-weight:600">{{ scanPrintResult.boothNo || '-' }}</span></div>
                  <div><span style="color:rgba(29,29,31,0.4)">中文包装：</span>{{ scanPrintResult.packagingCn || '-' }}</div>
                  <div><span style="color:rgba(29,29,31,0.4)">内盒/装箱：</span>{{ scanPrintResult.innerBoxCount != null ? scanPrintResult.innerBoxCount : '0' }}/{{ scanPrintResult.cartonCapacity || '-' }}</div>
                </div>
                <div style="margin-top:10px;font-size:24px;color:rgba(29,29,31,0.7)">
                  <span style="color:rgba(29,29,31,0.4)">样品名称：</span>{{ scanPrintResult.sampleName || '-' }}
                </div>
              </div>
              <div v-if="scanPrintError" style="margin-top:20px;padding:16px 22px;border-radius:12px;background:rgba(255,59,48,0.06);color:#ff3b30;font-size:26px;font-weight:600">
                {{ scanPrintError }}
              </div>
            </div>
            <div style="flex:1;display:flex;align-items:center;justify-content:center;border-radius:14px;border:1px solid rgba(0,122,255,0.12);min-height:600px;background:rgba(0,0,0,0.02);padding:20px;box-sizing:border-box">
              <img v-if="scanPrintImageSrc" :src="scanPrintImageSrc" style="max-width:100%;max-height:100%;object-fit:contain;border-radius:10px" />
              <div v-else style="text-align:center;color:rgba(29,29,31,0.3);font-size:26px">
                <ImageIcon :size="80" style="margin-bottom:12px;opacity:0.3" />
                <div>输入公司编号查询后显示图片</div>
              </div>
            </div>
          </div>

          <div v-if="scanPrintResult || scanPrintContinuous" style="border-top:1px solid rgba(0,122,255,0.08);padding-top:20px;display:flex;align-items:center;justify-content:space-between">
            <div style="display:flex;align-items:center;gap:20px">
              <label class="radio-item-mp" :class="{active:scanPrintType==='barcode'}" @click="scanPrintType='barcode'">
                <span class="radio-dot"></span> 大条码 (50×40mm)
              </label>
              <label class="radio-item-mp" :class="{active:scanPrintType==='quarter'}" @click="scanPrintType='quarter'">
                <span class="radio-dot"></span> 小条码 (25×25mm)
              </label>
              <div style="display:flex;align-items:center;gap:10px;margin-left:4px">
                <label style="font-size:26px;font-weight:600;color:rgba(29,29,31,0.5);white-space:nowrap">打印张数</label>
                <input type="number" v-model.number="scanPrintCount" min="1" max="99"
                  style="width:100px;height:56px;border-radius:10px;border:1px solid rgba(0,122,255,0.2);outline:none;text-align:center;font-size:28px;font-weight:600;color:#007aff;padding:0 8px" />
              </div>
            </div>
            <button class="sample-btn sample-btn-primary" style="font-size:24px;height:56px;padding:0 32px" @click="doScanPrint">立即打印</button>
          </div>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showBatchVideoModal" class="batch-image-modal-overlay import-modal-overlay">
       <div class="batch-image-modal" style="width:1400px">
        <div class="batch-image-modal-header">
          <strong>批量导入视频</strong>
          <button class="modal-close-btn" @click="closeBatchVideoModal"><X :size="22" /></button>
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
            <div class="upload-icon"><VideoIcon :size="64" /></div>
            <div class="upload-text">点击或拖拽上传视频文件</div>
            <div class="upload-hint" v-if="batchVideoType !== 'custom'">支持 MP4 / MOV，文件名需包含编号</div>
            <div class="upload-hint" v-else>支持 MP4 / MOV，视频将按顺序与输入的编号匹配</div>
            <input ref="batchVideoFileInput" type="file" accept=".mp4,.mov,.MP4,.MOV" multiple hidden @change="onVideoFileChange" />
          </div>

          <div v-if="videoMatchLoading" style="text-align:center;padding:24px;color:#999;font-size:20px">正在匹配样品...</div>

          <div v-else-if="batchVideoMatched.length > 0" class="batch-match-results">
            <div class="batch-nav">
              <button class="batch-nav-btn" :disabled="videoCurrentIndex === 0" @click="goToVideoPrev">
                <ChevronLeft :size="22" />
              </button>
              <span class="batch-nav-counter">第 {{ videoCurrentIndex + 1 }} / {{ batchVideoMatched.length }} 项</span>
              <button class="batch-nav-btn" :disabled="videoCurrentIndex >= batchVideoMatched.length - 1" @click="goToVideoNext">
                <ChevronRight :size="22" />
              </button>
            </div>

            <div class="batch-match-card-single">
              <template v-if="batchVideoMatched[videoCurrentIndex].matched">
                <div style="width:100%;max-height:420px;background:#000;border-radius:8px;overflow:hidden;margin-bottom:12px">
                  <video
                    :src="batchVideoMatched[videoCurrentIndex].previewUrl"
                    controls
                    style="width:100%;max-height:420px;display:block"
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
                  <span class="bmc-remove" @click="removeVideoFile(videoCurrentIndex)"><X :size="18" /></span>
                </div>
              </template>
              <template v-else>
                <div class="bmc-unmatched">
                  <ImageIcon :size="28" />
                  <span>{{ batchVideoMatched[videoCurrentIndex].file.name }} — 未匹配到样品</span>
                  <span class="bmc-remove" @click="removeVideoFile(videoCurrentIndex)"><X :size="18" /></span>
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
            <Upload :size="18" /> {{ videoUploading ? `上传中 ${videoUploadProgress.done}/${videoUploadProgress.total} (成功${videoUploadProgress.success} 失败${videoUploadProgress.fail})` : `开始上传 (${batchVideoMatched.filter(m => m.matched && m.action !== 'skip').length})` }}
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
              <video :src="'/videos/file/' + video.sampleId + '/' + (video.fileName || video.filePath)" preload="metadata" class="video-preview-thumb"></video>
              <div class="video-preview-name">{{ video.fileName }}</div>
              <div class="video-preview-size">{{ formatFileSize(video.fileSize) }}</div>
            </div>
          </div>
          <div v-if="sampleVideos.length > 0" class="video-preview-player">
            <video :src="'/videos/file/' + sampleVideos[videoPreviewIndex].sampleId + '/' + (sampleVideos[videoPreviewIndex].fileName || sampleVideos[videoPreviewIndex].filePath)" controls style="width:100%;max-height:400px;display:block;background:#000"></video>
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
      <div class="image-preview-dialog" :class="{ 'ip-dragover': ipDragOver && !ipUploadLocked }" @dragenter.prevent="onIpDragEnter" @dragover.prevent="onIpDragOver" @dragleave="handlePreviewDragLeave" @drop="onIpDrop">
        <div v-if="ipDragOver && !ipUploadLocked" class="ip-drag-hint">
          <div class="ip-drag-hint-icon"><ImagePlus :size="48" /></div>
          <div class="ip-drag-hint-text">释放鼠标以上传图片</div>
        </div>
        <div class="ip-header">
          <div class="ip-header-left">
            <ImageIcon :size="18" />
            <span>图片预览</span>
            <span class="ip-count" v-if="imagePreviewList.length > 1">{{ imagePreviewIndex + 1 }} / {{ imagePreviewList.length }}</span>
          </div>
          <div class="ip-header-right">
            <!-- 上传锁 -->
            <button class="ip-lock-btn" :class="{ locked: ipUploadLocked }" @click="ipUploadLocked = !ipUploadLocked" :title="ipUploadLocked ? '点击解锁拖拽上传' : '点击锁定拖拽上传'">
              <Lock v-if="ipUploadLocked" :size="14" />
              <Unlock v-else :size="14" />
            </button>
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
          <div class="ip-main" @wheel.prevent="onIpWheel" @mousemove="onIpMouseMove" @mouseup="onIpMouseUp" @mouseleave="onIpMouseUp">
            <img :src="currentPreviewSrc"
                 draggable="false"
                 @dragstart.prevent
                 @mousedown="onIpMouseDown"
                 @click.stop
                 :style="{ transform: `translate(${ipPanX}px, ${ipPanY}px) scale(${ipZoom})`, cursor: ipZoom <= 1 ? 'pointer' : 'grab' }" />
            <button v-if="imagePreviewList.length > 1" class="ip-nav ip-prev" @click="imagePreviewIndex = imagePreviewIndex > 0 ? imagePreviewIndex - 1 : imagePreviewList.length - 1"><ChevronLeft :size="24" /></button>
            <button v-if="imagePreviewList.length > 1" class="ip-nav ip-next" @click="imagePreviewIndex = imagePreviewIndex < imagePreviewList.length - 1 ? imagePreviewIndex + 1 : 0"><ChevronRight :size="24" /></button>
          </div>
          <div v-if="imagePreviewList.length > 1" class="ip-thumbs">
            <div
              v-for="(img, idx) in imagePreviewList" :key="img.hash || idx"
              class="ip-thumb" :class="{ active: idx === imagePreviewIndex, 'ip-thumb-popout': posPickerIdx === idx, 'ip-thumb-drag': thumbDragIdx === idx, 'ip-thumb-dragover': thumbDragOverIdx === idx }"
              @mousedown.prevent="onThumbSortDown($event, idx)"
              @click="thumbDragDone ? (thumbDragDone = false) : (imagePreviewIndex = idx)"
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
          <!-- 缩放工具栏 -->
          <div class="ip-zoom-bar" v-if="imagePreviewList.length > 0">
            <span class="ip-zoom-label">{{ Math.round(ipZoom * 100) }}%</span>
            <button class="ip-zoom-btn" @click="ipZoom = Math.min(5, +(ipZoom + 0.25).toFixed(2))">＋</button>
            <button class="ip-zoom-btn" @click="ipZoom = Math.max(0.3, +(ipZoom - 0.25).toFixed(2))">－</button>
            <button class="ip-zoom-btn" @click="ipZoom = 1; ipPanX = 0; ipPanY = 0">1:1</button>
          </div>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showPhotoModal" class="sample-photo-modal" :style="photoModalStyle">
      <div class="spm-header" @mousedown="startDragModal">
        <span class="spm-header-title">样品信息预览</span>
        <button class="spm-header-close" @click="closePhotoModal">&times;</button>
      </div>
      <div class="spm-body">
        <div class="spm-top-card" v-if="photoModalSample" style="display:none">
          <div class="spm-top-card-field"><span>样品名称</span><strong>{{ photoModalSample.sampleName || '-' }}</strong></div>
          <div class="spm-top-card-field"><span>公司编号</span><strong>{{ photoModalSample.sampleCode || '-' }}</strong></div>
          <div class="spm-top-card-field"><span>出厂货号</span><strong>{{ photoModalSample.factoryCode || '-' }}</strong></div>
          <div class="spm-top-card-field" v-if="!hideFactoryPrice"><span>出厂价</span><strong class="spm-price">{{ photoModalSample.factoryPrice || '-' }}</strong></div>
        </div>
        <div class="spm-body-main">
        <div class="spm-body-left">
          <div class="spm-main-img-wrap">
            <img v-if="photoModalImages.length > 0"
                 :src="photoModalImages[photoModalIndex]?.hash ? '/images/view/hash/' + photoModalImages[photoModalIndex]?.hash : '/thumbnails/' + photoModalImages[photoModalIndex]?.thumbnailPath"
                 :data-thumb="photoModalImages[photoModalIndex]?.thumbnailPath"
                 @error="onModalImgError"
                 @click="openFullPreview"
                 draggable="true" @dragstart="onSampleImgDragStart"
                 style="cursor:pointer;-webkit-user-drag:element" />
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
              <img :src="img.thumbnailPath ? '/thumbnails/' + img.thumbnailPath : ''" draggable="true" @dragstart="onSampleImgDragStart" style="-webkit-user-drag:element" />
            </div>
          </div>
        </div>
        <div class="spm-body-right" v-if="photoModalSample">
          <template v-if="!editing">
            <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">样品名称</span><span class="spm-field-value" :title="photoModalSample.sampleName || '-'">{{ photoModalSample.sampleName || '-' }}</span></div></div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">公司编号</span><span class="spm-field-value" :title="photoModalSample.sampleCode || '-'">{{ photoModalSample.sampleCode || '-' }}</span></div>
              <div class="spm-field"><span class="spm-field-label">出厂货号</span><span class="spm-field-value" :title="photoModalSample.factoryCode || '-'">{{ photoModalSample.factoryCode || '-' }}</span></div>
            </div>
            <div class="spm-field-row" :class="{ 'spm-hidden': hideFactoryPrice }">
              <div class="spm-field" v-if="!hideFactoryPrice"><span class="spm-field-label">出厂价</span><span class="spm-field-value spm-price" :title="photoModalSample.factoryPrice || '-'">{{ photoModalSample.factoryPrice || '-' }}</span></div>
              <div class="spm-field"><span class="spm-field-label">包装方式</span><span class="spm-field-value" :title="photoModalSample.packagingCn || '-'">{{ photoModalSample.packagingCn || '-' }}</span></div>
              <div class="spm-field"><span class="spm-field-label">内盒/装箱量</span><span class="spm-field-value" :title="(photoModalSample.innerBoxCount != null ? photoModalSample.innerBoxCount : '0') + ' / ' + (photoModalSample.cartonCapacity||'-')">{{ (photoModalSample.innerBoxCount != null ? photoModalSample.innerBoxCount : '0') + ' / ' + (photoModalSample.cartonCapacity||'-') }}</span></div>
            </div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">外箱规格</span><span class="spm-field-value" :title="fmt3(photoModalSample.cartonLength,photoModalSample.cartonWidth,photoModalSample.cartonHeight)+' CM'">{{ fmt3(photoModalSample.cartonLength,photoModalSample.cartonWidth,photoModalSample.cartonHeight) }} CM</span></div>
              <div class="spm-field"><span class="spm-field-label">外箱毛/净重</span><span class="spm-field-value" :title="(photoModalSample.cartonGrossWeight||'0')+' / '+(photoModalSample.cartonNetWeight||'0')+' KG'">{{ (photoModalSample.cartonGrossWeight||'0') + ' / ' + (photoModalSample.cartonNetWeight||'0') + ' KG' }}</span></div>
            </div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">包装规格</span><span class="spm-field-value" :title="fmt3(photoModalSample.packageLength,photoModalSample.packageWidth,photoModalSample.packageHeight)+' CM'">{{ fmt3(photoModalSample.packageLength,photoModalSample.packageWidth,photoModalSample.packageHeight) }} CM</span></div>
              <div class="spm-field"><span class="spm-field-label">英文包装</span><span class="spm-field-value" :title="photoModalSample.packagingEn || '-'">{{ photoModalSample.packagingEn || '-' }}</span></div>
            </div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">产品规格</span><span class="spm-field-value" :title="fmt3(photoModalSample.sampleLength,photoModalSample.sampleWidth,photoModalSample.sampleHeight)+' CM'">{{ fmt3(photoModalSample.sampleLength,photoModalSample.sampleWidth,photoModalSample.sampleHeight) }} CM</span></div>
              <div class="spm-field"><span class="spm-field-label">产品毛/净重</span><span class="spm-field-value" :title="(photoModalSample.sampleGrossWeight||'0')+' / '+(photoModalSample.sampleNetWeight||'0')+' KG'">{{ (photoModalSample.sampleGrossWeight||'0') + ' / ' + (photoModalSample.sampleNetWeight||'0') + ' KG' }}</span></div>
            </div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">体积/材积</span><span class="spm-field-value" :title="(photoModalSample.cartonVolume||'-')+' / '+(photoModalSample.cartonMaterialVolume||'-')">{{ (photoModalSample.cartonVolume||'-') + ' / ' + (photoModalSample.cartonMaterialVolume||'-') }}</span></div>
              <div class="spm-field"><span class="spm-field-label">电池信息</span><span class="spm-field-value" :title="photoModalSample.batteryInfo || '-'">{{ photoModalSample.batteryInfo || '-' }}</span></div>
            </div>
            <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">摊位号</span><span class="spm-field-value" :title="photoModalSample.boothNo || '-'">{{ photoModalSample.boothNo || '-' }}</span></div></div>
            <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">产品认证</span><span class="spm-field-value" :title="photoModalSample.certification || '-'">{{ photoModalSample.certification || '-' }}</span></div></div>
            <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">中文备注</span><span class="spm-field-value" :title="photoModalSample.remark || '-'">{{ photoModalSample.remark || '-' }}</span></div></div>
            <template v-if="!hideSupplierInfo">
              <div class="spm-section-title">厂商信息</div>
              <div class="spm-field-row">
                <div class="spm-field"><span class="spm-field-label">厂商编号</span><span class="spm-field-value" :title="photoModalSample.manufacturerCode || '-'">{{ photoModalSample.manufacturerCode || '-' }}</span></div>
                <div class="spm-field"><span class="spm-field-label">厂商名称</span><span class="spm-field-value" :title="photoModalSample.name || '-'">{{ photoModalSample.name || '-' }}</span></div>
              </div>
              <div class="spm-field-row">
                <div class="spm-field"><span class="spm-field-label">联系人</span><span class="spm-field-value" :title="photoModalSample.contact1 || '-'">{{ photoModalSample.contact1 || '-' }}</span></div>
                <div class="spm-field"><span class="spm-field-label">电话</span><span class="spm-field-value" :title="photoModalSample.phone1 || '-'">{{ photoModalSample.phone1 || '-' }}</span></div>
              </div>
              <div class="spm-field-row">
                <div class="spm-field"><span class="spm-field-label">手机</span><span class="spm-field-value" :title="photoModalSample.mobile1 || '-'">{{ photoModalSample.mobile1 || '-' }}</span></div>
                <div class="spm-field"><span class="spm-field-label">QQ</span><span class="spm-field-value" :title="photoModalSample.qq || '-'">{{ photoModalSample.qq || '-' }}</span></div>
              </div>
            </template>
          </template>

          <template v-else>
            <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">样品名称</span><input class="spm-input" v-model="editData.sampleName" /></div></div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">公司编号</span><input class="spm-input spm-input-ro" :value="photoModalSample.sampleCode || '-'" readonly /></div>
              <div class="spm-field"><span class="spm-field-label">出厂货号</span><input class="spm-input" v-model="editData.factoryCode" /></div>
            </div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">出厂价</span><input class="spm-input" v-model="editData.factoryPrice" /></div>
              <div class="spm-field"><span class="spm-field-label">包装方式</span><input class="spm-input" v-model="editData.packagingCn" /></div>
            </div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">内盒</span><input class="spm-input spm-input-sm" v-model="editData.innerBoxCount" /><span class="spm-field-label">装箱量</span><input class="spm-input spm-input-sm" v-model="editData.cartonCapacity" /></div>
            </div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">外箱规格</span><span class="spm-field-dim"><input class="spm-input spm-input-sm" v-model="editData.cartonLength" @input="onEditCartonInput" /> x <input class="spm-input spm-input-sm" v-model="editData.cartonWidth" @input="onEditCartonInput" /> x <input class="spm-input spm-input-sm" v-model="editData.cartonHeight" @input="onEditCartonInput" /> CM</span></div>
              <div class="spm-field"><span class="spm-field-label">毛/净</span><input class="spm-input spm-input-sm" v-model="editData.cartonGrossWeight" /><span class="spm-field-label">/</span><input class="spm-input spm-input-sm" v-model="editData.cartonNetWeight" /> KG</div>
            </div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">包装规格</span><span class="spm-field-dim"><input class="spm-input spm-input-sm" v-model="editData.packageLength" /> x <input class="spm-input spm-input-sm" v-model="editData.packageWidth" /> x <input class="spm-input spm-input-sm" v-model="editData.packageHeight" /> CM</span></div>
              <div class="spm-field"><span class="spm-field-label">英文包装</span><input class="spm-input" v-model="editData.packagingEn" /></div>
            </div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">产品规格</span><span class="spm-field-dim"><input class="spm-input spm-input-sm" v-model="editData.sampleLength" /> x <input class="spm-input spm-input-sm" v-model="editData.sampleWidth" /> x <input class="spm-input spm-input-sm" v-model="editData.sampleHeight" /> CM</span></div>
              <div class="spm-field"><span class="spm-field-label">毛/净</span><input class="spm-input spm-input-sm" v-model="editData.sampleGrossWeight" /><span class="spm-field-label">/</span><input class="spm-input spm-input-sm" v-model="editData.sampleNetWeight" /> KG</div>
            </div>
            <div class="spm-field-row">
              <div class="spm-field"><span class="spm-field-label">体积</span><input class="spm-input spm-input-sm" v-model="editData.cartonVolume" /><span class="spm-field-label">材积</span><input class="spm-input spm-input-sm" v-model="editData.cartonMaterialVolume" /></div>
              <div class="spm-field"><span class="spm-field-label">电池信息</span><input class="spm-input" v-model="editData.batteryInfo" /></div>
            </div>
            <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">摊位号</span><input class="spm-input spm-input-ro" :value="photoModalSample.boothNo || '-'" readonly /></div></div>
            <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">产品认证</span><input class="spm-input" v-model="editData.certification" /></div></div>
            <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">中文备注</span><input class="spm-input" v-model="editData.remark" /></div></div>
            <template v-if="!hideSupplierInfo">
              <div class="spm-section-title">厂商信息</div>
              <div class="spm-field-row">
                <div class="spm-field"><span class="spm-field-label">厂商编号</span><input class="spm-input spm-input-ro" :value="photoModalSample.manufacturerCode || '-'" readonly /></div>
                <div class="spm-field"><span class="spm-field-label">厂商名称</span><input class="spm-input spm-input-ro" :value="photoModalSample.name || '-'" readonly /></div>
              </div>
              <div class="spm-field-row">
                <div class="spm-field"><span class="spm-field-label">联系人</span><input class="spm-input" v-model="editData.contact1" /></div>
                <div class="spm-field"><span class="spm-field-label">电话</span><input class="spm-input" v-model="editData.phone1" /></div>
              </div>
              <div class="spm-field-row">
                <div class="spm-field"><span class="spm-field-label">手机</span><input class="spm-input" v-model="editData.mobile1" /></div>
                <div class="spm-field"><span class="spm-field-label">QQ</span><input class="spm-input" v-model="editData.qq" /></div>
              </div>
            </template>
          </template>
        </div>
        </div>
      </div>
      <div class="spm-footer">
        <div class="spm-toggle-group" v-if="!editing">
          <label class="spm-toggle"><input type="checkbox" v-model="hideFactoryPrice" /> 隐藏出厂价</label>
          <label class="spm-toggle"><input type="checkbox" v-model="hideSupplierInfo" /> 隐藏厂商信息</label>
        </div>
        <div class="spm-toggle-group" style="gap:8px; margin-left: auto">
          <button v-if="!editing" class="spm-btn-edit" @click="startModalEdit">编辑</button>
          <template v-else>
            <button class="spm-btn-save" @click="saveModalEdit">保存</button>
            <button class="spm-btn-close" @click="cancelModalEdit">取消</button>
          </template>
          <button v-if="!editing" class="spm-btn-close" @click="closePhotoModal">关闭</button>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showImportModal" class="batch-image-modal-overlay import-modal-overlay">
      <div class="batch-image-modal">
        <div class="batch-image-modal-header">
          <strong>导入样品数据</strong>
          <button class="modal-close-btn" @click="showImportModal = false">
            <X :size="22" />
          </button>
        </div>
        <div class="batch-image-modal-body">
          <div v-if="!importParsing" class="upload-area" @click="$refs.importFileInput.click()" @dragover.prevent="onDragOver" @dragleave="onDragLeave" @drop.prevent="onImportDrop">
            <div class="upload-icon"><FileSpreadsheet :size="48" /></div>
            <div class="upload-text">点击或拖拽上传 .xlsx 文件</div>
            <div class="upload-hint">支持 Excel 2007+ 格式，选择后将预览数据</div>
            <input ref="importFileInput" type="file" accept=".xlsx,.xls" hidden @change="onImportFileChange" />
          </div>
          <div v-else class="import-parse-progress">
            <div class="import-parse-icon">
              <Loader2 :size="32" class="spin" />
            </div>
            <div class="import-parse-stage">{{ importParsingStage }}</div>
            <div class="import-parse-bar-track">
              <div class="import-parse-bar-fill" :style="{ width: importParsingProgress + '%' }"></div>
            </div>
            <div class="import-parse-pct">{{ importParsingProgress }}%</div>
          </div>
          <div v-if="importFile && !importParsing" class="file-list show">
            <div class="file-list-header">
              <span>已选择文件</span>
              <span class="file-count">1 个文件</span>
            </div>
            <div class="file-list-items">
              <div class="file-item">
                <span class="file-item-icon"><FileSpreadsheet :size="22" /></span>
                <span class="file-item-name">{{ importFile.name }}</span>
                <span class="file-item-size">{{ formatFileSize(importFile.size) }}</span>
                <span class="file-item-remove" @click="importFile = null">
                  <X :size="20" />
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
    <div v-if="showImportPreview" class="batch-image-modal-overlay import-preview-overlay">
      <div class="batch-image-modal import-preview-modal">
        <div class="batch-image-modal-header">
          <strong>导入预览 - {{ importFile?.name || 'Excel数据' }}</strong>
          <button class="modal-close-btn" @click="cancelImportPreview">
            <X :size="16" />
          </button>
        </div>
        <div class="batch-image-modal-body import-preview-body">
          <div class="import-preview-summary">
            <span class="import-stat">共 <strong>{{ importPreviewAllRows.length }}</strong> 条数据</span>
            <span class="import-stat">筛选后 <strong>{{ importPreviewFilteredCount }}</strong> 条</span>
            <span class="import-stat">已选 <strong>{{ importSelectedRows.length }}</strong> 条</span>
            <button class="sample-btn sample-btn-ghost" style="font-size:18px;padding:4px 14px;height:40px" :disabled="importSelectedRows.length === 0" @click="deleteSelectedPreviewRows">
              <Trash2 :size="18" /> 批量删除
            </button>
          </div>

          <div ref="importPreviewWrapRef" class="import-preview-table-wrap">
            <vxe-grid
              ref="importPreviewGridRef"
              :columns="IMPORT_PREVIEW_ALL_COLUMNS"
              :data="importPreviewDisplayData"
              :height="700"
              :auto-resize="false"
              :toolbar-config="{ custom: true, refresh: false, zoom: true, slots: { buttons: 'importPreviewToolbarBtns' } }"
              :column-config="{ resizable: true }"
              :row-config="{ isHover: true, keyField: '_rowIndex' }"
              :checkbox-config="{ highlight: true, range: true }"
              :edit-config="IMPORT_PREVIEW_EDIT_CONFIG"
              :keep-source="true"
              :virtual-y-config="{ enabled: true, gt: 20, oSize: 60 }"
              :virtual-x-config="{ enabled: true, gt: 30 }"
              :optimization="{ animat: false, delayHover: 250, scrollY: { gt: 0, oSize: 60, rSize: 200, rHeight: 44 } }"
              :border="true"
              :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontWeight: 600, textAlign: 'center' }"
              :cell-style="importArea.cellAreaStyle"
              :row-class-name="importRowClassName"
              @edit-closed="onImportCellEdit"
              @checkbox-change="onImportPreviewCheckChange"
              @checkbox-all="onImportPreviewCheckChange"
            >
              <template #importPreviewToolbarBtns>
                <div class="import-batch-edit-inline">
                  <div class="import-batch-dropdown">
                    <button
                      class="import-batch-select-trigger"
                      @click.stop="batchEditDropdownOpen = !batchEditDropdownOpen"
                    >
                      <span>{{ batchEditFields.find(f=>f.value===batchEditField)?.label || '中文包装' }}</span>
                      <span class="import-arrow">&#9662;</span>
                    </button>
                    <div class="import-batch-panel" :class="{ open: batchEditDropdownOpen }">
                      <div
                        v-for="f in batchEditFields"
                        :key="f.value"
                        class="import-batch-panel-item"
                        :class="{ active: batchEditField === f.value }"
                        @click.stop="batchEditField = f.value; batchEditDropdownOpen = false"
                      >{{ f.label }}</div>
                    </div>
                    <div v-if="batchEditDropdownOpen" class="import-batch-overlay" @click="batchEditDropdownOpen = false"></div>
                  </div>
                  <input
                    v-model="batchEditValue"
                    class="import-batch-input"
                    :placeholder="'输入' + (batchEditFields.find(f=>f.value===batchEditField) || {}).label + '，批量修改勾选行'"
                    @keyup.enter="batchEditRun"
                  />
                  <button
                    class="sample-btn sample-btn-ghost"
                    style="font-size:18px;padding:4px 14px;height:40px"
                    :disabled="importSelectedRows.length === 0 || !batchEditValue.trim()"
                    @click="batchEditRun"
                  >
                    批量修改
                  </button>
                  <button
                    class="sample-btn sample-btn-ghost"
                    style="font-size:18px;padding:4px 14px;height:40px;margin-left:4px"
                    :disabled="importSelectedRows.length === 0"
                    @click="batchTranslateSelected"
                  >
                    翻译勾选
                  </button>
                  <button
                    class="sample-btn sample-btn-ghost"
                    style="font-size:18px;padding:4px 14px;height:40px;margin-left:4px"
                    @click="batchAbandonUpdate"
                  >
                    批量放弃更新
                  </button>
                  <span style="margin-left:12px;width:1px;height:28px;background:rgba(0,0,0,0.1)"></span>
                  <button class="sample-btn sample-btn-ghost import-filter-btn import-filter-cat" :class="{ active: importPreviewCatFilter }" style="font-size:16px;padding:4px 14px;height:40px" @click="onTogglePreviewFilter('cat')">
                    <AlertTriangle :size="16" /> 种类不符
                  </button>
                  <button class="sample-btn sample-btn-ghost import-filter-btn import-filter-pkg" :class="{ active: importPreviewPkgFilter }" style="font-size:16px;padding:4px 14px;height:40px" @click="onTogglePreviewFilter('pkg')">
                    <AlertTriangle :size="16" /> 包装不符
                  </button>
                  <button class="sample-btn sample-btn-ghost import-filter-btn import-filter-dup" :class="{ active: importPreviewDupFilter }" style="font-size:16px;padding:4px 14px;height:40px" @click="onTogglePreviewFilter('dup')">
                    <ShieldAlert :size="16" /> 货号重复
                  </button>
                </div>
              </template>
              <template #import_warnings="{ row }">
                <div v-if="row._warnings && row._warnings.length > 0" style="display:flex;flex-direction:column;gap:2px;align-items:center">
                  <span v-for="(w, wi) in row._warnings" :key="wi" style="color:#e67e22;font-size:16px;">{{ w }}</span>
                </div>
                <span v-else style="color:#27ae60;font-size:16px;">正常</span>
              </template>
              <template #import_action="{ row }">
                <div style="display:flex;gap:4px;justify-content:center;flex-wrap:nowrap">
                  <button v-if="row._status === 'dup_warning'" class="sample-table-action" style="color:#27ae60;font-size:16px;padding:4px 12px;height:36px;white-space:nowrap" @click.stop="openDupDetail(row)">查看</button>
                  <button class="sample-table-action" style="color:#007aff;font-size:16px;padding:4px 12px;height:36px;white-space:nowrap" @click.stop="restorePreviewRow(row)">还原</button>
                  <button class="sample-table-action" style="color:#ff3b30;font-size:16px;padding:4px 12px;height:36px;white-space:nowrap" @click.stop="deletePreviewRow(row)">删除</button>
                </div>
              </template>
            </vxe-grid>
          </div>

          <!-- 分页控件 -->
          <div class="import-preview-pager" v-if="importPreviewFilteredCount > importPreviewPageSize">
            <button class="sample-btn sample-btn-ghost" style="font-size:18px;padding:4px 14px;height:40px" :disabled="importPreviewPage <= 1" @click="onPreviewPageChange(1)">首页</button>
            <button class="sample-btn sample-btn-ghost" style="font-size:18px;padding:4px 14px;height:40px" :disabled="importPreviewPage <= 1" @click="onPreviewPageChange(importPreviewPage - 1)">上一页</button>
            <span style="font-size:20px;color:#64748b;margin:0 8px">第 {{ importPreviewPage }} / {{ importPreviewTotalPages }} 页</span>
            <button class="sample-btn sample-btn-ghost" style="font-size:18px;padding:4px 14px;height:40px" :disabled="importPreviewPage >= importPreviewTotalPages" @click="onPreviewPageChange(importPreviewPage + 1)">下一页</button>
            <button class="sample-btn sample-btn-ghost" style="font-size:18px;padding:4px 14px;height:40px" :disabled="importPreviewPage >= importPreviewTotalPages" @click="onPreviewPageChange(importPreviewTotalPages)">末页</button>
            <select class="import-preview-size-select" :value="importPreviewPageSize" @change="onPreviewPageSizeChange(Number($event.target.value))">
              <option :value="50">50条/页</option>
              <option :value="100">100条/页</option>
              <option :value="200">200条/页</option>
              <option :value="500">500条/页</option>
            </select>
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
            <button class="sample-btn sample-btn-danger" :disabled="importPreviewFilteredCount === 0 || importUploading" @click="doConfirmImport('all')">
              <Upload :size="14" /> {{ importUploading ? '导入中...' : `全选导入(${importPreviewFilteredCount})` }}
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
    <div v-if="showDupDetailModal" class="batch-image-modal-overlay" @click.self="cancelDupDetail">
      <div class="batch-image-modal import-preview-modal">
        <div class="batch-image-modal-header">
          <strong>重复资料详情 — 选择要覆盖的记录</strong>
          <button class="modal-close-btn" @click="cancelDupDetail">
            <X :size="16" />
          </button>
        </div>
        <div class="batch-image-modal-body import-preview-body">
          <vxe-grid
            ref="dupDetailGridRef"
            :columns="DUP_DETAIL_COLUMNS"
            :data="dupDetailRows"
            height="360"
            :auto-resize="true"
            :row-config="{ isHover: true }"
            :checkbox-config="{ highlight: true, trigger: 'row' }"
            :toolbar-config="{ custom: true }"
            :column-config="{ resizable: true, drag: true }"
            :border="true"
            :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontWeight: 600, textAlign: 'center' }"
            :cell-style="{ textAlign: 'center' }"
            :custom-column-config="{ checkMethod: ({ column }) => column.type !== 'checkbox' }"
            @checkbox-change="onDupDetailCheckChange"
          >
            <template #dup_detail_image="{ row }">
              <div style="display:flex;align-items:center;justify-content:center;height:100%">
                <img
                  v-if="row.thumbnail"
                  :src="'/thumbnails/' + row.thumbnail"
                  loading="lazy"
                  style="width:48px;height:36px;object-fit:cover;border-radius:4px;cursor:pointer"
                  @click.stop="openDupImage(row.firstImageHash, row.thumbnail)"
                />
                <span v-else style="color:rgba(29,29,31,0.25);font-size:11px">无图</span>
              </div>
            </template>
          </vxe-grid>
        </div>
        <div class="modal-footer import-preview-footer" style="justify-content:flex-end!important">
          <div class="import-toolbar-right">
            <button class="sample-btn sample-btn-ghost" @click="cancelDupDetail">放弃更新</button>
            <button class="sample-btn sample-btn-primary" :disabled="!dupDetailSelectedId" @click="confirmDupOverwrite">
              <Check :size="14" /> 确认更新
            </button>
          </div>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showDupImageModal" class="batch-image-modal-overlay" @click.self="closeDupImage">
      <div class="batch-image-modal" style="max-width:90vw;max-height:90vh;background:transparent;box-shadow:none;padding:0">
        <button class="modal-close-btn" style="position:fixed;top:16px;right:16px;background:rgba(0,0,0,0.5);color:#fff;border-radius:50%;width:36px;height:36px" @click="closeDupImage">
          <X :size="20" />
        </button>
        <img :src="dupImageUrl" style="max-width:90vw;max-height:90vh;object-fit:contain;border-radius:8px" @click.stop />
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showBatchImageModal" class="batch-image-modal-overlay import-modal-overlay">
      <div class="batch-image-modal" style="width:1400px">
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
          <div v-if="batchImageType === 'factory-code' && (manufacturerCode || (currentSample && currentSample.manufacturerCode))" class="batch-manufacturer-info">
            <span class="bm-info-label">关联厂商</span>
            <span class="bm-info-value">编号: <strong>{{ manufacturerCode || (currentSample && currentSample.manufacturerCode) }}</strong></span>
            <span v-if="currentSample && currentSample.name" class="bm-info-value">名称: <strong>{{ currentSample.name }}</strong></span>
            <span v-if="currentSample && currentSample.boothNo" class="bm-info-value">摊位号: <strong>{{ currentSample.boothNo }}</strong></span>
            <div v-if="!currentSample || !currentSample.name" class="bm-info-hint">请先在表格中选中一行样品以显示完整厂商信息</div>
          </div>
          <div v-if="batchMatched.length === 0"
            class="upload-area"
            @click="$refs.batchFileInput.click()"
            @dragover.prevent="onDragOver"
            @dragleave="onDragLeave"
            @drop.prevent="onBatchDrop"
          >
            <div class="upload-icon"><ImagePlus :size="64" /></div>
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
                      <ImageIcon :size="36" /> 暂无图片
                    </div>
                  </div>
                </div>
                <div class="bmc-meta">
                  <span>公司编号: <strong>{{ batchMatched[batchCurrentIndex].sampleCode || '-' }}</strong></span>
                  <span>样品名称: <strong>{{ batchMatched[batchCurrentIndex].sampleName || '-' }}</strong></span>
                  <span>出厂货号: <strong>{{ batchMatched[batchCurrentIndex].factoryCode || '-' }}</strong></span>
                  <span>厂商编号: <strong>{{ batchMatched[batchCurrentIndex].manufacturerCode || '-' }}</strong></span>
                  <span>厂商名称: <strong>{{ batchMatched[batchCurrentIndex].name || '-' }}</strong></span>
                  <span>摊位号: <strong>{{ batchMatched[batchCurrentIndex].boothNo || '-' }}</strong></span>
                </div>
                <div class="bmc-actions">
                  <button class="sample-btn sample-btn-ghost bmc-btn" :class="{ active: batchMatched[batchCurrentIndex].action === 'skip' }" @click="batchMatched[batchCurrentIndex].action = 'skip'">跳过</button>
                  <button class="sample-btn sample-btn-ghost bmc-btn" :class="{ active: batchMatched[batchCurrentIndex].action === 'cover' }" @click="batchMatched[batchCurrentIndex].action = 'cover'">覆盖</button>
                  <button class="sample-btn sample-btn-ghost bmc-btn" :class="{ active: batchMatched[batchCurrentIndex].action === 'append' }" @click="batchMatched[batchCurrentIndex].action = 'append'">追加</button>
                  <span class="bmc-remove" @click="removeBatchFile(batchCurrentIndex)"><X :size="18" /></span>
                </div>
              </template>
              <template v-else>
                <div class="bmc-unmatched">
                  <ImageIcon :size="28" />
                  <span>{{ batchMatched[batchCurrentIndex].file.name }} — 未匹配到样品</span>
                  <span class="bmc-remove" @click="removeBatchFile(batchCurrentIndex)"><X :size="18" /></span>
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
                <button class="sample-btn sample-btn-ghost" @click="setBatchActionAll('append')">全部追加</button>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="sample-btn sample-btn-ghost" @click="closeBatchModal">取消</button>
          <button class="sample-btn sample-btn-primary" :disabled="batchMatched.filter(m => m.matched && m.action !== 'skip').length === 0 || batchUploading" @click="doBatchImageUpload">
            <Upload :size="18" /> {{ batchUploading ? `上传中 ${batchUploadProgress.done}/${batchUploadProgress.total} (成功${batchUploadProgress.success} 失败${batchUploadProgress.fail})` : `开始上传 (${batchMatched.filter(m => m.matched && m.action !== 'skip').length})` }}
          </button>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showAdvancedSearch" class="adv-search-overlay">
      <div class="adv-search-panel">
        <div class="adv-search-body">
          <!-- Row 1 -->
          <div class="adv-field"><label>厂商编号</label><input v-model="advForm.manufacturerCode" placeholder="请输入厂商编号" /></div>
          <div class="adv-field"><label>厂商名称</label><input v-model="advForm.name" placeholder="请输入厂商名称" /></div>
          <div class="adv-field"><label>联系人</label><input v-model="advForm.contact1" placeholder="请输入联系人" /></div>
          <!-- Row 2 -->
          <div class="adv-field"><label>电话号码</label><input v-model="advForm.phone1" placeholder="请输入电话号码" /></div>
          <div class="adv-field"><label>手机号码</label><input v-model="advForm.mobile1" placeholder="请输入手机号码" /></div>
          <div class="adv-field"><label>样品名称</label><input v-model="advForm.sampleName" placeholder="请输入样品名称" /></div>
          <!-- Row 3 -->
          <div class="adv-field"><label>公司编号</label><input v-model="advForm.sampleCode" placeholder="请输入公司编号" /></div>
          <div class="adv-field"><label>出厂货号</label><input v-model="advForm.factoryCode" placeholder="请输入出厂货号" /></div>
          <div class="adv-field"><label>摊位编号</label><input v-model="advForm.boothNo" placeholder="请输入摊位编号" /></div>
          <!-- Row 4 -->
          <div class="adv-field adv-field-range">
            <label>出厂价</label>
            <div class="range-inputs">
              <input v-model.number="advForm.factoryPriceMin" placeholder="最低价" />
              <span>-</span>
              <input v-model.number="advForm.factoryPriceMax" placeholder="最高价" />
            </div>
          </div>
          <div class="adv-field"><label>种类名称</label><input v-model="advForm.category" placeholder="请输入种类名称" /></div>
          <div class="adv-field"><label>种类编号</label><input v-model="advForm.categoryCode" placeholder="请输入种类编号" /></div>
          <!-- Row 5 -->
          <div class="adv-field adv-field-range">
            <label>外箱数量</label>
            <div class="range-inputs">
              <input v-model.number="advForm.cartonCapacityMin" placeholder="最小数量" />
              <span>-</span>
              <input v-model.number="advForm.cartonCapacityMax" placeholder="最大数量" />
            </div>
          </div>
          <div class="adv-field"><label>包装编号</label><input v-model="advForm.packageCode" placeholder="请输入包装编号" /></div>
          <div class="adv-field"><label>中文包装</label><input v-model="advForm.packagingCn" placeholder="请输入中文包装" /></div>
          <!-- Row 6 -->
          <div class="adv-field"><label>产品认证</label><input v-model="advForm.certification" placeholder="请输入产品认证" /></div>
          <div class="adv-field"><label>侵权</label><select v-model="advForm.infringement"><option value="">请选择侵权状态</option><option value="侵权">侵权</option><option value="不侵权">不侵权</option><option value="其他">其他</option></select></div>
          <div class="adv-field adv-field-checks">
            <label>筛选条件</label>
            <div class="check-group">
              <label class="chk-item"><input type="checkbox" v-model="advForm.hasImage" /> 有图片</label>
              <label class="chk-item"><input type="checkbox" v-model="advForm.hasVideo" /> 有视频</label>
            </div>
          </div>
          <!-- Row 7 - 尺寸范围 -->
          <div class="adv-field adv-field-range-unit">
            <label>样品长度</label>
            <div class="range-inputs"><input v-model.number="advForm.sampleLengthMin" placeholder="最小长度" /><span>-</span><input v-model.number="advForm.sampleLengthMax" placeholder="最大长度" /><span class="unit">CM</span></div>
          </div>
          <div class="adv-field adv-field-range-unit">
            <label>样品宽度</label>
            <div class="range-inputs"><input v-model.number="advForm.sampleWidthMin" placeholder="最小宽度" /><span>-</span><input v-model.number="advForm.sampleWidthMax" placeholder="最大宽度" /><span class="unit">CM</span></div>
          </div>
          <div class="adv-field adv-field-range-unit">
            <label>样品高度</label>
            <div class="range-inputs"><input v-model.number="advForm.sampleHeightMin" placeholder="最小高度" /><span>-</span><input v-model.number="advForm.sampleHeightMax" placeholder="最大高度" /><span class="unit">CM</span></div>
          </div>
          <!-- Row 8 -->
          <div class="adv-field adv-field-range-unit">
            <label>包装长度</label>
            <div class="range-inputs"><input v-model.number="advForm.packageLengthMin" placeholder="最小长度" /><span>-</span><input v-model.number="advForm.packageLengthMax" placeholder="最大长度" /><span class="unit">CM</span></div>
          </div>
          <div class="adv-field adv-field-range-unit">
            <label>包装宽度</label>
            <div class="range-inputs"><input v-model.number="advForm.packageWidthMin" placeholder="最小宽度" /><span>-</span><input v-model.number="advForm.packageWidthMax" placeholder="最大宽度" /><span class="unit">CM</span></div>
          </div>
          <div class="adv-field adv-field-range-unit">
            <label>包装高度</label>
            <div class="range-inputs"><input v-model.number="advForm.packageHeightMin" placeholder="最小高度" /><span>-</span><input v-model.number="advForm.packageHeightMax" placeholder="最大高度" /><span class="unit">CM</span></div>
          </div>
          <!-- Row 9 -->
          <div class="adv-field adv-field-range-unit">
            <label>外箱长度</label>
            <div class="range-inputs"><input v-model.number="advForm.cartonLengthMin" placeholder="最小长度" /><span>-</span><input v-model.number="advForm.cartonLengthMax" placeholder="最大长度" /><span class="unit">CM</span></div>
          </div>
          <div class="adv-field adv-field-range-unit">
            <label>外箱宽度</label>
            <div class="range-inputs"><input v-model.number="advForm.cartonWidthMin" placeholder="最小宽度" /><span>-</span><input v-model.number="advForm.cartonWidthMax" placeholder="最大宽度" /><span class="unit">CM</span></div>
          </div>
          <div class="adv-field adv-field-range-unit">
            <label>外箱高度</label>
            <div class="range-inputs"><input v-model.number="advForm.cartonHeightMin" placeholder="最小高度" /><span>-</span><input v-model.number="advForm.cartonHeightMax" placeholder="最大高度" /><span class="unit">CM</span></div>
          </div>
          <!-- Row 10 -->
          <div class="adv-field adv-field-range">
            <label>在架数量</label>
            <div class="range-inputs">
              <input v-model.number="advForm.innerBoxCountMin" placeholder="最小数量" />
              <span>-</span>
              <input v-model.number="advForm.innerBoxCountMax" placeholder="最大数量" />
            </div>
          </div>
          <div class="adv-field"><label>厂商认证</label><input v-model="advForm.batteryInfo" placeholder="" /></div>
          <div class="adv-field"><label>关键词</label><input v-model="advForm.keyword" placeholder="" /></div>
          <!-- Row 11 -->
          <div class="adv-field adv-field-range">
            <label>登记日期</label>
            <div class="range-inputs">
              <SimpleDatePicker v-model="advForm.createTimeMin" placeholder="开始日期" />
              <span>至</span>
              <SimpleDatePicker v-model="advForm.createTimeMax" placeholder="结束日期" />
            </div>
          </div>
          <div class="adv-field adv-field-range">
            <label>修改日期</label>
            <div class="range-inputs">
              <SimpleDatePicker v-model="advForm.updateTimeMin" placeholder="开始日期" />
              <span>至</span>
              <SimpleDatePicker v-model="advForm.updateTimeMax" placeholder="结束日期" />
            </div>
          </div>
          <!-- Row 12 -->
          <div class="adv-field"><label>登记人</label><input v-model="advForm.registrant" placeholder="请输入登记人" /></div>
          <div class="adv-field"><label>修改人</label><input v-model="advForm.modifier" placeholder="请输入修改人" /></div>
        </div>
        <div class="adv-search-footer">
          <button class="sample-btn sample-btn-ghost" @click="clearAdvForm">清空条件</button>
          <div style="flex:1"></div>
          <button class="sample-btn sample-btn-ghost" @click="showAdvancedSearch = false">取消</button>
          <button class="sample-btn sample-btn-primary" @click="doAdvancedSearch">确认</button>
        </div>
      </div>
    </div>
    </Teleport>

    <Teleport to="body">
    <div v-if="showPrintDropdown" class="sample-more-dropdown-panel" :style="printDropdownStyle">
      <div class="sample-more-item" @click="doPrintMultiCopies">
        <Printer :size="18" /> 多款打印
      </div>
      <div class="sample-more-item" @click="doPrintQuarterTable">
        <Printer :size="18" /> 小条码打印
      </div>
      <div class="sample-more-item" @click="openScanPrintModal">
        <Crosshair :size="18" /> 扫码打印
      </div>
    </div>

    <div v-if="showMultiPrintModal" class="batch-image-modal-overlay import-modal-overlay" style="background:transparent">
      <div class="batch-image-modal multi-print-modal" style="width:1500px;max-height:90vh;display:flex;flex-direction:column">
        <div class="batch-image-modal-header">
          <span style="font-size:22px;font-weight:700">多款打印设置</span>
          <button class="modal-close-btn" @click="showMultiPrintModal = false"><X :size="22" /></button>
        </div>
        <div class="batch-image-modal-body" style="flex:1;overflow:hidden;display:flex;flex-direction:column;padding:20px 24px">
          <div style="display:flex;align-items:center;gap:20px;margin-bottom:14px;flex-shrink:0">
            <div style="display:flex;align-items:center;gap:10px">
              <label style="font-size:22px;font-weight:600;color:#1d1d1f;white-space:nowrap">批量设置张数</label>
              <input type="number" v-model.number="multiPrintBatchCopies" min="1" max="99"
                style="width:80px;height:44px;border-radius:10px;border:1px solid rgba(0,122,255,0.15);outline:none;font-size:22px;font-weight:600;text-align:center;color:#007aff;background:rgba(0,122,255,0.04);padding:0 8px"
                placeholder="1" />
              <button class="sample-btn sample-btn-blue" style="height:44px;font-size:20px;padding:0 18px" @click="batchSetCopies">应用</button>
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
              :max-height="700"
              :row-config="{ isHover: true, keyField: 'sampleCode' }"
              :cell-config="{ height: 72 }"
              :header-cell-style="{ background: '#f0f7ff', borderColor: 'rgba(0,122,255,0.12)', color: 'rgba(29,29,31,0.5)', fontWeight: 700, fontSize: '20px', textAlign: 'center' }"
              :cell-style="{ textAlign: 'center', fontSize: '18px' }"
              :border="true"
              :toolbar-config="{ custom: true, refresh: true, zoom: true }"
              :optimization="{ animat: false }"
            >
              <template #copies_edit="{ row }">
                <input type="number" v-model.number="row.copies" min="1" max="99"
                  style="width:72px;height:40px;border-radius:8px;border:1px solid rgba(0,122,255,0.2);text-align:center;font-size:22px;font-weight:600;color:#007aff;outline:none;padding:0 6px"
                  @click.stop />
              </template>
            </vxe-grid>
          </div>
          <div style="flex-shrink:0;text-align:right;font-size:22px;font-weight:700;color:#007aff">
            共 {{ totalPrintPages }} 张标签
          </div>
        </div>
        <div class="modal-footer" style="border-top:1px solid rgba(0,122,255,0.08);padding:14px 24px">
          <button class="sample-btn sample-btn-ghost" style="font-size:18px;height:44px;padding:0 24px" @click="showMultiPrintModal = false">取消</button>
          <button class="sample-btn sample-btn-primary" style="font-size:18px;height:44px;padding:0 24px" @click="confirmMultiPrint">确定打印</button>
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
      <div class="batch-result-modal" style="max-width:640px">
        <div class="batch-result-header">
          <strong>提示</strong>
          <button class="modal-close-btn" @click="onAlertClose"><X :size="24" /></button>
        </div>
        <div class="batch-result-body" style="text-align:center;padding:36px 32px">
          <CheckCircle :size="48" style="color:#34c759;margin-bottom:16px" v-if="alertType === 'success'" />
          <AlertCircle :size="48" style="color:#ff3b30;margin-bottom:16px" v-else-if="alertType === 'error'" />
          <AlertTriangle :size="48" style="color:#ff9500;margin-bottom:16px" v-else />
          <p style="font-size:22px;color:#1d1d1f;line-height:1.7;white-space:pre-wrap">{{ alertMessage }}</p>
        </div>
        <div class="modal-footer" style="justify-content:center">
          <button class="sample-btn sample-btn-primary" style="font-size:20px;padding:14px 40px" @click="onAlertClose">知道了</button>
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

    <!-- 导出字段选择模态框 -->
    <Teleport to="body">
    <div v-if="showExportModal" class="batch-image-modal-overlay" @click.self="showExportModal = false">
      <div class="export-field-dialog" @click="showTplMenu = false">
        <div class="export-field-header">
          <strong>导出字段选择</strong>
          <button class="modal-close-btn" @click="showExportModal = false"><X :size="16" /></button>
        </div>
        <div class="export-field-toolbar">
          <button class="sample-btn sample-btn-ghost" style="font-size:13px;padding:5px 12px" @click="selectAllExportFields">全选</button>
          <button class="sample-btn sample-btn-ghost" style="font-size:13px;padding:5px 12px" @click="deselectAllExportFields">全不选</button>
          <span class="export-toolbar-sep"></span>
          <div class="export-template-dropdown" @click.stop>
            <button class="sample-btn sample-btn-ghost" style="font-size:13px;padding:5px 10px" @click="showTplMenu = !showTplMenu">
              <Database :size="13" style="margin-right:4px" />模板 <ChevronDown :size="12" style="margin-left:2px" />
            </button>
            <div class="export-tpl-menu" v-if="showTplMenu">
              <div v-if="exportTemplates.length === 0" class="export-tpl-menu-empty">暂无保存的模板</div>
              <div v-for="t in exportTemplates" :key="t.name" class="export-tpl-menu-item" @click="loadExportTemplate(t); showTplMenu = false">{{ t.name }}</div>
            </div>
          </div>
          <button class="sample-btn sample-btn-ghost" style="font-size:13px;padding:5px 10px" @click="showTplSaveInput = true">
            <Save :size="12" style="margin-right:3px" />保存当前选择
          </button>
          <button v-if="currentTemplate" class="sample-btn sample-btn-ghost" style="font-size:13px;padding:5px 10px;color:#ff3b30" @click="deleteExportTemplate">
            <Trash2 :size="12" style="margin-right:3px" />删除
          </button>
          <div v-if="showTplSaveInput" class="export-tpl-save-row">
            <input v-model="templateName" class="export-template-input" placeholder="输入模板名称" @keyup.enter="saveExportTemplate" ref="tplSaveRef" />
            <button class="sample-btn sample-btn-ghost" style="font-size:12px;padding:3px 8px" @click="showTplSaveInput = false">取消</button>
            <button class="sample-btn sample-btn-primary" style="font-size:12px;padding:3px 10px" @click="saveExportTemplate" :disabled="!templateName.trim()">保存</button>
          </div>
          <span class="export-field-count">已选 {{ checkedExportFieldCount }} / {{ exportFields.length }}</span>
        </div>
        <div class="export-field-list">
          <div v-for="(f, i) in exportFields" :key="f.key"
               class="export-field-item"
               :class="{ 'export-field-dragging': dragIndex === i }"
               :draggable="true"
               @dragstart="onExportDragStart($event, i)"
               @dragover.prevent="onExportDragOver($event, i)"
               @drop="onExportDrop(i)"
               @dragend="onExportDragEnd"
          >
            <span class="export-field-grip"><GripVertical :size="14" /></span>
            <label class="export-field-label" @click.stop>
              <input type="checkbox" v-model="f.checked" />
              <span>{{ f.label }}</span>
            </label>
          </div>
        </div>
        <div class="export-field-footer">
          <button class="sample-btn sample-btn-ghost" @click="showExportModal = false">取消</button>
          <button class="sample-btn sample-btn-primary" @click="doExport" :disabled="checkedExportFieldCount === 0 || selectedIds.length === 0">确认导出</button>
          <span v-if="selectedIds.length === 0" style="font-size:12px;color:#ff3b30;margin-left:8px">请先在表格中勾选要导出的数据</span>
        </div>
      </div>
    </div>
    </Teleport>

    <!-- 选择报表模板弹窗 -->
    <Teleport to="body">
    <div v-if="showTemplateSelect" class="batch-image-modal-overlay" @click.self="showTemplateSelect = false">
      <div class="batch-image-modal" style="width:680px">
        <div class="batch-image-modal-header">
          <strong>选择报价模板</strong>
          <button class="modal-close-btn" @click="showTemplateSelect = false"><X :size="24" /></button>
        </div>
        <div class="batch-image-modal-body" style="padding:24px 28px;max-height:500px;overflow-y:auto">
          <input class="sr-input" v-model="templateSearchKeyword" placeholder="搜索模板..."
            style="width:100%;box-sizing:border-box;height:60px;font-size:24px;border-radius:12px;margin-bottom:24px;padding:0 18px;" />
          <div v-if="filteredTemplates.length === 0" style="text-align:center;color:#999;padding:28px 0;font-size:18px">{{ templateSearchKeyword ? '无匹配模板' : '暂无模板' }}</div>
          <div
            v-for="tpl in filteredTemplates"
            :key="tpl.id"
            class="tpl-select-item"
            :class="{ selected: selectedTemplateId === tpl.id }"
            @click="selectedTemplateId = tpl.id"
          >
            <div class="tpl-select-title">{{ tpl.title }}</div>
            <div class="tpl-select-date">{{ tpl.createTime || '' }}</div>
          </div>
        </div>
        <div style="padding:0 28px 20px;display:flex;gap:16px;justify-content:flex-end">
          <button class="sample-btn sample-btn-ghost" style="font-size:20px;padding:14px 32px" @click="showTemplateSelect = false">取消</button>
          <button class="sample-btn sample-btn-primary" style="font-size:20px;padding:14px 32px" :disabled="!selectedTemplateId || vcSessionLoading" @click="confirmTemplateAndOpen">
            {{ vcSessionLoading ? '加载中...' : '确认并预览' }}
          </button>
        </div>
      </div>
    </div>
    </Teleport>

    <!-- 厂商确认表（带图）模态框 -->
    <Teleport to="body">
    <div v-if="showVendorConfirmModal" class="batch-image-modal-overlay" @click.self="showVendorConfirmModal = false">
      <div class="vendor-confirm-modal" style="width:880px;max-height:85vh;display:flex;flex-direction:column">
        <div class="batch-image-modal-header" style="flex-shrink:0">
          <strong><FileSpreadsheet :size="16" style="margin-right:6px;vertical-align:text-bottom" /> 厂商确认表（带图）</strong>
          <button class="modal-close-btn" @click="showVendorConfirmModal = false"><X :size="16" /></button>
        </div>

        <div class="batch-image-modal-body vendor-confirm-body" style="flex:1;overflow-y:auto;padding:20px 24px">

          <!-- 抬头配置 -->
          <div class="vc-section">
            <div class="vc-section-title"><Settings :size="14" /> 抬头信息</div>
            <div class="vc-header-config">
              <div class="vc-logo-area">
                <label class="vc-label">公司 Logo</label>
                <div class="vc-logo-upload" @click="$refs.vcLogoInput.click()">
                  <img v-if="vcConfig.logoBase64" :src="vcConfig.logoBase64" class="vc-logo-img" />
                  <template v-else>
                    <ImageIcon :size="32" style="opacity:0.3" />
                    <span>点击上传</span>
                  </template>
                </div>
                <input ref="vcLogoInput" type="file" accept="image/*" hidden @change="onVcLogoUpload" />
                <button v-if="vcConfig.logoBase64" class="sample-btn sample-btn-ghost vc-small-btn" @click="vcConfig.logoBase64 = ''">清除</button>
              </div>
              <div class="vc-info-fields">
                <div class="vc-field-row">
                  <label class="vc-field-label">公司名称</label>
                  <input v-model="vcConfig.companyName" class="vc-input" placeholder="请输入公司名称" />
                </div>
                <div class="vc-field-row">
                  <label class="vc-field-label">地址</label>
                  <input v-model="vcConfig.address" class="vc-input" placeholder="请输入公司地址" />
                </div>
                <div class="vc-field-row">
                  <label class="vc-field-label">电话</label>
                  <input v-model="vcConfig.phone" class="vc-input" placeholder="请输入联系电话" />
                </div>
                <div class="vc-field-row">
                  <label class="vc-field-label">表单标题</label>
                  <input v-model="vcConfig.title" class="vc-input" placeholder="厂商确认表" />
                </div>
              </div>
            </div>
          </div>

          <!-- 字段选择 -->
          <div class="vc-section">
            <div class="vc-section-title"><Columns3 :size="14" /> 选择导出字段</div>
            <div class="vc-fields-bar">
              <button class="sample-btn sample-btn-ghost" style="font-size:12px;padding:4px 10px" @click="selectAllVcFields">全选</button>
              <button class="sample-btn sample-btn-ghost" style="font-size:12px;padding:4px 10px" @click="deselectAllVcFields">全不选</button>
              <span class="vc-hint">已选 {{ checkedVcFieldCount }} / {{ vcFields.length }} 个字段（图片列自动包含）</span>
            </div>
            <div class="vc-fields-grid">
              <label v-for="f in vcFields" :key="f.key" class="vc-field-chip" :class="{ active: f.checked }">
                <input type="checkbox" v-model="f.checked" />
                <span>{{ f.label }}</span>
              </label>
            </div>
          </div>

          <!-- 预览区域 -->
          <div class="vc-section">
            <div class="vc-section-title"><Eye :size="14" /> 效果预览</div>
            <div class="vc-preview">
              <!-- 抬头预览 -->
              <div class="vc-preview-header">
                <img v-if="vcConfig.logoBase64" :src="vcConfig.logoBase64" class="vc-preview-logo" />
                <div class="vc-preview-company">
                  <strong>{{ vcConfig.companyName || '公司名称' }}</strong>
                  <span v-if="vcConfig.address">{{ vcConfig.address }}</span>
                  <span v-if="vcConfig.phone">{{ vcConfig.phone }}</span>
                </div>
              </div>
              <div class="vc-preview-title">{{ vcConfig.title || '厂商确认表' }}</div>
              <div class="vc-preview-date">{{ new Date().toLocaleDateString('zh-CN') }}</div>
              <!-- 表格预览 -->
              <table class="vc-preview-table">
                <thead>
                  <tr>
                    <th>序号</th>
                    <th v-for="f in visibleVcFields" :key="f.key">{{ f.label }}</th>
                    <th>图片</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, idx) in vcPreviewData" :key="item.id">
                    <td>{{ idx + 1 }}</td>
                    <td v-for="f in visibleVcFields" :key="f.key">{{ item[f.key] || '-' }}</td>
                    <td class="vc-img-cell">
                      <img v-if="item.thumbnail" :src="'/thumbnails/' + item.thumbnail" class="vc-thumb-img" />
                      <span v-else class="vc-no-img">无图</span>
                    </td>
                  </tr>
                  <tr v-if="vcPreviewData.length === 0">
                    <td :colspan="visibleVcFields.length + 2" class="vc-empty-row">请在表格中勾选要导出的数据</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="modal-footer" style="border-top:1px solid #e5e5ea;padding:12px 20px;display:flex;align-items:center;justify-content:flex-end;gap:10px;flex-shrink:0">
          <button class="sample-btn sample-btn-ghost" @click="saveVcConfigToLocal">保存配置</button>
          <button class="sample-btn sample-btn-ghost" @click="showVendorConfirmModal = false">取消</button>
          <button class="sample-btn sample-btn-primary" :disabled="checkedVcFieldCount === 0 || selectedIds.length === 0 || vcExporting" @click="doVendorConfirmExport">
            <Download :size="14" /> {{ vcExporting ? '生成中...' : '导出 Excel' }}
          </button>
        </div>
      </div>
    </div>
    </Teleport>

    <!-- 厂商确认表全屏模态框 -->
    <Teleport to="body">
    <div v-if="showReportModal" class="report-modal-overlay" @click.self="closeReportModal">
      <div class="report-modal-container">
        <div class="report-modal-header">
          <span class="report-modal-title">厂商确认表</span>
          <div class="report-modal-actions">
            <button class="report-modal-btn" @click="doReportPrint" title="打印">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9V2h12v7M6 18H4a2 2 0 01-2-2v-5a2 2 0 012-2h16a2 2 0 012 2v5a2 2 0 01-2 2h-2"/><rect x="6" y="14" width="12" height="8"/></svg>
              打印
            </button>
            <button class="report-modal-btn" @click="closeReportModal" title="关闭 (ESC)">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              关闭
            </button>
          </div>
        </div>
        <div class="report-modal-body">
          <div v-if="reportModalLoading" class="report-modal-loading">
            <div class="report-modal-spinner"></div>
            <span>加载报表中...</span>
            <div class="report-modal-progress">
              <div class="report-modal-progress-bar" :style="{width: reportModalProgress + '%'}"></div>
            </div>
            <span class="report-modal-progress-text">{{ Math.round(reportModalProgress) }}%</span>
          </div>
          <iframe
            v-if="reportModalUrl"
            id="reportIframe"
            :src="reportModalUrl"
            class="report-modal-iframe"
            @load="onReportIframeLoad"
          ></iframe>
        </div>
      </div>
    </div>
    </Teleport>

    <!-- 悬浮大图预览 -->
    <Teleport to="body">
      <Transition name="hover-preview-fade">
        <div
          v-if="hoverPreview.show"
          class="sr-hover-preview"
          :style="{ left: hoverPreview.x + 'px', top: hoverPreview.y + 'px' }"
        >
          <img :src="hoverPreview.src" @error="hoverPreview.fallback && hoverPreview.src !== hoverPreview.fallback ? (hoverPreview.src = hoverPreview.fallback) : (hoverPreview.show = false)" />
        </div>
      </Transition>
    </Teleport>

    <!-- Toast -->
    <Teleport to="body">
    <Transition name="toast-fade">
      <div v-if="toast.show" class="sr-toast" :class="toast.type" style="z-index:999999!important;position:fixed">{{ toast.message }}</div>
    </Transition>
    </Teleport>

    <!-- 对照资料管理弹窗 -->
    <Teleport to="body">
      <div v-if="showRefDataModal" class="batch-image-modal-overlay import-modal-overlay" style="background:transparent">
        <div class="ref-modal">
          <div class="ref-modal-header">
            <strong>对照资料管理</strong>
            <div class="ref-tabs">
              <button :class="['ref-tab', { active: refActiveTab === 'category' }]" @click="refActiveTab = 'category'">产品种类</button>
              <button :class="['ref-tab', { active: refActiveTab === 'packaging' }]" @click="refActiveTab = 'packaging'">包装方式</button>
            </div>
            <X :size="22" class="cursor-pointer" @click="showRefDataModal = false" />
          </div>
          <div class="ref-modal-body">
            <!-- 种类管理 - 树形展开 -->
            <div v-if="refActiveTab === 'category'" class="ref-panel">
              <div class="ref-panel-toolbar">
                <button class="sample-btn sample-btn-primary" style="font-size:18px;height:44px;padding:0 18px" @click="openRefCategoryAdd"><Plus :size="18" /> 新增</button>
                <button class="sample-btn sample-btn-ghost" style="font-size:18px;height:44px;padding:0 18px" @click="refExpandAllCat"><ChevronsDownUp :size="18" /> 展开/折叠全部</button>
                <button class="sample-btn sample-btn-ghost" style="font-size:18px;height:44px;padding:0 18px" :disabled="refSelectedCatIds.length === 0" @click="refDeleteSelectedCats"><Trash2 :size="18" /> 删除选中</button>
                <div class="ref-search-box">
                  <Search :size="18" />
                  <input v-model="refCatKeyword" placeholder="搜索编号或名称..." @input="refFilterCategories" />
                </div>
              </div>
              <vxe-table ref="refCatGridRef" :data="refCatTreeData" :tree-config="{ transform: true, rowField: 'id', parentField: '_parentId', expandAll: true, line: false }"
                :checkbox-config="{ checkField: '_ck' }" height="720" stripe border size="small"
                :virtual-y-config="{ enabled: true, gt: 10 }"
                :header-cell-style="{ textAlign:'center', fontSize:'22px' }"
                :cell-style="{ textAlign:'center', fontSize:'20px' }"
                @checkbox-change="refCatGridRef && (refSelectedCatIds = refCatGridRef.getCheckboxRecords().map(r => r.id))"
                @checkbox-all="refCatGridRef && (refSelectedCatIds = refCatGridRef.getCheckboxRecords().map(r => r.id))">
                <vxe-column type="checkbox" width="50" />
                <vxe-column field="code" title="编号" width="140" tree-node show-overflow />
                <vxe-column field="name" title="名称" min-width="220" show-overflow />
                <vxe-column field="keywords" title="关键词(逗号分隔)" min-width="200" show-overflow>
                  <template #default="{ row }">
                    <input class="ref-inline-input" :value="row.keywords || ''" placeholder="合金,滑行,回力"
                      @blur="saveRefCatKeywords(row, $event.target.value)" />
                  </template>
                </vxe-column>
                <vxe-column field="_childCount" title="子项数" width="100" align="center">
                  <template #default="{ row }">{{ row._childCount || '' }}</template>
                </vxe-column>
                <vxe-column title="操作" width="160" fixed="right">
                  <template #default="{ row }">
                    <div style="display:flex;gap:6px;justify-content:center">
                    <button class="ref-action-btn" @click="refEditCategory(row)"><Pencil :size="16" /></button>
                    <button class="ref-action-btn danger" @click="refDeleteCategory(row)"><Trash2 :size="16" /></button>
                    </div>
                  </template>
                </vxe-column>
              </vxe-table>
            </div>

            <!-- 包装管理 -->
            <div v-if="refActiveTab === 'packaging'" class="ref-panel">
              <div class="ref-panel-toolbar">
                <button class="sample-btn sample-btn-primary" style="font-size:18px;height:44px;padding:0 18px" @click="openRefPackagingAdd"><Plus :size="18" /> 新增</button>
                <button class="sample-btn sample-btn-ghost" style="font-size:18px;height:44px;padding:0 18px" :disabled="refSelectedPkgIds.length === 0" @click="refDeleteSelectedPkgs"><Trash2 :size="18" /> 删除选中</button>
                <div class="ref-search-box">
                  <Search :size="18" />
                  <input v-model="refPkgKeyword" placeholder="搜索..." @keyup.enter="refLoadPackagings" />
                </div>
              </div>
              <vxe-table ref="refPkgGridRef" :data="refPackagings" :checkbox-config="{ checkField: '_ck' }" height="720" stripe border size="small"
                :virtual-y-config="{ enabled: true, gt: 10 }"
                :header-cell-style="{ textAlign:'center', fontSize:'22px' }"
                :cell-style="{ textAlign:'center', fontSize:'20px' }"
                @checkbox-change="refPkgGridRef && (refSelectedPkgIds = refPkgGridRef.getCheckboxRecords().map(r => r.id))"
                @checkbox-all="refPkgGridRef && (refSelectedPkgIds = refPkgGridRef.getCheckboxRecords().map(r => r.id))">
                <vxe-column type="checkbox" width="50" />
                <vxe-column field="code" title="编号" width="120" />
                <vxe-column field="name" title="中文包装" min-width="200" show-overflow />
                <vxe-column field="nameEn" title="英文包装" min-width="200" show-overflow />
                <vxe-column title="操作" width="160" fixed="right">
                  <template #default="{ row }">
                    <div style="display:flex;gap:6px;justify-content:center">
                    <button class="ref-action-btn" @click="refEditPackaging(row)"><Pencil :size="16" /></button>
                    <button class="ref-action-btn danger" @click="refDeletePackaging(row)"><Trash2 :size="16" /></button>
                    </div>
                  </template>
                </vxe-column>
              </vxe-table>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 种类新增/编辑小弹窗 -->
    <Teleport to="body">
      <div v-if="showRefCatForm" class="batch-image-modal-overlay import-modal-overlay" style="background:transparent">
        <div class="ref-form-modal">
          <div class="ref-modal-header">
            <strong>{{ refEditingCat ? '编辑种类' : '新增种类' }}</strong>
            <X :size="22" class="cursor-pointer" @click="showRefCatForm = false" />
          </div>
          <div class="ref-modal-body">
            <div class="ref-form-row">
              <label>种类编号 <span class="ref-required">*</span></label>
              <input v-model="refCatForm.code" placeholder="如 1, 101" />
            </div>
            <div class="ref-form-row">
              <label>种类名称 <span class="ref-required">*</span></label>
              <input v-model="refCatForm.name" placeholder="如 遥控玩具" />
            </div>
            <div class="ref-form-row">
              <label>匹配关键词</label>
              <input v-model="refCatForm.keywords" placeholder="如 遥控,无线,R/C（逗号分隔）" />
            </div>
            <div class="ref-form-row">
              <label>层级 <span class="ref-required">*</span></label>
              <select v-model="refCatForm.level">
                <option :value="1">一级类目</option>
                <option :value="2">二级类目</option>
              </select>
            </div>
            <div v-if="refCatForm.level === 2" class="ref-form-row">
              <label>父级编号</label>
              <select v-model="refCatForm.parentCode">
                <option value="">-- 请选择 --</option>
                <option v-for="p in refLevel1Cats" :key="p.code" :value="p.code">{{ p.code }} - {{ p.name }}</option>
              </select>
            </div>
          </div>
          <div class="ref-modal-footer">
            <button class="sample-btn sample-btn-ghost" @click="showRefCatForm = false">取消</button>
            <button class="sample-btn sample-btn-primary" @click="refSaveCategory">保存</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 包装新增/编辑小弹窗 -->
    <Teleport to="body">
      <div v-if="showRefPkgForm" class="batch-image-modal-overlay import-modal-overlay" style="background:transparent">
        <div class="ref-form-modal">
          <div class="ref-modal-header">
            <strong>{{ refEditingPkg ? '编辑包装方式' : '新增包装方式' }}</strong>
            <X :size="22" class="cursor-pointer" @click="showRefPkgForm = false" />
          </div>
          <div class="ref-modal-body">
            <div class="ref-form-row">
              <label>包装编号 <span class="ref-required">*</span></label>
              <input v-model="refPkgForm.code" placeholder="如 1" />
            </div>
            <div class="ref-form-row">
              <label>中文包装 <span class="ref-required">*</span></label>
              <input v-model="refPkgForm.name" placeholder="如 展示盒" />
            </div>
            <div class="ref-form-row">
              <label>英文包装</label>
              <input v-model="refPkgForm.nameEn" placeholder="如 Display Box" />
            </div>
          </div>
          <div class="ref-modal-footer">
            <button class="sample-btn sample-btn-ghost" @click="showRefPkgForm = false">取消</button>
            <button class="sample-btn sample-btn-primary" @click="refSavePackaging">保存</button>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount, onActivated, nextTick, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api'
import { useToast } from '@/composables/useToast'
import JSZip from 'jszip'
import { useCrop } from '@/composables/useCrop'
import { useModalDrag } from '@/composables/useModalDrag'
import { useCardMode } from '@/composables/useCardMode'
import { useSampleForm } from '@/composables/useSampleForm'
import { useImport } from '@/composables/useImport'
import { useDeletedRestore } from '@/composables/useDeletedRestore'
import { useAuth } from '@/stores/auth'
import SimpleDatePicker from '@/components/SimpleDatePicker.vue'
import { useBatchImage } from '@/composables/useBatchImage'
import { useBatchVideo } from '@/composables/useBatchVideo'
import { useScanPrint } from '@/composables/useScanPrint'
import { useExport } from '@/composables/useExport'
import { useRefData } from '@/composables/useRefData'
import { useAdvancedSearch } from '@/composables/useAdvancedSearch'
import { useGridPrefSync } from '@/composables/useGridPrefSync'
import '@/styles/sample.css'
import '@/styles/sample-form.css'
import '@/styles/client-sample-detail.css'
import {
  Database, Search, Plus, Pencil, Trash2, Save, X, Upload, Download,
  FileUp, FileDown, FileSpreadsheet, FileOutput, MoreHorizontal, Settings,
  ChevronsUp, ChevronsDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  MapPin, Crosshair, Filter, Columns3, ImagePlus, Coins, Package, PackageOpen, DollarSign, Printer, Loader2,
  Image as ImageIcon, RotateCcw, AlertTriangle, AlertCircle, Check, CheckCircle, CheckCircle as CheckCircleIcon, Info, Video as VideoIcon, List, ListChecks, LayoutGrid, Copy, GripVertical, RotateCw, ChevronDown, Eye, EyeOff, ChevronsDownUp, ShieldAlert, Lock, Unlock
} from 'lucide-vue-next'

// 批量翻译：通过后端代理调用百度翻译 API
async function baiduTranslateBatch(texts, from = 'zh', to = 'en') {
  if (!texts || texts.length === 0) return texts
  try {
    const res = await api('/api/translate/batch', {
      method: 'POST',
      body: JSON.stringify({ texts, from, to })
    })
    if (res.code === 200 && res.data) {
      return res.data
    }
    console.error('翻译失败:', res.message)
    return null
  } catch (e) {
    console.error('翻译请求异常:', e)
    return null
  }
}

const router = useRouter()

const { toast, showToast, showConfirm, confirmMessage, showConfirmDialog, onConfirmOk, onConfirmCancel, showAlert, alertMessage, alertType, showAlertDialog, onAlertClose } = useToast()
const route = useRoute()

const gridToolbarConfig = { custom: true }

const showScanPrintModal = ref(false)
const showMultiPrintModal = ref(false)

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
const { cropEditorRef, cropImgRef, cropSelecting, cropDone, cropX, cropY, cropW, cropH, cropOverlayStyle, cropBoxStyle, cropDisplayScale, resetCropState, resetCrop, onCropImgLoad, onCropMouseDown, onCropMouseMove, onCropMouseUp, onHandleDown } = useCrop()

const mpColumns = [
  { type: 'seq', width: 80, title: '序号' },
  { field: 'sampleCode', title: '公司编号', width: 200, showOverflow: true },
  { field: 'factoryCode', title: '出厂货号', width: 300, showOverflow: true },
  { field: 'sampleName', title: '样品名称', minWidth: 600, showOverflow: true, ellipsis: true },
  { field: 'copies', title: '打印张数', width: 300, slots: { default: 'copies_edit' } }
]

const { allFormFields, fieldVisible, visibleFormFields, showFieldSettings, toggleFieldSettings, formExpanded, formVisible, formMode, formData } = useSampleForm()
const auth = useAuth()

const currentSample = ref(null)
const currentSampleImages = ref([])
const stripIndex = ref(0)

const tableData = ref([])
const { cardMode, cardOverlayRef, cardScrollTop, cardContainerWidth, cardRowHeight, cardVisibleRange, cardTotalRows, cardVisibleItems, cardSpacerTop, cardSpacerBottom, onCardScroll } = useCardMode(tableData, formExpanded)
const tableLoading = ref(false)
const tableWrapHeight = ref(600)
const tableLoaded = ref(false)

// 悬浮预览大图
const hoverPreview = reactive({
  show: false,
  src: '',
  fallback: '',
  x: 0,
  y: 0
})
let hoverTimer = null
const currentPage = ref(1)
const pageSize = ref(2000)
const pageSizeOptions = [500, 1000, 2000, 4000, 5000]
const totalRecords = ref(0)
const totalPages = computed(() => Math.max(1, Math.ceil(totalRecords.value / pageSize.value)))
const currentSortField = ref('create_time')
const currentSortOrder = ref('desc')
const selectedIds = ref([])
const lastCheckboxIndex = ref(-1)
// 列区域选取
const areaDragging = ref(false)
const areaDragField = ref('')       // 真实字段名（用于 cell-style 匹配）
const areaDragColId = ref('')       // DOM colid（用于 querySelector）
const areaDragStartRowId = ref(null)
const areaDragEndRowId = ref(null)
const areaDragMoved = ref(false)
const areaDragStartY = ref(0)
const areaSelectedColumn = ref('')   // 已确认的选区列 — 真实字段名
const areaSelectedColId = ref('')    // 已确认的选区列 — DOM colid
const areaSelectedStartRowId = ref(null)  // 已确认的选区起行
const areaSelectedEndRowId = ref(null)    // 已确认的选区止行
const areaRenderTick = ref(0)
const extDragging = ref(false)    // 是否正在拖拽把手延伸选区
const isColumnDragging = ref(false) // 是否正在拖拽列排序（期间跳过 cell-style 重渲染）
let areaHandleEl = null           // 选区右下角把手元素
let _areaRaf = null                // RAF 节流：合并同一帧内的多次 areaRenderTick 更新
const areaSelectedCount = computed(() => {
  if (!areaSelectedColumn.value) return 0
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return 0
  return Math.abs(eIdx - sIdx) + 1
})

// ── 拖拽选区预计算 Set（O(1) 查找代替 O(n) findIndex）──
const areaDragRowIdSet = computed(() => {
  if (!areaDragging.value || !areaDragField.value) return null
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaDragStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaDragEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return null
  const min = Math.min(sIdx, eIdx)
  const max = Math.max(sIdx, eIdx)
  const set = new Set()
  for (let i = min; i <= max; i++) {
    set.add(data[i].id)
  }
  return set
})

const areaSelectedRowIdSet = computed(() => {
  if (!areaSelectedColumn.value) return null
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return null
  const min = Math.min(sIdx, eIdx)
  const max = Math.max(sIdx, eIdx)
  const set = new Set()
  for (let i = min; i <= max; i++) {
    set.add(data[i].id)
  }
  return set
})
const searchKeyword = ref('')
const locateKeyword = ref('')
const locateCursor = ref(-1)
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
const hideTaxPrice = ref(false)
const hideSupplierInfo = ref(false)
const editing = ref(false)
const editData = reactive({})
const { photoModalPos, photoModalW, photoModalH, photoModalInit, startDragModal } = useModalDrag()

const showImagePreview = ref(false)
const imagePreviewList = ref([])
const imagePreviewIndex = ref(0)
const imagePreviewSelected = ref(new Set())
// ── 大图预览缩放 ──
const ipZoom = ref(1)
const ipPanX = ref(0)
const ipPanY = ref(0)
const ipDragging = ref(false)
const ipDragStart = ref({ x: 0, y: 0, px: 0, py: 0 })
const showReportModal = ref(false)
const reportModalUrl = ref('')
const reportModalLoading = ref(false)
const reportModalProgress = ref(0)
const posPickerIdx = ref(null)
const ipUploading = ref(false)
const ipUploadDone = ref(0)
const ipUploadTotal = ref(0)
const ipDragOver = ref(false)
const ipUploadLocked = ref(true)
const thumbDragIdx = ref(-1)
const thumbDragOverIdx = ref(-1)
const thumbDragDone = ref(false)
const thumbSortClone = ref({ show: false, x: 0, y: 0, width: 0, height: 0, src: '' })
const isAllImageSelected = computed(() => {
  return imagePreviewList.value.length > 0 && imagePreviewSelected.value.size === imagePreviewList.value.length
})
const currentPreviewSrc = computed(() => {
  const img = imagePreviewList.value[imagePreviewIndex.value]
  if (!img) return ''
  if (img.filePath) return '/images/' + img.filePath
  if (img.hash) return '/images/view/hash/' + img.hash
  return '/thumbnails/' + img.thumbnailPath
})
const photoModalStyle = computed(() => ({
  display: showPhotoModal.value ? 'flex' : 'none',
  flexDirection: 'column',
  width: '2300px',
  height: '1200px',
  top: photoModalPos.y + 'px',
  left: photoModalPos.x + 'px',
  position: 'fixed'
}))

const showBatchResultModal = ref(false)
const batchResult = reactive({ successCount: 0, failCount: 0, duplicateCount: 0, updatedCount: 0, unmatchedCount: 0, failedRows: [], failList: [], failFiles: [], unmatchedList: [], unmatchedFiles: [] })

const showRestoreDeletedModal = ref(false)
const showMainBatchQuery = ref(false)
const mainBatchField = ref('sampleCode')
const mainBatchInput = ref('')
const mainBatchQueryActive = ref(false)

const showBatchImageModal = ref(false)
const batchImageType = ref('factory-code')

const showBatchVideoModal = ref(false)
const batchVideoType = ref('company-code')

const showVideoPreviewModal = ref(false)
const sampleVideos = ref([])
const videoPreviewIndex = ref(0)

const activeSearchConditions = ref(null)  // 保存当前活跃的综合查询条件

const allColumns = [
  { type: 'checkbox', width: 44, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'image', title: '图片', width: 90, sortable: true, slots: { default: 'image_default' } },
  { field: 'manufacturerCode', title: '厂商编号', width: 110, showOverflow: true, sortable: true, visible: false },
  { field: 'sampleCode', title: '公司编号', width: 140, showOverflow: true, sortable: true },
  { field: 'category', title: '种类名称', width: 110, showOverflow: true, visible: false },
  { field: 'categoryCode', title: '种类编号', width: 90, showOverflow: true, visible: false },
  { field: 'sampleName', title: '样品名称', width: 850, showOverflow: true, sortable: true },
  { field: 'englishName', title: '英文名称', width: 150, showOverflow: true, visible: false },
  { field: 'factoryCode', title: '出厂货号', width: 140, showOverflow: true, sortable: true },
  { field: 'sampleUnit', title: '样品单位', width: 100, showOverflow: true, visible: false },
  { field: 'sampleUnitEn', title: '英文单位', width: 100, showOverflow: true, visible: false },
  { field: 'packagingCn', title: '中文包装', width: 120, showOverflow: true, sortable: true },
  { field: 'packageCode', title: '包装编号', width: 100, showOverflow: true, visible: false },
  { field: 'packagingEn', title: '英文包装', width: 100, showOverflow: true, visible: false },
  { field: 'color', title: '颜色', width: 80, showOverflow: true, visible: false },
  { field: 'colorEn', title: '英文颜色', width: 90, showOverflow: true, visible: false },
  { field: 'size', title: '规格尺寸', width: 100, showOverflow: true, visible: false },
  { field: 'origin', title: '产地', width: 80, showOverflow: true, visible: false },
  { field: 'factoryPrice', title: '出厂价', width: 100, showOverflow: true, className: 'sample-cell-price', headerClassName: 'sample-header-red', sortable: true },
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
  { field: 'name', title: '厂商名称', minWidth: 140, showOverflow: true, visible: false },
  { field: 'boothNo', title: '摊位号', width: 80, showOverflow: true, visible: false },
  { field: 'contact1', title: '联系人', width: 90, showOverflow: true, visible: false },
  { field: 'phone1', title: '电话', width: 120, showOverflow: true, visible: false },
  { field: 'mobile1', title: '手机', width: 120, showOverflow: true, visible: false },
  { field: 'fax', title: '传真', width: 120, showOverflow: true, visible: false },
  { field: 'qq', title: 'QQ', width: 90, showOverflow: true, visible: false },
  { field: 'certification', title: '产品认证', width: 800, showOverflow: true, sortable: true },
  { field: 'certificationCount', title: '认证数', width: 70, showOverflow: true, visible: false },
  { field: 'remark', title: '备注', width: 800, showOverflow: true, sortable: true },
  { field: 'remarkEn', title: '英文备注', width: 140, showOverflow: true, visible: false },
  { field: 'registrant', title: '登记人', width: 100, showOverflow: true },
  { field: 'infringement', title: '侵权', width: 70, showOverflow: true, visible: false },
  { field: 'batteryInfo', title: '电池信息', width: 100, showOverflow: true, visible: false },
  { field: 'modifier', title: '修改人', width: 100, showOverflow: true },
  { field: 'updateTime', title: '修改日期', width: 300, sortable: true, showOverflow: true, formatter: ({ cellValue }) => cellValue ? String(cellValue).replace('T', ' ') : '' },
  { field: 'createTime', title: '登记时间', width: 300, sortable: true, showOverflow: true, formatter: ({ cellValue }) => cellValue ? String(cellValue).replace('T', ' ') : '' },
  { field: 'action', title: '操作', width: 82, fixed: 'right', slots: { default: 'action_default' }, visible: false }
]

// 表格列设置跨设备同步
const { fullKey: gridStorageKey, saveToBackend: saveGridPrefs, ready: prefReady } = useGridPrefSync(gridRef, 'sample', allColumns)

const loadTableData = async () => {
  tableLoading.value = true
  try {
    // 如果有活跃的综合查询条件，走搜索接口
    if (activeSearchConditions.value && activeSearchConditions.value.length > 0) {
      const conditions = [...activeSearchConditions.value]
      // 确保厂商筛选条件存在
      if (manufacturerCode.value && !conditions.some(c => c.field === 'manufacturerCode')) {
        conditions.push({ field: 'manufacturerCode', operator: 'eq', value: manufacturerCode.value })
      }
      const res = await api(`/samples/search?current=${currentPage.value}&size=${pageSize.value}&sortField=${currentSortField.value}&sortOrder=${currentSortOrder.value}`, {
        method: 'POST',
        body: JSON.stringify({ conditions })
      })
      const data = res.data || res || {}
      tableData.value = data.records || data.list || data || []
      totalRecords.value = data.total || tableData.value.length
    } else {
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
    }
  } catch (e) {
    console.error(e)
  } finally {
    tableLoading.value = false
    clearAreaSelection()
  }
}

const { showImportModal, importFile, importUploading, showImportPreview, importPreviewAllRows, importPreviewData, importOriginalData, importPreviewHeaders, importSelectedRows, importSelectedRowIndexes, importPreviewGridRef, importPreviewWrapRef, importPreviewPage, importPreviewPageSize, importArea, batchEditField, batchEditValue, batchEditDropdownOpen, batchEditFields, showImportConfirmModal, importConfirmCount, importProgress, importUpdateMode, importParsing, importParsingProgress, importParsingStage, importProgressText, importPreviewCatFilter, importPreviewPkgFilter, importPreviewDupFilter, importValidCatNames, importValidPkgNames, importPkgList, importCatList, importPreviewDisplayData, importPreviewFilteredCount, importPreviewTotalPages, IMPORT_PREVIEW_ALL_COLUMNS, showDupDetailModal, dupDetailRows, dupDetailGridRef, dupDetailSelectedId, showDupImageModal, dupImageUrl, DUP_DETAIL_COLUMNS, IMPORT_PREVIEW_EDIT_CONFIG, openDupDetail, confirmDupOverwrite, cancelDupDetail, batchAbandonUpdate, onDupDetailCheckChange, openDupImage, closeDupImage, openImportModal: _importOpenModal, onImportFileChange, onImportDrop, doConfirmImport, executeImport, exportImportFailedRows, doImport, syncPreviewPage, onPreviewPageSizeChange, onPreviewPageChange, onTogglePreviewFilter, restorePreviewRow, deletePreviewRow, deleteSelectedPreviewRows, batchEditRun, onImportPreviewCheckChange, onImportCellEdit, cancelImportPreview, selectAllPreviewRows, clearPreviewSelection, exportSelectedRows } = useImport(loadTableData, showBatchResultModal, batchResult, showToast, baiduTranslateBatch)

const openImportModal = () => {
  showMoreDropdown.value = false
  _importOpenModal()
}

const { deletedGridRef, deletedData, deletedLoading, deletedTotal, deletedSelected, deletedAllData, deletedFilterField, deletedFilterKeyword, deletedFullscreen, deletedFullscreenSearch, deletedGridMaxHeight, showDeletedBatchQuery, deletedBatchField, deletedBatchInput, deletedFilterActive, deletedSortMethod, deletedGridColumns, openRestoreDeletedModal, fetchDeletedSamples, applyDeletedFilter, doDeletedFilter, doDeletedResetFilter, onDeletedZoom, onDeletedToolbarClick, openDeletedBatchQuery, doDeletedBatchQuery, onDeletedCheckChange, doRestoreDeleted, onDeletedFullscreenSearch, clearDeletedFullscreenSearch } = useDeletedRestore(showRestoreDeletedModal, showMoreDropdown, showAlertDialog, showConfirmDialog, loadTableData)

const { batchFiles, batchUploading, batchUploadProgress, batchMatched, batchMatchLoading, batchCurrentIndex, showBatchConflictModal, batchConflicts, batchConflictSelected, resolveBatchConflicts, cancelBatchConflict, removeConflictCode, goToPrev, goToNext, openBatchImageModal, onDragOver, onDragLeave, onBatchDrop, onBatchFileChange, removeBatchFile, doBatchMatch, setBatchActionAll, closeBatchModal, doBatchImageUpload } = useBatchImage(showMoreDropdown, showBatchImageModal, batchImageType, showBatchResultModal, batchResult, showToast, tableData, manufacturerCode, currentSample)

const conflictGridColumns = [
  { type: 'checkbox', width: 44 },
  { title: '图片', width: 72, slots: { default: 'conflictImage' }, align: 'center' },
  { field: 'sampleName', title: '样品名称', minWidth: 120, align: 'center' },
  { field: 'sampleCode', title: '公司编号', minWidth: 100, align: 'center' },
  { field: 'factoryCode', title: '出厂货号', minWidth: 100, align: 'center' },
  { field: 'manufacturerCode', title: '厂商编号', width: 100, align: 'center' },
  { field: 'remark', title: '中文备注', minWidth: 120, align: 'center' },
  { field: 'boothNo', title: '摊位号', width: 80, align: 'center' }
]

// 多选：toggle 单行，选中的 ID 存入数组
function toggleConflictRow(conflict, row) {
  conflictValidationMsg.value = ''
  if (!batchConflictSelected.value[conflict.code]) {
    batchConflictSelected.value[conflict.code] = []
  }
  const arr = batchConflictSelected.value[conflict.code]
  const idx = arr.indexOf(row.id)
  if (idx > -1) {
    arr.splice(idx, 1)
  } else {
    arr.push(row.id)
  }
}

// 全选/取消全选（vxe @checkbox-all 返回当前页 visibleData）
function toggleConflictAll(conflict, records) {
  const allIds = conflict.samples.map(s => s.id)
  const current = batchConflictSelected.value[conflict.code] || []
  if (allIds.length > 0 && allIds.every(id => current.includes(id))) {
    // 已全选 → 取消
    batchConflictSelected.value[conflict.code] = []
  } else {
    batchConflictSelected.value[conflict.code] = [...allIds]
  }
}

// 确认冲突选择：每个货号至少选一条
const conflictValidationMsg = ref('')
function confirmConflictSelection() {
  const missing = batchConflicts.value.filter(c => {
    const sel = batchConflictSelected.value[c.code]
    return !sel || sel.length === 0
  })
  if (missing.length > 0) {
    conflictValidationMsg.value = `以下货号至少选择一条：${missing.map(c => c.code).join('、')}`
    return
  }
  conflictValidationMsg.value = ''
  resolveBatchConflicts(batchConflictSelected.value)
}

const conflictPreviewSrc = ref('')
watch(showBatchConflictModal, (val) => { if (!val) conflictPreviewSrc.value = '' })

const downloadFailedZip = async () => {
  const files = [
    ...(batchResult.failFiles || []),
    ...(batchResult.unmatchedFiles || [])
  ]
  if (files.length === 0) return
  const zip = new JSZip()
  files.forEach(f => {
    if (f.file instanceof File) zip.file(f.name, f.file)
  })
  const blob = await zip.generateAsync({ type: 'blob' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `导入失败图片_${new Date().toISOString().slice(0, 10)}.zip`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

const { batchVideoFiles, batchVideoMatched, videoMatchLoading, videoUploading, videoUploadProgress, videoCurrentIndex, customMatchSubType, customManufacturerCode, customCodesText, openBatchVideoModal, closeBatchVideoModal, onVideoDragOver, onVideoDrop, onVideoFileChange, goToVideoPrev, goToVideoNext, removeVideoFile, setVideoActionAll, doBatchVideoUpload
} = useBatchVideo(showBatchVideoModal, batchVideoType, showAlertDialog)

const {
  scanPrintCode, scanPrintResult, scanPrintImageSrc, scanPrintError,
  scanPrintType, scanPrintLoading, scanPrintContinuous, scanPrintCount, scanPrintInputRef,
  openScanPrintModal, searchScanPrint, doScanPrint,
  doPrintTable, doPrintQuarterTable, doPrintAllLabels, doPrintWithImages,
  multiPrintType, multiPrintBatchCopies, multiPrintRecords, totalPrintPages, batchSetCopies,
  doPrintMultiCopies, confirmMultiPrint
} = useScanPrint(
  showScanPrintModal, showMultiPrintModal, showPrintDropdown,
  gridRef, tableData, totalRecords, currentPage, pageSize, loadTableData,
  showToast, showConfirmDialog, showAlertDialog
)

const {
  downloadTemplate,
  showExportModal, exportFields, dragIndex, checkedExportFieldCount,
  templateName, currentTemplate, exportTemplates, showTplMenu, showTplSaveInput, tplSaveRef,
  initExportFields, loadExportTemplates, saveExportTemplate, loadExportTemplate, deleteExportTemplate,
  selectAllExportFields, deselectAllExportFields,
  onExportDragStart, onExportDragOver, onExportDrop, onExportDragEnd,
  doExport, exportExcel,
  showVendorConfirmModal, vcExporting, vcLogoInputRef, vcConfig,
  vcFields, visibleVcFields, checkedVcFieldCount, vcPreviewData,
  initVcFields, selectAllVcFields, deselectAllVcFields,
  onVcLogoUpload, saveVcConfigToLocal, doVendorConfirmExport,
  showTemplateSelect, availableTemplates, selectedTemplateId, templateSearchKeyword,
  vcSessionLoading, filteredTemplates,
  openVendorConfirmReport, confirmTemplateAndOpen,
} = useExport(
  selectedIds, gridRef, showToast, showAlertDialog, showMoreDropdown, showPrintDropdown
)

// ===== 对照资料管理 =====
const {
  showRefDataModal, refActiveTab, openReferenceDataModal,
  refCategories, refCatTreeData, refCatKeyword, refSelectedCatIds, refCatGridRef,
  showRefCatForm, refEditingCat, refCatForm, refLevel1Cats,
  refLoadCategories, refFilterCategories, refExpandAllCat,
  openRefCategoryAdd, refEditCategory, refSaveCategory, refDeleteCategory, refDeleteSelectedCats, saveRefCatKeywords,
  refPackagings, refPkgKeyword, refSelectedPkgIds, refPkgGridRef,
  showRefPkgForm, refEditingPkg, refPkgForm, refLoadPackagings,
  openRefPackagingAdd, refEditPackaging, refSavePackaging, refDeletePackaging, refDeleteSelectedPkgs,
} = useRefData(showMoreDropdown, showToast)

const {
  showAdvancedSearch, advForm, searchElapsed,
  saveAdvForm, restoreAdvForm, clearAdvForm, openAdvancedSearch, doAdvancedSearch: _doAdvancedSearch,
} = useAdvancedSearch(tableData, totalRecords, currentPage, pageSize, currentSortField, currentSortOrder, activeSearchConditions, manufacturerCode)

const doAdvancedSearch = async () => {
  const ok = await _doAdvancedSearch()
  if (ok) {
    nextTick(() => {
      if (tableData.value.length > 0 && gridRef.value) {
        gridRef.value.setCurrentRow(tableData.value[0])
        selectSample(tableData.value[0])
      }
    })
  }
}

const fetchImagesForSample = async (sampleId) => {
  try {
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

let thumbSortOrigin = null // { idx, startX, startY }
let thumbSortMoveHandler = null
let thumbSortUpHandler = null

const onThumbSortDown = (e, idx) => {
  if (e.button !== 0) return
  thumbDragDone.value = false
  const el = e.currentTarget
  const rect = el.getBoundingClientRect()
  const img = imagePreviewList.value[idx]
  const src = img.thumbnailPath ? '/thumbnails/' + img.thumbnailPath : '/images/' + img.filePath
  thumbSortOrigin = { idx, startX: e.clientX, startY: e.clientY, el, rect }
  // track mousemove/mouseup on document
  document.addEventListener('mousemove', thumbSortMoveHandler = (ev) => {
    const dx = ev.clientX - thumbSortOrigin.startX
    const dy = ev.clientY - thumbSortOrigin.startY
    const dist = Math.sqrt(dx * dx + dy * dy)
    if (!thumbSortClone.value.show && dist < 6) return // threshold to start drag
    if (!thumbSortClone.value.show) {
      // enter drag mode
      thumbDragIdx.value = idx
      thumbSortClone.value = {
        show: true,
        x: ev.clientX - rect.width / 2,
        y: ev.clientY - rect.height / 2,
        width: rect.width,
        height: rect.height,
        src
      }
      el.style.opacity = '0.3'
    }
    // update clone position
    thumbSortClone.value.x = ev.clientX - rect.width / 2
    thumbSortClone.value.y = ev.clientY - rect.height / 2
    // find which thumb we're over
    const target = document.elementFromPoint(ev.clientX, ev.clientY)
    const targetThumb = target?.closest('.ip-thumb')
    if (targetThumb) {
      const thumbs = Array.from(targetThumb.parentElement.children)
      const overIdx = thumbs.indexOf(targetThumb)
      if (overIdx >= 0 && overIdx !== idx) {
        thumbDragOverIdx.value = overIdx
      } else {
        thumbDragOverIdx.value = -1
      }
    } else {
      thumbDragOverIdx.value = -1
    }
  })
  document.addEventListener('mouseup', thumbSortUpHandler = async (ev) => {
    const targetIdx = thumbDragOverIdx.value
    const fromIdx = idx
    cleanupThumbSort()
    if (targetIdx < 0 || targetIdx === fromIdx) return
    thumbDragDone.value = true
    const list = [...imagePreviewList.value]
    const [moved] = list.splice(fromIdx, 1)
    list.splice(targetIdx, 0, moved)
    const items = list.map(i => ({ id: i.id, hash: i.hash })).filter(it => it.id)
    const result = await api('/images/reorder', { method: 'POST', body: JSON.stringify(items) })
    if (result && result.code === 200) {
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
      imagePreviewList.value = [...currentSampleImages.value]
    }
  }, { once: true })
}
const cleanupThumbSort = () => {
  if (thumbSortMoveHandler) {
    document.removeEventListener('mousemove', thumbSortMoveHandler)
    thumbSortMoveHandler = null
  }
  if (thumbSortUpHandler) {
    document.removeEventListener('mouseup', thumbSortUpHandler)
    thumbSortUpHandler = null
  }
  if (thumbSortOrigin && thumbSortOrigin.el) {
    thumbSortOrigin.el.style.opacity = ''
  }
  thumbSortOrigin = null
  thumbSortClone.value.show = false
  thumbDragIdx.value = -1
  thumbDragOverIdx.value = -1
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

// ── 拖拽上传锁控制 ──
const onIpDragEnter = () => {
  ipDragOver.value = true
}
const onIpDragOver = (e) => {
  e.preventDefault()
  if (!ipUploadLocked.value) {
    e.dataTransfer.dropEffect = 'copy'
  } else {
    e.dataTransfer.dropEffect = 'none'
  }
}
const handlePreviewDragLeave = (e) => {
  if (e.currentTarget === e.target) {
    ipDragOver.value = false
  }
}
const onIpDrop = async (e) => {
  e.preventDefault()
  ipDragOver.value = false
  if (ipUploadLocked.value) return
  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return
  const sampleId = currentSample.value?.id
  if (!sampleId) return
  const imageFiles = Array.from(files).filter(f => f.type.startsWith('image/'))
  if (imageFiles.length === 0) {
    showAlertDialog('请拖入图片文件', 'warning')
    return
  }
  ipUploading.value = true
  ipUploadTotal.value = imageFiles.length
  ipUploadDone.value = 0
  let successCount = 0
  for (const file of imageFiles) {
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
  await fetchImagesForSample(sampleId)
  imagePreviewList.value = currentSampleImages.value
  imagePreviewIndex.value = 0
  imagePreviewSelected.value = new Set()
  if (successCount > 0 && successCount === imageFiles.length) {
    showAlertDialog(`成功上传 ${successCount} 张图片`, 'success')
  } else if (successCount > 0) {
    showAlertDialog(`上传完成: 成功 ${successCount} 张, 失败 ${imageFiles.length - successCount} 张`, 'info')
  }
}

const viewOriginal = () => {
  if (!currentSample.value) return
  showPhotoModal.value = false
  photoModalImages.value = []
  imagePreviewList.value = currentSampleImages.value
  imagePreviewIndex.value = stripIndex.value
  imagePreviewSelected.value = new Set()
  showImagePreview.value = true
}

const openFullPreview = () => {
  if (photoModalImages.value.length === 0) return
  imagePreviewList.value = photoModalImages.value
  imagePreviewIndex.value = photoModalIndex.value
  imagePreviewSelected.value = new Set()
  showPhotoModal.value = false
  editing.value = false
  showImagePreview.value = true
}

// 关闭图片预览时恢复照片模态框
watch(showImagePreview, (v) => {
  if (!v) {
    ipZoom.value = 1
    ipPanX.value = 0
    ipPanY.value = 0
    if (photoModalImages.value.length > 0) {
      photoModalInit()
      showPhotoModal.value = true
    }
  } else {
    ipZoom.value = 1
    ipPanX.value = 0
    ipPanY.value = 0
  }
})

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

const onSearch = async () => {
  mainBatchQueryActive.value = false
  activeSearchConditions.value = null  // 清除综合查询条件
  currentPage.value = 1
  await loadTableData()
  // 搜索后自动选中第一条
  if (tableData.value.length > 0 && gridRef.value) {
    gridRef.value.setCurrentRow(tableData.value[0])
    selectSample(tableData.value[0])
  }
}

const onLocate = () => {
  const keyword = locateKeyword.value.trim()
  if (!keyword) return
  const lower = keyword.toLowerCase()
  const data = tableData.value
  // 只按出厂货号定位
  const matches = data.reduce((acc, item, i) => {
    if (item.factoryCode != null && String(item.factoryCode).toLowerCase().includes(lower)) acc.push(i)
    return acc
  }, [])
  if (matches.length === 0) return
  // 每次点击跳到下一个匹配项
  let nextCursor = locateCursor.value + 1
  if (nextCursor >= matches.length) nextCursor = 0
  locateCursor.value = nextCursor
  const idx = matches[nextCursor]
  if (gridRef.value) {
    gridRef.value.setCurrentRow(data[idx])
    selectSample(data[idx])
    gridRef.value.scrollToRow(data[idx])
  }
}

// 输入变化时重置游标
watch(locateKeyword, () => { locateCursor.value = -1 })

const clearSearch = () => {
  mainBatchQueryActive.value = false
  searchKeyword.value = ''
  locateKeyword.value = ''
  activeSearchConditions.value = null  // 清除综合查询条件
  currentPage.value = 1
  loadTableData().then(() => {
    nextTick(() => {
      if (tableData.value.length > 0 && gridRef.value) {
        gridRef.value.setCurrentRow(tableData.value[0])
        selectSample(tableData.value[0])
      }
    })
  })
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
  loadTableData().then(() => {
    nextTick(() => {
      if (tableData.value.length > 0 && gridRef.value) {
        gridRef.value.setCurrentRow(tableData.value[0])
        selectSample(tableData.value[0])
      }
    })
  })
}

const onCheckboxChange = ({ checked, row, rowIndex, $event }) => {
  if ($event && $event.shiftKey && lastCheckboxIndex.value >= 0 && rowIndex != null) {
    const startIdx = Math.min(lastCheckboxIndex.value, rowIndex)
    const endIdx = Math.max(lastCheckboxIndex.value, rowIndex)
    const data = gridRef.value.getTableData().fullData
    for (let i = startIdx; i <= endIdx; i++) {
      gridRef.value.setCheckboxRow(data[i], !!checked)
    }
  }
  lastCheckboxIndex.value = rowIndex != null ? rowIndex : -1
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

// ── 列区域选取 ──────────────────────────────────
const DATA_COL_FIELDS = new Set(
  allColumns
    .filter(c => c.field && c.type !== 'checkbox' && c.type !== 'seq')
    .map(c => c.field)
)

const getRowIdAndField = (el) => {
  const td = el.closest('td.vxe-body--column')
  if (!td) {
    const wrapper = el.closest('.vxe-body-cell--wrapper')
    if (!wrapper) return null
    const cid = wrapper.getAttribute('colid')
    const rid = wrapper.getAttribute('rowid')
    if (!cid || !rid) return null
    return { rowId: rid, field: cid }
  }
  const colid = td.getAttribute('colid')
  if (!colid) return null
  const tr = td.closest('tr')
  if (!tr) return null
  const rowid = tr.getAttribute('rowid')
  if (!rowid) return null
  return { rowId: rowid, field: colid }
}

const onTableWrapMouseDown = (e) => {
  if (e.button !== 0) return
  if (e.target.closest('.sample-area-handle')) return  // 跳过把手拖拽
  if (!tableWrapRef.value?.contains(e.target)) return
  const info = getRowIdAndField(e.target)
  if (!info) return
  areaDragStartRowId.value = info.rowId
  areaDragEndRowId.value = info.rowId
  areaDragColId.value = info.field                    // DOM colid
  areaDragField.value = getFieldByColId(info.field)   // 真实字段名
  areaDragging.value = false // 还没开始拖，只是按下
  areaDragMoved.value = false
  areaDragStartY.value = e.clientY
  // 清除之前选区
  areaSelectedColumn.value = ''
  areaSelectedColId.value = ''
  areaSelectedStartRowId.value = null
  areaSelectedEndRowId.value = null
  areaRenderTick.value++   // 触发 cellAreaStyle 重新求值
  document.addEventListener('mousemove', onDocMouseMove)
  document.addEventListener('mouseup', onDocMouseUp)
  e.preventDefault()
}

// ── 预定义的样式常量（避免每次调用创建新对象）──
const STYLE_PRICE_CELL = { textAlign: 'center', fontSize: '26px', color: '#dc2626', fontWeight: 600 }
const STYLE_DEFAULT_CELL = { textAlign: 'center', fontSize: '26px' }
const STYLE_DRAG_HIGHLIGHT = { textAlign: 'center', fontSize: '26px', background: '#e3f2fd', outline: '2px solid #4285f4', outlineOffset: '-2px' }
const STYLE_SELECTED_HIGHLIGHT = { textAlign: 'center', fontSize: '26px', background: '#dceefb', outline: '2px solid #4285f4', outlineOffset: '-2px' }
const STYLE_HEADER_DEFAULT = { background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontWeight: 600, textAlign: 'center' }
const STYLE_HEADER_PRICE = { background: '#ffffff', borderColor: '#a0bddb', color: '#dc2626', fontWeight: 600, textAlign: 'center' }

const headerCellStyleFn = ({ column }) => {
  const field = (column && (column.field || column.type)) || ''
  return field === 'factoryPrice' ? STYLE_HEADER_PRICE : STYLE_HEADER_DEFAULT
}

const cellAreaStyle = ({ row, column }) => {
  // 列拖拽排序期间完全短路，避免 vxe-grid 频繁重渲染触发响应式追踪
  if (isColumnDragging.value) {
    const f = (column && (column.field || column.type)) || ''
    return f === 'factoryPrice' ? STYLE_PRICE_CELL : STYLE_DEFAULT_CELL
  }
  const field = (column && (column.field || column.type)) || ''
  // 没有区域选区激活时，直接返回默认样式，不访问 areaRenderTick（避免 vxe-grid 内部渲染触发响应式追踪）
  if (!areaDragging.value && !areaSelectedColumn.value) {
    return field === 'factoryPrice' ? STYLE_PRICE_CELL : STYLE_DEFAULT_CELL
  }
  void areaRenderTick.value // 强制重新求值（仅选区激活时）
  if (field === 'factoryPrice') {
    return STYLE_PRICE_CELL
  }
  // 拖拽中的高亮 — O(1) Set 查找
  if (areaDragging.value && field === areaDragField.value) {
    const set = areaDragRowIdSet.value
    if (set && row && set.has(row.id)) {
      return STYLE_DRAG_HIGHLIGHT
    }
  }
  // 已确认选区高亮 — O(1) Set 查找
  if (areaSelectedColumn.value && field === areaSelectedColumn.value) {
    const set = areaSelectedRowIdSet.value
    if (set && row && set.has(row.id)) {
      return STYLE_SELECTED_HIGHLIGHT
    }
  }
  return STYLE_DEFAULT_CELL
}

const onDocMouseMove = (e) => {
  if (!areaDragging.value && !areaDragMoved.value) {
    if (Math.abs(e.clientY - areaDragStartY.value) < 6) return
    areaDragging.value = true
    areaDragMoved.value = true
    document.body.classList.add('sample-area-selecting')
  }
  if (!areaDragging.value) return
  const target = document.elementFromPoint(e.clientX, e.clientY)
  if (!target) return
  const info = getRowIdAndField(target)
  if (!info || info.field !== areaDragColId.value) return
  areaDragEndRowId.value = info.rowId
  if (!_areaRaf) {
    _areaRaf = requestAnimationFrame(() => {
      _areaRaf = null
      areaRenderTick.value++
    })
  }
}

const onDocMouseUp = () => {
  document.removeEventListener('mousemove', onDocMouseMove)
  document.removeEventListener('mouseup', onDocMouseUp)
  document.body.classList.remove('sample-area-selecting')
  if (_areaRaf) { cancelAnimationFrame(_areaRaf); _areaRaf = null }
  if (!areaDragging.value) {
    // 单击单个单元格 → 选中该单元格
    if (areaDragField.value) {
      areaSelectedColumn.value = areaDragField.value
      areaSelectedColId.value = areaDragColId.value
      areaSelectedStartRowId.value = areaDragStartRowId.value
      areaSelectedEndRowId.value = areaDragEndRowId.value
      areaRenderTick.value++
      attachAreaHandle()
    }
    return
  }
  areaDragging.value = false
  // 确认选区
  areaSelectedColumn.value = areaDragField.value
  areaSelectedColId.value = areaDragColId.value
  areaSelectedStartRowId.value = areaDragStartRowId.value
  areaSelectedEndRowId.value = areaDragEndRowId.value
  areaRenderTick.value++   // 触发 cellAreaStyle 重新求值
  attachAreaHandle()
}

const clearAreaSelection = () => {
  removeAreaHandle()
  areaSelectedColumn.value = ''
  areaSelectedColId.value = ''
  areaSelectedStartRowId.value = null
  areaSelectedEndRowId.value = null
  areaDragging.value = false
  if (_areaRaf) { cancelAnimationFrame(_areaRaf); _areaRaf = null }
  areaRenderTick.value++
}

// ===== 选区右下角把手（延伸选区） =====
const attachAreaHandle = () => {
  removeAreaHandle()
  if (!areaSelectedColId.value) return
  const wrapper = tableWrapRef.value
  if (!wrapper) return
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return
  // 选区最后一行（数据中靠后的）
  const lastIdx = Math.max(sIdx, eIdx)
  const lastId = String(data[lastIdx].id)
  // 等 vxe-grid 渲染完
  requestAnimationFrame(() => {
    const cellEl = wrapper.querySelector(`[rowid="${lastId}"] [colid="${areaSelectedColId.value}"]`)
    if (!cellEl) return
    const td = cellEl.tagName === 'TD' ? cellEl : cellEl.closest('td')
    if (!td) return
    const h = document.createElement('div')
    h.className = 'sample-area-handle'
    Object.assign(h.style, {
      position: 'absolute', right: '-6px', bottom: '-6px',
      width: '14px', height: '14px',
      background: '#4285f4', border: '2px solid #fff',
      borderRadius: '2px', boxShadow: '0 0 0 2px #4285f4',
      cursor: 'crosshair', zIndex: '10',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      color: '#fff', fontSize: '12px', fontWeight: 'bold', lineHeight: '1',
      userSelect: 'none'
    })
    h.textContent = '+'
    h.addEventListener('mousedown', onHandleMouseDown)
    td.style.position = 'relative'
    td.appendChild(h)
    areaHandleEl = h
  })
}

const removeAreaHandle = () => {
  if (areaHandleEl) {
    areaHandleEl.removeEventListener('mousedown', onHandleMouseDown)
    if (areaHandleEl.parentNode) areaHandleEl.parentNode.removeChild(areaHandleEl)
    areaHandleEl = null
  }
}

const onHandleMouseDown = (e) => {
  e.stopPropagation()
  e.preventDefault()
  extDragging.value = true
  document.body.classList.add('sample-area-selecting')
  document.addEventListener('mousemove', onExtMouseMove)
  document.addEventListener('mouseup', onExtMouseUp)
}

const onExtMouseMove = (e) => {
  if (!extDragging.value) return
  const target = document.elementFromPoint(e.clientX, e.clientY)
  if (!target) return
  const info = getRowIdAndField(target)
  if (!info) return
  areaSelectedEndRowId.value = info.rowId
  if (!_areaRaf) {
    _areaRaf = requestAnimationFrame(() => {
      _areaRaf = null
      areaRenderTick.value++
    })
  }
}

const onExtMouseUp = () => {
  extDragging.value = false
  document.body.classList.remove('sample-area-selecting')
  document.removeEventListener('mousemove', onExtMouseMove)
  document.removeEventListener('mouseup', onExtMouseUp)
  if (_areaRaf) { cancelAnimationFrame(_areaRaf); _areaRaf = null }
  // 重新挂把手到新的最后一行
  attachAreaHandle()
}

const getFieldByColId = (colId) => {
  const grid = gridRef.value
  if (!grid) return colId // fallback
  const cols = grid.getColumns() || []
  const col = cols.find(c => c.id === colId)
  return col ? col.field : colId
}

const getAreaSelectedValues = () => {
  if (!areaSelectedColumn.value) return []
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return []
  const min = Math.min(sIdx, eIdx)
  const max = Math.max(sIdx, eIdx)
  const field = areaSelectedColumn.value  // 已经是真实字段名
  return data.slice(min, max + 1).map(r => ({
    id: r.id,
    sampleCode: r.sampleCode,
    value: r[field]
  }))
}

const writeClipboard = (text) => {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'absolute'
  textarea.style.left = '-9999px'
  textarea.style.top = '0'
  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()
  textarea.setSelectionRange(0, textarea.value.length)
  try {
    document.execCommand('copy')
    return true
  } catch {
    return false
  } finally {
    document.body.removeChild(textarea)
  }
}

const copyAreaSelected = async () => {
  const vals = getAreaSelectedValues()
  if (vals.length === 0) {
    showAlertDialog('请先在某一列拖动鼠标选取区域')
    return
  }
  const text = vals.map(v => v.value != null ? String(v.value) : '').join('\n')
  if (writeClipboard(text)) {
    showAlertDialog(`已复制 ${vals.length} 个值`, 'success')
  } else {
    showAlertDialog('复制失败，请手动复制')
  }
}
// ─────────────────────────────────────────────

const onCellClick = ({ row }) => {
  selectSample(row)
}

const onCustomChange = ({ type }) => {
  if (type === 'confirm' || type === 'reset') {
    setTimeout(() => saveGridPrefs(), 50)
  }
}

const onColumnDragStart = () => {
  isColumnDragging.value = true
}

const onColumnDragEnd = () => {
  isColumnDragging.value = false
  setTimeout(() => saveGridPrefs(), 100)
}

const isCardSelected = (item) => {
  return selectedIds.value.includes(item.id)
}

const toggleCardSelect = (item) => {
  const idx = selectedIds.value.indexOf(item.id)
  if (idx > -1) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(item.id)
  }
}

const onCardImgError = (e) => {
  const img = e.target
  const thumb = img?.dataset?.thumb
  if (thumb && !img.src.includes('/thumbnails/')) {
    img.src = '/thumbnails/' + thumb
  }
}

// ── 大图预览滚轮缩放 ──
const onIpWheel = (e) => {
  const delta = e.deltaY > 0 ? -0.15 : 0.15
  ipZoom.value = Math.max(0.3, Math.min(5, +(ipZoom.value + delta).toFixed(2)))
}
const onIpMouseDown = (e) => {
  if (ipZoom.value <= 1) return
  ipDragging.value = true
  ipDragStart.value = { x: e.clientX, y: e.clientY, px: ipPanX.value, py: ipPanY.value }
}
const onIpMouseMove = (e) => {
  if (!ipDragging.value) return
  ipPanX.value = ipDragStart.value.px + (e.clientX - ipDragStart.value.x)
  ipPanY.value = ipDragStart.value.py + (e.clientY - ipDragStart.value.y)
}
const onIpMouseUp = () => { ipDragging.value = false }

// ── 图片拖拽到桌面 ──
const onSampleImgDragStart = (e) => {
  const src = e.target.currentSrc || e.target.src
  if (!src) return
  const fullUrl = src.startsWith('http') ? src : window.location.origin + src
  const fileName = src.split('/').pop().split('?')[0] || 'image.jpg'
  e.dataTransfer.setData('DownloadURL', `image/jpeg:${fileName}:${fullUrl}`)
  e.dataTransfer.effectAllowed = 'copyMove'
}

const onModalImgError = (e) => {
  const img = e.target
  const thumb = img?.dataset?.thumb
  if (thumb && !img.src.includes('/thumbnails/')) {
    img.src = '/thumbnails/' + thumb
  }
}

const formatCardDate = (val) => {
  if (!val) return '-'
  const s = String(val).replace('T', ' ')
  return s
}

const copyCardCode = (code) => {
  writeClipboard(code)
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
    Promise.all([fetchImagesForSample(row.id), loadSampleVideos(row.id)])
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
  if (!result.infringement) result.infringement = '其他'
  // 产品规格/包装规格空值默认显示0
  ;['sampleLength','sampleWidth','sampleHeight','packageLength','packageWidth','packageHeight','cartonLength','cartonWidth','cartonHeight','sampleGrossWeight','sampleNetWeight','innerBoxCount','cartonCapacity','cartonGrossWeight','cartonNetWeight'].forEach(k => {
    if (result[k] == null || result[k] === '') result[k] = '0'
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
    const userName = auth.state.userInfo?.realName || auth.state.userInfo?.username || ''
    if (formMode.value === 'add') {
      payload.registrant = userName
      const res = await api('/samples', { method: 'POST', body: JSON.stringify(payload) })
      if (res.code === 200 || res.id) {
        formMode.value = 'readonly'
        await loadTableData()
      }
    } else if (formMode.value === 'edit') {
      payload.modifier = userName
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

const onThumbMouseEnter = (e, row) => {
  if (showImagePreview.value || showPhotoModal.value) return
  if (!row.thumbnail && !row.firstImageHash) return
  // 优先用原图，缩略图兜底
  const thumbSrc = row.thumbnail ? '/thumbnails/' + row.thumbnail : ''
  const src = row.firstImageHash ? '/images/view/hash/' + row.firstImageHash : thumbSrc
  const rect = e.target.getBoundingClientRect()
  const gap = 12
  const previewSize = 800
  let left = rect.right + gap
  let top = rect.top
  if (left + previewSize > window.innerWidth) {
    left = rect.left - previewSize - gap
  }
  if (top + previewSize > window.innerHeight) {
    top = window.innerHeight - previewSize - gap
  }
  if (left < gap) left = gap
  if (top < gap) top = gap
  // 先清除上一个定时器
  clearTimeout(hoverTimer)
  hoverTimer = setTimeout(() => {
    hoverPreview.src = src
    hoverPreview.fallback = thumbSrc
    hoverPreview.x = left
    hoverPreview.y = top
    hoverPreview.show = true
  }, 300)
}

const onThumbMouseLeave = () => {
  clearTimeout(hoverTimer)
  hoverPreview.show = false
}

const openPhotoModalFor = (row) => {
  selectSample(row)
  photoModalSample.value = row
  photoModalIndex.value = 0
  const temp = {}
  if (row.firstImageHash) temp.hash = row.firstImageHash
  if (row.thumbnail) temp.thumbnailPath = row.thumbnail
  photoModalImages.value = (row.firstImageHash || row.thumbnail) ? [temp] : []
  photoModalInit()
  showPhotoModal.value = true
  fetchPhotoModalImages(row.id)
}

const fetchPhotoModalImages = async (sampleId) => {
  try {
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

const fmt3 = (a, b, c) => {
  if ((a == null || a === '') && (b == null || b === '') && (c == null || c === '')) {
    return '0'
  }
  return [(a != null && a !== '' ? a : '0'), (b != null && b !== '' ? b : '0'), (c != null && c !== '' ? c : '0')].join('x')
}

const startModalEdit = () => {
  const s = photoModalSample.value
  if (!s) return
  Object.keys(editData).forEach(k => delete editData[k])
  Object.assign(editData, {
    sampleName: s.sampleName || '',
    factoryCode: s.factoryCode || '',
    factoryPrice: s.factoryPrice || '',
    taxPrice: s.taxPrice || '',
    packagingCn: s.packagingCn || '',
    innerBoxCount: s.innerBoxCount || '',
    cartonCapacity: s.cartonCapacity || '',
    cartonLength: s.cartonLength || '',
    cartonWidth: s.cartonWidth || '',
    cartonHeight: s.cartonHeight || '',
    cartonGrossWeight: s.cartonGrossWeight || '',
    cartonNetWeight: s.cartonNetWeight || '',
    packageLength: s.packageLength || '',
    packageWidth: s.packageWidth || '',
    packageHeight: s.packageHeight || '',
    packagingEn: s.packagingEn || '',
    sampleLength: s.sampleLength || '',
    sampleWidth: s.sampleWidth || '',
    sampleHeight: s.sampleHeight || '',
    sampleGrossWeight: s.sampleGrossWeight || '',
    sampleNetWeight: s.sampleNetWeight || '',
    cartonVolume: s.cartonVolume || '',
    cartonMaterialVolume: s.cartonMaterialVolume || '',
    batteryInfo: s.batteryInfo || '',
    certification: s.certification || '',
    remark: s.remark || '',
    contact1: s.contact1 || '',
    phone1: s.phone1 || '',
    mobile1: s.mobile1 || '',
    qq: s.qq || ''
  })
  editing.value = true
}

const saveModalEdit = async () => {
  const s = photoModalSample.value
  if (!s) return
  try {
    const payload = { ...editData, id: s.id, modifier: auth.state.userInfo?.realName || auth.state.userInfo?.username || '' }
    const res = await api(`/samples/${s.id}`, { method: 'PUT', body: JSON.stringify(payload) })
    if (res.code === 200 || res.id) {
      Object.assign(s, editData)
      editing.value = false
      await loadTableData()
    }
  } catch (e) {
    console.error(e)
  }
}

const cancelModalEdit = () => {
  editing.value = false
}

const closePhotoModal = () => {
  showPhotoModal.value = false
  editing.value = false
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
  // 点击表格外部清除选区
  if (areaSelectedColumn.value && tableWrapRef.value && !tableWrapRef.value.contains(e.target)) {
    // 如果点击在导入预览内，不清主表选区
    if (!importPreviewWrapRef.value || !importPreviewWrapRef.value.contains(e.target)) {
      clearAreaSelection()
    }
  }
  // 导入预览选区清除
  importArea.onDocClick(e)
}

const batchSetPrice = () => {
  showMoreDropdown.value = false
  showToast('批量设置价格功能开发中', 'info')
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

// 在新标签页中打开报表设计器，避免覆盖主页面
const openReportDesigner = () => {
  showMoreDropdown.value = false
  const url = router.resolve({ name: 'ReportDesigner' }).href
  window.open(url, '_blank')
}

const finishProgress = () => {
  reportModalProgress.value = 100
  if (reportModalProgressTimer) {
    clearInterval(reportModalProgressTimer)
    reportModalProgressTimer = null
  }
}

let reportModalStartTime = 0

const closeReportModal = () => {
  showReportModal.value = false
  reportModalUrl.value = ''
  reportModalLoading.value = false
  reportModalProgress.value = 0
  if (reportModalProgressTimer) {
    clearInterval(reportModalProgressTimer)
    reportModalProgressTimer = null
  }
  if (paginationObserver) {
    paginationObserver.disconnect()
    paginationObserver = null
  }
  if (contentReadyObserver) {
    contentReadyObserver.disconnect()
    contentReadyObserver = null
  }
}

let paginationObserver = null
let reportModalProgressTimer = null
let contentReadyObserver = null

const onReportIframeLoad = () => {
  const elapsed = ((Date.now() - reportModalStartTime) / 1000).toFixed(1)
  try {
    const frame = document.getElementById('reportIframe')
    if (!frame) return
    const doc = frame.contentDocument || frame.contentWindow.document
    if (!doc) {
      reportModalLoading.value = false
      return
    }
    const checkContentReady = () => {
      const table = doc.querySelector('table')
      if (table && table.rows && table.rows.length > 1) {
        reportModalLoading.value = false
        finishProgress()
        if (contentReadyObserver) {
          contentReadyObserver.disconnect()
          contentReadyObserver = null
        }
        return true
      }
      return false
    }
    contentReadyObserver = new MutationObserver(() => { checkContentReady() })
    contentReadyObserver.observe(doc.body, { childList: true, subtree: true })
    checkContentReady()
    setTimeout(() => {
      if (reportModalLoading.value) {
        reportModalLoading.value = false
        finishProgress()
        if (contentReadyObserver) {
          contentReadyObserver.disconnect()
          contentReadyObserver = null
        }
      }
    }, 10000)
    const style = doc.createElement('style')
    style.textContent = `
      div[class*="search"], div[class*="query"], div[class*="filter"],
      div[class*="Search"], div[class*="Query"], div[class*="Filter"],
      [class*="-search-"], [class*="-query-"],
      form[class*="search"], form[class*="query"] {
        display: none !important;
      }
    `
    doc.head.appendChild(style)
    const findAndHide = () => {
      const w = doc.createTreeWalker(doc.body, NodeFilter.SHOW_TEXT)
      while (w.nextNode()) {
        if ((w.currentNode.textContent||'').trim()==='首页') {
          let p = w.currentNode.parentElement
          for (let i=0; i<8 && p && p!==doc.body; i++) {
            if (p.offsetHeight>15 && p.offsetHeight<120 && p.offsetWidth>60) {
              let pp=p.parentElement
              if (pp && pp.offsetHeight<100 && pp.offsetHeight>15) p=pp
              pp=p.parentElement
              if (pp && pp.offsetHeight<120 && pp.offsetHeight>15) p=pp
              p.style.display='none'
              return true
            }
            p=p.parentElement
          }
        }
      }
      return false
    }
    findAndHide()
    paginationObserver = new MutationObserver(() => { if (findAndHide()) { paginationObserver?.disconnect(); paginationObserver=null } })
    paginationObserver.observe(doc.body, { childList:true, subtree:true })
    const containerEl = frame.parentElement
    const bodyWidth = doc.body.scrollWidth || doc.documentElement.scrollWidth
    if (containerEl && bodyWidth > 0) {
      const containerWidth = containerEl.clientWidth - 4
      const ratio = Math.min(1, containerWidth / bodyWidth)
      if (ratio < 1) {
        const zoomStyle = doc.createElement('style')
        zoomStyle.textContent = `
          body { zoom: ${ratio}; -moz-transform: scale(${ratio}); -moz-transform-origin: top left; }
          @media print {
            body { zoom: 1 !important; -moz-transform: none !important; }
            @page { size: landscape; margin: 10mm; }
            * { overflow: visible !important; }
          }
        `
        doc.head.appendChild(zoomStyle)
        frame.style.height = `${containerEl.clientHeight / ratio + 60}px`
      }
    }
  } catch (e) {}
}

const doReportPrint = () => {
  const frame = document.getElementById("reportIframe")
  if (!frame) return
  try {
    const doc = frame.contentDocument || frame.contentWindow.document
    if (!doc || !doc.body) { frame.contentWindow.print(); return }
    const savedFrameW = frame.style.width
    const savedFrameH = frame.style.height
    const savedBodyZoom = doc.body.style.zoom
    const savedBodyTransform = doc.body.style.transform
    const removedStyles = []
    doc.querySelectorAll('style').forEach(s => {
      if (s.textContent && /zoom|transform.*scale/.test(s.textContent)) {
        removedStyles.push(s)
        s.remove()
      }
    })
    const fullW = doc.body.scrollWidth || doc.documentElement.scrollWidth || 1500
    const fullH = doc.body.scrollHeight || doc.documentElement.scrollHeight || 2000
    frame.style.width = (fullW + 40) + 'px'
    frame.style.height = (fullH + 40) + 'px'
    doc.body.style.zoom = '1'
    doc.body.style.transform = 'none'
    const ps = doc.createElement('style')
    ps.id = 'print-temp'
    ps.textContent = `@media print{@page{size:landscape;margin:8mm}*{overflow:visible!important}}`
    doc.head.appendChild(ps)
    setTimeout(() => {
      frame.contentWindow.focus()
      frame.contentWindow.print()
      setTimeout(() => {
        frame.style.width = savedFrameW || ''
        frame.style.height = savedFrameH || ''
        doc.body.style.zoom = savedBodyZoom
        doc.body.style.transform = savedBodyTransform
        removedStyles.forEach(s => doc.head.appendChild(s))
        const t = doc.getElementById('print-temp'); if (t) t.remove()
      }, 1500)
    }, 300)
  } catch(e) {
    frame.contentWindow?.print()
  }
}

const onReportEscKey = (e) => {
  if (e.key === 'Escape' && showReportModal.value) {
    closeReportModal()
  }
}

let areaCopyTextarea = null

const onAreaCopyKey = (e) => {
  if (!(e.ctrlKey || e.metaKey) || e.key !== 'c') return
  if (!areaSelectedColumn.value) return
  const vals = getAreaSelectedValues()
  if (vals.length === 0) return
  const text = vals.map(v => v.value != null ? String(v.value) : '').join('\n')
  areaCopyTextarea = document.createElement('textarea')
  areaCopyTextarea.value = text
  areaCopyTextarea.style.position = 'absolute'
  areaCopyTextarea.style.left = '-9999px'
  areaCopyTextarea.style.top = '0'
  document.body.appendChild(areaCopyTextarea)
  areaCopyTextarea.focus()
  areaCopyTextarea.select()
  areaCopyTextarea.setSelectionRange(0, areaCopyTextarea.value.length)
}

const onAreaCopyEvent = (e) => {
  if (!areaSelectedColumn.value) return
  const vals = getAreaSelectedValues()
  if (vals.length === 0) return
  const text = vals.map(v => v.value != null ? String(v.value) : '').join('\n')
  e.clipboardData.setData('text/plain', text)
  e.preventDefault()
  if (areaCopyTextarea && document.body.contains(areaCopyTextarea)) {
    document.body.removeChild(areaCopyTextarea)
    areaCopyTextarea = null
  }
}

const openVendorConfirmModal = () => {
  initVcFields()
  showVendorConfirmModal.value = true
}

const printTable = () => {
  showMoreDropdown.value = false
  window.print()
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

// 翻译勾选行的中文包装 + 样品名称 → 英文（覆盖写）
const batchTranslateSelected = async () => {
  const selected = importPreviewAllRows.value.filter(r => importSelectedRowIndexes.value.has(r._rowIndex))
  if (selected.length === 0) return

  // 收集需要翻译的文本：样品名称 + 中文包装
  const nameTexts = []
  const nameTargets = []
  const pkgTexts = []
  const pkgTargets = []
  selected.forEach(row => {
    const name = (row.sampleName || '').trim()
    if (name) { nameTexts.push(name); nameTargets.push(row) }
    const pkg = (row.packagingCn || '').trim()
    if (pkg) { pkgTexts.push(pkg); pkgTargets.push(row) }
  })
  const allTexts = [...nameTexts, ...pkgTexts]
  if (allTexts.length === 0) { showToast('所选行没有可翻译的内容', 'warning'); return }

  try {
    const translated = await baiduTranslateBatch(allTexts)
    if (translated && translated.length === allTexts.length) {
      let k = 0
      nameTargets.forEach(row => { row.englishName = translated[k++] })
      pkgTargets.forEach(row => { row.packagingEn = translated[k++] })
      selected.forEach(row => {
        const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
        if (idx >= 0) importPreviewAllRows.value.splice(idx, 1, markRaw({ ...row }))
      })
      syncPreviewPage()
      const count = nameTexts.length + pkgTexts.length
      showToast(`已翻译 ${count} 条（样品名称${nameTexts.length} + 包装${pkgTexts.length}）`, 'success')
    }
  } catch (e) {
    showToast('翻译失败，请稍后重试', 'error')
  }
}

const importRowClassName = ({ row }) => {
  if (row._status === 'cat_error') return 'import-row-cat-error'
  if (row._status === 'pkg_warning') return 'import-row-pkg-warning'
  if (row._status === 'dup_warning') return 'import-row-dup-warning'
  return ''
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

// 修改外箱规格时自动计算材积和体积（编辑弹窗）
function recalcEditCartonVol() {
  const cl = Number(editData.cartonLength) || 0
  const cw = Number(editData.cartonWidth) || 0
  const ch = Number(editData.cartonHeight) || 0
  if (cl > 0 && cw > 0 && ch > 0) {
    editData.cartonVolume = Number((cl * cw * ch / 1000000).toFixed(2))
    editData.cartonMaterialVolume = Number((cl * cw * ch / 28339.2).toFixed(2))
  } else if (cl === 0 && cw === 0 && ch === 0) {
    editData.cartonVolume = 0
    editData.cartonMaterialVolume = 0
  }
}
function onEditCartonInput() {
  nextTick(recalcEditCartonVol)
}

// 修改外箱规格时自动计算材积和体积（顶部表单卡片）
const autoCalcKeys = ['cartonLength', 'cartonWidth', 'cartonHeight']
function recalcCartonVol() {
  const cl = Number(formData.cartonLength) || 0
  const cw = Number(formData.cartonWidth) || 0
  const ch = Number(formData.cartonHeight) || 0
  if (cl > 0 && cw > 0 && ch > 0) {
    formData.cartonVolume = Number((cl * cw * ch / 1000000).toFixed(2))
    formData.cartonMaterialVolume = Number((cl * cw * ch / 28339.2).toFixed(2))
  } else if (cl === 0 && cw === 0 && ch === 0) {
    formData.cartonVolume = 0
    formData.cartonMaterialVolume = 0
  }
}
function onGroupInput(key) {
  if (autoCalcKeys.includes(key)) nextTick(recalcCartonVol)
}

// 导入预览打开/关闭时注册/销毁区域选取
let importAreaSetupDone = false
watch(showImportPreview, (v) => {
  if (v && !importAreaSetupDone) {
    importArea.setup()
    importAreaSetupDone = true
  }
  if (!v && importAreaSetupDone) {
    importArea.cleanup()
    importAreaSetupDone = false
  }
})

onMounted(() => {
  const route = useRoute()
  if (route.params.manufacturerCode) {
    manufacturerCode.value = route.params.manufacturerCode
  } else if (route.query.manufacturerCode) {
    manufacturerCode.value = route.query.manufacturerCode
  }
  document.addEventListener('click', closeDropdowns)
  window.addEventListener('keydown', onReportEscKey)
  window.addEventListener('keydown', onAreaCopyKey, true)
  document.addEventListener('copy', onAreaCopyEvent, true)
  document.addEventListener('mousedown', onTableWrapMouseDown, true)
  if (tableWrapRef.value) {
    // 虚拟滚动后重新挂选区把手
    let handleScrollTimer = null
    tableWrapRef.value.addEventListener('scroll', () => {
      if (!areaHandleEl || !document.contains(areaHandleEl)) {
        if (handleScrollTimer) clearTimeout(handleScrollTimer)
        handleScrollTimer = setTimeout(attachAreaHandle, 150)
      }
    }, { passive: true })
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

watch(() => route.path, () => {
  showPhotoModal.value = false
  editing.value = false
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
  showPhotoModal.value = false
  editing.value = false
  document.removeEventListener('click', closeDropdowns)
  window.removeEventListener('keydown', onReportEscKey)
  window.removeEventListener('keydown', onAreaCopyKey, true)
  document.removeEventListener('copy', onAreaCopyEvent, true)
  document.removeEventListener('mousedown', onTableWrapMouseDown, true)
  document.removeEventListener('mousemove', onDocMouseMove)
  document.removeEventListener('mouseup', onDocMouseUp)
  if (tableWrapRef.value) {
  }
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

/* 主表格 body 字号 */
:deep(.vxe-grid:not(#deletedGrid) .vxe-body--column .vxe-cell) {
  font-size: 26px !important;
}

/* 已删除表格表头底部边框 */
:deep(#deletedGrid .vxe-header--column) {
  border-bottom: 2px solid #d0d5dd !important;
}

/* 已删除表格分页器字体加大 */
:deep(#deletedGrid .vxe-pager) {
  font-size: 18px !important;
}
:deep(#deletedGrid .vxe-pager .vxe-pager--goto-btn),
:deep(#deletedGrid .vxe-pager .vxe-pager--prev-btn),
:deep(#deletedGrid .vxe-pager .vxe-pager--next-btn),
:deep(#deletedGrid .vxe-pager .vxe-pager--num-btn),
:deep(#deletedGrid .vxe-pager .vxe-pager--jump-prev),
:deep(#deletedGrid .vxe-pager .vxe-pager--jump-next) {
  font-size: 18px !important;
  min-width: 40px !important;
  height: 40px !important;
  line-height: 40px !important;
}
:deep(#deletedGrid .vxe-pager .vxe-select) {
  font-size: 18px !important;
}


/* 列管理按钮左移 */
:deep(.vxe-toolbar-custom-target) {
  margin-right: 5px;
}


.sample-btn-card-toggle {
  display: inline-flex !important;
  align-items: center;
  gap: 5px;
  height: 44px !important;
  min-height: 44px !important;
  padding: 0 18px !important;
  font-size: 18px !important;
  margin-right: 6px;
  margin-left: 9px;
  border-radius: 8px;
  border: 1px solid rgba(0,122,255,0.15);
  background: rgba(0,122,255,0.04);
  color: #007aff;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.sample-btn-card-toggle:hover {
  background: rgba(0,122,255,0.10);
  border-color: rgba(0,122,255,0.3);
}

/* 卡片覆盖层：绝对定位在 table-card 内，不影响布局 */
.sample-card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 56px;
  z-index: 10;
  overflow-y: auto;
  background: #f7f8fa;
}

.sample-card-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  padding: 24px;
  align-content: start;
}

.sample-card-item {
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
}

.sample-card-item:hover {
  border-color: rgba(0,122,255,0.3);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  transform: translateY(-3px);
}

.sample-card-img {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f7f8fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sample-card-item.card-selected {
  border-color: #007aff;
  box-shadow: 0 0 0 3px rgba(0,122,255,0.15);
}

.card-checkbox {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 5;
  width: 26px;
  height: 26px;
  border-radius: 4px;
  border: 2px solid rgba(255,255,255,0.7);
  background: rgba(0,0,0,0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: all 0.15s ease;
}
.card-checkbox:hover {
  border-color: #fff;
  background: rgba(0,0,0,0.45);
}
.card-checkbox.checked {
  border-color: #007aff;
  background: #007aff;
}

.sample-card-img img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  background: #f7f8fa;
}

.sample-card-no-img {
  color: #d1d5db;
}

.sample-card-body {
  padding: 16px 18px 18px;
}

.sample-card-name {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.35;
  margin-bottom: 12px;
}

.sample-card-fields {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px 10px;
  align-items: start;
  margin-top: 6px;
}

.card-val {
  font-size: 30px;
  color: #000;
  font-family: "SimSun", "宋体", serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.card-code {
  color: #007aff;
}

.card-val-copy {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-copy-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 3px;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  padding: 0;
  transition: color 0.15s, background 0.15s;
}

.card-copy-btn:hover {
  color: #007aff;
  background: rgba(0,122,255,0.08);
}

.card-price {
  font-size: 28px;
  font-weight: 700;
  color: #e11d48;
}

.sample-card-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 8px 0;
}

/* 厂商确认表全屏模态框 */
.report-modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.35); z-index: 99999;
  display: flex; align-items: center; justify-content: center;
  animation: reportModalFadeIn 0.2s ease;
}
@keyframes reportModalFadeIn {
  from { opacity: 0; } to { opacity: 1; }
}
.report-modal-container {
  width: 95vw; height: 92vh; max-width: 1600px;
  background: #fff; border-radius: 12px; overflow: hidden;
  display: flex; flex-direction: column;
  box-shadow: 0 8px 40px rgba(0,0,0,0.22);
  animation: reportModalSlideIn 0.25s ease;
}
@keyframes reportModalSlideIn {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.report-modal-header {
  height: 48px; background: #fff; border-bottom: 1px solid #e8e8e8;
  display: flex; align-items: center; padding: 0 16px; flex-shrink: 0;
}
.report-modal-title {
  font-size: 16px; font-weight: 600; color: #1a1a1a; margin-right: auto;
}
.report-modal-actions { display: flex; gap: 8px; }
.report-modal-btn {
  height: 32px; padding: 0 14px; border: 1px solid #d9d9d9; border-radius: 6px;
  background: #fff; color: #333; font-size: 13px; cursor: pointer;
  display: inline-flex; align-items: center; gap: 5px; transition: all 0.15s;
}
.report-modal-btn:hover { border-color: #1677ff; color: #1677ff; }
.report-modal-body {
  flex: 1; position: relative; overflow: auto;
}
.report-modal-iframe {
  width: 100%; height: 100%; border: none;
}
.report-modal-loading {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: #fff; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 12px;
  color: #999; font-size: 14px; z-index: 2;
}
.report-modal-spinner {
  width: 32px; height: 32px; border: 3px solid #e8e8e8;
  border-top-color: #1677ff; border-radius: 50%;
  animation: reportSpin 0.8s linear infinite;
}
@keyframes reportSpin { to { transform: rotate(360deg); } }
.report-modal-progress {
  width: 200px; height: 6px; background: #e8e8e8; border-radius: 3px; overflow: hidden;
}
.report-modal-progress-bar {
  height: 100%; background: linear-gradient(90deg, #1677ff, #40a9ff);
  border-radius: 3px; transition: width 0.3s ease;
}
.report-modal-progress-text {
  font-size: 12px; color: #666; min-width: 40px; text-align: center;
}

/* 模板选择列表 */
.tpl-select-item {
  padding: 20px 24px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.tpl-select-item:hover {
  border-color: #007aff;
  background: rgba(0,122,255,0.04);
}
.tpl-select-item.selected {
  border-color: #007aff;
  background: rgba(0,122,255,0.08);
}
.tpl-select-title {
  font-size: 22px;
  font-weight: 600;
  color: #1d1d1f;
}
.tpl-select-date {
  font-size: 16px;
  color: #999;
  margin-top: 6px;
}

/* Toast */
.sr-toast {
  position: fixed; top: 60px; left: 50%; transform: translateX(-50%); z-index: 100010;
  padding: 10px 24px; border-radius: 6px; font-size: 13px; color: #fff; white-space: nowrap;
  box-shadow: 0 4px 16px rgba(0,0,0,.15);
  pointer-events: none;
}
.sr-toast.success { background: #16a34a; }
.sr-toast.error { background: #e53e3e; }
.sr-toast.warn { background: #ea8c00; }
.sr-toast.info { background: #3a6ff6; }
.toast-fade-enter-active, .toast-fade-leave-active { transition: opacity .25s; }
.toast-fade-enter-from, .toast-fade-leave-to { opacity: 0; }

/* ===== 综合查询面板 ===== */
.adv-search-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.30); z-index: 99999;
  display: flex; align-items: center; justify-content: center;
}

.adv-search-panel {
  width: 90vw; max-width: 1600px; max-height: 78vh;
  background: #fff; border-radius: 28px;
  box-shadow: 0 32px 64px rgba(0,0,0,0.18);
  display: flex; flex-direction: column; overflow: hidden;
}

.adv-search-body {
  flex: 1; overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px 36px;
  padding: 40px 48px;
}

.adv-field {
  display: flex; flex-direction: column; gap: 12px;
}

.adv-field label {
  font-size: 26px; font-weight: 600; color: #374151;
}

.adv-field input,
.adv-field select {
  width: 100%; height: 56px; padding: 0 18px;
  border: 1px solid #d1d5db; border-radius: 10px;
  font-size: 22px; outline: none; background: #fff;
  box-sizing: border-box;
  transition: border-color 0.15s;
}

.adv-field input:focus,
.adv-field select:focus {
  border-color: #007aff;
  box-shadow: 0 0 0 3px rgba(0,122,255,0.1);
}

.adv-field-range,
.adv-field-range-unit {
  grid-column: span 1;
}

.adv-field-checks {
  grid-column: span 1;
}

.range-inputs {
  display: flex; align-items: stretch; gap: 16px;
}

.range-inputs input {
  flex: 1; min-width: 0;
}

.range-inputs span {
  color: #9ca3af; font-size: 22px; flex-shrink: 0; align-self: center;
}

.range-inputs .unit {
  color: #6b7280; font-size: 20px; white-space: nowrap;
}

.check-group {
  display: flex; gap: 20px; align-items: center; padding-top: 4px;
}

.chk-item {
  display: flex; align-items: center; gap: 8px;
  font-size: 22px; font-weight: 500; color: #374151; cursor: pointer;
}

.chk-item input[type="checkbox"] {
  width: 24px; height: 24px; cursor: pointer;
}

.adv-search-footer {
  display: flex; align-items: center; gap: 16px;
  padding: 28px 48px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}

.adv-search-footer .csd-btn {
  height: 56px !important;
  min-height: 56px !important;
  font-size: 22px !important;
  font-weight: 600 !important;
  padding: 0 24px !important;
}

/* 综合查询日期选择器 */
.adv-field-range :deep(.sdp-input) {
  flex: 1; min-width: 0;
  height: 56px;
  font-size: 22px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  user-select: none;
  box-sizing: border-box;
}
.adv-field-range :deep(.sdp-placeholder) { color: #999; flex: 1; }
.adv-field-range :deep(.sdp-value) { color: #007aff; flex: 1; }
.adv-field-range :deep(.sdp-icon) {
  width: 24px; height: 24px;
  color: #999;
  flex-shrink: 0;
}
.adv-field-range :deep(.sdp-clear) {
  width: 22px; height: 22px;
  color: #ccc;
  flex-shrink: 0;
  cursor: pointer;
}
.adv-field-range :deep(.sdp-clear:hover) { color: #999; }

/* ========== 对照资料管理弹窗 ========== */
.ref-modal { background: #fff; border-radius: 14px; width: 1200px; max-width: 96vw; padding: 28px; height:calc(86vh - 600px); max-height:calc(86vh - 600px); display: flex; flex-direction: column; box-shadow: 0 20px 60px rgba(0,0,0,.18); }
.ref-modal-header { display: flex; align-items: center; gap: 16px; margin-bottom: 14px; flex-wrap: wrap; }
.ref-modal-header strong { font-size: 24px; font-weight: 700; flex-shrink: 0; }
.ref-modal-body { flex: 1; overflow: hidden; min-height: 0; }
.ref-panel { display: flex; flex-direction: column; gap: 16px; }
.ref-panel-toolbar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.ref-tabs { display: flex; gap: 4px; background: #f1f5f9; border-radius: 10px; padding: 5px; margin-left: auto; }
.ref-tab { padding: 10px 22px; border-radius: 8px; border: none; background: transparent; font-size: 20px; cursor: pointer; transition: all .15s; color: #64748b; }
.ref-tab.active { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,.08); font-weight: 600; color: #1e293b; }
.ref-search-box { display: flex; align-items: center; gap: 6px; background: #f1f5f9; border-radius: 8px; padding: 8px 16px; }
.ref-search-box input { border: none; background: transparent; outline: none; font-size: 22px; width: 220px; }
.ref-filter-select { border: 1px solid #d1d5db; border-radius: 8px; padding: 8px 14px; font-size: 22px; background: #fff; }
.rf-tag { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 14px; font-weight: 500; }
.rf-tag-l1 { background: #dbeafe; color: #1d4ed8; }
.rf-tag-l2 { background: #fce7f3; color: #be185d; }
.ref-action-btn { padding: 14px 22px; border: 1px solid #e2e8f0; border-radius: 8px; background: #fff; cursor: pointer; display: inline-flex; align-items: center; transition: all .12s; font-size: 22px; }
.ref-action-btn:hover { background: #f1f5f9; border-color: #cbd5e1; }
.ref-action-btn.danger:hover { background: #fef2f2; border-color: #fecaca; color: #dc2626; }
.ref-inline-input { width: 100%; padding: 6px 10px; border: 1px solid transparent; border-radius: 6px; font-size: 20px; background: transparent; outline: none; transition: border-color .15s; }
.ref-inline-input:hover { border-color: #e2e8f0; }
.ref-inline-input:focus { border-color: #3b82f6; background: #fff; }
.ref-form-modal { background: #fff; border-radius: 14px; width: 600px; max-width: 92vw; padding: 28px; box-shadow: 0 16px 48px rgba(0,0,0,.15); }
.ref-form-row { display: flex; flex-direction: column; gap: 6px; margin-bottom: 14px; }
.ref-form-row label { font-size: 22px; font-weight: 600; color: #374151; }
.ref-form-row input, .ref-form-row select { border: 1px solid #d1d5db; border-radius: 8px; padding: 10px 14px; font-size: 22px; transition: border-color .15s; }
.ref-form-row input:focus, .ref-form-row select:focus { border-color: #007aff; outline: none; box-shadow: 0 0 0 3px rgba(0,122,255,.1); }
.ref-modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.ref-required { color: #ef4444; }

/* 树形表格内一级类目行加粗 */
.ref-panel :deep(.vxe-table--body .row--level-1) { font-weight: 600; color: #1e293b; }
.ref-panel :deep(.vxe-table--body .row--level-2) { color: #475569; }
.ref-panel :deep(.vxe-tree-node-wrapper) { padding-left: 6px !important; }
.ref-panel :deep(.vxe-table--body .vxe-cell) { white-space: nowrap; }
.ref-panel :deep(.vxe-tree-cell) { white-space: nowrap; }

.cursor-pointer { cursor: pointer; }

/* 导入预览行颜色 */
.import-preview-table-wrap :deep(.import-row-cat-error) { background-color: #ffebee !important; }
.import-preview-table-wrap :deep(.import-row-cat-error:hover) { background-color: #ffcdd2 !important; }
.import-preview-table-wrap :deep(.import-row-pkg-warning) { background-color: #fff8e1 !important; }
.import-preview-table-wrap :deep(.import-row-pkg-warning:hover) { background-color: #ffecb3 !important; }
.import-preview-table-wrap :deep(.import-row-dup-warning) { background-color: #e8f5e9 !important; }
.import-preview-table-wrap :deep(.import-row-dup-warning:hover) { background-color: #c8e6c9 !important; }

/* 横向滚动优化 */
:deep(.vxe-table--body-wrapper) {
  will-change: scroll-position;
  overscroll-behavior-x: contain;
}
:deep(.vxe-table--body th), :deep(.vxe-table--body td) {
  contain: layout style;
}
.import-preview-pager { display:flex; align-items:center; justify-content:center; gap:6px; padding:8px 0; border-top:1px solid #e2e8f0; }
.import-preview-size-select { padding:3px 6px; border:1px solid #e2e8f0; border-radius:4px; font-size:12px; outline:none; }

/* 导入解析进度条 */
.import-parse-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 28px 16px;
}
.import-parse-icon { color: #007aff; }
.import-parse-stage {
  font-size: 14px;
  color: #1d1d1f;
  font-weight: 500;
}
.import-parse-bar-track {
  width: 100%;
  max-width: 320px;
  height: 8px;
  background: #e8e8e8;
  border-radius: 4px;
  overflow: hidden;
}
.import-parse-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #007aff, #40a9ff);
  border-radius: 4px;
  transition: width 0.3s ease;
}
.import-parse-pct {
  font-size: 13px;
  color: #86868b;
}

.spin { animation: importSpin 0.8s linear infinite; }
@keyframes importSpin { 100% { transform: rotate(360deg); } }

/* 列区域选取时禁用文本选中 */
.sample-area-selecting *, .import-area-selecting * { user-select: none !important; -webkit-user-select: none !important; }

/* 列区域选中单元格高亮 */
.area-selected-cell { background: #dceefb !important; outline: 1px solid #007aff; outline-offset: -1px; }

/* 批量导入图片 - 厂商关联信息 */
.batch-manufacturer-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  padding: 14px 20px;
  background: #eef6ff;
  border: 1px solid #b3d8ff;
  border-radius: 8px;
  font-size: 20px;
}
.bm-info-label {
  font-weight: 600;
  color: #007aff;
  margin-right: 2px;
  font-size: 22px;
}
.bm-info-value {
  color: #333;
  font-size: 20px;
}
.bm-info-value strong {
  color: #1d1d1f;
}
.bm-info-hint {
  width: 100%;
  color: #999;
  font-size: 18px;
  margin-top: 2px;
}

/* 货号重复冲突解决模态框 */
.batch-conflict-modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: flex;
  align-items: center; justify-content: center; z-index: 100001;
}
.batch-conflict-modal {
  background: #fff; border-radius: 12px; width: 1200px; max-height: 92vh;
  display: flex; flex-direction: column; box-shadow: 0 8px 40px rgba(0,0,0,0.15);
}
.batch-conflict-modal-header {
  display: flex; align-items: center; padding: 16px 20px; border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}
.batch-conflict-modal-header strong {
  font-size: 15px; color: #1d1d1f;
}
.batch-conflict-modal-body {
  flex: 1; overflow-y: auto; padding: 16px 20px;
}
.batch-conflict-group {
  margin-bottom: 16px;
}
.batch-conflict-group-header {
  display: flex; align-items: center; gap: 12px;
  padding: 8px 0 6px;
}
.batch-conflict-upload-img {
  width: 64px; height: 64px; object-fit: cover; border-radius: 6px;
  border: 2px solid #d4380d; flex-shrink: 0; cursor: pointer;
}
.batch-conflict-code-label {
  font-size: 14px; color: #333;
}
.batch-conflict-code-label strong {
  color: #d4380d;
}
.batch-conflict-count {
  font-size: 13px; color: #999;
}
.batch-conflict-remove-btn {
  display: flex; align-items: center; gap: 4px;
  padding: 4px 10px; border: 1px solid #ffccc7; border-radius: 4px;
  background: #fff2f0; color: #cf1322; font-size: 12px; cursor: pointer;
  flex-shrink: 0;
}
.batch-conflict-remove-btn:hover {
  background: #ffd8d2; border-color: #ffa39e;
}
.batch-conflict-table {
  margin-top: 6px;
}
.conflict-img-preview-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.75);
  display: flex; align-items: center; justify-content: center;
  z-index: 100002;
}
.conflict-img-preview-close {
  position: absolute; top: 16px; right: 16px;
  background: none; border: none; color: #fff; cursor: pointer;
  opacity: 0.8; transition: opacity 0.15s;
}
.conflict-img-preview-close:hover { opacity: 1; }
.conflict-validation-msg {
  color: #d4380d; font-size: 13px; font-weight: 600;
  margin-right: auto;
}

/* ── 大图预览滚轮缩放 ── */
.ip-main img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  user-select: none;
  -webkit-user-drag: none;
}
.ip-zoom-bar {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
}
.ip-zoom-label {
  font-size: 11px;
  color: rgba(29,29,31,0.45);
  min-width: 36px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}
.ip-zoom-btn {
  width: 26px;
  height: 22px;
  border: none;
  border-radius: 4px;
  background: rgba(0,0,0,0.06);
  color: rgba(29,29,31,0.55);
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}
.ip-zoom-btn:hover {
  background: rgba(0,0,0,0.12);
  color: #1d1d1f;
}

/* ── 上传锁按钮 ── */
.ip-lock-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 28px;
  padding: 0 10px;
  border: 1px solid rgba(0,0,0,0.1);
  border-radius: 14px;
  background: #fff;
  color: rgba(29,29,31,0.45);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-right: 4px;
  white-space: nowrap;
}
.ip-lock-btn::after {
  content: '锁定';
  font-weight: 500;
}
.ip-lock-btn:hover {
  border-color: rgba(0,0,0,0.18);
  background: #f5f5f7;
  color: rgba(29,29,31,0.65);
}
.ip-lock-btn.locked {
  border-color: rgba(255,59,48,0.25);
  background: rgba(255,59,48,0.06);
  color: #ff3b30;
}
.ip-lock-btn.locked::after {
  content: '已锁';
}
.ip-lock-btn.locked:hover {
  background: rgba(255,59,48,0.12);
  border-color: rgba(255,59,48,0.35);
}
</style>

<style>
.image-preview-overlay {
  background: transparent !important;
}
</style>

<style scoped>
/* ── sample-photo-modal 模态框样式（独立,不依赖共享 spm-*） ── */

.sample-photo-modal {
  position: fixed;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  background: #f5f6f8;
  border-radius: 16px;
  box-shadow: 0 24px 80px rgba(0,0,0,0.30), 0 4px 20px rgba(0,0,0,0.12), inset 0 1px 0 rgba(255,255,255,0.8);
  font-size: 13px;
  color: #1d1d1f;
  user-select: none;
  overflow: hidden !important;
}

.spm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 18px;
  border-radius: 16px 16px 0 0;
  background: linear-gradient(180deg, #fff, #f7f9fc);
  border-bottom: 1px solid rgba(0,122,255,0.10);
  cursor: move;
  flex-shrink: 0;
}
.spm-header-title {
  font-size: 24px;
  font-weight: 720;
  letter-spacing: -0.01em;
}
.spm-header-close {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: rgba(29,29,31,0.40);
  font-size: 18px;
  cursor: pointer;
  transition: all 0.15s;
}
.spm-header-close:hover {
  background: rgba(255,59,48,0.10);
  color: #ff3b30;
}

.spm-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 1px;
  background: #fff;
  overflow: hidden;
}
.spm-body-main {
  display: flex;
  flex: 1;
  min-height: 0;
  gap: 1px;
}

.spm-top-card {
  display: flex;
  gap: 24px;
  padding: 20px 28px;
  background: #fff;
  border-bottom: 1px solid #e2e4ea;
  flex-wrap: wrap;
  flex-shrink: 0;
}
.spm-top-card-field {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.spm-top-card-field span {
  font-size: 14px;
  color: rgba(29,29,31,0.46);
  font-weight: 500;
}
.spm-top-card-field strong {
  font-size: 18px;
  color: #1d1d1f;
  font-weight: 700;
}
.spm-top-card-field strong.spm-price {
  color: #e53e3e;
}

.spm-body-left {
  width: 1280px;
  min-width: 1280px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;
  padding: 14px;
  gap: 10px;
  flex-shrink: 0;
}

.spm-main-img-wrap {
  width: 1200px;
  height: 900px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border-radius: 10px;
  overflow: hidden;
  cursor: grab;
  position: relative;
}
.spm-main-img-wrap img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  user-select: none;
  -webkit-user-drag: none;
}

.spm-main-img-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(0,0,0,0.45);
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.15s;
}
.spm-main-img-wrap:hover .spm-main-img-nav { opacity: 1; }
.spm-main-img-prev { left: 8px; }
.spm-main-img-next { right: 8px; }

.spm-thumb-strip {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  padding: 3px 0;
  flex-shrink: 0;
}
.spm-thumb-strip::-webkit-scrollbar { height: 4px; }
.spm-thumb-strip::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(0,122,255,0.18);
}

.spm-thumb-item {
  flex-shrink: 0;
  width: 80px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  background: #eee;
  transition: all 0.15s;
  position: relative;
}
.spm-thumb-item.active {
  border-color: #007aff;
  box-shadow: 0 0 0 2px rgba(0,122,255,0.15);
}
.spm-thumb-item:hover:not(.active) {
  border-color: rgba(0,122,255,0.35);
}
.spm-thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  pointer-events: none;
}

.spm-body-right {
  flex: 1;
  min-width: 340px;
  background: #fff;
  padding: 80px 22px;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.spm-field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
}

.spm-field {
  display: flex;
  align-items: baseline;
  gap: 30px;
  padding: 8px 14px;
  border-bottom: 1px solid #f0f2f5;
  line-height: 1.4;
}
.spm-field:nth-child(odd) {
  border-right: 1px solid #f0f2f5;
}
.spm-field.spm-field-full {
  grid-column: 1 / -1;
  border-right: none !important;
}

.spm-field-label {
  width: 100px;
  font-size: 24px;
  color: rgba(29,29,31,0.46);
  white-space: nowrap;
  flex-shrink: 0;
  font-weight: 600;
  text-align: left;
}

.spm-field-value {
  font-size: 26px;
  font-weight: 600;
  color: #1d1d1f;
  word-break: break-all;
  flex: 1;
  text-align: left;
}
.spm-field-value.spm-price {
  color: #ff3b30;
  font-weight: 750;
}

.spm-field-dim {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #86868b;
}

.spm-section-title {
  font-size: 17px;
  font-weight: 700;
  color: rgba(29,29,31,0.55);
  padding: 8px 10px 4px;
  margin-top: 4px;
  border-top: 1px dashed #e2e4ea;
}

.spm-input {
  flex: 1;
  height: 40px;
  min-width: 0;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 0 10px;
  font-size: 16px;
  color: #1d1d1f;
  background: #fff;
  outline: none;
  text-align: center;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.spm-input:focus {
  border-color: #007aff;
  box-shadow: 0 0 0 3px rgba(0,122,255,0.12);
}
.spm-input-sm {
  width: 72px;
  flex: none;
  text-align: center;
  padding: 0 6px;
}
.spm-input-ro {
  flex: 1;
  min-width: 0;
  font-size: 16px;
  font-weight: 600;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.spm-btn-edit {
  height: 56px;
  padding: 0 40px;
  border-radius: 10px;
  border: none;
  background: #007aff;
  color: #fff;
  font-size: 22px;
  font-weight: 650;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.spm-btn-edit:hover { background: #0066d6; }

.spm-btn-save {
  height: 56px;
  padding: 0 36px;
  border-radius: 10px;
  border: none;
  background: #007aff;
  color: #fff;
  font-size: 22px;
  font-weight: 650;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.spm-btn-save:hover { background: #0066d6; }

.spm-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 22px;
  background: linear-gradient(180deg, #f7f9fc, #f0f2f7);
  border-top: 1px solid rgba(0,122,255,0.08);
  border-radius: 0 0 16px 16px;
  gap: 12px;
  flex-shrink: 0;
}

.spm-toggle-group {
  display: flex;
  gap: 14px;
}

.spm-toggle {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  color: rgba(29,29,31,0.55);
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  font-weight: 600;
  transition: color 0.15s;
}
.spm-toggle:hover { color: #ff3b30; }
.spm-toggle input[type="checkbox"] {
  accent-color: #ff3b30;
  width: 22px;
  height: 22px;
  cursor: pointer;
}

.spm-btn-close {
  height: 56px;
  padding: 0 36px;
  border-radius: 10px;
  border: 1px solid rgba(0,122,255,0.15);
  background: #fff;
  color: rgba(29,29,31,0.65);
  font-size: 22px;
  font-weight: 650;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.spm-btn-close:hover {
  background: rgba(0,122,255,0.06);
  border-color: rgba(0,122,255,0.25);
  color: #007aff;
}

.spm-hidden { display: none !important; }

.spm-no-img {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 18px;
  font-weight: 700;
  color: rgba(29,29,31,0.22);
  letter-spacing: 0.1em;
}
</style>