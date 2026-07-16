<template>
  <div class="csd-page">
    <!-- 顶部固定信息栏 -->
    <div class="csd-info-bar">
      <div class="csd-info-item">
        <span class="csd-info-label">本次代号</span>
        <span class="csd-info-value code">{{ record?.codeName || '-' }}</span>
      </div>
      <span class="csd-info-sep"></span>
      <div class="csd-info-item">
        <span class="csd-info-label">客户名称</span>
        <span class="csd-info-value">{{ record?.clientName || '-' }}</span>
      </div>
      <span class="csd-info-sep"></span>
      <div class="csd-info-item">
        <span class="csd-info-label">客户编号</span>
        <span class="csd-info-value">{{ record?.clientCode || '-' }}</span>
      </div>
      <span class="csd-info-sep"></span>
      <div class="csd-info-item">
        <span class="csd-info-label">择样编号</span>
        <span class="csd-info-value">{{ record?.selectionId || '-' }}</span>
      </div>
      <span class="csd-info-sep"></span>
      <div class="csd-info-item">
        <span class="csd-info-label">录单人员</span>
        <span class="csd-info-value">{{ record?.recorder || '-' }}</span>
      </div>
      <span class="csd-info-sep"></span>
      <div class="csd-info-item" style="text-align:center">
        <span class="csd-info-label">录单日期</span>
        <span class="csd-info-value">{{ (record?.recordDate || '-').replace('T', ' ') }}</span>
      </div>
      <span class="csd-info-sep"></span>
      <div class="csd-info-item" style="flex:1;min-width:0">
        <span class="csd-info-label">备注</span>
        <span class="csd-info-value" style="overflow:hidden;text-overflow:ellipsis">{{ record?.remark || '-' }}</span>
      </div>

      <div class="csd-info-actions">
        <button class="csd-btn csd-btn-ghost" :class="{ active: !largeMode }" title="小图模式" @click="largeMode = false">
          <Minimize2 :size="22" /> 小图
        </button>
        <button class="csd-btn csd-btn-ghost" :class="{ active: largeMode }" title="大图模式" @click="largeMode = true">
          <Maximize2 :size="22" /> 大图
        </button>
        <button class="csd-btn csd-btn-ghost" :title="cardExpanded ? '收起卡片' : '展开卡片'" @click="cardExpanded = !cardExpanded">
          <ChevronsDown v-if="cardExpanded" :size="22" />
          <ChevronsUp v-else :size="22" />
        </button>
        <button class="csd-btn csd-btn-ghost" :title="formVisible ? '隐藏展示区' : '显示展示区'" @click="formVisible = !formVisible">
          <EyeOff v-if="formVisible" :size="22" />
          <Eye v-else :size="22" />
        </button>
      </div>
    </div>

    <!-- 表单卡片 -->
    <div class="csd-card csd-form-card sidebar-collapsed" :class="{ 'csd-card-expanded': cardExpanded }" v-show="formVisible">

      <div class="csd-form-body">
        <div class="csd-form-scroll">
          <div class="csd-form-grid">
            <div class="csd-form-field csd-fx-w200">
              <label class="csd-form-label">公司编号</label>
              <input class="csd-form-input" :value="sample?.sampleCode || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-price">
              <label class="csd-form-label label-red">出厂价</label>
              <input class="csd-form-input price-red" :value="sample?.factoryPrice != null ? sample.factoryPrice : ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-carton">
              <label class="csd-form-label">内盒/外箱装量</label>
              <div class="csd-form-double">
                <input class="csd-form-input" :value="sample?.innerBoxCount != null ? sample.innerBoxCount : ''" readonly />
                <input class="csd-form-input" :value="sample?.cartonCapacity != null ? sample.cartonCapacity : ''" readonly />
              </div>
            </div>
            <div class="csd-form-field csd-fx-weight">
              <label class="csd-form-label">外箱毛/净</label>
              <div class="csd-form-double">
                <input class="csd-form-input" :value="sample?.cartonGrossWeight != null ? sample.cartonGrossWeight : '0'" readonly />
                <input class="csd-form-input" :value="sample?.cartonNetWeight != null ? sample.cartonNetWeight : '0'" readonly />
              </div>
            </div>
            <div class="csd-form-field">
              <label class="csd-form-label">样品名称</label>
              <input class="csd-form-input" :value="sample?.sampleName || ''" readonly />
            </div>
            <div class="csd-form-row-break"></div>
            <div class="csd-form-field csd-fx-w200">
              <label class="csd-form-label">出厂货号</label>
              <input class="csd-form-input" :value="sample?.factoryCode || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-price">
              <label class="csd-form-label label-red">报出价</label>
              <input class="csd-form-input price-red" :value="sample?.calculatedPrice != null ? sample.calculatedPrice : ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-packaging">
              <label class="csd-form-label">中文/英文包装</label>
              <div class="csd-form-double">
                <input class="csd-form-input" :value="sample?.packagingCn || ''" readonly />
                <input class="csd-form-input" :value="sample?.packagingEn || ''" readonly />
              </div>
            </div>
            <div class="csd-form-field csd-fx-weight csd-fx-weight-sample">
              <label class="csd-form-label">样品毛/净</label>
              <div class="csd-form-double">
                <input class="csd-form-input" :value="sample?.sampleGrossWeight != null ? sample.sampleGrossWeight : '0'" readonly />
                <input class="csd-form-input" :value="sample?.sampleNetWeight != null ? sample.sampleNetWeight : '0'" readonly />
              </div>
            </div>
            <div class="csd-form-field csd-fx-certification">
              <label class="csd-form-label">产品认证</label>
              <input class="csd-form-input" :value="sample?.certification || ''" readonly />
            </div>
            <div class="csd-form-row-break"></div>
            <div class="csd-form-field csd-fx-code">
              <label class="csd-form-label">编号/种类</label>
              <div class="csd-form-double">
                <input class="csd-form-input" :value="sample?.categoryCode || ''" readonly />
                <input class="csd-form-input" :value="sample?.category || ''" readonly />
              </div>
            </div>
            <div class="csd-form-field csd-fx-price csd-fx-price2">
              <label class="csd-form-label label-red">报出价2</label>
              <input class="csd-form-input price-red" :value="sample?.taxPrice2 != null ? sample.taxPrice2 : ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-volume">
              <label class="csd-form-label label-w100">Cuft / CBM</label>
              <div class="csd-form-double">
                <input class="csd-form-input" :value="sample?.cartonMaterialVolume != null ? sample.cartonMaterialVolume : ''" readonly />
                <input class="csd-form-input" :value="sample?.cartonVolume != null ? sample.cartonVolume : ''" readonly />
              </div>
            </div>
            <div class="csd-form-field csd-fx-color">
              <label class="csd-form-label">颜色</label>
              <input class="csd-form-input" :value="sample?.color || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-remark-cn">
              <label class="csd-form-label">中文备注</label>
              <input class="csd-form-input" :value="sample?.remark || ''" readonly />
            </div>
            <div class="csd-form-row-break"></div>
            <div class="csd-form-field csd-fx-spec">
              <label class="csd-form-label">外箱规格</label>
              <div class="csd-form-triple">
                <input class="csd-form-input" :value="sample?.cartonLength != null ? sample.cartonLength : ''" readonly />
                <input class="csd-form-input" :value="sample?.cartonWidth != null ? sample.cartonWidth : ''" readonly />
                <input class="csd-form-input" :value="sample?.cartonHeight != null ? sample.cartonHeight : ''" readonly />
              </div>
            </div>
            <div class="csd-form-field csd-fx-supplier csd-fx-booth">
              <label class="csd-form-label">摊位号</label>
              <input class="csd-form-input" :value="sample?.boothNo || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-supplier csd-fx-supplier-name">
              <label class="csd-form-label">厂商名称</label>
              <input class="csd-form-input" :value="sample?.name || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-mobile">
              <label class="csd-form-label">手机</label>
              <input class="csd-form-input" :value="sample?.mobile1 || ''" readonly />
            </div>
            <div class="csd-form-row-break"></div>
            <div class="csd-form-field csd-fx-spec">
              <label class="csd-form-label">包装规格</label>
              <div class="csd-form-triple">
                <input class="csd-form-input" :value="sample?.packageLength != null ? sample.packageLength : ''" readonly />
                <input class="csd-form-input" :value="sample?.packageWidth != null ? sample.packageWidth : ''" readonly />
                <input class="csd-form-input" :value="sample?.packageHeight != null ? sample.packageHeight : ''" readonly />
              </div>
            </div>
            <div class="csd-form-field csd-fx-qq">
              <label class="csd-form-label">QQ</label>
              <input class="csd-form-input" :value="sample?.qq || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-contact">
              <label class="csd-form-label">联系人</label>
              <input class="csd-form-input" :value="sample?.contact1 || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-contact-phone">
              <label class="csd-form-label">见客手机</label>
              <input class="csd-form-input" :value="sample?.visitorMobile || ''" readonly />
            </div>
            <div class="csd-form-row-break"></div>
            <div class="csd-form-field csd-fx-spec csd-fx-spec-sample">
              <label class="csd-form-label">样品规格</label>
              <div class="csd-form-triple">
                <input class="csd-form-input" :value="sample?.sampleLength != null ? sample.sampleLength : ''" readonly />
                <input class="csd-form-input" :value="sample?.sampleWidth != null ? sample.sampleWidth : ''" readonly />
                <input class="csd-form-input" :value="sample?.sampleHeight != null ? sample.sampleHeight : ''" readonly />
              </div>
            </div>
            <div class="csd-form-field csd-fx-phone">
              <label class="csd-form-label">电话</label>
              <input class="csd-form-input" :value="sample?.phone1 || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-mfr-code">
              <label class="csd-form-label">厂商编号</label>
              <input class="csd-form-input" :value="sample?.manufacturerCode || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-remark-en">
              <label class="csd-form-label">英文备注</label>
              <input class="csd-form-input" :value="sample?.remarkEn || ''" readonly />
            </div>
            <div class="csd-form-row-break"></div>
            <div class="csd-form-field csd-fx-profit">
              <label class="csd-form-label">报价利润</label>
              <div class="csd-unit-wrapper">
                <input class="csd-form-input" :value="psCurrent.profitRate" readonly />
                <span class="csd-unit-suffix">%</span>
              </div>
            </div>
            <div class="csd-form-field csd-fx-rate">
              <label class="csd-form-label">汇率</label>
              <input class="csd-form-input" :value="psCurrent.exchangeRate" readonly />
            </div>
            <div class="csd-form-field csd-fx-markup">
              <label class="csd-form-label">加价</label>
              <input class="csd-form-input" :value="psCurrent.markup" readonly />
            </div>
            <div class="csd-form-field csd-fx-other-remark">
              <label class="csd-form-label">其他备注</label>
              <input class="csd-form-input" :value="sample?.otherRemark || ''" readonly />
            </div>
            <div class="csd-form-row-break"></div>
            <div class="csd-form-field csd-fx-boxcnt">
              <label class="csd-form-label">箱数</label>
              <input class="csd-form-input" :value="sample?.boxCount || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-date-add">
              <label class="csd-form-label">登记日期</label>
              <input class="csd-form-input" :value="sample?.addDate || ''" readonly />
            </div>
            <div class="csd-form-field csd-fx-date-mod">
              <label class="csd-form-label">修改日期</label>
              <input class="csd-form-input" :value="sample?.modifyDate || ''" readonly />
            </div>
            <div class="csd-form-row-break"></div>
        </div>
      </div>

        <div class="csd-image-strip">
          <template v-if="images.length > 0">
            <div class="csd-image-strip-single">
              <img
                :src="images[stripIndex]?.url"
                @click.stop="viewImage"
                style="cursor:pointer"
              />
              <button v-if="images.length > 1" class="csd-strip-nav csd-strip-prev" @click="stripPrev">
                <ChevronLeft :size="22" />
              </button>
              <button v-if="images.length > 1" class="csd-strip-nav csd-strip-next" @click="stripNext">
                <ChevronRight :size="22" />
              </button>
              <span class="csd-strip-counter">{{ stripIndex + 1 }} / {{ images.length }}</span>
            </div>
          </template>
          <template v-else>
            <span class="csd-image-strip-empty" style="cursor:pointer" @click="viewImage">暂无图片</span>
          </template>
        </div>
      </div>
    </div>

    <!-- 工具栏卡片 -->
    <div class="csd-card csd-toolbar-card">
      <div class="csd-toolbar-row">
        <!-- 添加区 -->
        <div class="csd-search csd-search-add">
          <input
            v-model="addInput"
            placeholder="输入公司编号添加..."
            @keyup.enter="onAdd"
          />
        </div>
        <button class="csd-btn csd-btn-primary" :disabled="!addInput.trim() || addLoading" @click="onAdd">
          <Plus :size="22" /> 添加
        </button>
        <button class="csd-btn csd-btn-primary" @click="onBatchAdd">
          <FilePlus :size="22" /> 批量添加
        </button>
        <div class="csd-toolbar-sep"></div>
        <!-- 编辑区 -->
        <button class="csd-btn csd-btn-primary" :disabled="!sample" @click="onModify">
          <Pencil :size="22" /> 修改
        </button>
        <button class="csd-btn csd-btn-danger" :disabled="!sample" @click="onDelete">
          <Trash2 :size="22" /> 删除
        </button>
        <button class="csd-btn csd-btn-danger" @click="onBatchDelete">
          <Trash2 :size="22" /> 批量删除
        </button>
        <button class="csd-btn csd-btn-primary" @click="onPriceSetting">
          <Settings :size="22" /> 报价设置
        </button>
        <div class="csd-toolbar-sep"></div>
        <!-- 操作区 -->
        <button class="csd-btn csd-btn-ghost" :disabled="checkedRows.length === 0" @click="onSendSms">
          <Send :size="22" /> 群发短信
        </button>
        <button class="csd-btn csd-btn-ghost" @click="onPrintLabel">
          <Tag :size="22" /> 标签打印
        </button>
        <button class="csd-btn csd-btn-ghost" @click="onExportImage">
          <ImageDown :size="22" /> 导出图片/报价
        </button>
        <div class="csd-toolbar-sep"></div>
        <!-- 库存区 -->
        <button class="csd-btn csd-btn-primary" :disabled="checkedRows.length === 0" @click="onSubmitToInventory">
          <PackageOpen :size="22" /> 提交入库
        </button>
        <button class="csd-btn csd-btn-primary" :disabled="checkedRows.length === 0" @click="onSubmitToOutbound">
          <Package :size="22" /> 提交出库
        </button>
        <div class="csd-toolbar-sep"></div>
        <!-- 定位区 -->
        <div class="csd-search">
          <Crosshair :size="22" />
          <input
            v-model="locateInput"
            placeholder="定位(货号)..."
            @keyup.enter="onLocate"
          />
          <button v-if="locateInput" class="csd-search-clear" @click="locateInput='';locateCursor=-1">&times;</button>
        </div>
        <button class="csd-btn csd-btn-ghost" @click="onLocate">
          <Crosshair :size="22" /> 定位
        </button>
        <div class="csd-toolbar-sep"></div>
        <!-- 其他功能下拉 -->
        <div class="csd-dropdown">
          <button class="csd-btn csd-btn-ghost" @click="toggleMenu">
            <MoreHorizontal :size="22" /> 其他功能
          </button>
          <div v-if="menuOpen" class="csd-dropdown-menu" @click.stop>
            <button class="csd-dropdown-item" @click="openReportDesigner">报表设计器</button>
            <button class="csd-dropdown-item" @click="onPrintPrice">报价打印</button>
            <button class="csd-dropdown-item" @click="onFilterDuplicate">筛选重复</button>
            <button class="csd-dropdown-item" @click="onRevertSubmissions">撤回提交库存</button>
            <button class="csd-dropdown-item" @click="onViewDeletedRecords">查看删除记录</button>
            <button class="csd-dropdown-item" @click="onCodeSearch">按编号批量查询</button>
          </div>
        </div>
        <div class="csd-toolbar-sep"></div>
        <!-- 筛选区 -->
        <div class="csd-search csd-search-floor">
          <input
            v-model="floorInput"
            @keyup.enter="onFloorFilter"
          />
        </div>
        <button class="csd-btn csd-btn-ghost" @click="onFloorFilter">
          选择楼层
        </button>
        <div class="csd-toolbar-sep"></div>
        <!-- 搜索区 -->
        <div class="csd-search">
          <Search :size="22" />
          <input
            v-model="searchKeyword"
            placeholder="模糊搜索..."
            @keyup.enter="onSearch"
          />
        </div>
        <button class="csd-btn csd-btn-primary" @click="onSearch">
          <Search :size="22" /> 查询
        </button>
        <button v-if="searchKeyword" class="csd-btn csd-btn-ghost" @click="clearSearch">
          <X :size="22" /> 清除
        </button>
        <div class="csd-toolbar-sep"></div>
        <!-- 卡片模式专用按钮 -->
        <button v-if="cardMode" class="csd-btn csd-btn-ghost" @click="hideFactoryPrice = !hideFactoryPrice">
          <EyeOff :size="22" /> {{ hideFactoryPrice ? '显示' : '隐藏' }}
        </button>
        <div v-if="cardMode" class="csd-toolbar-sep"></div>
        <!-- 图片/视频筛选 -->
        <div class="csd-filter-group">
          <ImageIcon :size="26" class="csd-filter-icon" :class="{ active: hasImageFilter || noImageFilter }" />
          <span class="csd-filter-label">图片</span>
          <div class="csd-switch">
            <button class="csd-switch-btn csd-switch-on" :class="{ active: hasImageFilter }" @click="toggleHasImage">有</button>
            <button class="csd-switch-btn csd-switch-off" :class="{ active: noImageFilter }" @click="toggleNoImage">无</button>
          </div>
        </div>
        <div class="csd-filter-group">
          <Video :size="26" class="csd-filter-icon" :class="{ active: hasVideoFilter || noVideoFilter }" />
          <span class="csd-filter-label">视频</span>
          <div class="csd-switch">
            <button class="csd-switch-btn csd-switch-on" :class="{ active: hasVideoFilter }" @click="toggleHasVideo">有</button>
            <button class="csd-switch-btn csd-switch-off" :class="{ active: noVideoFilter }" @click="toggleNoVideo">无</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 导出图片模态框 -->
    <Teleport to="body">
      <div v-if="exportModalOpen" class="modify-overlay" style="background:transparent">
        <div class="modify-dialog" style="width:96vw;max-width:1600px;max-height:92vh">
          <div class="modify-header">
            <h3 style="font-size:32px">导出图片/报价</h3>
            <div style="display:flex;align-items:center;gap:6px">
              <button v-if="exportStatus === 'loading'" class="export-minimize-btn" title="最小化到右下角" @click="onMinimizeExport">－</button>
              <button class="modify-close" :disabled="exportStatus === 'loading'" @click="onCloseExport">✕</button>
            </div>
          </div>
          <div class="modify-body" style="padding:40px;overflow:visible;font-size:22px">
            <p style="margin:0 0 24px 0;color:#6b7280;font-size:22px">
              已选择 <strong>{{ checkedRows.length }}</strong> 个样品，其中 <strong style="color:#059669">{{ checkedImageCount.withImg }}</strong> 条有图，<strong style="color:#dc2626">{{ checkedImageCount.withoutImg }}</strong> 条无图
            </p>
            <!-- 左右两栏 -->
            <div style="display:flex;gap:32px">
              <!-- 左栏：图片导出 -->
              <div style="flex:1;min-width:0">
                <label style="display:flex;align-items:center;gap:14px;padding:0 0 20px 0;cursor:pointer">
                  <input type="checkbox" v-model="exportIncludeImages" :disabled="exportStatus === 'loading'" style="accent-color:#007aff;width:24px;height:24px;flex-shrink:0" />
                  <span style="font-size:22px;font-weight:600;color:#1d1d1f">导出图片</span>
                </label>
                <template v-if="exportIncludeImages">
                  <label
                    :style="{ borderColor: exportNamingMode === 'sampleCode' ? '#007aff' : '#d1d5db', background: exportNamingMode === 'sampleCode' ? 'rgba(0,122,255,0.05)' : '#fff', boxShadow: exportNamingMode === 'sampleCode' ? '0 0 0 4px rgba(0,122,255,0.12)' : 'none' }"
                    style="display:flex;align-items:center;gap:20px;padding:22px 22px;border:2px solid #d1d5db;border-radius:14px;margin-bottom:14px;cursor:pointer;transition:all 0.2s">
                    <input type="radio" v-model="exportNamingMode" value="sampleCode" :disabled="exportStatus === 'loading'" style="accent-color:#007aff;width:24px;height:24px;flex-shrink:0" />
                    <div style="font-weight:600;font-size:22px;color:#1d1d1f">按公司编号命名</div>
                  </label>
                  <label
                    :style="{ borderColor: exportNamingMode === 'factoryCode' ? '#007aff' : '#d1d5db', background: exportNamingMode === 'factoryCode' ? 'rgba(0,122,255,0.05)' : '#fff', boxShadow: exportNamingMode === 'factoryCode' ? '0 0 0 4px rgba(0,122,255,0.12)' : 'none' }"
                    style="display:flex;align-items:center;gap:20px;padding:22px 22px;border:2px solid #d1d5db;border-radius:14px;margin-bottom:14px;cursor:pointer;transition:all 0.2s">
                    <input type="radio" v-model="exportNamingMode" value="factoryCode" :disabled="exportStatus === 'loading'" style="accent-color:#007aff;width:24px;height:24px;flex-shrink:0" />
                    <div style="font-weight:600;font-size:22px;color:#1d1d1f">按出厂货号命名</div>
                  </label>
                  <p style="margin:20px 0 0 0;font-weight:600;font-size:22px;color:#1d1d1f">图片文件命名</p>
                  <input
                    v-model="exportFolderName"
                    :disabled="exportStatus === 'loading'"
                    :placeholder="`择样图片_${codeName}`"
                    style="width:100%;height:52px;margin-top:12px;padding:0 16px;border:2px solid #d1d5db;border-radius:12px;font-size:22px;outline:none;color:#1d1d1f;box-sizing:border-box"
                  />
                </template>
                <div v-else style="padding:20px;color:#9ca3af;font-size:20px;text-align:center;border:2px dashed #d1d5db;border-radius:14px;margin-top:8px">
                  勾选上方选项以导出图片
                </div>
              </div>
              <!-- 分隔线 -->
              <div style="width:1px;background:#e5e7eb;flex-shrink:0;align-self:stretch"></div>
              <!-- 右栏：报价导出 -->
              <div style="flex:1;min-width:0">
                <label style="display:flex;align-items:center;gap:14px;padding:0 0 20px 0;cursor:pointer">
                  <input type="checkbox" v-model="exportIncludePricing" :disabled="exportStatus === 'loading'" style="accent-color:#007aff;width:24px;height:24px;flex-shrink:0" />
                  <span style="font-size:22px;font-weight:600;color:#1d1d1f">导出报价</span>
                </label>
                <template v-if="exportIncludePricing">
                  <p style="margin:0 0 14px 0;font-weight:600;font-size:22px;color:#1d1d1f">选择报价模板</p>
                  <select
                    v-model="exportPricingTemplateId"
                    :disabled="exportStatus === 'loading'"
                    style="width:100%;height:52px;padding:0 16px;border:2px solid #d1d5db;border-radius:12px;font-size:22px;outline:none;color:#1d1d1f;background:#fff;box-sizing:border-box;appearance:auto;cursor:pointer">
                    <option :value="null" disabled>请选择模板...</option>
                    <option v-for="t in pricingTemplates" :key="t.id" :value="t.id">{{ t.title }}</option>
                  </select>
                  <p style="margin:18px 0 0 0;font-weight:600;font-size:22px;color:#1d1d1f">报表文件命名</p>
                  <input
                    v-model="exportPricingFileName"
                    :disabled="exportStatus === 'loading'"
                    placeholder="报价明细"
                    style="width:100%;height:52px;margin-top:12px;padding:0 16px;border:2px solid #d1d5db;border-radius:12px;font-size:22px;outline:none;color:#1d1d1f;box-sizing:border-box"
                  />
                </template>
                <div v-else style="padding:20px;color:#9ca3af;font-size:20px;text-align:center;border:2px dashed #d1d5db;border-radius:14px;margin-top:8px">
                  勾选上方选项以导出报价报表
                </div>
              </div>
            </div>
            <!-- 进度条 -->
            <div v-if="exportStatus !== 'idle'" style="margin:36px 0 0 0">
              <!-- 同时导出报价+图片：双进度条 -->
              <template v-if="exportIncludePricing && exportIncludeImages">
                <div style="margin-bottom:20px">
                  <div style="display:flex;justify-content:space-between;margin-bottom:6px;font-size:18px;color:#6b7280">
                    <span>报价报表</span>
                    <span>{{ exportPricingProgress }}%</span>
                  </div>
                  <div style="height:12px;background:#e5e7eb;border-radius:6px;overflow:hidden">
                    <div :style="{ width: exportPricingProgress + '%', height:'100%', background:'#007aff', borderRadius:'6px', transition:'width 0.3s ease' }"></div>
                  </div>
                </div>
                <div>
                  <div style="display:flex;justify-content:space-between;margin-bottom:6px;font-size:18px;color:#6b7280">
                    <span>图片压缩包</span>
                    <span>{{ exportImageProgress }}%</span>
                  </div>
                  <div style="height:12px;background:#e5e7eb;border-radius:6px;overflow:hidden">
                    <div :style="{ width: exportImageProgress + '%', height:'100%', background:'#34c759', borderRadius:'6px', transition:'width 0.3s ease' }"></div>
                  </div>
                </div>
              </template>
              <!-- 单一导出：单进度条 -->
              <template v-else>
                <div style="display:flex;justify-content:space-between;margin-bottom:10px;font-size:20px;color:#6b7280">
                  <span>{{ exportStatusText }}</span>
                  <span>{{ exportCurrent }} / {{ exportTotal }}</span>
                </div>
                <div style="height:16px;background:#e5e7eb;border-radius:8px;overflow:hidden">
                  <div :style="{ width: exportProgress + '%', height:'100%', background:'#007aff', borderRadius:'8px', transition:'width 0.3s ease' }"></div>
                </div>
              </template>
            </div>
            <div style="display:flex;gap:20px;justify-content:flex-end;margin-top:32px">
              <button class="csd-btn csd-btn-ghost" @click="onCancelExport">{{ exportStatus === 'loading' ? '取消导出' : '取消' }}</button>
              <button class="csd-btn csd-btn-primary" :disabled="exportStatus === 'loading' || (!exportIncludeImages && !exportIncludePricing) || (exportIncludePricing && !exportPricingTemplateId)" @click="startExport">
                {{ exportStatus === 'idle' ? '开始导出' : '导出中...' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 导出最小化浮动小窗 -->
    <Teleport to="body">
      <div
        v-for="task in exportTasks"
        :key="task.id"
        class="export-float"
        :style="{ bottom: `${16 + exportTasks.indexOf(task) * 104}px` }"
        @click="onRestoreExport"
      >
        <div class="export-float-icon">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="export-float-spin">
            <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
          </svg>
        </div>
        <div class="export-float-info">
          <span class="export-float-title">导出中</span>
          <span class="export-float-progress">{{ task.progress }}%</span>
        </div>
      </div>
    </Teleport>

    <!-- 表格卡片 -->
    <div class="csd-card csd-table-card">
      <div v-show="!cardMode" ref="tableWrapRef" class="csd-table-wrap" :class="{ 'csd-table-large': largeMode }">
        <vxe-grid v-if="prefReady"
          ref="gridRef"
          :id="gridStorageKey"
          :columns="allColumns"
          :data="tableData"
          :loading="tableLoading"
          :height="tableWrapHeight"
          :toolbar-config="{ custom: true }"
          :custom-config="{ storage: true }"
          :column-config="{ resizable: true, drag: true, isMaximized: true }"
          :row-config="{ isHover: true, isCurrent: true, keyField: 'itemId' }"
          :checkbox-config="{ highlight: true, checkField: 'checkbox' }"
          :cell-config="{ height: largeMode ? 200 : 100 }"
          :sort-config="{ trigger: 'header', remote: true }"
          :scroll-y="{ enabled: true, gt: 0, oSize: 30, rSize: 100, rHeight: largeMode ? 200 : 100 }"
          :scroll-x="{ enabled: true, gt: 0 }"
          :virtual-y-config="{ enabled: true, gt: 0 }"
          :virtual-x-config="{ enabled: true, gt: 0 }"
          :optimization="{ animat: false, delayHover: 300, scrollX: { gt: 0, oSize: 0, rSize: 24 }, scrollY: { gt: 0, oSize: 30, rSize: 100, rHeight: largeMode ? 200 : 100 } }"
          :border="true"
          :header-cell-style="gridHeaderStyle"
          :cell-style="gridCellStyle"
          @cell-click="onCellClick"
          @sort-change="onSortChange"
          @checkbox-change="onCheckboxChange"
          @checkbox-all="onCheckboxAll"
          @resizable-change="saveGridPrefs"
          @custom="onCustomChange"
          @column-dragstart="onColumnDragStart"
          @column-dragend="onColumnDragEnd"
        >
          <template #checkbox_header>
            <div class="csd-checkbox-header">
              <input
                type="checkbox"
                :checked="allHeaderChecked === true"
                :indeterminate.prop="allHeaderChecked === 'indeterminate'"
                @change="onHeaderCheckAll($event.target.checked)"
              />
              <span class="csd-checkbox-sort">
                <span class="csd-sort-arrow" :class="{ 'csd-sort-active': checkboxSortOrder === 1 }" @click.stop="onCheckboxHeaderSort(checkboxSortOrder === 1 ? 'none' : 'asc')">▲</span>
                <span class="csd-sort-arrow" :class="{ 'csd-sort-active': checkboxSortOrder === 2 }" @click.stop="onCheckboxHeaderSort(checkboxSortOrder === 2 ? 'none' : 'desc')">▼</span>
              </span>
            </div>
          </template>
          <template #image="{ row }">
            <div class="csd-thumb-wrap" @mouseenter="showThumbTooltip($event, row)" @mouseleave="hideThumbTooltip">
              <img v-if="row.thumbnail" :src="'/thumbnails/' + row.thumbnail" :style="{ width: largeMode ? '240px' : '90px', height: largeMode ? '240px' : '90px', objectFit: 'cover', borderRadius: '4px', cursor: 'pointer' }" @click.stop="openPhotoModalFor(row)" />
              <span v-else :style="{ fontSize: largeMode ? '16px' : '11px', color: '#aaa', cursor: 'pointer' }" @click.stop="openPhotoModalFor(row)">无图</span>
            </div>
          </template>
          <template #showroomReplenished="{ row }">
            <input type="checkbox" :checked="!!row.showroomReplenished" style="width:30px;height:30px;cursor:pointer" @click.stop @change="e => onToggleFlag(row, 'showroomReplenished', e.target.checked)" />
          </template>
          <template #borrowedSample="{ row }">
            <input type="checkbox" :checked="!!row.borrowedSample" style="width:30px;height:30px;cursor:pointer" @click.stop @change="e => onToggleFlag(row, 'borrowedSample', e.target.checked)" />
          </template>
          <template #submitted="{ row }">
            <span :style="{ color: row.submitted ? '#16a34a' : '#9ca3af', fontWeight: 600 }">{{ row.submitted ? '已提交' : '未提交' }}</span>
          </template>
        </vxe-grid>
      </div>

      <!-- 卡片模式 -->
      <div v-if="cardMode" ref="cardContainerRef" class="csd-card-overlay" @scroll="onCardScroll">
        <div class="csd-card-virtual-wrap" :style="{ height: cardTotalHeight + 'px' }">
          <div class="csd-card-grid" :style="{ transform: `translateY(${cardOffsetY}px)` }">
            <div v-for="row in visibleCardData" :key="row.id"
                 class="csd-card-item" :class="{ 'csd-card-selected': isCardChecked(row) }">
              <div class="csd-card-img" @click.stop="openPhotoModalFor(row)">
                <div class="csd-card-checkbox" :class="{ checked: isCardChecked(row) }" @click.stop="toggleCardSelect(row)">
                  <Check v-if="isCardChecked(row)" :size="14" />
                </div>
                <img v-if="row.thumbnail" :src="'/thumbnails/' + row.thumbnail" @error="onCardImgError" loading="lazy" decoding="async" />
                <div v-else class="csd-card-no-img"><ImageIcon :size="36" /></div>
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
                  <div style="grid-column:1/-1; display:flex; flex-direction:column; gap:2px">
                    <div style="display:flex; gap:80px">
                      <span class="csd-card-val csd-card-price" :title="row.calculatedPrice ? '¥' + row.calculatedPrice : '-'">价①: {{ row.calculatedPrice ? '¥' + row.calculatedPrice : '-' }}</span>
                      <span v-if="!hideFactoryPrice" class="csd-card-val csd-card-price" :title="row.factoryPrice ? '¥' + row.factoryPrice : '-'">厂: {{ row.factoryPrice ? '¥' + row.factoryPrice : '-' }}</span>
                    </div>
                    <span class="csd-card-val csd-card-price" :title="row.taxPrice2 ? '¥' + row.taxPrice2 : '-'">价②: {{ row.taxPrice2 ? '¥' + row.taxPrice2 : '-' }}</span>
                  </div>
                </div>
                <div class="csd-card-divider"></div>
                <div class="csd-card-fields" style="margin-top:0;grid-template-columns:1fr">
                  <span class="csd-card-val" :title="row.name">{{ row.name || '-' }}</span>
                  <span class="csd-card-val" :title="row.mobile1">{{ row.mobile1 || '-' }}</span>
                  <span class="csd-card-val" :title="row.createTime">{{ row.createTime || '-' }}</span>
                  <span class="csd-card-val" :title="row.updateTime">{{ row.updateTime || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="csd-statusbar">
        <div class="csd-select-actions">
          <button class="csd-btn csd-btn-ghost" @click="onSelectAll">全选</button>
          <button class="csd-btn csd-btn-ghost" @click="onInvertSelect">反选</button>
          <button class="csd-btn csd-btn-ghost" @click="onClearSelect">清除</button>
          <button class="csd-btn csd-btn-ghost" @click="onSelectBorrowed">全选借样</button>
          <button class="csd-btn csd-btn-ghost" @click="onSelectReplenished">全选展厅已补</button>
          <button class="csd-btn csd-btn-ghost" @click="onSelectReplenishedAndBorrowed">全选展补/借样</button>
        </div>
        <div class="csd-status-info">
          已选 <strong>{{ checkedRows.length }}</strong> / 共 <strong>{{ filteredList.length }}</strong> 条
          <span v-if="codeSearchActive" style="margin-left:12px;color:#007aff;font-size:13px;">（按编号筛选: {{ codeSearchInput }}）</span>
        </div>
        <button class="csd-btn csd-btn-primary" @click="cardMode = !cardMode">
          <LayoutGrid :size="14" /> {{ cardMode ? '列表' : '卡片' }}
        </button>
        <div class="csd-pagination">
          <span class="csd-page-size-label">每页</span>
          <select class="csd-page-size-select" v-model.number="pageSize">
            <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
          <span class="csd-page-size-label">条</span>
          <button class="csd-btn csd-btn-ghost" :disabled="currentPage <= 1" @click="goPage(1)">
            <ChevronsLeft :size="22" />
          </button>
          <button class="csd-btn csd-btn-ghost" :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">
            <ChevronLeft :size="22" />
          </button>
          <span class="csd-page-text">{{ currentPage }} / {{ totalPages }}</span>
          <button class="csd-btn csd-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">
            <ChevronRight :size="22" />
          </button>
          <button class="csd-btn csd-btn-ghost" :disabled="currentPage >= totalPages" @click="goPage(totalPages)">
            <ChevronsRight :size="22" />
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 报价设置侧滑面板 -->
  <Teleport to="body">
    <Transition name="ps-slide">
      <div v-if="priceSettingOpen" class="ps-overlay" @click.self="priceSettingOpen = false">
        <div class="ps-panel">
          <div class="ps-header">
            <span class="ps-title">报价设置</span>
            <div class="ps-tabs">
              <button :class="['ps-tab', psType === '1' && 'ps-tab-active']" @click="switchPsType('1')">报价1设置</button>
              <button :class="['ps-tab', psType === '2' && 'ps-tab-active']" @click="switchPsType('2')">报价2设置</button>
            </div>
            <button class="ps-close" @click="priceSettingOpen = false">✕</button>
          </div>
          <div class="ps-body">
            <!-- 提示条 -->
            <div v-if="psAlertShow" class="ps-alert">
              <span>备注：每车尺码小于100时，采用体积计算，大于100时采用材积计算</span>
              <button @click="psAlertShow = false">✕</button>
            </div>
            <!-- 当前公式：仅在有设置时显示 -->
            <div class="ps-section" v-if="psHasSetting1 || psHasSetting2">
              <div class="ps-section-title">当前报价公式</div>
            <table class="ps-current-table">
              <thead>
                <tr>
                  <th>报价</th><th>报价方式</th><th>利润率</th><th>总费用</th><th>汇率</th>
                  <th>小数位数</th><th>币种</th><th>每车尺寸</th><th>取舍方式</th>
                  <th>报价量数</th><th>价格小于</th><th>价格小数</th><th>使用公式</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="psHasSetting1">
                  <td>报价1</td>
                  <td>{{ psCurrent.method }}</td><td>{{ psCurrent.profitRate }}</td><td>{{ psCurrent.totalCost }}</td><td>{{ psCurrent.exchangeRate }}</td>
                  <td>{{ psCurrent.decimals }}</td><td>{{ psCurrent.currency }}</td><td>{{ psCurrent.cartonSize }}</td><td>{{ psCurrent.roundMode }}</td>
                  <td>{{ psCurrent.unit }}</td><td>{{ psCurrent.priceLessThan }}</td><td>{{ psCurrent.priceDecimals }}</td>
                  <td class="ps-formula-cell">{{ psCurrent.formula }}</td>
                </tr>
                <tr v-if="psHasSetting2">
                  <td>报价2</td>
                  <td>{{ psCurrent2.method }}</td><td>{{ psCurrent2.profitRate }}</td><td>{{ psCurrent2.totalCost }}</td><td>{{ psCurrent2.exchangeRate }}</td>
                  <td>{{ psCurrent2.decimals }}</td><td>{{ psCurrent2.currency }}</td><td>{{ psCurrent2.cartonSize }}</td><td>{{ psCurrent2.roundMode }}</td>
                  <td>{{ psCurrent2.unit }}</td><td>{{ psCurrent2.priceLessThan }}</td><td>{{ psCurrent2.priceDecimals }}</td>
                  <td class="ps-formula-cell">{{ psCurrent2.formula }}</td>
                </tr>
              </tbody>
            </table>
            </div>
            <!-- 设置表单 -->
            <div class="ps-section">
              <div class="ps-section-title">设置报价公式</div>
            <div class="ps-form-rows">
              <!-- 第1行 -->
              <div class="ps-form-row">
                <div class="ps-field">
                  <label class="ps-label"><em>*</em> 报价模板：</label>
                  <select class="ps-input" v-model="psForm.template"><option value="除法">除法</option><option value="乘法">乘法</option><option value="自定义">自定义</option></select>
                </div>
                <div class="ps-field">
                  <label class="ps-label"><em>*</em> 报价方式：</label>
                  <select class="ps-input" v-model="psForm.method"><option value="除法">除法</option><option value="乘法">乘法</option><option value="自定义">自定义</option></select>
                </div>
                <div class="ps-field">
                  <label class="ps-label">利润率(%)：</label>
                  <div class="ps-input-wrap">
                    <input class="ps-input" v-model="psForm.profitRate" />
                    <span class="ps-unit">%</span>
                  </div>
                </div>
              </div>
              <!-- 总费用 -->
              <div class="ps-form-row">
                <div class="ps-field">
                  <label class="ps-label">总费用：</label>
                  <input class="ps-input" v-model="psForm.totalCost" placeholder="整柜总费用" />
                </div>
                <div class="ps-field"></div>
                <div class="ps-field"></div>
              </div>
              <!-- 第2行 -->
              <div class="ps-form-row">
                <div class="ps-field">
                  <label class="ps-label">货币种类：</label>
                  <select class="ps-input" v-model="psForm.currencyType"><option value="RMB">RMB</option><option value="USD">USD</option><option value="HKD">HKD</option><option value="EUR">EUR</option></select>
                </div>
                <div class="ps-field">
                  <label class="ps-label">货币符号：</label>
                  <input class="ps-input" v-model="psForm.currencySymbol" />
                </div>
                <div class="ps-field">
                  <label class="ps-label">货币名称：</label>
                  <input class="ps-input" v-model="psForm.currencyName" />
                </div>
              </div>
              <!-- 第3行 -->
              <div class="ps-form-row">
                <div class="ps-field">
                  <label class="ps-label"><em>*</em> 货币汇率：</label>
                  <input class="ps-input" v-model="psForm.exchangeRate" />
                </div>
                <div class="ps-field">
                  <label class="ps-label">每车尺码：</label>
                  <div class="ps-input-group">
                    <select class="ps-input ps-input-sm" v-model="cartonSizePreset" @change="onCartonSizePresetChange">
                      <option value="18">18</option><option value="28">28</option><option value="54">54</option><option value="68">68</option><option value="86">86</option>
                      <option value="__custom__">自定义</option>
                    </select>
                    <input v-if="cartonSizePreset === '__custom__'" class="ps-input ps-input-sm" v-model="psForm.cartonSize" type="number" placeholder="输入尺码" />
                    <label class="ps-checkbox-wrap"><input type="checkbox" v-model="psForm.useCubicM" /> 使用立方米</label>
                  </div>
                </div>
                <div class="ps-field">
                  <label class="ps-label"><em>*</em> 报价加价：</label>
                  <input class="ps-input" v-model="psForm.markup" />
                </div>
              </div>
              <!-- 第4行 -->
              <div class="ps-form-row ps-form-row-full">
                <div class="ps-field">
               <div class="ps-formula-block">
                  <label class="ps-label">公式类型：</label>
                  <div class="ps-radio-group">
                    <label><input type="radio" value="multiply" v-model="psForm.formulaType" /> 乘法公式</label>
                    <label><input type="radio" value="divide" v-model="psForm.formulaType" /> 除法公式</label>
                    <label><input type="radio" value="custom" v-model="psForm.formulaType" /> 自定义公式</label>
                  </div>
                  <div class="ps-formula-preview" v-if="psForm.formulaType === 'multiply'">
                    乘法公式：((出厂价+(总费用/(每车尺码/外箱材积*外箱装量)))*(1+报价利润%)+加价)/汇率
                  </div>
                  <div class="ps-formula-preview" v-else-if="psForm.formulaType === 'divide'">
                    除法公式：((出厂价+(总费用/(每车尺码/{{ cartonVolumeLabel }}*外箱装量)))/(1-报价利润%))+报价加价)/汇率
                  </div>
                  <!-- 自定义公式面板 -->
                  <div class="ps-custom-formula" v-if="psForm.formulaType === 'custom'">
                    <div class="ps-cf-row">
                      <label class="ps-cf-label">选择字段：</label>
                      <div class="ps-cf-btns">
                        <button type="button" class="ps-cf-btn ps-cf-field" v-for="f in cfFields" :key="f" @click="appendFormula(f)">{{ f }}</button>
                      </div>
                    </div>
                    <div class="ps-cf-row">
                      <label class="ps-cf-label">运算符：</label>
                      <div class="ps-cf-btns">
                        <button type="button" class="ps-cf-btn ps-cf-op" v-for="op in cfOps" :key="op" @click="appendFormula(op)">{{ op }}</button>
                      </div>
                    </div>
                    <div class="ps-cf-row">
                      <label class="ps-cf-label">数字：</label>
                      <div class="ps-cf-btns">
                        <button type="button" class="ps-cf-btn ps-cf-num" v-for="n in cfNums" :key="n" @click="appendFormula(n)">{{ n }}</button>
                      </div>
                    </div>
                    <div
                      ref="formulaEditor"
                      class="ps-cf-input"
                      contenteditable="true"
                      @input="onFormulaInput"
                      @paste.prevent="onFormulaPaste"
                      data-placeholder="请使用上方按钮输入自定义公式，例如：(出厂价 + 运费) * (1 + 利润率/100) / 汇率"
                    ></div>
                    <div class="ps-cf-actions">
                      <button type="button" class="ps-cf-clear" @click="psForm.customFormula = ''; if (formulaEditor) formulaEditor.innerHTML = ''">清空</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
              <!-- 第5行 -->
              <div class="ps-form-row">
                <div class="ps-field">
                  <label class="ps-label">取舍方式：</label>
                  <select class="ps-input" v-model="psForm.roundMode"><option value="四舍五入">四舍五入</option><option value="全舍">全舍</option><option value="全收">全收</option></select>
                </div>
                <div class="ps-field">
                  <label class="ps-label">小数位数：</label>
                  <select class="ps-input" v-model="psForm.decimals"><option v-for="n in 7" :key="n" :value="String(n-1)">{{ n-1 }}</option></select>
                </div>
                <div class="ps-field" />
              </div>
              <!-- 第6行 -->
              <div class="ps-form-row">
                <div class="ps-field">
                  <label class="ps-label">价格小于：</label>
                  <input class="ps-input" v-model="psForm.priceLessThan" />
                </div>
                <div class="ps-field">
                  <label class="ps-label">取舍方式：</label>
                  <select class="ps-input" v-model="psForm.roundMode2"><option value="四舍五入">四舍五入</option><option value="全舍">全舍</option><option value="全收">全收</option></select>
                </div>
                <div class="ps-field">
                  <label class="ps-label">价格小数位数：</label>
                  <select class="ps-input" v-model="psForm.priceDecimals"><option v-for="n in 7" :key="n" :value="String(n-1)">{{ n-1 }}</option></select>
                </div>
              </div>
              <!-- 第7行 -->
              <div class="ps-form-row ps-form-row-full">
                <div class="ps-field">
                  <label class="ps-label">应用于：</label>
                  <div class="ps-apply">
                    <label><input type="radio" value="continue" v-model="psForm.applyTo" checked /> 应用于继续报价样品</label>
                    <label><input type="radio" value="allPriced" v-model="psForm.applyTo" /> 应用于所有已报价样品</label>
                    <label><input type="radio" value="current" v-model="psForm.applyTo" /> 当前已打钩的所有已报价样品</label>
                  </div>
                </div>
              </div>
            </div>
            <!-- 保存按钮 -->
            <div class="ps-footer">
              <button class="ps-btn-save" @click="onSavePriceSetting">保存设置</button>
            </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- 打印预览模态框 -->
  <Teleport to="body">
    <Transition name="modify-fade">
    <div v-if="btPrintOpen" class="modify-overlay" @click.self="btPrintOpen = false" style="z-index:10001;">
      <div class="modify-dialog" style="width:98vw;max-height:98vh;">
        <div class="modify-header">
          <h3 style="font-size:20px;">标签打印预览</h3>
          <div style="display:flex;gap:10px;">
            <button class="csd-btn csd-btn-primary" style="font-size:15px;padding:10px 22px;" @click="btDoPrint">打印</button>
            <button class="csd-btn csd-btn-ghost" style="font-size:17px;padding:12px 24px;" @click="btPrintOpen = false">关闭</button>
          </div>
        </div>
        <div class="modify-body" style="padding:16px;overflow:auto;background:#f5f5f5;">
          <div id="bt-print-area">
            <div
              v-for="(item, idx) in tableData"
              :key="idx"
              class="bt-label"
              :style="{ width: (parseInt(btWidth) === 58 ? '55mm' : '78mm') }"
            >
              <div
                v-for="f in btPrintFields"
                :key="f.field"
                class="bt-label-row"
                :style="{
                  fontSize: (f.fontSize || 12) + 'px',
                  fontWeight: f.fontWeightBold ? 'bold' : 'normal',
                  lineHeight: ((f.fontSize || 12) * (f.lineSpacing || 2)) + 'px'
                }"
              >
                <span v-if="f.showTitle" class="bt-label-title">{{ f.printTitle || f.label }}</span>
                <span v-if="f.showTitle" class="bt-label-colon" :style="{ fontSize: (f.fontSize || 12) + 'px' }">:</span>
                <span class="bt-label-value" :style="{ visibility: f.hideValue ? 'hidden' : 'visible' }">{{ item[f.field] !== undefined && item[f.field] !== null ? item[f.field] : '' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    </Transition>
  </Teleport>
  <!-- 全屏大图预览 -->
  <Teleport to="body">
    <div v-if="showImagePreview" class="image-preview-overlay" @click.self="closeImagePreview">
      <div class="image-preview-dialog">
        <div class="ip-header">
          <div class="ip-header-left">
            <span>图片预览</span>
            <span class="ip-count" v-if="imagePreviewList.length > 1">{{ imagePreviewIndex + 1 }} / {{ imagePreviewList.length }}</span>
          </div>
          <div class="ip-header-right">
            <button class="ip-close" @click="closeImagePreview"><X :size="20" /></button>
          </div>
        </div>
        <div class="ip-body">
          <div class="ip-main" @wheel.prevent="onIpWheel" @mousemove="onIpMouseMove" @mouseup="onIpMouseUp" @mouseleave="onIpMouseUp">
            <img :src="currentPreviewSrc"
                 draggable="false" @dragstart.prevent
                 @mousedown="onIpMouseDown"
                 @click.stop
                 :style="{ transform: `translate(${ipPanX}px, ${ipPanY}px) scale(${ipZoom})`, cursor: ipZoom <= 1 ? 'pointer' : 'grab' }" />
            <button v-if="imagePreviewList.length > 1" class="ip-nav ip-prev" @click="imagePreviewIndex = imagePreviewIndex > 0 ? imagePreviewIndex - 1 : imagePreviewList.length - 1"><ChevronLeft :size="24" /></button>
            <button v-if="imagePreviewList.length > 1" class="ip-nav ip-next" @click="imagePreviewIndex = imagePreviewIndex < imagePreviewList.length - 1 ? imagePreviewIndex + 1 : 0"><ChevronRight :size="24" /></button>
          </div>
          <div v-if="imagePreviewList.length > 1" class="ip-thumbs">
            <div
              v-for="(img, idx) in imagePreviewList" :key="img.hash || idx"
              class="ip-thumb" :class="{ active: idx === imagePreviewIndex }"
              @click="imagePreviewIndex = idx"
            >
              <img :src="img.thumbnailPath ? '/thumbnails/' + img.thumbnailPath : ''" draggable="true" @dragstart="onDetailImgDragStart" style="-webkit-user-drag:element" />
            </div>
          </div>
        </div>
        <div class="ip-footer">
          <span class="ip-name">{{ imagePreviewList[imagePreviewIndex]?.originalName || '图片' }}</span>
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

  <!-- 照片预览模态框 -->
  <Teleport to="body">
    <div v-if="showPhotoModal" class="client-photo-modal" :style="photoModalStyle">
      <div class="spm-header" @mousedown="dragModal">
        <span class="spm-header-title">明细信息预览</span>
        <button class="spm-header-close" @click="closePhotoModal">&times;</button>
      </div>
      <div class="spm-body">
        <div class="spm-top-card" v-if="photoModalSample" style="visibility:hidden">
          <div class="spm-top-card-field"><span>样品名称</span><strong>{{ photoModalSample.sampleName || '-' }}</strong></div>
          <div class="spm-top-card-field"><span>公司编号</span><strong>{{ photoModalSample.sampleCode || '-' }}</strong></div>
          <div class="spm-top-card-field"><span>出厂货号</span><strong>{{ photoModalSample.factoryCode || '-' }}</strong></div>
          <div class="spm-top-card-field" v-if="!hideFactoryPrice"><span>出厂价</span><strong class="spm-price">{{ photoModalSample.factoryPrice || '-' }}</strong></div>
          <div class="spm-top-card-field" v-if="!hideTaxPrice"><span>报出价1</span><strong class="spm-price">{{ photoModalSample.calculatedPrice || '-' }}</strong></div>
          <div class="spm-top-card-field" v-if="!hideTaxPrice"><span>报出价2</span><strong class="spm-price">{{ photoModalSample.taxPrice2 || '-' }}</strong></div>
        </div>
        <div class="spm-body-main">
        <div class="spm-body-left">
          <div class="spm-main-img-wrap">
            <img v-if="photoModalImages.length > 0"
                 :src="photoModalImages[photoModalIndex]?.hash ? '/images/view/hash/' + photoModalImages[photoModalIndex]?.hash : '/thumbnails/' + photoModalImages[photoModalIndex]?.thumbnailPath"
                 @error="onModalImgError"
                 @click="openFullPreview"
                 draggable="true" @dragstart="onDetailImgDragStart"
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
              <img :src="img.thumbnailPath ? '/thumbnails/' + img.thumbnailPath : ''" draggable="true" @dragstart="onDetailImgDragStart" style="-webkit-user-drag:element" />
            </div>
          </div>
        </div>
        <div class="spm-body-right" v-if="photoModalSample">
          <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">样品名称</span><span class="spm-field-value">{{ photoModalSample.sampleName || '-' }}</span></div></div>
          <div class="spm-field-row">
            <div class="spm-field"><span class="spm-field-label">公司编号</span><span class="spm-field-value">{{ photoModalSample.sampleCode || '-' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">出厂货号</span><span class="spm-field-value">{{ photoModalSample.factoryCode || '-' }}</span></div>
          </div>
          <div class="spm-field-row">
            <div class="spm-field" v-if="!hideFactoryPrice"><span class="spm-field-label">出厂价</span><span class="spm-field-value spm-price">{{ photoModalSample.factoryPrice || '-' }}</span></div>
            <div class="spm-field" v-if="!hideTaxPrice"><span class="spm-field-label">报出价1</span><span class="spm-field-value spm-price">{{ photoModalSample.calculatedPrice || '-' }}</span></div>
          </div>
          <div class="spm-field-row">
            <div class="spm-field" v-if="!hideTaxPrice"><span class="spm-field-label">报出价2</span><span class="spm-field-value spm-price">{{ photoModalSample.taxPrice2 || '-' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">包装方式</span><span class="spm-field-value">{{ photoModalSample.packagingCn || '-' }}</span></div>
          </div>
          <div class="spm-field-row">
            <div class="spm-field"><span class="spm-field-label">内盒/装箱量</span><span class="spm-field-value">{{ (photoModalSample.innerBoxCount != null ? photoModalSample.innerBoxCount : '0') + ' / ' + (photoModalSample.cartonCapacity||'-') }}</span></div>
            <div class="spm-field"><span class="spm-field-label">外箱规格</span><span class="spm-field-value">{{ fmt3(photoModalSample.cartonLength,photoModalSample.cartonWidth,photoModalSample.cartonHeight) }} CM</span></div>
          </div>
          <div class="spm-field-row">
            <div class="spm-field"><span class="spm-field-label">外箱毛/净重</span><span class="spm-field-value">{{ (photoModalSample.cartonGrossWeight != null ? photoModalSample.cartonGrossWeight : '0') + ' / ' + (photoModalSample.cartonNetWeight != null ? photoModalSample.cartonNetWeight : '0') + ' KG' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">包装规格</span><span class="spm-field-value">{{ fmt3(photoModalSample.packageLength,photoModalSample.packageWidth,photoModalSample.packageHeight) }} CM</span></div>
          </div>
          <div class="spm-field-row">
            <div class="spm-field"><span class="spm-field-label">英文包装</span><span class="spm-field-value">{{ photoModalSample.packagingEn || '-' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">产品规格</span><span class="spm-field-value">{{ fmt3(photoModalSample.sampleLength,photoModalSample.sampleWidth,photoModalSample.sampleHeight) }} CM</span></div>
          </div>
          <div class="spm-field-row">
            <div class="spm-field"><span class="spm-field-label">产品毛/净重</span><span class="spm-field-value">{{ (photoModalSample.sampleGrossWeight != null ? photoModalSample.sampleGrossWeight : '0') + ' / ' + (photoModalSample.sampleNetWeight != null ? photoModalSample.sampleNetWeight : '0') + ' KG' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">体积/材积</span><span class="spm-field-value">{{ (photoModalSample.cartonVolume||'-') + ' / ' + (photoModalSample.cartonMaterialVolume||'-') }}</span></div>
          </div>
          <div class="spm-field-row">
            <div class="spm-field"><span class="spm-field-label">电池信息</span><span class="spm-field-value">{{ photoModalSample.batteryInfo || '-' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">摊位号</span><span class="spm-field-value">{{ photoModalSample.boothNo || '-' }}</span></div>
          </div>
          <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">产品认证</span><span class="spm-field-value">{{ photoModalSample.certification || '-' }}</span></div></div>
          <div class="spm-field-row"><div class="spm-field spm-field-full"><span class="spm-field-label">中文备注</span><span class="spm-field-value">{{ photoModalSample.remark || '-' }}</span></div></div>
          <div class="spm-section-title" v-if="!hideFactoryInfo">厂商信息</div>
          <div class="spm-field-row" v-if="!hideFactoryInfo">
            <div class="spm-field"><span class="spm-field-label">厂商编号</span><span class="spm-field-value">{{ photoModalSample.manufacturerCode || '-' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">厂商名称</span><span class="spm-field-value">{{ photoModalSample.name || '-' }}</span></div>
          </div>
          <div class="spm-field-row" v-if="!hideFactoryInfo">
            <div class="spm-field"><span class="spm-field-label">联系人</span><span class="spm-field-value">{{ photoModalSample.contact1 || '-' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">电话</span><span class="spm-field-value">{{ photoModalSample.phone1 || '-' }}</span></div>
          </div>
          <div class="spm-field-row" v-if="!hideFactoryInfo">
            <div class="spm-field"><span class="spm-field-label">手机</span><span class="spm-field-value">{{ photoModalSample.mobile1 || '-' }}</span></div>
            <div class="spm-field"><span class="spm-field-label">QQ</span><span class="spm-field-value">{{ photoModalSample.qq || '-' }}</span></div>
          </div>
        </div>
        </div>
      </div>
      <div class="spm-footer">
        <div class="spm-toggle-group" @mousedown.stop>
          <label class="spm-toggle"><input type="checkbox" v-model="hideFactoryPrice" /> 隐藏出厂价</label>
          <label class="spm-toggle"><input type="checkbox" v-model="hideTaxPrice" /> 隐藏报出价</label>
          <label class="spm-toggle"><input type="checkbox" v-model="hideFactoryInfo" /> 隐藏厂商信息</label>
        </div>
        <div class="spm-toggle-group" style="gap:8px; margin-left: auto">
          <button class="spm-btn-close" @click="closePhotoModal">关闭</button>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- 缩略图悬浮大图 -->
  <Teleport to="body">
    <div v-if="thumbTooltip.show" class="csd-thumb-tooltip" :style="{ left: thumbTooltip.x + 'px', top: thumbTooltip.y + 'px' }">
      <img :src="thumbTooltip.src" @error="thumbTooltip.fallback && thumbTooltip.src !== thumbTooltip.fallback ? (thumbTooltip.src = thumbTooltip.fallback) : (thumbTooltip.show = false)" />
    </div>
  </Teleport>

  <!-- 修改样品资料弹窗 -->
  <Teleport to="body">
    <div v-if="modifyOpen" class="modify-overlay" @click.self="modifyOpen = false">
      <div class="modify-dialog">
        <div class="modify-header">
          <h3>修改样品资料</h3>
          <button class="modify-close" @click="modifyOpen = false">✕</button>
        </div>
        <div class="modify-body">
          <div class="modify-layout">
            <!-- 图片区 -->
            <div
              class="modify-image-section"
              @dragover.prevent="modifyDragOver = true"
              @dragleave="modifyDragOver = false"
              @drop.prevent="onModifyDrop"
            >
              <template v-if="modifyImages.length > 0 || modifyDroppedPreview">
                <div class="modify-image-main" :class="{ 'modify-drag-over': modifyDragOver }">
                  <img :src="modifyDroppedPreview || modifyImages[modifyImageIndex]?.url" />
                  <button v-if="modifyImages.length > 1" class="modify-img-nav modify-img-prev" @click="modifyImageIndex = modifyImageIndex > 0 ? modifyImageIndex - 1 : modifyImages.length - 1"><ChevronLeft :size="22" /></button>
                  <button v-if="modifyImages.length > 1" class="modify-img-nav modify-img-next" @click="modifyImageIndex = modifyImageIndex < modifyImages.length - 1 ? modifyImageIndex + 1 : 0"><ChevronRight :size="22" /></button>
                  <span class="modify-img-counter" v-if="modifyImages.length > 1">{{ modifyImageIndex + 1 }} / {{ modifyImages.length }}</span>
                  <span v-if="modifyDroppedPreview" class="modify-drop-badge">拖入</span>
                </div>
                <div class="modify-thumb-strip" v-if="modifyImages.length > 1">
                  <div
                    v-for="(img, idx) in modifyImages"
                    :key="img.hash || idx"
                    class="modify-thumb-item"
                    :class="{ active: idx === modifyImageIndex }"
                    @click="modifyImageIndex = idx"
                  >
                    <img :src="img.url" />
                  </div>
                </div>
              </template>
              <div v-else class="modify-image-empty" :class="{ 'modify-drag-over': modifyDragOver }">
                暂无图片 / 拖入图片
              </div>
            </div>
            <!-- 表单区 -->
            <div class="modify-grid">
              <div class="modify-field"><label>公司编号</label><input v-model="modifyForm.sampleCode" readonly class="modify-readonly" /></div>
              <div class="modify-field"><label>出厂货号</label><input v-model="modifyForm.factoryCode" /></div>
              <div class="modify-field"><label>样品名称</label><input v-model="modifyForm.sampleName" /></div>
              <div class="modify-field"><label>英文名称</label><input v-model="modifyForm.englishName" /></div>
              <div class="modify-field"><label class="modify-label-red">出厂价</label><input v-model="modifyForm.factoryPrice" class="modify-input-red" /></div>
              <div class="modify-field"><label class="modify-label-red">报出价1</label><input v-model="modifyForm.calculatedPrice" class="modify-input-red" /></div>
              <div class="modify-field"><label class="modify-label-red">报出价2</label><input v-model="modifyForm.taxPrice2" class="modify-input-red" /></div>
              <div class="modify-field"><label>种类编号</label><input v-model="modifyForm.categoryCode" /></div>
              <div class="modify-field"><label>种类名称</label><input v-model="modifyForm.category" /></div>
              <div class="modify-field"><label>摊位号</label><input v-model="modifyForm.boothNo" readonly class="modify-readonly" /></div>
              <div class="modify-field"><label>厂商名称</label><input v-model="modifyForm.name" /></div>
              <div class="modify-field"><label>厂商编号</label><input v-model="modifyForm.manufacturerCode" readonly class="modify-readonly" /></div>
              <div class="modify-field"><label>颜色</label><input v-model="modifyForm.color" /></div>
              <div class="modify-field"><label>英文颜色</label><input v-model="modifyForm.colorEn" /></div>
              <div class="modify-field"><label>产地</label><input v-model="modifyForm.origin" /></div>
              <div class="modify-field"><label>中文包装</label><input v-model="modifyForm.packagingCn" /></div>
              <div class="modify-field"><label>英文包装</label><input v-model="modifyForm.packagingEn" /></div>
              <div class="modify-field"><label>样品单位</label><input v-model="modifyForm.sampleUnit" /></div>
              <div class="modify-field"><label>内盒个数</label><input v-model="modifyForm.innerBoxCount" /></div>
              <div class="modify-field"><label>外箱装量</label><input v-model="modifyForm.cartonCapacity" /></div>
              <div class="modify-field"><label>装箱单位</label><input v-model="modifyForm.packingUnit" /></div>
              <div class="modify-field"><label>外箱长度</label><input v-model="modifyForm.cartonLength" /></div>
              <div class="modify-field"><label>外箱宽度</label><input v-model="modifyForm.cartonWidth" /></div>
              <div class="modify-field"><label>外箱高度</label><input v-model="modifyForm.cartonHeight" /></div>
              <div class="modify-field"><label>外箱材积</label><input v-model="modifyForm.cartonMaterialVolume" /></div>
              <div class="modify-field"><label>外箱体积</label><input v-model="modifyForm.cartonVolume" /></div>
              <div class="modify-field"><label>外箱毛重</label><input v-model="modifyForm.cartonGrossWeight" /></div>
              <div class="modify-field"><label>外箱净重</label><input v-model="modifyForm.cartonNetWeight" /></div>
              <div class="modify-field"><label>样品长度</label><input v-model="modifyForm.sampleLength" /></div>
              <div class="modify-field"><label>样品宽度</label><input v-model="modifyForm.sampleWidth" /></div>
              <div class="modify-field"><label>样品高度</label><input v-model="modifyForm.sampleHeight" /></div>
              <div class="modify-field"><label>产品毛重</label><input v-model="modifyForm.sampleGrossWeight" /></div>
              <div class="modify-field"><label>产品净重</label><input v-model="modifyForm.sampleNetWeight" /></div>
              <div class="modify-field"><label>包装长度</label><input v-model="modifyForm.packageLength" /></div>
              <div class="modify-field"><label>包装宽度</label><input v-model="modifyForm.packageWidth" /></div>
              <div class="modify-field"><label>包装高度</label><input v-model="modifyForm.packageHeight" /></div>
              <div class="modify-field"><label>产品认证</label><input v-model="modifyForm.certification" /></div>
              <div class="modify-field"><label>电池信息</label><input v-model="modifyForm.batteryInfo" /></div>
              <div class="modify-field"><label>联系人</label><input v-model="modifyForm.contact1" /></div>
              <div class="modify-field"><label>联系电话</label><input v-model="modifyForm.phone1" /></div>
              <div class="modify-field"><label>手机</label><input v-model="modifyForm.mobile1" /></div>
              <div class="modify-field"><label>传真</label><input v-model="modifyForm.fax" /></div>
              <div class="modify-field"><label>QQ</label><input v-model="modifyForm.qq" /></div>
              <div class="modify-field"><label>中文备注</label><input v-model="modifyForm.remark" /></div>
              <div class="modify-field"><label>英文备注</label><input v-model="modifyForm.remarkEn" /></div>
              <div class="modify-field"><label>其他备注</label><input v-model="modifyForm.otherRemark" /></div>
              <div class="modify-field"><label>箱数</label><input v-model="modifyForm.boxCount" /></div>
              <div class="modify-field"><label>登记人</label><input v-model="modifyForm.registrant" /></div>
              <div class="modify-field"><label>修改人</label><input v-model="modifyForm.modifier" /></div>
            </div>
          </div>
        </div>
        <div class="modify-footer">
          <label class="modify-sync" title="勾选后同步修改到原始样品资料，否则仅影响当前代号">
            <input type="checkbox" v-model="modifySync" />
            同步到样品资料
          </label>
          <div class="modify-actions">
            <button class="csd-btn csd-btn-ghost" @click="modifyOpen = false">取消</button>
            <button class="csd-btn csd-btn-primary" :disabled="modifySaving" @click="onModifySave">保存</button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- 蓝牙打印模版设置模态框 -->
  <Teleport to="body">
    <Transition name="modify-fade">
    <div v-if="labelTemplateOpen" class="modify-overlay" @click.self="labelTemplateOpen = false">
      <div class="modify-dialog" style="width:98vw;max-width:2000px;max-height:96vh;">
        <div class="modify-header">
          <h3 style="font-size:28px;">蓝牙打印模版设置</h3>
          <button class="modify-close" style="font-size:30px;" @click="labelTemplateOpen = false">✕</button>
        </div>
        <div class="modify-body" style="padding:28px 32px;overflow:auto;">
          <div v-if="btLoading" style="text-align:center;padding:60px;font-size:22px;color:#94a3b8;">加载中...</div>
          <template v-else>
            <!-- 模板选择行 -->
            <div style="display:flex;gap:14px;align-items:center;margin-bottom:18px;flex-wrap:wrap;">
              <label style="font-weight:700;font-size:24px;white-space:nowrap;">选择模板</label>
              <select v-model="btSelectedId" class="ps-input" style="width:280px;height:56px;font-size:22px;flex:none;" @change="onBtSelectTemplate">
                <option :value="null">-- 新建模板 --</option>
                <option v-for="t in btTemplateList" :key="t.id" :value="t.id">{{ t.name }} ({{ t.width }}mm)</option>
              </select>
              <template v-if="btSelectedId && btCurrentTemplate && btEditing">
                <input v-model="btCurrentTemplate.name" style="width:200px;height:56px;border:1px solid #d1d5db;border-radius:8px;padding:0 16px;font-size:22px;color:#000;font-weight:700;" placeholder="模板名称" />
                <button class="csd-btn csd-btn-primary" style="font-size:22px;padding:10px 20px;" @click="renameBtTemplate">改名</button>
              </template>
              <button class="csd-btn csd-btn-ghost" style="font-size:22px;padding:10px 22px;" @click="btEditing ? (btEditing = false) : onBtEditTemplate()">{{ btEditing ? '取消修改' : '打开修改' }}</button>
              <button class="csd-btn csd-btn-primary" style="font-size:22px;padding:10px 22px;" @click="onBtNewTemplate">新建模板</button>
              <button class="csd-btn csd-btn-danger" style="font-size:22px;padding:10px 22px;" @click="onBtDeleteTemplate" :disabled="!btSelectedId||btEditing">删除当前模板</button>
              <label v-if="btEditing" style="display:inline-flex;align-items:center;gap:8px;font-size:22px;color:#000;font-weight:700;cursor:pointer;padding:10px 22px;border:1px solid rgba(0,122,255,0.10);border-radius:0.65em;background:rgba(255,255,255,0.72);">
                <input type="checkbox" :checked="btAllChecked" @change="e => btCheckAll(e.target.checked)" style="width:28px;height:28px;cursor:pointer;" />
                全部打印
              </label>
              <label v-if="btEditing" style="display:inline-flex;align-items:center;gap:10px;font-size:24px;color:#000;font-weight:700;cursor:pointer;padding:14px 28px;border:1px solid rgba(0,122,255,0.10);border-radius:0.65em;background:rgba(255,255,255,0.72);">
                <input type="checkbox" :checked="btAllShowTitle" @change="e => btCheckAllShowTitle(e.target.checked)" style="width:28px;height:28px;cursor:pointer;" />
                打印标题
              </label>
              <button v-if="btEditing" class="csd-btn csd-btn-ghost" style="font-size:22px;padding:10px 22px;color:#000;font-weight:700;" @click="btShowPrintedOnly = !btShowPrintedOnly">{{ btShowPrintedOnly ? '显示全部' : '只看打印' }}</button>
            </div>
            <!-- 字段配置表格 -->
            <vxe-grid
              ref="btGridRef"
              :id="btGridId"
              :columns="btColumns"
              :data="btDisplayList"
              :row-config="btRowConfig"
              :custom-config="{ storage: true }"
              :column-config="{ useKey: true, resizable: true }"
              :cell-config="{ height: 80 }"
              :cell-style="{ fontSize: '28px' }"
              @row-dragend="onBtDragEnd"
              :border="true"
              height="880"
              :header-cell-style="{ fontSize: '28px', fontWeight: 700 }"
            >
              <template #printTitle_default="{ row }">
                <input
                  v-model="row.printTitle"
                  :disabled="!btEditing"
                  style="width:100%;height:60px;border:1px solid #d1d5db;border-radius:6px;padding:0 14px;font-size:28px;text-align:center;"
                  :style="{ background: btEditing ? '#fff' : '#f3f4f6', color: btEditing ? '#1d1d1f' : '#6b7280' }"
                />
              </template>
              <template #showTitle_default="{ row }">
                <input type="checkbox" v-model="row.showTitle" :disabled="!btEditing" style="width:26px;height:26px;cursor:pointer;" />
              </template>
              <template #fontSizeLarge_default="{ row }">
                <select v-model.number="row.fontSize" :disabled="!btEditing" style="width:90px;height:50px;font-size:22px;text-align:center;">
                  <option v-for="s in [8,10,12,14,16,18,20,24,28,32,36,40,48,56,64]" :key="s" :value="s">{{ s }}</option>
                </select>
              </template>
              <template #fontWeightBold_default="{ row }">
                <input type="checkbox" v-model="row.fontWeightBold" :disabled="!btEditing" style="width:26px;height:26px;cursor:pointer;" />
              </template>
              <template #hideValue_default="{ row }">
                <input type="checkbox" v-model="row.hideValue" :disabled="!btEditing || row.field.startsWith('custom')" style="width:26px;height:26px;cursor:pointer;" />
              </template>
              <template #lineSpacing_default="{ row }">
                <input type="number" v-model.number="row.lineSpacing" :disabled="!btEditing" min="0.5" max="10" step="0.1" style="width:85px;height:50px;font-size:22px;text-align:center;" />
              </template>
              <template #sortOrder_default="{ row }">
                <input
                  v-model.number="row.sortOrder"
                  :disabled="!btEditing"
                  type="number"
                  min="0"
                  style="width:100px;height:50px;border:1px solid #d1d5db;border-radius:6px;padding:0;font-size:22px;text-align:center;"
                  :style="{ background: btEditing ? '#fff' : '#f3f4f6', color: btEditing ? '#1d1d1f' : '#6b7280' }"
                />
              </template>
              <template #isPrinted_default="{ row }">
                <input type="checkbox" v-model="row.isPrinted" :disabled="!btEditing" style="width:26px;height:26px;cursor:pointer;" />
              </template>
            </vxe-grid>
          </template>
        </div>
        <div class="modify-footer">
          <label class="modify-sync" style="display:flex;align-items:center;gap:24px;font-size:22px;font-weight:600;">
            <label style="display:flex;align-items:center;gap:10px;cursor:pointer;">
              <input type="radio" v-model="btWidth" value="58" :disabled="!btEditing" style="width:28px;height:28px;" @change="onBtWidthChanged(true)" /> 小标签(58mm)
            </label>
            <label style="display:flex;align-items:center;gap:10px;cursor:pointer;">
              <input type="radio" v-model="btWidth" value="80" :disabled="!btEditing" style="width:28px;height:28px;" @change="onBtWidthChanged(true)" /> 大标签(80mm)
            </label>
            <span style="margin-left:12px;color:#666;font-size:22px;">批量字号</span>
            <input type="number" v-model.number="btBatchFontSize" :disabled="!btEditing" min="8" max="64" style="width:80px;height:46px;font-size:22px;text-align:center;" />
            <button v-if="btEditing" class="csd-btn csd-btn-ghost" style="font-size:20px;padding:8px 18px;" @click="onBtBatchFontSize">应用</button>
            <span style="margin-left:12px;color:#666;font-size:22px;">批量行距</span>
            <input type="number" v-model.number="btBatchLineSpacing" :disabled="!btEditing" min="0.5" max="10" step="0.1" style="width:80px;height:46px;font-size:22px;text-align:center;" />
            <button v-if="btEditing" class="csd-btn csd-btn-ghost" style="font-size:20px;padding:8px 18px;" @click="onBtBatchLineSpacing">应用</button>
          </label>
          <div class="modify-actions">
            <button class="csd-btn csd-btn-ghost" style="font-size:22px;padding:10px 28px;" @click="labelTemplateOpen = false">关闭</button>
            <button v-if="btEditing" class="csd-btn csd-btn-primary" style="font-size:22px;padding:10px 28px;background:#10b981;" @click="saveBtTemplate">保存模板</button>
          </div>
        </div>
      </div>
    </div>
    </Transition>
  </Teleport>

  <!-- 恢复误添加库存数据模态框 -->
  <Teleport to="body">
    <Transition name="modify-fade">
    <div v-if="revertModalOpen" class="modify-overlay" style="background:transparent;backdrop-filter:none" @click.self="revertModalOpen = false">
      <div class="modify-dialog" style="width:85vw;height:85vh;display:flex;flex-direction:column;">
        <div class="modify-header">
          <h3 style="font-size:24px;">恢复误添加库存数据</h3>
          <div style="display:flex;align-items:center;gap:12px;">
            <button class="csd-btn csd-btn-ghost" style="font-size:16px;padding:6px 16px;" @click="onRevertSelectAll">全选</button>
            <button class="csd-btn csd-btn-ghost" style="font-size:16px;padding:6px 16px;" @click="onRevertClearAll">清除</button>
            <button
              class="csd-btn csd-btn-ghost"
              style="font-size:16px;padding:6px 16px;"
              :style="revertTypeFilter === '入库' ? { background:'#16a34a', color:'#fff', borderColor:'#16a34a' } : {}"
              @click="revertTypeFilter = revertTypeFilter === '入库' ? null : '入库'"
            >入库</button>
            <button
              class="csd-btn csd-btn-ghost"
              style="font-size:16px;padding:6px 16px;"
              :style="revertTypeFilter === '出库' ? { background:'#dc2626', color:'#fff', borderColor:'#dc2626' } : {}"
              @click="revertTypeFilter = revertTypeFilter === '出库' ? null : '出库'"
            >出库</button>
            <span style="font-size:16px;color:#666;margin-left:12px;">共 <strong>{{ filteredRevertList.length }}</strong> 条</span>
            <button class="csd-btn csd-btn-ghost" style="font-size:18px;padding:8px 22px;" @click="revertModalOpen = false">关闭</button>
          </div>
        </div>
        <div class="modify-body" style="padding:16px 24px;flex:1;overflow:hidden;display:flex;flex-direction:column;">
          <div v-if="revertLoading" style="text-align:center;padding:80px;font-size:24px;color:#94a3b8;">加载中...</div>
          <div v-else-if="revertList.length === 0" style="text-align:center;padding:80px;font-size:24px;color:#94a3b8;">
            当前代号尚无入库/出库记录
          </div>
          <vxe-grid
            v-else
            ref="revertGridRef"
            id="revert_stock_grid"
            :columns="revertColumns"
            :data="filteredRevertList"
            height="100%"
            :checkbox-config="{ highlight: true }"
            :row-config="{ isHover: true, height: 48, keyField: '_key' }"
            :column-config="{ resizable: true, drag: true }"
            :header-cell-style="{ background: '#f8fafd', borderColor: '#e0e4ea', fontSize: '16px', fontWeight: 600, textAlign: 'center' }"
            :cell-style="{ fontSize: '15px', textAlign: 'center' }"
            :border="true"
            :sort-config="{ trigger: 'header', remote: false }"
            :custom-config="{ storage: true }"
            :toolbar-config="{ custom: true }"
            @checkbox-change="onRevertCheckboxChange"
            @checkbox-all="onRevertCheckboxChange"
          >
            <template #_type="{ row }">
              <span :style="{ color: row._type === '入库' ? '#16a34a' : '#dc2626' }">{{ row._type }}</span>
            </template>
          </vxe-grid>
        </div>
        <div class="modify-footer">
          <div class="modify-actions" style="display:flex;align-items:center;justify-content:space-between;width:100%;">
            <div style="display:flex;align-items:center;gap:16px;font-size:18px;color:#666;">
              已选 <strong>{{ revertCheckedCount }}</strong> / 共 <strong>{{ filteredRevertList.length }}</strong> 条
            </div>
            <button class="csd-btn csd-btn-danger" style="font-size:18px;padding:8px 28px;" :disabled="revertCheckedCount === 0 || revertDeleting" @click="onRevertConfirm">
              {{ revertDeleting ? '撤销中...' : `确认撤销 (${revertCheckedCount})` }}
            </button>
          </div>
        </div>
      </div>
    </div>
    </Transition>
  </Teleport>

  <!-- 查看删除记录模态框 -->
  <Teleport to="body">
    <Transition name="modify-fade">
    <div v-if="deletedRecordsModalOpen" class="modify-overlay" style="background:transparent;backdrop-filter:none" @click.self="deletedRecordsModalOpen = false">
      <div class="modify-dialog" style="width:85vw;height:85vh;display:flex;flex-direction:column;">
        <div class="modify-header">
          <h3 style="font-size:24px;">删除记录</h3>
          <div style="display:flex;align-items:center;gap:16px;">
            <button class="csd-btn csd-btn-primary" style="font-size:22px;padding:10px 26px;" :disabled="deletedCheckedRows.length === 0" @click="restoreDeletedItems">恢复删除</button>
            <button class="csd-btn csd-btn-ghost" style="font-size:18px;padding:8px 22px;" @click="deletedRecordsModalOpen = false">关闭</button>
          </div>
        </div>
        <div class="modify-body" style="padding:16px 24px;flex:1;overflow:hidden;display:flex;flex-direction:column;">
          <vxe-grid
            id="deleted_records_grid"
            :columns="deletedLogColumns"
            :data="deletedPagedData"
            height="100%"
            :border="true"
            :toolbar-config="{ custom: true }"
            :custom-config="{ storage: true }"
            :column-config="{ resizable: true, drag: true }"
            :sort-config="{ trigger: 'header', remote: false }"
            :checkbox-config="{ highlight: true, checkField: '_checked' }"
            :row-config="{ isHover: true, keyField: 'itemId' }"
            :header-row-config="{ height: 120 }"
            :cell-config="{ height: 80 }"
            :header-cell-style="{ background: '#f8fafd', borderColor: '#e0e4ea', fontSize: '16px', fontWeight: 600, textAlign: 'center' }"
            :cell-style="{ fontSize: '15px', textAlign: 'center' }"
            @checkbox-change="onDeletedCheckChange"
            @checkbox-all="onDeletedCheckAll"
          >
            <template #empty>
              <span style="font-size:24px;color:#94a3b8;">暂无删除记录</span>
            </template>
          </vxe-grid>
        </div>
        <div class="modify-footer">
          <div class="modify-actions" style="display:flex;align-items:center;justify-content:space-between;width:100%;">
            <div style="display:flex;align-items:center;gap:16px;font-size:18px;color:#666;">
              已选 <strong>{{ deletedCheckedRows.length }}</strong> / 共 <strong>{{ deletedItemLog.length }}</strong> 条
            </div>
            <div style="display:flex;align-items:center;gap:12px;">
              <span style="font-size:18px;">每页</span>
              <select v-model.number="deletedPageSize" style="height:38px;border:1px solid #d1d5db;border-radius:6px;padding:0 8px;font-size:18px;">
                <option :value="20">20</option>
                <option :value="50">50</option>
                <option :value="100">100</option>
                <option :value="200">200</option>
              </select>
              <span style="font-size:18px;">条</span>
              <button class="csd-btn csd-btn-ghost" :disabled="deletedCurrentPage <= 1" @click="deletedCurrentPage = 1" style="font-size:16px;">
                <ChevronsLeft :size="18" />
              </button>
              <button class="csd-btn csd-btn-ghost" :disabled="deletedCurrentPage <= 1" @click="deletedCurrentPage--" style="font-size:16px;">
                <ChevronLeft :size="18" />
              </button>
              <span style="font-size:18px;">{{ deletedCurrentPage }} / {{ deletedTotalPages }}</span>
              <button class="csd-btn csd-btn-ghost" :disabled="deletedCurrentPage >= deletedTotalPages" @click="deletedCurrentPage++" style="font-size:16px;">
                <ChevronRight :size="18" />
              </button>
              <button class="csd-btn csd-btn-ghost" :disabled="deletedCurrentPage >= deletedTotalPages" @click="deletedCurrentPage = deletedTotalPages" style="font-size:16px;">
                <ChevronsRight :size="18" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    </Transition>
  </Teleport>

  <!-- 按编号批量查询模态框 -->
  <Teleport to="body">
    <Transition name="modify-fade">
    <div v-if="codeSearchModalOpen" class="modify-overlay" style="background:transparent;backdrop-filter:none;" @click.self="codeSearchModalOpen = false">
      <div class="modify-dialog"
        :style="{width:codeSearchW+'px',height:codeSearchH+'px',top:codeSearchPos.y+'px',left:codeSearchPos.x+'px',position:'fixed',display:'flex',flexDirection:'column'}"
      >
        <div class="modify-header" @mousedown="startCodeSearchDrag">
          <h3 style="font-size:26px;">按编号批量查询</h3>
          <button class="csd-btn csd-btn-ghost" style="font-size:20px;padding:10px 24px;" @click="codeSearchModalOpen = false">关闭</button>
        </div>
        <div class="modify-body" style="padding:24px 28px;flex:1;overflow:hidden;display:flex;flex-direction:column;">
          <!-- 模式切换 tabs -->
          <div style="display:flex;gap:0;margin-bottom:16px;border-bottom:2px solid #e5e7eb;">
            <button
              :style="{padding:'10px 28px',fontSize:'22px',fontWeight:'600',border:'none',background:'transparent',cursor:'pointer',borderBottom: codeInputMode === 'code' ? '3px solid #007aff' : '3px solid transparent',color: codeInputMode === 'code' ? '#007aff' : '#666',transition:'all .2s'}"
              @click="codeInputMode = 'code'"
            >按公司编号</button>
            <button
              :style="{padding:'10px 28px',fontSize:'22px',fontWeight:'600',border:'none',background:'transparent',cursor:'pointer',borderBottom: codeInputMode === 'factoryCode' ? '3px solid #007aff' : '3px solid transparent',color: codeInputMode === 'factoryCode' ? '#007aff' : '#666',transition:'all .2s'}"
              @click="codeInputMode = 'factoryCode'"
            >按出厂货号</button>
          </div>
          <div style="display:flex;gap:24px;flex:1;">
            <!-- 左：输入区 -->
            <div style="flex:1;display:flex;flex-direction:column;">
              <label style="font-size:22px;font-weight:600;margin-bottom:10px;white-space:nowrap;">{{ codeInputMode === 'code' ? '输入公司编号' : '输入出厂货号' }}</label>
              <textarea
                v-model="codeSearchInputText"
                :placeholder="codeInputMode === 'code' ? '每行一个公司编号，支持粘贴多行\n例如：\nYX18188104\nYX18188105' : '每行一个出厂货号，支持粘贴多行\n例如：\nAB12345\nAB12346'"
                style="flex:1;border:1px solid #d1d5db;border-radius:8px;padding:20px;font-size:22px;line-height:1.8;resize:none;outline:none;box-sizing:border-box;"
              ></textarea>
            </div>
            <!-- 右：未查到的编号 -->
            <div style="flex:1;display:flex;flex-direction:column;">
              <div style="font-size:22px;font-weight:600;color:#888;margin-bottom:10px;">
                未查到的{{ codeInputMode === 'code' ? '公司编号' : '货号' }}
              </div>
              <div style="flex:1;border:1px solid #e5e7eb;border-radius:8px;padding:14px;overflow-y:auto;background:#fafafa;font-size:22px;line-height:2.2;">
                <div v-if="!codeSearchDone" style="color:#aaa;text-align:center;padding-top:30px;">
                  搜索后将在此显示<br/>未查到的编号
                </div>
                <div v-else-if="notFoundCodes.length === 0" style="color:#22c55e;text-align:center;padding-top:30px;font-weight:600;">
                  全部查到
                </div>
                <div v-for="c in notFoundCodes" :key="c" style="color:#ef4444;">{{ c }}</div>
              </div>
            </div>
          </div>
          <div style="display:flex;align-items:center;justify-content:space-between;margin-top:16px;">
            <label style="display:flex;align-items:center;gap:10px;font-size:22px;cursor:pointer;">
              <input type="checkbox" v-model="codeSearchKeepInput" style="accent-color:#007aff;width:24px;height:24px;" />
              保留输入数据
            </label>
            <div v-if="codeSearchDone" style="font-size:20px;color:#666;">
              找到 <strong>{{ codeSearchResult }}</strong> 条匹配记录
            </div>
          </div>
        </div>
        <div class="modify-footer">
          <div class="modify-actions" style="width:100%;display:flex;justify-content:flex-end;gap:12px;">
            <button class="csd-btn csd-btn-ghost" style="font-size:22px;padding:12px 36px;" @click="clearCodeSearch">清除筛选</button>
            <button class="csd-btn csd-btn-primary" style="font-size:22px;padding:12px 36px;" @click="doCodeSearch">查询</button>
          </div>
        </div>
      </div>
    </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, onActivated, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Search, X, Eye, EyeOff,
  ChevronsUp, ChevronsDown, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  RotateCw, Plus, Trash2, Printer, Send, Settings, Tag, FilePlus, Pencil, Crosshair, MoreHorizontal, Filter, LayoutGrid,
  Minimize2, Maximize2, PackageOpen, Package, ImageDown, Check, Copy, Image as ImageIcon, Video
} from 'lucide-vue-next'
import ExcelJS from 'exceljs'
import '@/styles/client-sample-detail.css'
import '@/styles/sample.css'
import { api, authHeader } from '@/api/index'
import { appAlert, appConfirm } from '@/utils/dialog'
import { useModalDrag } from '@/composables/useModalDrag'
import { useAuth } from '@/stores/auth'
import { useGridPrefSync } from '@/composables/useGridPrefSync'
import { VxeUI } from 'vxe-table'
import { VxePager } from 'vxe-pc-ui'

const route = useRoute()
const router = useRouter()
const auth = useAuth()
const codeName = computed(() => route.params.codeName || '')

// 切换代号时清空删除记录日志
watch(codeName, () => {
  deletedItemLog.value = []
})

// ========== 表单区 ==========
const cardExpanded = ref(false)
const formVisible = ref(true)
const largeMode = ref(false)
const thumbTooltip = reactive({ show: false, src: '', fallback: '', x: 0, y: 0 })
let thumbTooltipTimer = null

const showThumbTooltip = (e, row) => {
  if (!row.thumbnail && !row.firstImageHash) return
  clearTimeout(thumbTooltipTimer)
  const thumbSrc = '/thumbnails/' + row.thumbnail
  const src = row.firstImageHash ? '/images/view/hash/' + row.firstImageHash : thumbSrc
  thumbTooltipTimer = setTimeout(() => {
    const gap = 12
    const previewSize = 620
    const rect = e.target.getBoundingClientRect()
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
    thumbTooltip.src = src
    thumbTooltip.fallback = thumbSrc
    thumbTooltip.x = left
    thumbTooltip.y = top
    thumbTooltip.show = true
  }, 300)
}

const hideThumbTooltip = () => {
  clearTimeout(thumbTooltipTimer)
  thumbTooltip.show = false
}

// 自定义拖拽：允许自由移动，仅限制不超出屏幕顶部和左侧
const dragModal = (e) => {
  const startX = e.clientX - photoModalPos.x
  const startY = e.clientY - photoModalPos.y
  const onMove = (ev) => {
    photoModalPos.x = Math.max(0, Math.min(ev.clientX - startX, window.innerWidth - photoModalW.value))
    photoModalPos.y = Math.max(0, ev.clientY - startY)  // 只限顶部，不限底部
  }
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

const record = ref(null)
const sample = ref(null)

// ========== 照片预览模态框 ==========
const showPhotoModal = ref(false)
const photoModalSample = ref(null)
const photoModalImages = ref([])
const photoModalIndex = ref(0)
const hideFactoryPrice = ref(false)
const hideTaxPrice = ref(false)
const hideFactoryInfo = ref(false)
const { photoModalPos, photoModalW, photoModalH, photoModalInit } = useModalDrag()

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

const onModalImgError = (e) => {
  e.target.style.display = 'none'
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

const closePhotoModal = () => {
  showPhotoModal.value = false
}

const photoModalPrev = () => {
  if (photoModalIndex.value > 0) photoModalIndex.value--
}

const photoModalNext = () => {
  if (photoModalIndex.value < photoModalImages.value.length - 1) photoModalIndex.value++
}

// ========== 全屏大图预览 ==========
const showImagePreview = ref(false)
const imagePreviewList = ref([])
const imagePreviewIndex = ref(0)
const previewFromModal = ref(false)
// ── 大图预览缩放 ──
const ipZoom = ref(1)
const ipPanX = ref(0)
const ipPanY = ref(0)
const ipDragging = ref(false)
const ipDragStart = ref({ x: 0, y: 0, px: 0, py: 0 })

const currentPreviewSrc = computed(() => {
  const img = imagePreviewList.value[imagePreviewIndex.value]
  if (!img) return ''
  if (img.hash) return '/images/view/hash/' + img.hash
  return '/thumbnails/' + img.thumbnailPath
})

const openFullPreview = () => {
  if (photoModalImages.value.length === 0) return
  imagePreviewList.value = photoModalImages.value
  imagePreviewIndex.value = photoModalIndex.value
  previewFromModal.value = true
  showPhotoModal.value = false
  showImagePreview.value = true
}

const closeImagePreview = () => {
  showImagePreview.value = false
  ipZoom.value = 1
  ipPanX.value = 0
  ipPanY.value = 0
  if (previewFromModal.value) {
    showPhotoModal.value = true
    previewFromModal.value = false
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

// ========== 图片区 ==========
const images = ref([])
const stripIndex = ref(0)

const loadCardImages = async (sampleId) => {
  if (!sampleId) { images.value = []; return }
  try {
    const res = await api(`/images/sample/${sampleId}`)
    const raw = (res && (res.data || res)) || []
    const arr = Array.isArray(raw) ? raw : []
    images.value = arr.map(img => ({
      hash: img.hash || '',
      thumbnailPath: img.thumbnailPath || '',
      url: img.hash ? '/images/view/hash/' + img.hash : '/thumbnails/' + img.thumbnailPath
    }))
    stripIndex.value = 0
  } catch {
    images.value = []
  }
}

// 选中样品后自动加载图片
watch(sample, (newVal) => {
  if (newVal?.id) loadCardImages(newVal.id)
  else images.value = []
})

const loadModifyImages = async (sampleId) => {
  modifyImages.value = []
  modifyImageIndex.value = 0
  if (!sampleId) return
  try {
    const res = await api(`/images/sample/${sampleId}`)
    const raw = (res && (res.data || res)) || []
    const arr = Array.isArray(raw) ? raw : []
    modifyImages.value = arr.map(img => ({
      hash: img.hash || '',
      thumbnailPath: img.thumbnailPath || '',
      url: img.hash ? '/images/view/hash/' + img.hash : '/thumbnails/' + img.thumbnailPath
    }))
  } catch {
    modifyImages.value = []
  }
}

const stripPrev = () => {
  if (images.value.length > 1) {
    stripIndex.value = (stripIndex.value - 1 + images.value.length) % images.value.length
  }
}

const stripNext = () => {
  if (images.value.length > 1) {
    stripIndex.value = (stripIndex.value + 1) % images.value.length
  }
}

const viewImage = () => {
  if (images.value.length === 0) return
  imagePreviewList.value = images.value.map(img => ({
    hash: img.hash || '',
    thumbnailPath: img.thumbnailPath || ''
  }))
  imagePreviewIndex.value = stripIndex.value
  showImagePreview.value = true
}

// ========== 表格区 ==========
const gridRef = ref(null)
const tableWrapRef = ref(null)
const tableWrapHeight = ref(600)
let resizeObserver = null
let resizeRafId = null
let lastObservedHeight = 0

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
const isColumnDragging = ref(false)
const extDragging = ref(false)
let areaHandleEl = null
let _areaRaf = null
const areaSelectedCount = computed(() => {
  if (!areaSelectedColumn.value) return 0
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.itemId) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.itemId) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return 0
  return Math.abs(eIdx - sIdx) + 1
})
const areaDragRowIdSet = computed(() => {
  if (!areaDragging.value || !areaDragField.value) return null
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.itemId) === String(areaDragStartRowId.value))
  const eIdx = data.findIndex(r => String(r.itemId) === String(areaDragEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return null
  const min = Math.min(sIdx, eIdx); const max = Math.max(sIdx, eIdx)
  const set = new Set()
  for (let i = min; i <= max; i++) set.add(data[i].itemId)
  return set
})
const areaSelectedRowIdSet = computed(() => {
  if (!areaSelectedColumn.value) return null
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.itemId) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.itemId) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return null
  const min = Math.min(sIdx, eIdx); const max = Math.max(sIdx, eIdx)
  const set = new Set()
  for (let i = min; i <= max; i++) set.add(data[i].itemId)
  return set
})

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(2000)
const pageSizeOptions = [100, 200, 500, 1000, 2000, 4000, 5000]
const totalRecords = ref(0)
const tableLoading = ref(false)
const sortField = ref('')
const sortOrder = ref('')
// 复选框列排序: 0=无, 1=升序(未勾选在前), 2=降序(已勾选在前)
const checkboxSortOrder = ref(0)
const onCheckboxHeaderSort = (dir) => {
  if (dir === 'asc') {
    checkboxSortOrder.value = 1
    sortField.value = '_checked'
    sortOrder.value = 'asc'
  } else if (dir === 'desc') {
    checkboxSortOrder.value = 2
    sortField.value = '_checked'
    sortOrder.value = 'desc'
  } else {
    checkboxSortOrder.value = 0
    sortField.value = ''
    sortOrder.value = ''
  }
  currentPage.value = 1
}
// 表头全选复选框状态
const allHeaderChecked = computed(() => {
  const data = tableData.value
  if (!data.length) return false
  const checkedCount = data.filter(r => r.checked).length
  if (checkedCount === 0) return false
  if (checkedCount === data.length) return true
  return 'indeterminate'
})
const checkedRows = ref([])
const lastCheckboxIndex = ref(-1)
const list = ref([])

// checkbox 选中状态同步到服务端（批量接口，避免并发过多导致 ERR_INSUFFICIENT_RESOURCES）
const syncCheckedToServer = (rows, checked) => {
  if (rows.length === 0) return
  const value = checked ? 1 : 0
  const flags = rows.map(row => {
    row.checked = value
    return { itemId: row.itemId, field: 'checked', value }
  })
  api(`/client-samples/${codeName.value}/items/flags`, {
    method: 'PUT',
    body: JSON.stringify(flags)
  }).catch(() => {
    rows.forEach(row => { row.checked = checked ? 0 : 1 })
  })
}
const restoreCheckedRows = () => {
  const rows = list.value.filter(r => r.checked)
  checkedRows.value = rows
  if (rows.length > 0) {
    nextTick(() => gridRef.value?.setCheckboxRow(rows, true))
  }
}

// 从 list 中恢复 checkedRows（应对 vxe-table 虚拟滚动事件覆盖）
const syncCheckedRowsFromList = () => {
  const rows = list.value.filter(r => r.checked)
  checkedRows.value = rows
  if (rows.length > 0) {
    nextTick(() => gridRef.value?.setCheckboxRow(rows, true))
  }
}

// ========== 工具栏 ==========
const addInput = ref('')
const addLoading = ref(false)

const onAdd = async () => {
  const code = addInput.value.trim()
  if (!code) return
  addLoading.value = true
  try {
    // 1. 查询样品资料（按公司编号精确匹配）
    const res = await api(`/samples?sampleCode=${encodeURIComponent(code)}&size=10`)
    if (res.code !== 200 || !res.data?.records?.length) {
      appAlert(`未找到公司编号为 "${code}" 的样品资料`, '提示', 'warning')
      return
    }
    const samples = res.data.records
    const sampleIds = samples.map(s => s.id)

    // 2. 检查是否已存在重复
    const existingCodes = new Set(tableData.value.map(item => item.sampleCode).filter(Boolean))
    const duplicates = samples.filter(s => existingCodes.has(s.sampleCode))
    let force = false
    if (duplicates.length > 0) {
      const dupCodes = duplicates.map(s => s.sampleCode).join('、')
      const confirmed = await appConfirm(`以下样品已存在于当前代号中：<br/><b>${dupCodes}</b><br/><br/>是否仍然添加（将更新快照数据）？`, '重复提醒', 'danger')
      if (!confirmed) return
      force = true
    }

    // 3. 保存到后端
    const query = force ? '?force=true' : ''
    const saveRes = await api(`/client-samples/${codeName.value}/items${query}`, {
      method: 'POST',
      body: JSON.stringify(sampleIds)
    })
    if (saveRes.code === 200) {
      // 4. 刷新表格数据，包含后端自动计算的报价
      await loadData()
    } else {
      appAlert('保存到后端失败: ' + (saveRes.message || '未知错误'), '错误', 'danger')
    }
  } catch (e) {
    console.error('添加样品失败:', e)
    appAlert('查询样品资料失败，请检查网络连接', '错误', 'danger')
  } finally {
    addInput.value = ''
    addLoading.value = false
  }
}

const onBatchAdd = () => {
  router.push({ name: 'ClientSampleBatchAdd', params: { codeName: codeName.value } })
}

const onModify = () => {
  if (!sample.value) return
  const s = sample.value
  modifyForm.id = s.id
  modifyForm.itemId = s.itemId
  modifyForm.sampleCode = s.sampleCode || ''
  modifyForm.factoryCode = s.factoryCode || ''
  modifyForm.sampleName = s.sampleName || ''
  modifyForm.englishName = s.englishName || ''
  modifyForm.factoryPrice = s.factoryPrice ?? ''
  modifyForm.calculatedPrice = s.calculatedPrice ?? ''
  modifyForm.taxPrice2 = s.taxPrice2 ?? ''
  modifyForm.categoryCode = s.categoryCode || ''
  modifyForm.category = s.category || ''
  modifyForm.boothNo = s.boothNo || ''
  modifyForm.name = s.name || ''
  modifyForm.manufacturerCode = s.manufacturerCode || ''
  modifyForm.color = s.color || ''
  modifyForm.colorEn = s.colorEn || ''
  modifyForm.origin = s.origin || ''
  modifyForm.packagingCn = s.packagingCn || ''
  modifyForm.packagingEn = s.packagingEn || ''
  modifyForm.sampleUnit = s.sampleUnit || ''
  modifyForm.innerBoxCount = s.innerBoxCount ?? ''
  modifyForm.cartonCapacity = s.cartonCapacity ?? ''
  modifyForm.packingUnit = s.packingUnit || ''
  modifyForm.cartonLength = s.cartonLength ?? ''
  modifyForm.cartonWidth = s.cartonWidth ?? ''
  modifyForm.cartonHeight = s.cartonHeight ?? ''
  modifyForm.cartonMaterialVolume = s.cartonMaterialVolume ?? ''
  modifyForm.cartonVolume = s.cartonVolume ?? ''
  modifyForm.cartonGrossWeight = s.cartonGrossWeight ?? ''
  modifyForm.cartonNetWeight = s.cartonNetWeight ?? ''
  modifyForm.sampleLength = s.sampleLength ?? ''
  modifyForm.sampleWidth = s.sampleWidth ?? ''
  modifyForm.sampleHeight = s.sampleHeight ?? ''
  modifyForm.sampleGrossWeight = s.sampleGrossWeight ?? ''
  modifyForm.sampleNetWeight = s.sampleNetWeight ?? ''
  modifyForm.packageLength = s.packageLength ?? ''
  modifyForm.packageWidth = s.packageWidth ?? ''
  modifyForm.packageHeight = s.packageHeight ?? ''
  modifyForm.certification = s.certification || ''
  modifyForm.batteryInfo = s.batteryInfo || ''
  modifyForm.contact1 = s.contact1 || ''
  modifyForm.phone1 = s.phone1 || ''
  modifyForm.mobile1 = s.mobile1 || ''
  modifyForm.fax = s.fax || ''
  modifyForm.qq = s.qq || ''
  modifyForm.remark = s.remark || ''
  modifyForm.remarkEn = s.remarkEn || ''
  modifyForm.otherRemark = s.otherRemark || ''
  modifyForm.boxCount = s.boxCount || ''
  modifyForm.registrant = s.registrant || ''
  modifyForm.modifier = s.modifier || ''
  modifySync.value = false
  modifyDragOver.value = false
  modifyDroppedFile.value = null
  modifyDroppedPreview.value = ''
  modifyOpen.value = true
  // 加载图片
  loadModifyImages(s.id)
}

const onModifyDrop = (e) => {
  modifyDragOver.value = false
  const file = e.dataTransfer?.files?.[0]
  if (!file || !file.type.startsWith('image/')) return
  modifyDroppedFile.value = file
  const reader = new FileReader()
  reader.onload = (ev) => {
    modifyDroppedPreview.value = ev.target.result
  }
  reader.readAsDataURL(file)
}

// ========== 修改弹窗 ==========
const modifyOpen = ref(false)
const modifySync = ref(false)
const modifySaving = ref(false)
const modifyImages = ref([])
const modifyImageIndex = ref(0)
const modifyDragOver = ref(false)
const modifyDroppedFile = ref(null)
const modifyDroppedPreview = ref('')
const modifyForm = reactive({
  id: null, itemId: null,
  sampleCode: '', factoryCode: '', sampleName: '', englishName: '',
  factoryPrice: '', calculatedPrice: '', taxPrice2: '',
  categoryCode: '', category: '', boothNo: '', name: '', manufacturerCode: '',
  color: '', colorEn: '', origin: '',
  packagingCn: '', packagingEn: '', sampleUnit: '',
  innerBoxCount: '', cartonCapacity: '', packingUnit: '',
  cartonLength: '', cartonWidth: '', cartonHeight: '',
  cartonMaterialVolume: '', cartonVolume: '',
  cartonGrossWeight: '', cartonNetWeight: '',
  sampleLength: '', sampleWidth: '', sampleHeight: '',
  sampleGrossWeight: '', sampleNetWeight: '',
  packageLength: '', packageWidth: '', packageHeight: '',
  certification: '', batteryInfo: '',
  contact1: '', phone1: '', mobile1: '', fax: '', qq: '',
  remark: '', remarkEn: '', otherRemark: '', boxCount: '', registrant: '', modifier: ''
})

const onModifySave = async () => {
  modifySaving.value = true
  try {
    // 始终更新快照（仅影响当前代号）
    const snapshotBody = { ...modifyForm }
    delete snapshotBody.id
    await api(`/client-samples/${codeName.value}/items/${modifyForm.itemId}`, {
      method: 'PUT',
      body: JSON.stringify(snapshotBody)
    })
    // 如果勾选同步，同时更新 samples 表
    if (modifySync.value) {
      const body = { ...modifyForm }
      delete body.id
      const res = await api(`/samples/${modifyForm.id}`, {
        method: 'PUT',
        body: JSON.stringify(body)
      })
      if (res.code !== 200) {
        appAlert('同步失败: ' + (res.message || '未知错误'), '错误', 'danger')
        return
      }
    }
    // 上传拖入的图片（需勾选同步）
    if (modifySync.value && modifyDroppedFile.value) {
      const fd = new FormData()
      fd.append('file', modifyDroppedFile.value)
      fd.append('sampleId', modifyForm.id)
      const imgRes = await api('/images/upload', {
        method: 'POST',
        headers: {},
        body: fd
      })
      if (imgRes.code === 200 && imgRes.data) {
        const newImg = imgRes.data
        modifyImages.value.unshift({ id: newImg.id, hash: newImg.hash, url: newImg.url || modifyDroppedPreview.value })
        modifyImageIndex.value = 0
      }
    }
    // 更新本地列表和卡片中的数据
    const idx = list.value.findIndex(item => item.itemId === modifyForm.itemId)
    if (idx >= 0) {
      Object.assign(list.value[idx], { ...modifyForm })
    }
    if (sample.value && sample.value.itemId === modifyForm.itemId) {
      Object.assign(sample.value, { ...modifyForm })
    }
    modifyOpen.value = false
    appAlert('修改成功', '提示', 'success')
  } catch (e) {
    console.error('修改失败:', e)
    appAlert('修改失败，请检查网络连接', '错误', 'danger')
  } finally {
    modifySaving.value = false
  }
}

const onDelete = async () => {
  if (!sample.value) return
  const row = sample.value
  const ok = await appConfirm(
    `确认删除公司编号「${row.sampleCode || ''}」的记录？<br/><small style="color:rgba(29,29,31,0.45)">删除后不可恢复</small>`,
    '删除记录',
    'danger'
  )
  if (!ok) return
  try {
    await api(`/client-samples/${codeName.value}/items/${row.itemId}`, { method: 'DELETE' })
    list.value = list.value.filter(item => item.itemId !== row.itemId)
    totalRecords.value = list.value.length
    sample.value = null
  } catch (e) {
    console.error('删除失败:', e)
    appAlert('删除失败，请检查网络连接', '错误', 'danger')
  }
}

const onToggleFlag = async (row, field, checked) => {
  row[field] = checked ? 1 : 0
  try {
    await api(`/client-samples/${codeName.value}/items/${row.itemId}/flag?field=${field}&value=${checked ? 1 : 0}`, {
      method: 'PUT'
    })
  } catch (e) {
    console.error('更新标记失败:', e)
    row[field] = checked ? 0 : 1
  }
}

const onBatchDelete = async () => {
  if (checkedRows.value.length === 0) {
    appAlert('请先勾选需要删除的记录', '提示', 'warning')
    return
  }
  const ids = checkedRows.value.map(row => row.itemId)
  const ok = await appConfirm(
    `确认删除选中的 ${ids.length} 条记录？<br/><small style="color:rgba(29,29,31,0.45)">删除后不可恢复</small>`,
    '批量删除',
    'danger'
  )
  if (!ok) return
  try {
    await api(`/client-samples/${codeName.value}/items`, {
      method: 'DELETE',
      body: JSON.stringify(ids)
    })
    const idSet = new Set(ids)
    list.value = list.value.filter(item => !idSet.has(item.itemId))
    totalRecords.value = list.value.length
    checkedRows.value = []
  } catch (e) {
    console.error('批量删除失败:', e)
    appAlert('批量删除失败，请检查网络连接', '错误', 'danger')
  }
}

const onPriceSetting = async () => {
  priceSettingOpen.value = true
  psType.value = '1'
  resetPsForm()
  loadPriceSetting('1')
  loadPriceSetting('2')
}

// 在新标签页中打开报表设计器，避免覆盖主页面
const openReportDesigner = () => {
  const url = router.resolve({ name: 'ReportDesigner' }).href
  window.open(url, '_blank')
}

const loadPriceSetting = async (type) => {
  try {
    const res = await api(`/client-samples/${codeName.value}/price-setting?type=${type}`)
    if (res.code === 200 && res.data) {
      if (type === '1') psHasSetting1.value = true
      else psHasSetting2.value = true
      const d = res.data
      const current = type === '1' ? psCurrent : psCurrent2
      current.method = d.method || '除法'
      current.profitRate = d.profitRate != null ? d.profitRate : 0
      current.totalCost = d.totalCost != null ? d.totalCost : 0
      current.exchangeRate = d.exchangeRate != null ? d.exchangeRate : 1
      current.markup = d.markup != null ? d.markup : 0
      current.decimals = d.decimals != null ? d.decimals : 2
      current.currency = d.currencyType || 'RMB'
      current.cartonSize = d.cartonSize != null ? d.cartonSize : 68
      current.roundMode = d.roundMode || '四舍五入'
      current.priceLessThan = d.priceLessThan != null ? d.priceLessThan : 0
      current.priceDecimals = d.priceDecimals != null ? d.priceDecimals : 2
      current.formulaType = d.formulaType || 'divide'
      current.formula = buildFormulaText(d)
      // 如果是当前标签页，回填表单
      if (type === psType.value) {
        fillPsForm(d)
      }
    }
  } catch (e) {
    console.error('加载报价设置失败:', e)
  }
}

const fillPsForm = (d) => {
  if (d.template != null) psForm.template = d.template
  if (d.method != null) psForm.method = d.method
  if (d.profitRate != null) psForm.profitRate = String(d.profitRate)
  if (d.totalCost != null) psForm.totalCost = String(d.totalCost)
  if (d.currencyType != null) psForm.currencyType = d.currencyType
  if (d.currencySymbol != null) psForm.currencySymbol = d.currencySymbol
  if (d.currencyName != null) psForm.currencyName = d.currencyName
  if (d.exchangeRate != null) psForm.exchangeRate = String(d.exchangeRate)
  if (d.cartonSize != null) psForm.cartonSize = String(d.cartonSize)
  syncCartonSizePreset(d.cartonSize != null ? d.cartonSize : 68)
  if (d.useCubicM != null) psForm.useCubicM = d.useCubicM === 1
  if (d.markup != null) psForm.markup = String(d.markup)
  if (d.formulaType != null) psForm.formulaType = d.formulaType
  if (d.roundMode != null) psForm.roundMode = d.roundMode
  if (d.decimals != null) psForm.decimals = String(d.decimals)
  if (d.priceLessThan != null) psForm.priceLessThan = String(d.priceLessThan)
  if (d.roundMode2 != null) psForm.roundMode2 = d.roundMode2
  if (d.priceDecimals != null) psForm.priceDecimals = String(d.priceDecimals)
  if (d.applyTo != null) psForm.applyTo = d.applyTo
  if (d.customFormula != null) {
    psForm.customFormula = d.customFormula
    nextTick(() => setFormulaContent(d.customFormula))
  } else {
    nextTick(() => setFormulaContent(''))
  }
}

const switchPsType = async (type) => {
  psType.value = type
  // 先重置表单为默认值（全新开始）
  resetPsForm()
  // 清空编辑器中的旧内容
  if (formulaEditor.value) formulaEditor.value.innerHTML = ''
  await loadPriceSetting(type)
  // 如果没有后端数据，保持重置后的默认值
}

const resetPsForm = () => {
  psForm.template = '除法'
  psForm.method = '除法'
  psForm.profitRate = '0'
  psForm.totalCost = '0'
  psForm.currencyType = 'RMB'
  psForm.currencySymbol = '¥'
  psForm.currencyName = '人民币'
  psForm.exchangeRate = '1'
  psForm.cartonSize = '68'
  cartonSizePreset.value = '68'
  psForm.useCubicM = false
  psForm.markup = '0'
  psForm.formulaType = 'divide'
  psForm.roundMode = '四舍五入'
  psForm.roundMode2 = '四舍五入'
  psForm.decimals = '2'
  psForm.priceLessThan = '0'
  psForm.priceDecimals = '2'
  psForm.customFormula = ''
  psForm.applyTo = 'continue'
}

// 报价设置面板数据
const priceSettingOpen = ref(false)
const psType = ref('1')  // 当前标签页: "1"=报价1, "2"=报价2
const psAlertShow = ref(true)
const psHasSetting1 = ref(false)
const psHasSetting2 = ref(false)
const psCurrent = reactive({
  method: '除法', profitRate: 0, totalCost: 0, exchangeRate: 1,
  decimals: 2, currency: 'RMB', cartonSize: 68, roundMode: '四舍五入',
  unit: 'PCS', priceLessThan: 0, priceDecimals: 2, formula: '', formulaType: 'divide', markup: 0
})
const psCurrent2 = reactive({
  method: '除法', profitRate: 0, totalCost: 0, exchangeRate: 1,
  decimals: 2, currency: 'RMB', cartonSize: 68, roundMode: '四舍五入',
  unit: 'PCS', priceLessThan: 0, priceDecimals: 2, formula: '', formulaType: 'divide', markup: 0
})
const psForm = reactive({
  template: '除法', method: '除法', currencyType: 'RMB',
  currencySymbol: '¥', currencyName: '人民币',
  formulaType: 'divide', roundMode: '四舍五入', roundMode2: '四舍五入',
  priceLessThan: '0', profitRate: '0', totalCost: '0',
  cartonSize: '68', useCubicM: false,
  markup: '0', exchangeRate: '1',
  decimals: '2', priceDecimals: '2', applyTo: 'continue',
  customFormula: ''
})

// 自定义公式面板数据
const cfFields = ['出厂价', '运费', '汇率', '利润率', '加价', '总费用', '外箱体积', '外箱材积', '外箱装量', '样品长', '样品宽', '样品高', '毛重', '净重', '外箱长', '外箱宽', '外箱高', '外箱毛重', '外箱净重', '税价', '内盒数']
const cfOps = ['+', '-', '×', '÷', '(', ')']
const cfNums = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.']
const formulaEditor = ref(null)
const CARTON_PRESETS = ['18', '28', '54', '68', '86']
const cartonSizePreset = ref('68')

const onCartonSizePresetChange = () => {
  if (cartonSizePreset.value !== '__custom__') {
    psForm.cartonSize = cartonSizePreset.value
  }
}

// 根据 cartonSize 同步 preset 显示
const syncCartonSizePreset = (val) => {
  const v = String(val)
  cartonSizePreset.value = CARTON_PRESETS.includes(v) ? v : '__custom__'
}

const cartonVolumeLabel = computed(() => {
  const sz = parseInt(psForm.cartonSize)
  return isNaN(sz) || sz <= 100 ? '外箱体积' : '外箱材积'
})

// 字段名集合，用于判断是否是变量
// 生成公式预览文本
const buildFormulaText = (d) => {
  if (!d) return ''
  const ft = d.formulaType
  if (ft === 'custom') {
    return d.customFormula || '(未设置)'
  } else if (ft === 'multiply') {
    return '((出厂价+运费)×(1+利润率%)+加价)÷汇率'
  } else {
    return '((出厂价+运费)÷(1-利润率%)+加价)÷汇率'
  }
}

const cfFieldSet = new Set(cfFields)

// 从 contenteditable div 提取纯文本公式
const getFormulaText = () => {
  const el = formulaEditor.value
  if (!el) return ''
  let text = ''
  el.childNodes.forEach(node => {
    if (node.nodeType === 3) {
      // 文本节点
      text += node.textContent
    } else if (node.nodeType === 1) {
      // 元素节点（变量 pill）
      if (node.classList.contains('cf-var')) {
        text += node.getAttribute('data-var') || node.textContent
      } else {
        text += node.textContent
      }
    }
  })
  // 统一全角→半角
  return text.replace(/[（]/g, '(').replace(/[）]/g, ')').replace(/[×]/g, '*').replace(/[÷]/g, '/').replace(/[＋]/g, '+').replace(/[－]/g, '-')
}

// 从纯文本重建 contenteditable 内容（用于加载已保存公式）
const setFormulaContent = (text) => {
  const el = formulaEditor.value
  if (!el) return
  el.innerHTML = ''
  if (!text) return
  // 按变量名拆分，交替插入文本和pill
  const parts = []
  let remaining = text
  while (remaining.length > 0) {
    let found = false
    for (const field of cfFields) {
      if (remaining.startsWith(field)) {
        parts.push({ type: 'var', value: field })
        remaining = remaining.slice(field.length)
        found = true
        break
      }
    }
    if (!found) {
      // 取连续非变量字符
      let i = 0
      while (i < remaining.length) {
        let isField = false
        for (const field of cfFields) {
          if (remaining.slice(i).startsWith(field)) { isField = true; break }
        }
        if (isField) break
        i++
      }
      if (i > 0) {
        parts.push({ type: 'text', value: remaining.slice(0, i) })
        remaining = remaining.slice(i)
      } else {
        parts.push({ type: 'text', value: remaining[0] })
        remaining = remaining.slice(1)
      }
    }
  }
  parts.forEach(p => {
    if (p.type === 'var') {
      const span = document.createElement('span')
      span.className = 'cf-var'
      span.contentEditable = 'false'
      span.textContent = p.value
      span.setAttribute('data-var', p.value)
      el.appendChild(span)
    } else {
      el.appendChild(document.createTextNode(p.value))
    }
  })
}

const appendFormula = (text) => {
  const el = formulaEditor.value
  if (!el) return
  el.focus()
  // 转换运算符
  if (text === '×') text = '*'
  if (text === '÷') text = '/'
  if (cfFieldSet.has(text)) {
    // 字段：插入不可编辑的 pill
    const span = document.createElement('span')
    span.className = 'cf-var'
    span.contentEditable = 'false'
    span.textContent = text
    span.setAttribute('data-var', text)
    insertAtCursor(el, span)
  } else {
    insertAtCursor(el, document.createTextNode(text))
  }
  // 同步到 psForm
  psForm.customFormula = getFormulaText()
}

const onFormulaInput = () => {
  psForm.customFormula = getFormulaText()
}

const onFormulaPaste = (e) => {
  const text = e.clipboardData.getData('text/plain')
  if (text) {
    insertAtCursor(formulaEditor.value, document.createTextNode(text))
    psForm.customFormula = getFormulaText()
  }
}

// 在光标处插入节点
const insertAtCursor = (el, node) => {
  const sel = window.getSelection()
  if (sel.rangeCount && el.contains(sel.anchorNode)) {
    const range = sel.getRangeAt(0)
    range.deleteContents()
    // 如果是变量pill，在后面加个空格方便继续输入
    range.insertNode(node)
    if (node.classList && node.classList.contains('cf-var')) {
      const space = document.createTextNode(' ')
      range.setStartAfter(node)
      range.collapse(true)
      range.insertNode(space)
      range.setStartAfter(space)
      range.collapse(true)
      sel.removeAllRanges()
      sel.addRange(range)
    } else {
      range.setStartAfter(node)
      range.collapse(true)
      sel.removeAllRanges()
      sel.addRange(range)
    }
  } else {
    el.appendChild(node)
  }
}

// 货币种类对应符号和名称
const currencyMap = {
  RMB: { symbol: '¥', name: '人民币' },
  USD: { symbol: '$', name: '美元' },
  HKD: { symbol: '$', name: '港币' },
  EUR: { symbol: '€', name: '欧元' }
}
watch(() => psForm.currencyType, (val) => {
  if (currencyMap[val]) {
    psForm.currencySymbol = currencyMap[val].symbol
    psForm.currencyName = currencyMap[val].name
  }
})

// 报价模板联动公式类型
watch(() => psForm.template, (val) => {
  if (val === '乘法') { psForm.formulaType = 'multiply'; psForm.method = '乘法' }
  else if (val === '除法') { psForm.formulaType = 'divide'; psForm.method = '除法' }
  else if (val === '自定义') { psForm.formulaType = 'custom'; psForm.method = '自定义' }
})

// 报价方式联动公式类型
watch(() => psForm.method, (val) => {
  if (val === '乘法') { psForm.formulaType = 'multiply'; psForm.template = '乘法' }
  else if (val === '除法') { psForm.formulaType = 'divide'; psForm.template = '除法' }
  else if (val === '自定义') { psForm.formulaType = 'custom'; psForm.template = '自定义' }
})

// 公式类型 radio 联动模板和报价方式
watch(() => psForm.formulaType, (val) => {
  if (val === 'multiply') { psForm.template = '乘法'; psForm.method = '乘法' }
  else if (val === 'divide') { psForm.template = '除法'; psForm.method = '除法' }
  else if (val === 'custom') { psForm.template = '自定义'; psForm.method = '自定义' }
})

const onSavePriceSetting = async () => {
  const setting = {
    template: psForm.template,
    method: psForm.method,
    profitRate: parseFloat(psForm.profitRate) || 0,
    totalCost: parseFloat(psForm.totalCost) || 0,
    currencyType: psForm.currencyType,
    currencySymbol: psForm.currencySymbol,
    currencyName: psForm.currencyName,
    exchangeRate: parseFloat(psForm.exchangeRate) || 1,
    cartonSize: (v => isNaN(v) ? 68 : v)(parseInt(psForm.cartonSize)),
    useCubicM: psForm.useCubicM ? 1 : 0,
    markup: parseFloat(psForm.markup) || 0,
    formulaType: psForm.formulaType,
    roundMode: psForm.roundMode,
    decimals: (v => isNaN(v) ? 2 : v)(parseInt(psForm.decimals)),
    priceLessThan: parseFloat(psForm.priceLessThan) || 0,
    roundMode2: psForm.roundMode2,
    priceDecimals: (v => isNaN(v) ? 2 : v)(parseInt(psForm.priceDecimals)),
    customFormula: psForm.customFormula,
    applyTo: psForm.applyTo
  }
  // 获取勾选的样品ID（当前打勾模式）
  let sampleIds = null
  if (psForm.applyTo === 'current') {
    const records = gridRef.value?.getCheckboxRecords() || []
    sampleIds = records.map(r => r.id)
  }
  const payload = { setting, sampleIds }
  try {
    const res = await api(`/client-samples/${codeName.value}/price-setting?type=${psType.value}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    if (res.code === 200) {
      if (psType.value === '1') psHasSetting1.value = true
      else psHasSetting2.value = true
      const current = psType.value === '1' ? psCurrent : psCurrent2
      current.method = setting.method
      current.profitRate = setting.profitRate
      current.totalCost = setting.totalCost
      current.exchangeRate = setting.exchangeRate
      current.decimals = setting.decimals
      current.currency = setting.currencyType
      current.cartonSize = setting.cartonSize
      current.roundMode = setting.roundMode
      current.priceLessThan = setting.priceLessThan
      current.priceDecimals = setting.priceDecimals
      current.formulaType = setting.formulaType
      current.formula = buildFormulaText(setting)
      appAlert('报价设置保存成功', '提示')
      priceSettingOpen.value = false
      // 刷新表格，显示计算后的报价
      loadData()
    } else {
      appAlert('保存失败: ' + (res.message || '未知错误'), '错误', 'danger')
    }
  } catch (e) {
    console.error('保存报价设置失败:', e)
    appAlert('保存报价设置失败，请检查网络连接', '错误', 'danger')
  }
}

const onSendSms = () => {
  if (checkedRows.value.length === 0) return
  // 按厂商去重，同厂商的货号用逗号拼接
  const map = new Map()
  checkedRows.value.forEach(r => {
    const key = r.manufacturerCode
    if (!map.has(key)) {
      map.set(key, {
        itemId: r.itemId,
        manufacturerCode: r.manufacturerCode || '-',
        manufacturerName: r.name || r.manufacturerCode || '-',
        mobile1: r.smsNumber || r.mobile1 || r.phone1 || '',
        boothNo: r.boothNo || '',
        factoryCodes: [r.factoryCode || '']
      })
    } else {
      const existing = map.get(key)
      if (r.factoryCode && !existing.factoryCodes.includes(r.factoryCode)) {
        existing.factoryCodes.push(r.factoryCode)
      }
    }
  })
  const manufacturers = Array.from(map.values()).map(m => ({
    ...m,
    factoryCode: m.factoryCodes.filter(Boolean).join(',') || ''
  }))
  sessionStorage.setItem('sms_checked_rows', JSON.stringify(manufacturers))
  sessionStorage.setItem('sms_record_info', JSON.stringify({
    codeName: record.value?.codeName || codeName.value,
    clientName: record.value?.clientName || ''
  }))
  router.push({ name: 'ClientSampleSms', params: { codeName: codeName.value } })
}

const onPrintPrice = () => {
  // TODO: 报价单打印
}

// ===== 蓝牙打印模版设置 =====
const labelTemplateOpen = ref(false)
const btLoading = ref(false)
const btTemplateList = ref([])
const btSelectedId = ref(null)
const btCurrentTemplate = ref(null)
const btWidth = ref('58')
const btPrintOpen = ref(false)
const btFieldList = ref([])
const btGridRef = ref(null)
const btRowConfig = computed(() => ({ isHover: true, keyField: 'field', useKey: true, drag: true }))
const btGridId = computed(() => `bt-label-template-${btSelectedId.value || 'new'}`)
const btEditing = ref(false)
const btShowPrintedOnly = ref(false)
const btAllChecked = computed(() => btFieldList.value.length > 0 && btFieldList.value.every(f => f.isPrinted))
const btAllShowTitle = computed(() => btFieldList.value.length > 0 && btFieldList.value.every(f => f.showTitle))
const btBatchFontSize = ref(12)
const btBatchLineSpacing = ref(2)
const btDisplayList = computed(() => btShowPrintedOnly.value ? btFieldList.value.filter(f => f.isPrinted) : btFieldList.value)

const btColumns = [
  { field: 'label', title: '字段名称', minWidth: 175, align: 'left', dragSort: true },
  { field: 'printTitle', title: '打印标题', minWidth: 165, align: 'center', slots: { default: 'printTitle_default' } },
  { field: 'hideValue', title: '隐藏数据', width: 125, align: 'center', slots: { default: 'hideValue_default' } },
  { field: 'showTitle', title: '是否打印标题', width: 185, align: 'center', slots: { default: 'showTitle_default' } },
  { field: 'fontSizeLarge', title: '字号', width: 120, align: 'center', slots: { default: 'fontSizeLarge_default' } },
  { field: 'fontWeightBold', title: '字体加粗', width: 130, align: 'center', slots: { default: 'fontWeightBold_default' } },
  { field: 'lineSpacing', title: '行距', width: 120, align: 'center', slots: { default: 'lineSpacing_default' } },
  { field: 'sortOrder', title: '打印顺序', width: 125, align: 'center', slots: { default: 'sortOrder_default' } },
  { field: 'isPrinted', title: '是否打印', width: 125, align: 'center', slots: { default: 'isPrinted_default' } },
]

const btFieldLabels = {
  sampleCode: '公司编号', factoryCode: '出厂货号', sampleName: '样品名称', englishName: '英文名称',
  factoryPrice: '出厂价', calculatedPrice: '价格1', taxPrice2: '价格2',
  packagingCn: '中文包装', packagingEn: '英文包装', sampleUnit: '样品单位',
  cartonCapacity: '外箱装量', innerBoxCount: '内盒数', packingUnit: '装箱单位',
  cartonLength: '外箱长', cartonWidth: '外箱宽', cartonHeight: '外箱高',
  cartonSpec: '外箱规格', cartonVolume: '外箱体积', cartonMaterialVolume: '外箱材积',
  cartonGrossWeight: '外箱毛重', cartonNetWeight: '外箱净重',
  sampleLength: '样品长', sampleWidth: '样品宽', sampleHeight: '样品高',
  sampleGrossWeight: '产品毛重', sampleNetWeight: '产品净重',
  packageLength: '包装长', packageWidth: '包装宽', packageHeight: '包装高',
  boothNo: '摊位号', color: '颜色', colorEn: '英文颜色', certification: '产品认证',
  category: '种类名称', categoryCode: '种类编号', productSpec: '产品规格',
  name: '厂商名称', manufacturerCode: '厂商编号', origin: '产地',
  batteryInfo: '电池信息', infringement: '侵权信息',
  remark: '中文备注', remarkEn: '英文备注', otherRemark: '其他备注',
  boxCount: '箱数', registrant: '登记人', modifier: '修改人',
  addDate: '添加日期', modifyDate: '修改日期', createTime: '登记时间', updateTime: '修改时间',
  exchangeRate: '货币汇率', profitRate: '报价利润',
  contact1: '联系人', phone1: '联系电话', mobile1: '手机', fax: '传真', qq: 'QQ',
  showroomReplenished: '展厅已补', borrowedSample: '借样',
  smsSent: '已发短信', vendorCertification: '厂商认证',
  hideFromXzx: '是否不在小竹熊显示',
  custom1: '自定义1', custom2: '自定义2', custom3: '自定义3', custom4: '自定义4', custom5: '自定义5',
}

function btDefaultFields() {
  return [
    'sampleCode', 'sampleName', 'factoryCode', 'englishName',
    'factoryPrice', 'calculatedPrice', 'taxPrice2',
    'boothNo', 'name', 'manufacturerCode',
    'category', 'categoryCode', 'color', 'colorEn',
    'packagingCn', 'packagingEn', 'sampleUnit',
    'innerBoxCount', 'cartonCapacity', 'packingUnit',
    'cartonLength', 'cartonWidth', 'cartonHeight', 'cartonSpec',
    'cartonVolume', 'cartonMaterialVolume',
    'cartonGrossWeight', 'cartonNetWeight',
    'sampleLength', 'sampleWidth', 'sampleHeight',
    'sampleGrossWeight', 'sampleNetWeight',
    'packageLength', 'packageWidth', 'packageHeight',
    'certification', 'batteryInfo', 'origin',
    'boxCount', 'remark', 'remarkEn', 'otherRemark',
    'addDate', 'modifyDate',
    'showroomReplenished', 'borrowedSample',
    'contact1', 'phone1', 'mobile1', 'fax', 'qq',
    'registrant', 'modifier',
    'custom1', 'custom2', 'custom3', 'custom4', 'custom5',
  ]
}

function btMakeFieldList(fieldsConfig) {
  const map = {}
  if (fieldsConfig) {
    try {
      const arr = typeof fieldsConfig === 'string' ? JSON.parse(fieldsConfig) : fieldsConfig
      if (Array.isArray(arr)) arr.forEach(f => { map[f.field] = f })
    } catch {}
  }
  return btDefaultFields().map((f, i) => {
    const cfg = map[f] || {}
    const isCustom = f.startsWith('custom')
    return {
      field: f,
      label: btFieldLabels[f] || f,
      printTitle: cfg.printTitle || (isCustom ? '' : btFieldLabels[f] || f),
      showTitle: cfg.showTitle !== undefined ? cfg.showTitle : true,
      fontSizeLarge: cfg.fontSizeLarge || false,
      fontSize: cfg.fontSize || 12,
      fontWeightBold: cfg.fontWeightBold || false,
      hideValue: cfg.hideValue !== undefined ? cfg.hideValue : isCustom,
      lineSpacing: cfg.lineSpacing || 2,
      sortOrder: cfg.sortOrder != null ? cfg.sortOrder : i + 1,
      isPrinted: cfg.isPrinted !== undefined ? cfg.isPrinted : !isCustom,
    }
  }).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
}

async function loadBtTemplates() {
  btLoading.value = true
  try {
    const resp = await api('/bluetooth-label-templates')
    if (resp.code === 200 && resp.data) {
      btTemplateList.value = resp.data
    }
  } catch (e) {
    console.error('加载蓝牙标签模板失败:', e)
    btTemplateList.value = []
  } finally {
    btLoading.value = false
  }
}

async function openLabelTemplateModal() {
  labelTemplateOpen.value = true
  btEditing.value = false
  await loadBtTemplates()
  // 加载当前择样单已关联的模板
  try {
    const selResp = await api(`/client-samples/${codeName.value}/label-templates`)
    if (selResp.code === 200 && selResp.data) {
      const ids = selResp.data.split(',').map(id => parseInt(id)).filter(id => id)
      if (ids.length && btTemplateList.value.some(t => t.id === ids[0])) {
        btSelectedId.value = ids[0]
        onBtSelectTemplate()
        return
      }
    }
  } catch {}
  // 默认选中第一个模板或新建状态
  if (btTemplateList.value.length) {
    btSelectedId.value = btTemplateList.value[0].id
    onBtSelectTemplate()
  } else {
    btSelectedId.value = null
    btCurrentTemplate.value = null
    btWidth.value = '58'
    btFieldList.value = btMakeFieldList(null)
  }
}

// 清除 vxe localStorage 中的拖拽缓存，避免旧顺序覆盖后端排序数据
function clearBtGridStorage() {
  try {
    const raw = localStorage.getItem('VXE_CUSTOM_STORE')
    if (!raw) return
    const maps = JSON.parse(raw)
    if (maps[btGridId.value]) {
      delete maps[btGridId.value]
      maps._v = VxeUI.getConfig().version
      localStorage.setItem('VXE_CUSTOM_STORE', JSON.stringify(maps))
    }
  } catch {}
}

function onBtSelectTemplate() {
  btEditing.value = false
  const tpl = btTemplateList.value.find(t => t.id === btSelectedId.value)
  if (tpl) {
    btCurrentTemplate.value = tpl
    btWidth.value = String(tpl.width || '58')
    btFieldList.value = btMakeFieldList(tpl.fields)
  } else {
    btCurrentTemplate.value = null
    btWidth.value = '58'
    btFieldList.value = btMakeFieldList(null)
  }
  // 清除旧 localStorage 缓存，让 vxe 按后端 sortOrder 排序
  nextTick(() => clearBtGridStorage())
}

function onBtEditTemplate() {
  if (!btSelectedId.value) {
    appAlert('请先选择一个模板', '提示', 'warning')
    return
  }
  btEditing.value = true
}

function onBtNewTemplate() {
  btSelectedId.value = null
  btCurrentTemplate.value = null
  btWidth.value = '58'
  btFieldList.value = btMakeFieldList(null)
  btEditing.value = true
  nextTick(() => clearBtGridStorage())
}

function onBtDragEnd() {
  // 拖拽排序后，从 vxe 内部获取最新顺序并更新 sortOrder
  nextTick(() => {
    const grid = btGridRef.value
    if (!grid) return
    const newData = grid.getTableData().fullData
    if (newData && newData.length) {
      btFieldList.value = [...newData]
      btFieldList.value.forEach((item, idx) => {
        item.sortOrder = idx + 1
      })
    }
  })
}

function onBtBatchFontSize() {
  if (!btBatchFontSize.value) return
  btFieldList.value.forEach(f => {
    f.fontSize = btBatchFontSize.value
  })
}

function onBtBatchLineSpacing() {
  if (!btBatchLineSpacing.value) return
  btFieldList.value.forEach(f => {
    f.lineSpacing = btBatchLineSpacing.value
  })
}

function btCheckAll(val) {
  btFieldList.value.forEach(f => { f.isPrinted = val })
  btFieldList.value = [...btFieldList.value]
}

function btCheckAllShowTitle(val) {
  btFieldList.value.forEach(f => { f.showTitle = val })
  btFieldList.value = [...btFieldList.value]
}

function onBtWidthChanged(save = false) {
  if (save && btSelectedId.value) {
    const fields = btFieldList.value.map(f => ({
      field: f.field, printTitle: f.printTitle, showTitle: f.showTitle,
      fontSizeLarge: f.fontSizeLarge, fontSize: f.fontSize,
      fontWeightBold: f.fontWeightBold, hideValue: f.hideValue, lineSpacing: f.lineSpacing,
      sortOrder: f.sortOrder, isPrinted: f.isPrinted
    }))
    api(`/bluetooth-label-templates/${btSelectedId.value}`, {
      method: 'PUT',
      body: JSON.stringify({ name: btCurrentTemplate.value?.name, width: parseInt(btWidth.value), fields: JSON.stringify(fields) })
    })
  }
}

const btPrintFields = computed(() => {
  return btFieldList.value
    .filter(f => f.isPrinted)
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
})

function onBtPrintPreview() {
  if (!btSelectedId.value && !btCurrentTemplate.value) {
    appAlert('请先选择或新建一个模板', '提示', 'warning')
    return
  }
  if (btPrintFields.value.length === 0) {
    appAlert('没有勾选任何打印字段', '提示', 'warning')
    return
  }
  btPrintOpen.value = true
}

function btDoPrint() {
  // 构建打印字段列表
  const fields = btPrintFields.value
  const items = tableData.value
  const widthMm = parseInt(btWidth.value) === 58 ? 55 : 78

  // 生成标签 HTML
  let labelsHtml = ''
  items.forEach(item => {
    let rows = ''
    fields.forEach(f => {
      const title = f.showTitle ? `<span class="btpt">${f.printTitle || f.label}：</span>` : ''
      const rawVal = item[f.field] !== undefined && item[f.field] !== null ? String(item[f.field]) : ''
      const val = f.hideValue ? '<span style="visibility:hidden">-</span>' : rawVal
      rows += `<div class="btpr" style="font-size:${f.fontSize || 12}px;font-weight:${f.fontWeightBold ? 'bold' : 'normal'};line-height:${(f.fontSize || 12) * (f.lineSpacing || 2)}px">${title}<span class="btpv">${val}</span></div>`
    })
    labelsHtml += `<div class="btpl" style="width:${widthMm}mm">${rows}</div>`
  })

  const html = `<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>标签打印</title>
<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  body { font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif; padding: 10px; }
  .btpl { display:inline-block; vertical-align:top; margin:0 4px 12px 0; padding:8px 10px;
    border:1px solid #ccc; background:#fff; word-break:break-all; }
  .btpr { word-break:break-all; }
  .btpt { color:#888; }
  .btpv { color:#000; }
  @media print { .btpl { page-break-inside:avoid; border:0; margin:0 2px 4px 0; padding:4px 6px; } }
</style></head><body>${labelsHtml}</body></html>`

  const w = window.open('', '_blank', 'width=800,height=600')
  w.document.write(html)
  w.document.close()
  w.focus()
  setTimeout(() => { w.print() }, 300)
}

async function renameBtTemplate() {
  if (!btSelectedId.value || !btCurrentTemplate.value) return
  const name = btCurrentTemplate.value.name?.trim()
  if (!name) {
    appAlert('模板名称不能为空', '提示', 'warning')
    return
  }
  try {
    const resp = await api(`/bluetooth-label-templates/${btSelectedId.value}`, {
      method: 'PUT',
      body: JSON.stringify({ name })
    })
    if (resp.code === 200) {
      // 同步更新本地模板列表中的名称，避免 reload 导致闪烁
      const tpl = btTemplateList.value.find(t => t.id === btSelectedId.value)
      if (tpl) tpl.name = name
    }
  } catch (e) {
    console.error('改名失败:', e)
  }
}

async function onBtDeleteTemplate() {
  if (!btSelectedId.value) return
  const ok = await appConfirm('确定删除当前模板？此操作不可恢复。', '确认删除', 'danger')
  if (!ok) return
  try {
    const resp = await api(`/bluetooth-label-templates/${btSelectedId.value}`, { method: 'DELETE' })
    if (resp.code === 200) {
      appAlert('删除成功')
      btSelectedId.value = null
      btCurrentTemplate.value = null
      btFieldList.value = btMakeFieldList(null)
      await loadBtTemplates()
    }
  } catch (e) {
    console.error('删除失败:', e)
  }
}

async function saveBtTemplate() {
  const fields = btFieldList.value.map(f => ({
    field: f.field, printTitle: f.printTitle, showTitle: f.showTitle,
    fontSizeLarge: f.fontSizeLarge, fontSize: f.fontSize,
    fontWeightBold: f.fontWeightBold, hideValue: f.hideValue, lineSpacing: f.lineSpacing,
    sortOrder: f.sortOrder, isPrinted: f.isPrinted
  }))
  try {
    let templateId = btSelectedId.value
    if (btCurrentTemplate.value && templateId) {
      // 更新已有模板
      const resp = await api(`/bluetooth-label-templates/${templateId}`, {
        method: 'PUT',
        body: JSON.stringify({ name: btCurrentTemplate.value.name, width: parseInt(btWidth.value), fields: JSON.stringify(fields) })
      })
      if (resp.code === 200) {
        // 同时保存到择样单关联
        await api(`/client-samples/${codeName.value}/label-templates`, {
          method: 'PUT',
          body: JSON.stringify({ templateIds: String(templateId) })
        })
        btEditing.value = false
        appAlert('保存成功')
      }
    } else {
      // 新建模板：弹出命名框
      const name = await new Promise(resolve => {
        const input = document.createElement('div')
        input.innerHTML = `<div class="app-dialog-mask show" style="position:fixed;top:0;left:0;right:0;bottom:0;z-index:10001;background:rgba(0,0,0,0.3);display:flex;align-items:center;justify-content:center;">
          <div style="background:#fff;border-radius:20px;padding:24px 28px;min-width:340px;box-shadow:0 20px 60px rgba(0,0,0,0.18);">
            <div style="font-weight:700;font-size:16px;margin-bottom:16px;">新建模板</div>
            <input id="bt-new-name" style="width:100%;height:40px;border-radius:10px;border:1px solid #d1d5db;padding:0 14px;font-size:14px;outline:none;box-sizing:border-box;" placeholder="请输入模板名称" />
            <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:16px;">
              <button id="bt-new-cancel" style="padding:8px 20px;border-radius:10px;border:1px solid #ddd;background:#fff;cursor:pointer;">取消</button>
              <button id="bt-new-ok" style="padding:8px 20px;border-radius:10px;border:none;background:#007aff;color:#fff;cursor:pointer;font-weight:600;">确定</button>
            </div>
          </div></div>`
        document.body.appendChild(input.firstElementChild)
        const mask = document.querySelector('.app-dialog-mask.show')
        const inp = mask.querySelector('#bt-new-name')
        mask.querySelector('#bt-new-ok').onclick = () => {
          mask.remove()
          resolve(inp.value.trim())
        }
        mask.querySelector('#bt-new-cancel').onclick = () => {
          mask.remove()
          resolve(null)
        }
        inp.addEventListener('keydown', e => {
          if (e.key === 'Enter') { mask.remove(); resolve(inp.value.trim()) }
          if (e.key === 'Escape') { mask.remove(); resolve(null) }
        })
        inp.focus()
      })
      if (!name) return

      const resp = await api('/bluetooth-label-templates', {
        method: 'POST',
        body: JSON.stringify({ name, width: parseInt(btWidth.value), fields: JSON.stringify(fields) })
      })
      if (resp.code === 200 && resp.data) {
        templateId = resp.data.id
        btSelectedId.value = templateId
        btCurrentTemplate.value = resp.data
        // 关联到择样单
        await api(`/client-samples/${codeName.value}/label-templates`, {
          method: 'PUT',
          body: JSON.stringify({ templateIds: String(templateId) })
        })
        await loadBtTemplates()
        btEditing.value = false
        appAlert('新建成功')
      }
    }
  } catch (e) {
    console.error('保存蓝牙标签模板失败:', e)
    appAlert('保存失败,请检查网络', '错误', 'danger')
  }
}

const onSubmitToInventory = async () => {
  if (checkedRows.value.length === 0) {
    appAlert('请先勾选需要提交入库的记录', '提示', 'warning')
    return
  }
  const ok = await appConfirm(
    `确认将选中的 ${checkedRows.value.length} 条记录提交入库？<br/><small style="color:rgba(29,29,31,0.45)">提交后数据将关联到总库存</small>`,
    '提交入库'
  )
  if (!ok) return
  try {
    const items = checkedRows.value.map(row => ({
      companyCode: row.sampleCode,
      quantity: 1
    }))
    const creator = auth.state.userInfo?.realName || auth.state.userInfo?.username || ''
    const res = await api(`/inventory/${codeName.value}/items/batch`, {
      method: 'POST',
      body: JSON.stringify({ creator, items, submitted: true })
    })
    if (res.code === 200) {
      const data = res.data
      appAlert(`提交入库完成：成功 ${data.success} 条，失败 ${data.fail} 条`, '提示', 'success')
      gridRef.value?.setAllCheckboxRow(false)
      checkedRows.value = []
      await loadData()
    } else {
      appAlert('提交入库失败: ' + (res.message || '未知错误'), '错误', 'danger')
    }
  } catch (e) {
    console.error('提交入库失败:', e)
    appAlert('提交入库失败，请检查网络连接', '错误', 'danger')
  }
}

const onSubmitToOutbound = async () => {
  if (checkedRows.value.length === 0) {
    appAlert('请先勾选需要提交出库的记录', '提示', 'warning')
    return
  }
  const ok = await appConfirm(
    `确认将选中的 ${checkedRows.value.length} 条记录提交出库？<br/><small style="color:rgba(29,29,31,0.45)">提交后数据将关联到总库存</small>`,
    '提交出库'
  )
  if (!ok) return
  try {
    const items = checkedRows.value.map(row => ({
      companyCode: row.sampleCode,
      quantity: 1
    }))
    const creator = auth.state.userInfo?.realName || auth.state.userInfo?.username || ''
    const res = await api(`/outbound/${codeName.value}/items/batch`, {
      method: 'POST',
      body: JSON.stringify({ creator, items, submitted: true })
    })
    if (res.code === 200) {
      const data = res.data
      appAlert(`提交出库完成：成功 ${data.success} 条，失败 ${data.fail} 条`, '提示', 'success')
      gridRef.value?.setAllCheckboxRow(false)
      checkedRows.value = []
      await loadData()
    } else {
      appAlert('提交出库失败: ' + (res.message || '未知错误'), '错误', 'danger')
    }
  } catch (e) {
    console.error('提交出库失败:', e)
    appAlert('提交出库失败，请检查网络连接', '错误', 'danger')
  }
}

const onPrintLabel = () => {
  openLabelTemplateModal()
}

// ========== 导出图片 ==========
const exportModalOpen = ref(false)
const exportNamingMode = ref('sampleCode')
// 计算已勾选样品中有图/无图数量
const checkedImageCount = computed(() => {
  const withImg = checkedRows.value.filter(r => r.firstImageHash).length
  const withoutImg = checkedRows.value.length - withImg
  return { withImg, withoutImg }
})
const exportFolderName = ref('')
const exportIncludePricing = ref(false)
const exportIncludeImages = ref(true)
const exportPricingTemplateId = ref(null)
const exportPricingFileName = ref('')
const pricingTemplates = ref([])
const exportStatus = ref('idle') // idle | loading | done | error
const exportProgress = ref(0)
const exportPricingProgress = ref(0) // 报价导出独立进度
const exportImageProgress = ref(0)   // 图片导出独立进度
const exportCurrent = ref(0)
const exportTotal = ref(0)
const exportStatusText = ref('')
const exportMinimized = ref(false)
const exportFloatingVisible = ref(false)
const exportStartTime = ref(0)
let currentExportTaskId = null // 当前弹窗关联的导出任务 ID
const exportTasks = ref([]) // 并行导出任务列表: { id, abortController, progress, startTime }

const onExportImage = () => {
  const gridRows = gridRef.value?.getCheckboxRecords()
  const hasSelection = (gridRows && gridRows.length > 0) || checkedRows.value.length > 0
  if (!hasSelection) {
    appAlert('请先勾选需要导出的样品', '提示', 'warning')
    return
  }
  exportModalOpen.value = true
  exportMinimized.value = false
  exportFloatingVisible.value = false
  exportNamingMode.value = 'sampleCode'
  exportFolderName.value = ''
  exportIncludeImages.value = true
  exportIncludePricing.value = false
  exportPricingTemplateId.value = null
  exportPricingFileName.value = ''
  exportStatus.value = 'idle'
  loadPricingTemplates()
  exportProgress.value = 0
  exportPricingProgress.value = 0
  exportImageProgress.value = 0
  exportCurrent.value = 0
  exportTotal.value = 0
  exportStatusText.value = ''
}

const onExportOverlayClick = () => {
  if (exportStatus.value !== 'loading') {
    exportModalOpen.value = false
  }
}

// 最小化导出弹窗到右下角浮动小窗
const onMinimizeExport = () => {
  exportMinimized.value = true
  exportFloatingVisible.value = true
  exportModalOpen.value = false
}

// 关闭导出弹窗（如果正在导出，自动最小化而非关闭）
const onCloseExport = () => {
  if (exportStatus.value === 'loading') {
    onMinimizeExport()
    return
  }
  exportModalOpen.value = false
}

// 从浮动小窗恢复导出弹窗
const onRestoreExport = () => {
  exportMinimized.value = false
  exportFloatingVisible.value = false
  exportModalOpen.value = true
}

// 取消导出
const onCancelExport = () => {
  if (exportStatus.value === 'loading' && currentExportTaskId) {
    const task = exportTasks.value.find(t => t.id === currentExportTaskId)
    if (task) {
      task.abortController.abort()
      exportTasks.value = exportTasks.value.filter(t => t.id !== currentExportTaskId)
    }
    exportStatus.value = 'idle'
    exportStatusText.value = '已取消导出'
    exportProgress.value = 0
    exportPricingProgress.value = 0
    exportImageProgress.value = 0
    exportFloatingVisible.value = false
    currentExportTaskId = null
  }
  exportModalOpen.value = false
}

// ===== 卡通可爱风导出完成通知 =====
const showExportDoneNotify = (elapsedSec, imageCount, hasPricing) => {
  // 移除已存在的通知
  const existing = document.querySelector('.export-done-notify')
  if (existing) existing.remove()

  const formatTime = (sec) => {
    if (sec < 60) return `${sec}秒`
    const m = Math.floor(sec / 60)
    const s = sec % 60
    return `${m}分${s}秒`
  }

  const elapsedStr = formatTime(elapsedSec)
  const detailParts = []
  if (imageCount > 0) detailParts.push(`${imageCount}张图片`)
  if (hasPricing) detailParts.push('报价报表')
  const detailStr = detailParts.join(' + ')

  const stars = ['✨','🌟','⭐','💫','🎀','🌸','🎵','💖']
  const emoji = stars[Math.floor(Math.random() * stars.length)]

  const notify = document.createElement('div')
  notify.className = 'export-done-notify'
  notify.innerHTML = `
    <div class="edn-bg"></div>
    <div class="edn-sparkle edn-s1">✨</div>
    <div class="edn-sparkle edn-s2">🌟</div>
    <div class="edn-sparkle edn-s3">💫</div>
    <div class="edn-sparkle edn-s4">⭐</div>
    <div class="edn-card">
      <div class="edn-hero">
        <span class="edn-emoji">${emoji}</span>
        <span class="edn-title">导出完成啦~</span>
      </div>
      <div class="edn-body">
        <p class="edn-detail">成功导出 <strong>${detailStr}</strong></p>
        <p class="edn-time">⏱ 仅耗时 <strong>${elapsedStr}</strong></p>
      </div>
      <button class="edn-ok-btn" onclick="this.closest('.export-done-notify').remove()">
        知道了~
      </button>
    </div>
  `
  document.body.appendChild(notify)

  // 自动消失
  setTimeout(() => {
    notify.style.transition = 'opacity 0.4s ease, transform 0.4s ease'
    notify.style.opacity = '0'
    notify.style.transform = 'translateY(20px) scale(0.95)'
    setTimeout(() => notify.remove(), 400)
  }, 8000)
}

// ===== 三维规格辅助函数 =====
const fmtThreeDim = (row, k1, k2, k3) => {
  if (!row) return ''
  const v1 = row[k1], v2 = row[k2], v3 = row[k3]
  if (v1 == null && v2 == null && v3 == null) return ''
  return (v1 != null ? v1 : '') + '*' + (v2 != null ? v2 : '') + '*' + (v3 != null ? v3 : '')
}

// ===== 将模板 cellData 解析为 cells 数组，并按"占位符单元格数最多"的连续簇确定循环区 =====
const computeBands = (cellData) => {
  const cells = []
  for (const [key, val] of Object.entries(cellData)) {
    const m = key.match(/^R(\d+)C(\d+)$/)
    if (!m) continue
    cells.push({ r: parseInt(m[1]), c: parseInt(m[2]), v: String(val.v ?? ''), fmt: val.fmt || {} })
  }
  if (!cells.length) return null

  const globalFields = ['title', 'logo', 'currentPage', 'page', 'currentDate', 'currentMonth', 'total_pages', 'printTime', 'operatorName']

  // 收集所有包含非全局占位符的行
  const allPlaceholderRows = new Set()
  cells.forEach(c => {
    if (!/\$\{/.test(c.v)) return
    const fields = [...c.v.matchAll(/\$\{(\w+)\}/g)].map(m => m[1])
    if (fields.some(f => !globalFields.includes(f))) allPlaceholderRows.add(c.r)
  })

  const loopStart = allPlaceholderRows.size ? Math.min(...allPlaceholderRows) : 0
  const sortedCandidate = [...allPlaceholderRows].filter(r => r >= loopStart).sort((a, b) => a - b)

  // 按连续性分组（中间最多夹 1 行非占位符行）
  const clusters = []
  let curCluster = []
  for (const r of sortedCandidate) {
    if (curCluster.length && r - curCluster[curCluster.length - 1] > 1) {
      clusters.push([...curCluster])
      curCluster = []
    }
    curCluster.push(r)
  }
  if (curCluster.length) clusters.push([...curCluster])

  // 占位符单元格数最多的簇 = 数据循环区
  const clusterCellCount = (cluster) => {
    let n = 0
    for (const r of cluster) {
      n += cells.filter(c => c.r === r && /\$\{/.test(c.v)).length
    }
    return n
  }
  let largestCluster = clusters[0] || []
  let maxCount = clusterCellCount(largestCluster)
  for (const cl of clusters) {
    const cnt = clusterCellCount(cl)
    if (cnt > maxCount) { largestCluster = cl; maxCount = cnt }
  }

  const loopBaseR = largestCluster.length ? Math.min(...largestCluster) : 0
  const loopMaxR = largestCluster.length ? Math.max(...largestCluster) : 0
  const loopRows = new Set(largestCluster)

  return { cells, allPlaceholderRows, loopRows, loopBaseR, loopMaxR, globalFields }
}

// ===== 前端 ExcelJS 渲染模板 Excel（替代后端 Apache POI，与报表设计器效果一致） =====
const renderTemplateExcel = async (templateData, rows, customerData) => {
  const { mergedCells = [], colWidths = {}, rowHeights = {},
          config = {}, defaultColWidth = 120, defaultRowHeight = 48 } = templateData

  const bands = computeBands(templateData.cellData || {})
  if (!bands) return null

  const { cells, loopRows, loopBaseR, loopMaxR } = bands
  const maxC = Math.max(...cells.map(c => c.c))

  // 预构建 cells 查找 Map，O(1) 替代 O(n) 的 find()，大幅提速
  const cellsByKey = new Map()
  cells.forEach(cd => cellsByKey.set(`${cd.r}_${cd.c}`, cd))

  // === 页眉行（1 ~ loopBaseR-1） ===
  const headerRows = []
  for (let r = 1; r < loopBaseR; r++) headerRows.push(r)

  // === 页脚行（> loopMaxR，且包含 template 单元格） ===
  const trailingRows = []
  const trailingRowSet = new Set(cells.filter(c => c.r > loopMaxR).map(c => c.r))
  for (const r of trailingRowSet) trailingRows.push(r)
  trailingRows.sort((a, b) => a - b)

  // === 创建工作簿 ===
  const wb = new ExcelJS.Workbook()
  const ws = wb.addWorksheet(config.title || '报价表')

  // 列宽
  for (let c = 1; c <= maxC; c++) {
    ws.getColumn(c).width = Math.round((colWidths[c] || defaultColWidth) / 7)
  }

  // === 取值函数，优先 sample → customer → config（不参与循环的全局字段） ===
  // 图片类占位符：解析为完整 URL 用于后续嵌入 Excel 单元格
  const imagePlaceholders = new Set(['thumbnail', 'imagePath', 'image_path', 'image', 'img', 'photo', 'pic', 'picture', 'imgUrl', 'photoUrl'])
  const isImageCell = (cell) => {
    const placeholders = [...String(cell.v).matchAll(/\$\{(\w+)\}/g)].map(m => m[1])
    return placeholders.some(f => imagePlaceholders.has(f))
  }

  const resolveValue = (val, row) => {
    // 快速路径：无占位符的纯文本直接返回
    if (typeof val !== 'string' || !val.includes('${')) return String(val)
    return String(val).replace(/\$\{(\w+)\}/g, (_, f) => {
      if (f === 'logo') return ''
      if (f === 'title') return config.title || ''
      // 图片类字段：解析为完整 URL（用于后续嵌入）
      if (imagePlaceholders.has(f)) {
        const tn = row[f]
        if (tn && typeof tn === 'string' && !/^https?:\/\//i.test(tn)) return `${window.location.origin}/thumbnails/${tn}`
        return tn || ''
      }
      if (row[f] != null && row[f] !== '') return String(row[f])
      if (customerData && customerData[f] != null) return String(customerData[f])
      return ''
    })
  }

  // === 应用单元格样式 ===
  const applyStyle = (cell, fmt) => {
    if (!fmt) return
    if (fmt.fontSize) { cell.font = cell.font || {}; cell.font.size = fmt.fontSize }
    if (fmt.bold) { cell.font = cell.font || {}; cell.font.bold = true }
    if (fmt.italic) { cell.font = cell.font || {}; cell.font.italic = true }
    if (fmt.color) { cell.font = cell.font || {}; cell.font.color = { argb: 'FF' + fmt.color.replace('#', '') } }
    if (fmt.fontFamily) { cell.font = cell.font || {}; cell.font.name = fmt.fontFamily.split(',')[0].trim() }
    if (fmt.align) { cell.alignment = cell.alignment || {}; cell.alignment.horizontal = fmt.align }
    if (fmt.verticalAlign) { cell.alignment = cell.alignment || {}; cell.alignment.vertical = fmt.verticalAlign }
    if (fmt.wordWrap) { cell.alignment = cell.alignment || {}; cell.alignment.wrapText = true }
    if (fmt.bgColor) {
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF' + fmt.bgColor.replace('#', '') } }
    }
    if (fmt.border && fmt.border !== 'none') {
      const bc = (fmt.borderColor || '#d4d6da').replace('#', '')
      const xb = { style: 'thin', color: { argb: 'FF' + bc } }
      cell.border = fmt.border === 'all' ? { top: xb, bottom: xb, left: xb, right: xb }
        : fmt.border === 'outer' ? { top: xb, bottom: xb, left: xb, right: xb }
        : { top: fmt.border === 'top' ? xb : {}, bottom: fmt.border === 'bottom' ? xb : {}, left: fmt.border === 'left' ? xb : {}, right: fmt.border === 'right' ? xb : {} }
    }
  }

  // === 行数据增强（添加计算字段 cartonSpec 等，与后端保持一致） ===
  const enrichRow = (row) => {
    if (!row) return {}
    return {
      ...row,
      cartonSpec: fmtThreeDim(row, 'cartonLength', 'cartonWidth', 'cartonHeight'),
      productSpec: fmtThreeDim(row, 'sampleLength', 'sampleWidth', 'sampleHeight'),
      packageSpec: fmtThreeDim(row, 'packageLength', 'packageWidth', 'packageHeight'),
      innerCartonSpec: fmtThreeDim(row, 'innerCartonLength', 'innerCartonWidth', 'innerCartonHeight'),
      fullSpec: (() => {
        const spec = fmtThreeDim(row, 'cartonLength', 'cartonWidth', 'cartonHeight')
        const coding = row.codingSpec || ''
        return spec + (coding ? ' ' + coding : '')
      })()
    }
  }

  // === 页眉数据上下文：第一条数据 + 客户数据 ===
  const hdrData = rows.length ? enrichRow(rows[0]) : {}
  // === 页脚数据上下文 ===
  const footerData = {
    ...hdrData,
    currentPage: 1,
    page: 1,
    total_pages: 1,
    printTime: new Date().toLocaleString('zh-CN'),
    title: config.title || ''
  }

  // === 1. 写入页眉行 ===
  const pendingImages = [] // 收集需要嵌入的图片：{ r, c, url }
  for (const r of headerRows) {
    for (let c = 1; c <= maxC; c++) {
      const cd = cellsByKey.get(`${r}_${c}`)
      const wsCell = ws.getRow(r).getCell(c)
      if (cd) {
        // 图片单元格：暂存位置和 URL，稍后嵌入
        if (isImageCell(cd)) {
          const imgUrl = resolveValue(cd.v, hdrData)
          const fullUrl = hdrData.firstImageHash ? `${window.location.origin}/images/view/hash/${hdrData.firstImageHash}` : imgUrl
          if (imgUrl) pendingImages.push({ r, c: c - 1, url: imgUrl, fullUrl })
        } else {
          wsCell.value = resolveValue(cd.v, hdrData)
        }
        applyStyle(wsCell, cd.fmt)
      }
    }
    ws.getRow(r).height = rowHeights[r] || defaultRowHeight
  }

  // === 2. 写入数据循环行 ===
  let totalLoopRows = 0
  if (rows.length && loopRows.size) {
    const blockRows = loopMaxR - loopBaseR + 1
    const headerRowCount = headerRows.length ? Math.max(...headerRows) : 0

    for (let di = 0; di < rows.length; di++) {
      const rowData = enrichRow(rows[di])
      const blockStart = headerRowCount + di * blockRows + 1

      for (let r0 = 0; r0 < blockRows; r0++) {
        const absR = blockStart + r0
        const tmplR = loopBaseR + r0
        for (let c = 1; c <= maxC; c++) {
          const cd = cellsByKey.get(`${tmplR}_${c}`)
          const wsCell = ws.getRow(absR).getCell(c)
          if (cd) {
            // 图片单元格：暂存位置和 URL，稍后嵌入
            if (isImageCell(cd)) {
              const imgUrl = resolveValue(cd.v, rowData)
              const fullUrl = rowData.firstImageHash ? `${window.location.origin}/images/view/hash/${rowData.firstImageHash}` : imgUrl
              if (imgUrl) pendingImages.push({ r: absR, c: c - 1, url: imgUrl, fullUrl })
            } else {
              wsCell.value = resolveValue(cd.v, rowData)
            }
            applyStyle(wsCell, cd.fmt)
          }
        }
        ws.getRow(absR).height = rowHeights[tmplR] || defaultRowHeight
      }

      // 当前数据块内的合并单元格
      for (const m of mergedCells) {
        if (!m.sR || m.sR < loopBaseR) continue
        const absSR = blockStart + m.sR - loopBaseR
        const absER = blockStart + m.eR - loopBaseR
        try { ws.mergeCells(absSR, m.sC, absER, m.eC) } catch (_) {}
      }
    }
    totalLoopRows = rows.length * blockRows
  }

  // === 3. 写入页脚行 ===
  if (trailingRows.length) {
    const headerMaxR = headerRows.length ? Math.max(...headerRows) : 0
    const footerStart = headerMaxR + totalLoopRows + 1
    const trMinR = trailingRows[0]

    for (const tr of trailingRows) {
      const absR = footerStart + tr - trMinR
      for (let c = 1; c <= maxC; c++) {
        const cd = cellsByKey.get(`${tr}_${c}`)
        const wsCell = ws.getRow(absR).getCell(c)
        if (cd) {
          // 图片单元格：暂存位置和 URL，稍后嵌入
          if (isImageCell(cd)) {
            const imgUrl = resolveValue(cd.v, footerData)
            const fullUrl = footerData.firstImageHash ? `${window.location.origin}/images/view/hash/${footerData.firstImageHash}` : imgUrl
            if (imgUrl) pendingImages.push({ r: absR, c: c - 1, url: imgUrl, fullUrl })
          } else {
            wsCell.value = resolveValue(cd.v, footerData)
          }
          applyStyle(wsCell, cd.fmt)
        }
      }
      ws.getRow(absR).height = rowHeights[tr] || defaultRowHeight
    }

    // 页脚区合并单元格
    for (const m of mergedCells) {
      if (!m.sR || m.sR < trMinR) continue
      const absSR = footerStart + m.sR - trMinR
      const absER = footerStart + m.eR - trMinR
      try { ws.mergeCells(absSR, m.sC, absER, m.eC) } catch (_) {}
    }
  }

  // === 页眉区合并单元格 ===
  for (const m of mergedCells) {
    if (!m.sR || m.sR >= loopBaseR) continue
    try { ws.mergeCells(m.sR, m.sC, m.eR, m.eC) } catch (_) {}
  }

  // === 写入 Logo 图片（只渲染一次，按合并区域等比缩放） ===
  const logoCell = cells.find(c => /\$\{logo\}/.test(String(c.v)))
  if (logoCell && config.logoImage) {
    try {
      const b64 = config.logoImage.replace(/^data:image\/\w+;base64,/, '')
      const binaryStr = atob(b64)
      const bytes = new Uint8Array(binaryStr.length)
      for (let i = 0; i < binaryStr.length; i++) bytes[i] = binaryStr.charCodeAt(i)
      // 获取原图尺寸，保持等比缩放
      const logoMerge = mergedCells.find(m => m.sR <= logoCell.r && m.eR >= logoCell.r && m.sC <= logoCell.c && m.eC >= logoCell.c)
      let areaW = 120, areaH = 60
      if (logoMerge) {
        areaW = 0; for (let cc = logoMerge.sC; cc <= logoMerge.eC; cc++) areaW += (colWidths[cc] || defaultColWidth)
        areaH = 0; for (let rr = logoMerge.sR; rr <= logoMerge.eR; rr++) areaH += (rowHeights[rr] || defaultRowHeight)
      }
      // 获取原图宽高比
      const getImgDims = () => new Promise((resolve) => {
        const img = new Image()
        img.onload = () => resolve({ w: img.naturalWidth, h: img.naturalHeight })
        img.onerror = () => resolve(null)
        img.src = config.logoImage
      })
      const dims = await getImgDims()
      let extW = areaW, extH = areaH
      if (dims && dims.w && dims.h) {
        const ratio = dims.w / dims.h
        if (areaW / areaH > ratio) { extW = areaH * ratio; extH = areaH }
        else { extW = areaW; extH = areaW / ratio }
      }
      const imgId = wb.addImage({ buffer: bytes, extension: 'png' })
      ws.addImage(imgId, {
        tl: { col: (logoMerge ? logoMerge.sC : logoCell.c) - 1, row: (logoMerge ? logoMerge.sR : logoCell.r) - 1 },
        ext: { width: Math.round(extW), height: Math.round(extH) }
      })
    } catch (e) {
      console.error('渲染Logo失败:', e)
    }
  } else {
    if (!logoCell) console.warn('[renderTemplateExcel] 模板中未找到 ${logo} 占位符，跳过Logo渲染')
    else if (!config.logoImage) console.warn('[renderTemplateExcel] config.logoImage 为空，请在报表设计器中上传Logo')
  }

  // === 嵌入产品图片（${thumbnail} 等占位符） ===
  if (pendingImages.length > 0) {
    // 去重：相同 URL 只下载一次
    const uniqueUrls = [...new Set(pendingImages.map(p => p.url))]
    const urlToBuffer = new Map()

    // 分批并发下载，避免浏览器连接数限制
    const batchSize = 16
    for (let bi = 0; bi < uniqueUrls.length; bi += batchSize) {
      const batch = uniqueUrls.slice(bi, bi + batchSize)
      const results = await Promise.allSettled(
        batch.map(async (url) => {
          try {
            const resp = await fetch(url)
            if (!resp.ok) return null
            const blob = await resp.blob()
            const arrayBuffer = await blob.arrayBuffer()
            return { url, buffer: new Uint8Array(arrayBuffer), ext: blob.type === 'image/png' ? 'png' : 'jpeg' }
          } catch (e) {
            console.warn(`[renderTemplateExcel] 下载图片失败: ${url}`, e)
            return null
          }
        })
      )
      for (const r of results) {
        if (r.status === 'fulfilled' && r.value) {
          urlToBuffer.set(r.value.url, r.value)
        }
      }
    }

    // 逐张嵌入 Excel 单元格 + 超链接指向原图
    for (const p of pendingImages) {
      const imgData = urlToBuffer.get(p.url)
      if (!imgData) continue
      try {
        const imgId = wb.addImage({ buffer: imgData.buffer, extension: imgData.ext })
        // 嵌入缩略图
        ws.addImage(imgId, {
          tl: { col: p.c, row: p.r - 1 },
          ext: { width: 100, height: 75 }
        })
        // 单元格超链接：点击可打开原图
        ws.getCell(p.r, p.c + 1).value = {
          text: '',
          hyperlink: p.fullUrl || p.url,
          tooltip: '点击查看原图'
        }
      } catch (e) {
        console.warn(`[renderTemplateExcel] 嵌入图片失败: R${p.r}C${p.c}`, e)
      }
    }
  }

  // === 生成 blob ===
  const buffer = await wb.xlsx.writeBuffer()
  return new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
}

const loadPricingTemplates = async () => {
  try {
    const res = await api('/report-templates/all')
    const data = (res && (res.data || res)) || []
    pricingTemplates.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('加载报价模板失败:', e)
    pricingTemplates.value = []
  }
}

const startExport = async () => {
  // 直接用 gridRef.getCheckboxRecords() 获取真实选中行，
  // 避免虚拟滚动下 checkedRows（通过 checkbox-all 事件赋值）丢失不可见行数据
  const gridRows = gridRef.value?.getCheckboxRecords()
  const rows = (gridRows && gridRows.length > 0) ? gridRows : checkedRows.value
  if (rows.length === 0) return
  if (!exportIncludeImages.value && !exportIncludePricing.value) return
  const startTime = Math.floor(Date.now() / 1000)
  const taskId = Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
  currentExportTaskId = taskId
  const abortController = new AbortController()
  const signal = abortController.signal
  exportTasks.value.push({ id: taskId, abortController, progress: 0, startTime })
  const taskIndex = () => exportTasks.value.findIndex(t => t.id === taskId)
  const cleanupTask = () => {
    exportTasks.value = exportTasks.value.filter(t => t.id !== taskId)
  }
  exportStatus.value = 'loading'
  const setProgress = (val) => {
    // 只有当前弹窗关联的任务才更新模态框进度条
    if (taskId === currentExportTaskId) {
      exportProgress.value = val
    }
    const idx = taskIndex()
    if (idx >= 0) exportTasks.value[idx].progress = val
  }
  exportProgress.value = 0
  exportPricingProgress.value = 0
  exportImageProgress.value = 0
  exportCurrent.value = 0
  exportTotal.value = 0

  try {
    const ids = rows.map(r => r.id).filter(id => id != null)

    // === 图片 ZIP 异步导出（后端打包，避免浏览器内存爆炸） ===
    let imageTaskId = null
    if (exportIncludeImages.value) {
      exportStatusText.value = '正在启动图片导出...'
      try {
        const startResp = await api(`/client-samples/${encodeURIComponent(codeName.value)}/image-export-async`, {
          method: 'POST',
          body: JSON.stringify({
            ids,
            namingMode: exportNamingMode.value,
            folderName: exportFolderName.value
          })
        })
        imageTaskId = (startResp?.data?.taskId || startResp?.taskId)
      } catch (e) {
        console.error('启动图片导出失败:', e)
      }
      if (!imageTaskId && !exportIncludePricing.value) {
        exportStatus.value = 'error'
        exportStatusText.value = '图片导出启动失败'
        appAlert('图片导出启动失败', '错误', 'error')
        return
      }
    }

    // === 报价 Excel 异步生成（避免 nginx 超时） ===
    const apiBase = window.electronAPI ? 'http://localhost:8080' : ''
    let pricingPromise = null
    if (exportIncludePricing.value && exportPricingTemplateId.value) {
      pricingPromise = (async () => {

        // 启动异步导出任务
        const startResp = await api(`/client-samples/${encodeURIComponent(codeName.value)}/price-export-async`, {
          method: 'POST',
          body: JSON.stringify({ ids, templateId: exportPricingTemplateId.value })
        })
        if (!startResp || startResp.code !== 200) {
          const msg = startResp?.message || '启动导出任务失败'
          appAlert('报价导出失败: ' + msg, '错误', 'error')
          return null
        }
        const taskId = startResp.data.taskId

        // 轮询任务状态，拿到真实进度
        while (true) {
          await new Promise(r => { setTimeout(r, 1500) })
          if (signal.aborted) return null

          const statusResp = await api(`/client-samples/price-export-async/${taskId}/status`)
          if (!statusResp || statusResp.code !== 200) {
            const msg = statusResp?.message || '查询任务状态失败'
            appAlert('报价导出失败: ' + msg, '错误', 'error')
            return null
          }
          const task = statusResp.data
          exportPricingProgress.value = task.progress
          // 浮动小窗/单报价模式才更新主进度条，双模式时主进度条看图片端
          if (!exportIncludeImages.value) setProgress(task.progress)
          exportStatusText.value = task.progressMessage

          // 90%~100% 阶段显示已等待秒数，避免看起来卡住
          if (task.status === 'PROCESSING' && task.progress >= 90 && task.progress < 100) {
            const baseMsg = task.progressMessage.replace(/\.+$/, '')
            const startTime = Date.now()
            const animTimer = setInterval(() => {
              if (exportStatus.value !== 'loading') { clearInterval(animTimer); return }
              if (exportStatusText.value && !exportStatusText.value.startsWith(baseMsg)) { clearInterval(animTimer); return }
              const elapsed = Math.floor((Date.now() - startTime) / 1000)
              exportStatusText.value = baseMsg + '  已等待 ' + elapsed + ' 秒'
            }, 1000)
            const stopWatch = setInterval(() => {
              if (exportProgress.value >= 100 || exportStatus.value !== 'loading') {
                clearInterval(animTimer)
                clearInterval(stopWatch)
              }
            }, 300)
          }

          if (task.status === 'DONE') {
            const downloadResp = await fetch(`${apiBase}/client-samples/price-export-async/${taskId}/download`, {
              headers: authHeader(),
              signal
            })
            if (!downloadResp.ok) {
              appAlert('报价导出失败: 下载文件出错 HTTP ' + downloadResp.status, '错误', 'error')
              return null
            }
            const blob = await downloadResp.blob()
            const name = (exportPricingFileName.value.trim() || '报价明细') + '.xlsx'
            const url = URL.createObjectURL(blob)
            return { url, name }
          }
          if (task.status === 'ERROR') {
            appAlert('报价导出失败: ' + (task.error || '未知错误'), '错误', 'error')
            return null
          }
        }
      })().catch(e => {
        if (e.name !== 'AbortError') {
          console.error('生成报价报表失败:', e)
          appAlert('报价导出失败: ' + (e.message || '未知错误'), '错误', 'error')
        }
        return null
      })
    }

    // === 如果只有报价，不需要图片/zip流程 ===
    if (!exportIncludeImages.value) {
      exportTotal.value = 1
      exportCurrent.value = 0
      setProgress(5)
      exportStatusText.value = '正在启动报价导出...'
      if (pricingPromise) {
        const pricingResult = await pricingPromise
        if (pricingResult) {
          setProgress(100)
          exportStatusText.value = '正在下载报价文件...'
          const a = document.createElement('a')
          a.href = pricingResult.url ; a.download = pricingResult.name
          document.body.appendChild(a) ; a.click() ; document.body.removeChild(a)
          URL.revokeObjectURL(pricingResult.url)
        }
      }
      if (exportStatus.value !== 'error') {
        exportCurrent.value = 1
        exportTotal.value = 1
        setProgress(100)
        exportStatus.value = 'done'
        exportStatusText.value = '导出完成！'
      }
      exportFloatingVisible.value = false
      const sec = Math.floor(Date.now() / 1000) - startTime
      cleanupTask()
      showExportDoneNotify(sec, 0, true)
      // 防止 vxe-table 虚拟滚动导致 checkedRows 被清空
      syncCheckedRowsFromList()
      setTimeout(() => { exportModalOpen.value = false }, 2500)
      return
    }

    // === 阶段2：图片 ZIP 异步生成（后端打包） ===
    if (exportIncludeImages.value && imageTaskId) {
      exportCurrent.value = 0
      exportTotal.value = 1
      setProgress(5)
      exportStatusText.value = '正在生成图片压缩包...'

      let imageResult = null
      while (imageResult === null) {
        if (!exportTasks.value.some(t => t.id === taskId)) return // 已取消
        await new Promise(r => { setTimeout(r, 1500) })
        const statusResp = await api(`/client-samples/image-export-async/${imageTaskId}/status`, { signal })
        const task = statusResp?.data || statusResp

        if (task.status === 'ERROR') {
          appAlert('图片导出失败: ' + (task.error || '未知错误'), '错误', 'error')
          return
        }
        if (task.progress !== undefined) {
          exportImageProgress.value = task.progress
          setProgress(task.progress) // 兼容单图片模式
        }
        if (task.progressMessage) {
          exportStatusText.value = task.progressMessage
        }

        if (task.status === 'DONE') {
          const downloadResp = await fetch(`${apiBase}/client-samples/image-export-async/${imageTaskId}/download`, {
            headers: authHeader(),
            signal
          })
          if (!downloadResp.ok) {
            appAlert('图片导出失败: 下载文件出错 HTTP ' + downloadResp.status, '错误', 'error')
            return
          }
          const blob = await downloadResp.blob()
          const zipName = exportFolderName.value.trim() || `择样图片_${codeName.value}`
          const url = URL.createObjectURL(blob)
          imageResult = { url, name: zipName + '.zip' }
        }
      }

      // 触发下载
      exportStatusText.value = '正在下载图片压缩包...'
      setProgress(98)
      const a = document.createElement('a')
      a.href = imageResult.url; a.download = imageResult.name
      document.body.appendChild(a); a.click(); document.body.removeChild(a)
      URL.revokeObjectURL(imageResult.url)
    }

    // 等待异步报价导出完成
    if (pricingPromise) {
      const pricingResult = await pricingPromise
      if (pricingResult) {
        exportStatusText.value = '正在下载报价文件...'
        const a = document.createElement('a')
        a.href = pricingResult.url ; a.download = pricingResult.name
        document.body.appendChild(a) ; a.click() ; document.body.removeChild(a)
        URL.revokeObjectURL(pricingResult.url)
      }
    }

    setProgress(100)
    exportStatus.value = 'done'
    exportStatusText.value = '导出完成！'
    exportCurrent.value = exportIncludeImages.value ? 1 : 0
    exportTotal.value = exportIncludeImages.value ? 1 : 0
    exportFloatingVisible.value = false

    cleanupTask()
    const sec = Math.floor(Date.now() / 1000) - startTime
    showExportDoneNotify(sec, ids.length, exportIncludePricing.value)
    // 防止 vxe-table 虚拟滚动导致 checkedRows 被清空
    syncCheckedRowsFromList()

    setTimeout(() => {
      exportModalOpen.value = false
    }, 2500)

  } catch (e) {
    if (e.name === 'AbortError') {
      // 用户主动取消，不弹错误提示
      cleanupTask()
      return
    }
    console.error('导出失败:', e)
    cleanupTask()
    exportStatus.value = 'error'
    exportStatusText.value = '导出失败'
    exportFloatingVisible.value = false
    appAlert('导出失败，请重试', '错误', 'danger')
  }
}

// 定位框
const locateInput = ref('')
const locateCursor = ref(-1)

const onLocate = () => {
  const keyword = locateInput.value.trim()
  if (!keyword) return
  const lower = keyword.toLowerCase()
  const data = filteredList.value
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
  currentPage.value = Math.floor(idx / pageSize.value) + 1
  nextTick(() => {
    gridRef.value?.setCurrentRow(data[idx])
    gridRef.value?.scrollToRow(data[idx])
  })
}

// 输入变化时重置游标
watch(locateInput, () => { locateCursor.value = -1 })

// 其他功能下拉
const menuOpen = ref(false)

const toggleMenu = () => {
  menuOpen.value = !menuOpen.value
}

const closeMenu = () => {
  menuOpen.value = false
}

// ===== 查看删除记录 =====
const deletedItemLog = ref([])
const deletedRecordsModalOpen = ref(false)
const deletedPageSize = ref(50)
const deletedCurrentPage = ref(1)

const deletedLogColumns = [
  { type: 'checkbox', title: '', width: 50, fixed: 'left' },
  { type: 'seq', title: '序号', width: 60 },
  { field: 'codeName', title: '本次代号', width: 140, sortable: true },
  { field: 'sampleCode', title: '公司编号', width: 160, sortable: true },
  { field: 'sampleName', title: '样品名称', minWidth: 300, sortable: true },
  { field: 'factoryCode', title: '出厂货号', width: 160, sortable: true },
  { field: 'packagingCn', title: '中文包装', width: 200, sortable: true },
  { field: 'boothNo', title: '摊位号', width: 160, sortable: true },
  { field: 'creator', title: '操作人', width: 140, sortable: true },
  { field: 'deletedAt', title: '删除时间', width: 180, sortable: true }
]

const deletedCheckedRows = ref([])

const onDeletedCheckChange = ({ records }) => {
  deletedCheckedRows.value = records
}

const onDeletedCheckAll = ({ records }) => {
  deletedCheckedRows.value = records
}

const deletedPagedData = computed(() => {
  const start = (deletedCurrentPage.value - 1) * deletedPageSize.value
  return deletedItemLog.value.slice(start, start + deletedPageSize.value)
})

const deletedTotalPages = computed(() => Math.max(1, Math.ceil(deletedItemLog.value.length / deletedPageSize.value)))

const onViewDeletedRecords = async () => {
  menuOpen.value = false
  deletedCurrentPage.value = 1
  try {
    const res = await api(`/client-samples/${codeName.value}/items/deleted`, { method: 'GET' })
    if (res.code === 200) {
      deletedItemLog.value = (res.data || []).map(item => ({
        ...item,
        itemId: item.itemId,
        codeName: item.codeName,
        creator: item.creator || '',
        deletedAt: item.deletedAt || ''
      }))
    }
  } catch (e) {
    console.error('加载删除记录失败:', e)
  }
  deletedRecordsModalOpen.value = true
}

watch(deletedPageSize, () => {
  deletedCurrentPage.value = 1
})

const clearDeletedLog = () => {
  deletedItemLog.value = []
}

const restoreDeletedItems = async () => {
  if (deletedCheckedRows.value.length === 0) return
  const ok = await appConfirm(`确定恢复选中的 ${deletedCheckedRows.value.length} 条记录吗？`, '确认恢复')
  if (!ok) return
  try {
    const items = deletedCheckedRows.value.map(r => {
      const { deletedAt, itemId, ...item } = r
      return item
    })
    await api(`/client-samples/${codeName.value}/items/batch-restore`, {
      method: 'POST',
      body: JSON.stringify(items)
    })
    // 从日志中移除已恢复的记录
    const restoredIds = new Set(deletedCheckedRows.value.map(r => r.itemId))
    deletedItemLog.value = deletedItemLog.value.filter(r => !restoredIds.has(r.itemId))
    deletedCheckedRows.value = []
    deletedCurrentPage.value = 1
    // 刷新表格
    await loadData()
    appAlert(`成功恢复 ${items.length} 条记录`, '成功')
  } catch (e) {
    console.error('恢复删除记录失败:', e)
    appAlert('恢复失败: ' + (e.message || '未知错误'), '错误', 'danger')
  }
}

// ===== 按编号批量查询 =====
const codeSearchModalOpen = ref(false)
const codeSearchInputText = ref('')
const codeInputMode = ref('code')
const codeSearchKeepInput = ref(false)
const codeSearchResult = ref(null)
const codeSearchInput = computed(() => {
  if (!codeSearchActive.value) return ''
  return `${codeInputMode.value === 'code' ? '公司编号' : '出厂货号'}: ${codeSearchCodeOrder.value.length}个编号`
})
const codeSearchDone = ref(false)
const codeSearchActive = ref(false)
const notFoundCodes = ref([])
// 存储解析后的编号集合和顺序
const codeSearchCodeSet = ref(null)
const codeSearchCodeOrder = ref([])

// 拖拽
const codeSearchW = ref(Math.round(window.innerWidth * 0.65))
const codeSearchH = ref(Math.round(window.innerHeight * 0.55))
const codeSearchPos = reactive({ x: 0, y: 0 })
const codeSearchDragStart = ref(null)

const initCodeSearchPos = () => {
  codeSearchW.value = Math.round(window.innerWidth * 0.65)
  codeSearchH.value = Math.round(window.innerHeight * 0.55)
  codeSearchPos.x = Math.max(0, Math.round((window.innerWidth - codeSearchW.value) / 2))
  codeSearchPos.y = Math.max(0, Math.round((window.innerHeight - codeSearchH.value) / 2))
}

const startCodeSearchDrag = (e) => {
  if (e.target.tagName === 'BUTTON') return
  codeSearchDragStart.value = { x: e.clientX - codeSearchPos.x, y: e.clientY - codeSearchPos.y }
  const onMove = (ev) => {
    codeSearchPos.x = Math.max(0, Math.min(ev.clientX - codeSearchDragStart.value.x, window.innerWidth - codeSearchW.value))
    codeSearchPos.y = Math.max(0, Math.min(ev.clientY - codeSearchDragStart.value.y, window.innerHeight - codeSearchH.value))
  }
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

const onCodeSearch = () => {
  menuOpen.value = false
  codeSearchInputText.value = ''
  codeInputMode.value = 'code'
  codeSearchResult.value = null
  codeSearchDone.value = false
  notFoundCodes.value = []
  initCodeSearchPos()
  codeSearchModalOpen.value = true
}

const doCodeSearch = () => {
  const raw = codeSearchInputText.value.trim()
  if (!raw) {
    codeSearchActive.value = false
    codeSearchResult.value = null
    codeSearchDone.value = false
    notFoundCodes.value = []
    return
  }
  const codes = raw.split(/[\n\r,，]+/).map(s => s.trim()).filter(Boolean)
  if (codes.length === 0) {
    codeSearchActive.value = false
    codeSearchDone.value = false
    return
  }

  codeSearchDone.value = false
  notFoundCodes.value = []

  const field = codeInputMode.value === 'code' ? 'sampleCode' : 'factoryCode'
  const codeSet = new Set(codes)
  codeSearchCodeSet.value = codeSet
  codeSearchCodeOrder.value = codes
  codeSearchActive.value = true
  currentPage.value = 1

  // 匹配并勾选
  const matched = list.value.filter(item => {
    return codeSet.has(String(item[field] || '').trim())
  })

  // 找出未查到的编号
  const foundSet = new Set(matched.map(r => String(r[field] || '').trim()))
  notFoundCodes.value = codes.filter(c => !foundSet.has(c))
  codeSearchResult.value = matched.length
  codeSearchDone.value = true

  // 勾选匹配的记录
  checkedRows.value = [...matched]
  nextTick(() => {
    gridRef.value?.setCheckboxRow(matched, true)
  })

  // 不保留则清除输入
  if (!codeSearchKeepInput.value) {
    codeSearchInputText.value = ''
  }
}

const clearCodeSearch = () => {
  codeSearchActive.value = false
  codeSearchInputText.value = ''
  codeSearchCodeSet.value = null
  codeSearchCodeOrder.value = []
  codeSearchResult.value = null
  codeSearchDone.value = false
  notFoundCodes.value = []
  // 清除勾选
  checkedRows.value = []
  nextTick(() => {
    gridRef.value?.setAllCheckboxRow(false)
  })
}

// ===== 恢复误添加库存数据模态框 =====
const revertModalOpen = ref(false)
const revertLoading = ref(false)
const revertDeleting = ref(false)
const revertList = ref([])
const revertCheckedCount = ref(0)
const revertGridRef = ref(null)

const revertColumns = [
  { type: 'checkbox', width: 50, fixed: 'left', align: 'center' },
  { field: '_type', title: '类型', width: 80, align: 'center', sortable: true, slots: { default: '_type' } },
  { field: 'companyCode', title: '公司编号', width: 200, align: 'center', sortable: true },
  { field: 'sampleName', title: '样品名称', width: 200, align: 'center', sortable: true },
  { field: 'boothNumber', title: '摊位号', width: 200, align: 'center', sortable: true },
  { field: 'factoryNo', title: '出厂编号', width: 200, align: 'center', sortable: true },
  { field: 'manufacturerName', title: '厂商名称', width: 200, align: 'center', sortable: true },
  { field: 'creator', title: '操作人', width: 200, align: 'center', sortable: true, slots: { default: 'revert_creator' } },
  { field: '_time', title: '时间', width: 200, align: 'center', sortable: true }
]

const onRevertCheckboxChange = ({ records }) => {
  revertCheckedCount.value = records.length
}

const onRevertSelectAll = () => {
  const grid = revertGridRef.value
  if (grid) {
    grid.setAllCheckboxRow(true)
  }
}

const onRevertClearAll = () => {
  const grid = revertGridRef.value
  if (grid) {
    grid.setAllCheckboxRow(false)
    revertCheckedCount.value = 0
  }
}

const revertTypeFilter = ref(null)
const filteredRevertList = computed(() => {
  if (!revertTypeFilter.value) return revertList.value
  return revertList.value.filter(r => r._type === revertTypeFilter.value)
})

watch(revertTypeFilter, () => {
  revertCheckedCount.value = 0
  nextTick(() => {
    revertGridRef.value?.setAllCheckboxRow(false)
  })
})

const openRevertModal = async () => {
  revertModalOpen.value = true
  revertLoading.value = true
  revertList.value = []
  revertCheckedCount.value = 0
  revertTypeFilter.value = null
  try {
    const [invRes, obRes] = await Promise.all([
      api(`/inventory/${codeName.value}/items`),
      api(`/outbound/${codeName.value}/items`)
    ])
    const inventory = (invRes.code === 200 && invRes.data) ? invRes.data : []
    const outbound = (obRes.code === 200 && obRes.data) ? obRes.data : []
    const merged = []
    inventory.forEach(item => {
      merged.push({ ...item, _key: `inv_${item.id}`, _type: '入库', _time: item.stockInTime || item.createDate || '' })
    })
    outbound.forEach(item => {
      merged.push({ ...item, _key: `ob_${item.id}`, _type: '出库', _time: item.stockOutTime || item.createDate || '' })
    })
    revertList.value = merged
  } catch (e) {
    console.error('加载入出库记录失败:', e)
    appAlert('加载失败，请检查网络', '错误', 'danger')
  } finally {
    revertLoading.value = false
  }
}

const onRevertConfirm = async () => {
  const records = revertGridRef.value?.getCheckboxRecords() || []
  if (records.length === 0) return
  revertDeleting.value = true
  try {
    const invIds = records.filter(r => r._type === '入库').map(r => r.id)
    const obIds = records.filter(r => r._type === '出库').map(r => r.id)
    const tasks = []
    if (invIds.length > 0) {
      tasks.push(api(`/inventory/${codeName.value}/items`, {
        method: 'DELETE',
        body: JSON.stringify(invIds)
      }))
    }
    if (obIds.length > 0) {
      tasks.push(api(`/outbound/${codeName.value}/items`, {
        method: 'DELETE',
        body: JSON.stringify(obIds)
      }))
    }
    await Promise.all(tasks)
    appAlert(`已撤销 ${invIds.length} 条入库、${obIds.length} 条出库记录`, '提示', 'success')
    revertModalOpen.value = false
    loadData()
  } catch (e) {
    console.error('撤销失败:', e)
    appAlert('撤销失败，请检查网络连接', '错误', 'danger')
  } finally {
    revertDeleting.value = false
  }
}

const onRevertSubmissions = () => {
  menuOpen.value = false
  openRevertModal()
}

// 筛选重复：按 样品名称+出厂货号+出厂价 分组，每组只勾选第一条，其余不勾选
const duplicateFilter = ref(false)

const onFilterDuplicate = () => {
  menuOpen.value = false
  duplicateFilter.value = !duplicateFilter.value
  currentPage.value = 1

  if (duplicateFilter.value) {
    const seen = new Map()
    const toCheck = []
    const toUncheck = []

    list.value.forEach(item => {
      const key = `${item.sampleName ?? ''}|${item.factoryCode ?? ''}|${item.factoryPrice ?? ''}`
      if (seen.has(key)) {
        if (item.checked) toUncheck.push(item)
      } else {
        seen.set(key, item)
        if (!item.checked) toCheck.push(item)
      }
    })

    // 更新 checked 状态并同步到服务端
    syncCheckedToServer(toUncheck, false)
    syncCheckedToServer(toCheck, true)

    // 更新本地 checkedRows
    checkedRows.value = list.value.filter(r => r.checked)

    // 更新当前页复选框 UI
    nextTick(() => {
      gridRef.value?.setAllCheckboxRow(false)
      nextTick(() => {
        const checked = tableData.value.filter(r => r.checked)
        if (checked.length) gridRef.value?.setCheckboxRow(checked, true)
      })
    })
  }
}

// 选择楼层
const floorInput = ref('')

// 图片/视频筛选
const hasImageFilter = ref(false)
const noImageFilter = ref(false)
const hasVideoFilter = ref(false)
const noVideoFilter = ref(false)

const toggleHasImage = () => {
  hasImageFilter.value = !hasImageFilter.value
  if (hasImageFilter.value) noImageFilter.value = false
  currentPage.value = 1
}
const toggleNoImage = () => {
  noImageFilter.value = !noImageFilter.value
  if (noImageFilter.value) hasImageFilter.value = false
  currentPage.value = 1
}
const toggleHasVideo = () => {
  hasVideoFilter.value = !hasVideoFilter.value
  if (hasVideoFilter.value) noVideoFilter.value = false
  currentPage.value = 1
}
const toggleNoVideo = () => {
  noVideoFilter.value = !noVideoFilter.value
  if (noVideoFilter.value) hasVideoFilter.value = false
  currentPage.value = 1
}

const onFloorFilter = () => {
  currentPage.value = 1
}

const totalPages = computed(() => Math.max(1, Math.ceil(filteredList.value.length / pageSize.value)))

const filteredList = computed(() => {
  let result = list.value
  const kw = searchKeyword.value.trim()
  const fl = floorInput.value.trim()
  if (kw) {
    const lower = kw.toLowerCase()
    result = result.filter(item => {
      const fields = [
        item.sampleCode, item.factoryCode, item.boothNo, item.mobile1,
        item.sampleName, item.name, item.manufacturerCode,
        item.category, item.categoryCode, item.color, item.fax
      ]
      return fields.some(f => f != null && String(f).toLowerCase().includes(lower))
    })
  }
  // 按楼层：输入数字匹配摊位号第一位
  if (fl) {
    result = result.filter(item => item.boothNo != null && String(item.boothNo).startsWith(fl))
  }
  // 按编号批量查询
  if (codeSearchActive.value) {
    const codeSet = codeSearchCodeSet.value
    result = result.filter(item => {
      const field = codeInputMode.value === 'code' ? 'sampleCode' : 'factoryCode'
      return codeSet.has(String(item[field] || '').trim())
    })
    // 按输入顺序排序
    const order = codeSearchCodeOrder.value
    const rankMap = new Map()
    order.forEach((code, i) => rankMap.set(code, i))
    const sortField = codeInputMode.value === 'code' ? 'sampleCode' : 'factoryCode'
    result.sort((a, b) => {
      const aVal = String(a[sortField] || '').trim()
      const bVal = String(b[sortField] || '').trim()
      const aRank = rankMap.has(aVal) ? rankMap.get(aVal) : 999999
      const bRank = rankMap.has(bVal) ? rankMap.get(bVal) : 999999
      return aRank - bRank
    })
  }
  // 图片筛选
  if (hasImageFilter.value) {
    result = result.filter(item => item.firstImageHash || item.thumbnail)
  } else if (noImageFilter.value) {
    result = result.filter(item => !item.firstImageHash && !item.thumbnail)
  }
  // 视频筛选
  if (hasVideoFilter.value) {
    result = result.filter(item => item.hasVideo)
  } else if (noVideoFilter.value) {
    result = result.filter(item => !item.hasVideo)
  }
  // 排序
  const sf = sortField.value
  const so = sortOrder.value
  if (sf && so) {
    result = [...result].sort((a, b) => {
      // 复选框列：按 checked 字段排序，asc=未勾选在前，desc=已勾选在前
      if (sf === '_checked') {
        const va = a.checked ? 1 : 0
        const vb = b.checked ? 1 : 0
        return so === 'asc' ? va - vb : vb - va
      }
      let va = a[sf]
      let vb = b[sf]
      // 数字类型排序
      if (typeof va === 'number' && typeof vb === 'number') {
        return so === 'asc' ? va - vb : vb - va
      }
      // 字符串排序
      va = va != null ? String(va) : ''
      vb = vb != null ? String(vb) : ''
      return so === 'asc' ? va.localeCompare(vb) : vb.localeCompare(va)
    })
  }
  return result
})

const tableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

// ========== 列宽缩放 ==========
const colScale = computed(() => largeMode.value ? 1.6 : 1)

// 表头/单元格样式
const gridHeaderStyle = computed(() => ({
  background: '#ff0000', borderColor: '#a0bddb', color: '#ffffff',
  fontSize: largeMode.value ? '22px' : '30px', fontWeight: 600, textAlign: 'center'
}))
const gridCellStyle = ({ row, column }) => {
  if (isColumnDragging.value) return { textAlign: 'center', fontSize: largeMode.value ? '26px' : '26px' }
  if (!areaDragging.value && !areaSelectedColumn.value) {
    return { textAlign: 'center', fontSize: largeMode.value ? '26px' : '26px' }
  }
  void areaRenderTick.value
  const field = (column && (column.field || column.type)) || ''
  const fs = largeMode.value ? '26px' : '26px'
  if (areaDragging.value && field === areaDragField.value) {
    const set = areaDragRowIdSet.value
    if (set && row && set.has(row.itemId)) return { textAlign: 'center', fontSize: fs, background: '#e3f2fd', outline: '2px solid #4285f4', outlineOffset: '-2px' }
  }
  if (areaSelectedColumn.value && field === areaSelectedColumn.value) {
    const set = areaSelectedRowIdSet.value
    if (set && row && set.has(row.itemId)) return { textAlign: 'center', fontSize: fs, background: '#dceefb', outline: '2px solid #4285f4', outlineOffset: '-2px' }
  }
  return { textAlign: 'center', fontSize: fs }
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
  if (e.target.closest('.csd-area-handle')) return
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
    document.body.classList.add('csd-area-selecting')
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
  document.body.classList.remove('csd-area-selecting')
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
  const sIdx = data.findIndex(r => String(r.itemId) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.itemId) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return
  const lastIdx = Math.max(sIdx, eIdx)
  const lastId = String(data[lastIdx].itemId)
  requestAnimationFrame(() => {
    const cellEl = wrapper.querySelector(`[rowid="${lastId}"] [colid="${areaSelectedColId.value}"]`)
    if (!cellEl) return
    const td = cellEl.tagName === 'TD' ? cellEl : cellEl.closest('td')
    if (!td) return
    const h = document.createElement('div')
    h.className = 'csd-area-handle'
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
  document.body.classList.add('csd-area-selecting')
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
  document.body.classList.remove('csd-area-selecting')
  document.removeEventListener('mousemove', onExtMouseMove)
  document.removeEventListener('mouseup', onExtMouseUp)
  if (_areaRaf) { cancelAnimationFrame(_areaRaf); _areaRaf = null }
  attachAreaHandle()
}

const getAreaSelectedValues = () => {
  if (!areaSelectedColumn.value) return []
  const data = tableData.value
  const sIdx = data.findIndex(r => String(r.itemId) === String(areaSelectedStartRowId.value))
  const eIdx = data.findIndex(r => String(r.itemId) === String(areaSelectedEndRowId.value))
  if (sIdx === -1 || eIdx === -1) return []
  const min = Math.min(sIdx, eIdx)
  const max = Math.max(sIdx, eIdx)
  const field = areaSelectedColumn.value
  return data.slice(min, max + 1).map(r => ({
    itemId: r.itemId,
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

// ========== 列定义 ==========
const calc = (w) => Math.round(w * colScale.value)
const allColumns = computed(() => [
  { type: 'checkbox', title: '', width: calc(50), fixed: 'left', slots: { header: 'checkbox_header' } },
  { type: 'seq', title: '序号', width: calc(60), fixed: 'left' },
  { field: 'image', title: '图片', width: calc(160), sortable: true, slots: { default: 'image' } },
  { field: 'sampleCode', title: '公司编号', width: calc(120), sortable: true, showOverflow: true },
  { field: 'boothNo', title: '摊位号', width: calc(100), sortable: true, showOverflow: true },
  { field: 'factoryCode', title: '出厂货号', width: calc(120), sortable: true, showOverflow: true },
  { field: 'factoryPrice', title: '出厂价', width: calc(95), sortable: true, headerClassName: 'csd-header-red', className: 'csd-cell-red', showOverflow: true },
  { field: 'sampleName', title: '样品名称', width: calc(150), showOverflow: true },
  { field: 'calculatedPrice', title: '报出价1', width: calc(95), sortable: true, headerClassName: 'csd-header-red', className: 'csd-cell-red', showOverflow: true },
  { field: 'taxPrice2', title: '报出价2', width: calc(95), sortable: true, headerClassName: 'csd-header-red', className: 'csd-cell-red', showOverflow: true },
  { field: 'addDate', title: '添加日期', width: calc(160), sortable: true, showOverflow: true },
  { field: 'showroomReplenished', title: '展厅已补', width: calc(90), sortable: true, slots: { default: 'showroomReplenished' } },
  { field: 'borrowedSample', title: '借样', width: calc(70), sortable: true, slots: { default: 'borrowedSample' } },
  { field: 'sampleStatus', title: '择样状态', width: calc(100), sortable: true, showOverflow: true },
  { field: 'submitted', title: '状态', width: calc(80), sortable: true, slots: { default: 'submitted' } },
  { field: 'boxCount', title: '箱数', width: calc(70), sortable: true, showOverflow: true },
  { field: 'manufacturerCode', title: '厂商编号', width: calc(120), sortable: true, showOverflow: true },
  { field: 'updateTime', title: '修改时间', width: calc(155), sortable: true, showOverflow: true },
  { field: 'name', title: '厂商名称', width: calc(140), sortable: true, showOverflow: true },
  { field: 'cartonLength', title: '外箱长度', width: calc(85), sortable: true, showOverflow: true },
  { field: 'cartonWidth', title: '外箱宽度', width: calc(85), sortable: true, showOverflow: true },
  { field: 'cartonHeight', title: '外箱高度', width: calc(85), sortable: true, showOverflow: true },
  { field: 'cartonMaterialVolume', title: '外箱材积', width: calc(85), sortable: true, showOverflow: true },
  { field: 'innerBoxCount', title: '内盒个数', width: calc(85), sortable: true, showOverflow: true },
  { field: 'cartonCapacity', title: '外箱装量', width: calc(85), sortable: true, showOverflow: true },
  { field: 'packingUnit', title: '装箱单位', width: calc(85), showOverflow: true },
  { field: 'cartonGrossWeight', title: '外箱毛重', width: calc(90), sortable: true, showOverflow: true },
  { field: 'cartonNetWeight', title: '外箱净重', width: calc(90), sortable: true, showOverflow: true },
  { field: 'sampleLength', title: '样品长度', width: calc(85), sortable: true, showOverflow: true },
  { field: 'sampleWidth', title: '样品宽度', width: calc(85), sortable: true, showOverflow: true },
  { field: 'sampleHeight', title: '样品高度', width: calc(85), sortable: true, showOverflow: true },
  { field: 'sampleGrossWeight', title: '产品毛重', width: calc(90), showOverflow: true },
  { field: 'sampleNetWeight', title: '产品净重', width: calc(90), showOverflow: true },
  { field: 'packagingCn', title: '中文包装', width: calc(110), sortable: true, showOverflow: true },
  { field: 'categoryCode', title: '种类编号', width: calc(100), showOverflow: true },
  { field: 'category', title: '种类名称', width: calc(110), sortable: true, showOverflow: true },
  { field: 'batteryInfo', title: '电池信息', width: calc(110), showOverflow: true },
  { field: 'exchangeRate', title: '货币汇率', width: calc(85), showOverflow: true },
  { field: 'profitRate', title: '报价利润', width: calc(85), showOverflow: true },
  { field: 'certification', title: '产品认证', width: calc(100), showOverflow: true },
  { field: 'createTime', title: '产品登记日期', width: calc(160), sortable: true, showOverflow: true },
  { field: 'modifyDate', title: '产品修改日期', width: calc(160), sortable: true, showOverflow: true },
  { field: 'color', title: '颜色', width: calc(90), showOverflow: true },
  { field: 'colorEn', title: '英文颜色', width: calc(90), showOverflow: true },
  { field: 'registerTime', title: '登记时间', width: calc(160), sortable: true, showOverflow: true },
  { field: 'packagingEn', title: '英文包装', width: calc(120), showOverflow: true },
  { field: 'packageLength', title: '包装长', width: calc(75), sortable: true, showOverflow: true },
  { field: 'packageWidth', title: '包装宽', width: calc(75), sortable: true, showOverflow: true },
  { field: 'packageHeight', title: '包装高', width: calc(75), sortable: true, showOverflow: true },
  { field: 'registrant', title: '登记人', width: calc(85), sortable: true, showOverflow: true },
  { field: 'modifier', title: '修改人', width: calc(85), showOverflow: true },
  { field: 'smsSent', title: '已发短信', width: calc(85), showOverflow: true },
  { field: 'remark', title: '中文备注', width: calc(200), showOverflow: true },
  { field: 'remarkEn', title: '英文备注', width: calc(200), showOverflow: true },
  { field: 'vendorCertification', title: '厂商认证', width: calc(100), showOverflow: true }
])

// 表格列设置跨设备同步
const { fullKey: gridStorageKey, saveToBackend: saveGridPrefs, ready: prefReady } = useGridPrefSync(gridRef, 'client-sample-detail', allColumns)

// ========== 事件处理 ==========
const onCellClick = ({ row }) => {
  if (row) {
    sample.value = row
  }
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
  // 更新本地状态 + 同步到服务端
  const newRecords = gridRef.value.getCheckboxRecords()
  const newIds = new Set(newRecords.map(r => r.itemId))
  const oldIds = new Set(checkedRows.value.map(r => r.itemId))
  const added = newRecords.filter(r => !oldIds.has(r.itemId))
  const removed = checkedRows.value.filter(r => !newIds.has(r.itemId))
  checkedRows.value = newRecords
  if (added.length) syncCheckedToServer(added, true)
  if (removed.length) syncCheckedToServer(removed, false)
}

const onCheckboxAll = ({ records }) => {
  // 全选/全不选：对比新旧差异
  // 用 getCheckboxRecords() 获取全部勾选行（虚拟滚动下 records 只含可见行）
  const allChecked = gridRef.value?.getCheckboxRecords() || records
  const newIds = new Set(allChecked.map(r => r.itemId))
  const oldIds = new Set(checkedRows.value.map(r => r.itemId))
  const added = allChecked.filter(r => !oldIds.has(r.itemId))
  const removed = checkedRows.value.filter(r => !newIds.has(r.itemId))
  checkedRows.value = allChecked
  if (added.length) syncCheckedToServer(added, true)
  if (removed.length) syncCheckedToServer(removed, false)
}

const onHeaderCheckAll = (check) => {
  const data = tableData.value
  if (!data.length) return
  if (check) {
    gridRef.value?.setAllCheckboxRow(true)
    syncCheckedToServer(data, true)
  } else {
    gridRef.value?.setAllCheckboxRow(false)
    syncCheckedToServer(data, false)
  }
}

const onSelectAll = () => {
  const all = tableData.value
  if (!all.length) return
  gridRef.value?.setAllCheckboxRow(true)
  syncCheckedToServer(all, true)
}

const onInvertSelect = async () => {
  const all = tableData.value
  if (!all.length) return
  const currentChecked = gridRef.value?.getCheckboxRecords() || []
  const checkedIds = new Set(currentChecked.map(r => r.itemId))
  const toCheck = all.filter(r => !checkedIds.has(r.itemId))
  const toUncheck = all.filter(r => checkedIds.has(r.itemId))

  gridRef.value?.setAllCheckboxRow(false)
  await nextTick()
  if (toCheck.length > 0) {
    gridRef.value?.setCheckboxRow(toCheck, true)
  }
  syncCheckedToServer(toCheck, true)
  syncCheckedToServer(toUncheck, false)
}

const onClearSelect = () => {
  gridRef.value?.setAllCheckboxRow(false)
  syncCheckedToServer(tableData.value, false)
}

const onSelectBorrowed = async () => {
  const all = tableData.value
  if (!all.length) return
  const borrowed = all.filter(r => r.borrowedSample)
  gridRef.value?.setAllCheckboxRow(false)
  await nextTick()
  if (borrowed.length > 0) {
    gridRef.value?.setCheckboxRow(borrowed, true)
  }
  checkedRows.value = borrowed
  syncCheckedToServer(borrowed, true)
}

const onSelectReplenished = async () => {
  const all = tableData.value
  if (!all.length) return
  const replenished = all.filter(r => r.showroomReplenished)
  gridRef.value?.setAllCheckboxRow(false)
  await nextTick()
  if (replenished.length > 0) {
    gridRef.value?.setCheckboxRow(replenished, true)
  }
  checkedRows.value = replenished
  syncCheckedToServer(replenished, true)
}

const onSelectReplenishedAndBorrowed = async () => {
  const all = tableData.value
  if (!all.length) return
  const result = all.filter(r => r.showroomReplenished || r.borrowedSample)
  gridRef.value?.setAllCheckboxRow(false)
  await nextTick()
  if (result.length > 0) {
    gridRef.value?.setCheckboxRow(result, true)
  }
  checkedRows.value = result
  syncCheckedToServer(result, true)
}

const onSortChange = ({ field, order, column }) => {
  // 其他列排序时清除复选框排序状态
  if (!column || column.type !== 'checkbox') {
    checkboxSortOrder.value = 0
  }
  sortField.value = field || ''
  sortOrder.value = order || ''
  currentPage.value = 1
}

const onSearch = () => {
  currentPage.value = 1
}

const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
}

const onRefresh = () => {
  loadData()
}

const goPage = (page) => {
  currentPage.value = page
}

watch(pageSize, () => {
  currentPage.value = 1
})

// 大小图切换时重新应用后端列宽
watch(largeMode, async () => {
  if (gridRef.value) {
    await nextTick()
    gridRef.value.loadColumn(allColumns.value)
  }
})

// ========== 数据加载 ==========
const loadData = async () => {
  if (!codeName.value) {
    list.value = []
    totalRecords.value = 0
    return
  }
  tableLoading.value = true
  try {
    const res = await api(`/client-samples/${codeName.value}/items`)
    if (res.code === 200 && res.data) {
      list.value = res.data
      totalRecords.value = res.data.length
      if (res.data.length > 0 && !sample.value) {
        sample.value = res.data[0]
        gridRef.value?.setCurrentRow(res.data[0])
      }
    } else {
      list.value = []
      totalRecords.value = 0
    }
  } catch (e) {
    console.error('加载择样明细失败:', e)
    list.value = []
    totalRecords.value = 0
  } finally {
    tableLoading.value = false
    await nextTick()
    restoreCheckedRows()
  }
}

// 加载择样单头信息
const loadRecord = async () => {
  if (!codeName.value) return
  try {
    const res = await api(`/client-samples?keyword=${encodeURIComponent(codeName.value)}&size=1`)
    if (res.code === 200 && res.data?.records?.length > 0) {
      record.value = res.data.records[0]
    }
  } catch (e) {
    console.error('加载择样单信息失败:', e)
  }
}

// 加载报价设置用于卡片展示
const loadPriceSettings = async () => {
  if (!codeName.value) return
  await loadPriceSetting('1')
  await loadPriceSetting('2')
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
          resizeRafId = requestAnimationFrame(() => { tableWrapHeight.value = h - 12 })
        }
      }
    })
    resizeObserver.observe(tableWrapRef.value)
    // 虚拟滚动后重新挂选区把手
    let handleScrollTimer = null
    tableWrapRef.value.addEventListener('scroll', () => {
      if (!areaHandleEl || !document.contains(areaHandleEl)) {
        if (handleScrollTimer) clearTimeout(handleScrollTimer)
        handleScrollTimer = setTimeout(attachAreaHandle, 150)
      }
    }, { passive: true })
  }
  document.addEventListener('click', onDocClick)
  window.addEventListener('keydown', onAreaCopyKey, true)
  document.addEventListener('copy', onAreaCopyEvent, true)
  document.addEventListener('mousedown', onTableWrapMouseDown, true)
  // 切换回浏览器标签页时自动刷新（用于 uniapp 等外部设备添加数据后同步）
  document.addEventListener('visibilitychange', onVisibilityChange)
  loadRecord()
  loadData()
  loadPriceSettings()
})

const onVisibilityChange = () => {
  if (document.visibilityState === 'visible') {
    loadRecord()
    loadData()
    loadPriceSettings()
  }
}

const onDocClick = (e) => {
  if (areaSelectedColumn.value && tableWrapRef.value && !tableWrapRef.value.contains(e.target)) {
    clearAreaSelection()
  }
  if (menuOpen.value && !e.target.closest('.csd-dropdown')) {
    menuOpen.value = false
  }
}

// ── 图片拖拽到桌面 ──
const onDetailImgDragStart = (e) => {
  const src = e.target.currentSrc || e.target.src
  if (!src) return
  const fullUrl = src.startsWith('http') ? src : window.location.origin + src
  const fileName = src.split('/').pop().split('?')[0] || 'image.jpg'
  e.dataTransfer.setData('DownloadURL', `image/jpeg:${fileName}:${fullUrl}`)
  e.dataTransfer.effectAllowed = 'copyMove'
}

onBeforeUnmount(() => {
  showPhotoModal.value = false
  document.removeEventListener('click', onDocClick)
  window.removeEventListener('keydown', onAreaCopyKey, true)
  document.removeEventListener('copy', onAreaCopyEvent, true)
  document.removeEventListener('mousedown', onTableWrapMouseDown, true)
  document.removeEventListener('mousemove', onDocMouseMove)
  document.removeEventListener('mouseup', onDocMouseUp)
  document.removeEventListener('visibilitychange', onVisibilityChange)
  removeAreaHandle()
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  // 关闭所有弹窗，防止切换页面后仍然显示
  showPhotoModal.value = false
  showImagePreview.value = false
  thumbTooltip.show = false
})

// 路由切换时关闭弹窗
watch(() => route.path, () => {
  showPhotoModal.value = false
  showImagePreview.value = false
  thumbTooltip.show = false
})

onActivated(() => {
  loadRecord()
  loadData()
  loadPriceSettings()
  requestAnimationFrame(() => {
    nextTick(() => {
      if (tableWrapRef.value) {
        const rect = tableWrapRef.value.getBoundingClientRect()
        if (rect.height > 0) tableWrapHeight.value = rect.height
      }
    })
  })
})

// ========== 卡片模式 ==========
const cardMode = ref(false)
const cardContainerRef = ref(null)
const cardScrollTop = ref(0)
const cardRowHeight = ref(420)
const cardCols = ref(6)
const cardBufferRows = 3
const cardMeasured = ref(false)

const totalCardRows = computed(() => Math.ceil(filteredList.value.length / cardCols.value) || 0)

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
  const endIdx = Math.min(filteredList.value.length, endRow * cardCols.value)
  return filteredList.value.slice(startIdx, endIdx)
})

const cardTotalHeight = computed(() => {
  return totalCardRows.value * cardRowHeight.value + 16
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
  const firstTop = items[0].getBoundingClientRect().top
  const lastInRow = items[cardCols.value - 1]
  if (!lastInRow) return
  const rowBottom = lastInRow.getBoundingClientRect().bottom
  const measured = rowBottom - firstTop + 14
  if (measured > 50) {
    cardRowHeight.value = measured
    cardMeasured.value = true
  }
}

watch(cardMode, async (v) => {
  if (v) {
    cardScrollTop.value = 0
    cardMeasured.value = false
    nextTick(() => {
      requestAnimationFrame(() => measureCardRowHeight())
    })
  } else {
    await nextTick()
    if (tableWrapRef.value) {
      const rect = tableWrapRef.value.getBoundingClientRect()
      if (rect.height > 0) tableWrapHeight.value = rect.height
    }
  }
})

const isCardChecked = (row) => {
  return checkedRows.value.some(r => r.itemId === row.itemId)
}

const toggleCardSelect = (row) => {
  const idx = checkedRows.value.findIndex(r => r.itemId === row.itemId)
  if (idx > -1) {
    checkedRows.value.splice(idx, 1)
  } else {
    checkedRows.value.push(row)
  }
  if (gridRef.value) {
    gridRef.value.setCheckboxRow(row, idx === -1)
  }
}

const copyCardCode = (code) => {
  if (!code) return
  navigator.clipboard?.writeText?.(code)?.catch?.(() => {})
    || navigator.clipboard?.write?.(new ClipboardItem({ 'text/plain': new Blob([code], { type: 'text/plain' }) }))?.catch?.(() => {})
}

const onCardImgError = (e) => {
  if (e.target) {
    e.target.style.display = 'none'
    const parent = e.target.parentElement
    if (parent && !parent.querySelector('.csd-card-no-img')) {
      const div = document.createElement('div')
      div.className = 'csd-card-no-img'
      div.textContent = '无图'
      parent.appendChild(div)
    }
  }
}
</script>

<style>
/* 蓝牙标签打印预览样式 */
.bt-label {
  display: inline-block;
  vertical-align: top;
  margin: 0 2px 8px 2px;
  padding: 6px 8px;
  border: 1px dashed #d1d5db;
  background: #fff;
  border-radius: 4px;
}
.bt-label-row {
  margin: 0;
  padding: 0;
  word-break: break-all;
}
.bt-label-title {
  display: inline;
}
.bt-label-colon {
  display: inline;
  margin: 0 2px;
}
.bt-label-value {
  display: inline;
}
@media print {
  .bt-label {
    border: 0;
    margin: 0 2px 4px 0;
    padding: 4px 6px;
    page-break-inside: avoid;
  }
}

/* 恢复库存弹窗表格样式（覆盖全局 modify-dialog 蓝牙打印规则） */
.modify-dialog .revert-grid .vxe-body--column,
.modify-dialog .revert-grid .vxe-body--column .vxe-cell,
.modify-dialog .revert-grid .vxe-table--body td,
.modify-dialog .revert-grid .vxe-table--body .vxe-cell {
  font-size: 24px !important;
}
.modify-dialog .revert-grid .vxe-table--header .vxe-cell {
  font-size: 22px !important;
}
/* 复选框居中 */
.modify-dialog .revert-grid .vxe-cell--checkbox,
.modify-dialog .revert-grid .vxe-body--column.col--checkbox,
.modify-dialog .revert-grid .vxe-table--fixed-left .vxe-cell--checkbox,
.modify-dialog .revert-grid .vxe-table--fixed-left .col--checkbox {
  justify-content: center !important;
  text-align: center !important;
}
/* 复选框图标大小 */
.modify-dialog .revert-grid .vxe-cell--checkbox .vxe-checkbox--icon {
  width: 22px !important;
  height: 22px !important;
}
/* 去掉首列左padding */
.modify-dialog .revert-grid .vxe-table--body td:first-child .vxe-cell,
.modify-dialog .revert-grid .vxe-table--fixed-left td:first-child .vxe-cell {
  padding-left: 0 !important;
}
/* 分页器字号 */
.modify-dialog .revert-grid .vxe-pager,
.modify-dialog .revert-grid .vxe-pager * {
  font-size: 24px !important;
}
</style>

<style scoped>
/* ── client-photo-modal 模态框样式（独立,不依赖共享 spm-*） ── */

.client-photo-modal {
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

/* === 修改模态框 ×1.5 放大 === */
.modify-dialog { width: 2700px; }
.modify-header { padding: 18px 30px; }
.modify-header h3 { font-size: 24px; }
.modify-close { font-size: 27px; }
.modify-body { padding: 24px; gap: 24px; }
.modify-image-section { width: 900px; }
.modify-image-main { width: 900px; height: 900px; }
.modify-img-nav { width: 42px; height: 42px; font-size: 21px; }
.modify-img-counter { font-size: 18px; }
.modify-thumb-strip { gap: 9px; }
.modify-thumb-item { width: 84px; height: 84px; }
.modify-thumb-item.active { border-width: 3px; }
.modify-drop-badge { font-size: 18px; padding: 12px 24px; }
.modify-fields-section { gap: 12px; }
.modify-field label { font-size: 21px; margin-bottom: 6px; }
.modify-field input,
.modify-field select { height: 54px; font-size: 23px; padding: 0 18px; border-radius: 9px; }
.modify-image-empty { font-size: 21px; }
.modify-footer { padding: 18px 30px; gap: 15px; }
.modify-footer-buttons { gap: 15px; }
.modify-btn-save,
.modify-btn-cancel { font-size: 21px; padding: 12px 45px; }
.modify-sync { font-size: 20px; }
.modify-sync input { width: 24px; height: 24px; }
</style>
