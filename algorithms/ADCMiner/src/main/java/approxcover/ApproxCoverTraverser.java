package approxcover;

import ch.javasoft.bitset.LongBitSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class ApproxCoverTraverser {
    public static long counter=0,counterOps=0;
    private final long minCoverTarget;

    private List<HyperEdge> edges;

    private Collection<ApproxCoverNode> ApproxCoverNodes;
    private Collection<LongBitSet> ApproxCovers;

    private long nodeCount = 0;

    ApproxCoverTraverser(int nEle, long minCover, LongBitSet[] mutexMap) {
        minCoverTarget = minCover;
        ApproxCoverNode.configure(nEle, mutexMap);
    }

    public List<LongBitSet> initiate(List<HyperEdge> _edges) {
        edges = new ArrayList<>(_edges);
        ApproxCoverNodes = walkDownFromRoot();
        return getNontrivialMinCovers();
    }

    private Collection<ApproxCoverNode> walkDownFromRoot() {
        ApproxCoverNode rootNode = new ApproxCoverNode(minCoverTarget, edges);
        Collection<ApproxCoverNode> newCoverNodes;
        newCoverNodes = new ArrayList<>();
        walkDown(newCoverNodes, rootNode);
        System.out.println("  [ADC] final node size: " + nodeCount);
        return newCoverNodes;
    }

    private void walkDown(Collection<ApproxCoverNode> newCoverNodes, ApproxCoverNode nd) {
        counter+=1;
        if (nd.isApproxCover() && nd.isMinimal()) {
            nd.addTo(newCoverNodes);
            return;
        }
        if (nd.canHitUncov.isEmpty()) return;

        HyperEdge edgeF = nd.chooseAnCanHitUncovEdge();

        // not hit edgeF
        ApproxCoverNode child1 = nd.getUnhitChild(edgeF);
        if (child1 != null) {
            walkDown(newCoverNodes, child1);
        }

        // hit edgeF
        LongBitSet verticesToAdd = nd.nontrivialCand.getAnd(edgeF.vertices);
        LongBitSet child2Cand = nd.nontrivialCand.getAndNot(verticesToAdd);
        int addV=0;
        for (addV = verticesToAdd.nextSetBit(0); addV >= 0; addV = verticesToAdd.nextSetBit(addV + 1)) {
            ApproxCoverNode child2 = nd.getHitChild(addV, child2Cand);
            counterOps+=1;
            if (child2 != null) {    // child2 is null if it's not minimal
                walkDown(newCoverNodes, child2);
                child2Cand.set(addV);
            }
        }
        
    }

    public List<LongBitSet> getNontrivialMinCovers() {
        return ApproxCoverNodes.stream().map(ApproxCoverNode::getVertices).collect(Collectors.toList());
    }

}
