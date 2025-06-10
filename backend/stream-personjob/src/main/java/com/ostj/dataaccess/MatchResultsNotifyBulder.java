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
import com.ostj.entities.MatchPosition;
import com.ostj.entities.MatchResultNotify;
import com.ostj.entities.Person;

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

    public String createEmailBody( Person person, int overall_score_treshhold ) throws Exception{
        MatchResultNotify result = new MatchResultNotify();
        String query = "SELECT date, score, persons.name, positions.title, positions.apply_url " + //
                        "FROM person_position_matches " + //
                        "JOIN persons ON persons.id = person_id " + //
                        "JOIN positions ON positions.id = position_id " + //
                        "WHERE person_id = ? and score > ? ;" ;
        List<Object> parameters = Arrays.asList( person.id , overall_score_treshhold);
        List<Map<String, Object>> res = dbConnector.query(query, parameters);
        if(res != null){
            for (Map<String, Object> rs : res) {
                result.person_name = (String)rs.get("name");
                MatchPosition matchPosition = new MatchPosition();
                DataMapper.convertToObject( rs, matchPosition, matchPosition.getClass() );
                result.positionList.add(matchPosition);
            }
        }
        return createEmailBody(  person,  result );
    }

    public String createEmailBody( Person person, MatchResultNotify result ) throws Exception{
        Template template = cfg.getTemplate("match-result-email-template.ftlh");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("person_name", person.name);
        data.put("details", getMapKeyValues(result.positionList)) ;
        Writer out = new StringWriter();
		template.process(data, out);
        return out.toString();
    }

    private List<Map<String, String>> getMapKeyValues(List<MatchPosition> matchPositions) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for(MatchPosition position : matchPositions){
            Map<String, String> map = new HashMap<>();
            map.put("title", position.title);
            map.put("published_date", position.date.toString());
            map.put("apply_url", position.apply_url);
            list.add(map);
        }
        return list;
    }
}
