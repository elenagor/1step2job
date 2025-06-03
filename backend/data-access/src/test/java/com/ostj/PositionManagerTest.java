package com.ostj;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.entities.Position;
import com.ostj.managers.PositionManager;

public class PositionManagerTest {
    private static Logger log = LoggerFactory.getLogger(PositionManagerTest.class);

    private String jdbcUrl = "jdbc:postgresql://localhost:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";

    @Test
	public void testJob1() throws Exception {
		PositionManager jobManager = new PositionManager(jdbcUrl, username, password);
		Position job = new Position();
		jobManager.getJobFromDB(1, job);
		log.trace("Response: {}", job.toString());
		assertTrue(job.description != null);
	}
	@Test
	public void testJob2() throws Exception {
		PositionManager jobManager = new PositionManager(jdbcUrl, username, password);
		Position job = new Position();
		jobManager.getJobFromDB("9055748138", job);
		log.trace("Response: {}", job.toString());
		assertTrue(job.description != null);
	}
}
