package me.scai.parsetree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Test_ParseTreeMathTexifier {

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
	public void testGetTexFunctionName() {
		String item = "GET(n0)";
		
		String funcName = ParseTreeMathTexifier.getTexFunctionName(item);
		
		assertEquals(funcName, "GET");
	}
	
	@Test
	public void testGetTexFunctionArgIndices0() {
		String item = "GETBAR(n1, n3)";
		
		int [] argIndices = ParseTreeMathTexifier.getTexFunctionArgIndices(item);
		int [] trueArgIndices = {1, 3};
		
		assertArrayEquals(argIndices, trueArgIndices);
	}
	
	@Test
	public void testGetTexFunctionArgIndices1() {
		String item = "GET_FOO(n88)";
		
		int [] argIndices = ParseTreeMathTexifier.getTexFunctionArgIndices(item);
		int [] trueArgIndices = {88};
		
		assertArrayEquals(argIndices, trueArgIndices);
	}

}
