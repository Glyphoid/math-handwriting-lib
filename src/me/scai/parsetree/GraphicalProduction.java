package me.scai.parsetree;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.handwriting.CWrittenTokenSet;
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
		AlignCenter,   /* Center of the left-right dimension */
		AlignWidthInclusion, /* Within the width range of the in-rel */
	};
	
	/* Member variables */
	AlignType alignType;
	
	/* Constructor */
	private AlignRelation() {}
	
	private static float inclusionEdgeDiff(float [] limsTested, float [] limsInRel) {
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
		if ( alignType == AlignType.AlignBottom || 
			 alignType == AlignType.AlignTop ||
			 alignType == AlignType.AlignMiddle || 
			 alignType == AlignType.AlignHeightInclusion ) { /* Align in the vertical dimension */
			/* sz is height */
			szTested = bndsTested[3] - bndsTested[1];
			szInRel = bndsInRel[3] - bndsInRel[1];
			
			if ( alignType == AlignType.AlignBottom ) {
				edgeDiff = Math.abs(bndsTested[3] - bndsInRel[3]);
			}
			else if ( alignType == AlignType.AlignTop ) {
				edgeDiff = Math.abs(bndsTested[1] - bndsInRel[1]);
			}
			else if ( alignType == AlignType.AlignMiddle ) {
				edgeDiff = Math.abs((bndsTested[1] + bndsTested[3]) * 0.5f - 
						            (bndsInRel[1] + bndsInRel[3]) * 0.5f);
			}
			else {
				float [] limsTested = new float[2];
				float [] limsInRel = new float[2];
				limsTested[0] = bndsTested[1]; 
				limsTested[1] = bndsTested[3];
				limsInRel[0] = bndsInRel[1];
				limsInRel[1] = bndsInRel[3]; 
				
				edgeDiff = inclusionEdgeDiff(limsTested, limsInRel);
			}
		}
		else {	/* sz is width */
			szTested = bndsTested[2] - bndsTested[0];
			szInRel = bndsInRel[2] - bndsInRel[0];
			
			if ( alignType == AlignType.AlignLeft ) {
				edgeDiff = Math.abs(bndsTested[2] - bndsInRel[2]);
			}
			else if ( alignType == AlignType.AlignRight ) {
				edgeDiff = Math.abs(bndsTested[0] - bndsInRel[0]);
			}
			else if ( alignType == AlignType.AlignCenter ) {
				edgeDiff = Math.abs((bndsTested[0] + bndsTested[2]) * 0.5f - 
			                        (bndsInRel[0] + bndsInRel[2]) * 0.5f);
			}
			else {
				float [] limsTested = new float[2];
				float [] limsInRel = new float[2];
				limsTested[0] = bndsTested[0]; 
				limsTested[1] = bndsTested[2];
				limsInRel[0] = bndsInRel[0];
				limsInRel[1] = bndsInRel[2]; 
				
				edgeDiff = inclusionEdgeDiff(limsTested, limsInRel);				
			}
		}
		
		szMean = (szTested + szInRel) * 0.5f;
		
		float v = 1 - edgeDiff / szMean;
		if ( v < 0f ) 
			v = 0f;
		
		return v;
	}

	@Override
	public void parseString(String str, int t_idxTested) {
		String [] items = splitInputString(str);
		
		if ( items[0].equals("AlignBottom") )
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
		else if ( items[0].equals("AlignCenter") )
			alignType = AlignType.AlignCenter;
		else
			alignType = AlignType.AlignWidthInclusion;
		
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
		else
			positionType = PositionType.PositionGenNorth;
		
		
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
			v = (hInRel - hTested) / hInRel;
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
		
		if ( items[0].equals("HeightRelationLess") )
			heightRelationType = HeightRelationType.HeightRelationLess;
		else if ( items[0].equals("HeightRelationEqual") )
			heightRelationType = HeightRelationType.HeightRelationEqual;
		else
			heightRelationType = HeightRelationType.HeightRelationGreater;
		
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
		else
			widthRelationType = WidthRelationType.WidthRelationGreater;
		
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
		horizontalDivideNS,		/* North to south */ 
		horizontalDivideSN,		/* South to north */
		verticalDivideWE,		/* West to east */
		verticalDivideEW,		/* East to west */
	}
	
	/* Member variables */
	public ShortcutType shortcutType = ShortcutType.noShortcut;
	
	/* Methods */
	public GeometricShortcut(GraphicalProduction gp) {
		if ( gp.geomRels.length != 3 ) {
			/* Currently, we deal with only bipartite shortcuts, such as linear divides.
			 * This may change in the future.
			 */
			shortcutType = ShortcutType.noShortcut;
			return;
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
				shortcutType = ShortcutType.verticalDivideWE;
			}
			else if ( posType[0] == PositionRelation.PositionType.PositionEast && posType[1] == PositionRelation.PositionType.PositionWest ||
					  posType[0] == PositionRelation.PositionType.PositionGenEast && posType[1] == PositionRelation.PositionType.PositionGenWest ) {
				shortcutType = ShortcutType.verticalDivideEW;
			}
			else if ( posType[0] == PositionRelation.PositionType.PositionNorth && posType[1] == PositionRelation.PositionType.PositionSouth ||
					  posType[0] == PositionRelation.PositionType.PositionGenNorth && posType[1] == PositionRelation.PositionType.PositionGenSouth ) {
				shortcutType = ShortcutType.horizontalDivideNS;
			}
			else if ( posType[0] == PositionRelation.PositionType.PositionSouth && posType[1] == PositionRelation.PositionType.PositionNorth ||
					  posType[0] == PositionRelation.PositionType.PositionGenSouth && posType[1] == PositionRelation.PositionType.PositionGenNorth ) {
				shortcutType = ShortcutType.horizontalDivideSN;
			}
		}
	}
	
	public boolean exists() {
		return (shortcutType != ShortcutType.noShortcut);
	}
	
	/* Main work: divide a token set into two (or more, for future) parts b
	 * based on the type of the geometric shortcut.
	 */
	public int [][] getPartition(CAbstractWrittenTokenSet wts, int [] iHead) {
		if ( !exists() ) {
			throw new RuntimeException("Geometric shortcut does not exist");
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
			if ( shortcutType == ShortcutType.verticalDivideWE ) {
				idx = rnht.get(i).isCenterWestOf(headCenterX) ? 0 : 1;
			}
			else if ( shortcutType == ShortcutType.verticalDivideEW ) {
				idx = rnht.get(i).isCenterEastOf(headCenterX) ? 0 : 1;
			}
			else if ( shortcutType == ShortcutType.horizontalDivideNS ) {
				idx = rnht.get(i).isCenterNorthOf(headCenterX) ? 1 : 0;
			}
			else if ( shortcutType == ShortcutType.horizontalDivideSN ) {
				idx = rnht.get(i).isCenterSouthOf(headCenterY) ? 1 : 0;
			}
			else {
				throw new RuntimeException("Unrecognized shortcut type");
			}
			
			labels[0][i] = idx;
		}
		
		return labels;
	}
	
	
	@Override
	public String toString() {
		String s = "GeometricShortcut: ";
				
		if ( shortcutType == ShortcutType.noShortcut ) {
			s += "noShortcut";
		}
		else if ( shortcutType == ShortcutType.horizontalDivideNS ) {
			s += "horiztonalDivdeNS";
		}
		else if ( shortcutType == ShortcutType.horizontalDivideSN ) {
			s += "horizontalDivideSN";
		}
		else if ( shortcutType == ShortcutType.verticalDivideWE ) {
			s += "verticalDivideWE";
		}
		else if ( shortcutType == ShortcutType.verticalDivideEW ) {
			s += "verticalDivideEW";
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
	private final static String tokenRelSeparator = ":";
	private final static String relSeparator = ",";
	private final static String sumStringArrow = "-->";
	
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
	
	//int headNode;	/* Index to the "head node" */
	/* Leaving this out for now, since the first rhs will always be the 
	 * head node, at least in simple productions.
	 */
	
//	private TerminalSet terminalSet = null; /* TODO */

	/* Methods */
	/* Constructors */
	public GraphicalProduction(String t_lhs, 
			                   String [] t_rhs, 
			                   boolean [] t_bt, 
			                   TerminalSet termSet, 		/* For determine if the rhs are terminals */
			                   GeometricRelation [][] t_geomRels) {
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
		
		/* Generate geometric shortcut, if any. 
		 * If there is no shortcut, shortcutType will be noShortcut. */
		geomShortcut = new GeometricShortcut(this);
//		if ( geomShortcut.shortcutType != GeometricShortcut.ShortcutType.noShortcut ) { //DEBUG
//			int i = 0; //DEBUG
//			i = 1;
//		}
		
		/* Generate summary string */
		genSumString();
	}
	
	private void genSumString() {
		sumString = lhs + " " + sumStringArrow + " ";
		for (int i = 0; i < rhs.length; ++i) {
			sumString += rhs[i];
			if ( i < rhs.length - 1 )
				sumString += " ";
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
	public Node attempt(CAbstractWrittenTokenSet tokenSet, 
			            int [] iHead,
			            ArrayList<CAbstractWrittenTokenSet> remainingSets, 
			            float [] maxGeomScore) {		
		/* Configuration constants */
		final boolean bUseShortcut = true; /* TODO: Get rid of this constant when the method proves to be reliable */
		
//		final float verifyThresh = 0.50f; /* TODO: make less ad hoc */
		int nnht = tokenSet.nTokens() - iHead.length; /* Number of non-head tokens */
		int nrn = nrhs - 1; /* Number of remaining nodes to match */
		
		/* TODO: check that remainingSets is empty */
		if ( remainingSets.size() != 0 ) {
			System.err.println("WARNING: remainingSets input to attempt() is not empty");
			remainingSets.clear();
		}
		
		if ( nrn == 1 && rhs[rhs.length - 1].equals(TerminalSet.epsString) ) {
			/* Empty set matches EPS */
			
			Node n = new Node(sumString, rhs);
			
			if ( nnht == 0 )
				/* Add a terminal, empty-set (EPS) node */
				maxGeomScore[0] = 1.0f;
			else
				/* The only non-head node is an EPS, but there is still some token(s) left */				
				maxGeomScore[0] = 0.0f;
				
			return n;
		}
						
		if ( nrn > nnht ) {
			maxGeomScore[0] = 0.0f;
			Node n = new Node(sumString, rhs);		
			return n;
		}
		
		//DEBUG
		if ( this.lhs.equals("FRACTION") ) {
			int ii;
			ii = 0;
		}
		//~DEBUG

		int [][] labels = null;
		if ( geomShortcut.exists() && bUseShortcut ) {
			/* Use this smarter approach when a geometric shortcut exists */
			labels = geomShortcut.getPartition(tokenSet, iHead);
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
	    boolean bFound = false;
	    CWrittenTokenSetNoStroke [][] a_rems = new CWrittenTokenSetNoStroke[labels.length][];
	    float [] geomScores = new float[labels.length];
	    
	    for (int i = 0; i < labels.length; ++i) {
	    	a_rems[i] = new CWrittenTokenSetNoStroke[nrn];
	    	boolean [] remsFilled = new boolean[nrn];
	    	
	    	for (int j = 0; j < nrn; ++j)
	    		 /* TODO: Type safety check */
	    		a_rems[i][j] = new CWrittenTokenSetNoStroke();
    		
    		for (int k = 0; k < labels[i].length; ++k) {
    			int inode = labels[i][k];
    			int irt = inht.get(k);
    			
    			a_rems[i][inode].setTokenNames(tokenSet.getTokenNames());
    			a_rems[i][inode].addToken(tokenSet.getTokenBounds(irt), 
    					                  tokenSet.recogWinners.get(irt), 
    					                  tokenSet.recogPs.get(irt), 
    					                  false);		
    			/* The last input argument sets bCheck to false for speed */
    			/* Is this a dangerous action? */
    			
    			remsFilled[inode] = true;
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
		    		
		    		boolean bVerified = true;
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
		    			
		    			if ( i == 2 && j == 1 && k == 0 ) //DEBUG
		    				k = k; //DEBUG
		    			
		    			float v = geomRels[j + 1][k].verify(a_rems[i][j], bndsInRel);
		    			if ( v > 1.0 ) //DEBUG
		    				v = v; // DEBUG
		    			
		    			t_t_geomScores[k] = v;
		    			
		    		}
		    		
		    		t_geomScores[j] = MathHelper.mean(t_t_geomScores);
		    		
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
	    for (int i = 0; i < a_rems[idxMax].length; ++i)
	    	remainingSets.add(a_rems[idxMax][i]);
	    return new Node(sumString, rhs);
	}
	
	
	public static GraphicalProduction genFromStrings(ArrayList<String> strs, TerminalSet termSet)
		throws Exception {
		String t_lhs = strs.get(0);
		
		int t_nrhs = strs.size() - 1;
		String [] t_rhs = new String[t_nrhs];
		boolean [] t_bt = new boolean[t_nrhs];
		GeometricRelation [][] t_geomRels = new GeometricRelation[t_nrhs][];
		
		for (int k = 0; k < t_nrhs; ++k) {
			String line = strs.get(k + 1);
			
			if ( k == 0 ) {
				/* This is the head node, no geometrical relation is expected */				
				if ( line.contains(tokenRelSeparator) ) 
					throw new Exception("Head node unexpectedly contains geometric relation(s)");
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
					if ( line.trim().equals(TerminalSet.epsString) )
						t_rhs[k] = TerminalSet.epsString;
					else
						throw new Exception("Encountered a non-head node with no geometric relations specified");
				}
			}
			
			t_bt[k] = termSet.isTypeTerminal(t_rhs[k]);
			
			
		}
		
		return new GraphicalProduction(t_lhs, t_rhs, t_bt, termSet, t_geomRels);
	}
	
	public int getNumNonHeadTokens() {
		return nrhs - 1;
	}
	
	@Override
	public String toString() {
		String s = "GrraphicalProduction: ";
		if ( sumString != null )
			s += sumString;
		return s;
	}
	
	
	/* Error classes */
//	class GeometricRelationCreationException extends Exception {
//		public GeometricRelationCreationException() {}
//		
//		public GeometricRelationCreationException(String s) { super(s); }
//	};
}

