<template>
  <div class="sr-designer">
    <!-- viewOnly 模式顶栏 -->
    <div class="sr-viewonly-topbar" v-if="viewOnly">
      <span class="sr-viewonly-title">{{ config.title }}</span>
      <span class="sr-viewonly-pages" v-if="previewMode">{{ previewPage }}/{{ previewTotalPages }} 页</span>
      <div class="sr-viewonly-actions">
        <button class="sr-viewonly-btn" @click="goPreviewPage(previewPage-1)" :disabled="previewPage <= 1">◀ 上页</button>
        <button class="sr-viewonly-btn" @click="goPreviewPage(previewPage+1)" :disabled="previewPage >= previewTotalPages">下页 ▶</button>
        <button class="sr-viewonly-btn sr-viewonly-btn-pri" @click="doPrint">打印</button>
        <button class="sr-viewonly-btn sr-viewonly-btn-sec" @click="doExportFull">导出Excel</button>
        <button class="sr-viewonly-btn" @click="closeViewOnly">关闭</button>
      </div>
    </div>

    <!-- 正常模式顶部工具-->
    <DesignerToolbar
      v-if="!viewOnly"
      :zoomLevel="zoomLevel"
      :zoomIn="zoomIn"
      :zoomOut="zoomOut"
      :reportTitle="config.title"
      @new="onToolbarNew"
      @save="saveConfig"
      @preview="doPreview"
      @print="doPrint"
      @export="doExport"
      @drafts="openDrafts"
    />
    
    <div class="sr-main">
      <!-- 左：数据+ 字段列表 (仅正常模 -->
      <div class="sr-left" v-if="!viewOnly">
        <div class="sr-left-header">
          <span>数据</span>
          <div class="ds-import-wrap">
            <button class="ds-import-btn" @click="showImportMenu = !showImportMenu">+ 导入 </button>
            <div class="ds-import-menu" v-if="showImportMenu">
              <div class="import-item" @click="quickImport('manufacturer')">厂商资料</div>
              <div class="import-item" @click="quickImport('samples')">样品资料</div>
              <div class="import-item" @click="quickImport('client-items')">择样明细</div>
              <div class="import-item" @click="quickImport('customer-info')">客户资料</div>
              <div class="import-item" @click="showImportModal = true; showImportMenu = false">自定义（CSV/SQL/API）</div>
            </div>
          </div>
        </div>
        <div class="sr-left-body">
          <div class="ds-search-wrap">
            <input class="ds-search" v-model="fieldSearch" placeholder="搜索字段..." />
            <span class="ds-search-clear" v-if="fieldSearch" @click="fieldSearch=''">✕</span>
          </div>
          <div class="ds-group" v-for="ds in filteredDatasets" :key="ds.name">
            <div class="ds-group-head" @click="dsExpanded[ds.name] = !dsExpanded[ds.name]">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#6b7280" stroke-width="2" :class="{ expanded: ds.expanded }" style="transition: .15s; flex-shrink: 0;">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
              <span class="ds-name-text">{{ ds.name }}</span>
              <span class="ds-count" v-if="fieldSearch">{{ ds.visibleFields.length }}/{{ ds.rawFields.length }}</span>
              <button class="ds-del-btn" title="删除数据集" @click.stop="removeDataset(ds.name)">&times;</button>
            </div>
            <div v-if="ds.expanded" class="ds-fields">
              <div class="ds-empty" v-if="!ds.visibleFields.length">{{ fieldSearch ? '无匹配字段' : '暂无字段，请先导入数据' }}</div>
              <div v-for="f in ds.visibleFields" :key="f.field"
                class="ds-field" draggable="true"
                @dragstart="onFieldDragStart($event, ds.name, f)"
              >
                <span class="df-key">{{ f.field }}</span>
                <span class="df-info">{{ f.title }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中：电子表格设计-->
      <div class="sr-center">
        <!-- Format 工具-->
        <div class="sr-format-bar">
          <!-- 预览模式提示-->
          <div class="preview-banner" v-if="previewMode">
            <span class="preview-badge">预览模式</span>
            <span class="preview-info">{{ previewTotal }} &middot; 第{{ previewPage }}/{{ previewTotalPages }}&middot; {{ previewRowCount }} </span>
            <button class="fmt-btn fmt-btn-preview" @click="goPreviewPage(previewPage-1)" :disabled="previewPage <= 1" style="background:#6b7280;border-color:#6b7280;padding:4px 12px;font-size:24px;">◀</button>
            <button class="fmt-btn fmt-btn-preview" @click="goPreviewPage(previewPage+1)" :disabled="previewPage >= previewTotalPages" style="background:#6b7280;border-color:#6b7280;padding:4px 12px;font-size:24px;"></button>
            <button class="fmt-btn fmt-btn-preview" @click="doPrint" style="background:#9333ea;border-color:#9333ea;margin-left:16px;">打印</button>
            <button class="fmt-btn fmt-btn-preview" @click="doExportFull" style="background:#16a34a;border-color:#16a34a;margin-left:8px;">导出完整报表</button>
            <button class="fmt-btn fmt-btn-preview" @click="exitPreview" style="background:#dc2626;border-color:#dc2626;margin-left:auto;">返回设计</button>
          </div>
          <div class="fmt-group" v-show="!previewMode">
            <button class="fmt-btn" title="撤销" @click="undo" :disabled="undoStack.length === 0">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 10h10a5 5 0 0 1 0 10H9"/><polyline points="7 6 3 10 7 14"/></svg>
            </button>
            <button class="fmt-btn" title="重做" @click="redo" :disabled="redoStack.length === 0">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10H11a5 5 0 0 0 0 10h4"/><polyline points="17 6 21 10 17 14"/></svg>
            </button>
            <span class="fmt-sep"></span>
            <select v-model.number="fmt.fontSize" class="fmt-sel" @change="applyFmt">
              <option v-for="s in [8,9,10,11,12,14,16,18,20,22,24]" :key="s" :value="s">{{ s }}</option>
            </select>
            <select v-model="fmt.fontFamily" class="fmt-sel fmt-sel-w" @change="applyFmt">
              <option value="SimSun, serif">宋体</option>
              <option value="SimHei, sans-serif">黑体</option>
              <option value="KaiTi, serif">楷体</option>
              <option value="Microsoft YaHei, sans-serif">微软雅黑</option>
              <option value="Arial, sans-serif">Arial</option>
            </select>
          </div>
          <span class="fmt-sep" v-show="!previewMode"></span>
          <input ref="logoInput" type="file" accept="image/*" style="display:none" @change="onLogoUpload" />
          <button class="fmt-btn fmt-btn-merge" title="上传Logo" @click="logoInput.click()" v-show="!previewMode">{{ config.logoImage ? '更换Logo' : '上传Logo' }}</button>
          <span class="fmt-sep" v-show="!previewMode"></span>
          <button class="fmt-btn" :class="{ on: fmt.bold }" title="加粗" @click="fmt.bold = !fmt.bold; applyFmt()" v-show="!previewMode"><b>B</b></button>
          <button class="fmt-btn" :class="{ on: fmt.italic }" title="斜体" @click="fmt.italic = !fmt.italic; applyFmt()" v-show="!previewMode"><i>I</i></button>
          <button class="fmt-btn" :class="{ on: fmt.underline }" title="下划线" @click="fmt.underline = !fmt.underline; applyFmt()" v-show="!previewMode"><u>U</u></button>
          <span class="fmt-sep" v-show="!previewMode"></span>
          <button class="fmt-btn" :class="{ on: selAlign === 'left' }" title="左对齐" @click="selAlign = 'left'; applyFmt()" v-show="!previewMode">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="17" y1="10" x2="3" y2="10"/><line x1="21" y1="6" x2="3" y2="6"/><line x1="17" y1="14" x2="3" y2="14"/><line x1="21" y1="18" x2="3" y2="18"/></svg>
          </button>
          <button class="fmt-btn" :class="{ on: selAlign === 'center' }" title="居中" @click="selAlign = 'center'; applyFmt()" v-show="!previewMode">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="10" x2="6" y2="10"/><line x1="21" y1="6" x2="3" y2="6"/><line x1="21" y1="14" x2="3" y2="14"/><line x1="18" y1="18" x2="6" y2="18"/></svg>
          </button>
          <button class="fmt-btn" :class="{ on: selAlign === 'right' }" title="右对齐" @click="selAlign = 'right'; applyFmt()" v-show="!previewMode">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="21" y1="10" x2="7" y2="10"/><line x1="21" y1="6" x2="3" y2="6"/><line x1="21" y1="14" x2="7" y2="14"/><line x1="21" y1="18" x2="3" y2="18"/></svg>
          </button>
          <span class="fmt-sep" v-show="!previewMode"></span>
          <button class="fmt-btn" :class="{ on: selVAlign === 'top' }" title="顶端对齐" @click="selVAlign = 'top'; applyFmt()" v-show="!previewMode">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="4" x2="21" y2="4"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="3" y1="14" x2="21" y2="14"/><line x1="3" y1="19" x2="21" y2="19"/></svg>
          </button>
          <button class="fmt-btn" :class="{ on: selVAlign === 'middle' }" title="垂直居中" @click="selVAlign = 'middle'; applyFmt()" v-show="!previewMode">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="7" x2="21" y2="7"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="17" x2="21" y2="17"/><line x1="3" y1="22" x2="21" y2="22"/></svg>
          </button>
          <button class="fmt-btn" :class="{ on: selVAlign === 'bottom' }" title="底端对齐" @click="selVAlign = 'bottom'; applyFmt()" v-show="!previewMode">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="10" x2="21" y2="10"/><line x1="3" y1="15" x2="21" y2="15"/><line x1="3" y1="19" x2="21" y2="19"/><line x1="3" y1="22" x2="21" y2="22"/></svg>
          </button>
          <span class="fmt-sep" v-show="!previewMode"></span>
          <button class="fmt-btn" :class="{ on: fmt.wordWrap }" title="自动换行" @click="fmt.wordWrap = !fmt.wordWrap; applyFmt()" v-show="!previewMode">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 6h10a4 4 0 0 1 0 8H4"/><polyline points="8 4 4 6 8 8"/><path d="M20 18H10a4 4 0 0 1 0-8h4"/><polyline points="16 16 20 18 16 20"/></svg>
          </button>
          <span class="fmt-sep" v-show="!previewMode"></span>
          <div class="fmt-color-pair" v-show="!previewMode">
            <button ref="fontClrRef" class="fmt-clr-btn" title="字体颜色" @click.stop="toggleFontClr">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><text x="12" y="10" text-anchor="middle" dominant-baseline="central" font-size="26" font-weight="bold" fill="currentColor" stroke="none">A</text><rect x="3" y="17" width="18" height="3" rx="1" :fill="fmt.color" stroke="none"/></svg>
            </button>
            <div class="fmt-clr-pop" v-if="fontClrOpen" :style="fontClrPos" @click.stop>
              <span v-for="c in colorPalette" :key="'fc'+c" class="fmt-clr-chip" :style="{ background: c }" :class="{ picked: fmt.color === c }" @click="fmt.color = c; fontClrOpen = false; applyFmt()"></span>
            </div>
          </div>
          <div class="fmt-color-pair" v-show="!previewMode">
            <button ref="bgClrRef" class="fmt-clr-btn" title="填充颜色" @click.stop="toggleBgClr">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M2 22 L2 20 L22 20 L22 22 Z" :fill="fmt.bgColor" stroke="none"/><path d="M2 22 L12 20 L22 22" fill="none"/><path d="M12 3 L16 12 L8 12 Z" :fill="fmt.bgColor" fill-opacity="0.3"/></svg>
            </button>
            <div class="fmt-clr-pop" v-if="bgClrOpen" :style="bgClrPos" @click.stop>
              <span v-for="c in colorPalette" :key="'bc'+c" class="fmt-clr-chip" :style="{ background: c }" :class="{ picked: fmt.bgColor === c }" @click="fmt.bgColor = c; bgClrOpen = false; applyFmt()"></span>
            </div>
          </div>
          <span class="fmt-sep" v-show="!previewMode"></span>
          <button class="fmt-btn fmt-btn-merge" title="合并单元格" @click="mergeSelection" v-show="!previewMode">合并</button>
          <button class="fmt-btn fmt-btn-merge" title="取消合并" @click="cancelMerge" v-show="!previewMode">拆分</button>
          <span class="fmt-sep" v-show="!previewMode"></span>
          <button class="fmt-btn fmt-btn-preview" @click="doPreview" v-if="!previewMode">加载预览</button>
          <button class="fmt-btn fmt-btn-preview" @click="saveAsTemplate" v-if="!previewMode">{{ currentTemplateId ? '更新模板' : '保存为模' }}</button>
          <button class="fmt-btn fmt-btn-preview" @click="openTemplateList" v-if="!previewMode" style="background:#6d28d9;">模板列表</button>
          <button class="fmt-btn fmt-btn-preview" @click="doExportFull" title="导出 Excel" v-if="!previewMode" style="background:#16a34a;">导出Excel</button>
        </div>

        <!-- 预览模式浮动退出按-->
        <Transition name="toast-fade">
          <div v-if="previewMode" class="preview-float-actions">
            <button class="preview-float-btn" @click="goPreviewPage(previewPage-1)" :disabled="previewPage <= 1">◀ 上页</button>
            <span class="preview-page-label">{{ previewPage }}/{{ previewTotalPages }}</span>
            <button class="preview-float-btn" @click="goPreviewPage(previewPage+1)" :disabled="previewPage >= previewTotalPages">下页 </button>
            <button class="preview-float-btn" @click="doPrint">打印</button>
            <button class="preview-float-btn preview-float-btn--primary" @click="doExportFull">导出完整报表</button>
            <button class="preview-float-btn preview-float-btn--exit" @click="exitPreview" v-if="!viewOnly">返回设计 (Esc)</button>
          </div>
        </Transition>

        <!-- Spreadsheet -->
        <div class="sr-sheet-wrap" @click.self="deselectAllCells" @dragover.prevent="onDragOver" @dragleave="onDragLeave" @drop.prevent="onCellDrop">
          <div class="sr-sheet-scroll" ref="sheetScrollRef" @scroll="onSheetScroll" @wheel="onSheetWheel" :style="{ zoom: zoomLevel }">
            <!-- 纸张分页虚线 -->
            <div class="page-break-overlay" :style="pageBreakStyle">
              <div v-for="pb in pageBreakLines.h" :key="'h'+pb" class="pb-line pb-h" :style="{ top: pb + 'px' }"></div>
              <div v-for="pb in pageBreakLines.v" :key="'v'+pb" class="pb-line pb-v" :style="{ left: pb + 'px' }"></div>
            </div>
            <!-- 选区覆盖层：VXE 风格，直接操DOM，避per-cell 重渲-->
            <div class="sel-overlay" v-if="selOverlay.style" :style="selOverlay.style"></div>
            <div class="drop-target-overlay" v-if="dropTargetStyle" :style="dropTargetStyle"></div>
            <table class="sr-sheet" :style="{ minWidth: totalSheetWidth() + 'px' }">
              <colgroup>
                <col :style="{ width: '92px' }"/>
                <col v-for="c in colCount" :style="{ width: getColWidth(c) + 'px' }" :key="c"/>
              </colgroup>
              <thead>
                <tr>
                  <th class="ss-corner"></th>
                  <th v-for="c in colCount" :key="c" v-memo="[c, isColSelected(c)]" class="ss-col-hdr" :class="{ 'sel-col': isColSelected(c) }"
                    @mousedown.prevent="onColHeaderDown(c, $event)"
                    @contextmenu.prevent="showCtxMenu('col', c, $event)">
                    {{ colLabel(c) }}
                    <span class="col-resize-grip" @mousedown.stop.prevent="startColResize(c, $event)"></span>
                  </th>
                </tr>
              </thead>
              <tbody>
                <!-- 虚拟滚动：上方占位 -->
                <tr v-if="beforeSpacerH > 0" style="height:0"><td :colspan="colCount + 1" style="padding:0;border:none;height:0">
                  <div :style="{ height: beforeSpacerH + 'px' }"></div>
                </td></tr>
                <tr v-for="r in visibleRows" :key="r" :style="rowStyle(r)">
                  <td class="ss-row-hdr" :class="{ 'sel-row': isRowSelected(r), 'loop-start': isLoopStartRow(r), 'page-hdr-start': isPageHeaderStartRow(r) }"
                  @mousedown.prevent="onRowHeaderDown(r, $event)"
                  @contextmenu.prevent="showCtxMenu('row', r, $event)">{{ r }}
                  <span class="loop-badge" v-if="isLoopStartRow(r)"></span>
                  <span class="row-resize-grip" @mousedown.stop.prevent="startRowResize(r, $event)"></span>
                </td>
                  <td v-for="c in colCount" :key="c"
                    v-memo="[r, c, isActive(r,c), isImageCell(r,c), cellDataMemo(r,c)]"
                    v-show="shouldRenderCell(r, c)"
                    class="ss-cell"
                    :data-r="r" :data-c="c"
                    :colspan="cellColSpan(r, c)"
                    :rowspan="cellRowSpan(r, c)"
                    :class="cellClass(r, c)"
                    :style="cellStyle(r, c)"
                    @mousedown="onCellMouseDown(r, c, $event)"
                    @dblclick="startEdit(r, c)"
                    @contextmenu.prevent="showCtxMenu('cell', { r, c }, $event)"
                  >
                    <input v-if="editing && editing.r === r && editing.c === c"
                      ref="cellInputRef"
                      v-model="editing.val"
                      class="ss-input"
                      @blur="commitEdit"
                      @keydown.enter.prevent="onEditEnter"
                      @keydown.escape.prevent="cancelEdit"
                      @keydown.tab.prevent="commitEdit; moveEdit(0, 1)"
                    />
                    <span v-else-if="isImageCell(r,c)" class="ss-img-wrap">
                      <img :src="getCellValue(r,c) === '${logo}' ? config.logoImage : getCellValue(r,c)" class="ss-img" />
                    </span>
                    <span v-else class="ss-text">{{ getCellValue(r, c) }}</span>
                  </td>
                </tr>
                <!-- 虚拟滚动：下方占位 -->
                <tr v-if="afterSpacerH > 0" style="height:0"><td :colspan="colCount + 1" style="padding:0;border:none;height:0">
                  <div :style="{ height: afterSpacerH + 'px' }"></div>
                </td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 右：属性面板 -->
      <div class="sr-right" v-if="!viewOnly">
        <div class="sr-right-tabs">
          <button class="srt on">属性</button>
        </div>

        <div class="sr-right-body">
          <div class="cfg-grp">
            <div class="cfg-ttl">表格设置</div>
            <div class="cfg-row"><label>行数</label><input class="prop-inp prop-inp-s" type="number" v-model.number="rowCount" min="1" max="200" @change="resizeSheet" /></div>
            <div class="cfg-row"><label>列数</label><input class="prop-inp prop-inp-s" type="number" v-model.number="colCount" min="1" max="50" @change="resizeSheet" /></div>
            <div class="cfg-row"><label>当前列宽</label><input class="prop-inp prop-inp-s" type="number" :value="colWidthBuf" min="1" max="1200" @input="onColWidthInput" @blur="applyColWidth" @keydown.enter="applyColWidth" /> px <span class="cfg-hint">{{ colLabel(activeCellC) }} </span></div>
            <div class="cfg-row"><label>当前行高</label><input class="prop-inp prop-inp-s" type="number" :value="rowHeightBuf" min="1" max="400" @input="onRowHeightInput" @blur="applyRowHeight" @keydown.enter="applyRowHeight" /> px <span class="cfg-hint">{{ activeCellR }} </span></div>
          </div>
          <div v-if="!selCell" class="sr-empty">选中单元格查看属</div>
          <div v-else class="sr-props">
            <div class="prop-row"><label>位置</label><span>{{ colLabel(selCell.c) }}{{ selCell.r }}</span></div>
            <div class="prop-row"><label></label><input class="prop-inp" :value="getCellValue(selCell.r, selCell.c)" @change="e => setCellValue(selCell.r, selCell.c, e.target.value)" /></div>
            <div class="prop-row"><label>字体</label>
              <select class="prop-sel" :value="getCellFmt(selCell.r, selCell.c, 'fontFamily') || fmt.fontFamily" @change="e => setCellFmt(selCell.r, selCell.c, 'fontFamily', e.target.value)">
                <option value="SimSun, serif">宋体</option>
                <option value="SimHei, sans-serif">黑体</option>
                <option value="Microsoft YaHei, sans-serif">微软雅黑</option>
                <option value="Arial, sans-serif">Arial</option>
              </select>
            </div>
            <div class="prop-row"><label>字号</label><input class="prop-inp prop-inp-s" type="number" :value="getCellFmt(selCell.r, selCell.c, 'fontSize') || fmt.fontSize" @change="e => setCellFmt(selCell.r, selCell.c, 'fontSize', +e.target.value)" /></div>
            <div class="prop-row prop-toggles">
              <button :class="{ on: getCellFmt(selCell.r, selCell.c, 'bold') }" @click="toggleCellFmt(selCell.r, selCell.c, 'bold')">B</button>
              <button :class="{ on: getCellFmt(selCell.r, selCell.c, 'italic') }" @click="toggleCellFmt(selCell.r, selCell.c, 'italic')"><i>I</i></button>
              <button :class="{ on: getCellFmt(selCell.r, selCell.c, 'underline') }" @click="toggleCellFmt(selCell.r, selCell.c, 'underline')"><u>U</u></button>
            </div>
            <div class="prop-row"><label>对齐</label>
              <select class="prop-sel" :value="getCellFmt(selCell.r, selCell.c, 'align') || 'left'" @change="e => setCellFmt(selCell.r, selCell.c, 'align', e.target.value)">
                <option value="left">左对</option>
                <option value="center">居中</option>
                <option value="right">右对</option>
              </select>
            </div>
            <div class="prop-row prop-toggles">
              <button :class="{ on: getCellFmt(selCell.r, selCell.c, 'wordWrap'), 'btn-wordwrap': true }" @click="toggleCellFmt(selCell.r, selCell.c, 'wordWrap')" title="自动换行">自动换行</button>
            </div>
            <div class="prop-row"><label>文字</label>
              <input class="prop-color" type="color" :value="getCellFmt(selCell.r, selCell.c, 'color') || '#333333'"
                @change="e => setCellFmt(selCell.r, selCell.c, 'color', e.target.value)" />
              <input class="prop-inp prop-inp-xs" :value="getCellFmt(selCell.r, selCell.c, 'color') || '#333333'"
                @change="e => setCellFmt(selCell.r, selCell.c, 'color', e.target.value)" />
            </div>
            <div class="prop-row"><label>背景</label>
              <input class="prop-color" type="color" :value="getCellFmt(selCell.r, selCell.c, 'bgColor') || '#ffffff'"
                @change="e => setCellFmt(selCell.r, selCell.c, 'bgColor', e.target.value)" />
              <input class="prop-inp prop-inp-xs" :value="getCellFmt(selCell.r, selCell.c, 'bgColor') || '#ffffff'"
                @change="e => setCellFmt(selCell.r, selCell.c, 'bgColor', e.target.value)" />
            </div>
            <div class="prop-row"><label>边框</label></div>
            <div class="prop-border-bar">
              <button class="bdr-btn" :class="{ on: (getCellFmt(selCell.r, selCell.c, 'border') || 'none') === 'none' }" title="无边框" @click="setCellFmt(selCell.r, selCell.c, 'border', 'none')">
                 <svg class="bdr-svg" viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" fill="none" stroke="#ccc" stroke-width=".8" stroke-dasharray="3,2"/></svg></button>
              <button class="bdr-btn" :class="{ on: (getCellFmt(selCell.r, selCell.c, 'border') || 'none') === 'all' }" title="全部边框" @click="setCellFmt(selCell.r, selCell.c, 'border', 'all')">
                <svg class="bdr-svg" viewBox="0 0 24 24"><path d="M3 3h18v18H3z" fill="none" stroke="#333" stroke-width="1.5"/><path d="M7 3v18M11 3v18M15 3v18M19 3v18M3 7h18M3 11h18M3 15h18M3 19h18" stroke="#999" stroke-width=".5"/></svg></button>
              <button class="bdr-btn" :class="{ on: (getCellFmt(selCell.r, selCell.c, 'border') || 'none') === 'outer' }" title="外边框" @click="setCellFmt(selCell.r, selCell.c, 'border', 'outer')">
                <svg class="bdr-svg" viewBox="0 0 24 24"><path d="M3 3h18v18H3z" fill="none" stroke="#333" stroke-width="1.5"/></svg></button>
              <button class="bdr-btn" :class="{ on: (getCellFmt(selCell.r, selCell.c, 'border') || 'none') === 'bottom' }" title="下边框" @click="setCellFmt(selCell.r, selCell.c, 'border', 'bottom')">
                <svg class="bdr-svg" viewBox="0 0 24 24"><path d="M3 21h18" stroke="#333" stroke-width="1.5"/></svg></button>
              <button class="bdr-btn" :class="{ on: (getCellFmt(selCell.r, selCell.c, 'border') || 'none') === 'top' }" title="上边框" @click="setCellFmt(selCell.r, selCell.c, 'border', 'top')">
                <svg class="bdr-svg" viewBox="0 0 24 24"><path d="M3 3h18" stroke="#333" stroke-width="1.5"/></svg></button>
              <button class="bdr-btn" :class="{ on: (getCellFmt(selCell.r, selCell.c, 'border') || 'none') === 'left' }" title="左边框" @click="setCellFmt(selCell.r, selCell.c, 'border', 'left')">
                <svg class="bdr-svg" viewBox="0 0 24 24"><path d="M3 3v18" stroke="#333" stroke-width="1.5"/></svg></button>
              <button class="bdr-btn" :class="{ on: (getCellFmt(selCell.r, selCell.c, 'border') || 'none') === 'right' }" title="右边框" @click="setCellFmt(selCell.r, selCell.c, 'border', 'right')">
                <svg class="bdr-svg" viewBox="0 0 24 24"><path d="M21 3v18" stroke="#333" stroke-width="1.5"/></svg></button>
            </div>
            <div class="prop-row"><label>线条</label>
              <select class="prop-sel" :value="getCellFmt(selCell.r, selCell.c, 'borderStyle') || 'solid'" @change="e => setCellFmt(selCell.r, selCell.c, 'borderStyle', e.target.value)">
                <option value="solid">实线</option>
                <option value="dashed">虚线</option>
                <option value="dotted">点线</option>
                <option value="double">双线</option>
              </select>
            </div>
            <div class="prop-row"><label>粗细</label>
              <select class="prop-sel" :value="getCellFmt(selCell.r, selCell.c, 'borderWidth') || 1" @change="e => setCellFmt(selCell.r, selCell.c, 'borderWidth', +e.target.value)">
                <option :value="1">细线</option>
                <option :value="2">正常</option>
                <option :value="3">中粗</option>
                <option :value="4">粗线</option>
              </select>
            </div>
            <div class="prop-row"><label>颜色</label>
              <input class="prop-color" type="color" :value="getCellFmt(selCell.r, selCell.c, 'borderColor') || '#333333'"
                @change="e => setCellFmt(selCell.r, selCell.c, 'borderColor', e.target.value)" />
            </div>
            <div class="prop-color-bar">
              <span v-for="c in borderPresetColors" :key="c" class="clr-swatch" :style="{ background: c }"
                :class="{ on: (getCellFmt(selCell.r, selCell.c, 'borderColor') || '#333333') === c }"
                @click="setCellFmt(selCell.r, selCell.c, 'borderColor', c)" title="c"></span>
            </div>
          </div>

          <!-- 报表配置（始终显示） -->
          <div class="cfg-grp" style="margin-top:18px;">
            <div class="cfg-ttl">基本信息</div>
            <div class="cfg-row"><label>报表名称</label><input class="prop-inp" v-model="config.title" /></div>
          </div>
          <div class="cfg-grp">
            <div class="cfg-ttl">纸张设置</div>
            <div class="cfg-row"><label>纸张</label>
              <select class="prop-sel" v-model="paperSize">
                <option v-for="p in paperSizes" :key="p.value" :value="p.value">{{ p.label }}</option>
              </select>
            </div>
            <div class="cfg-row"><label>方向</label>
              <select class="prop-sel" v-model="paperOrient">
                <option value="portrait">纵向</option>
                <option value="landscape">横向</option>
              </select>
            </div>
            <div class="cfg-row hint-row" v-if="pageBreakLines">
              <span>每页{{ colsPerPage }} &times; {{ rowsPerPage }} </span>
            </div>
          </div>
          <div class="cfg-grp">
            <div class="cfg-ttl">全局边框</div>
            <div class="cfg-row">
              <label><input type="checkbox" v-model="config.showBorder" style="margin-right:6px;" />显示边框</label>
            </div>
            <div class="cfg-row" v-if="config.showBorder"><label>粗细</label>
              <select class="prop-sel" v-model.number="config.borderWidth">
                <option :value="1">细线</option>
                <option :value="2">正常</option>
                <option :value="3">中粗</option>
                <option :value="4">粗线</option>
              </select>
            </div>
            <div class="cfg-row" v-if="config.showBorder"><label>颜色</label>
              <input class="prop-color" type="color" v-model="config.borderColor" />
            </div>
            <div class="cfg-row">
              <button class="btn btn-sm" style="color:#e74c3c" @click="clearAllBorders">清除全部边框</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 导入数据集弹-->
    <div class="modal-mask" v-if="showImportModal" @click.self="showImportModal = false">
      <div class="modal-card">
        <div class="modal-head">
          <span>导入数据</span>
          <button class="modal-close" @click="showImportModal = false">&times;</button>
        </div>
        <div class="modal-tabs">
          <button :class="{ on: importTab === 'csv' }" @click="importTab = 'csv'">CSV 文件</button>
          <button :class="{ on: importTab === 'sql' }" @click="importTab = 'sql'">SQL 查询</button>
          <button :class="{ on: importTab === 'api' }" @click="importTab = 'api'">API 接口</button>
        </div>
        <div class="modal-body">
          <!-- CSV 上传 -->
          <div v-if="importTab === 'csv'">
            <div class="modal-row"><label>数据集名</label><input class="prop-inp" v-model="importName" placeholder="输入名称" /></div>
            <div class="modal-row">
              <label>CSV 文件</label>
              <input type="file" accept=".csv" @change="onCsvFileChange" ref="csvFileRef" />
            </div>
            <div class="modal-hint">支持 UTF-8 编码CSV 文件，第一行为表头</div>
          </div>
          <!-- SQL 查询 -->
          <div v-if="importTab === 'sql'">
            <div class="modal-row"><label>数据集名</label><input class="prop-inp" v-model="importName" placeholder="输入名称" /></div>
            <div class="modal-row">
              <label>数据</label>
              <select class="prop-sel" v-model="importDb">
                <option value="default">默认数据</option>
                <option value="mysql">MySQL</option>
                <option value="pgsql">PostgreSQL</option>
                <option value="oracle">Oracle</option>
              </select>
            </div>
            <div class="modal-row flex-col">
              <label>SQL 语句</label>
              <textarea class="prop-inp sql-textarea" v-model="importSql" placeholder="SELECT * FROM table_name LIMIT 1000" rows="6"></textarea>
            </div>
            <div class="modal-hint">输入 SELECT 查询语句，执行后将字段自动填</div>
          </div>
          <!-- API 接口 -->
          <div v-if="importTab === 'api'">
            <div class="modal-row"><label>数据集名</label><input class="prop-inp" v-model="importName" placeholder="输入名称" /></div>
            <div class="modal-row"><label>接口 URL</label><input class="prop-inp" v-model="importApiUrl" placeholder="/api/data/list" /></div>
            <div class="modal-hint">GET 请求，返JSON 数组，自动解析字</div>
          </div>
        </div>
        <div class="modal-foot">
          <button class="fmt-btn" @click="showImportModal = false">取消</button>
          <button class="fmt-btn fmt-btn-preview" @click="doImport" :disabled="importing">确定导入</button>
        </div>
      </div>
    </div>

    <!-- 草稿箱弹窗 -->
    <div class="modal-mask" v-if="showDraftModal" @click.self="showDraftModal = false">
      <div class="modal-card">
        <div class="modal-head">
          <span>草稿箱</span>
          <button class="modal-close" @click="showDraftModal = false">&times;</button>
        </div>
        <div class="modal-body" style="max-height: 50vh; overflow-y: auto; padding: 16px 0;">
          <div v-if="!drafts.length" style="text-align:center;padding:96px 0;color:#94a3b8;">暂无草稿</div>
          <div v-for="d in drafts" :key="d.id" class="draft-item" @click="loadDraft(d)">
            <div class="draft-info">
              <div class="draft-title">{{ d.title }}</div>
              <div class="draft-meta">{{ d.updatedAt || d.createdAt }}</div>
            </div>
            <button class="draft-del" @click="deleteDraft(d.id, $event)">&times;</button>
          </div>
        </div>
        <div class="modal-foot">
          <button class="fmt-btn fmt-btn-preview" @click="onToolbarNew(); showDraftModal = false">新建</button>
        </div>
      </div>
    </div>

    <!-- 模板列表弹窗 -->
    <div class="modal-mask" v-if="showTemplateModal" @click.self="showTemplateModal = false">
      <div class="modal-card" style="max-width: 1680px;">
        <div class="modal-head">
          <span>报表模板列表</span>
          <button class="modal-close" @click="showTemplateModal = false">&times;</button>
        </div>
        <div style="padding:16px 56px 0;display:flex;gap:16px;align-items:center;">
          <label style="font-size:24px;color:#555;white-space:nowrap;">类型筛选：</label>
          <select v-model="templateTypeFilter" @change="loadTemplatesFromServer" style="height:60px;font-size:24px;border-radius:10px;border:2px solid #dde1e8;padding:0 16px;background:#fff;outline:none;font-family:inherit;">
            <option value="">全部</option>
            <option value="samples">样品资料</option>
            <option value="client-items">客户择样</option>
          </select>
        </div>
        <div class="modal-body" style="max-height: 50vh; overflow-y: auto; padding: 16px 0;">
          <div v-if="templateLoading" style="text-align:center;padding:80px 0;color:#94a3b8;">加载中...</div>
          <div v-else-if="!templateList.length" class="sr-empty" style="padding: 96px 0;">暂无模板，请先保存模板</div>
          <div v-for="tpl in templateList" :key="tpl.id" class="draft-item" @click="loadTemplateClick(tpl)">
            <div class="draft-info">
              <div class="draft-title">
                <span v-if="isTemplateLocked(tpl.id)" title="已锁定" style="color:#f59e0b;margin-right:12px;">&#128274;</span>
                {{ tpl.title }}
                <span v-if="tpl.type" style="display:inline-block;margin-left:12px;padding:4px 16px;border-radius:6px;font-size:20px;font-weight:500;" :style="tpl.type === 'samples' ? 'background:#dbeafe;color:#1d4ed8;' : 'background:#fef3c7;color:#b45309;'">
                  {{ tpl.type === 'samples' ? '样品资料' : '客户择样' }}
                </span>
              </div>
              <div class="draft-meta">{{ tpl.description || '' }} | {{ (tpl.updateTime || tpl.createTime) || '' }} | 操作人: {{ tpl.updateByName || tpl.updateBy || '-' }}</div>
            </div>
            <button class="draft-action" @click="toggleTemplateLock(tpl, $event)" :title="isTemplateLocked(tpl.id) ? '解锁' : '上锁'">
              {{ isTemplateLocked(tpl.id) ? '&#128275;' : '&#128274;' }}
            </button>
            <button class="draft-del" @click="deleteTemplateClick(tpl.id, $event)">&times;</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 密码验证弹窗（自定义） -->
    <div class="modal-mask" v-if="lockDialog.show" @click.self="lockDialog.show = false">
      <div class="modal-card" style="max-width: 1120px;">
        <div class="modal-head">
          <span>{{ lockDialog.title }}</span>
          <button class="modal-close" @click="lockDialog.show = false">&times;</button>
        </div>
        <div class="modal-body">
          <p style="margin-bottom:36px;color:#555;font-size:30px;line-height:1.6;">{{ lockDialog.hint }}</p>
          <input class="sr-input" v-model="lockDialog.password" type="password" placeholder="请输入密码"
            @keyup.enter="lockDialogConfirm" style="width:100%;box-sizing:border-box;height:84px;font-size:30px;border-radius:16px;" />
          <p v-if="lockDialog.error" style="color:#ef4444;font-size:24px;margin-top:20px;">{{ lockDialog.error }}</p>
          <div style="margin-top:20px;display:flex;gap:10px;justify-content:flex-end;">
            <button class="fmt-btn" @click="lockDialog.show = false">取消</button>
            <button class="fmt-btn fmt-btn-preview" @click="lockDialogConfirm">确定</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 模板命名弹窗 -->
    <div class="modal-mask" v-if="nameDialog.show" @click.self="nameDialog.show = false">
      <div class="modal-card" style="max-width: 1120px;">
        <div class="modal-head">
          <span>保存模板</span>
          <button class="modal-close" @click="nameDialog.show = false">&times;</button>
        </div>
        <div class="modal-body">
          <p style="margin-bottom:36px;color:#555;font-size:30px;line-height:1.6;">请输入模板名称</p>
          <input class="sr-input" v-model="nameDialog.name" type="text" placeholder="模板名称"
            @keyup.enter="nameDialogConfirm" style="width:100%;box-sizing:border-box;height:84px;font-size:30px;border-radius:16px;" />
          <div style="margin-top:24px;">
            <label style="font-size:24px;color:#555;display:block;margin-bottom:12px;">模板类型</label>
            <select v-model="nameDialog.type" style="width:100%;height:84px;font-size:30px;border-radius:16px;border:2px solid #dde1e8;padding:0 16px;background:#fff;outline:none;font-family:inherit;">
              <option value="">不分类</option>
              <option value="samples">样品资料</option>
              <option value="client-items">客户择样</option>
            </select>
          </div>
          <p v-if="nameDialog.error" style="color:#ef4444;font-size:24px;margin-top:20px;">{{ nameDialog.error }}</p>
          <div style="margin-top:20px;display:flex;gap:10px;justify-content:flex-end;">
            <button class="fmt-btn" @click="nameDialog.show = false">取消</button>
            <button class="fmt-btn fmt-btn-preview" @click="nameDialogConfirm">保存</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右键菜单 (仅正常模 -->
    <div v-if="ctxMenu.show && !viewOnly" class="ctx-menu" :style="{ left: ctxMenu.x + 'px', top: ctxMenu.y + 'px' }" @click.stop>
      <div class="ctx-item" @click="setLoopStartRow()">设为循环起始</div>
      <div class="ctx-item" v-if="dataLoopStartRow" @click="dataLoopStartRow = 0">取消循环标记</div>
      <div class="ctx-sep"></div>
      <div class="ctx-item" @click="setPageHeaderStartRow()">设为每页表头起始</div>
      <div class="ctx-item" v-if="pageHeaderStartRow" @click="pageHeaderStartRow = 0">取消表头标记</div>
      <div class="ctx-sep"></div>
      <div class="ctx-item" @click="ctxInsertRow('above')">上方插入</div>
      <div class="ctx-item" @click="ctxInsertRow('below')">下方插入</div>
      <div class="ctx-item" @click="ctxDeleteRow()">删除当前</div>
      <div class="ctx-sep"></div>
      <div class="ctx-item" @click="ctxInsertCol('left')">左侧插入</div>
      <div class="ctx-item" @click="ctxInsertCol('right')">右侧插入</div>
      <div class="ctx-item" @click="ctxDeleteCol()">删除当前</div>
    </div>

    <!-- 通用确认弹窗 -->
    <div class="modal-mask" v-if="confirmDialog.show" @click.self="confirmDialog.show = false">
      <div class="modal-card" style="max-width: 1120px;">
        <div class="modal-head">
          <span>提示</span>
          <button class="modal-close" @click="confirmDialog.show = false">&times;</button>
        </div>
        <div class="modal-body">
          <p style="margin-bottom:56px;color:#374151;font-size:30px;line-height:1.7;">{{ confirmDialog.message }}</p>
          <div style="display:flex;gap:10px;justify-content:flex-end;">
            <button class="fmt-btn" @click="confirmDialog.show = false">{{ confirmDialog.cancelText }}</button>
            <button class="fmt-btn fmt-btn-preview" :style="confirmDialog.danger ? { background: '#ef4444', borderColor: '#ef4444' } : {}" @click="confirmDialog.callback?.(); confirmDialog.show = false">{{ confirmDialog.confirmText }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <Transition name="toast-fade">
      <div v-if="toast.show" class="sr-toast" :class="toast.type">{{ toast.message }}</div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import ExcelJS from 'exceljs'
import DesignerToolbar from '@/components/designer/DesignerToolbar.vue'

// ===== 配置 =====
const route = useRoute()
const { state: authState } = useAuth()
const viewOnly = computed(() => route.query.viewOnly === '1')
const toast = reactive({ show: false, message: '', type: 'info' })
let toastTimer = null
let autoSaveTimer = null

// 静默保存（不弹出 toast）
function silentSave() {
  if (previewMode.value) return
  forceSaveDraft()
}
function showToast(msg, type = 'info', duration = 2500) {
  clearTimeout(toastTimer)
  toast.message = msg
  toast.type = type
  toast.show = true
  if (duration > 0) toastTimer = setTimeout(() => { toast.show = false }, duration)
}

const config = reactive({
  title: '',
  fontSize: 24,
  fontColor: '#333333',
  bgColor: '#ffffff',
  fontFamily: 'SimSun, serif',
  bold: false,
  alignCenter: false,
  wordWrap: false,
  showBorder: true,
  borderWidth: 1,
  borderColor: '#333333',
  logoImage: '',
})

const printConfig = reactive({
  companyName: '',
  reportTitle: '',
})

const rightTab = ref('props')
// 电子表格尺寸 (响应
const defaultColWidth = ref(180)
const defaultRowHeight = ref(48)
const colWidths = ref({})  // { c: px }
const rowHeights = ref({}) // { r: px }
function getColWidth(c) { return colWidths.value[c] || defaultColWidth.value }
function getRowHeight(r) { return rowHeights.value[r] || defaultRowHeight.value }
function rowStyle(r) {
  const rh = getRowHeight(r) + 'px'
  return { height: rh }
}
let rowWordWrapCache = {}

function refreshRowWordWrap(r) {
  for (let c = 1; c <= colCount.value; c++) {
    if (!shouldRenderCell(r, c)) continue
    const mi = getMergeInfo(r, c)
    const rr = mi ? mi.sR : r; const cc = mi ? mi.sC : c
    if (cellData[`R${rr}C${cc}`]?.fmt?.wordWrap) {
      rowWordWrapCache[r] = true
      return
    }
  }
  delete rowWordWrapCache[r]
}

function rebuildRowWordWrapCache() {
  rowWordWrapCache = {}
  for (let r = 1; r <= rowCount.value; r++) {
    refreshRowWordWrap(r)
  }
}
function setColWidth(c, w) { colWidths.value[c] = Math.max(1, Math.min(1200, w)) }

function colOffset(n) {
  return colPositions.value[n - 1] + 92
}
function totalSheetWidth() {
  return 92 + (colPositions.value.length > 0 ? colPositions.value[colPositions.value.length - 1] : 0)
}
function totalSheetHeight() {
  return 48 + (rowPositions.value.length > 0 ? rowPositions.value[rowPositions.value.length - 1] : 0)
}

// ===== 虚拟行滚动 =====
const VIRTUAL_ROW_BUFFER = 8 // 视口上下各缓冲行数
const theadHeight = ref(60) // 列头高度，首次渲染后自动测量

// 累计位置缓存（computed 自动响应 rowHeights/colWidths 变化）
const colPositions = computed(() => {
  const pos = [0] // pos[c] = 前 c 列的累计宽度
  for (let c = 1; c <= colCount.value; c++) pos.push(pos[c - 1] + getColWidth(c))
  return pos
})
const rowPositions = computed(() => {
  const pos = [0] // pos[r] = 前 r 行的累计高度（数据行，不含表头）
  for (let r = 1; r <= rowCount.value; r++) pos.push(pos[r - 1] + getRowHeight(r))
  return pos
})

// 虚拟滚动状态
const virtualState = reactive({ scrollTop: 0, viewH: 0 })
let _overlayScrollRaf = null
function onSheetScroll() {
  // 拖拽操作中跳过，避免重渲染干扰选区/拖拽
  if (colResizeDrag.active || rowResizeDrag.active || cellDrag.active || headerDrag.active) return
  const el = sheetScrollRef?.value
  if (!el) return
  virtualState.scrollTop = el.scrollTop / zoomLevel.value
  virtualState.viewH = el.clientHeight / zoomLevel.value
  // 滚动时更新选区覆盖层位置（RAF 节流）
  if (selCell.value && !_overlayScrollRaf) {
    _overlayScrollRaf = requestAnimationFrame(() => {
      _overlayScrollRaf = null
      updateSelOverlay()
    })
  }
}

function binarySearchLE(arr, target) {
  let lo = 0, hi = arr.length - 1
  while (lo <= hi) {
    const mid = (lo + hi) >>> 1
    if (arr[mid] <= target) lo = mid + 1
    else hi = mid - 1
  }
  return hi
}

function findRowAtY(y) {
  // y: 内容区Y坐标（不含表头）
  const idx = binarySearchLE(rowPositions.value, y)
  return Math.max(1, Math.min(rowCount.value, idx + 1))
}
function findColAtX(x) {
  const idx = binarySearchLE(colPositions.value, x)
  return Math.max(1, Math.min(colCount.value, idx + 1))
}

// 可视行范围（含合并单元格展开）
const virtualRowRange = computed(() => {
  const top = Math.max(0, virtualState.scrollTop - theadHeight.value)
  const bottom = top + virtualState.viewH
  const pos = rowPositions.value
  const rc = rowCount.value
  if (!rc || pos.length < 2) return { start: 1, end: 1 }
  let start = Math.max(1, findRowAtY(top) - VIRTUAL_ROW_BUFFER)
  let end = Math.min(rc, findRowAtY(bottom) + VIRTUAL_ROW_BUFFER)
  // 合并单元格展开：如果合并区域跨越 start，把 start 拉到合并起始行
  const expandStart = () => {
    for (let c = 1; c <= colCount.value; c++) {
      const mi = getMergeInfo(start, c)
      if (mi && !mi.isStart && mi.sR < start) {
        start = mi.sR
        return true
      }
    }
    return false
  }
  let expanded = true
  while (expanded && start > 1) { expanded = expandStart() }
  return { start, end }
})

const visibleRows = computed(() => {
  const { start, end } = virtualRowRange.value
  const rows = []
  for (let r = start; r <= end; r++) rows.push(r)
  return rows
})

const beforeSpacerH = computed(() => {
  const r = virtualRowRange.value.start
  return r > 1 ? rowPositions.value[r - 1] : 0
})
const afterSpacerH = computed(() => {
  const r = virtualRowRange.value.end
  const total = rowPositions.value[rowPositions.value.length - 1] || 0
  return r < rowCount.value ? total - rowPositions.value[r] : 0
})
function avgColWidth() {
  if (colCount.value === 0) return defaultColWidth.value
  let sum = 0; for (let i = 1; i <= colCount.value; i++) sum += getColWidth(i)
  return sum / colCount.value || defaultColWidth.value
}
function avgRowHeight() {
  if (rowCount.value === 0) return defaultRowHeight.value
  let sum = 0; for (let i = 1; i <= rowCount.value; i++) sum += getRowHeight(i)
  return sum / rowCount.value || defaultRowHeight.value
}
function setRowHeight(r, h) { rowHeights.value[r] = Math.max(1, Math.min(400, h)) }
function clearColWidths() { colWidths.value = {} }
function clearRowHeights() { rowHeights.value = {} }
const rowCount = ref(40)
const colCount = ref(12)

// ===== 预览模式 =====
const previewMode = ref(false)
const previewSnap = ref(null)  // { cellData, rowCount, mergedCells }
const previewRowCount = ref(0)
const previewPage = ref(1)
const previewPageSize = ref(6) // 兜底值，实际由 computePagePlan 动态计算覆盖
const previewTotal = ref(0)
const previewTemplate = ref(null)
const previewLoopInfo = ref(null)
const pagePlan = ref([]) // 动态分页计划: [{start,end,isFirst,isLast}, ...]
const previewTotalPages = computed(() => Math.max(1, pagePlan.value.length || Math.ceil(previewTotal.value / previewPageSize.value)))
function guardEdit() {
  if (viewOnly.value) return true
  if (previewMode.value) { showToast('预览模式下不可编辑，请点击返回设计"', 'warn'); return true }
  return false
}

// 循环起始行：该行及之后含${}的行会循环填充；之前的行用第一条数据填充一次
const dataLoopStartRow = ref(0)
// 每页表头起始行：该行之后（不含）到循环起始行之间的行每页重复，该行之前的行只在首页显示
const pageHeaderStartRow = ref(0)

const DPI = 3.78

const paperSizes = [
  { value: 'a0', label: 'A0 (841×1189mm)' },
  { value: 'a1', label: 'A1 (594×841mm)' },
  { value: 'a2', label: 'A2 (420×594mm)' },
  { value: 'a3', label: 'A3 (297×420mm)' },
  { value: 'a4', label: 'A4 (210×297mm)' },
  { value: 'letter', label: 'Letter (216×279mm)' },
  { value: 'legal', label: 'Legal (216×356mm)' },
  { value: 'bt58', label: '蓝牙标签(小) 58mm' },
  { value: 'bt80', label: '蓝牙标签(大) 80mm' },
]

const paperDims = {
  a0: { w: 841, h: 1189 },
  a1: { w: 594, h: 841 },
  a2: { w: 420, h: 594 },
  a3: { w: 297, h: 420 },
  a4: { w: 210, h: 297 },
  letter: { w: 215.9, h: 279.4 },
  legal: { w: 215.9, h: 355.6 },
  bt58: { w: 58, h: 9999 },
   bt80: { w: 80, h: 9999 },
}

const paperSize = ref('a4')
// 边框预设颜色
const borderPresetColors = [
  '#333333', '#000000', '#666666', '#999999', '#cccccc',
  '#e53333', '#e36c09', '#dba400', '#40a040', '#2f6ef2',
  '#1a47b0', '#7030a0', '#ffffff',
]
const paperOrient = ref('portrait')
const colsPerPage = computed(() => {
  const dim = paperDims[paperSize.value]
  const pw = paperOrient.value === 'landscape' ? dim.h : dim.w
  return Math.max(1, Math.floor((pw * DPI) / avgColWidth()))
})

const rowsPerPage = computed(() => {
  const dim = paperDims[paperSize.value]
  const ph = paperOrient.value === 'landscape' ? dim.w : dim.h
  return Math.max(1, Math.floor((ph * DPI) / avgRowHeight()))
})

const pageBreakLines = computed(() => {
  const h = [], v = []
  const cpp = colsPerPage.value
  const rpp = rowsPerPage.value
  for (let i = 1; cpp * i < colCount.value; i++) v.push(colOffset(cpp * i + 1))
  let rowY = 48
  for (let i = 1; i <= rowCount.value; i++) {
    if (i > 1 && (i - 1) % rpp === 0) h.push(rowY)
    rowY += getRowHeight(i)
  }
  return { h, v }
})

const pageBreakStyle = computed(() => {
  return {
    width: totalSheetWidth() + 'px',
    height: totalSheetHeight() + 'px',
  }
})
const zoomLevel = ref(1)

// 右键菜单
const ctxMenu = reactive({ show: false, x: 0, y: 0, type: '', idx: 0, cell: null })
function showCtxMenu(type, data, e) {
  ctxMenu.type = type
  if (type === 'cell') {
    ctxMenu.cell = data
    selectCell(data.r, data.c)
  } else {
    ctxMenu.idx = data
    ctxMenu.cell = null
  }
  // 将菜单定位在鼠标附近，但不超出视
  const pad = 4
  ctxMenu.x = Math.min(e.clientX, window.innerWidth - 180) + pad
  ctxMenu.y = Math.min(e.clientY, window.innerHeight - 230) + pad
  ctxMenu.show = true
  nextTick(() => {
    document.addEventListener('click', hideCtxMenu, { once: true })
  })
}
function hideCtxMenu() { ctxMenu.show = false }

const zoomIn = () => { zoomLevel.value = Math.min(3, +(zoomLevel.value + 0.1).toFixed(1)) }
const zoomOut = () => { zoomLevel.value = Math.max(0.25, +(zoomLevel.value - 0.1).toFixed(1)) }

function onSheetWheel(e) {
  if (!e.ctrlKey) return
  e.preventDefault()
  const delta = -Math.sign(e.deltaY) * 0.1
  zoomLevel.value = Math.max(0.25, Math.min(3, +(zoomLevel.value + delta).toFixed(1)))
}

// 单元格数据
const cellData = reactive({})

// 选择
const selection = reactive({ sR: 0, sC: 0, eR: 0, eC: 0 })
const selCell = ref(null)

// VXE 风格选区覆盖层：用绝对定位 div 代替 per-cell class 绑定，避免选中区域重渲染
const selOverlay = reactive({ style: null })
const dropTargetCell = ref(null) // 拖拽悬停目标格子 { r, c }，用于高亮指示
function updateSelOverlay() {
  if (!selCell.value) {
    selOverlay.style = null
    return
  }
  const scrollEl = sheetScrollRef?.value
  if (!scrollEl) { selOverlay.style = null; return }

  const minR = Math.min(selection.sR, selection.eR)
  const maxR = Math.max(selection.sR, selection.eR)
  const minC = Math.min(selection.sC, selection.eC)
  const maxC = Math.max(selection.sC, selection.eC)

  const cp = colPositions.value, rp = rowPositions.value

  // 宽高始终用内容坐标计算（基于 colWidths/rowHeights，精确且不受合并子格隐藏影响）
  const width = cp[maxC] - cp[minC - 1]
  const height = rp[maxR] - rp[minR - 1]

  // 位置：统一用 getBoundingClientRect 测量 + 内容坐标宽度，两者独立
  // 对于 border-collapse:collapse 的 <td>，offsetLeft 在不同内容下可能因边框塌陷机制产生偏差
  // 改用首格 rect 定左上角，内容坐标(rp/cp) 定宽高，两者互不干扰
  const firstCell = scrollEl.querySelector(`[data-r="${minR}"][data-c="${minC}"]`)

  let left, top
  if (firstCell && firstCell.offsetHeight > 0) {
    // rect 方案：不受 border-collapse 内容相关偏移影响
    const tr = firstCell.getBoundingClientRect()
    const sr = scrollEl.getBoundingClientRect()
    const z = zoomLevel.value
    left = (tr.left - sr.left) / z + scrollEl.scrollLeft
    top = (tr.top - sr.top) / z + scrollEl.scrollTop
    // 诊断日志：对比 offset 和 rect
    console.log(`[sel] R${minR}C${minC} offset=(${firstCell.offsetLeft},${firstCell.offsetTop}) rect=(${left.toFixed(1)},${top.toFixed(1)}) z=${z} scroll=(${scrollEl.scrollLeft},${scrollEl.scrollTop}) size=(${width},${height})`)
  } else {
    left = 2 + 92 + cp[minC - 1]
    top = 2 + theadHeight.value + rp[minR - 1]
    console.log(`[sel] R${minR}C${minC} FALLBACK pos=(${left},${top})`)
  }

  selOverlay.style = {
    position: 'absolute',
    left: left + 'px',
    top: top + 'px',
    width: width + 'px',
    height: height + 'px',
    pointerEvents: 'none', zIndex: 5,
    border: '2px solid #2f6ef2',
    background: 'rgba(47, 110, 242, 0.06)',
  }
}

// 选区 / 列宽 / 行高变化时刷新覆盖层
watch(
  [() => selection.sR, () => selection.sC, () => selection.eR, () => selection.eC, selCell, colWidths, rowHeights],
  () => nextTick(updateSelOverlay)
)

const activeCellR = computed(() => selCell.value?.r || 1)
const activeCellC = computed(() => selCell.value?.c || 1)

// 列宽/行高输入缓冲（避免实时截断干扰输入）
const colWidthBuf = ref(getColWidth(1))
const rowHeightBuf = ref(getRowHeight(1))
function onColWidthInput(e) { colWidthBuf.value = e.target.value }
function onRowHeightInput(e) { rowHeightBuf.value = e.target.value }
function applyColWidth() {
  let v = parseInt(colWidthBuf.value)
  if (isNaN(v) || v < 1) v = defaultColWidth.value
  if (v > 1200) v = 1200
  colWidthBuf.value = v
  const minC = Math.min(selection.sC, selection.eC)
  const maxC = Math.max(selection.sC, selection.eC)
  for (let c = minC; c <= maxC; c++) setColWidth(c, v)
}
function applyRowHeight() {
  let v = parseInt(rowHeightBuf.value)
  if (isNaN(v) || v < 1) v = defaultRowHeight.value
  if (v > 400) v = 400
  rowHeightBuf.value = v
  const minR = Math.min(selection.sR, selection.eR)
  const maxR = Math.max(selection.sR, selection.eR)
  for (let r = minR; r <= maxR; r++) setRowHeight(r, v)
}
// 选中单元格变化时同步缓冲
watch([activeCellR, activeCellC], () => {
  colWidthBuf.value = getColWidth(activeCellC.value)
  rowHeightBuf.value = getRowHeight(activeCellR.value)
})

function deselectAllCells() {
  selCell.value = null
  selection.sR = selection.sC = selection.eR = selection.eC = 0
  if (_overlayScrollRaf) { cancelAnimationFrame(_overlayScrollRaf); _overlayScrollRaf = null }
}

function selectCell(r, c) {
  const mi = getMergeInfo(r, c)
  if (mi) {
    r = mi.sR; c = mi.sC
    selection.sR = mi.sR; selection.eR = mi.eR
    selection.sC = mi.sC; selection.eC = mi.eC
  } else {
    selection.sR = selection.eR = r
    selection.sC = selection.eC = c
  }
  selCell.value = { r, c }
}

function isSelected(r, c) {
  if (!selCell.value) return false
  const { sR, sC, eR, eC } = selection
  const minR = Math.min(sR, eR), maxR = Math.max(sR, eR)
  const minC = Math.min(sC, eC), maxC = Math.max(sC, eC)
  return r >= minR && r <= maxR && c >= minC && c <= maxC
}

function isActive(r, c) {
  return selCell.value && selCell.value.r === r && selCell.value.c === c
}

// v-memo 依赖：返回一个稳定值，仅在单元格数量/格式/合并变化时刷新
function cellDataMemo(r, c) {
  const mi = getMergeInfo(r, c)
  const rr = mi ? mi.sR : r, cc = mi ? mi.sC : c
  const d = cellData[`R${rr}C${cc}`]
  // 返回 cellData 的浅引用 + 合并信息，Vue 通过 === 判断是否需要重渲染
  return [
    d?.v ?? '',
    d?.fmt ? Object.values(d.fmt).join(',') : '',
    mi ? `${mi.sR},${mi.sC},${mi.eR},${mi.eC},${mi.isStart}` : '',
    getColWidth(c),
    getRowHeight(r),
  ].join('|')
}

function isColSelected(c) {
  if (!selCell.value) return false
  const minC = Math.min(selection.sC, selection.eC)
  const maxC = Math.max(selection.sC, selection.eC)
  return c >= minC && c <= maxC
}

function isRowSelected(r) {
  if (!selCell.value) return false
  const minR = Math.min(selection.sR, selection.eR)
  const maxR = Math.max(selection.sR, selection.eR)
  return r >= minR && r <= maxR
}

function colLabel(n) {
  let s = ''
  while (n > 0) {
    n--
    s = String.fromCharCode(65 + (n % 26)) + s
    n = Math.floor(n / 26)
  }
  return s
}

function cellClass(r, c) {
  const mi = getMergeInfo(r, c)
  const rr = mi ? mi.sR : r; const cc = mi ? mi.sC : c
  const f = cellData[`R${rr}C${cc}`]?.fmt
  const v = getCellValue(r, c)
  const va = f?.verticalAlign
  return {
    'active-cell': isActive(r, c),
    'ss-wrap': !!(f?.wordWrap),
    'ss-multiline': !!(v && v.includes('\n')),
    'ss-vmiddle': va === 'middle',
    'ss-vbottom': va === 'bottom',
    // flex 子项水平对齐（始终跟align 设置，对 .ss-text block 无影响，
    // .ss-img-wrap flex 容器确保水平居中生效
    'ss-flex-left': f?.align !== 'center' && f?.align !== 'right',
    'ss-flex-center': f?.align === 'center',
    'ss-flex-right': f?.align === 'right',
  }
}

function cellStyle(r, c) {
  let key = `R${r}C${c}`
  let d = cellData[key]
  const mi = getMergeInfo(r, c)
  if ((!d || !d.fmt) && mi && !mi.isStart) {
    key = `R${mi.sR}C${mi.sC}`
    d = cellData[key]
  }
  const st = {}
  const isMerge = mi && mi.isStart && (mi.eR > mi.sR || mi.eC > mi.sC)
  // 合并单元格：计算跨行/跨列的总尺寸，用 border-box 确保边框不溢出被裁切
  let totalHeight, totalWidth
  if (isMerge) {
    totalHeight = 0
    for (let rr = mi.sR; rr <= mi.eR; rr++) totalHeight += getRowHeight(rr)
    totalWidth = 0
    for (let cc = mi.sC; cc <= mi.eC; cc++) totalWidth += getColWidth(cc)
    st.height = totalHeight + 'px'
    st.width = totalWidth + 'px'
    st.boxSizing = 'border-box'
    st.overflow = 'visible'
  } else {
    totalHeight = getRowHeight(r)
    totalWidth = getColWidth(c)
    st.height = totalHeight + 'px'
    st.maxWidth = totalWidth + 'px'
    st.overflow = 'hidden'
  }
  if (!d || !d.fmt) { st.borderStyle = 'none'; st.borderWidth = '0'; st.outline = 'none'; return st }
  const f = d.fmt
  if (f.bold) st.fontWeight = 'bold'
  if (f.italic) st.fontStyle = 'italic'
  if (f.underline) st.textDecoration = 'underline'
  if (f.color) st.color = f.color
  if (f.bgColor) st.backgroundColor = f.bgColor
  if (f.fontSize) st.fontSize = f.fontSize + 'pt'
  if (f.fontFamily) st.fontFamily = f.fontFamily
  if (f.align) st.textAlign = f.align
  // 垂直对齐CSS class (.ss-vmiddle / .ss-vbottom) 处理，此处无需额外设置
  // 自动换行覆盖高度限制
  if (f.wordWrap) { st.whiteSpace = 'normal'; st.wordBreak = 'break-all'; st.overflowWrap = 'break-word'; st.overflow = 'hidden'; st.maxHeight = totalHeight + 'px' }
  // 边框：仅当单元格显式设置时才渲染，否则清掉 CSS 默认虚线框
  if (f.border && f.border !== 'none') {
    const bw = f.borderWidth || config.borderWidth || 1
    const bc = f.borderColor || config.borderColor || config.fontColor || '#333333'
    const b = `${bw}px solid ${bc}`
    if (f.border === 'all') { st.border = b }
    else if (f.border === 'top') { st.borderTop = b }
    else if (f.border === 'bottom') { st.borderBottom = b }
    else if (f.border === 'left') { st.borderLeft = b }
    else if (f.border === 'right') { st.borderRight = b }
    else if (f.border === 'outer') { st.outline = b; st.outlineOffset = '-1px' }
  } else {
    st.borderStyle = 'none'; st.borderWidth = '0'; st.outline = 'none'
  }
  return st
}

function getCellValue(r, c) {
  const mi = getMergeInfo(r, c)
  if (mi) return cellData[`R${mi.sR}C${mi.sC}`]?.v ?? ''
  return cellData[`R${r}C${c}`]?.v ?? ''
}

function isImageCell(r, c) {
  const val = getCellValue(r, c)
  if (!val) return false
  // Logo占位符
  if (val === '${logo}' && config.logoImage) return true
  // 未解析的占位符（${thumbnail}），不当作图
  if (/\$\{/.test(val)) return false
  // 完整 URL 含图片扩展名
  if (/^https?:\/\/.+\.(jpg|jpeg|png|gif|webp|bmp|svg)(\?.*)?$/i.test(val)) return true
  // Data URI
  if (/^data:image\//i.test(val)) return true
  // 相对路径含图片扩展名
  if (/^[\w\/\.\-]+\.(jpg|jpeg|png|gif|webp|bmp|svg)(\?.*)?$/i.test(val)) return true
  // 模板字段检测：预览模式下，template 该列是否为图片字
  if (previewMode.value && previewTemplate.value) {
    const tpl = previewTemplate.value
    // 查模板中同列的单元格是否含图片占位符
    if (tpl.cells.some(tc => tc.c === c && /\$\{(image|img|thumbnail|photo|pic|picture|image_path|imgUrl|photoUrl|logo)\}/i.test(tc.val))) {
      const { masterRowCount, loopRows, dataRowSpan } = tpl
      if (!loopRows.size) return true
      const loopMinR = Math.min(...loopRows)
      // 表头行：仅当实际值是有效图片URL时才渲染为图片，否则显示文本
      if (r <= masterRowCount) {
        return /^https?:\/\/|\/|^data:image\//i.test(val)
      }
      const offset = (r - 1 - masterRowCount) % dataRowSpan
      if (loopRows.has(loopMinR + offset) && /^https?:\/\/|\/|^data:image\//i.test(val)) return true
    }
  }
  return false
}

function setCellValue(r, c, val) {
  const mi = getMergeInfo(r, c)
  if (mi) { r = mi.sR; c = mi.sC }
  const key = `R${r}C${c}`
  if (!cellData[key]) cellData[key] = { v: '' }
  cellData[key].v = val
}

function getCellFmt(r, c, prop) {
  const mi = getMergeInfo(r, c)
  if (mi) { r = mi.sR; c = mi.sC }
  return cellData[`R${r}C${c}`]?.fmt?.[prop]
}

function setCellFmt(r, c, prop, val) {
  pushUndo()
  const cells = getSelectedCells()
  if (prop === 'border') {
    console.log(`[border] 选中区域 R${Math.min(selection.sR,selection.eR)}-R${Math.max(selection.sR,selection.eR)} C${Math.min(selection.sC,selection.eC)}-C${Math.max(selection.sC,selection.eC)}, 设置 border='${val}'`)
  }
  const writtenKeys = new Set()
  cells.forEach(({ r: cr, c: cc }) => {
    const mi = getMergeInfo(cr, cc)
    const rr = mi ? mi.sR : cr, rcc = mi ? mi.sC : cc
    const key = `R${rr}C${rcc}`
    if (!cellData[key]) cellData[key] = { v: '' }
    if (!cellData[key].fmt) cellData[key].fmt = {}
    cellData[key].fmt[prop] = val
    if (prop === 'border') writtenKeys.add(key)
  })
  // 同步回工具栏状态，避免工具栏后续操作覆盖右侧面板的修改
  if (prop === 'fontSize') fmt.fontSize = val
  else if (prop === 'fontFamily') fmt.fontFamily = val
  else if (prop === 'bold') fmt.bold = val
  else if (prop === 'italic') fmt.italic = val
  else if (prop === 'underline') fmt.underline = val
  else if (prop === 'color') fmt.color = val
  else if (prop === 'bgColor') fmt.bgColor = val
  else if (prop === 'align') selAlign.value = val
  else if (prop === 'verticalAlign') selVAlign.value = val
  else if (prop === 'wordWrap') fmt.wordWrap = val
  if (prop === 'border') {
    console.log(`[border] 实际写入单元格:`, [...writtenKeys].join(', '))
  }
  if (prop === 'wordWrap') {
    const rows = new Set(cells.map(({ r: cr }) => cr))
    rows.forEach(row => refreshRowWordWrap(row))
  }
}

/** 一键清除所有单元格的边框格式 */
function clearAllBorders() {
  pushUndo()
  const keys = Object.keys(cellData)
  keys.forEach(key => {
    const fmt = cellData[key].fmt
    if (fmt) {
      delete fmt.border
      delete fmt.borderWidth
      delete fmt.borderColor
      delete fmt.borderStyle
    }
  })
  rebuildMergeCache()
  // 同步刷新快照，确保导出时不带残留边框
  if (previewSnap.value) {
    const snapKeys = Object.keys(previewSnap.value.cellData || {})
    snapKeys.forEach(key => {
      const fmt = previewSnap.value.cellData[key]?.fmt
      if (fmt) {
        delete fmt.border
        delete fmt.borderWidth
        delete fmt.borderColor
        delete fmt.borderStyle
      }
    })
  }
  showToast('已清除全部边框格式', 'success')
}

function toggleCellFmt(r, c, prop) {
  const cells = getSelectedCells()
  // 根据主单元格当前值决toggle 方向
  const mi = getMergeInfo(r, c)
  const rr = mi ? mi.sR : r, rcc = mi ? mi.sC : c
  const cur = cellData[`R${rr}C${rcc}`]?.fmt?.[prop]
  cells.forEach(({ r: cr, c: cc }) => {
    const mi2 = getMergeInfo(cr, cc)
    const rr2 = mi2 ? mi2.sR : cr, cc2 = mi2 ? mi2.sC : cc
    const key = `R${rr2}C${cc2}`
    if (!cellData[key]) cellData[key] = { v: '' }
    if (!cellData[key].fmt) cellData[key].fmt = {}
    cellData[key].fmt[prop] = !cur
  })
  // wordWrap 变动时刷新对应行的缓存
  if (prop === 'wordWrap') {
    const rows = new Set(cells.map(({ r: cr }) => cr))
    rows.forEach(row => refreshRowWordWrap(row))
  }
}

function getSelectedCells() {
  if (!selCell.value) return [{ r: 1, c: 1 }]
  const { sR, sC, eR, eC } = selection
  const minR = Math.min(sR, eR), maxR = Math.max(sR, eR)
  const minC = Math.min(sC, eC), maxC = Math.max(sC, eC)
  const cells = []
  for (let r = minR; r <= maxR; r++)
    for (let c = minC; c <= maxC; c++)
      cells.push({ r, c })
  return cells
}

// 键盘导航
function moveCell(dr, dc) {
  if (!selCell.value) return
  let nr = selCell.value.r + dr
  let nc = selCell.value.c + dc
  if (nr < 1) nr = 1; if (nr > rowCount.value) nr = rowCount.value
  if (nc < 1) nc = 1; if (nc > colCount.value) nc = colCount.value
  selectCell(nr, nc)
}

function onKeyDown(e) {
  if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return
  if ((e.ctrlKey || e.metaKey) && e.key === 'z') {
    if (previewMode.value) return; e.preventDefault(); undo(); return
  }
  if ((e.ctrlKey || e.metaKey) && (e.key === 'y' || (e.key === 'z' && e.shiftKey))) {
    if (previewMode.value) return; e.preventDefault(); redo(); return
  }
  if (previewMode.value) {
    if (e.key === 'Escape') { exitPreview(); return }
    return  // 预览模式禁用其他键盘编辑
  }
  if (e.key === 'ArrowUp') { e.preventDefault(); moveCell(-1, 0) }
  else if (e.key === 'ArrowDown') { e.preventDefault(); moveCell(1, 0) }
  else if (e.key === 'ArrowLeft') { e.preventDefault(); moveCell(0, -1) }
  else if (e.key === 'ArrowRight') { e.preventDefault(); moveCell(0, 1) }
  else if (e.key === 'Tab') { e.preventDefault(); moveCell(0, 1) }
  else if (e.key === 'F2') { e.preventDefault(); if (selCell.value) startEdit(selCell.value.r, selCell.value.c) }
  else if (e.key === 'Delete' || e.key === 'Backspace') { e.preventDefault(); clearSelectedCells() }
  else if (e.key.length === 1 && !e.ctrlKey && !e.metaKey) {
    e.preventDefault()
    if (selCell.value) {
      startEdit(selCell.value.r, selCell.value.c)
      editing.val = e.key
    }
  }
}

// 鼠标选择
function onCellMouseDown(r, c, e) {
  if (guardEdit()) return
  const mi = getMergeInfo(r, c)
  if (e.shiftKey && selCell.value) {
    selection.eR = mi ? mi.eR : r; selection.eC = mi ? mi.eC : c
  } else {
    selectCell(r, c)
  }
  cellDrag.active = true
  cellDrag.startR = mi ? mi.sR : r
  cellDrag.startC = mi ? mi.sC : c
}

function onColHeaderDown(c, e) {
  const mi = getMergeInfo(1, c)
  if (e.shiftKey && selCell.value) {
    selection.sR = 1; selection.eR = rowCount.value
    selection.eC = mi ? mi.eC : c
  } else {
    selectCell(1, c)
    selection.eR = rowCount.value
    selection.eC = Math.max(selection.eC, c)
  }
  headerDrag.active = true; headerDrag.type = 'col'; headerDrag.startIdx = mi ? mi.sC : c
}

function onRowHeaderDown(r, e) {
  const mi = getMergeInfo(r, 1)
  if (e.shiftKey && selCell.value) {
    selection.sC = 1; selection.eC = colCount.value
    selection.eR = mi ? mi.eR : r
  } else {
    selectCell(r, 1)
    selection.eR = r
    selection.eC = colCount.value
  }
  headerDrag.active = true; headerDrag.type = 'row'; headerDrag.startIdx = mi ? mi.sR : r
}

const headerDrag = reactive({ active: false, type: '', startIdx: 0 })
const cellDrag = reactive({ active: false, startR: 0, startC: 0 })
let lastDragTarget = '' // 节流：仅在目标格变化时更新选区
const colResizeDrag = reactive({ active: false, colIdx: 0, startX: 0, startW: 0 })
const rowResizeDrag = reactive({ active: false, rowIdx: 0, startY: 0, startH: 0 })

function startColResize(c, e) {
  colResizeDrag.active = true
  colResizeDrag.colIdx = c
  colResizeDrag.startX = e.clientX
  colResizeDrag.startW = getColWidth(c)
}

function startRowResize(r, e) {
  rowResizeDrag.active = true
  rowResizeDrag.rowIdx = r
  rowResizeDrag.startY = e.clientY
  rowResizeDrag.startH = getRowHeight(r)
}

function onGlobalMouseMove(e) {
  // 无拖拽时立即短路，避免每帧都做无效判断
  if (!colResizeDrag.active && !rowResizeDrag.active && !cellDrag.active && !headerDrag.active) return
  if (colResizeDrag.active) {
    const delta = (e.clientX - colResizeDrag.startX) / zoomLevel.value
    const w = Math.max(1, Math.min(1200, colResizeDrag.startW + delta))
    if (w !== getColWidth(colResizeDrag.colIdx)) setColWidth(colResizeDrag.colIdx, w)
    return
  }
  if (rowResizeDrag.active) {
    const delta = (e.clientY - rowResizeDrag.startY) / zoomLevel.value
    const h = Math.max(1, Math.min(400, rowResizeDrag.startH + delta))
    if (h !== getRowHeight(rowResizeDrag.rowIdx)) setRowHeight(rowResizeDrag.rowIdx, h)
    return
  }

  // 共用变量：scrollEl & rect
  const scrollEl = sheetScrollRef?.value
  if (!scrollEl) return
  const sr = scrollEl.getBoundingClientRect()
  const z = zoomLevel.value
  const EDGE = 50 // 边缘触发滚动的像素距离

  // ── 边缘自动滚动 ──
  if (cellDrag.active || headerDrag.active) {
    const speedX = e.clientX < sr.left + EDGE ? -(EDGE - (e.clientX - sr.left)) * 0.3
      : e.clientX > sr.right - EDGE ? (EDGE - (sr.right - e.clientX)) * 0.3 : 0
    const speedY = e.clientY < sr.top + EDGE ? -(EDGE - (e.clientY - sr.top)) * 0.3
      : e.clientY > sr.bottom - EDGE ? (EDGE - (sr.bottom - e.clientY)) * 0.3 : 0
    if (speedX !== 0 || speedY !== 0) {
      scrollEl.scrollBy({ left: speedX / z, top: speedY / z })
    }
  }

  if (cellDrag.active) {
    // 用坐标计算代替 elementFromPoint，兼容虚拟滚动
    const x = (e.clientX - sr.left) / z + scrollEl.scrollLeft - 92 // 减去行头列宽
    const y = (e.clientY - sr.top) / z + scrollEl.scrollTop - theadHeight.value
    const r = findRowAtY(y), c = findColAtX(x)
    const key = `${r}|${c}`
    if (r && c && r <= rowCount.value && c <= colCount.value && key !== lastDragTarget) {
      lastDragTarget = key
      // 合并单元格：拖拽时选区应展开到合并区域的右下角
      const mi = getMergeInfo(r, c)
      const er = mi ? mi.eR : r
      const ec = mi ? mi.eC : c
      selection.sR = Math.min(cellDrag.startR, er)
      selection.eR = Math.max(cellDrag.startR, er)
      selection.sC = Math.min(cellDrag.startC, ec)
      selection.eC = Math.max(cellDrag.startC, ec)
      selCell.value = { r: cellDrag.startR, c: cellDrag.startC }
    }
    return
  }

  if (!headerDrag.active) return
  if (headerDrag.type === 'col') {
    // 改用坐标计算代替 elementFromPoint，与虚拟滚动兼容
    const x = (e.clientX - sr.left) / z + scrollEl.scrollLeft - 92
    const c = findColAtX(x)
    const key = `col|${c}`
    if (key === lastDragTarget) return
    lastDragTarget = key
    if (c >= 1 && c <= colCount.value) {
      selection.sR = 1; selection.eR = rowCount.value
      const minC = Math.min(headerDrag.startIdx, c)
      const maxC = Math.max(headerDrag.startIdx, c)
      selection.sC = minC; selection.eC = maxC
      selCell.value = { r: 1, c: minC }
    }
  } else if (headerDrag.type === 'row') {
    // 行头虚拟滚动下可能不在 DOM，用坐标计算
    const y = (e.clientY - sr.top) / z + scrollEl.scrollTop - theadHeight.value
    const r = findRowAtY(y)
    const key = `row|${r}`
    if (key === lastDragTarget) return
    lastDragTarget = key
    if (r >= 1 && r <= rowCount.value) {
      selection.sC = 1; selection.eC = colCount.value
      const minR = Math.min(headerDrag.startIdx, r)
      const maxR = Math.max(headerDrag.startIdx, r)
      selection.sR = minR; selection.eR = maxR
      selCell.value = { r: minR, c: 1 }
    }
  }
}

function onGlobalMouseUp() {
  headerDrag.active = false
  cellDrag.active = false
  lastDragTarget = ''
  colResizeDrag.active = false
  rowResizeDrag.active = false
  if (_overlayScrollRaf) { cancelAnimationFrame(_overlayScrollRaf); _overlayScrollRaf = null }
  // 拖拽结束后立即刷新选区覆盖层
  if (selCell.value) updateSelOverlay()
}

// 单元格编辑
const editing = reactive({ r: 0, c: 0, val: '' })
const cellInputRef = ref(null)
const sheetScrollRef = ref(null)

function startEdit(r, c) {
  if (guardEdit()) return
  selectCell(r, c)
  editing.r = r; editing.c = c
  editing.val = getCellValue(r, c)
  nextTick(() => {
    if (cellInputRef.value) {
      const el = Array.isArray(cellInputRef.value) ? cellInputRef.value[0] : cellInputRef.value
      el?.focus?.()
    }
  })
}

function commitEdit() {
  if (editing.r > 0) {
    const old = getCellValue(editing.r, editing.c)
    if (editing.val !== old) pushUndo()
    setCellValue(editing.r, editing.c, editing.val)
  }
  editing.r = 0; editing.c = 0; editing.val = ''
}

function onEditEnter(e) {
  if (e.altKey || e.shiftKey) {
    const input = e.target
    const start = input.selectionStart
    const end = input.selectionEnd
    editing.val = editing.val.substring(0, start) + '\n' + editing.val.substring(end)
    nextTick(() => {
      input.selectionStart = input.selectionEnd = start + 1
    })
  } else {
    commitEdit()
  }
}

function cancelEdit() {
  editing.r = 0; editing.c = 0; editing.val = ''
}

function moveEdit(dr, dc) {
  commitEdit()
  nextTick(() => {
    moveCell(dr, dc)
    startEdit(selCell.value.r, selCell.value.c)
  })
}

function clearSelectedCells() {
  if (guardEdit()) return
  if (!selCell.value) return
  const cells = getSelectedCells()
  const affectedRows = new Set()
  for (const { r, c } of cells) {
    const key = `R${r}C${c}`
    if (cellData[key]) {
      const hadWrap = cellData[key]?.fmt?.wordWrap
      cellData[key] = { v: '' }
      if (hadWrap) affectedRows.add(r)
    }
  }
  affectedRows.forEach(row => refreshRowWordWrap(row))
}

// ===== 插入/删除行列 =====
function ctxInsertRow(pos) {
  const row = ctxMenu.type === 'row' ? ctxMenu.idx : (ctxMenu.cell?.r || selCell.value?.r || 1)
  const target = pos === 'above' ? row : row + 1
  insertRow(target)
  hideCtxMenu()
}
function ctxInsertCol(pos) {
  const col = ctxMenu.type === 'col' ? ctxMenu.idx : (ctxMenu.cell?.c || selCell.value?.c || 1)
  const target = pos === 'left' ? col : col + 1
  insertCol(target)
  hideCtxMenu()
}
function ctxDeleteRow() {
  const row = ctxMenu.type === 'row' ? ctxMenu.idx : (ctxMenu.cell?.r || selCell.value?.r || 1)
  if (rowCount.value <= 1) { showToast('至少保留一行', 'warn'); return }
  deleteRow(row)
  hideCtxMenu()
}
function ctxDeleteCol() {
  const col = ctxMenu.type === 'col' ? ctxMenu.idx : (ctxMenu.cell?.c || selCell.value?.c || 1)
  if (colCount.value <= 1) { showToast('至少保留一列', 'warn'); return }
  deleteCol(col)
  hideCtxMenu()
}

// 循环起始行设置
function setLoopStartRow() {
  const row = ctxMenu.type === 'row' ? ctxMenu.idx : (ctxMenu.cell?.r || selCell.value?.r || 1)
  dataLoopStartRow.value = row
  hideCtxMenu()
  showToast(`已将${row} 行设为循环起始行`, 'success')
}

// 行头是否循环标记
function isLoopStartRow(r) { return dataLoopStartRow.value === r }
// 行头是否每页表头起始标记
function isPageHeaderStartRow(r) { return pageHeaderStartRow.value === r }

// 设置每页表头起始行
function setPageHeaderStartRow() {
  const row = ctxMenu.type === 'row' ? ctxMenu.idx : (ctxMenu.cell?.r || selCell.value?.r || 1)
  pageHeaderStartRow.value = row
  hideCtxMenu()
  showToast(`已将${row} 行设为每页表头起始行（该行起每页重复）`, 'success')
}

function shiftCellKeys(shiftR, shiftC, fromR, fromC, toR, toC) {
  const newData = {}
  const allKeys = Object.keys(cellData)
  // 按行列从大到小排序，避免覆盖
  allKeys.sort((a, b) => {
    const [ar, ac] = a.match(/\d+/g).map(Number)
    const [br, bc] = b.match(/\d+/g).map(Number)
    if (shiftR) return shiftR > 0 ? (br - ar) : (ar - br)
    if (shiftC) return shiftC > 0 ? (bc - ac) : (ac - bc)
    return 0
  })
  allKeys.forEach(key => {
    const [r, c] = key.match(/\d+/g).map(Number)
    let nr = r, nc = c
    if (shiftR && r >= fromR) nr += shiftR
    if (shiftC && c >= fromC) nc += shiftC
    if (nr !== r || nc !== c) {
      newData[`R${nr}C${nc}`] = cellData[key]
    } else {
      newData[key] = cellData[key]
    }
  })
  // 清除key 并重新赋
  Object.keys(cellData).forEach(k => delete cellData[k])
  Object.assign(cellData, newData)
}

function shiftMergedCells(shiftR, shiftC, fromR, fromC) {
  mergedCells.value = mergedCells.value.map(m => {
    const nm = { ...m }
    if (shiftR && nm.sR >= fromR) { nm.sR += shiftR; nm.eR += shiftR }
    if (shiftC && nm.sC >= fromC) { nm.sC += shiftC; nm.eC += shiftC }
    return nm
  })
}

function insertRow(at) {
  if (guardEdit()) return
  if (at < 1 || at > rowCount.value + 1) return
  pushUndo()
  shiftCellKeys(1, 0, at, 0)
  shiftMergedCells(1, 0, at, 0)
  rowCount.value++
}

function insertCol(at) {
  if (guardEdit()) return
  if (at < 1 || at > colCount.value + 1) return
  pushUndo()
  shiftCellKeys(0, 1, 0, at)
  shiftMergedCells(0, 1, 0, at)
  colCount.value++
}

function deleteRow(at) {
  if (guardEdit()) return
  if (at < 1 || at > rowCount.value) return
  pushUndo()
  // 删除该行所有数
  const toDel = Object.keys(cellData).filter(k => {
    const r = Number(k.match(/R(\d+)/)[1])
    return r === at
  })
  toDel.forEach(k => delete cellData[k])
  shiftCellKeys(-1, 0, at + 1, 0)
  shiftMergedCells(-1, 0, at + 1, 0)
  // 清除shift 后超出范围的行合
  mergedCells.value = mergedCells.value.filter(m => m.sR >= 1 && m.sC >= 1)
  rowCount.value--
}

function deleteCol(at) {
  if (guardEdit()) return
  if (at < 1 || at > colCount.value) return
  pushUndo()
  // 删除该列所有数
  const toDel = Object.keys(cellData).filter(k => {
    const c = Number(k.match(/C(\d+)/)[1])
    return c === at
  })
  toDel.forEach(k => delete cellData[k])
  shiftCellKeys(0, -1, 0, at + 1)
  shiftMergedCells(0, -1, 0, at + 1)
  mergedCells.value = mergedCells.value.filter(m => m.sR >= 1 && m.sC >= 1)
  colCount.value--
}

// 合并单元格
const mergedCells = ref([])

// 合并单元格变化时刷新查找缓存
watch(mergedCells, () => rebuildMergeCache(), { deep: true })

function mergeSelection() {
  if (guardEdit()) return
  if (!selCell.value) return
  pushUndo()
  const minR = Math.min(selection.sR, selection.eR)
  const maxR = Math.max(selection.sR, selection.eR)
  const minC = Math.min(selection.sC, selection.eC)
  const maxC = Math.max(selection.sC, selection.eC)
  if (minR === maxR && minC === maxC) return

  // 移除与新选区重叠的已有合
  mergedCells.value = mergedCells.value.filter(m =>
    maxR < m.sR || minR > m.eR || maxC < m.sC || minC > m.eC
  )

  const val = getCellValue(minR, minC)
  const merge = { sR: minR, eR: maxR, sC: minC, eC: maxC }
  mergedCells.value.push(merge)
  for (let r = minR; r <= maxR; r++) {
    for (let c = minC; c <= maxC; c++) {
      if (r === minR && c === minC) {
        setCellValue(r, c, val)
      } else {
        const key = `R${r}C${c}`
        delete cellData[key]
      }
    }
  }
}

function cancelMerge() {
  if (guardEdit()) return
  if (!selCell.value) return
  pushUndo()
  const mi = getMergeInfo(selCell.value.r, selCell.value.c)
  if (!mi) return
  mergedCells.value = mergedCells.value.filter(
    m => !(m.sR === mi.sR && m.sC === mi.sC && m.eR === mi.eR && m.eC === mi.eC)
  )
  selection.sR = mi.sR; selection.eR = mi.eR
  selection.sC = mi.sC; selection.eC = mi.eC
}

// 合并单元格查找缓存
let mergeCache = {}
function rebuildMergeCache() {
  mergeCache = {}
  for (const m of mergedCells.value) {
    for (let r = m.sR; r <= m.eR; r++) {
      for (let c = m.sC; c <= m.eC; c++) {
        mergeCache[`R${r}C${c}`] = { ...m, isStart: r === m.sR && c === m.sC }
      }
    }
  }
}
function getMergeInfo(r, c) {
  return mergeCache[`R${r}C${c}`] || null
}

// 在合并变更后刷新缓存
function mergeCellsChanged() {
  rebuildMergeCache()
}

function shouldRenderCell(r, c) {
  const mi = getMergeInfo(r, c)
  if (!mi) return true
  return mi.isStart
}

function cellColSpan(r, c) {
  const mi = getMergeInfo(r, c)
  if (mi && mi.isStart) return mi.eC - mi.sC + 1
  return 1
}

function cellRowSpan(r, c) {
  const mi = getMergeInfo(r, c)
  if (mi && mi.isStart) return mi.eR - mi.sR + 1
  return 1
}

// ===== Format =====
const fmt = reactive({
  fontSize: 24,
  fontFamily: 'SimSun, serif',
  bold: false,
  italic: false,
  underline: false,
  wordWrap: false,
  color: '#333333',
  bgColor: '#ffffff',
})

const selAlign = ref('left')
const selVAlign = ref('top')
const colorPalette = [
  '#000000','#ffffff','#c00000','#ed7d31','#ffc000','#70ad47','#4472c4','#7030a0',
  '#333333','#f2f2f2','#ff0000','#f4b183','#ffd966','#a9d18e','#8db4e2','#b4a7d6',
  '#595959','#d9d9d9','#c55a11','#843c0c','#bf8f00','#548235','#2f5496','#624687',
  '#7f7f7f','#bfbfbf','#833c0b','#ed7d31','#7f6000','#375623','#1f3864','#442f62',
  '#a5a5a5','#e7e6e6','#f2dcdb','#fce4d6','#fff2cc','#e2efda','#d6dce4','#e4dfec',
]

const fontClrRef = ref(null), bgClrRef = ref(null)
const fontClrOpen = ref(false), bgClrOpen = ref(false)
const fontClrPos = ref({}), bgClrPos = ref({})

function popPos(el) {
  const r = el.getBoundingClientRect()
  return { position: 'fixed', top: (r.bottom + 4) + 'px', left: r.left + 'px' }
}

const toggleFontClr = () => { bgClrOpen.value = false; fontClrOpen.value = !fontClrOpen.value; if (fontClrOpen.value) fontClrPos.value = popPos(fontClrRef.value) }
const toggleBgClr = () => { fontClrOpen.value = false; bgClrOpen.value = !bgClrOpen.value; if (bgClrOpen.value) bgClrPos.value = popPos(bgClrRef.value) }

function closeColorPops() {
  fontClrOpen.value = false
  bgClrOpen.value = false
}

function applyFmt() {
  if (guardEdit()) return
  if (!selCell.value) return
  pushUndo()
  for (let r = 1; r <= rowCount.value; r++) {
    for (let c = 1; c <= colCount.value; c++) {
      if (!shouldRenderCell(r, c)) continue
      if (isSelected(r, c)) {
        const key = `R${r}C${c}`
        if (!cellData[key]) cellData[key] = { v: '' }
        if (!cellData[key].fmt) cellData[key].fmt = {}
        const f = cellData[key].fmt
        f.bold = fmt.bold
        f.italic = fmt.italic
        f.underline = fmt.underline
        f.color = fmt.color
        f.bgColor = fmt.bgColor
        f.fontSize = fmt.fontSize
        f.fontFamily = fmt.fontFamily
        f.align = selAlign.value
        f.verticalAlign = selVAlign.value
        f.wordWrap = fmt.wordWrap
      }
    }
  }
}

function syncFmtFromSelection() {
  if (!selCell.value) return
  const r = selCell.value.r, c = selCell.value.c
  const f = cellData[`R${r}C${c}`]?.fmt
  if (f) {
    if (f.fontSize != null) fmt.fontSize = f.fontSize
    if (f.fontFamily != null) fmt.fontFamily = f.fontFamily
    if (f.bold != null) fmt.bold = f.bold
    if (f.italic != null) fmt.italic = f.italic
    if (f.underline != null) fmt.underline = f.underline
    if (f.color != null) fmt.color = f.color
    if (f.bgColor != null) fmt.bgColor = f.bgColor
    if (f.align != null) selAlign.value = f.align
    if (f.verticalAlign != null) selVAlign.value = f.verticalAlign
    if (f.wordWrap != null) fmt.wordWrap = f.wordWrap
  } else {
    fmt.fontSize = 18; fmt.fontFamily = 'SimSun, serif'
    fmt.bold = false; fmt.italic = false; fmt.underline = false; fmt.wordWrap = false
    fmt.color = '#333333'; fmt.bgColor = '#ffffff'
    selAlign.value = 'left'
    selVAlign.value = 'top'
  }
}

watch(selCell, () => { syncFmtFromSelection() }, { deep: true })

watch([paperSize, paperOrient], () => {
  if (colCount.value < colsPerPage.value) colCount.value = colsPerPage.value
  if (rowCount.value < rowsPerPage.value) rowCount.value = rowsPerPage.value
  // 纸张变化时重新计算分页计划
  if (rawPreviewData.value.length) computePagePlan(rawPreviewData.value)
})

const onFieldDragStart = (e, dsName, field) => {
  if (previewMode.value) { e.preventDefault(); return }
  // 组合字段：field.field 已含完整模板表达式，直接传
  const val = (field.field.startsWith('${') || field.field.includes('×') || field.field.includes('~'))
    ? field.field
    : `\${${field.field}}`
  e.dataTransfer.setData('text/plain', val)
  e.dataTransfer.effectAllowed = 'copy'
}

const onCellDrop = (e) => {
  if (previewMode.value) return
  const val = e.dataTransfer.getData('text/plain')
  if (!val) return
  // 直接使用 dropTargetCell（由 onDragOver 设置），确保与绿色高亮框一致，避免鼠标微动导致偏差
  const dt = dropTargetCell.value
  if (!dt) { dropTargetCell.value = null; return }
  const mi = getMergeInfo(dt.r, dt.c)
  const r = mi ? mi.sR : dt.r
  const c = mi ? mi.sC : dt.c
  dropTargetCell.value = null
  pushUndo()
  setCellValue(r, c, val)
  selectCell(r, c)
}

const onDragLeave = (e) => {
  // 只有当鼠标真正离开 sheet-wrap 时才清除高亮（避免子元素间移动触发）
  const wrap = e.currentTarget
  if (!wrap.contains(e.relatedTarget)) {
    dropTargetCell.value = null
  }
}

const onDragOver = (e) => {
  if (previewMode.value) return
  // 方案：直接从鼠标位置找到底下的 td 元素，避免坐标计算与真实 DOM 的偏差
  const el = document.elementFromPoint(e.clientX, e.clientY)
  if (!el) { dropTargetCell.value = null; return }
  const td = el.closest('td[data-r][data-c]')
  if (!td) { dropTargetCell.value = null; return }
  let r = parseInt(td.dataset.r), c = parseInt(td.dataset.c)
  if (!r || !c || r > rowCount.value || c > colCount.value) {
    dropTargetCell.value = null
    return
  }
  const mi = getMergeInfo(r, c)
  if (mi) { r = mi.sR; c = mi.sC }
  const prev = dropTargetCell.value
  if (!prev || prev.r !== r || prev.c !== c) {
    dropTargetCell.value = { r, c }
  }
}

// 拖拽目标格子高亮样式（仅在格子变化时用 DOM 精确定位，不会频繁触发）
const dropTargetStyle = computed(() => {
  const dt = dropTargetCell.value
  if (!dt) return null
  const scrollEl = sheetScrollRef?.value
  if (!scrollEl) return null
  // 找到目标格子所在的实际渲染 td（处理合并单元格：被覆盖的 td 用 v-show 隐藏）
  const mi = getMergeInfo(dt.r, dt.c)
  const r = mi ? mi.sR : dt.r
  const c = mi ? mi.sC : dt.c
  const cell = scrollEl.querySelector(`[data-r="${r}"][data-c="${c}"]`)
  if (!cell || cell.offsetParent === null) return null
  const scrollRect = scrollEl.getBoundingClientRect()
  const cr = cell.getBoundingClientRect()
  const z = zoomLevel.value
  return {
    position: 'absolute',
    left: ((cr.left - scrollRect.left) / z + scrollEl.scrollLeft) + 'px',
    top: ((cr.top - scrollRect.top) / z + scrollEl.scrollTop) + 'px',
    width: (cr.width / z) + 'px',
    height: (cr.height / z) + 'px',
    pointerEvents: 'none',
    zIndex: 10,
    border: '2px dashed #22c55e',
    background: 'rgba(34,197,94,0.08)',
    boxSizing: 'border-box',
  }
})
// 安全 JSON 解析，防HTML 响应报错
async function safeFetchJson(url, opts = {}) {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token') || ''
  const resp = await fetch(url, {
    ...opts,
    headers: { 'Authorization': 'Bearer ' + token, ...(opts.headers || {}) }
  })
  if (!resp.ok) return { ok: false, data: null, status: resp.status }
  const ct = resp.headers.get('content-type') || ''
  if (!ct.includes('application/json')) {
    return { ok: false, data: null, status: resp.status }
  }
  try {
    return { ok: true, data: await resp.json(), status: resp.status }
  } catch {
    return { ok: false, data: null, status: resp.status }
  }
}

// 字段中文映射（与字段映射文档对齐）
const sampleFieldLabels = {
  // === 厂商信息（snake_case）===
  id: 'ID', cid: 'CID', rid: 'RID', sid: 'SID',
  company_id: '厂商ID', company_name: '公司名称',
  manufacturer_code: '厂商编号', manufacturer_name: '厂商名称',
  vendor_code: '厂商编号', vendor_name: '厂商名称', vendor_id: '厂商ID',
  booth_no: '摊位号', booth_type: '摊位类型', floor_zone: '楼层区位', booth_area: '摊位区位',
  booth_location: '展位位置', booth_number: '展位编号',
  contact: '联系人', mobile: '手机', phone: '电话', qq: 'QQ', email: '邮箱', fax: '传真',
  address: '厂商地址', country: '国家', city: '城市', province: '省份',
  cert_no: '厂商证书', last_expiry: '上次到期', expiry_date: '到期日期',
  registrant: '登记人', modifier: '修改人',
  create_by: '创建人ID', created_by: '创建人ID', update_by: '更新人ID', updated_by: '更新人ID',
  modify_by: '修改人', delete_by: '删除人', deleted_by: '删除人',
  create_time: '创建时间', update_time: '更新时间', created_at: '创建时间', updated_at: '更新时间',
  // === 样品信息（snake_case）===
  sample_id: '样品ID', sample_code: '公司编号', sample_name: '样品名称',
  english_name: '英文名称', factory_code: '出厂货号', sample_unit: '样品单位',
  packaging: '包装方式', factory_price: '出厂价', tax_price: '税点价',
  product_spec: '产品规格', gross_weight: '产品毛重', net_weight: '产品净重',
  carton_length: '外箱长', carton_width: '外箱宽', carton_height: '外箱高',
  carton_spec: '外箱规格', carton_volume: '体积', carton_material_vol: '材积', carton_material_volume: '材积',
  carton_gross_wt: '外箱毛重', carton_net_wt: '外箱净重', carton_gross_weight: '外箱毛重', carton_net_weight: '外箱净重',
  inner_box_qty: '内盒数', inner_box_count: '内盒数', inner_box: '内盒数', box_count: '箱数', carton_capacity: '装箱量', packing_unit: '装箱单位',
  package_spec: '包装规格', packaging_method: '包装方式',
  package_length: '包装长', package_width: '包装宽', package_height: '包装高',
  certification: '产品认证', certification_count: '认证数量', category: '分类', type: '类型',
  color: '颜色', size: '尺寸',
  sample_contact: '样品联系人', sample_phone: '样品电话',
  battery_info: '电池信息', infringement: '侵权信息',
  remark: '中文备注', remark_cn: '中文备注', remark_en: '英文备注',
  other_remark: '其他备注', notes: '附注',
  image_path: '图片路径', image: '图片', images: '图片列表',
  thumbnail: '缩略图', photo: '照片', photo_url: '照片URL',
  print_time: '打印时间', operator_name: '操作员',
  total_pages: '总页数', page_no: '页码',
  status: '状态', is_active: '启用', is_deleted: '已删除',
  model: '型号', style_no: '款号', item_no: '货号',
  unit_price: '单价', total_price: '总价', quantity: '数量', amount: '金额',
  volume: '体积', gross_wt: '毛重', net_wt: '净重',
  length: '长度', width: '宽度', height: '高度', diameter: '直径',
  moq: '起订量', delivery_time: '交货期', port: '港口',
  origin: '产地', name: '厂商名称',
  sample_unit_en: '样品单位(英)', packaging_cn: '中文包装', packaging_en: '英文包装',
  package_code: '包装编号', color_en: '颜色(英)',
  contact_person: '联系人', contact_phone: '联系电话',
  sample_length: '样品长', sample_width: '样品宽', sample_height: '样品高',
  sample_gross_weight: '产品毛重', sample_net_weight: '产品净重',
  category_code: '样品种类编号', package_code: '包装编号',
  hide_from_xzx: '是否不在小竹熊显示',
  visitor_mobile: '见客手机',
  // === 厂商特有（snake_case）===
  certificate: '厂商证书', floor_area: '楼层区位', booth_meters: '摊位米数',
  sms_number: '短信号码', main_card: '主卡', sub_card: '副卡',
  name: '厂商名称', code: '编码', notes: '附注',
  // contact1~3 系列
  contact1: '联系人1', phone1: '电话1', mobile1: '手机1',
  contact2: '联系人2', phone2: '电话2', mobile2: '手机2',
  contact3: '联系人3', phone3: '电话3', mobile3: '手机3',

  // ==============================
  // === camelCase 兼容（API 常返回的驼峰字段名）===
  // ==============================
  // 系统字段
  createTime: '创建时间', updateTime: '更新时间', createdAt: '创建时间', updatedAt: '更新时间',
  deleted: '已删除', isActive: '启用', isDeleted: '已删除',
  createBy: '创建人ID', createdBy: '创建人ID', updateBy: '更新人ID', updatedBy: '更新人ID',
  modifyBy: '修改人', deleteBy: '删除人', deletedBy: '删除人',
  codeName: '本次代号', code_name: '本次代号',
  // 厂商信息
  vendorCode: '厂商编号', vendorName: '厂商名称', vendorId: '厂商ID',
  companyName: '公司名称', companyId: '厂商ID',
  manufacturerCode: '厂商编号', manufacturerName: '厂商名称',
  boothNo: '摊位号', boothType: '摊位类型', floorZone: '楼层区位', boothArea: '摊位区位',
  boothLocation: '展位位置', boothNumber: '展位编号',
  contact: '联系人', mobile: '手机', phone: '电话', qq: 'QQ', email: '邮箱', fax: '传真',
  address: '地址', country: '国家', city: '城市', province: '省份',
  certNo: '厂商证书', lastExpiry: '上次到期', expiryDate: '到期日期',
  registrant: '登记人', modifier: '修改人',
  floorArea: '楼层区位', boothMeters: '摊位米数',
  smsNumber: '短信号码', mainCard: '主卡', subCard: '副卡',
  certificate: '厂商证书',
  phone1: '电话1', mobile1: '手机1', phone2: '电话2', mobile2: '手机2', phone3: '电话3', mobile3: '手机3',
  // 样品信息
  sampleId: '样品ID', sampleCode: '公司编号', sampleName: '样品名称', englishName: '英文名称',
  factoryCode: '出厂货号', sampleUnit: '样品单位', packaging: '包装方式',
  factoryPrice: '出厂价', taxPrice: '价格1', taxPrice2: '价格2', productSpec: '产品规格',
  grossWeight: '产品毛重', netWeight: '产品净重',
  cartonLength: '外箱长', cartonWidth: '外箱宽', cartonHeight: '外箱高',
  cartonSpec: '外箱规格', cartonVolume: '体积', cartonMaterialVol: '材积', cartonMaterialVolume: '材积',
  cartonGrossWt: '外箱毛重', cartonNetWt: '外箱净重', cartonGrossWeight: '外箱毛重', cartonNetWeight: '外箱净重',
  innerBoxQty: '内盒数', innerBoxCount: '内盒数', innerBox: '内盒数', boxCount: '箱数', cartonCapacity: '装箱量',
  packingUnit: '装箱单位', packageSpec: '包装规格', packagingMethod: '包装方式',
  packageLength: '包装长', packageWidth: '包装宽', packageHeight: '包装高',
  certification: '产品认证', certificationCount: '认证数量', category: '分类', type: '类型',
  color: '颜色', size: '尺寸',
  sampleContact: '样品联系人', samplePhone: '样品电话',
  batteryInfo: '电池信息', infringement: '侵权信息',
  remarkCn: '中文备注', remarkEn: '英文备注', otherRemark: '其他备注', sampleStatus: '择样状态', showroomReplenished: '展厅已补', borrowedSample: '借样',
  imagePath: '图片路径', image: '图片', images: '图片列表',
  thumbnail: '缩略图', photo: '照片', photoUrl: '照片URL',
  operatorName: '操作员', operator_name: '操作员', totalPages: '总页数',
  printTime: '打印时间', pageNo: '页码',
  status: '状态', model: '型号', styleNo: '款号', itemNo: '货号',
  unitPrice: '单价', totalPrice: '总价', quantity: '数量', amount: '金额',
  volume: '体积', grossWt: '毛重', netWt: '净重',
  length: '长度', width: '宽度', height: '高度', diameter: '直径',
  moq: '起订量', deliveryTime: '交货期', port: '港口',
  origin: '产地', name: '厂商名称',
  sampleUnitEn: '样品单位(英)', packagingCn: '中文包装', packagingEn: '英文包装',
  packageCode: '包装编号', colorEn: '颜色(英)',
  contact1: '联系人', phone1: '联系电话',
  sampleLength: '样品长', sampleWidth: '样品宽', sampleHeight: '样品高',
  sampleGrossWeight: '产品毛重', sampleNetWeight: '产品净重',
  categoryCode: '样品种类编号', packageCode: '包装编号',
  hideFromXzx: '是否不在小竹熊显示',
  visitorMobile: '见客手机',
  name: '厂商名称', code: '编码', notes: '附注',

  // === 客户资料（camelCase / snake_case）===
  customerCode: '客户编号', customerName: '客户名称',
  contactPerson1: '联系人1', contactPerson2: '联系人2', contactPerson3: '联系人3',
  region: '地区', smsNumber: '短信号码',
  registerDate: '登记日期', modifyDate: '修改日期',
  remark1: '备注1', remark2: '备注2',
  customer_code: '客户编号', customer_name: '客户名称',
  contact_person1: '联系人1', contact_person2: '联系人2', contact_person3: '联系人3',
  register_date: '登记日期', modify_date: '修改日期',
}

// camelCase snake_case 转换器（用于反向查找映射表）
function camelToSnake(str) {
  return str.replace(/[A-Z]/g, c => '_' + c.toLowerCase())
}

// === 字段别名映射（模块级常量，避免每次 resolveVal 重建） ===
const FIELD_ALIAS_MAP = {
  carton_gross_wt: 'cartonGrossWeight', carton_net_wt: 'cartonNetWeight',
  inner_box_qty: 'innerBoxCount', inner_box: 'innerBoxCount',
  packaging: 'packagingCn', packaging_cn: 'packagingCn',
  packing_unit: 'packingUnit', packaging_method: 'packagingCn',
  carton_material_vol: 'cartonMaterialVolume',
  gross_weight: 'sampleGrossWeight', net_weight: 'sampleNetWeight',
  vendor_name: 'vendorName', booth_no: 'boothNo', cert_no: 'certNo',
  sample_code: 'sampleCode', sample_name: 'sampleName', english_name: 'englishName',
  manufacturer_code: 'manufacturerCode', sample_unit: 'sampleUnit',
  factory_price: 'factoryPrice', tax_price: 'taxPrice',
  carton_length: 'cartonLength', carton_width: 'cartonWidth', carton_height: 'cartonHeight',
  carton_volume: 'cartonVolume', carton_capacity: 'cartonCapacity',
  remark_cn: 'remarkCn', remark_en: 'remarkEn', battery_info: 'batteryInfo',
  image_path: 'imagePath', sample_id: 'sampleId', company_id: 'companyId',
  vendor_code: 'vendorCode', vendor_id: 'vendorId',
  booth_type: 'boothType', floor_zone: 'floorZone', booth_area: 'boothArea',
  last_expiry: 'lastExpiry', expiry_date: 'expiryDate',
  create_time: 'createTime', update_time: 'updateTime',
  sample_contact: 'sampleContact', sample_phone: 'samplePhone',
  print_time: 'printTime', operator_name: 'operatorName',
  create_by: 'createBy', update_by: 'updateBy',
  sample_length: 'sampleLength', sample_width: 'sampleWidth', sample_height: 'sampleHeight',
  sample_gross_weight: 'sampleGrossWeight', sample_net_weight: 'sampleNetWeight',
  package_length: 'packageLength', package_width: 'packageWidth', package_height: 'packageHeight',
  product_spec: 'productSpec', package_spec: 'packageSpec', carton_spec: 'cartonSpec',
}
// 预编译反向别名 Map：key → camelCase 行字段名
const REVERSE_ALIAS_MAP = {}
for (const [k, camelK] of Object.entries(FIELD_ALIAS_MAP)) {
  REVERSE_ALIAS_MAP[k] = camelK
}
// 前缀列表
const FIELD_PREFIXES = ['sample_', 'vendor_', 'carton_', 'booth_', 'product_', 'company_']

// 统一模板填充{field} @{field} 都替换，区别在于循环检测时只统${
// 去除数值末尾无意义的 .00 / .0（如 25.00 → 25, 25.33 → 25.33）
// 保留前导零的字符串（如厂商编号 03340）不转数字
function fmtNumeric(val) {
  if (val == null) return val
  if (typeof val === 'number') {
    return Number.isInteger(val) ? String(val) : String(parseFloat(val.toFixed(10)))
  }
  if (typeof val === 'string' && /^-?\d+(\.\d+)?$/.test(val.trim())) {
    // 有前导零或超过15位的纯数字串，当作编码保留原值（如 03340）
    if ((val.length > 1 && val[0] === '0' && !val.includes('.')) || val.length > 15) {
      return val
    }
    const num = parseFloat(val)
    if (!isNaN(num)) {
      return Number.isInteger(num) ? String(num) : String(parseFloat(num.toFixed(10)))
    }
  }
  return val
}

function fillTemplate(str, row) {
  return str.replace(/[\$\@]\{(\w+)\}/g, (match, f) => {
    const v = resolveVal(row, f)
    return v != null ? fmtNumeric(v) : ''
  })
}
// 判断单元格是否包含循环占位符 ${...}（排除全局/meta字段，与computeTemplateBands保持一致）
function hasLoopPlaceholder(val) {
  if (!/\$\{/.test(val)) return false
  const globalFields = ['title', 'logo', 'currentPage', 'page', 'currentDate', 'currentMonth', 'total_pages', 'printTime', 'operatorName']
  const fields = [...val.matchAll(/\$\{(\w+)\}/g)].map(m => m[1])
  return fields.some(f => !globalFields.includes(f))
}

// resolveVal：从数据行解析占位符字段值（兼容 snake_case / camelCase / 前缀变体 / SQL计算字段）
function resolveVal(row, field) {
  // Logo 全局字段，不依赖数据行
  if (field === 'logo' && config.logoImage) return config.logoImage
  // title 全局字段
  if (field === 'title') return config.title || ''
  // === 拼接字段（必须在通用 row[field] 之前，否则 SQL 的 CONCAT 结果会跳过本地格式化） ===
  if (field === 'carton_spec' || field === 'cartonSpec') {
    const len = row.cartonLength ?? row.carton_length ?? null
    const wid = row.cartonWidth ?? row.carton_width ?? null
    const hei = row.cartonHeight ?? row.carton_height ?? null
    if (len != null && wid != null && hei != null) return `${fmtNumeric(len)}×${fmtNumeric(wid)}×${fmtNumeric(hei)}`
  }
  if (field === 'product_spec' || field === 'productSpec') {
    const sl = row.sampleLength ?? row.sample_length ?? null
    const sw = row.sampleWidth ?? row.sample_width ?? null
    const sh = row.sampleHeight ?? row.sample_height ?? null
    if (sl != null && sw != null && sh != null) return `${fmtNumeric(sl)}×${fmtNumeric(sw)}×${fmtNumeric(sh)}`
  }
  if (field === 'package_spec' || field === 'packageSpec') {
    const pl = row.packageLength ?? row.package_length ?? null
    const pw = row.packageWidth ?? row.package_width ?? null
    const ph = row.packageHeight ?? row.package_height ?? null
    if (pl != null && pw != null && ph != null) return `${fmtNumeric(pl)}×${fmtNumeric(pw)}×${fmtNumeric(ph)}`
  }
  if (field === 'innerCarton') {
    const ib = row.innerBoxCount ?? row.inner_box_qty ?? row.inner_box ?? null
    const ca = row.cartonCapacity ?? row.carton_capacity ?? null
    if (ib != null && ca != null) return `${fmtNumeric(ib)} / ${fmtNumeric(ca)}`
    if (ib != null) return fmtNumeric(ib)
    if (ca != null) return fmtNumeric(ca)
  }
  // 厂商联系方式：名称 + 电话1 + 手机1
  if (field === 'vendorContact') {
    const name = row.name ?? row.vendor_name ?? ''
    const phone = row.phone1 ?? row.phone ?? row.contactPhone ?? ''
    const mobile = row.mobile1 ?? row.mobile ?? ''
    const parts = [name, phone, mobile].filter(p => p != null && p !== '')
    return parts.join('  ') || ''
  }
  // thumbnail: 拼接完整缩略图URL（必须在通用查找之前处理，否则返回裸文件名）
  if (field === 'thumbnail') {
    const tn = row.thumbnail ?? null
    if (tn && !/^https?:\/\//i.test(tn)) return `${window.location.origin}/thumbnails/${tn}`
    return tn
  }
  if (row[field] != null) return row[field]
  const sk = camelToSnake(field); if (row[sk] != null) return row[sk]
  const cc = field.replace(/_([a-z])/g, (_, c) => c.toUpperCase()); if (row[cc] != null) return row[cc]
  // 前缀变体
  for (const p of FIELD_PREFIXES) {
    if (field.startsWith(p)) { const sub = field.slice(p.length); if (row[sub] != null) return row[sub] }
  }
  // 常用别名check（提前，比完整 aliasMap 更高效）
  if (field === 'name' && row.vendor_name != null) return row.vendor_name
  if (field === 'contact1' && row.contact != null) return row.contact
  if (field === 'phone1' && row.phone != null) return row.phone
  // === SQL 计算字段 / 别名映射 ===
  if (field === 'carton_material_vol' || field === 'cartonMaterialVol') {
    if (row.cartonMaterialVolume != null) return row.cartonMaterialVolume
    if (row.carton_material_volume != null) return row.carton_material_volume
  }
  if (field === 'remark' || field === 'remark_cn' || field === 'remarkCn') {
    if (row.remark != null) return row.remark
    if (row.remarkCn != null) return row.remarkCn
    if (row.remark_cn != null) return row.remark_cn
  }
  if (field === 'remark_en' || field === 'remarkEn') {
    if (row.remarkEn != null) return row.remarkEn
    if (row.remark_en != null) return row.remark_en
  }
  if (field === 'image_path' || field === 'imagePath') {
    const tn = row.thumbnail ?? null
    if (tn) return `${window.location.origin}/thumbnails/${tn}`
  }
  // === 预编译别名映射（单次 Map 查找） ===
  const mapped = REVERSE_ALIAS_MAP[field]
  if (mapped && row[mapped] != null) return row[mapped]
  // 反向映射：camelCase → snake_case 后再查（兼容历史模板中 camelCase 占位符）
  const sk2 = camelToSnake(field)
  if (sk2 !== field) {
    const rev = REVERSE_ALIAS_MAP[sk2]
    if (rev && row[rev] != null) return row[rev]
  }
  return null
}

// 智能字段标签生成：有映射用中文，无映射自动转可读中文
function fieldLabel(key) {
  if (!key) return ''
  // 1. 直接匹配（snake_case / camelCase / 纯小写）
  if (sampleFieldLabels[key]) return sampleFieldLabels[key]
  // 2. camelCase snake_case 后再查（createTime create_time
  const sk = camelToSnake(key)
  if (sampleFieldLabels[sk]) return sampleFieldLabels[sk]
  // 3. 部分匹配：去掉前缀 (sample_, vendor_, carton_, booth_) 后查
  const prefixes = ['sample_', 'vendor_', 'carton_', 'booth_', 'product_', 'company_']
  for (const p of prefixes) {
    const k = key.startsWith(p) ? key.slice(p.length) : sk.startsWith(p) ? sk.slice(p.length) : null
    if (k && sampleFieldLabels[k]) return sampleFieldLabels[k]
  }
  // 4. 自动生成：snake_case / camelCase 可读中文风格标签
  return sk.replace(/_/g, ' ').split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ')
}

const sampleCoreFields = [
  'vendor_name', 'boothNo', 'sampleCode', 'sampleName', 'englishName',
  'manufacturerCode', 'category', 'categoryCode', 'factoryCode', 'sampleUnit', 'packagingCn', 'factoryPrice',
  'taxPrice', 'productSpec', 'sampleLength', 'sampleWidth', 'sampleHeight',
  'sampleGrossWeight', 'sampleNetWeight',
  'cartonSpec', 'cartonLength', 'cartonWidth', 'cartonHeight',
  'cartonVolume', 'cartonMaterialVolume', 'cartonGrossWeight', 'cartonNetWeight',
  'innerBoxCount', 'cartonCapacity', 'packingUnit',
  'packageCode', 'packageLength', 'packageWidth', 'packageHeight',
  'color', 'size', 'certification',
  'contact', 'mobile', 'phone', 'qq', 'address',
  'remarkCn', 'remarkEn', 'batteryInfo', 'infringement',
  'imagePath', 'thumbnail', 'hideFromXzx', 'operator_name', 'total_pages',
]

// ===== 组合字段（常用字段快捷入口）=====
const compositeFields = [
  { field: '${cartonSpec}', title: '外箱规格(长×宽×高)' },
  { field: '${productSpec}', title: '样品规格(长×宽×高)' },
  { field: '${packageSpec}', title: '包装规格(长×宽×高)' },
  { field: '${packagingCn}', title: '中文包装' },
  { field: '${innerCarton}', title: '内盒/装箱量' },
  { field: '${logo}', title: '公司Logo(上传后可用)' },
  { field: '${vendorContact}', title: '厂商联系方式(名称+电话+手机)' },
]

// ===== 数据=====
const _makeFields = (keys, _labels) => keys.map(k => ({ field: k, title: fieldLabel(k) }))
const datasets = ref([
  {
    name: '样品资料',
    expanded: true,
    fields: _makeFields([
      'vendor_name', 'boothNo', 'sampleCode', 'sampleName', 'englishName',
      'manufacturerCode', 'category', 'categoryCode', 'factoryCode', 'sampleUnit', 'packagingCn', 'packagingEn', 'factoryPrice',
      'taxPrice', 'productSpec', 'sampleLength', 'sampleWidth', 'sampleHeight',
      'sampleGrossWeight', 'sampleNetWeight',
      'cartonSpec', 'cartonLength', 'cartonWidth', 'cartonHeight',
      'cartonVolume', 'cartonMaterialVolume', 'cartonGrossWeight', 'cartonNetWeight',
      'innerBoxCount', 'boxCount', 'cartonCapacity', 'packingUnit',
      'packageCode', 'packageLength', 'packageWidth', 'packageHeight',
      'color', 'size', 'certification',
      'contact', 'mobile', 'phone', 'qq', 'address',
      'remarkCn', 'remarkEn', 'batteryInfo', 'infringement',
      'imagePath', 'thumbnail', 'hideFromXzx',
      'boothType', 'floorZone', 'boothArea',
      'certNo', 'lastExpiry', 'expiryDate', 'registrant', 'createTime',
      'printTime', 'operator_name', 'total_pages',
      'createBy', 'updateBy',
    ], sampleFieldLabels)
  },
  {
    name: '厂商资料',
    expanded: false,
    fields: _makeFields([
      'manufacturerCode', 'name', 'boothNo',
      'contact1', 'phone1', 'mobile1',
      'contact2', 'phone2', 'mobile2',
      'contact3', 'phone3', 'mobile3',
      'address', 'qq',
      'certificate', 'boothMeters', 'boothType', 'floorArea', 'boothArea',
      'smsNumber', 'mainCard', 'subCard',
      'lastExpiry', 'expiryDate', 'registrant',
      'remark', 'otherRemark', 'visitorMobile',
      'createTime', 'updateTime',
    ])
  },
  {
    name: '择样明细',
    expanded: false,
    fields: _makeFields([
      'codeName', 'sampleCode', 'sampleName', 'englishName', 'factoryCode', 'manufacturerCode',
      'category', 'categoryCode', 'factoryPrice',
      'taxPrice', 'taxPrice2',
      'boxCount', 'otherRemark',
      'showroomReplenished', 'borrowedSample', 'sampleStatus',
      'packagingCn', 'packagingEn', 'productSpec', 'cartonSpec',
      'cartonCapacity', 'innerBoxCount',
      'sampleLength', 'sampleWidth', 'sampleHeight',
      'cartonLength', 'cartonWidth', 'cartonHeight',
      'cartonVolume', 'cartonGrossWeight', 'cartonNetWeight',
      'boothNo', 'color', 'size', 'certification',
      'itemId', 'addDate', 'modifyDate',
    ])
  },
  {
    name: '客户资料',
    expanded: false,
    fields: _makeFields([
      'customerCode', 'customerName', 'country', 'address',
      'contactPerson1', 'mobile1', 'phone1',
      'contactPerson2', 'mobile2', 'phone2',
      'contactPerson3', 'mobile3', 'phone3',
      'email', 'qq',
      'region', 'smsNumber',
      'registerDate', 'modifyDate',
      'registrant', 'modifier',
      'certificate',
    ])
  },
  {
    name: '组合字段',
    expanded: true,
    fields: compositeFields,
  }
])

// ===== 字段搜索 =====
const fieldSearch = ref('')
// 独立跟踪各数据集面板的展开/折叠状态（避免 computed 重建对象丢失状态）
const dsExpanded = reactive({})
function ensureDsExpanded(dsName, defaultVal = true) {
  if (!(dsName in dsExpanded)) dsExpanded[dsName] = defaultVal
}
const filteredDatasets = computed(() => {
  const q = fieldSearch.value.trim().toLowerCase()
  if (!q) return datasets.value.map(ds => {
    ensureDsExpanded(ds.name, ds.expanded ?? true)
    return { ...ds, rawFields: ds.fields, visibleFields: ds.fields, expanded: dsExpanded[ds.name] }
  })
  return datasets.value.map(ds => {
    ensureDsExpanded(ds.name, ds.expanded ?? true)
    const rawFields = ds.fields
    const visibleFields = rawFields.filter(f =>
      f.field.toLowerCase().includes(q) || f.title.toLowerCase().includes(q)
    )
    return { ...ds, rawFields, visibleFields, expanded: dsExpanded[ds.name] }
  })
})

// ===== 导入数据=====
const showImportModal = ref(false)
const showImportMenu = ref(false)
const importTab = ref('csv')
const importName = ref('')
const importDb = ref('default')
const importSql = ref('')
const importApiUrl = ref('')
const importing = ref(false)
const csvFileRef = ref(null)
let csvFileData = null

const logoInput = ref(null)
function onLogoUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) { showToast('请选择图片文件', 'warn'); return }
  const reader = new FileReader()
  reader.onload = () => {
    config.logoImage = reader.result
    showToast('Logo已上传', 'success')
  }
  reader.onerror = () => showToast('文件读取失败', 'error')
  reader.readAsDataURL(file)
}

async function quickImport(source) {
  showImportMenu.value = false
  importing.value = true
  try {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token') || ''
    let fields = []

    if (source === 'samples') {
      let raw = []
      const r1 = await safeFetchJson('/samples?size=5')
      if (r1.ok) {
        raw = r1.data?.data?.records || r1.data?.records || r1.data?.data?.list || r1.data?.list || []
        if (raw.length === 0 && Array.isArray(r1.data?.data)) raw = r1.data.data
      }
      if (raw.length === 0) {
        const r2 = await safeFetchJson('/samples/vendor-confirm-report')
        if (r2.ok) {
          raw = r2.data?.data || r2.data?.records || r2.data || []
        }
      }
      if (raw.length === 0) throw new Error('样品数据为空，请先确保系统中有样品数据')
      // 合并所有记录的 key，避免第一条缺失某些字
      const keys = [...new Set(raw.flatMap(r => Object.keys(r)))]
      fields = sampleCoreFields
        .filter(k => keys.includes(k))
        .map(k => ({ field: k, title: fieldLabel(k) }))
      for (const k of keys) {
        if (!fields.find(f => f.field === k)) {
          fields.push({ field: k, title: fieldLabel(k) })
        }
      }
      previewData.value = raw.slice(0, 50)
      const name = '样品资料'
      datasets.value = [...datasets.value.filter(d => d.name !== name), { name, expanded: true, fields }]
      showToast(`已导${fields.length} 个字段`, 'success')
      console.log('[quickImport] 样品资料:', { records: raw.length, fields: fields.length, keys: keys.length, sample: fields[0] })
    } else if (source === 'manufacturer') {
      const resp = await safeFetchJson('/manufacturers?size=5')
      if (resp.ok) {
        const data = resp.data?.data?.records || resp.data?.records || resp.data?.data || resp.data || []
        if (Array.isArray(data) && data.length > 0) {
          const keys = Object.keys(data[0])
          fields = keys.map(k => ({ field: k, title: fieldLabel(k) }))
          previewData.value = data
        }
      }
      if (!fields.length) throw new Error('厂商数据为空')
      const name = '厂商资料'
      datasets.value = [...datasets.value.filter(d => d.name !== name), { name, expanded: true, fields }]
      showToast(`已导${fields.length} 个字段`, 'success')
    } else if (source === 'client-items') {
      const resp = await safeFetchJson('/client-samples/items/sample?size=5')
      if (resp.ok) {
        const data = resp.data?.data || resp.data || []
        if (Array.isArray(data) && data.length > 0) {
          const keys = [...new Set(data.flatMap(r => Object.keys(r)))]
          fields = keys.map(k => ({ field: k, title: fieldLabel(k) }))
          previewData.value = data
        }
      }
      if (!fields.length) throw new Error('择样明细数据为空')
      const name = '择样明细'
      datasets.value = [...datasets.value.filter(d => d.name !== name), { name, expanded: true, fields }]
      showToast(`已导${fields.length} 个字段`, 'success')
    } else if (source === 'customer-info') {
      const resp = await safeFetchJson('/customers?size=5')
      if (resp.ok) {
        const data = resp.data?.data?.records || resp.data?.records || resp.data?.data || resp.data || []
        if (Array.isArray(data) && data.length > 0) {
          const keys = [...new Set(data.flatMap(r => Object.keys(r)))]
          fields = keys.map(k => ({ field: k, title: fieldLabel(k) }))
          previewData.value = data
        }
      }
      if (!fields.length) throw new Error('客户数据为空')
      const name = '客户资料'
      datasets.value = [...datasets.value.filter(d => d.name !== name), { name, expanded: true, fields }]
      showToast(`已导${fields.length} 个字段`, 'success')
    }
  } catch (e) {
    showToast('导入失败: ' + e.message, 'error')
  } finally {
    importing.value = false
  }
}

function onCsvFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  csvFileData = file
  if (!importName.value) importName.value = file.name.replace(/\.csv$/i, '')
}

function resetImportForm() {
  importName.value = ''
  importSql.value = ''
  importApiUrl.value = ''
  importDb.value = 'default'
  csvFileData = null
  if (csvFileRef.value) csvFileRef.value.value = ''
}

async function doImport() {
  if (!importName.value.trim()) { showToast('请输入数据集名称', 'warn'); return }
  importing.value = true
  try {
    if (importTab.value === 'csv') {
      if (!csvFileData) { showToast('请选择 CSV 文件', 'warn'); importing.value = false; return }
      const text = await csvFileData.text()
      const lines = text.trim().split(/\r?\n/)
      if (lines.length < 2) { showToast('CSV 至少需要表头一行数据', 'warn'); importing.value = false; return }
      const parseCsvLine = (line) => {
        const result = []; let current = ''; let inQuote = false
        for (let i = 0; i < line.length; i++) {
          if (inQuote) {
            if (line[i] === '"') { if (i + 1 < line.length && line[i + 1] === '"') { current += '"'; i++ } else { inQuote = false } }
            else { current += line[i] }
          } else {
            if (line[i] === '"') { inQuote = true }
            else if (line[i] === ',') { result.push(current); current = '' }
            else { current += line[i] }
          }
        }
        result.push(current); return result
      }
      const headers = parseCsvLine(lines[0])
      const fields = headers.map(h => ({ field: h.trim(), title: h.trim() }))

      try {
        const token = sessionStorage.getItem('token') || localStorage.getItem('token') || ''
        await fetch('/api/datasets/import', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
          body: JSON.stringify({ name: importName.value.trim(), fields, type: 'csv' })
        })
      } catch (_) {}

      datasets.value = [...datasets.value.filter(d => d.name !== importName.value.trim()), {
        name: importName.value.trim(), expanded: true, fields
      }]
    } else if (importTab.value === 'sql') {
      if (!importSql.value.trim()) { showToast('请输SQL 语句', 'warn'); importing.value = false; return }
      const resp = await safeFetchJson('/api/datasets/execute-sql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: importName.value.trim(), sql: importSql.value.trim(), db: importDb.value })
      })
      if (!resp.ok) throw new Error('SQL 执行失败: ' + resp.status)
      const data = resp.data
      const fields = (data.fields || data.columns || Object.keys(data[0] || {})).map(f => {
        const key = typeof f === 'string' ? f : (f.field || f.name || f)
        const title = typeof f === 'object' ? (f.title || f.comment || f.label || key) : key
        return { field: key, title }
      })
      datasets.value = [...datasets.value.filter(d => d.name !== importName.value.trim()), {
        name: importName.value.trim(), expanded: true, fields
      }]
      if (data.rows || data.length) {
        previewData.value = data.rows || data
      }
    } else if (importTab.value === 'api') {
      if (!importApiUrl.value.trim()) { showToast('请输API 地址', 'warn'); importing.value = false; return }
      const resp = await safeFetchJson(importApiUrl.value.trim())
      if (!resp.ok) throw new Error('请求失败: ' + resp.status)
      const list = resp.data
      const arr = Array.isArray(list) ? list : (list.data || list.rows || list.records || [])
      if (!arr.length) throw new Error('返回数据为空')
      const keys = Object.keys(arr[0])
      const fields = keys.map(k => ({ field: k, title: k }))
      datasets.value = [...datasets.value.filter(d => d.name !== importName.value.trim()), {
        name: importName.value.trim(), expanded: true, fields
      }]
      previewData.value = arr
    }
    showImportModal.value = false
    resetImportForm()
  } catch (e) {
    showToast('导入失败: ' + e.message, 'error')
  } finally {
    importing.value = false
  }
}

function removeDataset(name) {
  datasets.value = datasets.value.filter(d => d.name !== name)
}

// ===== 表格大小调整 =====
function resizeSheet() {
  rowCount.value = Math.max(1, Math.min(200, rowCount.value))
  colCount.value = Math.max(1, Math.min(50, colCount.value))
}

// ===== 预览数据 =====
const previewData = ref([])
const loading = ref(false)
const rawPreviewData = ref([]) // 原始 API 数据

const visibleColumns = computed(() => {
  if (!previewData.value.length) return []
  const row = previewData.value[0] || {}
  return Object.keys(row).map(k => ({ field: k, title: fieldLabel(k) }))
})

// 从模板单元格中提取占位行（支持从指定源数据提取，避免使用被覆盖的 cellData）
function extractTemplateBands(srcData, srcMerges) {
  const src = srcData || cellData
  const cells = []
  // 构建可见性检查：有 srcMerges 时用它，否则用全局 mergeCache
  const isVisible = (r, c) => {
    if (!srcMerges) return shouldRenderCell(r, c)
    const m = srcMerges.find(m => r >= m.sR && r <= m.eR && c >= m.sC && c <= m.eC)
    if (!m) return true
    return m.sR === r && m.sC === c
  }
  for (let r = 1; r <= rowCount.value; r++) {
    for (let c = 1; c <= colCount.value; c++) {
      if (!isVisible(r, c)) continue
      // 从指定源读取值（优先用传入的 srcData，否则读全局 cellData）
      // 合并信息：优先用传入的 srcMerges，否则用全局 getMergeInfo
      let rr = r, cc = c
      if (srcMerges) {
        const m = srcMerges.find(m => r >= m.sR && r <= m.eR && c >= m.sC && c <= m.eC)
        if (m) { rr = m.sR; cc = m.sC }
      } else {
        const mi = getMergeInfo(r, c)
        if (mi) { rr = mi.sR; cc = mi.sC }
      }
      const val = src[`R${rr}C${cc}`]?.v ?? ''
      if (val && val.trim()) {
        cells.push({ r, c, val: val.trim(), fmt: src[`R${rr}C${cc}`]?.fmt ? { ...src[`R${rr}C${cc}`].fmt } : {} })
      }
    }
  }
  if (!cells.length) return null
  cells.sort((a, b) => a.r - b.r || a.c - b.c)
  const minR = cells[0].r, maxR = cells[cells.length - 1].r
  const minC = Math.min(...cells.map(x => x.c)), maxC = Math.max(...cells.map(x => x.c))
  return { cells, minR, maxR, minC, maxC }
}

// cells 计算模板区域划分（首表头/循环/页脚），共享给预览和导出使用
function computeTemplateBands(cells, loopStartOverride) {
  // 全局/meta 字段，不应参与循环起点计算
  const globalFields = ['title', 'logo', 'currentPage', 'page', 'currentDate', 'currentMonth', 'total_pages', 'printTime', 'operatorName']
  const allPlaceholderRows = new Set()
  cells.forEach(c => {
    if (/\$\{/.test(c.val)) {
      // 只把包含非全局占位符的行纳入占位符行集合
      const fields = [...c.val.matchAll(/\$\{(\w+)\}/g)].map(m => m[1])
      const hasNonGlobal = fields.some(f => !globalFields.includes(f))
      if (hasNonGlobal) allPlaceholderRows.add(c.r)
    }
  })
  const autoLoopStart = allPlaceholderRows.size ? Math.min(...allPlaceholderRows) : 0
  const loopStart = loopStartOverride || autoLoopStart
  const sortedCandidate = [...allPlaceholderRows].filter(r => r >= loopStart).sort((a, b) => a - b)

  // 将占位符行按连续性分组（允许中间夹1行非占位符行，如列标题）
  // 取占位符单元格数最多的簇作为数据循环区，小簇归入页眉/页脚 — 避免"客户："等标签误入循环
  const clusters = []
  let cur = []
  for (const r of sortedCandidate) {
    if (cur.length && r - cur[cur.length - 1] > 1) { clusters.push([...cur]); cur = [] }
    cur.push(r)
  }
  if (cur.length) clusters.push([...cur])

  // 按簇内占位符单元格总数排序（而非行数），数据行通常单元格密度远高于信息行/页脚行
  const clusterCellCount = (cluster) => {
    let n = 0
    for (const r of cluster) {
      n += cells.filter(c => c.r === r && /\$\{/.test(c.val)).length
    }
    return n
  }
  let largestCluster = clusters[0] || []
  let maxCount = clusterCellCount(largestCluster)
  for (const cl of clusters) {
    const cnt = clusterCellCount(cl)
    if (cnt > maxCount) { largestCluster = cl; maxCount = cnt }
  }
  const loopRows = new Set(largestCluster)
  const loopBaseR = loopRows.size ? Math.min(...loopRows) : 0
  const loopMaxR = loopRows.size ? Math.max(...loopRows) : 0
  const dataRowSpan = loopRows.size ? loopMaxR - loopBaseR + 1 : 0

  const hdrStart = pageHeaderStartRow.value || 1
  const firstPageRows = new Set()
  for (let r = 1; r < hdrStart; r++) firstPageRows.add(r)
  const firstPageRowCount = firstPageRows.size ? Math.max(...firstPageRows) : 0
  // 每页表头 = hdrStart 到 循环第一行之前的所有行（覆盖可能被小簇占用的行）
  const everyPageHeaderRows = new Set()
  for (let r = hdrStart; r < loopBaseR; r++) everyPageHeaderRows.add(r)
  const everyPageHeaderRowCount = everyPageHeaderRows.size
  const masterRows = new Set([...firstPageRows, ...everyPageHeaderRows])
  const masterRowCount = masterRows.size ? Math.max(...masterRows) : 0
  // 页脚 = 循环最大行之后的所有行（包含非占位符行）
  const trailingCells = cells.filter(c => c.r > loopMaxR)
  const trailingRows = new Set(trailingCells.map(c => c.r))
  const trailingRowCount = trailingRows.size

  console.log('[模板分析] allPlaceholderRows:', [...allPlaceholderRows].sort((a,b)=>a-b), 'clusters:', clusters.map(c=>`[${c[0]}..${c[c.length-1]}](${c.length})`).join(', '), 'largest:', largestCluster.length ? `[${largestCluster[0]}..${largestCluster[largestCluster.length-1]}]` : 'none', 'loopRows:', [...loopRows].sort((a,b)=>a-b), 'firstPageRows:', [...firstPageRows], 'everyPageHeaderRows:', [...everyPageHeaderRows], 'trailingRows:', [...trailingRows])

  return { cells, masterRows, firstPageRows, everyPageHeaderRows, firstPageRowCount, everyPageHeaderRowCount, loopRows, masterRowCount, dataRowSpan, loopBaseR, loopMaxR, trailingCells, trailingRows, trailingRowCount }
}

// 判断是否有模板（单元格中${xxx} 占位符或静态文本）
const hasTemplate = computed(() => {
  for (let r = 1; r <= rowCount.value; r++) {
    for (let c = 1; c <= colCount.value; c++) {
      const val = getCellValue(r, c)
      if (val && val.trim()) return true
    }
  }
  return false
})

const doPreview = async () => {
  loading.value = true
  try {
    let arr = []
    const r1 = await safeFetchJson('/samples?size=50')
    if (r1.ok) {
      arr = r1.data?.data?.records || r1.data?.records || r1.data?.data || []
      if (arr.length === 0 && Array.isArray(r1.data?.data)) arr = r1.data.data
    }
    if (arr.length === 0) {
      const r2 = await safeFetchJson('/samples/vendor-confirm-report')
      if (r2.ok) {
        arr = r2.data?.data || r2.data?.records || []
        if (!Array.isArray(arr) && Array.isArray(r2.data)) arr = r2.data
      }
    }

    const template = extractTemplateBands()
    console.log('[模板提取] 第6-7行 cellData 原始数据:')
    for (let r = 6; r <= 7; r++) {
      for (let c = 1; c <= colCount.value; c++) {
        const v = cellData[`R${r}C${c}`]?.val
        if (v && v.trim()) console.log(`  R${r}C${c} = "${v.trim()}"`)
      }
    }
    const hasPlaceholders = template?.cells.some(c => hasLoopPlaceholder(c.val))

    if (template && hasPlaceholders && arr.length > 0) {
      // 保存设计快照
      previewSnap.value = {
        cellData: JSON.parse(JSON.stringify(cellData)),
        mergedCells: JSON.parse(JSON.stringify(mergedCells.value)),
        rowCount: rowCount.value,
        rowHeights: { ...rowHeights.value },
        colWidths: { ...colWidths.value },
      }

      const { cells } = template
      const bands = computeTemplateBands(cells, dataLoopStartRow.value)

      // 缓存模板信息
      previewTemplate.value = bands
      previewLoopInfo.value = { loopBaseR: bands.loopBaseR, dataRowSpan: bands.dataRowSpan, loopRows: bands.loopRows, masterRowCount: bands.masterRowCount }

      // 总数据条
      rawPreviewData.value = arr
      previewTotal.value = arr.length
      previewPage.value = 1
      computePagePlan(arr)
      // 渲染当前
      renderPreviewPage(arr, 1)

      // 延迟构建导出表格，避免阻塞首次渲
      nextTick(() => {
        const resolveVal = (row, field) => {
          if (row[field] != null) return row[field]
          const sk = camelToSnake(field); if (row[sk] != null) return row[sk]
          const cc = field.replace(/_([a-z])/g, (_, c) => c.toUpperCase()); if (row[cc] != null) return row[cc]
          for (const p of ['sample_', 'vendor_', 'carton_', 'booth_', 'product_', 'company_']) {
            if (field.startsWith(p)) { const sub = field.slice(p.length); if (row[sub] != null) return row[sub] }
          }
          return null
        }
        const tableRows = []
        for (let r = 1; r <= bands.masterRowCount; r++) {
          const rowObj = {}
          template.cells.filter(c => c.r === r).forEach(c => {
            rowObj[colLabel(c.c)] = fillTemplate(c.val, arr[0])
          })
          if (Object.keys(rowObj).length) tableRows.push(rowObj)
        }
        arr.forEach((dataRow) => {
          const rowObj = {}
          template.cells.filter(c => bands.loopRows.has(c.r)).forEach(c => {
            rowObj[colLabel(c.c)] = fillTemplate(c.val, dataRow)
          })
          tableRows.push(rowObj)
        })
        previewData.value = tableRows
      })

      previewMode.value = true
      deselectAllCells()
      nextTick(updateSelOverlay)
      showToast(`已加${arr.length} 条（每页${previewPageSize.value}条，{previewTotalPages.value}页）`, 'success')
    } else {
      previewData.value = arr
      rawPreviewData.value = arr
      if (previewData.value.length === 0) {
        showToast('未获取到数据。请确保已登录且系统中有数据', 'warn')
      }
    }
  } catch (e) {
    console.error(e)
    showToast('加载预览失败: ' + e.message, 'error')
  } finally {
    loading.value = false
  }
}

// 动态分页：根据纸张高度 / 行高 自动计算每页容纳的数据条数
function computePagePlan(arr) {
  const tpl = previewTemplate.value
  if (!arr.length || !tpl) { pagePlan.value = []; return }
  const { firstPageRows, everyPageHeaderRows, trailingRows, loopBaseR, loopMaxR } = tpl
  // 纸张可用高度（mm）- 边距
  const dim = paperDims[paperSize.value]
  const phMm = paperOrient.value === 'landscape' ? dim.w : dim.h
  const marginMm = 20 // CSS @page margin:10mm (上下各10)
  const usablePx = (phMm - marginMm) * 3.78 // mm → px（96dpi）
  // 各区域高度
  const snapRH = previewSnap.value?.rowHeights || {}
  const sumH = (rowSet) => {
    let h = 0; for (const r of rowSet) h += snapRH[r] || defaultRowHeight.value; return h
  }
  const firstHdrH = sumH(firstPageRows) + sumH(everyPageHeaderRows)
  const normalHdrH = sumH(everyPageHeaderRows)
  const footerH = sumH(trailingRows)
  let dataRowH = defaultRowHeight.value
  for (let r = loopBaseR; r <= loopMaxR; r++) { const h = snapRH[r]; if (h && h > dataRowH) dataRowH = h }
  // 每页容量（至少1条）
  const firstCap = Math.max(1, Math.floor((usablePx - firstHdrH - footerH) / dataRowH))
  const normalCap = Math.max(1, Math.floor((usablePx - normalHdrH) / dataRowH))
  const lastCap = Math.max(1, Math.floor((usablePx - normalHdrH - footerH) / dataRowH))
  // 构建计划
  const plan = []
  let idx = 0
  while (idx < arr.length) {
    const isFirst = plan.length === 0
    const remaining = arr.length - idx
    if (isFirst) {
      const take = Math.min(remaining, firstCap)
      plan.push({ start: idx, end: idx + take - 1, isFirst: true, isLast: take >= remaining })
      idx += take
    } else {
      const isFinal = idx + normalCap >= arr.length
      const cap = isFinal ? lastCap : normalCap
      const take = Math.min(remaining, cap)
      plan.push({ start: idx, end: idx + take - 1, isFirst: false, isLast: take >= remaining })
      idx += take
    }
  }
  if (plan.length) plan[plan.length - 1].isLast = true
  pagePlan.value = plan
}

// 渲染当前预览页（批量写入 cellData，避免逐格触发响应式卡死）
function renderPreviewPage(arr, page) {
  const tpl = previewTemplate.value
  if (!tpl) return
  const { cells, masterRows, firstPageRows, everyPageHeaderRows, firstPageRowCount, everyPageHeaderRowCount, loopRows, masterRowCount, dataRowSpan, loopBaseR, trailingCells, trailingRows, trailingRowCount } = tpl
  // 从动态分页计划取本页信息（兜底用固定 pageSize）
  const plan = pagePlan.value.length ? pagePlan.value[page - 1] : null
  const isFirstPage = plan?.isFirst ?? (page === 1)
  const isLastPage = plan?.isLast ?? (page >= Math.ceil(arr.length / previewPageSize.value))
  const startIdx = plan?.start ?? ((page - 1) * previewPageSize.value)
  const endIdx = plan?.end ?? (Math.min(startIdx + previewPageSize.value, arr.length) - 1)

  const batch = {}

  // 清除上一页残留数
  for (const k of Object.keys(cellData)) {
    if (/^R\d+C\d+$/.test(k)) delete cellData[k]
  }
  clearRowHeights()
  // 保存原始合并定义（从设计快照读取，不受翻页污染）
  const originalMerges = previewSnap.value?.mergedCells || []
  const snapRowHeights = previewSnap.value?.rowHeights || {}
  mergedCells.value = []
  rebuildMergeCache()

  // 恢复所有主表行的设计行高（从设计快照读取，不受翻页污染
  const allMasterRows = new Set([...masterRows, ...firstPageRows, ...everyPageHeaderRows])
  for (const [r, h] of Object.entries(snapRowHeights)) {
    const rn = parseInt(r)
    if (!allMasterRows.has(rn)) continue
    // 首页专属行：保持原行号；每页表头行：首页保持原行号，非首页从 1 开
    if (firstPageRows.has(rn) && !isFirstPage) continue  // 非首页不显示首页专属行
    const targetR = (everyPageHeaderRows.has(rn) && !isFirstPage) ? (rn - Math.min(...everyPageHeaderRows) + 1) : rn
    rowHeights.value[targetR] = h
  }

  // 辅助：从模板 cell fmt 写入batch dstKey
  const copyFmt = (dstKey, cell) => {
    const sf = cell.fmt
    if (sf && Object.keys(sf).length) {
      if (!batch[dstKey]) batch[dstKey] = {}
      if (!batch[dstKey].fmt) batch[dstKey].fmt = {}
      Object.assign(batch[dstKey].fmt, sf)
    }
  }

  // 1. 首页专属行（仅首页）
  let logoRendered = false
  let titleRendered = false
  if (isFirstPage && firstPageRows.size) {
    cells.filter(c => firstPageRows.has(c.r)).forEach(c => {
      let val = c.val
      if (val.includes('${logo}') && logoRendered) val = ''
      if (val.includes('${title}') && titleRendered) val = ''
      const filled = fillTemplate(val, arr[0])
      if (c.val.includes('${logo}') && !logoRendered) logoRendered = true
      if (c.val.includes('${title}') && !titleRendered) titleRendered = true
      const key = `R${c.r}C${c.c}`
      if (!batch[key]) batch[key] = { v: filled }
      else batch[key].v = filled
      copyFmt(key, c)
    })
  }
  // 1b. 每页表头行（非首页从行开始，首页保持原位
  if (everyPageHeaderRows.size) {
    const hdrMinR = Math.min(...everyPageHeaderRows)
    cells.filter(c => everyPageHeaderRows.has(c.r)).forEach(c => {
      const r = isFirstPage ? c.r : (c.r - hdrMinR + 1)
      let val = c.val
      if (val.includes('${logo}') && logoRendered) val = ''
      if (val.includes('${title}') && titleRendered) val = ''
      const filled = fillTemplate(val, arr[0])
      if (c.val.includes('${logo}') && !logoRendered) logoRendered = true
      if (c.val.includes('${title}') && !titleRendered) titleRendered = true
      const key = `R${r}C${c.c}`
      if (!batch[key]) batch[key] = { v: filled }
      else batch[key].v = filled
      copyFmt(key, c)
    })
  }
  // 1c. 兼容旧版：无 pageHeaderStartRow 时，所masterRows 正常渲染
  if (!firstPageRows.size && !everyPageHeaderRows.size) {
    cells.filter(c => masterRows.has(c.r)).forEach(c => {
      let val = c.val
      if (val.includes('${logo}') && logoRendered) val = ''
      if (val.includes('${title}') && titleRendered) val = ''
      const filled = fillTemplate(val, arr[0])
      if (c.val.includes('${logo}') && !logoRendered) logoRendered = true
      if (c.val.includes('${title}') && !titleRendered) titleRendered = true
      const key = `R${c.r}C${c.c}`
      if (!batch[key]) batch[key] = { v: filled }
      else batch[key].v = filled
      copyFmt(key, c)
    })
  }

  // 2. 行数 = 首页专属(仅首 + 每页表头 + 本页数据 + 页脚
  const headerRowCount = isFirstPage ? masterRowCount : everyPageHeaderRowCount
  const pageItems = arr.slice(startIdx, endIdx + 1)
  const needed = headerRowCount + pageItems.length * dataRowSpan + (isLastPage ? trailingRowCount : 0)
  rowCount.value = Math.max(needed, headerRowCount + 1)
  previewRowCount.value = needed

  // 3. 本页数据
  pageItems.forEach((dataRow, di) => {
    const baseR = headerRowCount + di * dataRowSpan + 1
    const rowOffset = baseR - loopBaseR
    cells.filter(c => loopRows.has(c.r)).forEach(c => {
      const offsetR = c.r + rowOffset
      let val = c.val
      if (val.includes('${logo}') && logoRendered) val = ''
      if (val.includes('${title}') && titleRendered) val = ''
      const filled = fillTemplate(val, dataRow)
      if (c.val.includes('${logo}') && !logoRendered) logoRendered = true
      if (c.val.includes('${title}') && !titleRendered) titleRendered = true
      const dstKey = `R${offsetR}C${c.c}`
      if (!batch[dstKey]) batch[dstKey] = { v: filled }
      else batch[dstKey].v = filled
      copyFmt(dstKey, c)
    })
    for (let tr = loopBaseR; tr < loopBaseR + dataRowSpan; tr++) {
      const offsetR = tr + rowOffset
      if (snapRowHeights[tr] != null) rowHeights.value[offsetR] = snapRowHeights[tr]
    }
  })

  // 4. 页脚行（仅最后一页）
  const footBaseR = headerRowCount + pageItems.length * dataRowSpan + 1
  const trailingMinR = trailingRows.size ? Math.min(...trailingRows) : 0
  if (isLastPage && trailingCells.length) {
    const totalPages = pagePlan.value.length
    const opName = authState.userInfo?.realName || authState.userInfo?.username || arr[0]?.operator_name || ''
    const footerData = { ...arr[0], operatorName: opName, printTime: new Date().toLocaleString('zh-CN'), currentPage: page, page: totalPages, total_pages: totalPages, title: config.title, logo: config.logoImage }
    trailingCells.forEach(c => {
      const offsetR = footBaseR + (c.r - trailingMinR)
      const filled = fillTemplate(c.val, footerData)
      const dstKey = `R${offsetR}C${c.c}`
      if (!batch[dstKey]) batch[dstKey] = { v: filled }
      else batch[dstKey].v = filled
      copyFmt(dstKey, c)
      if (snapRowHeights[c.r] != null) rowHeights.value[offsetR] = snapRowHeights[c.r]
    })
  }

  // 5. 恢复合并单元格（重映射行号到预览位置：表头区 + 最后一页页脚区
  const hdrMinR = everyPageHeaderRows.size ? Math.min(...everyPageHeaderRows) : 1
  for (const m of originalMerges) {
    if (isFirstPage) {
      if (masterRows.has(m.sR) && masterRows.has(m.eR)) {
        mergedCells.value.push({ ...m })
      } else if (isLastPage && trailingRows.has(m.sR) && trailingRows.has(m.eR)) {
        mergedCells.value.push({
          ...m,
          sR: footBaseR + (m.sR - trailingMinR),
          eR: footBaseR + (m.eR - trailingMinR),
        })
      }
    } else {
      if (everyPageHeaderRows.has(m.sR) && everyPageHeaderRows.has(m.eR)) {
        const offset = 1 - hdrMinR
        mergedCells.value.push({
          ...m,
          sR: m.sR + offset,
          eR: m.eR + offset,
        })
      } else if (isLastPage && trailingRows.has(m.sR) && trailingRows.has(m.eR)) {
        mergedCells.value.push({
          ...m,
          sR: footBaseR + (m.sR - trailingMinR),
          eR: footBaseR + (m.eR - trailingMinR),
        })
      }
    }
  }
  rebuildMergeCache()

  // 被合并完全覆盖或完全没有单元格数据的行设最小高度，避免 logo 底部多余空白
  for (let r = 1; r <= rowCount.value; r++) {
    let allCovered = true
    for (let c = 1; c <= colCount.value; c++) {
      const m = getMergeInfo(r, c)
      if (!m) { allCovered = false; break }
      // 是某合并的起始格，说明该行有独立内容
      if (m.isStart) { allCovered = false; break }
    }
    // 补充检查：行中是否有任何有效单元格数据
    if (!allCovered) {
      let hasAnyCell = false
      for (let c = 1; c <= colCount.value; c++) {
        const key = `R${r}C${c}`
        if (batch[key] && (batch[key].v != null && batch[key].v !== '')) { hasAnyCell = true; break }
      }
      if (!hasAnyCell) allCovered = true
    }
    if (allCovered) rowHeights.value[r] = 0
  }

  Object.assign(cellData, batch)
}

// 翻页
function goPreviewPage(page) {
  const arr = rawPreviewData.value
  if (!arr.length) return
  const total = pagePlan.value.length || Math.ceil(arr.length / previewPageSize.value)
  if (page < 1 || page > total) return
  previewPage.value = page
  renderPreviewPage(arr, page)
}

// 退出预览，恢复设计态
function exitPreview() {
  if (viewOnly.value) return  // viewOnly 模式不允许退回设计态
  if (!previewSnap.value) { previewMode.value = false; return }
  const snap = previewSnap.value
  // 恢复 cellData
  Object.keys(cellData).forEach(k => delete cellData[k])
  Object.assign(cellData, snap.cellData)
  // 恢复合并单元
  mergedCells.value = snap.mergedCells
  // 恢复行数
  rowCount.value = snap.rowCount
  // 恢复行高和列
  rowHeights.value = snap.rowHeights || {}
  if (snap.colWidths) Object.assign(colWidths.value, snap.colWidths)
  previewMode.value = false
  previewSnap.value = null
  previewRowCount.value = 0
  deselectAllCells()
  nextTick(updateSelOverlay)
}

// viewOnly 关闭：有 opener 则 close，否则直接导航到空白
function closeViewOnly() {
  if (window.opener) {
    window.close()
  } else {
    window.location.replace('about:blank')
  }
}


// ===== 草稿=====
const currentDraftId = ref(null)
const currentTemplateId = ref(null)  // 当前加载的服务端模板ID，保存时自动覆盖
const drafts = ref([])
const showDraftModal = ref(false)

// ===== 模板管理 =====
const showTemplateModal = ref(false)
const templateList = ref([])
const templateLoading = ref(false)
const templateTypeFilter = ref('')
const draftTitleInput = ref('')

// ===== 模板锁定管理（锁信息存储在服务端 templateData.__lock_pwd 中） =====
// 响应式锁状态: { templateId -> lockHash }
const templateLockMap = ref({})
// 本次会话中临时解锁的模板及其锁哈希（关闭页面时自动恢复）
const sessionUnlocked = new Map()
// 当前加载模板的锁哈希（用于保存时保留锁）
let currentTemplateLockHash = null

let currentTemplateType = '' // 当前编辑模板的类型

function isTemplateLocked(templateId) {
  return !!templateLockMap.value[templateId]
}
function getTemplateLockHash(templateId) {
  return templateLockMap.value[templateId] || null
}
// 从 templateData JSON 字符串中提取锁哈希
function parseLockFromData(templateDataStr) {
  try {
    const data = JSON.parse(templateDataStr)
    return data?.__lock_pwd || null
  } catch { return null }
}

// 简单哈希
function simpleHash(s) {
  let h = 0
  for (let i = 0; i < s.length; i++) {
    h = ((h << 5) - h) + s.charCodeAt(i); h |= 0
  }
  return String(Math.abs(h))
}
function verifyLockPassword(templateId, password) {
  const hash = templateLockMap.value[templateId]
  if (!hash) return true
  return simpleHash(password) === hash
}

// 通过 PUT 更新模板的 templateData（仅修改锁字段，保留其他数据）
async function persistLockChange(templateId, lockHashOrNull) {
  try {
    // 1. 获取完整模板
    const resp = await api('/report-templates/' + templateId)
    if (resp.code !== 200 || !resp.data?.templateData) {
      showToast('获取模板数据失败', 'error'); return false
    }
    const tpl = resp.data
    const data = JSON.parse(tpl.templateData)
    if (lockHashOrNull) {
      data.__lock_pwd = lockHashOrNull
    } else {
      delete data.__lock_pwd
    }
    // 2. PUT 更新
    const putResp = await api('/report-templates/' + templateId, {
      method: 'PUT',
      body: JSON.stringify({ title: tpl.title, description: tpl.description || '', templateData: JSON.stringify(data) })
    })
    if (putResp.code === 200) {
      // 3. 更新本地锁映射
      const newMap = { ...templateLockMap.value }
      if (lockHashOrNull) {
        newMap[templateId] = lockHashOrNull
      } else {
        delete newMap[templateId]
      }
      templateLockMap.value = newMap
      return true
    } else {
      showToast('操作失败: ' + (putResp.message || '未知错误'), 'error')
      return false
    }
  } catch (e) {
    showToast('操作失败: ' + e.message, 'error')
    return false
  }
}

// 密码弹窗状态
const lockDialog = reactive({
  show: false, title: '', hint: '', password: '', error: '',
  action: null
})

function lockDialogConfirm() {
  const pw = lockDialog.password.trim()
  if (!pw) { lockDialog.error = '请输入密码'; return }
  lockDialog.error = ''
  if (lockDialog.action) lockDialog.action(pw)
  lockDialog.show = false
  lockDialog.password = ''
}

// 模板命名弹窗
const nameDialog = reactive({
  show: false, name: '', type: '', error: '', onSave: null
})

function nameDialogConfirm() {
  const name = nameDialog.name.trim()
  if (!name) { nameDialog.error = '请输入模板名称'; return }
  nameDialog.error = ''
  if (nameDialog.onSave) nameDialog.onSave(name)
  nameDialog.show = false
  nameDialog.name = ''
}

// ===== 通用确认弹窗 =====
const confirmDialog = reactive({
  show: false, message: '', confirmText: '确定', cancelText: '取消', danger: false, callback: null
})
function showConfirm(message, callback, { danger = false, confirmText = '确定', cancelText = '取消' } = {}) {
  confirmDialog.message = message
  confirmDialog.callback = callback
  confirmDialog.danger = danger
  confirmDialog.confirmText = confirmText
  confirmDialog.cancelText = cancelText
  confirmDialog.show = true
}

// 切换模板锁定状态（上锁/解锁）
async function toggleTemplateLock(tpl, e) {
  e.stopPropagation()
  const tid = tpl.id
  if (isTemplateLocked(tid)) {
    // 解锁
    lockDialog.title = '解锁模板'
    lockDialog.hint = `"${tpl.title}" 已锁定，请输入密码解锁`
    lockDialog.password = ''
    lockDialog.error = ''
    lockDialog.action = async (pw) => {
      if (verifyLockPassword(tid, pw)) {
        const hash = templateLockMap.value[tid]
        const ok = await persistLockChange(tid, null)
        if (ok) { sessionUnlocked.set(tid, hash); showTemplateModal.value = false; loadTemplateById(tid); showToast('模板已加载', 'success') }
      } else {
        showToast('密码错误', 'error')
      }
    }
    lockDialog.show = true
  } else {
    // 上锁
    lockDialog.title = '锁定模板'
    lockDialog.hint = `为 "${tpl.title}" 设置锁定密码（6位以上）`
    lockDialog.password = ''
    lockDialog.error = ''
    lockDialog.action = async (pw) => {
      if (pw.length < 6) { showToast('密码至少6位', 'error'); return }
      const ok = await persistLockChange(tid, simpleHash(pw))
      if (ok) { sessionUnlocked.delete(tid); showToast('模板已锁定', 'success') }
    }
    lockDialog.show = true
  }
  nextTick(() => {
    const input = document.querySelector('.modal-card input[type="password"]')
    if (input) input.focus()
  })
}

// 加载模板（带锁检查）
function loadTemplateClick(tpl) {
  if (isTemplateLocked(tpl.id)) {
    lockDialog.title = '模板已锁定'
    lockDialog.hint = `"${tpl.title}" 已锁定，请输入密码`
    lockDialog.password = ''
    lockDialog.error = ''
    lockDialog.action = (pw) => {
      if (verifyLockPassword(tpl.id, pw)) {
        loadTemplateById(tpl.id)
      } else {
        showToast('密码错误', 'error')
      }
    }
    lockDialog.show = true
    nextTick(() => {
      const input = document.querySelector('.modal-card input[type="password"]')
      if (input) input.focus()
    })
  } else {
    loadTemplateById(tpl.id)
  }
}
const DRAFTS_KEY = 'report_drafts'

function getDrafts() {
  try {
    const raw = JSON.parse(localStorage.getItem(DRAFTS_KEY) || '[]')
    // 清理超过12小时的草稿（timestamp 字段存的是 Date.now()）
    const cutoff = Date.now() - 12 * 60 * 60 * 1000
    const valid = raw.filter(d => d.timestamp && d.timestamp > cutoff)
    if (valid.length !== raw.length) {
      localStorage.setItem(DRAFTS_KEY, JSON.stringify(valid))
    }
    return valid
  } catch { return [] }
}

function saveDrafts(list) {
  localStorage.setItem(DRAFTS_KEY, JSON.stringify(list))
}

function buildDraftData() {
  const data = {
    config: { ...config },
    printConfig: { ...printConfig },
    rowCount: rowCount.value,
    colCount: colCount.value,
    paperSize: paperSize.value,
    paperOrient: paperOrient.value,
    cellData: JSON.parse(JSON.stringify(cellData)),
    mergedCells: JSON.parse(JSON.stringify(mergedCells.value)),
    datasets: JSON.parse(JSON.stringify(datasets.value)),
    colWidths: JSON.parse(JSON.stringify(colWidths.value)),
    rowHeights: JSON.parse(JSON.stringify(rowHeights.value)),
    defaultColWidth: defaultColWidth.value,
    defaultRowHeight: defaultRowHeight.value,
    dataLoopStartRow: dataLoopStartRow.value,
    pageHeaderStartRow: pageHeaderStartRow.value,
  }
  // 如果当前模板已锁定，保留锁哈希
  if (currentTemplateLockHash) {
    data.__lock_pwd = currentTemplateLockHash
  }
  return data
}

function applyDraftData(d) {
  if (d.config) Object.assign(config, d.config)
  if (d.printConfig) Object.assign(printConfig, d.printConfig)
  if (d.rowCount) rowCount.value = d.rowCount
  if (d.colCount) colCount.value = d.colCount
  if (d.paperSize) paperSize.value = d.paperSize
  if (d.paperOrient) paperOrient.value = d.paperOrient
  if (d.cellData) {
    Object.keys(cellData).forEach(k => delete cellData[k])
    Object.assign(cellData, d.cellData)
  }
  if (d.mergedCells) mergedCells.value = d.mergedCells
  if (d.datasets?.length) {
    // 内置数据集始终使用最新代码定义，自定义数据集从草稿恢复
    const builtinNames = ['样品资料', '厂商资料', '择样明细', '客户资料', '组合字段']
    const builtinDefaults = {
      '样品资料': datasets.value.find(ds => ds.name === '样品资料'),
      '厂商资料': datasets.value.find(ds => ds.name === '厂商资料'),
      '择样明细': datasets.value.find(ds => ds.name === '择样明细'),
      '客户资料': datasets.value.find(ds => ds.name === '客户资料'),
    }
    const restored = d.datasets.filter(ds => !builtinNames.includes(ds.name))
    datasets.value = [
      ...restored,
      ...Object.values(builtinDefaults).filter(Boolean),
      { name: '组合字段', expanded: true, fields: compositeFields },
    ]
  }
  if (d.colWidths) colWidths.value = d.colWidths
  if (d.rowHeights) rowHeights.value = d.rowHeights
  if (d.defaultColWidth) defaultColWidth.value = d.defaultColWidth
  if (d.defaultRowHeight) defaultRowHeight.value = d.defaultRowHeight
  if (d.dataLoopStartRow != null) dataLoopStartRow.value = d.dataLoopStartRow
  if (d.pageHeaderStartRow != null) pageHeaderStartRow.value = d.pageHeaderStartRow
  rebuildRowWordWrapCache()
}

// 保存当前草稿
function saveConfig() {
  try {
    const data = buildDraftData()
    const all = getDrafts()
    const now = new Date().toLocaleString('zh-CN')
    const ts = Date.now()
    if (currentDraftId.value) {
      const idx = all.findIndex(d => d.id === currentDraftId.value)
      if (idx >= 0) {
        all[idx].title = config.title || '未命名报'
        all[idx].data = data
        all[idx].updatedAt = now
        all[idx].timestamp = ts
      }
    } else {
      const id = 'draft_' + Date.now()
      currentDraftId.value = id
      all.push({
        id,
        title: config.title || '未命名报',
        createdAt: now,
        updatedAt: now,
        timestamp: ts,
        data,
      })
    }
    saveDrafts(all)
    drafts.value = all
    // 同时保存当前选中草稿 ID
    localStorage.setItem('report_current_draft', currentDraftId.value)
    showToast('已保存到草稿箱', 'success')
  } catch (e) {
    showToast('保存失败: ' + e.message, 'error')
  }
}

// 保存模板：加载过的模板覆盖更新，否则新建
async function saveAsTemplate() {
  const tid = currentTemplateId.value
  // 更新已锁定的模板需要验证密码
  if (tid && isTemplateLocked(tid)) {
    lockDialog.title = '模板已锁定'
    lockDialog.hint = `更新 "${config.title}" 需要验证密码`
    lockDialog.password = ''
    lockDialog.error = ''
    lockDialog.action = (pw) => {
      if (verifyLockPassword(tid, pw)) {
        doSaveAsTemplate()
      } else {
        showToast('密码错误', 'error')
      }
    }
    lockDialog.show = true
    nextTick(() => {
      const input = document.querySelector('.modal-card input[type="password"]')
      if (input) input.focus()
    })
    return
  }
  doSaveAsTemplate()
}
async function doSaveAsTemplate() {
  // 新建模板或无标题时需要先命名 + 选类型
  if (!config.title.trim() && !currentTemplateId.value) {
    nameDialog.name = config.title.trim()
    nameDialog.type = currentTemplateType
    nameDialog.error = ''
    nameDialog.onSave = (name) => {
      config.title = name
      currentTemplateType = nameDialog.type
      doSaveAsTemplate()
    }
    nameDialog.show = true
    nextTick(() => {
      const input = document.querySelector('.modal-card input[type="text"]')
      if (input) input.focus()
    })
    return
  }
  try {
    const title = config.title.trim()
    const templateType = nameDialog.type || currentTemplateType
    const data = buildDraftData()
    const tid = currentTemplateId.value
    const resp = tid
      ? await api('/report-templates/' + tid, {
          method: 'PUT',
          body: JSON.stringify({ title, description: '', type: templateType || '', templateData: JSON.stringify(data) })
        })
      : await api('/report-templates', {
          method: 'POST',
          body: JSON.stringify({ title, description: '', type: templateType || '', templateData: JSON.stringify(data) })
        })
    if (resp.code === 200) {
      if (!tid && resp.data?.id) {
        currentTemplateId.value = resp.data.id
      }
      currentTemplateType = templateType
      showToast(tid ? '模板已更新' : '模板已保存', 'success')
    } else {
      showToast('保存失败: ' + (resp.message || '未知错误'), 'error')
    }
  } catch (e) {
    showToast('保存失败: ' + e.message, 'error')
  }
}

/** 加载服务端模板列表，同时解析锁信息 */
async function loadTemplatesFromServer() {
  templateLoading.value = true
  try {
    const typeParam = templateTypeFilter.value ? '?type=' + templateTypeFilter.value : ''
    const resp = await api('/report-templates/all' + typeParam)
    if (resp.code === 200) {
      templateList.value = resp.data || []
      // 从每个模板的 templateData 中提取锁信息
      const newLockMap = { ...templateLockMap.value }
      for (const tpl of templateList.value) {
        if (tpl.templateData) {
          const lockHash = parseLockFromData(tpl.templateData)
          if (lockHash) {
            newLockMap[tpl.id] = lockHash
          } else if (newLockMap[tpl.id]) {
            delete newLockMap[tpl.id]
          }
        }
      }
      templateLockMap.value = newLockMap
    } else {
      templateList.value = []
    }
  } catch (e) {
    console.error('加载模板列表失败:', e)
    templateList.value = []
  } finally {
    templateLoading.value = false
  }
}

/** 打开模板列表 */
async function openTemplateList() {
  showTemplateModal.value = true
  await loadTemplatesFromServer()
}

/** 根据ID加载模板 */
async function loadTemplateById(id) {
  try {
    const resp = await api('/report-templates/' + id)
    if (resp.code === 200 && resp.data?.templateData) {
      const tpl = resp.data
      const data = JSON.parse(tpl.templateData)
      // 提取并保留锁信息，然后从数据中移除 __lock_pwd 再应用
      if (data.__lock_pwd) {
        currentTemplateLockHash = data.__lock_pwd
        // 更新锁映射
        const newMap = { ...templateLockMap.value }
        newMap[tpl.id] = data.__lock_pwd
        templateLockMap.value = newMap
        delete data.__lock_pwd
      } else {
        currentTemplateLockHash = null
      }
      applyDraftData(data)
      config.title = tpl.title || config.title
      currentTemplateId.value = tpl.id
      currentTemplateType = tpl.type || ''
      showTemplateModal.value = false
      selectCell(1, 1)
      showToast('已加载模板: ' + tpl.title, 'success')
    } else {
      showToast('模板加载失败', 'error')
    }
  } catch (e) {
    showToast('模板加载失败: ' + e.message, 'error')
  }
}

/** 删除模板 */
async function deleteTemplateClick(id, e) {
  e.stopPropagation()
  if (isTemplateLocked(id)) {
    const tpl = templateList.value.find(t => t.id === id)
    lockDialog.title = '模板已锁定'
    lockDialog.hint = `删除 "${tpl?.title || id}" 需要验证密码`
    lockDialog.password = ''
    lockDialog.error = ''
    lockDialog.action = (pw) => {
      if (verifyLockPassword(id, pw)) {
        doDeleteTemplate(id)
      } else {
        showToast('密码错误', 'error')
      }
    }
    lockDialog.show = true
    nextTick(() => {
      const input = document.querySelector('.modal-card input[type="password"]')
      if (input) input.focus()
    })
    return
  }
  showConfirm('确定删除此模板？此操作不可恢复。', () => { doDeleteTemplate(id) }, { danger: true, confirmText: '删除' })
}
async function doDeleteTemplate(id) {
  try {
    const resp = await api('/report-templates/' + id, { method: 'DELETE' })
    if (resp.code === 200) {
      templateList.value = templateList.value.filter(t => t.id !== id)
      showToast('模板已删除', 'success')
    } else {
      showToast('删除失败: ' + (resp.message || '未知错误'), 'error')
    }
  } catch (err) {
    showToast('删除失败: ' + err.message, 'error')
  }
}

// 另存为新草稿
function saveAsNewDraft() {
  try {
    const title = draftTitleInput.value.trim() || config.title || '未命名报'
    const data = buildDraftData()
    const all = getDrafts()
    const id = 'draft_' + Date.now()
    const now = new Date().toLocaleString('zh-CN')
    all.push({ id, title, createdAt: now, updatedAt: now, data })
    saveDrafts(all)
    drafts.value = all
    currentDraftId.value = id
    localStorage.setItem('report_current_draft', id)
    config.title = title
    showDraftModal.value = false
    showToast('已另存为: ' + title, 'success')
  } catch (e) {
    showToast('保存失败: ' + e.message, 'error')
    }
  }

// 加载草稿
function loadDraft(draft) {
  if (!draft?.data) return
  applyDraftData(draft.data)
  currentDraftId.value = draft.id
  currentTemplateId.value = null
  currentTemplateLockHash = null
  config.title = draft.title || '未命名报'
  localStorage.setItem('report_current_draft', draft.id)
  showDraftModal.value = false
  selectCell(1, 1)
}

// 删除草稿
function deleteDraft(id, e) {
  e.stopPropagation()
  showConfirm('确定删除此草稿？', () => {
    const all = getDrafts().filter(d => d.id !== id)
    saveDrafts(all)
    drafts.value = all
    if (currentDraftId.value === id) {
      currentDraftId.value = null
      localStorage.removeItem('report_current_draft')
    }
  }, { danger: true, confirmText: '删除' })
}

// 打开草稿箱
function openDrafts() {
  drafts.value = getDrafts()
  showDraftModal.value = true
}

function onToolbarNew() {
  if (Object.keys(cellData).length > 0) {
    showConfirm('当前内容未保存，确定新建？', () => { resetToNew() })
    return
  }
  resetToNew()
}
function resetToNew() {
  Object.keys(cellData).forEach(k => delete cellData[k])
  mergedCells.value = []
  deselectAllCells()
  rowWordWrapCache = {}
  printConfig.reportTitle = ''
  printConfig.companyName = ''
  config.title = ''
  currentDraftId.value = null
  currentTemplateId.value = null
  currentTemplateType = ''
  currentTemplateLockHash = null
  localStorage.removeItem('report_current_draft')
}

// ===== 关闭前保存提=====
function onBeforeUnload(e) {
  forceSaveDraft()
}

// 页面隐藏/切换标签时保存（beforeunload 更可靠）
function onVisibilityChange() {
  if (document.hidden) forceSaveDraft()
}

// pagehide 作为最后一道防线
function onPageHide() { forceSaveDraft(); restoreUnlockedLocks() }

// 关闭页面前自动恢复所有临时解锁的模板
async function restoreUnlockedLocks() {
  if (!sessionUnlocked.size) return
  for (const [tid, hash] of sessionUnlocked) {
    try {
      await persistLockChange(tid, hash)
    } catch (_) {}
  }
}

// 强制保存草稿（各处共用，避免静默失败）
function forceSaveDraft() {
  if (previewMode.value) return
  if (Object.keys(cellData).length === 0) return
  try {
    const data = buildDraftData()
    const all = getDrafts()
    const id = currentDraftId.value || 'draft_' + Date.now()
    const now = new Date().toLocaleString('zh-CN')
    const idx = all.findIndex(d => d.id === id)
    if (idx >= 0) {
      all[idx].data = data
      all[idx].updatedAt = now
    } else {
      all.push({ id, title: config.title || '自动保存', createdAt: now, updatedAt: now, data })
    }
    saveDrafts(all)
    currentDraftId.value = id
    localStorage.setItem('report_current_draft', id)
  } catch (e) {
    console.error('草稿保存失败:', e)
  }
}

const doPrint = () => {
  if (viewOnly.value) {
    const html = buildPrintHtml()
    if (!html) { showToast('无数据可打印', 'warn'); return }
    const w = window.open('', '_blank', 'width=900,height=700')
    w.document.write(html)
    w.document.close()
    setTimeout(() => w.print(), 500)
    return
  }
  if (!previewMode.value) {
    showToast('请先点击"加载预览"获取数据后再打印', 'warn')
    return
  }
  const arr = rawPreviewData.value
  if (!arr.length) { showToast('没有数据可打印', 'warn'); return }
  const tpl = previewTemplate.value
  if (!tpl) { window.print(); return }

  // 统一使用 buildPrintHtml 生成含完整格式的 HTML
  const printHtml = buildPrintHtml()
  if (!printHtml) { showToast('无数据可打印', 'warn'); return }
  const w = window.open('', '_blank', 'width=900,height=700')
  w.document.write(printHtml)
  w.document.close()
  setTimeout(() => w.print(), 500)
}

// ===== 导出功能 =====

/** 构建完整报表数据（含标题数据脚注行，保留模板结构*/
function buildFullReportData() {
  const template = extractTemplateBands()
  if (!template) return previewData.value.map(row => ({ ...row }))
  const { cells } = template
  const maxR = Math.max(...cells.map(c => c.r))
  const rows = []
  for (let r = 1; r <= maxR; r++) {
    const rowCells = cells.filter(c => c.r === r)
    if (!rowCells.length) continue
    const rowObj = {}
    rowCells.forEach(rc => {
      // 预览模式getCellValue 返回已填充的值，设计模式下返回占位符
      rowObj[rc.c] = getCellValue(r, rc.c)
    })
    rows.push({ r, cols: rowObj, hasPlaceholder: rowCells.some(c => /\$\{/.test(c.val)) })
  }
  return rows
}

/** 导出 CSV */
const doExport = () => {
  const data = previewData.value
  if (!data.length) { showToast('请先加载预览数据', 'warn'); return }
  const headers = visibleColumns.value.map(c => c.title)
  const ws = [headers, ...data.map(row => visibleColumns.value.map(c => row[c.field] ?? ''))]
  const csv = ws.map(r => r.map(v => '"' + String(v ?? '').replace(/"/g, '""') + '"').join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  downloadBlob(blob, config.title + '.csv')
  showToast('CSV 导出成功', 'success')
}

/** 导出 Excel（HTML Table .xls，无需额外库） */
const doExportExcel = () => {
  const data = previewData.value
  if (!data.length) { showToast('请先加载预览数据', 'warn'); return }
  const headers = visibleColumns.value.map(c => c.title)
  const fs = config.fontSize || 24
  const ff = (config.fontFamily || 'SimSun').split(',')[0].trim()
  const fc = config.fontColor || '#333333'
  const bold = config.bold ? 'font-weight:bold;' : ''
  let html = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel">'
    + '<head><meta charset="UTF-8"><style>'
    + `table{border-collapse:collapse;}td{padding:8px 16px;font-size:${fs}px;font-family:${ff},serif;color:${fc};border:none;${bold}}`
    + '.th{background:#f0f0f0;font-weight:bold;text-align:center;}'
    + '</style></head><body>'
    + '<table>'
    // 表头
    + '<tr>' + headers.map(h => `<td class="th">${escHtml(h)}</td>`).join('') + '</tr>'
    // 数据
    + data.map(row =>
      '<tr>' + visibleColumns.value.map(c => `<td>${escHtml(row[c.field] ?? '')}</td>`).join('') + '</tr>'
    ).join('')
    + '</table></body></html>'
  const blob = new Blob([html], { type: 'application/vnd.ms-excel;charset=utf-8' })
  downloadBlob(blob, config.title + '.xls')
  showToast('Excel 导出成功', 'success')
}

/** 导出完整模板（含标题/表头/数据/脚注，保留原始布局与格式） */
const doExportFull = async () => {
  const arr = rawPreviewData.value
  if (!arr.length) { showToast('无数据可导出', 'warn'); return }
  // 如果没有预览快照，从当前设计态自动生成（导出不需要先预览）
  if (!previewSnap.value) {
    previewSnap.value = {
      cellData: JSON.parse(JSON.stringify(cellData)),
      mergedCells: JSON.parse(JSON.stringify(mergedCells.value)),
      rowCount: rowCount.value,
      rowHeights: { ...rowHeights.value },
      colWidths: { ...colWidths.value },
    }
  }
  // 从快照重新提取最新模板（避免使用可能过期的 previewTemplate 缓存）
  const snapCellData = previewSnap.value.cellData
  const snapMerges = previewSnap.value?.mergedCells || []
  const freshTemplate = extractTemplateBands(snapCellData, snapMerges)
  if (!freshTemplate) { showToast('模板为空，无法导出', 'warn'); return }
  console.log('[导出] 重新从快照提取模板, 全部cells:', freshTemplate.cells.map(c => `R${c.r}C${c.c}="${c.val}"`))
  const bands = computeTemplateBands(freshTemplate.cells, dataLoopStartRow.value)
  const { cells, masterRows, firstPageRows, firstPageRowCount, everyPageHeaderRows, everyPageHeaderRowCount, loopRows, masterRowCount, dataRowSpan, loopBaseR, trailingCells, trailingRows, trailingRowCount } = bands
  const cellMaxC = cells.length ? Math.max(...cells.map(c => c.c)) : 0
  const mergeMaxC = snapMerges.length ? Math.max(...snapMerges.map(m => m.eC)) : 0
  const maxC = Math.max(cellMaxC, mergeMaxC)
  // 自动计算分页计划（用户可能未先预览就直接导出）
  if (!pagePlan.value.length && arr.length) {
    const savedTpl = previewTemplate.value
    previewTemplate.value = bands
    computePagePlan(arr)
    previewTemplate.value = savedTpl
  }
  const plan = pagePlan.value.length ? pagePlan.value : null
  const totalPages = plan?.length || Math.ceil(arr.length / previewPageSize.value)
  const snapColWidths = previewSnap.value?.colWidths || {}
  const snapRowHeights = previewSnap.value?.rowHeights || {}
  const resolvedCfg = { ...printConfig }

  // ---- ExcelJS workbook ----
  const wb = new ExcelJS.Workbook()
  const ws = wb.addWorksheet(config.title || '报表')
  // 关闭默认网格线，只显示显式设定的边框
  ws.properties.showGridLines = false
  // 列宽（根据字体族动态计算 px→Excel 字符宽度单位）
  // Excel 列宽单位 = 默认字体下 "0" 字符的像素宽度
  const colWidthPxPerChar = (() => {
    const ff = (config.fontFamily || 'SimSun').split(',')[0].trim().toLowerCase()
    // 常见中文字体字符宽度近似值（11pt 基准）
    if (ff.includes('simsun') || ff.includes('宋体') || ff.includes('楷体')) return 7.0
    if (ff.includes('simhei') || ff.includes('黑体')) return 7.2
    if (ff.includes('microsoft yahei') || ff.includes('微软雅黑')) return 7.5
    if (ff.includes('arial') || ff.includes('helvetica')) return 7.8
    return 7.2 // 默认中文字体
  })()
  for (let c = 1; c <= maxC; c++) {
    const cw = snapColWidths[c] || defaultColWidth.value
    ws.getColumn(c).width = Math.round((cw / colWidthPxPerChar) * 10) / 10
  }

  // 合并辅助
  const mergesForRow = {}; for (const m of snapMerges) {
    for (let rr = m.sR; rr <= m.eR; rr++) {
      if (!mergesForRow[rr]) mergesForRow[rr] = []
      mergesForRow[rr].push(m)
    }
  }
  const getMerge = (r, c) => (mergesForRow[r] || []).find(m => c >= m.sC && c <= m.eC && r >= m.sR && r <= m.eR) || null
  const isMergeStart = (r, c) => { const m = getMerge(r, c); return (m && m.sR === r && m.sC === c) ? m : null }
  const isMergeCovered = (r, c) => { const m = getMerge(r, c); return m && !(m.sR === r && m.sC === c) }

  // === 性能优化：预建索引，避免 cells.filter/find 在循环内 O(n²) ===
  const cellsByRow = {}
  for (const cd of cells) {
    if (!cellsByRow[cd.r]) cellsByRow[cd.r] = []
    cellsByRow[cd.r].push(cd)
  }
  const cellsByKey = {}
  for (const cd of cells) {
    cellsByKey[cd.r + '_' + cd.c] = cd
  }
  // 预计算合并覆盖集合（O(1) 查找，替代 isMergeCovered 的 O(n) 扫描）
  const mergeCoveredKeySet = new Set()
  const mergeStartKeyMap = {} // "R_C" -> merge object
  for (const m of snapMerges) {
    for (let rr = m.sR; rr <= m.eR; rr++) {
      for (let cc = m.sC; cc <= m.eC; cc++) {
        const k = rr + '_' + cc
        if (rr === m.sR && cc === m.sC) {
          mergeStartKeyMap[k] = m
        } else {
          mergeCoveredKeySet.add(k)
        }
      }
    }
  }

  // fmt ExcelJS style
  const fmtToXlsx = (fmt) => {
    const f = fmt || {}
    const style = { font: {}, alignment: {}, border: {} }
    // alignment
    if (f.align) style.alignment.horizontal = f.align
    style.alignment.vertical = f.verticalAlign || 'top'
    style.alignment.wrapText = !!f.wordWrap
    // font
    if (f.bold) style.font.bold = true
    if (f.italic) style.font.italic = true
    if (f.underline) style.font.underline = 'single'
    if (f.color) style.font.color = { argb: 'FF' + f.color.replace('#', '') }
    else if (config.fontColor) style.font.color = { argb: 'FF' + (config.fontColor || '#333').replace('#', '') }
    if (f.fontSize) style.font.size = f.fontSize
    else style.font.size = config.fontSize || 24
    if (f.fontFamily) style.font.name = f.fontFamily.split(',')[0].trim()
    else if (config.fontFamily) style.font.name = config.fontFamily.split(',')[0].trim()
    // fill：仅当有显式背景色时才设置
    if (f.bgColor) style.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF' + f.bgColor.replace('#', '') } }
    // 边框：默认无边框，仅当显式设置了 f.border 且不为 none 时才画线
    const xbNone = { style: 'none' }
    if (f.border && f.border !== 'none') {
      const bw = f.borderWidth || config.borderWidth || 1
      const bwStyle = bw <= 1 ? 'thin' : bw <= 2 ? 'medium' : 'thick'
      const bc = (f.borderColor || config.borderColor || '#333333').replace('#', '')
      const xb = { style: bwStyle, color: { argb: 'FF' + bc } }
      if (f.border === 'all' || f.border === 'outer') {
        style.border = { top: xb, bottom: xb, left: xb, right: xb }
      } else if (f.border === 'top') {
        style.border = { top: xb, bottom: xbNone, left: xbNone, right: xbNone }
      } else if (f.border === 'bottom') {
        style.border = { top: xbNone, bottom: xb, left: xbNone, right: xbNone }
      } else if (f.border === 'left') {
        style.border = { top: xbNone, bottom: xbNone, left: xb, right: xbNone }
      } else if (f.border === 'right') {
        style.border = { top: xbNone, bottom: xbNone, left: xbNone, right: xb }
      }
    } else {
      // 显式清零：避免 ExcelJS 默认行为产生意外边线
      style.border = { top: xbNone, bottom: xbNone, left: xbNone, right: xbNone }
    }
    return style
  }

  // 写单元格
  const isImageUrl = (val) => val && (/^https?:\/\/.+\.(jpg|jpeg|png|gif|webp)(\?.*)?$/i.test(String(val)) || /^data:image\//i.test(String(val)) || /^\/?[\w\/\.\-]+\.(jpg|jpeg|png|gif|webp|bmp|svg)(\?.*)?$/i.test(String(val)))
  const pendingImages = []
  const writeCell = (r, c, val, fmt, ms) => {
    // 空值、无边框、且非合并起始格时跳过（合并起始格必须写入以应用样式，否则 Excel 会用默认格式产生灰线）
    const hasBorder = fmt && fmt.border && fmt.border !== 'none'
    const isMergeStartCell = !!ms
    if ((val === '' || val == null) && !hasBorder && !isMergeStartCell) return
    const cell = ws.getCell(r, c)
    const st = fmtToXlsx(fmt)
    cell.style = st
    if (isImageUrl(val)) {
      cell.value = ''
      pendingImages.push({ excelRow: r, col: c, url: val, ms: ms || null })
    } else {
      cell.value = val || ''
    }
  }

  // 计算行高：优先取设计值，否则根据该行最大字体自动计
  // 已合并的 Excel 单元格集合，防止重复合并
  const mergedExcelCells = new Set()
  const cellKey = (r, c) => `${r},${c}`
  const isRangeAlreadyMerged = (sR, sC, eR, eC) => {
    for (let rr = sR; rr <= eR; rr++) {
      for (let cc = sC; cc <= eC; cc++) {
        if (mergedExcelCells.has(cellKey(rr, cc))) return true
      }
    }
    return false
  }
  const markRangeMerged = (sR, sC, eR, eC) => {
    for (let rr = sR; rr <= eR; rr++) {
      for (let cc = sC; cc <= eC; cc++) {
        mergedExcelCells.add(cellKey(rr, cc))
      }
    }
  }
  const safeMergeCells = (sR, sC, eR, eC) => {
    if (isRangeAlreadyMerged(sR, sC, eR, eC)) {
      // 查找冲突的已有合并，方便快速定位重叠区域
      const conflicts = []
      for (let rr = sR; rr <= eR; rr++) {
        for (let cc = sC; cc <= eC; cc++) {
          if (mergedExcelCells.has(cellKey(rr, cc))) conflicts.push(`R${rr}C${cc}`)
        }
      }
      console.warn(`[导出合并] 跳过重复合并 Excel R${sR}C${sC}-R${eR}C${eC}（重叠格：${conflicts.join(',')}），当前 excelRow=${excelRow}`)
      return
    }
    markRangeMerged(sR, sC, eR, eC)
    ws.mergeCells(sR, sC, eR, eC)
  }

  // px→pt 转换系数（72pt/inch ÷ 96px/inch）
  const pxToPt = 0.75

  const computeRowHeight = (r) => {
    // 优先使用设计时设定的行高（px → pt）
    if (snapRowHeights[r] != null) return Math.round(snapRowHeights[r] * pxToPt)
    // 自动计算：从该行单元格中找最大字体（用预建索引 O(1)）
    const rowCells = cellsByRow[r] || []
    let maxFs = config.fontSize || 24
    let hasWordWrap = false
    for (const cv of rowCells) {
      if (cv.fmt?.fontSize && cv.fmt.fontSize > maxFs) maxFs = cv.fmt.fontSize
      if (cv.fmt?.wordWrap) hasWordWrap = true
    }
    // 行高 = 字号 × 行间距系数；中文需要 1.5-1.8 倍，英文 ~1.3 倍
    const lineHeightRatio = (config.fontFamily || '').includes('宋体') || (config.fontFamily || '').includes('SimSun') ? 1.7 : 1.5
    let estH = Math.round(maxFs * lineHeightRatio)
    // 自动换行单元格至少给 3 行空间
    if (hasWordWrap) estH = Math.max(estH, Math.round(maxFs * lineHeightRatio * 2.5))
    // 不低于面板默认行高的 pt 等价
    const minH = Math.round(defaultRowHeight.value * pxToPt)
    return Math.max(minH, estH)
  }
  // 写页面行
  let excelRow = 1
  let logoRendered = false // logo 只渲染一次，避免循环区域重复
  let titleRendered = false // title 只渲染一次，避免循环区域重复
  const firstMaxR = firstPageRows.size ? Math.max(...firstPageRows) : 0
  // 表头范围：有显式每页表头就用它，否则masterRows 但跳过已渲染的首页行
  const hdrMinR = everyPageHeaderRows.size ? Math.min(...everyPageHeaderRows) : (firstMaxR ? firstMaxR + 1 : 1)
  const hdrMaxR = everyPageHeaderRows.size ? Math.max(...everyPageHeaderRows) : masterRowCount
  const loopMinR = loopRows.size ? Math.min(...loopRows) : 0
  const loopMaxR = loopRows.size ? Math.max(...loopRows) : 0
  const trMinR = trailingRows?.size ? Math.min(...trailingRows) : 0
  const trMaxR = trailingRows?.size ? Math.max(...trailingRows) : 0

  const renderRowRange = (startR, endR, rowSet, dataRow) => {
    const start = excelRow
    for (let tr = startR; tr <= endR; tr++) {
      // 用预建索引 O(1) 替代 cells.filter
      const rowCells = cellsByRow[tr] || []
      let rowHasContent = rowCells.length > 0
      if (!rowHasContent) {
        // 检查整行是否被合并覆盖
        let covered = false
        for (let c = 1; c <= maxC; c++) {
          if (mergeCoveredKeySet.has(tr + '_' + c)) { covered = true; break }
        }
        if (!covered) continue
      }
      for (let c = 1; c <= maxC; c++) {
        if (mergeCoveredKeySet.has(tr + '_' + c)) continue
        const ms = mergeStartKeyMap[tr + '_' + c] || null
        // 用预建索引 O(1) 替代 rowCells.find
        const cv = cellsByKey[tr + '_' + c] || null
        let cellVal = cv ? cv.val : ''
        if (/\\$\\{logo\\}/.test(cellVal || '') && logoRendered) cellVal = ''
        if (/\\$\\{title\\}/.test(cellVal || '') && titleRendered) cellVal = ''
        const raw = cv ? fillTemplate(cellVal, dataRow) : ''
        if (cv && /\\$\\{logo\\}/.test(cv.val || '') && !logoRendered) logoRendered = true
        if (cv && /\\$\\{title\\}/.test(cv.val || '') && !titleRendered) titleRendered = true
        writeCell(excelRow, c, raw, cv?.fmt || null, ms)
        if (ms) {
          safeMergeCells(excelRow, c, excelRow + (ms.eR - ms.sR), c + (ms.eC - ms.sC))
        }
      }
      ws.getRow(excelRow).height = rowHasContent ? computeRowHeight(tr) : 0.1
      excelRow++
    }
    return excelRow - start
  }

  // 首页专属行（仅第1页对应数据的第一条）
   if (firstPageRows.size) {
     for (let r = 1; r <= firstMaxR; r++) {
       if (!firstPageRows.has(r)) continue
       const rowCells = cellsByRow[r] || []
       let rowHasContent = rowCells.length > 0
       if (!rowHasContent) {
         let covered = false
         for (let c = 1; c <= maxC; c++) {
           if (mergeCoveredKeySet.has(r + '_' + c)) { covered = true; break }
         }
         if (!covered) continue
       }
       const firstData = { ...arr[0], currentPage: 1, page: totalPages, title: config.title, logo: config.logoImage }
       for (let c = 1; c <= maxC; c++) {
         if (mergeCoveredKeySet.has(r + '_' + c)) continue
         const ms = mergeStartKeyMap[r + '_' + c] || null
         const cv = cellsByKey[r + '_' + c] || null
         let cellVal = cv ? cv.val : ''
         if (/\\$\\{logo\\}/.test(cellVal || '') && logoRendered) cellVal = ''
         if (/\\$\\{title\\}/.test(cellVal || '') && titleRendered) cellVal = ''
         const raw = cv ? fillTemplate(cellVal, firstData) : ''
         if (cv && /\\$\\{logo\\}/.test(cv.val || '') && !logoRendered) logoRendered = true
         if (cv && /\\$\\{title\\}/.test(cv.val || '') && !titleRendered) titleRendered = true
         writeCell(excelRow, c, raw, cv?.fmt || null, ms)
         if (ms) {
           safeMergeCells(excelRow, c, excelRow + (ms.eR - ms.sR), c + (ms.eC - ms.sC))
         }
       }
       ws.getRow(excelRow).height = rowHasContent ? computeRowHeight(r) : 0.1
       excelRow++
     }
   }

  // 表头（导出只渲染一次，不每页重复）
  const hdrData = { ...arr[0], currentPage: 1, page: totalPages, title: config.title, logo: config.logoImage }
  renderRowRange(hdrMinR, hdrMaxR, everyPageHeaderRows.size ? everyPageHeaderRows : null, hdrData)
  showToast(`正在构建报表...（共 ${totalPages} 页，${arr.length} 条数据）`, 'info', 0)
  let writtenRows = 0
  for (let pg = 0; pg < totalPages; pg++) {
    // 每 5 页让出主线程 + 更新进度提示（含百分比）
    if (pg > 0 && pg % 5 === 0) {
      showToast(`正在构建报表... 第 ${pg}/${totalPages} 页 (${Math.round(pg / totalPages * 100)}%)`, 'info', 0)
      await new Promise(r => setTimeout(r, 0))
    }
    const pp = plan ? plan[pg] : null
    const pageItems = pp ? arr.slice(pp.start, pp.end + 1) : arr.slice(pg * previewPageSize.value, (pg + 1) * previewPageSize.value)
    // 数据行默认值兜底
    for (const dataRow of pageItems) {
      if (!dataRow.package_length) dataRow.package_length = '0'
      if (!dataRow.package_width) dataRow.package_width = '0'
      if (!dataRow.package_height) dataRow.package_height = '0'
      if (!dataRow.innerBoxCount) dataRow.innerBoxCount = '0'
    }
    // 数据
    for (const dataRow of pageItems) {
      const rowData = { ...dataRow, currentPage: pg + 1, page: totalPages, title: config.title, logo: config.logoImage }
      for (let tr = loopMinR; tr <= loopMaxR; tr++) {
        renderRowRange(tr, tr, loopRows.has(tr) ? loopRows : null, rowData)
      }
      writtenRows++
    }
    // 每页页脚（每页都渲染，currentPage 动态为当前页码）
    if (trailingCells.length) {
      const opName = authState.userInfo?.realName || authState.userInfo?.username || arr[0]?.operator_name || ''
      const footerData = { ...arr[0], operatorName: opName, printTime: new Date().toLocaleString('zh-CN'), currentPage: pg + 1, page: totalPages, total_pages: totalPages, title: config.title, logo: config.logoImage }
      renderRowRange(trMinR, trMaxR, trailingRows, footerData)
    }
  }
  showToast(`表格构建完成，共 ${writtenRows} 行，正在生成文件...`, 'info', 0)
  await new Promise(r => setTimeout(r, 0))

  // 嵌入图片Excel（并发加载，避免串行瓶颈）
  if (pendingImages.length) {
    showToast(`正在导出图片...（共 ${pendingImages.length} 张）`, 'info', 0)
  }
  console.log(`[导出图片] 待处理图片数: ${pendingImages.length}`, pendingImages.map(p => ({ row: p.excelRow, col: p.col, ms: p.ms ? `${p.ms.sR}-${p.ms.eR},${p.ms.sC}-${p.ms.eC}` : null, isDataUri: /^data:image/.test(p.url) })))

  // 图片并发加载辅助函数
  const loadImageBuffer = async (imgUrl) => {
    let arrBuf, ext
    if (/^data:image\//i.test(imgUrl)) {
      // data URI (base64) — 使用 fetch 直接获取 ArrayBuffer，避免 atob/charCodeAt 性能问题
      try {
        const resp = await fetch(imgUrl)
        if (!resp.ok) return null
        arrBuf = await resp.arrayBuffer()
        const m = imgUrl.match(/^data:image\/(\w+);/)
        ext = (m ? m[1] : 'png').toLowerCase()
      } catch {
        // fallback: atob 解码（旧方案兼容）
        const m = imgUrl.match(/^data:image\/(\w+);base64,/)
        if (!m) return null
        ext = m[1].toLowerCase()
        const base64 = imgUrl.split(',')[1]
        try {
          const binaryStr = atob(base64)
          const bytes = Uint8Array.from(binaryStr, c => c.charCodeAt(0))
          arrBuf = bytes.buffer
        } catch (atobErr) {
          console.error('[导出图片] atob/base64解码失败:', atobErr.message)
          return null
        }
      }
    } else {
      try {
        const resp = await fetch(imgUrl, { mode: 'cors' })
        if (!resp.ok) {
          console.error(`[导出图片] HTTP ${resp.status} 加载失败: ${imgUrl}`)
          return null
        }
        const blob = await resp.blob()
        arrBuf = await blob.arrayBuffer()
        let extRaw = (imgUrl.split('.').pop() || 'png').split('?')[0].toLowerCase()
        const mimeMap = { jpg: 'jpeg', jpeg: 'jpeg', png: 'png', gif: 'gif', webp: 'png', bmp: 'bmp', svg: 'png' }
        ext = mimeMap[extRaw] || 'png'
      } catch (fetchErr) {
        console.error('[导出图片] 网络请求失败:', fetchErr.message, imgUrl)
        return null
      }
    }
    const mimeMap2 = { jpg: 'jpeg', jpeg: 'jpeg', png: 'png', gif: 'gif', webp: 'png', bmp: 'bmp', svg: 'png' }
    ext = mimeMap2[ext] || 'png'
    // 读取图片原始尺寸，用于等比缩放
    let dims = null
    try {
      const mimeType = ext === 'jpeg' ? 'image/jpeg' : `image/${ext}`
      dims = await new Promise((resolve) => {
        const img = new Image()
        img.onload = () => { const r = { w: img.naturalWidth, h: img.naturalHeight }; URL.revokeObjectURL(img.src); resolve(r) }
        img.onerror = () => { URL.revokeObjectURL(img.src); resolve(null) }
        img.src = URL.createObjectURL(new Blob([arrBuf], { type: mimeType }))
      })
    } catch { dims = null }
    return { arrBuf, ext, dims }
  }

  // 分批并发加载图片（每批 6 张并发）
  const IMG_CONCURRENCY = 6
  const failedImages = []
  let loadedCount = 0
  const totalImgCount = pendingImages.length
  for (let batchStart = 0; batchStart < pendingImages.length; batchStart += IMG_CONCURRENCY) {
    const batch = pendingImages.slice(batchStart, batchStart + IMG_CONCURRENCY)
    const batchResults = await Promise.all(
      batch.map(async (img) => {
        const result = await loadImageBuffer(img.url)
        return { img, result }
      })
    )
    for (const { img, result } of batchResults) {
      loadedCount++
      if (loadedCount % 30 === 0 || loadedCount === totalImgCount) {
        showToast(`正在导出图片... ${loadedCount}/${totalImgCount}`, 'info', 0)
      }
      if (!result) {
        failedImages.push(img)
        continue
      }
      const { arrBuf, ext } = result
      const imgId = wb.addImage({ buffer: arrBuf, extension: ext })
      let startCol = img.col, endCol = img.col
      let startRow = img.excelRow, endRow = img.excelRow
      if (img.ms) {
        startCol = img.ms.sC; endCol = img.ms.eC
        endRow = img.excelRow + (img.ms.eR - img.ms.sR)
      }
      // 图片像素尺寸：用设计器快照值（精确对应预览效果，避免Excel单位换算偏差）
      let totalW = 0, totalH = 0
      if (img.ms) {
        for (let cc = img.ms.sC; cc <= img.ms.eC; cc++) totalW += snapColWidths[cc] || defaultColWidth.value
        for (let rr = img.ms.sR; rr <= img.ms.eR; rr++) totalH += snapRowHeights[rr] || defaultRowHeight.value
      } else {
        totalW = snapColWidths[img.col] || defaultColWidth.value
        totalH = (ws.getRow(img.excelRow).height || (defaultRowHeight.value * pxToPt)) * (4 / 3)
      }
      // 等比缩放：保持原图比例，不撑满合并区域（避免logo等被拉伸）
      let extW = Math.round(totalW), extH = Math.round(totalH)
      if (result.dims && result.dims.w && result.dims.h) {
        const ratio = result.dims.w / result.dims.h
        if (totalW / totalH > ratio) {
          extW = Math.round(totalH * ratio)
          extH = Math.round(totalH)
        } else {
          extW = Math.round(totalW)
          extH = Math.round(totalW / ratio)
        }
      }
      ws.addImage(imgId, {
        tl: { col: startCol - 1, row: startRow - 1 },
        ext: { width: extW, height: extH }
      })
    }
    // 更新进度提示（每批最后更新一次）
    if (totalImgCount > IMG_CONCURRENCY && batchStart + IMG_CONCURRENCY >= pendingImages.length) {
      showToast(`正在生成文件...`, 'info', 0)
    }
  }
  if (failedImages.length > 0) {
    console.warn(`[导出图片] ${failedImages.length}/${pendingImages.length} 张图片加载失败:`, failedImages.map(p => ({ row: p.excelRow, col: p.col, url: p.url.slice(0,80) })))
    showToast(`警告: ${failedImages.length} 张图片未能加载，对应单元格已留空`, 'warn', 4000)
  }

  // 写入 Blob 并下载（writeBuffer 对大工作簿可能耗时较长，给出提示）
  showToast('正在生成 Excel 文件，请稍候...', 'info', 0)
  await new Promise(r => setTimeout(r, 0))
  const buffer = await wb.xlsx.writeBuffer()
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  downloadBlob(blob, config.title + '.xlsx')
  showToast('Excel 导出成功', 'success')
}

/** 构建含完整格式的打印HTML（所有页面） */
const buildPrintHtml = (singleTable = false) => {
  const arr = rawPreviewData.value
  if (!arr.length) return null
  const tpl = previewTemplate.value
  if (!tpl) return null
  const { cells, masterRows, firstPageRows, everyPageHeaderRows, firstPageRowCount, everyPageHeaderRowCount, loopRows, masterRowCount, dataRowSpan, loopBaseR, trailingCells, trailingRows, trailingRowCount } = tpl
  const snapMerges = previewSnap.value?.mergedCells || []
  const cellMaxC = cells.length ? Math.max(...cells.map(c => c.c)) : 0
  const mergeMaxC = snapMerges.length ? Math.max(...snapMerges.map(m => m.eC)) : 0
  const maxC = Math.max(cellMaxC, mergeMaxC)
  const plan = pagePlan.value.length ? pagePlan.value : null
  const totalPages = plan?.length || Math.ceil(arr.length / previewPageSize.value)
  const snapColWidths = previewSnap.value?.colWidths || {}
  const snapRowHeights = previewSnap.value?.rowHeights || {}

  // 合并辅助
  const mergesForRow = {}  // r -> [{ sR,eR,sC,eC }]
  for (const m of snapMerges) {
    for (let rr = m.sR; rr <= m.eR; rr++) {
      if (!mergesForRow[rr]) mergesForRow[rr] = []
      mergesForRow[rr].push(m)
    }
  }
  const getMerge = (r, c) => {
    const list = mergesForRow[r] || []
    return list.find(m => c >= m.sC && c <= m.eC && r >= m.sR && r <= m.eR) || null
  }
  const isMergeStart = (r, c) => {
    const m = getMerge(r, c)
    return m && m.sR === r && m.sC === c ? m : null
  }
  const isMergeCovered = (r, c) => {
    const m = getMerge(r, c)
    return m && !(m.sR === r && m.sC === c)
  }

  // 格式inline CSS（始终输出完整默认值，避免被全局样式覆盖
  const fmtToStyle = (cell) => {
    const f = cell?.fmt || {}
    const s = []
    // 对齐
    if (f.align) s.push(`text-align:${f.align}`)
    if (f.verticalAlign === 'middle') s.push('vertical-align:middle')
    else if (f.verticalAlign === 'bottom') s.push('vertical-align:bottom')
    else s.push('vertical-align:top')
    // font
    if (f.bold)       s.push('font-weight:bold')
    if (f.italic)     s.push('font-style:italic')
    if (f.underline)  s.push('text-decoration:underline')
    if (f.color)      s.push(`color:${f.color}`)
    else if (config.fontColor) s.push(`color:${config.fontColor}`)
    if (f.bgColor)    s.push(`background-color:${f.bgColor}`)
    if (f.fontSize)   s.push(`font-size:${f.fontSize}pt`)
    else s.push(`font-size:${config.fontSize || 24}pt`)
    if (f.fontFamily) s.push(`font-family:${f.fontFamily}`)
    else if (config.fontFamily) s.push(`font-family:${config.fontFamily}`)
    // 换行与溢
    if (f.wordWrap) {
      s.push('white-space:normal;word-break:break-all;overflow:visible')
    } else {
      s.push('white-space:nowrap;overflow:hidden')
    }
    // 边框：默认无边框，仅当显式设置时才渲染
    if (f.border && f.border !== 'none') {
      const bw = f.borderWidth || config.borderWidth || 1
      const bc = f.borderColor || config.borderColor || config.fontColor || '#333333'
      const b = `${bw}px solid ${bc}`
      if (f.border === 'all' || f.border === 'outer') s.push(`border:${b}`)
      else if (f.border === 'top') s.push(`border-top:${b};border-left:none;border-right:none;border-bottom:none`)
      else if (f.border === 'bottom') s.push(`border-bottom:${b};border-left:none;border-right:none;border-top:none`)
      else if (f.border === 'left') s.push(`border-left:${b};border-top:none;border-right:none;border-bottom:none`)
      else if (f.border === 'right') s.push(`border-right:${b};border-top:none;border-left:none;border-bottom:none`)
    } else {
      s.push('border:none')
    }
    return s.join(';')
  }

  // <colgroup> 定义列宽（HTML width 属性，Excel 兼容
  let colGroup = '<colgroup>'
  for (let c = 1; c <= maxC; c++) {
    const cw = snapColWidths[c] || defaultColWidth.value
    colGroup += `<col width="${cw}" style="width:${cw}px">`
  }
  colGroup += '</colgroup>'
  // 构建HTML
  const buildRowHtml = (r, rowSet, dataRow) => {
    const rowCells = cells.filter(c => rowSet.has(c.r) && c.r === r)
    const hasWordWrap = rowCells.some(rc => rc.fmt?.wordWrap)
    const baseH = snapRowHeights[r] != null ? snapRowHeights[r] : defaultRowHeight.value

    let tr = '<tr style="mso-height-source:userset;">'
    for (let c = 1; c <= maxC; c++) {
      if (isMergeCovered(r, c)) continue
      const ms = isMergeStart(r, c)
      const cv = rowCells.find(rc => rc.c === c)
      // logo、title 等全局字段只渲染一次
      let cellVal = cv ? cv.val : ''
      if (cellVal.includes('${logo}') && logoRendered) cellVal = ''
      if (cellVal.includes('${title}') && titleRendered) cellVal = ''
      const raw = cv ? fillTemplate(cellVal, dataRow) : ''
      if (cv && cv.val.includes('${logo}') && !logoRendered) logoRendered = true
      if (cv && cv.val.includes('${title}') && !titleRendered) titleRendered = true
      const isImg = raw && (/^https?:\/\/.+\.(jpg|jpeg|png|gif|webp|bmp|svg)(\?.*)?$/i.test(raw) || /^data:image\//i.test(raw) || /^\/[\w\/\.\-]+\.(jpg|jpeg|png|gif|webp|bmp|svg)(\?.*)?$/i.test(raw))

      let td = `<td height="${baseH}" style="height:${baseH}px;`
      if (cv?.fmt?.wordWrap) {
        td += 'height:auto;min-height:' + baseH + 'px;'
      }
      if (cv) td += fmtToStyle(cv)
      else td += fmtToStyle(null)
      td += '"'
      if (ms) td += ` colspan="${ms.eC - ms.sC + 1}" rowspan="${ms.eR - ms.sR + 1}"`
      td += '>'
      if (isImg) {
        const mergedRows = ms ? (ms.eR - ms.sR + 1) : 1
        const imgH = Math.max(baseH * mergedRows - 6, 20)
        td += `<img src="${raw}" style="max-width:100%;max-height:${imgH}px;display:block;margin:0 auto;" onerror="this.style.display='none'"/>`
      } else {
        td += escHtml(raw)
      }
      td += '</td>'
      tr += td
    }
    tr += '</tr>'
    return tr
  }

  // 首页专属行（仅第1页）
  let logoRendered = false
  let titleRendered = false
  const firstPageHtmlArr = []
  if (firstPageRows.size) {
    for (let r = 1; r <= firstPageRowCount; r++) {
      firstPageHtmlArr.push(buildRowHtml(r, firstPageRows, arr[0]))
    }
  }

  // 每页表头（仅 everyPageHeaderRows，不回退到 masterRows 避免与 firstPageRows 重复）
  const everyPageHeaderHtmlArr = []
  if (everyPageHeaderRows.size) {
    const hdrMinR = Math.min(...everyPageHeaderRows)
    const hdrMaxR = Math.max(...everyPageHeaderRows)
    for (let r = hdrMinR; r <= hdrMaxR; r++) {
      everyPageHeaderHtmlArr.push(buildRowHtml(r, everyPageHeaderRows, arr[0]))
    }
  }

  // 构建输出
  const loopMinR = loopRows.size ? Math.min(...loopRows) : 0
  const loopMaxR = loopRows.size ? Math.max(...loopRows) : 0

  let pagesHtml = ''
  if (singleTable) {
    // Excel HTML 导出：单连续表格，页间无截断
    let bodyHtml = firstPageHtmlArr.join('')  // 首页专属行只在开头一次'
    for (let pg = 0; pg < totalPages; pg++) {
      const pp = plan ? plan[pg] : null
      const pageItems = pp ? arr.slice(pp.start, pp.end + 1) : arr.slice(pg * previewPageSize.value, (pg + 1) * previewPageSize.value)
      // 数据行默认值兜底
      for (const dataRow of pageItems) {
        if (!dataRow.package_length) dataRow.package_length = '0'
        if (!dataRow.package_width) dataRow.package_width = '0'
        if (!dataRow.package_height) dataRow.package_height = '0'
        if (!dataRow.innerBoxCount) dataRow.innerBoxCount = '0'
      }
      bodyHtml += everyPageHeaderHtmlArr.join('') // 每页表头'
      for (const dataRow of pageItems) {
        for (let tr = loopMinR; tr <= loopMaxR; tr++) {
          const rowCells = cells.filter(c => c.r === tr && loopRows.has(c.r))
          if (!rowCells.length) {
            bodyHtml += buildRowHtml(tr, new Set([tr]), dataRow)
            continue
          }
          bodyHtml += buildRowHtml(tr, loopRows, dataRow)
        }
      }
      // 每页页脚（仅最后一页，currentPage 动态）
      if (trailingCells.length && pg === totalPages - 1) {
        const trMinR = Math.min(...trailingRows), trMaxR = Math.max(...trailingRows)
        const opName = authState.userInfo?.realName || authState.userInfo?.username || arr[0]?.operator_name || ''
        const footerData = { ...arr[0], operatorName: opName, printTime: new Date().toLocaleString('zh-CN'), currentPage: pg + 1, page: totalPages, total_pages: totalPages, title: config.title, logo: config.logoImage }
        for (let r = trMinR; r <= trMaxR; r++) {
          bodyHtml += buildRowHtml(r, trailingRows, footerData)
        }
      }
    }
    pagesHtml = `<table>${colGroup}${bodyHtml}</table>`
  } else {
    // 打印：标题行独立在表格外 + 数据表格用thead自动每页重复表头
    const hasEveryPageHeader = everyPageHeaderHtmlArr.length > 0
    const hasTrailing = trailingCells.length > 0

    // 1. 首页专属行（标题/公司信息等）— 独立小表格，放在数据表格之前，仅出现一次
    let titleHtml = ''
    if (firstPageHtmlArr.length) {
      titleHtml = `<table style="border:none;border-collapse:collapse;margin:0 auto;">${colGroup}<tbody>${firstPageHtmlArr.join('')}</tbody></table>`
    }

    // 2. 数据表格：thead（表头，浏览器打印时每页自动重复）+ tbody（全部数据+页脚）
    let dataBodyParts = []
    // 全部数据行（不切片，让浏览器自然分页）
    for (const dataRow of arr) {
      for (let tr = loopMinR; tr <= loopMaxR; tr++) {
        const rowCells = cells.filter(c => c.r === tr && loopRows.has(c.r))
        if (!rowCells.length) {
          dataBodyParts.push(buildRowHtml(tr, new Set([tr]), dataRow))
          continue
        }
        dataBodyParts.push(buildRowHtml(tr, loopRows, dataRow))
      }
    }
    // 页脚行（仅在最后出现一次）
    if (hasTrailing) {
      const trMinR = Math.min(...trailingRows)
      const trMaxR = Math.max(...trailingRows)
      const opName = authState.userInfo?.realName || authState.userInfo?.username || arr[0]?.operator_name || ''
      // 用实际总数据条数估算页码（首页约5条+后续每页约6条的规则）
      const estFirstCap = 5
      const estNormalCap = 6
      let estPages = 1
      let remaining = Math.max(0, arr.length - estFirstCap)
      if (remaining > 0) estPages += Math.ceil(remaining / estNormalCap)
      const footerData = { ...arr[0], operatorName: opName, printTime: new Date().toLocaleString('zh-CN'), currentPage: estPages, page: estPages, total_pages: estPages, title: config.title, logo: config.logoImage }
      for (let r = trMinR; r <= trMaxR; r++) {
        dataBodyParts.push(buildRowHtml(r, trailingRows, footerData))
      }
    }

    // 组装：标题(可选) + 数据表格(thead+tbody)
    const headerThead = hasEveryPageHeader ? `<thead>${everyPageHeaderHtmlArr.join('')}</thead>` : ''
    const dataTable = `<table>${colGroup}${headerThead}<tbody>${dataBodyParts.join('')}</tbody></table>`
    pagesHtml = titleHtml + dataTable
  }

  const resolvedCfg = { ...printConfig }
  const pageSizeCss = paperSize.value.charAt(0).toUpperCase() + paperSize.value.slice(1)
  const pageCss = singleTable ? '' : `@page{size:${pageSizeCss} ${paperOrient.value};margin:10mm;}`
  // 打印：thead自动每页重复 + tr/td避免行内断页
  const printMediaCss = singleTable ? '' : '@media print{thead{display:table-header-group}tr{page-break-inside:avoid}td{page-break-inside:avoid}.print-title{-webkit-print-color-adjust:exact;print-color-adjust:exact;}}'
  return '<!DOCTYPE html><html><head><meta charset="UTF-8"><title>' + escHtml(config.title) + '</title>'
    + '<style>'
    + '*{margin:0;padding:0;box-sizing:border-box;}'
    + pageCss
    + 'body{font-family:' + config.fontFamily + ';font-size:' + config.fontSize + 'pt;color:' + (config.fontColor || '#333') + ';}'
    + 'table{border-collapse:collapse;margin:0 auto;}'
    + 'td{padding:4px 8px;}'
    + (singleTable ? '' : '.print-title{text-align:center;font-weight:bold;font-size:32px;padding:16px 0;margin-bottom:12px;}')
    + '.print-footer{text-align:left;font-size:22px;color:#666;margin-top:20px;}'
    + printMediaCss
    + '</style></head><body>'
    + (singleTable ? '' : '')  // 不额外输出模板标题，模板单元格已有
    + pagesHtml
    + '</body></html>'
}

function escHtml(s) { return String(s ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;') }
function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  // 延迟 revoke 确保下载触发完成（大文件可能需要更长时间，使用60s）
  setTimeout(() => URL.revokeObjectURL(url), 60000)
}

// ===== 撤销 / 重做 =====
const undoStack = ref([])
const redoStack = ref([])
const MAX_UNDO = 50

function snapshotState() {
  return {
    cellData: JSON.parse(JSON.stringify(cellData)),
    mergedCells: JSON.parse(JSON.stringify(mergedCells.value)),
    rowCount: rowCount.value,
    colCount: colCount.value,
    colWidths: JSON.parse(JSON.stringify(colWidths.value)),
    rowHeights: JSON.parse(JSON.stringify(rowHeights.value)),
  }
}
function restoreState(s) {
  Object.keys(cellData).forEach(k => delete cellData[k])
  Object.assign(cellData, s.cellData)
  mergedCells.value = s.mergedCells
  rowCount.value = s.rowCount
  colCount.value = s.colCount
  colWidths.value = s.colWidths
  rowHeights.value = s.rowHeights
  nextTick(() => { rebuildMergeCache(); rebuildRowWordWrapCache() })  // watch 触发后再刷新
}

function pushUndo() {
  if (undoGuard) return
  undoStack.value.push(snapshotState())
  if (undoStack.value.length > MAX_UNDO) undoStack.value.shift()
  redoStack.value = []
}
let undoGuard = false

function undo() {
  if (undoStack.value.length === 0) return
  undoGuard = true
  redoStack.value.push(snapshotState())
  restoreState(undoStack.value.pop())
  undoGuard = false
}
function redo() {
  if (redoStack.value.length === 0) return
  undoGuard = true
  undoStack.value.push(snapshotState())
  restoreState(redoStack.value.pop())
  undoGuard = false
}

// ===== 生命周期 =====
onMounted(async () => {
  document.addEventListener('keydown', onKeyDown)
  document.addEventListener('click', closeColorPops)
  document.addEventListener('mousemove', onGlobalMouseMove)
  document.addEventListener('mouseup', onGlobalMouseUp)
  window.addEventListener('beforeunload', onBeforeUnload)
  document.addEventListener('visibilitychange', onVisibilityChange)
  window.addEventListener('pagehide', onPageHide)
  rebuildMergeCache()
  // 诊断：在控制台输入 dumpBorders() 查看所有有边框的单元格
  window.__cellData = cellData
  window.__selCell = selCell
  window.dumpBorders = () => {
    const result = []
    Object.entries(cellData).forEach(([key, d]) => {
      if (d.fmt?.border && d.fmt.border !== 'none') {
        result.push(`${key}: border=${d.fmt.border} w=${d.fmt.borderWidth||1} c=${d.fmt.borderColor||'default'}`)
      }
    })
    console.log(`[dumpBorders] 共 ${result.length} 个单元格有边框:\n` + (result.length ? result.join('\n') : '(无)'))
    return result
  }
  // DOM 诊断：inspectCell(r, c) 查看单元格的实际渲染样式
  window.inspectCell = (r, c) => {
    const el = document.querySelector(`[data-r="${r}"][data-c="${c}"]`)
    if (!el) { console.log(`[inspect] R${r}C${c} 未找到 DOM 元素`); return }
    const cs = getComputedStyle(el)
    const mi = getMergeInfo(r, c)
    console.log(`[inspect] R${r}C${c}:`, {
      display: cs.display,
      height: cs.height,
      width: cs.width,
      border: cs.border,
      borderTop: cs.borderTopWidth + ' ' + cs.borderTopStyle + ' ' + cs.borderTopColor,
      borderRight: cs.borderRightWidth + ' ' + cs.borderRightStyle + ' ' + cs.borderRightColor,
      borderBottom: cs.borderBottomWidth + ' ' + cs.borderBottomStyle + ' ' + cs.borderBottomColor,
      borderLeft: cs.borderLeftWidth + ' ' + cs.borderLeftStyle + ' ' + cs.borderLeftColor,
      boxShadow: cs.boxShadow,
      overflow: cs.overflow,
      rowSpan: el.rowSpan,
      colSpan: el.colSpan,
      offsetHeight: el.offsetHeight,
      offsetWidth: el.offsetWidth,
      mergeInfo: mi,
      cellData: cellData[`R${r}C${c}`] || (mi ? cellData[`R${mi.sR}C${mi.sC}`] : null),
    })
  }
  // 启动时清除超过12小时的过期草稿
  getDrafts()

  // === viewOnly 模式
  if (viewOnly.value) {
    loading.value = true
    try {
      const templateId = route.query.templateId
      const cacheKey = route.query.key
      const token = route.query.token || sessionStorage.getItem('token') || localStorage.getItem('token')
      if (!templateId) { showToast('缺少模板ID参数', 'error'); loading.value = false; return }
      if (!cacheKey) { showToast('缺少数据key参数', 'error'); loading.value = false; return }
      // 加载模板（从后端 API
      const tplResp = await api('/report-templates/' + templateId)
      if (tplResp.code !== 200 || !tplResp.data?.templateData) { showToast('未找到指定的报表模板', 'error'); loading.value = false; return }
      const tpl = tplResp.data
      const tplData = JSON.parse(tpl.templateData)
      applyDraftData(tplData)
      config.title = tpl.title || config.title

      // 加载数据
      const resp = await safeFetchJson('/samples/vendor-confirm-report?key=' + encodeURIComponent(cacheKey))
      let dataArr = []
      if (resp.ok) {
        dataArr = resp.data?.data || resp.data?.records || resp.data || []
        if (!Array.isArray(dataArr) && Array.isArray(resp.data)) dataArr = resp.data
      }
      if (!dataArr.length) { showToast('没有符合条件的记录', 'error'); loading.value = false; return }
      // 复用 doPreview 的模板提+ 预览逻辑
       const tmpl = extractTemplateBands()
       const hasPh = tmpl?.cells.some(c => hasLoopPlaceholder(c.val))
       if (tmpl && hasPh) {
         previewSnap.value = {
           cellData: JSON.parse(JSON.stringify(cellData)),
           mergedCells: JSON.parse(JSON.stringify(mergedCells.value)),
           rowCount: rowCount.value,
           rowHeights: { ...rowHeights.value },
           colWidths: { ...colWidths.value },
         }
         await nextTick()

         const { cells } = tmpl
         const bands = computeTemplateBands(cells, dataLoopStartRow.value)
         previewTemplate.value = bands
         previewLoopInfo.value = { loopBaseR: bands.loopBaseR, dataRowSpan: bands.dataRowSpan, loopRows: bands.loopRows, masterRowCount: bands.masterRowCount }
         rawPreviewData.value = dataArr
         previewTotal.value = dataArr.length
         previewPage.value = 1
         computePagePlan(dataArr)
         renderPreviewPage(dataArr, 1)
         previewMode.value = true
         deselectAllCells()
         nextTick(updateSelOverlay)
         showToast(`已加${dataArr.length} 条`, 'success')
      } else {
        showToast('模板中没有循环占位符 ${field}', 'warn')
      }
    } catch (e) {
      showToast('加载失败: ' + e.message, 'error')
    } finally {
      loading.value = false
    }
    return
  }

  // === 正常模式 ===
  // 定时自动保存（30秒）
  autoSaveTimer = setInterval(silentSave, 30_000)

  // 向后兼容：迁移旧版保存数据
  try {
    const raw = localStorage.getItem('report_designer_v2')
    if (raw) {
      const d = JSON.parse(raw)
      applyDraftData(d)
      const id = 'draft_' + Date.now()
      const now = new Date().toLocaleString('zh-CN')
      const all = getDrafts()
      all.push({ id, title: d.config?.title || '旧版报表', createdAt: now, updatedAt: now, data: d })
      saveDrafts(all)
      currentDraftId.value = id
      localStorage.setItem('report_current_draft', id)
      localStorage.removeItem('report_designer_v2')
    }
  } catch (_) {}

  // 测量表头高度用于虚拟滚动偏移
  await nextTick()
  const thead = sheetScrollRef?.value?.querySelector('thead')
  if (thead) theadHeight.value = thead.offsetHeight
  onSheetScroll() // 初始化视口高度

  selectCell(1, 1)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeyDown)
  document.removeEventListener('click', closeColorPops)
  document.removeEventListener('mousemove', onGlobalMouseMove)
  document.removeEventListener('mouseup', onGlobalMouseUp)
  window.removeEventListener('beforeunload', onBeforeUnload)
  document.removeEventListener('visibilitychange', onVisibilityChange)
  window.removeEventListener('pagehide', onPageHide)
  clearInterval(autoSaveTimer)
  if (_overlayScrollRaf) { cancelAnimationFrame(_overlayScrollRaf); _overlayScrollRaf = null }
})
</script>

<style scoped>
/* ========== 整体布局 ========== */
.sr-designer {
  position: fixed; inset: 0; display: flex; flex-direction: column;
  background: #f0f2f5; color: #333; overflow: hidden;
  font: 26px -apple-system, BlinkMacSystemFont, 'Microsoft YaHei', sans-serif;
}

.sr-main {
  flex: 1; display: flex; overflow: hidden; gap: 0;
}

/* ========== 左面板：数据========== */
.sr-left {
  width: 520px; background: #fff; border-right: 2px solid #e0e3e8;
  display: flex; flex-direction: column; flex-shrink: 0;
}
.sr-left-header {
  height: 72px; padding: 0 20px; font-weight: 600; font-size: 26px;
  color: #333; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 2px solid #f0f1f3;
}
.ds-import-btn {
  border: 2px solid #d0d5dd; border-radius: 8px; background: #fff;
  font-size: 22px; padding: 4px 16px; cursor: pointer; color: #2f6ef2;
  font-family: inherit; transition: .12s;
}
.ds-import-btn:hover { background: #eef1f7; border-color: #a3bbf0; }
.ds-import-wrap { position: relative; }
.ds-import-menu {
  position: absolute; right: 0; top: 100%; z-index: 200; background: #fff;
  border: 2px solid #e0e3e8; border-radius: 12px; box-shadow: 0 8px 32px rgba(0,0,0,.1);
  min-width: 360px; padding: 8px; margin-top: 8px;
}
.import-item {
  padding: 16px 24px; font-size: 26px; cursor: pointer; border-radius: 8px;
  color: #333; white-space: nowrap;
}
.import-item:hover { background: #f0f4ff; color: #2f6ef2; }
.sr-left-body { flex: 1; overflow-y: auto; padding: 8px 0; }
.ds-search-wrap { position: relative; margin: 8px 16px; }
.ds-search {
  width: 100%; padding: 12px 52px 12px 20px; border: 2px solid #d4d6da; border-radius: 10px;
  font-size: 24px; outline: none; background: #f9fafb; box-sizing: border-box;
}
.ds-search:focus { border-color: #2563eb; background: #fff; box-shadow: 0 0 0 4px rgba(37,99,235,.12); }
.ds-search-clear { position: absolute; right: 16px; top: 50%; transform: translateY(-50%); font-size: 24px; color: #9ca3af; cursor: pointer; }
.ds-count { font-size: 22px; color: #9ca3af; flex-shrink: 0; }
.ds-group-head {
  display: flex; align-items: center; gap: 12px; padding: 14px 24px;
  font-size: 26px; font-weight: 500; color: #333; cursor: pointer;
  user-select: none;
}
.ds-group-head:hover { background: #f5f6f8; }
.ds-group-head svg.expanded { transform: rotate(90deg); }
.ds-name-text { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ds-del-btn {
  width: 36px; height: 36px; border: none; background: none; cursor: pointer;
  color: #c0c5cc; font-size: 28px; border-radius: 6px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center; opacity: 0; transition: .12s;
}
.ds-group-head:hover .ds-del-btn { opacity: 1; }
.ds-del-btn:hover { background: #fee2e2; color: #ef4444; }
.ds-empty { padding: 24px 32px; font-size: 22px; color: #9ca3af; text-align: center; }
.ds-fields { padding-left: 48px; }
.ds-field {
  display: flex; flex-direction: column; gap: 4px; padding: 12px 20px;
  cursor: grab; font-size: 24px; user-select: none;
  border-bottom: 2px solid #f5f6f8;
}
.ds-field:hover { background: #eef1f7; }
.ds-field:active { cursor: grabbing; }
.df-key { font-weight: 600; font-size: 24px; color: #1a2233; font-family: 'Consolas','Monaco','Courier New',monospace; word-break: break-all; }
.df-info { color: #6b7280; font-size: 22px; line-height: 1.3; }

/* ========== 中心========== */
.sr-center { flex: 1; display: flex; flex-direction: column; min-width: 0; background: #e8ebf0; }
.sr-format-bar {
  display: flex; align-items: center; gap: 10px; padding: 10px 24px;
  background: #fff; border-bottom: 2px solid #e0e3e8; flex-shrink: 0; height: 72px; user-select: none;
  overflow-x: auto; white-space: nowrap;
}
.fmt-group { display: flex; gap: 6px; }
.fmt-sel { height: 52px; padding: 0 10px; border: 2px solid #d0d5dd; border-radius: 6px; font-size: 24px; background: #fff; color: #333; outline: none; cursor: pointer; }
.fmt-sel:focus { border-color: #2f6ef2; }
.fmt-sel-w { width: 170px; }
.fmt-sep { width: 2px; height: 40px; background: #e0e3e8; margin: 0 6px; }
.fmt-btn {
  width: 56px; height: 52px; border: 2px solid #d0d5dd; border-radius: 6px;
  background: #fff; cursor: pointer; font-size: 24px; color: #555;
  display: flex; align-items: center; justify-content: center;
  font-family: inherit; transition: .12s;
}
.fmt-btn:hover { background: #f3f4f6; }
.fmt-btn.on { background: #e8edf9; color: #2f6ef2; border-color: #a3bbf0; }
.fmt-btn-merge { width: auto; padding: 0 16px; font-size: 24px; }
.fmt-btn-preview { width: auto; padding: 0 20px; background: #2f6ef2; color: #fff; border-color: #2f6ef2; font-weight: 500; }
.fmt-btn-preview:hover { background: #2563eb; }

/* 预览模式横幅 */
.preview-banner {
  display: flex; align-items: center; gap: 16px;
  width: 100%; padding: 8px 16px;
  background: linear-gradient(135deg, #ede9fe, #f5f3ff);
  border: 2px solid #c4b5fd; border-radius: 8px; flex-shrink: 0;
}
.preview-badge {
  font-size: 22px; font-weight: 600; text-transform: uppercase;
  background: #7c3aed; color: #fff;
  padding: 4px 16px; border-radius: 6px; letter-spacing: 1px;
}
.preview-info { font-size: 24px; color: #6d28d9; font-weight: 500; }

/* 预览模式浮动按钮 */
.preview-float-actions {
  position: fixed; bottom: 32px; left: 50%; transform: translateX(-50%);
  display: flex; gap: 16px; z-index: 100;
  background: #fff; padding: 16px 32px; border-radius: 20px;
  box-shadow: 0 4px 32px rgba(0,0,0,0.15);
  border: 2px solid #e5e7eb;
}
.preview-float-btn {
  padding: 16px 36px; border-radius: 12px; border: 2px solid #d1d5db;
  background: #fff; font-size: 26px; cursor: pointer; white-space: nowrap;
  transition: background 0.15s;
}
.preview-float-btn:hover { background: #f3f4f6; }
.preview-float-btn:disabled { opacity: 0.3; cursor: default; }
.preview-page-label { font-size: 26px; font-weight: 600; color: #374151; padding: 16px 8px; white-space: nowrap; }
.preview-float-btn--primary { background: #16a34a; color: #fff; border-color: #16a34a; }
.preview-float-btn--primary:hover { background: #15803d; }
.preview-float-btn--exit { background: #dc2626; color: #fff; border-color: #dc2626; }
.preview-float-btn--exit:hover { background: #b91c1c; }

/* ===== viewOnly 模式顶栏 ===== */
.sr-viewonly-topbar {
  height: 88px; background: #fff; border-bottom: 2px solid #e5e7eb;
  display: flex; align-items: center; padding: 0 40px; gap: 32px;
  flex-shrink: 0;
}
.sr-viewonly-title {
  font-size: 32px; font-weight: 700; color: #1d1d1f;
}
.sr-viewonly-pages {
  font-size: 26px; color: #888;
}
.sr-viewonly-actions {
  display: flex; gap: 16px; margin-left: auto;
}
.sr-viewonly-btn {
  padding: 12px 28px; border-radius: 16px; border: 2px solid #d0d0d0;
  background: #fff; font-size: 26px; cursor: pointer; color: #333;
  font-family: inherit;
}
.sr-viewonly-btn:hover { background: #f0f0f0; }
.sr-viewonly-btn:disabled { opacity: 0.4; cursor: default; }
.sr-viewonly-btn-pri { background: #007aff; color: #fff; border-color: #007aff; }
.sr-viewonly-btn-pri:hover { background: #0064d6; }
.sr-viewonly-btn-sec { background: #16a34a; color: #fff; border-color: #16a34a; }
.sr-viewonly-btn-sec:hover { background: #15803d; }

/* 预览模式下禁用边*/
.preview-cell-readonly { cursor: default; }
.preview-cell-readonly .ss-text { user-select: none; }
.fmt-color-pair { position: relative; }
.fmt-clr-btn {
  display: flex; align-items: center; justify-content: center; height: 52px; width: 60px;
  border: 2px solid #d0d5dd; border-radius: 6px; background: #fff; cursor: pointer; color: #555;
}
.fmt-clr-btn:hover { border-color: #999; }
.fmt-clr-pop {
  position: fixed; z-index: 20000; padding: 10px; background: #fff;
  border: 2px solid #d0d5dd; border-radius: 8px;
  box-shadow: 0 8px 28px rgba(0,0,0,.1);
  display: grid; grid-template-columns: repeat(8, 40px); gap: 4px;
}
.fmt-clr-chip { width: 40px; height: 40px; border-radius: 4px; cursor: pointer; border: 2px solid #e0e3e8; transition: transform .1s; }
.fmt-clr-chip:hover { transform: scale(1.2); z-index: 1; }
.fmt-clr-chip.picked { box-shadow: 0 0 0 4px #2f6ef2; }

.sr-sheet-wrap { flex: 1; overflow: hidden; padding: 16px; background: #e8ebf0; }
.sr-sheet-scroll { overflow: auto; position: relative; width: 100%; height: 100%; }
.page-break-overlay { position: absolute; top: 0; left: 0; pointer-events: none; z-index: 10; }
.pb-line { position: absolute; pointer-events: none; }
.pb-h { left: 0; width: 100%; height: 0; border-top: 4px dashed #2563eb; }
.pb-v { top: 0; height: 100%; width: 0; border-left: 4px dashed #2563eb; }
.sr-sheet { border-collapse: separate; border-spacing: 0; table-layout: fixed; background: #fff; border: 2px solid #c0c5cc; }
.ss-corner { background: #f5f6f8; border-bottom: 2px solid #c0c5cc; border-right: 2px solid #c0c5cc; width: 92px; min-width: 92px; position: sticky; top: 0; left: 0; z-index: 3; }
.ss-col-hdr { background: #f5f6f8; border-bottom: 2px solid #c0c5cc; border-right: 2px solid #e0e3e8; height: 48px; font-size: 22px; color: #555; font-weight: 500; text-align: center; user-select: none; position: sticky; top: 0; z-index: 2; cursor: pointer; }
.ss-col-hdr:hover { background: #e0e4ec; }
.ss-col-hdr.sel-col { background: #d6e0f5; color: #2f6ef2; font-weight: 600; }
.col-resize-grip { position: absolute; right: 0; top: 0; bottom: 0; width: 10px; cursor: col-resize; z-index: 10; }
.col-resize-grip:hover { background: #2f6ef2; }
.ss-row-hdr { background: #f5f6f8; border-bottom: 2px solid #e0e3e8; border-right: 2px solid #c0c5cc; width: 92px; font-size: 22px; color: #555; text-align: center; user-select: none; position: sticky; left: 0; z-index: 1; cursor: pointer; position: relative; }
.ss-row-hdr:hover { background: #e0e4ec; }
.ss-row-hdr.sel-row { background: #d6e0f5; color: #2f6ef2; font-weight: 600; }
.ss-row-hdr.loop-start { background: #fef3c7; color: #b45309; font-weight: 700; border-bottom: 4px solid #f59e0b; }
.ss-row-hdr.page-hdr-start { background: #dbeafe; color: #1d4ed8; font-weight: 700; border-top: 4px solid #3b82f6; }
.loop-badge { position: absolute; top: 2px; right: 4px; font-size: 16px; color: #f59e0b; }
.row-resize-grip { position: absolute; bottom: 0; left: 0; right: 0; height: 10px; cursor: row-resize; z-index: 10; }
.row-resize-grip:hover { background: #2f6ef2; }
.ss-cell { border: none; padding: 0; position: relative; cursor: cell; overflow: hidden; background: #fff; min-width: 0; font-size: 24px; font-family: SimSun, serif; color: #333; user-select: none; }
.ss-cell.active-cell { /* overlay 已负责选区视觉，active-cell 不再需要额外样*/ }
.ss-cell.ss-wrap { overflow: hidden; }
.ss-text { display: block; padding: 2px 8px; line-height: 1.8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; height: 100%; }
.ss-wrap .ss-text { position: absolute; top: 0; left: 0; right: 0; bottom: 0; white-space: normal; word-break: break-all; overflow-wrap: break-word; overflow: hidden; text-overflow: clip; }
/* 含手动换行的单元格：保留\n换行 */
.ss-cell.ss-multiline .ss-text { position: absolute; top: 0; left: 0; right: 0; bottom: 0; white-space: pre-line; overflow: hidden; }
/* 垂直居中：用 flex 让文本在单元格内纵向居中 */
.ss-cell.ss-vmiddle .ss-text { display: flex; align-items: center; height: 100%; padding: 0 8px; }
.ss-cell.ss-vbottom .ss-text { display: flex; align-items: flex-end; height: 100%; padding: 0 8px; }
/* flex 子项水平对齐 */
.ss-flex-left .ss-text { justify-content: flex-start; }
.ss-flex-center .ss-text { justify-content: center; }
.ss-flex-right .ss-text { justify-content: flex-end; }
.ss-img { display: block; max-width: 100%; max-height: 100%; object-fit: scale-down; }
.ss-img-wrap { position: absolute; top: 0; left: 0; right: 0; bottom: 0; display: flex; align-items: flex-start; justify-content: flex-start; overflow: hidden; }
/* 图片容器也响应垂水平居中 */
.ss-cell.ss-vmiddle .ss-img-wrap { align-items: center; }
.ss-cell.ss-vbottom .ss-img-wrap { align-items: flex-end; }
.ss-flex-left .ss-img-wrap { justify-content: flex-start; }
.ss-flex-center .ss-img-wrap { justify-content: center; }
.ss-flex-right .ss-img-wrap { justify-content: flex-end; }
.ss-input { width: calc(100% + 4px); height: calc(100% + 2px); border: none; outline: none; padding: 2px 8px; font-size: inherit; font-family: inherit; background: #fff; position: absolute; top: -2px; left: -2px; right: -2px; bottom: -2px; z-index: 2; }

/* ========== 右侧面板 ========== */
.sr-right {
  width: 488px;
  background: linear-gradient(180deg, #fafbfc 0%, #f5f7fa 100%);
  border-left: 2px solid #e2e5ea;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: -2px 0 8px rgba(0,0,0,.03);
  overflow: hidden;
}
.sr-right-tabs {
  display: flex;
  border-bottom: 2px solid #e8ebef;
  flex-shrink: 0;
  background: rgba(255,255,255,.6);
  backdrop-filter: blur(16px);
}
.srt {
  flex: 1;
  height: 76px;
  border: none;
  background: transparent;
  font-size: 26px;
  color: #8b95a5;
  cursor: pointer;
  font-family: inherit;
  position: relative;
  transition: color .2s;
  letter-spacing: .10px;
}
.srt:hover { color: #4a5568; }
.srt.on {
  color: #1a56db;
  font-weight: 600;
}
.srt.on::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 20%;
  right: 20%;
  height: 4px;
  background: linear-gradient(90deg, transparent, #1a56db, transparent);
  border-radius: 2px;
}
.sr-right-body {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px 20px;
}
.sr-right-body::-webkit-scrollbar { width: 8px; }
.sr-right-body::-webkit-scrollbar-track { background: transparent; }
.sr-right-body::-webkit-scrollbar-thumb { background: #c9cdd4; border-radius: 4px; }

.sr-empty {
  color: #b0b7c3;
  font-size: 24px;
  text-align: center;
  padding-top: 120px;
  line-height: 1.6;
}

/* 属性分组 */
.sr-props {
  display: flex;
  flex-direction: column;
}
.sr-props > .prop-row,
.sr-props > .prop-toggles {
  padding: 10px 12px;
  margin: 2px 0;
  border-radius: 10px;
  transition: background .15s;
}
.sr-props > .prop-row:hover,
.sr-props > .prop-toggles:hover {
  background: rgba(255,255,255,.7);
}

.prop-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
}
.prop-row label {
  width: 60px;
  color: #7a8599;
  flex-shrink: 0;
  font-weight: 500;
  font-size: 23px;
  letter-spacing: .6px;
}

/* 输入框 */
.prop-inp {
  flex: 1;
  min-width: 0;
  height: 56px;
  padding: 0 16px;
  border: 2px solid #dde1e8;
  border-radius: 10px;
  font-size: 24px;
  outline: none;
  color: #2d3748;
  font-family: inherit;
  background: #fff;
  transition: border-color .2s, box-shadow .2s;
}
.prop-inp:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59,130,246,.1);
}
.prop-inp-s { width: 108px; flex: none; }
.prop-inp-xs { width: 136px; flex: none; }

/* 下拉框 */
.prop-sel {
  flex: 1;
  min-width: 0;
  height: 56px;
  padding: 0 12px;
  border: 2px solid #dde1e8;
  border-radius: 10px;
  font-size: 24px;
  outline: none;
  color: #2d3748;
  font-family: inherit;
  background: #fff;
  cursor: pointer;
  transition: border-color .2s, box-shadow .2s;
}
.prop-sel:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59,130,246,.1);
}

/* 颜色选择器 */
.prop-color {
  width: 60px;
  height: 56px;
  border: 2px solid #dde1e8;
  border-radius: 10px;
  padding: 4px;
  cursor: pointer;
  background: #fff;
  flex: none;
  transition: border-color .2s, transform .15s;
}
.prop-color:hover {
  border-color: #3b82f6;
  transform: scale(1.05);
}

/* 开关按钮 B/I/U / 自动换行 */
.prop-toggles { gap: 8px; }
.prop-toggles button {
  width: 56px;
  height: 52px;
  border: 2px solid #dde1e8;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  font-size: 24px;
  color: #64748b;
  font-family: inherit;
  transition: all .18s;
  font-weight: 500;
}
.prop-toggles button:hover {
  border-color: #93c5fd;
  color: #3b82f6;
  background: #eff6ff;
}
.prop-toggles button.on {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(59,130,246,.35);
}
.prop-toggles button i { font-style: italic; }
.prop-toggles .btn-wordwrap {
  width: auto;
  padding: 0 20px;
  white-space: nowrap;
}

/* 边框预设按钮组 */
.prop-border-bar {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  padding: 12px 12px;
  margin: 4px 0;
  background: rgba(255,255,255,.5);
  border-radius: 12px;
  border: 2px solid rgba(222,226,232,.5);
}
.bdr-btn {
  width: 56px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #e0e4ea;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  padding: 4px;
  transition: all .18s;
}
.bdr-btn:hover {
  border-color: #93c5fd;
  background: #eff6ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(59,130,246,.12);
}
.bdr-btn.on {
  border-color: #3b82f6;
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  box-shadow: 0 4px 12px rgba(59,130,246,.25), inset 0 2px 0 rgba(255,255,255,.6);
}
.bdr-svg { width: 36px; height: 36px; }

/* 预设颜色条 */
.prop-color-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px 12px;
  margin-top: 4px;
}
.clr-swatch {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  border: 4px solid transparent;
  cursor: pointer;
  flex-shrink: 0;
  transition: all .18s;
  box-shadow: 0 2px 4px rgba(0,0,0,.08);
}
.clr-swatch:hover {
  border-color: #93c5fd;
  transform: scale(1.15) translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,.12);
}
.clr-swatch.on {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59,130,246,.3), 0 4px 8px rgba(0,0,0,.1);
  transform: scale(1.1);
}

/* 配置面板分组卡片 */
.cfg-grp {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  border: 2px solid #e8ecf1;
  box-shadow: 0 2px 6px rgba(0,0,0,.04);
  transition: box-shadow .2s;
}
.cfg-grp:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,.06);
}
.cfg-grp:last-child { margin-bottom: 0; }
.cfg-ttl {
  font-size: 24px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f2f5;
  letter-spacing: .6px;
}
.cfg-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 24px;
}
.cfg-row:last-child { margin-bottom: 0; }
.cfg-row label {
  width: 112px;
  color: #7a8599;
  flex-shrink: 0;
  font-size: 23px;
}
.cfg-hint { color: #a0aab8; font-size: 22px; margin-left: 4px; }

.cfg-acts { display: flex; flex-direction: column; gap: 12px; padding-top: 4px; }
.hint-row { color: #a0aab8; font-size: 22px; }
.hint-row label { display: none; }
.hint-row span { flex: 1; }

/* 操作按钮 */
.btn-a {
  height: 64px;
  border: 2px solid #dde1e8;
  border-radius: 12px;
  font-size: 24px;
  cursor: pointer;
  font-family: inherit;
  background: #fff;
  color: #4a5568;
  transition: all .18s;
  font-weight: 500;
  letter-spacing: .4px;
}
.btn-a:hover {
  background: #f8fafc;
  border-color: #c9cdd4;
  box-shadow: 0 2px 6px rgba(0,0,0,.06);
}
.btn-save {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  border-color: transparent;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(59,130,246,.25);
}
.btn-save:hover {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  box-shadow: 0 6px 20px rgba(59,130,246,.35);
  transform: translateY(-2px);
}
.btn-exp { display: flex; align-items: center; justify-content: center; }

/* 导入弹窗 */
.modal-mask { position: fixed; inset: 0; z-index: 30000; background: rgba(0,0,0,.3); display: flex; align-items: center; justify-content: center; }
.modal-card { background: #fff; border-radius: 24px; width: 1520px; max-height: 80vh; display: flex; flex-direction: column; box-shadow: 0 32px 100px rgba(0,0,0,.18); border: 2px solid #e5e7eb; overflow: hidden; }
.modal-head { display: flex; align-items: center; justify-content: space-between; padding: 40px 56px; border-bottom: 2px solid #f0f1f5; font-size: 32px; font-weight: 600; color: #1a1a2e; background: #fafbfc; }
.modal-close { width: 64px; height: 64px; border: none; background: transparent; font-size: 44px; cursor: pointer; color: #b0b5c0; border-radius: 12px; transition: all .15s; line-height: 1; }
.modal-close:hover { background: #eef1f6; color: #555; }
.modal-tabs { display: flex; padding: 0 40px; border-bottom: 2px solid #f0f1f3; gap: 0; }
.modal-tabs button { border: none; background: none; padding: 20px 32px; cursor: pointer; font-size: 26px; color: #6b7280; border-bottom: 4px solid transparent; font-family: inherit; transition: .12s; }
.modal-tabs button:hover { color: #333; }
.modal-tabs button.on { color: #2f6ef2; border-bottom-color: #2f6ef2; font-weight: 500; }
.modal-body { padding: 48px 56px; overflow-y: auto; }
.modal-row { display: flex; align-items: center; gap: 24px; margin-bottom: 32px; font-size: 28px; }
.modal-row label { width: 160px; color: #6b7280; flex-shrink: 0; }
.modal-row.flex-col { flex-direction: column; align-items: stretch; }
.modal-row.flex-col label { width: auto; margin-bottom: 8px; }
.modal-row input[type="file"] { font-size: 24px; }
.sql-textarea { resize: vertical; min-height: 160px; font-family: 'Consolas','Monaco',monospace !important; font-size: 24px; line-height: 1.5; }
.modal-hint { color: #9ca3af; font-size: 22px; margin-top: 8px; }
.modal-foot { display: flex; gap: 24px; justify-content: flex-end; padding: 36px 56px; border-top: 2px solid #f0f1f5; background: #fafbfc; }

/* 草稿/模板列表项 */
.draft-item { display: flex; align-items: center; padding: 32px 48px; cursor: pointer; border-bottom: 2px solid #f3f4f6; transition: background .12s; gap: 20px; }
.draft-info { flex: 1; min-width: 0; }
.draft-title { font-size: 30px; font-weight: 500; color: #1e293b; }
.draft-meta { font-size: 26px; color: #94a3b8; margin-top: 8px; }
.draft-del { width: 64px; height: 64px; border: none; background: transparent; font-size: 40px; color: #c8ccd4; cursor: pointer; border-radius: 12px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.draft-del:hover { background: #fef2f2; color: #ef4444; }
.draft-action { width: 68px; height: 68px; border: none; background: transparent; font-size: 32px; color: #a0a6b4; cursor: pointer; border-radius: 12px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; transition: all .15s; }
.draft-action:hover { background: #eef2ff; color: #6366f1; }

/* 弹窗内专用按钮 */
.modal-card .fmt-btn,
.modal-card .fmt-btn-preview {
  height: 76px; min-width: 160px; padding: 0 56px; border-radius: 16px; font-size: 28px; font-weight: 500;
  white-space: nowrap; box-sizing: border-box;
}
.modal-card .fmt-btn {
  background: #f8f9fb; color: #4b5563; border-color: #e2e5ea;
}
.modal-card .fmt-btn:hover { background: #eef0f4; border-color: #c9cdd6; color: #333; }
.modal-card .fmt-btn-preview { font-weight: 600; letter-spacing: .6px; }

/* 右键菜单 */
.ctx-menu { position: fixed; z-index: 9998; background: #fff; border: 2px solid #e0e3e8; border-radius: 16px; box-shadow: 0 16px 64px rgba(0,0,0,.12); padding: 12px 0; min-width: 280px; }
.ctx-item { padding: 14px 32px; font-size: 24px; cursor: pointer; color: #374151; }
.ctx-item:hover { background: #f0f4ff; color: #2f6ef2; }
.ctx-sep { height: 2px; background: #e5e7eb; margin: 8px 16px; }

/* Toast */
.sr-toast { position: fixed; bottom: 64px; left: 50%; transform: translateX(-50%); padding: 20px 56px; border-radius: 16px; font-size: 26px; font-weight: 500; z-index: 9999; pointer-events: none; box-shadow: 0 8px 48px rgba(0,0,0,.15); }
.sr-toast.success { background: #ecfdf5; color: #065f46; border: 2px solid #a7f3d0; }
.sr-toast.error { background: #fef2f2; color: #991b1b; border: 2px solid #fecaca; }
.sr-toast.warn { background: #fffbeb; color: #92400e; border: 2px solid #fde68a; }
.sr-toast.info { background: #eff6ff; color: #1e40af; border: 2px solid #bfdbfe; }
.toast-fade-enter-active { transition: all .25s ease-out; }
.toast-fade-leave-active { transition: all .2s ease-in; }
.toast-fade-enter-from { opacity: 0; transform: translateX(-50%) translateY(24px); }
.toast-fade-leave-to { opacity: 0; transform: translateX(-50%) translateY(16px); }
</style>