<template>
  <div class="csd-page">
    <!-- 工具栏卡片 -->
    <div class="csd-card csd-toolbar-card">
      <div class="csd-toolbar-row">
        <div class="csd-search csd-search-wide">
          <Search :size="16" />
          <input
            v-model="searchKeyword"
            placeholder="搜索公司编号/货号/名称..."
            @keyup.enter="onSearch"
          />
        </div>
        <button class="csd-btn csd-btn-primary" @click="onSearch">
          <Search :size="16" /> 搜索
        </button>
        <button class="csd-btn csd-btn-ghost" @click="onClear">
          <RotateCcw :size="16" /> 清除
        </button>
        <button class="csd-btn csd-btn-primary" @click="onSearchByCode">
          <Hash :size="16" /> 按编号搜索
        </button>
        <button class="csd-btn csd-btn-primary" @click="onSearchByFactoryCode">
          <Hash :size="16" /> 按货号搜索
        </button>
        <div class="csd-toolbar-sep"></div>
        <button class="csd-btn csd-btn-ghost" @click="openAdvancedSearch">
          <Filter :size="16" /> 综合查询
        </button>
        <button class="csd-btn csd-btn-ghost" @click="onImageSearch">
          <ImageIcon :size="16" /> 图像搜索
        </button>
        <div class="csd-toolbar-sep"></div>
        <button class="csd-btn csd-btn-orange" @click="onOpenFactoryModal">
          <Building2 :size="16" /> 按厂商选品
        </button>
        <button class="csd-btn csd-btn-primary" :disabled="checkedRows.length === 0 || addLoading" @click="onBatchAdd">
          <CheckCircle :size="16" /> 提交选取 ({{ checkedRows.length }})
        </button>
        <label class="csd-check-btn">
          <input type="checkbox" v-model="onlyHasImage" @change="onImageVideoChange" />
          <ImageIcon :size="16" /> 图片
        </label>
        <label class="csd-check-btn">
          <input type="checkbox" v-model="onlyHasVideo" @change="onImageVideoChange" />
          <Video :size="16" /> 视频
        </label>
        <span v-if="searchElapsed != null" style="margin-left:auto;color:#3b82f6;font-size:20px;white-space:nowrap;align-self:center">查询耗时 {{ searchElapsed }} ms</span>
      </div>
    </div>

    <!-- 表格卡片 -->
    <div v-if="!showImageSearch" class="csd-card csd-table-card">
      <div v-if="!cardMode" ref="tableWrapRef" class="csd-table-wrap">
        <vxe-grid
          v-if="prefReady"
          ref="gridRef"
          :id="gridStorageKey"
          :columns="computedColumns"
          :data="tableData"
          :loading="tableLoading"
          :height="tableWrapHeight"
          :toolbar-config="{ custom: true, zoom: true }"
          :custom-config="{ storage: true }"
          :column-config="{ resizable: true, drag: true, isMaximized: true }"
          :row-config="{ isHover: true, isCurrent: true, keyField: 'id' }"
          :checkbox-config="{ highlight: true, checkField: 'checkbox' }"
          :cell-config="cellConfig"
          :cell-style="gridCellStyle"
          header-align="center"
          :sort-config="{ trigger: 'header', remote: true }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 30, rSize: 60 }"
          :scroll-x="{ enabled: true, gt: 0 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :virtual-x-config="{ enabled: true, gt: 0 }"
          :border="true"
          @checkbox-change="onCheckboxChange"
          @checkbox-all="onCheckboxAll"
          @sort-change="onSortChange"
          @column-dragend="onColumnDragEnd"
          @resizable-change="saveGridPrefs"
          @custom="onCustomChange"
          @cell-dblclick="onCellDblclick"
        >
          <template #image="{ row }">
            <div style="display:flex;align-items:center;justify-content:center;height:100%">
              <img
                v-if="row.thumbnail"
                :src="'/thumbnails/' + row.thumbnail"
                loading="lazy"
                :style="{ width: '90px', height: '90px', objectFit: 'cover', borderRadius: '4px', cursor: 'pointer' }"
                @click.stop="openPhotoModal(row)"
                @mouseenter="onThumbMouseEnter($event, row)"
                @mouseleave="onThumbMouseLeave"
              />
              <span v-else style="font-size:11px;color:#aaa;cursor:pointer" @click.stop="openPhotoModal(row)">无图</span>
            </div>
          </template>
        </vxe-grid>
      </div>
      <div class="csd-statusbar">
        <div class="csd-select-actions">
          <button class="csd-btn csd-btn-primary" @click="onSelectAll">全选</button>
          <button class="csd-btn csd-btn-primary" @click="onInvertSelect">反选</button>
          <button class="csd-btn csd-btn-primary" @click="onClearSelect">清除</button>
        </div>
        <div class="csd-status-info">
          已选 {{ checkedRows.length }} / {{ tableData.length }} 条
        </div>
        <button class="csd-btn csd-btn-primary" @click="cardMode = !cardMode">
          <LayoutGrid :size="14" /> {{ cardMode ? '列表' : '卡片' }}
        </button>
        <div class="csd-pagination">
          <span class="csd-page-size-label">每页</span>
          <select class="csd-page-size-select" :value="pageSize" @change="onPageSizeChange">
            <option v-for="s in pageSizes" :key="s" :value="s">{{ s }}</option>
          </select>
          <button class="csd-btn csd-btn-ghost" :disabled="currentPage <= 1" @click="onPageChange(currentPage - 1)">&lt;</button>
          <span class="csd-page-text">{{ currentPage }} / {{ totalPages || 1 }}</span>
          <button class="csd-btn csd-btn-ghost" :disabled="currentPage >= totalPages" @click="onPageChange(currentPage + 1)">&gt;</button>
        </div>
      </div>

      <!-- 卡片覆盖层 - 虚拟滚动 -->
      <div v-if="cardMode" ref="cardContainerRef" class="csd-card-overlay" @scroll="onCardScroll">
        <div class="csd-card-virtual-wrap" :style="{ height: cardTotalHeight + 'px' }">
          <div class="csd-card-grid" :style="{ transform: `translateY(${cardOffsetY}px)` }">
            <div v-for="row in visibleCardData" :key="row.id"
                 class="csd-card-item" :class="{ 'csd-card-selected': isCardChecked(row) }"
                 @dblclick.stop="addSingleCard(row)">
              <div class="csd-card-img">
                <div class="csd-card-checkbox" :class="{ checked: isCardChecked(row) }" @click.stop="toggleCardSelect(row)">
                  <Check v-if="isCardChecked(row)" :size="14" />
                </div>
                <img v-if="row.thumbnail" :src="'/thumbnails/' + row.thumbnail" @error="onCardImgError" @click.stop="openPhotoModal(row)" loading="lazy" decoding="async" />
                <div v-else class="csd-card-no-img" @click.stop="openPhotoModal(row)"><ImageIcon :size="36" /></div>
              </div>
              <div class="csd-card-body">
                <div class="csd-card-name" :title="row.sampleName">{{ row.sampleName }}</div>
                <div class="csd-card-fields">
                  <span class="csd-card-val csd-card-val-copy csd-card-code" :title="row.sampleCode">
                    {{ row.sampleCode }}
                    <button class="csd-card-copy-btn" @click.stop="copyCardCode(row.sampleCode)"><Copy :size="16" /></button>
                  </span>
                  <span class="csd-card-val" :title="row.factoryCode">{{ row.factoryCode }}</span>
                  <span class="csd-card-val" :title="(row.innerBoxCount || '-') + ' / ' + (row.cartonCapacity || '-')">{{ row.innerBoxCount || '-' }} / {{ row.cartonCapacity || '-' }}</span>
                  <span class="csd-card-val" :title="(row.cartonGrossWeight || '-') + ' / ' + (row.cartonNetWeight || '-')">{{ row.cartonGrossWeight || '-' }} / {{ row.cartonNetWeight || '-' }}</span>
                  <span class="csd-card-val" :title="(row.cartonMaterialVolume || '-') + ' / ' + (row.cartonVolume || '-')">{{ row.cartonMaterialVolume || '-' }} / {{ row.cartonVolume || '-' }}</span>
                  <span class="csd-card-val" :title="row.boothNo">{{ row.boothNo || '-' }}</span>
                  <span class="csd-card-val csd-card-price" :title="row.factoryPrice ? '¥' + row.factoryPrice : '-'">{{ row.factoryPrice ? '¥' + row.factoryPrice : '-' }}</span>
                </div>
                <div class="csd-card-divider"></div>
                <div class="csd-card-fields" style="margin-top:0;grid-template-columns:1fr">
                  <span class="csd-card-val" :title="row.name">{{ row.name || '-' }}</span>
                  <span class="csd-card-val" :title="row.mobile1">{{ row.mobile1 || '-' }}</span>
                  <span class="csd-card-val" :title="row.createTime">{{ row.createTime || '-' }}</span>
                  <div class="csd-card-last-row">
                    <span class="csd-card-val" :title="row.updateTime">{{ row.updateTime || '-' }}</span>
                    <button class="csd-card-add-btn" :disabled="cardAddingIds.has(row.id)" @click.stop="addSingleCard(row)">
                      <Loader v-if="cardAddingIds.has(row.id)" :size="14" class="spin" />
                      <Plus v-else :size="14" />
                      添加
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 综合查询弹窗 -->
    <Teleport to="body">
    <div v-if="showAdvancedSearch" class="adv-search-overlay">
      <div class="adv-search-panel">
        <div class="adv-search-body">
          <div class="adv-field"><label>厂商编号</label><input v-model="advForm.manufacturerCode" placeholder="请输入厂商编号" /></div>
          <div class="adv-field"><label>厂商名称</label><input v-model="advForm.name" placeholder="请输入厂商名称" /></div>
          <div class="adv-field"><label>联系人</label><input v-model="advForm.contact1" placeholder="请输入联系人" /></div>
          <div class="adv-field"><label>电话号码</label><input v-model="advForm.phone1" placeholder="请输入电话号码" /></div>
          <div class="adv-field"><label>手机号码</label><input v-model="advForm.mobile1" placeholder="请输入手机号码" /></div>
          <div class="adv-field"><label>样品名称</label><input v-model="advForm.sampleName" placeholder="请输入样品名称" /></div>
          <div class="adv-field"><label>公司编号</label><input v-model="advForm.sampleCode" placeholder="请输入公司编号" /></div>
          <div class="adv-field"><label>出厂货号</label><input v-model="advForm.factoryCode" placeholder="请输入出厂货号" /></div>
          <div class="adv-field"><label>摊位编号</label><input v-model="advForm.boothNo" placeholder="请输入摊位编号" /></div>
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
          <div class="adv-field"><label>产品认证</label><input v-model="advForm.certification" placeholder="请输入产品认证" /></div>
          <div class="adv-field"><label>侵权</label><select v-model="advForm.infringement"><option value="">请选择侵权状态</option><option value="侵权">侵权</option><option value="不侵权">不侵权</option><option value="其他">其他</option></select></div>
          <div class="adv-field adv-field-checks">
            <label>筛选条件</label>
            <div class="check-group">
              <label class="chk-item"><input type="checkbox" v-model="advForm.hasImage" /> 有图片</label>
              <label class="chk-item"><input type="checkbox" v-model="advForm.hasVideo" /> 有视频</label>
            </div>
          </div>
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
          <div class="adv-field adv-field-range">
            <label>在架数量</label>
            <div class="range-inputs">
              <input v-model.number="advForm.innerBoxCountMin" placeholder="最小数量" />
              <span>-</span>
              <input v-model.number="advForm.innerBoxCountMax" placeholder="最大数量" />
            </div>
          </div>
          <div class="adv-field"><label>电池信息</label><input v-model="advForm.batteryInfo" placeholder="" /></div>
          <div class="adv-field"><label>关键词</label><input v-model="advForm.keyword" placeholder="" /></div>
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
          <div class="adv-field"><label>登记人</label><input v-model="advForm.registrant" placeholder="请输入登记人" /></div>
          <div class="adv-field"><label>修改人</label><input v-model="advForm.modifier" placeholder="请输入修改人" /></div>
        </div>
        <div class="adv-search-footer">
          <button class="csd-btn csd-btn-ghost" @click="clearAdvForm">清空条件</button>
          <div style="flex:1"></div>
          <button class="csd-btn csd-btn-ghost" @click="showAdvancedSearch = false">取消</button>
          <button class="csd-btn csd-btn-primary" @click="doAdvancedSearch">确认</button>
        </div>
      </div>
    </div>
    </Teleport>

    <!-- 批量编号/货号输入弹窗 -->
    <div v-if="showCodeInput" class="code-input-panel" :style="panelX === -1 ? {} : { left: panelX + 'px', top: panelY + 'px', transform: 'none' }">
      <div class="code-input-header" @mousedown="onPanelDragStart">
        <span class="code-input-title">{{ codeInputTitle }}</span>
        <button class="code-input-close" @click="closeCodeInput">&times;</button>
      </div>
      <div class="code-input-body">
        <div class="code-input-left">
          <textarea
            v-model="codeInputText"
            class="code-input-textarea"
            :placeholder="codeInputMode === 'code' ? '每行一个公司编号，支持粘贴多行\n例如：\nYX18188104\nYX18188105' : '每行一个出厂货号，支持粘贴多行\n例如：\nAB12345\nAB12346'"
            rows="15"
          ></textarea>
        </div>
        <div class="code-input-right">
          <div class="code-input-subtitle">未查到的{{ codeInputMode === 'code' ? '公司编号' : '货号' }}</div>
          <div class="code-input-result-list">
            <div v-if="!codeSearchDone" class="code-input-empty">搜索后将在此显示未查到的编号</div>
            <div v-else-if="notFoundCodes.length === 0" class="code-input-empty code-input-all-found">全部查到</div>
            <div v-for="c in notFoundCodes" :key="c" class="code-input-notfound-item">{{ c }}</div>
          </div>
        </div>
      </div>
      <div class="code-input-footer">
        <button class="csd-btn csd-btn-ghost" @click="closeCodeInput">取消</button>
        <button class="csd-btn csd-btn-primary" @click="doCodeSearch" :disabled="codeSearchLoading">
          {{ codeSearchLoading ? '搜索中...' : '确认搜索' }}
        </button>
      </div>
    </div>

    <!-- 图像搜索内嵌面板 -->
    <div v-if="showImageSearch" class="isp-embed-panel">
      <div class="isp-embed-header">
        <button class="csd-btn csd-btn-primary" @click="showImageSearch = false">
          <ArrowLeft :size="16" /> 返回
        </button>
        <span class="isp-embed-title">图像搜索-{{ codeName }}</span>
        <button v-if="imageSearchHasResults"
                class="csd-btn csd-btn-primary"
                :disabled="imageSearchSelectedCount === 0"
                @click="imageSearchRef?.batchAddSelected()">
          <CheckCircle :size="16" /> 批量添加 ({{ imageSearchSelectedCount }})
        </button>
      </div>
      <div class="isp-embed-body">
        <BatchImageSearchView ref="imageSearchRef" @addSample="onAddFromImageSearch" @addSamples="onBatchAddFromImageSearch" />
      </div>
    </div>
