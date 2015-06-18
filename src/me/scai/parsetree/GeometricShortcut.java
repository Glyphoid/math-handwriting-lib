package me.scai.parsetree;

import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.handwriting.Rectangle;
import me.scai.parsetree.geometry.PositionRelation;

import java.util.ArrayList;

/* Class GeometricShortcut
 * Detects short cut for dividing non-head tokens into sets for parsing
 * see: GraphicalProduction.attempt(); MathHelper.getFullDiscreteSpace();
 */
class GeometricShortcut {
    public enum ShortcutType {
        noShortcut,
        horizontalTerminalDivideNS,	    /* 3-part shortcut types (i.e., production with 3 nodes) */ /* North to south */
        horizontalTerminalDivideSN,		/* South to north */
        verticalTerminalDivideWE,       /* 3-part shortcut types, with the head being T */ /* West to east */
        verticalTerminalDivideEW,       /* East to west */
        verticalNT1T2DivideWE,		    /* 3-part shortcut types, with the head being NT and the remaining two items being both T. Hence the "NT1T2". Example: (Addition, Bracket_L, Bracket_R) */
        westEast, 				        /* 2-part shortcut type: head is at west and the (only) non-head is at the east */
    }

    /* Member variables */
    private ShortcutType shortcutType = ShortcutType.noShortcut;

    /* Methods */
    public GeometricShortcut(GraphicalProduction gp,
                             TerminalSet termSet) {
        int nrhs = gp.geomRels.length; 	/* Number of rhs items */

        if ( !(nrhs == 2 || nrhs == 3) ) {
			/* Currently, we deal with only bipartite or tripartite shortcuts, such as linear divides.
			 * This may change in the future.
			 */
            shortcutType = ShortcutType.noShortcut;
            return;
        }

        if ( nrhs == 2 ) {
            if ( gp.geomRels[1] == null ) {
                shortcutType = ShortcutType.noShortcut;
            } else {
                PositionRelation.PositionType posType = null;
                int nPosRels = 0;

                for (int j = 0; j < gp.geomRels[1].length; ++j) {
                    if ( gp.geomRels[1][j].getClass() == PositionRelation.class ) {
                        nPosRels++;

                        PositionRelation posRel = (PositionRelation) gp.geomRels[1][j];
                        posType = posRel.positionType;
                    }
                }

                if ( nPosRels == 1 ) {
                    if ( posType == PositionRelation.PositionType.PositionEast ||
                         posType == PositionRelation.PositionType.PositionGenEast )
                        shortcutType = ShortcutType.westEast;
                }
            }
        } else if ( nrhs == 3 ){
			/* Examine whether the head-node is T.
			 * If so, this is potentially a _TerminalDivide__ (e.g., verticalDivideWE) type shortcut.
			 * If not, go to the next logical branch. */
            String tripartiteType = null;
            if ( termSet.isTypeTerminal(gp.rhs[0]) ) {
                tripartiteType = "TerminalDivide";
            } else if ( !termSet.isTypeTerminal(gp.rhs[0]) &&
                        termSet.isTypeTerminal(gp.rhs[1]) && termSet.isTypeTerminal(gp.rhs[2]) ) {
                tripartiteType = "NT1T2Divide";
            }

            PositionRelation.PositionType [] posType = new PositionRelation.PositionType[2];
            int [] nPosRels = new int[2];
			/* Only if each of the two non-head tokens have exactly one positional relation,
			 * can we construct a meaningful shortcut (at least for the time being).
			 */

            for (int i = 1; i < 3; ++i) {
                for (int j = 0; j < gp.geomRels[i].length; ++j) {
                    if ( gp.geomRels[i][j].getClass() == PositionRelation.class ) {
                        nPosRels[i - 1]++;

                        PositionRelation posRel = (PositionRelation) gp.geomRels[i][j];
                        posType[i - 1] = posRel.positionType;
                    }
                }
            }

            if ( nPosRels[0] == 1 && nPosRels[1] == 1 ) {
                if ( posType[0] == PositionRelation.PositionType.PositionWest && posType[1] == PositionRelation.PositionType.PositionEast ||
                     posType[0] == PositionRelation.PositionType.PositionGenWest && posType[1] == PositionRelation.PositionType.PositionGenEast ) {
                    if ( tripartiteType.equals("TerminalDivide") ) {
                        shortcutType = ShortcutType.verticalTerminalDivideWE;
                    } else if ( tripartiteType.equals("NT1T2Divide") ) {
                        shortcutType = ShortcutType.verticalNT1T2DivideWE;
                    }
                } else if ( posType[0] == PositionRelation.PositionType.PositionEast && posType[1] == PositionRelation.PositionType.PositionWest ||
                        posType[0] == PositionRelation.PositionType.PositionGenEast && posType[1] == PositionRelation.PositionType.PositionGenWest ) {
                    if ( tripartiteType.equals("TerminalDivide") ) {
                        shortcutType = ShortcutType.verticalTerminalDivideEW;
                    }
                } else if ( posType[0] == PositionRelation.PositionType.PositionNorth && posType[1] == PositionRelation.PositionType.PositionSouth ||
                        posType[0] == PositionRelation.PositionType.PositionGenNorth && posType[1] == PositionRelation.PositionType.PositionGenSouth ) {
                    if ( tripartiteType.equals("TerminalDivide") ) {
                        shortcutType = ShortcutType.horizontalTerminalDivideNS;
                    }
                } else if ( posType[0] == PositionRelation.PositionType.PositionSouth && posType[1] == PositionRelation.PositionType.PositionNorth ||
                          posType[0] == PositionRelation.PositionType.PositionGenSouth && posType[1] == PositionRelation.PositionType.PositionGenNorth ) {
                    if ( tripartiteType.equals("TerminalDivide") ) {
                        shortcutType = ShortcutType.horizontalTerminalDivideSN;
                    }
                }
            }
        } else {
            throw new IllegalStateException("Unexpected number of rhs items: " + nrhs);
        }
    }

