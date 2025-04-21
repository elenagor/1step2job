import Prompt from './Prompt.js';
import OpenAI from 'openai';

export default async function getResponce(resume){
    let prompt = new Prompt(resume);
    const client = new OpenAI({
            base_url:"http://localhost:8000/v1",
            apiKey:"EMPTY"
      });
      
    const response = await client.responses.create({
        model: 'qwen-2.5',
        messages:[
            {"role": "system", "content": "You are Human Resoures expert helping to idetify candidate's resume"},
            {"role": "user", "content": prompt}
        ] 
      });
  
    return(response.output_text);
}