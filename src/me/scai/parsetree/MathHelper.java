package me.scai.parsetree;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

public class MathHelper {
	/* Arithmetic mean of an array of float */
	public static float mean(float [] xs) {
		if ( xs == null || xs.length == 0 )
			throw new IllegalArgumentException("Input to mean() is an empty array");
		
		float sum = 0.0f;
		
		for (int i = 0; i < xs.length; ++i)
			sum += xs[i];
		
		return sum / (float) xs.length;
	}
	
	public static float arrayMean(ArrayList<Float> xs) {
		float [] axs = new float[xs.size()];
		for (int i = 0; i < xs.size(); ++i)
			axs[i] = xs.get(i);
		
		return mean(axs);
	}
	
	/* Find the index to the largest element of an array of float */
	public static int indexMax(float [] xs) {
		if ( xs == null || xs.length == 0 ) 
			throw new IllegalArgumentException("Input to mean() is an empty array");
		
		float max = xs[0];
		int idxMax = 0;
		
		for (int i = 1; i < xs.length; ++i) {
			if ( xs[i] > max ) {
				max = xs[i];
				idxMax = i;
			}
		}
		
		return idxMax;
	}
	
	/* Get the index to the largest element in a 2D array of float, 
	 * i.e., array of array.
	 * Output: length-2 array of int. 1st element: 1st index.
	 */
	public static int [] indexMax2D(float [][] xs) {
		if ( xs == null || xs.length == 0 )
			throw new IllegalArgumentException("Input to mean() is an empty array");
		
		float [] max1 = new float[xs.length];
		int [] idxMax1 = new int[xs.length];
		
		for (int i = 0; i < xs.length; ++i) {
			idxMax1[i] = indexMax(xs[i]);
			max1[i] = xs[i][idxMax1[i]];
		}
		
		int idxMax2 = indexMax(max1);
		
		int [] r = new int[2];
		r[0] = idxMax2;
		r[1] = idxMax1[idxMax2];
		
		return r;
	}
	
	public static int [][] findTies2D(float [][] xs, float maxScore) {
		/* Search for ties */
		ArrayList<int []> idxTieMax = new ArrayList<int []>();
		for (int i = 0; i < xs.length; ++i) {
			for (int j = 0; j < xs[i].length; ++j) {
				if ( xs[i][j] == maxScore ) {
					int [] t_idx = new int[2];
					t_idx[0] = i;
					t_idx[1] = j;
					
					idxTieMax.add(t_idx);
				}
			}
		}
		
		if ( idxTieMax.size() > 1 ) {
			/* Found tie */
			int i = 0; /* TODO */
		}
		
		int [][] r = new int[idxTieMax.size()][];
		for (int i = 0; i < idxTieMax.size(); ++i)
			r[i] = idxTieMax.get(i);
		
		return r;
	}
	
	/* Input: dimSize: number of discrete values in each dimension; 
	 *        numDims: number of dimensions;
	 */
	public static int [][] getFullDiscreteSpace(int dimSize, int numDims) {		
	    int numRows = (int) Math.pow(dimSize, numDims);
	    int [][] labels = new int[numRows][numDims];
	    
	    for (int i = 0; i < numRows; ++i) {
	    	int n = i;
	    	
	    	for (int j = 0; j < numDims; ++j) {
	    		int denom = (int) Math.pow(dimSize, numDims - j - 1);
	    		labels[i][j] = n / denom;
	    		n -= (int) denom * labels[i][j];
	    	}
	    }
	    
	    return labels;
	}
	
	/* Get the bigger of two float numbers */
	public static float max(float x, float y) {
		return (x > y) ? x : y;
	}
	
	/* Get the smaller of two float numbers */
	public static float min(float x, float y) {
		return (x < y) ? x : y;
	}
}
