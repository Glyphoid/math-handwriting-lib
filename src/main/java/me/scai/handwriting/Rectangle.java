package me.scai.handwriting;

import me.scai.parsetree.MathHelper;

public class Rectangle {
	private float xMin, yMin, xMax, yMax;
	
	/* Methods */
	
	/* Constructors */
	public Rectangle(float [] bnds) {
		xMin = bnds[0];
		yMin = bnds[1];
		xMax = bnds[2];
		yMax = bnds[3];
	}
	
	public Rectangle(float t_xMin, float t_yMin, float t_xMax, float t_yMax) {
		xMin = t_xMin;
		yMin = t_yMin;
		xMax = t_xMax;
		yMax = t_yMax;
	}
	
	public Rectangle(CWrittenToken wt) {
		float [] bnds = wt.getBounds();
		
		xMin = bnds[0];
		yMin = bnds[1];
		xMax = bnds[2];
		yMax = bnds[3];
	}
	
	public Rectangle(CAbstractWrittenTokenSet wts) {
		float [] bnds = wts.getSetBounds();
		
		xMin = bnds[0];
		yMin = bnds[1];
		xMax = bnds[2];
		yMax = bnds[3];
	}	
	
	public Rectangle(CAbstractWrittenTokenSet wts, int [] inds) {
		xMin = Float.POSITIVE_INFINITY;
		yMin = Float.POSITIVE_INFINITY;
		xMax = Float.NEGATIVE_INFINITY;
		yMax = Float.NEGATIVE_INFINITY;
		
		if ( inds.length == 0 ) {
			throw new RuntimeException("Index to tokens is empty.");
		}
		
		for (int i = 0; i < inds.length; ++i) {
			float [] bnds = wts.getTokenBounds(inds[i]);
			
			if (xMin > bnds[0])
				xMin = bnds[0];
			if (yMin > bnds[1])
				yMin = bnds[1];
			if (xMax < bnds[2])
				xMax = bnds[2];
			if (yMax < bnds[3])
				yMax = bnds[3];

		}
	}
	
	public float getCentralX() {
		return (xMin + xMax) * 0.5f;
	}
	
	public float getCentralY() {
		return (yMin + yMax) * 0.5f;	
	}
	
	public void mergeWith(Rectangle rct) {
		float new_xMin, new_yMin, new_xMax, new_yMax;
		
		new_xMin = MathHelper.min(xMin, rct.xMin);
		new_yMin = MathHelper.min(yMin, rct.yMin);
		new_xMax = MathHelper.max(xMax, rct.xMax);
		new_yMax = MathHelper.max(yMax, rct.yMax);
		
		xMin = new_xMin;
		yMin = new_yMin;
		xMax = new_xMax;
		yMax = new_yMax;
	}
		
	
	/* Geometric relations */
	public boolean isCenterWestOf(float x) {
		return ((xMin + xMax) * 0.5f) < x; 
	}
	
	public boolean isCenterEastOf(float x) {
		return ((xMin + xMax) * 0.5f) > x; 
	}
	
	public boolean isCenterNorthOf(float y) {
		return ((yMin + yMax) * 0.5f) < y; 
	}
	
	public boolean isCenterSouthOf(float y) {
		return ((yMin + yMax) * 0.5f) > y; 
	}
}
