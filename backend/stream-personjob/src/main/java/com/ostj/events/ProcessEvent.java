package com.ostj.events;

public class ProcessEvent {
    public int PersonId = -1;
    public int ProfileId = -1;
    public int PositionId = -1;
    public int PromptId = -1;

    public ProcessEvent() {

    }

    public ProcessEvent(int person_id, int profile_id, int position_id, int prompt_id) {
        PersonId = person_id;
        ProfileId = profile_id;
        PositionId = position_id;
        PromptId = prompt_id;
    }

    public String toString(){
        return String.format("PersonId=%d, ProfileId=%d, PositionId=%d, PromptId=%d", PersonId, ProfileId, PositionId, PromptId);
    }

    public boolean equals(ProcessEvent other){
        if(PersonId == other.PersonId && ProfileId == other.ProfileId && PositionId == other.PositionId){
            return true;
        }
        return false;
    }
}
