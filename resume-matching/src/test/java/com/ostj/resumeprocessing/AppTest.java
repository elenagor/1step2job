package com.ostj.resumeprocessing;
import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testResumeMatching() {
        try{
            Matcher resumeMatcher = new Matcher();
            String response = resumeMatcher.run_resume_matching( "/mnt/c/1step2job/1step2job/playground/data/Person1/Person.txt",  
            "",  "/mnt/c/1step2job/1step2job/resume-matching/src/main/resources/prompt_get_info.txt");
            System.out.println("Response: " + response);
         }
        catch(Throwable e){
            System.out.println("Error: " + e.toString());
        }
    }
}
