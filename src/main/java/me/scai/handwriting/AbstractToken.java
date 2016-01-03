package me.scai.handwriting;

import me.scai.parsetree.TerminalSet;

import java.util.List;

public abstract class AbstractToken {
    /* Member variables */
    protected float [] tokenBounds = new float[4];
    public float width = 0f;
    public float height = 0f;

    /* The type of a token, according to the terminal set, if applicable.
     * For a node token, the terminal type will be null */
    public List<String> tokenTermTypes = null;

    /* Constructor */
    public AbstractToken() {
        initializeTokenBounds();
    }

    /* Methods */
    /* Abstract methods */

    /**
     * Get the result of the token-level recognition or token set-level parsing
     */
    public abstract String getRecogResult();

    /** Set the result of the token-level recogniton or token set-level parsing */
    public abstract void setRecogResult(String rw);


    public abstract float getCentralX();
    public abstract float getCentralY();
    public abstract void getTokenTerminalType(TerminalSet termSet);

    /* Concrete methods */
    /* Get the bounds: min_x, min_y, max_x, max_y */
    public float [] getBounds() {
        return tokenBounds;
    }

    public void setBounds(float [] bounds) {
        this.tokenBounds = bounds;
    }

    protected void initializeTokenBounds() {
	    /* Initialize the token bounds: [min_x, min_y, max_x, max_y] */
        tokenBounds[0] = Float.POSITIVE_INFINITY;
        tokenBounds[1] = Float.POSITIVE_INFINITY;
        tokenBounds[2] = Float.NEGATIVE_INFINITY;
        tokenBounds[3] = Float.NEGATIVE_INFINITY;
    }

    public void clear() {
        initializeTokenBounds();

        width = 0f;
        height = 0f;
    }
}
