package me.scai.parsetree.evaluation;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import me.scai.parsetree.Node;

public class EvaluatorHelper {
    static final Pattern argNamePattern = Pattern.compile("__funcArg[0-9]+__");
    
    public static String genInternalFuncArgName(int argIdx) {
        return String.format("__funcArg%d__", argIdx);
    }
    
    public static String genFuncArgName(int stackPos, int argIdx) {
        return String.format("__stack%d_funcArg%d__", stackPos, argIdx);
    }
    
    public static String [] genFuncArgNames(int stackPos, int numArgs) {
        String [] funcArgNames = new String[numArgs];
        
        for (int i = 0; i < numArgs; ++i) {
            funcArgNames[i] = genFuncArgName(stackPos, i);
        }
        
        return funcArgNames;
    }
    
    /* Get argument index from temporary internal argument names such as 
     * "__funcArg0__". 
     * 
     * @return  -1 if the argument name is not temporary internal
     *          int >= 0 if the argument name is temporary internal 
     * */
    public static int getArgIdx(String name) {
        Matcher matcher = argNamePattern.matcher(name);
        
        if (!matcher.matches()) {
            return -1;
        }
        else {
            String match = matcher.group(0);
            return Integer.valueOf(match.replace("__funcArg", "").replace("__", ""));
        }
    }
    
    /* Replace the VARIALBE --> VARIALBE_SYMBOL nodes in the parse tree 
     * with special symbol names such as __funcArg0__ and __funcArg1__.
     */
    public static void functionizeBody(Node node, List<String> origArgNames) {
        /* Implementation uses recursion */
        if (node.isTerminal()) {
            if (node.lhs.equals("VARIABLE") && 
                origArgNames.contains(node.termName)) {
                int argIdx = origArgNames.indexOf(node.termName); 
                node.termName = genInternalFuncArgName(argIdx); //TODO: Remove
//                node.auxTermName = genInternalFuncArgName(argIdx);
            }
        }
        else {
            /* Call recursively */
            int nc = node.numChildren();
            for (int i = 0; i < nc; ++i) {
                functionizeBody(node.ch[i], origArgNames);
            }
        }
        
    }
}
