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
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

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

    private static boolean search(IBlock bb, LabelCounter counter, Map<IBlock, Integer> labels) {
        // do not label a basic block twice
        if (labels.containsKey(bb)) {
            return true;
        } else {
            // label this vertex
            labels.put(bb, counter.getUniqueLabel());

            // check the number of successors of this vertex
            if (bb.getRegularSuccessorCount() == 2) {
                return search(getTrueOutgoing(bb), counter, labels) && search(getFalseOutgoing(bb), counter, labels);
            } else if (bb.getRegularSuccessorCount() == 1) {
                return search(bb.getRegularSuccessor(), counter, labels);
            } else {
                return true; // this is the end block, we can stop searching
            }
        }
    }

    private static class Pair<K, V> {
        public final K first;
        public final V second;

        Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }
    }

    private static class LabelCounter {
        private int counter;

        public LabelCounter() {
            counter = 1;
        }

        public int getUniqueLabel() {
            return counter++;
        }
    }

    private static void printLabels(Map<IBlock, Integer> labels) {
        for (IBlock bb : labels.keySet()) {
            System.out.println(bb.getId() + " : " + labels.get(bb));
        }
    }

    /**
     * check whether the given two graphs CFG are isomorphic
     * @param bb1 first
     * @param bb2 second
     * @return true if the graphs are isomorphic, false otherwise
     */
    public static boolean isIsomorphic(IBlock bb1, IBlock bb2) {
        Map<IBlock, Integer> labelBB1 = new HashMap<>();
        Map<IBlock, Integer> labelBB2 = new HashMap<>();

        // label first graph
        search(bb1, new LabelCounter(), labelBB1);

        // label second graph
        search(bb2, new LabelCounter(), labelBB2);

        printLabels(labelBB1);
        printLabels(labelBB2);

        // compare the labels on the two graphs
        // labels on the corresponding nodes in the graphs should match
        Queue<Pair<IBlock, IBlock>> queue = new ArrayDeque<>();
        queue.add(new Pair<>(bb1, bb2));

        while (! queue.isEmpty()) {
            Pair<IBlock, IBlock> p = queue.poll();

            if (p.first != null && p.second != null) {
            System.out.println("cmp: " + p.first.getId() + ", " + p.second.getId());
            System.out.println("label: " + labelBB1.get(p.first) + ", " + labelBB2.get(p.second));
                }

            // end of the graph, return true
            if ((p.first == null && p.second == null) ||
                (p.first.getRegularSuccessorCount() == 0 && p.second.getRegularSuccessorCount() == 0)) {
                return true;
            } else if (! labelBB1.get(p.first).equals(labelBB2.get(p.second))) {
                return false;
            } if (p.first.getRegularSuccessorCount() != p.second.getRegularSuccessorCount()) {
                return false;
            } else if (p.first.getRegularSuccessorCount() == 1) {
                queue.add(new Pair<>(p.first.getRegularSuccessor(), p.second.getRegularSuccessor()));
            } else if (p.first.getRegularSuccessorCount() == 2) {
                queue.add(new Pair<>(getTrueOutgoing(p.first), getTrueOutgoing(p.second)));
                queue.add(new Pair<>(getFalseOutgoing(p.first), getFalseOutgoing(p.second)));
            }
        }

        return false;
    }
}
