package com.ostj.resumeprocessing.events;

public class ResumeProcessEvent {

    public String resumeFilePath="";
    public String jdFilePath="";
    public String promptFilePath = "";
    public int PersonId = 0;
    public int JobId = 0;
    public String JobExtId = "";
    public int PromptId = 0;

    public ResumeProcessEvent(){
        
    }
    
    public ResumeProcessEvent(int PersonId, String JobExtId, int JobId, int PromptId, String promptFilePath) {
        this.JobId = JobId;
        this.JobExtId = JobExtId;
        this.PersonId = PersonId;
        this.PromptId = PromptId;
        this.promptFilePath = promptFilePath;
    }

    public String toString(){
        return "resumeFilePath="+resumeFilePath+", PersonId="+PersonId+", jdFilePath="+jdFilePath+", JobId="+JobId+", promptFilePath="+promptFilePath+", PromptId="+PromptId;
    }
}
