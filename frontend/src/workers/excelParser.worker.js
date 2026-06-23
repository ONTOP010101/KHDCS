/**
 * Excel 解析 Web Worker — 在后台线程执行 XLSX.read/sheet_to_json，不阻塞主线程 UI
 * 接收: { type: 'parse', buffer: ArrayBuffer, options?: { ... } }
 * 返回: { type: 'progress', stage: string, progress: number } 或 { type: 'result', data: ... } 或 { type: 'error', message: string }
 */

import * as XLSX from 'xlsx'

self.onmessage = async (e) => {
  const { type, buffer } = e.data
  if (type !== 'parse' || !buffer) return

  try {
    self.postMessage({ type: 'progress', stage: '正在解析 Excel 文件...', progress: 10 })

    // 快速解析模式：跳过样式、公式、日期等不需要的元数据
    const workbook = XLSX.read(buffer, {
      type: 'array',
      cellFormula: false,
      cellStyles: false,
      cellDates: false,
      sheetStubs: false,
      bookVBA: false,
      bookFiles: false
    })

    self.postMessage({ type: 'progress', stage: '正在修剪列范围...', progress: 20 })

    const sheetName = workbook.SheetNames[0]
    const worksheet = workbook.Sheets[sheetName]

    // 修剪列范围：避免遍历 16376 空列
    let actualMaxCol = 0
    if (worksheet['!ref']) {
      const origRange = XLSX.utils.decode_range(worksheet['!ref'])
      if (origRange.e.c > 50) {
        const scanRows = Math.min(5, origRange.e.r)
        for (let r = 0; r <= scanRows; r++) {
          for (let c = origRange.e.c; c > actualMaxCol; c--) {
            if (worksheet[XLSX.utils.encode_cell({ r, c })] != null) {
              if (c > actualMaxCol) actualMaxCol = c
              break
            }
          }
        }
        if (origRange.e.r > scanRows) {
          const sampleRow = Math.min(scanRows + 20, origRange.e.r)
          for (let c = origRange.e.c; c > actualMaxCol; c--) {
            if (worksheet[XLSX.utils.encode_cell({ r: sampleRow, c })] != null) {
              if (c > actualMaxCol) actualMaxCol = c
              break
            }
          }
        }
        worksheet['!ref'] = XLSX.utils.encode_range({
          s: origRange.s,
          e: { r: origRange.e.r, c: actualMaxCol }
        })
      }
    }

    self.postMessage({ type: 'progress', stage: '正在提取表格数据...', progress: 35 })

    // 转为二维数组
    const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1, defval: '', raw: false })

    self.postMessage({ type: 'progress', stage: '正在检测表头...', progress: 45 })

    if (jsonData.length === 0) {
      self.postMessage({ type: 'error', message: 'Excel 文件为空' })
      return
    }

    // 把结果传回主线程（用 transferable 转移 ArrayBuffer 所有权以释放内存）
    self.postMessage({
      type: 'result',
      jsonData: jsonData,
      sheetName: sheetName
    })
  } catch (err) {
    self.postMessage({ type: 'error', message: err.message || '解析失败' })
  }
}