</div>

<!-- 悬浮大图预览 -->
<Teleport to="body">
  <Transition name="hover-preview-fade">
    <div
      v-if="hoverPreview.show"
      class="sr-hover-preview"
      :style="{ left: hoverPreview.x + 'px', top: hoverPreview.y + 'px' }"
    >
      <img :src="hoverPreview.src" @error="hoverPreview.show = false" />
    </div>
  </Transition>
</Teleport>

<!-- 图片预览模态框 - 样品信息预览 -->
<Teleport to="body">
<div v-if="showPhotoModal" class="batch-photo-modal" :style="photoModalStyle">
  <div class="spm-header" @mousedown="startDragModal">
    <span class="spm-header-title">样品信息预览</span>
    <button class="spm-header-close" @click="closePhotoModal">&times;</button>
  </div>
  <div class="spm-body">
    <div class="spm-top-card" v-if="photoModalSample" style="visibility:hidden">
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
             draggable="true" @dragstart="onBatchAddImgDragStart"
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
          <img :src="img.thumbnailPath ? '/thumbnails/' + img.thumbnailPath : ''" draggable="true" @dragstart="onBatchAddImgDragStart" style="-webkit-user-drag:element" />
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
        </div>
        <div class="spm-field-row">
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
    </div>
    </div>
  </div>
  <div class="spm-footer">
    <div class="spm-toggle-group" v-if="!editing">
      <label class="spm-toggle"><input type="checkbox" v-model="hideFactoryPrice" /> 隐藏出厂价</label>
      <label class="spm-toggle"><input type="checkbox" v-model="hideSupplierInfo" /> 隐藏厂商信息</label>
    </div>
    <div class="spm-toggle-group" style="gap:8px; margin-left: auto">
      <button v-if="!editing" class="spm-btn-close" @click="closePhotoModal">关闭</button>
    </div>
  </div>
</div>
</Teleport>

<!-- 全屏大图预览（滚轮缩放） -->
<Teleport to="body">
  <div v-if="showFullPreview" class="full-preview-overlay" @click.self="closeFullPreview" @wheel="onFullPreviewWheel" @mousemove="onFullPreviewMouseMove" @mouseup="onFullPreviewMouseUp" @mouseleave="onFullPreviewMouseUp">
    <img
      :src="fullPreviewSrc"
      draggable="true" @dragstart="onBatchAddImgDragStart"
      @mousedown="onFullPreviewMouseDown"
      @click.stop
      :style="{
        transform: `translate(${fullPreviewPanX}px, ${fullPreviewPanY}px) scale(${fullPreviewZoom})`,
        cursor: fullPreviewZoom > 1 ? 'grab' : 'default'
      }"
    />
    <div class="full-preview-toolbar">
      <span class="full-preview-zoom-label">{{ Math.round(fullPreviewZoom * 100) }}%</span>
      <button class="full-preview-btn" @click="fullPreviewZoom = Math.min(5, +(fullPreviewZoom + 0.25).toFixed(2))">＋</button>
      <button class="full-preview-btn" @click="fullPreviewZoom = Math.max(0.3, +(fullPreviewZoom - 0.25).toFixed(2))">－</button>
      <button class="full-preview-btn" @click="fullPreviewZoom = 1; fullPreviewPanX = 0; fullPreviewPanY = 0">1:1</button>
      <button class="full-preview-btn full-preview-close" @click="closeFullPreview">&times;</button>
    </div>
  </div>
</Teleport>

<!-- 按厂商选品模态框 -->
<Teleport to="body">
  <div v-if="showFactoryModal" class="factory-modal-overlay" style="background:transparent;backdrop-filter:none;-webkit-backdrop-filter:none">
    <div class="factory-modal-panel">
      <div class="factory-modal-header">
        <h3><Building2 :size="26" /> 按厂商编号选品</h3>
        <button class="factory-modal-close" @click="showFactoryModal = false"><X :size="24" /></button>
      </div>
      <div class="factory-modal-body">
        <div class="adv-field">
          <label>选择厂商编号数量</label>
          <input
            v-model.number="factoryCount"
            type="number"
            min="1"
            :max="totalFactoryCount"
            placeholder="输入厂商数量"
          />
          <span class="adv-field-hint">共 {{ totalFactoryCount }} 家厂商编号，默认为全部</span>
        </div>
        <div class="adv-field">
          <label>产品分组数量</label>
          <input
            v-model.number="productsPerFactory"
            type="number"
            min="1"
            placeholder="每家厂商的产品数量"
          />
          <span class="adv-field-hint">不足该数量的厂商将取其实际产品数</span>
        </div>
        <div v-if="factoryPreviewLoading" class="factory-preview-tip">正在计算...</div>
        <div v-else-if="factoryRealTotal > 0" class="factory-preview-tip">
          预计实际添加 <strong>{{ factoryRealTotal.toLocaleString() }}</strong> 条记录
          <template v-if="factoryPreviewData">
            （{{ factoryPreviewData.fullCount || 0 }} 家厂商满额，{{ factoryPreviewData.partialCount || 0 }} 家不足）
          </template>
        </div>
      </div>
      <div class="factory-modal-footer">
        <button class="csd-btn csd-btn-ghost" @click="showFactoryModal = false">取消</button>
        <button
          class="csd-btn csd-btn-primary"
          :disabled="!factoryCount || !productsPerFactory || factoryLoading"
          @click="onFactorySubmit"
        >
          <Loader v-if="factoryLoading" :size="14" class="spin-icon" />
          确认添加
        </button>
      </div>
    </div>
  </div>
</Teleport>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, RotateCcw, Filter, Image as ImageIcon, Hash, CheckCircle, LayoutGrid, Check, Copy, ArrowLeft, Building2, Video, X, Plus, Loader } from 'lucide-vue-next'
import { api } from '@/api/index'
import { appAlert, appConfirm, appToast } from '@/utils/dialog'
import { useAdvancedSearch } from '@/composables/useAdvancedSearch'
import { useModalDrag } from '@/composables/useModalDrag'
import { useGridPrefSync } from '@/composables/useGridPrefSync'
import SimpleDatePicker from '@/components/SimpleDatePicker.vue'
import BatchImageSearchView from '@/views/clientSample/BatchImageSearchView.vue'
import '@/styles/client-sample-detail.css'
import '@/styles/sample.css'

const route = useRoute()
const router = useRouter()
const codeName = computed(() => route.params.codeName || '')

