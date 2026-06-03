import torch
import torchvision
import os

model = torchvision.models.mobilenet_v2(pretrained=False)
model.load_state_dict(torch.load("models/mobilenet_v2/mobilenet_v2.pt", map_location="cpu"))
model.classifier = torch.nn.Identity()
model.eval()

example = torch.randn(1, 3, 224, 224)
traced = torch.jit.trace(model, example)

out_dir = "models/mobilenet_v2_feat"
os.makedirs(out_dir, exist_ok=True)
traced.save(os.path.join(out_dir, "mobilenet_v2_feat.pt"))

output = model(example)
print(f"Feature dim: {output.shape[1]}")
print("Model saved to models/mobilenet_v2_feat/mobilenet_v2_feat.pt")
