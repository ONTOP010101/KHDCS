import { ref, computed, nextTick } from 'vue'
import { api } from '@/api'
import QRCode from 'qrcode'

export function useScanPrint(
  showScanPrintModal,
  showMultiPrintModal,
  showPrintDropdown,
  gridRef,
  tableData,
  totalRecords,
  currentPage,
  pageSize,
  loadTableData,
  showToast,
  showConfirmDialog
) {
  // ========== 扫码打印 ==========
  const scanPrintCode = ref('')
  const scanPrintResult = ref(null)
  const scanPrintImageSrc = ref('')
  const scanPrintError = ref('')
  const scanPrintType = ref('barcode')
  const scanPrintLoading = ref(false)
  const scanPrintContinuous = ref(false)
  const scanPrintCount = ref(1)
  const scanPrintInputRef = ref(null)

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

  // ========== 打印标签生成 ==========
  const esc = (s) => { if (!s && s !== 0) return ''; return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;') }
  const escAttr = (s) => { if (!s && s !== 0) return ''; return String(s).replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/</g,'&lt;').replace(/>/g,'&gt;') }

  const printHtml = (html) => {
    return new Promise((resolve) => {
      const iframe = document.createElement('iframe')
      iframe.style.cssText = 'position:fixed;top:0;left:0;width:600px;height:400px;border:none;z-index:-1;opacity:0'
      document.body.appendChild(iframe)

      const cleanup = () => {
        try { document.body.removeChild(iframe) } catch(e) {}
        resolve()
      }

      iframe.onload = () => {
        // 等 iframe 内所有图片加载完
        const imgs = iframe.contentDocument.querySelectorAll('img')
        let loaded = 0
        const check = () => {
          loaded++
          if (loaded >= imgs.length) {
            iframe.contentWindow.print()
            iframe.contentWindow.onafterprint = () => cleanup()
            setTimeout(cleanup, 8000)
          }
        }
        if (imgs.length === 0) {
          iframe.contentWindow.print()
          iframe.contentWindow.onafterprint = () => cleanup()
          setTimeout(cleanup, 8000)
        } else {
          imgs.forEach(img => {
            if (img.complete) check()
            else { img.onload = check; img.onerror = check }
          })
        }
      }

      iframe.srcdoc = html
      // 兜底
      setTimeout(cleanup, 20000)
    })
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

      const inner = r.innerBoxCount != null ? r.innerBoxCount : '0'
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

  // ========== 表格勾选打印 ==========
  const doPrintTable = () => {
    showPrintDropdown.value = false
    const records = gridRef.value ? gridRef.value.getCheckboxRecords() : []
    if (!records || records.length === 0) {
      showToast('请先勾选要打印的样品数据', 'warn')
      return
    }
    generateBarcodeLabels(records)
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

  // ========== 批量多份打印 ==========
  const multiPrintType = ref('barcode')
  const multiPrintBatchCopies = ref(1)
  const multiPrintRecords = ref([])

  const totalPrintPages = computed(() => {
    return multiPrintRecords.value.reduce((sum, r) => sum + (r.copies || 0), 0)
  })

  const batchSetCopies = () => {
    const n = multiPrintBatchCopies.value || 1
    multiPrintRecords.value.forEach(r => { r.copies = n })
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

  return {
    // 扫码打印
    scanPrintCode,
    scanPrintResult,
    scanPrintImageSrc,
    scanPrintError,
    scanPrintType,
    scanPrintLoading,
    scanPrintContinuous,
    scanPrintCount,
    scanPrintInputRef,
    openScanPrintModal,
    searchScanPrint,
    doScanPrint,
    // 表格打印
    doPrintTable,
    doPrintQuarterTable,
    doPrintAllLabels,
    doPrintWithImages,
    // 批量多份打印
    multiPrintType,
    multiPrintBatchCopies,
    multiPrintRecords,
    totalPrintPages,
    batchSetCopies,
    doPrintMultiCopies,
    confirmMultiPrint,
  }
}
