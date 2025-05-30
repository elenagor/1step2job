package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.dataaccess.PromptManager;
//import com.ostj.dataaccess.ResultManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.dataentity.Alignment;
import com.ostj.dataentity.Result;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;

public class SQLAccessTest {
    private static Logger log = LoggerFactory.getLogger(SQLAccessTest.class);

	private String jdbcUrl = "jdbc:postgresql://localhost:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";

	private ResumeProcessEvent event = new ResumeProcessEvent();
 
	@Test
	public void testGetPrompt() throws Exception {
		SQLAccess dbConnector = new SQLAccess(jdbcUrl, username, password);
		PromptManager jobManager = new PromptManager(dbConnector);
		event.PromptId = 0;
		String prompt = jobManager.getPrompt(event);
		assertTrue(prompt != null);
		log.trace("Response: {}", prompt);
	}

	@Test
	public void testInserDeleteResult() throws Exception {
		//SQLAccess dbConnector = new SQLAccess(jdbcUrl, username, password);
		//ResultManager resultManager = new ResultManager(dbConnector);
		Result result = new Result();
		result.PersonId = 1;
		result.ProfileId = 3;
		result.JobId = 5;
		result.overall_score = 0;
		result.date = new java.util.Date(); // Current date
		result.key_arias_of_comparison.add(new Alignment());
		//int resultId = resultManager.saveMatchResult(result);
		//assertTrue(resultId >= 0);
		//resultManager.deleteMatchResult(resultId);
	}

}
