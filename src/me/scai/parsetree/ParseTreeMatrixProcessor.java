package me.scai.parsetree;

import me.scai.parsetree.geometry.GeometryHelper;

import java.util.*;

public class ParseTreeMatrixProcessor {
    // TODO: Do not hard-code these
    private static final String MATRIX_NODE_IDENTIFIER_STRING           = "Node (MATRIX->COLUMN_CONTENT BRACKET_L BRACKET_R)(NT)";
    private static final String COLUMN_EXPANSION_NODE_IDENTIFIER_STRING = "Node (COLUMN_CONTENT->COLUMN_CONTENT ROW_CONTENT)(NT)";
    private static final String COLUMN_BASE_NODE_IDENTIFIER_STRING      = "Node (COLUMN_CONTENT->ROW_CONTENT)(NT)";
    private static final String ROW_EXPANSION_NODE_IDENTIFIER_STRING    = "Node (ROW_CONTENT->ROW_CONTENT EXPR_LV4)(NT)";
    private static final String ROW_BASE_NODE_IDENTIFIER_STRING         = "Node (ROW_CONTENT->EXPR_LV4)(NT)";

    private static final String MATRIX_GRAMMAR_STRING                   = "MATRIX->COLUMN_CONTENT BRACKET_L BRACKET_R";
    private static final String COLUMN_GRAMMAR_STRING                   = "COLUMN_CONTENT->COLUMN_CONTENT ROW_CONTENT";
    private static final String COLUMN_BASE_GRAMMAR_STRING              = "COLUMN_CONTENT->ROW_CONTENT";
    private static final String ROW_EXPANSION_GRAMMAR_STRING            = "ROW_CONTENT->ROW_CONTENT EXPR_LV4";
    private static final String ROW_BASE_GRAMMAR_STRING                 = "ROW_CONTENT->EXPR_LV4";

    private static final String MATRIX_L_BRACKET                        = "BRACKET_L";
    private static final String MATRIX_R_BRACKET                        = "BRACKET_R";
    // ~TODO: Do not hard-code these

    public ParseTreeMatrixProcessor() {

    }

    /* Helper class for matrix element information */
    class MatrixElemInfo {
        public int rowNum; /* 0-based */
        public int colNum; /* 0-based */
        List<String> termNames;
        float[] bounds;

        Node parentNode;
        int parentChildIndex;

