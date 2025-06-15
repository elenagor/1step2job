#!/usr/bin/env python3
import os
import argparse
import json
import html2text
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy import text
import ostj_client as ostj

class Job_Description_Record:
    def __init__(self,  ext_id, title, title_embeddings, location_country, location_city, location_state, 
                 published, description, application_url, remote, salary_min, salary_max, type):
        self.external_id = ext_id
        self.title = title
        self.title_embeddings = title_embeddings
        self.location_country = location_country
        self.location_city = location_city
        self.location_state = location_state
        self.published = published
        self.description = description
        self.apply_url = application_url
        self.location_is_remote = remote
        self.salary_min = salary_min
        self.salary_max = salary_max
        self.type = type
    def __str__(self):
        return f"""
        '{self.external_id}', '{self.title}', '{self.title_embeddings}', '{self.location_country}', '{self.location_city}', '{self.location_state}', 
        '{self.published}', '{self.description}', '{self.apply_url}', '{self.location_is_remote}', '{self.salary_min}', '{self.salary_max}', '{self.type}'
        """

def html_to_text(html_content):  
    text_maker = html2text.HTML2Text()
    text_maker.ignore_links = True
    return text_maker.handle(html_content).replace("\n", " ").replace("*", "").replace("#", "").replace("-", "").replace("'", "")

def get_title_embeddings(title):
    title = title.upper()
    title_embedding = ostj.get_embedding(title)
    return title_embedding

def get_field_value_with_subkey(result, key, subkey):
    try:
        return result[key][0][subkey]
    except Exception as e:
        return ""
    
def get_field_real_value(result, key):
    try:
        if result[key] == None:
            return 0
        else:
            return result[key]
    except Exception as e:
        return 0

def save_to_db(result):
    record = Job_Description_Record(
                                    result["ext_id"], 
                                    result["title"].replace("'", ""), 
                                    get_title_embeddings(result["title"]),
                                    get_field_value_with_subkey (result, "countries", "code"),
                                    get_field_value_with_subkey(result, "cities", "name"),
                                    get_field_value_with_subkey(result, "states", "code"),
                                    result["published"],
                                    result["description"].replace("'", ""),
                                    result["application_url"], 
                                    result["has_remote"], 
                                    get_field_real_value(result,"salary_min"), 
                                    get_field_real_value(result,"salary_max"), 
                                    get_field_value_with_subkey(result,"types","name")
                                    )

    session = Session()
    ls_cols = [ "external_id", "title", "title_embeddings", "location_country", "location_city", "location_state_or_region", 
               "published", "description", "apply_url", "is_remote", "salary_min", "salary_max", "type"]
    ls_vals = [str(record)]
    s_cols = ', '.join(ls_cols)
    s_vals = '(' + '), ('.join(ls_vals) + ')'
    query = f"INSERT INTO positions ({s_cols}) VALUES {s_vals}"
    #print(query)
    res = session.execute(text(query))
    #print(str(res))
    session.commit()

def update_embeding(result):
    embedding = get_title_embeddings(result["title"])
    session = Session()
    query = f"UPDATE positions SET title_embeddings=? WHERE external_id = '{result["ext_id"]}';"
    res = session.execute(text(query), (embedding) )
    session.commit()


def exists_in_db(result):
    sql_expression = text(f"SELECT * FROM positions WHERE external_id = '{result["ext_id"]}'")
    session = Session()
    results = session.execute(sql_expression )
    for record in results:
        return record
                     
def load_and_save_file(full_file_path):
    with open(full_file_path) as json_data:
        try:
            data = json.load(json_data)
            print(str(data["count"]) + " records in request " + full_file_path)
            count = 0
            for result in data["results"]:
                if get_field_value_with_subkey (result, "countries", "code") == 'US':
                    if exists_in_db(result) == None:
                        save_to_db(result)
                        count = count + 1
        except Exception as e:
            print(e)
    print(str(count) + " records were been saved from file " + full_file_path)

parser = argparse.ArgumentParser()
parser.add_argument("--inputfilename", help="input file name to load to db", default="")
parser.add_argument("--inputfolder", help="input directory to store files in db", default="")
parser.add_argument("dbhost", help="connection string to postgres", default="")
parser.add_argument("dbuser", help="connection user to postgres", default="")
parser.add_argument("dbpassword", help="connection password to postgres", default="")
args = parser.parse_args()

if (len(args.inputfolder) == 0 and len(args.inputfilename) == 0 ):
    print("Provide input parameters inputfilename or inputfolder")
    exit(1)

dbconn = f"postgresql+psycopg2://{args.dbuser}:{args.dbpassword}@{args.dbhost}:5432/ostjdb"
# Create a database engine
engine = create_engine(dbconn)
Session = sessionmaker(bind=engine)

if len(args.inputfolder) != 0:
    if (not os.path.exists(args.inputfolder)):
        print("Directory " + args.inputfolder + " does not exist")
        exit(1)
    for subdir, dirs, files in os.walk(args.inputfolder):
        for file in files:
            print ("Read " + os.path.join(subdir, file))
            if file.endswith((".json")):
                full_file_path = os.path.join(subdir,file)
                load_and_save_file(full_file_path)
                
else:
    if (not os.path.isfile(args.inputfilename)):
        print("File " + args.inputfilename + " does not exist")
        exit(1)
    if args.inputfilename.endswith((".json")):
        load_and_save_file(args.inputfilename)