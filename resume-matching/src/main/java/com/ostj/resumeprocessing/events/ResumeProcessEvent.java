package com.ostj.resumeprocessing.events;

public class ResumeProcessEvent {

    public String resumeFilePath="";
    public String jdFilePath="";
    public String promptFilePath = "prompt.txt";
    public int PersonId = 0;
    public String JobId = "";
    
    public String toString(){
        return "resumeFilePath="+resumeFilePath+", PersonId="+PersonId+", jdFilePath="+jdFilePath+", JobId="+JobId+", promptFilePath="+promptFilePath;
    }
}
