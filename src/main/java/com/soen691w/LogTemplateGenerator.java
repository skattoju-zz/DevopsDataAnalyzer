package com.soen691w;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.metamodel.CompilationUnitMetaModel;
import com.github.javaparser.printer.JsonPrinter;
import sun.management.ThreadInfoCompositeData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class LogTemplateGenerator {

    public static HashMap<String, HashSet<String>> templates = new HashMap<>();
    public Map<String, String> variableToTemplateMap = new HashMap<String, String >();
    private String currentFile = "";


    public void processFile(String filePath)
    {
        try {
            Path pathToJavaFile = Paths.get(filePath);
            this.currentFile= pathToJavaFile.getFileName().toString();
            CompilationUnit cu = JavaParser.parse(pathToJavaFile.toFile());
            parseVariableDeclarationExpression(cu);
            parseAssignmentExpressions(cu);
            parseMethodExpression(cu);

        }
        catch (Exception e)
        {
            System.out.println("Error when processing file "+e.getMessage());
            //e.printStackTrace();
        }
    }

    public void parseVariableDeclarationExpression(CompilationUnit cu){

        List<VariableDeclarationExpr> varDecs = cu.findAll(VariableDeclarationExpr.class);
        for (VariableDeclarationExpr expr : varDecs) {
            try {
                if (expr != null) {
                    String logStr = "";
                    List<StringLiteralExpr> strEx = expr.findAll(StringLiteralExpr.class);
                    if(strEx != null && strEx.size() >0) {
                        for (StringLiteralExpr s : strEx) {
                            logStr = logStr + s.getValue() + "~~";
                        }
                        String targetVar = expr.getVariables().get(0).getName().getIdentifier();
                        variableToTemplateMap.put(targetVar, logStr);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void parseAssignmentExpressions(CompilationUnit cu) {

        List<AssignExpr> assignmentExprs = cu.findAll(AssignExpr.class);
        for (AssignExpr expr : assignmentExprs) {
            try {
                Expression targetExpr = expr.getTarget();
                if (targetExpr != null) {
                    String targetVar = targetExpr.asNameExpr().getName().getIdentifier();
                    String logStr = "";
                    List<StringLiteralExpr> strEx = expr.findAll(StringLiteralExpr.class);
                    if(strEx != null && strEx.size() >0) {
                        for (StringLiteralExpr s : strEx) {
                            logStr = logStr + s.getValue() + "~~";
                        }
                        variableToTemplateMap.put(targetVar, logStr);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Error when parsing assignment expressions "+e.getMessage());
            }
        }
    }

    
    public void parseMethodExpression(CompilationUnit cu)
    {
        List<MethodCallExpr> methodcalls = cu.findAll(MethodCallExpr.class);
        for (MethodCallExpr expr: methodcalls) {
            try {
                Optional<Expression> logexp = expr.getScope();
                Expression exp = logexp.orElse(null);
                if (exp != null) {
                    String identifier = logexp.get().asNameExpr().getName().getIdentifier();
                    if (identifier.equalsIgnoreCase("log")) {
                        String logStr = "";
                        NodeList<Expression> args = expr.getArguments();
                        for (Expression expression: args) {
                            logStr = getLogTemplate(expression, logStr);
                            logStr = logStr.trim();
                            addToTemplates(logStr);
                        }
                    }
                }
            } catch (Exception e) {
                    System.out.println("Error when parsing method expression "+e.getMessage());
            }
        }
    }

    private void addToTemplates(String template){
        try{
            // Clean the ~~ at starting of each line.
            if(template.length() >2)
            {
                if(template.substring(0,2).equals("~~")){
                    template = template.substring(2);
                }
            }
            template = template.trim();

            /* Empty templates the basically variables printed without any accompanying text.
             We keep it to keep the file names. We skip these when doing the matching.
            if(template.equals(""))
                return;

            We are also keeping very small templates now because we are filtering by files first when matching.
            if(template.length()<2)
                return;
            */

            if(template.contains("{}"))
                template = template.replace("{}", "~~");

            // If its a  single word we need to add spaces otherwise the matching can be incorrect.
            if(!template.contains(" "))
                template =  " " + template + " ";

            if(templates.containsKey(this.currentFile))
            {
                templates.get(this.currentFile).add(template);
            }
            else{
                HashSet<String> set  = new HashSet<>();
                set.add(template);
                templates.put(this.currentFile, set);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String getLogTemplate(Expression expression, String template){

        if(expression instanceof  StringLiteralExpr)
        {
            return  template + "~~" + ((StringLiteralExpr) expression).getValue();
        }
        else if(expression instanceof NameExpr)
        {
            String var = expression.asNameExpr().getName().getIdentifier();
            if(variableToTemplateMap.containsKey(var)){
                return template + variableToTemplateMap.get(var);
            }
        }else{
            List<Node> childNodes = expression.getChildNodes();
            for(Node n : childNodes) {
                if(n instanceof Expression)
                    template = getLogTemplate((Expression) n, template);
            }
        }
        return template;
    }

    public void printAST(String filePath){
        try {
            Path pathToJavaFile = Paths.get(filePath);
            CompilationUnit cu = JavaParser.parse(pathToJavaFile.toFile());
            CompilationUnitMetaModel x = cu.getMetaModel();
            JsonPrinter j = new JsonPrinter(true);
            j.output(cu.findRootNode());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void printTemplates()
    {
        ArrayList<String> lines = new ArrayList<>();
        Path logfile = Paths.get(Main.logTemplateFile);
        try {
            if (Files.exists(logfile)) {
                Files.delete(logfile);
            }
            else {
                Files.createDirectories(logfile.getParent());
            }
            Files.createFile(logfile);

            int i = 1;
            for (String fileName: templates.keySet())
            {
                for (String template: templates.get(fileName)) {
                    String templateLine = fileName + ";;;" + template;
                    lines.add(templateLine);
                }
            }

            Files.write(logfile, lines, StandardOpenOption.APPEND);
        }
        catch(Exception ie){
            ie.printStackTrace();
        }
    }

}
