grammar Yapl;

program: 'Program' Id (declarationBlock | procedure)* 'Begin' statement* 'End' Id '.' EOF;

declarationBlock: 'Declare' (constDeclaration | varDeclaration | recordDeclaration)*;
constDeclaration: 'Const' Id '=' literal ';';
varDeclaration: type Id (',' Id)* ';';
recordDeclaration: 'Record' Id varDeclaration+ 'EndRecord' ';';

procedure: 'Procedure' ('void' | type) Id '(' (param (',' param)*)? ')' block Id ';';
param: type Id;
procedureCall: Id '(' (expression (',' expression)*)? ')';
returnStatement: 'Return' expression?;

block: declarationBlock? 'Begin' statement* 'End';
statement: (assignment | procedureCall | returnStatement | ifStatement | whileStatement | writeStatement | block) ';';

assignment: fullIdentifier ':=' expression;
selector: ('[' expression ']' | '.' Id) selector?;
ifStatement: 'If' expression 'Then' statement* ('Else' statement*)? 'EndIf';
whileStatement: 'While' expression 'Do' statement* 'EndWhile';
writeStatement: 'Write' String;

expression: expression  op=(MUL | DIV | MOD) expression #ArithmeticExpr
          | expression  op=(ADD | SUB) expression #ArithmeticExpr
          | expression  op=(LT | LE | GT | GE) expression #Comparison
          | expression  op=(EQ | NE) expression #Comparison
          | expression  op=AND expression #BooleanExpr
          | expression  op=OR expression #BooleanExpr
          | sign=(ADD | SUB)? primaryExpr #UnaryExpr
          | 'new' baseType ('[' expression ']')* #CreationExpr
          ;

primaryExpr : literal
            | fullIdentifier
            | procedureCall
            | arrayLength
            | '(' expression ')'
            ;

arrayLength : '#' fullIdentifier;
fullIdentifier: Id selector?;

type : baseType ('[' ']')*;
baseType : 'int' | 'bool' | Id;
literal: Boolean | Number;



LT: '<';
LE: '<=';
GT: '>';
GE: '>=';
EQ: '==';
NE: '!=';
AND: 'And';
OR: 'Or';
ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';
MOD: '%';

Number: DIGIT+;
Boolean: 'True' | 'False';
String: '"' (~[\r\n"] | '\\"')* '"';
Id: (LETTER | '_') (LETTER | '_' | DIGIT)*;

COMMENT: '/*' .*? '*/' -> skip;
WHITESPACE: [ \t\r\n] -> skip;

fragment DIGIT: [0-9];
fragment LETTER: [a-zA-Z];
