package me.scai.parsetree.geometry;

import me.scai.handwriting.CAbstractWrittenTokenSet;
import me.scai.parsetree.TerminalSet;

/* WidthRelation */
public class SpacingRelation extends GeometricRelation {
	public enum SpacingRelationType {
		SpacingHorizontalExceedsMeanMaxMajorTokenWidth,
		SpacingHorizontalBelowMeanMaxMajorTokenWidth
	}
	
	/* Member variables */
	private NodeInternalGeometry nodeInternalGeom;
	SpacingRelationType spacingRelationType;
	
	/* Constructor */
	/* Default constructor */
	private SpacingRelation() {}
	
	public SpacingRelation(SpacingRelationType srt, 
			               int t_idxTested, 
			               int t_idxInRel, 
			               TerminalSet termSet) {
		spacingRelationType = srt;
		
		idxTested = new int[1];
		idxTested[0] = t_idxTested;
		
		idxInRel = new int[1];
		idxInRel[0] = t_idxInRel;
		
		nodeInternalGeom = new NodeInternalGeometry(termSet);
	}

	private float getMaxMajorTokenWidth(CAbstractWrittenTokenSet wts) {
		float maxWidth = 0.0f;
		for (int i = 0; i < wts.getNumTokens(); ++i) {						
			String tokenTermType = wts.getTokenTermType(i);
			
			if (nodeInternalGeom.isTerminalTypeMajor(tokenTermType)) {
				float [] bnds = wts.getTokenBounds(i);
				float w = bnds[2] - bnds[0];
				
				if (w > maxWidth) {
					maxWidth = w; 
				}
			}
		}
		
		return maxWidth;
	}
	
	
	@Override
	public float verify(CAbstractWrittenTokenSet wtsTested,  float [] bndsInRel) {
		float v = 0.0f;
		
		float [] bndsTested = wtsTested.getSetBounds();
		if ( bndsInRel.length != 4 ) {
			throw new IllegalArgumentException("tiTested does not have length 1");
		}
		
		float [] xBoundsTested = new float[2];	/* x-bounds of tested token */
		float [] xBoundsInRel = new float[2];   /* x-bounds of in-relation-to token */		
		xBoundsTested[0] = bndsTested[0];
		xBoundsTested[1] = bndsTested[2];
		xBoundsInRel[0] = bndsInRel[0];
		xBoundsInRel[1] = bndsInRel[2];
		
		float maxWidth = getMaxMajorTokenWidth(wtsTested);
		float meanMaxWidth = maxWidth; /* TODO: Count the in-relation-to token set */
		
		float overlap = GeometryHelper.pctOverlap(xBoundsTested, xBoundsInRel);
		if ( spacingRelationType == SpacingRelationType.SpacingHorizontalExceedsMeanMaxMajorTokenWidth ) {
			if (overlap > 0.0f) {
				v = 0.0f;
			}
			else {
				if (meanMaxWidth <= 0.0f) {
					v = 0.0f;
				}
				else {
					float horizontalSpacing = (xBoundsTested[0] > xBoundsInRel[1]) ? 
						                  	  (xBoundsTested[0] - xBoundsInRel[1]) :
						                  	  (xBoundsInRel[0] - xBoundsTested[1]);
                  	float spacingToWidthRatio = horizontalSpacing / meanMaxWidth;
                  	v = 2.5f * (spacingToWidthRatio - 1.0f); /* TODO: Hardcoding warning */
				}
			}
		}
		else if ( spacingRelationType == SpacingRelationType.SpacingHorizontalBelowMeanMaxMajorTokenWidth ) {
			if (overlap > 0.0f) {
				v = 1.0f;
			}
			else {
				if (meanMaxWidth <= 0.0f) {
					v = 0.0f;
				}
				else {
					float horizontalSpacing = (xBoundsTested[0] > xBoundsInRel[1]) ? 
						                  	  (xBoundsTested[0] - xBoundsInRel[1]) :
						                  	  (xBoundsInRel[0] - xBoundsTested[1]);
                  	float spacingToWidthRatio = horizontalSpacing / meanMaxWidth;
                  	v = 1.0f - spacingToWidthRatio * 0.5f; /* TODO: Hardcoding warning */
				}
			}
		}
		
//		return v;
//		float v = 0.0f;
//		if ( spacingRelationType == SpacingRelationType.HorizontalSpacingExceedsMeanMaxMajorTokenWidth ) {
//			v = 1.0f - Math.abs(wTested - wInRel) / wMean;
//			if ( v > 0.75f ) /* Slack */
//				v = 1.0f;
//		}
//		
		if ( v > 1.0f ) {
			v = 1.0f;
		}
		else if ( v < 0.0f ) {
			v = 0.0f;
		}
		
		return v;
	}
	
	@Override
	public void parseString(String str, int t_idxTested) {
		String [] items = splitInputString(str);
		
		spacingRelationType = SpacingRelationType.valueOf(items[0]);
		
		idxTested = new int[1];
		idxTested[0] = t_idxTested;
		
		if  ( items[1] != null ) {
			idxInRel = new int[1];
			idxInRel[0] = Integer.parseInt(items[1]);
		}
		
	}
	
	/* Factory method */
	public static SpacingRelation createFromString(String str, int t_idxTested, TerminalSet termSet) {
		SpacingRelation r = new SpacingRelation();
		
		r.parseString(str, t_idxTested);
		r.nodeInternalGeom = new NodeInternalGeometry(termSet);
		
		return r;
	}
}