// ========== 搜索 ==========
const searchKeyword = ref('')
const tableData = ref([])
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(2000)
const totalRecords = ref(0)
const searchElapsed = ref(null)
const pageSizes = [500, 1000, 2000, 4000, 5000]
const sortField = ref('')
const sortOrder = ref('')
const activeSearchConditions = ref([])
const lastSearchPayload = ref(null) // { keyword, conditions, logic } - 当前列表的搜索条件，供工厂选品使用
const manufacturerCode = ref('')

const totalPages = computed(() => Math.ceil(totalRecords.value / pageSize.value) || 1)

const onSearch = () => {
  currentPage.value = 1
  loadData('all')
}

const onSearchByCode = () => {
  codeInputMode.value = 'code'
  codeInputTitle.value = '按编号搜索 — 输入公司编号'
  codeInputText.value = ''
  notFoundCodes.value = []
  codeSearchDone.value = false
  panelX.value = -1
  panelY.value = 80
  showCodeInput.value = true
}

const onSearchByFactoryCode = () => {
  codeInputMode.value = 'factoryCode'
  codeInputTitle.value = '按货号搜索 — 输入出厂货号'
  codeInputText.value = ''
  notFoundCodes.value = []
  codeSearchDone.value = false
  panelX.value = -1
  panelY.value = 80
  showCodeInput.value = true
}

const showCodeInput = ref(false)
const codeInputText = ref('')
const codeInputMode = ref('code')
const codeInputTitle = ref('')
const notFoundCodes = ref([])
const codeSearchDone = ref(false)
const codeSearchLoading = ref(false)
const panelX = ref(20)
const panelY = ref(80)
const panelDragging = ref(false)
const panelDragStartX = ref(0)
const panelDragStartY = ref(0)

const onPanelDragStart = (e) => {
  if (e.target.tagName === 'BUTTON') return
  panelDragging.value = true
  const rect = e.currentTarget.parentElement.getBoundingClientRect()
  // 首次拖拽时从居中位置获取实际坐标
  if (panelX.value === -1) {
    panelX.value = rect.left
    panelY.value = rect.top
  }
  panelDragStartX.value = e.clientX - panelX.value
  panelDragStartY.value = e.clientY - panelY.value
  document.addEventListener('mousemove', onPanelDragMove)
  document.addEventListener('mouseup', onPanelDragEnd)
}

const onPanelDragMove = (e) => {
  if (!panelDragging.value) return
  panelX.value = e.clientX - panelDragStartX.value
  panelY.value = e.clientY - panelDragStartY.value
}

const onPanelDragEnd = () => {
  panelDragging.value = false
  document.removeEventListener('mousemove', onPanelDragMove)
  document.removeEventListener('mouseup', onPanelDragEnd)
}

const closeCodeInput = () => {
  showCodeInput.value = false
  codeInputText.value = ''
  notFoundCodes.value = []
  codeSearchDone.value = false
}

const doCodeSearch = async () => {
  const raw = codeInputText.value.trim()
  if (!raw) return
  const codes = raw.split(/[\n\r]+/).map(c => c.trim()).filter(Boolean)
  if (codes.length === 0) return
  currentPage.value = 1
  codeSearchDone.value = false
  notFoundCodes.value = []
  codeSearchLoading.value = true
  // 清除筛选条件，确保搜索不受影响
  const savedImage = onlyHasImage.value
  const savedVideo = onlyHasVideo.value
  onlyHasImage.value = false
  onlyHasVideo.value = false
  try {
    await loadData(codeInputMode.value, codes.join(','), codes)
    // 比较输入编号与搜索结果，找出未查到的
    const field = codeInputMode.value === 'code' ? 'sampleCode' : 'factoryCode'
    const foundSet = new Set(tableData.value.map(r => r[field]))
    notFoundCodes.value = codes.filter(c => !foundSet.has(c))
    // 按输入顺序排序
    const orderMap = new Map(codes.map((c, i) => [c, i]))
    tableData.value = [...tableData.value].sort((a, b) => {
      const ia = orderMap.get(a[field]) ?? Infinity
      const ib = orderMap.get(b[field]) ?? Infinity
      return ia - ib
    })
    codeSearchDone.value = true
  } catch (e) {
    console.error('搜索失败:', e)
  } finally {
    codeSearchLoading.value = false
    onlyHasImage.value = savedImage
    onlyHasVideo.value = savedVideo
  }
}

const onSearchBySupplier = () => {
  if (!searchKeyword.value.trim()) return
  currentPage.value = 1
  loadData('supplier')
}

// ===== 按厂商选品模态框 =====
const showFactoryModal = ref(false)
const factoryCount = ref(0)
const productsPerFactory = ref(1)
const totalFactoryCount = ref(0)
const factoryLoading = ref(false)
const factoryPreviewLoading = ref(false)
const factoryPreviewData = ref(null)
const factoryRealTotal = ref(0)
let factoryPreviewTimer = null

const fetchFactoryPreview = async () => {
  if (!factoryCount.value || !productsPerFactory.value) {
    factoryRealTotal.value = 0
    factoryPreviewData.value = null
    return
  }
  factoryPreviewLoading.value = true
  try {
    const body = { factoryCount: factoryCount.value, productsPerFactory: productsPerFactory.value }
    const searchKw = searchKeyword.value?.trim()
    const allConditions = [...(activeSearchConditions.value || [])]
    if (onlyHasImage.value) allConditions.push({ field: 'image', operator: 'eq', value: '1' })
    if (onlyHasVideo.value) allConditions.push({ field: 'video', operator: 'eq', value: '1' })
    if (searchKw) body.keyword = searchKw
    if (allConditions.length > 0) { body.conditions = allConditions; body.logic = null }
    const res = await api('/client-samples/factory-count/preview', { method: 'POST', body: JSON.stringify(body) })
    if (res.code === 200 && res.data) {
      factoryPreviewData.value = res.data
      factoryRealTotal.value = res.data.realTotal || 0
    }
  } catch (e) {
    factoryRealTotal.value = 0
  } finally {
    factoryPreviewLoading.value = false
  }
}

// 防抖预览
const triggerFactoryPreview = () => {
  clearTimeout(factoryPreviewTimer)
  factoryPreviewTimer = setTimeout(fetchFactoryPreview, 400)
}
watch(factoryCount, triggerFactoryPreview)
watch(productsPerFactory, triggerFactoryPreview)

const onOpenFactoryModal = async () => {
  try {
    // 从当前搜索状态直接构建过滤参数
    const searchKw = searchKeyword.value?.trim()
    const allConditions = [...(activeSearchConditions.value || [])]
    if (onlyHasImage.value) allConditions.push({ field: 'image', operator: 'eq', value: '1' })
    if (onlyHasVideo.value) allConditions.push({ field: 'video', operator: 'eq', value: '1' })
    const hasAnyFilter = allConditions.length > 0

    if (searchKw || hasAnyFilter) {
      const payload = {}
      if (searchKw) payload.keyword = searchKw
      if (hasAnyFilter) {
        payload.conditions = allConditions
        payload.logic = null
      }
      const res = await api('/client-samples/factory-count', {
        method: 'POST',
        body: JSON.stringify(payload)
      })
      if (res.code === 200) {
        totalFactoryCount.value = res.data || 0
        factoryCount.value = totalFactoryCount.value
      }
    } else {
      // 无筛选：直接从当前数据计算厂商数
      const codes = new Set(tableData.value.map(r => r.manufacturerCode).filter(Boolean))
      totalFactoryCount.value = codes.size
      factoryCount.value = totalFactoryCount.value
    }
  } catch (e) {
    console.error('获取厂商数量失败:', e)
  }
  productsPerFactory.value = 1
  showFactoryModal.value = true
}

const onFactorySubmit = async () => {
  if (!factoryCount.value || !productsPerFactory.value) return
  
  const searchKw = searchKeyword.value?.trim()
  const allConditions = [...(activeSearchConditions.value || [])]
  if (onlyHasImage.value) allConditions.push({ field: 'image', operator: 'eq', value: '1' })
  if (onlyHasVideo.value) allConditions.push({ field: 'video', operator: 'eq', value: '1' })

  if (searchKw || allConditions.length > 0) {
    // 有筛选条件：走后端，先二次确认
    if (!factoryRealTotal.value) {
      appAlert('预览数据尚未加载完成，请稍候再试', '提示', 'warning')
      return
    }
    const estimated = factoryRealTotal.value
    const confirmed = await appConfirm(`确认按厂商选品，预计添加 ${estimated} 条记录？`, '确认操作')
    if (!confirmed) return

    factoryLoading.value = true
    try {
      const body = {
        factoryCount: factoryCount.value,
        productsPerFactory: productsPerFactory.value,
        force: false
      }
      if (searchKw) body.keyword = searchKw
      if (allConditions.length > 0) {
        body.conditions = allConditions
        body.logic = null
      }
      const res = await api(`/client-samples/${codeName.value}/select-by-factory`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
      if (res.code === 200) {
        const added = res.data || 0
        await appConfirm(`按厂商选品完成，成功添加 ${added} 条记录`, '操作成功', 'success')
        showFactoryModal.value = false
        try {
          const itemsRes = await api(`/client-samples/${codeName.value}/items?page=1&pageSize=99999`)
          if (itemsRes.code === 200 && itemsRes.data) {
            const codes = (itemsRes.data.records || itemsRes.data.list || itemsRes.data || [])
              .map(r => r.sampleCode || (r.sample && r.sample.sampleCode))
              .filter(Boolean)
            existingSampleCodes.value = new Set(codes)
          }
        } catch (e) { /* ignore */ }
      } else {
        appAlert('按厂商选品失败: ' + (res.message || '未知错误'), '错误', 'danger')
      }
    } catch (e) {
      console.error('按厂商选品失败:', e)
      appAlert('按厂商选品失败，请检查网络连接', '错误', 'danger')
    } finally {
      factoryLoading.value = false
    }
    return
  }

  // 无筛选：从当前数据按厂商分组选取
  const byFactory = {}
  for (const row of tableData.value) {
    const mc = row.manufacturerCode
    if (!mc) continue
    if (!byFactory[mc]) byFactory[mc] = []
    byFactory[mc].push({ id: row.id, sampleCode: row.sampleCode })
  }
  const sorted = Object.entries(byFactory).sort((a, b) => b[1].length - a[1].length)
  const selected = sorted.slice(0, factoryCount.value)
  const allPicked = []
  for (const [, samples] of selected) {
    allPicked.push(...samples.slice(0, productsPerFactory.value))
  }

  if (allPicked.length === 0) {
    appAlert('没有可选的样品', '提示', 'warning')
    return
  }

  // 精确检测重复
  const duplicates = allPicked.filter(s => existingSampleCodes.value.has(s.sampleCode))
  const toAdd = allPicked.filter(s => !existingSampleCodes.value.has(s.sampleCode))

  if (toAdd.length === 0) {
    await appConfirm(`按厂商选品完成，${allPicked.length}条均为重复，已全部跳过`, '提示', 'warning')
    showFactoryModal.value = false
    return
  }

  let confirmMsg = `确认添加 ${toAdd.length} 条样品到 <span style="color:#ff3b30;font-weight:700">代号</span>："${codeName.value}"？`
  if (duplicates.length > 0) {
    confirmMsg += `  （${duplicates.length}条重复将跳过）`
  }
  const confirmed = await appConfirm(confirmMsg, '确认操作')
  if (!confirmed) return

  factoryLoading.value = true
  try {
    const sampleIds = toAdd.map(s => s.id)
    const res = await api(`/client-samples/${codeName.value}/items`, {
      method: 'POST',
      body: JSON.stringify(sampleIds)
    })
    if (res.code === 200) {
      const codes = toAdd.map(s => s.sampleCode).filter(Boolean)
      if (codes.length) existingSampleCodes.value = new Set([...existingSampleCodes.value, ...codes])
      if (duplicates.length > 0) {
        await appConfirm(`按厂商选品完成，成功添加${toAdd.length}条，${duplicates.length}条重复跳过`, '操作成功', 'success')
      } else {
        await appConfirm(`按厂商选品完成，成功添加 ${toAdd.length} 条记录`, '操作成功', 'success')
      }
      showFactoryModal.value = false
    } else {
      appAlert('添加失败: ' + (res.message || '未知错误'), '错误', 'danger')
    }
  } catch (e) {
    console.error('按厂商选品失败:', e)
    appAlert('按厂商选品失败，请检查网络连接', '错误', 'danger')
  } finally {
    factoryLoading.value = false
  }
}

const onClear = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  sortField.value = ''
  sortOrder.value = ''
  tableData.value = []
  totalRecords.value = 0
  checkedRows.value = []
  lastSearchPayload.value = null
  loadData()
}

