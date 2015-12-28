package me.scai.handwriting;

import com.google.gson.JsonObject;

import java.util.List;

public interface StrokeCurator {

	/* Add a new stroke */
	void addStroke(CStroke s);

	/* Merge a number of strokes as a token. These strokes may have already been
	 * incorporated in other tokens, in which case the proper removal and plucking
	 * of old tokens need to take place. */
	void mergeStrokesAsToken(int [] indices);

    /* Move a token
     * @returns  old bounds
     */
    public float[] moveToken(int tokenIdx, float [] newBounds);

	/* Remove the last stroke */
    // TODO
    //	void removeLastStroke();

    /**
     * Remove i-th token
     * @param     idxToken: Index to the token
     * @return    Indices to constituent strokes that made up the removed token */
    int [] removeToken(int idxToken);

	/**
	 * Remove the last token
	 * @return    Indices to constituent strokes that made up the removed token */
	int [] removeLastToken();

	/* Delete all strokes */
	void clear();

	/* Get the set of written tokens (CWrittenTokenSet) */
	CWrittenTokenSet getTokenSet();

    List<String> getTokenUuids();

    String getTokenUuid(int tokenIdx);

	/* Get the number of strokes that have been added */
	int getNumStrokes();

	/* Get the number of recognized tokens */
	int getNumTokens();

	/* Test and see if the stroke set is currently empty */
	boolean isEmpty();

	/* Getters for status information: written tokens and their recognition results */
	CWrittenTokenSet getWrittenTokenSet();
	List<String> getWrittenTokenRecogWinners();
	List<double []> getWrittenTokenRecogPs();

	/* Get the constituent stroke indicies */
	List<int []> getWrittenTokenConstStrokeIndices();

	/* Force setting the recognition winner */
	void forceSetRecogWinner(int tokenIdx, String recogWinner);

    /* Serialization methods */
    /* Get serialized form of the strokes */
    List<String> getSerializedStrokes();

    String getSerializedTokenSet();

    String getSerializedConstStrokeIndices();

    /* State and stack */
    JsonObject getStateSerialization();
    String getStateSerializationString();

    /**
     * Get the last user action;
     *
     * @return
     */
    StrokeCuratorUserAction getLastUserAction();
    void undoUserAction();
    void redoUserAction();

    boolean canUndoUserAction();
    boolean canRedoUserAction();

    /* Injection of serialized state */
    void injectSerializedState(JsonObject json);

    /**
     * Get all possible toke names (NOT display names)
     */
    List<String> getAllTokenNames();


}
