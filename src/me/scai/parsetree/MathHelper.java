package me.scai.parsetree;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.Arrays;

/* Helper class for sort */
class PairedValueIndex implements Comparable<PairedValueIndex> {
	public float x;
	int index;
	
	/* Methods */
	public PairedValueIndex(float t_x, int i) {
		x = t_x;
		index = i;
	}

	@Override
	public int compareTo(PairedValueIndex pvi0) {
		
		// TODO Auto-generated method stub
		if ( this.x > pvi0.x )
			return 1;
		else  if ( this.x < pvi0.x )
			return -1;
		else
			return 0;
			
	}
}

/* Class: MathHelper */
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
	
	public static float mean(float x, float y) {
		return (x + y) * 0.5f;
	}
	
	/* Geometric mean of an array of float */
	public static float geometricMean(float [] xs) {
		if ( xs == null || xs.length == 0 )
			throw new IllegalArgumentException("Input to mean() is an empty array");
		
		float prod = 1.0f;
		
		for (int i = 0; i < xs.length; ++i)
			prod *= xs[i];
		
		return (float) Math.pow((double) prod, 1.0 / xs.length);
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
			throw new IllegalArgumentException("Input to indexMax() is an empty array");
		
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
			/* TODO */
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
	
	/* Sorting an array of float in place and give the indices in the sorted array */
	public static void sort(float [] xs, int [] idxInSorted) {
		if ( xs.length != idxInSorted.length )
			throw new RuntimeException("Length of idxInSorted does not equal length of x");

		PairedValueIndex [] pvis = new PairedValueIndex[xs.length];
		
		for (int i = 0; i < xs.length; ++i)
			pvis[i] = new PairedValueIndex(xs[i], i);
		
		Arrays.sort(pvis);
		
		for (int i = 0; i < xs.length; ++i) {
			xs[i] = pvis[i].x;
			idxInSorted[i] = pvis[i].index;
		}
	}
	
	/* Convert an array of integers to string */
	public static String intArray2String(int [] xs) {
		String s = "";
		
		for (int i = 0; i < xs.length; ++i) {
			s += xs[i];
			
			if ( i < xs.length - 1 )
				s += ",";
		}
		
		return s;
	}
	
	
	/* Differentiation of a float array: 
	 * Emulating "diff" command in MATLAB */
	public static float [] diff(final float [] x) {
		if ( x == null )
			throw new RuntimeException("diff function encountered null input");
		if ( x.length == 0 )
			throw new RuntimeException("diff function encountered empty array");
		
		float [] dx = new float[x.length - 1];
		for (int i = 0; i < x.length - 1; ++i)
			dx[i] = x[i + 1] - x[i];
		
		return dx;
	}
	
	/* Cumulative sum of a float array:
	 * Emulating "cumsum" function in MATLAB
	 */
	 public static float [] cumsum(final float [] x, final boolean bInitialZero) {
		 if ( x == null )
			 throw new RuntimeException("diff function encountered null input");
		 if ( x.length == 0 )
			 throw new RuntimeException("diff function encountered empty array");
		 
		 float [] cx = null;
		 if ( !bInitialZero ) {
			 cx = new float[x.length];
			 
			 cx[0] = x[0];
			 for (int i = 1; i < x.length; ++i)
				 cx[i] = cx[i - 1] + x[i];
		 }
		 else {
			cx = new float[x.length + 1];
			
			cx[0] = 0f;
			cx[1] = x[0];
			for (int i = 1; i < x.length; ++i)
				cx[i + 1] = cx[i] + x[i];
		 }
		 
		 return cx;
	 }
	 
	 /* Sum of elements in a floar array:
	  * Emulating "sum" function in MATLAB
	  */
	 public static float sum(final float [] x) {
		 if ( x == null )
			 throw new RuntimeException("diff function encountered null input");

		 float s = 0f;
		 for (int i = 0; i < x.length; ++i)
			 s += x[i];
		 
		 return s;
	 }
}
