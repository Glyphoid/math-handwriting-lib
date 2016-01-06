package me.scai.plato.engine;

import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import me.scai.handwriting.*;

import me.scai.parsetree.*;
import me.scai.parsetree.evaluation.ParseTreeEvaluator;
import me.scai.parsetree.evaluation.ParseTreeEvaluatorException;

import me.scai.parsetree.evaluation.PlatoVarMap;
import me.scai.parsetree.evaluation.ValueUnion;
import me.scai.plato.helpers.CWrittenTokenSetJsonHelper;
import me.scai.utilities.PooledWorker;
import org.apache.commons.lang.ArrayUtils;

public class HandwritingEngineImpl implements HandwritingEngine, PooledWorker {
    /* Constants */
    private static final Gson gson = new Gson();

    private static final String STROKE_CURATOR_STATE_JSON_KEY = "strokeCuratorState";
    private static final String CURRENT_TOKEN_SET_JSON_KEY    = "currentTokenSet";
    private static final String ABSTRACT_2_WRITTEN_TOKEN_UUIDS_JSON_KEY = "abstract2WrittenTokenUuids";

    private static final int STATE_STACK_CAPACITY = 20;

    /* Member variables */
    public StrokeCurator strokeCurator;
    public TokenSetParser tokenSetParser;
    public TokenSet2NodeTokenParser tokenSet2NodeTokenParser;
    public ParseTreeStringizer stringizer;
    public ParseTreeMathTexifier mathTexifier;
    public ParseTreeEvaluator evaluator;

    // Keeps track of the latest CWrittenTokenSetNoStroke for the parser. Could contain NodeTokens if subset parsing
    // has occurred before.
    private CWrittenTokenSetNoStroke currentTokenSet;

    // Keeps track of which written tokens make up of the node tokens
    // Only the node tokens are tracked by this variable
    private LinkedList<List<String>> abstract2WrittenTokenUuids;


    // State stack for undo/redo
    private StateStack<HandwritingEngineState> stateStack = new StateStack<>(STATE_STACK_CAPACITY);
    private JsonObject initialState;

    /* Constructor */
    public HandwritingEngineImpl(StrokeCurator tStrokeCurator,
                                 TokenSetParser tTokenSetParser,
                                 GraphicalProductionSet gpSet,
                                 TerminalSet termSet) {
        strokeCurator  = tStrokeCurator;
        tokenSetParser = tTokenSetParser;

        stringizer     = gpSet.genStringizer();
        mathTexifier   = new ParseTreeMathTexifier(gpSet, termSet);
        evaluator      = gpSet.genEvaluator();

        tokenSet2NodeTokenParser = new TokenSet2NodeTokenParser(tokenSetParser, stringizer);

        currentTokenSet = new CWrittenTokenSetNoStroke();
        abstract2WrittenTokenUuids = new LinkedList<>();

        initialState = getStateSerialization();
    }

    /* Mehtods */
    @Override
    public void addStroke(CStroke stroke) {
        this.strokeCurator.addStroke(stroke);

        updateCurrentTokenSet();
        pushStateStack(HandwritingEngineUserAction.AddStroke);
    }

    @Override
    public void removeLastToken() {
        this.strokeCurator.removeLastToken();

        updateCurrentTokenSet();
        pushStateStack(HandwritingEngineUserAction.RemoveLastToken);
    }

    @Override
    public void removeToken(int idxToken) throws HandwritingEngineException {
        if (idxToken >= currentTokenSet.getNumTokens()) {
            throw new HandwritingEngineException("Token index (" + idxToken + ") exceeds the number of availbale tokens");
        }

        if (idxToken < 0) {
            throw new HandwritingEngineException("Token index (" + idxToken + ") is invalid");
        }

        List<Integer> wtIndices = abstract2writtenTokenIndex(idxToken);

        assert(!wtIndices.isEmpty());

        try {
            for (int wtIndex : wtIndices) {
                this.strokeCurator.removeToken(wtIndex);
            }
        } catch (IllegalArgumentException exc) {
            throw new HandwritingEngineException("removeToken() failed due to: " + exc.getMessage());
        }

        updateCurrentTokenSet();
        pushStateStack(HandwritingEngineUserAction.RemoveToken);
    }

