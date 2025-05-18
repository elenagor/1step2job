package com.firststeptojob.resumeprocessing;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.apache.commons.cli.*;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

public class App {
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

            String response = call_openai( resume,  job_description,  prompt);
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

    public static String call_openai(String resume, String job_description, String prompt) throws Exception {
        String user_content = String.format("Resume:%s\nJob Description:%s", resume, job_description);

        // Create the JSON payload
        JSONObject payload = new JSONObject();
        payload.put("model", "qwen");
        payload.put("messages", new JSONObject[] {
            new JSONObject().put("role", "system").put("content", prompt)
        });
        payload.put("messages", new JSONObject[] {
            new JSONObject().put("role", "user").put("content", user_content)
        });
        String endpoint="http://localhost:8000/v1";
        String apiKey="EMPTY";
        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

        // Send the request
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}
