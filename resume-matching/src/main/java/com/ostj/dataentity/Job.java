package com.ostj.dataentity;

import java.util.Date;

public class Job {
    public int id;
    public String external_id;
    public String title;
    public String location_country;
    public String location_city;
    public String location_state;
    public Date published;
    public String description;
    public String apply_url;
    public float salary_min;
    public float salary_max;
    public String location_is_remote;
    public String type;
        
    public String toString(){
        return String.format("Id=%d", id )+", ext_id="+external_id+", title="+title+", location="+location_country+", published="+published+", application_url="+apply_url;
    }
}
