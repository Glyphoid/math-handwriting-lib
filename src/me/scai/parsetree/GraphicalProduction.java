package me.scai.parsetree;

import me.scai.handwriting.CWrittenTokenSet;

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
	
	/* eval: Test if the geometric relation is true */
	/*     Output is a float number between 0 and 1 */
	/*     0: 100% not true; 1: 100% true */
	/*	   "ti" stands for token index */
	public abstract float eval(CWrittenTokenSet wts, int [] tiTested, int [] tiInRel);
}


/* AlignRelation */
class AlignRelation extends GeometricRelation {
	public enum AlignType {
		AlignBottom, 
		AlignTop, 
		AlignLeft,
		AlignRight,
	};
	
	/* Member variables */
	AlignType alignType;
	
	/* Constructor */
	public AlignRelation(AlignType at, int t_idxTested, int t_idxInRel) {
		alignType = at;
		
		idxTested = new int[1];
		idxTested[0] = t_idxTested;
		
		idxInRel = new int[1];
		idxInRel[0] = t_idxInRel;
	}

	@Override
	public float eval(CWrittenTokenSet wts, int [] tiTested, int [] tiInRel) 
		throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
		if ( tiTested.length != 1 )
			throw new IllegalArgumentException("tiTested does not have length 1");
		
		if ( tiInRel.length != 1 )
			throw new IllegalArgumentException("tiTested does not have length 1");
		
		float [] bndsTested = wts.getTokenBounds(tiTested[0]);
		float [] bndsInRel = wts.getTokenBounds(tiInRel[1]);		
		
		float szTested, szInRel, szMean, edgeDiff;
		if ( alignType == AlignType.AlignBottom || 
			 alignType == AlignType.AlignTop ) {
			/* sz is height */
			szTested = bndsTested[3] - bndsTested[1];
			szInRel = bndsInRel[3] - bndsTested[1];
			
			if ( alignType == AlignType.AlignBottom ) 
				edgeDiff = Math.abs(bndsTested[3] - bndsInRel[3]);
			else
				edgeDiff = Math.abs(bndsTested[1] - bndsInRel[1]);
		}
		else {	/* sz is width */
			szTested = bndsTested[2] - bndsTested[0];
			szInRel = bndsInRel[2] - bndsTested[0];
			
			if ( alignType == AlignType.AlignRight ) 
				edgeDiff = Math.abs(bndsTested[2] - bndsInRel[2]);
			else
				edgeDiff = Math.abs(bndsTested[0] - bndsInRel[0]);
		}
		
		szMean = (szTested + szInRel) * 0.5f;
		
		float v = 1 - edgeDiff / szMean;
		if ( v < 0f ) 
			v = 0f;
		
		return v;
	}
	
}

/* PositionRelation */
class PositionRelation extends GeometricRelation {
	public enum PositionType {
		PositionWest,
		PositionEast,
		PositionSouth,
		PositionNorth,
		//PositionNorthwest, // TODO 
		//PositionSoutheast, // TODO
	}
	
	/* Member variables */
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
	public float eval(CWrittenTokenSet wts, int [] tiTested, int [] tiInRel) 
		throws IllegalArgumentException {
		if ( tiTested.length != 1 )
			throw new IllegalArgumentException("tiTested does not have length 1");
		
		if ( tiInRel.length != 1 )
			throw new IllegalArgumentException("tiTested does not have length 1");
		
		float [] bndsTested = wts.getTokenBounds(tiTested[0]);
		float [] bndsInRel = wts.getTokenBounds(tiInRel[1]);		
		
		float [] oldStayBnds = new float[2];
		float [] newStayBnds = new float[2];
		float [] lesserMoveBnds = new float[2];
		float [] greaterMoveBnds = new float[2];
		if ( positionType == PositionType.PositionEast || 
			 positionType == PositionType.PositionWest ) {
			/* Staying bounds are top and bottom */
			oldStayBnds[0] = bndsTested[1];
			oldStayBnds[1] = bndsTested[3];
			
			newStayBnds[0] = bndsInRel[1];
			newStayBnds[1] = bndsInRel[3];
			
			if ( positionType == PositionType.PositionEast ) { 
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
				  positionType == PositionType.PositionNorth ) {	/* sz is width */
			/* Staying bounds are left and right */
			oldStayBnds[0] = bndsTested[0];
			oldStayBnds[1] = bndsTested[2];
			
			newStayBnds[0] = bndsInRel[0];
			newStayBnds[1] = bndsInRel[2];
			
			if ( positionType == PositionType.PositionNorth ) { 
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
		
		float stayScore = GeometryHelper.pctOverlap(oldStayBnds, newStayBnds);
		if ( stayScore > 0.5f )
			stayScore = 1.0f;
		
		float moveScore = GeometryHelper.pctMove(lesserMoveBnds, greaterMoveBnds);
		
		float v = stayScore * moveScore;
		if ( v < 0.0f ) 
			v = 0.0f;
		else if ( v > 1.0f )
			v = 1.0f;
		
		return v;
	}
	
}


/* GraphicalProduction: Key class in the parser infrastructure.
 *     This specify the rules by which multiple 
 * tokens (CWrittenToken) or nodes (Node) combine into a meaningful 
 * collection. 
 */
public class GraphicalProduction {
	String lhs; 	/* Left-hand side, i.e., name of the production, e.g., DIGIT_STRING */
	
	int nhrs; 	   	/* Number of right-hand side tokens, e.g., 2 */
	String [] rhs; 	
	/* Right-hand side items: can be a list of terminal (T) and non-terminal (NT) items.
	 * E.g., {DIGIT, DIGIT_STRING} */
	
	boolean [] bt; 	/* Boolean flags for terminals (T), e.g., {true, false} */
	
	int headNode;	/* Index to the "head node" */

	
	public enum HeightRelationType {
		HeightRelationLess,
		HeightRelationEqual,
		HeightRelationGreater,
	}
	
	public enum WidthRelationType {
		WidthRelationLess,
		WidthRelationEqual,
		WidthRelationGreater,		
	}
}
