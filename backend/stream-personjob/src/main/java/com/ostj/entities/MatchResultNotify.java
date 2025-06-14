package com.ostj.entities;


public class MatchResultNotify {
    public int id = -1;
    public String person_name;
    public Position position;

    public String toString(){
        return String.format("person_name=%s, count of match position=%d", person_name, position);
    }
}
