import { ref, reactive } from 'vue'
import { api } from '@/api'

const ADV_SEARCH_KEY = 'sample_adv_search_form'

const defaultAdvForm = () => ({
  manufacturerCode: '', name: '', contact1: '',
  phone1: '', mobile1: '', sampleName: '',
  sampleCode: '', factoryCode: '', boothNo: '',
  factoryPriceMin: null, factoryPriceMax: null, category: '', categoryCode: '',
  cartonCapacityMin: null, cartonCapacityMax: null, packageCode: '', packagingCn: '',
  certification: '', infringement: '', hasImage: false, hasVideo: false,
  sampleLengthMin: null, sampleLengthMax: null,
  sampleWidthMin: null, sampleWidthMax: null, sampleHeightMin: null, sampleHeightMax: null,
  packageLengthMin: null, packageLengthMax: null,
  packageWidthMin: null, packageWidthMax: null, packageHeightMin: null, packageHeightMax: null,
  cartonLengthMin: null, cartonLengthMax: null,
  cartonWidthMin: null, cartonWidthMax: null, cartonHeightMin: null, cartonHeightMax: null,
  innerBoxCountMin: null, innerBoxCountMax: null, batteryInfo: '', keyword: '',
  createTimeMin: '', createTimeMax: '', updateTimeMin: '', updateTimeMax: '',
  registrant: '', modifier: ''
})

/**
 * 综合查询（高级搜索）composable
 * @param {import('vue').Ref} tableData - 表格数据 ref
 * @param {import('vue').Ref} totalRecords - 总记录数 ref
 * @param {import('vue').Ref} currentPage - 当前页码 ref
 * @param {import('vue').Ref} pageSize - 每页条数 ref
 * @param {import('vue').Ref} currentSortField - 排序字段 ref
 * @param {import('vue').Ref} currentSortOrder - 排序方向 ref
 * @param {import('vue').Ref} activeSearchConditions - 活跃搜索条件 ref（需在外部定义，供 loadTableData 等使用）
 */
export function useAdvancedSearch(tableData, totalRecords, currentPage, pageSize, currentSortField, currentSortOrder, activeSearchConditions, manufacturerCode) {
  const showAdvancedSearch = ref(false)
  const advForm = reactive(defaultAdvForm())
  const searchElapsed = ref(null)

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

  const doAdvancedSearch = async () => {
    const f = advForm
    const conditions = []
    const push = (field, op, val) => { if (val !== '' && val != null && val !== false) conditions.push({ field, operator: op, value: String(val) }) }
    const pushLike = (field, val) => push(field, 'like', val)
    const pushEq = (field, val) => push(field, 'eq', val)

    // 文本模糊匹配
    pushLike('manufacturerCode', f.manufacturerCode)
    pushLike('name', f.name)
    pushLike('contact1', f.contact1)
    pushLike('phone1', f.phone1)
    pushLike('mobile1', f.mobile1)
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
    pushLike('registrant', f.registrant)
    pushLike('modifier', f.modifier)

    // 范围字段
    if (f.factoryPriceMin != null) push('factoryPrice', 'ge', f.factoryPriceMin)
    if (f.factoryPriceMax != null) push('factoryPrice', 'le', f.factoryPriceMax)
    if (f.cartonCapacityMin != null) push('cartonCapacity', 'ge', f.cartonCapacityMin)
    if (f.cartonCapacityMax != null) push('cartonCapacity', 'le', f.cartonCapacityMax)
    if (f.innerBoxCountMin != null) push('innerBoxCount', 'ge', f.innerBoxCountMin)
    if (f.innerBoxCountMax != null) push('innerBoxCount', 'le', f.innerBoxCountMax)

    // 日期范围
    if (f.createTimeMin) push('createTime', 'ge', f.createTimeMin + ' 00:00:00')
    if (f.createTimeMax) push('createTime', 'le', f.createTimeMax + ' 23:59:59')
    if (f.updateTimeMin) push('updateTime', 'ge', f.updateTimeMin + ' 00:00:00')
    if (f.updateTimeMax) push('updateTime', 'le', f.updateTimeMax + ' 23:59:59')

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

    // 图片/视频筛选
    if (f.hasImage) {
      conditions.push({ field: 'image', operator: 'eq', value: '1' })
    }
    if (f.hasVideo) {
      conditions.push({ field: 'video', operator: 'eq', value: '1' })
    }

    // 如果有厂商筛选，追加条件
    if (manufacturerCode && manufacturerCode.value) {
      conditions.push({ field: 'manufacturerCode', operator: 'eq', value: manufacturerCode.value })
    }

    saveAdvForm()
    activeSearchConditions.value = conditions
    // 综合查询始终从第 1 页开始
    currentPage.value = 1

    try {
      const res = await api(`/samples/search?current=${currentPage.value}&size=${pageSize.value}&sortField=${currentSortField.value}&sortOrder=${currentSortOrder.value}`, {
        method: 'POST',
        body: JSON.stringify({ conditions })
      })
      const data = res.data || res || {}
      tableData.value = data.records || data.list || data || []
      totalRecords.value = data.total || tableData.value.length
      searchElapsed.value = data.elapsed != null ? data.elapsed : null
      showAdvancedSearch.value = false
      return true
    } catch (e) {
      console.error(e)
      return false
    }
  }

  return {
    showAdvancedSearch,
    advForm,
    searchElapsed,
    saveAdvForm,
    restoreAdvForm,
    clearAdvForm,
    openAdvancedSearch,
    doAdvancedSearch,
  }
}
