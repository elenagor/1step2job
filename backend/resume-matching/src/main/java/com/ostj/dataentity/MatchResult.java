package com.ostj.dataentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class MatchResult {
    public int Id = -1;
    public int Person_Id = -1;
    public int Profile_Id = -1;
    public int Position_Id = -1;
    public int overall_score = 0;
    public Date date;
    public String Reasoning = "";
    public List<Alignment> key_arias_of_comparison = new ArrayList<Alignment>();
        
    public String toString(){
        return String.format("Id=%d, PersonId=%d, ProfileId=%d, Position_Id=%d, overall_score=%d, count of key_arias_of_comparison=%d", Id , Person_Id, Profile_Id, Position_Id, overall_score, key_arias_of_comparison.size());
    }
}
