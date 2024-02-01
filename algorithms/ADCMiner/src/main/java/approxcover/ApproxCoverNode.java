package approxcover;

import ch.javasoft.bitset.LongBitSet;

import java.util.*;

class ApproxCoverNode {

    private static LongBitSet[] mutexMap;
    private static LongBitSet cardinalityMask;

    static void configure(int nEle, LongBitSet[] mutex) {
        mutexMap = mutex;

        cardinalityMask = new LongBitSet(nEle);
        for (int i = 0; i < nEle; i++)
            cardinalityMask.set(i);
    }


    final long target;   // require covering how many more edges to be an approx cover
    final LongBitSet vertices;

    LongBitSet nontrivialCand;
    final List<HyperEdge> canHitUncov;
    final ArrayList<HyperEdge>[] crit;

    /* only for initiating the root */
    ApproxCoverNode(long target, List<HyperEdge> edges) {
        this.target = target;
        vertices = new LongBitSet();
        nontrivialCand = cardinalityMask.clone();

        //canHitUncov = new ArrayList<>(edges);
        canHitUncov = new ArrayList<>(edges);
        crit = new ArrayList[0];
    }

    /* invoked by a parent node to create an unhit child node */
    private ApproxCoverNode(long _target, LongBitSet _vertices, LongBitSet _cand, List<HyperEdge> _canHitUncov, ArrayList<HyperEdge>[] _crit) {
        target = _target;
        vertices = _vertices;
        nontrivialCand = _cand;
        canHitUncov = _canHitUncov;
        crit = _crit;
    }


    HyperEdge chooseAnCanHitUncovEdge() {
        Comparator<HyperEdge> cmp = Comparator.comparing(e -> LongBitSet.getAndCardinality(nontrivialCand, e.vertices));
        //return Collections.max(canHitUncov, cmp);
        return Collections.min(canHitUncov, cmp);
    }

    /* return null if none of the child and its children can be a cover */
    ApproxCoverNode getUnhitChild(HyperEdge unhitEdge) {
        LongBitSet childCand = nontrivialCand.getAndNot(unhitEdge.vertices);
        List<HyperEdge> childCanHitUncov = new ArrayList<>();

        long bestTarget = target;
        for (HyperEdge e : canHitUncov) {
            if (childCand.intersectWith(e.vertices)) {
                childCanHitUncov.add(e);
                bestTarget -= e.count;
            }
        }

        return bestTarget > 0 ? null :
                new ApproxCoverNode(target, vertices.clone(), childCand, childCanHitUncov, crit.clone());
    }

    /* return null if child is not minimal */
    ApproxCoverNode getHitChild(int addV, LongBitSet childCand) {
        ArrayList<HyperEdge>[] childCrit = new ArrayList[Math.max(addV + 1, crit.length)];
        for (int i = vertices.nextSetBit(0); i >= 0; i = vertices.nextSetBit(i + 1)) {
            childCrit[i] = new ArrayList<>();
            for (HyperEdge edge : crit[i])
                if (!edge.vertices.get(addV))
                    childCrit[i].add(edge);
            if (childCrit[i].isEmpty())
                return null;
        }

        LongBitSet childVertices = vertices.clone();
        childVertices.set(addV);

        LongBitSet childNontrivialCand = childCand.getAndNot(mutexMap[addV]);

        long childTarget = target;
        List<HyperEdge> childCanHitUncov = new ArrayList<>();
        childCrit[addV] = new ArrayList<>();
        for (HyperEdge edge : canHitUncov) {
            if (edge.vertices.get(addV)) {
                childCrit[addV].add(edge);
                childTarget -= edge.count;
            } else
                childCanHitUncov.add(edge);
        }

        return new ApproxCoverNode(childTarget, childVertices, childNontrivialCand, childCanHitUncov, childCrit);
    }


    void resetCand() {
        LongBitSet mutex = new LongBitSet();
        for (int v = vertices.nextSetBit(0); v >= 0; v = vertices.nextSetBit(v + 1))
            mutex.or(mutexMap[v]);

        nontrivialCand = LongBitSet.getAndNot(cardinalityMask, vertices);
        nontrivialCand.andNot(mutex);
    }

    void clearCrit() {
        for (List<HyperEdge> c : crit)
            if (c != null)
                c.clear();
    }

    boolean isApproxCover() {
        return target <= 0;
    }

    boolean isMinimal() {
        for (int v = vertices.nextSetBit(0); v >= 0; v = vertices.nextSetBit(v + 1)) {
            long parentTarget = target;
            for (HyperEdge e : crit[v])
                parentTarget += e.count;
            if (parentTarget <= 0)
                return false;
        }
        return true;
    }

    void addTo(Collection<ApproxCoverNode> nodes) {
//        nontrivialCand = null;
//        canHitUncov.clear();
//        clearCrit();
        nodes.add(this);
//        if(nodes.size()%1000==0) System.out.println("  [ADC] curr DC size: "+ nodes.size());
    }


    /**
     * @return the position of critical element wrt edge,
     * or -1 if edge is not covered,
     * or -2 if edge is covered by more than one element
     */
    private int getCritElement(LongBitSet edge) {
        return LongBitSet.getOnlySetBitOfAnd(edge, vertices);
    }

    LongBitSet getVertices() {
        return vertices.clone();
    }

    LongBitSet getNontrivialCand() {
        return nontrivialCand.clone();
    }

}
