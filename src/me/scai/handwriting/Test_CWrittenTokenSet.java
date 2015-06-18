package me.scai.handwriting;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by scai on 5/23/2015.
 */
public class Test_CWrittenTokenSet {
    private static final float floatTol = 1e-6f;

    @Test
    public void TestWrittenTokenSet() {
        String testJSON = "{\"numStrokes\":2,\"strokes\":{\"0\":{\"numPoints\":22,\"x\":[106,109,120,127,136,150,168,205,246,267,285,325,342,357,370,384,415,427,439,441,448,443],\"y\":[182,184,185,187,188,190,193,199,205,206,209,212,214,215,217,217,218,218,218,220,220,220]},\"1\":{\"numPoints\":23,\"x\":[284,282,279,278,276,276,276,276,276,276,277,277,279,279,280,280,280,282,282,282,281,281,281],\"y\":[75,75,82,89,98,110,124,151,164,181,196,212,242,257,271,281,292,307,310,314,323,328,329]}}}";

        CWrittenToken wt = new CWrittenToken(testJSON);
        CWrittenTokenSet wtSet = new CWrittenTokenSet();
        wtSet.addToken(wt);

        assertEquals(wtSet.getNumTokens(), 1);
        assertEquals(wtSet.getNumStrokes(), 2);
    }

    @Test
    public void TestSetTokenBounds() {
        String testJSON = "{\"numStrokes\":2,\"strokes\":{\"0\":{\"numPoints\":22,\"x\":[106,109,120,127,136,150,168,205,246,267,285,325,342,357,370,384,415,427,439,441,448,443],\"y\":[182,184,185,187,188,190,193,199,205,206,209,212,214,215,217,217,218,218,218,220,220,220]},\"1\":{\"numPoints\":23,\"x\":[284,282,279,278,276,276,276,276,276,276,277,277,279,279,280,280,280,282,282,282,281,281,281],\"y\":[75,75,82,89,98,110,124,151,164,181,196,212,242,257,271,281,292,307,310,314,323,328,329]}}}";

        CWrittenToken wt = new CWrittenToken(testJSON);
        CWrittenTokenSet wtSet = new CWrittenTokenSet();

        wtSet.addToken(wt);

        final float[] tokenBounds = wtSet.getTokenBounds(0);
        float[] knownBounds = new float[] {106.0f, 75.0f, 448.0f, 329.0f};
        for (int k = 0; k < knownBounds.length; ++k) {
            assertEquals(knownBounds[k], tokenBounds[k], floatTol);
        }

        for (int i = 0; i < 2; ++i) {
            final float[] newBounds = new float[] {knownBounds[0] + 1.5f * i,
                                                   knownBounds[1] + 1.5f * i,
                                                   knownBounds[2] + 1.5f * i,
                                                   knownBounds[3] + 1.5f * i};

            wtSet.setTokenBounds(0, newBounds);
            final float[] newRetrievedBounds = wtSet.getTokenBounds(0);

            for (int k = 0; k < knownBounds.length; ++k) {
                assertEquals(newBounds[k], newRetrievedBounds[k], floatTol);
            }
        }
    }
}
