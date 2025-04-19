#!/usr/bin/env python3
import os
import argparse
import json
import pymongo


parser = argparse.ArgumentParser()
parser.add_argument("in_folder", help="output directory to store response files", default="")
parser.add_argument("mongoconnection", help="output directory to store response files", default="")
args = parser.parse_args()

if (not os.path.exists(args.in_folder)):
    print("Directory " + args.in_folder + " does not exist")
    exit(1)

mongoclient = pymongo.MongoClient(args.mongoconnection)
mongodb = mongoclient["1step2job"]
jobs_collection = mongodb.get_collection("jobs")

for subdir, dirs, files in os.walk(args.in_folder):
    for file in files:
        print ("Read " + os.path.join(subdir, file))
        if file.endswith((".json")):
            full_file_path = os.path.join(subdir,file)
            with open(full_file_path) as json_data:
                try:
                    data = json.load(json_data)
                    print(str(data["count"]) + " records in request " + full_file_path)
                    count = 0
                    for result in data["results"]:
                        if not result['ext_id']:
                            query = {"id": result['id']  }
                        else:
                            query = {"id": result['id'], "ext_id": str(result['ext_id']) }
                        #print("Add IDs: " + str(query) )
                        json_result = json.dumps(result, indent=4)
                        record = jobs_collection.find_one(query)
                        if not record:
                            #print("Add Record: " + json_result )
                            jobs_collection.insert_one(result)
                            count = count + 1
                    print(str(count) + " records were been saved from file " + full_file_path)
                except Exception as e:
                    print(e)
        os.remove(full_file_path)

    

