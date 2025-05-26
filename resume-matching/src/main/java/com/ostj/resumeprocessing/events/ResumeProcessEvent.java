package com.ostj.resumeprocessing.events;

public class ResumeProcessEvent {

    public String resumeFilePath="";
    public String jdFilePath="";
    public String promptFilePath = null;
    public int PersonId = 0;
    public String JobId = "";
    public int PromptId = 0;
    
    public ResumeProcessEvent(int PersonId, String JobId, int PromptId, String promptFilePath) {
        this.JobId = JobId;
        this.PersonId = PersonId;
        this.PromptId = PromptId;
        this.promptFilePath = promptFilePath;
    }

    public String toString(){
        return "resumeFilePath="+resumeFilePath+", PersonId="+PersonId+", jdFilePath="+jdFilePath+", JobId="+JobId+", promptFilePath="+promptFilePath+", PromptId="+PromptId;
    }
}
