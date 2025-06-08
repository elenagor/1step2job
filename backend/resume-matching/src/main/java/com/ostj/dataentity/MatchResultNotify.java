package com.ostj.dataentity;

import java.sql.Timestamp;
import java.util.List;

public class MatchResultNotify {
    public String person_name;
    public String job_title;
    public Timestamp published_date;
    public int overall_score;
    public String apply_url;
    public String job_description;
    public String explanation_score;
    public List<Alignment> key_arias_of_comparison ;

    public String toString(){
        return String.format("published_date=%s, overall_score=%d", published_date, overall_score);
    }
}
