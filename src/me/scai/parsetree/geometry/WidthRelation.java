package me.scai.parsetree.geometry;

import me.scai.handwriting.CAbstractWrittenTokenSet;

/* WidthRelation */
public class WidthRelation extends GeometricRelation {
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