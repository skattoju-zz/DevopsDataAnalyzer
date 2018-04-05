package com.soen691w;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMatcher {

    ArrayList<String> templates = new ArrayList<>();
    ArrayList<String> matches = new ArrayList<>();

    public void startMaching(){
        loadTemplates();
        matchTemplates();
    }

    private void loadTemplates()
    {
        String fileName = Main.logTemplateFile;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                templates.add(line);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void matchTemplates()
    {
        for(int i=0; i<templates.size(); i++){
            if(templates.get(i).trim().equals("") || templates.get(i).trim().equals(":") )
                continue;
            matches.clear();
            matches.add("TEMPLATE: ~~~~" + templates.get(i) + "~~~~");
            processLog(i);
            createFile(i);
        }
    }

    private void processLog(int templateIndex){

        try (BufferedReader br = new BufferedReader(new FileReader(Main.logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
               matchTemplate(line, templateIndex);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void matchTemplate(String line, int templateIndex){
        try{
            String template = templates.get(templateIndex);
            if(template.contains("*")){
                /*Pattern p = Pattern.compile(template);
                Matcher m = p.matcher(line);
                if (m.find()){
                    matches.add(line);
                }*/
                boolean match = true;
                String[] s = template.split("~~");
                for (String subString : s) {
                    subString=subString.trim();
                    if(!line.contains(subString))
                        match = false;
                }

                if(match)
                    matches.add(line);

            }else {
                if (line.contains(template)) {
                    matches.add(line);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private void createFile(int i){
        Path file = Paths.get("D:\\P\\logsClassified\\Template_" + i + ".txt");
        try {
            if (Files.exists(file)) {
                Files.delete(file);
            }
            else {
                Files.createDirectories(file.getParent());
            }
            Files.createFile(file);
            Files.write(file, matches, StandardOpenOption.APPEND);
        }catch(Exception ie){
            ie.printStackTrace();
        }
    }
}
