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
	public void test() {
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

}
