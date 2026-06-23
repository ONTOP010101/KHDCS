import { ref, computed, watch, nextTick } from 'vue'

export function useCardMode(tableData, formExpanded) {
  const cardMode = ref(false)
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

  return {
    cardMode,
    cardOverlayRef,
    cardScrollTop,
    cardContainerWidth,
    cardRowHeight,
    cardVisibleRange,
    cardTotalRows,
    cardVisibleItems,
    cardSpacerTop,
    cardSpacerBottom,
    onCardScroll,
  }
}
