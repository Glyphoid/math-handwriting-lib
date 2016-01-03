package me.scai.parsetree;

import static org.junit.Assert.*;

import me.scai.handwriting.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Test_TerminalSet {
    private static final String TEST_ROOT_DIR        = TestHelper.TEST_ROOT_DIR;
	private static final String RESOURCES_DIR        = "resources";
	private static final String RESOURCES_CONFIG_DIR = "config";
	private static final String TERMINALS_JSON_FILE  = "terminals.json";

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
		TerminalSet ts = new TerminalSet();

		final URL tsJsonFileUrl = Test_TerminalSet.class.getClassLoader().getResource(
                TEST_ROOT_DIR + File.separator + RESOURCES_DIR +
				File.separator + RESOURCES_CONFIG_DIR + 
				File.separator + TERMINALS_JSON_FILE);
		try {
			ts.readFromJsonAtUrl(tsJsonFileUrl);
		}
		catch (IOException exc) {
			fail("Failed due to IOException: " + exc.getMessage());
		}
		
		assertFalse(ts.token2TypesMap.isEmpty());
		assertFalse(ts.type2TokenMap.isEmpty());
		assertFalse(ts.token2TexNotationMap.isEmpty());
	}

	@Test
	public void testFactoryMethod() {
		final URL tsJsonFileUrl = Test_TerminalSet.class.getClassLoader().getResource(
				TEST_ROOT_DIR + File.separator + RESOURCES_DIR +
				File.separator + RESOURCES_CONFIG_DIR + 
				File.separator + TERMINALS_JSON_FILE);
		
		TerminalSet ts = null;
		try {
			ts = TerminalSet.createFromJsonAtUrl(tsJsonFileUrl);
		}
		catch (Exception exc) {
			fail("Failed due to Exception: " + exc.getMessage());
		}
		
		assertFalse(ts.token2TypesMap.isEmpty());
		assertFalse(ts.type2TokenMap.isEmpty());
		assertFalse(ts.token2TexNotationMap.isEmpty());
	}
}
