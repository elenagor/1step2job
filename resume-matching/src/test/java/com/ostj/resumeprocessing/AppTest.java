package com.ostj.resumeprocessing;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import io.netty.util.internal.StringUtil;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testGetUserInfo() {
        try{
            Matcher resumeMatcher = new Matcher();
            String resume = readFile(this.getClass().getClassLoader().getResourceAsStream("Person.txt"));
            String prompt = readFile(this.getClass().getClassLoader().getResourceAsStream("prompt_get_info.txt"));
            String response = resumeMatcher.call_openai( resume,  "",  prompt);
            System.out.println("Response: " + response);
         }
        catch(Throwable e){
            System.out.println("Error: " + e.toString());
        }
    }
    @Test
    public void testHttpUserInfo() {
        try{
            Matcher resumeMatcher = new Matcher();
            String resume = readFile(this.getClass().getClassLoader().getResourceAsStream("Person.txt"));
            String prompt = readFile(this.getClass().getClassLoader().getResourceAsStream("prompt_get_info.txt"));
            String response = resumeMatcher.http_call_openai( resume,  "",  prompt);
            System.out.println("Response: " + response);
         }
        catch(Throwable e){
            System.out.println("Error: " + e.toString());
        }
    }

    private  String readFile(InputStream fis) throws Exception {
        return IOUtils.toString(fis, "UTF-8");
    }
}
