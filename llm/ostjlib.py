import os, torch, gc
from transformers import AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig
from transformers.utils.logging import disable_progress_bar

# Constants
CACHE_DIR = "/var/projects/.hf_cache"
MODEL_DIR = "/var/projects/.models"

class OstjLLM:

    def __init__(self, model_name, verbose=False):
        self.verbose = verbose
        # Set environemnt
        disable_progress_bar()
        os.environ["HF_HOME"] = CACHE_DIR

        # Model details
        model_dir = os.path.join(MODEL_DIR, model_name.lower())

        # Load tokenizer
        self.tokenizer = AutoTokenizer.from_pretrained(
            model_dir,
            cache_dir=CACHE_DIR,
            trust_remote_code=True
        )

        # Load model
        self.model = AutoModelForCausalLM.from_pretrained(
            model_dir,
            device_map="cpu",
            trust_remote_code=True
        )

    def __del__(self): 
        # Clean up
        del self.model
        del self.tokenizer
        torch.cuda.empty_cache()
        gc.collect()        

    def run_prompt(self, prompt, placeholders: dict[str, str] = None):
        if placeholders:
            for key, value in placeholders.items(): 
                prompt = prompt.replace(f"<{key}>", value)

        if self.verbose:
            print(f"PROMPT: {prompt}")

        inputs = self.tokenizer(prompt, return_tensors="pt").to(self.model.device)
        with torch.no_grad():
            outputs = self.model.generate(
                **inputs,
                max_new_tokens=2028
            )

        response = self.tokenizer.decode(outputs[0], skip_special_tokens=True)
        if self.verbose:
            print(f"RAW RESULT: {response}")
        if response.startswith(prompt):
            response = response[len(prompt):].strip()
        return response
