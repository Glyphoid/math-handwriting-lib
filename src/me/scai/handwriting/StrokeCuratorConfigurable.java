package me.scai.handwriting;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.net.URL;

import org.apache.commons.lang.ArrayUtils;

import me.scai.handwriting.StrokeCuratorConfig;
import me.scai.parsetree.GeometryHelper;
import me.scai.parsetree.MathHelper;
import me.scai.parsetree.TerminalSet;
import me.scai.parsetree.TextHelper;

public class StrokeCuratorConfigurable implements StrokeCurator {
	/* Enum types */
//	private enum StrokeStat {
//		Unprocessed,
//		Incorporated, 
//	};
	
	/* Member variables */
	/* Parameters */
	private final String configFileCommentString = "#";
	
//	private List<String> noMergeTokens = new ArrayList<String>();
	private StrokeCuratorConfig config;
	
	private float overlapCoeffLB = 0.25f;	/* Being smaller than this upper bound results in breaking from loop */
	private float overlapCoeffUB = 0.75f;	/* Being greater than this upper bound guarantees automatic merging */	
	/* TODO: Make less ad hoc */
	/* ~Parameters */
	
	private int nClosestToTest = 2;	/* TODO: Remove arbitrariness */
	
	private TokenRecogEngine tokenEngine = null;
	private CWrittenTokenSet wtSet = new CWrittenTokenSet();
	private List<CStroke> strokes = new LinkedList<CStroke>();
	private List<CStroke> strokesUN = new LinkedList<CStroke>(); 	/* Unnormalized */ 
	
	private List<Integer> strokeStats = new LinkedList<Integer>();  /* Stroke status */
	
	TerminalSet termSet;
	/* -1: unincorporated (unprocessed); >= 0: index to the containing token */
	
//	private List<Float> ctrXs = new LinkedList<Float>();	/* Central X coordinate of the strokes */
//	private List<Float> ctrYs = new LinkedList<Float>();	/* Central Y coordinate of the strokes */
	private List<Float> wtCtrXs = new LinkedList<Float>(); 	/* Central X coordinate of the written tokens */
	private List<Float> wtCtrYs = new LinkedList<Float>(); 	/* Central X coordinate of the written tokens */
	
	private List<String> wtRecogWinners = new LinkedList<String>();
	private List<double []> wtRecogPs = new LinkedList<double []>();
	private List<Double> wtRecogMaxPs = new LinkedList<Double>();
	
	private List<int []> wtConstStrokeIdx = new LinkedList<int []>();	/* Indices to constituents stroke indices */ 
	
	private float mergePValueRatioThresh = 0.5F;
	/* ~Member variables */
	
	/* Methods */
	/* Constructor: 
	 * 	Input arguments: configFN: Configuration file
	 *   */
	public StrokeCuratorConfigurable(URL configFN, TokenRecogEngine tokEngine) {
		tokenEngine = tokEngine;
		
		config = StrokeCuratorConfig.fromJsonFileAtUrl(configFN);
	}
	
	/* Constructor */
//	public StrokeCuratorConfigurable(String [] tokNames, TokenRecogEngine tokEngine) {
//		if ( tokNames == null )
//			throw new RuntimeException("Input tokNames is null");
//			
//		if ( tokEngine == null )
//			throw new RuntimeException("Input tokEngine is null");
//		
////		initialize(tokEngine);
//	}
	
//	public void initialize(//String [] tokNames, 
//						   TokenRecogEngine tokEngine) {
////		tokenNames = tokNames;
//		tokenEngine = tokEngine;
//	}
	
