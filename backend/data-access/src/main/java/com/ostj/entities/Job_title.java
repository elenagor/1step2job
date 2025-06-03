package com.ostj.entities;

import java.sql.Array;

public class Job_title {
    public int id = -1;
    public int profile_id = -1;
    public boolean is_user_defined;
    public String title;
    public Array embeddings;

    public String toString(){
        return String.format("Id=%d, profile_id=%s, Title=%s, is_user_defined=%d", id, profile_id, title, is_user_defined );
    }
}
