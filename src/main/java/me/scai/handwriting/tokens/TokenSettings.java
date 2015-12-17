package me.scai.handwriting.tokens;

import me.scai.handwriting.TokenDegeneracy;

import java.util.Arrays;

public class TokenSettings {
    /* Member variables */
    private boolean includeTokenSize;
    private boolean includeTokenWHRatio;
    private boolean includeTokenNumStrokes;

    private String [] hardCodedTokens;

    // Number of discrete points per token used when generating stroke direction vector (SDV)
    private int npPerStroke;

    // Maximum number of strokes
    private int maxNumStrokes;

    private TokenDegeneracy tokenDegeneracy;

    /* Constructor */
    public TokenSettings(boolean includeTokenSize,
                         boolean includeTokenWHRatio,
                         boolean includeTokenNumStrokes,
                         String[] hardCodedTokens,
                         int npPerStroke,
                         int maxNumStrokes,
                         TokenDegeneracy tokenDegeneracy) {
        this.includeTokenSize = includeTokenSize;
        this.includeTokenWHRatio = includeTokenWHRatio;
        this.includeTokenNumStrokes = includeTokenNumStrokes;
        this.hardCodedTokens = hardCodedTokens;
        this.npPerStroke = npPerStroke;
        this.maxNumStrokes = maxNumStrokes;
        this.tokenDegeneracy = tokenDegeneracy;
    }

    /* Getters */
    public boolean isIncludeTokenSize() {
        return includeTokenSize;
    }

    public boolean isIncludeTokenWHRatio() {
        return includeTokenWHRatio;
    }

    public boolean isIncludeTokenNumStrokes() {
        return includeTokenNumStrokes;
    }

    public String[] getHardCodedTokens() {
        return hardCodedTokens;
    }

    public int getNpPerStroke() {
        return npPerStroke;
    }

    public int getMaxNumStrokes() {
        return maxNumStrokes;
    }

    public TokenDegeneracy getTokenDegeneracy() {
        return tokenDegeneracy;
    }

    /* Other methods */
    public boolean isTokenHardCoded(String token) {
        if (hardCodedTokens == null) {
            return false;
        } else {
            return Arrays.asList(hardCodedTokens).indexOf(token) != -1;
        }

    }
}
