package de.codesourcery.asm.util;

import de.codesourcery.asm.controlflow.ControlFlowGraph;
import de.codesourcery.asm.controlflow.DOTRenderer;
import de.codesourcery.asm.controlflow.Edge;
import de.codesourcery.asm.controlflow.IBlock;
import de.codesourcery.asm.misc.TestingUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;

/**
 * Provides common utility tools to create and extract information from CFGs.
 */
public class CFGUtil {
    /**
     * read the class at the given location
     * @param className name of the class
     * @param classPath path to search
     * @return a ClassNode object associated with the class
     * @throws IOException
     */
    public static ClassNode readClass(String className, File[] classPath) throws IOException {
        ClassReader reader = ASMUtil.createClassReader(className, classPath);
        ClassNode cn = new ClassNode();
        reader.accept(cn, 0);
        return cn;
    }

    /**
     * writes a graph of a method to the resources directory
     * @param className name of the class in which the method appears
     * @param graph CFG associated with the method
     * @throws FileNotFoundException
     */
    public static void generateDOTFile(String className, ControlFlowGraph graph) throws FileNotFoundException {
        String cfg = new DOTRenderer().render(graph);
        PrintWriter pw = new PrintWriter(new FileOutputStream(
                new File(TestingUtil.RESOURCES + "/dot/" + className + ".dot")));
        pw.write(cfg);
        pw.close();
    }

    /**
     * checks if this method is a constructor or not
     * @param m method node
     * @return
     */
    public static boolean isConstructor(MethodNode m) {
        return "<init>".equals(m.name);
    }

    /**
     * a helper function to return an outgoing edge with the specific metadata
     * @param bb basic block
     * @param metadata metadata to look for
     * @return block which shares an edge to bb with this metadata
     *         if no such edge exists returns null
     */
    private static IBlock getOutgoing(IBlock bb, String metadata) {
        for (Edge edge : bb.getEdges()) {
            if (edge.metaData != null && edge.metaData.equals(metadata)) {
                return edge.dst;
            }
        }

        return null;
    }

    /**
     * return the true outgoing edge from the basic block
     * @param bb basic block
     * @return outgoing true basic block
     */
    public static IBlock getTrueOutgoing(IBlock bb) {
        return getOutgoing(bb, "true");
    }

    /**
     * return the false outgoing edge from the basic block
     * @param bb basic block
     * @return outgoing false basic block
     */
    public static IBlock getFalseOutgoing(IBlock bb) {
        return getOutgoing(bb, "false");
    }

    /**
     * check whether the given two subtree in a CFG are isomorphic
     * @param bb1 first subtree
     * @param bb2 second subtree
     * @return true if the trees are isomorphic, false otherwise
     */
    public static boolean isIsomorphic(IBlock bb1, IBlock bb2) {
        if (bb1 == null && bb2 == null) {
            return true;
        } else if (bb1 == null || bb2 == null) {
            return false;
        } else if (bb1.getRegularPredecessorCount() != bb2.getRegularPredecessorCount()) {
            return false;
        } else if (bb1.getRegularSuccessorCount() != bb2.getRegularSuccessorCount()) {
            return false;
        } else if (bb1.getRegularSuccessorCount() == 1) { // only one outgoing edge
            return isIsomorphic(bb1.getRegularSuccessor(), bb2.getRegularSuccessor());
        } else { // two outgoing edges, true and false
            return isIsomorphic(getTrueOutgoing(bb1), getTrueOutgoing(bb2))
                && isIsomorphic(getFalseOutgoing(bb1), getFalseOutgoing(bb2));
        }
    }
}
