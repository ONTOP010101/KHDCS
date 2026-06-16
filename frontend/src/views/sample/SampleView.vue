<template>
  <div class="sample-page sample-samples-page">
    <div class="sample-card sample-form-card" :class="{ expanded: formExpanded }" v-show="formVisible">
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
          <button class="sample-btn sample-btn-ghost" :title="formVisible ? '隐藏展示区' : '显示展示区'" @click="formVisible = !formVisible">
            <EyeOff v-if="formVisible" :size="14" />
            <Eye v-else :size="14" />
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
                    :title="formData[sf.key] || ''"
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
                  :title="formData[f.key] || ''"
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
        <!-- 展示区隐藏时，在搜索框左侧显示恢复按钮 -->
        <button v-if="!formVisible" class="sample-btn sample-btn-primary" style="font-size:11px;height:30px;flex-shrink:0;margin-right:6px" @click="formVisible = true" title="显示展示区">
          <Eye :size="13" />
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
            <div class="sample-more-group-label"><PackageOpen :size="13" /> 导入</div>
            <div class="sample-more-item" @click="downloadTemplate"><Download :size="16" /> 下载导入模板</div>
            <div class="sample-more-item" @click="openBatchImageModal"><ImagePlus :size="16" /> 批量导入图片</div>
            <div class="sample-more-item" @click="openBatchVideoModal"><VideoIcon :size="16" /> 批量导入视频</div>
            <div class="sample-more-sep"></div>
            <div class="sample-more-group-label"><Database :size="13" /> 数据</div>
            <div class="sample-more-item" @click="openReferenceDataModal"><ListChecks :size="16" /> 对照资料管理</div>
            <div class="sample-more-item" @click="openRestoreDeletedModal"><RotateCcw :size="16" /> 恢复误删数据</div>
            <div class="sample-more-item" @click="openMainBatchQuery"><List :size="16" /> 按编号批量查询</div>
            <div class="sample-more-item" @click="batchSetPrice"><Coins :size="16" /> 批量设置价格</div>
            <div class="sample-more-sep"></div>
            <div class="sample-more-group-label"><FileOutput :size="13" /> 导出</div>
            <div class="sample-more-item" @click="exportExcel"><FileDown :size="16" /> 导出Excel</div>
            <div class="sample-more-sep"></div>
            <div class="sample-more-item sample-more-item-accent" @click="openReportDesigner"><LayoutGrid :size="16" /> 报表设计器</div>
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
            <Printer :size="14" /> 其他打印 <ChevronsDown :size="12" />
          </button>
        </div>
        <button class="sample-btn sample-btn-primary" @click="openVendorConfirmReport">
          <FileSpreadsheet :size="14" /> 打印报价
        </button>

        <button class="sample-btn sample-btn-ghost" @click="router.push({ name: 'ImageSearch' })">
          <ImageIcon :size="14" /> 图像搜索
        </button>
      </div>
    </div>

    <div class="sample-table-card">
      <div ref="tableWrapRef" class="sample-table-wrap">
        <vxe-grid
          ref="gridRef"
          :columns="allColumns"
          :data="tableData"
          :loading="tableLoading"
          :height="tableWrapHeight"
          :toolbar-config="gridToolbarConfig"
          :column-config="{ resizable: true, drag: true }"
          :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
          :cell-config="{ height: 44 }"
          :checkbox-config="{ highlight: true, range: true }"
          :sort-config="{ trigger: 'header', remote: true, defaultSort: { field: 'createTime', order: 'desc' } }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 0, rSize: 60, rHeight: 44 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :virtual-x-config="{ enabled: true, gt: 20 }"
          :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 0 }, scrollY: { gt: 0, oSize: 0, rSize: 60, rHeight: 44 } }"
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
                @mouseenter="onThumbMouseEnter($event, row)"
                @mouseleave="onThumbMouseLeave"
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

      <!-- 卡片覆盖层：绝对定位，不影响布局 -->
      <div v-if="cardMode" ref="cardOverlayRef" class="sample-card-overlay" @scroll.passive="onCardScroll">
        <div class="sample-card-scroll-body" :style="{ paddingTop: cardSpacerTop + 'px', paddingBottom: cardSpacerBottom + 'px' }">
          <div class="sample-card-grid">
            <div v-for="item in cardVisibleItems" :key="item.id"
                 class="sample-card-item" :class="{ 'card-selected': isCardSelected(item) }" @click="onCellClick({ row: item })">
              <div class="sample-card-img">
                <div class="card-checkbox" :class="{ checked: isCardSelected(item) }" @click.stop="toggleCardSelect(item)">
                  <Check v-if="isCardSelected(item)" :size="14" />
                </div>
                <img v-if="item.firstImageHash || item.thumbnail" :src="item.firstImageHash ? '/images/view/hash/' + item.firstImageHash : '/thumbnails/' + item.thumbnail" :data-thumb="item.thumbnail" @error="onCardImgError" @click.stop="openPhotoModalFor(item)" loading="lazy" decoding="async" />
                <div v-else class="sample-card-no-img" @click.stop="openPhotoModalFor(item)"><ImageIcon :size="36" /></div>
              </div>
              <div class="sample-card-body">
                <div class="sample-card-name" :title="item.sampleName">{{ item.sampleName || '--' }}</div>
                <div class="sample-card-field">
                  <span class="card-val-cell" style="grid-column:1/3"><span class="card-val" :title="item.sampleCode">{{ item.sampleCode || '-' }}</span><button v-if="item.sampleCode" class="card-copy-btn" @click.stop="copyCardCode(item.sampleCode)" :title="'复制 ' + item.sampleCode"><Copy :size="10" /></button></span>
                  <span class="card-label">货号</span><span class="card-val" :title="item.factoryCode">{{ item.factoryCode || '-' }}</span>
                </div>
                <div class="sample-card-field">
                  <span class="card-label">装量</span><span class="card-val" :title="(item.innerBoxCount ?? '-') + ' / ' + (item.cartonCapacity ?? '-')">{{ item.innerBoxCount != null ? item.innerBoxCount : '-' }} / {{ item.cartonCapacity != null ? item.cartonCapacity : '-' }}</span>
                  <span class="card-label">毛/净</span><span class="card-val" :title="(item.cartonGrossWeight ?? '-') + ' / ' + (item.cartonNetWeight ?? '-')">{{ item.cartonGrossWeight != null ? item.cartonGrossWeight : '-' }} / {{ item.cartonNetWeight != null ? item.cartonNetWeight : '-' }}</span>
                </div>
                <div class="sample-card-field">
                  <span class="card-label">材积体积</span><span class="card-val" :title="(item.cartonMaterialVolume ?? '-') + ' / ' + (item.cartonVolume ?? '-')">{{ item.cartonMaterialVolume != null ? item.cartonMaterialVolume : '-' }} / {{ item.cartonVolume != null ? item.cartonVolume : '-' }}</span>
                  <span class="card-label">摊位号</span><span class="card-val" :title="item.boothNo">{{ item.boothNo || '-' }}</span>
                </div>
                <div class="sample-card-field">
                  <span class="card-label">出厂价</span>
                  <span v-if="item.factoryPrice" class="card-val card-price" style="grid-column:2/-1" :title="'¥' + item.factoryPrice">¥{{ item.factoryPrice }}</span>
                  <span v-else class="card-val" style="grid-column:2/-1">-</span>
                </div>
                <div class="sample-card-divider"></div>
                <div class="sample-card-field">
                  <span class="card-label">厂名</span><span class="card-val" style="grid-column:2/-1" :title="item.supplier">{{ item.supplier || '-' }}</span>
                </div>
                <div class="sample-card-field">
                  <span class="card-label">手机</span><span class="card-val" :title="item.mobile">{{ item.mobile || '-' }}</span>
                  <span class="card-label">电话</span><span class="card-val" :title="item.contactPhone">{{ item.contactPhone || '-' }}</span>
                </div>
                <div class="sample-card-field">
                  <span class="card-label">登记日期</span><span class="card-val" style="grid-column:2/-1" :title="formatCardDate(item.createTime)">{{ formatCardDate(item.createTime) }}</span>
                </div>
                <div class="sample-card-field">
                  <span class="card-label">修改日期</span><span class="card-val" style="grid-column:2/-1" :title="formatCardDate(item.updateTime)">{{ formatCardDate(item.updateTime) }}</span>
                </div>
              </div>
            </div>
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
                 :src="photoModalImages[photoModalIndex]?.hash ? '/images/view/hash/' + photoModalImages[photoModalIndex]?.hash : '/thumbnails/' + photoModalImages[photoModalIndex]?.thumbnailPath"
                 :data-thumb="photoModalImages[photoModalIndex]?.thumbnailPath"
                 @error="onModalImgError"
                 @click="openFullPreview"
                 style="cursor:pointer" />
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
            <span class="import-stat">共 <strong>{{ importPreviewAllRows.length }}</strong> 条数据</span>
            <span class="import-stat">筛选后 <strong>{{ importPreviewFilteredCount }}</strong> 条</span>
            <span class="import-stat">已选 <strong>{{ importSelectedRows.length }}</strong> 条</span>
            <button class="sample-btn sample-btn-ghost" style="font-size:11px;padding:2px 10px;height:26px" :disabled="importSelectedRows.length === 0" @click="deleteSelectedPreviewRows">
              <Trash2 :size="13" /> 批量删除
            </button>
            <span style="margin-left:auto"></span>
            <button class="sample-btn sample-btn-ghost" :class="{ active: importPreviewCatFilter }" style="font-size:11px;padding:2px 10px;height:26px;background:#ffeaea;border-color:#e74c3c" @click="onTogglePreviewFilter('cat')">
              <AlertTriangle :size="12" /> 筛选种类不符
            </button>
            <button class="sample-btn sample-btn-ghost" :class="{ active: importPreviewPkgFilter }" style="font-size:11px;padding:2px 10px;height:26px;background:#fff8e1;border-color:#f39c12" @click="onTogglePreviewFilter('pkg')">
              <AlertTriangle :size="12" /> 筛选包装不符
            </button>
          </div>

          <div class="import-preview-table-wrap">
            <vxe-grid
              ref="importPreviewGridRef"
              :columns="IMPORT_PREVIEW_ALL_COLUMNS"
              :data="importPreviewDisplayData"
              :height="380"
              :auto-resize="false"
              :toolbar-config="{ custom: true, refresh: false, zoom: true, slots: { buttons: 'importPreviewToolbarBtns' } }"
              :column-config="{ resizable: true }"
              :row-config="{ isHover: true, keyField: '_rowIndex' }"
              :checkbox-config="{ highlight: true, range: true }"
              :edit-config="{ mode: 'cell', trigger: 'dblclick', showStatus: true, enabled: true, keepSource: true }"
              :virtual-y-config="{ enabled: true, gt: 15, oSize: 5 }"
              :virtual-x-config="{ enabled: true, gt: 15 }"
              :optimization="{ animat: false, delayHover: 250, scrollX: { gt: 0, oSize: 100, rSize: 100 }, scrollY: { gt: 0, oSize: 0, rSize: 60, rHeight: 44 } }"
              :border="true"
              :header-cell-style="{ background: '#ffffff', borderColor: '#a0bddb', color: '#1d1d1f', fontWeight: 600, textAlign: 'center' }"
              :cell-style="{ textAlign: 'center' }"
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
                    style="font-size:12px;padding:4px 12px;height:30px"
                    :disabled="importSelectedRows.length === 0 || !batchEditValue.trim()"
                    @click="batchEditRun"
                  >
                    批量修改
                  </button>
                  <button
                    class="sample-btn sample-btn-ghost"
                    style="font-size:12px;padding:4px 12px;height:30px;margin-left:4px"
                    :disabled="importSelectedRows.length === 0"
                    @click="batchTranslateSelected"
                  >
                    翻译勾选
                  </button>
                </div>
              </template>
              <template #import_warnings="{ row }">
                <div v-if="row._warnings && row._warnings.length > 0" style="display:flex;flex-direction:column;gap:2px;align-items:center">
                  <span v-for="(w, wi) in row._warnings" :key="wi" style="color:#e67e22;font-size:11px;white-space:nowrap;">{{ w }}</span>
                </div>
                <span v-else style="color:#27ae60;font-size:11px;">正常</span>
              </template>
              <template #import_action="{ row }">
                <div style="display:flex;gap:4px;justify-content:center">
                  <button class="sample-table-action" style="color:#007aff;font-size:11px;padding:2px 8px;height:24px" @click.stop="restorePreviewRow(row)">还原</button>
                  <button class="sample-table-action" style="color:#ff3b30;font-size:11px;padding:2px 8px;height:24px" @click.stop="deletePreviewRow(row)">删除</button>
                </div>
              </template>
            </vxe-grid>
          </div>

          <!-- 分页控件 -->
          <div class="import-preview-pager" v-if="importPreviewFilteredCount > importPreviewPageSize">
            <button class="sample-btn sample-btn-ghost" style="font-size:11px;padding:2px 10px;height:26px" :disabled="importPreviewPage <= 1" @click="onPreviewPageChange(1)">首页</button>
            <button class="sample-btn sample-btn-ghost" style="font-size:11px;padding:2px 10px;height:26px" :disabled="importPreviewPage <= 1" @click="onPreviewPageChange(importPreviewPage - 1)">上一页</button>
            <span style="font-size:12px;color:#64748b;margin:0 8px">第 {{ importPreviewPage }} / {{ importPreviewTotalPages }} 页</span>
            <button class="sample-btn sample-btn-ghost" style="font-size:11px;padding:2px 10px;height:26px" :disabled="importPreviewPage >= importPreviewTotalPages" @click="onPreviewPageChange(importPreviewPage + 1)">下一页</button>
            <button class="sample-btn sample-btn-ghost" style="font-size:11px;padding:2px 10px;height:26px" :disabled="importPreviewPage >= importPreviewTotalPages" @click="onPreviewPageChange(importPreviewTotalPages)">末页</button>
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
    <div v-if="showAdvancedSearch" class="adv-search-overlay" @click.self="showAdvancedSearch = false">
      <div class="adv-search-panel">
        <div class="adv-search-body">
          <!-- Row 1 -->
          <div class="adv-field"><label>厂商编号</label><input v-model="advForm.manufacturerCode" placeholder="请输入厂商编号" /></div>
          <div class="adv-field"><label>厂商名称</label><input v-model="advForm.supplier" placeholder="请输入厂商名称" /></div>
          <div class="adv-field"><label>联系人</label><input v-model="advForm.contactPerson" placeholder="请输入联系人" /></div>
          <!-- Row 2 -->
          <div class="adv-field"><label>电话号码</label><input v-model="advForm.contactPhone" placeholder="请输入电话号码" /></div>
          <div class="adv-field"><label>手机号码</label><input v-model="advForm.mobile" placeholder="请输入手机号码" /></div>
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
          <div class="adv-field"><label>种类编号</label><select v-model="advForm.categoryCode"><option value="">请选择种类</option></select></div>
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
          <div class="adv-field"><label>侵权</label><select v-model="advForm.infringement"><option value="">请选择侵权状态</option><option value="1">是</option><option value="0">否</option></select></div>
          <div class="adv-field adv-field-checks">
            <label>有无图片</label>
            <div class="check-group">
              <label class="chk-item"><input type="checkbox" v-model="advForm.hasImage" /> 有图片</label>
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
        <Printer :size="16" /> 多款打印
      </div>
      <div class="sample-more-item" @click="doPrintQuarterTable">
        <Printer :size="16" /> 小条码打印
      </div>
      <div class="sample-more-item" @click="openScanPrintModal">
        <Crosshair :size="16" /> 扫码打印
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
      <div class="batch-image-modal" style="width:420px">
        <div class="batch-image-modal-header">
          <span>选择报表模板</span>
          <button class="modal-close-btn" @click="showTemplateSelect = false"><X :size="16" /></button>
        </div>
        <div class="batch-image-modal-body" style="padding:16px 20px;max-height:360px;overflow-y:auto">
          <input class="sr-input" v-model="templateSearchKeyword" placeholder="搜索模板..."
            style="width:100%;box-sizing:border-box;height:36px;font-size:13px;border-radius:6px;margin-bottom:12px;" />
          <div v-if="filteredTemplates.length === 0" style="text-align:center;color:#999;padding:24px 0">{{ templateSearchKeyword ? '无匹配模板' : '暂无模板' }}</div>
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
        <div style="padding:0 20px 16px;display:flex;gap:8px;justify-content:flex-end">
          <button class="sample-btn sample-btn-ghost" @click="showTemplateSelect = false">取消</button>
          <button class="sample-btn sample-btn-primary" :disabled="!selectedTemplateId || vcSessionLoading" @click="confirmTemplateAndOpen">
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
    <Transition name="toast-fade">
      <div v-if="toast.show" class="sr-toast" :class="toast.type">{{ toast.message }}</div>
    </Transition>

    <!-- 对照资料管理弹窗 -->
    <Teleport to="body">
      <div v-if="showRefDataModal" class="batch-image-modal-overlay" @click.self="showRefDataModal = false">
        <div class="ref-modal">
          <div class="ref-modal-header">
            <strong>对照资料管理</strong>
            <div class="ref-tabs">
              <button :class="['ref-tab', { active: refActiveTab === 'category' }]" @click="refActiveTab = 'category'">产品种类</button>
              <button :class="['ref-tab', { active: refActiveTab === 'packaging' }]" @click="refActiveTab = 'packaging'">包装方式</button>
            </div>
            <X :size="16" class="cursor-pointer" @click="showRefDataModal = false" />
          </div>
          <div class="ref-modal-body">
            <!-- 种类管理 - 树形展开 -->
            <div v-if="refActiveTab === 'category'" class="ref-panel">
              <div class="ref-panel-toolbar">
                <button class="sample-btn sample-btn-primary" style="font-size:11px;height:28px;padding:0 10px" @click="openRefCategoryAdd"><Plus :size="12" /> 新增</button>
                <button class="sample-btn sample-btn-ghost" style="font-size:11px;height:28px;padding:0 10px" @click="refExpandAllCat"><ChevronsDownUp :size="12" /> 展开/折叠全部</button>
                <button class="sample-btn sample-btn-ghost" style="font-size:11px;height:28px;padding:0 10px" :disabled="refSelectedCatIds.length === 0" @click="refDeleteSelectedCats"><Trash2 :size="12" /> 删除选中</button>
                <div class="ref-search-box">
                  <Search :size="12" />
                  <input v-model="refCatKeyword" placeholder="搜索编号或名称..." @input="refFilterCategories" />
                </div>
              </div>
              <vxe-table ref="refCatGridRef" :data="refCatTreeData" :tree-config="{ transform: true, rowField: 'id', parentField: '_parentId', expandAll: true, line: false }"
                :checkbox-config="{ checkField: '_ck' }" height="360" stripe border size="small"
                :virtual-y-config="{ enabled: true, gt: 10 }"
                @checkbox-change="refCatGridRef && (refSelectedCatIds = refCatGridRef.getCheckboxRecords().map(r => r.id))"
                @checkbox-all="refCatGridRef && (refSelectedCatIds = refCatGridRef.getCheckboxRecords().map(r => r.id))">
                <vxe-column type="checkbox" width="38" />
                <vxe-column field="code" title="编号" width="100" tree-node show-overflow />
                <vxe-column field="name" title="名称" min-width="180" show-overflow />
                <vxe-column field="keywords" title="关键词(逗号分隔)" min-width="150" show-overflow>
                  <template #default="{ row }">
                    <input class="ref-inline-input" :value="row.keywords || ''" placeholder="合金,滑行,回力"
                      @blur="saveRefCatKeywords(row, $event.target.value)" />
                  </template>
                </vxe-column>
                <vxe-column field="_childCount" title="子项数" width="60" align="center">
                  <template #default="{ row }">{{ row._childCount || '' }}</template>
                </vxe-column>
                <vxe-column title="操作" width="80" fixed="right">
                  <template #default="{ row }">
                    <button class="ref-action-btn" @click="refEditCategory(row)"><Pencil :size="11" /></button>
                    <button class="ref-action-btn danger" @click="refDeleteCategory(row)"><Trash2 :size="11" /></button>
                  </template>
                </vxe-column>
              </vxe-table>
            </div>

            <!-- 包装管理 -->
            <div v-if="refActiveTab === 'packaging'" class="ref-panel">
              <div class="ref-panel-toolbar">
                <button class="sample-btn sample-btn-primary" style="font-size:11px;height:28px;padding:0 10px" @click="openRefPackagingAdd"><Plus :size="12" /> 新增</button>
                <button class="sample-btn sample-btn-ghost" style="font-size:11px;height:28px;padding:0 10px" :disabled="refSelectedPkgIds.length === 0" @click="refDeleteSelectedPkgs"><Trash2 :size="12" /> 删除选中</button>
                <div class="ref-search-box">
                  <Search :size="12" />
                  <input v-model="refPkgKeyword" placeholder="搜索..." @keyup.enter="refLoadPackagings" />
                </div>
              </div>
              <vxe-table ref="refPkgGridRef" :data="refPackagings" :checkbox-config="{ checkField: '_ck' }" height="320" stripe border size="small"
                :virtual-y-config="{ enabled: true, gt: 10 }"
                @checkbox-change="refPkgGridRef && (refSelectedPkgIds = refPkgGridRef.getCheckboxRecords().map(r => r.id))"
                @checkbox-all="refPkgGridRef && (refSelectedPkgIds = refPkgGridRef.getCheckboxRecords().map(r => r.id))">
                <vxe-column type="checkbox" width="40" />
                <vxe-column field="code" title="编号" width="90" />
                <vxe-column field="name" title="中文包装" min-width="150" show-overflow />
                <vxe-column field="nameEn" title="英文包装" min-width="150" show-overflow />
                <vxe-column title="操作" width="70" fixed="right">
                  <template #default="{ row }">
                    <button class="ref-action-btn" @click="refEditPackaging(row)"><Pencil :size="11" /></button>
                    <button class="ref-action-btn danger" @click="refDeletePackaging(row)"><Trash2 :size="11" /></button>
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
      <div v-if="showRefCatForm" class="batch-image-modal-overlay" @click.self="showRefCatForm = false">
        <div class="ref-form-modal">
          <div class="ref-modal-header">
            <strong>{{ refEditingCat ? '编辑种类' : '新增种类' }}</strong>
            <X :size="16" class="cursor-pointer" @click="showRefCatForm = false" />
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
      <div v-if="showRefPkgForm" class="batch-image-modal-overlay" @click.self="showRefPkgForm = false">
        <div class="ref-form-modal">
          <div class="ref-modal-header">
            <strong>{{ refEditingPkg ? '编辑包装方式' : '新增包装方式' }}</strong>
            <X :size="16" class="cursor-pointer" @click="showRefPkgForm = false" />
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
import '@/styles/sample.css'
import '@/styles/sample-form.css'
import ExcelParserWorker from '@/workers/excelParser.worker.js?worker'
import * as XLSX from 'xlsx'
import QRCode from 'qrcode'
import {
  Database, Search, Plus, Pencil, Trash2, Save, X, Upload, Download,
  FileUp, FileDown, FileSpreadsheet, FileOutput, MoreHorizontal, Settings,
  ChevronsUp, ChevronsDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  MapPin, Crosshair, Filter, Columns3, ImagePlus, Coins, Package, PackageOpen, DollarSign, Printer, Loader2,
  Image as ImageIcon, RotateCcw, AlertTriangle, AlertCircle, Check, CheckCircle, CheckCircle as CheckCircleIcon, Info, Video as VideoIcon, List, ListChecks, LayoutGrid, Copy, GripVertical, RotateCw, ChevronDown, Eye, EyeOff, ChevronsDownUp
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

// ===== Toast 提示 =====
const toast = reactive({ show: false, message: '', type: 'info' })
let toastTimer = null
function showToast(msg, type = 'info') {
  toast.message = msg
  toast.type = type
  toast.show = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.show = false }, 2500)
}
const route = useRoute()