	/* Generate a new CWrittenToken from an existing token in the token set and an 
	 * unincorporated stroke. 
	 * Input arguments: 
	 *     oldWrittenTokenIdx : Index to the existing token in the token set
	 *     strokeUNIdx        : Index to the unincorporated stroke 
	 */
	private CWrittenToken generateMergedToken(int oldWrittenTokenIdx, int strokeUNIdx) {
		CWrittenToken tmpWT = new CWrittenToken();
		
		/* Add the old strokes */
		int [] constIdx = wtConstStrokeIdx.get(oldWrittenTokenIdx);
		for (int j = 0; j < constIdx.length; ++j) {
			tmpWT.addStroke(new CStroke(strokesUN.get(constIdx[j])));
		}
		/* TODO: Look into the memory and performance issues caused by this repeated copying */
		
		/* Add the new stroke */
		tmpWT.addStroke(new CStroke(strokesUN.get(strokeUNIdx)));
		tmpWT.normalizeAxes();
		
		return tmpWT;
	}
	
	private boolean mergeTokenWithStroke(boolean toCompareMaxPs, int oldWrittenTokenIdx, int strokeUNIdx) {
		boolean merged = false;
		
		int [] constIdx = wtConstStrokeIdx.get(oldWrittenTokenIdx);
		
		CWrittenToken tmpWT = generateMergedToken(oldWrittenTokenIdx, strokesUN.size() - 1);
		
		double [] newPs = new double[tokenEngine.tokenNames.size()];
		int newRecRes = tokenEngine.recognize(tmpWT, newPs);
		
		String newWinnerTokenName = tokenEngine.getTokenName(newRecRes); 
		double newMaxP = newPs[newRecRes];
		
		if ( (!toCompareMaxPs) || (newMaxP > wtRecogMaxPs.get(oldWrittenTokenIdx) * mergePValueRatioThresh ) ) {
			/* Replace the old token set with a new one that includes the new stroke */
			wtRecogWinners.set(oldWrittenTokenIdx, newWinnerTokenName);
			wtRecogPs.set(oldWrittenTokenIdx, newPs);
			wtRecogMaxPs.set(oldWrittenTokenIdx, newMaxP);
			
			int [] newConstIdx = new int[constIdx.length + 1];
			java.lang.System.arraycopy(constIdx, 0, newConstIdx, 0, constIdx.length);
			newConstIdx[constIdx.length] = strokesUN.size() - 1;
			wtConstStrokeIdx.set(oldWrittenTokenIdx, newConstIdx);
			
			strokeStats.set(strokeStats.size() - 1, oldWrittenTokenIdx);
			
			wtSet.deleteToken(oldWrittenTokenIdx);
			tmpWT.setRecogWinner(newWinnerTokenName);
			tmpWT.setRecogPs(newPs);
			
			wtSet.addToken(oldWrittenTokenIdx, tmpWT, newWinnerTokenName, newPs);
			/* TODO: wtSet.addToken(wtIdx, wt, winnerTokenName, ps) */
			
			/* Central X and Y coordinates of the token */
			wtCtrXs.set(oldWrittenTokenIdx, tmpWT.getCentralX());
			wtCtrYs.set(oldWrittenTokenIdx, tmpWT.getCentralY());
			
			merged = true;
		}
		
		return merged;
	}
	