    @Override
    public void removeTokens(int[] tokenIndices) throws HandwritingEngineException {
        List<Integer> writtenTokenIndices = new ArrayList<>();
        for (int tokenIndex : tokenIndices) {
            writtenTokenIndices.addAll(abstract2writtenTokenIndex(tokenIndex));
        }

        Integer[] sortedWrittenTokenIndices = new Integer[writtenTokenIndices.size()];
        writtenTokenIndices.toArray(sortedWrittenTokenIndices);
        Arrays.sort(sortedWrittenTokenIndices);

        try {
            // Remove the written tokens in the reverse (descending-index) order, to avoid out-of-date indices
            for (int i = sortedWrittenTokenIndices.length - 1; i >= 0; --i) {
                this.strokeCurator.removeToken(sortedWrittenTokenIndices[i]);
            }
        } catch (IllegalArgumentException exc) {
            throw new HandwritingEngineException("removeToken() failed due to: " + exc.getMessage());
        }

        updateCurrentTokenSet();
        pushStateStack(HandwritingEngineUserAction.RemoveTokens);
    }

    /**
     * Move an abstract token
     * @param tokenIdx  Index to the abstract token
     * @param newBounds
     * @throws HandwritingEngineException
     */
    @Override
    public void moveToken(int tokenIdx, float[] newBounds)
            throws HandwritingEngineException {
        moveTokensInternal(new int[] {tokenIdx}, new float[][] {newBounds});

        pushStateStack(HandwritingEngineUserAction.MoveToken);
    }

    @Override
    public void moveTokens(int[] tokenIndices, float[][] newBoundsArray)
            throws HandwritingEngineException {
        moveTokensInternal(tokenIndices, newBoundsArray);

        pushStateStack(HandwritingEngineUserAction.MoveTokens);
    }

    private void moveTokensInternal(int[] tokenIndices, float[][] newBoundsArray)
            throws HandwritingEngineException {
        // Input sanity check
        assert(tokenIndices.length == newBoundsArray.length);

        for (int n = 0; n < tokenIndices.length; ++n) {
            final int tokenIdx = tokenIndices[n];
            final float[] newBounds = newBoundsArray[n];

            // Translate the abstract token index into written token index
            List<Integer> wtIndices = abstract2writtenTokenIndex(tokenIdx);

            if (wtIndices.size() > 1) {
                // Moving an abstract bound. Need to explicitly calculate the displacement vector
                float[] dispVec = new float[2]; // X and Y displacements

                float[] origBounds = getTokenBounds(tokenIdx);

                dispVec[0] = newBounds[0] - origBounds[0]; // X displacement
                dispVec[1] = newBounds[1] - origBounds[1]; // Y displacement

                for (int i = 0; i < wtIndices.size(); ++i) {
                    final int wtIndex = wtIndices.get(i);

                    float[] origWrittenTokenBound = strokeCurator.getTokenSet().getTokenBounds(wtIndex);
                    float[] newWrittenTokenBound = ArrayUtils.clone(origWrittenTokenBound);

                    newWrittenTokenBound[0] += dispVec[0]; // Apply X displacement
                    newWrittenTokenBound[2] += dispVec[0];
                    newWrittenTokenBound[1] += dispVec[1]; // Apply X displacement
                    newWrittenTokenBound[3] += dispVec[1];

                    try {
                        strokeCurator.moveToken(wtIndex, newWrittenTokenBound);
                    } catch (IllegalArgumentException exc) {
                        throw new HandwritingEngineException(exc.getMessage());
                    }

                }

            } else {
                assert (wtIndices.size() == 1); // TODO: Implement moving of abstract tokens

                try {
                    strokeCurator.moveToken(wtIndices.get(0), newBounds);
                } catch (IllegalArgumentException exc) {
                    throw new HandwritingEngineException(exc.getMessage());
                }
            }
        }

        updateCurrentTokenSet();
    }

    @Override
    public void mergeStrokesAsToken(int [] strokeInds) {
        this.strokeCurator.mergeStrokesAsToken(strokeInds);

        updateCurrentTokenSet();
        pushStateStack(HandwritingEngineUserAction.MergeStrokesAsToken);
    }

