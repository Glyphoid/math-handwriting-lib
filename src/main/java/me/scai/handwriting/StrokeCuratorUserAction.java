package me.scai.handwriting;

public enum StrokeCuratorUserAction {
    AddStroke("add-stroke"),
    MoveToken("move-token"),
//    MoveTokens("move-tokens"),
    RemoveLastToken("remove-last-token"),
    RemoveToken("remove-token"),
    MergeStrokesAsToken("merge-strokes-as-token"),
    ForceSetTokenName("force-set-token-name"),
    ClearStrokes("clear"),
    GetGraphicalProductions("get-graphical-productions");

    private String commandString;     // String for HTTP requests

    StrokeCuratorUserAction(String commandString) {
        this.commandString  = commandString;
    }

    @Override
    public String toString() {
        return this.commandString;
    }
}
