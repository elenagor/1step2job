package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;


public class MatcherTest {
	Matcher matcher = new Matcher("EMPY", "http://localhost:8000/v1", "qwen");
	
	@Test
	public void testGetUserInfo() throws Exception {
		String resume = readFile(this.getClass().getClassLoader().getResourceAsStream("Person.txt"));
		String prompt = readFile(this.getClass().getClassLoader().getResourceAsStream("prompt_get_info.txt"));
		String response = matcher.call_openai(resume, "", prompt);
		System.out.println("Response: " + response);
		assertTrue(response.contains("Sergei Azarkhin"));
		assertTrue(response.contains("sergei.azarkhin@gmail.com"));
	}

	private String readFile(InputStream fis) throws Exception {
		return IOUtils.toString(fis, "UTF-8");
	}
}
