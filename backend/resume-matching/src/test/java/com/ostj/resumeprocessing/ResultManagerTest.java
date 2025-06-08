package com.ostj.resumeprocessing;

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.dataaccess.ResultManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.dataentity.Alignment;
import com.ostj.dataentity.MatchResult;
import com.ostj.entities.Person;
import com.ostj.entities.Position;

public class ResultManagerTest {
    private static Logger log = LoggerFactory.getLogger(ResultManagerTest.class);
	private String jdbcUrl = "jdbc:postgresql://db.1step2job.ai:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";

    @Test
	public void testCreateEmailBody() throws Exception{
        SQLAccess dbConnector = new SQLAccess(jdbcUrl, username, password);
		ResultManager resultManager = new ResultManager(dbConnector);
        MatchResult result = new MatchResult();
		result.overall_score = 0;
        result.score_explanation = "Test";
        Alignment one = new Alignment();
        one.title = "Title 1";
        one.score = 0;
        one.alignment = "Alignment 1";
        result.key_arias_of_comparison.add(one);
		Person person = new Person();
        Position position = new Position();
        position.published = Timestamp.valueOf("2025-06-06 04:00:00");
        position.apply_url = "www.1step2job.ai";
        position.description = "Test";
        String emailBody = resultManager.createEmailBody(result, person, position);
        log.trace("emailBody: {}", emailBody);
    }
}
