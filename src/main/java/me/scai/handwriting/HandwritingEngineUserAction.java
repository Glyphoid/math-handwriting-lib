package me.scai.handwriting;

public enum HandwritingEngineUserAction {
    AddStroke,
    RemoveLastToken,
    RemoveToken,
    RemoveTokens,
    MoveToken,
    MoveTokens,
    MergeStrokesAsToken,
    ForceSetTokenName,
    ClearStrokes,
    ParseTokenSubset;

//    private String commandString;     // String for HTTP requests
//
//    HandwritingEngineUserAction(String commandString) {
//        this.commandString  = commandString;
//    }
//
//    @Override
//    public String toString() {
//        return this.commandString;
//    }
}
