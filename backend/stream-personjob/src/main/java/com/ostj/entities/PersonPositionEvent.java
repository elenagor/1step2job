package com.ostj.entities;

public class PersonPositionEvent {
    public int PersonId = -1;
    public int ProfileId = -1;
    public int PositionId = -1;
    public int PromptId = -1;
    public Boolean isFinished = false;

    public PersonPositionEvent() {

    }

    public PersonPositionEvent(int person_id, int profile_id, int position_id, int prompt_id, Boolean _isFinished) {
        PersonId = person_id;
        ProfileId = profile_id;
        PositionId = position_id;
        PromptId = prompt_id;
        isFinished = _isFinished;
    }

    public String toString(){
        return String.format("PersonId=%d, ProfileId=%d, PositionId=%d, PromptId=%d, isFinished=%b", PersonId, ProfileId, PositionId, PromptId, isFinished);
    }

    public boolean equals(PersonPositionEvent other){
        if(PersonId == other.PersonId && ProfileId == other.ProfileId && PositionId == other.PositionId){
            return true;
        }
        return false;
    }
}
