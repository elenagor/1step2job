#!/usr/bin/env python3
import os, json
import ostj_client as ostj
from pathlib import Path
from flask import Flask, request, jsonify
import openai as openai_client

app = Flask(__name__)

@app.route('/getuserinfo', methods=['POST'])
def get_user_info():
    params: dict[str, str] = dict()
    data = request.json
    resume_file_name = data.get('resume_file_name', '')
    resume_content = data.get('resume_content', '')
    if not resume_file_name and not resume_content:
        return jsonify({'error': 'resume is not provided'}), 400
    
    try:
        prompt_file = os.path.join( os.path.dirname(os.path.abspath(__file__)), "prompt_get_info.txt")
        prompt = Path(prompt_file).read_text(encoding="UTF-8")
        
        if not resume_file_name:
            params["resume"] = resume_content
        else:
            params["resume"] = Path(resume_file_name).read_text(encoding="UTF-8")

        responce = ostj.run_prompt_internal(prompt, params)
        return jsonify("{"+ str(responce) + "}")
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
