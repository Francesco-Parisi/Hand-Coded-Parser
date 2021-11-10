Realizzazione di un Parser in Java.

Esercitazione 3 del Corso Compilatori dell'Università degli studi di Salerno.

## Table

|Lessema|Token|Attributo|
|------|------|---------|
|**Delimitatori**|
|* *|DELIM|-|
|*\n*|DELIM|-|
|*\t*|DELIM|-|
|*"*|DELIM|-|
||||
|**Separatori**|
|(|SEPA|OPT|
|)|SEPA|QTT|
|{|SEPA|OPG|
|}|SEPA|QTG|
|[|SEPA|OPQ|
|]|SEPA|QTQ|
|,|SEPA|VIR|
|;|SEPA|PVIR|
||||
|**Relop**|
|<|RELOP|MIN|
|<=|RELOP|MINEQ|
|=|RELOP|EQ|
|<>|RELOP|DIS|
|>|RELOP|MAX|
|>=|RELOP|MAXEQ|
|<--|ASSIGN|
||||
|**Identificatori**|
|id|ID|
||||
|**Numeri**|
|numero|NUM|
||||
|**Operatori Aritmetici**|
|+|AOP|ADD|
|-|AOP|SUB|
|*|AOP|MUL|
|/|AOP|DIV|
|%|AOP|MOD|
|**Operatori Logici**|
|&&|LOP|AND|
|\| \||LOP|OR|


## Grammar

 N = {S, Program, Stmt, Expr},
 T = {EOF. ';', IF, THEN, ELSE, END, ID, ASSIGN, WHILE, LOOP, RELOP, NUMBER},  
 S

 P = {

          S -> Program  EOF

          Program -> Program ; Stmt
                           |  Stmt
           Stmt -> IF Expr THEN Stmt ELSE Stmt END IF
                     | ID ASSIGN Expr

                     | WHILE Epr LOOP Stmt END LOOP

          Expr ->  Expr  RELOP Expr

          Expr ->   ID
                     |  NUMBER
       }

Per questa grammatica è stato necessario trasformarla, applicando la ricorsione sinistra.

La grammatica risultante è la seguente:

      S -> Program EOF
      Program -> Stmt S'
      S' -> ; Stmt S'
      S' -> eps
      Stmt -> IF Expr THEN Stmt ELSE Stmt END IF
      Stmt -> ID ASSIGN Expr
      Stmt -> WHILE Expr LOOP Stmt END LOOP
      Expr -> ID E'
      Expr -> NUMBER E'
      E' -> RELOP Expr E'
      E' -> eps

La grammatica pur effettuando la trasformazione risulta comunque ambigua.

## Note