const loadData = async (mode = 'all', keywordParam, codesArr) => {
  const keyword = keywordParam !== undefined ? keywordParam.trim() : searchKeyword.value.trim()
  const hasFilters = onlyHasImage.value || onlyHasVideo.value
  const isMultiSearch = codesArr && codesArr.length > 1

  tableLoading.value = true
  const t0 = Date.now()
  try {
    const params = new URLSearchParams()
    params.set('current', currentPage.value)
    params.set('size', pageSize.value)
    if (sortField.value) {
      params.set('sortField', sortField.value)
      params.set('sortOrder', sortOrder.value)
    }

    // 无关键词无筛选 → GET 加载全部（与 SampleView 一致）
    if (!keyword && !hasFilters) {
      const res = await api(`/samples?${params.toString()}`)
      const data = res.data || res || {}
      tableData.value = data.records || data.list || data || []
      totalRecords.value = data.total || tableData.value.length
      lastSearchPayload.value = null // 全资料，无搜索条件
      restoreCheckedFromStorage()
      return
    }

    // 有关键词或筛选 → POST 搜索
    const conditions = []
    if (keyword) {
      if (isMultiSearch) {
        const field = mode === 'code' ? 'sampleCode' : 'factoryCode'
        const op = 'eq'
        codesArr.forEach(c => conditions.push({ field, operator: op, value: c }))
      } else if (mode === 'code') {
        conditions.push({ field: 'sampleCode', operator: 'eq', value: keyword })
      } else if (mode === 'factoryCode') {
        conditions.push({ field: 'factoryCode', operator: 'eq', value: keyword })
      } else if (mode === 'supplier') {
        conditions.push({ field: 'name', operator: 'like', value: keyword })
      } else {
        conditions.push({ field: 'sampleCode', operator: 'like', value: keyword })
        conditions.push({ field: 'factoryCode', operator: 'like', value: keyword })
        conditions.push({ field: 'sampleName', operator: 'like', value: keyword })
      }
    }
    if (onlyHasImage.value) {
      conditions.push({ field: 'image', operator: 'eq', value: '1' })
    }
    if (onlyHasVideo.value) {
      conditions.push({ field: 'video', operator: 'eq', value: '1' })
    }

    const logic = hasFilters ? undefined : 'or'
    // 保存当前搜索条件，供工厂选品使用
    lastSearchPayload.value = { keyword: keyword || null, conditions, logic: logic || null }
    const res = await api(`/samples/search?${params.toString()}`, {
      method: 'POST',
      body: JSON.stringify({ conditions, logic: logic || undefined })
    })
    const data = res.data || res || {}
    tableData.value = data.records || data.list || data || []
    totalRecords.value = data.total || tableData.value.length
    restoreCheckedFromStorage()
  } catch (e) {
    console.error('搜索样品失败:', e)
    appAlert('搜索样品失败，请检查网络连接', '错误', 'danger')
  } finally {
    searchElapsed.value = Date.now() - t0
    tableLoading.value = false
  }
}

// ========== 综合查询 ==========
const {
  showAdvancedSearch, advForm,
  clearAdvForm, openAdvancedSearch, doAdvancedSearch: _doAdvancedSearch,
} = useAdvancedSearch(tableData, totalRecords, currentPage, pageSize, sortField, sortOrder, activeSearchConditions, manufacturerCode)

const doAdvancedSearch = async () => {
  const t0 = Date.now()
  const ok = await _doAdvancedSearch()
  searchElapsed.value = Date.now() - t0
  if (ok) {
    searchKeyword.value = ''
    gridRef.value?.setAllCheckboxRow(false)
    checkedRows.value = []
    // 保存综合查询条件，供工厂选品使用
    lastSearchPayload.value = { keyword: null, conditions: activeSearchConditions.value, logic: null }
  }
}

// ========== 图像搜索 ==========
const showImageSearch = ref(false)
const imageSearchRef = ref(null)
const imageSearchHasResults = computed(() => {
  return imageSearchRef.value?.filteredResults?.length > 0
})
const imageSearchSelectedCount = computed(() => {
  return imageSearchRef.value?.selectedResultIds?.size ?? 0
})

const onImageSearch = () => {
  showImageSearch.value = true
}

const onAddFromImageSearch = (item) => {
  // 从图像搜索结果中找到完整的行数据来添加
  const row = tableData.value.find(r => r.id === item.sampleId)
  if (row) {
    addSingleCard(row)
  } else {
    // 如果表格中没有该数据，通过 sampleId 构造一个基本对象
    addSingleCard({
      id: item.sampleId,
      sampleCode: item.sampleCode,
      sampleName: item.sampleName,
      factoryCode: item.factoryCode,
      factoryPrice: item.factoryPrice,
      name: item.name,
      boothNo: item.boothNo
    })
  }
}

const onBatchAddFromImageSearch = async (items) => {
  const duplicates = items.filter(item => existingSampleCodes.value.has(item.sampleCode))
  const toAdd = items.filter(item => !existingSampleCodes.value.has(item.sampleCode))

  if (toAdd.length === 0) {
    appToast(`本次添加成功0个，${duplicates.length}个重复已跳过`, 1800, 'app-toast-lg')
    return
  }

  let confirmMsg = `确认添加 ${toAdd.length} 个样品到 <span style="color:#ff3b30;font-weight:700">代号</span>："${codeName.value}"？`
  if (duplicates.length > 0) {
    confirmMsg += `  （${duplicates.length}个重复将跳过）`
  }
  const confirmed = await appConfirm(confirmMsg, '确认操作')
  if (!confirmed) return

  try {
    const ids = toAdd.map(item => item.sampleId)
    const res = await api(`/client-samples/${codeName.value}/items`, {
      method: 'POST',
      body: JSON.stringify(ids)
    })
    if (res.code === 200) {
      const codes = toAdd.map(item => item.sampleCode)
      existingSampleCodes.value = new Set([...existingSampleCodes.value, ...codes])
      if (duplicates.length > 0) {
        appToast(`成功添加${toAdd.length}个，${duplicates.length}个重复跳过`, 1800, 'app-toast-lg')
      } else {
        appToast(`成功添加 ${toAdd.length} 个样品`, 1800, 'app-toast-lg')
      }
    } else {
      appAlert('添加失败: ' + (res.message || '未知错误'), '错误', 'danger')
    }
  } catch (e) {
    console.error('批量添加失败:', e)
    appAlert('添加失败，请检查网络连接', '错误', 'danger')
  }
}

// ========== 表格 ==========
const gridRef = ref(null)
const tableWrapRef = ref(null)
const tableWrapHeight = ref('400px')

const checkedRows = ref([])
const lastCheckboxIndex = ref(-1)

// ========== 选中状态 localStorage 持久化 ==========
const checkedStorageKey = computed(() => `batch_add_checked_${codeName.value}`)

const saveCheckedToStorage = () => {
  const ids = checkedRows.value.map(r => r.id)
  if (ids.length > 0) {
    localStorage.setItem(checkedStorageKey.value, JSON.stringify(ids))
  } else {
    localStorage.removeItem(checkedStorageKey.value)
  }
}

const restoreCheckedFromStorage = () => {
  const raw = localStorage.getItem(checkedStorageKey.value)
  if (!raw) return
  try {
    const ids = JSON.parse(raw)
    if (!ids.length) return
    const idSet = new Set(ids)
    const rows = tableData.value.filter(r => idSet.has(r.id))
    if (rows.length > 0) {
      checkedRows.value = rows
      nextTick(() => {
        gridRef.value?.setCheckboxRow(rows, true)
      })
    }
  } catch {
    localStorage.removeItem(checkedStorageKey.value)
  }
}

const clearCheckedStorage = () => {
  localStorage.removeItem(checkedStorageKey.value)
}

// 选中变化自动持久化
watch(checkedRows, () => {
  saveCheckedToStorage()
}, { deep: true })

// ========== 列区域选取 ==========
const areaDragging = ref(false)
const areaDragField = ref('')
const areaDragColId = ref('')
const areaDragStartRowId = ref(null)
const areaDragEndRowId = ref(null)
const areaDragMoved = ref(false)
const areaDragStartY = ref(0)
const areaSelectedColumn = ref('')
const areaSelectedColId = ref('')
const areaSelectedStartRowId = ref(null)
const areaSelectedEndRowId = ref(null)
const areaRenderTick = ref(0)
const extDragging = ref(false)
let areaHandleEl = null
let _areaRaf = null

const areaSelectedCount = computed(() => {
  if (!areaSelectedColumn.value) return 0
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return 0
  return Math.abs(eIdx - sIdx) + 1
})

const areaDragRowIdSet = computed(() => {
  if (!areaDragging.value || !areaDragField.value) return null
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaDragStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaDragEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return null
  const min = Math.min(sIdx, eIdx); const max = Math.max(sIdx, eIdx)
  const set = new Set()
  for (let i = min; i <= max; i++) set.add(data[i].id)
  return set
})

