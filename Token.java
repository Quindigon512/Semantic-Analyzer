//----------------------------------------------
// Authors:    Quinn Trate & Maliha Doria
// Date:       April 24, 2024
// Class:      CMPSC 470 Section 1: Compilers
// Instructor: Dr. Hyuntae Na
// Purpose:    Constructor for Token from the
//             Lexer.
//----------------------------------------------

public class Token
{
    public String lexeme;
    public int lineno;
    public int column;
    public Token(String lexeme, int lineno, int column)
    {
        this.lexeme = lexeme;
        this.lineno = lineno;
        this.column = column;
    }
}
