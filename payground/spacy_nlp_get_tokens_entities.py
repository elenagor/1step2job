import spacy
import pytextrank
import argparse
import json, os
import html2text

from lib_util import *

def print_sentences(doc):
    for sentence in doc.sents:
        print(sentence)

def print_phrases(doc):
    for phrase in doc._.phrases:
        print(phrase)

def print_entities(doc, list_of_label):
    for entity in doc.ents:
        if  not list_of_label == None:
            if entity.label_ in list_of_label: 
                print(f"{entity.text} {entity.label_}")
        else: 
            print(f"{entity.text} {entity.label_}")

def print_entities_phrases_sents(text):
    text = text.replace('\n', ' ')
    #print("Text: " + text)
    # Process a text
    doc = nlp(text)

    # Extract named entities
    print_sentences(doc)
    print_phrases(doc)
    print_entities(doc, None)

def read_file(file_name):
    if file_name.endswith((".json")):
        with open(file_name) as json_data:
            data = json.load(json_data)
            for result in data["results"]:
                if result['description']:
                    text = html_to_text(result['description'] )
                    if len(text) > 0:
                        print_entities_phrases_sents(text)
                else:
                    continue
    elif file_name.endswith(".doc") or file_name.endswith(".txt"):
        if file_name.endswith(".doc"):
            text = read_docx(file_name)
        if file_name.endswith(".txt"):
            text = open(file_name, "r").read()
        if len(text) > 0:
            print_entities_phrases_sents(text)

parser = argparse.ArgumentParser()
parser.add_argument("file")
args = parser.parse_args()
print("SpaCy en_core_web_lg model")
# Load the custom spaCy NER model
nlp = spacy.load("en_core_web_lg")
# add PyTextRank to the spaCy pipeline
model = nlp.add_pipe("textrank")
print("Pipe names: " + str(nlp.pipe_names))

if args.file.endswith(".doc") or args.file.endswith(".txt") or args.file.endswith((".json")):
   read_file(args.file)
else:
    for subdir, dirs, files in os.walk(args.file):
        for file in files:
            print ("Read " + os.path.join(subdir, file))
            read_file(os.path.join(subdir, file))







