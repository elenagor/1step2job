package com.ostj.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

import kotlin.text.Charsets;

public class Utils {
    
    public static String getPromptByFileName(String resourceName) throws IOException{
        return Files.readString(Path.of(getPromtPath(resourceName)), Charsets.UTF_8);
    }

    private static String getPromtPath(String resourceName) throws IOException{
        return IOUtils.toString(Utils.class.getClassLoader().getResourceAsStream(resourceName),  "UTF-8");
    }
}
