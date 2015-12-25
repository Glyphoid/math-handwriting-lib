package me.scai.parsetree;

import me.scai.handwriting.AbstractToken;
import me.scai.handwriting.CWrittenTokenSetNoStroke;
import me.scai.handwriting.NodeToken;

public class TokenSet2NodeTokenParser {
    // Member variables
    final private ITokenSetParser parser;
    final private ParseTreeStringizer stringizer;

    // Constructor
    public TokenSet2NodeTokenParser(ITokenSetParser parser, ParseTreeStringizer stringizer) {
        this.parser = parser;
        this.stringizer = stringizer;
    }

    // Methods

    /**
     * Parse an entire token set and return a node token
     * @param tokenSet The input token set. It will not be modified.
     * @return The NodeToken, with node, wtSet, recognition, and possibly other fields set.
     */
    public NodeToken parse2NodeToken(CWrittenTokenSetNoStroke tokenSet)
            throws TokenSetParserException, InterruptedException {
        Node node = parser.parse(tokenSet);

        if (node == null) {
            throw new TokenSetParserException("Generation of NodeToken from token set failed due to failure to parse the token set");
        }

        NodeToken nodeToken = new NodeToken(node, tokenSet);

        // Add the recognition result
        nodeToken.setRecogResult(stringizer.stringize(node));

        return nodeToken;
    }

    /**
     * Parse a subset of the tokens in a token set and turn them into a NodeToken. The remaining tokens are
     * preserved. The NodeToken is placed at the front of the token list of the token set, after all the other node
     * tokens that already exist.
     * @param tokenSet      Input token set. It is not meant to be modified. Instead, a new token set will be returned.
     * @param tokenIndices  Indices to the tokens to be parsed into a node token (0-based)
     * @return              Token set with the NodeToken and the un-parsed tokens included
     */
    public CWrittenTokenSetNoStroke parseAsNodeToken(CWrittenTokenSetNoStroke tokenSet, int[] tokenIndices)
            throws TokenSetParserException, InterruptedException{

        CWrittenTokenSetNoStroke subsetToParse = tokenSet.fromSubset(tokenIndices);

        NodeToken nodeToken = parse2NodeToken(subsetToParse);

        final int origNumTokens = tokenSet.getNumTokens();
        final int newNumTokens = origNumTokens - tokenIndices.length + 1;

        AbstractToken[] newTokens = new AbstractToken[newNumTokens];

        int counter = 0;
        newTokens[counter++] = nodeToken;

        for (int i = 0; i < origNumTokens; ++i) {
            if (MathHelper.find(tokenIndices, i).length == 0) {  // This token is outside the subset parsed this time
                newTokens[counter++] = tokenSet.tokens.get(i); // TODO: Concurrency issue?
            }
        }

        assert(counter == newNumTokens);

        return CWrittenTokenSetNoStroke.from(newTokens);
    }
}
