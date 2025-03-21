import os
import time, uuid
import argparse
import traceback
from pathlib import Path
from ostjlib import OstjLLM

verbose = False
ostjllm: OstjLLM = None

def run_compare(tokenizer, model, prompt, resume, job):
    if resume:
        prompt = prompt.replace("<resume>", resume)
    if job:
        prompt = prompt.replace("<job_description>", job)
    return ostjllm.run_prompt(prompt)

def save_response(response, out_dir):
    out_file_name = os.path.join(out_dir, str(uuid.uuid4())+".txt")
    with open(out_file_name, "w") as f:
        f.write(response)
    return out_file_name

def run_prompt(prompt, out_dir, params):
    start_time = time.time()
    response = ostjllm.run_prompt(prompt, placeholders=params)

    # Save response to a file in the same folder as resume and name matching job desciption
    out_file = save_response(response, out_dir)
    elapsed_time = time.time() - start_time
    print(f"\tComplete, output file {out_file}. Elapsed time {round(elapsed_time)} sec")

def main(model_name, prompt, out_dir, resume_file_name=None, job_file_name_list=None):
    try:
        global ostjllm
        ostjllm = OstjLLM(model_name, verbose=verbose)
        params: dict[str, str] = dict()

        print(f"Processing resume from {resume_file_name}")
        resume_text = None
        job_text = None
        
        if resume_file_name:
            params["resume"] = Path(resume_file_name).read_text(encoding="UTF-8")

        if job_file_name_list:
            for job in job_file_name_list:
                print(f"\n\tComparing with job description from {job}")
                params["job_description"] = Path(job).read_text(encoding="UTF-8")
                run_prompt(prompt, out_dir, params)
        else:
            run_prompt(prompt, out_dir, params)
    except Exception as e:
        print(f"❌ Fatal error: {str(e)}")
        if verbose:
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
    # Set up argument parser
    parser = argparse.ArgumentParser(description="Download Qwen model")
    parser.add_argument("-m", "--model", type=str, help="Qwen model name", default="Qwen2.5-14B-Instruct", metavar="FILE")
    parser.add_argument("-p", "--prompt", type=file_path, help="Prompt File Name. The prompt file may contain placeholders <resume> and <job_description>, those placeholders will be replaced by respecive valued at runtime.")
    parser.add_argument("-r", "--resume", type=file_path, help="Resume File Name")
    parser.add_argument("-o", "--out-dir", type=dir_path, help="Output directory, if not provided, current dir will be used", default=".")
    parser.add_argument("-v", "--verbose", help="Increase verbosity level", action="store_true")

    group = parser.add_mutually_exclusive_group()
    group.add_argument("-f", "--job", type=file_path, help="Job Description File Name", metavar="FILE")
    group.add_argument("-d", "--job-dir", type=dir_path, help="Job Description Directory", metavar="DIR")
    args = parser.parse_args()

    if args.verbose:
        verbose = True

    if args.prompt:
        prompt_file = args.prompt
    else:
        prompt_file = os.path.join(os.path.dirname(os.path.abspath(__file__)), "prompt.txt")
    prompt = Path(prompt_file).read_text(encoding="UTF-8")

    if args.resume and (args.job or args.job_dir):
        if args.job:
            main(args.model, prompt, args.out_dir, args.resume, [args.job])
        elif args.job_dir:
            main(args.model, prompt, args.out_dir, args.resume, list_files(args.job_dir))
    elif not args.resume and not args.job and not args.job_dir:
        main(args.model, prompt, args.out_dir)
    else:
        print(f"❌ Fatal error: '--resume' flag requires one of '--job' or '--job-dir' are mandatory")
        exit(1)
 
