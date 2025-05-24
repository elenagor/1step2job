package com.ostj.entity;

import java.util.ArrayList;
import java.util.List;

public class Person {
    public int Id = 0;
    public String Name = "";
    public String Email = "";
    public String Phone = "";
    public String City = "";
    public String State = "";
    public List<Resume> resumes = new ArrayList();

    public String toString(){
        return String.format("Id=%d", Id )+", Name="+Name+", Email="+Email+", Phone="+Phone+", City="+City+", State="+State+", resumes count="+resumes.size();
    }

}