	@Override
	public void addStroke(CStroke s) {
		strokes.add(s);
		strokesUN.add(new CStroke(s)); 	/* Uses copy constructor */
		strokeStats.add(-1);
		
		float ctr_x = MathHelper.mean(s.min_x, s.max_x);
		float ctr_y = MathHelper.mean(s.min_y, s.max_y);
		
//		ctrXs.add(ctr_x);
//		ctrYs.add(ctr_y);
		
		/* Get recognition result from the single stroke first */
		String strokeRecogWinner = recognizeSingleStroke(s);
		
		/* Detect the closest set of existing strokes */
		if ( strokes.size() == 1 ) {
			addNewWrittenTokenFromStroke(s);
		}
		else {
			/* More than one strokes exist */
			boolean merged = false;			
			
			/* Inclusion coefficients */
			float [] negOverlapCoeffs = new float[wtSet.nTokens()];
			int recomMergeIdx = -1;			/* TODO: Remove this var? */
			String recomMergeToken = null;	/* TODO: Remove this var? */
			for (int i = 0; i < wtSet.nTokens(); ++i) {
				String tokenRecogWinner = wtSet.recogWinners.get(i);
				
				if ( !config.potentiallyMergeable(strokeRecogWinner, tokenRecogWinner) ) {					
					continue;
				}
				
				/* 2. ... TODO */
				float [] t_bounds = wtSet.getTokenBounds(i);
				float [] t_xBounds = new float[2];
				t_xBounds[0] = t_bounds[0];
				t_xBounds[1] = t_bounds[2];
				float [] t_yBounds = new float[2];
				t_yBounds[0] = t_bounds[1];
				t_yBounds[1] = t_bounds[3];
				
				float [] s_xBounds = new float[2];
				s_xBounds[0] = s.min_x;
				s_xBounds[1] = s.max_x;
				float [] s_yBounds = new float[2];
				s_yBounds[0] = s.min_y;
				s_yBounds[1] = s.max_y;
				float [] s_bounds = new float[4];
				s_bounds[0] = s.min_x;
				s_bounds[1] = s.min_y;
				s_bounds[2] = s.max_x;
				s_bounds[3] = s.max_y;
				
				/* Apply rules in StrokeCuratorConfig */
				if (recomMergeIdx == -1) {
					recomMergeToken = config.applyRule(strokeRecogWinner, tokenRecogWinner, 												       
												       s_bounds, t_bounds, 
												       wtCtrXs, wtCtrYs);	/* Assume, the first argument is the new stroke. TODO: Make this assumption a necessity. */
					if (recomMergeToken != null) {
						recomMergeIdx = i;
						
						mergeTokenWithStroke(false, i, strokesUN.size() - 1); /* Obey the rule and perform the merging */
						merged = true;
					}
				}
				
				/* Calculate the negative overlap coefficient */
				negOverlapCoeffs[i] = -1f * GeometryHelper.pctOverlap(t_xBounds, s_xBounds) * 
						                    GeometryHelper.pctOverlap(t_yBounds, s_yBounds);
			}
			
			int [] idxInSorted = new int[negOverlapCoeffs.length];			
			MathHelper.sort(negOverlapCoeffs, idxInSorted);
			
			if (!merged) {
				for (int n = 0; n < idxInSorted.length; ++n) {
					if ( -negOverlapCoeffs[n] < overlapCoeffLB ) {
						break;
					}
					
					int wtIdx = idxInSorted[n];
					String tokenRecogWinner = wtSet.recogWinners.get(wtIdx);
					
					if ( !config.potentiallyMergeable(strokeRecogWinner, tokenRecogWinner) ) {
						continue;
					}
					
					if ( mergeTokenWithStroke(true, wtIdx, strokesUN.size() - 1) ) {
						merged = true;
						break;
					}
					
				}
			}
			
			if ( !merged ) { /* The new stroke will become a new token */
				addNewWrittenTokenFromStroke(strokesUN.get(strokesUN.size() - 1));
			}
			
		}
		
		/* TODO */
//		reRecognize();
	}
	
	private String recognizeSingleStroke(CStroke s0) {
		CStroke s = new CStroke(s0); /* TODO: Look into the necessity of this copying */
		CWrittenToken wt = new CWrittenToken();
		wt.addStroke(s);
		wt.normalizeAxes();
		
		double [] ps = new double[tokenEngine.tokenNames.size()];
		int recRes = tokenEngine.recognize(wt, ps);
		
		return tokenEngine.getTokenName(recRes);
	}
	
