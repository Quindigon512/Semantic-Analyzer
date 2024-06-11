/////////////////////////////////////////////////////////////////////////////////////////
//  MIT License                                                                        //
//                                                                                     //
//  Copyright (c) 2024 Hyuntae Na                                                      //
//                                                                                     //
//  Permission is hereby granted, free of charge, to any person obtaining a copy       //
//  of this software and associated documentation files (the "Software"), to deal      //
//  in the Software without restriction, including without limitation the rights       //
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell          //
//  copies of the Software, and to permit persons to whom the Software is              //
//  furnished to do so, subject to the following conditions:                           //
//                                                                                     //
//  The above copyright notice and this permission notice shall be included in all     //
//  copies or substantial portions of the Software.                                    //
//                                                                                     //
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR         //
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,           //
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE        //
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER             //
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,      //
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE      //
//  SOFTWARE.                                                                          //
/////////////////////////////////////////////////////////////////////////////////////////

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class ParseTree
{
    public static class ExecEnv
    {
        // function-running environment
        // * This provide the envieonment of running/calling functions, and 
        //   mimics the stack frame structure in call stack.
        // * reference https://en.wikipedia.org/wiki/Call_stack
        class StackFrame
        {
            HashMap<Integer, Object> reladdr_val;
                // the runtime environment of running function
                // idx -i: i-th param
                // idx  0: return value
                // idx  i: i-th local variable
            public StackFrame()
            {
                reladdr_val = new HashMap<Integer, Object>();
            }
            public boolean HasValue(int reladdr)
            {
                // get value at reladdr (relative address)
                return reladdr_val.containsKey(reladdr);
            }
            public Object GetValue(int reladdr)
            {
                // get value at reladdr (relative address)
                assert(reladdr_val.containsKey(reladdr));
                Object val = reladdr_val.get(reladdr);
                return val;
            }
            public void SetValue(int reladdr, Object val)
            {
                // update value at reladdr (relative address)
                reladdr_val.put(reladdr, val);
            }
        }
        // RunEnv maintains the program running environment by maintaining
        // * the function call stack (each func-call stack item contains the running environment including variable vlaues)
        // * the map for func_name -> func_body
        public Stack<StackFrame>            stackframes;        // stack of stack frames (stack of function calls)
        public HashMap<String, FuncDecl>    funcname_funcdecl;  // name -> function body (parse-tree node)

        public ExecEnv(ArrayList<FuncDecl> funcs)
        {
            // create calling stack environment
            stackframes = new Stack<StackFrame>();
            // create map for name->function-body
            funcname_funcdecl = new HashMap<String, FuncDecl>();
            for(FuncDecl func : funcs)
            {
                funcname_funcdecl.put(func.ident, func);
            }
        }
        public StackFrame GetTopStackFrame()
        {
            StackFrame top = stackframes.peek();
            return top;
        }
        public void PushStackFrame()
        {
            // push a new function-running environment (stackframe)
            // to prepare function call
            stackframes.push(new StackFrame());
        }
        public void PopStackFrame()
        {
            // pop the used function-running environment
            // to cleaning the called-function environment
            stackframes.pop();
        }
    }

    public static abstract class Node
    {
        public static enum OptToString
        {
            Default,        // print code with indentation
            CommentExecEnv  // print code with comment for running environment
        }
        abstract public String[] ToStringList(OptToString opt) throws Exception; // This is used to print code with indentation and comments
    }

    public static class TypeSpec extends Node
    {
        public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo(); // store your own data in TypeSpecInfo
        public String typename;
        public TypeSpec(String typename)              { this.typename = typename; }
        public String ToString()                      { return typename; }
        public String[] ToStringList(OptToString opt) { return new String[] { ToString() }; }
    }

    public static class Program extends Node
    {
        public ParseTreeInfo.ProgramInfo info = new ParseTreeInfo.ProgramInfo(); // store your own data in ProgramInfo
        public ArrayList<FuncDecl> funcs;
        public Program(ArrayList<FuncDecl> funcs)
        {
            this.funcs = funcs;
        }
        public Object Exec() throws Exception
        {
            ExecEnv execenv = new ExecEnv(funcs);                   // prepare running environment
            FuncDecl main = execenv.funcname_funcdecl.get("main");  // find main function
            Object ret = main.Exec(execenv, new Object[0]);         // run the main function
            return ret;                                             // return value
        }
        public String[] ToStringList(OptToString opt) throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            for(var func : funcs)
            {
                for(String str : func.ToStringList(opt))
                    strs.add(str);
            }
            return strs.toArray(String[]::new);
        }
    }

    public static class FuncDecl extends Node
    {
        public ParseTreeInfo.FuncDeclInfo info = new ParseTreeInfo.FuncDeclInfo(); // store your own data in FuncDeclInfo
        public String               ident     ;
        public TypeSpec             rettype   ;
        public ArrayList<Param    > params    ;
        public ArrayList<LocalDecl> localdecls;
        public ArrayList<Stmt     > stmtlist  ;
        public FuncDecl(String ident, TypeSpec rettype, ArrayList<Param> params, ArrayList<LocalDecl> localdecls, ArrayList<Stmt> stmtlist)
        {
            this.ident      = ident     ;
            this.rettype    = rettype   ;
            this.params     = params    ;
            this.localdecls = localdecls;
            this.stmtlist   = stmtlist  ;
        }
        public Object Exec(ExecEnv execenv, Object[] vals) throws Exception
        {
            // enter function environment
            execenv.PushStackFrame();
            Object ret;
            {
                // pass params' values into the running-function's stackframe
                for(int i=0; i<vals.length; i++)
                {
                    Param param = params.get(i);
                    if(param.reladdr == null)
                        throw new Exception("The "+i+"-th Param.reladdr is not assigned.");
                    execenv.GetTopStackFrame().SetValue(param.reladdr, vals[i]);
                }
            
                // call function's instructions
                for(Stmt stmt : stmtlist)
                {
                    Stmt.ExecStatus stat = stmt.Exec(execenv);
                    if(stat == Stmt.ExecStatus.Return)
                        break;
                }

                // get return value
                ret = execenv.GetTopStackFrame().GetValue(0);
            }
            // leave function environment
            execenv.PopStackFrame();

            return ret;
        }
        public String[] ToStringList(OptToString opt) throws Exception
        {
            String head = "func " + ident + "::" + rettype.ToString()+ "(";
            if(params.size() != 0) head += "";
            for(int i=0; i<params.size(); i++) {
                if (i == 0) head += params.get(i).ToString(opt);
                else head += ", " + params.get(i).ToString(opt);
            }
            if(params.size() != 0) head += "";
            head += ")";

            ArrayList<String> strs = new ArrayList<String>();
            strs.add(head);
            if(opt == OptToString.CommentExecEnv)
            {   // print comments for executing environment
                for(int i=0; i<params.size(); i++)
                {
                    Param param = params.get(i);
                    if(param.reladdr == null)
                        throw new Exception("The "+i+"-th Param.reladdr is not assigned.");
                    strs.add("// relative address of parameter "+param.ident+" from this function call base pointer is "+param.reladdr+"");
                }
            }
            strs.add("begin");
            for(var localdecl : localdecls)
                strs.add("    " + localdecl.ToString(opt));
            for(var stmt : stmtlist)
                for(String str : stmt.ToStringList(opt))
                    strs.add("    "+str);
            strs.add("end");

            return strs.toArray(String[]::new);
        }
    }

    public static class Param extends Node
    {
        public ParseTreeInfo.ParamInfo info = new ParseTreeInfo.ParamInfo(); // store your own data in ParamInfo
        public String   ident   ;
        public TypeSpec typespec;
        public Integer  reladdr = null; // assign this value later for running the parse tree
        public Param(String ident, TypeSpec typespec)
        {
            this.ident    = ident   ;
            this.typespec = typespec;
        }
        public String[] ToStringList(OptToString opt) { return new String[] { ToString(opt) }; }
        public String   ToString(OptToString opt)     { return ident + "::"  + typespec.ToString(); }
    }

    public static class LocalDecl extends Node
    {
        public ParseTreeInfo.LocalDeclInfo info = new ParseTreeInfo.LocalDeclInfo(); // store your own data in LocalDeclInfo
        public String   ident   ;
        public TypeSpec typespec;
        public Integer  reladdr = null; // assign this value later for running the parse tree
        public LocalDecl(String ident, TypeSpec typespec)
        {
            this.ident    = ident   ;
            this.typespec = typespec;
        }
        public String[] ToStringList(OptToString opt) { return new String[] { ToString(opt) }; }
        public String ToString(OptToString opt)
        {
            String str ="var " + typespec.ToString() + "::" + ident + ";";
            if(opt == OptToString.CommentExecEnv)
            {   // print comments for running environment
                str += " // relative address of local variable "+ident+" from this func call base pointer is "+reladdr+"";
            }
            return str;
        }
    }

    public abstract static class Stmt extends Node
    {
        public static enum ExecStatus
        {
            Normal,     // normal statement
            Return      // return function-call without running other statements
        }

        public ParseTreeInfo.StmtStmtInfo info = new ParseTreeInfo.StmtStmtInfo();  // store your own data in StmtStmtInfo
        abstract public ExecStatus Exec(ExecEnv execenv)       throws Exception;
        abstract public String[] ToStringList(OptToString opt) throws Exception;
    }
    public static class AssignStmt extends Stmt
    {
        public String  ident;
        public Integer ident_reladdr = null; // assign this value later for running the parse tree
        public Expr    expr ;
        public AssignStmt(String ident, Expr expr)
        {
            this.ident = ident;
            this.expr  = expr ;
        }
        public ExecStatus Exec(ExecEnv execenv) throws Exception
        {
            Object exprval = expr.Exec(execenv);                         // get value of expr
            execenv.GetTopStackFrame().SetValue(ident_reladdr, exprval); // update the value to ident_reladdr
            return ExecStatus.Normal;
        }
        public String[] ToStringList(OptToString opt) throws Exception 
        {
            String str = ident;
            if(opt == OptToString.CommentExecEnv)
            {   // print comments for running environment
                if(ident_reladdr == null)
                    throw new Exception("AssignStmt.ident_reladdr is not assigned.");
                str += "{addr:"+ident_reladdr+"}";
            }
            str  += " := " + expr.ToString(opt) + ";";
            return new String[] { str };
        }
    }
    public static class AssignStmtForArray extends Stmt
    {
        public String  ident;
        public Integer ident_reladdr = null; // assign this value later for running the parse tree
        public Expr    idx  ;
        public Expr    expr ;
        public AssignStmtForArray(String ident, Expr idx, Expr expr)
        {
            this.ident = ident;
            this.idx   = idx  ;
            this.expr  = expr ;
        }
        public ExecStatus Exec(ExecEnv execenv) throws Exception
        {
            // get value of expr
            Object exprval = expr.Exec(execenv);

            // get index of the array
            Object oidx = idx.Exec(execenv);
            double didx = ((Double)oidx);
            int    iidx = ((int)didx);

            // get array to assign value
            if(ident_reladdr == null)
                throw new Exception("AssignStmtForArray.ident_reladdr is not assigned.");
            if(execenv.GetTopStackFrame().HasValue(ident_reladdr) == false)
                throw new Exception("Unassigned variable is accessed.");
            Object[] ident_arr = (Object[])execenv.GetTopStackFrame().GetValue(ident_reladdr);
            if(iidx < 0 || ident_arr.length <= iidx)
                throw new Exception("An element is accessed that is out of the range of an array.");

            // update the value to arr[idx]
            ident_arr[iidx] = exprval;

            return ExecStatus.Normal;
        }
        public String[] ToStringList(OptToString opt) throws Exception
        {
            String str = ident;
            if(opt == OptToString.CommentExecEnv)
            {   // print comments for running environment
                if(ident_reladdr == null)
                    throw new Exception("AssignStmtForArray.ident_reladdr is not assigned.");
                str += "{addr:"+ident_reladdr+"}";
            }
            str  += "["+idx.ToString(opt)+"] := " + expr.ToString(opt) + ";";
            return new String[] { str };
        }
    }
    public static class PrintStmt extends Stmt
    {
        public Expr expr;
        public PrintStmt(Expr expr)
        {
            this.expr = expr;
        }
        public ExecStatus Exec(ExecEnv execenv) throws Exception
        {
            // get value
            Object exprval = expr.Exec(execenv);
            // print on screen
            if(     exprval instanceof Double  ) System.out.println(exprval);
            else if(exprval instanceof Boolean ) System.out.println(exprval);
            else if(exprval instanceof Object[]) {
                Object[] arr = (Object[])exprval;
                //System.out.print("array of " + arr.length + " elems:");
                System.out.print("[");
                boolean printdelim = false;
                for (int i=0; i<arr.length; i++) {
                    if(printdelim == true) System.out.print(", "+arr[i]);
                    else                   System.out.print(     arr[i]);
                    printdelim = true;
                }
                System.out.println("]");
            }
            return ExecStatus.Normal;
        }
        public String[] ToStringList(OptToString opt) throws Exception
        {
            return new String[]
            {
                "print " + expr.ToString(opt) + ";"
            };
        }
    }
    public static class ReturnStmt extends Stmt
    {
        public Expr expr;
        public ReturnStmt(Expr expr)
        {
            this.expr = expr;
        }
        public ExecStatus Exec(ExecEnv execenv) throws Exception
        {
            Object val = expr.Exec(execenv);             // evaluate the return value
            execenv.GetTopStackFrame().SetValue(0, val); // assign the return value at location 0
            return ExecStatus.Return;                    // exec status is "return"
        }
        public String[] ToStringList(OptToString opt) throws Exception
        {
            return new String[]
            {
                "return " + expr.ToString(opt) + ";"
            };
        }
    }
    public static class IfStmt extends Stmt
    {
        public Expr            cond        ;
        public ArrayList<Stmt> thenstmtlist;
        public ArrayList<Stmt> elsestmtlist;
        public IfStmt(Expr cond, ArrayList<Stmt> thenstmtlist, ArrayList<Stmt> elsestmtlist)
        {
            this.cond     = cond    ;
            this.thenstmtlist = thenstmtlist;
            this.elsestmtlist = elsestmtlist;
        }
        public ExecStatus Exec(ExecEnv execenv) throws Exception
        {
            // get the conditional value
            Object condval = cond.Exec(execenv);

            // if the condition is true, exec thenstmts
            // if not                  , exec elsestmts
            if((boolean)condval)
            {
                for(Stmt stmt : thenstmtlist)
                {
                    ExecStatus stat = stmt.Exec(execenv);
                    if(stat == ExecStatus.Return)
                        return ExecStatus.Return;
                }
            }
            else
            {
                for(Stmt stmt : elsestmtlist)
                {
                    ExecStatus stat = stmt.Exec(execenv);
                    if(stat == ExecStatus.Return)
                        return ExecStatus.Return;
                }
            }

            return ExecStatus.Normal;
        }
        public String[] ToStringList(OptToString opt) throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            strs.add("if " + cond.ToString(opt) + " then");
            for(Stmt stmt : thenstmtlist)
                for(String str : stmt.ToStringList(opt))
                    strs.add("    "+str);
            strs.add("else");
            for(Stmt stmt : elsestmtlist)
                for(String str : stmt.ToStringList(opt))
                    strs.add("    "+str);
            strs.add("end");
            return strs.toArray(String[]::new);
        }
    }
    public static class WhileStmt extends Stmt
    {
        public Expr            cond    ;
        public ArrayList<Stmt> stmtlist;
        public WhileStmt(Expr cond, ArrayList<Stmt> stmtlist)
        {
            this.cond     = cond    ;
            this.stmtlist = stmtlist;
        }
        public ExecStatus Exec(ExecEnv execenv) throws Exception
        {
            while(true)
            {
                // if condition is false, break
                // if not               , repeat
                Object condval = cond.Exec(execenv);
                if((boolean)condval == false)
                    break;

                // exec stat
                for(Stmt stmt : stmtlist)
                {
                    ExecStatus stat = stmt.Exec(execenv);
                    if(stat == ExecStatus.Return)
                        return ExecStatus.Return;
                }
            }
            return ExecStatus.Normal;
        }
        public String[] ToStringList(OptToString opt) throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            strs.add("while " + cond.ToString(opt) + "");
            strs.add("begin");
            for(Stmt stmt : stmtlist)
                for(String str : stmt.ToStringList(opt))
                    strs.add("    "+str);
            strs.add("end");
            return strs.toArray(String[]::new);
        }
    }
    public static class CompoundStmt extends Stmt
    {
        public ArrayList<LocalDecl> localdecls;
        public ArrayList<Stmt     > stmtlist  ;
        public CompoundStmt(ArrayList<LocalDecl> localdecls, ArrayList<Stmt> stmtlist)
        {
            this.localdecls = localdecls;
            this.stmtlist   = stmtlist  ;
        }
        public ExecStatus Exec(ExecEnv execenv) throws Exception
        {
            for(Stmt stmt : stmtlist)
            {
                // exec each stmt
                ExecStatus stat = stmt.Exec(execenv);

                // if exec_status was return,
                // then steop exec, and return with "return" to prevent executing other statements
                if(stat == ExecStatus.Return)           
                    return ExecStatus.Return;
            }
            return ExecStatus.Normal;
        }
        public String[] ToStringList(OptToString opt) throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            strs.add("begin");
            for(LocalDecl localdecl : localdecls)
                strs.add("    " + localdecl.ToString(opt));
            for(Stmt stmt : stmtlist)
                for(String str : stmt.ToStringList(opt))
                    strs.add("    "+str);
            strs.add("end");
            return strs.toArray(String[]::new);
        }
    }

    public static class Arg extends Node
    {
        public ParseTreeInfo.ArgInfo info = new ParseTreeInfo.ArgInfo(); // store your own data in ArgInfo
        public Expr expr;
        public Arg(Expr expr)                                          { this.expr = expr;       }
        public Object Exec(ExecEnv execenv)           throws Exception { return expr.Exec(execenv); }
        public String ToString(OptToString opt)       throws Exception { return expr.ToString(opt); }
        public String[] ToStringList(OptToString opt) throws Exception { return new String[] { ToString(opt) }; }
    }

    public static abstract class Expr extends Node
    {
        public ParseTreeInfo.ExprInfo info = new ParseTreeInfo.ExprInfo(); // store your own data in ExprInfo
        abstract public Object Exec(ExecEnv execenv)     throws Exception;
        abstract public String ToString(OptToString opt) throws Exception;
        public String[] ToStringList(OptToString opt)    throws Exception { return new String[] { ToString(opt) }; }
    }
    public static class ExprBoolLit extends Expr
    {
        boolean val;
        public ExprBoolLit(boolean val)         { this.val = val; }
        public Object Exec(ExecEnv execenv)     { return val; }
        public String ToString(OptToString opt) { return ""+val; }
    }
    public static class ExprNumLit extends Expr
    {
        Double val;
        public ExprNumLit(Double val)           { this.val = val; }
        public Object Exec(ExecEnv execenv)     { return val; }
        public String ToString(OptToString opt) { return ""+val; }
    }
    public static class ExprIdent extends Expr
    {
        public String   ident;
        public Integer  reladdr = null; // assign this value later for running the parse tree
        public ExprIdent  (String ident)  { this.ident = ident; }
        public Object Exec(ExecEnv execenv) throws Exception
        {
            if(reladdr == null)
                throw new Exception("ExprIdent.reladdr is not assigned.");
            if(execenv.GetTopStackFrame().HasValue(reladdr) == false)
                throw new Exception("Unassigned variable is accessed.");
            Object val = execenv.GetTopStackFrame().GetValue(reladdr);
            return val;
        }
        public String ToString(OptToString opt) throws Exception
        {
            String str = ident;
            if(opt == OptToString.CommentExecEnv)
            {   // print comments for running environment
                if(reladdr == null)
                    throw new Exception("ExprIdent.reladdr is not assigned.");
                str += "{addr:"+reladdr+"}";
            }
            return str;
        }
    }
    public static class ExprNewArray extends Expr
    {
        TypeSpec elemtype;
        Expr     expr    ;
        public ExprNewArray(TypeSpec elemtype, Expr expr)  { this.elemtype = elemtype; this.expr = expr; }
        public Object Exec(ExecEnv execenv) throws Exception
        {
            Object onum = expr.Exec(execenv);
            double dnum = (Double)onum;
            int    inum = (int)dnum;
            return new Object[inum];
        }
        public String ToString(OptToString opt) throws Exception
        {
            String str = "new " + elemtype.typename + "[" + expr.ToString(opt)+"]";
            return str;
        }
    }
    public static class ExprArrayElem extends Expr
    {
        public String   ident;
        public Integer  reladdr = null; // assign this value later for running the parse tree
        public Expr     expr;
        public ExprArrayElem(String ident, Expr expr)  { this.ident = ident; this.expr = expr; }
        public Object Exec(ExecEnv execenv) throws Exception
        {
            Object oidx = expr.Exec(execenv);
            double didx = ((Double)oidx);
            int    iidx = ((int)didx);

            if(reladdr == null)
                throw new Exception("ExprArrayElem.reladdr is not assigned.");
            if(execenv.GetTopStackFrame().HasValue(reladdr) == false)
                throw new Exception("Unassigned variable is accessed.");
            Object[] arr = (Object[])execenv.GetTopStackFrame().GetValue(reladdr);
            if(iidx < 0 || arr.length <= iidx)
                throw new Exception("An element is accessed that is out of the range of an array.");

            return arr[iidx];
        }
        public String ToString(OptToString opt) throws Exception
        {
            String str = ident+"["+expr.ToString(opt)+"]";
            return str;
        }
    }
    public static class ExprArraySize extends Expr
    {
        public String   ident;
        public Integer  reladdr = null; // assign this value later for running the parse tree
        public ExprArraySize(String ident)  { this.ident = ident; }
        public Object Exec(ExecEnv execenv) throws Exception
        {
            if(reladdr == null)
                throw new Exception("ExprArraySize.reladdr is not assigned.");
            if(execenv.GetTopStackFrame().HasValue(reladdr) == false)
                throw new Exception("Unassigned variable is accessed.");
            Object[] arr = (Object[])execenv.GetTopStackFrame().GetValue(reladdr);

            return (arr.length * 1.0);
        }
        public String ToString(OptToString opt) throws Exception
        {
            String str = ident+".size";
            return str;
        }
    }
    public static class ExprFuncCall extends Expr
    {
        public String         ident;
        public ArrayList<Arg> args ;

        public ExprFuncCall(String ident, ArrayList<Arg> args)
        {
            this.ident = ident;
            this.args  = args ;
        }
        public Object Exec(ExecEnv execenv) throws Exception
        {
            FuncDecl func = execenv.funcname_funcdecl.get(ident);

            // calculate argument values
            assert(args.size() == func.params.size());
            Object[] vals = new Object[args.size()];
            for(int i=0; i<vals.length; i++)
                vals[i] = args.get(i).Exec(execenv);

            // call the function with passing the calculated arguments
            Object   ret  = func.Exec(execenv, vals);
            
            return   ret;
        }
        public String ToString(OptToString opt) throws Exception
        {
            String str = "" + ident + "(";
            if(args.size() != 0) str += "";
            for(int i=0; i<args.size(); i++)
                if(i == 0) str +=      args.get(i).ToString(opt);
                else       str += ","+args.get(i).ToString(opt);
            if(args.size() != 0) str += "";
            str += ")";
            return str;
        }
    }
    public static abstract class ExprUnary  extends Expr
    {
        public Expr op;
        public ExprUnary (Expr op)
        {
            this.op = op;
        }
        public String ToString(OptToString opt) throws Exception
        {
            String str = "";
            str += OperString();
            str += op.ToString(opt);
            return str;
        }
        public Object Exec(ExecEnv execenv) throws Exception
        {
            Object val = op.Exec(execenv);
            Object ret = Exec(val);
            return ret;
        }
        public abstract String OperString();
        public abstract Object Exec(Object val);
    }
    public static abstract class ExprBinary extends Expr
    {
        public Expr op1;
        public Expr op2;
        public ExprBinary(Expr op1, Expr op2)
        {
            this.op1 = op1;
            this.op2 = op2;
        }
        public String ToString(OptToString opt) throws Exception
        {
            String str = "";
            str += op1.ToString(opt);
            str += OperString();
            str += op2.ToString(opt);
            return str;
        }
        public Object Exec(ExecEnv execenv) throws Exception
        {
            Object val1 = op1.Exec(execenv);
            Object val2 = op2.Exec(execenv);
            Object ret  = Exec(val1, val2);
            return ret;
        }
        public abstract String OperString();
        public abstract Object Exec(Object val1, Object val2);
    }
    public static class ExprAdd extends ExprBinary { public ExprAdd(Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Double )val1 +  (Double )val2); } public String OperString() { return " + " ; } }
    public static class ExprSub extends ExprBinary { public ExprSub(Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Double )val1 -  (Double )val2); } public String OperString() { return " - " ; } }
    public static class ExprMul extends ExprBinary { public ExprMul(Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Double )val1 *  (Double )val2); } public String OperString() { return " * " ; } }
    public static class ExprDiv extends ExprBinary { public ExprDiv(Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Double )val1 /  (Double )val2); } public String OperString() { return " / " ; } }
    public static class ExprMod extends ExprBinary { public ExprMod(Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Double )val1 %  (Double )val2); } public String OperString() { return " % " ; } }
    public static class ExprAnd extends ExprBinary { public ExprAnd(Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Boolean)val1 && (Boolean)val2); } public String OperString() { return " and ";} }
    public static class ExprOr  extends ExprBinary { public ExprOr (Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Boolean)val1 || (Boolean)val2); } public String OperString() { return " or "; } }
    public static class ExprNot extends ExprUnary  { public ExprNot(Expr op           ) { super(op      ); } public Object Exec(Object val              ) { return (!(Boolean)val                  ); } public String OperString() { return "not "; } }
    public static class ExprEq  extends ExprBinary { public ExprEq (Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return (          val1 .equals(   val2)); } public String OperString() { return " = " ; } }
    public static class ExprNe  extends ExprBinary { public ExprNe (Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return (!         val1 .equals(   val2)); } public String OperString() { return " != "; } }
    public static class ExprLe  extends ExprBinary { public ExprLe (Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Double )val1 <= (Double )val2); } public String OperString() { return " <= "; } }
    public static class ExprLt  extends ExprBinary { public ExprLt (Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Double )val1 <  (Double )val2); } public String OperString() { return " < " ; } }
    public static class ExprGe  extends ExprBinary { public ExprGe (Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Double )val1 >= (Double )val2); } public String OperString() { return " >= "; } }
    public static class ExprGt  extends ExprBinary { public ExprGt (Expr op1, Expr op2) { super(op1, op2); } public Object Exec(Object val1, Object val2) { return ( (Double )val1 >  (Double )val2); } public String OperString() { return " > " ; } }
    public static class ExprParen  extends Expr
    {
        public Expr op1;
        public ExprParen (Expr op1)
        {
            this.op1 = op1;
        }
        public String ToString(OptToString opt) throws Exception
        {
            String str = op1.ToString(opt);
            str = "(" + str + ")";
            return str;
        }
        public Object Exec(ExecEnv execenv) throws Exception
        {
            Object ret = op1.Exec(execenv);
            return ret;
        }
    }
    //public static class ExprOper extends Expr
    //{
    //    public Expr   op1 ;
    //    public Expr   op2 ;
    //    public String oper;
    //    public ExprOper(String oper, Expr op1, Expr op2)
    //    {
    //        this.op1  = op1 ;
    //        this.op2  = op2 ;
    //        this.oper = oper;
    //    }
    //    public Object Exec(RunEnv execenv) throws Exception
    //    {
    //        Object val1 = null; if(op1 != null) val1 = op1.Exec(execenv);
    //        Object val2 = null; if(op2 != null) val2 = op2.Exec(execenv);
    //        switch(oper)
    //        {
    //            case "+"  : return  ((Integer)val1 +  (Integer)val2 );
    //            case "-"  : return  ((Integer)val1 -  (Integer)val2 );
    //            case "*"  : return  ((Integer)val1 *  (Integer)val2 );
    //            case "/"  : return  ((Integer)val1 /  (Integer)val2 );
    //            case "%"  : return  ((Integer)val1 %  (Integer)val2 );
    //            case "and": return  ((Boolean)val1 && (Boolean)val2 );
    //            case "or" : return  ((Boolean)val1 || (Boolean)val2 );
    //            case "not": return !((Boolean)val1                  );
    //            case "="  : return  ((        val1).equals(    val2));
    //            case "!=" : return !((        val1).equals(    val2));
    //            case "<=" : return  ((Integer)val1 <= (Integer)val2 );
    //            case "<"  : return  ((Integer)val1 <  (Integer)val2 );
    //            case ">=" : return  ((Integer)val1 >= (Integer)val2 );
    //            case ">"  : return  ((Integer)val1 >  (Integer)val2 );
    //            case "()" : return  (         val1                  );
    //        }
    //        return null;
    //    }
    //    public String ToString(int opt) throws Exception
    //    {
    //        String str1 = null; if(op1 != null) str1 = op1.ToString(opt);
    //        String str2 = null; if(op2 != null) str2 = op2.ToString(opt);
    //        switch(oper)
    //        {
    //            case "+"  : return (       str1+" + "  +str2);
    //            case "-"  : return (       str1+" - "  +str2);
    //            case "*"  : return (       str1+" * "  +str2);
    //            case "/"  : return (       str1+" / "  +str2);
    //            case "%"  : return (       str1+" % "  +str2);
    //            case "and": return (       str1+" and "+str2);
    //            case "or" : return (       str1+" or " +str2);
    //            case "not": return ("not "+str1             );
    //            case "="  : return (       str1+" = "  +str2);
    //            case "!=" : return (       str1+" != " +str2);
    //            case "<=" : return (       str1+" <= " +str2);
    //            case "<"  : return (       str1+" < "  +str2);
    //            case ">=" : return (       str1+" >= " +str2);
    //            case ">"  : return (       str1+" > "  +str2);
    //            case "()" : return (  "( "+str1+" )"        );
    //        }
    //        return null;
    //    }
    //}
}
