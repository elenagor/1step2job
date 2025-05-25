package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.dataaccess.SQLAccess;
import com.ostj.dataentity.Job;
import com.ostj.dataentity.MatchResult;
import com.ostj.dataentity.Person;

public class SQLAccessTest {
    private static Logger log = LoggerFactory.getLogger(SQLAccessTest.class);

	@Test
	public void testGetUserInfo() throws Exception {
		SQLAccess dbConnector = new SQLAccess("jdbc:postgresql://localhost:5432/ostjdb", "ostjuser", "ostjuser!");
		List<Person> persons = dbConnector.getPersonData(1);
		assertTrue(persons != null);
		assertTrue(persons.size() > 0);
		assertTrue(persons.get(0).resumes != null);
		assertTrue(persons.get(0).resumes.size() != 0);
		log.trace("Response: {}", persons.get(0).resumes.get(0).Content);
		assertTrue(persons.get(0).resumes.get(0).PersonId == 1);
	}

	@Test
	public void testJob() throws Exception {
		SQLAccess dbConnector = new SQLAccess("jdbc:postgresql://localhost:5432/ostjdb", "ostjuser", "ostjuser!");
		Job job = dbConnector.getJob("9055748138");
		assertTrue(job != null);
		log.trace("Response: {}", job.description);
	}

	@Test
	public void testInserDeleteResult() throws Exception {
		SQLAccess dbConnector = new SQLAccess("jdbc:postgresql://localhost:5432/ostjdb", "ostjuser", "ostjuser!");
		MatchResult result = new MatchResult();
		result.PersonId = 1;
		result.ResumeId = 3;
		result.JobId = 5;
		result.overall_score = 0;
		int resultId = dbConnector.saveMatchResult(result);
		assertTrue(resultId >= 0);
		dbConnector.deleteMatchResult(resultId);
	}
}