	private void addNewWrittenTokenFromStroke(CStroke s0) {
		if ( s0.isNormalized() )
			throw new RuntimeException("addNewWrittenTokenFromStroke received normalized CStroke");
		CStroke s = new CStroke(s0); 	/* Performance issue? */
//		/* TODO: Look into the necessity of this copying */
		
		CWrittenToken wt = new CWrittenToken();
		
		wt.addStroke(s);
		wt.normalizeAxes();
		
		double [] ps = new double[tokenEngine.tokenNames.size()];
		int recRes = tokenEngine.recognize(wt, ps);
		String winnerTokenName = tokenEngine.getTokenName(recRes);
		
		wtRecogWinners.add(winnerTokenName);
		wtRecogPs.add(ps);
		wtRecogMaxPs.add(ps[recRes]);
		
		wt.setRecogPs(ps);	/* TODO: Wrap into tokenEngine.recognize() */
		wt.setRecogWinner(winnerTokenName); /* TODO: Wrap into tokenEngine.recognize() */
		
		wtSet.addToken(wt, winnerTokenName, ps);
		
		strokeStats.set(strokeStats.size() - 1, wtSet.nTokens() - 1);
		
		int [] constStrokeIdx = new int[1];
		constStrokeIdx[0] = strokesUN.size() - 1;
		wtConstStrokeIdx.add(constStrokeIdx);
		
		/* Central X and Y coordinates of the token */
		wtCtrXs.add(wt.getCentralX());
		wtCtrYs.add(wt.getCentralY());
	}
	
	@Override
	public int [] removeLastToken() {
		if ( wtSet.empty() )
			return null;
		
		int i = wtSet.nTokens() - 1;
		
		wtCtrXs.remove(i);
		wtCtrYs.remove(i);
		
		wtSet.deleteToken(i);
		wtRecogWinners.remove(i);
		wtRecogPs.remove(i);
		wtRecogMaxPs.remove(i);
		
		/* Make copy of the constituent indices */
		int [] constIdx = new int[wtConstStrokeIdx.get(i).length];
		for (int n = 0; n < constIdx.length; ++n)
			constIdx[n] = wtConstStrokeIdx.get(i)[n];
		
		for (int n = constIdx.length - 1; n >= 0; --n) {
			strokes.remove(constIdx[n]);
			strokesUN.remove(constIdx[n]);
			strokeStats.remove(constIdx[n]);
		}
		
		wtConstStrokeIdx.remove(i);
		
		return constIdx;
	}
		
//	@Override
//	public void removeLastStroke() {
//		if ( strokes.isEmpty() )
//			return;
//		
//		strokes.remove(strokes.size() - 1);
//		strokeStats.remove(strokeStats.size() - 1);
//		
//		ctrXs.remove(ctrXs.size() - 1);
//		ctrYs.remove(ctrYs.size() - 1);
//		
//		/* TODO */
////		wtRecogWinners.removeLast();
////		wtRecogPs.removeLast();
////		wtRecogMaxPs.removeLast();
////		
////		/* TODO: Re-recognize */
////		wtConstStrokeIdx.removeLast();
//	}
	
	@Override
	public List<int []> getWrittenTokenConstStrokeIndices() {
		return wtConstStrokeIdx;
	}
	
	/* Get the index of the owning token of a stroke */
	private int getOwningTokenIndex(int strokeIdx) {
		if (strokeIdx < 0 || strokeIdx >= strokes.size()) { /* Bound check on index */
			return -1; /* -1 indicates that the stroke does not exist */
		}
				
		for (int i = 0; i < wtConstStrokeIdx.size(); ++i) {
			int [] constStrokeIndices = wtConstStrokeIdx.get(i);
			
			for (int j = 0; j < constStrokeIndices.length; ++j) {
				if (constStrokeIndices[j] == strokeIdx) {
					return i;
				}
			}
		}
		
		return -1; /* Edge case in which the specified stroke does not have an out-of-bound index, but is somehow not included in any tokens */
	}
	
