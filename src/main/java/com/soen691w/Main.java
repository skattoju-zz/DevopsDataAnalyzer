package com.soen691w;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Main {

    public static final String logTemplateFile = "logTemplates.txt";
    public static final String logFile = "log.txt";
    public static final String classifiedLogsFile = "classifiedLogs.txt";
    public static final String templateEidMappingsFile = "templateEidMappings.txt";
    public static final String statsFile = "stats.txt";


    public static void main(String[] args) {

        if (args == null || args.length == 0){
            System.out.println("No arguments provided.");
            printUsage();
            return;
        } else if (args.length == 1 && args[0].equals("--help")){
            printUsage();
            return;
        }

        for (String arg: args) {
            try {
                File FilesToProcess = new File(arg);
                if (FilesToProcess.isFile()) {
                    System.out.println("Processing file "+arg);
                    AnalyzeFiles(parseTextFile(FilesToProcess));
                } else if (FilesToProcess.isDirectory()) {
                    System.out.println("Processing directory "+arg);
                    AnalyzeFiles(getJavaFilesFromDirectory(FilesToProcess));
                } else {
                    System.out.println("Invalid Argument "+arg);
                    printUsage();
                }
            } catch (Exception e){
                System.out.println("Error processing files "+e.getMessage());
            }
        }
    }

    private static void AnalyzeFiles(ArrayList<String> javaFiles){

        for (String javaFile : javaFiles) {
            try{
                LogTemplateGenerator ltg = new LogTemplateGenerator();
                ltg.processFile(javaFile);
            }catch(Exception e){
                System.out.println("Error when analyzing files "+e.getMessage());
            }
        }
        LogTemplateGenerator.printTemplates();

        LogMatcher lm = new LogMatcher();
        lm.classifyLogs();
    }

    /**
     * returns an array of java code names we need to analyze.
     */
    private static ArrayList<String> parseTextFile(File file){
        ArrayList<String> javaFiles = new ArrayList<>();
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(file.toPath())) {
            stream.forEach(javaFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return javaFiles;
    }

    /**
     * returns an array of java file names to analyze.
     */
    private static ArrayList<String> getJavaFilesFromDirectory(File dirName){

        ArrayList<String> javaFiles = new ArrayList<>();
        Path start = Paths.get(dirName.getPath());
        int maxDepth = Integer.MAX_VALUE;
        try (Stream<Path> stream = Files.find(start, maxDepth, (path, attr) ->
                String.valueOf(path).endsWith(".java"))) {
            stream.forEach(file -> javaFiles.add(file.toString()));
        } catch (IOException e) {
            System.out.println("Error reading directory " + dirName + " " + e.getMessage());
        }
        return javaFiles;
    }

    private static void printUsage(){
        System.out.println("--help print this help");
        System.out.println("<file> text file with list of java files to analyze");
        System.out.println("<dir> directory of files to analyze. Only .java files will be analyzed.");
    }
}