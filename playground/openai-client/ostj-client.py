import os
import time, uuid
import argparse
import traceback
import asyncio
from pathlib import Path
import openai as openai_client

SYSTEM_MARK = "{system}"

verbose = False

client = openai_client.OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")

# resume = Path("/Users/yackerman/projects/ostj/playground/data/Person1/Person.txt").read_text(encoding="UTF-8")
# job_description = Path("/Users/yackerman/projects/ostj/playground/data/Person1/jds/JD_Match1.txt").read_text(encoding="UTF-8")

# start_time = time.time()
# response = client.chat.completions.create(
#         model="qwen-2.5", # This will depend on the model you're running locally
#         messages=[
#             {"role": "system", "content": "You are Human Resoures expert helping to idetify if candidate's resume matches job description"},
#             {"role": "user", "content": prompt.replace("{resume}", resume).replace("{job_description}", job_description)}
#         ])

# print(response.choices[0].message.content)
# elapsed_time = time.time() - start_time
# print(f"Elapsed time: {round(elapsed_time)} sec")


def save_response(response, out_dir):
    out_file_name = os.path.join(out_dir, str(uuid.uuid4()) + ".txt")
    with open(out_file_name, "w") as f:
        f.write(response)
    return out_file_name


def run_prompt(prompt, out_dir, params):
    start_time = time.monotonic()
    response = run_prompt_internal(prompt, params)

    # Save response to a file in the same folder as resume and name matching job desciption
    task_name = params["task_name"] if "task_name" in params else "Simple prompt"
    out_file = save_response(response.choices[0].message.content, out_dir)
    elapsed_time = time.monotonic() - start_time
    return f"{task_name}\n\tComplete, output file {out_file}.\n\tElapsed time {round(elapsed_time)} sec"

def run_prompt_internal(prompt, params):
    if SYSTEM_MARK in prompt:
        spos = prompt.index(SYSTEM_MARK) + len(SYSTEM_MARK)
        epos = prompt.find("\n\n")
        sys_prompt = prompt[spos:epos]
        prompt = prompt[epos + 2 :]
    else:
        sys_prompt = ""
    if "resume" in params:
        prompt = prompt.replace("{resume}", params["resume"])
    if "job_description" in params:
        prompt = prompt.replace("{job_description}", params["job_description"])


    response = client.chat.completions.create(
        model="qwen",  # This will depend on the model you're running locally
        messages=[
            {"role": "system", "content": sys_prompt.strip()},
            {"role": "user", "content": prompt.strip()},
        ],
        temperature=0.4
    )
    
    return response


async def worker(queue, worker_id):
    while True:
        (prompt, out_dir, params) = await queue.get()
        status = await asyncio.to_thread(run_prompt, prompt, out_dir, params)
        queue.task_done()
        print(f"Worker_{worker_id}: {status}")


async def main(
    prompt, out_dir, resume_file_name=None, job_file_name_list=None, num_workers=1
):
    try:
        queue = asyncio.Queue(len(job_file_name_list) if job_file_name_list else 1)
        params: dict[str, str] = dict()

        print(f"Processing resume from {resume_file_name}")
        resume_text = None
        job_text = None

        started = time.monotonic()
        if resume_file_name:
            params["resume"] = Path(resume_file_name).read_text(encoding="UTF-8")

        if job_file_name_list:
            for job in job_file_name_list:
                p = params.copy()
                p["task_name"] = f"Job description file: {job}"
                p["job_description"] = Path(job).read_text(encoding="UTF-8")
                queue.put_nowait((prompt, out_dir, p))
        else:
            queue.put_nowait((prompt, out_dir, params))

        tasks = []
        for i in range(num_workers):
            print(f"Staring worker {i}")
            w = asyncio.create_task(worker(queue, i))
            tasks.append(w)

        await queue.join()
        for task in tasks:
            task.cancel()
        await asyncio.gather(*tasks, return_exceptions=True)

        print(f"Total elapsed time {round(time.monotonic() - started)} sec")

        # run_prompt(prompt, out_dir, params)
    except Exception as e:
        print(f"❌ Fatal error: {str(e)}")
        if verbose:
            traceback.print_exc()


def dir_path(path):
    if os.path.isdir(path) and os.path.exists(path):
        return path
    else:
        raise argparse.ArgumentTypeError(
            f"bad_argument: {path} is not a valid dir path"
        )


def file_path(path):
    if os.path.isfile(path) and os.path.exists(path):
        return path
    else:
        raise argparse.ArgumentTypeError(
            f"bad_argument:{path} is not a valid file path"
        )


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
    parser.add_argument(
        "-p",
        "--prompt",
        type=file_path,
        help="Prompt File Name. The prompt file may contain placeholders <resume> and <job_description>, those placeholders will be replaced by respecive valued at runtime.",
    )
    parser.add_argument("-r", "--resume", type=file_path, help="Resume File Name")
    parser.add_argument(
        "-o",
        "--out-dir",
        type=dir_path,
        help="Output directory, if not provided, current dir will be used",
        default=".",
    )
    parser.add_argument(
        "-n",
        "--num-workers",
        type=int,
        metavar="INT",
        help="Number of concurrent workers",
        default=1,
    )
    parser.add_argument(
        "-v", "--verbose", help="Increase verbosity level", action="store_true"
    )

    group = parser.add_mutually_exclusive_group()
    group.add_argument(
        "-f", "--job", type=file_path, help="Job Description File Name", metavar="FILE"
    )
    group.add_argument(
        "-d",
        "--job-dir",
        type=dir_path,
        help="Job Description Directory",
        metavar="DIR",
    )
    args = parser.parse_args()

    if args.verbose:
        verbose = True

    if args.prompt:
        prompt_file = args.prompt
    else:
        prompt_file = os.path.join(
            os.path.dirname(os.path.abspath(__file__)), "prompt.txt"
        )
    prompt = Path(prompt_file).read_text(encoding="UTF-8")

    if args.resume and (args.job or args.job_dir):
        jobs = []
        if args.job:
            jobs = [args.job]
        elif args.job_dir:
            jobs = list_files(args.job_dir)
        asyncio.run(main(prompt, args.out_dir, args.resume, jobs, args.num_workers))
    elif args.resume:
        asyncio.run(main(prompt, args.out_dir, args.resume))
    else:
        asyncio.run(main(prompt, args.out_dir))
    