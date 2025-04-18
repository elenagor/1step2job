#!/usr/bin/env python3
import argparse
from keybert import KeyBERT

parser = argparse.ArgumentParser()
parser.add_argument("file")
args = parser.parse_args()

# Initialize the KeyBERT model
model = KeyBERT("distilbert-base-nli-mean-tokens")

f = open(args.file, "r")
text = f.read()

# Extract keywords
keywords = model.extract_keywords(text)

# Print the keywords
print("Keywords:")
for keyword in keywords:
    print(keyword)