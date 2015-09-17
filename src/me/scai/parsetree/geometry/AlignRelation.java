package me.scai.parsetree.geometry;

import me.scai.handwriting.CAbstractWrittenTokenSet;

/* AlignRelation */
public class AlignRelation extends GeometricRelation {
	public enum AlignType {
		AlignBottom, 
		AlignTop, 
		AlignMiddle,  /* Middle of the vertical dimension */
		AlignHeightInclusion, /* Within the height range of the in-rel */ 
		AlignLeft,
		AlignRight,
		AlignLeftWithin,
		AlignRightWithin,
        AlignBottomWithin,
		AlignCenter,   /* Center of the left-right dimension */
        AlignWidthOverlapGreaterThanHalf,
		AlignWidthInclusion, /* Within the width range of the in-rel */
		AlignBottomNorthPastMiddle, /* The bottom of the token should be more north than the middle of the in-rel token */
        AlignTopSouthPastMiddle,    /* The top of the token should be more south than the middle of the in-rel token */
        AlignTopSouthPastMiddleNotTooFarSouth, /* Same as AlignTopSouthPastMiddle, but the top cannot be too far down */
		AlignTopNorthPastTop, 		/* The top of the token should be more north than the top of the in-rel token */
        AlignBottomSouthPastBottom  /* The bottom of the token should be more south than the bottom of the in-rel token */
	};
	
	/* Member variables */
	/* Constants */
	static final float pastMiddleDisplacementLB = 0.50f;
	static final float pastMiddleDisplacementUB = 0.75f;	/* Apply to relations such as AlignBottomNorthPastMiddle */
	static final float pastEdgeDisplacementLB = -0.25f;
	static final float pastEdgeDisplacementUB = 0.00f;		/* Apply to relations such as AlignTopNorthPastTop */

    public AlignType alignType;
	
	/* Constructor */
	private AlignRelation() {}
	
	private static float inclusionEdgeDiff(float [] limsTested, float [] limsInRel) {
		/* Tests if limsTested is contained with in limsInRel.
		 * A smaller return value corresponds to better inclusion under this criterion. */
		
		if ( limsTested[0] >= limsInRel[0] && limsTested[1] <= limsInRel[1] )
			return 0.0f;
		else if ( limsTested[0] < limsInRel[0] && limsTested[1] > limsInRel[1] )
			return ((limsInRel[0] - limsTested[0]) + (limsTested[1] - limsInRel[1])) * 0.5f;
		else
			if ( limsTested[0] < limsInRel[0] )
				return (limsInRel[0] - limsTested[0]);
			else
				return (limsTested[1] - limsInRel[1]);
	}
	
	public AlignRelation(AlignType at, int t_idxTested, int t_idxInRel) {
		alignType = at;
		
		idxTested = new int[1];
		idxTested[0] = t_idxTested;
		
		idxInRel = new int[1];
		idxInRel[0] = t_idxInRel;
	}
	
