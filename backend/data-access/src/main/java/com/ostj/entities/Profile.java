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
    public String location;
    public String resume = "";
    public List<Job_title> job_titles;

    public Profile(){
        this.job_titles = new ArrayList<Job_title>();
    }

    public String toString(){
        return String.format("Id=%d, PersonId=%s, location=%s, job_titles count=%d, resume=%s", id, person_id, location, job_titles.size(), resume );
    }
}