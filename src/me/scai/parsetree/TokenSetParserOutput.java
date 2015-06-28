package me.scai.parsetree;

public class TokenSetParserOutput {
    /* TODO: Add parse tree */
    private String stringizerOutput;
    private String evaluatorOutput;
    private String mathTex;

    private String errorMsg;

    public TokenSetParserOutput(String stringizerOutput, String evaluatorOutput, String mathTex) {
        this.stringizerOutput = stringizerOutput;
        this.evaluatorOutput = evaluatorOutput;
        this.mathTex = mathTex;
    }

    public TokenSetParserOutput(String errorMsg) {
        this.errorMsg = errorMsg;
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

    public String getErrorMsg() {
        return errorMsg;
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

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
