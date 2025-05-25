package com.ostj.dataentity;

import java.util.ArrayList;
import java.util.List;

public class MatchResult {
    public int Id;
    public int overall_score;
    List<Alignment> key_arias_of_comparison = new ArrayList<Alignment>();
        
    public String toString(){
        return String.format("Id=%d, overall_score=%d, count of key_arias_of_comparison=%d", Id , overall_score, key_arias_of_comparison.size());
    }
}
