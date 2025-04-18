#!/usr/bin/env python3
import os
import argparse
import requests
import json
import datetime
from datetime import datetime

parser = argparse.ArgumentParser()
parser.add_argument("experience_level", help="EN - Entry-level / Junior, MI - Mid-level / Intermediate, SE - Senior level / Expert, EX - Executive level / Director")
parser.add_argument("--job_by_type", default="1", help="1 - full-time, 2- part-time")
parser.add_argument("--day_period", default="1", help="days period: 1, 7, 30 - by default 1")
parser.add_argument("--region", default="5", help="3 - Europe, 4- Middle East,  5- North America, ")
parser.add_argument("--out_folder", help="output directory to store response files", default=".")
args = parser.parse_args()
if (not os.path.exists(args.out_folder)):
    print("Directory " + args.out_folder + " does not exist")
    exit(1)

# Define your API key and endpoint
API_KEY = 'YOUR_API_KEY'
url = "https://jobdataapi.com/api/jobs/"

# Set parameters for the API request
params = {
    #"language": "en",  # Filter for English job postings
    #"description_str": "true",  # Include cleaned description
    #"page_size": 5000,  # Number of results per page
    "max_age": args.day_period,
    "region":args.region,
    "experience_level": args.experience_level,
    "type_id": args.job_by_type
}
#headers = {"Authorization": "Api-Key " + API_KEY}
#response = requests.get(url, headers=headers, params=params)
# Fetch job listings
response = requests.get(url, params=params)
data = response.json()
try:
    if not data["results"]:
        print("response " + str(response) )
        exit(1)
except:
    print("response " + str(data) )
    exit(1)

json_string = json.dumps(data, indent=4)

folder = args.out_folder + "/" + datetime.today().strftime('%m%d%Y')
if not os.path.exists(folder):
    os.makedirs(folder)

filename = folder +  "/response_" + args.experience_level + "_" + args.job_by_type + "_" + args.day_period + "_" + args.region + "_" + datetime.today().strftime('%H%M') + ".json"
with open(filename, 'w') as json_file:
    json_file.write(json_string)
print("Result was saved in file " + filename)
