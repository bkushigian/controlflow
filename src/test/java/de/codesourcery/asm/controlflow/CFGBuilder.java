package de.codesourcery.asm.controlflow;

import java.util.*;

/**
 * Simplies creating CFG in code.
 */
public class CFGBuilder {
    IBlock startNode;
    IBlock endNode;
    private Set<IBlock> allNodes;

    public CFGBuilder() {
        startNode = makeBlock("start_bb");
        endNode = makeBlock("end_bb");
        allNodes = new HashSet<>();
    }

    /**
     * @return start node of the CFG
     */
    public IBlock getStart() {
        return startNode;
    }

    /**
     * @return end node of the CFG
     */
    public IBlock getEnd() {
        return endNode;
    }

    /**
     * @param id id for the basic block
     * @return basic block with the given id
     */
    public IBlock makeBlock(String id) {
        IBlock bb = new Block();
        bb.setId(id);
        return bb;
    }

    /**
     * add a true outgoing edge from bb to trueBB
     * @param bb basic block
     * @param trueBB true outgoing edge from bb
     */
    public void addTrueEdge(IBlock bb, IBlock trueBB) {
        assert bb != trueBB;

        bb.addSuccessor(trueBB, Edge.EdgeType.REGULAR, "true");
        trueBB.addRegularPredecessor(bb);

        allNodes.add(bb);
        allNodes.add(trueBB);
    }

    /**
     * add a false outgoing edge from bb to trueBB
     * @param bb basic block
     * @param falseBB false outgoing edge from bb
     */
    public void addFalseEdge(IBlock bb, IBlock falseBB) {
        assert bb != falseBB;

        bb.addSuccessor(falseBB, Edge.EdgeType.REGULAR, "false");
        falseBB.addRegularPredecessor(bb);

        allNodes.add(bb);
        allNodes.add(falseBB);
    }

    /**
     * add a regular outgoing edge from bb to outBB
     * @param bb basic block
     * @param outBB outgoing edge from bb
     */
    public void addEdge(IBlock bb, IBlock outBB) {
        assert bb != outBB;

        bb.addRegularSuccessor(outBB);
        outBB.addRegularPredecessor(bb);

        allNodes.add(bb);
        allNodes.add(outBB);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");

        for (IBlock bb : allNodes) {
            for (Edge edge : bb.getEdges()) {
                if (bb != edge.dst) {
                    sb.append("    " + bb.getId() + " -> " + edge.dst.getId() + "\n");
                }
            }
        }

        sb.append("}\n");
        return sb.toString();
    }
}
