#!/usr/bin/env python3
"""
将训练好的 Siamese MobileNet 导出为 TorchScript，供 Java DJL 加载推理
"""

import os
import argparse

import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import models


class SiameseMobileNet(nn.Module):
    def __init__(self):
        super().__init__()
        backbone = models.mobilenet_v2(weights=None)
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


def export_model(checkpoint_path, output_path):
    device = torch.device("cpu")

    model = SiameseMobileNet()
    checkpoint = torch.load(checkpoint_path, map_location=device)
    model.load_state_dict(checkpoint["model_state_dict"])
    model.eval()
    model.to(device)

    example = torch.rand(1, 3, 224, 224, device=device)
    with torch.no_grad():
        output = model(example)
        print(f"Test output shape: {output.shape} (expected [1, 128])")
        print(f"Test output norm:  {output.norm(dim=1)} (expected ~1.0)")

    traced = torch.jit.trace(model, example)
    traced.save(output_path)

    file_size = os.path.getsize(output_path) / (1024 * 1024)
    print(f"\nModel exported to: {output_path} ({file_size:.1f} MB)")
    print(f"Embedding dimension: 128")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Export trained model to TorchScript")
    parser.add_argument("--checkpoint", required=True, help="Path to checkpoint .pt file")
    parser.add_argument("--output", default="siamese_mobilenet.pt", help="Output TorchScript path")
    args = parser.parse_args()

    export_model(args.checkpoint, args.output)
