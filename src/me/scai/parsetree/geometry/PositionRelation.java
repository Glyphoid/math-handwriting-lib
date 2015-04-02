package me.scai.parsetree.geometry;

import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.parsetree.geometry.GeometricRelation;
import me.scai.parsetree.geometry.GeometryHelper;

/* PositionRelation */
public class PositionRelation extends GeometricRelation {
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
	
	public PositionType positionType;
	
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
//			if ( positionType == PositionType.PositionSouth ||
//			     positionType == PositionType.PositionGenSouth) {
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
		
		positionType = PositionType.valueOf(items[0]);
		
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
