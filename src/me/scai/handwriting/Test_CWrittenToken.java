package me.scai.handwriting;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Test_CWrittenToken {
	static final float floatTol = 1e-6F;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() {
		String testJSON = "{\"numStrokes\":2,\"strokes\":{\"0\":{\"numPoints\":22,\"x\":[106,109,120,127,136,150,168,205,246,267,285,325,342,357,370,384,415,427,439,441,448,443],\"y\":[182,184,185,187,188,190,193,199,205,206,209,212,214,215,217,217,218,218,218,220,220,220]},\"1\":{\"numPoints\":23,\"x\":[284,282,279,278,276,276,276,276,276,276,277,277,279,279,280,280,280,282,282,282,281,281,281],\"y\":[75,75,82,89,98,110,124,151,164,181,196,212,242,257,271,281,292,307,310,314,323,328,329]}}}";
		
		CWrittenToken wt = new CWrittenToken(testJSON);
		assertEquals(wt.nStrokes(), 2);
		assertEquals(wt.getStroke(0).nPoints(), 22);
		assertEquals(wt.getStroke(1).nPoints(), 23);
		float [] normXs = wt.getStroke(0).getXs();
		assertEquals(normXs[0], 0.0F, floatTol);
		assertEquals(normXs[20], 1.0F, floatTol);
		
		assertEquals(wt.getStroke(-1), null);
		assertEquals(wt.getStroke(2), null);
	}
	
	@Test
	public void test2() {
		String testJSON = "{\"numStrokes\":2,\"strokes\":{\"0\":{\"numPoints\":40,\"x\":[291,291,289,289,287,284,280,277,272,269,262,256,250,248,243,240,238,236,236,236,236,236,236,237,238,240,248,253,260,269,277,286,296,307,314,323,329,336,338,339],\"y\":[130,132,134,138,141,147,151,157,161,164,169,172,175,176,179,181,183,184,185,186,187,188,189,189,190,190,191,191,191,192,192,192,192,192,192,192,192,192,192,192]},\"1\":{\"numPoints\":25,\"x\":[307,307,307,307,306,303,302,300,299,298,297,296,296,296,296,296,296,296,296,296,296,296,296,296,296],\"y\":[123,126,130,136,143,151,161,171,179,185,189,194,198,201,204,207,208,210,211,212,214,216,217,218,219]}}}";
		
		CWrittenToken wt = new CWrittenToken(testJSON);
		assertEquals(wt.nStrokes(), 2);
		assertEquals(wt.getStroke(0).nPoints(), 40);
		assertEquals(wt.getStroke(1).nPoints(), 25);
	}
	
	@Test
	public void testZeroHeightToken() {
		String testJSON = "{\"numStrokes\":1,\"strokes\":{\"0\":{\"numPoints\":19,\"x\":[274,274,276,279,282,288,294,302,316,328,340,348,357,363,370,375,378,379,380],\"y\":[237,237,237,237,237,237,237,237,237,237,237,237,237,237,237,237,237,237,237]}}}";
		
		CWrittenToken wt = new CWrittenToken(testJSON);
		assertEquals(wt.nStrokes(), 1);
		assertEquals(wt.getStroke(0).nPoints(), 19);
		
		final int npPerStroke = 20;
		final int maxNumStrokes = 4;
		
		float [] sdv = wt.getSDV(npPerStroke, maxNumStrokes, null);
		assertEquals(sdv.length, (npPerStroke - 1) * maxNumStrokes);
		
		for (int i = 0; i < sdv.length; ++i) {
			assertEquals(sdv[i], 0.0f, floatTol);
		}
	}
	
	@Test
	public void testZeroWidthToken() {
		String testJSON = "{\"numStrokes\":1,\"strokes\":{\"0\":{\"numPoints\":19,\"x\":[237,237,237,237,237,237,237,237,237,237,237,237,237,237,237,237,237,237,237],\"y\":[274,274,276,279,282,288,294,302,316,328,340,348,357,363,370,375,378,379,380]}}}";
		
		CWrittenToken wt = new CWrittenToken(testJSON);
		assertEquals(wt.nStrokes(), 1);
		assertEquals(wt.getStroke(0).nPoints(), 19);
		
		final int npPerStroke = 20;
		final int maxNumStrokes = 4;
		
		float [] sdv = wt.getSDV(npPerStroke, maxNumStrokes, null);
		assertEquals(sdv.length, (npPerStroke - 1) * maxNumStrokes);
		
		for (int i = 0; i < npPerStroke - 1; ++i) {
			assertEquals(sdv[i], Math.PI / 2.0f, floatTol);
		}
		for (int i = npPerStroke; i < sdv.length; ++i) {
			assertEquals(sdv[i], 0.0f, floatTol);
		}
	}

}
