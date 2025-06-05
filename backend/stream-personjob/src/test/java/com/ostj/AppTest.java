package com.ostj;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ostj.dataproviders.PersonProvider;
import com.ostj.dataproviders.PositionProvider;
import com.ostj.entities.Person;
import com.ostj.entities.Position;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private String jdbcUrl = "jdbc:postgresql://localhost:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";

    @Test
    public void calcTrashhold() throws Exception {
        PersonProvider personProvider = new PersonProvider(jdbcUrl, username, password);
        PositionProvider positionProvider = new PositionProvider(jdbcUrl, username, password);
        
        Person person = new Person();
        personProvider.getPersonData(1, 1, person);
		assertTrue(person.id == 1);
		assertTrue(person.profiles != null);
		assertTrue(person.profiles.size() != 0);
		assertTrue(person.profiles.get(0).id == 1);
		assertTrue(person.profiles.get(0).job_titles.size() != 0);

        Position position = new Position();
		positionProvider.getPositionFromDB(1, position);
        assertTrue(position.id == 1);
    }
}