const areaSelectedRowIdSet = computed(() => {
  if (!areaSelectedColumn.value) return null
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return null
  const min = Math.min(sIdx, eIdx); const max = Math.max(sIdx, eIdx)
  const set = new Set()
  for (let i = min; i <= max; i++) set.add(data[i].id)
  return set
})

const columns = [
  { type: 'checkbox', title: '', width: 44, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60, fixed: 'left' },
  { field: 'image', title: '图片', width: 90, slots: { default: 'image' }, sortable: true },
  { field: 'manufacturerCode', title: '厂商编号', width: 110, showOverflow: 'ellipsis', visible: false },
  { field: 'sampleCode', title: '公司编号', width: 140, showOverflow: 'ellipsis', sortable: true },
  { field: 'category', title: '种类名称', width: 110, showOverflow: 'ellipsis', visible: false },
  { field: 'categoryCode', title: '种类编号', width: 90, showOverflow: 'ellipsis', visible: false },
  { field: 'sampleName', title: '样品名称', width: 140, showOverflow: 'ellipsis', sortable: true },
  { field: 'englishName', title: '英文名称', width: 150, showOverflow: 'ellipsis', visible: false },
  { field: 'factoryCode', title: '出厂货号', width: 140, showOverflow: 'ellipsis', sortable: true },
  { field: 'sampleUnit', title: '样品单位', width: 100, showOverflow: 'ellipsis', visible: false },
  { field: 'sampleUnitEn', title: '英文单位', width: 100, showOverflow: 'ellipsis', visible: false },
  { field: 'packagingCn', title: '中文包装', width: 120, showOverflow: 'ellipsis', sortable: true },
  { field: 'packageCode', title: '包装编号', width: 100, showOverflow: 'ellipsis', visible: false },
  { field: 'packagingEn', title: '英文包装', width: 100, showOverflow: 'ellipsis', visible: false },
  { field: 'color', title: '颜色', width: 80, showOverflow: 'ellipsis', visible: false },
  { field: 'colorEn', title: '英文颜色', width: 90, showOverflow: 'ellipsis', visible: false },
  { field: 'size', title: '规格尺寸', width: 100, showOverflow: 'ellipsis', visible: false },
  { field: 'origin', title: '产地', width: 80, showOverflow: 'ellipsis', visible: false },
  { field: 'factoryPrice', title: '出厂价', width: 100, headerClassName: 'csd-header-red', className: 'csd-cell-red', sortable: true },
  { field: 'taxPrice', title: '税点价', width: 100, visible: false },
  { field: 'sampleLength', title: '样品长', width: 120, sortable: true },
  { field: 'sampleWidth', title: '样品宽', width: 120, sortable: true },
  { field: 'sampleHeight', title: '样品高', width: 120, sortable: true },
  { field: 'sampleGrossWeight', title: '样品毛重', width: 120, sortable: true },
  { field: 'sampleNetWeight', title: '样品净重', width: 120, sortable: true },
  { field: 'cartonLength', title: '外箱长', width: 120, sortable: true },
  { field: 'cartonWidth', title: '外箱宽', width: 120, sortable: true },
  { field: 'cartonHeight', title: '外箱高', width: 120, sortable: true },
  { field: 'cartonMaterialVolume', title: '材积', width: 80, visible: false },
  { field: 'cartonVolume', title: '体积', width: 80, visible: false },
  { field: 'innerBoxCount', title: '内盒数', width: 110, sortable: true },
  { field: 'cartonCapacity', title: '装箱量', width: 110, sortable: true },
  { field: 'packingUnit', title: '装箱单位', width: 85, visible: false },
  { field: 'packageLength', title: '包装长', width: 120, sortable: true },
  { field: 'packageWidth', title: '包装宽', width: 120, sortable: true },
  { field: 'packageHeight', title: '包装高', width: 120, sortable: true },
  { field: 'cartonGrossWeight', title: '外箱毛重', width: 120, sortable: true },
  { field: 'cartonNetWeight', title: '外箱净重', width: 120, sortable: true },
  { field: 'name', title: '厂商名称', width: 140, showOverflow: 'ellipsis', visible: false },
  { field: 'boothNo', title: '摊位号', width: 80, showOverflow: 'ellipsis', visible: false },
  { field: 'contact1', title: '联系人', width: 90, showOverflow: 'ellipsis', visible: false },
  { field: 'phone1', title: '电话', width: 120, showOverflow: 'ellipsis', visible: false },
  { field: 'mobile1', title: '手机', width: 120, showOverflow: 'ellipsis', visible: false },
  { field: 'fax', title: '传真', width: 120, showOverflow: 'ellipsis', visible: false },
  { field: 'qq', title: 'QQ', width: 90, showOverflow: 'ellipsis', visible: false },
  { field: 'certification', title: '产品认证', width: 120, showOverflow: 'ellipsis', sortable: true },
  { field: 'certificationCount', title: '认证数', width: 70, visible: false },
  { field: 'remark', title: '备注', width: 200, showOverflow: 'ellipsis', sortable: true },
  { field: 'remarkEn', title: '英文备注', width: 140, showOverflow: 'ellipsis', visible: false },
  { field: 'registrant', title: '登记人', width: 100 },
  { field: 'infringement', title: '侵权', width: 70, visible: false },
  { field: 'batteryInfo', title: '电池信息', width: 100, showOverflow: 'ellipsis', visible: false },
  { field: 'modifier', title: '修改人', width: 100 },
  { field: 'updateTime', title: '修改日期', width: 160, showOverflow: 'ellipsis', sortable: true },
  { field: 'createTime', title: '登记时间', width: 160, showOverflow: 'ellipsis', sortable: true }
]

// 表格列设置跨设备同步
const { fullKey: gridStorageKey, saveToBackend: saveGridPrefs, ready: prefReady } = useGridPrefSync(gridRef, 'batch-add', columns)

// 动态列宽
const computedColumns = computed(() => {
  return columns.map(col => {
    if (col.field === 'image') {
      return { ...col, width: 110 }
    }
    return col
  })
})

// 行高
const cellConfig = computed(() => ({
  height: 100,
  align: 'center'
}))

// ========== 区域选取 cell-style ==========
const gridCellStyle = ({ row, column }) => {
  if (!areaDragging.value && !areaSelectedColumn.value) {
    return { textAlign: 'center' }
  }
  void areaRenderTick.value
  const field = (column && (column.field || column.type)) || ''
  if (areaDragging.value && field === areaDragField.value) {
    const set = areaDragRowIdSet.value
    if (set && row && set.has(row.id)) {
      return { textAlign: 'center', background: '#e3f2fd', outline: '2px solid #4285f4', outlineOffset: '-2px' }
    }
  }
  if (areaSelectedColumn.value && field === areaSelectedColumn.value) {
    const set = areaSelectedRowIdSet.value
    if (set && row && set.has(row.id)) {
      return { textAlign: 'center', background: '#dceefb', outline: '2px solid #4285f4', outlineOffset: '-2px' }
    }
  }
  return { textAlign: 'center' }
}

// ========== 区域选取辅助函数 ==========
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

const getFieldByColId = (colId) => {
  const grid = gridRef.value
  if (!grid) return colId
  const cols = grid.getColumns() || []
  const col = cols.find(c => c.id === colId)
  return col ? col.field : colId
}

const onTableWrapMouseDown = (e) => {
  if (e.button !== 0) return
  if (e.target.closest('.ba-area-handle')) return
  if (!tableWrapRef.value?.contains(e.target)) return
  const info = getRowIdAndField(e.target)
  if (!info) return
  areaDragStartRowId.value = info.rowId
  areaDragEndRowId.value = info.rowId
  areaDragColId.value = info.field
  areaDragField.value = getFieldByColId(info.field)
  areaDragging.value = false
  areaDragMoved.value = false
  areaDragStartY.value = e.clientY
  areaSelectedColumn.value = ''
  areaSelectedColId.value = ''
  areaSelectedStartRowId.value = null
  areaSelectedEndRowId.value = null
  areaRenderTick.value++
  document.addEventListener('mousemove', onDocMouseMove)
  document.addEventListener('mouseup', onDocMouseUp)
  e.preventDefault()
}

