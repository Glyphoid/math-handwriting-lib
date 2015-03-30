package me.scai.parsetree;

import java.util.ArrayList;

import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.handwriting.CWrittenTokenSetNoStroke;
import me.scai.handwriting.Rectangle;

abstract class GeometricRelation {	
	protected int [] idxTested;      /* Indices to the tested tokens */ 
	protected int [] idxInRel;	 /* Indices to the in-relation-to tokens */
	/* Note that we use arrays to represent both the tested and the in-relation-to
	 * tokens, in order to make it sufficiently general. In most simple cases, 
	 * there should be only only one tested token and only one in-relation-to token. 
	 * Note that these are indices to items in GraphicalProduction rhs, not 
	 * tokens in the token set. The indices of the tokens in the token set are
	 * specified as input arguments to eval(). 
	 */
	
	/* Get the tested indices */
	public int [] getIdxTested() {
		return idxTested;
	}
	
	public int getNTested() {
		return idxTested.length;
	}
	
	/* Get the in-relation-to indices */
	public int [] getIdxInRel() {
		return idxInRel;
	}	

	public int getNInRel() {
		return idxInRel.length;
	}
	
	protected String [] splitInputString(String str) {
		String [] items;
		if ( (str.contains("(") && !str.contains(")")) ||
		     (str.contains("(") && !str.contains(")")) )
			throw new IllegalArgumentException("Unbalanced bracket order in string defining geometric relation");
				
		
		if ( str.contains("(") && str.contains(")") ) {
			int idxLB = str.indexOf("(");
			int idxRB = str.indexOf(")");
			
			if ( idxLB > idxRB )
				throw new IllegalArgumentException("Wrong bracket order in string defining geometric relation");
			
			str = str.substring(0, str.length() - 1);	/* Strip the right bracket */
			items = str.split("\\(");
		}
		else {
			items = new String[2];
			items[0] = str;
		}
		
		return items;
	}
	
	/* eval: Test if the geometric relation is true */
	/*     Output is a float number between 0 and 1 */
	/*     0: 100% not true; 1: 100% true */
	/*	   "ti" stands for token index */
	//public abstract float eval(CWrittenTokenSet wts, int [] tiTested, int [] tiInRel); /* To remove? */
	public abstract void parseString(String str, int t_idxTested);
	public abstract float verify(CAbstractWrittenTokenSet wtsTested,  float [] bndsInRel);
}


/* AlignRelation */
class AlignRelation extends GeometricRelation {
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

/* PositionRelation */
class PositionRelation extends GeometricRelation {
	public enum PositionType {
		PositionWest,
		PositionGenWest,
		PositionEast,
		PositionGenEast,
		PositionSouth,
		PositionGenSouth, 
		PositionNorth,
		PositionGenNorth
		//PositionNorthwest, // TODO 
		//PositionSoutheast, // TODO
	}
	/* Difference between PositionA and PositionGenA:
	 * For PositionA, the tested token must be to the due canonical direction 
	 * of the in-relation token. In addition, the tested token and the in-relation
	 * token must be sufficiently overlapping in the orthogonal direction. 
	 * PositionGenA does not have the second requirement. 
	 */
	
	/* Member variables */
	private PositionRelation() {}
	
	PositionType positionType;
	
