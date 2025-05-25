package com.ostj.dataentity;

public class Alignment {
    public int Id;
    public String title;
    public int score;
    public String alignment;
        
    public String toString(){
        return String.format("Id=%d, title=%s, score=%d, alignment=%s", Id , title, score, alignment);
    }
}