    // tokenIdx is for the written tokens (stroke curator), instead of the abstract token set.
    @Override
    public void forceSetRecogWinner(int tokenIdx, String tokenName) throws HandwritingEngineException {
        List<Integer> wtIndices = abstract2writtenTokenIndex(tokenIdx);

        if (wtIndices.size() != 1) {
            throw new HandwritingEngineException("The token (" + tokenIdx + ")is not a simple written token");
        }

        this.strokeCurator.forceSetRecogWinner(wtIndices.get(0), tokenName);

        updateCurrentTokenSet();
        pushStateStack(HandwritingEngineUserAction.ForceSetTokenName);
    }

    @Override
    public void clearStrokes() {
        this.strokeCurator.clear();

        updateCurrentTokenSet();
        pushStateStack(HandwritingEngineUserAction.ClearStrokes);
    }

    /**
     * Could contain node tokens
     * @return
     */
    @Override
    public CAbstractWrittenTokenSet getTokenSet() {
        return currentTokenSet;
    }

    /**
     * Contains only basic written tokens: Never contains node tokens
     * @return
     */
    @Override
    public CWrittenTokenSet getWrittenTokenSet() {
        return strokeCurator.getWrittenTokenSet();
    }

    @Override
    public List<String> getWrittenTokenUUIDs() {
        return strokeCurator.getTokenUuids();
    }

    @Override
    public List<int []> getTokenConstStrokeIndices() {
        return strokeCurator.getWrittenTokenConstStrokeIndices();
    }

    /* Perform token set parsing */
    @Override
    public TokenSetParserOutput parseTokenSet() throws HandwritingEngineException {
        return parseTokenSet(false, null);
    }

    @Override
    public TokenSetParserOutput parseTokenSubset(int[] tokenIndices) throws HandwritingEngineException {
        TokenSetParserOutput parserOutput = parseTokenSet(true, tokenIndices);

        pushStateStack(HandwritingEngineUserAction.ParseTokenSubset);
        return parserOutput;
    }

    private TokenSetParserOutput parseTokenSet(boolean isSubsetParsing, int[] tokenIndices) throws HandwritingEngineException {
        // Input sanity check
        if ( isSubsetParsing ) {
            assert(tokenIndices != null);
        } else{
            assert(tokenIndices == null);
        }

        Node parseOutRoot = null;
        try {
            if (isSubsetParsing) {
                abstract2WrittenTokenUuids = new LinkedList<>();

                currentTokenSet = tokenSet2NodeTokenParser.parseAsNodeToken(currentTokenSet, tokenIndices, abstract2WrittenTokenUuids);
                // Token indices holds

                // TODO: This is probably not compatible with nested subset parsing yet. Make it compatible.
                // Supply the constituent token UUIDs of the new node token
                List<String> nodeTokenConstituentTokenUuids = new ArrayList<>();
                ((ArrayList) nodeTokenConstituentTokenUuids).ensureCapacity(tokenIndices.length);
                for (int tokenIndex : tokenIndices) {
                    nodeTokenConstituentTokenUuids.add(strokeCurator.getTokenUuid(tokenIndex));
                }

                // Assume the the result from parsing the subset is stored in the first abstract token, i.e., the
                // first abstract token is expected to be a NodeToken
                NodeToken nodeToken = (NodeToken) currentTokenSet.tokens.get(0);
                parseOutRoot = nodeToken.getNode();
            } else {
                parseOutRoot = tokenSetParser.parse(currentTokenSet);
            }
        } catch (TokenSetParserException exc) {
            String msg = "Failed to parse token set";
            if (exc.getMessage() != null && !exc.getMessage().isEmpty()) {
                msg += " due to: " + exc.getMessage();
            }

            throw new HandwritingEngineException(msg);
        } catch (InterruptedException exc) {
            throw new HandwritingEngineException("Parser interrupted");
        } catch (Throwable thr) {
            thr.printStackTrace();
            throw new HandwritingEngineException("Parsing failed due to: " + thr.getMessage());
        }

        /* Invoke evaluator */
        String evalRes = null;
        try {
            evalRes = evaluator.eval2String(parseOutRoot);
        }
        catch (ParseTreeEvaluatorException exc) {
            evalRes = "Exception occurred during evaluation: " + exc.getMessage();
        }

        TokenSetParserOutput output = new TokenSetParserOutput(
                stringizer.stringize(parseOutRoot),
                evalRes,
                mathTexifier.texify(parseOutRoot)
        );

        return output;
    }


