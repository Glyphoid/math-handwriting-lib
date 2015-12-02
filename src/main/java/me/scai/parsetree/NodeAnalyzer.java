package me.scai.parsetree;

import me.scai.parsetree.geometry.GeometryHelper;

import java.util.ArrayList;
import java.util.List;

public class NodeAnalyzer {
    /**
     * Get all graphical productions that the node can represent.
     * For example if the grammar consists of:
     *     root --> gpA,
     *     gpA --> gpB,
     *     gpB --> gpC,
     *     gpC --> gpD,
     *     gpD --> [terminalA, terminalB]
     *  then a node of the typca: root --> gpA --> gpB --> grpC --> grD can potentially represent
     *   gpA, gpB, gpC or gpD.
     *  Algorithm: starting from the root node, follow the node tree until we reach the first node
     *  which has more than one children or the only child is a terminal. Add all the nodes visited
     *  to the list to be returned
     * @param node    Input node
     * @return     The list of valid productions LHS (TODO: replace the string types with appropriate types for LHS)
     */
    public static List<String> getValidProductionLHS(Node node) {
        List<String> validProds = new ArrayList<>();

        Node n = node;

        while ( !n.isTerminal() &&
                n.numChildren() == 1 &&
                !n.ch[0].isTerminal() ) {
            validProds.add(n.lhs);

            n = n.ch[0];
        }

        validProds.add(n.lhs);

        return validProds;
    }

    /**
     * Calculate the bounds of a node, generally non-terminal. The algorithm merges the
     * bounds of all terminal tokens that belong to this node.
     * @param node    The node
     * @return        The total (merged) bounds of the node.
     * Side effect: It also calls the setBounds method of the node if the node is non-terminal.
     */
    public static float[] calcNodeBounds(Node node) {
        if (node.isTerminal()) {
            assert node.getBounds() != null;

            return node.getBounds();
        } else {
            float[] mergedBounds = GeometryHelper.getInitBounds();

            for (Node child : node.ch) {
                float[] childBounds = calcNodeBounds(child);
                mergedBounds = GeometryHelper.mergeBounds(mergedBounds, childBounds);
            }

            node.setBounds(mergedBounds);

            return mergedBounds;
        }
    }
}