	/* Remove tokens, without removing the constituent strokes */
	private void removeTokens(int [] removedTokenIndices) {				
		System.out.println("removeTokens: Removing " + removedTokenIndices.length + " token(s)"); //DEBUG
		if (removedTokenIndices.length == 0) {
			return;
		}
		
		for (int i = 0; i < removedTokenIndices.length; ++i) { //DEBUG
			System.out.println("  " + removedTokenIndices[i]); //DEBUG
		}                                                      //DEBUG
		
		boolean [] toPreserve = new boolean[getNumTokens()];
		for (int k = 0; k < toPreserve.length; ++k) {
			toPreserve[k] = ! ( ArrayUtils.contains(removedTokenIndices, k) );
			System.out.println("removeTokens: toPreserve[" + k + "] = " + toPreserve[k]); //DEBUG
		}
		
		/* Copies of old data */
		CWrittenTokenSet new_wtSet = new CWrittenTokenSet();
		List<Float> new_wtCtrXs = new LinkedList<Float>(); 	/* Central X coordinate of the written tokens */
		List<Float> new_wtCtrYs = new LinkedList<Float>(); 	/* Central X coordinate of the written tokens */		
		List<String> new_wtRecogWinners = new LinkedList<String>();
		List<double []> new_wtRecogPs = new LinkedList<double []>();
		List<Double> new_wtRecogMaxPs = new LinkedList<Double>();
		List<int []> new_wtConstStrokeIdx = new LinkedList<int []>();	/* Indices to constituents stroke indices */
		
		for (int k = 0; k < toPreserve.length; ++k) {
			if (toPreserve[k]) {				
				new_wtSet.addToken(wtSet.tokens.get(k), wtRecogWinners.get(k), wtRecogPs.get(k));
				
				new_wtCtrXs.add(wtCtrXs.get(k));
				new_wtCtrYs.add(wtCtrYs.get(k));
				new_wtRecogWinners.add(wtRecogWinners.get(k));
				new_wtRecogPs.add(wtRecogPs.get(k));
				new_wtRecogMaxPs.add(wtRecogMaxPs.get(k));
				new_wtConstStrokeIdx.add(wtConstStrokeIdx.get(k));
			}
		}
		
		/* Update the references to the new data */
		wtSet = new_wtSet;
		wtCtrXs = new_wtCtrXs;
		wtCtrYs = new_wtCtrYs;
		wtRecogWinners = new_wtRecogWinners;
		wtRecogPs = new_wtRecogPs;
		wtRecogMaxPs = new_wtRecogMaxPs;
		wtConstStrokeIdx = new_wtConstStrokeIdx;
	}
	
	/* Pluck a list of tokens of strokes, while preserving the strokes. 
	 *   "Plucking" means removing a subset of strokes from a token. */
	public void pluckTokens(List<Integer> idxTokensToPluck, 
			                List<int []> idxStrokeIndicesToPluck) {		
		int numTokensToPluck = idxTokensToPluck.size();		
		
		for (int i = 0; i < numTokensToPluck; ++i) {
			int tokenIdx = idxTokensToPluck.get(i);
			int [] constIdx = wtConstStrokeIdx.get(tokenIdx);
			int [] strokesToRemove = idxStrokeIndicesToPluck.get(i);
			
			CWrittenToken tmpWT = new CWrittenToken();
			List<Integer> constIndices = new ArrayList<Integer>(); 
			for (int j = 0; j < constIdx.length; ++j) {
				if ( !Arrays.asList(strokesToRemove).contains(constIdx[j]) ) {
					tmpWT.addStroke(new CStroke(strokesUN.get(constIdx[j])));
					constIndices.add(constIdx[j]);
				}
			}			
			tmpWT.normalizeAxes();
			
			/* Perform recognition on the new token */
			double [] newPs = new double[tokenEngine.tokenNames.size()];
			int newRecRes = tokenEngine.recognize(tmpWT, newPs);
			
			String newWinnerTokenName = tokenEngine.getTokenName(newRecRes); 
			double newMaxP = newPs[newRecRes];
			
			tmpWT.setRecogWinner(newWinnerTokenName);
			tmpWT.setRecogPs(newPs);
			
			wtSet.replaceToken(tokenIdx, tmpWT, newWinnerTokenName, newPs);
			wtCtrXs.set(tokenIdx, tmpWT.getCentralX());
			wtCtrYs.set(tokenIdx, tmpWT.getCentralY());
			wtRecogWinners.set(tokenIdx, newWinnerTokenName);
			wtRecogPs.set(tokenIdx, newPs);
			wtRecogMaxPs.set(tokenIdx, newMaxP);
			wtConstStrokeIdx.set(tokenIdx, MathHelper.listOfIntegers2ArrayOfInts(constIndices)); 		
		}
	}