	@Override
	public float verify(CAbstractWrittenTokenSet wtsTested,  float [] bndsInRel) {
		float [] bndsTested = wtsTested.getSetBounds();
		if ( bndsInRel.length != 4 )
			throw new IllegalArgumentException("tiTested does not have length 1");
	
		float szTested, szInRel, szMean, edgeDiff;
//		float szMin;
		float v;	/* Return value */
		if ( alignType == AlignType.AlignBottom || 
			 alignType == AlignType.AlignTop ||
			 alignType == AlignType.AlignMiddle ||
             alignType == AlignType.AlignBottomWithin ||
			 alignType == AlignType.AlignHeightInclusion || 
			 alignType == AlignType.AlignBottomNorthPastMiddle ||
             alignType == AlignType.AlignTopSouthPastMiddle ||
             alignType == AlignType.AlignTopSouthPastMiddleNotTooFarSouth ||
			 alignType == AlignType.AlignTopNorthPastTop ||
             alignType == AlignType.AlignBottomSouthPastBottom ) { /* Align in the vertical dimension */
			/* sz is height */
			szTested = bndsTested[3] - bndsTested[1];
			szInRel = bndsInRel[3] - bndsInRel[1];
			
			szMean = (szTested + szInRel) * 0.5f;
			
			if ( alignType == AlignType.AlignBottom ) {
				edgeDiff = Math.abs(bndsTested[3] - bndsInRel[3]);
				v = 1 - edgeDiff / szMean;
			}
			else if ( alignType == AlignType.AlignTop ) {
				edgeDiff = Math.abs(bndsTested[1] - bndsInRel[1]);
				v = 1 - edgeDiff / szMean;				
			}
			else if ( alignType == AlignType.AlignMiddle ) {
				edgeDiff = Math.abs((bndsTested[1] + bndsTested[3]) * 0.5f - 
						            (bndsInRel[1] + bndsInRel[3]) * 0.5f);
				v = 1 - edgeDiff / szMean;
			}
			else if ( alignType == AlignType.AlignHeightInclusion ) {
				float [] limsTested = new float[2];
				float [] limsInRel = new float[2];
				limsTested[0] = bndsTested[1]; 
				limsTested[1] = bndsTested[3];
				limsInRel[0] = bndsInRel[1];
				limsInRel[1] = bndsInRel[3]; 
				
				edgeDiff = inclusionEdgeDiff(limsTested, limsInRel);
				v = 1 - edgeDiff / szMean;
			}
			else if ( alignType == AlignType.AlignBottomNorthPastMiddle ||
                      alignType == AlignType.AlignTopSouthPastMiddle ||
                      alignType == AlignType.AlignTopSouthPastMiddleNotTooFarSouth ) {
                // New AlignBottomNorthPastMiddle
                if ( alignType == AlignType.AlignBottomNorthPastMiddle ) {
                    float sb = bndsInRel[3] - pastMiddleDisplacementLB * szInRel;
                    float nb = bndsInRel[3] - pastMiddleDisplacementUB * szInRel;

                    v = (sb - bndsTested[3]) / (sb - nb);
                } else { // AlignTopSouthPastMiddle OR AlignTopSouthPastMiddleNotTooFarDown
                    float sb = bndsInRel[1] + pastMiddleDisplacementUB * szInRel;
                    float nb = bndsInRel[1] + pastMiddleDisplacementLB * szInRel;

                    v = (bndsTested[1] - nb) / (sb - nb);

                    if (alignType == AlignType.AlignTopSouthPastMiddleNotTooFarSouth) {
                        if (bndsTested[1] - bndsInRel[3] > szInRel * 1.0f) {     // This is the criterion for being too far south
                            v = 0.0f;
                        }
                    }
                }

                // Old AlignBottomNorthPastMiddle
//              float midYInRel = (bndsInRel[3] + bndsInRel[1]) * 0.5f;
//
//				float sb = bndsTested[3] - pastMiddleDisplacementLB * szTested;
//				float nb = bndsTested[3] - pastMiddleDisplacementUB * szTested;
//
//                if ( alignType == AlignType.AlignBottomNorthPastMiddle ) {
//                    v = (sb - midYInRel) / (sb - nb);
//                } else { // AlignTopSouthPastMiddles
//                    v = (midYInRel - nb) / (sb - nb);
//                }
			}
            else if ( alignType == AlignType.AlignBottomWithin ) {
                edgeDiff = bndsInRel[3] - bndsTested[3];
                v = edgeDiff / (szMean * 0.05f); /* TODO: 0.05 is somewhat ad hoc - Correct it */

            }
			else if ( alignType == AlignType.AlignTopNorthPastTop ||
                      alignType == AlignType.AlignBottomSouthPastBottom ) {
                if ( alignType == AlignType.AlignTopNorthPastTop ) {
                    float sb = bndsInRel[1] + pastEdgeDisplacementUB * szTested;
                    float nb = bndsInRel[1] + pastEdgeDisplacementLB * szTested;

//                    float sb = bndsInRel[1] + pastEdgeDisplacementUB * szInRel;
//                    float nb = bndsInRel[1] + pastEdgeDisplacementLB * szInRel;

                    v = (sb - bndsTested[1]) / (sb - nb);
                } else { // AlignBottomSouthPastBottom
                    float sb = bndsInRel[3] - pastEdgeDisplacementLB * szTested;
                    float nb = bndsInRel[3] + pastEdgeDisplacementUB * szTested;

                    v = (bndsTested[3] - nb) / (sb - nb);
                }
			}
			else {
				throw new RuntimeException("Unrecognized alignType");
			}
		}
		else {	/* sz is width */
			szTested = bndsTested[2] - bndsTested[0];
			szInRel = bndsInRel[2] - bndsInRel[0];
			szMean = (szTested + szInRel) * 0.5f;
			
			if ( alignType == AlignType.AlignLeft ) {
				edgeDiff = Math.abs(bndsTested[2] - bndsInRel[2]); /* TODO: Check: Is AlignLeft and AlignRight supposed to be swapped? */
				v = 1 - edgeDiff / szMean;
			}
			else if ( alignType == AlignType.AlignRight ) {
				edgeDiff = Math.abs(bndsTested[0] - bndsInRel[0]);
				v = 1 - edgeDiff / szMean;
			}
			else if ( alignType == AlignType.AlignLeftWithin ) {
				edgeDiff = bndsTested[0] - bndsInRel[0];
				v = edgeDiff / (szMean * 0.1f); /* TODO: 0.1 is somewhat ad hoc - Correct it */
			}
			else if ( alignType == AlignType.AlignRightWithin ) {
				edgeDiff = bndsInRel[2] - bndsTested[2];
				v = edgeDiff / (szMean * 0.1f); /* TODO: 0.1 is somewhat ad hoc - Correct it */
			}
			else if ( alignType == AlignType.AlignCenter ) {
				edgeDiff = Math.abs((bndsTested[0] + bndsTested[2]) * 0.5f - 
			                        (bndsInRel[0] + bndsInRel[2]) * 0.5f);
				v = 1 - edgeDiff / szMean;
			}
            else if ( alignType == AlignType.AlignWidthOverlapGreaterThanHalf) {
                v = GeometryHelper.pctOverlap(bndsInRel[0], bndsInRel[2], bndsTested[0], bndsTested[2], true);
                v = v / 0.5f;
            }
			else if ( alignType == AlignType.AlignWidthInclusion ) {
				float [] limsTested = new float[2];
				float [] limsInRel = new float[2];
				limsTested[0] = bndsTested[0];
				limsTested[1] = bndsTested[2];
				limsInRel[0] = bndsInRel[0];
				limsInRel[1] = bndsInRel[2]; 
				
				edgeDiff = inclusionEdgeDiff(limsTested, limsInRel);
				v = 1 - edgeDiff * 4.0f / szMean;
				/* TODO: The 4.0f value here is rather ad hoc (same for AlignHeightInclusion
				 * above). Is there a way to make it more elegant?
				 */
			}
			else {
				throw new RuntimeException("Unrecognized alignType");
			}
			
		}
		
		if ( v > 1f ) {
            v = 1f;
        }
		if ( v < 0f ) {
            v = 0f;
        }
		
		return v;
	}

	@Override
	public void parseString(String str, int t_idxTested) {
		String [] items = splitInputString(str);
		
		alignType = AlignType.valueOf(items[0]);
		
		idxTested = new int[1];
		idxTested[0] = t_idxTested;
		
		if  ( items[1] != null ) {
			idxInRel = new int[1];
			idxInRel[0] = Integer.parseInt(items[1]);
		}	
		
	}
	
	/* Factory method */
	public static AlignRelation createFromString(String str, int t_idxTested) {
		AlignRelation r = new AlignRelation();
		
		r.parseString(str, t_idxTested);
		return r;
	}
	
}
