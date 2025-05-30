package com.ostj.events;

public class ProcessEvent {
    public int PersonId = -1;
    public int ProfileId = -1;
    public int JobId = -1;
    public int PromptId = -1;

    public String toString(){
        return String.format("PersonId=%d, ProfileId=%d, JobId=%d, PromptId=%d", PersonId, ProfileId, JobId, PromptId);
    }
}
