package me.scai.parsetree;

import java.util.List;

public class GeometryHelper {
	public static float pctOverlap(float [] oldBnds, float [] newBnds) {
		if ( oldBnds.length != 2 || newBnds.length != 2 )
			throw new IllegalArgumentException("Input bounds are not all of length 2");
		
		if ( oldBnds[1] < oldBnds[0] || newBnds[1] < newBnds[0] )
			throw new IllegalArgumentException("Input bounds are not all in the ascending order");
		
		float oldSize = oldBnds[1] - oldBnds[0];
		float newSize = newBnds[1] - newBnds[0];
		float meanSize = (oldSize + newSize) * 0.5f;
		
		/* Branch depending on relations */
		if ( newBnds[1] < oldBnds[0] || newBnds[0] > oldBnds[1] ) {
			return 0.0f;
		}
		else { /* Guaranteed overlap */
			if ( newBnds[1] < oldBnds[1] ) {
				if ( newBnds[0] >= oldBnds[0] ) {
					/* oldBnds:      [              ] */
					/* newBnds:         [         ]   */
					return 1.0f;
				}
				else {
					/* oldBnds:      [              ] */
					/* newBnds:   [               ]   */
					return (newBnds[1] - oldBnds[0]) / meanSize;
				}
				
			}
			else {
				if ( newBnds[0] <= oldBnds[0] ) {
					/* oldBnds:      		[     ]         */
					/* newBnds:         [               ]   */
					return 1.0f;
				}
				else {
					/* oldBnds:       [           ]         */
					/* newBnds:         [               ]   */
					return ( oldBnds[1] - newBnds[0] ) / meanSize;
				}
				
				
			}
		}
			
	}

	public static float pctMove(float [] lesserBnds, float [] greaterBnds) {
		if ( lesserBnds.length != 2 || greaterBnds.length != 2 )
			throw new IllegalArgumentException("Input bounds are not all of length 2");
		
		if ( lesserBnds[1] < lesserBnds[0] || greaterBnds[1] < greaterBnds[0] )
			throw new IllegalArgumentException("Input bounds are not all in the ascending order");
		
		float pm = (greaterBnds[0] - lesserBnds[0]) / (lesserBnds[1] - lesserBnds[0]);
//		if ( pm < 1.0f )
//			pm = 0.0f;
		
		if ( pm > 1.0f )
			pm = 1.0f;
		else if ( pm < 0.0f )
			pm = 0.0f;
		
		return pm;
	}
	
	/* Calculate the absolute relative difference, defined as abs(x - y) / ((x + y) / 2) */
	public static float absoluteRelativeDifference(float x, float y) {
		/* Require that x and y are both positive. If this is not the case, take their negatives */		
		if (x < 0F) {
			x = -x;
		}		
		if (y < 0F) {
			y = -y;
		}
		
		/* Edge case: both are exactly zero */
		if (x == 0F && y == 0F) {
			return 0F;
		}
		
		float out = 2.0F * (x - y) / (x + y);
		if (out < 0F) {
			out = -out;
		}
		
		return out;
	}
	
	private static float absoluteRelativeLeftRightOffset(int index, float [] bnds1, float [] bnds2) {
		if ( !(index == 0 || index == 2) ) {
			throw new IllegalArgumentException();
		}
		
		float width1 = bnds1[2] - bnds1[0];
		float width2 = bnds2[2] - bnds2[0];
		float meanWidth = (width1 + width2) * 0.5F;
		
		float out = (bnds1[index] - bnds2[index]) / meanWidth;
		if (out < 0) {
			out = -out;
		}
		
		return out;
	}
	
	private static float absoluteRelativeTopBottomOffset(int index, float [] bnds1, float [] bnds2) {
		if ( !(index == 1 || index == 3) ) {
			throw new IllegalArgumentException();
		}
		
		float height1 = bnds1[3] - bnds1[1];
		float height2 = bnds2[3] - bnds2[1];
		float meanHeight = (height1 + height2) * 0.5F;
		
		float out = (bnds1[index] - bnds2[index]) / meanHeight;
		if (out < 0) {
			out = -out;
		}
		
		return out;
	}
	
	
	
	/* Calculate the relative left x offset, defined as abs(left1 - left2) / ((width1 + width2) / 2) */
	public static float absoluteRelativeLeftXOffset(float [] bnds1, float [] bnds2) {
		return absoluteRelativeLeftRightOffset(0, bnds1, bnds2);
	}
	
