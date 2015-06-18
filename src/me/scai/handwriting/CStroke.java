package me.scai.handwriting;

import java.util.LinkedList;

/* CStroke: class for supporting a single, continuous stroke */
public class CStroke {
	/* Member variables */
	private LinkedList<Float> xs = new LinkedList<Float>();
	private LinkedList<Float> ys = new LinkedList<Float>();
	private boolean bNormalized = false;
	
	public float min_x = Float.MAX_VALUE, max_x = Float.MIN_VALUE; /* Record bounds */
	public float min_y = Float.MAX_VALUE, max_y = Float.MIN_VALUE; /* Record bounds */
	/* ~Member variables */

	/* Constructor: no initial point: empty initially */
	public CStroke() {
		
	}
	
	/* Constructor: supply the initial x and y coordinates */
	public CStroke(float x, float y) {
		addPoint(x, y);
	}
	
	/* Copy constructor */
	public CStroke(CStroke s0) {
		xs = new LinkedList<Float>();
		ys = new LinkedList<Float>();
		
		for (int i = 0; i < s0.xs.size(); ++i) {
			xs.add(s0.xs.get(i));
			ys.add(s0.ys.get(i));
		}
		
		bNormalized = s0.bNormalized;
		
		min_x = s0.min_x;
		max_x = s0.max_x;
		min_y = s0.min_y;
		max_y = s0.max_y;
	}
	
	/* Add a single point */
	public void addPoint(Float x, Float y) {
		xs.add(x);
		ys.add(y);
		
		if ( x < min_x ) min_x = x;
		if ( x > max_x ) max_x = x;
		if ( y < min_y ) min_y = y;
		if ( y > max_y ) max_y = y;
	}
	
	/* Get the number of points */
	public int nPoints() {
		return xs.size();
	}
	
	/* Normalize axes */
	public void normalizeAxes(float min_x, float max_x, 
			                  float min_y, float max_y) {
		float rng_x = max_x - min_x;
		float rng_y = max_y - min_y;
		for (int i = 0; i < xs.size(); ++i) {
			if (rng_x == 0.0f) {
				xs.set(i, 0.0f); /* Edge case: Zero width */
			}
			else {
				xs.set(i, (xs.get(i) - min_x) / rng_x);
			}
			
			if (rng_y == 0.0f) {
				ys.set(i, 0.0f); /* Edge case: Zero height */
			}
			else {
				ys.set(i, (ys.get(i) - min_y) / rng_y);
			}
		}
		
		bNormalized = true;
	}
	
	/* Fill imageMap: do this only after calling normalizeAxes() 
	 * Input arguments: im - 1D array, size must be equal to h * w
	 * 					h - height 
	 * 					w - width 
	 * 	Storage in im: x varies the fastest 
	 *  x increase: left to right;
	 *  y increase: top to bottom */
	public void fillImageMap(int [] im, int w, int h) {
		if ( im.length != h * w )
			System.err.println("UNEXPECTED_INT_ARRAY_SIZE");
	
		if ( !bNormalized )
			System.err.println("CSTROKE_UNNORMALIZED: CStroke object has not been normalized when fillImageMap is called!");
		
		int prev_ix = 0;
		int prev_iy = 0;
		for (int i = 0; i < xs.size(); ++i) {
			int ix = (int)(xs.get(i) * w);
			if ( ix == w )
				ix--;
			
			int iy = (int)(ys.get(i) * h);
			if ( iy == h )
				iy--;
			
			if ( i == 0 ) { /* First step */
				int ind = iy * w + ix;
				im[ind] = 1;				
			} 
			else {
				int x_step = ix - prev_ix;
				int y_step = iy - prev_iy;
				
				if ( x_step == 0 && y_step == 0 )
					continue;
				
				int abs_x_step = (x_step > 0) ? x_step : -x_step;
				int abs_y_step = (y_step > 0) ? y_step : -y_step;
				
				if ( abs_x_step > abs_y_step ) {
					/* x is the driver */
					int inc = (x_step > 0) ? 1 : -1;
					int x = prev_ix + inc;					
					while ( true ) {
						double f = ( (double) x - (double) prev_ix ) / (double) x_step;
						int y = (int) ((double) prev_iy + f * y_step);
						if ( x == w ) x--;
						if ( y == h ) y--;
						
						int ind = y * w + x;
						im[ind] = 1;
						
						if ( x == ix )
							break;
						
						x += inc;
					}
					
				}
				else {
					/* x is the driver */
					int inc = (y_step > 0) ? 1 : -1;
					int y = prev_iy + inc;
					while ( true ) {
						double f = ( (double) y - (double) prev_iy ) / (double) y_step;
						int x = (int) ((double) prev_ix + f * x_step);
						if ( x == w ) x--;
						if ( y == h ) y--;
						
						int ind = y * w + x;
						im[ind] = 1;
						
						if ( y == iy )
							break;
						
						y += inc;
					}
					
				}
			
			}
			
			prev_ix = ix;
			prev_iy = iy;
		}
	}
	
	/* Get a float array of the xs and ys coordinates */
	public float[] getXs() {
		float [] oxs = new float[xs.size()];		
		
		for (int i = 0; i < xs.size(); ++i) {
			oxs[i] = xs.get(i);
		}
		
		return oxs;
	}
	
	public float[] getYs() {
		float [] oys = new float[ys.size()];
		
		for (int i = 0; i < ys.size(); ++i) {
			oys[i] = ys.get(i);
		}
		
		return oys;
	}
	
	@Override
	public String toString() {
		String s = new String();
		
		s += "Stroke (np=" + xs.size() + "):\n\txs=[";
		for (int i = 0; i < xs.size(); ++i) {
			s += String.format("%.3f", xs.get(i));
			if ( i != xs.size() - 1 )
				s += ", ";
			else
				s += "]"; 	
		}
		
		s += "\n\tys=[";
		for (int i = 0; i < ys.size(); ++i) {
			s += String.format("%.3f", ys.get(i));
			if ( i != ys.size() - 1 )
				s += ", ";
			else
				s += "]";
		}
		
		return s;
	}
	
	public boolean isNormalized() {
		return bNormalized;
	}
}