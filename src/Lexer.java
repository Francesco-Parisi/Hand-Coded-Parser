import java.io.*;
import java.util.*;

public class Lexer {

    private int dot;
    private File inputFile;
    private RandomAccessFile ra_file;
    private static HashMap<String, Token> stringTable;  // la struttura dati potrebbe essere una hash map
    private int state;

    public Lexer() {
        // la symbol table in questo caso la chiamiamo stringTable
        stringTable = new HashMap<String, Token>();
        dot = 0;
        state = 0;

        //Inserimento delle parole chiavi nella stringTable per evitare di scrivere un diagramma di transizione per ciascuna di esse (le parole chiavi verranno "catturate" dal diagramma di transizione e gestite e di conseguenza). IF poteva anche essere associato ad una costante numerica
        stringTable.put("if", new Token("IF","if"));
        stringTable.put("then", new Token("THEN","then"));
        stringTable.put("else", new Token("ELSE","else"));
        stringTable.put("while", new Token("WHILE","while"));
        stringTable.put("end",new Token("END","end"));
        stringTable.put("loop",new Token("LOOP","loop"));
        stringTable.put("for", new Token("FOR","for"));
        stringTable.put("int", new Token("INT","int"));
        stringTable.put("float", new Token("FLOAT","float"));
    }

    // prepara file input per lettura e controlla errori
    public boolean initialize(String filePath) throws IOException {
        inputFile = new File(filePath);
        ra_file = new RandomAccessFile(inputFile, "r");
        return inputFile.exists();
    }

    public int AnotherChar() throws IOException {
        ra_file.seek(dot++);
        return ra_file.read();
    }

