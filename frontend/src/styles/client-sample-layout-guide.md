# 自适应排版方案

## 核心公式

```
Grid 等宽列 + 标签固定宽度 + 输入框 flex:1 = 行列严格对齐 + 自适应宽度
```

## Grid 网格 + 响应式降级

```css
/* 桌面：6列 × 2行 */
.client-sample-form-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px 20px;
}

/* 中等屏：4列 */
@media (max-width: 1200px) { grid-template-columns: repeat(4, 1fr); }
/* 平板：3列 */
@media (max-width: 980px)  { grid-template-columns: repeat(3, 1fr); }
/* 手机：1列 */
@media (max-width: 560px)  { grid-template-columns: 1fr; }
```

## 标签 + 输入框自适应

```css
.client-sample-form-label {
  width: 72px;                /* 固定宽度 */
  flex-shrink: 0;
  text-align-last: justify;   /* 不同长度中文标签两端对齐 */
}

.client-sample-form-input {
  flex: 1;       /* 吃掉剩余宽度，自适应 */
  min-width: 0;  /* 防止撑破 */
}
```

每个字段格子内是 `flex` 行，标签锁死 72px，输入框自动铺满剩下的空间，无论屏幕多宽都不会错位。
