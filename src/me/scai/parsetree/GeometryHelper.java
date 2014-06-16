package me.scai.parsetree;

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
}
