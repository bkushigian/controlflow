package de.codesourcery.asm.controlflow;

import de.codesourcery.asm.misc.TestingUtil;
import de.codesourcery.asm.util.CFGUtil;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import javax.naming.ldap.Control;
import java.io.*;

import static org.junit.Assert.*;

public class ControlFlowAnalyzerTest {
    private static File[] classPath = {new File(TestingUtil.TEST_CLASSES)};

    private void testMethodInClass(String className, String methodName, IBlock startBlock, boolean expectedEqual)
            throws IOException, AnalyzerException {
        ClassNode cn = CFGUtil.readClass(className, classPath);

        for (Object m : cn.methods) {
            MethodNode mn = (MethodNode) m;
            if (! CFGUtil.isConstructor(mn)) {
                ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
                final ControlFlowGraph graph = analyzer.analyze(className, mn);

                if (methodName.equals(mn.name)) {
                    CFGUtil.generateDOTFile(className + ":" + methodName, graph);

                    if (expectedEqual) {
                        assertTrue(CFGUtil.isIsomorphic(startBlock, graph.getStart()));
                    } else {
                        assertFalse(CFGUtil.isIsomorphic(startBlock, graph.getStart()));
                    }
                }
            }
        }
    }

    private void expectedEqual(String className, String methodName, IBlock startBlock)
            throws IOException, AnalyzerException {
        testMethodInClass(className, methodName, startBlock, true);
    }

    private void expectedUnequal(String className, String methodName, IBlock startBlock)
            throws IOException, AnalyzerException {
        testMethodInClass(className, methodName, startBlock, false);
    }

    @Test
    public void testTestsEmptyBlock() throws Exception {
        CFGBuilder builder = new CFGBuilder();
        IBlock bb1 = builder.makeBlock("bb1");
        IBlock bb2 = builder.makeBlock("bb2");
        IBlock bb3 = builder.makeBlock("bb3");

        builder.addEdge(builder.getStart(), bb1);

        builder.addTrueEdge(bb1, bb2);
        builder.addFalseEdge(bb1, bb2);

        builder.addTrueEdge(bb2, bb3);
        builder.addFalseEdge(bb2, bb3);

        builder.addEdge(bb3, builder.getEnd());

        expectedEqual("Tests", "emptyBlock", builder.getStart());
    }

    @Test
    public void testTestsEmptyBlockWithSideEffects() throws Exception {
        CFGBuilder builder = new CFGBuilder();
        IBlock bb1 = builder.makeBlock("bb1");
        IBlock bb2 = builder.makeBlock("bb2");
        IBlock bb3 = builder.makeBlock("bb3");

        builder.addEdge(builder.getStart(), bb1);

        builder.addTrueEdge(bb1, bb2);
        builder.addFalseEdge(bb1, bb2);

        builder.addTrueEdge(bb2, bb3);
        builder.addFalseEdge(bb2, bb3);

        builder.addEdge(bb3, builder.getEnd());

        expectedEqual("Tests", "emptyBlockWithSideEffects", builder.getStart());

        ClassNode cn = CFGUtil.readClass("Tests", classPath);

       for (Object m : cn.methods) {
            MethodNode mn = (MethodNode) m;
            if (! CFGUtil.isConstructor(mn)) {
                // TODO: Test structural properties
                ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
                final ControlFlowGraph graph = analyzer.analyze("Tests", mn);
                if ("tripleAnd".equals(mn.name)) {
                    CFGUtil.generateDOTFile("Tests" + ":" + "tripleAnd", graph);
                    assertEquals(9, graph.getAllNodes().size());
                }
            }
        }
    }

    @Test
    public void testIf() throws Exception {
        CFGBuilder builder = new CFGBuilder();
        IBlock bb1 = builder.makeBlock("bb1");
        IBlock bb2 = builder.makeBlock("bb2");
        IBlock bb3 = builder.makeBlock("bb3");

        builder.addEdge(builder.getStart(), bb1);

        builder.addTrueEdge(bb1, bb2);
        builder.addFalseEdge(bb1, bb3);

        builder.addEdge(bb2, builder.getEnd());
        builder.addEdge(bb3, builder.getEnd());

        expectedEqual("IfTest", "f", builder.getStart());
    }

    @Test
    public void testEmptyBlock1() throws Exception {
        CFGBuilder builder = new CFGBuilder();
        IBlock bb1 = builder.makeBlock("bb1");
        IBlock bb2 = builder.makeBlock("bb2");
        IBlock bb3 = builder.makeBlock("bb3");
        IBlock bb4 = builder.makeBlock("bb4");

        builder.addEdge(builder.getStart(), bb1);

        builder.addTrueEdge(bb1, bb2);
        builder.addFalseEdge(bb1, bb3);

        builder.addEdge(bb2, builder.getEnd());
        builder.addEdge(bb3, bb4);
        builder.addEdge(bb4, builder.getEnd());

        expectedEqual("EmptyBlock1", "f", builder.getStart());
    }

