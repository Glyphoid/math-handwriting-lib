package me.scai.parsetree.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import me.scai.parsetree.Node;

public class EvaluatorHelper {
//    static final Pattern argNamePatternPart = Pattern.compile("__funcArg[0-9]+__");
    static final Pattern argNamePattern = Pattern.compile("__stack[0-9]+__funcArg[0-9]+__.*__");
    
    public static String genInternalFuncArgName(int stackHeight, int argIdx, String argOrigName) {
//        return String.format("__funcArg%d__", argIdx);
//        return genFuncArgName(stackHeight - 1, argIdx);
        return genFuncArgName(stackHeight - 1, argIdx, argOrigName);
    }
    
    public static String genFuncArgName(int stackPos, int argIdx, String argOrigName) {
        if (argOrigName != null) {
            return String.format("__stack%d__funcArg%d__%s__", stackPos, argIdx, argOrigName);
        } else {
            return String.format("__stack%d__funcArg%d__", stackPos, argIdx);
        }
    }

    /**
     * Get first encountered terminal name
     * @param n
     * @return
     */
    public static String getFirstTermName(Node n) {
        if (n.isTerminal()) {
            return n.termName;
        } else {
            for (int i = 0; i < n.ch.length; ++i) {
                return getFirstTermName(n.ch[i]);
            }
        }

        return null;
    }
    
//    public static String [] genFuncArgNames(int stackPos, int numArgs) {
//        String [] funcArgNames = new String[numArgs];
//
//        for (int i = 0; i < numArgs; ++i) {
//            funcArgNames[i] = genFuncArgName(stackPos, i);
//        }
//
//        return funcArgNames;
//    }

    public static String [] genFuncArgNames(int stackPos, FunctionArgumentList argList) {
        String [] funcArgNames = new String[argList.numArgs()];

        for (int i = 0; i < argList.numArgs(); ++i) {
            if (argList.args.get(i).getClass() == String.class){
                String argName = (String) argList.args.get(i); // TODO: Is this kosher?
                funcArgNames[i] = genFuncArgName(stackPos, i, argName);
            } else {
                funcArgNames[i] = genFuncArgName(stackPos, i, null);
            }

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
        Matcher matcher     = argNamePattern.matcher(name);
//        Matcher partMatcher = argNamePatternPart.matcher(name);
        
        if ( !(matcher.matches()) ) {
            return -1;
        } else {
            String[] parts = matcher.group(0).split("__");
            return Integer.valueOf(parts[2].replace("funcArg", ""));
//            String match = matcher.group(0);
//            int idx = match.indexOf("_funcArg");    // TODO: Better approach
//            String part = match.substring(idx);
//            return Integer.valueOf(part.replace("_funcArg", "").replace("__", ""));
//
//            return Integer.valueOf(match.replace("__funcArg", "").replace("__", ""));
        }
    }
    
    /* Replace the VARIALBE --> VARIALBE_SYMBOL nodes in the parse tree 
     * with special symbol names such as __funcArg0__ and __funcArg1__.
     */
    public static void functionizeBody(Node node, int stackHeight, List<String> origArgNames) {
        if (stackHeight <= 0) {
            throw new IllegalStateException("Unexpected call to functionizeBody() while the function call stack is empty");
        }

        // TODO: Accommodate nested sigma/pi/integ: Do a first pass to determine what variable names are already functionized
        // TODO: Perhaps the functionziation needs to take into account the current stack position

        /* Implementation uses recursion */
        if (node.isTerminal()) {
            if (node.lhs.equals("VARIABLE") && 
                origArgNames.contains(node.termName)) {
                int argIdx = origArgNames.indexOf(node.termName); 
                node.termName = genInternalFuncArgName(stackHeight, argIdx, node.termName); //TODO: Remove
//                node.auxTermName = genInternalFuncArgName(argIdx);
            }
        }
        else {
            /* Call recursively */
            int nc = node.numChildren();
            for (int i = 0; i < nc; ++i) {
                functionizeBody(node.ch[i], stackHeight, origArgNames);
            }
        }
        
    }
}
