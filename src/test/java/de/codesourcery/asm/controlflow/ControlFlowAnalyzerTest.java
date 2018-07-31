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

    private void testMethodInClass(String className, String methodName, IBlock expected)
            throws IOException, AnalyzerException {
        ClassNode cn = CFGUtil.readClass(className, classPath);

        for (Object m : cn.methods) {
            MethodNode mn = (MethodNode) m;
            if (! CFGUtil.isConstructor(mn)) {
                ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
                final ControlFlowGraph graph = analyzer.analyze(className, mn);

                if (methodName.equals(mn.name)) {
                    assertTrue(CFGUtil.isIsomorphic(expected, graph.getStart()));
                }
            }
        }
    }

    @Test
    public void testTests() throws Exception {
        ClassNode cn = CFGUtil.readClass("Tests", classPath);

       for (Object m : cn.methods) {
            MethodNode mn = (MethodNode) m;
            if (! CFGUtil.isConstructor(mn)) {
                // TODO: Test structural properties
                ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
                final ControlFlowGraph graph = analyzer.analyze("Tests", mn);
                if ("emptyBlock".equals(mn.name)) {
                    assertEquals(5, graph.getAllNodes().size());
                }
                else if ("emptyBlockWithSideEffects".equals(mn.name)) {
                    assertEquals(5, graph.getAllNodes().size());
                } else if ("tripleAnd".equals(mn.name)) {
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

        testMethodInClass("IfTest", "f", builder.getStart());
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

        testMethodInClass("EmptyBlock1", "f", builder.getStart());
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

        testMethodInClass("DoubleNestedIf", "maxOfThree", builder.getStart());
    }

    /**
     * The following two graphs are not isomorphic.
     *
     * @throws Exception
     */
    @Test
    public void testIsIso() throws Exception {
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

        assertFalse(CFGUtil.isIsomorphic(builder.getStart(), builder_.getStart()));
    }
}
