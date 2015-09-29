package me.scai.parsetree.evaluation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlatoVarMap {
    private Map<String, ValueUnion> varMap;

    /* Constructors */
    public PlatoVarMap() {
        this.varMap = new HashMap<String, ValueUnion>();
    }

    public PlatoVarMap(Map<String, ValueUnion> varMap) {
        this.varMap = varMap;
    }

    /* Methods */
    public List<String> getVarNamesSorted() {
//        List<String> varNamesSorted = new ArrayList<String>();
//        ((ArrayList<String>) varNamesSorted).ensureCapacity(varMap.size());
        String[] varNamesSorted = new String[varMap.size()];

        int i = 0;
        for (Map.Entry<String, ValueUnion> varEntry : varMap.entrySet()) {
            varNamesSorted[i++] = varEntry.getKey();
        }

        Arrays.sort(varNamesSorted);

        return Arrays.asList(varNamesSorted);
    }

    public void addVar(String varName, ValueUnion varValue) {
        varMap.put(varName, varValue);
    }

    public int numVars() {
        return varMap.size();
    }

    public boolean containsVarName(String varName) {
        if (varMap.containsKey(varName)) {
            return true;
        } else {
            if ( varName.indexOf("funcArg") != -1 &&
                 varName.substring(varName.indexOf("funcArg")).indexOf("__") !=
                 varName.substring(varName.indexOf("funcArg")).lastIndexOf("__") ) {
                String[] parts = varName.split("__");
                String varName1 = String.format("__%s__%s__", parts[1], parts[2]);

                return varMap.containsKey(varName1);
            } else {
                return false;
            }

        }
    }

    public ValueUnion getVarValue(String varName) {
        if (varMap.containsKey(varName)) {
            return varMap.get(varName);
        } else {
            if ( varName.substring(varName.indexOf("funcArg")).indexOf("__") !=
                    varName.substring(varName.indexOf("funcArg")).lastIndexOf("__") ) {
                String[] parts = varName.split("__");
                String varName1 = String.format("__%s__%s__", parts[1], parts[2]);

                if (varMap.containsKey(varName1)) {
                    return varMap.get(varName1);
                } else {
                    return null;
                }
            } else {
                return null;
            }

        }

//        if (varMap.containsKey(varName)) {
//
//        } else {
//            return null;
//        }
    }

    public void removeVar(String varName) {
        if (varName == null) {
            throw new IllegalStateException("Encountered null variable name");
        }
        if (varMap.containsKey(varName)) {
            varMap.remove(varName);
        }
    }

    public void clear() {
        varMap.clear();
    }
}
