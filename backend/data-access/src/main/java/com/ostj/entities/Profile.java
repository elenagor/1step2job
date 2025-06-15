package com.ostj.entities;

import java.util.ArrayList;
import java.util.List;

public class Profile
{
    public int id = -1;
    public int person_id = -1;
    public boolean accept_remote;
    public float salary_min;
    public float salary_max;
    public String location_country;
    public String location_city;
    public String location_state_or_region;
    public String resume = "";
    public List<Job_title> job_titles;

    public Profile(){
        this.job_titles = new ArrayList<Job_title>();
    }

    public String toString(){
        return String.format("Id=%d, PersonId=%s, location=%s %s %s, job_titles count=%d", 
        id, person_id, location_country, location_city, location_state_or_region, job_titles.size() );
    }
}