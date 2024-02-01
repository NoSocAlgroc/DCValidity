package approxcover;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.evidenceset.IEvidenceSet;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;

import java.util.ArrayList;
import java.util.List;

public class ApproxDCBuilder {

    private final long minCoverCount;   // min number of evidences that an AC should cover
    private final PredicateBuilder predicateBuilder;
    private final ApproxCoverTraverser traverser;

    private DenialConstraintSet DCs;

    public ApproxDCBuilder(long _minCoverCount, PredicateBuilder builder) {
        minCoverCount = _minCoverCount;
        traverser = new ApproxCoverTraverser(builder.getPredicates().size(), minCoverCount, builder.getMutexMap());
        predicateBuilder = builder;
    }

    public DenialConstraintSet buildApproxDCs(IEvidenceSet evidenceSet) {
        System.out.println(" [ADC] Searching min covers...");

        List<HyperEdge> edges = new ArrayList<>();
        for (PredicateSet ps : evidenceSet)
            edges.add(new HyperEdge((LongBitSet) ps.getBitset(), evidenceSet.getCount(ps)));

        List<LongBitSet> rawMinCovers = traverser.initiate(edges);
        return DCs = buildMinDCs(rawMinCovers);
    }

    private DenialConstraintSet buildMinDCs(List<LongBitSet> rawMinCovers) {
        System.out.println(" [ADC] Min cover size: " + rawMinCovers.size());

        DenialConstraintSet dcs = new DenialConstraintSet(predicateBuilder, rawMinCovers);
        System.out.println(" [ADC] Total DC size: " + dcs.size());

        dcs.minimize();
        System.out.println(" [ADC] Min DC size : " + dcs.size());

        return dcs;
    }

    public DenialConstraintSet getDCs() {
        return DCs;
    }

}
