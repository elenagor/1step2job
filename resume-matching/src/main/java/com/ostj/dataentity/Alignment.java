package com.ostj.dataentity;

public class Alignment {
    public String title = "";
    public int score = 0;
    public String alignment = "";

        
    public String toString(){
        return String.format("title=%s, score=%d, alignment=%s", title, score, alignment);
    }
}
