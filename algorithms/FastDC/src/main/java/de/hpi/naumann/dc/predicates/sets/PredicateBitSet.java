package de.hpi.naumann.dc.predicates.sets;

import ch.javasoft.bitset.BitSetFactory;
import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet.LongBitSetFactory;
import de.hpi.naumann.dc.helpers.IndexProvider;
import de.hpi.naumann.dc.predicates.Predicate;

public class PredicateBitSet implements IPredicateSetBinary {

	private IBitSet bitset;

	public PredicateBitSet() {
		this.bitset = bf.create();
	}

	public PredicateBitSet(IBitSet bitset) {
		this.bitset = bitset.clone();
	}

	public PredicateBitSet(IPredicateSetBinary pS) {
		this.bitset = pS.getBitset().clone();
	}

	public PredicateBitSet(Predicate p) {
		this.bitset = getBitSet(p);
	}

	public PredicateBitSet(IPredicateSet set) {
		this.bitset = indexProvider.getBitSet(set);
	}

	@Override
	public void add(Predicate predicate) {
		this.bitset.set(indexProvider.getIndex(predicate));
	}

	@Override
	public void remove(Predicate predicate) {
		this.bitset.clear(indexProvider.getIndex(predicate));
	}

	@Override
	public boolean containsPredicate(Predicate predicate) {
		return this.bitset.get(indexProvider.getIndex(predicate));
	}

	@Override
	public boolean isSubsetOf(IPredicateSetBinary superset) {
		return this.bitset.isSubSetOf(superset.getBitset());
	}

	@Override
	public int getIntersectionCount(IPredicateSet iPredicateSet) {
		
		return iPredicateSet.getIPredicateSetBinary().getBitset().getAndCardinality(this.getBitset());
	
		//return 0;
	}

	@Override
	public IBitSet getBitset() {
		return bitset;
	}

	public IPredicateSet convert() {
		IPredicateSet converted = PredicateSetFactory.create();
		for (int l = bitset.nextSetBit(0); l >= 0; l = bitset.nextSetBit(l + 1)) {
			converted.add(indexProvider.getObject(l));
		}
		return converted;
	}

	public static IndexProvider<Predicate> indexProvider = new IndexProvider<>();
	private static BitSetFactory bf = new LongBitSetFactory();

	static public Predicate getPredicate(int index) {
		return indexProvider.getObject(index);
	}

	static public IBitSet getBitSet(Predicate p) {
		int index = indexProvider.getIndex(p);
		IBitSet bitset = bf.create();
		bitset.set(index);
		return bitset;
	}

	@Override
	public void addAll(IPredicateSetBinary iPredicateSet2) {
		this.bitset.or(iPredicateSet2.getBitset());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bitset == null) ? 0 : bitset.hashCode());
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
		PredicateBitSet other = (PredicateBitSet) obj;
		if (bitset == null) {
			if (other.bitset != null)
				return false;
		} else if (!bitset.equals(other.bitset))
			return false;
		return true;
	}

	public static int getIndex(Predicate add) {
		return indexProvider.getIndex(add);
	}

}
