public class RecDesParser {
     static Lexer analyzer = new Lexer();
     static Token token;
     public static void main(String args[]) throws Exception {
          String filePath = args[0];

          if (analyzer.initialize(filePath)) {
               if ((token = analyzer.nextToken()) != null) {
                    boolean isValid = S();
                    if ((isValid)) {
                         System.out.println("Input is valid");
                    } else {
                         System.out.println("Syntax error");
                    }
               }
          }
     }

     //Non Terminale iniziale
     static boolean S() throws Exception {

          if (Program()) {
               String s = token.getName();
               if (s.equals("EOF")) {
                    return true;
               }
          }
          return false;
     }

     static boolean Program() throws Exception {
          if (Stmt()) {
               if (S1()) {
                    return true;
               }
          }
          return false;
     }

     static boolean S1() throws Exception {
          String s = token.getName();
          if(s.equals("PVIR") || s.equals("IF")|| s.equals("ID")|| s.equals("WHILE")){
               token= analyzer.nextToken();

               if (Stmt()) {
                    if (S1()) {
                         return true;
                    }
               }
          }

          if (s.equals("EOF")) {
               return true;
          }
          return false;
     }

     static boolean Stmt() throws Exception {
          String s = token.getName();
          if(s.equals("PVIR")){

               return true;
          }
          if(s.equals("IF")) {
               if(Expr()) {
                    token= analyzer.nextToken();

                    if (Stmt()) {

                         s = token.getName();
                         if (R1()) {
                              s = token.getName();
                              if(s.equals("END")){
                                   token= analyzer.nextToken();
                                   s= token.getName();
                                   if(s.equals("IF")){
                                        token = analyzer.nextToken();
                                        return true;
                                   }
                              }
                         }
                    }
               }
          }

          if (s.equals("ID")) {
               token = analyzer.nextToken();
               s = token.getName();
               if (s.equals("ASSIGN")) {
                    if (Expr()) {
                         return true;
                    }
               }
          }

          if (s.equals("WHILE")) {
               if (Expr()) {
                    s = token.getName();
                    if(s.equals("LOOP")) {
                         token= analyzer.nextToken();
                         if (Stmt()) {
                              s = token.getName();
                              if(s.equals("END")){
                                   token= analyzer.nextToken();
                                   s= token.getName();
                                   if(s.equals("LOOP")){
                                        token = analyzer.nextToken();
                                        return true;
                                   }
                              }
                         }
                    }
               }
          }
          return false;
     }

     static boolean R1() throws Exception {
          String s = token.getName();

          if(s.equals("END")) {
               return true;
          }
          if(s.equals("ELSE")) {
               token = analyzer.nextToken();
               if (Stmt()) {
                    return true;
               }
          }
          return false;
     }
     static boolean Expr() throws Exception {
          token= analyzer.nextToken();
          String s = token.getName();

          if (s.equals("ID") ||s.equals("NUM")) {
               token = analyzer.nextToken();
               if (E1()) {
                    return true;
               }
          }
          return false;
     }

     static boolean E1() throws Exception {

          String s= token.getName();

          if(token == null || s.equals("EOF")){
               return true;
          }

          if(s.equals("RELOP")){
               if(Expr()){
                    if(E1()){
                         return true;
                    }
               }
          }

          if(s.equals("PVIR")){
               return true;
          }

          if(s.equals("THEN")) {
               return true;
          }

          if(s.equals("ELSE")) {
               return true;
          }

          if(s.equals("END")) {
               return true;
          }

          if(s.equals("LOOP")) {
               return true;
          }

          return false;
     }
}
