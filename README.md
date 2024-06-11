Author:     Quinn Trate & Maliha Doria
Date:       April 23, 2024
Class:      CMPSC 470 Section 1: Compilers
Instructor: Dr. Hyuntae Na
Decription: Takes .minc Files as Input and
            Returns Success or Error.
            Will Compile the Code.


Made and Compiled in UNIX Machine

Command for Generating Lexer.java:
java -jar jflex-1.6.1.jar Lexer.flex

Command for Generating Parcer.java & ParcerVal.java (Windows):
./yacc.exe -Jthrows="Exception" -Jextends=ParserImpl -Jclass=Parser -Jnorun -J Parser.y

Command for Generating Parcer.java & ParcerVal.java  (Linux):
./yacc.linux -Jthrows="Exception" -Jextends=ParserImpl -Jclass=Parser -Jnorun -J Parser.y
		
Command for Testing:
javac *.java && java Program succ_X.minc

Takes .minc Files as Input and Returns Compiled Code or Failure. Semantic Errors are Given and Corrected. Uses JFlex and BYacc/J and Includes Test Files.
