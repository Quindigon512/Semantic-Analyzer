java -jar jflex-1.6.1.jar Lexer.flex
yacc.exe -Jthrows="Exception" -Jextends=ParserImpl -Jclass=Parser -Jnorun -J Parser.y

del ..\samples\output_*.txt

javac *.java

java TestEnv

java  Program   ..\samples\succ_01.minc    > ..\samples\output_succ_01.txt 
java  Program   ..\samples\succ_02.minc    > ..\samples\output_succ_02.txt 
java  Program   ..\samples\succ_03.minc    > ..\samples\output_succ_03.txt 
java  Program   ..\samples\succ_04.minc    > ..\samples\output_succ_04.txt 
java  Program   ..\samples\succ_05.minc    > ..\samples\output_succ_05.txt 
java  Program   ..\samples\succ_06.minc    > ..\samples\output_succ_06.txt 
java  Program   ..\samples\succ_07.minc    > ..\samples\output_succ_07.txt 
java  Program   ..\samples\succ_08.minc    > ..\samples\output_succ_08.txt 
java  Program   ..\samples\succ_09.minc    > ..\samples\output_succ_09.txt 
java  Program   ..\samples\succ_10.minc    > ..\samples\output_succ_10.txt 

java  Program   ..\samples\fail_01a.minc   > ..\samples\output_fail_01a.txt
java  Program   ..\samples\fail_01b.minc   > ..\samples\output_fail_01b.txt
java  Program   ..\samples\fail_01c.minc   > ..\samples\output_fail_01c.txt
java  Program   ..\samples\fail_01d.minc   > ..\samples\output_fail_01d.txt
java  Program   ..\samples\fail_02a.minc   > ..\samples\output_fail_02a.txt
java  Program   ..\samples\fail_02b.minc   > ..\samples\output_fail_02b.txt
java  Program   ..\samples\fail_03a.minc   > ..\samples\output_fail_03a.txt
java  Program   ..\samples\fail_04a.minc   > ..\samples\output_fail_04a.txt
java  Program   ..\samples\fail_04b.minc   > ..\samples\output_fail_04b.txt
java  Program   ..\samples\fail_04c.minc   > ..\samples\output_fail_04c.txt
java  Program   ..\samples\fail_04d.minc   > ..\samples\output_fail_04d.txt
java  Program   ..\samples\fail_04e.minc   > ..\samples\output_fail_04e.txt
java  Program   ..\samples\fail_05a.minc   > ..\samples\output_fail_05a.txt
java  Program   ..\samples\fail_07a.minc   > ..\samples\output_fail_07a.txt
java  Program   ..\samples\fail_08a.minc   > ..\samples\output_fail_08a.txt
java  Program   ..\samples\fail_08b.minc   > ..\samples\output_fail_08b.txt
java  Program   ..\samples\fail_08c.minc   > ..\samples\output_fail_08c.txt
java  Program   ..\samples\fail_08d.minc   > ..\samples\output_fail_08d.txt
java  Program   ..\samples\fail_08e.minc   > ..\samples\output_fail_08e.txt
java  Program   ..\samples\fail_08f.minc   > ..\samples\output_fail_08f.txt
java  Program   ..\samples\fail_08g.minc   > ..\samples\output_fail_08g.txt
java  Program   ..\samples\fail_08h.minc   > ..\samples\output_fail_08h.txt
java  Program   ..\samples\fail_08i.minc   > ..\samples\output_fail_08i.txt
java  Program   ..\samples\fail_08j.minc   > ..\samples\output_fail_08j.txt


del *.class
