package me.scai.handwriting;

import java.util.List;

public interface StrokeCurator {
	/* Set the token engine */
//	void initialize(String [] tokNames, TokenEngineDerived tokEngine);
//	void initialize(TokenRecogEngine tokEngine);
	
	/* Add a new stroke */
	void addStroke(CStroke s);
	
	/* Remove the last stroke */
//	void removeLastStroke();
	
	/* Remove the last token */
	public int [] removeLastToken();
	
	/* Delete all strokes */
	void clear();
	
	/* Get the set of written tokens (CWrittenTokenSet) */
	CWrittenTokenSet getTokenSet();	
	
	/* Get the number of strokes that have been added */
	int getNumStrokes();
	
	/* Get the number of recognized tokens */
	int getNumTokens();
	
	/* Test and see if the stroke set is currently empty */
	boolean isEmpty();
	
	/* Getters for status information: written tokens and their recognition results */
	public CWrittenTokenSet getWrittenTokenSet();
	public List<String> getWrittenTokenRecogWinners();
	public List<double []> getWrittenTokenRecogPs();
}
