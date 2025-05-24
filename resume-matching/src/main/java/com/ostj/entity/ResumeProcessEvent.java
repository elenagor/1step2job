package com.ostj.entity;

public class ResumeProcessEvent {

    public String resumeFilePath="";
    public String jdFilePath="";
    public String promptFilePath = "prompt.txt";

    public int PersonId = 0;
    
    public String toString(){
        return "resumeFilePath="+resumeFilePath+",jdFilePath="+jdFilePath+",promptFilePath="+promptFilePath;
    }
}
