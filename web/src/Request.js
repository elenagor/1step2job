import Prompt from './Prompt.js';

export default async function getResponce(resume){
    let prompt = new Prompt(resume);

    const response = await fetch('/getname', {
        method: 'POST',
        headers: {
          'Content-Type': 'plan/text',
        },
        body: { prompt },
      });
  
      if (response.ok) {
        return(response.body);
      } else {
        return('Failed to submit file.');
      }
}