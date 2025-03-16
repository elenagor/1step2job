import os, gc
import time
import torch
import argparse
import traceback
from pathlib import Path
from transformers import AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig
from transformers.utils.logging import disable_progress_bar

# Constants
CACHE_DIR = "/var/projects/.hf_cache"
MODEL_DIR = "/var/projects/.models"

def run_compare(tokenizer, model, prompt, resume, job):
    prompt = prompt.replace("<job_description>", job).replace("<resume>", resume)
    return run_prompt(tokenizer, model, prompt)

def run_prompt(tokenizer, model, prompt):
    inputs = tokenizer(prompt, return_tensors="pt").to(model.device)
    with torch.no_grad():
        outputs = model.generate(
            **inputs,
            max_new_tokens=2028
        )

    response = tokenizer.decode(outputs[0], skip_special_tokens=True)
    if response.startswith(prompt):
        response = response[len(prompt):].strip()
    return response

def init(model_name):
    # Model details
    model_dir = os.path.join(MODEL_DIR, model_name.lower())

    # Load tokenizer
    tokenizer = AutoTokenizer.from_pretrained(
        model_dir,
        cache_dir=CACHE_DIR,
        trust_remote_code=True
    )

    # Load model
    model = AutoModelForCausalLM.from_pretrained(
        model_dir,
        device_map="cpu",
        trust_remote_code=True
    )

    return (model, tokenizer)

def cleanup(model, tokenizer):
    # Clean up
    del model
    del tokenizer
    torch.cuda.empty_cache()
    gc.collect()        

def main(model_name, prompt, resume, jobs):
    try:
        (model, tokenizer) = init(model_name)

        print(f"Processing resume from {resume}")
        resume_text = ""
        job_text = ""
        
        resume_text = Path(resume).read_text(encoding="UTF-8")

        for job in jobs:
            print(f"\n\tComparing with job description from {job}")
            start_time = time.time()
            job_text = Path(job).read_text(encoding="UTF-8")
            response = run_compare(tokenizer, model, prompt, resume_text, job_text)

            # Save response to a file in the same folder as resume and name matching job desciption
            out_file = os.path.join(os.path.dirname(resume), os.path.basename(job) + '.json')
            with open(out_file, "w") as f:
                f.write(response)
            elapsed_time = time.time() - start_time
            print(f"\tComplete, output file {out_file}. Elapsed time {round(elapsed_time)} sec")

        cleanup(model, tokenizer)
    except Exception as e:
        print(f"❌ Fatal error:")
        traceback.print_exc()

def dir_path(path):
    if os.path.isdir(path) and os.path.exists(path):
        return path
    else:
        raise argparse.ArgumentTypeError(f"bad_argument: {path} is not a valid dir path")

def file_path(path):
    if os.path.isfile(path) and os.path.exists(path):
        return path
    else:
        raise argparse.ArgumentTypeError(f"bad_argument:{path} is not a valid file path")
    
def list_files(dir):
    files = []
    for f in os.listdir(dir):
        fn = os.path.join(dir, f)
        if os.path.isfile(fn):
            files.append(fn)
    return files

if __name__ == "__main__":
    # Set environemnt
    disable_progress_bar()
    os.environ["HF_HOME"] = CACHE_DIR
    # Set up argument parser
    parser = argparse.ArgumentParser(description="Download Qwen model")
    parser.add_argument("-m", "--model", type=str, help="Qwen model name", default="Qwen2.5-14B-Instruct", metavar="FILE")
    parser.add_argument("-p", "--prompt", type=file_path, help="Prompt File Name. The prompt file should contain placeholders <resume> and <job_description>, those placeholders will be replaced by respecive valued at runtime.")
    parser.add_argument("-r", "--resume", type=file_path, help="Resume File Name", required=True)

    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("-f", "--job", type=file_path, help="Job Description File Name", metavar="FILE")
    group.add_argument("-d", "--job-dir", type=dir_path, help="Job Description Directory", metavar="DIR")
    args = parser.parse_args()

    if args.prompt:
        prompt_file = args.prompt
    else:
        prompt_file = os.path.join(os.path.dirname(os.path.abspath(__file__)), "prompt.txt")
    prompt = Path(prompt_file).read_text(encoding="UTF-8")

    if args.job:
        main(args.model, prompt, args.resume, [args.job])
    elif args.job_dir:
        main(args.model, prompt, args.resume, list_files(args.job_dir))
