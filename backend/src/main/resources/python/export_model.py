"""Export MobileNetV2 feature extractor as TorchScript model.
Usage: python export_model.py <output_path>
Output: 1280-dim feature vector (before classifier).
"""
import sys
import torch
import torchvision


def export_mobilenet_v2_feat(output_path):
    # Load pretrained MobileNetV2
    model = torchvision.models.mobilenet_v2(weights="DEFAULT")
    model.eval()

    # Remove classifier, keep 1280-dim features
    model.classifier = torch.nn.Identity()

    # Trace with example input
    example = torch.randn(1, 3, 224, 224)
    traced = torch.jit.trace(model, example)

    traced.save(output_path)
    print(f"Model saved to: {output_path}")

    # Verify
    loaded = torch.jit.load(output_path)
    with torch.no_grad():
        out = loaded(example)
    print(f"Output dim: {out.shape[1]} (expected: 1280)")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python export_model.py <output.pt>")
        sys.exit(1)
    export_mobilenet_v2_feat(sys.argv[1])
