package com.ostj.entity;

public class Job {
    public String ext_id;
    public String title;
    public String location;
    public String published;
    public String description;
    public String application_url;
    public String salary;
    public String remote;
    public String type;
        public String toString(){
        return "ext_id="+ext_id+", title="+title+", location="+location+", published="+published+", application_url="+application_url;
    }
}
