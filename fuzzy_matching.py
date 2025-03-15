from fuzzywuzzy import fuzz
from fuzzywuzzy import process
import spacy
import pytextrank
import argparse
import json
import html2text
import pymongo

class Resume_Job_Ratio:
    def __init__(self, resume_phrase, job_phrase, ratio):
        self.resume_phrase = resume_phrase
        self.job_phrase = job_phrase
        self.ratio = ratio
    def __str__(self):
        return f"{self.resume_phrase} <-> {self.job_phrase} : {self.ratio}"
    def __repr__(self):
        return f"\n['{self.resume_phrase}' vs '{self.job_phrase}' ratio={self.ratio}]"

def extract_skills(doc):
    extracted_skills = set()
    for phrase in doc._.phrases:
        if phrase.rank > 0.05:
            extracted_skills.add(phrase.text)
    return extracted_skills

def load_job_description(html_content):
    text_maker = html2text.HTML2Text()
    text_maker.ignore_links = True
    job_description_text = text_maker.handle(html_content)
    return job_description_text.replace("\n", " ").replace("*", "")

# Function to compare titles using fuzzy matching
def compare_skills(job_skills, resume_skills, trace_label):
    list_resume_job_ratio = []
    job_score = 0
    for resume_skill in resume_skills:
        try:
            resume_skill = resume_skill.lower().strip()
            for job_skill in job_skills:
                try:
                    job_skill = job_skill.lower().strip()
                    # Use fuzzy matching to compare titles
                    ratio = fuzz.ratio(job_skills, resume_skill )
                    resume_job_ratio = Resume_Job_Ratio(resume_skill, job_skill, ratio)
                    if trace_label > 3:
                        print("Compare: " + str(resume_job_ratio))
                    list_resume_job_ratio.append(resume_job_ratio )
                    job_score = job_score + ratio
                    print(str(job_score) + "\r", end="")
                except Exception as e:
                    print("job_skill: " + str(job_skill)+ " Error: " + str(e))
                    break
        except Exception as e:
            print("resume_skill: " + str(resume_skill)+ " Error: " + str(e))
            break
    if trace_label > 3:
        print("Compare result: " + str(list_resume_job_ratio))
    return job_score, list_resume_job_ratio
    
def compare_resume_doc_with_job_description(resume_skills, job_description_doc, title, trace_label):
    job_score = 0
    resume_job_ratio = None
    job_skills = extract_skills(job_description_doc)
    if len(job_skills) > 0:
        job_score, resume_job_ratio = compare_skills(job_skills, resume_skills, trace_label) 
        if trace_label > 1:
            print("Job title: '" + title + "' Job skills: " + str(job_skills) + " job score=" + str(job_score) + "\n")
    else:
        if trace_label > 1:
            print("No skills of job title: " + title)
    return job_score, resume_job_ratio

def compare_with_job_discription_from_file(resume_skills, job_file_path, trace_label):
    with open(job_file_path) as json_data:
        data = json.load(json_data)
        best_score = 0
        best_job_title = None
        best_job_link = None
        best_job_id = None
        best_job_skills = None
        for result in data["results"]:
            if result['description']:
                job_description_text =  load_job_description(result['description']) 
                if trace_label > 2:
                    print("  Job description: " + job_description_text + "\n")
                job_description_doc = nlp(job_description_text)
                job_score, job_skills = compare_resume_doc_with_job_description(resume_skills, job_description_doc, result['title'], trace_label )
                if job_score > best_score:
                    best_score = job_score
                    best_job_title = result['title']
                    best_job_link = result['application_url']
                    best_job_id = result['id']
                    best_job_skills = job_skills
            else:
                continue
        if best_score > 0:
            print(str(best_job_skills))
            print("Best Job: ID=" + str(best_job_id) + " title is '" + best_job_title + "' score=" + str(best_score) + " apply to " + str(best_job_link))
        else:
            print("Best Job is not found")

def compare_with_job_discription_from_mongo(resume_skills, mongoconnection, trace_label):
    mongoclient = pymongo.MongoClient(mongoconnection)
    mongodb = mongoclient["1step2job"]
    jobs_collection = mongodb.get_collection("jobs")
    with jobs_collection.find({}, {"id":1, "title":1, "description":1, "application_url":1}) as cursor:
        results = [record for record in cursor]
        print("Found " + str(len(results)) + " records in DB")
        best_score = 0
        best_job_title = None
        best_job_link = None
        best_job_id = None
        best_job_skills = None
        for result in results:
            job_description_text =  load_job_description(result['description']) 
            if trace_label > 2:
                print(" => job description: " + job_description_text + "\n")
            job_description_doc = nlp(job_description_text)
            job_score, job_skills = compare_resume_doc_with_job_description(resume_skills, job_description_doc, result['title'], trace_label) 
            if job_score > best_score:
                    best_score = job_score
                    best_job_title = result['title']
                    best_job_link = result['application_url']
                    best_job_id = result['id']
                    best_job_skills = job_skills
        if best_score > 0:
            print(str(best_job_skills))
            print("Best Job ID:" + str(best_job_id) + " title: '" + best_job_title + "' score " + str(best_score) +" apply to " + str(best_job_link))
        else:
            print("No Job with score > 0")

parser = argparse.ArgumentParser()
parser.add_argument("resume_file_path")
parser.add_argument("--mongoconnection", help="output directory to store response files", default="")
parser.add_argument("--job_file_path", help="output directory to store response files", default="")
parser.add_argument("--print_label", help="print label 1=info/2=debug/3=trace", default="1")
args = parser.parse_args()
if len(args.mongoconnection) == 0 and len(args.job_file_path) == 0:
    print("Enter mongoconnection or job_file_path")
    exit(1)
f = open(args.resume_file_path, "r")
resume_text = f.read()
resume_text = resume_text.replace("\n", " ")
trace_label = int(args.print_label)
if trace_label > 2:
    print("Resume: " + resume_text + "\n")

# Load the spaCy English model
nlp = spacy.load("en_core_web_lg")
# add PyTextRank to the spaCy pipeline
nlp.add_pipe("textrank")

resume_doc = nlp(resume_text)
resume_skills = extract_skills(resume_doc)
if trace_label > 0:
    print(" -> resume skills: " + str(resume_skills) + "\n") 
if len(resume_skills) == 0:
    exit(1)
    
if len(args.job_file_path) != 0:
    compare_with_job_discription_from_file(resume_skills, args.job_file_path, trace_label)

if len(args.mongoconnection) != 0:
    compare_with_job_discription_from_mongo(resume_skills, args.mongoconnection, trace_label)