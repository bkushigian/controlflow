package de.codesourcery.asm.controlflow;

import de.codesourcery.asm.misc.TestingUtil;
import de.codesourcery.asm.util.CFGUtil;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractTest {
    public static File[] classPath = {new File(TestingUtil.TEST_CLASSES)};

    /**
     * get control flow graph of a method with a given name from a classfile in the classPath
     * @param className name of the classfile in the classPath
     * @param methodName name of the method in the classfile
     * @return control flow graph of the method
     * @throws IOException
     * @throws AnalyzerException
     */
    public ControlFlowGraph getMethodInClass(String className, String methodName)
            throws IOException, AnalyzerException {
        ClassNode cn = CFGUtil.readClass(className, classPath);

        for (Object m : cn.methods) {
            MethodNode mn = (MethodNode) m;

            if (! CFGUtil.isConstructor(mn)) {
                ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
                final ControlFlowGraph graph = analyzer.analyze(className, mn);

                if (methodName.equals(mn.name)) {
                    return graph;
                }
            }
        }

        return null;
    }

    /**
     * assert that the method CFG and the CFG represented by startBlock are isomorphic
     * @throws IOException
     * @throws AnalyzerException
     */
    public void expectedIsomorphic(String className, String methodName, IBlock startBlock)
            throws IOException, AnalyzerException {
        ControlFlowGraph methodCFG = getMethodInClass(className, methodName);
        CFGUtil.generateDOTFile(className + ":" + methodName, methodCFG);

        assertTrue(CFGUtil.isIsomorphic(startBlock, methodCFG.getStart()));
    }

    /**
     * assert that the method CFG and the CFG represented by startBlock are non isomorphic
     * @throws IOException
     * @throws AnalyzerException
     */
    public void expectedNonIsomorphic(String className, String methodName, IBlock startBlock)
            throws IOException, AnalyzerException {
        ControlFlowGraph methodCFG = getMethodInClass(className, methodName);
        CFGUtil.generateDOTFile(className + ":" + methodName, methodCFG);

        assertFalse(CFGUtil.isIsomorphic(startBlock, methodCFG.getStart()));
    }
}
