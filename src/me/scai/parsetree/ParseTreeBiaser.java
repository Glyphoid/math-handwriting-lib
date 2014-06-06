package me.scai.parsetree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;

public class ParseTreeBiaser {
	/* Member variables */
	HashMap<String, GraphicalProduction.BiasType> sumString2BiasMap
		= new HashMap<String, GraphicalProduction.BiasType>();
	
	/* ~Member variables */
	
	/* Methods */
	/* Constructor */
	public ParseTreeBiaser(GraphicalProductionSet gpSet) {
		for (int i = 0; i < gpSet.prods.size(); ++i) {
			GraphicalProduction prod = gpSet.prods.get(i);
			
			sumString2BiasMap.put(prod.sumString, prod.biasType);
		}
	}
	
	public <T> void swapStackItems(LinkedList<T> stack, int i0, int i1) {
		ArrayList<T> items = new ArrayList<T>();
		items.ensureCapacity(i1 - i0 + 1);
		for (int k = 0; k < i1 - i0 + 1; ++k) {
			items.add(stack.get(i0)); 
			stack.remove(i0);
		}
		
		T top = stack.pop();
		
		for (int k = items.size() - 1; k >= 0; --k)
			stack.push(items.get(k));
		
		stack.add(i1, top);
		
//		nStack.remove(k - 1);
//		nStack.remove(k - 2);
//		nStack.remove(k - 3);
		
//		nStack.push(A);
//		nStack.push(ACh1);
//		nStack.push(ACh0);
		
	}
	
	public void process(Node n) {
		if ( n == null ) {
//			throw new RuntimeException("Received null Node as input");
			return;
		}
			
		
		if ( n.isTerminal() )
			return;
		
		LinkedList<Node> nStack = new LinkedList<Node>();
		LinkedList<Boolean> rStack = new LinkedList<Boolean>();	/* "r" stands for ready (for popping) */
		LinkedList<Integer> lvStack = new LinkedList<Integer>();
		
		nStack.push(n);
		rStack.push(n.isTerminal());
		lvStack.push(0);
		
		while ( nStack.size() != 0 ) {
//			System.out.println("nStack.size() = " + nStack.size() + 
//					           "; rStack.size() = " + rStack.size() + 
//					           "; lvStack.size() = " + lvStack.size());	//DEBUG
			
			Node nTop = nStack.getFirst();	
			boolean rTop = rStack.getFirst();
			int lv = lvStack.getFirst();
			
			GraphicalProduction.BiasType bias = sumString2BiasMap.get(nTop.prodSumString);
			if ( bias != null && bias != GraphicalProduction.BiasType.NoBias 
				 && nStack.size() > 1 && rTop ) {	/* TODO: Check to make sure that bias cannot be null */
				/* Look for the actual matching parent. 
				 * Note that it may not be the immediate parent */
				String pString = nTop.prodSumString;
				
				if ( bias != GraphicalProduction.BiasType.BiasLeft )
					throw new RuntimeException("Unsupported bias type");
				int nInterExpected = nTop.rhsTypes.length - 1;
				
				boolean bMatchFound = false;
				Node passageBegin = null;
				Node passageEnd = null;
				int nInter = 0;
				int endLv = lv;
				int k;
//				String thisRHS = nTop.prodSumString.split(GraphicalProduction.sumStringArrow)[0];
				String thisRHS = nTop.lhs;
				for (k = 1; k < nStack.size(); ++k) {
					if ( nStack.get(k).prodSumString.equals(pString) ) {
						bMatchFound = true;
						break;
					}
					else if ( nStack.get(k).nc == 1 && !nStack.get(k).isTerminal() 
							  && nStack.get(k).rhsTypes[0].equals(thisRHS) ) {
						/* This is a passage node */
						if ( passageBegin == null )
							passageBegin = nStack.get(k);
						
						passageEnd = nStack.get(k);
						endLv = lvStack.get(k);
//						thisRHS = nStack.get(k).prodSumString.split(GraphicalProduction.sumStringArrow)[0];
						thisRHS = nStack.get(k).lhs;
						
					}
					else if ( endLv == lvStack.get(k) ) {
						nInter++;
					}
					else if ( !nStack.get(k).isTerminal() ) {
						break;
					}
				}
				
				if ( bMatchFound && nInter == nInterExpected ) {
//					System.out.println("Match found for bias type"); //DEBUG
					
					Node A = nStack.get(k);
					Node B = nStack.get(0);
//					String A_LHS = A.prodSumString.split(GraphicalProduction.sumStringArrow)[0];
					String A_LHS = A.lhs;
					
					/* Find the parent of A */
					int ALevel = lvStack.get(k);
					int j = k + 1;
					Node AParent = null;
					while ( j < nStack.size() && ALevel == lvStack.get(j) )
						ALevel = lvStack.get(j++);					
					
					AParent = nStack.get(j);
					boolean bFoundACh = false;
					int AParentChIdx = 0;
					while ( AParentChIdx < AParent.nc ) {
						if ( AParent.rhsTypes[AParentChIdx].equals(A_LHS) ) {
							bFoundACh = true;
							break;
						}
					}
					
					if ( !bFoundACh )
						throw new RuntimeException("");
				
//					passageBegin.ch[0] = 
					
					Node tmpNode = B.ch[1];
					passageBegin.ch[0] = A;
					B.ch[1] = passageEnd;
					A.ch[A.nc - 1] = tmpNode;
					
					/* Let the parent of A point to B */
					AParent.ch[AParentChIdx] = B; 
					
					/* Swap items in nStack */
					swapStackItems(nStack, k - 2, k);
					swapStackItems(rStack, k - 2, k);	/* TODO: Set the top item in rStack to true (?) */
					
					/* Recalculate the values in lvStack */
					for (int m = lvStack.size() - 1; m >= 0; --m) {
						if ( m == lvStack.size() - 1 ) {
							lvStack.set(m, 0);
						}
						else {
							if ( nStack.get(m + 1).isTerminal() ) {
								lvStack.set(m, lvStack.get(m + 1));
							}
							else {
								String [] rhsTypes = nStack.get(m + 1).rhsTypes;
								
//								String thisNodeType = nStack.get(m).prodSumString.split(GraphicalProduction.sumStringArrow)[0];
								String thisNodeType = nStack.get(m).lhs;
								
								boolean bMatch = false;
								for (int p = 0; p < rhsTypes.length; ++p) {
									if ( rhsTypes[p].equals(thisNodeType) ) {
										bMatch = true; 
										break;
									}
								}
								
								if ( bMatch )
									lvStack.set(m, lvStack.get(m + 1) + 1);
								else
									lvStack.set(m, lvStack.get(m + 1));
							}		
						}
					}
					
				}
			

			}
				
			
			if ( rTop ) {	/* Action: Pop */
//				System.out.println("Popping: " + nTop.toString()); //DEBUG

				nStack.pop();
				rStack.pop();
				lvStack.pop();
				
				if ( rStack.size() > 0 
				     && !rStack.getFirst() 
				     && lv == lvStack.getFirst() + 1 ) 
					rStack.set(0,  true);
			}
			else { /* Action: Push */
				for (int i = 0; i < nTop.nc; ++i) {
					nStack.push(nTop.ch[i]);
					rStack.push(nTop.ch[i].isTerminal());
					lvStack.push(lv + 1);
				}
			}
		}
	}
	/* ~Methods */
}
