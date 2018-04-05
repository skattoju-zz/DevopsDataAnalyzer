package com.soen691w;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

public class RegexGenerater {

    private  HashSet<String> regexes = new HashSet<>();

    public void processTemplateFile(String fileName)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                processTemplateString(line);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processTemplateString(String template)
    {
        try{
            template = template.replace("{}", "*");
            template = template.replace("~~", "*");
            template = template.replace("[]", "*");
            template = template.replace("()", "*");
            template = template.trim();
            regexes.add(template);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void printRegexes()
    {
        Path file = Paths.get("D:\\P\\Regexes.txt");
        try {
            if (Files.exists(file)) {
                Files.delete(file);
            }
            else {
                Files.createDirectories(file.getParent());
            }
            Files.createFile(file);
            Files.write(file, regexes, StandardOpenOption.APPEND);
        }catch(Exception ie){
            ie.printStackTrace();
        }
    }
}
