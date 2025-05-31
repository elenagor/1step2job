package com.ostj.resumeprocessing.events;

public class ResumeProcessEvent {

    public String resumeFilePath="";
    public String jdFilePath="";
    public String promptFilePath = "";
    public int PersonId = -1;
    public int ProfileId = -1;
    public int JobId = -1;
    public String JobExtId = "";
    public int PromptId = -1;

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
        return "resumeFilePath="+resumeFilePath+", jdFilePath="+jdFilePath+", promptFilePath="+promptFilePath
        +String.format("PersonId=%d, ProfileId=%d, JobId=%d, PromptId=%d", PersonId, ProfileId, JobId, PromptId);
    }
}
