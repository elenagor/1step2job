#!/usr/bin/env python3
import argparse
from lib_util import *
from transformers import AutoModelForCausalLM, AutoTokenizer

model_name = "Qwen/QwQ-32B"

model = AutoModelForCausalLM.from_pretrained(
    model_name,
    torch_dtype="auto",
    device_map="auto"
)
tokenizer = AutoTokenizer.from_pretrained(model_name)

parser = argparse.ArgumentParser()
parser.add_argument("resume")
args = parser.parse_args()
text = ""
if args.resume.find(".txt") != -1:
    text = read_txt_file(args.resume)
if args.resume.find(".doc") != -1:
    text = read_docx(args.resume)

prompt = "Extract skills from sentenses"
messages = [
    {"role": "system", "content": text},
    {"role": "user", "content": prompt},
]
text = tokenizer.apply_chat_template(
    messages,
    tokenize=False,
    add_generation_prompt=True
)

model_inputs = tokenizer([text], return_tensors="pt").to(model.device)

generated_ids = model.generate(
    **model_inputs,
    max_new_tokens=32768
)
generated_ids = [
    output_ids[len(input_ids):] for input_ids, output_ids in zip(model_inputs.input_ids, generated_ids)
]

response = tokenizer.batch_decode(generated_ids, skip_special_tokens=True)[0]
print(response)