	/* Constructor */
	public PositionRelation(PositionType pt, int t_idxTested, int t_idxInRel) {
		positionType = pt;
		
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
		
		float [] oldStayBnds = new float[2];
		float [] newStayBnds = new float[2];
		float [] lesserMoveBnds = new float[2];
		float [] greaterMoveBnds = new float[2];
		if ( positionType == PositionType.PositionEast || 
			 positionType == PositionType.PositionWest ||
			 positionType == PositionType.PositionGenEast ||
			 positionType == PositionType.PositionGenWest ) {
			/* Staying bounds are top and bottom */
			oldStayBnds[0] = bndsTested[1];
			oldStayBnds[1] = bndsTested[3];
			
			newStayBnds[0] = bndsInRel[1];
			newStayBnds[1] = bndsInRel[3];
			
			if ( positionType == PositionType.PositionEast || 
				 positionType == PositionType.PositionGenEast ) { 
				/* InRel is on the smaller side */
				lesserMoveBnds[0] = bndsInRel[0];
				lesserMoveBnds[1] = bndsInRel[2];
			
				greaterMoveBnds[0] = bndsTested[0];
				greaterMoveBnds[1] = bndsTested[2];
			}
			else {
				/* Tested is on the smaller side */
				lesserMoveBnds[0] = bndsTested[0];
				lesserMoveBnds[1] = bndsTested[2];
			
				greaterMoveBnds[0] = bndsInRel[0];
				greaterMoveBnds[1] = bndsInRel[2];
			}
			
		}
		else if ( positionType == PositionType.PositionSouth || 
				  positionType == PositionType.PositionNorth || 
				  positionType == PositionType.PositionGenSouth || 
				  positionType == PositionType.PositionGenNorth) {	/* sz is width */
			/* Staying bounds are left and right */
			oldStayBnds[0] = bndsTested[0];
			oldStayBnds[1] = bndsTested[2];
			
			newStayBnds[0] = bndsInRel[0];
			newStayBnds[1] = bndsInRel[2];
			
			if ( positionType == PositionType.PositionNorth ||
				 positionType == PositionType.PositionGenNorth) { 
				/* InRel is on the smaller side */
				lesserMoveBnds[0] = bndsInRel[1];
				lesserMoveBnds[1] = bndsInRel[3];
			
				greaterMoveBnds[0] = bndsTested[1];
				greaterMoveBnds[1] = bndsTested[3];
			}		
			else {
				/* Tested is on the smaller side */
				lesserMoveBnds[0] = bndsTested[1];
				lesserMoveBnds[1] = bndsTested[3];
			
				greaterMoveBnds[0] = bndsInRel[1];
				greaterMoveBnds[1] = bndsInRel[3];
			}
		}
		
		float stayScore;
		if ( positionType == PositionType.PositionGenEast || 
			 positionType == PositionType.PositionGenWest ||
			 positionType == PositionType.PositionGenNorth ||
		     positionType == PositionType.PositionGenSouth ) {
			stayScore = 1.0f;
		}
		else {
			stayScore = GeometryHelper.pctOverlap(oldStayBnds, newStayBnds);
			if ( stayScore > 0.5f )
				stayScore = 1.0f;
		}
		
		float moveScore = GeometryHelper.pctMove(lesserMoveBnds, greaterMoveBnds);
		if ( positionType == PositionType.PositionGenEast || 
				 positionType == PositionType.PositionGenWest ||
				 positionType == PositionType.PositionGenNorth ||
			     positionType == PositionType.PositionGenSouth )
			/* Leniency designed specially for exponentiation */
			moveScore = (float) Math.sqrt((double) moveScore);
			/* TODO: is sqrt() function ad hoc? */
		
		float v = stayScore * moveScore;
		if ( v < 0.0f ) 
			v = 0.0f;
		else if ( v > 1.0f )
			v = 1.0f;
		
		return v;
	}
	
	@Override
	public void parseString(String str, int t_idxTested) {
		String [] items = splitInputString(str);
		
		if ( items[0].equals("PositionWest") )
			positionType = PositionType.PositionWest;
		else if ( items[0].equals("PositionEast") )
			positionType = PositionType.PositionEast;
		else if ( items[0].equals("PositionSouth") )
			positionType = PositionType.PositionSouth;
		else if ( items[0].equals("PositionNorth") )
			positionType = PositionType.PositionNorth;
		else if ( items[0].equals("PositionGenWest") )
			positionType = PositionType.PositionGenWest;
		else if ( items[0].equals("PositionGenEast") )
			positionType = PositionType.PositionGenEast;
		else if ( items[0].equals("PositionGenSouth") )
			positionType = PositionType.PositionGenSouth;
		else if ( items[0].equals("PositionGenNorth") )
			positionType = PositionType.PositionGenNorth;
		else
			throw new RuntimeException("Unrecognized PositionType: " + items[0]);
		
		idxTested = new int[1];
		idxTested[0] = t_idxTested;
		
		if  ( items[1] != null ) {
			idxInRel = new int[1];
			idxInRel[0] = Integer.parseInt(items[1]);
		}	
		
	}
	
	/* Factory method */
	public static PositionRelation createFromString(String str, int t_idxTested) {
		PositionRelation r = new PositionRelation();
		
		r.parseString(str, t_idxTested);
		return r;
	}
	
}

/* HeightRelation */
class HeightRelation extends GeometricRelation {
	public enum HeightRelationType {
		HeightRelationLess,
		HeightRelationEqual,
		HeightRelationGreater,
	}
	
	/* Member variables */
	private HeightRelation() {}
	
	HeightRelationType heightRelationType;
	
	/* Constructor */
	public HeightRelation(HeightRelationType hrt, int t_idxTested, int t_idxInRel) {
		heightRelationType = hrt;
		
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
		
		float hTested = bndsTested[3] - bndsTested[1];
		float hInRel = bndsInRel[3] - bndsInRel[1];
		float hMean = (hTested + hInRel) * 0.5f;
		
		float v;
		if ( heightRelationType == HeightRelationType.HeightRelationEqual ) {
			v = 1.0f - Math.abs(hTested - hInRel) / hMean;
			if ( v > 0.75f ) /* Slack */
				v = 1.0f;
		}
		else if ( heightRelationType == HeightRelationType.HeightRelationGreater  ) {
			v = (hTested - hInRel) / hInRel;
			if ( v > 0.5f )
				v = 1.0f;
		}
		else /* heightRelationType == HeightRelationType.HeightRelationLess */ {
			v = (hInRel - hTested) / (hInRel * 0.1f); /* TODO: Remove ad hoc coefficients */
//			if ( v > 0.1f )
//				v = 1.0f;
		}
		
		if ( v > 1.0f )
			v = 1.0f;
		else if ( v < 0.0f )
			v = 0.0f;
		
		return v;
	}
	
