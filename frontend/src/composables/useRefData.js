import { ref, reactive } from 'vue'
import { api } from '@/api'

/**
 * 对照资料管理 composable
 * 管理产品种类（树形）和包装方式的 CRUD
 * @param {import('vue').Ref<boolean>} showMoreDropdown - 更多操作下拉显隐
 * @param {(msg: string, type?: string) => void} showToast - toast 提示函数
 */
export function useRefData(showMoreDropdown, showToast) {
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
  const refCategories = ref([])
  const refCatTreeData = ref([])
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
    const childCountMap = {}
    list.filter(r => r.level === 2).forEach(r => {
      const pc = r.parentCode || ''
      childCountMap[pc] = (childCountMap[pc] || 0) + 1
    })
    return list.map(r => ({
      ...r,
      _parentId: r.level === 2 ? null : undefined,
      _ck: false,
      _childCount: r.level === 1 ? (childCountMap[r.code] || 0) : undefined,
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

  return {
    showRefDataModal,
    refActiveTab,
    openReferenceDataModal,
    // 种类
    refCategories,
    refCatTreeData,
    refCatKeyword,
    refSelectedCatIds,
    refCatGridRef,
    showRefCatForm,
    refEditingCat,
    refCatForm,
    refLevel1Cats,
    refLoadLevel1Cats,
    refLoadCategories,
    refFilterCategories,
    refExpandAllCat,
    openRefCategoryAdd,
    refEditCategory,
    refSaveCategory,
    refDeleteCategory,
    refDeleteSelectedCats,
    saveRefCatKeywords,
    // 包装
    refPackagings,
    refPkgKeyword,
    refSelectedPkgIds,
    refPkgGridRef,
    showRefPkgForm,
    refEditingPkg,
    refPkgForm,
    refLoadPackagings,
    openRefPackagingAdd,
    refEditPackaging,
    refSavePackaging,
    refDeletePackaging,
    refDeleteSelectedPkgs,
  }
}
