package de.hpi.naumann.dc.evidenceset.build;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.hpi.naumann.dc.evidenceset.AtomicLongEvidenceSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.input.ColumnPair;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;

public class ParallelEvidenceSetBuilder extends RowEvidenceSetBuilder {

	public ParallelEvidenceSetBuilder(PredicateBuilder predicates2) {
		super(predicates2);
	}

	@Override
	public IEvidenceSet buildEvidenceSet(Input input) {
		int[][] input2s = input.getInts();

		Collection<ColumnPair> pairs = predicates.getColumnPairs();
		createSets(pairs);

		IEvidenceSet evidenceSet = new AtomicLongEvidenceSet();

		ExecutorService e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
		for (int i = 0; i < input.getLineCount(); ++i) {
			final int iF = i;
			e.execute(() -> {
				int[] row1 = input2s[iF];
				IPredicateSetBinary staticSet = getStatic(pairs, row1);
				for (int j = 0; j < input.getLineCount(); ++j) {
					// TODO: need to check this still for same subindex?
					int[] row2 = input2s[j];
					if (iF != j) {
						IPredicateSetBinary set = getPredicateSet(staticSet, pairs, row1, row2);
						evidenceSet.add(set);
					}
				}
			});
		}
		e.shutdown();
		try {
			e.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return evidenceSet;
	}

}
