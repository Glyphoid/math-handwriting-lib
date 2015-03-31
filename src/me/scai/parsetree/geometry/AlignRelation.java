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
		AlignCenter,   /* Center of the left-right dimension */
		AlignWidthInclusion, /* Within the width range of the in-rel */
		AlignBottomNorthPastMiddle, /* The bottom of the token should be more north than the middle of the in-rel token */
		AlignTopNorthPastTop, 		/* The top of the token should be more north than the top of the in-rel token */
	};
	
	/* Member variables */
	/* Constants */
	static final float pastMiddleDisplacementLB = 0.25f; 	
	static final float pastMiddleDisplacementUB = 0.75f;	/* Apply to relations such as AlignBottomNorthPastMiddle */
	static final float pastEdgeDisplacementLB = -0.25f;
	static final float pastEdgeDisplacementUB = 0.00f;		/* Apply to relations such as AlignTopNorthPastTop */
	
	AlignType alignType;
	
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
			 alignType == AlignType.AlignHeightInclusion || 
			 alignType == AlignType.AlignBottomNorthPastMiddle || 
			 alignType == AlignType.AlignTopNorthPastTop ) { /* Align in the vertical dimension */
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
			else if ( alignType == AlignType.AlignBottomNorthPastMiddle ) {
				float midYInRel = (bndsInRel[3] + bndsInRel[1]) * 0.5f;
				
				float sb = bndsTested[3] - pastMiddleDisplacementLB * szTested;
				float nb = bndsTested[3] - pastMiddleDisplacementUB * szTested;
				
				v = (sb - midYInRel) / (sb - nb);
			}
			else if ( alignType == AlignType.AlignTopNorthPastTop ) {
				float sb = bndsInRel[1] + pastEdgeDisplacementUB * szTested;
				float nb = bndsInRel[1] + pastEdgeDisplacementLB * szTested;
				
				v = (sb - bndsTested[1]) / (sb - nb);
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
		
		if ( v > 1f )
			v = 1f;
		if ( v < 0f ) 
			v = 0f;
		
		return v;
	}

	@Override
	public void parseString(String str, int t_idxTested) {
		String [] items = splitInputString(str);
		
		if ( items[0].equals("AlignBottom") )	/* TODO: Replace with valueOf() */
			alignType = AlignType.AlignBottom;
		else if ( items[0].equals("AlignTop") )
			alignType = AlignType.AlignTop;
		else if ( items[0].equals("AlignMiddle") )
			alignType = AlignType.AlignMiddle;
		else if ( items[0].equals("AlignHeightInclusion") )
			alignType = AlignType.AlignHeightInclusion;
		else if ( items[0].equals("AlignLeft") )
			alignType = AlignType.AlignLeft;
		else if ( items[0].equals("AlignRight") )
			alignType = AlignType.AlignRight;
		else if ( items[0].equals("AlignLeftWithin") )
			alignType = AlignType.AlignLeftWithin;
		else if ( items[0].equals("AlignRightWithin") )
			alignType = AlignType.AlignRightWithin;
		else if ( items[0].equals("AlignCenter") )
			alignType = AlignType.AlignCenter;
		else if ( items[0].equals("AlignWidthInclusion") )
			alignType = AlignType.AlignWidthInclusion;
		else if ( items[0].equals("AlignBottomNorthPastMiddle") )
			alignType = AlignType.AlignBottomNorthPastMiddle;
		else if ( items[0].equals("AlignTopNorthPastTop") )
			alignType = AlignType.AlignTopNorthPastTop;		
		else
			throw new RuntimeException("Unrecognized AlignRelation type: " + items[0]);
		
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
