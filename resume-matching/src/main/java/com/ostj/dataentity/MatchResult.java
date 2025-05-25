package com.ostj.dataentity;

import java.util.ArrayList;
import java.util.List;

public class MatchResult {
    public int Id;
    public int PersonId;
    public int ResumeId;
    public int JobId;
    public int overall_score;
    List<Alignment> key_arias_of_comparison = new ArrayList<Alignment>();
        
    public String toString(){
        return String.format("Id=%d, PersonId=%d, ResumeId=%d, JobId=%d, overall_score=%d, count of key_arias_of_comparison=%d", Id , PersonId, ResumeId, JobId, overall_score, key_arias_of_comparison.size());
    }
}
