import os
import torch
import argparse
from transformers import AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig
from huggingface_hub import login

# Constants
CACHE_DIR = "/var/projects/.hf_cache"
MODEL_DIR = "/var/projects/.models"

# Set up argument parser
parser = argparse.ArgumentParser(description="Download Qwen model")
parser.add_argument("--token", type=str, help="Hugging Face token for authentication", required=True)
parser.add_argument("--model", type=str, help="Qwen model name", required=True)
args = parser.parse_args()

# Login to Hugging Face
login(token=args.token)
print("✅ Logged in to Hugging Face")

# Set HF cache directory
os.environ["HF_HOME"] = CACHE_DIR
os.makedirs(CACHE_DIR, exist_ok=True)
print(f"✅ Cache directory set to {CACHE_DIR}")

# Model details
model_name = args.model
model_id = f"Qwen/{model_name}"
save_directory = f"{MODEL_DIR}/{model_name}".lower()

# Create new directory
os.makedirs(save_directory, exist_ok=True)
print(f"Will save model to {save_directory}")

# Download tokenizer
print("Downloading tokenizer...")
tokenizer = AutoTokenizer.from_pretrained(
    model_id,
    cache_dir=CACHE_DIR,
    trust_remote_code=True
)
tokenizer.save_pretrained(save_directory)
print(f"✅ Tokenizer saved to {save_directory}")


# Download and quantize model
print(f"Downloading {model_name} model (this may take a while)...")
try:
    model = AutoModelForCausalLM.from_pretrained(
        model_id,
        device_map="cpu",
        trust_remote_code=True,
        cache_dir=CACHE_DIR
    )

    print("Saving model...")
    model.save_pretrained(save_directory)
    print(f"✅ Model saved to {save_directory}")

    # Test loading
    print("Testing model loading...")
    test_model = AutoModelForCausalLM.from_pretrained(
        save_directory,
        device_map="cpu",
        trust_remote_code=True
    )
    print("✅ Test loading successful!")

    # Clean up
    del model
    del test_model
    torch.cuda.empty_cache()

    print(r"\n✅ {model_name} successfully downloaded and tested")
    print(f"New path: {save_directory}")

except Exception as e:
    print(f"❌ Model download failed: {str(e)}")
    print("You might need more memory and/or GPU to download this model directly.")
