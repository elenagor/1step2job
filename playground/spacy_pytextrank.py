#!/usr/bin/env python3
#DeepSeek API key sk-a26f606229e1401bb4b856d599e60968
import spacy
import os, time
import pytextrank
import argparse
import json
import html2text
import pymongo
from lib_util import *

def get_sents(doc):
    sentences = []
    for sentence in doc.sents:
        #print(sentence)
        sentences.append( sentence )
    return sentences

# Function to calculate similarity between sentences
def calculate_similarity(docs_job, docs_resume, title):
    score = 0
    max = len(docs_job) * len(docs_resume)
    for i in range(len(docs_job)):
        for j in range(len(docs_resume)):
            similarity = docs_job[i].similarity(docs_resume[j])
            score = score + similarity
            #if similarity > 0.95:
            print(f"Similarity between sentence '{docs_job[i]}' and sentence '{docs_resume[j]}': {similarity:.2f}")
    ratio = (score*100)/max
    if ratio > 50:
        print( f"Rank between resume and job title '{title}' : {ratio:.2f}")
    return score, max

def calculate_score_from_html_content(html_content, title):
    text = text = html_to_text(html_content)
    return calculate_score(text, title)

def calculate_score(text, title):
    text = text.replace("\n", " ").replace("*", "").replace("#", "").replace("-", "")
    doc_job = nlp(text)
    docs_job = get_sents(doc_job)
    return calculate_similarity(docs_job, docs_resume, title)

parser = argparse.ArgumentParser()
parser.add_argument("resume")
parser.add_argument("--mongoconnection", help="output directory to store response files", default="")
parser.add_argument("--job_file_path", help="output directory to store response files", default="")
args = parser.parse_args()
if len(args.mongoconnection) == 0 and len(args.job_file_path) == 0:
    print("Enter mongoconnection or job_file_path")
    exit(1)

# load a spaCy model, depending on language, scale, etc.
nlp = spacy.load("en_core_web_lg")
# add PyTextRank to the spaCy pipeline
model = nlp.add_pipe("textrank")

text = ""
if args.resume.find(".txt") != -1:
    text = read_txt_file(args.resume)
if args.resume.find(".doc") != -1:
    text = read_docx(args.resume)

text = text.replace("\n", " ")
doc_resume = nlp(text)
docs_resume = get_sents(doc_resume)

if len(args.job_file_path) != 0:
    if args.job_file_path.find(".json") != -1:
        with open(args.job_file_path) as json_data:
            best_title = ""
            best_ratio = 0
            data = json.load(json_data)
            for result in data["results"]:
                title = result['title']
                description = result['description']
                if len(description) > 0:
                    score, max = calculate_score_from_html_content(description, title)
                    ratio = (score*100)/max
                    if ratio > best_ratio:
                        best_ratio = ratio
                        best_title = title
            if best_ratio > 0:
                print(f"Best Job title: '{best_title}' with ratio: {best_ratio:.2f}%" )
            else:
                print("No Best Job found > 0")
    if args.job_file_path.find(".doc") != -1:
        text = read_docx(args.job_file_path)
        file_name = os.path.basename(args.job_file_path)
        score, max = calculate_score(text, file_name)
        print(f"Score : {score:.2f} from {max}, ratio: {((score*100)/max):.2f}%")
    if args.job_file_path.find(".txt") != -1:
        f = open(args.job_file_path, "r")
        text = f.read()
        file_name = os.path.basename(args.job_file_path)
        score, max = calculate_score(text, file_name)
        print(f"Score : {score:.2f} from {max}, ratio: {((score*100)/max):.2f}%")


if len(args.mongoconnection) != 0:
    start_time = time.time()
    mongoclient = pymongo.MongoClient(args.mongoconnection)
    mongodb = mongoclient["1step2job"]
    jobs_collection = mongodb.get_collection("jobs")
    with jobs_collection.find({}, {"id":1, "title":1, "description":1, "application_url":1}) as cursor:
        results = [record for record in cursor]
        best_ratio = 0
        best_job_title = None
        best_job_link = None
        best_job_id = None
        for result in results:
            title = result['title']
            description = result['description']
            if len(description) > 0:
                score, max = calculate_score_from_html_content(description, title)
                ratio = (score*100)/max
                if ratio > best_ratio:
                    best_ratio = ratio
                    best_job_title = title
                    best_job_link = result['application_url']
                    best_job_id = result['id']
        if best_ratio > 0:
            print(f"Best Job ID: {best_job_id} title: '{best_job_title}' ratio: {best_ratio:.2f}% apply to {best_job_link}")
        else:
            print("No best Job found")
    print(f"Execution Time: {time.time()-start_time} seconds")