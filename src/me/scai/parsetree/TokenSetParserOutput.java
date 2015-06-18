package me.scai.parsetree;

/**
 * Created by scai on 5/11/2015.
 */
public class TokenSetParserOutput {
    /* TODO: Add parse tree */
    private String stringizerOutput;
    private String evaluatorOutput;
    private String mathTex;

    public TokenSetParserOutput(String stringizerOutput, String evaluatorOutput, String mathTex) {
        this.stringizerOutput = stringizerOutput;
        this.evaluatorOutput = evaluatorOutput;
        this.mathTex = mathTex;
    }

    /* Getters */
    public String getStringizerOutput() {
        return stringizerOutput;
    }

    public String getEvaluatorOutput() {
        return evaluatorOutput;
    }

    public String getMathTex() {
        return mathTex;
    }

    /* Setters */
    public void setStringizerOutput(String stringizerOutput) {
        this.stringizerOutput = stringizerOutput;
    }

    public void setEvaluatorOutput(String evaluatorOutput) {
        this.evaluatorOutput = evaluatorOutput;
    }

    public void setMathTex(String mathTex) {
        this.mathTex = mathTex;
    }
}
