package me.scai.parsetree;

import me.scai.parsetree.evaluation.ParseTreeEvaluator;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class Test_NodeAnalyzer {
    private static final float tol = 1e-9f;

    @Test
    public void testNodeHasTwoTerminalChildren() {
        Node n1 = new Node("ROOT", "ROOT->gpA", new String[] {"gpA"});
        Node n2 = new Node("gpA", "gpA->gpB", new String[] {"gpB"});
        Node n3 = new Node("gpB", "gpB->gpC", new String[] {"gpC"});
        Node n4 = new Node("gpC", "gpC->terminalX terminalY", new String[] {"terminalX", "terminalY"});
        Node nt1 = new Node();
        Node nt2 = new Node();

        assertTrue(nt1.isTerminal());
        assertTrue(nt2.isTerminal());

        n1.setChild(0, n2);
        n2.setChild(0, n3);
        n3.setChild(0, n4);
        n4.setChild(0, nt1);
        n4.setChild(1, nt2);

        List<String> validLHS = NodeAnalyzer.getValidProductionLHS(n1);

        assertNotNull(validLHS);
        assertEquals(4, validLHS.size());
        assertEquals("ROOT", validLHS.get(0));
        assertEquals("gpA", validLHS.get(1));
        assertEquals("gpB", validLHS.get(2));
        assertEquals("gpC", validLHS.get(3));
    }

    @Test
    public void testNodeHasOneChild() {
        Node n1 = new Node("ROOT", "ROOT->gpA", new String[] {"gpA"});
        Node n2 = new Node("gpA", "gpA->gpB", new String[] {"gpB"});
        Node n3 = new Node("gpB", "gpB->gpC", new String[] {"gpC"});
        Node n4 = new Node("gpC", "gpC->terminalX", new String[] {"terminalX"});
        Node nt1 = new Node();

        assertTrue(nt1.isTerminal());

        n1.setChild(0, n2);
        n2.setChild(0, n3);
        n3.setChild(0, n4);
        n4.setChild(0, nt1);

        List<String> validLHS = NodeAnalyzer.getValidProductionLHS(n1);

        assertNotNull(validLHS);
        assertEquals(4, validLHS.size());
        assertEquals("ROOT", validLHS.get(0));
        assertEquals("gpA", validLHS.get(1));
        assertEquals("gpB", validLHS.get(2));
        assertEquals("gpC", validLHS.get(3));

    }

    @Test
    public void testOneValidGraphicalProduction() {
        Node n1 = new Node("gpA", "gpA->terminalA", new String[] {"terminalA"});
        Node nt1 = new Node();

        assertFalse(n1.isTerminal());
        assertTrue(nt1.isTerminal());

        n1.setChild(0, nt1);

        List<String> validLHS = NodeAnalyzer.getValidProductionLHS(n1);

        assertNotNull(validLHS);
        assertEquals(1, validLHS.size());
        assertEquals("gpA", validLHS.get(0));

    }

    @Test
    public void testCalcNodeBounds1() {
        Node n1 = new Node("gpA", "gpA->terminalA terminalB", new String[] {"terminalA", "terminalB"});
        Node nt1 = new Node();
        Node nt2 = new Node();

        nt1.setBounds(new float[] {0f, 0f, 1f, 1f});
        nt2.setBounds(new float[] {2f, 0f, 3f, 1f});

        assertTrue(nt1.isTerminal());
        assertTrue(nt2.isTerminal());
        n1.setChild(0, nt1);
        n1.setChild(1, nt2);

        float[] nodeBounds = NodeAnalyzer.calcNodeBounds(n1);
        assertArrayEquals(new float[] {0f, 0f, 3f, 1f}, nodeBounds, tol);
    }

    @Test
    public void testCalcNodeBounds2() {

        Node n1 = new Node("gpA", "gpA->terminalA gpB", new String[] {"terminalA", "gpB"});
        Node n2 = new Node("gpB", "gpB->gpC", new String[] {"grC"});
        Node n3 = new Node("gpC", "gpC->terminalB", new String[] {"terminalB"});

        Node ntA = new Node();
        Node ntB = new Node();

        ntA.setBounds(new float[] {0f, 0f, 1f, 1f});
        ntB.setBounds(new float[] {0.5f, 0.25f, 1.5f, 0.75f});

        assertFalse(n1.isTerminal());
        assertFalse(n2.isTerminal());
        assertFalse(n3.isTerminal());

        assertTrue(ntA.isTerminal());
        assertTrue(ntB.isTerminal());

        n1.setChild(0, ntA);
        n1.setChild(1, n2);
        n2.setChild(0, n3);
        n3.setChild(0, ntB);

        float[] nodeBounds = NodeAnalyzer.calcNodeBounds(n1);
        assertArrayEquals(new float[] {0f, 0f, 1.5f, 1f}, nodeBounds, tol);
    }




}
