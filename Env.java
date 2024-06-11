//----------------------------------------------
// Authors:    Quinn Trate & Maliha Doria
// Date:       April 23, 2024
// Class:      CMPSC 470 Section 1: Compilers
// Instructor: Dr. Hyuntae Na
// Purpose:    Constructor for Linked List
//             of Symbol Tables. Includes
//             Setter and Getter
//----------------------------------------------

import java.util.ArrayList;
import java.util.HashMap;

public class Env
{
    public Env prev;
    private HashMap<String, Object> symbols;
    
    public Env(Env prev)
    {
        this.prev = prev;
        this.symbols = new HashMap<String, Object>();
    }
    
    public void Put(String name, Object value)
    {
        symbols.put(name, value);
    }
    
    public Object Get(String name)
    {
        if(symbols.containsKey(name))
            return symbols.get(name);
        else if(prev == null)
            return null;
        else
            return prev.Get(name);
    }
    
    public Object GetCurr(String name) { return symbols.get(name); }
}
class EnvInfo
{
    public String name;
    public String type;
    public String value;
    public ArrayList<String> params;
    public int reladdr;
    public boolean isFunction;

    public EnvInfo(String name, String type, String value, ArrayList<String> params, int reladdr)
    {
        this.name = name;
        this.type = type;
        this.value = value;
        this.params = params;
        this.reladdr = reladdr;
        this.isFunction =  false;
    }

    public EnvInfo(String name, String type, String value, ArrayList<String> params, int reladdr, boolean isFunction)
    {
        this.name = name;
        this.type = type;
        this.value = value;
        this.params = params;
        this.isFunction = isFunction;
    }
}
