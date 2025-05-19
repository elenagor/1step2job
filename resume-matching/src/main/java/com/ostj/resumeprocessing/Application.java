package com.ostj.resumeprocessing;

import java.util.*;
import org.apache.commons.cli.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;


@SpringBootApplication
public class Application {

    @Autowired
	Matcher resumeMatcher;

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).run(args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    return args -> {
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            //System.out.println(beanName);
        }
        };
    }

    public void runApp(String[] args) {
        Options options = new Options();

        Option resume_opt = new Option("r", "resume", true, "input resume file path");
        resume_opt.setRequired(true);
        options.addOption(resume_opt);

        Option job_description_opt = new Option("j", "job_description", true, "input job description file path");
        job_description_opt.setRequired(true);
        options.addOption(job_description_opt);

        Option prompt_opt = new Option("p", "prompt", true, "input prompt file path");
        prompt_opt.setRequired(true);
        options.addOption(prompt_opt);

        try{
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String resumeFilePath = cmd.getOptionValue("resume");
            String jdFilePath = cmd.getOptionValue("job_description");
            String promptFilePath = cmd.getOptionValue("prompt");
    
            String response = resumeMatcher.run_resume_matching( resumeFilePath,  jdFilePath,  promptFilePath);
            System.out.println("Response: " + response);
        }
        catch(Exception e){
            System.out.println("Error: " + e.toString());
        }
    }
}
