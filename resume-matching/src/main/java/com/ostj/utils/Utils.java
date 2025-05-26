package com.ostj.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kotlin.text.Charsets;

public class Utils {
        private static Logger log = LoggerFactory.getLogger(Utils.class);

    public static String getPromptByFileName(String resourceName) throws IOException{
        return Files.readString(Path.of(getPromtPath(resourceName)), Charsets.UTF_8);
    }

    private static String getPromtPath(String resourceName) throws IOException{
        return IOUtils.toString(Utils.class.getClassLoader().getResourceAsStream(resourceName),  "UTF-8");
    }

    public static <T> void convertToObject(ResultSet rs, T obj, Class<?> classType) throws Exception{
        for(Field fieldname : classType.getDeclaredFields()){
            setValue( fieldname, obj, rs);
        }
    }
    private static <T> void setValue(Field fieldname, T obj, ResultSet rs){
        try{
            fieldname.setAccessible(true);
            if( fieldname.getType() == String.class ){
                fieldname.set(obj, rs.getString(fieldname.getName().toLowerCase()) );
            } 
            if( fieldname.getType() == int.class ){
                fieldname.set(obj, rs.getInt(fieldname.getName().toLowerCase()) );
            }
        }
        catch(Exception e){
            log.error("set field error {}", e);
        }
    }
}