	/* Force the merging of specified strokes into a token. The strokes may already
	 * belong to other tokens, in which case the other tokens need to be taken care of
	 * accordingly. 
	 */
	@Override
	public void mergeStrokesAsToken(int [] indices) {
		/* TODO: Sanity check on indices: no repeats, no invalid indices */
		
		int nStrokes = indices.length;
		
		System.out.println("nStrokes = " + nStrokes); //DEBUG
		System.out.println("  wtSet.recogWinners.size() = " + wtSet.recogWinners.size()); //DEBUG
		
		if (nStrokes == 0) { /* Edge case: no strokes to merge */
			return;
		}
		
		/* Determine the indices of the owning tokens */
		int [] ownerIndices = new int[nStrokes];
		List<Integer> idxTokensToRemove = new ArrayList<Integer>();
		List<Integer> idxTokensToPluck = new ArrayList<Integer>();
		List<int []> idxStrokeIndicesToPluck = new ArrayList<int []>();
		
		for (int i = 0; i < nStrokes; ++i) {
			ownerIndices[i] = getOwningTokenIndex(indices[i]);
			System.out.println("ownerIndices[" + i + "] = " + ownerIndices[i]); //DEBUG
		}
		
		/* Determine tokens needs to be removed and which need to be plucked */
		for (int i = 0; i < nStrokes; ++i) {
			int [] constIndices = wtConstStrokeIdx.get(ownerIndices[i]); 
			
			int numConstStrokes = constIndices.length;	/* Number of strokes in the token */
			int numOccurrences = MathHelper.countOccurrences(ownerIndices, ownerIndices[i]);
			System.out.println("Number of occurrences for token " + ownerIndices[i] + " in ownerIndices = " + numOccurrences); //DEBUG

			if (numConstStrokes == numOccurrences) {
				if ( !idxTokensToRemove.contains(ownerIndices[i]) ) {	/* TODO: Replace with Set */
					idxTokensToRemove.add(ownerIndices[i]);
					System.out.println("Adding token " + ownerIndices[i] + " to list of tokens to be removed"); //DEBUG
				}
			}
			else {
				idxTokensToPluck.add(ownerIndices[i]);
				idxStrokeIndicesToPluck.add(MathHelper.find(ownerIndices, indices[i]));
				System.out.println("Adding token " + ownerIndices[i] + " to list of tokens to be plucked"); //DEBUG
			}
		}
		
		System.out.println("# of tokens to be removed = " + idxTokensToRemove.size()); //DEBUG
		System.out.println("# of tokens to be plucked = " + idxTokensToPluck.size()); //DEBUG
		
		/* Remove the tokens that need to be removed (while preserving the strokes) */
		System.out.println("About to call removeTokens()");
		System.out.println("  wtSet.recogWinners.size() = " + wtSet.recogWinners.size()); //DEBUG
		
		int [] removedTokenIndices = MathHelper.listOfIntegers2ArrayOfInts(idxTokensToRemove);		
		removeTokens(removedTokenIndices);
		
		System.out.println("Done calling removeTokens()"); //DEBUG
		System.out.println("  getNumTokens() = " + this.getNumTokens()); //DEBUG
		System.out.println("  getNuMStrokes() = " + this.getNumStrokes()); //DEBUG
		System.out.println("  wtSet.recogWinners.size() = " + wtSet.recogWinners.size()); //DEBUG
		
		/* Pluck tokens that need to be plucked (while preserving the strokes */
		System.out.println("idxTokensToPluck.size() = " + idxTokensToPluck.size()); //DEBUG
		System.out.println("idxStrokeIndicesToPluck.size() = " + idxStrokeIndicesToPluck.size()); //DEBUG
		
		pluckTokens(idxTokensToPluck, idxStrokeIndicesToPluck);
		
		System.out.println("Done calling pluckTokens()"); //DEBUG
		
		/* Create a new token and add the strokes in */
		CWrittenToken tmpWT = new CWrittenToken();		
		for (int j = 0; j < indices.length; ++j) {
			tmpWT.addStroke(new CStroke(strokesUN.get(indices[j])));
		}
		tmpWT.normalizeAxes();
		
		/* Perform recognition on the new token */
		double [] newPs = new double[tokenEngine.tokenNames.size()];
		int newRecRes = tokenEngine.recognize(tmpWT, newPs);
		
		String newWinnerTokenName = tokenEngine.getTokenName(newRecRes); 
		double newMaxP = newPs[newRecRes];
		
		tmpWT.setRecogWinner(newWinnerTokenName); /* TODO: Refactor into tokenEngine.recognize() */
		tmpWT.setRecogPs(newPs);
		
		System.out.println("Done calling tokenEngine.recognize()"); //DEBUG
		System.out.println("  newWinnerTokenName = \"" + newWinnerTokenName + "\""); //DEBUG
		System.out.println("  newMaxP = \"" + newMaxP + "\""); //DEBUG
		
		/* Add the new token */
		System.out.println("About to call wtSet.addToken(): getNumTokens() = " + wtSet.getNumTokens()); //DEBUG
		System.out.println("  wtSet.recogWinners.size() = " + wtSet.recogWinners.size()); //DEBUG
		wtSet.addToken(wtSet.getNumTokens(), tmpWT, newWinnerTokenName, newPs);	
		
		/* Central X and Y coordinates of the token */
		wtCtrXs.add(tmpWT.getCentralX());
		wtCtrYs.add(tmpWT.getCentralY());
		
		/* Update recognition results */
		wtRecogWinners.add(newWinnerTokenName);
		wtRecogPs.add(newPs);
		wtRecogMaxPs.add(newMaxP);		
		
		/* Update the wtConstStrokeIdx */
		wtConstStrokeIdx.add(indices);
		
	}
	
