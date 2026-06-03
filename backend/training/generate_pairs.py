#!/usr/bin/env python3
"""
自动生成 Siamese 微调训练数据
输入：产品原图目录
输出：正负样本对 JSON 文件
"""

import os
import json
import random
import argparse
from pathlib import Path
from io import BytesIO

from PIL import Image, ImageFilter, ImageEnhance, ImageOps

SIMULATE_COUNT = 8


def simulate_screenshot(img):
    """模拟浏览器截图的各种变换"""
    variants = []
    w, h = img.size

    for _ in range(SIMULATE_COUNT):
        v = img.copy()

        # 1. 浏览器 UI 边框留白
        if random.random() < 0.6:
            top_margin = random.uniform(0.08, 0.15)
            bottom_margin = random.uniform(0.03, 0.10)
            new_h = int(h * (1 + top_margin + bottom_margin))
            canvas = Image.new("RGB", (w, new_h), (255, 255, 255))
            paste_y = int(new_h * top_margin)
            canvas.paste(v, (0, paste_y))
            v = canvas

        # 2. JPEG 压缩
        if random.random() < 0.7:
            quality = random.randint(50, 80)
            buf = BytesIO()
            v.save(buf, format="JPEG", quality=quality)
            v = Image.open(buf)

        # 3. 缩放
        if random.random() < 0.8:
            scale = random.uniform(0.4, 0.8)
            new_w = int(v.width * scale)
            new_h = int(v.height * scale)
            v = v.resize((new_w, new_h), Image.BICUBIC)

        # 4. 色彩偏移
        if random.random() < 0.5:
            color_factor = random.uniform(0.95, 1.05)
            v = ImageEnhance.Color(v).enhance(color_factor)
            brightness_factor = random.uniform(0.95, 1.05)
            v = ImageEnhance.Brightness(v).enhance(brightness_factor)

        # 5. 轻微旋转（模拟手机截屏角度）
        if random.random() < 0.3:
            angle = random.uniform(-3, 3)
            v = v.rotate(angle, expand=False, fillcolor=(255, 255, 255))

        variants.append(v)

    return variants


def generate_pairs(image_dir, output_path, negative_ratio=0.5):
    """
    生成训练数据对
    negative_ratio: 负样本对的比例
    """
    supported = {'.jpg', '.jpeg', '.png', '.webp', '.bmp'}
    image_files = []
    for ext in supported:
        image_files.extend(Path(image_dir).rglob(f'*{ext}'))
        image_files.extend(Path(image_dir).rglob(f'*{ext.upper()}'))

    if not image_files:
        print(f"No images found in {image_dir}")
        return

    print(f"Found {len(image_files)} original images")

    pairs = []
    product_ids = list(range(len(image_files)))

    for idx, img_path in enumerate(image_files):
        try:
            original = Image.open(img_path).convert("RGB")
        except Exception as e:
            print(f"  Skip {img_path.name}: {e}")
            continue

        # 生成模拟截图作为正样本
        try:
            screenshots = simulate_screenshot(original)
        except Exception as e:
            print(f"  Simulate failed for {img_path.name}: {e}")
            continue

        for ss in screenshots:
            pairs.append({
                "anchor": str(img_path),
                "pair": None,  # pair is the simulated image (saved separately)
                "pair_type": "simulated",
                "label": 1,
                "anchor_idx": idx,
            })

        if idx % 20 == 0:
            print(f"  Processing {idx}/{len(image_files)} ...")

    # 生成负样本对（不同产品间随机配对）
    positive_count = len(pairs)
    negative_count = int(positive_count * negative_ratio)

    for _ in range(negative_count):
        i, j = random.sample(product_ids, 2)
        pairs.append({
            "anchor": str(image_files[i]),
            "pair": str(image_files[j]),
            "pair_type": "original",
            "label": 0,
            "anchor_idx": i,
        })

    random.shuffle(pairs)

    os.makedirs(os.path.dirname(output_path) or ".", exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(pairs, f, ensure_ascii=False, indent=2)

    print(f"\nGenerated {len(pairs)} pairs:")
    print(f"  Positive (simulated screenshots): {positive_count}")
    print(f"  Negative (different products):    {negative_count}")
    print(f"Saved to {output_path}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate Siamese training pairs")
    parser.add_argument("--image-dir", required=True, help="Directory of original product images")
    parser.add_argument("--output", default="training_pairs.json", help="Output JSON path")
    parser.add_argument("--negative-ratio", type=float, default=0.5,
                        help="Ratio of negative pairs to positive pairs")
    args = parser.parse_args()

    generate_pairs(args.image_dir, args.output, args.negative_ratio)
