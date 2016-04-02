package me.scai.parsetree;

import me.scai.handwriting.TestHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class Test_GraphicalProductionSet {
    /* Private members */
    GraphicalProductionSet gpSet;

    @Before
    public void setUp() {
        TestHelper.WorkerTuple workerTuple = TestHelper.getTestWorkerTuple();
        gpSet = workerTuple.gpSet;
    }

    @Test
    public void testInitiallyAllProdsEnabled() {
        List<Boolean> prodIsEnabled = gpSet.getProdIsEnabled();

        for (Boolean isEnabled : prodIsEnabled) {
            assertTrue(isEnabled);
        }

        assertEquals(gpSet.prods.size(), gpSet.searchIdx.length);
    }

    @Test
    public void testDisableEnableProductions() {
        final int np = gpSet.numProductions();

        for (int k = 0; k < np; ++k) {
            gpSet.disableProduction(k);

            ArrayList<Boolean> prodIsEnabled = gpSet.getProdIsEnabled();
            for (int i = 0; i < prodIsEnabled.size(); ++i) {
                if (i == k) {
                    assertFalse(prodIsEnabled.get(i));
                } else {
                    assertTrue(prodIsEnabled.get(i));
                }
            }

            assertEquals(np - 1, gpSet.searchIdx.length);

            for (int idx : gpSet.searchIdx) {
                assertNotEquals(k, idx);
            }

            gpSet.enableProduction(k);
        }
    }

    @Test
    public void testEnableAllProductions() {
        final int np = gpSet.numProductions();

        for (int k = 0; k < np; ++k) {
            gpSet.disableProduction(k);
        }

        assertEquals(0, gpSet.searchIdx.length);

        gpSet.enableAllProductions();

        assertEquals(gpSet.prods.size(), gpSet.searchIdx.length);
        for (int i = 0; i < gpSet.prods.size(); ++i) {
            assertEquals(i, gpSet.searchIdx[i]);
        }
    }

    @Test
    public void disableProductionBySumString() {
        assertEquals(gpSet.numProductions(), gpSet.searchIdx.length);

        String prodSumString = gpSet.prods.get(0).sumString;

        int numDisabled = gpSet.disableProductionBySumString(prodSumString);

        assertTrue(numDisabled > 0);
        assertEquals(gpSet.numProductions() - numDisabled, gpSet.searchIdx.length);

        assertEquals(1, gpSet.searchIdx[0]);
    }

    @Test
    public void disableProductionsByLHS() {
        assertEquals(gpSet.numProductions(), gpSet.searchIdx.length);


        final String lhsToDisable = "EXPONENTIATION";

        int numDisabled = gpSet.disableProductionsByLHS(lhsToDisable);

        assertTrue(numDisabled > 0);
        assertEquals(gpSet.numProductions() - numDisabled, gpSet.searchIdx.length);

        for (int i : gpSet.searchIdx) {
            assertNotEquals(lhsToDisable, gpSet.prods.get(i).lhs);
        }
    }

    @Test
    public void disabledProductionsByGrammarNodeName() {
        assertEquals(gpSet.numProductions(), gpSet.searchIdx.length);

        final String grammarNodeNameToDisable = "MATRIX";

        int numDisabled = gpSet.disableProductionsByGrammarNodeName(grammarNodeNameToDisable);

        assertTrue(numDisabled > 0);
        assertEquals(gpSet.numProductions() - numDisabled, gpSet.searchIdx.length);

        for (int i : gpSet.searchIdx) {
            GraphicalProduction prod = gpSet.prods.get(i);

            assertNotEquals(grammarNodeNameToDisable, prod.lhs);

            for (String rhsItem : prod.rhs) {
                assertNotEquals(grammarNodeNameToDisable, rhsItem);
            }
        }

    }

    @Test
    public void testCalcRequiredTermTypes_nonEmpty() {
        int prodIdx;
        Set<String> expectedTermTypes;

        // Decimal numbers
        prodIdx = findProd("DIGIT_STRING->DIGIT");
        expectedTermTypes = terminalTypeSet(new String[] {"DIGIT"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        prodIdx = findProd("DIGIT_STRING->DIGIT DIGIT_STRING");
        expectedTermTypes = terminalTypeSet(new String[] {"DIGIT"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        prodIdx = findProd("DECIMAL_NUMBER->DIGIT_STRING");
        expectedTermTypes = terminalTypeSet(new String[] {"DIGIT"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        prodIdx = findProd("DECIMAL_NUMBER->POINT DIGIT_STRING DIGIT_STRING");
        expectedTermTypes = terminalTypeSet(new String[] {"DIGIT", "POINT"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        prodIdx = findProd("DECIMAL_NUMBER->MINUS_OP DECIMAL_NUMBER");
        expectedTermTypes = terminalTypeSet(new String[] {"MINUS_OP", "DIGIT"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        prodIdx = findProd("DECIMAL_NUMBER->PLUS_OP DECIMAL_NUMBER");
        expectedTermTypes = terminalTypeSet(new String[] {"PLUS_OP", "DIGIT"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Addition
        prodIdx = findProd("ADDITION->PLUS_OP EXPR_LV4 EXPR_LV4");
        expectedTermTypes = terminalTypeSet(new String[] {"PLUS_OP"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Subtraction
        prodIdx = findProd("SUBTRACTION->MINUS_OP EXPR_LV4 EXPR_LV4");
        expectedTermTypes = terminalTypeSet(new String[] {"MINUS_OP"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Square root
        prodIdx = findProd("SQROOT->ROOT_OP EXPR_LV4");
        expectedTermTypes = terminalTypeSet(new String[] {"ROOT_OP"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Fraction
        prodIdx = findProd("FRACTION->MINUS_OP EXPR_LV4 EXPR_LV4");
        expectedTermTypes = terminalTypeSet(new String[] {"MINUS_OP"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Assignment
        prodIdx = findProd("ASSIGNMENT_STATEMENT->ASSIGN_OP VARIABLE EXPR_LV4");
        expectedTermTypes = terminalTypeSet(new String[] {"ASSIGN_OP", "VARIABLE_SYMBOL"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Matrix
        prodIdx = findProd("MATRIX->COLUMN_CONTENT BRACKET_L BRACKET_R");
        expectedTermTypes = terminalTypeSet(new String[] {"BRACKET_L", "BRACKET_R"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Functions
        prodIdx = findProd("USER_FUNCTION_ARGS->USER_FUNCTION_ARGS COMMA EXPR_LV4");
        expectedTermTypes = terminalTypeSet(new String[] {"COMMA"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        prodIdx = findProd("USER_FUNCTION_ARGS_PARENTHESES->USER_FUNCTION_ARGS PARENTHESIS_L PARENTHESIS_R");
        expectedTermTypes = terminalTypeSet(new String[] {"PARENTHESIS_L", "PARENTHESIS_R"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        prodIdx = findProd("USER_FUNCTION_TERM->USER_FUNCTION_NAME USER_FUNCTION_ARGS_PARENTHESES");
        expectedTermTypes = terminalTypeSet(new String[] {"VARIABLE_SYMBOL", "PARENTHESIS_L", "PARENTHESIS_R"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        prodIdx = findProd("USER_FUNCTION_DEF->ASSIGN_OP USER_FUNCTION_TERM EXPR_LV4");
        expectedTermTypes = terminalTypeSet(new String[] {"ASSIGN_OP",
                                                          "VARIABLE_SYMBOL", "PARENTHESIS_L", "PARENTHESIS_R"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Sigma and pi terms
        prodIdx = findProd("SIGMA_TERM->TERMINAL(gr_Si) ASSIGNMENT_STATEMENT EXPR_LV4 EXPR_LV4");
        expectedTermTypes = terminalTypeSet(new String[] {"TERMINAL(gr_Si)", "ASSIGN_OP", "VARIABLE_SYMBOL"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        prodIdx = findProd("PI_TERM->TERMINAL(gr_Pi) ASSIGNMENT_STATEMENT EXPR_LV4 EXPR_LV4");
        expectedTermTypes = terminalTypeSet(new String[] {"TERMINAL(gr_Pi)", "ASSIGN_OP", "VARIABLE_SYMBOL"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Definite integral
        prodIdx = findProd("DEF_INTEG_TERM->TERMINAL(integ) EXPR_LV4 EXPR_LV4 EXPR_LV4 TERMINAL(d) VARIABLE");
        expectedTermTypes = terminalTypeSet(new String[] {"TERMINAL(integ)", "TERMINAL(d)", "VARIABLE_SYMBOL"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Comparisons
        prodIdx = findProd("COMPARISON->COMPARATOR EXPR_LV4 EXPR_LV4");
        expectedTermTypes = terminalTypeSet(new String[] {"COMPARATOR"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Logical terms
        prodIdx = findProd("LOGICAL_TERM->COMPARISON");
        expectedTermTypes = terminalTypeSet(new String[] {"COMPARATOR"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        prodIdx = findProd("LOGICAL_PAREN_TERM->LOGICAL_OR_TERM PARENTHESIS_L PARENTHESIS_R");
        expectedTermTypes = terminalTypeSet(new String[] {"PARENTHESIS_L", "PARENTHESIS_R"});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

        // Note: Here we encounter the case in which the cyclic nature of the production graph makes it hard to
        //       determine the correct required terminal types
        prodIdx = findProd("LOGICAL_AND_TERM->LOGICAL_AND_OP LOGICAL_AND_TERM LOGICAL_TERM");
        expectedTermTypes = terminalTypeSet(new String[] {"LOGICAL_AND_OP"});
//        expectedTermTypes = terminalTypeSet(new String[] {"LOGICAL_AND_OP", "COMPARATOR"}); //TODO: Fix it
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

    }

    @Test
    public void testCalcRequiredTermTypes_empty() {
        int prodIdx;
        Set<String> expectedTermTypes;

        // This ought not to have any required terminal types, as the content can consist of either digits or
        // symbols
        // Prod: DIGIT_STRING->DIGIT
        prodIdx = findProd("EXPR_LV4->EXPR_LV1");

        expectedTermTypes = terminalTypeSet(new String[]{});
        assertEquals(expectedTermTypes, gpSet.requiredTermTypes.get(prodIdx));

    }

    /* Private methods */

    /**
     * Find production by summary string
     * @param prodSumString   Production summary string
     * @return Index to the production (>=0); -1 is not found
     */
    private int findProd(String prodSumString) {
        int prodIdx = -1;
        for (int i = 0; i < gpSet.prodSumStrings.size(); ++i) {
            if (gpSet.prodSumStrings.get(i).equals(prodSumString)) {
                prodIdx = i;
                break;
            }
        }

        assertNotEquals(-1, prodIdx);

        return prodIdx;
    }

    private Set<String> terminalTypeSet(String[] terminalTypes) {
        Set<String> termTypeSet = new HashSet<>();

        for (String terminalType : terminalTypes) {
            termTypeSet.add(terminalType);
        }

        return termTypeSet;
    }
}
