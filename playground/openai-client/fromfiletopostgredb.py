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
    def __init__(self,  ext_id, title, title_embeddings, published, description, application_url, remote, type):
        self.external_id = ext_id
        self.title = title
        self.title_embeddings = title_embeddings
        self.published = published
        self.description = description
        self.apply_url = application_url
        self.location_is_remote = remote
        self.type = type
    def __str__(self):
        return f"'{self.external_id}', '{self.title}', '{self.title_embeddings}', '{self.published}', '{self.description}', '{self.apply_url}', '{self.location_is_remote}', '{self.type}'"

def html_to_text(html_content):  
    text_maker = html2text.HTML2Text()
    text_maker.ignore_links = True
    return text_maker.handle(html_content).replace("\n", " ").replace("*", "").replace("#", "").replace("-", "")

def get_title_embeddings(title):
    title_embedding = ostj.get_embedding(title)
    print(title_embedding)
    return title_embedding

def save_to_db(result):
    record = Job_Description_Record(
                                    result["ext_id"], 
                                    result["title"], 
                                    get_title_embeddings(result["title"]),
                                    result["published"],
                                    html_to_text(result["description"]),
                                    result["application_url"], 
                                    result["has_remote"], 
                                    result["types"][0]["name"]
                                    )
    Session = sessionmaker(bind=engine)
    session = Session()

    s_tb_name = "public.jobs"
    ls_cols = [ "external_id", "title", "title_embeddings", "published", "description", "apply_url", "location_is_remote", "type"]
    ls_vals = [str(record)]
    s_cols = ', '.join(ls_cols)
    s_vals = '(' + '), ('.join(ls_vals) + ')'
    query = f"INSERT INTO {s_tb_name} ({s_cols}) VALUES {s_vals}"
    print(query)
    print(str(session.execute(text(query))))
    session.commit()

def exists_in_db(result):
    sql_expression = text(f"SELECT * FROM public.jobs WHERE external_id = '{result["ext_id"]}'")
    Session = sessionmaker(bind=engine)
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
                if exists_in_db(result) == None:
                    save_to_db(result)
                    count = count + 1
                else:
                    print(f"Record {result["ext_id"]} exists")
        except Exception as e:
            print(e)
    print(str(count) + " records were been saved from file " + full_file_path)

parser = argparse.ArgumentParser()
parser.add_argument("--inputfilename", help="input file name to load to db", default="")
parser.add_argument("--inputfolder", help="input directory to store files in db", default="")
parser.add_argument("dbconnection", help="connection string to postgre db. exsample:postgresql+psycopg2://your_username:your_password@localhost:5432/your_database", default="")
args = parser.parse_args()

if (len(args.inputfolder) == 0 and len(args.inputfilename) == 0 ):
    print("Provide input parameters inputfilename or inputfolder")
    exit(1)

# Create a database engine
engine = create_engine(args.dbconnection)

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