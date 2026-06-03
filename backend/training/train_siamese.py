#!/usr/bin/env python3
"""
Siamese MobileNetV2 微调训练脚本
架构：两张图走同一个MobileNet → 128维归一化向量 → ContrastiveLoss
"""

import os
import json
import argparse
from pathlib import Path
from io import BytesIO

import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms, models
from PIL import Image


class SiameseMobileNet(nn.Module):
    """共享权重的 Siamese 网络，输出 128 维 L2 归一化嵌入"""

    def __init__(self, pretrained=True):
        super().__init__()
        backbone = models.mobilenet_v2(weights="IMAGENET1K_V1" if pretrained else None)
        self.features = backbone.features
        self.embedding = nn.Sequential(
            nn.AdaptiveAvgPool2d(1),
            nn.Flatten(),
            nn.Dropout(0.2),
            nn.Linear(1280, 256),
            nn.ReLU(inplace=True),
            nn.Dropout(0.2),
            nn.Linear(256, 128),
        )

    def forward(self, x):
        x = self.features(x)
        x = self.embedding(x)
        return F.normalize(x, p=2, dim=1)


class ContrastiveLoss(nn.Module):
    """对比损失：同产品拉近，不同产品推远"""

    def __init__(self, margin=0.5):
        super().__init__()
        self.margin = margin

    def forward(self, emb_a, emb_b, label):
        dist = F.pairwise_distance(emb_a, emb_b, p=2)
        pos_loss = label * dist.pow(2)
        neg_loss = (1 - label) * F.relu(self.margin - dist).pow(2)
        return (pos_loss + neg_loss).mean()


class SimulatedPairDataset(Dataset):
    """动态生成训练样本对"""

    def __init__(self, pairs_file, transform=None):
        with open(pairs_file, "r", encoding="utf-8") as f:
            self.pairs = json.load(f)
        self.transform = transform or transforms.Compose([
            transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406],
                                 std=[0.229, 0.224, 0.225]),
        ])
        self.originals = {}
        for p in self.pairs:
            if p["anchor"] not in self.originals:
                try:
                    img = Image.open(p["anchor"]).convert("RGB")
                    self.originals[p["anchor"]] = self.transform(img)
                except Exception:
                    self.originals[p["anchor"]] = None

    def __len__(self):
        return len(self.pairs)

    def __getitem__(self, idx):
        p = self.pairs[idx]
        anchor_tensor = self.originals.get(p["anchor"])
        if anchor_tensor is None:
            anchor_tensor = torch.zeros(3, 224, 224)

        if p["pair_type"] == "simulated":
            pair_tensor = self._load_simulated(p)
        else:
            pair_tensor = self._load_original(p)

        label = torch.tensor(p["label"], dtype=torch.float32)
        return anchor_tensor, pair_tensor, label

    def _load_simulated(self, p):
        """对于模拟截图，用相同的变换但原始图做基础"""
        anchor = Image.open(p["anchor"]).convert("RGB")
        ss = self._simulate_quick(anchor)
        return self.transform(ss) if ss is not None else torch.zeros(3, 224, 224)

    def _load_original(self, p):
        try:
            img = Image.open(p["pair"]).convert("RGB")
            return self.transform(img)
        except Exception:
            return torch.zeros(3, 224, 224)

    @staticmethod
    def _simulate_quick(img, scale_range=(0.4, 0.8)):
        import random
        v = img.copy()
        w, h = v.size
        scale = random.uniform(*scale_range)
        new_w = int(w * scale)
        new_h = int(h * scale)
        v = v.resize((new_w, new_h), Image.BICUBIC)
        quality = random.randint(50, 80)
        buf = BytesIO()
        v.save(buf, format="JPEG", quality=quality)
        return Image.open(buf)


def train(args):
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")

    model = SiameseMobileNet(pretrained=True).to(device)
    criterion = ContrastiveLoss(margin=args.margin)
    optimizer = optim.Adam(model.parameters(), lr=args.lr, weight_decay=1e-5)
    scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=args.epochs)

    dataset = SimulatedPairDataset(args.pairs_file)
    dataloader = DataLoader(
        dataset, batch_size=args.batch_size, shuffle=True,
        num_workers=args.workers, pin_memory=True, drop_last=True
    )

    best_loss = float("inf")
    os.makedirs(args.output_dir, exist_ok=True)

    for epoch in range(1, args.epochs + 1):
        model.train()
        total_loss = 0.0
        batches = 0

        for batch_idx, (anchor, pair, label) in enumerate(dataloader):
            anchor = anchor.to(device)
            pair = pair.to(device)
            label = label.to(device)

            emb_a = model(anchor)
            emb_b = model(pair)
            loss = criterion(emb_a, emb_b, label)

            optimizer.zero_grad()
            loss.backward()
            optimizer.step()

            total_loss += loss.item()
            batches += 1

            if batch_idx % 50 == 0:
                print(f"  Epoch {epoch} Batch {batch_idx}: loss={loss.item():.6f}")

        scheduler.step()
        avg_loss = total_loss / max(batches, 1)
        print(f"Epoch {epoch}/{args.epochs} - Avg Loss: {avg_loss:.6f}")

        checkpoint_path = os.path.join(args.output_dir, f"checkpoint_epoch_{epoch}.pt")
        torch.save({
            "epoch": epoch,
            "model_state_dict": model.state_dict(),
            "optimizer_state_dict": optimizer.state_dict(),
            "loss": avg_loss,
        }, checkpoint_path)

        if avg_loss < best_loss:
            best_loss = avg_loss
            best_path = os.path.join(args.output_dir, "best_model.pt")
            torch.save({
                "epoch": epoch,
                "model_state_dict": model.state_dict(),
                "loss": avg_loss,
            }, best_path)
            print(f"  Saved best model (loss={avg_loss:.6f})")

    print(f"\nTraining complete. Best loss: {best_loss:.6f}")
    print(f"Models saved in: {args.output_dir}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Train Siamese MobileNetV2")
    parser.add_argument("--pairs-file", required=True, help="Training pairs JSON")
    parser.add_argument("--output-dir", default="./checkpoints", help="Output directory")
    parser.add_argument("--epochs", type=int, default=30, help="Training epochs")
    parser.add_argument("--batch-size", type=int, default=32, help="Batch size")
    parser.add_argument("--lr", type=float, default=1e-4, help="Learning rate")
    parser.add_argument("--margin", type=float, default=0.5, help="Contrastive loss margin")
    parser.add_argument("--workers", type=int, default=4, help="DataLoader workers")
    args = parser.parse_args()

    train(args)
