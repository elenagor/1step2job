package com.ostj.entities;


import java.util.ArrayList;
import java.util.List;


public class MatchResultNotify {
    public String person_name;
    public List<MatchPosition> positionList = new ArrayList<MatchPosition>();


    public String toString(){
        return String.format("person_name=%s, count of match position=%d", person_name, positionList.size());
    }
}
