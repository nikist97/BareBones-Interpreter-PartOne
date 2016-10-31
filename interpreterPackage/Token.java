package interpreterPackage;

// type = command,variable,integer,eol
public class Token {

    private String type;
    String value;
    private String name;

    //constructor for command, variable, loop
    public Token(String type, String value){
        this.type = type;
        if (this.type.equals("command") || this.type.equals("loop")){
            this.value = value;
        }
        else if (this.type.equals("variable")){
            this.name = value;
        }
    }


    //constructor for end-of-line
    public Token(String type){
        this.type = type;
    }

    //constructor for integer
    public Token(String type, int value){
        this.type = type;
        this.value = Integer.toString(value);
    }

    public String getType(){
        return this.type;
    }

    public String getValue(){
        return this.value;
    }

    public String getName(){
        return this.name;
    }

}
