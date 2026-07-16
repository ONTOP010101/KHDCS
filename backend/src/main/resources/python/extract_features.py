"""Persistent GPU feature extractor using PyTorch MobileNetV2.
Communicates via stdin/stdout JSON protocol.
Input:  image file path (one per line)
Output: JSON per line + "DONE" marker
"""
import sys
import json
import torch
import torchvision.transforms as T
from PIL import Image


def load_model(model_path):
    model = torch.jit.load(model_path, map_location="cuda")
    model.eval()
    return model


def get_transform():
    return T.Compose([
        T.Resize((224, 224)),
        T.ToTensor(),
        T.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])


def extract(model, transform, image_path):
    img = Image.open(image_path).convert("RGB")
    tensor = transform(img).unsqueeze(0).cuda()
    with torch.no_grad():
        features = model(tensor).squeeze(0).cpu().numpy().astype("float32")
    return features.tolist()


def main():
    if len(sys.argv) < 2:
        sys.stderr.write("[extract_features.py] Usage: python extract_features.py <model.pt>\n")
        sys.stderr.flush()
        sys.exit(1)

    model_path = sys.argv[1]
    sys.stderr.write(f"[extract_features.py] Loading model from: {model_path}\n")
    sys.stderr.flush()

    model = load_model(model_path)
    transform = get_transform()

    sys.stderr.write("[extract_features.py] Ready on GPU\n")
    sys.stderr.flush()

    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        try:
            features = extract(model, transform, line)
            result = {"features": features, "dim": len(features)}
            sys.stdout.write(json.dumps(result) + "\n")
        except Exception as e:
            sys.stdout.write(json.dumps({"error": str(e)}) + "\n")
        sys.stdout.write("DONE\n")
        sys.stdout.flush()


if __name__ == "__main__":
    main()
