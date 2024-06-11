//----------------------------------------------
// Authors:    Quinn Trate & Maliha Doria
// Date:       April 24, 2024
// Class:      CMPSC 470 Section 1: Compilers
// Instructor: Dr. Hyuntae Na
// Purpose:    ParserImpl Class that Contains
//             Nodes for the Parse Tree.
//             Check for Semantic Errors
//----------------------------------------------

import java.util.*;
import java.util.HashMap;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ParserImpl
{
    int globalreladdr = 1;
    int globalparamreladdr = -1;
    public static Boolean _debug = false;
    
    void Debug(String message)
    {
        if(_debug)
            System.out.println(message);
    }

    // For chained symbol table, Includes the Global Scope Only at this Moment
    Env env = new Env(null);
    
    // Stores the Root of Parse Tree, Used to Print and Run the Parse Tree
    ParseTree.Program parsetree_program = null;
    String funcname = "";
    
    // Function for Setting stmt Return Type
    boolean recursiveReturnHelper(ArrayList<ParseTree.Stmt> stmtlist)
    {
        boolean flag = false;
        for (ParseTree.Stmt stmt : stmtlist)
        {
            if (stmt instanceof ParseTree.ReturnStmt)
            {
                flag = true;
                break;
            }
            if (stmt instanceof ParseTree.IfStmt)
            {
                ParseTree.IfStmt ifstmt = (ParseTree.IfStmt) stmt;
                flag = (flag || (recursiveReturnHelper(ifstmt.thenstmtlist) || recursiveReturnHelper(ifstmt.elsestmtlist)));
            }
            if (stmt instanceof ParseTree.WhileStmt)
            {
                ParseTree.WhileStmt whileStmt = (ParseTree.WhileStmt) stmt;
                flag = (flag || recursiveReturnHelper(whileStmt.stmtlist));
            }
            if (stmt instanceof ParseTree.CompoundStmt)
            {
                ParseTree.CompoundStmt compoundStmt = (ParseTree.CompoundStmt) stmt;
                flag = (flag || recursiveReturnHelper(compoundStmt.stmtlist));
            }
        }
        return flag;
    }
    
    // Function for Swapping Suffix for Error Message
    String stString(int num)
    {
        if (num % 10 == 1 && num != 11)
            return num + "st";
        else if (num % 10 == 2 && num != 12)
            return num + "nd";
        else if (num % 10 == 3 && num != 13)
            return num + "rd";
        else
            return num + "th";
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //    program         -> decl_list
    //    decl_list       -> decl_list decl
    //                     | eps
    //    decl            -> func_decl        
    //    func_decl       -> FUNC IDENT TYPEOF type_spec LPAREN params RPAREN BEGIN local_decls stmt_list END
    //    params          -> param_list
    //                     | eps
    //    param_list      -> param_list COMMA param
    //                     | param
    //    param           -> IDENT TYPEOF type_spec
    //    type_spec       -> prim_type
    //                     | prim_type LBRACKET RBRACKET
    //    prim_type       -> NUM
    //                     | BOOL
    //    local_decls     -> local_decls local_decl
    //                     | eps
    //    local_decl      -> VAR IDENT TYPEOF type_spec SEMI
    //    stmt_list       -> stmt_list stmt
    //                     | eps
    //    stmt            -> assign_stmt
    //                     | print_stmt
    //                     | return_stmt
    //                     | if_stmt
    //                     | while_stmt
    //                     | compound_stmt
    //    assign_stmt     -> IDENT ASSIGN expr SEMI
    //                     | IDENT LBRACKET expr RBRACKET ASSIGN expr SEMI
    //    print_stmt      -> PRINT expr SEMI
    //    return_stmt     -> RETURN expr SEMI
    //    if_stmt         -> IF expr THEN stmt_list ELSE stmt_list END
    //    while_stmt      -> WHILE expr BEGIN stmt_list END
    //    compound_stmt   -> BEGIN local_decls stmt_list END
    //    args            -> arg_list
    //                     | eps
    //    arg_list        -> arg_list COMMA expr
    //                     | expr
    //    expr            -> expr ADD expr
    //                     | expr SUB expr
    //                     | expr MUL expr
    //                     | expr DIV expr
    //                     | expr MOD expr
    //                     | expr EQ  expr
    //                     | expr NE  expr
    //                     | expr LE  expr
    //                     | expr LT  expr
    //                     | expr GE expr
    //                     | expr GT expr
    //                     | expr AND expr
    //                     | expr OR  expr
    //                     | NOT expr
    //                     | LPAREN expr RPAREN
    //                     | IDENT
    //                     | NUM_LIT
    //                     | BOOL_LIT
    //                     | NEW prim_type LBRACKET expr RBRACKET
    //                     | IDENT LBRACKET expr RBRACKET
    //                     | IDENT DOT SIZE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object program____decllist(Object s1) throws Exception
    {
        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>)s1;
        
        // Check if decllist has Main Function Having no Parameters and Returns int typebreak;
        boolean mainFlag = false;
        for (int i = 0; i < decllist.size(); i++)
        {
            if (decllist.get(i).info.info.equals("main") && decllist.get(i).info.paramtypes.size() == 0 && decllist.get(i).info.type.equals("num"))
                mainFlag = true;
        }
        
        if (!mainFlag)
            throw new Exception("The program must have one main function that returns num value and has no parameters.");
            
        // Assign the Root, Whose Type is ParseTree.Program, to parsetree_program
        parsetree_program = new ParseTree.Program(decllist);
        return parsetree_program;
    }

    Object decllist____decllist_decl(Object s1, Object s2) throws Exception
    {
        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>)s1;
        ParseTree.FuncDecl decl = (ParseTree.FuncDecl)s2;
        decllist.add(decl);
        return decllist;
    }
    
    Object decllist____eps() throws Exception { return new ArrayList<ParseTree.FuncDecl>(); }
    Object decl____funcdecl(Object s1) throws Exception { return s1; }
    
    Object fundecl____FUNC_IDENT_TYPEOF_typespec_LPAREN_params_RPAREN_BEGIN_localdecls_10X_stmtlist_END(Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8, Object s9) throws Exception
    {
        Token id = (Token)s2;
        ParseTree.TypeSpec rettype = (ParseTree.TypeSpec)s4;
        ArrayList<ParseTree.Param> params = (ArrayList<ParseTree.Param>)s6;
        ArrayList<ParseTree.LocalDecl> localdecls = (ArrayList<ParseTree.LocalDecl>)s9;
        funcname = id.lexeme;
        // Add function_type_info Object (name, return type, params) into the Global Scope of env
        ArrayList<String> paramTypes = new ArrayList<>();
        
        for (int i = 0; i < params.size(); i++)
        {
            ParseTree.Param param = params.get(i);
            paramTypes.add(param.typespec.typename);
        }
        
        EnvInfo envInfo = new EnvInfo(null, rettype.typename, null, paramTypes, 0, true);
        
        if(env.Get(id.lexeme) != null)
            throw new Exception("[Error at " + id.lineno + ":" + id.column + "] Identifier " + id.lexeme + " is already defined.");
            
        // Create a New Symbol Table on Top of env
        env.Put(id.lexeme, envInfo);
        env = new Env(env);
        int paramreladdr = -1;
        
        for (int i = 0; i < params.size(); i++)
        {
            ParseTree.Param param = params.get(i);
            if (env.GetCurr(param.ident) != null)
                throw new Exception("[Error at " + param.info.lineno + ":" + param.info.column + "] Identifier " + param.ident + " is already defined.");
            env.Put(param.ident, new EnvInfo(null, param.typespec.typename, null, null, paramreladdr));
            paramreladdr--;
        }
        
        for (int i = 0; i < localdecls.size(); i++)
        {
            ParseTree.LocalDecl localDecl = localdecls.get(i);
            if (env.GetCurr(localDecl.ident) != null)
                throw new Exception("[Error at " + localDecl.info.lineno + ":" + localDecl.info.column + "] Identifier " + localDecl.ident + " is already defined.");
            env.Put(localDecl.ident, new EnvInfo(null, localDecl.typespec.typename, null, null, globalreladdr));
            globalreladdr++;
        }
        
        return null;
    }
    
    Object fundecl____FUNC_IDENT_TYPEOF_typespec_LPAREN_params_RPAREN_BEGIN_localdecls_X10_stmtlist_END(Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8, Object s9, Object s10, Object s11, Object s12) throws Exception
    {
        Token id = (Token)s2;
        ParseTree.TypeSpec rettype = (ParseTree.TypeSpec)s4;
        ArrayList<ParseTree.Param> params = (ArrayList<ParseTree.Param>)s6;
        ArrayList<ParseTree.LocalDecl> localdecls = (ArrayList<ParseTree.LocalDecl>)s9;
        ArrayList<ParseTree.Stmt> stmtlist   = (ArrayList<ParseTree.Stmt>)s11;
        Token end = (Token)s12;
        
        // Check if this Function has at Least one Return Type
        if (!recursiveReturnHelper(stmtlist))
            throw new Exception("[Error at " + id.lineno + ":" + id.column + "] Function " + id.lexeme + "() should return at least one value.");
            
        globalreladdr = 1;
        globalparamreladdr = -1;
        ParseTree.FuncDecl funcdecl = new ParseTree.FuncDecl(id.lexeme, rettype, params, localdecls, stmtlist);
        funcdecl.info.info = id.lexeme;
        // Create and Return funcdecl Node
        funcdecl.info.paramtypes = new ArrayList<>(params.stream().map((param) -> param.info.type).collect(Collectors.toList()));
        funcdecl.info.type = rettype.typename;
        
        for (int i = 0; i < params.size(); i++)
        {
            ParseTree.Param param = params.get(i);
            param.reladdr = ((EnvInfo)env.Get(param.ident)).reladdr;
        }
            
        for (int i = 0; i < localdecls.size(); i++)
        {
            ParseTree.LocalDecl localdecl = localdecls.get(i);
            localdecl.reladdr = ((EnvInfo)env.Get(localdecl.ident)).reladdr;
        }
            
        env = env.prev;
        return funcdecl;
    }
    
    Object params____eps() throws Exception  { return new ArrayList<ParseTree.Param>(); }

    Object params____paramlist(Object s1) throws Exception 
    {
        ArrayList<ParseTree.Param> paramlist = (ArrayList<ParseTree.Param>)s1;
        return paramlist;
    }
    
    Object paramlist____paramlist_COMMA_param(Object s1, Object s2, Object s3) throws Exception 
    {
        ArrayList<ParseTree.Param> paramL = (ArrayList<ParseTree.Param>)s1;
        ParseTree.Param param = (ParseTree.Param)s3;
        paramL.add(param);
        return paramL;
    }
    
    Object paramlist____param(Object s1) throws Exception 
    {
        ParseTree.Param param = (ParseTree.Param)s1;
        ArrayList<ParseTree.Param> paramList = new ArrayList<ParseTree.Param>();
        paramList.add(param);
        return paramList;
    }
    
    Object param____IDENT_TYPEOF_typespec(Object s1, Object s2, Object s3) throws Exception 
    {
        Token id = (Token) s1;
        ParseTree.TypeSpec typeSpec = (ParseTree.TypeSpec)s3;
        ParseTree.Param param = new ParseTree.Param(id.lexeme, typeSpec);
        param.info.lineno = id.lineno;
        param.info.column = id.column;
        param.reladdr = globalparamreladdr;
        globalparamreladdr--;
        return param;
    }
    
    Object typespec____primtype(Object s1) throws Exception
    {
        ParseTree.TypeSpec primtype = (ParseTree.TypeSpec)s1;
        return primtype;
    }
    
    Object typespec____primtype_LBRACKET_RBRACKET(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.TypeSpec primtype = (ParseTree.TypeSpec)s1;
        Token lbracket = (Token)s2;
        Token rbracket = (Token)s2;
        primtype.typename = primtype.typename + ("[]");
        return primtype;
    }
    
    Object primtype____NUM(Object s1) throws Exception
    {
        ParseTree.TypeSpec typespec = new ParseTree.TypeSpec("num");
        typespec.info.type = "num";
        return typespec;
    }
    
    Object primtype____BOOL(Object s1) throws Exception
    {
        ParseTree.TypeSpec typespec = new ParseTree.TypeSpec("bool");
        typespec.info.type = "bool";
        return typespec;
    }
    
    Object localdecls____localdecls_localdecl(Object s1, Object s2) throws Exception
    {
        ArrayList<ParseTree.LocalDecl> localdecls = (ArrayList<ParseTree.LocalDecl>)s1;
        ParseTree.LocalDecl localdecl = (ParseTree.LocalDecl)s2;
        localdecls.add(localdecl);
        return localdecls;
    }

    Object localdecls____eps() throws Exception { return new ArrayList<ParseTree.LocalDecl>(); }

    Object localdecl____VAR_IDENT_TYPEOF_typespec_SEMI(Object s1, Object s2, Object s3, Object s4, Object s5) throws Exception
    {
        Token id = (Token)s2;
        ParseTree.TypeSpec typespec = (ParseTree.TypeSpec)s4;
        ParseTree.LocalDecl localdecl = new ParseTree.LocalDecl(id.lexeme, typespec);
        localdecl.info.lineno = id.lineno;
        localdecl.info.column = id.column;
        return localdecl;
    }
    
    Object stmtlist____stmtlist_stmt(Object s1, Object s2) throws Exception
    {
        ArrayList<ParseTree.Stmt> stmtlist = (ArrayList<ParseTree.Stmt>)s1;
        ParseTree.Stmt stmt = (ParseTree.Stmt)s2;
        stmtlist.add(stmt);
        return stmtlist;
    }
    
    Object stmtlist____eps() throws Exception { return new ArrayList<ParseTree.Stmt>(); }

    Object stmt____assignstmt  (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.AssignStmt);
        return s1;
    }
    
    Object stmt____printstmt  (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.PrintStmt);
        return s1;
    }
    
    Object stmt____returnstmt  (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.ReturnStmt);
        return s1;
    }
    
    Object stmt____ifstmt (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.IfStmt);
        return s1;
    }
    
    Object stmt____whilestmt (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.WhileStmt);
        return s1;
    }
    
    Object stmt____compoundstmt (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.CompoundStmt);
        return s1;
    }

    Object assignstmt____IDENT_ASSIGN_expr_SEMI(Object s1, Object s2, Object s3, Object s4) throws Exception
    {
        Token          id     = (Token         )s1;
        Token          assign = (Token         )s2;
        ParseTree.Expr expr   = (ParseTree.Expr)s3;
        EnvInfo envInfo = (EnvInfo) env.Get(id.lexeme);
        
        // Check if ident.value_type Matches With expr.value_type
        if (envInfo == null)
            throw new Exception("[Error at " + id.lineno + ":" + id.column + "] Variable " + id.lexeme + " is not defined.");
            
        if (!envInfo.type.equals(expr.info.type))
            throw new Exception("[Error at " + id.lineno + ":" + id.column + "] Variable " + id.lexeme + " should have " + envInfo.type + " value, instead of " + expr.info.type + " value.");
            
        // Create and Return Node
        ParseTree.AssignStmt stmt = new ParseTree.AssignStmt(id.lexeme, expr); //   MOVE DOWN
        stmt.ident_reladdr = envInfo.reladdr;
        return stmt;
    }
    
    Object assignstmt____IDENT_LBRACKET_expr_RBRACKET_ASSIGN_expr_SEMI (Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7) throws Exception
    {
        Token ident = (Token) s1;
        Token lbracket = (Token) s2;
        ParseTree.Expr expr = (ParseTree.Expr) s3;
        Token rbracket = (Token) s4;
        Token to = (Token) s5;
        ParseTree.Expr expr1 = (ParseTree.Expr)s6;
        Token nur = (Token) s7;
        EnvInfo envInfo = (EnvInfo) env.Get(ident.lexeme);
        
        // Check if expr.value_type Matches With the Current Function Return Type
        if(envInfo == null)
            throw new Exception("[Error at " + ident.lineno + ":" + ident.column + "] Array " + ident.lexeme + " is not defined.");
            
        if (!expr.info.type.equals("num"))
            throw new Exception("[Error at " + expr.info.lineno + ":" + expr.info.column + "] Array index must be num value.");
            
        if (!envInfo.type.replace("[]", "").equals(expr1.info.type))
                throw new Exception("[Error at " + ident.lineno + ":" + ident.column + "] Element of array " + ident.lexeme + " should have " + envInfo.type.replace("[]", "") + " value, instead of " + expr1.info.type + " value.");
                
        // Create and Return Node
        ParseTree.AssignStmtForArray assignstmtforarray = new ParseTree.AssignStmtForArray(ident.lexeme, expr, expr1);
        assignstmtforarray.ident_reladdr = envInfo.reladdr;
        return assignstmtforarray;
    }

    Object printstmt____PRINT_expr_SEMI (Object s1,Object s2, Object s3) throws Exception
    {
        ParseTree.Expr print = (ParseTree.Expr)s2;
        // Create and Return Node
        ParseTree.PrintStmt printstmt = new ParseTree.PrintStmt(print);
        return printstmt;
    }
    
    Object returnstmt____RETURN_expr_SEMI(Object s1,Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr = (ParseTree.Expr)s2;
        EnvInfo envInfo = (EnvInfo)env.Get(funcname); // REMOVE SPACE
        
        // Check if expr.value_type Matches With the Current Function Return Type
        if (!envInfo.type.equals(expr.info.type))
            throw new Exception("[Error " + expr.info.lineno + ":" + expr.info.column + "] Function " + funcname + "() should return " + envInfo.type + " value, instead of " + expr.info.type + " value.");
            
        // Create and Return Node
        return new ParseTree.ReturnStmt(expr);
    }

    Object ifstmt____IF_expr_THEN_stmtlist_ELSE_stmtlist_END(Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7) throws Exception
    {
        ParseTree.Expr expr = (ParseTree.Expr)s2;
        ArrayList<ParseTree.Stmt> stmtlist = (ArrayList<ParseTree.Stmt>)s4;
        ArrayList<ParseTree.Stmt> stmtslist2 = (ArrayList<ParseTree.Stmt>)s6;
        
        // Check if expr.value_type Matches With the Current Function Return Type
        if (!expr.info.type.equals("bool"))
            throw new Exception("[Error at " + expr.info.lineno + ":" + expr.info.column + "] Condition of if or while statement should be bool value.");
            
        // Create and Return Node
        ParseTree.IfStmt ifStmt = new ParseTree.IfStmt(expr, stmtlist, stmtslist2);
        return ifStmt;
    }

    Object whilestmt____WHILE_expr_BEGIN_stmtlist_END(Object s1, Object s2, Object s3, Object s4, Object s5) throws Exception
    {
        ParseTree.Expr expr = (ParseTree.Expr)s2;
        ArrayList<ParseTree.Stmt> stmts = (ArrayList<ParseTree.Stmt>)s4;
        
        // Check if expr.value_type Matches With the Current Function Return Type
        if (!expr.info.type.equals("bool"))
            throw new Exception("[Error at " + expr.info.lineno + ":" + expr.info.column + "] Condition of if or while statement should be bool value.");
            
        // Create and Return Node
        ParseTree.WhileStmt ifStmt = new ParseTree.WhileStmt(expr, stmts);
        return ifStmt;
    }
    
    Object compoundstmt____BEGIN_localdecls_3X_stmtlist_END(Object s1, Object s2) throws Exception
    {
        ArrayList<ParseTree.LocalDecl> localDecls = (ArrayList<ParseTree.LocalDecl>)s2;
        env = new Env(env);
        
        for (int i = 0; i < localDecls.size(); i++)
        {
            ParseTree.LocalDecl localdecl = localDecls.get(i);
            if (env.GetCurr(localdecl.ident) != null)
                throw new Exception("[Error at " + localdecl.info.lineno + ":" + localdecl.info.column + "] Identifier " + localdecl.ident + " is already defined.");
            env.Put(localdecl.ident, new EnvInfo(null, localdecl.typespec.typename, null, null, globalreladdr));
            globalreladdr++;
        }
        
        return null;
    }
    
    Object compoundstmt____BEGIN_localdecls_X3_stmtlist_END(Object s1, Object s2, Object s3, Object s4, Object s5) throws Exception
    {
        ArrayList<ParseTree.LocalDecl> localDecls = (ArrayList<ParseTree.LocalDecl>)s2;
        ArrayList<ParseTree.Stmt> stmtlist = (ArrayList<ParseTree.Stmt>)s4;
        
        for (int i = 0; i < localDecls.size(); i++)
        {
            ParseTree.LocalDecl localdecl = localDecls.get(i);
            localdecl.reladdr = ((EnvInfo)env.Get(localdecl.ident)).reladdr;
        }
            
        // Create and Return Node
        ParseTree.CompoundStmt compoundStmt = new ParseTree.CompoundStmt(localDecls, stmtlist);
        env = env.prev;
        return compoundStmt;
    }

    Object args____arglist(Object s1) throws Exception
    {
        ArrayList<ParseTree.Arg> arglist = (ArrayList <ParseTree.Arg>)s1;
        return arglist;
    }

    Object args____eps() throws Exception { return new ArrayList<ParseTree.Expr>(); }

    Object arglist____arglist_COMMA_expr (Object s1, Object s2, Object s3) throws Exception
    {
       ArrayList<ParseTree.Arg> arglist = (ArrayList<ParseTree.Arg>)s1;
       arglist.add (new ParseTree.Arg((ParseTree.Expr)s3));
       return arglist;
    }
    
    Object arglist____expr (Object s1) throws Exception
    {
        ParseTree.Expr expr = (ParseTree.Expr)s1;
        ArrayList<ParseTree.Arg> args = new ArrayList<ParseTree.Arg>();
        args.add(new ParseTree.Arg((ParseTree.Expr)s1));
        return args;
    }

    Object expr____expr_ADD_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("num") || !expr2.info.type.equals("num"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprAdd exprAdd = new ParseTree.ExprAdd(expr1, expr2);
        exprAdd.info.type = "num";
        exprAdd.info.lineno = expr1.info.lineno;
        exprAdd.info.column = expr1.info.column;
        return exprAdd;
    }
    
    Object expr____expr_SUB_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("num") || !expr2.info.type.equals("num"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprSub exprSub = new ParseTree.ExprSub(expr1, expr2);
        exprSub.info.type = "num";
        exprSub.info.lineno = oper.lineno;
        exprSub.info.column = oper.column;
        return exprSub;
     
    }

    Object expr____expr_MUL_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("num") || !expr2.info.type.equals("num"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprMul exprMul = new ParseTree.ExprMul(expr1, expr2);
        exprMul.info.type = "num";
        exprMul.info.lineno = oper.lineno;
        exprMul.info.column = oper.column;
        return exprMul;
    }

    Object expr____expr_DIV_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("num") || !expr2.info.type.equals("num"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprDiv exprDiv = new ParseTree.ExprDiv(expr1, expr2);
        exprDiv.info.type = "num";
        exprDiv.info.lineno = oper.lineno;
        exprDiv.info.column = oper.column;
        return exprDiv;
    }

    Object expr____expr_MOD_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("num") || !expr2.info.type.equals("num"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprMod exprMod = new ParseTree.ExprMod(expr1, expr2);
        exprMod.info.type = "num";
        exprMod.info.lineno = oper.lineno;
        exprMod.info.column = oper.column;
        return exprMod;
    }

    Object expr____expr_EQ_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper  = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals(expr2.info.type))
            throw new Exception("[Error at" + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + "cannot be used with " + expr1.info.type + "and" + expr2.info.type + "values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprEq exprEQ = new ParseTree.ExprEq(expr1, expr2);
        exprEQ.info.type = "bool";
        exprEQ.info.lineno = expr1.info.lineno;
        exprEQ.info.column = expr1.info.column;
        return exprEQ;
    }
    
    Object expr____expr_NE_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals(expr2.info.type))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprNe exprNE = new ParseTree.ExprNe(expr1, expr2);
        exprNE.info.type = "bool";
        exprNE.info.lineno = oper.lineno;
        exprNE.info.lineno = oper.column;
        return exprNE;
    }
    
    Object expr____expr_LE_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("num") || !expr2.info.type.equals("num"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprLe exprLE = new ParseTree.ExprLe(expr1, expr2);
        exprLE.info.type = "bool";
        exprLE.info.lineno = oper.lineno;
        exprLE.info.column = oper.column;
        return exprLE;
    }

    Object expr____expr_LT_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("num") || !expr2.info.type.equals("num"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprLt exprLT = new ParseTree.ExprLt(expr1, expr2);
        exprLT.info.type = "bool";
        exprLT.info.lineno = oper.lineno;
        exprLT.info.column = oper.column;
        return exprLT;
    }
    
    Object expr____expr_GE_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper  = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("num") || !expr2.info.type.equals("num"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprGe exprGE = new ParseTree.ExprGe(expr1, expr2);
        exprGE.info.type = "bool";
        exprGE.info.lineno = oper.lineno;
        exprGE.info.column = oper.column;
        return exprGE;
    }

    Object expr____expr_GT_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("num") || !expr2.info.type.equals("num"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprGt exprGT = new ParseTree.ExprGt(expr1, expr2);
        exprGT.info.type = "bool";
        exprGT.info.lineno = oper.lineno;
        exprGT.info.column = oper.column;
        return exprGT;
    }
    
    Object expr____expr_AND_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("bool") || !expr2.info.type.equals("bool"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values." );
            
        // Create and Return Node That has value_type
        ParseTree.ExprAnd exprAnd = new ParseTree.ExprAnd(expr1, expr2);
        exprAnd.info.type = "bool";
        exprAnd.info.lineno = oper.lineno;
        exprAnd.info.column = oper.column;
        return exprAnd;
    }

    Object expr____expr_OR_expr(Object s1, Object s2, Object s3) throws Exception
    {
        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token oper = (Token)s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;
        
        // Check if expr1.type Matches With expr2.type
        if(!expr1.info.type.equals("bool") || !expr2.info.type.equals("bool"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Binary operation " + oper.lexeme + " cannot be used with " + expr1.info.type + " and " + expr2.info.type + " values.");
            
        // Create and Return Node That has value_type
        ParseTree.ExprOr exprOr = new ParseTree.ExprOr(expr1, expr2);
        exprOr.info.type = "bool";
        exprOr.info.lineno = oper.lineno;
        exprOr.info.column = oper.column;
        return exprOr;

    }

    Object expr____NOT_expr(Object s1,Object s2) throws Exception
    {
        Token oper = (Token)s1;
        ParseTree.Expr expr = (ParseTree.Expr)s2;
        // Check if expr.type is bool Type
        if(!expr.info.type.equals("bool"))
            throw new Exception("[Error at " + oper.lineno + ":" + oper.column + "] Unary operation " + oper.lexeme + " cannot be used with " + expr.info.type + " value.");
            
        // Create and Return Node That has value_type
        ParseTree.ExprNot exprNot = new ParseTree.ExprNot(expr);
        exprNot.info.type = "bool";
        exprNot.info.lineno = oper.lineno;
        exprNot.info.column = oper.column;
        return exprNot;
    }
    
    Object expr____LPAREN_expr_RPAREN(Object s1, Object s2, Object s3) throws Exception
    {
        Token lparen = (Token)s1;
        ParseTree.Expr expr = (ParseTree.Expr)s2;
        Token rparen = (Token)s3;
        // Create and Return Node Whose value_type is the Same to the expr.value_type
        ParseTree.ExprParen exprParen = new ParseTree.ExprParen(expr);
        exprParen.info.type = expr.info.type;
        exprParen.info.lineno = lparen.lineno;
        exprParen.info.column = lparen.column;
        return exprParen;
    }
   
    Object expr____IDENT(Object s1) throws Exception
    {
        Token id = (Token) s1;
        // Create and Return Node that has the value_type of the id.lexeme
        ParseTree.ExprIdent exprIdent = new ParseTree.ExprIdent(id.lexeme);
        EnvInfo envInfo = (EnvInfo) env.Get(id.lexeme);
        
        // Check if id.lexeme can be Found in Chained Symbol Tables
        if (envInfo == null)
            throw new Exception("[Error at " + id.lineno + ":" + id.column + "] Variable " + id.lexeme + " is not defined.");
            
        // Check if it is Variable Type
        if(envInfo.isFunction)
            throw new Exception("[Error at " + id.lineno + ":" + id.column + "] Identifier " + id.lexeme + " should be non-function type.");
            
        exprIdent.reladdr = envInfo.reladdr;
        exprIdent.info.type = envInfo.type;
        exprIdent.info.lineno = id.lineno;
        exprIdent.info.column = id.column;
        return exprIdent;
    }

    Object expr____NUMLIT(Object s1) throws Exception
    {
        // Create and Return Node That has int Type
        Token token = (Token) s1;
        double value = Double.parseDouble(token.lexeme);
        ParseTree.ExprNumLit num = new ParseTree.ExprNumLit(value);
        num.info.type = "num";
        num.info.lineno = token.lineno;
        num.info.column = token.column;
        return num;
    }

    Object expr____BOOLLIT(Object s1) throws Exception
    {
        // Create and Return Node That has bool Type
        Token token = (Token) s1;
        boolean value = Boolean.parseBoolean(token.lexeme);
        ParseTree.ExprBoolLit bool = new ParseTree.ExprBoolLit(value);
        bool.info.type = "bool";
        bool.info.lineno = token.lineno;
        bool.info.column = token.column;
        return bool;
    }

    Object expr____IDENT_LPAREN_args_RPAREN(Object s1,  Object s2, Object s3, Object s4) throws Exception
    {
        Token id = (Token)s1;
        ArrayList<ParseTree.Arg> args = (ArrayList<ParseTree.Arg>)s3;
        EnvInfo envInfo = (EnvInfo) env.Get(id.lexeme);
        // Create and Return Node That has the value_type of env(id.lexeme).return_type
        ParseTree.Expr exprFuncCall = new ParseTree.ExprFuncCall(id.lexeme, args);
        
        // Check if id.lexeme can be Found in Chained Symbol Tables
        if (envInfo == null)
            throw new Exception("[Error at " + id.lineno + ":" + id.column + "] Function " + id.lexeme + "() is not defined.");
            
        // Check if it is Function Type
        if (!envInfo.isFunction)
            throw new Exception("[Error at " + id.lineno + ":" + id.column + "] Identifier " + id.lexeme + " should be function.");
            
        if (envInfo.params.size() != args.size())
            throw new Exception("[Error at " + id.lineno + ":" + id.column + "] Function " + id.lexeme + "() should be called with the correct number of arguments.");
            
        // Check if the Number and Types of env(id.lexeme).params Match With Those of args
        int i = 0;
        for (int j = 0; j < args.size(); j++)
        {
            ParseTree.Arg arg = args.get(j);
            if (!arg.expr.info.type.equals(envInfo.params.get(i)))
                throw new Exception("[Error at " + arg.expr.info.lineno + ":" + arg.expr.info.column + "] The " + stString(i + 1) + " argument of function " + id.lexeme + "() should be " + envInfo.params.get(i) + " value, instead of " + arg.expr.info.type + " value.");
            i++;
        }
        
        exprFuncCall.info.type = envInfo.type;
        exprFuncCall.info.lineno = id.lineno;
        exprFuncCall.info.column = id.column;
        return exprFuncCall;
    }

    Object expr____NEW_primtype_LBRACKET_expr_RBRACKET(Object s1, Object s2, Object s3, Object s4, Object s5) throws Exception
    {
        Token id = (Token) s1;
        ParseTree.TypeSpec primtype = (ParseTree.TypeSpec)s2;
        ParseTree.Expr expr = (ParseTree.Expr)s4;
        ParseTree.ExprNewArray exprNewArray = new ParseTree.ExprNewArray(primtype, expr);
        
        if (!expr.info.type.equals("num"))
            throw new Exception("[Error at " + expr.info.lineno + ":" + expr.info.column + "] Array index must be num value.");
            
        exprNewArray.info.type = primtype.info.type + "[]";
        exprNewArray.info.value = expr.info.type;
        exprNewArray.info.lineno = id.lineno;
        exprNewArray.info.column = id.column;
        return exprNewArray;
    }

    Object expr____IDENT_LBRACKET_expr_RBRACKET(Object s1, Object s2, Object s3, Object s4) throws Exception
    {
        Token ident = (Token)s1;
        ParseTree.Expr expr = (ParseTree.Expr)s3;
        EnvInfo envInfo = (EnvInfo) env.Get(ident.lexeme);
        
        if (envInfo == null)
            throw new Exception("[Error at " + ident.lineno + ":" + ident.column + "] Array " + ident.lexeme + " is not defined.");
            
        if (!envInfo.type.contains("[]"))
            throw new Exception("[Error at " + ident.lineno + ":" + ident.column + "] Identifier " + ident.lexeme + " should be array variable.");
            
        if(!expr.info.type.equals("num"))
            throw new Exception("[Error at " + expr.info.lineno + ":" + expr.info.column + "] Array index must be num value.");
            
        ParseTree.ExprArrayElem exprArrayElem = new ParseTree.ExprArrayElem(ident.lexeme, expr);
        exprArrayElem.info.type = envInfo.type.replace("[]", "");
        exprArrayElem.info.value = envInfo.value;
        exprArrayElem.info.lineno = ident.lineno;
        exprArrayElem.info.column = ident.column;
        exprArrayElem.reladdr = envInfo.reladdr;
        return exprArrayElem;
    }
    
    Object expr____IDENT_DOT_SIZE(Object s1, Object s2, Object s3) throws Exception
    {
        Token ident = (Token)s1;
        EnvInfo envInfo = (EnvInfo) env.Get(ident.lexeme);
        
        if (envInfo == null)
            throw new Exception("[Error at " + ident.lineno + ":" + ident.column + "] Array " + ident.lexeme + " is not defined.");
            
        if (!envInfo.type.contains("[]"))
            throw new Exception("[Error at " + ident.lineno + ":" + ident.column + "] Identifier " + ident.lexeme + " should be array variable.");
            
        ParseTree.ExprArraySize exprArraySize = new ParseTree.ExprArraySize(ident.lexeme);
        exprArraySize.info.type = "num";
        exprArraySize.info.lineno = ident.lineno;
        exprArraySize.info.column = ident.column;
        exprArraySize.reladdr = envInfo.reladdr;
        return exprArraySize;
    }
}