	@Override
	public void parseString(String str, int t_idxTested) {
		String [] items = splitInputString(str);
		
		if ( items[0].equals("HeightRelationLess") )
			heightRelationType = HeightRelationType.HeightRelationLess;
		else if ( items[0].equals("HeightRelationEqual") )
			heightRelationType = HeightRelationType.HeightRelationEqual;
		else if ( items[0].equals("HeightRelationGreater") )
			heightRelationType = HeightRelationType.HeightRelationGreater;
		else
			throw new RuntimeException("Unrecognized HeightRelation: " + items[0]);
			
		idxTested = new int[1];
		idxTested[0] = t_idxTested;
		
		if  ( items[1] != null ) {
			idxInRel = new int[1];
			idxInRel[0] = Integer.parseInt(items[1]);
		}	
		
	}
	
	/* Factory method */
	public static HeightRelation createFromString(String str, int t_idxTested) {
		HeightRelation r = new HeightRelation();
		
		r.parseString(str, t_idxTested);
		return r;
	}
	
}

/* WidthRelation */
class WidthRelation extends GeometricRelation {
	public enum WidthRelationType {
		WidthRelationLess,
		WidthRelationEqual,
		WidthRelationGreater,
	}
	
	/* Member variables */		
	WidthRelationType widthRelationType;
	
	/* Constructor */
	private WidthRelation() {}
	
	public WidthRelation(WidthRelationType hrt, int t_idxTested, int t_idxInRel) {
		widthRelationType = hrt;
		
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

		float wTested = bndsTested[3] - bndsTested[1];
		float wInRel = bndsInRel[3] - bndsInRel[1];
		float wMean = (wTested + wInRel) * 0.5f;
		
		float v;
		if ( widthRelationType == WidthRelationType.WidthRelationEqual ) {
			v = 1.0f - Math.abs(wTested - wInRel) / wMean;
			if ( v > 0.75f ) /* Slack */
				v = 1.0f;
		}
		else if ( widthRelationType == WidthRelationType.WidthRelationGreater  ) {
			v = (wTested - wInRel) / wInRel;
			if ( v > 0.5f )
				v = 1.0f;
		}
		else /* widthRelationType == WidthRelationType.WidthRelationLess */ {
			v = (wInRel - wTested) / wInRel;
			if ( v > 0.5f )
				v = 1.0f;
		}
		
		if ( v > 1.0f )
			v = 1.0f;
		else if ( v < 0.0f )
			v = 0.0f;
		
		return v;
	}
	
	@Override
	public void parseString(String str, int t_idxTested) {
		String [] items = splitInputString(str);
		
		if ( items[0].equals("WidthRelationLess") )
			widthRelationType = WidthRelationType.WidthRelationLess;
		else if ( items[0].equals("WidthRelationEqual") )
			widthRelationType = WidthRelationType.WidthRelationEqual;
		else if ( items[0].equals("WidthRelationGreater") )
			widthRelationType = WidthRelationType.WidthRelationGreater;
		else
			throw new RuntimeException("Unrecognized WidthRelationType: " + items[0]);
		
		idxTested = new int[1];
		idxTested[0] = t_idxTested;
		
		if  ( items[1] != null ) {
			idxInRel = new int[1];
			idxInRel[0] = Integer.parseInt(items[1]);
		}	
		
	}
	
	/* Factory method */
	public static WidthRelation createFromString(String str, int t_idxTested) {
		WidthRelation r = new WidthRelation();
		
		r.parseString(str, t_idxTested);
		return r;
	}
}

/* Class GeometricShortcut 
 * Detects short cut for dividing non-head tokens into sets for parsing 
 * see: GraphicalProduction.attempt(); MathHelper.getFullDiscreteSpace();
 */
class GeometricShortcut {
	public enum ShortcutType {
		noShortcut,
		horizontalTerminalDivide,		  /* 2-part shortcut types */ /* North to south */
		horizontalTerminalDivideSN,		  /* South to north */
		verticalTerminalDivideWE, /* 3-part shortcut types, with the head being T */ /* West to east */
		verticalTerminalDivideEW, /* East to west */
		verticalNT1T2DivideWE,		  /* 3-part shortcut types, with the head being NT and the remaining two items being both T. Hence the "NT1T2". Example: (Addition, Bracket_L, Bracket_R) */		
		westEast, 				  /* 1-part shortcut type: head is at west and the (only) non-head is at the east */
	}
	
