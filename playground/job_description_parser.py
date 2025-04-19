#!/usr/bin/env python3
import os
import argparse
import json
import pymongo
import html2text
import spacy
import pytextrank

from lib_util import *

def save_result_todb( result):
    if not result['ext_id']:
        query = {"id": result['id']  }
    else:
        query = {"id": result['id'], "ext_id": str(result['ext_id']) }
    record = {"$set": result }
    print("Update record by IDs: " + str(query) )
    if args.dry_run == "False":
        res = jobs_collection.update_one(query, record, upsert=True)
        count = res.modified_count
    else:
        print( str(result) )
        count = 1
    return count

def create_record_todb(result, description):
    text = html_to_text(description)
    text = text.replace("\n", " ").replace("*", "").replace("#", "").replace("-", "")
    job = nlp(text)
    result['sentences'] = get_sentences(job)
    result['phraces'] = get_phrases(job)
    result['entities'] = get_entities(job)
    return result

def save_records_from_file_to_db(file_name):
    with open(file_name) as json_data:
        data = json.load(json_data)
        print(str(data["count"]) + " records in request " + os.path.basename(file_name))
        count = 0
        for result in data["results"]:
            if not result['ext_id']:
                query = {"id": result['id']  }
            else:
                query = {"id": result['id'], "ext_id": str(result['ext_id']) }
            #print("Add IDs: " + str(query) )
            description = result['description']
            if len(description) > 0:
                result = create_record_todb(result, description)

            json_result = json.dumps(result, indent=4)
            #print("Add Record: " + json_result )

            record = jobs_collection.find_one(query)
            if not record:
                if args.dry_run == "False":
                    jobs_collection.insert_one(result)
                else:
                    print(str(result) )
                count = count + 1
            else:
                record = {"$set": result }
                if args.dry_run == "False":
                    res = jobs_collection.update_one(query, record, upsert=True)
                    count = res.modified_count
                else:
                    print( str(result) )
                    count = count + 1
    return count

def save_data_from_file_to_db(file_name):
    if file_name.endswith((".json")):
        return save_records_from_file_to_db(file_name)
    if file_name.endswith(".doc") or file_name.endswith(".txt"):
        result = {}
        text = ""
        if file_name.endswith(".doc"):
            text = read_docx(file_name)
        if file_name.endswith(".txt"):
            text = open(file_name, "r").read()
        if len(text) > 0:
            result['description'] = text
            result['id'] = hash_string_md5(file_name)
            result['name'] = os.path.basename(file_name)
            result = create_record_todb(result, text)
            jobs_collection.insert_one(result)
            return 1
    print("Unknow file format " + file_name)
    return 0

parser = argparse.ArgumentParser()
parser.add_argument("--input_file", help="output directory to store response files", default="")
parser.add_argument("--input_folder", help="output directory to store response files", default="")
parser.add_argument("mongoconnection", help="output directory to store response files", default="")
parser.add_argument("--dry_run", help="output directory to store response files", default="True")
args = parser.parse_args()

# load a spaCy model, depending on language, scale, etc.
nlp = spacy.load("en_core_web_lg")
# add PyTextRank to the spaCy pipeline
model = nlp.add_pipe("textrank")

if len(args.input_folder) > 0 and (not os.path.exists(args.input_folder)):
    print("Directory " + args.input_folder + " does not exist")
    exit(1)

mongoclient = pymongo.MongoClient(args.mongoconnection)
mongodb = mongoclient["1step2job"]
jobs_collection = mongodb.get_collection("jobs")

if len(args.input_file) > 0:
    count = save_data_from_file_to_db(args.input_file)
    print(str(count) + " records were been saved from file " + os.path.basename(args.input_file))
if len(args.input_folder) > 0:
    for subdir, dirs, files in os.walk(args.input_folder):
        for file in files:
            print ("Read " + os.path.join(subdir, file))
            count = save_data_from_file_to_db(os.path.join(subdir,file))
            print(str(count) + " records were been saved from file " + file)
#else:
    #if args.dry_run == "False":
    #    with jobs_collection.find({}, {}) as cursor:
    #        results = [record for record in cursor]
    #        modified_count = 0
    #        print(f"Found {str(len(results))} in db. going to update...")
    #        for result in results:
    #            description = result['description']
    #            if len(description) > 0:
    #                result = create_record_todb(result, description)
    #                modified_count = modified_count + save_result_todb( result)
    #        print(f"Updated {str(modified_count)} records in db")