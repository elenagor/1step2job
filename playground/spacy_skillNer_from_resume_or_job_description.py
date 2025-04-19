import spacy
import argparse
import json
import html2text

# load default skills data base
from skillNer.general_params import SKILL_DB
from spacy.matcher import PhraseMatcher
from skillNer.skill_extractor_class import SkillExtractor

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
    print("Load file is is")

if len(text) == 0:
    f = open(args.file, "r")
    text = f.read()

if len(text) == 0:
    print("File is empty")
    exit(1)

text = text.replace('\n', ' ')
#print("Text: " + text)

print("SpaCy en_core_web_lg model get skills\n")
nlp=spacy.load("en_core_web_lg")

skill_extractor = SkillExtractor(nlp, SKILL_DB, PhraseMatcher)
annotations = skill_extractor.annotate(text)
#skill_extractor.display(annotations)

for full_matche in annotations['results']['full_matches']:
    print("full_matche: " + str(full_matche['doc_node_value']) + " score=" + str(full_matche['score']))

for ngram_score in annotations['results']['ngram_scored']:
    print("ngram_score: " + str(ngram_score['doc_node_value']) + " score=" + str(ngram_score['score']))

