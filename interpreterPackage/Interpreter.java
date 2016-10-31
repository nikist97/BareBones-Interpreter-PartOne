package interpreterPackage;

import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class Interpreter {
    private ArrayList<Token> variables;
    private String sourceCodefile;
    private ArrayList<String> possibleCommands = new ArrayList<String>();

    public Interpreter(String sourceCodefile){
        this.sourceCodefile = sourceCodefile;
        this.variables = new ArrayList<Token>();
        this.possibleCommands.add("clear");
        this.possibleCommands.add("incr");
        this.possibleCommands.add("decr");
    }

    private void error(){
        throw new SyntaxException("Wrong syntax");
    }

    private ArrayList<String> getTokens(String statement){
        ArrayList<String> tokens = new ArrayList<String>();
        StringTokenizer stringTokenizer = new StringTokenizer(statement);
        while(stringTokenizer.hasMoreTokens()){
            tokens.add((stringTokenizer.nextToken()));
        }
        return tokens;
    }

    private Token createToken(String token_input){
        switch (token_input) {
            case "clear":
                return new Token("command", "clear");

            case "incr":
                return new Token("command", "incr");

            case "decr":
                return new Token("command", "decr");
            case "while":
                return new Token("loop", "while");
            case "not":
                return new Token("not");
            case "do":
                return new Token("do");
            case "end":
                return new Token("end");
            case ";" :
                return new Token("eol");

            default:
                if (token_input.matches("^-?\\d+$")){
                    return new Token("integer", Integer.parseInt(token_input));
                }
                else if (token_input.matches("^[a-zA-Z]+$")){
                    return new Token("variable", token_input);
                }

                this.error();
        }

        return new Token("Null", "Null");
    }

    private void statement(String statement){
        int index = 0;
        ArrayList<String> tokens = getTokens(statement);
        if (tokens.size() == 3){
            Token currentToken = this.createToken(tokens.get(index));
            Token nextToken = this.createToken(tokens.get(index + 1));
            Token lastToken = this.createToken(tokens.get(index + 2));

            //all expressions start with a command and follow the syntax rule - 'command variable ;'
            if (currentToken.getType().equals("command") && nextToken.getType().equals("variable") && lastToken.getType().equals("eol")){
                switch (currentToken.getValue()) {
                    case "clear" :
                        boolean searchTest = false;
                        for (Token variable : variables){
                            if (variable.getName().equals(nextToken.getName())){
                                variable.value = Integer.toString(0);
                                searchTest = true;
                                break;
                            }
                        }

                        if (!searchTest){
                            nextToken.value = Integer.toString(0);
                            variables.add(nextToken);
                        }
                        break;

                    case "incr" :
                        boolean searchTest1 = false;
                        for (Token variable : variables){
                            if (variable.getName().equals(nextToken.getName())){
                                int value_plus = Integer.parseInt(variable.value);
                                value_plus += 1;
                                variable.value = Integer.toString(value_plus);
                                searchTest1 = true;
                            }
                        }

                        if (!searchTest1){
                            throw new SyntaxException("No variable named " + nextToken.getName());
                        }

                        break;

                    case "decr" :
                        boolean searchTest2 = false;
                        for (Token variable : variables){
                            if (variable.getName().equals(nextToken.getName())){
                                int value_minus = Integer.parseInt(variable.value);
                                value_minus -= 1;
                                variable.value = Integer.toString(value_minus);
                                searchTest2 = true;
                            }
                        }

                        if (!searchTest2){
                            throw new SyntaxException("No variable named " + nextToken.getName());
                        }

                        break;

                    default :
                        this.error();
                }
                for (Token token : variables){
                    System.out.print(token.getName() + " = " + token.getValue());
                    System.out.print("  ");
                }
                System.out.println(" ");
            }
        }
        else if (tokens.size() != 0) {
            this.error();
        }
    }

    private boolean isStatement(String statement) {
        ArrayList<String> tokens = getTokens(statement);
        if (tokens.size() != 0){
            if (this.possibleCommands.contains(tokens.get(0))){
                return true;
            }
        }
        return false;
    }

    private void loop(ArrayList<String> loopSource){
        Iterator iterator = loopSource.iterator();

        String first_line = (String) iterator.next();
        String last_line = loopSource.get(loopSource.size() - 1);

        //making sure first line follows the syntax rule : while variable not integer do ;
        ArrayList<String> FirstLineTokens = this.getTokens(first_line);
        assert FirstLineTokens.size() == 6 : "Wrong syntax";
        Token token1 = this.createToken(FirstLineTokens.get(0));
        assert token1.getType().equals("loop") && token1.getValue().equals("while") : "Wrong syntax";
        Token token2 = this.createToken(FirstLineTokens.get(1));
        assert token2.getType().equals("variable") : "Wrong syntax";
        Token token3 = this.createToken(FirstLineTokens.get(2));
        assert token3.getType().equals("not") : "Wrong syntax";
        Token token4 = this.createToken(FirstLineTokens.get(3));
        assert token4.getType().equals("integer") : "Wrong syntax";
        Token token5 = this.createToken(FirstLineTokens.get(4));
        assert token5.getType().equals("do") : "Wrong syntax";
        Token token6 = this.createToken(FirstLineTokens.get(5));
        assert token6.getType().equals("eol") : "Wrong syntax";

        boolean findVariable = false;
        for(Token token : variables){
            if (token.getName().equals(token2.getName())){
                token2 = token;
                findVariable = true;
            }
        }
        if (!findVariable){
            System.out.println("No variable named " + token2.getName());
            this.error();
        }

        boolean condition = loopCondition(token2, token4);

        //making sure last line follows the syntax rule : end ;
        ArrayList<String> LastLineTokens = this.getTokens(last_line);
        assert LastLineTokens.size() == 2 : "Wrong syntax";
        Token newToken = this.createToken(LastLineTokens.get(0));
        assert newToken.getType().equals("end") : "Wrong syntax";
        newToken = this.createToken(LastLineTokens.get(1));
        assert newToken.getType().equals("eol") : "Wrong syntax";

        for(int index = 0; index < loopSource.size(); index++){
            String line = loopSource.get(index);
            if (line.substring(0,3).equals("   ")){
                String new_line = line.substring(3);
                loopSource.set(index,new_line);
            }
        }

        System.out.println(first_line);
        String line = (String) iterator.next();
        while (condition){

            if (this.isStatement(line)){
                System.out.println(line);
                this.statement(line);
            }

            else if (this.isLoop(line)){
                ArrayList<String> nestedLoopSource = new ArrayList<String>();
                nestedLoopSource.add(line);

                while (true){
                    line = (String) iterator.next();

                    if (line.length() == 0){
                        continue;
                    }
                    else if (this.getTokens(line).get(0).equals("end") && !line.substring(0,3).equals("   ")){
                        nestedLoopSource.add(line);
                        break;
                    }
                    else if (line.substring(0,3).equals("   ")){
                        nestedLoopSource.add(line);
                    }
                    else {
                        this.error();
                    }
                }
                this.loop(nestedLoopSource);

            }

            line = (String) iterator.next();

            if (line.equals(last_line)){
                iterator = loopSource.iterator();
                iterator.next();
                line = (String) iterator.next();
                condition = loopCondition(token2, token4);
            }
        }
        System.out.println("end ;");
    }

    private boolean isLoop(String statement){
        ArrayList<String> tokens = this.getTokens(statement);
        return (tokens.size() != 0 && tokens.get(0).equals("while"));
    }

    private boolean loopCondition(Token variableToken, Token integerToken){
        int variableValue = Integer.parseInt(variableToken.value);
        int integerValue = Integer.parseInt(integerToken.getValue());
        return (variableValue != integerValue);
    }

    public String readSource(){
        String variablesState = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(this.sourceCodefile));
            String line = bufferedReader.readLine();
            while (line != null){
                if (line.length() != 0 && line.substring(0,1).equals(" ")){
                    this.error();
                }

                if (this.isStatement(line)){
                    System.out.println(line);
                    this.statement(line);
                    line = bufferedReader.readLine();
                }
                else if (this.isLoop(line)){
                    ArrayList<String> loopSource = new ArrayList<String>();
                    loopSource.add(line);
                    while (true){
                        line = bufferedReader.readLine();
                        if (line.length() == 0){
                            continue;
                        }
                        else if (line.length() != 0 && this.getTokens(line).get(0).equals("end") && !line.substring(0,3).equals("   ")){
                            loopSource.add(line);
                            break;
                        }
                        else if (line != null && line.substring(0,3).equals("   ")){
                            loopSource.add(line);
                        }

                        else {
                            this.error();
                        }
                    }
                    this.loop(loopSource);
                    line = bufferedReader.readLine();
                }
                else if (line.length() == 0) {
                    line = bufferedReader.readLine();
                }
                else {
                    this.error();
                }
            }
            System.out.println("Final state of variables:");
            for(Token token : this.variables){
                variablesState += token.getName() + " = " + token.getValue() + "  ";
                System.out.print(token.getName() + " = " + token.getValue());
                System.out.print("  ");
            }
            return variablesState;
        }
        catch (IOException ioe) {
            System.out.println("IOexception");
            System.out.println("in the main method of interpreter.java specify the absolute path of the source code");
        }
        return null;
    }

    public static void main(String[] args) {
        //in the brackets specify the absolute or the relative path of the source code text file
        Interpreter interpreter = new Interpreter("interpreterPackage/source.txt");
        interpreter.readSource();
    }
}
