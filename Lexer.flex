/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2000 Gerwin Klein <lsf@jflex.de>                          *
 * All rights reserved.                                                    *
 *                                                                         *
 * Thanks to Larry Bell and Bob Jamison for suggestions and comments.      *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 
 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Author:     Quinn Trate                                                 *
 * Date:       April 15, 2024                                              *
 * Class:      CMPSC 470 Section 1: Compilers                              *
 * Instructor: Dr. Hyuntae Na                                              *
 * Purpose:    Generates the Lexer.java File to be the DFA for the         *
 *             Lexical Analyzer                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

%%

%class Lexer
%byaccj
%unicode
%line
%column

%{

  public Parser   parser;
  public int      lineno;
  public int      column;
  public int	  prevCol;

  public Lexer(java.io.Reader r, Parser parser) {
    this(r);
    this.parser = parser;
    this.lineno = 1;
    this.column = 1;
    this.prevCol = 1;
  }

%}

num          = [0-9]+("."[0-9]+)?
bool         = "true"|"false"
identifier   = [a-zA-Z][a-zA-Z0-9_]*
newline      = \n
whitespace   = [ \t\r]+
linecomment  = "//".*
blockcomment = "{"[^]*"}"

%%

"return"                            { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.RETURN     ; }
"begin"                             { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.BEGIN      ; }
"print"                             { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.PRINT      ; }
"while"                             { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.WHILE      ; }
"bool"                              { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.BOOL       ; }
"else"                              { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.ELSE       ; }
"func"                              { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.FUNC       ; }
"size"                              { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.SIZE       ; }
"then"                              { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.THEN       ; }
"and"                               { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.AND        ; }
"end"                               { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.END        ; }
"new"                               { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.NEW        ; }
"not"                               { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.NOT        ; }
"num"                               { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.NUM        ; }
"var"                               { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.VAR        ; }
"if"                                { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.IF         ; }
"or"                                { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.OR         ; }
":="                                { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.ASSIGN     ; }
"::"                                { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.TYPEOF     ; }
"<>"                                { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.NE         ; }
"<="                                { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.LE         ; }
">="                                { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.GE         ; }
"("                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.LPAREN     ; }
")"                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.RPAREN     ; }
"["                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.LBRACKET   ; }
"]"                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.RBRACKET   ; }
"+"                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.ADD        ; }
"-"                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.SUB        ; }
"*"                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.MUL        ; }
"%"                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.MOD        ; }
"="                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.EQ         ; }
"<"                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.LT         ; }
">"                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.GT         ; }
";"                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.SEMI       ; }
","                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.COMMA      ; }
"."                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.DOT        ; }
{num}                               { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.NUM_LIT    ; }
{bool}                              { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.BOOL_LIT   ; }
{identifier}                        { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.IDENT      ; }
{newline}                           { /* skip */ }
{whitespace}                        { /* skip */ }
{linecomment}                       { /* skip */ }
{blockcomment}                      { /* skip */ }
"/"                                 { parser.yylval = new ParserVal(new Token(yytext(), yyline + 1, yycolumn + 1)); return Parser.DIV        ; }


\b     { column = prevCol; System.out.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { column = prevCol; System.out.println("\nLexical error: unexpected character '" + yytext() + "' at " + lineno + ":" + column + "."); return -1; }
