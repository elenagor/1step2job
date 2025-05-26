package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.dataaccess.JobManager;
import com.ostj.dataaccess.PersonManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.dataentity.Job;
import com.ostj.dataentity.Result;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.dataentity.Person;

public class SQLAccessTest {
    private static Logger log = LoggerFactory.getLogger(SQLAccessTest.class);
	private ResumeProcessEvent event = new ResumeProcessEvent();
	@Test
	public void testGetUserInfo() throws Exception {
		SQLAccess dbConnector = new SQLAccess("jdbc:postgresql://localhost:5432/ostjdb", "ostjuser", "ostjuser!");
		PersonManager personManager = new PersonManager(dbConnector);
		event.PersonId = 1;
		Person person = personManager.getPersonData(event);
		assertTrue(person != null);
		assertTrue(person.resumes != null);
		assertTrue(person.resumes.size() != 0);
		log.trace("Response: {}", person.resumes.get(0).Content);
		assertTrue(person.resumes.get(0).PersonId == 1);
	}

	@Test
	public void testJob() throws Exception {
		SQLAccess dbConnector = new SQLAccess("jdbc:postgresql://localhost:5432/ostjdb", "ostjuser", "ostjuser!");
		JobManager jobManager = new JobManager(dbConnector);
		event.JobId = "9055748138";
		Job job = jobManager.getJob(event);
		assertTrue(job != null);
		log.trace("Response: {}", job.description);
	}

	@Test
	public void testInserDeleteResult() throws Exception {
		//SQLAccess dbConnector = new SQLAccess("jdbc:postgresql://localhost:5432/ostjdb", "ostjuser", "ostjuser!");
		Result result = new Result();
		result.PersonId = 1;
		result.ResumeId = 3;
		result.JobId = 5;
		result.overall_score = 0;
		//int resultId = dbConnector.saveMatchResult(result);
		//assertTrue(resultId >= 0);
		//dbConnector.deleteMatchResult(resultId);
	}
}
