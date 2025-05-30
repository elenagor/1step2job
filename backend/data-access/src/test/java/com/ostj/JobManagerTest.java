package com.ostj;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.entities.Job;
import com.ostj.managers.JobManager;

public class JobManagerTest {
    private static Logger log = LoggerFactory.getLogger(JobManagerTest.class);

    private String jdbcUrl = "jdbc:postgresql://localhost:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";

    @Test
	public void testJob1() throws Exception {
		JobManager jobManager = new JobManager(jdbcUrl, username, password);
		Job job = new Job();
		jobManager.getJobFromDB(1, job);
		log.trace("Response: {}", job.toString());
		assertTrue(job.description != null);
	}
	@Test
	public void testJob2() throws Exception {
		JobManager jobManager = new JobManager(jdbcUrl, username, password);
		Job job = new Job();
		jobManager.getJobFromDB("9055748138", job);
		log.trace("Response: {}", job.toString());
		assertTrue(job.description != null);
	}
}
