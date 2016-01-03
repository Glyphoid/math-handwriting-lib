package me.scai.handwriting;

import me.scai.parsetree.GraphicalProductionSet;
import me.scai.parsetree.Node;
import me.scai.parsetree.TerminalSet;

import java.util.LinkedList;
import java.util.List;

/**
 * A token that is a parsed node
 */
public class NodeToken extends AbstractToken {
    /* Members */
    private Node node;
    private CAbstractWrittenTokenSet wtSet;

    private List<Integer> matchingGraphicalProductionIndices; // TODO: Do not make public just for the sake of serialization/desrialization

    private String parsingResult;

    /* Constructors */
    public NodeToken(Node node, CAbstractWrittenTokenSet wtSet) {
        // Set the bounds of the node
        node.setBounds(wtSet.getSetBounds());

        // Make sure that tokenNames are available
        if (wtSet instanceof CWrittenTokenSetNoStroke && wtSet.tokenNames == null) {
            CWrittenTokenSetNoStroke wtSetNS = (CWrittenTokenSetNoStroke) wtSet;

            String[] tokenNames = new String[wtSet.getNumTokens()];
            for (int i = 0; i < wtSetNS.tokens.size(); ++i) {
                tokenNames[i] = wtSetNS.tokens.get(i).getRecogResult();
            }

            wtSet.setTokenNames(tokenNames);
        }

        if (node.getBounds() == null) {
            throw new IllegalArgumentException("node bounds not available");
        }

        this.node = node;
        this.wtSet = wtSet;

        this.tokenBounds = this.node.getBounds();
        this.width  = this.tokenBounds[2] - this.tokenBounds[0];
        this.height = this.tokenBounds[3] - this.tokenBounds[1];

    }

    /* Methods */
    @Override
    public float getCentralX() {
        return 0.5f * (tokenBounds[2] + tokenBounds[0]);
    }

    @Override
    public float getCentralY() {
        return 0.5f * (tokenBounds[3] + tokenBounds[1]);
    }

    @Override
    public void getTokenTerminalType(TerminalSet termSet) {
        tokenTermTypes = null;
    }

    @Override
    public String getRecogResult() {
        return parsingResult;
    }

    @Override
    public void setRecogResult(String pr) {
        parsingResult = pr;
    }

    public Node getNode() {
        return node;
    }

    public CAbstractWrittenTokenSet getTokenSet() {
        return wtSet;
    }

    public boolean isPotentiallyTerminal() {
        return wtSet.getNumTokens() == 1;
        // It is potentially a terminal if and only if the written token set has only one terminal
    }

    /**
     * Get all the matching graphical production indices inside a given GraphicalProductionSet
     * @param gpSet
     * @return
     */
    public List<Integer> getMatchingGraphicalProductionIndices(GraphicalProductionSet gpSet) {
        if (matchingGraphicalProductionIndices == null) { // Use caching
            matchingGraphicalProductionIndices = new LinkedList<>();

            Node nd = node;

            while (nd != null) {
                String prodSumString = nd.prodSumString;

                if (gpSet.prodSumStrings.contains(prodSumString)) {
                    matchingGraphicalProductionIndices.add(gpSet.prodSumStrings.indexOf(prodSumString));
                }

                if (nd.ch == null) {
                    break;
                }

                if (nd.ch.length == 1) {
                    nd = nd.ch[0];
                } else {
                    break;
                }
            }
        }

        return matchingGraphicalProductionIndices;

    }

    /**
     * Get the direct, single-lineage, descendant of the node that has the specified production LHS
     * @param lhs   Production LHS
     * @return      null if such a descendant does not exist.
     *              The descendant node if it exists.
     */
    public Node getSingleLineageDirectDescendant(String lhs) {
        if (lhs == null) {
            throw new IllegalArgumentException("lhs is null and hence invalid");
        }

        Node nd = this.node;

        while (nd.ch != null && nd.ch.length == 1) {
            if (lhs.equals(nd.ch[0].lhs)) {
                return nd.ch[0];
            }

            nd = nd.ch[0];
        }

        return null;
    }

    public List<Integer> getMatchingGraphicalProductionIndices() {
        return matchingGraphicalProductionIndices;
    }

    public void setMatchingGraphicalProductionIndices(List<Integer> matchingGraphicalProductionIndices) {
        this.matchingGraphicalProductionIndices = matchingGraphicalProductionIndices;
    }
}
