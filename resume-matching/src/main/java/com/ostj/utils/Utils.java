package com.ostj.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static <T> void convertToObject(Map<String, Object> rs, T obj, Class<?> classType) throws Exception{
        for(Field fieldname : classType.getDeclaredFields()){
            setValue( fieldname, obj, rs);
        }
    }
    private static <T> void setValue(Field fieldname, T obj, Map<String, Object> rs){
        try{
            fieldname.setAccessible(true);
            if( fieldname.getType() == String.class ){
                fieldname.set(obj, rs.get(fieldname.getName().toLowerCase()) );
            } 
            if( fieldname.getType() == int.class ){
                fieldname.set(obj, rs.get(fieldname.getName().toLowerCase()) );
            }
        }
        catch(Exception e){
            log.error("set field error {}", e);
        }
    }

    public static String getThinksAsText(String text) {
        Pattern compiledPattern = Pattern.compile("<think>(.+)</think>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = compiledPattern.matcher(text);
        if (matcher.find()) {
           return matcher.group(1);
        }
        return "No Thinks";
    }

    public static String getJsonContextAsString(String text) {

        Pattern compiledPattern = Pattern.compile("^[^{]*(\\{.*\\})[^}]*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = compiledPattern.matcher(text);
        if (matcher.find()) {
           return matcher.group(1);
        }
        return "{\"Error\":\"Json is invalid\"}";
    }
}
