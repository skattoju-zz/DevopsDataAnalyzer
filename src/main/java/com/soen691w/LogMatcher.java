package com.soen691w;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMatcher {

    HashMap<String, ArrayList<String>> templates = new HashMap<>();
    ArrayList<String> matches = new ArrayList<>();
    HashMap<String, String> eids = new HashMap<>();
    
    int eidSeq = 1;
    int unclassified = 0;
    int templateClassification = 0;
    int fileClassification = 0;

    public void classifyLogs(){
        loadTemplates();
        processLogs();
        printClassifiedLog();
        printTemapltesEidMapping();
        printStatistics();
    }

    private void loadTemplates()
    {
        String fileName = Main.logTemplateFile;
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // vals[0] is fileName, vals[1] is template.
                    String[] vals = line.split(";;;");
                    if (templates.containsKey(vals[0])) {
                        templates.get(vals[0]).add(vals[1]);
                    } else {
                        ArrayList<String> l = new ArrayList<>();
                        l.add(vals[1]);
                        templates.put(vals[0], l);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (String k: templates.keySet()){
                ArrayList<String> arr = templates.get(k);
                arr.sort((s1, s2) -> s2.length() - s1.length());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void processLogs()
    {
        try (BufferedReader br = new BufferedReader(new FileReader(Main.logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLogLine(line);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processLogLine(String line)
    {
        try{
            String packagePath = line.substring(line.indexOf("] "), line.indexOf(": "));
            String fileName = packagePath.substring(packagePath.lastIndexOf(".")+1);
            fileName = fileName + ".java";
            boolean isTemplateMatch = false;
            String matchedTemplate = "";
            if(templates.containsKey(fileName)){
                for (String template : templates.get(fileName))
                {
                    // if the line matches a template, get/create the eid and append to the line [classify the line].
                    if(template.contains("~~")){
                         boolean isFullMatch = true;
                         String[] parts = template.split("~~");
                         for(String part: parts){
                            if(!line.contains(part)) {
                                isFullMatch = false;
                                break;
                            }
                         }
                         if(isFullMatch){
                             matchedTemplate = template;
                             isTemplateMatch = true;
                             break;
                         }
                    }
                    else {
                        if (line.contains(template)) {
                            matchedTemplate = template;
                            isTemplateMatch = true;
                            break;
                        }
                    }
                }
                // If no template matches the line then classify using the file name.
                if(isTemplateMatch) {
                    // see if an eid exists for that template otherwise create one.
                    if (!eids.containsKey(fileName + "_" + matchedTemplate)) {
                        String eid = "E_" + ("00000" + eidSeq).substring(("" + eidSeq).length());
                        eids.put(fileName + "_" + matchedTemplate, eid);
                        eidSeq++;
                    }
                    String classifiedLine = eids.get(fileName + "_" + matchedTemplate) + " " + line;
                    this.matches.add(classifiedLine);
                    this.templateClassification++;
                }
                else{
                    if(!eids.containsKey(fileName)){
                        String eid = "O_" + ("00000" + eidSeq).substring(("" + eidSeq).length());
                        eids.put(fileName, eid);
                        eidSeq++;
                    }
                    String classifiedLine = eids.get(fileName) + " " + line;
                    this.matches.add(classifiedLine);
                    this.fileClassification++;
                }
            }
            else
            {
                // We most likely did not find any logging in this file so we are missing it. Only soln is to fix the template generator.
                // Mark the files as unclassified.
                String cLine = "UNKNOWN " + line;
                this.matches.add(cLine);
                this.unclassified++;
            }
        }catch(Exception e)
        {
            System.out.println("Exception for Line: " + line + " Exception: " + e.getMessage());
        }
    }

    private void printClassifiedLog(){
        Path file = Paths.get(Main.classifiedLogsFile);
        try {
            if (Files.exists(file)) {
                Files.delete(file);
            }

            Files.createFile(file);
            Files.write(file, matches, StandardOpenOption.APPEND);
        }
        catch(Exception ie){
            ie.printStackTrace();
        }
    }

    private void printTemapltesEidMapping(){
        Path file = Paths.get(Main.templateEidMappingsFile);
        try {
            if (Files.exists(file)) {
                Files.delete(file);
            }

            Files.createFile(file);

            ArrayList<String> mappings = new ArrayList<>();
            for(String template : this.eids.keySet())
                mappings.add(eids.get(template) + " " + template);

            Files.write(file, mappings, StandardOpenOption.APPEND);
        }catch(Exception ie){
            ie.printStackTrace();
        }
    }

    private void printStatistics(){
        Path file = Paths.get(Main.statsFile);
        try {
            if (Files.exists(file)) {
                Files.delete(file);
            }
            Files.createFile(file);

            ArrayList<String> stats = new ArrayList<>();
            stats.add("Template classifications = " + this.templateClassification);
            stats.add("File classifications = " + this.fileClassification);
            stats.add("Unclassified = " + this.unclassified);

            Files.write(file, stats, StandardOpenOption.APPEND);
        }catch(Exception ie){
            ie.printStackTrace();
        }
    }
}
