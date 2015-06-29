package me.scai.parsetree.scientific;

import me.scai.parsetree.evaluation.ValueUnion;
import org.jscience.physics.amount.Constants;

import java.util.Map;

public class ScientificConstants {
    public static final void inject2VariableMap(Map<String, ValueUnion> varMap) {
        varMap.put("gr_pi",   new ValueUnion(Math.PI));        // Pi
        varMap.put("e",       new ValueUnion(Math.E));         // e, base of natural logs

        varMap.put("c",       new ValueUnion(Constants.c));    // Speed of light
        varMap.put("h",       new ValueUnion(Constants.ℎ));    // Planck constant
        varMap.put("hbar",    new ValueUnion(Constants.ℏ));    // Planck constant over 2*pi
        varMap.put("N_A",     new ValueUnion(Constants.N));    // Avogadro's number
        varMap.put("G",       new ValueUnion(Constants.G));    // Newtonian constant of gravitatino
    }
}
