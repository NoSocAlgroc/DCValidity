package de.metanome.algorithms.dcfinder.evidenceset.builders;

import de.metanome.algorithms.dcfinder.evidenceset.IEvidenceSet;
import de.metanome.algorithms.dcfinder.evidenceset.TroveEvidenceSet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.predicates.PredicateBuilder;
import de.metanome.algorithms.dcfinder.predicates.sets.PredicateSet;
import edu.stanford.nlp.util.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SplitReconstructEvidenceSetBuilder {

    private Input input;
    private PredicateBuilder predicateBuilder;
    private long fullTuplePairsRange;
    private long chunkLength;
    private int bufferLength;

    private List<Interval<Long>> chunckIntervals;

    IEvidenceSet fullEvidenceSet;

    public SplitReconstructEvidenceSetBuilder(Input input, PredicateBuilder predicates, long chunkLength,
                                              int bufferLength) {
        this.input = input;
        this.predicateBuilder = predicates;
        this.chunkLength = chunkLength;
        this.bufferLength = bufferLength;

        fullTuplePairsRange = (long) input.getLineCount() * (long) input.getLineCount();

        chunckIntervals = new ArrayList<>();
        for (long i = 0; i < fullTuplePairsRange; i += chunkLength) {
            chunckIntervals.add(Interval.toInterval(i, i + chunkLength, Interval.INTERVAL_OPEN_END));
        }
        chunckIntervals.get(chunckIntervals.size() - 1).setSecond(fullTuplePairsRange);

        System.out.println("  [Evi Builder] First level chunks: " + chunckIntervals.size());
        BufferedEvidenceSetBuilder.configure(bufferLength, input.getLineCount(), predicateBuilder);
    }

    public TroveEvidenceSet buildEvidenceSet() {
        int processors = Runtime.getRuntime().availableProcessors();
        List<List<Interval<Long>>> listOfchunckIntervals = new ArrayList<>();
        int numPartialEvidenceSets = processors * 4; // merge partial eviset from time to time based on this number
        for (int i = 0; i < chunckIntervals.size(); i += numPartialEvidenceSets) {
            if (i + numPartialEvidenceSets >= chunckIntervals.size()) {
                listOfchunckIntervals.add(chunckIntervals.subList(i, chunckIntervals.size()));
                break;
            }
            listOfchunckIntervals.add(chunckIntervals.subList(i, i + numPartialEvidenceSets));
        }

        fullEvidenceSet = new TroveEvidenceSet();
        System.out.println(" [Evi Builder] Building the Evidence Set...");

        for (List<Interval<Long>> chunkForThreads : listOfchunckIntervals) {
            Queue<IEvidenceSet> pEvidenceSets = new ConcurrentLinkedDeque<>();
            chunkForThreads.stream().parallel().forEach(interval -> {
                var partialEviSetBuilder = new BufferedEvidenceSetBuilder(interval, (interval.second - interval.first));
                IEvidenceSet partialEvidenceSet = partialEviSetBuilder.buildPartialEvidenceSet();
                pEvidenceSets.add(partialEvidenceSet);
            });
            for (IEvidenceSet evis : pEvidenceSets) {
                for (PredicateSet ps : evis) {
                    fullEvidenceSet.add(ps, evis.getCount(ps));
                }
            }

        }

        fullEvidenceSet.adjustCount(BufferedEvidenceSetBuilder.getCardinalityMask(), -input.getLineCount());
        if (fullEvidenceSet.getCount(BufferedEvidenceSetBuilder.getCardinalityMask()) == 0L)
            fullEvidenceSet.getSetOfPredicateSets().remove(BufferedEvidenceSetBuilder.getCardinalityMask());

        return (TroveEvidenceSet) fullEvidenceSet;
    }

    public IEvidenceSet getFullEvidenceSet() {
        return fullEvidenceSet;
    }

}
