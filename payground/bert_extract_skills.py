#!/usr/bin/env python3
import argparse
import json
import html2text
from transformers import BertTokenizer, BertForTokenClassification
from transformers import pipeline
import torch
 
# Load pre-trained model and tokenizer
tokenizer = BertTokenizer.from_pretrained('bert-base-cased')
model = BertForTokenClassification.from_pretrained('dbmdz/bert-large-cased-finetuned-conll03-english')
nlp = pipeline("ner", model=model, tokenizer=tokenizer)

def extract_skills(sentences):
    for sentence in sentences:
        if len(sentence) > 0:
            inputs = tokenizer(sentence, return_tensors="pt")
            outputs = model(**inputs).logits
            predictions = torch.argmax(outputs, dim=2)

            tokens = tokenizer.convert_ids_to_tokens(inputs["input_ids"][0])
            labels = [model.config.id2label[p.item()] for p in predictions[0]]

            for token, label in zip(tokens, labels):
                print("token: " + token + ", lebel: " + label)


parser = argparse.ArgumentParser()
parser.add_argument("file")
args = parser.parse_args()

text = ""
try:
    with open(args.file) as json_data:
        data = json.load(json_data)
        for result in data["results"]:
            if result['description']:
                html_content = result['description']  
                text_maker = html2text.HTML2Text()
                text_maker.ignore_links = True
                text = text_maker.handle(html_content)
                break
            else:
                continue
except:
    print("Load file as is")

if len(text) == 0:
    f = open(args.file, "r")
    text = f.read()

if len(text) == 0:
    print("File is empty")
    exit(1)

extract_skills(text.split("\n"))
#text = text.replace('\n', ' ')
#result = nlp(text)
#for doc in result:
#    print(doc.items())