const onDocMouseMove = (e) => {
  if (!areaDragging.value && !areaDragMoved.value) {
    if (Math.abs(e.clientY - areaDragStartY.value) < 6) return
    areaDragging.value = true
    areaDragMoved.value = true
    document.body.classList.add('ba-area-selecting')
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
  document.body.classList.remove('ba-area-selecting')
  if (_areaRaf) { cancelAnimationFrame(_areaRaf); _areaRaf = null }
  if (!areaDragging.value) {
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
  areaSelectedColumn.value = areaDragField.value
  areaSelectedColId.value = areaDragColId.value
  areaSelectedStartRowId.value = areaDragStartRowId.value
  areaSelectedEndRowId.value = areaDragEndRowId.value
  areaRenderTick.value++
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

const attachAreaHandle = () => {
  removeAreaHandle()
  if (!areaSelectedColId.value) return
  const wrapper = tableWrapRef.value
  if (!wrapper) return
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return
  const lastIdx = Math.max(sIdx, eIdx)
  const lastId = String(data[lastIdx].id)
  requestAnimationFrame(() => {
    const cellEl = wrapper.querySelector(`[rowid="${lastId}"] [colid="${areaSelectedColId.value}"]`)
    if (!cellEl) return
    const td = cellEl.tagName === 'TD' ? cellEl : cellEl.closest('td')
    if (!td) return
    const h = document.createElement('div')
    h.className = 'ba-area-handle'
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
  document.body.classList.add('ba-area-selecting')
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
  document.body.classList.remove('ba-area-selecting')
  document.removeEventListener('mousemove', onExtMouseMove)
  document.removeEventListener('mouseup', onExtMouseUp)
  if (_areaRaf) { cancelAnimationFrame(_areaRaf); _areaRaf = null }
  attachAreaHandle()
}

const getAreaSelectedValues = () => {
  if (!areaSelectedColumn.value) return []
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.id) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.id) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return []
  const min = Math.min(sIdx, eIdx)
  const max = Math.max(sIdx, eIdx)
  const field = areaSelectedColumn.value
  return data.slice(min, max + 1).map(r => ({
    id: r.id,
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

const onDocClick = (e) => {
  if (areaSelectedColumn.value && tableWrapRef.value && !tableWrapRef.value.contains(e.target)) {
    clearAreaSelection()
  }
}

const onCheckboxChange = ({ checked, row, rowIndex, $event, records }) => {
  // Shift 连选：按住 Shift 点击时，选中两次点击之间的所有行
  if ($event && $event.shiftKey && lastCheckboxIndex.value >= 0 && rowIndex != null) {
    const startIdx = Math.min(lastCheckboxIndex.value, rowIndex)
    const endIdx = Math.max(lastCheckboxIndex.value, rowIndex)
    const data = gridRef.value.getTableData().fullData
    for (let i = startIdx; i <= endIdx; i++) {
      gridRef.value.setCheckboxRow(data[i], !!checked)
    }
  }
  lastCheckboxIndex.value = rowIndex != null ? rowIndex : -1
  checkedRows.value = gridRef.value.getCheckboxRecords()
}

const onCheckboxAll = ({ records }) => {
  checkedRows.value = records
}

const onColumnDragEnd = () => {
  setTimeout(() => saveGridPrefs(), 100)
}

const onCustomChange = ({ type }) => {
  if (type === 'confirm' || type === 'reset') {
    setTimeout(() => saveGridPrefs(), 50)
  }
}

const onSelectAll = () => {
  if (cardMode.value) {
    checkedRows.value = [...tableData.value]
    return
  }
  gridRef.value?.setAllCheckboxRow(true)
  checkedRows.value = [...tableData.value]
}

const onInvertSelect = () => {
  const all = tableData.value
  if (!all.length) return
  if (cardMode.value) {
    const checkedIds = new Set(checkedRows.value.map(r => r.id))
    checkedRows.value = all.filter(r => !checkedIds.has(r.id))
    return
  }
  const currentChecked = gridRef.value?.getCheckboxRecords() || []
  const checkedIds = new Set(currentChecked.map(r => r.id))
  const toCheck = all.filter(r => !checkedIds.has(r.id))
  gridRef.value?.setAllCheckboxRow(false)
  if (toCheck.length > 0) {
    gridRef.value?.setCheckboxRow(toCheck, true)
  }
  checkedRows.value = [...toCheck]
}

const onClearSelect = () => {
  if (cardMode.value) {
    checkedRows.value = []
    return
  }
  gridRef.value?.setAllCheckboxRow(false)
  checkedRows.value = []
}

const onSortChange = ({ field, order }) => {
  sortField.value = field
  sortOrder.value = order
  loadData()
}

const onPageChange = (page) => {
  currentPage.value = page
  loadData()
}

const onPageSizeChange = (e) => {
  pageSize.value = Number(e.target.value)
  currentPage.value = 1
  loadData()
}

// ========== 批量添加 ==========
const addLoading = ref(false)
const cardAddingIds = ref(new Set())
const existingSampleCodes = ref(new Set())
const onlyHasImage = ref(false)
const onlyHasVideo = ref(false)

const onImageVideoChange = () => {
  currentPage.value = 1
  loadData('all')
}
const cardMode = ref(false)

// ========== 卡片虚拟滚动 ==========
const cardContainerRef = ref(null)
const cardScrollTop = ref(0)
const cardRowHeight = ref(420) // 估算行高，首次渲染后实测修正
const cardCols = ref(6)
const cardBufferRows = 3
const cardMeasured = ref(false)

const totalCardRows = computed(() => Math.ceil(tableData.value.length / cardCols.value) || 0)

const visibleCardRange = computed(() => {
  const containerH = cardContainerRef.value?.clientHeight || 600
  const startRow = Math.max(0, Math.floor(cardScrollTop.value / cardRowHeight.value) - cardBufferRows)
  const visibleRows = Math.ceil(containerH / cardRowHeight.value)
  const endRow = Math.min(totalCardRows.value, startRow + visibleRows + cardBufferRows * 2)
  return { startRow, endRow }
})

const visibleCardData = computed(() => {
  const { startRow, endRow } = visibleCardRange.value
  const startIdx = startRow * cardCols.value
  const endIdx = Math.min(tableData.value.length, endRow * cardCols.value)
  return tableData.value.slice(startIdx, endIdx)
})

const cardTotalHeight = computed(() => {
  return totalCardRows.value * cardRowHeight.value + 16 // 16 = grid padding
})

const cardOffsetY = computed(() => {
  return visibleCardRange.value.startRow * cardRowHeight.value + 16
})

const onCardScroll = () => {
  if (!cardContainerRef.value) return
  cardScrollTop.value = cardContainerRef.value.scrollTop
}

const measureCardRowHeight = () => {
  if (cardMeasured.value) return
  const grid = cardContainerRef.value?.querySelector('.csd-card-grid')
  if (!grid) return
  const items = grid.querySelectorAll('.csd-card-item')
  if (items.length < cardCols.value) return
  // 取第一行最后一个卡片底部 - 第一个卡片顶部 = 行高
  const firstTop = items[0].getBoundingClientRect().top
  const lastInRow = items[cardCols.value - 1]
  const rowBottom = lastInRow.getBoundingClientRect().bottom
  const measured = rowBottom - firstTop + 14 // 14 = grid gap
  if (measured > 50) {
    cardRowHeight.value = measured
    cardMeasured.value = true
  }
}

// 卡片/列表模式切换：卡片模式启用虚拟滚动+测量行高，列表模式恢复表格高度自适应
watch(cardMode, async (v) => {
  if (v) {
    cardScrollTop.value = 0
    cardMeasured.value = false
    nextTick(() => {
      requestAnimationFrame(() => measureCardRowHeight())
    })
  } else {
    await nextTick()
    updateTableHeight()
    if (tableWrapRef.value) {
      if (resizeObserver) resizeObserver.disconnect()
      resizeObserver = new ResizeObserver(() => updateTableHeight())
      resizeObserver.observe(tableWrapRef.value)
    }
  }
})

const isCardChecked = (row) => {
  return checkedRows.value.some(r => r.id === row.id)
}

const toggleCardSelect = (row) => {
  const idx = checkedRows.value.findIndex(r => r.id === row.id)
  if (idx > -1) {
    checkedRows.value.splice(idx, 1)
  } else {
    checkedRows.value.push(row)
  }
  if (gridRef.value) {
    gridRef.value.setCheckboxRow(row, idx === -1)
  }
}

const copyCardCode = async (code) => {
  if (!code) return
  try {
    await navigator.clipboard.writeText(code)
  } catch {
    // fallback: 使用传统方法
    const textarea = document.createElement('textarea')
    textarea.value = code
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
    } catch { /* ignore */ }
    document.body.removeChild(textarea)
  }
}

const onCardImgError = (e) => {
  e.target.style.display = 'none'
  const next = e.target.nextElementSibling
  if (next && next.classList.contains('csd-card-no-img')) {
    next.style.display = 'flex'
  }
}

const loadExistingItems = async () => {
  try {
    const res = await api(`/client-samples/${codeName.value}/items`)
    const data = res.data || res || []
    const items = Array.isArray(data) ? data : []
    existingSampleCodes.value = new Set(items.map(item => item.sampleCode).filter(Boolean))
  } catch (e) {
    console.error('加载已有样品失败:', e)
  }
}

// 格式化重复编号提示文本，超过限制条数时省略显示
const formatDuplicateCodes = (duplicates, maxShow = 10) => {
  if (duplicates.length <= maxShow) {
    return duplicates.map(r => r.sampleCode).join('、')
  }
  const shown = duplicates.slice(0, maxShow).map(r => r.sampleCode).join('、')
  return `${shown} 等 ${duplicates.length} 条重复`
}

const onBatchAdd = async () => {
  if (checkedRows.value.length === 0) return
  if (addLoading.value) return

  const duplicates = checkedRows.value.filter(r => existingSampleCodes.value.has(r.sampleCode))
  const toAdd = checkedRows.value.filter(r => !existingSampleCodes.value.has(r.sampleCode))

  if (toAdd.length === 0) {
    appToast(`本次添加成功0条，${duplicates.length}条重复已跳过`, 1800, 'app-toast-lg')
    gridRef.value?.setAllCheckboxRow(false)
    checkedRows.value = []
    return
  }

  let confirmMsg = `确认添加选中的 ${toAdd.length} 条样品到 <span style="color:#ff3b30;font-weight:700">代号</span>："${codeName.value}"？`
  if (duplicates.length > 0) {
    confirmMsg += `  （${duplicates.length}条重复将跳过）`
  }
  const confirmed = await appConfirm(confirmMsg, '确认操作')
  if (!confirmed) return

  addLoading.value = true
  try {
    const sampleIds = toAdd.map(r => r.id)
    const res = await api(`/client-samples/${codeName.value}/items`, {
      method: 'POST',
      body: JSON.stringify(sampleIds)
    })
    if (res.code === 200) {
      const codes = toAdd.map(r => r.sampleCode)
      existingSampleCodes.value = new Set([...existingSampleCodes.value, ...codes])
      if (duplicates.length > 0) {
        appToast(`成功添加${toAdd.length}条，${duplicates.length}条重复跳过`, 1800, 'app-toast-lg')
      } else {
        appToast(`成功添加 ${toAdd.length} 条样品`, 1800, 'app-toast-lg')
      }
      gridRef.value?.setAllCheckboxRow(false)
      checkedRows.value = []
    } else {
      appAlert('批量添加失败: ' + (res.message || '未知错误'), '错误', 'danger')
    }
  } catch (e) {
    console.error('批量添加失败:', e)
    appAlert('批量添加失败，请检查网络连接', '错误', 'danger')
  } finally {
    addLoading.value = false
  }
}

const addSingleCard = async (row) => {
  if (cardAddingIds.value.has(row.id)) return
  let skipSecondConfirm = false
  let force = false
  if (existingSampleCodes.value.has(row.sampleCode)) {
    const proceed = await appConfirm(`公司编号 "${row.sampleCode}" 已存在于当前代号中，是否仍然添加？`, '重复提醒', 'danger')
    if (!proceed) return
    skipSecondConfirm = true
    force = true
  }
  if (!skipSecondConfirm) {
    const confirmed = await appConfirm(`确认添加样品 "${row.sampleName || row.sampleCode}" 到 <span style="color:#ff3b30;font-weight:700">代号</span>："${codeName.value}"？`, '确认操作')
    if (!confirmed) return
  }

  cardAddingIds.value = new Set([...cardAddingIds.value, row.id])
  try {
    const query = force ? '?force=true' : ''
    const res = await api(`/client-samples/${codeName.value}/items${query}`, {
      method: 'POST',
      body: JSON.stringify([row.id])
    })
    if (res.code === 200) {
      existingSampleCodes.value = new Set([...existingSampleCodes.value, row.sampleCode])
      appToast('添加成功', 1800, 'app-toast-lg')
    } else {
      appAlert('添加失败: ' + (res.message || '未知错误'), '错误', 'danger')
    }
  } catch (e) {
    console.error('添加失败:', e)
    appAlert('添加失败，请检查网络连接', '错误', 'danger')
  } finally {
    const next = new Set(cardAddingIds.value)
    next.delete(row.id)
    cardAddingIds.value = next
  }
}

const onCellDblclick = ({ row }) => {
  if (row) addSingleCard(row)
}

// ========== 表格高度自适应 ==========
let resizeObserver = null

const updateTableHeight = () => {
  if (tableWrapRef.value) {
    const rect = tableWrapRef.value.getBoundingClientRect()
    tableWrapHeight.value = Math.max(200, rect.height) + 'px'
  }
}

onMounted(() => {
  nextTick(() => updateTableHeight())
  resizeObserver = new ResizeObserver(() => updateTableHeight())
  if (tableWrapRef.value) {
    resizeObserver.observe(tableWrapRef.value)
  }
  // 区域选取：注册事件
  document.addEventListener('mousedown', onTableWrapMouseDown, true)
  window.addEventListener('keydown', onAreaCopyKey, true)
  document.addEventListener('copy', onAreaCopyEvent, true)
  document.addEventListener('click', onDocClick)
  // 滚动时重新挂把手
  const wrapper = tableWrapRef.value
  if (wrapper) {
    let handleScrollTimer = null
    wrapper.addEventListener('scroll', () => {
      if (!areaHandleEl || !document.contains(areaHandleEl)) {
        if (handleScrollTimer) clearTimeout(handleScrollTimer)
        handleScrollTimer = setTimeout(attachAreaHandle, 150)
      }
    }, { passive: true })
  }
  // 自动加载全部数据
  loadData()
  loadExistingItems()
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
  document.removeEventListener('mousedown', onTableWrapMouseDown, true)
  window.removeEventListener('keydown', onAreaCopyKey, true)
  document.removeEventListener('copy', onAreaCopyEvent, true)
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('mousemove', onDocMouseMove)
  document.removeEventListener('mouseup', onDocMouseUp)
  removeAreaHandle()
})

// ========== 悬浮大图预览 ==========
const hoverPreview = reactive({
  show: false,
  src: '',
  fallback: '',
  x: 0,
  y: 0
})
let hoverTimer = null

const onThumbMouseEnter = (e, row) => {
  if (showPhotoModal.value) return
  if (!row.thumbnail) return
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

// 路由切换时关闭模态框
watch(route, () => {
  showPhotoModal.value = false
  showFullPreview.value = false
})

// ========== 图片预览模态框（样品信息预览） ==========
const showPhotoModal = ref(false)
const showFullPreview = ref(false)
const fullPreviewSrc = ref('')
const fullPreviewZoom = ref(1)
const fullPreviewPanX = ref(0)
const fullPreviewPanY = ref(0)
const fullPreviewDragging = ref(false)
const fullPreviewDragStart = ref({ x: 0, y: 0, px: 0, py: 0 })
const photoModalSample = ref(null)
const photoModalImages = ref([])
const photoModalIndex = ref(0)
const hideFactoryPrice = ref(false)
const hideSupplierInfo = ref(false)
const editing = ref(false)
const { photoModalPos, photoModalW, photoModalH, photoModalInit, startDragModal } = useModalDrag()

const photoModalStyle = computed(() => ({
  display: showPhotoModal.value ? 'flex' : 'none',
  flexDirection: 'column',
  width: photoModalW.value + 'px',
  height: photoModalH.value + 'px',
  top: photoModalPos.y + 'px',
  left: photoModalPos.x + 'px',
  position: 'fixed'
}))

const fmt3 = (a, b, c) => {
  if ((a == null || a === '') && (b == null || b === '') && (c == null || c === '')) return '0'
  return [(a != null && a !== '' ? a : '0'), (b != null && b !== '' ? b : '0'), (c != null && c !== '' ? c : '0')].join('x')
}

const openPhotoModal = async (row) => {
  hoverPreview.show = false
  clearTimeout(hoverTimer)
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
    const res = await api(`/images/sample/${sampleId}`)
    const raw = (res.data || res) || []
    const images = Array.isArray(raw) ? raw : []
    if (images.length > 0) {
      photoModalImages.value = images
    }
  } catch {
    // 保持初始图片
  }
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

// ── 全屏大图预览（支持滚轮缩放 + 拖拽平移）──
const openFullPreview = () => {
  const img = photoModalImages.value[photoModalIndex.value]
  if (img) {
    fullPreviewSrc.value = img.hash ? '/images/view/hash/' + img.hash : '/thumbnails/' + img.thumbnailPath
    fullPreviewZoom.value = 1
    fullPreviewPanX.value = 0
    fullPreviewPanY.value = 0
    showFullPreview.value = true
  }
}
const closeFullPreview = () => {
  showFullPreview.value = false
  fullPreviewSrc.value = ''
}
const onFullPreviewWheel = (e) => {
  e.preventDefault()
  const delta = e.deltaY > 0 ? -0.15 : 0.15
  fullPreviewZoom.value = Math.max(0.3, Math.min(5, +(fullPreviewZoom.value + delta).toFixed(2)))
}
const onFullPreviewMouseDown = (e) => {
  if (fullPreviewZoom.value <= 1) return
  fullPreviewDragging.value = true
  fullPreviewDragStart.value = { x: e.clientX, y: e.clientY, px: fullPreviewPanX.value, py: fullPreviewPanY.value }
}
const onFullPreviewMouseMove = (e) => {
  if (!fullPreviewDragging.value) return
  fullPreviewPanX.value = fullPreviewDragStart.value.px + (e.clientX - fullPreviewDragStart.value.x)
  fullPreviewPanY.value = fullPreviewDragStart.value.py + (e.clientY - fullPreviewDragStart.value.y)
}
const onFullPreviewMouseUp = () => {
  fullPreviewDragging.value = false
}

const onModalImgError = (e) => {
  const img = photoModalImages.value[photoModalIndex.value]
  if (img?.thumbnailPath && e.target.src !== '/thumbnails/' + img.thumbnailPath) {
    e.target.src = '/thumbnails/' + img.thumbnailPath
  }
}

// ── 图片拖拽到桌面 ──
const onBatchAddImgDragStart = (e) => {
  const src = e.target.currentSrc || e.target.src
  if (!src) return
  const fullUrl = src.startsWith('http') ? src : window.location.origin + src
  const fileName = src.split('/').pop().split('?')[0] || 'image.jpg'
  e.dataTransfer.setData('DownloadURL', `image/jpeg:${fileName}:${fullUrl}`)
  e.dataTransfer.effectAllowed = 'copyMove'
}
</script>

<style scoped>
.csd-search-wide {
  width: 11em;
}

/* 工具栏放大 */
.csd-toolbar-card {
  padding: 1.1em 1.6em !important;
  font-size: clamp(12px, 1.1vw + 4px, 17px) !important;
}

.csd-toolbar-card .csd-btn {
   height: 56px !important;
   min-height: 56px !important;
   font-size: 1.25em !important;
   font-weight: 500 !important;
   padding: 0 1.4em !important;
   border-radius: 0.7em !important;
 }

.csd-toolbar-card .csd-search {
  width: 11em;
  height: 3.4em;
}

.csd-toolbar-card .csd-search input {
  font-size: 0.88em;
  padding: 0 0.85em 0 2.4em !important;
}

.csd-toolbar-card .csd-toolbar-sep {
  height: 28px;
}

.csd-btn-orange {
  background: linear-gradient(135deg, #f97316, #ea580c) !important;
  color: #fff !important;
  border-color: transparent !important;
  box-shadow: 0 2px 6px rgba(249,115,22,0.25) !important;
}

.csd-btn-orange:hover {
  background: linear-gradient(135deg, #fb923c, #f97316) !important;
  box-shadow: 0 4px 10px rgba(249,115,22,0.35) !important;
}

/* 复选框按钮 */
.csd-check-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 56px;
  padding: 0 20px;
  font-size: 1.25em;
  font-weight: 500;
  border: 2px solid #d1d5db;
  border-radius: 0.7em;
  background: #fff;
  color: #6b7280;
  cursor: pointer;
  user-select: none;
  transition: all 0.15s;
}

.csd-check-btn:hover {
  border-color: #93c5fd;
  background: rgba(0,122,255,0.03);
}

.csd-check-btn input[type="checkbox"] {
  width: 16px; height: 16px;
  cursor: pointer;
  accent-color: #007aff;
}

.csd-check-btn:has(input:checked) {
  border-color: #007aff;
  background: rgba(0,122,255,0.06);
  color: #007aff;
}

/* 复选框按钮 */
.csd-check-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 56px;
  padding: 0 20px;
  font-size: 1.25em;
  font-weight: 500;
  border-radius: 0.7em;
  border: 1px solid #d1d5db;
  background: #fff;
  color: #374151;
   cursor: pointer;
   user-select: none;
  transition: all 0.15s;
}
.csd-check-btn:hover { border-color: #007aff; }
.csd-check-btn input[type="checkbox"] {
  width: 16px; height: 16px; cursor: pointer; accent-color: #f97316;
}
.csd-check-btn:has(input:checked) {
  background: #fff7ed;
  border-color: #f97316;
  color: #f97316;
}

/* 表格状态栏也放大一点 */
.csd-statusbar {
  height: 80px !important;
  min-height: 80px !important;
  flex-basis: 80px !important;
  gap: 16px !important;
}

.csd-statusbar .csd-btn {
   height: 56px !important;
   min-height: 56px !important;
   font-size: 20px !important;
   font-weight: 500 !important;
   padding: 0 24px !important;
 }
 
 .csd-statusbar .csd-page-size-select {
   height: 48px !important;
   font-size: 17px !important;
   padding: 0 12px !important;
  border-radius: 6px !important;
  border: 1px solid #d1d5db !important;
}

.csd-statusbar .csd-page-text {
  font-size: 18px !important;
}

.csd-page-size-label {
  font-size: 17px !important;
  font-weight: 500 !important;
}

.csd-status-info {
  font-size: 17px !important;
  font-weight: 500 !important;
}

.csd-pagination {
   gap: 14px !important;
   margin-left: 8px !important;
 }

/* ========== 综合查询弹窗 ========== */
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
  transition: border-color 0.15s;
  box-sizing: border-box;
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

/* 综合查询 - SimpleDatePicker */
.adv-field-range :deep(.sdp-input) {
  height: 56px;
  font-size: 22px;
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
.adv-field-range :deep(.sdp-placeholder) { color: #999; flex: 1; }
.adv-field-range :deep(.sdp-value) { color: #007aff; flex: 1; }
.adv-field-range :deep(.sdp-icon) { width: 26px; height: 26px; color: #999; flex-shrink: 0; }
.adv-field-range :deep(.sdp-clear) { width: 24px; height: 24px; color: #ccc; flex-shrink: 0; cursor: pointer; }
.adv-field-range :deep(.sdp-clear:hover) { color: #999; }

/* ========== 批量编号/货号输入面板 ========== */
.code-input-panel {
  width: min(1300px, calc(100vw - 40px));
  position: fixed;
  top: 50%; left: 50%; transform: translate(-50%, -50%);
  z-index: 100;
  margin: 0;
  background: #fff; border-radius: 28px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.1);
  display: flex; flex-direction: column; overflow: hidden;
}

.code-input-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 32px 40px 0;
  cursor: move;
  user-select: none;
}

.code-input-title {
  font-size: 28px; font-weight: 700; color: #1d1d1f;
}

.code-input-close {
  width: 44px; height: 44px;
  border: none; background: #f3f4f6; border-radius: 50%;
  font-size: 24px; color: #6b7280; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.15s;
}
.code-input-close:hover { background: #e5e7eb; color: #111; }

.code-input-body {
  display: flex; gap: 0;
  padding: 28px 40px;
}

.code-input-left {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column;
}

.code-input-right {
  width: 380px; flex-shrink: 0;
  display: flex; flex-direction: column;
  border-left: 1px solid #e5e7eb;
  padding-left: 28px; margin-left: 28px;
}

.code-input-subtitle {
  font-size: 22px; font-weight: 600; color: #374151;
  margin-bottom: 14px; padding-bottom: 12px;
  border-bottom: 2px solid #007aff;
}

.code-input-result-list {
  flex: 1;
  overflow-y: auto;
  background: #f9fafb;
  border-radius: 12px;
  padding: 16px;
  max-height: 560px;
}

.code-input-empty {
  font-size: 18px; color: #9ca3af;
  text-align: center; padding: 40px 12px;
}

.code-input-all-found {
  color: #10b981; font-weight: 600;
}

.code-input-notfound-item {
  font-size: 18px; color: #ef4444;
  padding: 10px 14px;
  background: #fef2f2;
  border-radius: 6px;
  margin-bottom: 6px;
  word-break: break-all;
}

.code-input-textarea {
  width: 100%;
  min-height: 520px;
  padding: 20px;
  border: 1px solid #d1d5db;
  border-radius: 12px;
  font-size: 22px;
  font-family: inherit;
  line-height: 1.8;
  outline: none;
  resize: vertical;
  transition: border-color 0.15s;
}

.code-input-textarea:focus {
  border-color: #007aff;
  box-shadow: 0 0 0 3px rgba(0,122,255,0.1);
}

.code-input-footer {
  display: flex; align-items: center; justify-content: flex-end; gap: 12px;
  padding: 20px 40px 28px;
}

.code-input-footer .csd-btn {
  height: 56px !important;
  min-height: 56px !important;
  font-size: 22px !important;
  font-weight: 500 !important;
  padding: 0 28px !important;
}

/* ========== 卡片模式 ========== */
.csd-card-overlay {
    flex: 1;
    min-height: 200px;
    overflow-y: auto;
    overflow-x: hidden;
    background: #f7f8fa;
    border-radius: 0 0 12px 12px;
    contain: strict;
  }

.csd-card-virtual-wrap {
  position: relative;
  width: 100%;
}

.csd-card-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  padding: 24px;
  will-change: transform;
}

.csd-card-item {
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
}

.csd-card-item:hover {
  border-color: rgba(0,122,255,0.3);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  transform: translateY(-3px);
}

.csd-card-item.csd-card-selected {
  border-color: #007aff;
  box-shadow: 0 0 0 3px rgba(0,122,255,0.15);
}

.csd-card-img {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  background: #f3f4f6;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.csd-card-img img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #fafafa;
}

.csd-card-no-img {
  color: #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
}

.csd-card-checkbox {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border: 2px solid #fff;
  border-radius: 6px;
  background: rgba(0,0,0,0.15);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  z-index: 2;
  transition: all 0.15s ease;
}

.csd-card-checkbox.checked {
  background: #007aff;
  border-color: #007aff;
}

.csd-card-checkbox:hover {
  background: rgba(0,0,0,0.3);
}

.csd-card-body {
  padding: 16px 18px 18px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.csd-card-name {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 12px;
}

.csd-card-fields {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px 10px;
  align-items: start;
  margin-top: 6px;
}

.csd-card-val {
  font-size: 30px;
  color: #000;
  font-family: "SimSun", "宋体", serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.csd-card-val-copy {
  display: flex;
  align-items: center;
  gap: 4px;
}

.csd-card-copy-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 3px;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transition: color 0.15s, background 0.15s;
}

.csd-card-copy-btn:hover {
  color: #007aff;
  background: rgba(0,122,255,0.08);
}

.csd-card-price {
  font-size: 28px !important;
  font-weight: 700;
  color: #e11d48;
}

.csd-card-code {
  color: #007aff;
}

.csd-card-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 8px 0;
}

.csd-card-last-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.csd-card-add-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 44px;
  padding: 0 20px;
  font-size: 20px;
  font-weight: 600;
  border: 1px solid #007aff;
  border-radius: 8px;
  background: #007aff;
  color: #fff;
  cursor: pointer;
  transition: all 0.15s;
}

.csd-card-add-btn:hover:not(:disabled) {
  background: #0056cc;
  border-color: #0056cc;
}

.csd-card-add-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.spin {
  animation: spin 0.8s linear infinite;
}

/* ========== 图像搜索内嵌面板 ========== */
.isp-embed-panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid rgba(0,122,255,0.10);
  border-radius: 28px;
  box-shadow: 0 16px 36px rgba(0,122,255,0.075);
  overflow: hidden;
}

.isp-embed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;
  background: linear-gradient(145deg, rgba(255,255,255,0.86), rgba(235,247,255,0.56));
  border-bottom: 1px solid rgba(0,122,255,0.10);
  flex-shrink: 0;
}

.isp-embed-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.isp-embed-header .csd-btn {
  height: 44px !important;
  font-size: 18px !important;
}

.isp-embed-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* vxe 表格单元格居中 */
:deep(.vxe-body--column) {
  text-align: center !important;
}

/* 悬浮大图预览 */
.sr-hover-preview {
  position: fixed;
  z-index: 10000;
  pointer-events: none;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.25), 0 0 0 1px rgba(0,0,0,0.08);
  overflow: hidden;
  max-width: 620px;
  max-height: 600px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.sr-hover-preview img {
  width: 588px;
  height: 588px;
  object-fit: contain;
  border-radius: 8px;
  background: #f5f5f7;
}
.hover-preview-fade-enter-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.hover-preview-fade-leave-active {
  transition: opacity 0.1s ease;
}
.hover-preview-fade-enter-from {
  opacity: 0;
  transform: scale(0.92);
}
.hover-preview-fade-leave-to {
  opacity: 0;
}

/* ===== 按厂商选品模态框 ===== */
.factory-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.32);
  backdrop-filter: blur(6px);
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
}

.factory-modal-panel {
  width: 1100px;
  max-width: 96vw;
  background: #fff;
  border-radius: 32px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.14);
  overflow: hidden;
}

.factory-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 42px 56px 0;
}

.factory-modal-header h3 {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 42px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0;
}

.factory-modal-close {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #86868b;
  transition: all 0.2s;
}

.factory-modal-close:hover {
  background: rgba(0, 0, 0, 0.08);
  color: #1d1d1f;
}

.factory-modal-body {
  padding: 42px 56px;
}

.factory-modal-body .adv-field {
  margin-bottom: 44px;
}

.factory-modal-body .adv-field label {
  display: block;
  margin-bottom: 18px;
  font-size: 30px;
  font-weight: 650;
  color: #1d1d1f;
}

.factory-modal-body .adv-field input {
  width: 100%;
  height: 88px;
  border-radius: 20px;
  border: 1px solid rgba(0, 122, 255, 0.16);
  padding: 0 28px;
  font-size: 30px;
  font-weight: 650;
  outline: none;
  background: rgba(255, 255, 255, 0.88);
  box-sizing: border-box;
  color: #1d1d1f;
  transition: border 0.25s;
}

.factory-modal-body .adv-field input:focus {
  border-color: rgba(0, 122, 255, 0.45);
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.08);
}

.adv-field-hint {
  display: block;
  margin-top: 12px;
  font-size: 24px;
  color: #86868b;
}

.factory-preview-tip {
  padding: 24px 28px;
  background: rgba(52, 199, 89, 0.06);
  border-radius: 16px;
  font-size: 26px;
  color: #1d1d1f;
  text-align: center;
}

.factory-preview-tip strong {
  color: #007aff;
  font-weight: 700;
}

.factory-modal-summary {
  padding: 24px 28px;
  background: rgba(0, 122, 255, 0.04);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 24px;
  color: #1d1d1f;
}

.factory-modal-formula {
  font-size: 20px;
  color: #86868b;
}

.factory-modal-footer {
  display: flex;
  gap: 20px;
  justify-content: flex-end;
  padding: 0 56px 42px;
}

.factory-modal-footer .csd-btn {
  height: 72px;
  padding: 0 52px;
  font-size: 30px;
  border-radius: 18px;
  font-weight: 650;
}

.spin-icon {
  animation: spin 1s linear infinite;
}

/* ── 全屏大图预览（滚轮缩放）── */
.full-preview-overlay {
  position: fixed;
  inset: 0;
  z-index: 100000;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.full-preview-overlay img {
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  transition: transform 0.08s ease-out;
  user-select: none;
  -webkit-user-drag: element;
}
.full-preview-toolbar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(0,0,0,0.65);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  padding: 6px 12px;
  z-index: 100001;
}
.full-preview-zoom-label {
  font-size: 12px;
  color: rgba(255,255,255,0.75);
  min-width: 40px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}
.full-preview-btn {
  width: 32px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: rgba(255,255,255,0.12);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}
.full-preview-btn:hover {
  background: rgba(255,255,255,0.22);
}
.full-preview-close {
  font-size: 20px;
  margin-left: 6px;
}
</style>

<style scoped>
/* ── batch-photo-modal 模态框样式（独立,不依赖共享 spm-*） ── */

.batch-photo-modal {
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
  gap: 1px;
  background: #fff;
  flex: 1;
  min-height: 0;
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
  gap: 8px;
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
  width: 96px;
  height: 72px;
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
  min-width: 0;
  background: #fff;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 0;
  overflow-y: auto;
}

.spm-field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
}

.spm-field {
  display: flex;
  align-items: baseline;
  gap: 15px;
  padding: 10px 14px;
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
  width: 120px;
  font-size: 24px;
  color: rgba(29,29,31,0.46);
  white-space: nowrap;
  flex-shrink: 0;
  font-weight: 600;
  text-align: left;
}
.spm-field-label:not(:first-child) {
  width: auto;
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

.spm-section-title {
  font-size: 21px;
  font-weight: 700;
  color: rgba(29,29,31,0.55);
  padding: 8px 10px 4px;
  margin-top: 4px;
  border-top: 1px dashed #e2e4ea;
}

.spm-input {
  flex: 1;
  height: 34px;
  min-width: 0;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 0 10px;
  font-size: 15px;
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
  width: 60px;
  flex: none;
  text-align: center;
  padding: 0 6px;
}
.spm-input-ro {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 600;
  color: #6b7280;
}
input.spm-input-ro {
  background: #f5f5f7;
  cursor: default;
  color: #6b7280;
  font-weight: 600;
}

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
  font-size: 14px;
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