	/* Calculate the relative left x offset, defined as abs(left1 - left2) / ((width1 + width2) / 2) */
	public static float absoluteRelativeRightXOffset(float [] bnds1, float [] bnds2) {
		return absoluteRelativeLeftRightOffset(2, bnds1, bnds2);
	}
	
	public static float absoluteRelativeTopYOffset(float [] bnds1, float [] bnds2) {
		return absoluteRelativeTopBottomOffset(1, bnds1, bnds2);
	}
	
	public static float absoluteRelativeBottomYOffset(float [] bnds1, float [] bnds2) {
		return absoluteRelativeTopBottomOffset(3, bnds1, bnds2);
	}
	
	/* From the right edge of bnds1 to the left edge of bnds2. 
	 * Return value: > 0 for rightward movement;
	 *               < 0 for leftward movement  */
	public static float relativeRightToLeftOffset(float [] bnds1, float [] bnds2) {
		float width1 = bnds1[2] - bnds1[0];
		float width2 = bnds2[2] - bnds2[0];
		float meanWidth = (width1 + width2) * 0.5F;
		
		float out = (bnds2[0] - bnds1[2]) / meanWidth;
	
		return out;
	}
	
	/* Calculate the number of tokens in between two tokens along the Y direction. 
	 * If there is any overlap between the two tokens in the Y dimension, then the 
	 * return value will be zero. If there is no overlap in the Y dimension, then
	 * the return value will be the number of tokens that
	 *   a) Has ctrX that falls in the joint X bounds of the two tokens and 
	 *   b) Has ctrY that falls in the joint inner Y bounds of the two tokens */
	public static int getNumTokensInBetween(String dimension, float [] bndsA, float [] bndsB, List<Float> wtCtrXs, List<Float> wtCtrYs) {
		/* TODO: Replace String with a more efficient type */
		boolean isX;
		int idx1, idx2, auxIdx1, auxIdx2;
		if (dimension.equals("X")) {
			isX = true;
			idx1 = 0;
			idx2 = 2;
			auxIdx1 = 1;
			auxIdx2 = 3;
		}
		else if (dimension.equals("Y")) {
			isX = false;
			idx1 = 1;
			idx2 = 3;
			auxIdx1 = 0;
			auxIdx2 = 2;
		}
		else {
			throw new IllegalArgumentException("dimension must be either \"X\" or \"Y\"");
		}
		
		if ( bndsA[idx1] < bndsB[idx1] && bndsB[idx1] < bndsA[idx2] || 
		     bndsB[idx1] < bndsA[idx1] && bndsA[idx1] < bndsB[idx2] ) {
			/* There is overlap in the x- or y-ranges of the two tokens:
			 * return value will be 0. */
			return 0;
		}
		
		float auxMin = MathHelper.min(bndsA[auxIdx1], bndsB[auxIdx1]);
		float auxMax = MathHelper.max(bndsA[auxIdx2], bndsB[auxIdx2]);
		
		float dimMin, dimMax;
		if ( bndsA[idx1] > bndsB[idx2] ) {
			/* A is below B */
			dimMin = bndsB[idx2]; /* yMin: Upper bound */
			dimMax = bndsA[idx1]; /* yMax: Lower bound */
		}
		else {
			/* A is above B */
			dimMin = bndsB[idx2];
			dimMax = bndsA[idx1];
		}
		
		if (dimMin == dimMax) {
			/* Just touching edge: return 0 */
			return 0;
		}

		if (dimMin > dimMax) {	/* Make sure that dimMin <= dimMax is always satisfied */
			float dimTmp = dimMax;
			dimMax = dimMin;
			dimMin = dimTmp;
		}
		
		int nBetween = 0;
		for (int i = 0; i < wtCtrXs.size(); ++i) {
			float ctrAuxVal, ctrDimVal;
			if (isX) {
				ctrAuxVal = wtCtrYs.get(i);
				ctrDimVal = wtCtrXs.get(i);
			}
			else {
				ctrAuxVal = wtCtrXs.get(i);
				ctrDimVal = wtCtrYs.get(i);
			}
			
			if (ctrAuxVal > auxMin && ctrAuxVal < auxMax && ctrDimVal > dimMin && ctrDimVal < dimMax) {
				nBetween++;
			}
		}
		
		return nBetween;
		
	}
}
