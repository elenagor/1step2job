#!/usr/bin/env python3
import os
import hashlib
from docx import Document
import spacy
import pytextrank
import html2text

def read_txt_file(file_name):
    f = open(file_name, "r", encoding='utf-8')
    return f.read()

def read_docx(file_path):
    doc = Document(file_path)
    full_text = []
    for paragraph in doc.paragraphs:
        full_text.append(paragraph.text)
    return ' '.join(full_text)

def hash_string_md5(input_string):
    md5_hash = hashlib.md5()
    md5_hash.update(input_string.encode('utf-8'))
    return md5_hash.hexdigest()

def html_to_text(html_content):  
    text_maker = html2text.HTML2Text()
    text_maker.ignore_links = True
    return text_maker.handle(html_content)

def get_sentences(doc):
    sentences = []
    for sentence in doc.sents:
        #print(sentence)
        sentences.append( sentence.text )
    return sentences

def get_phrases(doc):
    phrases = []
    for phrase in doc._.phrases:
        #print(phrase)
        phrases.append( phrase.text )
    return phrases

def get_entities(doc):
    entities = []
    for entity in doc.ents:
        #print(f"{entity.text} {entity.label_}")
        entities.append( entity.text )
    return entities