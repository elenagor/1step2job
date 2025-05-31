package com.ostj.events;

public class ProcessEvent {
    public int PersonId = -1;
    public int ProfileId = -1;
    public int JobId = -1;
    public int PromptId = -1;

    public ProcessEvent() {

    }

    public ProcessEvent(int person_id, int profile_id, int job_id, int prompt_id) {
        PersonId = person_id;
        ProfileId = profile_id;
        JobId = job_id;
        PromptId = prompt_id;
    }

    public String toString(){
        return String.format("PersonId=%d, ProfileId=%d, JobId=%d, PromptId=%d", PersonId, ProfileId, JobId, PromptId);
    }
}
