package de.hpi.naumann.dc.evidenceset;

import java.util.Iterator;
import java.util.Set;

import com.google.common.util.concurrent.AtomicLongMap;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;
import de.hpi.naumann.dc.predicates.sets.PredicateBitSet;

public class AtomicLongEvidenceSet implements IEvidenceSet {

	AtomicLongMap<IPredicateSetBinary> evidences = AtomicLongMap.create();

	private long sizeEvi = 0;

	@Override
	public boolean add(IPredicateSetBinary predicateSet) {
		return evidences.incrementAndGet(predicateSet) == 1;

	}

	@Override
	public boolean add(IPredicateSetBinary predicateSet, long count) {
		return evidences.addAndGet(predicateSet, count) == count;

	}

	@Override
	public long getCount(IPredicateSetBinary predicateSet) {
		return evidences.get(predicateSet);
	}

	@Override
	public boolean contains(IPredicateSetBinary i) {
		return evidences.containsKey(i);
	}

	@Override
	public long[] getKEvidencesCounts(DenialConstraint dc) {
		long[] counts = new long[dc.getPredicateCount()];
		evidences.asMap().forEach((predicateset, tpCount) -> {
			int k = predicateset.getIntersectionCount(dc.getPredicateSet());
			counts[k] += tpCount;
		});

		return counts;
	}

	@Override
	public int getSubsetCount(IPredicateSetBinary dcPredicates) {
		int error = 0;
		for (IPredicateSetBinary p : this) {
			if (dcPredicates.isSubsetOf(p))
				++error;
		}
		return error;
	}

	@Override
	public Iterator<IPredicateSetBinary> iterator() {
		return evidences.asMap().keySet().iterator();
	}

	@Override
	public Set<IPredicateSetBinary> getSetOfPredicateSets() {
		return evidences.asMap().keySet();
	}

	@Override
	public int size() {
		return evidences.size();
	}

	@Override
	public boolean isEmpty() {
		return evidences.isEmpty();
	}

	public long getTotalCountEvi() {
		if (sizeEvi == 0) {
			sizeEvi = evidences.sum();
		}
		return sizeEvi;
	}

}
