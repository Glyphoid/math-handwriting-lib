package me.scai.parsetree;

//import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.handwriting.CWrittenTokenSetNoStroke;

public interface ITokenSetParser {
	Node parse(CWrittenTokenSetNoStroke tokenSet)
            throws TokenSetParserException, InterruptedException; /* Return reference to root node */

    /**
     * Enable or disabled production;
     * @param prodIdx: 0-based production index as it appears in the production configuration file (e.g,. productions.txt)
     */
    void setProductionEnabled(int prodIdx);
}