const allFormFields = [
  { key: 'sampleCode', label: '公司编号', labelWidth: 84, labelJustify: true, width: 180 },
  { group: true, key: 'g-packaging', label: '包装方式', labelWidth: 84, labelJustify: true, width: 0, fields: [
    { key: 'packageCode', placeholder: '包装编号', width: 60 },
    { key: 'packagingCn', placeholder: '中文包装', width: 130 },
    { key: 'packagingEn', placeholder: '英文包装', width: 48 },
  ]},
  { group: true, key: 'g-category', label: '种类名称', labelWidth: 84, labelJustify: true, width: 0, fields: [
    { key: 'categoryCode', placeholder: '种类编号', width: 44 },
    { key: 'category', placeholder: '种类名称', width: 100 },
  ]},
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
  { key: 'infringement', label: '是否侵权', labelWidth: 84, labelJustify: true, width: 180 },
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
  { key: 'englishName', label: '英文名称', labelWidth: 84, labelJustify: true, width: 180 },
]

const fieldVisible = reactive({})
allFormFields.forEach(f => { fieldVisible[f.key] = true })

const visibleFormFields = computed(() => allFormFields.filter(f => fieldVisible[f.key]))

const showFieldSettings = ref(false)
const toggleFieldSettings = () => { showFieldSettings.value = !showFieldSettings.value }

const cardMode = ref(false)
const gridToolbarConfig = { custom: true }

// ===== 卡片虚拟滚动 =====
const cardOverlayRef = ref(null)
const cardScrollTop = ref(0)
const cardContainerWidth = ref(1200)

const CARD_COLS = 6
const CARD_GAP = 14
const CARD_BODY_H = 80

const cardRowHeight = computed(() => {
  const w = cardContainerWidth.value
  const cardW = (w - (CARD_COLS - 1) * CARD_GAP) / CARD_COLS
  return cardW + CARD_BODY_H + CARD_GAP
})

const cardVisibleRange = computed(() => {
  const h = cardOverlayRef.value?.clientHeight || 600
  const rh = cardRowHeight.value
  if (rh <= 0) return { start: 0, end: 24 }
  const buffer = 2
  const start = Math.max(0, Math.floor(cardScrollTop.value / rh) - buffer)
  const end = Math.ceil((cardScrollTop.value + h) / rh) + buffer
  return { start, end }
})

const cardTotalRows = computed(() => Math.ceil(tableData.value.length / CARD_COLS))

const cardVisibleItems = computed(() => {
  const { start, end } = cardVisibleRange.value
  return tableData.value.slice(start * CARD_COLS, end * CARD_COLS)
})

const cardSpacerTop = computed(() => cardVisibleRange.value.start * cardRowHeight.value)

const cardSpacerBottom = computed(() => {
  const total = cardTotalRows.value
  const end = cardVisibleRange.value.end
  return Math.max(0, (total - end) * cardRowHeight.value)
})

watch(cardMode, async (v) => {
  if (v) {
    formExpanded.value = false
    await nextTick()
    cardScrollTop.value = 0
    if (cardOverlayRef.value) {
      cardContainerWidth.value = cardOverlayRef.value.clientWidth
    }
  }
})

function onCardScroll() {
  if (cardOverlayRef.value) {
    cardScrollTop.value = cardOverlayRef.value.scrollTop
    cardContainerWidth.value = cardOverlayRef.value.clientWidth
  }
}

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
const formVisible = ref(true)
const formMode = ref('readonly')
const formData = reactive({})

const currentSample = ref(null)
const currentSampleImages = ref([])
const stripIndex = ref(0)

const tableData = ref([])
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
const showReportModal = ref(false)
const reportModalUrl = ref('')
const reportModalLoading = ref(false)
const reportModalProgress = ref(0)
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
  if (img.filePath) return '/images/' + img.filePath
  if (img.hash) return '/images/view/hash/' + img.hash
  return '/thumbnails/' + img.thumbnailPath
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
  const f = (label, val, cls) => `<div class="spm-field"><span class="spm-field-label">${label}</span><span class="spm-field-value${cls ? ' ' + cls : ''}" title="${val || '-'}">${val || '-'}</span></div>`
  const ff = (label, val, cls) => `<div class="spm-field spm-field-full"><span class="spm-field-label">${label}</span><span class="spm-field-value${cls ? ' ' + cls : ''}" title="${val || '-'}">${val || '-'}</span></div>`
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
    row(ff('中文备注', v('remark'))) +
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
const importPreviewAllRows = ref([])   // 全量数据
const importPreviewData = ref([])      // 当前页数据（给 vxe-grid 渲染）
const importOriginalData = ref([])
const importPreviewHeaders = ref([])
const importSelectedRows = ref([])
const importSelectedRowIndexes = ref(new Set())  // 跨页跟踪勾选
const importPreviewGridRef = ref(null)
const importPreviewPage = ref(1)
const importPreviewPageSize = ref(3000)
const batchEditField = ref('packagingCn')   // 批量修改-选择的字段
const batchEditValue = ref('')             // 批量修改-输入的值
const batchEditDropdownOpen = ref(false)   // 下拉面板开关
const batchEditFields = [
  { value: 'packagingCn', label: '中文包装' },
  { value: 'category', label: '种类名称' },
  { value: 'factoryPrice', label: '出厂价' },
  { value: 'cartonLength', label: '外箱长' },
  { value: 'cartonWidth', label: '外箱宽' },
  { value: 'cartonHeight', label: '外箱高' },
  { value: 'packageLength', label: '包装长' },
  { value: 'packageWidth', label: '包装宽' },
  { value: 'packageHeight', label: '包装高' },
  { value: 'sampleLength', label: '样品长' },
  { value: 'sampleWidth', label: '样品宽' },
  { value: 'sampleHeight', label: '样品高' },
  { value: 'cartonGrossWeight', label: '箱毛重' },
  { value: 'cartonNetWeight', label: '箱净重' },
  { value: 'innerBoxCount', label: '内盒' },
  { value: 'cartonCapacity', label: '装箱量' },
  { value: 'hideFromXzx', label: '不在小竹熊显示' },
  { value: 'infringement', label: '侵权' }
]

