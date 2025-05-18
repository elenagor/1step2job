package com.ostj.resumeprocessing;

import java.util.*;
import org.apache.commons.cli.*;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import com.azure.ai.openai.*;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
public class App {
    @Value("${ostj.openai.apikey}")
    String apiKey;

    @Value("${ostj.openai.endpoint}")
    String endpoint;

    @Value("${ostj.openai.model}")
    String model;

    public static void main(String[] args) {
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

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try{
            CommandLine cmd = parser.parse(options, args);

            String resumeFilePath = cmd.getOptionValue("resume");
            String jdFilePath = cmd.getOptionValue("job_description");
            String promptFilePath = cmd.getOptionValue("prompt");

            String resume = readFile(resumeFilePath);
            String job_description = readFile(jdFilePath);
            String prompt = readFile(promptFilePath);

            App main = new App();
            String response = main.call_openai( resume,  job_description,  prompt);
            System.out.println("Response: " + response);
        }
        catch (IOException ioe){
            System.out.println("IO Error: " + ioe.toString());
        }
        catch(Exception e){
            System.out.println("Error: " + e.toString());
        }
    }

    private static String readFile(String path) throws Exception{
        FileInputStream fis = new FileInputStream(path);
        return IOUtils.toString(fis, "UTF-8");
    }

    public String call_openai( String resume, String job_description, String prompt) throws Exception {
        String user_content = String.format("Resume:%s\nJob Description:%s", resume, job_description);

        System.out.printf("apiKey="+apiKey+",endpoint="+endpoint+",model="+model+"\n");

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new KeyCredential(apiKey))
                .buildClient();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage(prompt));
        chatMessages.add(new ChatRequestUserMessage(user_content));
        
        ChatCompletions chatCompletions = client.getChatCompletions(model, new ChatCompletionsOptions(chatMessages));

        StringBuffer sb = new StringBuffer();
        for (ChatChoice choice : chatCompletions.getChoices()) {
            sb.append(choice.getMessage().getContent());
        }

        //CompletionsUsage usage = chatCompletions.getUsage();
        //System.out.printf("Usage: number of prompt token is %d, "
        //                + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
        //        usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
 
        return sb.toString();
    }

}
