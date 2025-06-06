package com.ostj.dataaccess;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import com.google.gson.Gson;
import com.ostj.dataentity.MatchResult;
import com.ostj.dataentity.MatchResultNotify;
import com.ostj.entities.Person;
import com.ostj.entities.Position;

public class ResultManager {
    private static Logger log = LoggerFactory.getLogger(ResultManager.class);
    Gson gson = new Gson();
	static Configuration cfg;
	static {
		cfg = new Configuration(Configuration.VERSION_2_3_29);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setClassForTemplateLoading(ResultManager.class, "/templates/");
	}

    @Autowired
	SQLAccess dbConnector;

    public ResultManager(){
        log.debug("Start ResultManager");
    }

    public ResultManager(SQLAccess dbConnector){
        this.dbConnector = dbConnector;
    }

    public int saveMatchResult(MatchResult result) throws Exception {
        String insertQuery = "INSERT INTO person_position_matches(person_id, profile_id, position_id, score, date, reasoning, comparison_details)VALUES (?, ?, ?, ?, ?, ?, ? ::json);";

        java.sql.Date sqlDate = new java.sql.Date(result.date.getTime());
        String details = gson.toJson(result.key_arias_of_comparison);

        List<Object> parameters = Arrays.asList( result.Person_Id, result.Profile_Id, result.Position_Id, result.overall_score, sqlDate, result.Reasoning, details );

        return dbConnector.update( insertQuery, parameters);
    }

    public String createEmailBody(MatchResult result, Person person, Position position) throws Exception{
        Template template = cfg.getTemplate("match-result-email-template.ftlh");
        MatchResultNotify resultNotify = new MatchResultNotify();
        resultNotify.published_date = position.published;
        resultNotify.overall_score = result.overall_score;
        resultNotify.apply_url = position.apply_url;
        resultNotify.job_description = position.description;
        Map<String, MatchResultNotify> data = new HashMap<String, MatchResultNotify>();
		data.put("resultNotify", resultNotify);
        Writer out = new StringWriter();
		template.process(data, out);
        return out.toString();
    }

    public void deleteMatchResult(int resultId) throws Exception {
        String query = "DELETE FROM person_position_matches WHERE id = ? ;";
        log.debug("Start query DB: {}", query);

        List<Object> parameters = Arrays.asList( resultId );
        dbConnector.update(query, parameters) ;
    }
}