    public boolean existsTripartiteTerminal() {
        return (shortcutType != ShortcutType.noShortcut)
                && ( !existsTripartiteNT1T2() )
                &&  ( !existsBipartite() );
    }

    public boolean existsTripartiteNT1T2() {
        return (shortcutType == ShortcutType.verticalNT1T2DivideWE);
    }

    public boolean existsBipartite() {
        return (shortcutType == ShortcutType.westEast);
    }


    /* Main work: divide a token set into two (or more, for future) parts b
     * based on the type of the geometric shortcut.
     * Return value: 0-1 indicators of whether a token is to be head or non-head
     */
    public int [][] getPartitionBipartite(CAbstractWrittenTokenSet wts, boolean bReverse) {
        int nt = wts.nTokens();
        int [][] labels = null;

        if ( nt == 0 ) {
            throw new RuntimeException("Attempting to apply bipartite shortcut on one or fewer tokens");
        }

        if ( nt == 1 ) {
            labels = new int[2][];

            labels[0] = new int[1];
            labels[0][0] = 0;

            labels[1] = new int[1];
            labels[1][0] = 1;
        }

        if ( shortcutType == ShortcutType.westEast ) {
			/* Calculate the center X of all tokens */
            float [] cntX = new float[nt];

            for (int i = 0; i < nt; ++i) {
                float [] t_bnds = wts.getTokenBounds(i);

                cntX[i] = (t_bnds[0] + t_bnds[2]) * 0.5f;
            }

			/* Sort */
            int [] srtIdx = new int[nt];
            MathHelper.sort(cntX, srtIdx);

			/* Generate all the valid partitions */
            labels = new int[nt - 1][];
            for (int i = 0; i < nt - 1; ++i) {
                labels[i] = new int[nt];

                for (int j = 0; j < nt; ++j) {
                    if ( j > i )
                        labels[i][srtIdx[j]] = 1;
                    else
                        labels[i][srtIdx[j]] = 0;

                    if ( bReverse )
                        labels[i][srtIdx[j]] = 1 - labels[i][srtIdx[j]];
                }


            }
        }

        return labels;
    }