    @Test
    public void testTriangle127() throws Exception {
        String className = "Triangle127";
        ClassNode cn = CFGUtil.readClass(className, classPath);

        for (Object m : cn.methods) {
            MethodNode mn = (MethodNode) m;
            if (!CFGUtil.isConstructor(mn)) {
                ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
                final ControlFlowGraph graph = analyzer.analyze(className, mn);

                if ("classify".equals(mn.name)) {
                    CFGUtil.generateDOTFile(className, graph);

                    assertEquals(1, graph.getStart().getRegularSuccessorCount());
                    assertEquals(7, graph.getEnd().getRegularPredecessorCount());
                }
            }
        }
    }

    @Test
    public void testDoubleNestedIf() throws Exception {
        CFGBuilder builder = new CFGBuilder();
        IBlock bb1 = builder.makeBlock("bb1");
        IBlock bb2 = builder.makeBlock("bb2");
        IBlock bb3 = builder.makeBlock("bb3");
        IBlock bb4 = builder.makeBlock("bb4");
        IBlock bb5 = builder.makeBlock("bb5");
        IBlock bb6 = builder.makeBlock("bb6");
        IBlock bb7 = builder.makeBlock("bb7");

        builder.addEdge(builder.getStart(), bb1);

        builder.addTrueEdge(bb1, bb2);
        builder.addFalseEdge(bb1, bb3);

        builder.addTrueEdge(bb2, bb4);
        builder.addFalseEdge(bb2, bb5);

        builder.addTrueEdge(bb3, bb6);
        builder.addFalseEdge(bb3, bb7);

        builder.addEdge(bb4, builder.getEnd());
        builder.addEdge(bb5, builder.getEnd());
        builder.addEdge(bb6, builder.getEnd());
        builder.addEdge(bb7, builder.getEnd());

        expectedEqual("DoubleNestedIf", "maxOfThree", builder.getStart());
    }

    @Test
    public void testNotIso1() {
        CFGBuilder builder = new CFGBuilder();
        CFGBuilder builder_ = new CFGBuilder();
        IBlock a = builder.makeBlock("a"),
               b = builder.makeBlock("b"),
               c = builder.makeBlock("c"),
               d = builder.makeBlock("d"),
               e = builder.makeBlock("e"),
               f = builder.makeBlock("f"),
               g = builder.makeBlock("g"),
               h = builder.makeBlock("h"),
               i = builder.makeBlock("i"),
               j = builder.makeBlock("j"),
               a_ = builder_.makeBlock("a_"),
               b_ = builder_.makeBlock("_b"),
               c_ = builder_.makeBlock("_c"),
               d_ = builder_.makeBlock("_d"),
               e_ = builder_.makeBlock("_e"),
               f_ = builder_.makeBlock("_f"),
               g_ = builder.makeBlock("g_"),
               h_ = builder.makeBlock("_h"),
               i_ = builder.makeBlock("_i"),
               j_ = builder.makeBlock("_j") ;

        builder.addEdge(builder.getStart(), a);
        builder_.addEdge(builder_.getStart(), a_);

        // Graph G
        builder.addTrueEdge (a, b);
        builder.addFalseEdge(a, c);
        builder.addTrueEdge (b, d);
        builder.addFalseEdge(b, e);
        builder.addTrueEdge (c, f);
        builder.addFalseEdge(c, g);
        builder.addEdge     (d, h);
        builder.addEdge     (e, h);
        builder.addEdge     (f, i);
        builder.addEdge     (g, i);
        builder.addEdge     (h, j);
        builder.addEdge     (i, j);
        builder.addEdge     (j, builder.getEnd());

        // Graph G_
        builder_.addTrueEdge (a_, b_);
        builder_.addFalseEdge(a_, c_);
        builder_.addTrueEdge (b_, d_);
        builder_.addFalseEdge(b_, e_);
        builder_.addTrueEdge (c_, f_);
        builder_.addFalseEdge(c_, g_);
        builder_.addEdge     (d_, i_);
        builder_.addEdge     (e_, h_);
        builder_.addEdge     (f_, h_);
        builder_.addEdge     (g_, i_);
        builder_.addEdge     (h_, j_);
        builder_.addEdge     (i_, j_);
        builder_.addEdge     (j_, builder_.getEnd());

        assertFalse(CFGUtil.isIsomorphic(builder.getStart(), builder_.getStart()));
    }

