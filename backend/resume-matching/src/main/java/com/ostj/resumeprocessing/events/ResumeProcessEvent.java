package com.ostj.resumeprocessing.events;

public class ResumeProcessEvent {

    public String resumeFilePath="";
    public String jdFilePath="";
    public String promptFilePath = "";
    public int PersonId = -1;
    public int ProfileId = -1;
    public int PositionId = -1;
    public String PositionExternalId = "";
    public int PromptId = -1;
    public boolean isFinished = false;

    public ResumeProcessEvent(){
        
    }
    
    public ResumeProcessEvent(int PersonId, String PositionExternalId, int PositionId, int PromptId, String promptFilePath) {
        this.PositionId = PositionId;
        this.PositionExternalId = PositionExternalId;
        this.PersonId = PersonId;
        this.PromptId = PromptId;
        this.promptFilePath = promptFilePath;
    }

    public String toString(){
        return "resumeFilePath="+resumeFilePath+", jdFilePath="+jdFilePath+", promptFilePath="+promptFilePath
        +String.format(", PersonId=%d, ProfileId=%d, PositionId=%d, PromptId=%d", PersonId, ProfileId, PositionId, PromptId)
        +", isFinished="+isFinished;
    }
}
