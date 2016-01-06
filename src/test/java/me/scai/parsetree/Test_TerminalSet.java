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
    /* Constants */
    private static final String TEST_ROOT_DIR        = TestHelper.TEST_ROOT_DIR;
	private static final String RESOURCES_DIR        = "resources";
	private static final String RESOURCES_CONFIG_DIR = "config";
	private static final String TERMINALS_JSON_FILE  = "terminals.json";

    /* Member variables */
    TerminalSet ts;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
        ts = new TerminalSet();

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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		assertFalse(ts.token2TypesMap.isEmpty());
		assertFalse(ts.type2TokenMap.isEmpty());
		assertFalse(ts.token2TexNotationMap.isEmpty());
	}

	@Test
	public void testFactoryMethod() {
		assertFalse(ts.token2TypesMap.isEmpty());
		assertFalse(ts.type2TokenMap.isEmpty());
		assertFalse(ts.token2TexNotationMap.isEmpty());
	}

    @Test
    public void testTokenName2TokenTypeMatch() {
        // WARNING: These assertions are dependent on the content of the terminal set config file

        // Negative cases
        assertFalse(ts.match("1", "COMPARATOR"));
        assertFalse(ts.match("2", "COMPARATOR"));

        assertFalse(ts.match("k", "TERMINAL(j)"));

        // Positive cases
        assertTrue(ts.match("gt", "COMPARATOR"));
        assertTrue(ts.match("gte", "COMPARATOR"));

        // Overloaded operators
        assertTrue(ts.match("=", "COMPARATOR"));
        assertTrue(ts.match("=", "ASSIGN_OP"));

        assertTrue(ts.match("V", "VARIABLE_SYMBOL"));
        assertTrue(ts.match("V", "LOGICAL_OR_OP"));

        assertTrue(ts.match("i", "TERMINAL(i)"));
    }
}
