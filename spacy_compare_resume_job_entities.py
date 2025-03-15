import spacy
import argparse
import json
import html2text
import pymongo

    
def compare_entities_resume_doc_with_job_description(resume_doc, job_description_doc, title, entityTitles, anyType, anyLabel):
    scores = []
    job_skills = extract_skills(job_description_doc, entityTitles)
    if len(job_skills) > 0 or anyType == True:
        total_sum = 0
        for resume_entity in resume_doc.ents:
            if resume_entity.label_ in entityTitles : 
                if resume_entity and resume_entity.vector_norm :
                    total_num_per_entity = 0
                    similar_score_per_entity = []
                    for job_entity in job_description_doc.ents:
                        if resume_entity.label_ == job_entity.label_ or anyLabel == True:
                            if job_entity and job_entity.vector_norm:
                                total_sum = total_sum + 1
                                total_num_per_entity = total_num_per_entity + 1
                                score = resume_entity.similarity(job_entity)
                                if score > 0.6:
                                    scores.append( [ resume_entity.text, resume_entity.label_, job_entity.text, job_entity.label_,  score ])
                                    similar_score_per_entity.append( [ resume_entity.text, resume_entity.label_, job_entity.text, job_entity.label_,  score ])
                    if total_num_per_entity > 0:
                        print("Entity: " + resume_entity.text + " Type: " + resume_entity.label_ + " has similarity > 0.6 is " + str(len(similar_score_per_entity)) + " from " + str(total_num_per_entity ) + ".")
        if total_sum > 0:
            print("\njob skills: " + str(job_skills) + ", title: " + title + " has sum similarity > 0.6 is " + str(len(scores) ) + " from " + str(total_sum) + ".\n")
        i = 1
        for score in scores:
            print(str(i) + " => " + str(score))
            i = i + 1
    return len(scores)

def extract_skills(doc, list_of_label_to_check):
    extracted_skills = set()
    for entity in doc.ents:
        if entity.label_ in list_of_label_to_check: 
            extracted_skills.add(entity.text)
    return extracted_skills

def load_job_description(html_content):
    text_maker = html2text.HTML2Text()
    text_maker.ignore_links = True
    job_description_text = text_maker.handle(html_content)
    return job_description_text.replace("\n", " ")

def compare_with_job_discription_from_file(resume_doc, job_file_path, list_of_label_to_check):
    resume_skills = extract_skills(resume_doc, list_of_label_to_check)
    print("resume skills: " + str(resume_skills) + "\n")
    with open(job_file_path) as json_data:
        data = json.load(json_data)
        for result in data["results"]:
            if result['description']:
                job_description_text =  load_job_description(result['description']) 
                print("job description: " + job_description_text + "\n")
                job_description_doc = nlp(job_description_text)
                print("All job entities vs all resume entities")
                compare_entities_resume_doc_with_job_description(resume_doc, job_description_doc, result['title'], list_of_label_to_check, True, True )
                print("\nAll job entities vs save resume entities")
                compare_entities_resume_doc_with_job_description(resume_doc, job_description_doc, result['title'], list_of_label_to_check, True, False )
                print("\nJob skills vs same resume skills")
                compare_entities_resume_doc_with_job_description(resume_doc, job_description_doc, result['title'], list_of_label_to_check, False, False )
                print("\nJob skills vs all resume entities")
                compare_entities_resume_doc_with_job_description(resume_doc, job_description_doc, result['title'], list_of_label_to_check, False, True )
            else:
                continue



def compare_with_job_discription_from_mongo(resume_doc, mongoconnection, list_of_label_to_check):
    resume_skills = extract_skills(resume_doc)
    print("resume skills: " + str(resume_skills) + "\n")
    match_count = 0
    mongoclient = pymongo.MongoClient(mongoconnection)
    mongodb = mongoclient["1step2job"]
    jobs_collection = mongodb.get_collection("jobs")
    with jobs_collection.find({}, {"title":1, "description":1}) as cursor:
        results = [record for record in cursor]
        print("Found " + str(len(results)) + " records in DB")
        res = 0
        for result in results:
            job_description_text =  load_job_description(result['description']) 
            #print("job description: " + job_description_text)
            job_description_doc = nlp(job_description_text)
            job_skills = extract_skills(job_description_doc)
            #print(" job skills: " + str(job_skills) + "\n")
            res = compare_entities_resume_doc_with_job_description(resume_doc, job_description_doc, result['title'], list_of_label_to_check, False, False) 
            
        print("Found " + str(res) + " records in DB that has similarity > 0.6" )





parser = argparse.ArgumentParser()
parser.add_argument("resume_file_path")
parser.add_argument("--mongoconnection", help="output directory to store response files", default="")
parser.add_argument("--job_file_path", help="output directory to store response files", default="")
args = parser.parse_args()

if len(args.mongoconnection) == 0 and len(args.job_file_path) == 0:
    print("Enter mongoconnection or job_file_path")
    exit(1)

f = open(args.resume_file_path, "r")
resume_text = f.read()
resume_text = resume_text.replace("\n", " ")
print("Resume: " + resume_text + "\n")

# Load the spaCy English model
nlp = spacy.load("en_core_web_lg")

# Extract skills from resume and job description
resume_doc = nlp(resume_text)
list_of_label_to_check = { "PRODUCT", "WORK_OF_ART", "PERSON", "GPE" }

if len(args.job_file_path) != 0:
    compare_with_job_discription_from_file(resume_doc, args.job_file_path, list_of_label_to_check)

if len(args.mongoconnection) != 0:
    compare_with_job_discription_from_mongo(resume_doc, args.mongoconnection, list_of_label_to_check)