	/* Member variables */
	public ShortcutType shortcutType = ShortcutType.noShortcut;
	
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
			}
			else {
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
		}
		else if ( nrhs == 3 ){ // 0712	
			/* Examine whether the head-node is T. 
			 * If so, this is potentially a _TerminalDivide__ (e.g., verticalDivideWE) type shortcut.
			 * If not, go to the next logical branch. */
			String tripartiteType = null;
			if ( termSet.isTypeTerminal(gp.rhs[0]) )
				tripartiteType = "TeminalDivide";
			else if ( !termSet.isTypeTerminal(gp.rhs[0]) &&
				      termSet.isTypeTerminal(gp.rhs[1]) && termSet.isTypeTerminal(gp.rhs[2]) )
				tripartiteType = "NT1T2Divide";
			
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
					if ( tripartiteType.equals("TeminalDivide") )
						shortcutType = ShortcutType.verticalTerminalDivideWE;
					else if ( tripartiteType.equals("NT1T2Divide") )
						shortcutType = ShortcutType.verticalNT1T2DivideWE;
				}
				else if ( posType[0] == PositionRelation.PositionType.PositionEast && posType[1] == PositionRelation.PositionType.PositionWest ||
						  posType[0] == PositionRelation.PositionType.PositionGenEast && posType[1] == PositionRelation.PositionType.PositionGenWest ) {
					if ( tripartiteType.equals("TeminalDivide") )
						shortcutType = ShortcutType.verticalTerminalDivideEW;
				}
				else if ( posType[0] == PositionRelation.PositionType.PositionNorth && posType[1] == PositionRelation.PositionType.PositionSouth ||
						  posType[0] == PositionRelation.PositionType.PositionGenNorth && posType[1] == PositionRelation.PositionType.PositionGenSouth ) {
					if ( tripartiteType.equals("TeminalDivide") )
						shortcutType = ShortcutType.horizontalTerminalDivide;
				}
				else if ( posType[0] == PositionRelation.PositionType.PositionSouth && posType[1] == PositionRelation.PositionType.PositionNorth ||
						  posType[0] == PositionRelation.PositionType.PositionGenSouth && posType[1] == PositionRelation.PositionType.PositionGenNorth ) {
					if ( tripartiteType.equals("TeminalDivide") )
						shortcutType = ShortcutType.horizontalTerminalDivideSN;
				}
			}
		}		
		else {
			throw new RuntimeException("Unexpected number of rhs items: " + nrhs);
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
		
		if ( nt == 0 )
			throw new RuntimeException("Attempting to apply bipartite shortcut on one or fewer tokens");
		
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
			throw new RuntimeException("Geometric shortcuts do not exist");
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
			else if ( shortcutType == ShortcutType.horizontalTerminalDivide ) {
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
		else if ( shortcutType == ShortcutType.horizontalTerminalDivide ) {
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

/* GraphicalProduction: Key class in the parser infrastructure.
 *     This specify the rules by which multiple 
 * tokens (CWrittenToken) or nodes (Node) combine into a meaningful 
 * collection. 
 */
public class GraphicalProduction {
	/* Tree association for dealing with structures such as additions and subtractions in a row 
	 * This could probably be taken care of by grammar. But currently we deal with it by 
	 * using this association.
	 */
	public enum AssocType {	
		NoAssoc,	
		AssocLeft2A,		/* For productions with two RHS items. Bias toward the first item. */
		AssocRight2B, 		/* For productions with two RHS items. Bias toward the second item. */
		AssocLeft3B,			/* For productions with three RHS items, the first one being the T head node. Bias toward the second item. */		
//		BiasRight
	};
	
	/* Member variables */
	/* Constants */
	final static String strinigzationTag = "_STRINGIZE_";
	final static String evalTag = "_EVAL_";
	final static String mathTexTag = "_MATH_TEX_";
	/* ~Constants */
	
	private final static String tokenRelSeparator = ":";
	private final static String relSeparator = ",";
	public final static String sumStringArrow = "->";
	
	public final static float flagNTNeedsParsing = 111f;
	
	String lhs; 	/* Left-hand side, i.e., name of the production, e.g., DIGIT_STRING */
	
	//int level;		/* Level: 0 is the lowest: numbers */
	private int nrhs; 	   	/* Number of right-hand side tokens, e.g., 2 */
	String [] rhs;
	boolean [] rhsIsTerminal;
	/* Right-hand side items: can be a list of terminal (T) and non-terminal (NT) items.
	 * E.g., {DIGIT, DIGIT_STRING} */
	
	boolean [] bt; 	/* Boolean flags for terminals (T), e.g., {true, false} */
	
	String sumString; 
	/* Production summary string that does not contain geometric information, e.g.,
	 * "DIGIT_STRING --> DIGIT DIGIT_STRING" 
	 */
	
	GeometricRelation [][] geomRels;
	GeometricShortcut geomShortcut;
	
	AssocType assocType = AssocType.NoAssoc;
	String assocName = "";
	/* In addition to association type, the association name needs to be specified. 
	 * This is because different sets of productions may share different associations, 
	 * e.g., addition-subtraction, multiplication-division (non-fraction)
	 */
	
	String [] stringizeInstr;		/* Instruction for stringization */
	String [] mathTexInstr;         /* Instruction for generating Math TeX */
	String [] mathMlInstr;         /* Instruction for generating MathML */
	String [] evalInstr;            /* Instruction for evaluation */
	
	/* ~Member variables */

	/* Methods */
	/* Constructors */
	public GraphicalProduction(String t_lhs, 
			                   String [] t_rhs, 
			                   boolean [] t_bt, 
			                   TerminalSet termSet, 		/* For determine if the rhs are terminals */
			                   GeometricRelation [][] t_geomRels, 
			                   AssocType t_assocType,
			                   String t_assocName, 
			                   String [] t_stringizeInstr,
			                   String [] t_mathTexInstr,
			                   String [] t_evalInstr) {
		lhs = t_lhs;
		rhs = t_rhs;
		bt = t_bt;
		geomRels = t_geomRels;
		
		nrhs = t_rhs.length;
		
		/* Determine if the rhs items are terminals */
		if ( rhs != null ) {
			rhsIsTerminal = new boolean[nrhs];
			for (int i = 0; i < nrhs; ++i)
				rhsIsTerminal[i] = termSet.isTypeTerminal(rhs[i]); 
		}
		else {
			System.err.println("WARNING: no rhs");
		}
		
		/* Generate summary string */
		genSumString();
		
		/* Generate geometric shortcut, if any. 
		 * If there is no shortcut, shortcutType will be noShortcut. */
		if (t_lhs.startsWith("ASSIGNMENT_")) {
			int i = 0; 
			i += 0;
		}
		
		geomShortcut = new GeometricShortcut(this, termSet);
		
		assocType = t_assocType;
		assocName = t_assocName;
		stringizeInstr = t_stringizeInstr;
		mathTexInstr = t_mathTexInstr;
		evalInstr = t_evalInstr;
	}
	
	private void genSumString() {
		sumString = lhs + sumStringArrow;
		for (int i = 0; i < rhs.length; ++i) {
			sumString += rhs[i];
			if ( i < rhs.length - 1 )
				sumString += " ";
		}
	}
	
	private void createHeadChild(Node n) {
		/* Create head child node */
		if ( n != null && 
	         rhs.length > 0 ) {
			/* TODO: hc should contain information about the 
			 * tokens that make up of the head child for further 
			 * parsing.
			 * hc also needs to be expanded if it is an NT. 
			 */
			
			Node hc = new Node(lhs, rhs[0], rhs[0]); /* TODO: Second input argument is erroneous? */
			n.setChild(0, hc);
//			n.addChild(hc);
		}
	}
	
	/* Attempt to parse the input token set with this production, 
	 * given that the j-th token is used as the head.
	 * 
	 * Return: Node: node with the production, geometric, and other 
	 *               information. 
	 *               
	 * Side effect input:
	 *         remaainingSets: token sets after the head node is 
	 *         parsed out. null if parsing is unsuccessful. 
	 */
	public Node attempt(CWrittenTokenSetNoStroke tokenSet, 
			            int [] iHead,
			            CAbstractWrittenTokenSet [] remainingSets, 			//PerfTweak new
			            float [] maxGeomScore) {

		if ( iHead.length == 0 )
			throw new RuntimeException("GraphicalProductionSet.attempt encountered empty idxHead.");
		
		/* Configuration constants */
		final boolean bUseShortcut = true; /* TODO: Get rid of this constant when the method proves to be reliable */
		
		int nnht = tokenSet.nTokens() - iHead.length; /* Number of non-head tokens */
		int nrn = nrhs - 1; /* Number of remaining nodes to match */
						
		if ( (nrn > nnht) || 
		     (nrn == 0 && nnht > 0) ) {
			maxGeomScore[0] = 0.0f;
			Node n = new Node(lhs, sumString, rhs);
			
			createHeadChild(n);
			
			return n;
		}		

		int [][] labels = null;

		if ( geomShortcut.existsTripartiteTerminal() && bUseShortcut ) {
			/* Use this smarter approach when a geometric shortcut exists */
			labels = geomShortcut.getPartitionTripartiteTerminal(tokenSet, iHead);
		}
		else {
			/* Get all possible partitions: in "labels" */
			/* This is the brute-force approach. */
			labels = MathHelper.getFullDiscreteSpace(nrn, nnht);
		}
		
		/* TODO: Use shortcuts based on the production */

	    /* Get index to all non-head token */
	    ArrayList<Integer> inht = new ArrayList<Integer>();
	    
	    for (int i = 0; i < tokenSet.nTokens(); ++i) {
	    	boolean bContains = false;
	    	for (int j = 0; j < iHead.length; ++j) {
	    		if ( iHead[j] == i ) {
	    			bContains = true;
	    			break;
	    		}
	    	}
	    	if ( !bContains )
	    		inht.add(i);
	    }
	    
	    /* Construct the remaining sets and evaluate their geometric relations */
//	    boolean bFound = false;
	    CWrittenTokenSetNoStroke [][] a_rems = new CWrittenTokenSetNoStroke[labels.length][];
	    float [] geomScores = new float[labels.length];
	    
	    for (int i = 0; i < labels.length; ++i) {		/* Iterate through all partitions */
	    	a_rems[i] = new CWrittenTokenSetNoStroke[nrn];
	    	boolean [] remsFilled = new boolean[nrn];
	    	
	    	for (int j = 0; j < nrn; ++j)
	    		 /* TODO: Type safety check */
	    		a_rems[i][j] = new CWrittenTokenSetNoStroke();
    		
    		for (int k = 0; k < labels[i].length; ++k) {
    			int inode = labels[i][k];
    			int irt = inht.get(k);
    			
    			/* The last input argument sets bCheck to false for speed */
    			/* Is this a dangerous action? */
    			a_rems[i][inode].addToken(tokenSet.tokens.get(irt));
    			a_rems[i][inode].tokenIDs.add(tokenSet.tokenIDs.get(irt));
    			
    			remsFilled[inode] = true;
    		}
    		
    		for (int j = 0; j < nrn; ++j) {
    			a_rems[i][j].calcBounds();
    		}
    		
    		/* If there is any unfilled remaining token set, skip */
    		boolean bAllFilled = true;
    		for (int j = 0; j < nrn; j++) {
    			if ( !remsFilled[j] ) {
    				bAllFilled = false;
    				break;
    			}
    		}
    		
    		if ( !bAllFilled )
    			continue;
    		
    		for (int j = 0; j < nrn; ++j)
    			a_rems[i][j].calcBounds();
    		
    		/* Verify geometric relations */
//    		boolean bAllGeomRelVerified = true;
    		if ( nrn > 0 ) {    		
	    		float [] t_geomScores = new float[nrn];
		    	for (int j = 0; j < nrn; ++j) {
		    		
		    		/* Assume: there is only one head 
		    		 * TODO: Make more general */
		    		/* TODO: Deal with the case in which the remaining node is a Terminal (T) */
		    		
		    		if ( this.rhsIsTerminal[j + 1] ) {
//		    			System.out.println("attempt encountered Terminal non-head: " + this.rhs[j + 1] + 
//		    					           "; rem set nTokens = " + a_rems[i][j].nTokens() + 
//		    					           "; rem set tokens[0] = " + a_rems[i][j].tokens.get(0).getRecogWinner());
		    			/* TODO: Get the type of string, e.g, 1 -> DIGIT, ( -> BRACKET_L. 
		    			 *       May need to add terminal set as an input argument. */
		    			CWrittenTokenSetNoStroke tTokenSet = a_rems[i][j];
		    			int t_nTokens = tTokenSet.nTokens();
		    			if ( t_nTokens != 1 )
		    				throw new RuntimeException("Encountered unexpected value of t_nTokens: != 1");
		    			
		    			String tokenTermType = tTokenSet.tokens.get(0).tokenTermType;
		    			if ( tokenTermType.equals(this.rhs[j + 1]) )
		    				t_geomScores[j] = 1.0f;
	    				else
	    					t_geomScores[j] = 0.0f;
		    		}
		    		else {		    		
//		    			boolean bVerified = true;
			    		if ( geomRels[j + 1] == null ) {
			    			t_geomScores[j] = 1.0f;
			    			continue;
			    		}
			    		
			    		float [] t_t_geomScores = new float[geomRels[j + 1].length];
			    		for (int k = 0; k < geomRels[j + 1].length; ++k) {
			    			int idxInRel = geomRels[j + 1][k].idxInRel[0];
			    			float [] bndsInRel;
			    			if ( idxInRel == 0 ) {
			    				bndsInRel = tokenSet.getTokenBounds(iHead);
			    			}
			    			else {
			    				bndsInRel = a_rems[i][idxInRel - 1].getSetBounds();
			    			}
			    			
			    			float v = geomRels[j + 1][k].verify(a_rems[i][j], bndsInRel);
	
			    			t_t_geomScores[k] = v;
			    			
			    		}
			    		
			    		t_geomScores[j] = MathHelper.mean(t_t_geomScores);
		    		}
		    	}
		    	
		    	geomScores[i] = MathHelper.mean(t_geomScores);
    		}
    		else {
    			/* This is the case in which the entire token is the head, 
    			 * and the head is an NT. 
    			 */
    			if ( !rhsIsTerminal[0] )
    				geomScores[i] = flagNTNeedsParsing;	/* 2.0f is a flag that indicates further geometric parsing is necessary */
    			else
    				geomScores[i] = 1.0f;
    		}
	    }
	    
	    /* Find the partition that leads to the maximum geometric score */
	    int idxMax = MathHelper.indexMax(geomScores);
	    maxGeomScore[0] = geomScores[idxMax];
	    
	    /* For head */
	    /* TODO: Replace with constructor */
	    CWrittenTokenSetNoStroke headTokenSet = new CWrittenTokenSetNoStroke(tokenSet, iHead);
	    
	    remainingSets[0] = headTokenSet;
	    
	    /* For non-head */
	    for (int i = 0; i < a_rems[idxMax].length; ++i) {
	    	remainingSets[i + 1] = a_rems[idxMax][i];		//PerfTweak new
	    }
	    
	    Node n = new Node(lhs, sumString, rhs);
	    
	    createHeadChild(n);
	    
	    return n;
	}
	
	
	public static GraphicalProduction genFromStrings(ArrayList<String> strs, TerminalSet termSet)
		throws Exception 
	{		
		final int expectedNumNonRhsLines = 4;
				
		if ( strs.size() <= expectedNumNonRhsLines ) {
			throw new RuntimeException("Incorrect number of lines (" + strs.size() 
					                   + ") for creating new graphical production");
		}
		
		String t_lhs;
		AssocType t_assocType = AssocType.NoAssoc;
		String t_assocName = "";
		String headLine = strs.get(0).trim();
		
//		if ( headLine.contains(tokenRelSeparator) ) 
//			throw new Exception("Head node unexpectedly contains geometric relation(s)");
		
		int nLBs = TextHelper.numInstances(headLine, "(");
		int nRBs = TextHelper.numInstances(headLine, ")");
		
		if ( nLBs == 1 && nRBs == 1 ) {	/* Bias is included */
			int iLB = headLine.indexOf("(");
			int iRB = headLine.indexOf(")");
			if ( iLB > iRB )
				throw new Exception("Syntax error in line: \"" + headLine + "\": Wrong order of the left and right brackets");
			
			String assocStr = headLine.substring(iLB + 1, iRB).trim();
			int [] idxColon = TextHelper.findAll(assocStr, ":");
			if ( idxColon.length != 2 )
				throw new Exception("Syntax error in line: \"" + headLine + "\": Number of colons is not equal to two");
			
			String assocHeaderStr = assocStr.substring(0, idxColon[0]).trim();
			if ( !assocHeaderStr.equals("ASSOC") )
				throw new Exception("Syntax error in line: \"" + headLine + "\": The string preceding the first colon is not as expected");
			
			String assocTypeStr = assocStr.substring(idxColon[0] + 1, idxColon[1]).trim();
			if ( assocTypeStr.equals("ASSOC_LEFT_2A") )
				t_assocType = AssocType.AssocLeft2A;
			else if ( assocTypeStr.equals("ASSOC_RIGHT_2B") )
				t_assocType = AssocType.AssocRight2B;
			else if ( assocTypeStr.equals("ASSOC_LEFT_3B") )
				t_assocType = AssocType.AssocLeft3B;
			else
				throw new Exception("Unrecognized association type: " + assocTypeStr);
			
			t_assocName = assocStr.substring(idxColon[1] + 1, assocStr.length()).trim();
			if ( t_assocName.length() == 0 )
				throw new Exception("Syntax error in line: \"" + headLine + "\": The association name is empty");
			
			t_lhs = headLine.substring(0, iLB).trim();
		}
		else {
			if ( !( nLBs == 0 && nRBs == 0 ) )
				throw new Exception("Syntax error in line: \"" + headLine + "\"");
			
			t_lhs = headLine;
		}		
		
		int t_nrhs = strs.size() - expectedNumNonRhsLines;
		String [] t_rhs = new String[t_nrhs];
		boolean [] t_bt = new boolean[t_nrhs];
		GeometricRelation [][] t_geomRels = new GeometricRelation[t_nrhs][];
		
		String [] t_stringizeInstr = null;
		String [] t_mathTexInstr = null;
		String [] t_evalInstr = null;
		
		for (int k = 0; k < t_nrhs; ++k) {
			String line = strs.get(k + 1);
			
			if ( k == 0 ) {
				/* This is the head node, no geometrical relation is expected */	
				/* We also need to extract the BiasType, if it exists */ 
				
				t_rhs[k] = line.trim();
			}
			else {
				if ( line.contains(tokenRelSeparator) ) {
					t_rhs[k] = line.split(tokenRelSeparator)[0].trim();
					
					String relString = line.split(tokenRelSeparator)[1].trim();
					if ( !relString.startsWith("{") || !relString.endsWith("}") )
						throw new Exception("Syntax error in input productions file"); 
					relString = relString.substring(1, relString.length() - 1);
					
					String [] relItems = relString.split(relSeparator);
					
					t_geomRels[k] = new GeometricRelation[relItems.length];
					for (int j = 0; j < relItems.length; ++j) {
						String relStr = relItems[j].trim();
						if ( relStr.startsWith("Align") )
							t_geomRels[k][j] = AlignRelation.createFromString(relStr, k);
						else if ( relStr.startsWith("Position") )
							t_geomRels[k][j] = PositionRelation.createFromString(relStr, k);
						else if ( relStr.startsWith("Height") )
							t_geomRels[k][j] = HeightRelation.createFromString(relStr, k);
						else if ( relStr.startsWith("Width") )
							t_geomRels[k][j] = WidthRelation.createFromString(relStr, k);
						else 
							throw new Exception("Unrecognized geometric relation string: " + relStr); 
					}
				}
				else {
//					if ( line.trim().equals(TerminalSet.epsString) )
//						t_rhs[k] = TerminalSet.epsString;
//					else
					throw new RuntimeException("Encountered a non-head node with no geometric relations specified");
				}
			}
			
			t_bt[k] = termSet.isTypeTerminal(t_rhs[k]);
			
			
		}
		
		/* Parse stringizeInst: stringization instructions */
		String tline = strs.get(strs.size() - 3).trim();
		if ( !tline.startsWith(strinigzationTag) ) {
			throw new RuntimeException("Cannot find stringization tag in line: \"" + tline + "\"");
		}
		tline = tline.replace("\t", " ");
		ArrayList<String> listItems = new ArrayList<String>();
		String [] t_items = tline.split(" ");
		
		for (int n = 1; n < t_items.length; ++n)
			if ( t_items[n].length() > 0 )
				listItems.add(t_items[n]);
		
		t_stringizeInstr = new String[listItems.size()];
		listItems.toArray(t_stringizeInstr);
		
		/* Parse mathTeX instructions */
		tline = strs.get(strs.size() - 2).trim();
		if ( !tline.startsWith(mathTexTag) ) {
			throw new RuntimeException("Cannot find math TeX tag in line: \"" + tline + "\"");
		}
		tline = tline.replace("\t", " ");
		
		ArrayList<String> mtListItems = new ArrayList<String>();
		String [] t_mtItems = tline.split(" ");
		
		for (int n = 1; n < t_mtItems.length; ++n) {
			if ( t_mtItems[n].length() > 0 ) {
				mtListItems.add(t_mtItems[n]);
			}
		}
		
		t_mathTexInstr = new String[mtListItems.size()];
		mtListItems.toArray(t_mathTexInstr);	
		
		/* Parse evalInstr: evaluation instructions */
		tline = strs.get(strs.size() - 1).trim();
		if ( !tline.startsWith(evalTag) ) {
			throw new RuntimeException("Cannot find evaluation tag in line: \"" + tline + "\"");
		}
		
		String evalStr = tline.replace(evalTag, "").trim();
		nLBs = TextHelper.numInstances(evalStr, "(");
		nRBs = TextHelper.numInstances(evalStr, ")");
		
		ArrayList<String> evalItems = new ArrayList<String>();
		if ( nLBs == 0 && nRBs == 0 ) {
			evalItems.add("PASS");
			if ( evalStr.contains(" ")  || evalStr.contains("\t") )
				throw new Exception("Syntax error in evaluation instruction: \"" + evalStr + "\"");
			evalItems.add(evalStr);
		}
		else if ( nLBs == 1 && nRBs == 1 ) {
			String funcName = evalStr.substring(0, evalStr.indexOf("("));
			evalItems.add(funcName);
			
			String argsStr = evalStr.substring(evalStr.indexOf("(") + 1, evalStr.indexOf(")")).trim();
			argsStr = argsStr.replace("\t", " ").replace(",", " ");
			String [] argsItems = argsStr.split(" ");
			for (int m = 0; m < argsItems.length; ++m)
				if ( argsItems[m].length() > 0 )
					evalItems.add(argsItems[m]);
		}
		
		t_evalInstr = new String[evalItems.size()];
		evalItems.toArray(t_evalInstr);
			
		
		/* Sanity check for association types */
		/* TODO */
		if ( t_assocType == AssocType.AssocLeft2A || 
			 t_assocType == AssocType.AssocRight2B) {
			if ( t_rhs.length != 2 )
				throw new RuntimeException("Under the current association type, the number of rhs (" + t_rhs.length + ") is incorrect.");
			if ( !t_rhs[0].equals(t_rhs[1]) )
				throw new RuntimeException("Under the current association type, it is unacceptable that the 2nd and 3rd RHS items are different");
		}
		else if ( t_assocType == AssocType.AssocLeft3B ) {
			if ( t_rhs.length != 3 )
				throw new RuntimeException("Under the current association type, the number of rhs (" + t_rhs.length + ") is incorrect.");
			if ( !t_rhs[1].equals(t_rhs[2]) )
				throw new RuntimeException("Under the current association type, it is unacceptable that the 2nd and 3rd RHS items are different");
		}
				
		return new GraphicalProduction(t_lhs, t_rhs, t_bt, termSet, t_geomRels, 
									   t_assocType, t_assocName, 
									   t_stringizeInstr, t_mathTexInstr, t_evalInstr);
	}
	
	public int getNumNonHeadTokens() {
		return nrhs - 1;
	}
	
	@Override
	public String toString() {
		String s = "GP: ";
		if ( sumString != null )
			s += sumString;
		return s;
	}
	
}