    @Override
    public JsonArray getGraphicalProductions() {
        return gson.toJsonTree(tokenSetParser.getGraphicalProductionSet().prods).getAsJsonArray();
    }

    /**
     * Get the bounds of a specified abstract token (Could contain node tokens)
     * @param tokenIdx
     * @return
     * @throws HandwritingEngineException
     */
    @Override
    public float[] getTokenBounds(int tokenIdx)
            throws HandwritingEngineException {
        if (tokenIdx < 0 || tokenIdx >= currentTokenSet.nTokens()) {
            throw new HandwritingEngineException("Invalid abstract token index " + tokenIdx);
        } else {
            return currentTokenSet.getTokenBounds(tokenIdx);
        }
    }

    /**
     * Get bounds of a specified basic written token (Never concerned with node tokens)
     * @param tokenIdx
     * @return
     * @throws HandwritingEngineException
     */
    @Override
    public float[] getWrittenTokenBounds(int tokenIdx)
            throws HandwritingEngineException {
        CWrittenTokenSet wtSet = strokeCurator.getTokenSet();

        if (tokenIdx < 0 || tokenIdx >= wtSet.nTokens()) {
            throw new HandwritingEngineException("Invalid written token index " + tokenIdx);
        } else {
            return wtSet.getTokenBounds(tokenIdx);
        }
    }

    @Override
    public PlatoVarMap getVarMap()
            throws HandwritingEngineException {
        return evaluator.getVarMap();
    }

    @Override
    public ValueUnion getFromVarMap(String varName)
            throws HandwritingEngineException {
        ValueUnion vu = evaluator.getFromVarMap(varName);

        if (vu == null) {
            throw new HandwritingEngineException("There is no variable with name \"" + varName + "\" currently defined");
        } else {
            return vu;
        }
    }

