package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;


public class MatcherTest {
	Matcher matcher = new Matcher("EMPY", "http://localhost:8000/v1", "qwen");
	
	@Test
	public void testGetUserInfo() throws Exception {
		String resume = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("Person.txt"),  "UTF-8");
		String prompt =  IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("prompt_get_info.txt"),  "UTF-8");
		String response = matcher.call_openai(resume, "", prompt);
		System.out.println("Response: " + response);
		assertTrue(response.contains("Sergei Azarkhin"));
		assertTrue(response.contains("sergei.azarkhin@gmail.com"));
	}
}
