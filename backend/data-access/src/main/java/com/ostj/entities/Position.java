package com.ostj.entities;

import java.sql.Timestamp;

public class Position {
    public int id = -1;
    public String external_id;
    public String title;
    public String location_country;
    public String location_city;
    public String location_state_or_region;
    public Timestamp published;
    public String description;
    public String apply_url;
    public float salary_min;
    public float salary_max;
    public boolean is_remote;
    public String type;
        
    public String toString(){
        return String.format("Id=%d", id )
        +", ext_id="+external_id
        +", title="+title
        +", location_country="+location_country 
        +", location_city="+location_city 
        +", location_state_or_region="+location_state_or_region 
        +", published="+published
        +", location_is_remote="+is_remote
        + String.format(", salary_min=%f", salary_min )
        + String.format(", salary_max=%f", salary_max )
        +", type="+type;
        
    }

}
