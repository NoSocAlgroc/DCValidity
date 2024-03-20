package de.hpi.naumann.dc.evidenceset.build;

import java.util.Collection;

import de.hpi.naumann.dc.evidenceset.TroveEvidenceSet;
import de.hpi.naumann.dc.evidenceset.IEvidenceSet;
import de.hpi.naumann.dc.input.ColumnPair;
import de.hpi.naumann.dc.input.Input;
import de.hpi.naumann.dc.predicates.PredicateBuilder;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;
import de.hpi.naumann.dc.predicates.sets.PredicateSetFactory;

public class RowEvidenceSetBuilder extends EvidenceSetBuilder {

	public RowEvidenceSetBuilder(PredicateBuilder predicates2) {
		super(predicates2);
	}

	@Override
	public IEvidenceSet buildEvidenceSet(Input input) {
		int[][] input2s = input.getInts();

		Collection<ColumnPair> pairs = predicates.getColumnPairs();
		createSets(pairs);

		IEvidenceSet evidenceSet = new TroveEvidenceSet();
		for (int i = 0; i < input.getLineCount(); ++i) {
			if (i % 10000 == 0)
				System.out.println(i);
			int[] row1 = input2s[i];
			IPredicateSetBinary staticSet = getStatic(pairs, row1);
			for (int j = 0; j < input.getLineCount(); ++j) {
				// TODO: need to check this still for same subindex?
				int[] row2 = input2s[j];
				if (i != j) {
					IPredicateSetBinary set = getPredicateSet(staticSet, pairs, row1, row2);
					evidenceSet.add(set);
				}
			}
		}
		return evidenceSet;

	}

	protected IPredicateSetBinary getPredicateSet(IPredicateSetBinary staticSet, Collection<ColumnPair> pairs, int[] row1,
			int[] row2) {
		IPredicateSetBinary set = PredicateSetFactory.create2(staticSet);
		// which predicates are satisfied by these two lines?
		for (ColumnPair p : pairs) {
			IPredicateSetBinary[] list = map.get(p);
			if (p.isJoinable()) {
				if (equals(row1, row2, p))
					set.addAll(list[0]);
				else
					set.addAll(list[1]);
			}
			if (p.isComparable()) {
				int compare = compare(row1, row2, p);
				if (compare < 0) {
					set.addAll(list[4]);
				} else if (compare == 0) {
					set.addAll(list[5]);
				} else {
					set.addAll(list[6]);
				}

			}

		}
		return set;
	}

	protected IPredicateSetBinary getStatic(Collection<ColumnPair> pairs, int[] row1) {
		IPredicateSetBinary set = PredicateSetFactory.create2();
		// which predicates are satisfied by these two lines?
		for (ColumnPair p : pairs) {
			if (p.getC1().equals(p.getC2()))
				continue;

			IPredicateSetBinary[] list = map.get(p);
			if (p.isJoinable()) {
				if (equals(row1, row1, p))
					set.addAll(list[2]);
				else
					set.addAll(list[3]);
			}
			if (p.isComparable()) {
				int compare2 = compare(row1, row1, p);
				if (compare2 < 0) {
					set.addAll(list[7]);
				} else if (compare2 == 0) {
					set.addAll(list[8]);
				} else {
					set.addAll(list[9]);
				}
			}

		}
		return set;
	}

	private int compare(int[] row1, int[] row2, ColumnPair p) {
		return Integer.compare(row1[p.getC1().getIndex()], row2[p.getC2().getIndex()]);
	}

	private boolean equals(int[] row1, int[] row2, ColumnPair p) {
		return row1[p.getC1().getIndex()] == row2[p.getC2().getIndex()];
	}

}