    public int [][] getPartitionTripartiteTerminal(CAbstractWrittenTokenSet wts, int [] iHead) {
        if ( !existsTripartiteTerminal() ) {
            throw new IllegalStateException("Geometric shortcuts do not exist");
        }

        if ( iHead.length >= wts.nTokens() ) {
            throw new RuntimeException("The number of indices to heads equals or exceeds the number of tokens in the token set");
        }

        Rectangle rectHead = new Rectangle(wts, iHead);
        float headCenterX = rectHead.getCentralX();
        float headCenterY = rectHead.getCentralY();

        int [][] labels = new int[1][];
        int nnht = wts.nTokens() - iHead.length;
        labels[0] = new int[nnht];

		/* Get indices to all non-head tokens */
        ArrayList<Integer> inht = new ArrayList<Integer>();
        ArrayList<Rectangle> rnht = new ArrayList<Rectangle>();
        for (int i = 0; i < wts.nTokens(); ++i) {
            boolean bContains = false;
            for (int j = 0; j < iHead.length; ++j) {
                if ( iHead[j] == i ) {
                    bContains = true;
                    break;
                }
            }
            if ( !bContains ) {
                inht.add(i);
                rnht.add(new Rectangle(wts.getTokenBounds(i)));
            }
        }

        for (int i = 0; i < inht.size(); ++i) {
            int idx;
            if ( shortcutType == ShortcutType.verticalTerminalDivideWE ) {
                idx = rnht.get(i).isCenterWestOf(headCenterX) ? 0 : 1;
            }
            else if ( shortcutType == ShortcutType.verticalTerminalDivideEW ) {
                idx = rnht.get(i).isCenterEastOf(headCenterX) ? 0 : 1;
            }
            else if ( shortcutType == ShortcutType.horizontalTerminalDivideNS) {
                idx = rnht.get(i).isCenterNorthOf(headCenterX) ? 1 : 0;
            }
            else if ( shortcutType == ShortcutType.horizontalTerminalDivideSN ) {
                idx = rnht.get(i).isCenterSouthOf(headCenterY) ? 1 : 0;
            }
            else {
                throw new RuntimeException("Unrecognized shortcut type");
            }

            labels[0][i] = idx;
        }

        return labels;
    }


    public int [][] getPartitionTripartiteNT1T2(CAbstractWrittenTokenSet wts) {
        int nt = wts.nTokens();
        int [][] labels = null;

        if ( nt < 3 ) {
            labels = new int[0][];
            return labels;
        }
//			throw new RuntimeException("Attempting to apply tripartite shortcut on two or fewer tokens");

        if ( shortcutType == ShortcutType.verticalNT1T2DivideWE ) {
			/* Calculate the center X of all tokens */
            float [] cntX = new float[nt];

            for (int i = 0; i < nt; ++i) {
                float [] t_bnds = wts.getTokenBounds(i);

                cntX[i] = (t_bnds[0] + t_bnds[2]) * 0.5f;
            }

			/* Sort */
            int iRightmost = MathHelper.indexMax(cntX);
            int iLeftmost = MathHelper.indexMin(cntX);

            labels = new int[1][];
            labels[0] = new int[nt];

//			int cnt = 0;
            for (int i = 0; i < nt; ++i)
                if (i != iRightmost && i != iLeftmost)
                    labels[0][i] = 1;
        }
        else {
            throw new RuntimeException("Unexpected shortcut type encountered in getPartitionTripartiteNT1T2()");
        }

        return labels;
    }


    @Override
    public String toString() {
        String s = "GeometricShortcut: ";

        if ( shortcutType == ShortcutType.noShortcut ) {
            s += "noShortcut";
        }
        else if ( shortcutType == ShortcutType.horizontalTerminalDivideNS) {
            s += "horiztonalDivdeNS";
        }
        else if ( shortcutType == ShortcutType.horizontalTerminalDivideSN ) {
            s += "horizontalTerminalDivideSN";
        }
        else if ( shortcutType == ShortcutType.verticalTerminalDivideWE ) {
            s += "verticalTerminalDivideWE";
        }
        else if ( shortcutType == ShortcutType.verticalTerminalDivideEW ) {
            s += "verticalTerminalDivideEW";
        }
        else {
            s += "(unknown shortcut type)";
        }

        return s;
    }
}