    @Override
    public void removeEngine() {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void injectState(JsonObject stateData) {
        strokeCurator.injectSerializedState(stateData); //TODO: Check validity in the face of subset parsing

        updateCurrentTokenSet();
    }

    @Override
    public HandwritingEngineUserAction getLastUserAction() {
//        return strokeCurator.getLastUserAction();
        HandwritingEngineUserAction userAction = null;
        if (stateStack.getLastUserAction() != null) {
            userAction = HandwritingEngineUserAction.valueOf(stateStack.getLastUserAction());
        }

        return userAction;
    }

    @Override
    public void undoUserAction() {
        stateStack.undo();

        injectSerializedState(stateStack.getLastSerializedState() == null ?
                              initialState : stateStack.getLastSerializedState());
    }

    @Override
    public void redoUserAction() {
        stateStack.redo();

        injectSerializedState(stateStack.getLastSerializedState());
    }

    @Override
    public boolean canUndoUserAction() {
        return stateStack.canUndo();
    }

    @Override
    public boolean canRedoUserAction() {
        return stateStack.canRedo();
    }

    @Override
    public List<String> getAllTokenNames() {
        return strokeCurator.getAllTokenNames();
    }

    /**
     * Update currentTokenSet and abstract2WrittenTokenUuids. Depending on whether there are node tokens changes, the values will be
     * refreshed from the stroke curator or not.
     */
    private void updateCurrentTokenSet() {

        boolean refreshFromStrokeCurator = false;

        if (this.currentTokenSet.hasNodeToken()) {
            // Examine the correspondence between the abstract tokens and the written tokens. If all node tokens'
            // constituents written tokens still exist, will just copy the changes in the written tokens outside the
            // node tokens.
            List<String> wtUuids = strokeCurator.getTokenUuids(); // UUIDs of the written tokens
            final int nst = wtUuids.size(); // Number of stroke-curator tokens

            ArrayList<Boolean> marked = new ArrayList<>();
            marked.ensureCapacity(nst);
            for (int i = 0; i < nst; ++i) {
                marked.add(false);
            }

            boolean nodeTokensOkay = true;  // Whether the tokens that comprise the node tokens are still all available
            LinkedList<Integer> tokensToRemove = new LinkedList<>(); // Tokens in currentTokenSet to remove

            List<AbstractToken> tokensToAdd = new ArrayList<>(); // New tokens that appears in stroke curator and hence need to be added to currentTokenSet
            List<String> tokenUuidsToAdd = new ArrayList<>();    // UUIDs of the new tokens to be added

            for (int i = 0; i < currentTokenSet.tokens.size(); ++i) {
                if (currentTokenSet.tokens.get(i) instanceof NodeToken) {
                    // Examine node tokens for changes

                    NodeToken nodeToken = (NodeToken) currentTokenSet.tokens.get(i);

                    assert (nodeToken.getTokenSet() instanceof CWrittenTokenSetNoStroke);
                    CWrittenTokenSetNoStroke tokenSet = (CWrittenTokenSetNoStroke) nodeToken.getTokenSet();

                    List<List<String>> tokenUuidsLists = tokenSet.getConstituentTokenUuids();
                    for (List<String> tokenUuids : tokenUuidsLists) {
                        for (String tokenUuid : tokenUuids) {
                            final int idx = wtUuids.indexOf(tokenUuid);
                            if (idx == -1) {
                                nodeTokensOkay = false;
                                break;
                            } else {
                                marked.set(idx, true);
                            }
                        }

                        if (!nodeTokensOkay) { break; }
                    }

                    if (!nodeTokensOkay) { break; }

                } else {
                    // Examine written tokens for changes
                    List<String> tokenUuids = currentTokenSet.getConstituentTokenUuids(i);
                    assert(tokenUuids.size() == 1);

                    String tokenUuid = tokenUuids.get(0);

                    final int idx = wtUuids.indexOf(tokenUuid);

                    if (idx == -1) {
                        // This token has been removed from the stroke curator
                        // Add items in the reverse order so that when we remove them, we can go from the head to the
                        // tail without worrying about out-of-date indices
                        tokensToRemove.addFirst(i);
                    } else {
                        marked.set(idx, true);
                    }
                }
            }

            // Tally the new tokens in stroke curator that need to be added to currentTokenSet
            for (int k = 0; k < nst; ++k) {
                if ( !marked.get(k) ) { // Not marked yet.
                    tokensToAdd.add(strokeCurator.getTokenSet().tokens.get(k));
                    tokenUuidsToAdd.add(strokeCurator.getTokenUuid(k));
                }
            }

            if (nodeTokensOkay) {
                refreshFromStrokeCurator = false;

                // Remove any tokens to be removed from currentTokenSet
                Iterator<Integer> removeIdxIter = tokensToRemove.iterator();
                while (removeIdxIter.hasNext()) {
                    final int removeIdx = removeIdxIter.next();

                    currentTokenSet.removeToken(removeIdx);
                    abstract2WrittenTokenUuids.remove(removeIdx);
                }

                // Add new tokens
                assert(tokensToAdd.size() == tokenUuidsToAdd.size());
                for (int i = 0; i < tokensToAdd.size(); ++i) {
                    currentTokenSet.addTokenWithAutoTokenID(tokensToAdd.get(i), tokenUuidsToAdd.get(i));

                    List<String> singleItemList = new ArrayList<>();
                    singleItemList.add(tokenUuidsToAdd.get(i));
                    abstract2WrittenTokenUuids.add(singleItemList);
                }

                assert(currentTokenSet.getNumTokens() == abstract2WrittenTokenUuids.size());

            } else {
                refreshFromStrokeCurator = true;
            }
        } else {
            refreshFromStrokeCurator = true;
        }

        // If necessary, update currentTokenSet to the latest written token set from the stroke curator. This will
        // abandon node tokens (if any).
        if (refreshFromStrokeCurator) {
            // Update currentTokenSet
            this.currentTokenSet = new CWrittenTokenSetNoStroke(strokeCurator.getTokenSet(), strokeCurator.getTokenUuids());

            // Update abstract2WrittenTokenUuids
            this.abstract2WrittenTokenUuids = new LinkedList<>();
            for (int i = 0; i < this.currentTokenSet.getNumTokens(); ++i) {
                List<String> singleItemList = new ArrayList<>();
                singleItemList.add(strokeCurator.getTokenUuid(i));

                abstract2WrittenTokenUuids.add(singleItemList);
            }

            assert(currentTokenSet.getNumTokens() == abstract2WrittenTokenUuids.size());
        }

        assert(currentTokenSet.tokenUuids.size() == currentTokenSet.tokens.size());
        assert(currentTokenSet.tokenIDs.size() == currentTokenSet.tokens.size());

    }

    /**
     * Translate index of abstract token (in currentTokenSet) to the token indices in stroke curator
     * @param idxToken  Index of the abstract token in currentTokenSet
     * @return          A list of indices of written tokens in stroke curator
     */
    private List<Integer> abstract2writtenTokenIndex(int idxToken) {
        List<String> uuids = currentTokenSet.getConstituentTokenUuids(idxToken);

        List<String> wtUuids = strokeCurator.getTokenUuids();
        List<Integer> wtIndices = new ArrayList<>();

        for (String uuid : uuids) {
            int wtIdx = wtUuids.indexOf(uuid);
            if (wtIdx == -1) {
                throw new RuntimeException("Failed to find token UUID " + uuid + " in stroke curator");
            }

            wtIndices.add(wtIdx);
        }

        return wtIndices;
    }

    @Override
    public List<List<String>> getConstituentWrittenTokenUUIDs() {
        return abstract2WrittenTokenUuids;
    }

    @Override
    public JsonObject getStateSerialization() {

        JsonObject currentTokenSetJson = CWrittenTokenSetJsonHelper.CAbstractWrittenTokenSet2JsonObj(currentTokenSet);
        JsonElement abstract2WrittenTokenUuidsJson = gson.toJsonTree(abstract2WrittenTokenUuids);

        JsonObject stateJson = new JsonObject();

        stateJson.add(STROKE_CURATOR_STATE_JSON_KEY, strokeCurator.getStateSerialization());
        stateJson.add(CURRENT_TOKEN_SET_JSON_KEY, currentTokenSetJson);
        stateJson.add(ABSTRACT_2_WRITTEN_TOKEN_UUIDS_JSON_KEY, abstract2WrittenTokenUuidsJson);

        return stateJson;
    }

    @Override
    public String getStateSerializationString() {
        return gson.toJson(getStateSerialization());
    }

    @Override
    public void injectSerializedState(JsonObject json) {
        // Inject state to stroke curator
        if ( !(json.has(STROKE_CURATOR_STATE_JSON_KEY) && json.get(STROKE_CURATOR_STATE_JSON_KEY).isJsonObject()) ) {
            throw new RuntimeException("Serialized state is missing field: " + STROKE_CURATOR_STATE_JSON_KEY);
        }
        strokeCurator.injectSerializedState(json.get(STROKE_CURATOR_STATE_JSON_KEY).getAsJsonObject());

        // Inject state to current token set
        if ( !(json.has(CURRENT_TOKEN_SET_JSON_KEY) && json.get(CURRENT_TOKEN_SET_JSON_KEY).isJsonObject()) ) {
            throw new RuntimeException("Serialized state is missing field: " + CURRENT_TOKEN_SET_JSON_KEY);
        }
//        currentTokenSet = gson.fromJson(json.get(CURRENT_TOKEN_SET_JSON_KEY).getAsJsonObject(), CWrittenTokenSetNoStroke.class);
        currentTokenSet = CWrittenTokenSetJsonHelper.jsonObj2CWrittenTokenSetNoStroke(json.get(CURRENT_TOKEN_SET_JSON_KEY).getAsJsonObject());

        // Inject state to abstract2WrittenTokenUuids
        if ( !(json.has(ABSTRACT_2_WRITTEN_TOKEN_UUIDS_JSON_KEY) && json.get(ABSTRACT_2_WRITTEN_TOKEN_UUIDS_JSON_KEY).isJsonArray()) ) {
            throw new RuntimeException("Serialized state is missing field: " + ABSTRACT_2_WRITTEN_TOKEN_UUIDS_JSON_KEY);
        }
        abstract2WrittenTokenUuids = gson.fromJson(json.get(ABSTRACT_2_WRITTEN_TOKEN_UUIDS_JSON_KEY).getAsJsonArray(),
                new TypeToken<LinkedList<List<String>>>() {}.getType());


    }

    private void pushStateStack(HandwritingEngineUserAction action) {
        stateStack.push(new HandwritingEngineState(action, getStateSerialization()));
    }

}