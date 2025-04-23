import OpenAI from 'openai';

export async function UserProfileByResume(resume) {
    const client = new OpenAI({
        baseURL:"http://localhost:8000/v1",
        apiKey:"EMPTY"
    });
    const roleHR = "You are Human Resoures expert helping to idetify candidate's by resume"
    const prompt = 'Extract email adress and candidate name in format json [<name>,<email>] from Resume:'+ resume;

    const response = await client.responses.create({
        model: 'qwen-2.5',
        messages:[
            {"role": "system", "content": roleHR},
            {"role": "user", "content": prompt}
        ] 
    });
    return(response.output_text); 
}

export async function MatchResumeToJD(resume, jobdescription){
    const client = new OpenAI({
        baseURL:"http://localhost:8000/v1",
        apiKey:"EMPTY"
    });
    const roleHR = "You are Human Resoures expert helping to idetify if candidate's resume matches job description"
    const prompt = 'Extract email adress and candidate name in format json [<name>,<email>] from Resume:"'+ resume + '" Job description:"' + jobdescription + '"';
    
    const response = await client.responses.create({
        model: 'qwen-2.5',
        messages:[
            {"role": "system", "content": roleHR},
            {"role": "user", "content": prompt}
        ] 
    });

    return(response.output_text);
}