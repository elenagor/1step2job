package com.ostj.convertors;

import java.lang.reflect.Field;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.managers.PersonManager;

public class DataMapper {
    private static Logger log = LoggerFactory.getLogger(DataMapper.class);

    public static <T> void convertToObject(Map<String, Object> rs, T obj, Class<?> classType) throws Exception{
        for(Field fieldname : classType.getDeclaredFields()){
            setValue( fieldname, obj, rs);
        }
    }
    private static <T> void setValue(Field fieldname, T obj, Map<String, Object> rs){
        try{
            if(rs.containsKey(fieldname.getName().toLowerCase()) && rs.get(fieldname.getName().toLowerCase()) != null){
                fieldname.setAccessible(true);
                fieldname.set(obj, rs.get(fieldname.getName().toLowerCase()) );
            }
        }
        catch(Exception e){
            log.error("set field error {}", e);
        }
    }
}