    @Test
    public void testNotIso2() {
        CFGBuilder builder = new CFGBuilder();
        CFGBuilder builder_ = new CFGBuilder();
        IBlock a = builder.makeBlock("a"),
               b = builder.makeBlock("b"),
               c = builder.makeBlock("c"),
               d = builder.makeBlock("d"),
               a_ = builder_.makeBlock("a_"),
               b_ = builder_.makeBlock("_b"),
               c_ = builder_.makeBlock("_c"),
               d_ = builder_.makeBlock("_d"),
               e_ = builder_.makeBlock("_e"),
               f_ = builder_.makeBlock("_f");

        builder.addEdge(builder.getStart(), a);
        builder_.addEdge(builder_.getStart(), a_);

        // Graph G
        builder.addTrueEdge (a, b);
        builder.addFalseEdge(a, d);
        builder.addEdge     (b, c);
        builder.addEdge     (d, c);
        builder.addEdge     (c, builder.getEnd());

        // Graph G_
        builder_.addTrueEdge (a_, b_);
        builder_.addFalseEdge(a_, e_);
        builder_.addEdge     (b_, c_);
        builder_.addEdge     (e_, f_);
        builder_.addEdge     (c_, d_);
        builder_.addEdge     (f_, d_);
        builder_.addEdge     (d_, builder_.getEnd());

        assertFalse(CFGUtil.isIsomorphic(builder.getStart(), builder_.getStart()));
    }

    @Test
    public void testIsoBackEdge1() {
        CFGBuilder builder = new CFGBuilder();
        CFGBuilder builder_ = new CFGBuilder();
        IBlock a = builder.makeBlock("a"),
               b = builder.makeBlock("b"),
               c = builder.makeBlock("c"),
               d = builder.makeBlock("d"),
               e = builder.makeBlock("e"),
               f = builder.makeBlock("f"),
               g = builder.makeBlock("g"),
               h = builder.makeBlock("h"),
               a_ = builder_.makeBlock("a_"),
               b_ = builder_.makeBlock("_b"),
               c_ = builder_.makeBlock("_c"),
               d_ = builder_.makeBlock("_d"),
               e_ = builder_.makeBlock("_e"),
               f_ = builder_.makeBlock("_f"),
               g_ = builder_.makeBlock("_g"),
               h_ = builder_.makeBlock("_h");

        builder.addEdge(builder.getStart(), a);
        builder_.addEdge(builder_.getStart(), a_);

        // Graph G
        builder.addTrueEdge (a, b);
        builder.addFalseEdge(a, h);
        builder.addEdge     (b, c);
        builder.addTrueEdge (c, d);
        builder.addFalseEdge(c, e);
        builder.addEdge     (d, f);
        builder.addEdge     (e, f);
        builder.addTrueEdge (f, c);
        builder.addFalseEdge(f, g);
        builder.addEdge     (h, g);
        builder.addEdge     (g, builder.getEnd());

        // Graph G_
        builder.addTrueEdge (a_, b_);
        builder.addFalseEdge(a_, h_);
        builder.addEdge     (b_, c_);
        builder.addTrueEdge (c_, d_);
        builder.addFalseEdge(c_, e_);
        builder.addEdge     (d_, f_);
        builder.addEdge     (e_, f_);
        builder.addTrueEdge (f_, c_);
        builder.addFalseEdge(f_, g_);
        builder.addEdge     (h_, g_);
        builder.addEdge     (g_, builder.getEnd());

        assertTrue(CFGUtil.isIsomorphic(builder.getStart(), builder_.getStart()));
    }

    @Test
    public void testNonIso3() throws Exception {
        CFGBuilder builder = new CFGBuilder();
        IBlock a = builder.makeBlock("a"),
               b = builder.makeBlock("b"),
               c = builder.makeBlock("c"),
               d = builder.makeBlock("d"),
               e = builder.makeBlock("e"),
               f = builder.makeBlock("f"),
               g = builder.makeBlock("g"),
               h = builder.makeBlock("h");

        builder.addEdge(builder.getStart(), a);

        // Graph G
        builder.addTrueEdge (a, b);
        builder.addFalseEdge(a, h);
        builder.addEdge     (b, c);
        builder.addTrueEdge (c, d);
        builder.addFalseEdge(c, e);
        builder.addEdge     (d, f);
        builder.addEdge     (e, f);
        builder.addTrueEdge (f, c);
        builder.addFalseEdge(f, g);
        builder.addEdge     (h, g);
        builder.addEdge     (g, builder.getEnd());

        expectedUnequal("IfTest", "f", builder.getStart());
    }
}
