package de.hpi.naumann.dc.evidenceset;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;

public class HashEvidenceSet implements IEvidenceSet {

	Set<IPredicateSetBinary> evidences = new HashSet<>();
	//Set<IPredicateSetBinary> evidences = new TreeSet<>();

	@Override
	public boolean add(IPredicateSetBinary predicateSet) {
		return evidences.add(predicateSet);

	}

	@Override
	public boolean add(IPredicateSetBinary predicateSet, long count) {
		return evidences.add(predicateSet);

	}

	@Override
	public long getCount(IPredicateSetBinary predicateSet) {
		return evidences.contains(predicateSet) ? 1 : 0;
	}

	@Override
	public boolean contains(IPredicateSetBinary i) {
		return evidences.contains(i);
	}

	@Override
	public long[] getKEvidencesCounts(DenialConstraint dc) {
		long[] counts = new long[dc.getPredicateCount()];
		evidences.forEach((predicateset) -> {
			// TODO... need to check on that
			int k = predicateset.getIntersectionCount(dc.getPredicateSet());
			

			counts[k] += 1;
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
		return evidences.iterator();
	}

	@Override
	public Set<IPredicateSetBinary> getSetOfPredicateSets() {
		return evidences;
	}

	@Override
	public int size() {
		return evidences.size();
	}

	@Override
	public boolean isEmpty() {
		return evidences.isEmpty();
	}

	@Override
	public long getTotalCountEvi() {
		return 0;
	}

}
