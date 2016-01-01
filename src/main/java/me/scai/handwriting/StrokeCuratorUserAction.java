package me.scai.handwriting;

import me.scai.plato.state.PlatoState;

public enum StrokeCuratorUserAction {
//    AddStroke("add-stroke"),
//    MoveToken("move-token"),
//    RemoveLastToken("remove-last-token"),
//    RemoveToken("remove-token"),
//    MergeStrokesAsToken("merge-strokes-as-token"),
//    ForceSetTokenName("force-set-token-name"),
//    ClearStrokes("clear"),
//    GetGraphicalProductions("get-graphical-productions");
    AddStroke,
    MoveToken,
    RemoveLastToken,
    RemoveToken,
    MergeStrokesAsToken,
    ForceSetTokenName,
    ClearStrokes,
    GetGraphicalProductions;

//    private String commandString;     // String for HTTP requests

//    StrokeCuratorUserAction(String commandString) {
//        this.commandString  = commandString;
//    }

//    @Override
//    public String toString() {
//        return this.commandString;
//    }

//    public static StrokeCuratorUserAction fromString(String s) {
//        if (s.equals("clear")) {
//            return ClearStrokes;
//        } else {
//            if (s.charAt(0) >= 'A' && s.charAt(0) <= 'Z') {
//                return StrokeCuratorUserAction.valueOf(s);
//            } else {
//                String as = "";
//
//                String[] items = s.split("-");
//                for (String item : items) {
//                    String ts = "";
//                    ts += item.charAt(0);
//
//                    as += ts.toUpperCase() + item.substring(1, item.length());
//                }
//
//                return StrokeCuratorUserAction.valueOf(as);
//            }
//
//        }
//    }
}