        public MatrixElemInfo(int rowNum, int colNum, Node parentNode, int parentChildIndex) {
            this.rowNum = rowNum;
            this.colNum = colNum;
            this.termNames = new ArrayList<>();

            this.bounds = new float[] {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
            this.parentNode = parentNode;
            this.parentChildIndex = parentChildIndex;
        }

        public void addBounds(final float[] termBounds) {
            bounds[0] = (termBounds[0] < bounds[0]) ? termBounds[0] : bounds[0];
            bounds[1] = (termBounds[1] < bounds[1]) ? termBounds[1] : bounds[1];
            bounds[2] = (termBounds[2] > bounds[2]) ? termBounds[2] : bounds[2];
            bounds[3] = (termBounds[3] > bounds[3]) ? termBounds[3] : bounds[3];
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("[" + rowNum + ", " + colNum + "]: ");
            sb.append("{");
            for (int i = 0; i < termNames.size(); ++i) {
                sb.append(termNames.get(i));

                if (i < termNames.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("} = [");
            for (int i = 0; i < bounds.length; ++i) {
                sb.append(Float.toString(bounds[i]));

                if (i < bounds.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");

            return sb.toString();
        }
    }

    /* Helper class */
    class NonOverlappingBounds implements Comparable<NonOverlappingBounds> {
        private float min;
        private float max;
        private List<Integer> involvedRows;
        private List<Integer> involvedCols;
        private List<Node> elemParents;

        public NonOverlappingBounds(float min, float max) {
            this.min = min;
            this.max = max;

            this.involvedRows = new ArrayList<>();
            this.involvedCols = new ArrayList<>();
            this.elemParents = new ArrayList<>();
        }

        public NonOverlappingBounds(float min, float max, int rowNum, int colNum, Node elemParent) {
            this(min, max);

            this.addElem(rowNum, colNum, min, max, elemParent);
        }

        public boolean overlaps(float min, float max) {
            return GeometryHelper.pctOverlap(this.min, this.max, min, max, false) > 0.0f;
        }

        public void addElem(int rowNum, int colNum, float newMin, float newMax, Node elemParent) {
            this.involvedRows.add(rowNum);
            this.involvedCols.add(colNum);
            this.elemParents.add(elemParent);

            if (newMin < this.min) {
                this.min = newMin;
            }
            if (newMax > this.max) {
                this.max = newMax;
            }
        }

        /**
         *
         * @param rowNum
         * @return The parent node of the first element that hits this interval
         */
        public Node findRowElemParent(int rowNum) {
            for (int i = 0; i < involvedRows.size(); ++i) {
                if (involvedRows.get(i) == rowNum) {
                    return elemParents.get(i);
                }
            }

            return null;
        }

        @Override
        public int compareTo(NonOverlappingBounds noBounds1) {
            if (noBounds1.min > this.min) {
                return -1;
            } else if (noBounds1.min < this.min) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("[").append(Float.toString(min)).append(", ").append(Float.toString(max)).append("]");

            return sb.toString();
        }
    }

    class MatrixElemInfoList {
        private List<MatrixElemInfo> infoList;
        private int length;

        public MatrixElemInfoList() {
            this.infoList = new ArrayList<>();
            length = 0;
        }

        public void addRow() {
            /* First increment the row number of all existing rows */
            for (int i = 0; i < infoList.size(); ++i) {
                infoList.get(i).rowNum++;
            }
        }

        public void addElem(Node parentNode, int parentChildIndex) {
            /* First increment the column numbers of all elements that belong to the current row */
            for (int i = 0; i < infoList.size(); ++i) {
                if (infoList.get(i).rowNum == 0) {
                    infoList.get(i).colNum++;
                }
            }

            infoList.add(new MatrixElemInfo(0, 0, parentNode, parentChildIndex));

            length++;
        }

        public void addNameAndBoundsToCurrentElem(String termName, final float[] termBounds) {
            infoList.get(length - 1).addBounds(termBounds);
            infoList.get(length - 1).termNames.add(termName);
        }

        public void process() {
            /* Determine if there is any mismatch in the row sizes. If these is none, will return right away */
            if (infoList.isEmpty()) {
                return;
            }

            final int nRows = infoList.get(0).rowNum + 1;

//            int[] nCols = new int[nRows];
//            for (int i = 0; i < nCols.length; ++i) {
//                nCols[i] = 0;
//            }
//
//            for (int i = 0; i < infoList.size(); ++i) {
//                nCols[infoList.get(i).colNum]++;
//            }
//
//            Set<Integer> nColsSet = new HashSet<>();
//            for (int i = 0; i < nRows; ++i) {
//                nColsSet.add(nCols[i]);
//            }

//            if (nColsSet.size() == 1) {
//                return; // This probably isn't right. Consider case [0, 1, 0, 2; 3, 0, 4, 0];
//            }

            /* All xBounds, non-overlapping */
            List<NonOverlappingBounds> noXBounds = new ArrayList<>();

            for (int i = 0; i < infoList.size(); ++i) {
                MatrixElemInfo elemInfo = infoList.get(i);

                boolean foundOverlap = false;
                for (NonOverlappingBounds tNoXBounds : noXBounds) {
                    if (tNoXBounds.overlaps(elemInfo.bounds[0], elemInfo.bounds[2])) {
                        tNoXBounds.addElem(elemInfo.rowNum, elemInfo.colNum, elemInfo.bounds[0], elemInfo.bounds[2], elemInfo.parentNode);
                        foundOverlap = true;
                        break;
                    }
                }

                if ( !foundOverlap ) {
                    noXBounds.add(new NonOverlappingBounds(elemInfo.bounds[0], elemInfo.bounds[2], elemInfo.rowNum, elemInfo.colNum, elemInfo.parentNode));
                }
            }

            /* Sort the non-overlapping columns */
            Collections.sort(noXBounds);

            final int finalNumCols = noXBounds.size();

            /* Iterate through all rows, determine which rows are missing which columns */
            int[] missingColNums = new int[nRows];
            for (int i = 0; i < nRows; ++i) {
                Node[] hittingElemParents = new Node[finalNumCols];

                int nHasCols = 0;
                for (int j = 0; j < finalNumCols; ++j) {
                    hittingElemParents[j] = noXBounds.get(j).findRowElemParent(i);
                    if ( hittingElemParents[j] != null ) {
                        nHasCols++;
                    }
                }

                if (nHasCols == finalNumCols) {
                    continue; /* No holes in this row */
                }

                for (int j = 0; j < finalNumCols; ++j) {
                    if (hittingElemParents[j] == null) {
//                        System.out.println("Filling hole: row " + i + ", col " + j);

                        // TODO: Refactor to reduce McCabe complexity
                        if (j == 0) { // TODO: Fix predicament
                            /* Find the first non-null element */   //TODO: More than one holes in the beginning
                            int k;
                            for (k = j + 1; k < hittingElemParents.length; ++k) {
                                if (hittingElemParents[k] != null) {
                                    break;
                                }
                            }

                            int hittingElemParentChildIndex;
                            if (hittingElemParents[k].toString().equals(ROW_BASE_NODE_IDENTIFIER_STRING)) {
                                hittingElemParentChildIndex = 0;
                            } else if (hittingElemParents[k].toString().equals(ROW_EXPANSION_NODE_IDENTIFIER_STRING)) {
                                hittingElemParentChildIndex = 1;
                            } else {
                                throw new IllegalStateException("Not implemented");
                                //TODO
                            }

                            Node oldContentNode = hittingElemParents[k].ch[hittingElemParentChildIndex];
                            Node grandparent = hittingElemParents[k].p;

                            int grandParentChildIndex;
                            if (grandparent.toString().equals(COLUMN_BASE_NODE_IDENTIFIER_STRING)) {
                                grandParentChildIndex = 0;
                            } else if (grandparent.toString().equals(COLUMN_EXPANSION_NODE_IDENTIFIER_STRING)) {
                                grandParentChildIndex = 1;
                            } else if (grandparent.toString().equals(ROW_EXPANSION_NODE_IDENTIFIER_STRING)) {
                                grandParentChildIndex = 0;
                            } else{
                                throw new IllegalStateException("Not implemented");
                                //TODO
                            }

                            // TODO: Do not hardcode grammar
                            Node newRowContentNode = new Node("ROW_CONTENT", ROW_EXPANSION_GRAMMAR_STRING, new String[] {"ROW_CONTENT", "EXPR_LV4"});

                            assert(grandparent.rhsTypes[grandParentChildIndex].equals(newRowContentNode.lhs));
                            grandparent.ch[grandParentChildIndex] = newRowContentNode; // TODO: Do not hardcode ch index
                            newRowContentNode.p = grandparent; // Parent link may come in handy later

                            assert(newRowContentNode.rhsTypes[1].equals(oldContentNode.lhs));
                            newRowContentNode.ch[1] = oldContentNode;
                            oldContentNode.p = newRowContentNode;

                            Node newHoleBranch = constructHoldBranchWithRowContent();
                            assert(newRowContentNode.rhsTypes[0].equals(newHoleBranch.lhs));
                            newRowContentNode.ch[0] = newHoleBranch;
                            newHoleBranch.p = newRowContentNode;

                            // Update parent in hit record
                            hittingElemParents[k] = newRowContentNode;
                            hittingElemParents[j] = newRowContentNode.ch[0];

                        } else if (j == finalNumCols - 1) {
                            /* Find the last non-null element */ //TODO: More than one holes in the back
                            int k;
                            for (k = j - 1; k >= 0; --k) {
                                if (hittingElemParents[k] != null) {
                                    break;
                                }
                            }

                            Node oldContentNode = hittingElemParents[k];
                            Node grandparent = hittingElemParents[k].p;

                            int grandParentChildIndex;
                            if (grandparent.toString().equals(COLUMN_BASE_NODE_IDENTIFIER_STRING)) {
                                grandParentChildIndex = 0;
                            } else if (grandparent.toString().equals(COLUMN_EXPANSION_NODE_IDENTIFIER_STRING)) {
                                grandParentChildIndex = 1;
                            } else{
                                throw new IllegalStateException("Not implemented");
                                //TODO
                            }

                            // TODO: Do not hardcode grammar
                            Node newRowContentNode = new Node("ROW_CONTENT", ROW_EXPANSION_GRAMMAR_STRING, new String[] {"ROW_CONTENT", "EXPR_LV4"});

                            assert(grandparent.rhsTypes[grandParentChildIndex].equals(newRowContentNode.lhs));
                            grandparent.ch[grandParentChildIndex] = newRowContentNode; // TODO: Do not hardcode ch index
                            newRowContentNode.p = grandparent; // Parent link may come in handy later

                            assert(newRowContentNode.rhsTypes[0].equals(oldContentNode.lhs));
                            newRowContentNode.ch[0] = oldContentNode;
                            oldContentNode.p = newRowContentNode;

                            Node newHoleBranch = constructHoldBranchWithoutRowContent();
                            assert(newRowContentNode.rhsTypes[1].equals(newHoleBranch.lhs));
                            newRowContentNode.ch[1] = newHoleBranch;
                            newHoleBranch.p = newRowContentNode;

                            // Update parent in hit record
                            hittingElemParents[k] = newRowContentNode;

                        } else { //j == finalNumCols - 1
                            /* Find the last non-null element */ //TODO: More than one holes in the back
                            int k;
                            for (k = j - 1; k >= 0; --k) {
                                if (hittingElemParents[k] != null) {
                                    break;
                                }
                            }

//                            Node oldContentNode = hittingElemParents[k].ch[0]; // TODO: Do not hardcode ch index
                            Node oldContentNode = hittingElemParents[k]; // TODO: Do not hardcode ch index
                            Node grandparent = hittingElemParents[k].p;

                            int grandparentChildIndex;
                            if (grandparent.toString().equals(COLUMN_BASE_NODE_IDENTIFIER_STRING)) {
                                grandparentChildIndex = 0;
                            } else if (grandparent.toString().equals(COLUMN_EXPANSION_NODE_IDENTIFIER_STRING)) {
                                grandparentChildIndex = 1;
                            } else if (grandparent.toString().equals(ROW_EXPANSION_NODE_IDENTIFIER_STRING)) {
                                grandparentChildIndex = 0;
                            } else {
                                throw new IllegalStateException("Not implemented");
                                //TODO
                            }

                            // TODO: Do not hardcode grammar
                            Node newRowContentNode = new Node("ROW_CONTENT", ROW_EXPANSION_GRAMMAR_STRING, new String[]{"ROW_CONTENT", "EXPR_LV4"});

                            assert grandparent.rhsTypes[grandparentChildIndex].equals(newRowContentNode.lhs);
                            grandparent.ch[grandparentChildIndex] = newRowContentNode; // TODO: Do not hardcode ch index
                            newRowContentNode.p = grandparent; // Parent link may come in handy later

                            int idxA, idxB;
                            if (newRowContentNode.rhsTypes[0].equals(oldContentNode.lhs)) {
                                idxA = 0;
                                idxB = 1;
                            } else if (newRowContentNode.rhsTypes[1].equals(oldContentNode.lhs)) {
                                idxA = 1;
                                idxB = 0;
                            } else {
                                throw new RuntimeException();
                            }

                            assert newRowContentNode.rhsTypes[idxA].equals(oldContentNode.lhs);
                            newRowContentNode.ch[idxA] = oldContentNode;
                            oldContentNode.p = newRowContentNode;

                            Node newHoleBranch = constructHoldBranchWithoutRowContent();
                            assert newRowContentNode.rhsTypes[idxB].equals(newHoleBranch.lhs);
                            newRowContentNode.ch[idxB] = newHoleBranch;
                            newHoleBranch.p = newRowContentNode;

                            // Update hit element
                            hittingElemParents[j] = newRowContentNode;
                        }
                    }


                }

            }
        }

    }

    private Node constructHoldBranchWithRowContent() {
        // TODO: Do not hardcode grammar
        Node n0 = new Node("ROW_CONTENT", "ROW_CONTENT->EXPR_LV4", new String[] {"EXPR_LV4"});

        Node n1 = constructHoldBranchWithoutRowContent();

        n0.ch[0] = n1;
        n1.p = n0;

        return n0;
    }

    private Node constructHoldBranchWithoutRowContent() {
        // TODO: Do not hardcode grammar
        Node n1 = new Node("EXPR_LV4", "EXPR_LV4->EXPR_LV1", new String[] {"EXPR_LV1"});
        Node n2 = new Node("EXPR_LV1", "EXPR_LV1->EXPR_LV0_6", new String[] {"EXPR_LV0_6"});
        Node n3 = new Node("EXPR_LV0_6", "EXPR_LV0_6->DECIMAL_NUMBER", new String[] {"DECIMAL_NUMBER"});
        Node n4 = new Node("DECIMAL_NUMBER", "DECIMAL_NUMBER->DIGIT_STRING", new String[] {"DIGIT_STRING"});
        Node n5 = new Node("DIGIT_STRING", "DIGIT_STRING->DIGIT", new String[] {"DIGIT"});
        Node n6 = new Node("DIGIT_STRING", "DIGIT", "0", null); // TODO: Create dummy bounds //DEBUG

        n1.ch[0] = n2;
        n2.p = n1;

        n2.ch[0] = n3;
        n3.p = n2;

        n3.ch[0] = n4;
        n4.p = n3;

        n4.ch[0] = n5;
        n5.p = n4;

        n5.ch[0] = n6;
        n6.p = n5;

        return n1;
    }

    public void process(Node n) {
        if (n == null) {
            return;
        }

        /* 1. Traverse the tree and find all the matrix root nodes */
        List<Node> matrixNodes = extractMatrixNodes(n);

        for (Node matrixNode : matrixNodes) {
            /* Process the matrix */
            int nRows = 0;
            List<Integer> nColumns = new ArrayList<>();
            MatrixElemInfoList matrixElemInfoList = new MatrixElemInfoList();

            LinkedList<Node> nStack = new LinkedList<>();
            LinkedList<Boolean> isTerminalStack = new LinkedList<>();
            LinkedList<Boolean> bParsedStack = new LinkedList<>();
            LinkedList<Integer> ntIndexStack = new LinkedList<>();

            nStack.push(matrixNode);
            isTerminalStack.push(matrixNode.isTerminal());
            bParsedStack.push(false);
            ntIndexStack.push(-1);

            while ( !nStack.isEmpty() ) {
                Node nTop = nStack.getFirst();
                boolean bParsedTop = bParsedStack.getFirst();

                if ( !bParsedTop ) {
                    int ntIndex = ntIndexStack.size() - 1;
                    for (int i = 0; i < nTop.ch.length; ++i) {
//                        System.out.println("Pushing node: " + nTop.ch[i]);

                        Node ch = nTop.ch[i];

                        if (ch.p == null) {
                            /* Parent links will come in handy during tree restructuring */
                            ch.p = nTop;
                        }

                        nStack.push(ch);
                        isTerminalStack.push(ch.isTerminal());
                        bParsedStack.push(ch.isTerminal());
                        ntIndexStack.push(ntIndex);

                        if ( nTop.toString().equals(COLUMN_BASE_NODE_IDENTIFIER_STRING) ||
                                (nTop.toString().equals(COLUMN_EXPANSION_NODE_IDENTIFIER_STRING) && !(ch.toString().equals(COLUMN_BASE_NODE_IDENTIFIER_STRING) || ch.toString().equals(COLUMN_EXPANSION_NODE_IDENTIFIER_STRING))) ) {
                            nRows++;
                            nColumns.add(0);

                            matrixElemInfoList.addRow();
                        }

                        if ( nTop.toString().equals(ROW_BASE_NODE_IDENTIFIER_STRING) ||
                             (nTop.toString().equals(ROW_EXPANSION_NODE_IDENTIFIER_STRING) && !(ch.toString().equals(ROW_BASE_NODE_IDENTIFIER_STRING) || ch.toString().equals(ROW_EXPANSION_NODE_IDENTIFIER_STRING))) ) {
                            nColumns.set(nColumns.size() - 1, nColumns.get(nColumns.size() - 1) + 1);
                            matrixElemInfoList.addElem(nTop, i);
                        }

                        if (ch.isTerminal()) {
                            if ( !(ch.termName.equals(MATRIX_L_BRACKET) || ch.termName.equals(MATRIX_R_BRACKET)) ) {
//                                System.out.println("(): Terminal: " + ch.termName);

                                float[] termBounds = ch.getBounds();

                                /* Update terminal namse and matrix element bounds */
                                matrixElemInfoList.addNameAndBoundsToCurrentElem(ch.termName, termBounds);
                            }
                        }
                    }
                } else {
                    if (ntIndexStack.size() - 2 == ntIndexStack.getFirst() && bParsedStack.size() > 1) {
                        bParsedStack.set(1, true);
                    }
//                    System.out.println("Popping " + nStack.getFirst().toString());

                    // Detect popping of a matrix element
                    int ntIdx0 = ntIndexStack.getFirst();

                    int j;
                    for (j = 0; j < ntIndexStack.size(); ++j) {
                        if (ntIndexStack.get(j) != ntIdx0) {
                            break;
                        }
                    }

                    nStack.pop();
                    isTerminalStack.pop();
                    bParsedStack.pop();
                    ntIndexStack.pop();
                }
            }

            matrixElemInfoList.process();
        }


    }

    private List<Node> extractMatrixNodes(Node n) {
        List<Node> matrixNodes = new ArrayList<>();

        LinkedList<Node> nStack = new LinkedList<>();
        LinkedList<Boolean> isTerminalStack = new LinkedList<>();
        LinkedList<Boolean> bParsedStack = new LinkedList<>();
        LinkedList<Integer> ntIndexStack = new LinkedList<>();

        nStack.push(n);
        isTerminalStack.push(n.isTerminal());
        bParsedStack.push(false);
        ntIndexStack.push(0);

        while ( !nStack.isEmpty() ) {
            Node nTop = nStack.getFirst();
            boolean bParsedTop = bParsedStack.getFirst();

            if ( !bParsedTop ) {
                int ntIndex = ntIndexStack.size() - 1;
                for (int i = 0; i < nTop.ch.length; ++i) {
                    nStack.push(nTop.ch[i]);
                    isTerminalStack.push(nTop.ch[i].isTerminal());
                    bParsedStack.push(nTop.ch[i].isTerminal());
                    ntIndexStack.push(ntIndex);

                    if (nTop.ch[i].toString().equals(MATRIX_NODE_IDENTIFIER_STRING)) {
                        matrixNodes.add(nTop.ch[i]);
                    }
                }
            } else {
                if (ntIndexStack.size() - 2 == ntIndexStack.getFirst()) {
                    bParsedStack.set(1, true);
                }

                nStack.pop();
                isTerminalStack.pop();
                bParsedStack.pop();
                ntIndexStack.pop();
            }
        }

        return matrixNodes;
    }
}

