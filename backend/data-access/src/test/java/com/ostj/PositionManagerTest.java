package com.ostj;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.dataproviders.PositionProvider;
import com.ostj.entities.Position;

public class PositionManagerTest {
    private static Logger log = LoggerFactory.getLogger(PositionManagerTest.class);

    private String jdbcUrl = "jdbc:postgresql://db.1step2job.ai:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";

    @Test
	public void testPosition1() throws Exception {
		PositionProvider positionProvider = new PositionProvider(jdbcUrl, username, password);
		Position position = new Position();
		positionProvider.getPositionFromDB(58, position);
		log.trace("Response: {}", position.toString());
		assertTrue(position.description != null);
	}
	@Test
	public void testPosition2() throws Exception {
		PositionProvider positionProvider = new PositionProvider(jdbcUrl, username, password);
		Position position = new Position();
		positionProvider.getPositionFromDB("1adaf2aa-d8e9-4585-a719-44f259b3379e", position);
		log.trace("Response: {}", position.toString());
		assertTrue(position.description != null);
	}
}
