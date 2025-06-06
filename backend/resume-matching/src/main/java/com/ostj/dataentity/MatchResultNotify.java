package com.ostj.dataentity;

import java.sql.Timestamp;

public class MatchResultNotify {
    public Timestamp published_date;
    public String job_description;
    public int overall_score;
    public String apply_url;

    public String toString(){
        return String.format("published_date=%s, overall_score=%d", published_date, overall_score);
    }
}
