package com.ostj.entities;

import java.sql.Timestamp;

public class Position {
    public int id = -1;
    public String external_id;
    public String title;
    public String location_country;
    public String location_city;
    public String location_state;
    public Timestamp published;
    public String description;
    public String apply_url;
    public float salary_min;
    public float salary_max;
    public boolean location_is_remote;
    public String type;
        
    public String toString(){
        return String.format("Id=%d", id )+", ext_id="+external_id+", title="+title+", location="+location_country+", published="+published+", location_is_remote="+location_is_remote+", type="+type;
    }

}
