package com.ostj.dataaccess;


import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.convertors.DataMapper;
import com.ostj.entities.Job_title;
import com.ostj.entities.MatchResultNotify;
import com.ostj.entities.Person;
import com.ostj.entities.Position;
import com.ostj.entities.Profile;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class MatchResultsNotifyBulder {
    private static Logger log = LoggerFactory.getLogger(MatchResultsNotifyBulder.class);
	private SQLAccess dbConnector;
    static Configuration cfg;
	static {
		cfg = new Configuration(Configuration.VERSION_2_3_29);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setClassForTemplateLoading(MatchResultsNotifyBulder.class, "/templates/");
	}

    public MatchResultsNotifyBulder(){
        log.debug("Start MatchResultsNotifyBulder");
    }

    public MatchResultsNotifyBulder(String jdbcUrl, String username, String password) throws Exception{
        this.dbConnector = new SQLAccess(jdbcUrl, username, password);
    }

    public String createEmailBody( Person person, int overall_score_treshhold, List<MatchResultNotify> resultList ) throws Exception{
        String query = "SELECT person_position_matches.id, score, is_sent, persons.name, positions.title, positions.apply_url, positions.published " + //
                        ",positions.location_country, positions.location_state, positions.location_city " + //
                        ",positions.type, positions.location_is_remote, positions.salary_min, positions.salary_max " +//
                        "FROM person_position_matches " + //
                        "JOIN persons ON persons.id = person_id " + //
                        "JOIN positions ON positions.id = position_id " + //
                        "WHERE is_sent = false and person_id = ? and score > ? ;" ;
        List<Object> parameters = Arrays.asList( person.id , overall_score_treshhold);
        List<Map<String, Object>> res = dbConnector.query(query, parameters);
        if(res != null){
            for (Map<String, Object> rs : res) {
                MatchResultNotify result = new MatchResultNotify();
                result.id = (int)rs.get("id");
                result.person_name = (String)rs.get("name");
                Position matchPosition = new Position();
                DataMapper.convertToObject( rs, matchPosition, matchPosition.getClass() );
                result.position = matchPosition;
                resultList.add(result);
            }
        }
        if(resultList.size() > 0){
            return createEmailBody(  person,  resultList );
        }
        else{
            return createEmailBody(person) ;
        }
    }

    public String createEmailBody( Person person ) throws Exception{
        Template template = cfg.getTemplate("no-result-email-template.ftlh");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("person_name", person.name);
        data.put("profiles", createProfileMapKeyValues(person.profiles)) ;
        Writer out = new StringWriter();
		template.process(data, out);
        return out.toString();
    }

    private List<Map<String, String>> createProfileMapKeyValues(List<Profile> profiles) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for(Profile profile : profiles){
            for(Job_title title : profile.job_titles){
                Map<String, String> map = new HashMap<>();
                map.put("title", title.title);
                list.add(map);
            }
        }
        return list;
    }

    public String createEmailBody( Person person, List<MatchResultNotify> resultList ) throws Exception{
        Template template = cfg.getTemplate("match-result-email-template.ftlh");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("person_name", person.name);
        data.put("details", createMatchPositionMapKeyValues(resultList)) ;
        Writer out = new StringWriter();
		template.process(data, out);
        return out.toString();
    }

    private List<Map<String, String>> createMatchPositionMapKeyValues(List<MatchResultNotify> results) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for(MatchResultNotify result : results){
            Map<String, String> map = new HashMap<>();
            map.put("title", result.position.title);
            map.put("published_date", result.position.published.toString());
            map.put("apply_url", result.position.apply_url);
            map.put("location", String.format("%s %s %s", result.position.location_country, result.position.location_state_or_region, result.position.location_city));
            map.put("type", result.position.type);
            map.put("is_remote", String.format("%b", result.position.is_remote));
            map.put("salary_min", String.format("$%.2f",result.position.salary_min));
            map.put("salary_max", String.format("$%.2f",result.position.salary_max));
            list.add(map);
        }
        return list;
    }
}