    public Token nextToken() throws Exception {
        //Ad ogni chiamata del lexer (nextToken()) si resettano tutte le variabili utilizzate
        state = 0; // setta lo stato a 0
        String lessema = ""; //qui si concatena il lessema riconosciuto
        int value;
        char c;

        while (true) {
            // legge un carattere da input e lancia eccezione quando incontra EOF per restituire null
            //  per indicare che non ci sono piu token
            value = AnotherChar(); //value punta a un carattere nel file
            c = (char) value;

            //Relop Switch
            switch (state) {

                case 0:
                    if (Character.isDigit(c)) {
                        state = 5;
                    } else if (Character.isLetter(c)) {
                        state = 1;
                    } else if (Character.isWhitespace(c)) {
                        state = 11;
                    } else if (c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == ',' || c == ';') {
                        state = 12;
                    } else if(c == '+' || c == '-' || c == '*' || c == '/' || c == '%'){
                        state = 32;
                    } else if(c == '&'){
                        state = 33;
                    } else if(c == '|'){
                        state = 34;
                    } else if (c == '>') {
                        state = 15;
                    } else if (c == '<') {
                        state = 14;
                    } else if (c == '=') {
                        return new Token("RELOP", "EQ");
                    } else if (value == -1) {
                        return new Token("EOF");
                    } else {
                        return new Token("ERROR", String.valueOf(c));
                    }
                    break;

                case 14:
                    if (c == '=') {
                        return new Token("RELOP", "MINEQ");
                    } else if( c == '>'){
                        return new Token("RELOP", "DIS");
                    } else if( c == '-'){
                        state = 28;
                    } else {
                        retrack();
                        return new Token("RELOP", "MIN");
                    }
                    break;

                case 15:
                    if (c == '=') {
                        return new Token("RELOP", "MAXEQ");
                    } else {
                        retrack();
                        return new Token("RELOP", "MAX");
                    }

                case 28:
                    if(c == '-'){
                        return new Token("ASSIGN");
                    } else {
                        retrack();
                        retrack();
                        return new Token("RELOP", "MIN");
                    }
            } //End Relop Switch

            //Identificators Switch
            switch (state){
                case 1:
                    if(Character.isLetter(c)){
                        state = 20;
                        lessema += c;
                        if (value == -1)
                            return installID(lessema);
                        break;
                    } else {
                        state = 5;
                    }
                    break;

                case 20:
                    if(Character.isLetterOrDigit(c)){
                        lessema +=c;
                        if(value == -1){
                            return installID(lessema);
                        }
                    } else{
                        retrack();
                        return installID(lessema);
                    }
            } // End Identificators Switch

            //Numbers Switch
            switch (state){
                case 5:
                    if(Character.isDigit(c)){
                        state = 21;
                        lessema += c;
                    } else {
                        retrack();
                        return new Token("NUM", lessema);
                    }
                    break;

                case 21:
                    if(Character.isDigit(c)){
                        state = 21;
                        lessema += c;
                    } else if(c =='.'){
                        state = 10;
                        lessema += c;
                    } else if(c == 'E'){
                        state = 38;
                        lessema +=c;
                    }else {
                        retrack();
                        return new Token("NUM", lessema);
                    }
                    break;

                case 10:
                    if(Character.isDigit(c)){
                        state = 31;
                        lessema += c;
                    } else if(value == -1){
                        return new Token("NUM", lessema);
                    } else{
                        retrack();
                        return new Token("NUM", lessema);
                    }
                    break;

                case 31:
                    if(Character.isDigit(c)){
                        state = 31;
                        lessema += c;
                    } else if (c == 'E') {
                        state = 38;
                        lessema += c;
                    } else if(value == -1){
                        return new Token("NUM", lessema);
                    } else{
                        retrack();
                        return new Token("NUM", lessema);
                    }
                    break;

                case 38:
                    if(Character.isDigit(c)){
                        state = 40;
                        lessema += c;
                    } else if(c == '+' || c == '-'){
                        state = 39;
                        lessema += c;
                    } else {
                        state = 11;
                    }
                    break;

                case 39:
                    if (Character.isDigit(c)) {
                        state = 40;
                        lessema += c;
                    }
                    break;

                case 40:
                    if(Character.isDigit(c)){
                        state = 40;
                        lessema += c;
                    } else{
                        retrack();
                        return new Token("NUM", lessema);
                    }

            } //End Numbers Switch

            //Arithmetic Operators Switch
            switch (state){
                case 32:
                    if(c == '+')
                        return new Token("AOP","ADD");
                    else if(c == '-')
                        return new Token("AOP","SUB");
                    else if(c == '*')
                        return new Token("AOP","MUL");
                    else if(c == '/')
                        return new Token("AOP","DIV");
                    else if(c == '%')
                        return new Token("AOP","MOD");
            } // End Arithmetic Operators Switch

            //Logic Operators Switch
            switch (state){
                case 33:
                    if (c == '&'){
                        state = 35;
                        lessema += c;
                    } else {
                        retrack();
                        return new Token("ERROR", String.valueOf(c));
                    }
                    break;

                case 34:
                    if (c == '|'){
                        state = 37;
                        lessema += c;
                    } else {
                        retrack();
                        return new Token("ERROR", String.valueOf(c));
                    }
                    break;

                case 35:
                    if (c == '&'){
                        return new Token("LOP", "AND");
                    } else{
                        retrack();
                        return new Token("ERROR", String.valueOf(lessema));
                    }

                case 37:
                    if (c == '|'){
                        return new Token("LOP", "OR");
                    } else{
                        retrack();
                        return new Token("ERROR", String.valueOf(lessema));
                    }
            } //Logic Operators Switch

            //Delim Switch
            switch (state){
                case 11:
                    if (Character.isWhitespace(c)) {
                        c = (char) value;
                        state = 11;
                    } else{
                        state = 12;
                    }
                    break;

            } //End Delim Switch

            //Separators Switch
            switch (state) {

                case 12:
                    if (c == '(')
                        return new Token("OPT");
                    else if (c == ')')
                        return new Token("QTT");
                    else if (c == '{')
                        return new Token("OPG");
                    else if (c == '}')
                        return new Token("QTG");
                    else if (c == '[')
                        return new Token("OPQ");
                    else if (c == ']')
                        return new Token("QTQ");
                    else if (c == ',')
                        return new Token("VIR");
                    else if (c == ';')
                        return new Token("PVIR");

                    else {
                        retrack();
                        state = 0;
                    }

            } //End Separators Switch
        }
    }
    private Token installID(String lessema) {
        Token token;

        //utilizzo come chiave della hashmap il lessema
        if (stringTable.containsKey(lessema))
            return stringTable.get(lessema);
        else {
            token = new Token("ID", lessema);
            stringTable.put(lessema, token);
            return token;
        }
    }

    private void retrack() {
        dot--;
    }
}