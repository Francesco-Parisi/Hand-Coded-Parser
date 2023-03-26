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
          state = 100;
          //Inserimento delle parole chiavi nella stringTable per evitare di scrivere un diagramma di transizione per ciascuna di esse (le parole chiavi verranno "catturate" dal diagramma di transizione e gestite e di conseguenza). IF poteva anche essere associato ad una costante numerica
          stringTable.put("if", new Token("IF"));
          stringTable.put("then", new Token("THEN"));
          stringTable.put("else", new Token("ELSE"));
          stringTable.put("while", new Token("WHILE"));
          stringTable.put("end",new Token("END"));
          stringTable.put("loop",new Token("LOOP"));
          stringTable.put("for", new Token("FOR"));
          stringTable.put("int", new Token("INT"));
          stringTable.put("float", new Token("FLOAT"));
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
          state = 100; // setta lo stato a 0
          String lessema = ""; //qui si concatena il lessema riconosciuto
          int value;
          char c;

          while (true) {
               //Si controlla se il valore della funzione ritorna -1
               //Se abbiamo -1 il file viene chiuso e restutuito il token EOF
               try {
                    if ((value = AnotherChar()) != -1) {
                         c = (char) value;
                    }
                    else
                    {
                         ra_file.close();
                         Token token= new Token("EOF");
                         stringTable.put(lessema,token);
                         return token;
                    }
               }
               catch(IOException e) {
                    return null;
               }

               //Initial Switch
               switch (state) {

                    case 100:
                         if (Character.isDigit(c)) {
                              state = 12;
                         } else if (Character.isLetter(c)) {
                              state = 9;
                         } else if (Character.isWhitespace(c)|| c =='"') {
                              state = 32;
                         } else if(c == '+' || c == '-' || c == '*' || c == '/' || c == '%'){
                              state = 22;
                         } else if (c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == ',' || c == ';') {
                              state = 24;
                         } else if(c == '&'){
                              state = 26;
                         } else if(c == '|'){
                              state = 29;
                         } else if (c == '>' || c =='<'|| c=='=') {
                              state = 34;
                         }else {
                              return new Token("ERROR", String.valueOf(c));
                         }
                         break;
               } //End Initial Switch

               //Relop Switch
               switch (state){
                    case 34:
                         if(c == '>'){
                              state = 6;
                              if(nextChar()){
                                   return new Token("RELOP", "MAX");
                              }
                         }else if (c == '<') {
                              lessema += c;
                              if(nextChar()){
                                   return new Token("RELOP", "MIN");
                              }
                              state = 1;
                         }else if (c == '=') {
                              return new Token("RELOP", "EQ");
                         }
                         break;

                    case 1:
                         if (c == '=') {
                              return new Token("RELOP", "MINEQ");
                         } else if( c == '>'){
                              return new Token("RELOP", "DIS");
                         } else if( c == '-'){
                              lessema += c;
                              if(nextChar()){
                                   return new Token("ERROR", lessema);
                              }
                              state = 33;
                         } else {
                              retrack();
                              return new Token("RELOP", "MIN");
                         }
                         break;

                    case 6:
                         if (c == '=') {
                              return new Token("RELOP", "MAXEQ");
                         } else {
                              retrack();
                              return new Token("RELOP", "MAX");
                         }

                    case 33:
                         if(c == '-'){
                              return new Token("ASSIGN");
                         } else {
                              retrack();
                              return new Token("ERROR", lessema);
                         }
               } //End Relop Switch

               //Identificators Switch
               switch (state){
                    case 9:
                         if (Character.isLetter(c)) {
                              lessema += c;
                              if(nextChar()){
                                   return installID(lessema);
                              }
                              state = 10;
                         }
                         break;

                    case 10:
                         if(Character.isLetterOrDigit(c)){
                              state=10;
                              lessema +=c;
                              if(nextChar())
                                   return installID(lessema);
                              break;
                         } else {
                              retrack();
                              return installID(lessema);
                         }
               } //End Identificators Switch

               //Numbers Switch
               switch (state){
                    case 12:
                         if(Character.isDigit(c)){
                              lessema += c;
                              if(nextChar()){
                                   return installNUM(lessema);
                              }
                              state = 13;
                         } else {
                              retrack();
                              return(installNUM(lessema));
                         }
                         break;

                    case 13:
                         if(Character.isDigit(c)){
                              lessema += c;
                              if(nextChar()){
                                   return installNUM(lessema);
                              }
                              state = 13;
                         } else if(c =='.'){
                              lessema += c;
                              if (nextChar()) {
                                   retrack();
                                   retrack();
                                   lessema = lessema.substring(0,lessema.length()-1);
                                   return installNUM(lessema);
                              }
                              state = 14;
                         } else if(c == 'E'){
                              lessema +=c;
                              if (nextChar()) {
                                   retrack();
                                   retrack();
                                   lessema = lessema.substring(0,lessema.length()-1);
                                   return installNUM(lessema);
                              }
                              state = 16;
                         } else {
                              retrack();
                              return(installNUM(lessema));
                         }
                         break;

                    case 17:
                         if(Character.isDigit(c)){
                              lessema += c;
                              if(nextChar()){
                                   return installNUM(lessema);
                              }
                              state = 18;
                         } else {
                              retrack();
                              retrack();
                              retrack();
                              lessema = lessema.substring(0,lessema.length()-2);
                              return(installNUM(lessema));

                         }
                         break;

                    case 14:
                         if(Character.isDigit(c)){
                              lessema += c;
                              if(nextChar()){
                                   return installNUM(lessema);
                              }
                              state = 15;
                         } else {
                              retrack();
                              retrack();
                              lessema = lessema.substring(0,lessema.length()-1);
                              return(installNUM(lessema));
                         }
                         break;

                    case 15:
                         if(Character.isDigit(c)){
                              lessema += c;
                              if (nextChar()) {
                                   return installNUM(lessema);
                              }
                              state = 15;
                         } else if (c == 'E') {
                              lessema += c;
                              if(nextChar()){
                                   retrack();
                                   retrack();
                                   lessema = lessema.substring(0,lessema.length()-1);
                                   return installNUM(lessema);
                              }
                              state = 16;
                         } else{
                              retrack();
                              return(installNUM(lessema));
                         }
                         break;

                    case 16:
                         if(Character.isDigit(c)){
                              lessema += c;
                              if(nextChar()){
                                   return installNUM(lessema);
                              }
                              state = 18;
                         } else if(c == '+' || c == '-'){
                              lessema += c;
                              if(nextChar()){
                                   retrack();
                                   retrack();
                                   retrack();
                                   lessema = lessema.substring(0,lessema.length()-1);
                                   return installNUM(lessema);
                              }
                              state = 17;
                         } else {
                              retrack();
                              retrack();
                              lessema = lessema.substring(0,lessema.length()-1);
                              return(installNUM(lessema));
                         }
                         break;

                    case 18:
                         if (Character.isDigit(c)) {
                              lessema += c;
                              if(nextChar()){
                                   return installNUM(lessema);
                              }
                              state = 18;
                         } else {
                              retrack();
                              return(installNUM(lessema));
                         }
                         break;

               } //End Numbers Switch

               //Arithmetic Operators Switch
               switch (state){
                    case 22:
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
               } //End Arithmetic Operators Switch

               //Logic Operators Switch
               switch (state){
                    case 26:
                         if (c == '&'){
                              lessema += c;
                              if(nextChar()){
                                   return new Token("ERROR", lessema);
                              }
                              state = 27;
                         } else {
                              retrack();
                              return new Token("ERROR", lessema);
                         }
                         break;

                    case 29:
                         if (c == '|'){
                              lessema += c;
                              if(nextChar()){
                                   return new Token("ERROR", lessema);
                              }
                              state = 31;
                         } else {
                              retrack();
                              return new Token("ERROR", lessema);
                         }
                         break;

                    case 27:
                         if (c == '&'){
                              return new Token("RELOP", "AND");
                         } else{
                              retrack();
                              return new Token("ERROR", lessema);
                         }

                    case 31:
                         if (c == '|'){
                              return new Token("RELOP", "OR");
                         } else{
                              retrack();
                              return new Token("ERROR", lessema);
                         }
               } //End Logic Operators Switch

               //Delim Switch
               switch (state){
                    case 32:
                         if (Character.isWhitespace(c) || c=='"') {
                              state = 32;
                         } else{
                              state = 24;
                         }
                         break;

               } //End Delim Switch

               //Separators Switch
               switch (state) {

                    case 24:
                         if (c == '(')
                              return new Token("OPT",String.valueOf(c));
                         else if (c == ')')
                              return new Token("QTT",String.valueOf(c));
                         else if (c == '{')
                              return new Token("OPG",String.valueOf(c));
                         else if (c == '}')
                              return new Token("QTG",String.valueOf(c));
                         else if (c == '[')
                              return new Token("OPQ",String.valueOf(c));
                         else if (c == ']')
                              return new Token("QTQ",String.valueOf(c));
                         else if (c == ',')
                              return new Token("VIR",String.valueOf(c));
                         else if (c == ';')
                              return new Token("PVIR",String.valueOf(c));

                         else {
                              retrack();
                              state = 100;
                         }

               } //End Separators Switch
          }
     }

     private Token installID(String lessema) {
          Token token;

          if (stringTable.containsKey(lessema))
               return stringTable.get(lessema);
          else {
               token = new Token("ID", lessema);
               stringTable.put(lessema, token);
               return token;
          }
     }
     private Token installNUM(String lessema)
     {
          Token token =  new Token("NUM",lessema);
          stringTable.put(lessema, token);
          return token;
     }

     private void retrack() {
          dot--;
     }

     private boolean nextChar(){
          try {
               if(AnotherChar()==-1)
               {
                    return true;
               }


          } catch (IOException e) {
               e.printStackTrace();
          }
          retrack();
          return false;

     }

}