	@Override
	public void clear() {
		wtSet.clear();
		strokes.clear();
		strokesUN.clear();
		strokeStats.clear();
		
//		ctrXs.clear();
//		ctrYs.clear();
		wtCtrXs.clear();
		wtCtrYs.clear();
		
		wtRecogWinners.clear();
		wtRecogPs.clear();
		wtRecogMaxPs.clear();
		
		wtConstStrokeIdx.clear();
	}
	
	@Override
	public CWrittenTokenSet getTokenSet() {
		return wtSet;
	}
	
	@Override
	public int getNumStrokes() {
		return strokes.size();
	}
	
	@Override
	public int getNumTokens() {
		return wtSet.nTokens();
	}
	
	@Override
	public boolean isEmpty() {
		return wtSet.empty();
	}
		
	/* Private methods */
	@Override
	public CWrittenTokenSet getWrittenTokenSet() {
		return wtSet;
	}
	
	@Override
	public List<String> getWrittenTokenRecogWinners() {
		return wtRecogWinners;
	}
	
	@Override
	public List<double []> getWrittenTokenRecogPs() {
		return wtRecogPs;
	}
 
	
	/* ~Methods */
	
	/* Setters */
	public void setMergePValueRatioThresh(float tMergPValueRatioThresh) {
		mergePValueRatioThresh = tMergPValueRatioThresh;
	}
	
	/* Getters */
	public float getMergePValueRatioThresh() {
		return mergePValueRatioThresh;
	}
}
