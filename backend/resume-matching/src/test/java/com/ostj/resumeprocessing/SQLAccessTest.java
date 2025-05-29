package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.dataaccess.JobManager;
import com.ostj.dataaccess.PersonManager;
import com.ostj.dataaccess.PromptManager;
import com.ostj.dataaccess.ResultManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.dataentity.Alignment;
import com.ostj.dataentity.Job;
import com.ostj.dataentity.Result;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.dataentity.Person;

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
	public void testGetUserInfo1() throws Exception {
		SQLAccess dbConnector = new SQLAccess(jdbcUrl, username, password);
		PersonManager personManager = new PersonManager(dbConnector);
		event.PersonId = 1;
		Person person = personManager.getPersonData(event);
		assertTrue(person != null);
		assertTrue(person.profiles != null);
		assertTrue(person.profiles.size() != 0);
		log.trace("Response: {}", person.toString());
		assertTrue(person.profiles.get(0).person_id == 1);
	}

	@Test
	public void testGetUserInfo2() throws Exception {
		SQLAccess dbConnector = new SQLAccess(jdbcUrl, username, password);
		PersonManager personManager = new PersonManager(dbConnector);
		event.PersonId = 1;
		event.ProfileId = 1;
		Person person = personManager.getPersonData(event);
		assertTrue(person != null);
		assertTrue(person.id == 1);
		assertTrue(person.profiles != null);
		assertTrue(person.profiles.size() != 0);
		log.trace("Response: {}", person.toString());
		assertTrue(person.profiles.get(0).person_id == 1);
		log.trace("Response: {}", person.profiles.get(0).toString());
	}
	@Test
	public void testJob1() throws Exception {
		SQLAccess dbConnector = new SQLAccess(jdbcUrl, username, password);
		JobManager jobManager = new JobManager(dbConnector);
		event.JobId = 1;
		Job job = jobManager.getJob(event);
		assertTrue(job != null);
		log.trace("Response: {}", job.toString());
	}
	@Test
	public void testJob2() throws Exception {
		SQLAccess dbConnector = new SQLAccess(jdbcUrl, username, password);
		JobManager jobManager = new JobManager(dbConnector);
		event.JobExtId = "9055748138";
		Job job = jobManager.getJob(event);
		assertTrue(job != null);
		log.trace("Response: {}", job.toString());
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
