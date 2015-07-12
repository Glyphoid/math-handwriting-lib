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
        return varMap.containsKey(varName);
    }

    public ValueUnion getVarValue(String varName) {
        if (varMap.containsKey(varName)) {
            return varMap.get(varName);
        } else {
            return null;
        }
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
