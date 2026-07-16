import { ref, reactive, computed } from 'vue'

export function useSampleForm() {
  const allFormFields = [
    // 第一行
    { key: 'sampleCode', label: '公司编号', labelWidth: 84, labelJustify: true, width: 220 },
    { group: true, key: 'g-packaging', label: '包装方式', labelWidth: 84, labelJustify: true, width: 0, fields: [
      { key: 'packageCode', placeholder: '包装编号', width: 60 },
      { key: 'packagingCn', placeholder: '中文包装', width: 150 },
      { key: 'packagingEn', placeholder: '英文包装', width: 48 },
    ]},
    { group: true, key: 'g-category', label: '种类名称', labelWidth: 84, labelJustify: true, width: 0, fields: [
      { key: 'categoryCode', placeholder: '种类编号', width: 74 },
      { key: 'category', placeholder: '种类名称', width: 158 },
    ]},
    { key: 'sampleName', label: '样品名称', labelWidth: 84, labelJustify: true },
    { break: true },
    // 第二行
    { key: 'factoryCode', label: '出厂货号', labelWidth: 84, labelJustify: true, width: 220 },
    { group: true, key: 'g-cartonSize', label: '外箱规格', labelWidth: 84, labelJustify: true, width: 0, fields: [
      { key: 'cartonLength', placeholder: '外箱长', width: 86 },
      { key: 'cartonWidth', placeholder: '外箱宽', width: 86 },
      { key: 'cartonHeight', placeholder: '外箱高', width: 86 },
    ]},
    { key: 'factoryPrice', label: '出厂价', labelWidth: 84, labelJustify: true, color: 'red', width: 230 },
    { key: 'remark', label: '中文备注', labelWidth: 84, labelJustify: true },
    { break: true },
    // 第三行
    { group: true, key: 'g-innerBox', label: '内盒/外箱装量', labelWidth: 84, labelJustify: true, width: 0, fields: [
      { key: 'innerBoxCount', placeholder: '内盒数', width: 60 },
      { key: 'cartonCapacity', placeholder: '装箱量', width: 119 },
    ]},
    { group: true, key: 'g-sampleSize', label: '包装规格', labelWidth: 84, labelJustify: true, width: 0, fields: [
      { key: 'packageLength', placeholder: '包装长', width: 86 },
      { key: 'packageWidth', placeholder: '包装宽', width: 86 },
      { key: 'packageHeight', placeholder: '包装高', width: 86 },
    ]},
    { group: true, key: 'g-cartonVol', label: '材积/体积', labelWidth: 84, labelJustify: true, width: 0, fields: [
      { key: 'cartonMaterialVolume', placeholder: '材积', width: 112 },
      { key: 'cartonVolume', placeholder: '体积', width: 112 },
    ]},
    { key: 'certification', label: '产品认证', labelWidth: 84, labelJustify: true },
    { break: true },
    // 第四行
    { key: 'infringement', label: '是否侵权', labelWidth: 84, labelJustify: true, width: 220 },
    { group: true, key: 'g-productSize', label: '产品规格', labelWidth: 84, labelJustify: true, width: 0, fields: [
      { key: 'sampleLength', placeholder: '产品长', width: 86 },
      { key: 'sampleWidth', placeholder: '产品宽', width: 86 },
      { key: 'sampleHeight', placeholder: '产品高', width: 86 },
    ]},
    { group: true, key: 'g-cartonWeight', label: '外箱毛/净重', labelWidth: 84, labelJustify: true, width: 0, fields: [
      { key: 'cartonGrossWeight', placeholder: '外箱毛重', width: 101 },
      { key: 'cartonNetWeight', placeholder: '外箱净重', width: 101 },
    ]},
    { group: true, key: 'g-sampleWeight', label: '样品毛/净重', labelWidth: 84, labelJustify: true, width: 0, fields: [
      { key: 'sampleGrossWeight', placeholder: '样品毛重', width: 101 },
      { key: 'sampleNetWeight', placeholder: '样品净重', width: 101 },
    ]},
    { key: 'batteryInfo', label: '电池信息', labelWidth: 84, labelJustify: true },
    { break: true },
    // 第五行
    { key: 'manufacturerCode', label: '厂商编号', labelWidth: 84, labelJustify: true, width: 220 },
    { key: 'boothNo', label: '摊位号', labelWidth: 84, labelJustify: true, width: 265 },
    { key: 'name', label: '厂商名称', labelWidth: 84, labelJustify: true, width: 220 },
    { key: 'phone1', label: '电话', labelWidth: 94, labelJustify: true, width: 220 },
    { key: 'mobile1', label: '手机', labelWidth: 94, labelJustify: true },
    { break: true },
    // 第六行
    { key: 'contact1', label: '联系人', labelWidth: 94, labelJustify: true, width: 220 },
    { key: 'qq', label: 'QQ', labelWidth: 84, labelJustify: true, width: 265 },
    { key: 'fax', label: '传真', labelWidth: 94, labelJustify: true, width: 220 },
    { key: 'taxPrice', label: '税点价', labelWidth: 94, labelJustify: true, color: 'red', width: 220 },
    { key: 'smsNumber', label: '短信号码', labelWidth: 94, labelJustify: true },
    { break: true },
    // 第七行
    { key: 'registrant', label: '登记人', labelWidth: 94, labelJustify: true, width: 220 },
    { key: 'modifier', label: '修改人', labelWidth: 84, labelJustify: true, width: 265 },
    { key: 'color', label: '颜色', labelWidth: 94, labelJustify: true, width: 220 },
    { key: 'englishName', label: '英文名称', labelWidth: 84, labelJustify: true, width: 220 },
    { key: 'createTime', label: '登记日期', labelWidth: 84, labelJustify: true, width: 500 },
    { key: 'updateTime', label: '修改日期', labelWidth: 84, labelJustify: true },
  ]

  const fieldVisible = reactive({})
  allFormFields.forEach(f => { fieldVisible[f.key] = true })

  const visibleFormFields = computed(() => allFormFields.filter(f => fieldVisible[f.key]))

  const showFieldSettings = ref(false)
  const toggleFieldSettings = () => { showFieldSettings.value = !showFieldSettings.value }

  const formExpanded = ref(false)
  const formVisible = ref(true)
  const formMode = ref('readonly')
  const formData = reactive({})

  return {
    allFormFields,
    fieldVisible,
    visibleFormFields,
    showFieldSettings,
    toggleFieldSettings,
    formExpanded,
    formVisible,
    formMode,
    formData,
  }
}
