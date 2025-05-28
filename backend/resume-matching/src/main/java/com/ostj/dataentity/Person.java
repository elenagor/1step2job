package com.ostj.dataentity;

import java.util.ArrayList;
import java.util.List;

public class Person {
    public int id = -1;
    public String name = "";
    public String email = "";
    public String phone = "";
    public String city = "";
    public String state = "";
    public List<Profile> profiles;

    public Person(){
        this.profiles = new ArrayList<Profile>();
    }

    public String toString(){
        return String.format("Id=%d", id )+", Name="+name+", Email="+email+", Phone="+phone+", City="+city+", State="+state+", resumes count="+profiles.size();
    }
}
