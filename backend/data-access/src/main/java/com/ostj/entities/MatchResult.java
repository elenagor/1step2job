package com.ostj.entities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class MatchResult {
    public int Id = -1;
    public int Person_Id = -1;
    public int Profile_Id = -1;
    public int Position_Id = -1;
    public int overall_score = 0;
    public String Status = "";
    public String score_explanation = "";
    public Timestamp date;
    public String Reasoning = "";
    public List<Alignment> key_arias_of_comparison = new ArrayList<Alignment>();
    public boolean is_sent = false;
        
    public String toString(){
        return String.format("Id=%d, PersonId=%d, ProfileId=%d, Position_Id=%d, overall_score=%d, date=%s, Status=%s", 
        Id , Person_Id, Profile_Id, Position_Id, overall_score, date.toString(), Status);
    }
}
