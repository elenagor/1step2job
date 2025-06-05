package com.ostj.convertors;

import java.lang.reflect.Field;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataMapper {
    private static Logger log = LoggerFactory.getLogger(DataMapper.class);
    private static final List<String> dateUTCPatterns = Arrays.asList("yyyyMMddHHmmssZ", "yyyyMMddHHmmZ","yyyyMMddHHmmss", "yyyyMMddHHmm",
			"MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm", "yyyy-MM-dd HH:mm:ss", "yyyyMMdd", "MM/dd/yyyy", "MM-dd-yyyy");
    private static List<SimpleDateFormat> dateUTCFormats = createSimpleDateFormatList(dateUTCPatterns);

    private static List<SimpleDateFormat> createSimpleDateFormatList(List<String> patterns) {
		ArrayList<SimpleDateFormat> formats = new ArrayList<>();
		patterns.forEach(pattern -> formats.add(createSimpleDateFormat(pattern)));
		return formats;
	}
    private static SimpleDateFormat createSimpleDateFormat(String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern);
		df.setLenient(false);
		df.setTimeZone(TimeZone.getTimeZone("UTC")); // should be tenant timezone
		return df;
	}

    public static <T> void convertToObject(Map<String, Object> rs, T obj, Class<?> classType) throws Exception{
        for(Field fieldname : classType.getDeclaredFields()){
            setValue( fieldname, obj, rs);
        }
    }
    private static <T> void setValue(Field fieldname, T obj, Map<String, Object> rs){
        try{
            if(rs.containsKey(fieldname.getName().toLowerCase()) && rs.get(fieldname.getName().toLowerCase()) != null){
                fieldname.setAccessible(true);
                if(fieldname.getType() == String.class){
                    fieldname.set(obj, (String)rs.get(fieldname.getName().toLowerCase()) );
                }
                if(fieldname.getType() == int.class){
                    fieldname.set(obj, (int)rs.get(fieldname.getName().toLowerCase()) );
                }
                if(fieldname.getType() == boolean.class){
                    fieldname.set(obj,  (Boolean)rs.get(fieldname.getName().toLowerCase()));
                }
                if(fieldname.getType() == float.class){
                    fieldname.set(obj, Float.parseFloat((String)rs.get(fieldname.getName().toLowerCase()) ));
                }
                if(fieldname.getType() == Date.class){
                    fieldname.set(obj, getDate( (String)rs.get(fieldname.getName().toLowerCase()) ) );
                }
            }
        }
        catch(Exception e){
            log.error("set field error {}", e);
        }
    }
    private static Date getDate(String dateStr) throws ParseException{
        for (SimpleDateFormat frmt : dateUTCFormats) {
			try {
				return frmt.parse(dateStr);
			} catch (Exception e) {
				//log.trace("failed with formatter  "+ frmt.toPattern() + " for date " + dateStr, e);
			}
		}
		throw new ParseException("cannot parse date " + dateStr, 0);
    }
}
