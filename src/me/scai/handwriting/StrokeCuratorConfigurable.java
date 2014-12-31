package me.scai.handwriting;

import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

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
	
	private List<Integer> strokeStats = new LinkedList<Integer>();
	
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
	/* ~Member variables */
	
	/* Methods */
	/* Constructor: 
	 * 	Input arguments: configFN: Configuration file
	 *   */
	public StrokeCuratorConfigurable(String configFN, TokenRecogEngine tokEngine) {
		tokenEngine = tokEngine;
		
		config = StrokeCuratorConfig.fromJsonFile(configFN);
		
//		String [] lines;
//		try {
//			lines = TextHelper.readLinesTrimmedNoComment(configFN, configFileCommentString);
//		}
//		catch ( Exception e ) {
//			throw new RuntimeException("Failed to read terminal set from file: " + configFN);
//		}
//		
//		for (int i = 0; i < lines.length; ++i) {
//			String line = lines[i];
//			
//			if ( line.startsWith("<NO_MERGE>") ) {
//				String [] items = line.replace("\t", " ").split(" ");
//				
//				for (int j = 1; j < items.length; ++j)
//					if ( items[j].length() > 0 )
//						noMergeTokens.add(items[j]);
//			}
//		}
	}
	
	/* Constructor */
	public StrokeCuratorConfigurable(String [] tokNames, TokenRecogEngine tokEngine) {
		if ( tokNames == null )
			throw new RuntimeException("Input tokNames is null");
			
		if ( tokEngine == null )
			throw new RuntimeException("Input tokEngine is null");
		
//		initialize(tokEngine);
	}
	
//	public void initialize(//String [] tokNames, 
//						   TokenRecogEngine tokEngine) {
////		tokenNames = tokNames;
//		tokenEngine = tokEngine;
//	}
	
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
	
	private boolean tryMergeTokenWithStroke(boolean toCompareMaxPs, int oldWrittenTokenIdx, int strokeUNIdx) {
		boolean merged = false;
		
		int [] constIdx = wtConstStrokeIdx.get(oldWrittenTokenIdx);
		
		CWrittenToken tmpWT = generateMergedToken(oldWrittenTokenIdx, strokesUN.size() - 1);
		
		double [] newPs = new double[tokenEngine.tokenNames.size()];
		int newRecRes = tokenEngine.recognize(tmpWT, newPs);
		
		String newWinnerTokenName = tokenEngine.getTokenName(newRecRes); 
		double newMaxP = newPs[newRecRes];
		
		if ( (!toCompareMaxPs) || (newMaxP > wtRecogMaxPs.get(oldWrittenTokenIdx)) ) {
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
					recomMergeToken = config.applyRule(strokeRecogWinner, 
												       wtSet.recogWinners.get(i), 
												       s_bounds, t_bounds, 
												       wtCtrXs, wtCtrYs);	/* Assume, the first argument is the new stroke. TODO: Make this assumption a necessity. */
					if (recomMergeToken != null) {
						recomMergeIdx = i;
						
						merged = true;
						tryMergeTokenWithStroke(false, i, strokesUN.size() - 1);
					}
				}
				negOverlapCoeffs[i] = -1f * GeometryHelper.pctOverlap(t_xBounds, s_xBounds) * 
						                    GeometryHelper.pctOverlap(t_yBounds, s_yBounds);
			}
			
			int [] idxInSorted = new int[negOverlapCoeffs.length];			
			MathHelper.sort(negOverlapCoeffs, idxInSorted);
			
			/* DEBUG */
//			String stringDebug = "";
//			for (int k = 0; k < idxInSorted.length; ++k)
//				stringDebug += " " + negOverlapCoeffs[idxInSorted[k]];
			/* ~DEBUG */
			
			if (!merged) {
				for (int n = 0; n < idxInSorted.length; ++n) {
					if ( -negOverlapCoeffs[n] < overlapCoeffLB ) {
						break;
					}
					
					int wtIdx = idxInSorted[n];
					
					if ( tryMergeTokenWithStroke(true, wtIdx, strokesUN.size() - 1) ) {
						merged = true;
						break;
					}
					
	//				int [] constIdx = wtConstStrokeIdx.get(wtIdx);
	//				
	//				CWrittenToken tmpWT = this.generateMergedToken(wtIdx, strokesUN.size() - 1);
	//				
	//				double [] newPs = new double[tokenEngine.tokenNames.size()];
	//				int newRecRes = tokenEngine.recognize(tmpWT, newPs);
	//				
	//				String newWinnerTokenName = tokenEngine.getTokenName(newRecRes); 
	//				double newMaxP = newPs[newRecRes];
	//				
	//				if ( newMaxP > wtRecogMaxPs.get(wtIdx) ) {
	//					/* Replace the old token set with a new one that includes the new stroke */
	//					wtRecogWinners.set(wtIdx, newWinnerTokenName);
	//					wtRecogPs.set(wtIdx, newPs);
	//					wtRecogMaxPs.set(wtIdx, newMaxP);
	//					
	//					int [] newConstIdx = new int[constIdx.length + 1];
	//					java.lang.System.arraycopy(constIdx, 0, newConstIdx, 0, constIdx.length);
	//					newConstIdx[constIdx.length] = strokesUN.size() - 1;
	//					wtConstStrokeIdx.set(wtIdx, newConstIdx);
	//					
	//					strokeStats.set(strokeStats.size() - 1, wtIdx);
	//					
	//					wtSet.deleteToken(wtIdx);
	//					wtSet.addToken(wtIdx, tmpWT, newWinnerTokenName, newPs);
	//					/* TODO: wtSet.addToken(wtIdx, wt, winnerTokenName, ps) */
	//					
	//					/* Central X and Y coordinates of the token */
	//					wtCtrXs.set(wtIdx, tmpWT.getCentralX());
	//					wtCtrYs.set(wtIdx, tmpWT.getCentralY());
	//					
	//					merged = true;
	//					break;
	//				}
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
	private void reRecognize() {
		/* TODO */
	}
	
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
	
}
