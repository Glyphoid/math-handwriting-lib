package me.scai.parsetree.evaluation;

public class Undefined {
    /* Singleton instance */
    private static Undefined instance;

    /* Constructor: private as per singleton pattern */
    private Undefined() {}

    /* Singleton access */
    public static Undefined getInstance() {
        if (instance == null) {
            instance = new Undefined();
        }

        return instance;
    }
}