const showImportConfirmModal = ref(false)
const importConfirmCount = ref(0)
const importProgress = ref(0)
const importParsing = ref(false)
const importParsingProgress = ref(0)
const importParsingStage = ref('')

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
  { field: 'categoryCode', title: '种类编号', width: 90, sortable: true },
  { field: 'factoryCode', title: '出厂货号', width: 110, sortable: true },
  { field: 'registrant', title: '登记人', width: 90, sortable: true },
  { field: 'updateTime', title: '删除时间', width: 160, sortable: true, formatter: ({ cellValue }) => cellValue ? new Date(cellValue).toLocaleString('zh-CN', {year:'numeric',month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit'}) : '-' }
]
const importProgressText = ref('')
const importUpdateMode = ref(false)
const importPreviewCatFilter = ref(false)
const importPreviewPkgFilter = ref(false)

const importPreviewDisplayData = computed(() => importPreviewData.value)

const importPreviewFilteredCount = computed(() => {
  let list = importPreviewAllRows.value
  if (importPreviewCatFilter.value) {
    list = list.filter(r => r._status === 'cat_error')
  }
  if (importPreviewPkgFilter.value) {
    list = list.filter(r => r._status === 'pkg_warning' || r._status === 'cat_error')
  }
  return list.length
})

const importPreviewTotalPages = computed(() =>
  Math.ceil(importPreviewFilteredCount.value / importPreviewPageSize.value) || 1
)

/** 从全量数据同步当前页（应用筛选+分页） */
const syncPreviewPage = () => {
  let list = importPreviewAllRows.value
  if (importPreviewCatFilter.value) {
    list = list.filter(r => r._status === 'cat_error')
  }
  if (importPreviewPkgFilter.value) {
    list = list.filter(r => r._status === 'pkg_warning' || r._status === 'cat_error')
  }
  const totalPages = Math.ceil(list.length / importPreviewPageSize.value) || 1
  if (importPreviewPage.value > totalPages) importPreviewPage.value = totalPages
  const start = (importPreviewPage.value - 1) * importPreviewPageSize.value
  importPreviewData.value = list.slice(start, start + importPreviewPageSize.value)
}

/** 切换分页大小，回到第1页 */
const onPreviewPageSizeChange = (size) => {
  importPreviewPageSize.value = size
  importPreviewPage.value = 1
  syncPreviewPage()
}

/** 切换页码 */
const onPreviewPageChange = (page) => {
  importPreviewPage.value = page
  syncPreviewPage()
}

/** 切换筛选，回到第1页 */
const onTogglePreviewFilter = (type) => {
  if (type === 'cat') importPreviewCatFilter.value = !importPreviewCatFilter.value
  else importPreviewPkgFilter.value = !importPreviewPkgFilter.value
  importPreviewPage.value = 1
  syncPreviewPage()
}

// 缓存的对照表名称集合，供编辑时重新校验
const importValidCatNames = ref(new Set())
const importValidPkgNames = ref(new Set())
const importPkgList = ref([])  // 完整包装列表，供编辑时重新关键词匹配
const importCatList = ref([])  // 完整种类列表，供编辑时查找编码

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
  '厂商编号': 'manufacturerCode', '公司编号': 'sampleCode', '种类编号': 'categoryCode',
  '种类名称': 'category', '样品名称': 'sampleName', '英文名称': 'englishName',
  '出厂货号': 'factoryCode', '货号': 'factoryCode',
  '样品单位': 'sampleUnit', '样品英文单位': 'sampleUnitEn',
  '中文包装': 'originalPackagingCn', '原始中文包装': 'originalPackagingCn', '英文包装': 'packagingEn', '包装编号': 'packageCode',
  '出厂价': 'factoryPrice', '价格': 'factoryPrice', '单价': 'factoryPrice',
  '税点价': 'taxPrice', '样品长度': 'sampleLength', '样品宽度': 'sampleWidth', '样品高度': 'sampleHeight',
  '样品毛重': 'sampleGrossWeight', '样品净重': 'sampleNetWeight',
  '外箱长度': 'cartonLength', '外箱宽度': 'cartonWidth', '外箱高度': 'cartonHeight',
  '外箱材积': 'cartonMaterialVolume', '外箱体积': 'cartonVolume',
  '内盒个数': 'innerBoxCount', '外箱装量': 'cartonCapacity',
  '装箱单位': 'packingUnit', '外箱毛重': 'cartonGrossWeight', '外箱净重': 'cartonNetWeight',
  '包装长度': 'packageLength', '包装宽度': 'packageWidth', '包装高度': 'packageHeight',
  '产品认证': 'certification', '认证总数': 'certificationCount', '颜色': 'color',
  '英文颜色': 'colorEn', '备注': 'remark', '英文备注': 'remarkEn',
  '厂商名称': 'supplier', '摊位号': 'boothNo', '联系人': 'contactPerson',
  '电话': 'contactPhone', '手机': 'mobile', '传真': 'fax', 'QQ': 'qq',
  '登记人': 'registrant', '修改人': 'modifier', '侵权': 'infringement',
  '电池信息': 'batteryInfo', '电话/信息': 'contactPhone',
  '不在小竹熊显示': 'hideFromXzx', '是否不在小竹熊显示': 'hideFromXzx',
  // 复合列 → 后续拆分
  '品名': 'sampleName', '产品名称': 'sampleName',
  '包装': 'originalPackagingCn',
  '包装规格': '_pkgDimensions', '包装尺寸': '_pkgDimensions',
  '外箱规格': '_cartonDimensions', '外箱尺寸': '_cartonDimensions', '规格': '_cartonDimensions', '箱规': '_cartonDimensions',
  '产品规格': '_productDimensions', '产品尺寸': '_productDimensions', '尺寸': '_productDimensions',
  '毛/净重': '_grossNetWeight', '毛净重': '_grossNetWeight',
}

// 表头匹配函数：去除空格后查找
function resolveHeader(rawHeader) {
  const cleaned = rawHeader.replace(/\s+/g, '')
  if (HEADER_TO_FIELD[cleaned]) return HEADER_TO_FIELD[cleaned]
  // 再试模糊匹配（兼容已存在的空格写法如"样品 长度"）
  if (HEADER_TO_FIELD[rawHeader]) return HEADER_TO_FIELD[rawHeader]
  return null
}

// 尺寸拆分：支持 * x X 分隔，自动去 CM/cm 后缀
function splitDimensions(raw) {
  if (!raw) return null
  const cleaned = raw.toString().trim().replace(/cm$/i, '')
  const parts = cleaned.split(/[*xX]/).map(s => s.trim()).filter(Boolean)
  if (parts.length >= 3) {
    const [l, w, h] = parts.map(Number)
    if (!isNaN(l) && !isNaN(w) && !isNaN(h)) return { l, w, h }
  }
  return null
}

// 毛净重拆分：支持 / 分隔，大值=毛重
function splitGrossNet(raw) {
  if (!raw) return null
  const parts = raw.toString().trim().split('/').map(s => s.trim()).filter(Boolean)
  if (parts.length >= 2) {
    const [a, b] = [Number(parts[0]), Number(parts[1])]
    if (!isNaN(a) && !isNaN(b)) {
      return { gross: Math.max(a, b), net: Math.min(a, b) }
    }
  }
  return null
}

// 应用拆分结果到行对象
function applySplits(rowObj) {
  // 包装规格 → 包装长宽高
  if (rowObj._pkgDimensions) {
    const dim = splitDimensions(rowObj._pkgDimensions)
    if (dim) { rowObj.packageLength = dim.l; rowObj.packageWidth = dim.w; rowObj.packageHeight = dim.h; rowObj.originalPackagingCn = rowObj.originalPackagingCn || rowObj._pkgDimensions }
    delete rowObj._pkgDimensions
  }
  // 外箱规格 → 外箱长宽高
  if (rowObj._cartonDimensions) {
    const dim = splitDimensions(rowObj._cartonDimensions)
    if (dim) { rowObj.cartonLength = dim.l; rowObj.cartonWidth = dim.w; rowObj.cartonHeight = dim.h }
    delete rowObj._cartonDimensions
  }
  // 产品规格 → 产品长宽高
  if (rowObj._productDimensions) {
    const dim = splitDimensions(rowObj._productDimensions)
    if (dim) { rowObj.sampleLength = dim.l; rowObj.sampleWidth = dim.w; rowObj.sampleHeight = dim.h }
    delete rowObj._productDimensions
  }
  // 毛/净重 → 外箱毛重/净重
  if (rowObj._grossNetWeight) {
    const gn = splitGrossNet(rowObj._grossNetWeight)
    if (gn) { rowObj.cartonGrossWeight = gn.gross; rowObj.cartonNetWeight = gn.net }
    delete rowObj._grossNetWeight
  }
}

const EDIT_RENDER = { name: 'input' }

// 隐藏列无需 editRender，减少 vxe-grid 初始化开销（23列×可见行×编辑渲染器）
const IMPORT_PREVIEW_ALL_COLUMNS = [
  { type: 'checkbox', width: 44, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'manufacturerCode', title: '厂商编号', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'sampleCode', title: '公司编号', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'categoryCode', title: '种类编号', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'category', title: '种类名称', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'sampleName', title: '样品名称', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'englishName', title: '英文名称', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'factoryCode', title: '出厂货号', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'infringement', title: '侵权', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'batteryInfo', title: '电池信息', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'hideFromXzx', title: '不在小竹熊显示', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packageCode', title: '包装编号', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packagingCn', title: '中文包装', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packagingEn', title: '英文包装', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'factoryPrice', title: '价格', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true },
  { field: 'sampleLength', title: '样品长度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'sampleWidth', title: '样品宽度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'sampleHeight', title: '样品高度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packageLength', title: '包装长度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packageWidth', title: '包装宽度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'packageHeight', title: '包装高度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonLength', title: '外箱长度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonWidth', title: '外箱宽度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonHeight', title: '外箱高度', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'sampleGrossWeight', title: '样品毛重', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'sampleNetWeight', title: '样品净重', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonMaterialVolume', title: '外箱材积', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonVolume', title: '外箱体积', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'innerBoxCount', title: '内盒个数', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonCapacity', title: '外箱装量', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonGrossWeight', title: '外箱毛重', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'cartonNetWeight', title: '外箱净重', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'certification', title: '产品认证', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  { field: 'remark', title: '备注', width: 200, showOverflow: true, editRender: EDIT_RENDER },
  // --- 以下默认隐藏 ---
  { field: 'originalPackagingCn', title: '原始中文包装', width: 200, showOverflow: true, className: 'col-original-pkg', editRender: EDIT_RENDER, visible: false },
  { field: 'sampleUnit', title: '样品单位', width: 200, visible: false },
  { field: 'sampleUnitEn', title: '英文单位', width: 200, visible: false },
  { field: 'taxPrice', title: '税点价', width: 200, visible: false },
  { field: 'color', title: '颜色', width: 200, visible: false },
  { field: 'colorEn', title: '英文颜色', width: 200, visible: false },
  { field: 'packingUnit', title: '装箱单位', width: 200, visible: false },
  { field: 'supplier', title: '厂商名称', width: 200, showOverflow: true, editRender: EDIT_RENDER, sortable: true, visible: false },
  { field: 'boothNo', title: '摊位号', width: 200, visible: false },
  { field: 'contactPerson', title: '联系人', width: 200, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'contactPhone', title: '电话', width: 200, showOverflow: true, editRender: EDIT_RENDER, visible: false },
  { field: 'mobile', title: '手机', width: 200, visible: false },
  { field: 'fax', title: '传真', width: 200, visible: false },
  { field: 'qq', title: 'QQ', width: 200, visible: false },
  { field: 'certificationCount', title: '认证数', width: 200, visible: false },
  { field: 'remarkEn', title: '英文备注', width: 200, visible: false },
  { field: 'registrant', title: '登记人', width: 200, visible: false },
  { title: '校验警告', width: 100, fixed: 'right', slots: { default: 'import_warnings' } },
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

const loadTableData = async () => {
  tableLoading.value = true
  try {
    // 如果有活跃的综合查询条件，走搜索接口
    if (activeSearchConditions.value && activeSearchConditions.value.length > 0) {
      const res = await api(`/samples/search?current=${currentPage.value}&size=${pageSize.value}&sortField=${currentSortField.value}&sortOrder=${currentSortOrder.value}`, {
        method: 'POST',
        body: JSON.stringify({ conditions: activeSearchConditions.value })
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

const openFullPreview = () => {
  if (photoModalImages.value.length === 0) return
  imagePreviewList.value = photoModalImages.value
  imagePreviewIndex.value = photoModalIndex.value
  imagePreviewSelected.value = new Set()
  showPhotoModal.value = false
  showImagePreview.value = true
}

// 关闭图片预览时恢复照片模态框
watch(showImagePreview, (v) => {
  if (!v && photoModalImages.value.length > 0) {
    showPhotoModal.value = true
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

const onSearch = () => {
  mainBatchQueryActive.value = false
  activeSearchConditions.value = null  // 清除综合查询条件
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
  activeSearchConditions.value = null  // 清除综合查询条件
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
  if (navigator.clipboard) {
    navigator.clipboard.writeText(code).catch(() => {})
  } else {
    const ta = document.createElement('textarea')
    ta.value = code
    ta.style.position = 'fixed'
    ta.style.left = '-9999px'
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
  }
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

const onThumbMouseEnter = (e, row) => {
  if (!row.thumbnail) return
  // 优先用原图，缩略图兜底
  const thumbSrc = '/thumbnails/' + row.thumbnail
  const src = row.firstImageHash ? '/images/view/hash/' + row.firstImageHash : thumbSrc
  const rect = e.target.getBoundingClientRect()
  const gap = 12
  const previewSize = 620
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
  showToast('批量设置价格功能开发中', 'info')
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

// ===================== 导出字段选择 =====================
const EXPORT_FIELD_CONFIG = [
  { key: 'sampleCode', label: '公司编号', default: true },
  { key: 'factoryCode', label: '出厂货号', default: true },
  { key: 'manufacturerCode', label: '厂商编号' },
  { key: 'category', label: '种类名称' },
  { key: 'sampleName', label: '样品名称', default: true },
  { key: 'englishName', label: '英文名称' },
  { key: 'factoryPrice', label: '出厂价', default: true },
  { key: 'taxPrice', label: '报出价' },
  { key: 'packagingCn', label: '包装规格' },
  { key: 'packagingEn', label: '包装规格(英)' },
  { key: 'packingUnit', label: '包装单位' },
  { key: 'innerBoxCount', label: '内盒数' },
  { key: 'cartonCapacity', label: '装箱量' },
  { key: 'cartonLength', label: '外箱长' },
  { key: 'cartonWidth', label: '外箱宽' },
  { key: 'cartonHeight', label: '外箱高' },
  { key: 'cartonGrossWeight', label: '外箱毛重' },
  { key: 'cartonNetWeight', label: '外箱净重' },
  { key: 'sampleLength', label: '产品长' },
  { key: 'sampleWidth', label: '产品宽' },
  { key: 'sampleHeight', label: '产品高' },
  { key: 'sampleGrossWeight', label: '产品毛重' },
  { key: 'sampleNetWeight', label: '产品净重' },
  { key: 'cartonVolume', label: '体积' },
  { key: 'cartonMaterialVolume', label: '材积' },
  { key: 'boothNo', label: '摊位号' },
  { key: 'supplier', label: '厂商名称' },
  { key: 'contactPerson', label: '联系人' },
  { key: 'contactPhone', label: '联系电话' },
  { key: 'mobile', label: '手机' },
  { key: 'fax', label: '传真' },
  { key: 'qq', label: 'QQ' },
  { key: 'material', label: '材料' },
  { key: 'color', label: '颜色' },
  { key: 'colorEn', label: '颜色(英)' },
  { key: 'size', label: '尺寸' },
  { key: 'origin', label: '原产地' },
  { key: 'sampleUnit', label: '样品单位' },
  { key: 'sampleUnitEn', label: '样品单位(英)' },
  { key: 'certification', label: '认证' },
  { key: 'certificationCount', label: '认证数量' },
  { key: 'batteryInfo', label: '电池信息' },
  { key: 'infringement', label: '侵权信息' },
  { key: 'remark', label: '中文备注' },
  { key: 'remarkEn', label: '备注(英)' },
  { key: 'registrant', label: '登记人' },
  { key: 'modifier', label: '修改人' },
  { key: 'createTime', label: '登记时间' },
  { key: 'updateTime', label: '修改时间' },
]

const showExportModal = ref(false)
const exportFields = ref([])
const dragIndex = ref(-1)

const initExportFields = () => {
  // 尝试从 localStorage 恢复上次保存的模板
  const saved = localStorage.getItem('export_template_last')
  if (saved) {
    try {
      const last = JSON.parse(saved)
      const keySet = new Set(last.fields)
      exportFields.value = EXPORT_FIELD_CONFIG.map(f => ({ ...f, checked: keySet.has(f.key) }))
      currentTemplate.value = { name: last.name, fields: last.fields }
      return
    } catch (e) {}
  }
  exportFields.value = EXPORT_FIELD_CONFIG.map(f => ({ ...f }))
  currentTemplate.value = null
}

const checkedExportFieldCount = computed(() => exportFields.value.filter(f => f.checked).length)

// 模板相关
const templateName = ref('')
const currentTemplate = ref(null)
const exportTemplates = ref([])
const showTplMenu = ref(false)
const showTplSaveInput = ref(false)
const tplSaveRef = ref(null)

const loadExportTemplates = () => {
  try {
    exportTemplates.value = JSON.parse(localStorage.getItem('export_templates') || '[]')
  } catch (e) { exportTemplates.value = [] }
}

const saveExportTemplate = () => {
  const name = templateName.value.trim()
  if (!name) return
  const checked = exportFields.value.filter(f => f.checked).map(f => f.key)
  if (checked.length === 0) return
  const templates = JSON.parse(localStorage.getItem('export_templates') || '[]')
  const idx = templates.findIndex(t => t.name === name)
  const obj = { name, fields: checked }
  if (idx >= 0) templates[idx] = obj
  else templates.push(obj)
  localStorage.setItem('export_templates', JSON.stringify(templates))
  exportTemplates.value = templates
  currentTemplate.value = obj
  templateName.value = ''
  showTplSaveInput.value = false
}

const loadExportTemplate = (t) => {
  const keySet = new Set(t.fields)
  exportFields.value = EXPORT_FIELD_CONFIG.map(f => ({ ...f, checked: keySet.has(f.key) }))
  currentTemplate.value = t
}

const deleteExportTemplate = () => {
  if (!currentTemplate.value) return
  const templates = JSON.parse(localStorage.getItem('export_templates') || '[]').filter(t => t.name !== currentTemplate.value.name)
  localStorage.setItem('export_templates', JSON.stringify(templates))
  exportTemplates.value = templates
  currentTemplate.value = null
}

const selectAllExportFields = () => exportFields.value.forEach(f => f.checked = true)
const deselectAllExportFields = () => exportFields.value.forEach(f => f.checked = false)

// 拖拽排序
const onExportDragStart = (e, i) => {
  dragIndex.value = i
  e.dataTransfer.effectAllowed = 'move'
}
const onExportDragOver = (e, i) => {
  if (dragIndex.value === -1 || dragIndex.value === i) return
  const arr = [...exportFields.value]
  const [removed] = arr.splice(dragIndex.value, 1)
  arr.splice(i, 0, removed)
  exportFields.value = arr
  dragIndex.value = i
}
const onExportDrop = (i) => { dragIndex.value = -1 }
const onExportDragEnd = () => { dragIndex.value = -1 }

const doExport = async () => {
  const selected = exportFields.value.filter(f => f.checked).map(f => f.key)
  if (selected.length === 0 || selectedIds.value.length === 0) return
  try {
    const resp = await fetch('/samples/export', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + (localStorage.getItem('token') || '') },
      body: JSON.stringify({ fields: selected, ids: selectedIds.value })
    })
    if (!resp.ok) throw new Error('导出失败')
    const blob = await resp.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const today = new Date()
    const dateStr = today.getFullYear() + String(today.getMonth() + 1).padStart(2, '0') + String(today.getDate()).padStart(2, '0')
    a.download = '样品资料' + dateStr + '.xlsx'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    // 保存最后使用的模板
    if (currentTemplate.value) {
      localStorage.setItem('export_template_last', JSON.stringify(currentTemplate.value))
    } else {
      localStorage.setItem('export_template_last', JSON.stringify({ name: '_auto', fields: selected }))
    }
    showExportModal.value = false
    // 清除勾选
    selectedIds.value = []
    if (gridRef.value) gridRef.value.setCheckboxRow([], false)
  } catch (e) {
    console.error('导出失败', e)
  }
}

const exportExcel = () => {
  showMoreDropdown.value = false
  loadExportTemplates()
  initExportFields()
  showExportModal.value = true
}

// 在新标签页中打开报表设计器，避免覆盖主页面
const openReportDesigner = () => {
  showMoreDropdown.value = false
  const url = router.resolve({ name: 'ReportDesigner' }).href
  window.open(url, '_blank')
}

// ===== 厂商确认表（带图） =====
const showVendorConfirmModal = ref(false)
const vcExporting = ref(false)
const vcLogoInputRef = ref(null)

// 抬头配置
const vcConfig = reactive({
  logoBase64: '',
  companyName: '',
  address: '',
  phone: '',
  title: '厂商确认表'
})

// 字段列表（复用导出字段，默认选中常用字段）
const vcFields = ref([])
const defaultVcKeys = ['sampleCode', 'factoryCode', 'sampleName', 'factoryPrice', 'packagingCn', 'cartonCapacity', 'supplier', 'boothNo', 'remark']

const initVcFields = () => {
  vcFields.value = EXPORT_FIELD_CONFIG.map(f => ({
    ...f,
    checked: defaultVcKeys.includes(f.key)
  }))
  // 恢复本地保存的配置
  loadVcConfigFromLocal()
}

const visibleVcFields = computed(() => vcFields.value.filter(f => f.checked))
const checkedVcFieldCount = computed(() => vcFields.value.filter(f => f.checked).length)

const selectAllVcFields = () => vcFields.value.forEach(f => f.checked = true)
const deselectAllVcFields = () => vcFields.value.forEach(f => f.checked = false)

// 预览数据：从表格中获取勾选行的数据
const vcPreviewData = computed(() => {
  if (!gridRef.value || selectedIds.value.length === 0) return []
  const records = gridRef.value.getCheckboxRecords() || []
  return records.map(r => ({ id: r.id, ...r }))
})

// Logo 上传
const onVcLogoUpload = (e) => {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => { vcConfig.logoBase64 = ev.target.result }
  reader.readAsDataURL(file)
  e.target.value = ''
}

// 配置持久化
const saveVcConfigToLocal = () => {
  const obj = { ...vcConfig, fields: vcFields.value.filter(f => f.checked).map(f => f.key) }
  localStorage.setItem('vendor_confirm_config', JSON.stringify(obj))
  showToast('配置已保存', 'success')
}

const loadVcConfigFromLocal = () => {
  try {
    const saved = JSON.parse(localStorage.getItem('vendor_confirm_config') || '{}')
    if (saved.companyName) vcConfig.companyName = saved.companyName
    if (saved.address) vcConfig.address = saved.address
    if (saved.phone) vcConfig.phone = saved.phone
    if (saved.title) vcConfig.title = saved.title
    if (saved.logoBase64) vcConfig.logoBase64 = saved.logoBase64
    if (saved.fields && saved.fields.length > 0) {
      const keySet = new Set(saved.fields)
      vcFields.value.forEach(f => { f.checked = keySet.has(f.key) })
    }
  } catch (e) {}
}

// 打开厂商确认报表（新标签页）
const showTemplateSelect = ref(false)
const availableTemplates = ref([])
const selectedTemplateId = ref('')
const templateSearchKeyword = ref('')
const vcSessionLoading = ref(false)

const filteredTemplates = computed(() => {
  const kw = templateSearchKeyword.value.trim().toLowerCase()
  if (!kw) return availableTemplates.value
  return availableTemplates.value.filter(tpl =>
    (tpl.title || '').toLowerCase().includes(kw) ||
    (tpl.description || '').toLowerCase().includes(kw)
  )
})

const openVendorConfirmReport = async () => {
  const records = gridRef.value?.getCheckboxRecords() || []
  if (records.length === 0) {
    showAlertDialog('请先勾选要打印的样品', 'warn')
    return
  }
  // 从后端加载已保存的报表模板
  let templates = []
  try {
    const resp = await api('/report-templates/all')
    if (resp.code === 200) {
      templates = resp.data || []
    }
  } catch (e) {
    console.error('加载模板失败', e)
  }

  if (templates.length === 0) {
    showAlertDialog('未找到报表模板，请先在报表设计器中设计模板并「保存为模板」', 'warn')
    return
  }
  availableTemplates.value = templates
  selectedTemplateId.value = ''
  templateSearchKeyword.value = ''
  // 关闭其他打印下拉
  showPrintDropdown.value = false
  showTemplateSelect.value = true
}

// 用户选择模板后，创建会话并打开预览
const confirmTemplateAndOpen = async () => {
  if (!selectedTemplateId.value) return
  const records = gridRef.value?.getCheckboxRecords() || []
  const sampleIds = records.map(r => r.id)
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')

  vcSessionLoading.value = true
  try {
    const resp = await fetch('/samples/vendor-confirm-session', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + (token || '') },
      body: JSON.stringify({ sampleIds })
    })
    const result = await resp.json()
    const cacheKey = result.key
    const params = new URLSearchParams()
    if (token) params.set('token', token)
    params.set('key', cacheKey)
    params.set('templateId', selectedTemplateId.value)
    params.set('viewOnly', '1')
    const url = `/#/report/designer?${params.toString()}`
    showTemplateSelect.value = false
    window.open(url, '_blank')
  } catch (e) {
    console.error('厂商确认表打开失败', e)
    showAlertDialog('打开失败，请稍后重试', 'error')
  } finally {
    vcSessionLoading.value = false
  }
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
  // 清理进度条定时器
  if (reportModalProgressTimer) {
    clearInterval(reportModalProgressTimer)
    reportModalProgressTimer = null
  }
  // 断开分页观察器
  if (paginationObserver) {
    paginationObserver.disconnect()
    paginationObserver = null
  }
  // 断开内容就绪观察器
  if (contentReadyObserver) {
    contentReadyObserver.disconnect()
    contentReadyObserver = null
  }
}

let paginationObserver = null
let reportModalProgressTimer = null
let contentReadyObserver = null // 监听内容渲染完成

const onReportIframeLoad = () => {
  const elapsed = ((Date.now() - reportModalStartTime) / 1000).toFixed(1)
  console.log(`[厂商确认表] iframe load事件触发，已耗时: ${elapsed}秒，开始注入CSS...`)
  // 注入CSS到iframe内（同源后应该能访问）
  try {
    const frame = document.getElementById('reportIframe')
    if (!frame) return
    const doc = frame.contentDocument || frame.contentWindow.document
    if (!doc) {
      console.warn('无法访问iframe文档，可能仍存在跨域')
      reportModalLoading.value = false
      return
    }

    // 监听iframe内部内容渲染完成（表格出现时）
    const checkContentReady = () => {
      const table = doc.querySelector('table')
      if (table && table.rows && table.rows.length > 1) {
        const elapsed = ((Date.now() - reportModalStartTime) / 1000).toFixed(1)
        console.log(`[厂商确认表] 报表内容渲染完成，总耗时: ${elapsed}秒 (表格${table.rows.length}行)`)
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

    // 启动内容就绪检测
    contentReadyObserver = new MutationObserver(() => { checkContentReady() })
    contentReadyObserver.observe(doc.body, { childList: true, subtree: true })
    // 立即检查一次（表格可能已存在）
    checkContentReady()

    // 超时兜底（最多等10秒）
    setTimeout(() => {
      if (reportModalLoading.value) {
        const elapsed = ((Date.now() - reportModalStartTime) / 1000).toFixed(1)
        console.log(`[厂商确认表] 超时兜底关闭loading，已等待: ${elapsed}秒`)
        reportModalLoading.value = false
        finishProgress()
        if (contentReadyObserver) {
          contentReadyObserver.disconnect()
          contentReadyObserver = null
        }
      }
    }, 10000)

    // 隐藏查询栏
    const style = doc.createElement('style')
    style.textContent = `
      /* 隐藏查询栏 */
      div[class*="search"], div[class*="query"], div[class*="filter"],
      div[class*="Search"], div[class*="Query"], div[class*="Filter"],
      [class*="-search-"], [class*="-query-"],
      form[class*="search"], form[class*="query"] {
        display: none !important;
      }
    `
    doc.head.appendChild(style)

    // MutationObserver实时隐藏分页
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

    // 缩放
    const containerEl = frame.parentElement // .report-modal-body
    const bodyWidth = doc.body.scrollWidth || doc.documentElement.scrollWidth
    console.log('报表原始宽度:', bodyWidth, '模态框容器宽度:', containerEl?.clientWidth)
    if (containerEl && bodyWidth > 0) {
      const containerWidth = containerEl.clientWidth - 4 // 留2px边距
      const ratio = Math.min(1, containerWidth / bodyWidth)
      console.log('缩放比例:', ratio)
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
        // 调整iframe高度以适应缩放后的内容
        frame.style.height = `${containerEl.clientHeight / ratio + 60}px`
      }
    }
    console.log('报表CSS注入成功')
  } catch (e) {
    console.warn('CSS注入失败:', e.message)
  }
}

const doReportPrint = () => {
  const frame = document.getElementById("reportIframe")
  if (!frame) return
  try {
    const doc = frame.contentDocument || frame.contentWindow.document
    if (!doc || !doc.body) { frame.contentWindow.print(); return }
    // 临时移除zoom并展开iframe为完整尺寸
    const savedFrameW = frame.style.width
    const savedFrameH = frame.style.height
    const savedBodyZoom = doc.body.style.zoom
    const savedBodyTransform = doc.body.style.transform
    // 找到并移除zoom相关style标签
    const removedStyles = []
    doc.querySelectorAll('style').forEach(s => {
      if (s.textContent && /zoom|transform.*scale/.test(s.textContent)) {
        removedStyles.push(s)
        s.remove()
      }
    })
    // 展开iframe到完整内容尺寸
    const fullW = doc.body.scrollWidth || doc.documentElement.scrollWidth || 1500
    const fullH = doc.body.scrollHeight || doc.documentElement.scrollHeight || 2000
    frame.style.width = (fullW + 40) + 'px'
    frame.style.height = (fullH + 40) + 'px'
    doc.body.style.zoom = '1'
    doc.body.style.transform = 'none'
    // 注入打印专用样式
    const ps = doc.createElement('style')
    ps.id = 'print-temp'
    ps.textContent = `@media print{@page{size:landscape;margin:8mm}*{overflow:visible!important}}`
    doc.head.appendChild(ps)
    setTimeout(() => {
      frame.contentWindow.focus()
      frame.contentWindow.print()
      // 打印后恢复
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

// ESC关闭报表模态框
const onReportEscKey = (e) => {
  if (e.key === 'Escape' && showReportModal.value) {
    closeReportModal()
  }
}

// 打开模态框（保留旧入口，如需使用原弹窗可改名调用）
const openVendorConfirmModal = () => {
  initVcFields()
  showVendorConfirmModal.value = true
}

// 导出厂商确认表
const doVendorConfirmExport = async () => {
  if (checkedVcFieldCount.value === 0 || selectedIds.value.length === 0) return
  vcExporting.value = true
  try {
    const fields = visibleVcFields.value.map(f => f.key)
    const resp = await fetch('/samples/vendor-confirm', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + (localStorage.getItem('token') || '') },
      body: JSON.stringify({
        ids: selectedIds.value,
        fields,
        header: {
          companyName: vcConfig.companyName,
          address: vcConfig.address,
          phone: vcConfig.phone,
          title: vcConfig.title,
          logoBase64: vcConfig.logoBase64 ? vcConfig.logoBase64.split(',')[1] : ''
        }
      })
    })
    if (!resp.ok) throw new Error('导出失败')
    const blob = await resp.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const today = new Date()
    const dateStr = today.getFullYear() + String(today.getMonth() + 1).padStart(2, '0') + String(today.getDate()).padStart(2, '0')
    a.download = '厂商确认表' + dateStr + '.xlsx'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    showVendorConfirmModal.value = false
    selectedIds.value = []
    if (gridRef.value) gridRef.value.setCheckboxRow([], false)
  } catch (e) {
    console.error('厂商确认表导出失败', e)
    showToast('导出失败，请重试', 'error')
  } finally {
    vcExporting.value = false
  }
}

const printTable = () => {
  showMoreDropdown.value = false
  window.print()
}

const doPrintTable = () => {
  showPrintDropdown.value = false
  const records = gridRef.value ? gridRef.value.getCheckboxRecords() : []
  if (!records || records.length === 0) {
    showToast('请先勾选要打印的样品数据', 'warn')
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
      showToast('没有数据可打印', 'warn')
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
      showToast('请先勾选要打印的样品数据', 'warn')
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
    showToast('请先勾选要打印的样品数据', 'warn')
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
    showToast('没有要打印的数据', 'warn')
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
    showToast('没有有效的打印张数', 'warn')
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

const ADV_SEARCH_KEY = 'sample_adv_search_form'

const defaultAdvForm = () => ({
  manufacturerCode: '', supplier: '', contactPerson: '',
  contactPhone: '', mobile: '', sampleName: '',
  sampleCode: '', factoryCode: '', boothNo: '',
  factoryPriceMin: null, factoryPriceMax: null, category: '', categoryCode: '',
  cartonCapacityMin: null, cartonCapacityMax: null, packageCode: '', packagingCn: '',
  certification: '', infringement: '', hasImage: false,
  sampleLengthMin: null, sampleLengthMax: null,
  sampleWidthMin: null, sampleWidthMax: null, sampleHeightMin: null, sampleHeightMax: null,
  packageLengthMin: null, packageLengthMax: null,
  packageWidthMin: null, packageWidthMax: null, packageHeightMin: null, packageHeightMax: null,
  cartonLengthMin: null, cartonLengthMax: null,
  cartonWidthMin: null, cartonWidthMax: null, cartonHeightMin: null, cartonHeightMax: null,
  innerBoxCountMin: null, innerBoxCountMax: null, batteryInfo: '', keyword: ''
})

const advForm = reactive(defaultAdvForm())

const saveAdvForm = () => {
  try {
    localStorage.setItem(ADV_SEARCH_KEY, JSON.stringify({ ...advForm }))
  } catch (e) { /* ignore quota */ }
}

const restoreAdvForm = () => {
  try {
    const raw = localStorage.getItem(ADV_SEARCH_KEY)
    if (!raw) return false
    const saved = JSON.parse(raw)
    Object.keys(defaultAdvForm()).forEach(k => {
      if (saved.hasOwnProperty(k)) advForm[k] = saved[k]
    })
    return true
  } catch (e) { return false }
}

const clearAdvForm = () => {
  const def = defaultAdvForm()
  Object.keys(def).forEach(k => { advForm[k] = def[k] })
  try { localStorage.removeItem(ADV_SEARCH_KEY) } catch (e) {}
}

const openAdvancedSearch = () => {
  clearAdvForm()
  showAdvancedSearch.value = true
}

const onImportFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  importFile.value = file
  // 选择文件后立即显示进度条，不等 FileReader 回调
  importParsing.value = true
  importParsingStage.value = '正在读取文件...'
  importParsingProgress.value = 5
  const parsingStartTime = Date.now()
  try {
    await parseExcelFile(file, parsingStartTime)
  } catch (err) {
    // parseExcelFile 内部已处理 toast
    importParsing.value = false
    importParsingProgress.value = 0
    importParsingStage.value = ''
  }
}

const parseExcelFile = (file, parsingStartTime) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = async (e) => {
      const arrayBuf = e.target.result

      // 创建 Web Worker，将 XLSX 解析放到后台线程执行
      const worker = new ExcelParserWorker()

      worker.onmessage = async (ev) => {
        const msg = ev.data
        if (msg.type === 'progress') {
          importParsingStage.value = msg.stage
          importParsingProgress.value = msg.progress
        } else if (msg.type === 'error') {
          worker.terminate()
          importParsing.value = false
          importParsingProgress.value = 0
          importParsingStage.value = ''
          showToast(msg.message || 'Excel 解析失败', 'warn')
          resolve()
        } else if (msg.type === 'result') {
          worker.terminate()
          const { jsonData } = msg

          try {
            importParsingStage.value = '正在检测表头...'
            importParsingProgress.value = 55
            if (jsonData.length === 0) {
              importParsing.value = false
              importParsingProgress.value = 0
              importParsingStage.value = ''
              showToast('Excel 文件为空', 'warn')
              resolve()
              return
            }

            // 自动检测表头行：扫描前5行，匹配别名最多的作为表头
            let bestRow = 0, bestMatch = 0
            let bestHeaders = []
            const scanLimit = Math.min(5, jsonData.length)
            for (let r = 0; r < scanLimit; r++) {
              const candidate = jsonData[r]
              if (!candidate || candidate.every(c => !c)) continue
              let match = 0
              const tentative = candidate.map(c => String(c || '').trim())
              tentative.forEach(h => { if (h && resolveHeader(h)) match++ })
              if (match > bestMatch) {
                bestMatch = match
                bestRow = r
                bestHeaders = tentative
              }
            }
            const headers = bestHeaders
            const dataStartRow = bestRow + 1
            importPreviewHeaders.value = headers

            const rows = []
            const totalDataRows = jsonData.length - dataStartRow
            importParsingStage.value = `正在提取数据 (${totalDataRows} 行)...`
            importParsingProgress.value = 65
            for (let i = dataStartRow; i < jsonData.length; i++) {
              const rawRow = jsonData[i]
              if (!rawRow || rawRow.every(cell => !cell && cell !== 0)) continue

              const rowObj = { _rowIndex: i, _status: 'pending', _warnings: [] }
              headers.forEach((header, idx) => {
                const fieldName = resolveHeader(header)
                if (fieldName) {
                  rowObj[fieldName] = rawRow[idx] != null ? String(rawRow[idx]).trim() : ''
                }
              })
              // 复合列拆分
              applySplits(rowObj)
              rows.push(markRaw(rowObj))
            }

            // 预加载对照表（一次请求复用匹配+校验）
            importParsingStage.value = '正在加载种类/包装对照表...'
            importParsingProgress.value = 70
            let catList = [], pkgList = []
            try {
              const [catRes, pkgRes] = await Promise.all([
                api('/product-categories/all'),
                api('/packaging-methods/all')
              ])
              catList = Array.isArray(catRes?.data) ? catRes.data : []
              pkgList = Array.isArray(pkgRes?.data) ? pkgRes.data : []
            } catch (e) { console.warn('对照表加载失败', e) }

            // 构建种类关键词索引：一次性预拆词
            const catKwsIndex = catList.map(cat => {
              const rawKws = cat.keywords || cat.name || ''
              let kws = rawKws.split(/[,，]/).map(k => k.trim().toLowerCase()).filter(Boolean)
              if (!cat.keywords && cat.name) {
                for (let i = 0; i < cat.name.length - 1; i++) {
                  const sub = cat.name.substring(i, i + 2).toLowerCase()
                  if (!kws.includes(sub)) kws.push(sub)
                }
              }
              return { ...cat, _kws: kws }
            })

            // 自动匹配种类（预索引免逐个拆词）
            importParsingStage.value = '正在自动匹配种类...'
            importParsingProgress.value = 80
            let autoMatched = 0
            if (catKwsIndex.length > 0) {
              rows.forEach(row => {
                if (row.category && row.category.trim()) return
                const pname = (row.sampleName || '').trim().toLowerCase()
                if (!pname) return
                for (const cat of catKwsIndex) {
                  if (cat._kws.some(kw => pname.includes(kw))) {
                    row.category = cat.name
                    row.categoryCode = cat.code
                    autoMatched++
                    break
                  }
                }
              })
              if (autoMatched > 0) showToast(`已自动匹配 ${autoMatched} 条种类`, 'success')
            }

            // 自动匹配包装：从原始中文包装匹配包装方式表
            importParsingStage.value = '正在自动匹配包装...'
            importParsingProgress.value = 85
            let pkgAutoMatched = 0
            if (pkgList.length > 0) {
              // 构建包装关键词索引
              const pkgKwsIndex = pkgList.map(pkg => {
                const name = (pkg.name || '').trim()
                const kws = [name.toLowerCase()]
                // 2-gram 滑动窗口拆词："开窗盒" → ["开窗盒", "开窗", "窗盒"]
                for (let i = 0; i < name.length - 1; i++) {
                  const sub = name.substring(i, i + 2).toLowerCase()
                  if (!kws.includes(sub)) kws.push(sub)
                }
                return { ...pkg, _kws: kws }
              })
              // 按关键词长度降序排列，优先匹配长关键词（"开窗盒" 优先于 "盒"）
              pkgKwsIndex.sort((a, b) => {
                const aMax = Math.max(...a._kws.map(k => k.length))
                const bMax = Math.max(...b._kws.map(k => k.length))
                return bMax - aMax
              })
              rows.forEach(row => {
                if (row.packagingCn && row.packagingCn.trim()) return  // 已有匹配包装则跳过
                const orig = (row.originalPackagingCn || '').trim().toLowerCase()
                if (!orig) return
                for (const pkg of pkgKwsIndex) {
                  if (pkg._kws.some(kw => orig.includes(kw))) {
                    row.packagingCn = pkg.name
                    row.packageCode = pkg.code
                    pkgAutoMatched++
                    break
                  }
                }
                // 没匹配到则用原始值作为中文包装
                if (!row.packagingCn) row.packagingCn = row.originalPackagingCn
              })
              if (pkgAutoMatched > 0) showToast(`已自动匹配 ${pkgAutoMatched} 条包装`, 'success')
            }

            // 百度翻译：英文名称 + 英文包装（批量请求一次搞定）
            importParsingStage.value = '正在自动翻译英文...'
            importParsingProgress.value = 88
            const translateTexts = []      // 待翻译文本
            const translateTargets = []    // 对应的 [row, field]
            rows.forEach(row => {
              // 英文名称为空时，翻译样品名称
              if (!row.englishName || !row.englishName.trim()) {
                const src = (row.sampleName || '').trim()
                if (src) {
                  translateTexts.push(src)
                  translateTargets.push([row, 'englishName'])
                }
              }
              // 英文包装为空时，翻译中文包装
              if (!row.packagingEn || !row.packagingEn.trim()) {
                const src = (row.packagingCn || row.originalPackagingCn || '').trim()
                if (src) {
                  translateTexts.push(src)
                  translateTargets.push([row, 'packagingEn'])
                }
              }
              // 英文备注为空时，翻译中文备注
              if (!row.remarkEn || !row.remarkEn.trim()) {
                const src = (row.remark || '').trim()
                if (src) {
                  translateTexts.push(src)
                  translateTargets.push([row, 'remarkEn'])
                }
              }
            })
            if (translateTexts.length > 0) {
              importParsingStage.value = `正在自动翻译英文 (${translateTexts.length} 条)...`
              const translated = await baiduTranslateBatch(translateTexts)
              if (translated && translated.length === translateTexts.length) {
                translateTargets.forEach(([row, field], i) => {
                  row[field] = translated[i]
                })
                showToast(`已自动翻译 ${translated.length} 条英文`, 'success')
              } else {
                showToast('翻译接口异常，已跳过', 'warning')
              }
            }

            // 校验种类名称和中文包装是否在对照表中
            importParsingStage.value = '正在校验数据...'
            importParsingProgress.value = 92
            let catErrorCount = 0
            let pkgWarnCount = 0
            const validCatNames = new Set(catList.map(r => r.name).filter(Boolean))
            const validPkgNames = new Set(pkgList.map(r => r.name).filter(Boolean))
            importValidCatNames.value = validCatNames
            importValidPkgNames.value = validPkgNames
            importCatList.value = catList   // 缓存完整种类列表供编辑时查找编码
            importPkgList.value = pkgList  // 缓存完整包装列表供编辑时重新匹配

            for (const row of rows) {
              const catName = row.category
              const pkgName = row.packagingCn
              let hasCatErr = false, hasPkgWarn = false
              if (catName && !validCatNames.has(catName)) {
                row._warnings.push(`种类名称「${catName}」不在对照表中`)
                row._status = 'cat_error'
                hasCatErr = true
              }
              if (pkgName && !validPkgNames.has(pkgName)) {
                row._warnings.push(`中文包装「${pkgName}」不在对照表中`)
                if (!hasCatErr) row._status = 'pkg_warning'
                hasPkgWarn = true
              }
              if (hasCatErr) catErrorCount++
              if (hasPkgWarn) pkgWarnCount++
            }

            importPreviewAllRows.value = rows
            importOriginalData.value = rows.map(r => markRaw({ ...r, _warnings: [...(r._warnings || [])] }))
            importParsingStage.value = '解析完成，正在渲染预览...'
            importParsingProgress.value = 95
            importSelectedRows.value = []
            importSelectedRowIndexes.value = new Set()
            importPreviewCatFilter.value = false
            importPreviewPkgFilter.value = false
            importPreviewPage.value = 1
            syncPreviewPage()
            importParsingProgress.value = 100
            importParsingStage.value = '完成'
            // 确保进度条至少显示 600ms，小文件也能感知进度
            const elapsed = Date.now() - parsingStartTime
            const minDelay = Math.max(0, 600 - elapsed)
            setTimeout(() => {
              importParsing.value = false
              showImportModal.value = false
              showImportPreview.value = true
            }, minDelay)

            if (catErrorCount > 0 || pkgWarnCount > 0) {
              const msgs = []
              if (catErrorCount > 0) msgs.push(`${catErrorCount} 行种类名称不符`)
              if (pkgWarnCount > 0) msgs.push(`${pkgWarnCount} 行中文包装不符`)
              showToast(msgs.join('，') + '，请核实', 'warn')
            }
            resolve()
          } catch (err) {
            importParsing.value = false
            importParsingProgress.value = 0
            importParsingStage.value = ''
            console.error('解析 Excel 失败:', err)
            showToast('解析 Excel 文件失败: ' + err.message, 'error')
            reject(err)
          }
        }

      }

      worker.onerror = (err) => {
        worker.terminate()
        importParsing.value = false
        importParsingProgress.value = 0
        importParsingStage.value = ''
        console.error('Worker 错误:', err)
        showToast('Excel 解析失败', 'error')
        reject(new Error('Worker error'))
      }

      worker.postMessage({ type: 'parse', buffer: arrayBuf }, [arrayBuf])
    }

    reader.onerror = () => {
      importParsing.value = false
      importParsingProgress.value = 0
      importParsingStage.value = ''
      reject(new Error('文件读取失败'))
    }
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
  // 还原整行：直接重新解析标记即可，无需存储原始副本
  const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
  if (idx >= 0) {
    // 用原始解析数据的浅拷贝来还原（原始数据在第一次解析时就存好了）
    const orig = importOriginalData.value.find(r => r._rowIndex === row._rowIndex)
    if (orig) {
      importPreviewAllRows.value.splice(idx, 1, markRaw({ ...orig }))
      syncPreviewPage()
    }
  }
}

const deletePreviewRow = (row) => {
  const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
  if (idx >= 0) {
    importPreviewAllRows.value.splice(idx, 1)
    importSelectedRowIndexes.value.delete(row._rowIndex)
    importSelectedRows.value = [...importSelectedRowIndexes.value]
    syncPreviewPage()
    onImportPreviewCheckChange()
  }
}

const deleteSelectedPreviewRows = () => {
  if (!importPreviewGridRef.value) return
  const selectedRecords = importPreviewGridRef.value.getCheckboxRecords()
  if (selectedRecords.length === 0) return
  const rowIndexes = new Set(selectedRecords.map(r => r._rowIndex))
  importPreviewAllRows.value = importPreviewAllRows.value.filter(r => !rowIndexes.has(r._rowIndex))
  importPreviewGridRef.value.clearCheckboxRow()
  importSelectedRows.value = []
  importSelectedRowIndexes.value = new Set()
  syncPreviewPage()
}

const batchEditRun = async () => {
  const val = batchEditValue.value.trim()
  const field = batchEditField.value
  if (!val) return
  const selected = importPreviewAllRows.value.filter(r => importSelectedRowIndexes.value.has(r._rowIndex))
  if (selected.length === 0) return
  const fieldLabel = batchEditFields.find(f => f.value === field)?.label || field

  // 包装字段：同步编号和英文
  let matchedPkg = null
  if (field === 'packagingCn') {
    matchedPkg = importPkgList.value.find(p => p.name === val)
  }
  // 种类字段：同步种类编号
  let matchedCat = null
  if (field === 'category') {
    matchedCat = importCatList.value.find(c => c.name === val)
  }

  const needsTranslate = field === 'packagingCn'

  selected.forEach(row => {
    row[field] = val
    if (field === 'packagingCn' && matchedPkg) {
      row.packageCode = matchedPkg.code
      row.packagingEn = matchedPkg.nameEn || ''
    }
    if (field === 'category' && matchedCat) {
      row.categoryCode = matchedCat.code
    }
    // 清除旧警告
    if (field === 'packagingCn') {
      row._warnings = row._warnings.filter(w => !w.startsWith('中文包装'))
    } else if (field === 'category') {
      row._warnings = row._warnings.filter(w => !w.startsWith('种类'))
    }
    // 校验
    if (field === 'category') {
      const hasCatErr = val && !importValidCatNames.value.has(val)
      if (hasCatErr) {
        row._warnings.push(`种类「${val}」不在对照表中`)
        row._status = 'cat_error'
      } else {
        row._status = 'pending'
      }
    } else if (field === 'packagingCn') {
      const catName = row.category
      const hasCatErr = catName && !importValidCatNames.value.has(catName)
      if (val && !importValidPkgNames.value.has(val)) {
        row._warnings.push(`中文包装「${val}」不在对照表中`)
        if (!hasCatErr) row._status = 'pkg_warning'
      } else if (!hasCatErr) {
        row._status = 'pending'
      }
    }
    // 同步到全量
    const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
    if (idx >= 0) importPreviewAllRows.value.splice(idx, 1, markRaw({ ...row }))
  })

  batchEditValue.value = ''
  syncPreviewPage()

  // 翻译同步：中文包装改完后，翻译英文包装
  let translatedCount = 0
  if (needsTranslate && !matchedPkg) {
    // 不在对照表中的才需要翻译
    const translateTargets = selected.map(r => [r, 'packagingEn'])
    const translateTexts = selected.map(r => val)
    try {
      const translated = await baiduTranslateBatch(translateTexts)
      if (translated && translated.length === translateTexts.length) {
        translateTargets.forEach(([row, fieldName], i) => {
          row[fieldName] = translated[i]
          // 同步到全量
          const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
          if (idx >= 0) importPreviewAllRows.value.splice(idx, 1, markRaw({ ...row }))
        })
        translatedCount = translated.length
      }
    } catch (e) {
      // 翻译失败跳过
    }
  }

  let msg = `已批量修改 ${selected.length} 条${fieldLabel}为「${val}」`
  if (field === 'packagingCn' && matchedPkg) {
    msg += `，已同步包装编号「${matchedPkg.code}」${matchedPkg.nameEn ? '、英文包装「' + matchedPkg.nameEn + '」' : ''}`
  } else if (translatedCount > 0) {
    msg += `，已自动翻译 ${translatedCount} 条英文包装`
  }
  if (field === 'category' && matchedCat) {
    msg += `，已同步种类编号「${matchedCat.code}」`
  }
  showToast(msg, 'success')
}

// 翻译勾选行的中文包装 → 英文包装（覆盖写）
const batchTranslateSelected = async () => {
  const selected = importPreviewAllRows.value.filter(r => importSelectedRowIndexes.value.has(r._rowIndex))
  if (selected.length === 0) return
  const texts = selected.map(r => (r.packagingCn || '').trim()).filter(Boolean)
  if (texts.length === 0) { showToast('所选行没有中文包装内容', 'warning'); return }
  try {
    const translated = await baiduTranslateBatch(texts)
    if (translated && translated.length === texts.length) {
      let j = 0
      selected.forEach(row => {
        const src = (row.packagingCn || '').trim()
        if (src) {
          row.packagingEn = translated[j++]
          const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
          if (idx >= 0) importPreviewAllRows.value.splice(idx, 1, markRaw({ ...row }))
        }
      })
      syncPreviewPage()
      showToast(`已翻译 ${texts.length} 条中文包装→英文包装`, 'success')
    }
  } catch (e) {
    showToast('翻译失败，请稍后重试', 'error')
  }
}

const importRowClassName = ({ row }) => {
  if (row._status === 'cat_error') return 'import-row-cat-error'
  if (row._status === 'pkg_warning') return 'import-row-pkg-warning'
  return ''
}

const onImportPreviewCheckChange = () => {
  if (importPreviewGridRef.value) {
    const records = importPreviewGridRef.value.getCheckboxRecords()
    // 当前页勾选的 _rowIndex
    const currentPageIndexes = new Set(records.map(r => r._rowIndex))
    // 跨页全集：移除当前页的选中（用新状态替换），加入当前页勾选的
    const currentPageRows = importPreviewData.value
    currentPageRows.forEach(r => importSelectedRowIndexes.value.delete(r._rowIndex))
    currentPageIndexes.forEach(idx => importSelectedRowIndexes.value.add(idx))
    importSelectedRows.value = [...importSelectedRowIndexes.value]
  }
}

// 编辑单元格后重新校验该行的种类/包装
const onImportCellEdit = ({ row, column }) => {
  const field = column?.field || column?.property
  if (field !== 'category' && field !== 'packagingCn' && field !== 'originalPackagingCn') return

  // 编辑原始中文包装时，重新关键词匹配
  if (field === 'originalPackagingCn') {
    const orig = (row.originalPackagingCn || '').trim().toLowerCase()
    const pkgList = importPkgList.value
    if (orig && pkgList.length > 0) {
      const pkgKwsIndex = pkgList.map(pkg => {
        const name = (pkg.name || '').trim()
        const kws = [name.toLowerCase()]
        for (let i = 0; i < name.length - 1; i++) {
          const sub = name.substring(i, i + 2).toLowerCase()
          if (!kws.includes(sub)) kws.push(sub)
        }
        return { ...pkg, _kws: kws }
      })
      pkgKwsIndex.sort((a, b) => {
        const aMax = Math.max(...a._kws.map(k => k.length))
        const bMax = Math.max(...b._kws.map(k => k.length))
        return bMax - aMax
      })
      for (const pkg of pkgKwsIndex) {
        if (pkg._kws.some(kw => orig.includes(kw))) {
          row.packagingCn = pkg.name
          row.packageCode = pkg.code
          break
        }
      }
      if (!row.packagingCn) row.packagingCn = row.originalPackagingCn
    }
  }

  // 清除该行原有校验警告
  row._warnings = row._warnings.filter(w => !w.startsWith('种类名称') && !w.startsWith('中文包装'))

  const catName = row.category
  const pkgName = row.packagingCn
  let hasCatErr = false, hasPkgWarn = false

  if (catName && !importValidCatNames.value.has(catName)) {
    row._warnings.push(`种类名称「${catName}」不在对照表中`)
    hasCatErr = true
  }
  if (pkgName && !importValidPkgNames.value.has(pkgName)) {
    row._warnings.push(`中文包装「${pkgName}」不在对照表中`)
    if (!hasCatErr) hasPkgWarn = true
  }

  row._status = hasCatErr ? 'cat_error' : (hasPkgWarn ? 'pkg_warning' : 'pending')

  // 同步到全量数据并刷新当前页
  const idx = importPreviewAllRows.value.findIndex(r => r._rowIndex === row._rowIndex)
  if (idx >= 0) {
    importPreviewAllRows.value.splice(idx, 1, markRaw({ ...row }))
  }
  syncPreviewPage()
}

const exportSelectedRows = () => {
  if (importSelectedRows.value.length === 0) {
    showToast('请先选择要导出的行', 'warn')
    return
  }
  showToast('已选择 ' + importSelectedRows.value.length + ' 行数据准备导出（功能开发中）', 'info')
}

const selectAllPreviewRows = () => {
  // 全选当前筛选结果的所有行（跨页）
  let list = importPreviewAllRows.value
  if (importPreviewCatFilter.value) list = list.filter(r => r._status === 'cat_error')
  if (importPreviewPkgFilter.value) list = list.filter(r => r._status === 'pkg_warning' || r._status === 'cat_error')
  list.forEach(r => importSelectedRowIndexes.value.add(r._rowIndex))
  importSelectedRows.value = [...importSelectedRowIndexes.value]
  // 同步当前页 UI
  if (importPreviewGridRef.value) {
    importPreviewGridRef.value.setAllCheckboxRow(true)
  }
}

const clearPreviewSelection = () => {
  importSelectedRowIndexes.value.clear()
  importSelectedRows.value = []
  if (importPreviewGridRef.value) {
    importPreviewGridRef.value.clearCheckboxRow()
  }
}

const cancelImportPreview = () => {
  showImportPreview.value = false
  importPreviewAllRows.value = []
  importPreviewData.value = []
  importPreviewHeaders.value = []
  importSelectedRows.value = []
  importSelectedRowIndexes.value = new Set()
  importOriginalData.value = []  // 释放深拷贝内存
  importPkgList.value = []
  importCatList.value = []
  importPreviewCatFilter.value = false
  importPreviewPkgFilter.value = false
  importPreviewPage.value = 1
  batchEditValue.value = ''
  importFile.value = null
}

const doConfirmImport = (mode) => {
  const filteredData = (() => {
    let list = importPreviewAllRows.value
    if (importPreviewCatFilter.value) list = list.filter(r => r._status === 'cat_error')
    if (importPreviewPkgFilter.value) list = list.filter(r => r._status === 'pkg_warning' || r._status === 'cat_error')
    return list
  })()
  const count = mode === 'all' ? filteredData.length : importSelectedRowIndexes.value.size
  if (count === 0) {
    showToast('请至少选择一行数据进行导入', 'warn')
    return
  }
  importConfirmCount.value = count
  showImportConfirmModal.value = true
}

const INFRINGEMENT_MAP = { '1': '侵权', '2': '不侵权' }

// 预缓存：避免每条记录都调用 Object.keys(HEADER_TO_FIELD)
const HEADER_FIELD_KEYS = Object.keys(HEADER_TO_FIELD).filter(h => !HEADER_TO_FIELD[h].startsWith('_'))

const buildSamplesToSend = (records) => {
  return records.map(row => {
    const sample = {}
    HEADER_FIELD_KEYS.forEach(header => {
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
    // packagingCn 和 originalPackagingCn 需要双双发送
    if (row.packagingCn) sample.packagingCn = row.packagingCn
    return sample
  })
}

const BATCH_SIZE = 50

const executeImport = async () => {
  showImportConfirmModal.value = false
  const filteredData = (() => {
    let list = importPreviewAllRows.value
    if (importPreviewCatFilter.value) list = list.filter(r => r._status === 'cat_error')
    if (importPreviewPkgFilter.value) list = list.filter(r => r._status === 'pkg_warning' || r._status === 'cat_error')
    return list
  })()
  const recordsToImport = importConfirmCount.value === filteredData.length
    ? filteredData
    : filteredData.filter(r => importSelectedRowIndexes.value.has(r._rowIndex))
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
    showToast('导入失败: ' + (e.message || '未知错误'), 'error')
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
    showToast('没有需要上传的图片', 'warn')
    return
  }

  let successCount = 0
  let failCount = 0
  let submitDone = 0
  const failList = []

  batchUploading.value = true
  batchUploadProgress.value = { done: 0, total: uploadList.length, success: 0, fail: 0 }

  try {
    // 第一步：清除需要覆盖的旧图片 + 提交所有上传任务（秒回）
    const taskItems = []  // { taskId, sampleId, fileName }
    for (const item of uploadList) {
      // 过滤空文件，避免"文件不能为空"错误
      if (!item.file || item.file.size === 0) {
        failCount++
        submitDone++
        failList.push(`${item.file?.name || '未知文件'}: 文件不能为空`)
        batchUploadProgress.value = { done: submitDone, total: uploadList.length, success: successCount, fail: failCount }
        continue
      }

      if (item.action === 'cover' && item.hasExisting) {
        try {
          await api(`/images/sample/${item.sampleId}`, { method: 'DELETE' })
        } catch (e) {
          console.warn('[批量上传] 清除旧图片失败:', item.file.name, e.message)
        }
      }
      try {
        const fd = new FormData()
        fd.append('file', item.file)
        fd.append('sampleId', item.sampleId)
        const res = await api('/images/upload/async', { method: 'POST', body: fd })
        if (res && res.code === 200 && res.data) {
          taskItems.push({
            taskId: res.data.taskId,
            sampleId: item.sampleId,
            fileName: item.file.name
          })
        } else {
          failCount++
          failList.push(`${item.file.name}: 提交失败`)
        }
      } catch (e) {
        failCount++
        failList.push(`${item.file.name}: ${e.message || '提交失败'}`)
      }
      submitDone++
      batchUploadProgress.value = { done: submitDone, total: uploadList.length, success: successCount, fail: failCount }
    }

    if (taskItems.length === 0) {
      batchResult.successCount = 0
      batchResult.failCount = failCount
      batchResult.unmatchedCount = unmatchedList.length
      batchResult.failList = failList
      batchResult.unmatchedList = unmatchedList
      showBatchResultModal.value = true
      closeBatchModal()
      return
    }

    // 第二步：轮询进度（每2秒一次）
    let pendingIds = taskItems.map(t => t.taskId)
    const MAX_POLL_TIME = 10 * 60 * 1000
    const POLL_INTERVAL = 2000
    const startTime = Date.now()

    while (pendingIds.length > 0 && (Date.now() - startTime) < MAX_POLL_TIME) {
      await new Promise(r => setTimeout(r, POLL_INTERVAL))
      try {
        const pollRes = await api('/images/upload/progress-batch', {
          method: 'POST',
          body: JSON.stringify(pendingIds)
        })
        if (pollRes && pollRes.code === 200 && pollRes.data) {
          const tasks = pollRes.data
          const newPending = []
          for (const t of tasks) {
            if (t.status === 'SUCCESS') {
              successCount++
              const info = taskItems.find(i => i.taskId === t.taskId)
              if (info) {
                const row = tableData.value.find(r => r.id === info.sampleId)
                if (row) {
                  row.thumbnail = t.thumbnailPath
                  row.firstImageId = t.imageId
                }
              }
            } else if (t.status === 'FAILED') {
              failCount++
              const info = taskItems.find(i => i.taskId === t.taskId)
              failList.push(`${info?.fileName || t.taskId}: ${t.errorMsg || '失败'}`)
            } else {
              newPending.push(t.taskId)
            }
          }
          pendingIds = newPending
        }
      } catch (e) {
        console.warn('[批量上传] 轮询失败:', e.message)
      }
      batchUploadProgress.value.done = successCount + failCount
      batchUploadProgress.value.success = successCount
      batchUploadProgress.value.fail = failCount
    }

    if (pendingIds.length > 0) {
      failCount += pendingIds.length
      pendingIds.forEach(id => {
        const info = taskItems.find(i => i.taskId === id)
        failList.push(`${info?.fileName || id}: 超时未完成`)
      })
    }

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
  const f = advForm
  const conditions = []
  const push = (field, op, val) => { if (val !== '' && val != null && val !== false) conditions.push({ field, operator: op, value: String(val) }) }
  const pushLike = (field, val) => push(field, 'like', val)
  const pushEq = (field, val) => push(field, 'eq', val)

  // 文本模糊匹配
  pushLike('manufacturerCode', f.manufacturerCode)
  pushLike('supplier', f.supplier)
  pushLike('contactPerson', f.contactPerson)
  pushLike('contactPhone', f.contactPhone)
  pushLike('mobile', f.mobile)
  pushLike('sampleName', f.sampleName)
  pushLike('sampleCode', f.sampleCode)
  pushLike('factoryCode', f.factoryCode)
  pushLike('boothNo', f.boothNo)
  pushLike('category', f.category)
  if (f.categoryCode) pushEq('categoryCode', f.categoryCode)
  pushLike('packageCode', f.packageCode)
  pushLike('packagingCn', f.packagingCn)
  pushLike('certification', f.certification)
  if (f.infringement !== '') pushEq('infringement', f.infringement)
  pushLike('batteryInfo', f.batteryInfo)
  pushLike('keyword', f.keyword)

  // 范围字段
  if (f.factoryPriceMin != null) push('factoryPrice', 'ge', f.factoryPriceMin)
  if (f.factoryPriceMax != null) push('factoryPrice', 'le', f.factoryPriceMax)
  if (f.cartonCapacityMin != null) push('cartonCapacity', 'ge', f.cartonCapacityMin)
  if (f.cartonCapacityMax != null) push('cartonCapacity', 'le', f.cartonCapacityMax)
  if (f.innerBoxCountMin != null) push('innerBoxCount', 'ge', f.innerBoxCountMin)
  if (f.innerBoxCountMax != null) push('innerBoxCount', 'le', f.innerBoxCountMax)

  // 尺寸范围
  if (f.sampleLengthMin != null) push('sampleLength', 'ge', f.sampleLengthMin)
  if (f.sampleLengthMax != null) push('sampleLength', 'le', f.sampleLengthMax)
  if (f.sampleWidthMin != null) push('sampleWidth', 'ge', f.sampleWidthMin)
  if (f.sampleWidthMax != null) push('sampleWidth', 'le', f.sampleWidthMax)
  if (f.sampleHeightMin != null) push('sampleHeight', 'ge', f.sampleHeightMin)
  if (f.sampleHeightMax != null) push('sampleHeight', 'le', f.sampleHeightMax)
  if (f.packageLengthMin != null) push('packageLength', 'ge', f.packageLengthMin)
  if (f.packageLengthMax != null) push('packageLength', 'le', f.packageLengthMax)
  if (f.packageWidthMin != null) push('packageWidth', 'ge', f.packageWidthMin)
  if (f.packageWidthMax != null) push('packageWidth', 'le', f.packageWidthMax)
  if (f.packageHeightMin != null) push('packageHeight', 'ge', f.packageHeightMin)
  if (f.packageHeightMax != null) push('packageHeight', 'le', f.packageHeightMax)
  if (f.cartonLengthMin != null) push('cartonLength', 'ge', f.cartonLengthMin)
  if (f.cartonLengthMax != null) push('cartonLength', 'le', f.cartonLengthMax)
  if (f.cartonWidthMin != null) push('cartonWidth', 'ge', f.cartonWidthMin)
  if (f.cartonWidthMax != null) push('cartonWidth', 'le', f.cartonWidthMax)
  if (f.cartonHeightMin != null) push('cartonHeight', 'ge', f.cartonHeightMin)
  if (f.cartonHeightMax != null) push('cartonHeight', 'le', f.cartonHeightMax)

  // 图片筛选
  if (f.hasImage) {
    conditions.push({ field: 'image', operator: 'eq', value: '1' })
  }

  // 保存查询条件到本地
  saveAdvForm()
  // 保存活跃查询条件（用于排序时不丢失搜索）
  activeSearchConditions.value = conditions

  try {
    console.log('[ADV_SEARCH] conditions:', JSON.stringify(conditions))
    const res = await api(`/samples/search?current=${currentPage.value}&size=${pageSize.value}&sortField=${currentSortField.value}&sortOrder=${currentSortOrder.value}`, {
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
  window.addEventListener('keydown', onReportEscKey)
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
  window.removeEventListener('keydown', onReportEscKey)
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

// ===== 对照资料管理 =====
const showRefDataModal = ref(false)
const refActiveTab = ref('category')

const openReferenceDataModal = () => {
  showMoreDropdown.value = false
  showRefDataModal.value = true
  refLoadCategories()
  refLoadPackagings()
}

// -- 种类管理（树形） --
const refCategories = ref([]) // 原始扁平数据
const refCatTreeData = ref([]) // 树形数据
const refCatKeyword = ref('')
const refSelectedCatIds = ref([])
const refCatGridRef = ref(null)

const showRefCatForm = ref(false)
const refEditingCat = ref(null)
const refCatForm = reactive({ code: '', name: '', keywords: '', level: 1, parentCode: '' })
const refLevel1Cats = ref([])

function refLoadLevel1Cats() {
  api('/product-categories?current=1&size=500&level=1').then(res => {
    refLevel1Cats.value = Array.isArray(res?.data?.records) ? res.data.records : []
  })
}

function buildTreeData(list) {
  // 统计每个一级类目的子项数
  const childCountMap = {}
  list.filter(r => r.level === 2).forEach(r => {
    const pc = r.parentCode || ''
    childCountMap[pc] = (childCountMap[pc] || 0) + 1
  })
  return list.map(r => ({
    ...r,
    _parentId: r.level === 2 ? null : undefined, // 一级类目 _parentId=undefined 作为根节点
    _ck: false,
    _childCount: r.level === 1 ? (childCountMap[r.code] || 0) : undefined,
    // 二级类目需要找到父级 id 作为 _parentId
    ...(r.level === 2 ? { _parentId: list.find(p => p.code === r.parentCode && p.level === 1)?.id } : {})
  }))
}

async function refLoadCategories() {
  try {
    let url = '/product-categories?current=1&size=500'
    if (refCatKeyword.value) url += `&keyword=${encodeURIComponent(refCatKeyword.value)}`
    const res = await api(url)
    const rawList = Array.isArray(res?.data?.records) ? res.data.records : []
    refCategories.value = rawList
    refCatTreeData.value = buildTreeData(rawList)
    refSelectedCatIds.value = []
  } catch (e) { console.error('加载种类失败', e) }
}

// 实时搜索过滤
let _catFilterTimer = null
function refFilterCategories() {
  clearTimeout(_catFilterTimer)
  _catFilterTimer = setTimeout(() => refLoadCategories(), 300)
}

function refExpandAllCat() {
  if (!refCatGridRef.value) return
  const table = refCatGridRef.value
  const expanded = table.getTreeExpandRecords()
  if (expanded && expanded.length > 0) {
    table.clearTreeExpand()
  } else {
    table.setAllTreeExpand(true)
  }
}

function openRefCategoryAdd() {
  refEditingCat.value = null
  refCatForm.code = ''; refCatForm.name = ''; refCatForm.keywords = ''; refCatForm.level = 1; refCatForm.parentCode = ''
  showRefCatForm.value = true
  refLoadLevel1Cats()
}

function refEditCategory(row) {
  refEditingCat.value = row
  refCatForm.code = row.code; refCatForm.name = row.name; refCatForm.keywords = row.keywords || ''; refCatForm.level = row.level; refCatForm.parentCode = row.parentCode || ''
  showRefCatForm.value = true
  refLoadLevel1Cats()
}

async function refSaveCategory() {
  if (!refCatForm.code.trim() || !refCatForm.name.trim()) { showToast('编号和名称不能为空', 'warn'); return }
  try {
    const body = { code: refCatForm.code.trim(), name: refCatForm.name.trim(), keywords: refCatForm.keywords.trim() || null, level: refCatForm.level, parentCode: refCatForm.level === 2 ? refCatForm.parentCode || null : null }
    if (refEditingCat.value) {
      await api(`/product-categories/${refEditingCat.value.id}`, { method: 'PUT', body: JSON.stringify(body) })
    } else {
      await api('/product-categories', { method: 'POST', body: JSON.stringify(body) })
    }
    showRefCatForm.value = false
    refLoadCategories()
    showToast(refEditingCat.value ? '种类已更新' : '种类已新增', 'success')
  } catch (e) { showToast('保存失败: ' + (e.message || '未知错误'), 'error') }
}

async function refDeleteCategory(row) {
  if (!confirm(`确定删除种类「${row.code} ${row.name}」？`)) return
  try { await api(`/product-categories/${row.id}`, { method: 'DELETE' }); refLoadCategories(); showToast('已删除', 'success') } catch (e) { showToast('删除失败', 'error') }
}

async function refDeleteSelectedCats() {
  if (refSelectedCatIds.value.length === 0) return
  if (!confirm(`确定删除选中的 ${refSelectedCatIds.value.length} 条种类？`)) return
  try { await api('/product-categories/batch-delete', { method: 'POST', body: JSON.stringify(refSelectedCatIds.value) }); refSelectedCatIds.value = []; refLoadCategories(); showToast('已删除', 'success') } catch (e) { showToast('删除失败', 'error') }
}

async function saveRefCatKeywords(row, val) {
  if (row.keywords === val) return
  try {
    await api(`/product-categories/${row.id}`, { method: 'PUT', body: JSON.stringify({ code: row.code, name: row.name, keywords: val || null, level: row.level, parentCode: row.parentCode || null }) })
    row.keywords = val
  } catch (e) { showToast('保存失败: ' + (e.message || '未知错误'), 'error') }
}

// -- 包装管理 --
const refPackagings = ref([])
const refPkgKeyword = ref('')
const refSelectedPkgIds = ref([])
const refPkgGridRef = ref(null)

const showRefPkgForm = ref(false)
const refEditingPkg = ref(null)
const refPkgForm = reactive({ code: '', name: '', nameEn: '' })

function refLoadPackagings() {
  let url = '/packaging-methods?current=1&size=500'
  if (refPkgKeyword.value) url += `&keyword=${encodeURIComponent(refPkgKeyword.value)}`
  api(url).then(res => {
    refPackagings.value = (Array.isArray(res?.data?.records) ? res.data.records : []).map(r => ({ ...r, _ck: false }))
    refSelectedPkgIds.value = []
  })
}

function openRefPackagingAdd() {
  refEditingPkg.value = null
  refPkgForm.code = ''; refPkgForm.name = ''; refPkgForm.nameEn = ''
  showRefPkgForm.value = true
}

function refEditPackaging(row) {
  refEditingPkg.value = row
  refPkgForm.code = row.code; refPkgForm.name = row.name; refPkgForm.nameEn = row.nameEn || ''
  showRefPkgForm.value = true
}

async function refSavePackaging() {
  if (!refPkgForm.code.trim() || !refPkgForm.name.trim()) { showToast('编号和名称不能为空', 'warn'); return }
  try {
    const body = { code: refPkgForm.code.trim(), name: refPkgForm.name.trim(), nameEn: refPkgForm.nameEn.trim() || null }
    if (refEditingPkg.value) {
      await api(`/packaging-methods/${refEditingPkg.value.id}`, { method: 'PUT', body: JSON.stringify(body) })
    } else {
      await api('/packaging-methods', { method: 'POST', body: JSON.stringify(body) })
    }
    showRefPkgForm.value = false
    refLoadPackagings()
    showToast(refEditingPkg.value ? '包装方式已更新' : '包装方式已新增', 'success')
  } catch (e) { showToast('保存失败: ' + (e.message || '未知错误'), 'error') }
}

async function refDeletePackaging(row) {
  if (!confirm(`确定删除包装方式「${row.code} ${row.name}」？`)) return
  try { await api(`/packaging-methods/${row.id}`, { method: 'DELETE' }); refLoadPackagings(); showToast('已删除', 'success') } catch (e) { showToast('删除失败', 'error') }
}

async function refDeleteSelectedPkgs() {
  if (refSelectedPkgIds.value.length === 0) return
  if (!confirm(`确定删除选中的 ${refSelectedPkgIds.value.length} 条包装方式？`)) return
  try { await api('/packaging-methods/batch-delete', { method: 'POST', body: JSON.stringify(refSelectedPkgIds.value) }); refSelectedPkgIds.value = []; refLoadPackagings(); showToast('已删除', 'success') } catch (e) { showToast('删除失败', 'error') }
}
</script>

<style scoped>
:deep(.vxe-pager) {
  justify-content: center;
}

/* 列管理按钮左移 */
:deep(.vxe-toolbar-custom-target) {
  margin-right: 5px;
}

.sample-btn-card-toggle {
  display: inline-flex !important;
  align-items: center;
  gap: 5px;
  height: 30px !important;
  min-height: 30px !important;
  padding: 0 14px !important;
  font-size: 13px !important;
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
  gap: 14px;
  padding: 8px 12px 12px;
  align-content: start;
}

.sample-card-item {
  background: #fff;
  border: 1px solid #eaecef;
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
  box-shadow: 0 0 0 2px rgba(0,122,255,0.25);
}

.card-checkbox {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 5;
  width: 22px;
  height: 22px;
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
  color: rgba(29,29,31,0.1);
}

.sample-card-body {
  padding: 10px 12px 12px;
}

.sample-card-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.35;
  margin-bottom: 4px;
}

.sample-card-field {
  display: grid;
  grid-template-columns: 54px 1fr 54px 1fr;
  gap: 2px 6px;
  align-items: baseline;
  font-size: 13px;
  line-height: 1.55;
  color: rgba(29,29,31,0.72);
}

.card-label {
  color: rgba(29,29,31,0.38);
  text-align: right;
  white-space: nowrap;
}

.card-val {
  color: rgba(29,29,31,0.72);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.card-val-cell {
  display: flex;
  align-items: center;
  gap: 2px;
  min-width: 0;
  overflow: hidden;
}

.card-copy-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border: none;
  border-radius: 3px;
  background: transparent;
  color: rgba(29,29,31,0.2);
  cursor: pointer;
  padding: 0;
  transition: color 0.15s, background 0.15s;
}

.card-copy-btn:hover {
  color: rgba(29,29,31,0.6);
  background: rgba(29,29,31,0.06);
}

.card-price {
  font-size: 15px;
  font-weight: 700;
  color: #e03e2d;
}

.sample-card-divider {
  height: 1px;
  background: rgba(29,29,31,0.08);
  margin: 4px 0;
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
  padding: 10px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  margin-bottom: 8px;
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
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}
.tpl-select-date {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

/* Toast */
.sr-toast {
  position: fixed; top: 60px; left: 50%; transform: translateX(-50%); z-index: 100000;
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
  animation: advFadeIn 0.18s ease;
}
@keyframes advFadeIn { from { opacity: 0; } to { opacity: 1; } }

.adv-search-panel {
  width: 95vw; max-width: 1500px; max-height: 92vh;
  background: #fff;
  border: 1px solid #e0e3e8;
  border-radius: 10px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.18);
  display: flex; flex-direction: column;
  animation: advSlideIn 0.2s ease;
}
@keyframes advSlideIn { from { transform: translateY(-8px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

.adv-search-body {
  flex: 1; overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px 20px;
  padding: 24px 28px 16px;
}

.adv-field {
  display: flex; flex-direction: column; gap: 5px;
}

.adv-field > label {
  font-size: 12px; font-weight: 600; color: #333;
  white-space: nowrap; line-height: 1.2;
}

.adv-field > input,
.adv-field > select {
  height: 34px;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 13px;
  color: #1d1d1f;
  outline: none;
  background: #fff;
  transition: border-color 0.15s;
}
.adv-field > input:focus,
.adv-field > select:focus {
  border-color: #007aff;
  box-shadow: 0 0 0 2px rgba(0,122,255,0.08);
}
.adv-field > input::placeholder { color: #bbb; }

/* 范围输入（无单位） */
.adv-field-range {
  display: flex; flex-direction: column; gap: 5px;
}
.range-inputs {
  display: flex; align-items: center; gap: 6px;
}
.range-inputs > input {
  flex: 1;
  height: 34px;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 0 8px;
  font-size: 13px;
  outline: none;
  text-align: center;
  transition: border-color 0.15s;
}
.range-inputs > input:focus {
  border-color: #007aff;
  box-shadow: 0 0 0 2px rgba(0,122,255,0.08);
}
.range-inputs > span {
  color: #999; font-size: 13px; user-select: none;
}

/* 带单位的范围输入 */
.adv-field-range-unit .range-inputs {
  display: flex; align-items: center; gap: 6px;
}
.adv-field-range-unit .range-inputs > input {
  flex: 1;
  height: 34px;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 0 8px;
  font-size: 13px;
  outline: none;
  text-align: center;
  transition: border-color 0.15s;
}
.adv-field-range-unit .range-inputs > input:focus {
  border-color: #007aff;
  box-shadow: 0 0 0 2px rgba(0,122,255,0.08);
}
.unit {
  font-size: 12px; color: #999; white-space: nowrap; min-width: 22px;
}

/* 复选框 */
.adv-field-checks .check-group {
  display: flex; align-items: center; gap: 16px; height: 34px;
}
.chk-item {
  display: flex; align-items: center; gap: 4px;
  font-size: 13px; color: #555; cursor: pointer; user-select: none;
}
.chk-item input[type="checkbox"] {
  width: 14px; height: 14px; accent-color: #007aff; cursor: pointer;
}

.adv-search-footer {
  display: flex; align-items: center; justify-content: flex-end; gap: 10px;
  padding: 14px 24px;
  border-top: 1px solid #eee;
}
.adv-search-footer button {
  height: 40px;
  padding: 0 28px;
  font-size: 15px;
  min-width: 100px;
}

/* ========== 对照资料管理弹窗 ========== */
.ref-modal { background: #fff; border-radius: 14px; width: 680px; max-width: 94vw; padding: 20px 24px; max-height: 86vh; display: flex; flex-direction: column; box-shadow: 0 20px 60px rgba(0,0,0,.18); }
.ref-modal-header { display: flex; align-items: center; gap: 16px; margin-bottom: 14px; flex-wrap: wrap; }
.ref-modal-header strong { font-size: 15px; font-weight: 700; flex-shrink: 0; }
.ref-modal-body { flex: 1; overflow: hidden; min-height: 0; }
.ref-panel { display: flex; flex-direction: column; gap: 10px; }
.ref-panel-toolbar { display: flex; align-items: center; gap: 7px; flex-wrap: wrap; }
.ref-tabs { display: flex; gap: 3px; background: #f1f5f9; border-radius: 8px; padding: 3px; margin-left: auto; }
.ref-tab { padding: 5px 14px; border-radius: 6px; border: none; background: transparent; font-size: 12.5px; cursor: pointer; transition: all .15s; color: #64748b; }
.ref-tab.active { background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,.08); font-weight: 600; color: #1e293b; }
.ref-search-box { display: flex; align-items: center; gap: 5px; background: #f1f5f9; border-radius: 6px; padding: 4px 10px; }
.ref-search-box input { border: none; background: transparent; outline: none; font-size: 12px; width: 150px; }
.ref-filter-select { border: 1px solid #d1d5db; border-radius: 6px; padding: 4px 8px; font-size: 12px; background: #fff; }
.rf-tag { display: inline-block; padding: 1px 7px; border-radius: 10px; font-size: 10.5px; font-weight: 500; }
.rf-tag-l1 { background: #dbeafe; color: #1d4ed8; }
.rf-tag-l2 { background: #fce7f3; color: #be185d; }
.ref-action-btn { padding: 3px 6px; border: 1px solid #e2e8f0; border-radius: 4px; background: #fff; cursor: pointer; display: inline-flex; align-items: center; transition: all .12s; }
.ref-action-btn:hover { background: #f1f5f9; border-color: #cbd5e1; }
.ref-action-btn.danger:hover { background: #fef2f2; border-color: #fecaca; color: #dc2626; }
.ref-inline-input { width: 100%; padding: 2px 4px; border: 1px solid transparent; border-radius: 3px; font-size: 12px; background: transparent; outline: none; transition: border-color .15s; }
.ref-inline-input:hover { border-color: #e2e8f0; }
.ref-inline-input:focus { border-color: #3b82f6; background: #fff; }
.ref-form-modal { background: #fff; border-radius: 14px; width: 420px; max-width: 90vw; padding: 22px; box-shadow: 0 16px 48px rgba(0,0,0,.15); }
.ref-form-row { display: flex; flex-direction: column; gap: 4px; margin-bottom: 11px; }
.ref-form-row label { font-size: 12.5px; font-weight: 600; color: #374151; }
.ref-form-row input, .ref-form-row select { border: 1px solid #d1d5db; border-radius: 7px; padding: 7px 10px; font-size: 13px; transition: border-color .15s; }
.ref-form-row input:focus, .ref-form-row select:focus { border-color: #007aff; outline: none; box-shadow: 0 0 0 3px rgba(0,122,255,.1); }
.ref-modal-footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
.ref-required { color: #ef4444; }

/* 树形表格内一级类目行加粗 */
.ref-panel :deep(.vxe-table--body .row--level-1) { font-weight: 600; color: #1e293b; }
.ref-panel :deep(.vxe-table--body .row--level-2) { color: #475569; }
.ref-panel :deep(.vxe-tree-node-wrapper) { padding-left: 6px !important; }
.ref-panel :deep(.vxe-tree-cell) { white-space: nowrap; }

.cursor-pointer { cursor: pointer; }

/* 导入预览行颜色 */
.import-preview-table-wrap :deep(.import-row-cat-error) { background-color: #ffebee !important; }
.import-preview-table-wrap :deep(.import-row-cat-error:hover) { background-color: #ffcdd2 !important; }
.import-preview-table-wrap :deep(.import-row-pkg-warning) { background-color: #fff8e1 !important; }
.import-preview-table-wrap :deep(.import-row-pkg-warning:hover) { background-color: #ffecb3 !important; }

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
</style>