package de.hpi.naumann.dc.evidenceset;

import java.util.Iterator;
import java.util.Set;

import de.hpi.naumann.dc.denialcontraints.DenialConstraint;
import de.hpi.naumann.dc.predicates.sets.IPredicateSetBinary;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TObjectLongProcedure;

public class TroveEvidenceSet implements IEvidenceSet {

	TObjectLongMap<IPredicateSetBinary> sets = new TObjectLongHashMap<>();

	public void forEachEntry(TObjectLongProcedure<IPredicateSetBinary> fun) {
		sets.forEachEntry(fun);
	}
	
	@Override
	public boolean add(IPredicateSetBinary predicateSet) {
		return this.add(predicateSet, 1);
	}

	@Override
	public boolean add(IPredicateSetBinary create, long count) {
		return sets.adjustOrPutValue(create, count, count) == count;
	}

	@Override
	public long getCount(IPredicateSetBinary predicateSet) {
		return sets.get(predicateSet);
	}

	@Override
	public boolean contains(IPredicateSetBinary i) {
		return sets.containsKey(i);
	}

	@Override
	public long[] getKEvidencesCounts(DenialConstraint dc) {
		long[] counts = new long[dc.getPredicateCount()];
		sets.forEachEntry(new TObjectLongProcedure<IPredicateSetBinary>() {

			@Override
			public boolean execute(IPredicateSetBinary a, long b) {
				int k = a.getIntersectionCount(dc.getPredicateSet());

				counts[k] += b;
				return true;
			}
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
		return sets.keySet().iterator();
	}

	@Override
	public Set<IPredicateSetBinary> getSetOfPredicateSets() {
		return sets.keySet();
	}

	@Override
	public int size() {
		return sets.size();
	}

	@Override
	public boolean isEmpty() {
		return sets.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sets == null) ? 0 : sets.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TroveEvidenceSet other = (TroveEvidenceSet) obj;
		if (sets == null) {
			if (other.sets != null)
				return false;
		} else if (!sets.equals(other.sets))
			return false;
		return true;
	}
	
	@Override
	public long getTotalCountEvi() {
		//TODO
		return 0;
	}

}
