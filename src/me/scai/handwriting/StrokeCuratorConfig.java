package me.scai.handwriting;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.net.URL;
import com.google.gson.Gson;
import me.scai.parsetree.TextHelper;
import me.scai.parsetree.GeometryHelper;

/* Class tokenPairRule */
class TokenPairRule {
	public String tokenA;
	public String tokenB;
	public List<String> predicaments;
	public String recommendation;
	
	public boolean applyPredicaments(float [] boundsA, float [] boundsB, 
			                         List<Float> wtCtrXs, List<Float> wtCtrYs) {
		boolean allPredSatisfied = true;
		
		for (int i = 0; i < predicaments.size(); ++i) {
			String pred = predicaments.get(i);	/* TODO: Fine-tune performance */
			
			String [] items = pred.split(" ");
			assert(items.length == 3);
			
			String predType = items[0].trim();
			String compareType = items[1].trim();
			float criterionVal = Float.parseFloat(items[2].trim());
			
			float val = 0.0F;
			if (predType.equals("relativeWidthDifference")) {
				val = GeometryHelper.absoluteRelativeDifference(boundsA[2] - boundsA[0], boundsB[2] - boundsB[0]);
			}
			else if (predType.equals("relativeHeightDifference")) {
				val = GeometryHelper.absoluteRelativeDifference(boundsA[3] - boundsA[1], boundsB[3] - boundsB[1]);
			}			
			else if (predType.equals("relativeLeftXOffset")) {
				val = GeometryHelper.absoluteRelativeLeftXOffset(boundsA, boundsB);
			}
			else if (predType.equals("relativeRightXOffset")) {
				val = GeometryHelper.absoluteRelativeRightXOffset(boundsA, boundsB);
			}
			else if (predType.equals("relativeRightToLeftOffset")) {
				val = GeometryHelper.relativeRightToLeftOffset(boundsA, boundsB);
			}
			else if (predType.equals("relativeTopYOffset")) {
				val = GeometryHelper.absoluteRelativeTopYOffset(boundsA, boundsB);
			}
			else if (predType.equals("relativeBottomYOffset")) {
				val = GeometryHelper.absoluteRelativeBottomYOffset(boundsA, boundsB);
			}
			else if (predType.equals("numTokensInBetweenX")) {
				val = (float) GeometryHelper.getNumTokensInBetween("X", boundsA, boundsB, wtCtrXs, wtCtrYs);				
			}
			else if (predType.equals("numTokensInBetweenY")) {
				val = (float) GeometryHelper.getNumTokensInBetween("Y", boundsA, boundsB, wtCtrXs, wtCtrYs);				
			}
			else {
				val = 0.0F; /* TODO: Implement */
			}
			
			boolean predSat = false;
			if (compareType.equals("<")) {
				predSat = val < criterionVal;
			}
			else if (compareType.equals(">")) {
				predSat = val > criterionVal;
			}
			else if (compareType.equals("<=")) {
				predSat = val <= criterionVal;
			}
			else if (compareType.equals(">=")) {
				predSat = val >= criterionVal;
			}
			else if (compareType.equals("==")) {
				predSat = val == criterionVal;
			}
			else {
				throw new RuntimeException("Unrecognized comparison types: \"" + compareType + "\"");
			}
			
			if (!predSat) {
				allPredSatisfied = false;
				break;
			}
		
		}
		
		return allPredSatisfied;
	}
	
	public String getMergeRecommendation() {
		final String assertPrefix = "mergeAs: \"";
		final String assertSuffix = "\"";
		
		assert(recommendation.startsWith(assertPrefix));
		assert(recommendation.endsWith(assertSuffix));
		
		String out = recommendation.replace(assertPrefix, "");
		return out.substring(0, out.length() - assertSuffix.length());
	}
}


/* Class: StrokeCuratorConfig: a set of rules */
public class StrokeCuratorConfig {
	private static final Gson gson  = new Gson();
	
	public List<TokenPairRule> tokenPairRules;
	public Map<String, List<String> > mergePartners; 
	
	/* Factory method: From JSON String */
	public static StrokeCuratorConfig fromJson(String json) {
		return (StrokeCuratorConfig) gson.fromJson(json, StrokeCuratorConfig.class);
	}
	
	public static StrokeCuratorConfig fromJsonFile(String configFilePath) {
		String configJson = null;
		try {
			configJson = TextHelper.readTextFile(configFilePath);
		}
		catch (IOException exc) {
			return null;
		}
		
		return fromJson(configJson);
	}
	
	public static StrokeCuratorConfig fromJsonFileAtUrl(URL configFileUrl) {
		String configJson = null;
		try {
			configJson = TextHelper.readTextFileAtUrl(configFileUrl);
		}
		catch (IOException exc) {
			return null;
		}
//		System.out.println("configJson = \"" + configJson + "\""); //DEBUG
		
		return fromJson(configJson);
	}
	
	
	public List<String> getMergePartners(String token) {
		return mergePartners.get(token);
	}
	
	public boolean potentiallyMergeable(String tokenA, String tokenB) {				
		if (mergePartners.containsKey(tokenA)) {
			return mergePartners.get(tokenA).contains(tokenB);
		}
		else if (mergePartners.containsKey(tokenB)) {
			return mergePartners.get(tokenB).contains(tokenA);
		}
		else {
			return true;
		}
	}
	
	/* Return value: the name of the recommended merged. If merge is not recommended, return null. */
	public String applyRule(String tokenNameA, String tokenNameB, 
			                float [] boundsA, float [] boundsB, 
			                List<Float> wtCtrXs, List<Float> wtCtrYs) {
		String out = null;
		
		for (int i = 0; i < tokenPairRules.size(); ++i) {
			TokenPairRule rule = tokenPairRules.get(i);
			
			/* Iterate through the predicaments. All of them have to be satisfied */
			if ( (rule.tokenA.equals(tokenNameA) && rule.tokenB.equals(tokenNameB)) || 
				 (rule.tokenA.equals(tokenNameB) && rule.tokenB.equals(tokenNameA)) ) {
				if ( rule.applyPredicaments(boundsA, boundsB, wtCtrXs, wtCtrYs) || 
				     rule.applyPredicaments(boundsB, boundsA, wtCtrXs, wtCtrYs) ) {
					out = rule.getMergeRecommendation();
					break;
				}
			}
		}
		
		return out;
	}
	
}
