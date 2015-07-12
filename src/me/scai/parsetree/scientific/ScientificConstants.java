package me.scai.parsetree.scientific;

import me.scai.parsetree.evaluation.PlatoVarMap;
import me.scai.parsetree.evaluation.ValueUnion;
import org.jscience.physics.amount.Constants;

public class ScientificConstants {
    public static final void inject2VariableMap(PlatoVarMap varMap) {
        varMap.addVar("gr_pi",   new ValueUnion(Math.PI, "ratio of a circle's circumference to its diameter"));        // Pi
        varMap.addVar("e",       new ValueUnion(Math.E,  "base of natural log"));         // e, base of natural logs

        varMap.addVar("c",       new ValueUnion(Constants.c, "speed of light in vacuum"));    // Speed of light
        varMap.addVar("h",       new ValueUnion(Constants.ℎ, "Planck constant"));    // Planck constant
        varMap.addVar("hbar",    new ValueUnion(Constants.ℏ, "Planck constant divided by 2 * pi"));    // Planck constant over 2*pi
        varMap.addVar("N_A",     new ValueUnion(Constants.N, "Avogadro constant"));    // Avogadro's number
        varMap.addVar("G",       new ValueUnion(Constants.G, "gravitational constant"));    // Newtonian constant of gravitatino
    }
}
